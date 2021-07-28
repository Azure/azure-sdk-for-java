// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.implementation.intellij.IntelliJKdbxDatabase;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4jextensions.persistence.mac.KeyChainAccessor;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Crypt32Util;

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
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;

/**
 * This class accesses IntelliJ Azure Tools credentials cache via JNA.
 */
public class IntelliJCacheAccessor {
    private final ClientLogger logger = new ClientLogger(IntelliJCacheAccessor.class);
    private String keePassDatabasePath;
    private static final String INTELLIJ_CREDENTIAL_NOT_AVAILABLE_ERROR = "IntelliJ Authentication not available."
              + " Please log in with Azure Tools for IntelliJ plugin in the IDE.";
    private static final byte[] CRYPTO_KEY = new byte[] {0x50, 0x72, 0x6f, 0x78, 0x79, 0x20, 0x43, 0x6f, 0x6e, 0x66,
        0x69, 0x67, 0x20, 0x53, 0x65, 0x63};


    /**
     * Creates an instance of {@link IntelliJCacheAccessor}
     *
     * @param keePassDatabasePath the KeePass database path.
     */
    public IntelliJCacheAccessor(String keePassDatabasePath) {
        this.keePassDatabasePath = keePassDatabasePath;
    }

    private List<String> getAzureToolsforIntelliJPluginConfigPaths() {
        return Arrays.asList(Paths.get(System.getProperty("user.home"), "AzureToolsForIntelliJ").toString(),
            Paths.get(System.getProperty("user.home"), ".AzureToolsForIntelliJ").toString());
    }

    /**
     * Get the Device Code credential details of Azure Tools plugin in the IntelliJ IDE.
     *
     * @return the {@link JsonNode} holding the authentication details.
     * @throws IOException
     */
    public JsonNode getDeviceCodeCredentials() throws IOException {
        if (Platform.isMac()) {
            KeyChainAccessor accessor = new KeyChainAccessor(null, "ADAuthManager",
                "cachedAuthResult");

            String jsonCred  = new String(accessor.read(), Charset.forName("UTF-8"));

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonCred);

        } else if (Platform.isLinux()) {
            LinuxKeyRingAccessor accessor = new LinuxKeyRingAccessor(
                "com.intellij.credentialStore.Credential",
                "service", "ADAuthManager",
                "account", "cachedAuthResult");

            String jsonCred  = new String(accessor.read(), Charset.forName("UTF-8"));
            if (jsonCred.startsWith("cachedAuthResult@")) {
                jsonCred = jsonCred.replaceFirst("cachedAuthResult@", "");
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonCred);

        } else if (Platform.isWindows()) {
            return getCredentialFromKdbx();
        } else {
            throw logger.logExceptionAsError(new RuntimeException(String.format("OS %s Platform not supported.",
                    Platform.getOSType())));
        }
    }

    /**
     * Get the Service Principal credential details of Azure Tools plugin in the IntelliJ IDE.
     *
     * @param credFilePath the file path holding authentication details
     * @return the {@link HashMap} holding auth details.
     * @throws IOException if an error is countered while reading the credential file.
     */
    public Map<String, String> getIntellijServicePrincipalDetails(String credFilePath) throws IOException {
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
        if (CoreUtils.isNullOrEmpty(keePassDatabasePath)) {
            throw logger.logExceptionAsError(
                    new CredentialUnavailableException("The KeePass database path is either empty or not configured."
                           + " Please configure it on the builder. It is required to use "
                           + "IntelliJ credential on the windows platform."));
        }
        String extractedpwd = getKdbxPassword();

        SecretKeySpec key = new SecretKeySpec(CRYPTO_KEY, "AES");
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
            password = new String(decrypted, Charset.forName("UTF-8"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw logger.logExceptionAsError(new RuntimeException("Unable to access cache.", e));
        }

        try {
            InputStream inputStream = new FileInputStream(new File(keePassDatabasePath));
            IntelliJKdbxDatabase kdbxDatabase = IntelliJKdbxDatabase.parse(inputStream, password);

            String jsonToken = kdbxDatabase.getDatabaseEntryValue("ADAuthManager");
            if (CoreUtils.isNullOrEmpty(jsonToken)) {
                throw logger.logExceptionAsError(new CredentialUnavailableException("No credentials found in the cache."
                        + " Please login with IntelliJ Azure Tools plugin in the IDE."));
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(jsonToken);
        } catch (Exception e) {
            throw logger.logExceptionAsError(new RuntimeException("Failed to read KeePass database.", e));
        }
    }

    private String getKdbxPassword() throws IOException {
        String passwordFilePath = new File(keePassDatabasePath).getParent() + File.separator + "c.pwd";
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

    /**
     * Get the auth host of the specified {@code azureEnvironment}.
     * @param azureEnvironment the specified Azure Environment
     * @return the auth host.
     */
    public String getAzureAuthHost(String azureEnvironment) {

        switch (azureEnvironment) {
            case "GLOBAL":
                return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
            case "CHINA":
                return AzureAuthorityHosts.AZURE_CHINA;
            case "GERMAN":
                return AzureAuthorityHosts.AZURE_GERMANY;
            case "US_GOVERNMENT":
                return AzureAuthorityHosts.AZURE_GOVERNMENT;
            default:
                return AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        }
    }


    /**
     * Parse the auth details of the specified file.
     * @param file the file input;
     * @return the parsed {@link IntelliJAuthMethodDetails} from the file input.
     * @throws IOException when invalid file path is specified.
     */
    public IntelliJAuthMethodDetails parseAuthMethodDetails(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper
            .readValue(file, IntelliJAuthMethodDetails.class);
    }

    /**
     * Get the current authentication method details of Azure Tools plugin in IntelliJ IDE.
     *
     * @return the {@link IntelliJAuthMethodDetails}
     * @throws IOException if an error is encountered while reading the auth details file.
     */
    public IntelliJAuthMethodDetails getAuthDetailsIfAvailable() throws IOException {
        File authFile = null;
        for (String metadataPath : getAzureToolsforIntelliJPluginConfigPaths()) {
            String authMethodDetailsPath =
                Paths.get(metadataPath, "AuthMethodDetails.json").toString();
            authFile = new File(authMethodDetailsPath);
            if (authFile.exists()) {
                break;
            }
        }
        if (authFile == null || !authFile.exists()) {
            throw logger.logExceptionAsError(
                    new CredentialUnavailableException(INTELLIJ_CREDENTIAL_NOT_AVAILABLE_ERROR));
        }

        IntelliJAuthMethodDetails authMethodDetails = parseAuthMethodDetails(authFile);

        String authType = authMethodDetails.getAuthMethod();
        if (CoreUtils.isNullOrEmpty(authType)) {
            throw logger.logExceptionAsError(
                    new CredentialUnavailableException(INTELLIJ_CREDENTIAL_NOT_AVAILABLE_ERROR));
        }
        if (authType.equalsIgnoreCase("SP")) {
            if (CoreUtils.isNullOrEmpty(authMethodDetails.getCredFilePath())) {
                throw logger.logExceptionAsError(
                        new CredentialUnavailableException(INTELLIJ_CREDENTIAL_NOT_AVAILABLE_ERROR));
            }
        } else if (authType.equalsIgnoreCase("DC")) {
            if (CoreUtils.isNullOrEmpty(authMethodDetails.getAccountEmail())) {
                throw logger.logExceptionAsError(
                        new CredentialUnavailableException(INTELLIJ_CREDENTIAL_NOT_AVAILABLE_ERROR));
            }
        }
        return authMethodDetails;
    }
}
