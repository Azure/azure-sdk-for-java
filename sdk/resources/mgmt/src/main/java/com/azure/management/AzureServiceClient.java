// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.implementation.polling.PollerFactory;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Enumeration;

/**
 * ServiceClient is the abstraction for accessing REST operations and their payload data types.
 */
public abstract class AzureServiceClient {

    protected AzureServiceClient(HttpPipeline httpPipeline, AzureEnvironment environment) {
        ((AzureJacksonAdapter) serializerAdapter).serializer().registerModule(DateTimeDeserializer.getModule());
    }

    /**
     * The default User-Agent header. Override this method to override the user agent.
     *
     * @return the user agent string.
     */
    public String userAgent() {
        return String.format("Azure-SDK-For-Java/%s OS:%s MacAddressHash:%s Java:%s",
            getClass().getPackage().getImplementationVersion(),
            OS,
            MAC_ADDRESS_HASH,
            JAVA_VERSION);
    }

    private static final String MAC_ADDRESS_HASH;
    private static final String OS;
    private static final String OS_NAME;
    private static final String OS_VERSION;
    private static final String JAVA_VERSION;
    private static final String SDK_VERSION = "2.0.0";

    static {
        OS_NAME = System.getProperty("os.name");
        OS_VERSION = System.getProperty("os.version");
        OS = OS_NAME + "/" + OS_VERSION;
        String macAddress = "Unknown";
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null) {
                    macAddress = getSha256(mac);
                    break;
                }
            }
        } catch (Throwable t) {
            // It's okay ignore mac address hash telemetry
        }
        MAC_ADDRESS_HASH = macAddress;
        String version = System.getProperty("java.version");
        JAVA_VERSION = version != null ? version : "Unknown";
    }

    private final SerializerAdapter serializerAdapter = new AzureJacksonAdapter();

    private String sdkName;

    /**
     * Gets serializer adapter for JSON serialization/de-serialization.
     *
     * @return the serializer adapter.
     */
    public SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * Gets default client context.
     *
     * @return the default client context.
     */
    public Context getContext() {
        Context context = new Context("java.version", JAVA_VERSION);
        if (!CoreUtils.isNullOrEmpty(OS_NAME)) {
            context = context.addData("os.name", OS_NAME);
        }
        if (!CoreUtils.isNullOrEmpty(OS_VERSION)) {
            context = context.addData("os.version", OS_VERSION);
        }
        if (sdkName == null) {
            String packageName = this.getClass().getPackage().getName();
            if (packageName.endsWith(".models")) {
                sdkName = packageName.substring(0, packageName.length() - ".models".length());
            }
        }
        context = context.addData("Sdk-Name", sdkName);
        context = context.addData("Sdk-Version", SDK_VERSION);
        return context;
    }

    /**
     * Gets long running operation result.
     *
     * @param lroInit the raw response of init operation.
     * @param httpPipeline the http pipeline.
     * @param pollResultType type of poll result.
     * @param finalResultType type of final result.
     * @param <T> type of poll result.
     * @param <U> type of final result.
     * @return poller flux for poll result and final result.
     */
    public <T, U> PollerFlux<PollResult<T>, U> getLroResultAsync(Mono<SimpleResponse<Flux<ByteBuffer>>> lroInit,
                                                                 HttpPipeline httpPipeline,
                                                                 Type pollResultType, Type finalResultType) {
        return PollerFactory.create(
            getSerializerAdapter(),
            httpPipeline,
            pollResultType,
            finalResultType,
            SdkContext.getLroRetryDuration(),
            activationOperation(lroInit)
        );
    }

    private Mono<Response<Flux<ByteBuffer>>> activationOperation(Mono<SimpleResponse<Flux<ByteBuffer>>> lroInit) {
        return lroInit.flatMap(fluxSimpleResponse -> Mono.just(fluxSimpleResponse));
    }

    private static String getSha256(byte[] bytes) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes);
            messageDigest.digest(bytes).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    // this should be moved to core-mgmt when stable.
    private static class DateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

        public static SimpleModule getModule() {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(OffsetDateTime.class, new DateTimeDeserializer());
            return module;
        }

        @Override
        public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                    throws IOException, JsonProcessingException {
            String string = jsonParser.getText();
            TemporalAccessor temporal =
                DateTimeFormatter.ISO_DATE_TIME.parseBest(string, OffsetDateTime::from, LocalDateTime::from);
            if (temporal.query(TemporalQueries.offset()) == null) {
                return LocalDateTime.from(temporal).atOffset(ZoneOffset.UTC);
            } else {
                return OffsetDateTime.from(temporal);
            }
        }
    }
}
