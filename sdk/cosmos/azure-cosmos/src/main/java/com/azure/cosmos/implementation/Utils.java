// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.uuid.EthernetAddress;
import com.azure.cosmos.implementation.uuid.Generators;
import com.azure.cosmos.implementation.uuid.impl.TimeBasedGenerator;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.DedicatedGatewayRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Utils {
    private final static Logger logger = LoggerFactory.getLogger(Utils.class);

    private static final int JAVA_VERSION = getJavaVersion();
    private static final int ONE_KB = 1024;
    private static final ZoneId GMT_ZONE_ID = ZoneId.of("GMT");
    public static final Base64.Encoder Base64Encoder = Base64.getEncoder();
    public static final Base64.Decoder Base64Decoder = Base64.getDecoder();
    public static final Base64.Encoder Base64UrlEncoder = Base64.getUrlEncoder();

    private static final ObjectMapper simpleObjectMapperAllowingDuplicatedProperties =
        createAndInitializeObjectMapper(true);
    private static final ObjectMapper simpleObjectMapperDisallowingDuplicatedProperties =
        createAndInitializeObjectMapper(false);

    private static ObjectMapper simpleObjectMapper = simpleObjectMapperDisallowingDuplicatedProperties;
    private static final TimeBasedGenerator TIME_BASED_GENERATOR =
            Generators.timeBasedGenerator(EthernetAddress.constructMulticastAddress());
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

    // NOTE DateTimeFormatter.RFC_1123_DATE_TIME cannot be used.
    // because cosmos db rfc1123 validation requires two digits for day.
    // so Thu, 04 Jan 2018 00:30:37 GMT is accepted by the cosmos db service,
    // but Thu, 4 Jan 2018 00:30:37 GMT is not.
    // Therefore, we need a custom date time formatter.
    private static final DateTimeFormatter RFC_1123_DATE_TIME = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    private static ObjectMapper createAndInitializeObjectMapper(boolean allowDuplicateProperties) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        if (!allowDuplicateProperties) {
            objectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
        }
        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);


        // We will not register after burner for java 16+, due to its breaking changes
        // https://github.com/Azure/azure-sdk-for-java/issues/23005
        if (JAVA_VERSION != -1 && JAVA_VERSION < 16) {
            objectMapper.registerModule(new AfterburnerModule());
        }

        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    private static int getJavaVersion() {
        int version = -1;
        try {
            String completeJavaVersion = System.getProperty("java.version");
            String[] versionElements = completeJavaVersion.split("\\.");
            int versionFirstPart = Integer.parseInt(versionElements[0]);
            // Java 8 or lower format is 1.6.0, 1.7.0, 1.7.0, 1.8.0
            // Java 9 or higher format is 9.0, 10.0, 11.0
            if (versionFirstPart == 1) {
                version = Integer.parseInt(versionElements[1]);
            } else {
                version = versionFirstPart;
            }
            return version;
        } catch (Exception ex) {
            // Consumed the exception we got during parsing
            // For unknown version we wil mark it as -1
            logger.warn("Error while fetching java version", ex);
            return version;
        }
    }

    public static byte[] getUTF8BytesOrNull(String str) {
        if (str == null) {
            return null;
        }

        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] getUTF8Bytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);

    }

    public static String encodeBase64String(byte[] binaryData) {
        String encodedString = Base64Encoder.encodeToString(binaryData);

        if (encodedString.endsWith("\r\n")) {
            encodedString = encodedString.substring(0, encodedString.length() - 2);
        }
        return encodedString;
    }

    public static String decodeBase64String(String encodedString) {
        byte[] decodeString = Base64Decoder.decode(encodedString);
        return new String(decodeString, StandardCharsets.UTF_8);
    }

    public static String decodeAsUTF8String(String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            return inputString;
        }
        try {
            return URLDecoder.decode(inputString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            logger.warn("Error while decoding input string", e);
            return inputString;
        }
    }

    public static String encodeUrlBase64String(byte[] binaryData) {
        String encodedString = Base64UrlEncoder.withoutPadding().encodeToString(binaryData);

        if (encodedString.endsWith("\r\n")) {
            encodedString = encodedString.substring(0, encodedString.length() - 2);
        }
        return encodedString;
    }

    public static void configureSimpleObjectMapper(boolean allowDuplicateProperties) {
        if (allowDuplicateProperties) {
            Utils.simpleObjectMapper = Utils.simpleObjectMapperAllowingDuplicatedProperties;
        } else {
            Utils.simpleObjectMapper = Utils.simpleObjectMapperDisallowingDuplicatedProperties;
        }
    }

    /**
     * Joins the specified paths by appropriately padding them with '/'
     *
     * @param path1 the first path segment to join.
     * @param path2 the second path segment to join.
     * @return the concatenated path with '/'
     */
    public static String joinPath(String path1, String path2) {
        path1 = trimBeginningAndEndingSlashes(path1);
        String result = "/" + path1 + "/";

        if (!StringUtils.isEmpty(path2)) {
            path2 = trimBeginningAndEndingSlashes(path2);
            result += path2 + "/";
        }

        return result;
    }

    /**
     * Trims the beginning and ending '/' from the given path
     *
     * @param path the path to trim for beginning and ending slashes
     * @return the path without beginning and ending '/'
     */
    public static String trimBeginningAndEndingSlashes(String path) {
        if(path == null) {
            return null;
        }

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    public static String createQuery(Map<String, String> queryParameters) {
        if (queryParameters == null)
            return "";
        StringBuilder queryString = new StringBuilder();
        for (Map.Entry<String, String> nameValuePair : queryParameters.entrySet()) {
            String key = nameValuePair.getKey();
            String value = nameValuePair.getValue();
            if (key != null && !key.isEmpty()) {
                if (queryString.length() > 0) {
                    queryString.append(RuntimeConstants.Separators.Query[1]);
                }
                queryString.append(key);
                if (value != null) {
                    queryString.append(RuntimeConstants.Separators.Query[2]);
                    queryString.append(value);
                }
            }
        }
        return queryString.toString();
    }

    public static URI setQuery(String urlString, String query) {

        if (urlString == null)
            throw new IllegalStateException("urlString parameter can't be null.");
        query = Utils.removeLeadingQuestionMark(query);
        try {
            if (query != null && !query.isEmpty()) {
                return new URI(Utils.addTrailingSlash(urlString) + RuntimeConstants.Separators.Query[0] + query);
            } else {
                return new URI(Utils.addTrailingSlash(urlString));
            }
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Uri is invalid: ", e);
        }
    }

    /**
     * Given the full path to a resource, extract the collection path.
     *
     * @param resourceFullName the full path to the resource.
     * @return the path of the collection in which the resource is.
     */
    public static String getCollectionName(String resourceFullName) {
        if (resourceFullName != null) {
            resourceFullName = Utils.trimBeginningAndEndingSlashes(resourceFullName);

            int slashCount = 0;
            for (int i = 0; i < resourceFullName.length(); i++) {
                if (resourceFullName.charAt(i) == '/') {
                    slashCount++;
                    if (slashCount == 4) {
                        return resourceFullName.substring(0, i);
                    }
                }
            }
        }
        return resourceFullName;
    }

    public static <T> int getCollectionSize(Collection<T> collection) {
        if (collection == null) {
            return 0;
        }
        return collection.size();
    }

    public static boolean isCollectionChild(ResourceType type) {
        return type == ResourceType.Document || type == ResourceType.Attachment || type == ResourceType.Conflict
                || type == ResourceType.StoredProcedure || type == ResourceType.Trigger || type == ResourceType.UserDefinedFunction;
    }

    public static boolean isWriteOperation(OperationType operationType) {
        return operationType == OperationType.Create || operationType == OperationType.Upsert || operationType == OperationType.Delete || operationType == OperationType.Replace
                || operationType == OperationType.ExecuteJavaScript || operationType == OperationType.Batch;
    }

    private static String addTrailingSlash(String path) {
        if (path == null || path.isEmpty())
            path = new String(RuntimeConstants.Separators.Url);
        else if (path.charAt(path.length() - 1) != RuntimeConstants.Separators.Url[0])
            path = path + RuntimeConstants.Separators.Url[0];

        return path;
    }

    private static String removeLeadingQuestionMark(String path) {
        if (path == null || path.isEmpty())
            return path;

        if (path.charAt(0) == RuntimeConstants.Separators.Query[0])
            return path.substring(1);

        return path;
    }

    public static boolean isValidConsistency(ConsistencyLevel backendConsistency,
                                             ConsistencyLevel desiredConsistency) {
        switch (backendConsistency) {
            case STRONG:
                return desiredConsistency == ConsistencyLevel.STRONG ||
                        desiredConsistency == ConsistencyLevel.BOUNDED_STALENESS ||
                        desiredConsistency == ConsistencyLevel.SESSION ||
                        desiredConsistency == ConsistencyLevel.EVENTUAL ||
                        desiredConsistency == ConsistencyLevel.CONSISTENT_PREFIX;

            case BOUNDED_STALENESS:
                return desiredConsistency == ConsistencyLevel.BOUNDED_STALENESS ||
                        desiredConsistency == ConsistencyLevel.SESSION ||
                        desiredConsistency == ConsistencyLevel.EVENTUAL ||
                        desiredConsistency == ConsistencyLevel.CONSISTENT_PREFIX;

            case SESSION:
            case EVENTUAL:
            case CONSISTENT_PREFIX:
                return desiredConsistency == ConsistencyLevel.SESSION ||
                        desiredConsistency == ConsistencyLevel.EVENTUAL ||
                        desiredConsistency == ConsistencyLevel.CONSISTENT_PREFIX;

            default:
                throw new IllegalArgumentException("backendConsistency");
        }
    }

    public static String getUserAgent() {
        return getUserAgent(HttpConstants.Versions.SDK_NAME, HttpConstants.Versions.getSdkVersion());
    }

    public static String getUserAgent(String sdkName, String sdkVersion) {
        String osName = System.getProperty("os.name");
        if (osName == null) {
            osName = "Unknown";
        }
        osName = SPACE_PATTERN.matcher(osName).replaceAll("");
        return String.format("%s%s/%s %s/%s JRE/%s",
                UserAgentContainer.AZSDK_USERAGENT_PREFIX,
                sdkName,
                sdkVersion,
                osName,
                System.getProperty("os.version"),
                System.getProperty("java.version")
                );
    }

    public static ObjectMapper getSimpleObjectMapper() {
        return Utils.simpleObjectMapper;
    }

    /**
     * Returns Current Time in RFC 1123 format, e.g,
     * Fri, 01 Dec 2017 19:22:30 GMT.
     *
     * @return an instance of STRING
     */
    public static String nowAsRFC1123() {
        ZonedDateTime now = ZonedDateTime.now(GMT_ZONE_ID);
        return Utils.RFC_1123_DATE_TIME.format(now);
    }

    public static UUID randomUUID() {
        return TIME_BASED_GENERATOR.generate();
    }

    public static String instantAsUTCRFC1123(Instant instant){
        return Utils.RFC_1123_DATE_TIME.format(instant.atZone(GMT_ZONE_ID));
    }

    public static int getValueOrDefault(Integer val, int defaultValue) {
        return val != null ? val : defaultValue;
    }

    public static void checkStateOrThrow(boolean value, String argumentName, String message) throws IllegalArgumentException {

        IllegalArgumentException t = checkStateOrReturnException(value, argumentName, message);
        if (t != null) {
            throw t;
        }
    }

    public static void checkNotNullOrThrow(Object val, String argumentName, String message) throws NullPointerException {

        NullPointerException t = checkNotNullOrReturnException(val, argumentName, message);
        if (t != null) {
            throw t;
        }
    }

    public static void checkStateOrThrow(boolean value, String argumentName, String messageTemplate, Object... messageTemplateParams) throws IllegalArgumentException {
        IllegalArgumentException t = checkStateOrReturnException(value, argumentName, argumentName, messageTemplateParams);
        if (t != null) {
            throw t;
        }
    }

    public static IllegalArgumentException checkStateOrReturnException(boolean value, String argumentName, String message) {

        if (value) {
            return null;
        }

        return new IllegalArgumentException(String.format("argumentName: %s, message: %s", argumentName, message));
    }

    public static IllegalArgumentException checkStateOrReturnException(boolean value, String argumentName, String messageTemplate, Object... messageTemplateParams) {
        if (value) {
            return null;
        }

        return new IllegalArgumentException(String.format("argumentName: %s, message: %s", argumentName, String.format(messageTemplate, messageTemplateParams)));
    }

    private static NullPointerException checkNotNullOrReturnException(Object val, String argumentName, String messageTemplate, Object... messageTemplateParams) {
        if (val != null) {
            return null;
        }

        return new NullPointerException(String.format("argumentName: %s, message: %s", argumentName, String.format(messageTemplate, messageTemplateParams)));
    }

    public static BadRequestException checkRequestOrReturnException(boolean value, String argumentName, String messageTemplate, Object... messageTemplateParams) {
        if (value) {
            return null;
        }

        return new BadRequestException(String.format("argumentName: %s, message: %s", argumentName, String.format(messageTemplate, messageTemplateParams)));
    }

    @SuppressWarnings("unchecked")
    public static <O, I> O as(I i, Class<O> klass) {
        if (i == null) {
            return null;
        }

        if (klass.isInstance(i)) {
            return (O) i;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> List<V> immutableListOf() {
        return Collections.EMPTY_LIST;
    }

    public static <K, V> Map<K, V>immutableMapOf(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1,  v1);
        map = Collections.unmodifiableMap(map);
        return map;
    }

    public static <V> V firstOrDefault(List<V> list) {
        return list.size() > 0? list.get(0) : null ;
    }

    public static class ValueHolder<V> {

        public ValueHolder() {
        }

        public ValueHolder(V v) {
            this.v = v;
        }
        public V v;

        public static <T> ValueHolder<T> initialize(T v) {
            return new ValueHolder<>(v);
        }
    }

    public static <K, V> boolean tryGetValue(Map<K, V> dictionary, K key, ValueHolder<V> holder) {
        // doesn't work for dictionary with null value
        holder.v = dictionary.get(key);
        return holder.v != null;
    }

    public static <K, V> boolean tryRemove(Map<K, V> dictionary, K key, ValueHolder<V> holder) {
        // doesn't work for dictionary with null value
        holder.v = dictionary.remove(key);
        return holder.v != null;
    }

    public static <T> T parse(String itemResponseBodyAsString, Class<T> itemClassType) {
        if (StringUtils.isEmpty(itemResponseBodyAsString)) {
            return null;
        }
        try {
            return getSimpleObjectMapper().readValue(itemResponseBodyAsString, itemClassType);
        } catch (IOException e) {
            throw new IllegalStateException(
                String.format("Failed to parse string [%s] to POJO.", itemResponseBodyAsString), e);
        }
    }

    public static <T> T parse(byte[] item, Class<T> itemClassType) {
        if (Utils.isEmpty(item)) {
            return null;
        }

        try {
            return getSimpleObjectMapper().readValue(item, itemClassType);
        } catch (IOException e) {
            throw new IllegalStateException(
                String.format("Failed to parse byte-array %s to POJO.", Arrays.toString(item)), e);
        }
    }

    public static <T> T parse(byte[] item, Class<T> itemClassType, ItemDeserializer itemDeserializer) {
        if (Utils.isEmpty(item)) {
            return null;
        }

        if (itemDeserializer == null) {
            return Utils.parse(item, itemClassType);
        }

        return itemDeserializer.parseFrom(itemClassType, item);
    }

    public static ByteBuffer serializeJsonToByteBuffer(ObjectMapper objectMapper, Object object) {
        try {
            ByteBufferOutputStream byteBufferOutputStream = new ByteBufferOutputStream(ONE_KB);
            objectMapper.writeValue(byteBufferOutputStream, object);
            return byteBufferOutputStream.asByteBuffer();
        } catch (IOException e) {
            // TODO moderakh: on serialization/deserialization failure we should throw CosmosException here and elsewhere
            throw new IllegalArgumentException("Failed to serialize the object into json", e);
        }
    }

    public static boolean isEmpty(byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }

    public static String utf8StringFromOrNull(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void setContinuationTokenAndMaxItemCount(CosmosPagedFluxOptions pagedFluxOptions, CosmosQueryRequestOptions cosmosQueryRequestOptions) {
        if (pagedFluxOptions == null) {
            return;
        }
        if (pagedFluxOptions.getRequestContinuation() != null) {
            ModelBridgeInternal.setQueryRequestOptionsContinuationToken(cosmosQueryRequestOptions, pagedFluxOptions.getRequestContinuation());
        }
        if (pagedFluxOptions.getMaxItemCount() != null) {
            ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(cosmosQueryRequestOptions, pagedFluxOptions.getMaxItemCount());
        } else {
            ImplementationBridgeHelpers
                .CosmosQueryRequestOptionsHelper
                .getCosmosQueryRequestOptionsAccessor()
                .applyMaxItemCount(cosmosQueryRequestOptions, pagedFluxOptions);

            // if query request options also don't have maxItemCount set, apply defaults
            if (pagedFluxOptions.getMaxItemCount() == null) {
                ModelBridgeInternal.setQueryRequestOptionsMaxItemCount(
                    cosmosQueryRequestOptions, Constants.Properties.DEFAULT_MAX_PAGE_SIZE);
                pagedFluxOptions.setMaxItemCount(Constants.Properties.DEFAULT_MAX_PAGE_SIZE);
            }
        }
    }

    public static CosmosChangeFeedRequestOptions getEffectiveCosmosChangeFeedRequestOptions(
        CosmosPagedFluxOptions pagedFluxOptions,
        CosmosChangeFeedRequestOptions cosmosChangeFeedRequestRequestOptions) {

        checkNotNull(
            cosmosChangeFeedRequestRequestOptions,
            "Argument 'cosmosChangeFeedRequestRequestOptions' must not be null");

        return ModelBridgeInternal
            .getEffectiveChangeFeedRequestOptions(
                cosmosChangeFeedRequestRequestOptions, pagedFluxOptions);
    }

    static String escapeNonAscii(String partitionKeyJson) {
        // if all are ascii original string will be returned, and avoids copying data.
        StringBuilder sb = null;
        for (int i = 0; i < partitionKeyJson.length(); i++) {
            int val = partitionKeyJson.charAt(i);
            if (val > 127) {
                if (sb == null) {
                    sb = new StringBuilder(partitionKeyJson.length());
                    sb.append(partitionKeyJson, 0, i);
                }
                sb.append("\\u").append(String.format("%04X", val));
            } else {
                if (sb != null) {
                    sb.append(partitionKeyJson.charAt(i));
                }
            }
        }

        if (sb == null) {
            // all are ascii character
            return partitionKeyJson;
        } else {
            return sb.toString();
        }
    }

    public static byte[] toByteArray(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public static String toJson(ObjectMapper mapper, ObjectNode object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert JSON to STRING", e);
        }
    }

    public static long getMaxIntegratedCacheStalenessInMillis(DedicatedGatewayRequestOptions dedicatedGatewayRequestOptions) {
        Duration maxIntegratedCacheStaleness = dedicatedGatewayRequestOptions.getMaxIntegratedCacheStaleness();
        if (maxIntegratedCacheStaleness.toNanos() > 0 && maxIntegratedCacheStaleness.toMillis() <= 0) {
            throw new IllegalArgumentException("MaxIntegratedCacheStaleness granularity is milliseconds");
        }
        if (maxIntegratedCacheStaleness.toMillis() < 0) {
            throw new IllegalArgumentException("MaxIntegratedCacheStaleness duration cannot be negative");
        }
        return maxIntegratedCacheStaleness.toMillis();
    }
}
