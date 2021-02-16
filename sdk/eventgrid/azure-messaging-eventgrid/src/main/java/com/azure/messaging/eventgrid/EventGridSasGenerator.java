package com.azure.messaging.eventgrid;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.logging.ClientLogger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * This is a utility to create a SAS, or Shared Access Signature token, from the endpoint and key of an
 * Event Grid Topic or Domain.
 *
 * If you would like to grant somebody to publish events to your Event Grid Topic/Domain resource for a limited time,
 * you could use this utility class to create a SAS token and share the token to them. They can then use
 * {@link EventGridPublisherClientBuilder#credential(AzureSasCredential)} to to create a client to publish events.
 *
 */
public final class EventGridSasGenerator {
    private EventGridSasGenerator() {
        // Hide the constructor
    }
    private static final ClientLogger logger = new ClientLogger(EventGridPublisherClient.class);

    /**
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service with the latest Event Grid service API defined in {@link EventGridServiceVersion#getLatest()}.
     * @param endpoint the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential  the access key obtained from the Event Grid topic or domain.
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
     */
    public static String generateSas(String endpoint, AzureKeyCredential keyCredential, OffsetDateTime expirationTime) {
        return generateSas(endpoint, keyCredential, expirationTime, EventGridServiceVersion.getLatest());
    }

    /**
     * Generate a shared access signature to provide time-limited authentication for requests to the Event Grid
     * service.
     * @param endpoint the endpoint of the Event Grid topic or domain.
     * @param expirationTime the time in which the signature should expire, no longer providing authentication.
     * @param keyCredential  the access key obtained from the Event Grid topic or domain.
     * @param apiVersion the EventGrid service api version defined in {@link EventGridServiceVersion}
     *
     * @return the shared access signature string which can be used to construct an instance of
     * {@link AzureSasCredential}.
     */
    public static String generateSas(String endpoint, AzureKeyCredential keyCredential, OffsetDateTime expirationTime,
        EventGridServiceVersion apiVersion) {
        try {
            String resKey = "r";
            String expKey = "e";
            String signKey = "s";

            Charset charset = StandardCharsets.UTF_8;
            endpoint = endpoint + "?api-version=" + apiVersion.getVersion();
            String encodedResource = URLEncoder.encode(endpoint, charset.name());
            String encodedExpiration = URLEncoder.encode(expirationTime.atZoneSameInstant(ZoneOffset.UTC).format(
                DateTimeFormatter.ofPattern("M/d/yyyy h:m:s a")),
                charset.name());

            String unsignedSas = String.format("%s=%s&%s=%s", resKey, encodedResource, expKey, encodedExpiration);

            Mac hmac = Mac.getInstance("hmacSHA256");
            hmac.init(new SecretKeySpec(Base64.getDecoder().decode(keyCredential.getKey()), "hmacSHA256"));
            String signature = new String(Base64.getEncoder().encode(
                hmac.doFinal(unsignedSas.getBytes(charset))),
                charset);

            String encodedSignature = URLEncoder.encode(signature, charset.name());

            return String.format("%s&%s=%s", unsignedSas, signKey, encodedSignature);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }
}
