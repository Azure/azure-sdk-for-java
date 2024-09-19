// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpMessageConstant;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.util.Context;
import com.azure.messaging.eventhubs.implementation.instrumentation.OperationName;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.codec.ReadableBuffer;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.Proxy;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.ProxyOptions.PROXY_AUTHENTICATION_TYPE;
import static com.azure.core.amqp.ProxyOptions.PROXY_PASSWORD;
import static com.azure.core.amqp.ProxyOptions.PROXY_USERNAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.ERROR_TYPE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_CONSUMER_GROUP_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_PARTITION_ID;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_OPERATION_NAME;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_OPERATION_TYPE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SYSTEM;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_SYSTEM_VALUE;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.SERVER_ADDRESS;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.getOperationType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Contains helper methods for working with AMQP messages
 */
public final class TestUtils {
    private static final ClientLogger LOGGER = new ClientLogger(TestUtils.class);

    private static final String AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME = "AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME";
    private static final String AZURE_EVENTHUBS_EVENT_HUB_NAME = "AZURE_EVENTHUBS_EVENT_HUB_NAME";
    private static final String AZURE_EVENTHUBS_CONNECTION_STRING = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final Configuration GLOBAL_CONFIGURATION = Configuration.getGlobalConfiguration();

    // System and application properties from the generated test message.
    static final Instant ENQUEUED_TIME = Instant.ofEpochSecond(1561344661);
    static final Long OFFSET = 1534L;
    static final String PARTITION_KEY = "a-partition-key";
    static final Long SEQUENCE_NUMBER = 1025L;
    static final String OTHER_SYSTEM_PROPERTY = "Some-other-system-property";
    static final Boolean OTHER_SYSTEM_PROPERTY_VALUE = Boolean.TRUE;
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
     * Gets the configured ProxyConfiguration from environment variables.
     */
    public static ProxyOptions getProxyConfiguration() {
        final String address = GLOBAL_CONFIGURATION.get(Configuration.PROPERTY_HTTP_PROXY);

        if (address == null) {
            return null;
        }

        final String[] host = address.split(":");
        if (host.length < 2) {
            LOGGER.warning("Environment variable '{}' cannot be parsed into a proxy. Value: {}",
                Configuration.PROPERTY_HTTP_PROXY, address);
            return null;
        }

        final String hostname = host[0];
        final int port = Integer.parseInt(host[1]);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));

        final String username = GLOBAL_CONFIGURATION.get(PROXY_USERNAME);

        if (username == null) {
            LOGGER.info("Environment variable '{}' is not set. No authentication used.");
            return new ProxyOptions(ProxyAuthenticationType.NONE, proxy, null, null);
        }

        final String password = GLOBAL_CONFIGURATION.get(PROXY_PASSWORD);
        final String authentication = GLOBAL_CONFIGURATION.get(PROXY_AUTHENTICATION_TYPE);

        final ProxyAuthenticationType authenticationType = CoreUtils.isNullOrEmpty(authentication)
            ? ProxyAuthenticationType.NONE
            : ProxyAuthenticationType.valueOf(authentication);

        return new ProxyOptions(authenticationType, proxy, username, password);
    }

    public static String getFullyQualifiedDomainName() {
        return GLOBAL_CONFIGURATION.get(AZURE_EVENTHUBS_FULLY_QUALIFIED_DOMAIN_NAME);
    }

    public static String getEventHubName() {
        return GLOBAL_CONFIGURATION.get(AZURE_EVENTHUBS_EVENT_HUB_NAME);
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
        systemProperties.put(Symbol.getSymbol(OTHER_SYSTEM_PROPERTY), OTHER_SYSTEM_PROPERTY_VALUE);

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

    /**
     * Obtain a {@link com.azure.identity.AzurePipelinesCredentialBuilder} when running in Azure pipelines that is
     * configured with service connections federated identity.
     *
     * @return A {@link com.azure.identity.AzurePipelinesCredentialBuilder} when running in Azure pipelines that is
     *   configured with service connections federated identity, {@code null} otherwise.
     */
    private static TokenCredential getPipelineCredential() {
        final String serviceConnectionId  = getPropertyValue("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
        final String clientId = getPropertyValue("AZURESUBSCRIPTION_CLIENT_ID");
        final String tenantId = getPropertyValue("AZURESUBSCRIPTION_TENANT_ID");
        final String systemAccessToken = getPropertyValue("SYSTEM_ACCESSTOKEN");

        if (CoreUtils.isNullOrEmpty(serviceConnectionId) || CoreUtils.isNullOrEmpty(clientId)
            || CoreUtils.isNullOrEmpty(tenantId) || CoreUtils.isNullOrEmpty(systemAccessToken)) {
            return null;
        }

        return new AzurePipelinesCredentialBuilder()
            .systemAccessToken(systemAccessToken)
            .clientId(clientId)
            .tenantId(tenantId)
            .serviceConnectionId(serviceConnectionId)
            .build();
    }

    /**
     * Obtain the Azure Pipelines credential if running in Azure Pipelines configured with service connections federated identity.
     *
     * @return the Azure Pipelines credential.
     */
    public static TokenCredential getPipelineCredential(AtomicReference<TokenCredential> credentialCached) {
        return credentialCached.updateAndGet(cached -> {
            if (cached != null) {
                return cached;
            }

            final TokenCredential tokenCredential = TestUtils.getPipelineCredential();

            assumeTrue(tokenCredential != null, "Test required to run on Azure Pipelines that is configured with service connections federated identity.");

            return request -> Mono.defer(() -> tokenCredential.getToken(request))
                .subscribeOn(Schedulers.boundedElastic());
        });
    }

    public static String getConnectionString(boolean withSas) {
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

    private static String getPropertyValue(String propertyName) {
        return Configuration.getGlobalConfiguration().get(propertyName, System.getenv(propertyName));
    }

    public static void assertAttributes(String hostname, String entityName, OperationName operationName, Map<String, Object> attributes) {
        assertAllAttributes(hostname, entityName, null, null, null, operationName, attributes);
    }

    public static Map<String, Object> attributesToMap(Attributes attributes) {
        return attributes.asMap().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getKey(), e -> e.getValue()));
    }

    public static String getSpanName(OperationName operation, String eventHubName) {
        return String.format("%s %s", operation, eventHubName);
    }

    public static void assertAllAttributes(String hostname, String entityName, String partitionId,
        String consumerGroup, String errorType, OperationName operationName, Map<String, Object> attributes) {
        assertEquals(MESSAGING_SYSTEM_VALUE, attributes.get(MESSAGING_SYSTEM));
        assertEquals(hostname, attributes.get(SERVER_ADDRESS));
        assertEquals(entityName, attributes.get(MESSAGING_DESTINATION_NAME));
        assertEquals(partitionId, attributes.get(MESSAGING_DESTINATION_PARTITION_ID));
        assertEquals(consumerGroup, attributes.get(MESSAGING_CONSUMER_GROUP_NAME));
        if (operationName == null) {
            assertNull(attributes.get(MESSAGING_OPERATION_NAME));
            assertNull(attributes.get(MESSAGING_OPERATION_TYPE));
        } else {
            assertEquals(operationName.toString(), attributes.get(MESSAGING_OPERATION_NAME));
            assertEquals(getOperationType(operationName), attributes.get(MESSAGING_OPERATION_TYPE));
        }
        assertEquals(errorType, attributes.get(ERROR_TYPE));
    }

    public static void assertSpanStatus(String description, SpanData span) {
        if (description != null) {
            assertEquals(StatusCode.ERROR, span.getStatus().getStatusCode());
            assertEquals(description, span.getStatus().getDescription());
        } else {
            assertEquals(StatusCode.UNSET, span.getStatus().getStatusCode());
        }
    }

    public static EventData createEventData(AmqpAnnotatedMessage amqpAnnotatedMessage, long offset,
                                                                   long sequenceNumber, Instant enqueuedTime) {
        amqpAnnotatedMessage.getMessageAnnotations()
                .put(AmqpMessageConstant.OFFSET_ANNOTATION_NAME.getValue(), offset);
        amqpAnnotatedMessage.getMessageAnnotations()
                .put(AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(), sequenceNumber);
        amqpAnnotatedMessage.getMessageAnnotations()
                .put(AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), enqueuedTime);

        SystemProperties systemProperties = new SystemProperties(amqpAnnotatedMessage, offset, enqueuedTime, sequenceNumber, null);
        return new EventData(amqpAnnotatedMessage, systemProperties, Context.NONE);
    }

    private TestUtils() {
    }
}
