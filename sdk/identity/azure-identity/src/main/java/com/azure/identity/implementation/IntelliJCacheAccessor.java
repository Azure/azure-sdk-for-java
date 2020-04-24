package com.azure.identity.implementation;

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
import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class IntelliJCacheAccessor {
    private String keepPassDatabasePath;

    public IntelliJCacheAccessor(String keepPassDatabasePath) {
        this.keepPassDatabasePath = keepPassDatabasePath;
    }

    private String getAzureToolsforIntelliJPluginConfigPath(){
        return System.getProperty("user.home") + String.format("%sAzureToolsForIntelliJ", File.separator);
    }

    public IntelliJAuthMethodDetails getIntelliJAuthDetails() throws IOException {
        StringBuilder authMethodDetailsPath = new StringBuilder(getAzureToolsforIntelliJPluginConfigPath());
        authMethodDetailsPath.append(String.format("%sAuthMethodDetails.json", File.separator));
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(authMethodDetailsPath.toString());
        return objectMapper.readValue(file, IntelliJAuthMethodDetails.class);
    }

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
            throw new RuntimeException(String.format("OS %s Platform not supported.", os));
        }
    }

    public HashMap<String, String> getIntellijServicePrincipalDetails(String credFilePath) throws IOException {
        BufferedReader reader = null;
        HashMap<String, String> servicePrincipalDetails = new HashMap<>(8);
        try {
            reader = new BufferedReader(new FileReader(credFilePath));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
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

    public JsonNode getCredentialFromKdbx() throws IOException {
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
            throw new RuntimeException("Unable to access cache.", e);
        }

        try {
            KdbxCreds creds = new KdbxCreds(password.getBytes());
            InputStream inputStream = new FileInputStream(new File(keepPassDatabasePath));
            Database database = SimpleDatabase.load(creds, inputStream);

            List<Entry> entries = database.findEntries("ADAuthManager");
            if (entries.size() == 0) {
                throw new RuntimeException("No credentials found in the cache. Please login with IntelliJ"
                        + " Azure Tools plugin in the IDE.");
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(entries.get(0).getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read database.", e);
        }
    }

    private String getKdbxPassword() throws IOException {
        String passwordFilePath = new File(keepPassDatabasePath).getParent() + "\\" + "c.pwd";
        String extractedpwd = "";

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(passwordFilePath));
            String line = reader.readLine();

            while (line != null) {
                if (line.contains("value")) {
                    System.out.println(line);
                    String[] tokens = line.split(" ");
                    if (tokens.length == 3) {
                        extractedpwd = tokens[2];
                        break;
                    } else {
                        throw new RuntimeException("Password not found in the file.");
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
