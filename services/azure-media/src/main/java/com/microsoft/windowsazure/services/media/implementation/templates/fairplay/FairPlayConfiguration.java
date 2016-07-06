package com.microsoft.windowsazure.services.media.implementation.templates.fairplay;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.windowsazure.core.utils.Base64;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FairPlayConfiguration {

    @JsonProperty("ASkId")
    public String ASkId;
	
    @JsonProperty("FairPlayPfxPasswordId")
    public String FairPlayPfxPasswordId;
	
    @JsonProperty("FairPlayPfx")
    public String FairPlayPfx;
	
    @JsonProperty("ContentEncryptionIV")
    public String ContentEncryptionIV;
	
	public static String createSerializedFairPlayOptionConfiguration(
		  KeyStore keyStore, String pfxPassword, String pfxPasswordKeyId, String askId,
	      String contentIv)
    {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            keyStore.store(outputStream, pfxPassword.toCharArray());
            String certString = Base64.encode(outputStream.toByteArray());
            FairPlayConfiguration config = new FairPlayConfiguration();
            config.ASkId = askId;
            config.ContentEncryptionIV = contentIv;
            config.FairPlayPfx = certString;
            config.FairPlayPfxPasswordId = pfxPasswordKeyId;
            ObjectMapper mapper = new ObjectMapper();
            String configuration = mapper.writeValueAsString(config);

            return configuration;
        } catch(Throwable t){
            throw new RuntimeException(t);
        }
    }

	final public static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

}
