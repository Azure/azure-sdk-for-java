// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.servicebus.administration.models.AccessRights;
import com.azure.messaging.servicebus.administration.models.AuthorizationRule;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    // System and application properties from the generated test message.
    static final Instant ENQUEUED_TIME = Instant.ofEpochSecond(1561344661);
    static final Long SEQUENCE_NUMBER = 1025L;
    static final String OTHER_SYSTEM_PROPERTY = "Some-other-system-property";
    static final Boolean OTHER_SYSTEM_PROPERTY_VALUE = Boolean.TRUE;
    static final Map<String, Object> APPLICATION_PROPERTIES = new HashMap<>();
    static final int USE_CASE_DEFAULT = 0;
    static final int USE_CASE_RECEIVE_MORE_AND_COMPLETE = 1;
    static final int USE_CASE_SCHEDULE_MESSAGES = 2;
    static final int USE_CASE_RECEIVE_NO_MESSAGES = 3;
    static final int USE_CASE_SEND_RECEIVE_WITH_PROPERTIES = 4;
    static final int USE_CASE_MULTIPLE_RECEIVE_ONE_TIMEOUT = 5;
    static final int USE_CASE_PEEK_BATCH_MESSAGES = 6;
    static final int USE_CASE_SEND_READ_BACK_MESSAGES = 7;
    static final int USE_CASE_DEFERRED_MESSAGE_BY_SEQUENCE_NUMBER = 8;
    static final int USE_CASE_PEEK_MESSAGE_FROM_SEQUENCE = 9;
    static final int USE_CASE_PEEK_RECEIVE_AND_DEFER = 10;
    static final int USE_CASE_PEEK_TRANSACTION_SENDRECEIVE_AND_COMPLETE = 11;
    static final int USE_CASE_SINGLE_SESSION = 12;
    static final int USE_CASE_TXN_1 = 13;
    static final int USE_CASE_TXN_2 = 14;
    static final int USE_CASE_SEND_VIA_TOPIC_1 = 15;
    static final int USE_CASE_SEND_VIA_TOPIC_2 = 16;
    static final int USE_CASE_VALIDATE_AMQP_PROPERTIES = 17;
    static final int USE_CASE_EMPTY_ENTITY = 18;
    static final int USE_CASE_CANCEL_MESSAGES = 19;
    static final int USE_CASE_AUTO_COMPLETE = 20;
    static final int USE_CASE_PEEK_BATCH = 21;
    static final int USE_CASE_PROXY = 22;
    static final int USE_CASE_PROCESSOR_RECEIVE = 23;
    static final int USE_CASE_AMQP_TYPES = 24;
    static final int USE_CASE_RENEW_LOCK = 25;
    static final int USE_CASE_SEND_SCHEDULED = 26;
    static final int USE_CASE_RECEIVE_AND_COMPLETE = 27;
    static final int USE_CASE_PEEK_MESSAGE = 28;
    static final int USE_CASE_MULTIPLE_SESSIONS1 = 29;
    static final int USE_CASE_MULTIPLE_SESSIONS2 = 30;
    static final int USE_CASE_MULTIPLE_SESSIONS3 = 31;
    static final int USE_CASE_AUTO_RENEW_RECEIVE = 32;
    static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();

    // An application property key to identify where in the stream this message was created.
    static final String MESSAGE_POSITION_ID = "message-position";

    static {
        APPLICATION_PROPERTIES.put("test-name", ServiceBusMessage.class.getName());
        APPLICATION_PROPERTIES.put("a-number", 10L);
        APPLICATION_PROPERTIES.put("status-code", AmqpResponseCode.OK.getValue());
    }

    /**
     * Namespace used to record tests.
     */
    public static final String TEST_NAMESPACE = "sb-java-conniey-sb1";

    /**
     * Gets the namespace connection string.
     *
     * @return The namespace connection string.
     */
    public static String getConnectionString(boolean withSas) {
        String connectionString = getPropertyValue("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
        if (withSas) {
            final String shareAccessSignatureFormat = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
            String connectionStringWithSasAndEntityFormat = "Endpoint=%s;SharedAccessSignature=%s;EntityPath=%s";
            String connectionStringWithSasFormat = "Endpoint=%s;SharedAccessSignature=%s";

            ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
            URI endpoint = properties.getEndpoint();
            String entityPath = properties.getEntityPath();
            String resourceUrl = entityPath == null || entityPath.trim().length() == 0
                ? endpoint.toString() : endpoint.toString() +  properties.getEntityPath();

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

    /**
     * Gets the fully qualified domain name for the service bus resource.
     *
     * @return The fully qualified domain name for the service bus resource.
     */
    public static String getFullyQualifiedDomainName() {
        return getPropertyValue("AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME", "REDACTED.servicebus.windows.net");
    }

    public static String getEndpoint() {
        return getPropertyValue("AZURE_SERVICEBUS_ENDPOINT_SUFFIX", ".servicebus.windows.net");
    }

    /**
     * The Service Bus queue name (NOT session enabled).
     *
     * @return The Service Bus queue name.
     */
    public static String getQueueBaseName() {
        return getPropertyValue("AZURE_SERVICEBUS_QUEUE_NAME", "queue");
    }

    /**
     * Gets the Service Bus rule name
     *
     * @return The Service Bus rule name.
     */
    public static String getRuleBaseName() {
        return getPropertyValue("AZURE_SERVICEBUS_RULE_NAME", "rule");
    }

    /**
     * The Service Bus queue name (session enabled).
     *
     * @return The Service Bus queue name.
     */
    public static String getSessionQueueBaseName() {
        return getPropertyValue("AZURE_SERVICEBUS_SESSION_QUEUE_NAME", "queue-session");
    }

    /**
     * Gets the Service Bus subscription name (NOT session enabled)
     *
     * @return The Service Bus subscription name.
     */
    public static String getSubscriptionBaseName() {
        return getPropertyValue("AZURE_SERVICEBUS_SUBSCRIPTION_NAME", "subscription");
    }

    /**
     * Gets the Service Bus subscription name (NOT session enabled)
     *
     * @return The Service Bus subscription name.
     */
    public static String getTopicBaseName() {
        return getPropertyValue("AZURE_SERVICEBUS_TOPIC_NAME", "topic");
    }

    /**
     * Gets the Service Bus subscription name (session enabled)
     *
     * @return The Service Bus subscription name.
     */
    public static String getSessionSubscriptionBaseName() {
        return getPropertyValue("AZURE_SERVICEBUS_SESSION_SUBSCRIPTION_NAME", "subscription-session");
    }

    /**
     * Gets the name of an entity based on its base name.
     *
     * @param baseName Base of the entity.
     * @param index Index number.
     *
     * @return The entity name.
     */
    public static String getEntityName(String baseName, int index) {
        return String.join("-", baseName, String.valueOf(index));
    }

    public static Configuration getGlobalConfiguration() {
        return GLOBAL_CONFIGURATION;
    }

    /**
     * Creates a message with the given contents, default system properties, and adds a {@code messageId} in the
     * application properties. Useful for helping filter messages.
     */
    public static Message getMessage(byte[] contents, String messageId, Map<String, String> additionalProperties) {
        final Map<Symbol, Object> systemProperties = new HashMap<>();
        systemProperties.put(Symbol.getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue()), Date.from(ENQUEUED_TIME));
        systemProperties.put(Symbol.getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue()), SEQUENCE_NUMBER);

        final Message message = Proton.message();
        message.setMessageAnnotations(new MessageAnnotations(systemProperties));
        message.setBody(new Data(new Binary(contents)));
        message.getMessageAnnotations().getValue()
            .put(Symbol.getSymbol(OTHER_SYSTEM_PROPERTY), OTHER_SYSTEM_PROPERTY_VALUE);

        Map<String, Object> applicationProperties = new HashMap<>();
        APPLICATION_PROPERTIES.forEach(applicationProperties::put);

        if (!CoreUtils.isNullOrEmpty(messageId)) {
            message.setMessageId(messageId);
        }

        if (additionalProperties != null) {
            additionalProperties.forEach(applicationProperties::put);
        }

        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        return message;
    }

    /**
     * Creates a mock message with the contents provided.
     */
    public static Message getMessage(byte[] contents) {
        return getMessage(contents, null);
    }

    /**
     * Creates a mock message with the contents provided.
     */
    public static Message getMessage(byte[] contents, String messageTrackingValue) {
        return getMessage(contents, messageTrackingValue, Collections.emptyMap());
    }

    /**
     * Gets a set of messages with {@link ServiceBusMessage#getMessageId()} as a unique identifier for that service bus
     * message.
     *
     * @param numberOfEvents Number of events to create.
     * @param messageId An identifier for the set of messages.
     *
     * @return A list of messages.
     */
    public static List<ServiceBusMessage> getServiceBusMessages(int numberOfEvents, String messageId, byte[] content) {
        return IntStream.range(0, numberOfEvents)
            .mapToObj(number -> {
                final ServiceBusMessage message = getServiceBusMessage(content, messageId);
                message.getApplicationProperties().put(MESSAGE_POSITION_ID, number);

                return message;
            })
            .collect(Collectors.toList());
    }

    /**
     * Gets a set of messages with {@link ServiceBusMessage#getMessageId()} as a unique identifier for that service bus
     * message.
     *
     * @param numberOfEvents Number of events to create.
     * @param messageId An identifier for the set of messages.
     *
     * @return A list of messages.
     */
    public static List<ServiceBusMessage> getServiceBusMessages(int numberOfEvents, String messageId) {
        return IntStream.range(0, numberOfEvents)
            .mapToObj(number -> {
                final ServiceBusMessage message = getServiceBusMessage("Event " + number, messageId);
                message.getApplicationProperties().put(MESSAGE_POSITION_ID, number);

                return message;
            })
            .collect(Collectors.toList());
    }

    public static ServiceBusMessage getServiceBusMessage(String body, String messageId) {
        return getServiceBusMessage(body.getBytes(UTF_8), messageId);
    }

    public static ServiceBusMessage getServiceBusMessage(byte[] body, String messageId) {
        final ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromBytes(body));
        message.setMessageId(messageId);
        return message;
    }

    public static void assertAuthorizationRules(AuthorizationRule expected, AuthorizationRule actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertNotNull(actual);
        assertEquals(expected.getKeyName(), actual.getKeyName());
        assertEquals(expected.getClaimType(), actual.getClaimType());
        assertEquals(expected.getClaimValue(), actual.getClaimValue());
        assertEquals(expected.getPrimaryKey(), actual.getPrimaryKey());
        assertEquals(expected.getSecondaryKey(), actual.getSecondaryKey());

        final HashSet<AccessRights> expectedRights = new HashSet<>(expected.getAccessRights());
        final HashSet<AccessRights> actualRights = new HashSet<>(actual.getAccessRights());

        assertEquals(expectedRights.size(), actualRights.size());
        expectedRights.forEach(right -> assertTrue(actualRights.contains(right)));
    }

    public static void assertAuthorizationRules(List<AuthorizationRule> expected, List<AuthorizationRule> actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            final AuthorizationRule expectedItem = expected.get(i);
            final AuthorizationRule actualItem = actual.get(i);

            assertAuthorizationRules(expectedItem, actualItem);
        }
    }

    public static String getPropertyValue(String propertyName) {
        return GLOBAL_CONFIGURATION.get(propertyName, System.getenv(propertyName));
    }

    public static String getPropertyValue(String propertyName, String defaultValue) {
        return GLOBAL_CONFIGURATION.get(propertyName, defaultValue);
    }
}
