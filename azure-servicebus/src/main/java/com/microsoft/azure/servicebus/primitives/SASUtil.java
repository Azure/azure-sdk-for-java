package com.microsoft.azure.servicebus.primitives;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SASUtil {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SASUtil.class);
    private static final String SAS_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
    private static final String HMACAlgorithm = "HMACSHA256";
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    
    public static String generateSharedAccessSignatureToken(String sasKeyName, String sasKey, String resourceURI, int validityInSeconds) throws InvalidKeyException
    {
        if(StringUtil.isNullOrWhiteSpace(sasKey))
        {
            throw new IllegalArgumentException("Invalid SAS key");
        }
        
        if(StringUtil.isNullOrWhiteSpace(resourceURI))
        {
            throw new IllegalArgumentException("Invalid resourceURI");
        }
        
        if(validityInSeconds <= 0)
        {
            throw new IllegalArgumentException("validityInSeconds should be positive");
        }
        
        String validUntil = String.valueOf(Instant.now().getEpochSecond() + validityInSeconds);        
        try
        {
            String utf8EncodingName = StandardCharsets.UTF_8.name();
            String encodedResourceURI = URLEncoder.encode(resourceURI, utf8EncodingName);
            String secretToSign = encodedResourceURI + "\n" + validUntil;
            Mac hmac = Mac.getInstance(HMACAlgorithm);
            SecretKeySpec secretKey = new SecretKeySpec(StringUtil.convertStringToBytes(sasKey), HMACAlgorithm);
            hmac.init(secretKey);       
            byte[] signatureBytes = hmac.doFinal(StringUtil.convertStringToBytes(secretToSign));
            String signature = base64Encoder.encodeToString(signatureBytes);
            TRACE_LOGGER.debug("Generated SAS token for resource: {} with sas key name : {}", resourceURI, sasKeyName);
            return String.format(Locale.US, SAS_FORMAT,
                    encodedResourceURI,
                    URLEncoder.encode(signature, utf8EncodingName),
                    validUntil,
                    URLEncoder.encode(sasKeyName, utf8EncodingName));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            // These exceptions shouldn't occur.
            String errorMessage = "UTF-8 encoding or HMACSHA256 algorithm is missing in the java runtime.";
            Marker fatalMarker = MarkerFactory.getMarker(ClientConstants.FATAL_MARKER);
            TRACE_LOGGER.error(fatalMarker, errorMessage, e);
            throw new RuntimeException(errorMessage);
        }   
    } 

    static int getCBSTokenRenewIntervalInSeconds(int tokenValidityInSeconds)
    {
        if(tokenValidityInSeconds >= 300)
        {
            return tokenValidityInSeconds - 30;
        }
        else if(tokenValidityInSeconds >= 60)
        {
            return tokenValidityInSeconds - 10;
        }
        else
        {            
            return (tokenValidityInSeconds - 1) > 0 ? tokenValidityInSeconds - 1 : 0;
        }
    }    
}
