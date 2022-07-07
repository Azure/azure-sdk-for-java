package com.azure.iot.deviceupdate.implementation;

import com.azure.core.exception.AzureException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.OperationResourcePollingStrategy;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

import static com.azure.iot.deviceupdate.implementation.PollingUtils.convertResponse;
import static com.azure.iot.deviceupdate.implementation.PollingUtils.getAbsolutePath;
import static com.azure.iot.deviceupdate.implementation.PollingUtils.serializeResponse;


public class OperationResourcePollingStrategyWithEndpoint<T, U> extends OperationResourcePollingStrategy<T, U>  {
    private static final ClientLogger LOGGER = new ClientLogger(OperationResourcePollingStrategy.class);

    private final String endpoint;
    private final String operationLocationHeaderName;
    private final ObjectSerializer serializer;
    private final HttpPipeline httpPipeline;
    private final Context context;

    /**
     * Creates an instance of the operation resource polling strategy.
     *
     * @param httpPipeline an instance of {@link HttpPipeline} to send requests with.
     * @param endpoint an endpoint for creating an absolute path when the path itself is relative.
     * @param serializer a custom serializer for serializing and deserializing polling responses.
     * @param operationLocationHeaderName a custom header for polling the long running operation.
     * @param context an instance of {@link com.azure.core.util.Context}.
     */
    public OperationResourcePollingStrategyWithEndpoint(HttpPipeline httpPipeline, String endpoint, ObjectSerializer serializer,
                                            String operationLocationHeaderName, Context context) {
        super(httpPipeline, serializer, operationLocationHeaderName, context);
        this.operationLocationHeaderName = operationLocationHeaderName != null ? operationLocationHeaderName : "Operation-Location";
        this.endpoint = endpoint;
        this.serializer = (ObjectSerializer)(serializer != null ? serializer : new DefaultJsonSerializer());
        this.httpPipeline = (HttpPipeline) Objects.requireNonNull(httpPipeline, "'httpPipeline' cannot be null");
        this.context = context == null ? Context.NONE : context;
    }

    @Override
    public Mono<Boolean> canPoll(Response<?> initialResponse) {
        HttpHeader operationLocationHeader = initialResponse.getHeaders().get(operationLocationHeaderName);
        if (operationLocationHeader != null) {
            try {
                new URL(getAbsolutePath(operationLocationHeader.getValue(), endpoint, LOGGER));
                return Mono.just(true);
            } catch (MalformedURLException e) {
                return Mono.just(false);
            }
        }
        return Mono.just(false);
    }

    @Override
    public Mono<PollResponse<T>> onInitialResponse(Response<?> response, PollingContext<T> pollingContext,
                                                   TypeReference<T> pollResponseType) {
        HttpHeader operationLocationHeader = response.getHeaders().get(operationLocationHeaderName);
        HttpHeader locationHeader = response.getHeaders().get(PollingConstants.LOCATION);
        if (operationLocationHeader != null) {
            pollingContext.setData(operationLocationHeaderName,
                getAbsolutePath(operationLocationHeader.getValue(), endpoint, LOGGER));
        }
        if (locationHeader != null) {
            pollingContext.setData(PollingConstants.LOCATION,
                getAbsolutePath(locationHeader.getValue(), endpoint, LOGGER));
        }
        pollingContext.setData(PollingConstants.HTTP_METHOD, response.getRequest().getHttpMethod().name());
        pollingContext.setData(PollingConstants.REQUEST_URL, response.getRequest().getUrl().toString());

        if (response.getStatusCode() == 200
            || response.getStatusCode() == 201
            || response.getStatusCode() == 202
            || response.getStatusCode() == 204) {
            String retryAfterValue = response.getHeaders().getValue(PollingConstants.RETRY_AFTER);
            Duration retryAfter = retryAfterValue == null ? null : Duration.ofSeconds(Long.parseLong(retryAfterValue));
            return convertResponse(response.getValue(), serializer, pollResponseType)
                .map(value -> new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, value, retryAfter))
                .switchIfEmpty(Mono.fromSupplier(() -> new PollResponse<>(
                    LongRunningOperationStatus.IN_PROGRESS, null, retryAfter)));
        } else {
            return Mono.error(new AzureException(String.format("Operation failed or cancelled with status code %d,"
                    + ", '%s' header: %s, and response body: %s", response.getStatusCode(), operationLocationHeaderName,
                operationLocationHeader, serializeResponse(response.getValue(), serializer))));
        }
    }

    @Override
    public Mono<U> getResult(PollingContext<T> pollingContext, TypeReference<U> resultType) {
        if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.FAILED) {
            return Mono.error(new AzureException("Long running operation failed."));
        } else if (pollingContext.getLatestResponse().getStatus() == LongRunningOperationStatus.USER_CANCELLED) {
            return Mono.error(new AzureException("Long running operation cancelled."));
        }
        String finalGetUrl = getAbsolutePath(pollingContext.getData(PollingConstants.RESOURCE_LOCATION), endpoint, LOGGER);
        if (finalGetUrl == null) {
            String httpMethod = pollingContext.getData(PollingConstants.HTTP_METHOD);
            if (HttpMethod.PUT.name().equalsIgnoreCase(httpMethod)
                || HttpMethod.PATCH.name().equalsIgnoreCase(httpMethod)) {
                finalGetUrl = pollingContext.getData(PollingConstants.REQUEST_URL);
            } else if (HttpMethod.POST.name().equalsIgnoreCase(httpMethod)
                && pollingContext.getData(PollingConstants.LOCATION) != null) {
                finalGetUrl = pollingContext.getData(PollingConstants.LOCATION);
            } else {
                return Mono.error(new AzureException("Cannot get final result"));
            }
        }
        if (finalGetUrl == null) {
            String latestResponseBody = pollingContext.getData(PollingConstants.POLL_RESPONSE_BODY);
            return PollingUtils.deserializeResponse(BinaryData.fromString(latestResponseBody), serializer, resultType);
        } else {
            HttpRequest request = new HttpRequest(HttpMethod.GET, finalGetUrl);
            return FluxUtil.withContext(context1 -> httpPipeline.send(request,
                    CoreUtils.mergeContexts(context1, this.context)))
                .flatMap(HttpResponse::getBodyAsByteArray)
                .map(BinaryData::fromBytes)
                .flatMap(binaryData -> PollingUtils.deserializeResponse(binaryData, serializer, resultType));
        }
    }
}
