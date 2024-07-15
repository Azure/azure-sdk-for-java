// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.apache.qpid.proton.message.Message;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains helper methods for working with AMQP messages
 */
public final class TestUtils {
    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();

    // System and application properties from the generated test message.
    static final Instant ENQUEUED_TIME = Instant.ofEpochSecond(1561344661);
    static final Long OFFSET = 1534L;
    static final String PARTITION_KEY = "a-partition-key";
    static final Long SEQUENCE_NUMBER = 1025L;
    static final String OTHER_SYSTEM_PROPERTY = "Some-other-system-property";
    static final Map<String, Object> APPLICATION_PROPERTIES = new HashMap<>();

    // An application property key used to identify that the request belongs to a test set.
    public static final String MESSAGE_ID = "message-id";
    // An application property key to identify where in the stream this event was created.
    public static final String MESSAGE_POSITION_ID = "message-position";

    /**
     * For integration tests.
     */
    public static final String INTEGRATION = "integration";

    static {
        APPLICATION_PROPERTIES.put("test-name", EventDataTest.class.getName());
        APPLICATION_PROPERTIES.put("a-number", 10L);
    }

    static Symbol getSymbol(AmqpMessageConstant messageConstant) {
        return Symbol.getSymbol(messageConstant.getValue());
    }

    /**
     * Creates a mock message with the contents provided.
     */
    static Message getMessage(byte[] contents, String messageTrackingValue) {
        return getMessage(contents, messageTrackingValue, SEQUENCE_NUMBER, OFFSET, Date.from(ENQUEUED_TIME));
    }

    /**
     * Creates a message with the required system properties set.
     */
    static Message getMessage(byte[] contents, String messageTrackingValue, Long sequenceNumber, Long offsetNumber,
                              Date enqueuedTime) {

        final Map<Symbol, Object> systemProperties = new HashMap<>();
        systemProperties.put(getSymbol(OFFSET_ANNOTATION_NAME), offsetNumber);
        systemProperties.put(getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME), enqueuedTime);
        systemProperties.put(getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME), sequenceNumber);
        systemProperties.put(getSymbol(PARTITION_KEY_ANNOTATION_NAME), PARTITION_KEY);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(systemProperties));

        final Data body;
        if (contents != null) {
            body = new Data(new Binary(contents));
        } else {
            body = new Data(Binary.create((ReadableBuffer) null));
        }

        message.setBody(body);

        final Map<String, Object> applicationProperties = new HashMap<>(APPLICATION_PROPERTIES);
        applicationProperties.put(MESSAGE_ID, messageTrackingValue);

        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        return message;
    }

    public static List<EventData> getEvents(int numberOfEvents, String messageTrackingValue) {
        return IntStream.range(0, numberOfEvents)
            .mapToObj(number -> getEvent("Event " + number, messageTrackingValue, number))
            .collect(Collectors.toList());
    }

    static EventData getEvent(String body, String messageTrackingValue, int position) {
        final EventData eventData = new EventData(body.getBytes(UTF_8));
        eventData.getProperties().put(MESSAGE_ID, messageTrackingValue);
        eventData.getProperties().put(MESSAGE_POSITION_ID, position);
        return eventData;
    }

    /**
     * Checks the {@link #MESSAGE_ID} to see if it matches the {@code expectedValue}.
     */
    public static boolean isMatchingEvent(PartitionEvent partitionEvent, String expectedValue) {
        return isMatchingEvent(partitionEvent.getData(), expectedValue);
    }

    /**
     * Checks the {@link #MESSAGE_ID} to see if it matches the {@code expectedValue}.
     */
    public static boolean isMatchingEvent(EventData event, String expectedValue) {
        LOGGER.atInfo()
            .addKeyValue("expectedMessageId", expectedValue)
            .addKeyValue("sequenceNo", event.getSequenceNumber())
            .addKeyValue("enqueuedTime", event.getEnqueuedTime())
            .addKeyValue("MESSAGE_ID", event.getProperties() == null ? null : event.getProperties().get(MESSAGE_ID))
            .log("isMatchingEvent");

        return event.getProperties() != null && event.getProperties().containsKey(MESSAGE_ID)
            && expectedValue.equals(event.getProperties().get(MESSAGE_ID));
    }

    public static ConnectionStringProperties getConnectionStringProperties() {
        return new ConnectionStringProperties(getConnectionString(false));
    }

    public static ConnectionStringProperties getConnectionStringProperties(boolean withSas) {
        return new ConnectionStringProperties(getConnectionString(withSas));
    }

    static String getConnectionString() {
        return getConnectionString(false);
    }

    static String getConnectionString(boolean withSas) {
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_EVENTHUBS_CONNECTION_STRING");
        if (withSas) {
            String shareAccessSignatureFormat = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
            String connectionStringWithSasAndEntityFormat = "Endpoint=%s;SharedAccessSignature=%s;EntityPath=%s";
            String connectionStringWithSasFormat = "Endpoint=%s;SharedAccessSignature=%s";

            ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
            URI endpoint = properties.getEndpoint();
            String entityPath = properties.getEntityPath();
            String resourceUrl = entityPath == null || entityPath.trim().length() == 0
                ? endpoint.toString() : endpoint.toString() +  entityPath;

            String utf8Encoding = UTF_8.name();
            OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plus(Duration.ofHours(2L));
            String expiresOnEpochSeconds = Long.toString(expiresOn.toEpochSecond());

            try {
                String audienceUri = URLEncoder.encode(resourceUrl, utf8Encoding);
                String secretToSign = audienceUri + "\n" + expiresOnEpochSeconds;
                byte[] sasKeyBytes = properties.getSharedAccessKey().getBytes(utf8Encoding);

                Mac hmacsha256 = Mac.getInstance("HMACSHA256");
                hmacsha256.init(new SecretKeySpec(sasKeyBytes, "HMACSHA256"));

                byte[] signatureBytes = hmacsha256.doFinal(secretToSign.getBytes(utf8Encoding));
                String signature = Base64.getEncoder().encodeToString(signatureBytes);

                String signatureValue = String.format(Locale.US, shareAccessSignatureFormat,
                    audienceUri,
                    URLEncoder.encode(signature, utf8Encoding),
                    URLEncoder.encode(expiresOnEpochSeconds, utf8Encoding),
                    URLEncoder.encode(properties.getSharedAccessKeyName(), utf8Encoding));

                if (entityPath == null) {
                    return String.format(connectionStringWithSasFormat, endpoint, signatureValue);
                }
                return String.format(connectionStringWithSasAndEntityFormat, endpoint, signatureValue, entityPath);
            } catch (Exception e) {
                LOGGER.log(LogLevel.VERBOSE, () -> "Error while getting connection string", e);
            }
        }
        return connectionString;
    }

    private TestUtils() {
    }
}
