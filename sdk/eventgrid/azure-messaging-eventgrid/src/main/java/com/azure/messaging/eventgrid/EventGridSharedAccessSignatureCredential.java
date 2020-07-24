package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.CoreUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A way to use a generated shared access signature as a credential to publish events to a topic through a client.
 */
public final class EventGridSharedAccessSignatureCredential {

    private String accessToken;

    public static String createSharedAccessSignature(String resource, OffsetDateTime expiration,
                                                     AzureKeyCredential key) {
        try {
            String resKey = "r";
            String expKey = "e";
            String signKey = "s";

            String encoder = StandardCharsets.UTF_8.name();
            String encodedResource = URLEncoder.encode(resource, encoder);
            String encodedExpiration = URLEncoder.encode(expiration.format(DateTimeFormatter.BASIC_ISO_DATE), encoder);

            String unsignedSas = String.format("%s=%s&%s=%s", resKey, encodedResource, expKey, encodedExpiration);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //TODO: finish
        return null;
    }

    /**
     * Create an instance of this object to authenticate calls to the EventGrid service.
     * @param accessToken the shared access signature to use.
     */
    public EventGridSharedAccessSignatureCredential(String accessToken) {
        if (CoreUtils.isNullOrEmpty(accessToken)) {
            throw new IllegalArgumentException("the access token cannot be null or empty");
        }
        this.accessToken = accessToken;
    }

    /**
     * Get the token string to authenticate service calls
     * @return the SharedAccessSignature token as a string
     */
    public String getSignature() {
        return accessToken;
    }


    /**
     * Change the shared access signature token to a new one.
     * @param accessToken the shared access signature token to use.
     */
    public void update(String accessToken) {
        this.accessToken = accessToken;
    }
}
