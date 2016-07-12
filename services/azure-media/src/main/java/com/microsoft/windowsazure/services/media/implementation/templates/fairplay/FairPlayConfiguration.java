package com.microsoft.windowsazure.services.media.implementation.templates.fairplay;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.windowsazure.core.utils.Base64;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FairPlayConfiguration {

    private String askId;

    private String fairPlayPfxPasswordId;

    private String fairPlayPfx;

    private String contentEncryptionIV;

    public static String createSerializedFairPlayOptionConfiguration(
          KeyStore keyStore, String pfxPassword, String pfxPasswordKeyId, String askId,
          String contentIv) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            keyStore.store(outputStream, pfxPassword.toCharArray());
            String certString = Base64.encode(outputStream.toByteArray());
            FairPlayConfiguration config = new FairPlayConfiguration();
            config.askId = askId;
            config.contentEncryptionIV = contentIv;
            config.fairPlayPfx = certString;
            config.fairPlayPfxPasswordId = pfxPasswordKeyId;
            ObjectMapper mapper = new ObjectMapper();
            String configuration = mapper.writeValueAsString(config);

            return configuration;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    static final char[] HEXARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEXARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEXARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    @JsonProperty("ASkId")
    public String getASkId() {
        return askId;
    }

    @JsonProperty("FairPlayPfxPasswordId")
    public String getFairPlayPfxPasswordId() {
        return fairPlayPfxPasswordId;
    }

    @JsonProperty("FairPlayPfx")
    public String getFairPlayPfx() {
        return fairPlayPfx;
    }

    @JsonProperty("ContentEncryptionIV")
    public String getContentEncryptionIV() {
        return contentEncryptionIV;
    };
}
