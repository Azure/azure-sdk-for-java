// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4jextensions.persistence.mac.KeyChainAccessor;
import com.sun.jna.platform.win32.Crypt32Util;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * This class accesses IntelliJ Azure Tools credentials cache via JNA.
 */
public class IntelliJCacheAccessor {
    private final ClientLogger logger = new ClientLogger(IntelliJCacheAccessor.class);
    private String keepPassDatabasePath;

    /**
     * Creates an instance of {@link IntelliJCacheAccessor}
     *
     * @param keepPassDatabasePath the keep pass database path.
     */
    public IntelliJCacheAccessor(String keepPassDatabasePath) {
        this.keepPassDatabasePath = keepPassDatabasePath;
    }

    private String getAzureToolsforIntelliJPluginConfigPath() {
        return System.getProperty("user.home") + String.format("%sAzureToolsForIntelliJ", File.separator);
    }

    /**
     * Get the current authentication method details of Azure Tools plugin in IntelliJ IDE.
     *
     * @return the {@link IntelliJAuthMethodDetails}
     * @throws IOException if an error is encountered while reading the auth details file.
     */
    public IntelliJAuthMethodDetails getIntelliJAuthDetails() throws IOException {
        StringBuilder authMethodDetailsPath = new StringBuilder(getAzureToolsforIntelliJPluginConfigPath());
        authMethodDetailsPath.append(String.format("%sAuthMethodDetails.json", File.separator));
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(authMethodDetailsPath.toString());
        return objectMapper.readValue(file, IntelliJAuthMethodDetails.class);
    }

    /**
     * Get the Device Code credential details of Azure Tools plugin in the IntelliJ IDE.
     *
     * @return the {@link JsonNode} holding the authentication details.
     * @throws IOException
     */
    public JsonNode getDeviceCodeCredentials() throws IOException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (os.contains("mac")) {
            KeyChainAccessor accessor = new KeyChainAccessor(null, "ADAuthManager",
                "cachedAuthResult");

            String jsonCred  = new String(accessor.read());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonCred);

        } else if (os.contains("nix") || os.contains("nux")) {
            LinuxKeyRingAccessor accessor = new LinuxKeyRingAccessor(
                "com.intellij.credentialStore.Credential",
                "service", "ADAuthManager",
                "account", "cachedAuthResult");

            String jsonCred  = new String(accessor.read());
            if (jsonCred.startsWith("cachedAuthResult@")) {
                jsonCred = jsonCred.replaceFirst("cachedAuthResult@", "");
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonCred);

        } else if (os.contains("win")) {
            return getCredentialFromKdbx();
        } else {
            throw logger.logExceptionAsError(new RuntimeException(String.format("OS %s Platform not supported.", os)));
        }
    }

    /**
     * Get the Service Principal credential details of Azure Tools plugin in the IntelliJ IDE.
     *
     * @param credFilePath the file path holding authentication details
     * @return the {@link HashMap} holding auth details.
     * @throws IOException if an error is countered while reading the credential file.
     */
    public HashMap<String, String> getIntellijServicePrincipalDetails(String credFilePath) throws IOException {
        BufferedReader reader = null;
        HashMap<String, String> servicePrincipalDetails = new HashMap<>(8);
        try {
            reader = new BufferedReader(new FileReader(credFilePath));
            String line = reader.readLine();
            while (line != null) {
                String[] split = line.split("=");
                split[1] = split[1].replace("\\", "");
                servicePrincipalDetails.put(split[0], split[1]);
                // read next line
                line = reader.readLine();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return servicePrincipalDetails;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private JsonNode getCredentialFromKdbx() throws IOException {
        String extractedpwd = getKdbxPassword();

        byte[] cryptoKey = new byte[]{0x50, 0x72, 0x6f, 0x78, 0x79, 0x20, 0x43, 0x6f, 0x6e, 0x66,
            0x69, 0x67, 0x20, 0x53, 0x65, 0x63};

        SecretKeySpec key = new SecretKeySpec(cryptoKey, "AES");
        String password = "";

        byte[] dataToDecrypt = Crypt32Util.cryptUnprotectData(Base64.getDecoder().decode(extractedpwd));

        ByteBuffer decryptBuffer = ByteBuffer.wrap(dataToDecrypt);
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            int ivLen = decryptBuffer.getInt();
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(dataToDecrypt, decryptBuffer.position(), ivLen));
            int dataOffset = decryptBuffer.position() + ivLen;
            byte[] decrypted = cipher.doFinal(dataToDecrypt, dataOffset, dataToDecrypt.length - dataOffset);
            password = new String(decrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw logger.logExceptionAsError(new RuntimeException("Unable to access cache.", e));
        }

        try {
            KdbxCreds creds = new KdbxCreds(password.getBytes());
            InputStream inputStream = new FileInputStream(new File(keepPassDatabasePath));
            Database database = SimpleDatabase.load(creds, inputStream);

            List<Entry> entries = database.findEntries("ADAuthManager");
            if (entries.size() == 0) {
                throw logger.logExceptionAsError(new RuntimeException("No credentials found in the cache."
                        + " Please login with IntelliJ Azure Tools plugin in the IDE."));
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(entries.get(0).getPassword());
        } catch (Exception e) {
            throw logger.logExceptionAsError(new RuntimeException("Failed to read keep pass database.", e));
        }
    }

    private String getKdbxPassword() throws IOException {
        if (CoreUtils.isNullOrEmpty(keepPassDatabasePath)) {
            throw new IllegalArgumentException("The windows keep pass database path is either empty or not configured."
                                                   + " Please configure it on the IntelliJ Credential builder.");
        }
        String passwordFilePath = new File(keepPassDatabasePath).getParent() + "\\" + "c.pwd";
        String extractedpwd = "";

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(passwordFilePath));
            String line = reader.readLine();

            while (line != null) {
                if (line.contains("value")) {
                    String[] tokens = line.split(" ");
                    if (tokens.length == 3) {
                        extractedpwd = tokens[2];
                        break;
                    } else {
                        throw logger.logExceptionAsError(new RuntimeException("Password not found in the file."));
                    }
                }
                line = reader.readLine();
            }
            reader.close();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return extractedpwd;
    }
}
