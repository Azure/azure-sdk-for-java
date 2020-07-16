// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.polling.PollerFactory;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Map;

/**
 * ServiceClient is the abstraction for accessing REST operations and their payload data types.
 */
public abstract class AzureServiceClient {

    private final ClientLogger logger = new ClientLogger(getClass());

    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure.properties");

    private static final String SDK_VERSION;
    static {
        SDK_VERSION = PROPERTIES.getOrDefault("version", "UnknownVersion");
    }

    private final SerializerAdapter serializerAdapter = new AzureJacksonAdapter();

    private final String sdkName;

    protected AzureServiceClient(HttpPipeline httpPipeline, AzureEnvironment environment) {
        sdkName = this.getClass().getPackage().getName();
        ((AzureJacksonAdapter) serializerAdapter).serializer().registerModule(DateTimeDeserializer.getModule());
    }

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
        return new Context("Sdk-Name", sdkName)
            .addData("Sdk-Version", SDK_VERSION);
    }

    /**
     * Merges default client context with provided context.
     *
     * @param context the context to be merged with default client context.
     * @return the merged context.
     */
    public Context mergeContext(Context context) {
        for (Map.Entry<Object, Object> entry : this.getContext().getValues().entrySet()) {
            context = context.addData(entry.getKey(), entry.getValue());
        }
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
    public <T, U> PollerFlux<PollResult<T>, U> getLroResultAsync(Mono<Response<Flux<ByteBuffer>>> lroInit,
                                                                 HttpPipeline httpPipeline,
                                                                 Type pollResultType, Type finalResultType) {
        return PollerFactory.create(
            getSerializerAdapter(),
            httpPipeline,
            pollResultType,
            finalResultType,
            SdkContext.getLroRetryDuration(),
            lroInit
        );
    }

    /**
     * Gets the final result, or an error, based on last async poll response.
     *
     * @param response the last async poll response.
     * @param <T> type of poll result.
     * @param <U> type of final result.
     * @return the final result, or an error.
     */
    public <T, U> Mono<U> getLroFinalResultOrError(AsyncPollResponse<PollResult<T>, U> response) {
        if (response.getStatus() != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            String errorMessage;
            ManagementError managementError = null;
            if (response.getValue().getError() != null) {
                errorMessage = response.getValue().getError().getMessage();
                String errorBody = response.getValue().getError().getResponseBody();
                if (errorBody != null) {
                    // try to deserialize error body to ManagementError
                    try {
                        managementError = this.getSerializerAdapter().deserialize(
                            errorBody,
                            ManagementError.class,
                            SerializerEncoding.JSON);
                        if (managementError.getCode() == null || managementError.getMessage() == null) {
                            managementError = null;
                        }
                    } catch (IOException ioe) {
                        logger.logThrowableAsWarning(ioe);
                    }
                }
            } else {
                // fallback to default error message
                errorMessage = "Long running operation failed.";
            }
            if (managementError == null) {
                // fallback to default ManagementError
                managementError = new ManagementError(response.getStatus().toString(), errorMessage);
            }
            return Mono.error(new ManagementException(errorMessage, null, managementError));
        } else {
            return response.getFinalResult();
        }
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
