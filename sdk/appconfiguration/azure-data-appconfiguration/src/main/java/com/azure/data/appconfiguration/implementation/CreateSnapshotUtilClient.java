// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.implementation.models.CreateSnapshotHeaders;
import com.azure.data.appconfiguration.implementation.models.OperationDetails;
import com.azure.data.appconfiguration.implementation.models.State;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.data.appconfiguration.implementation.models.State.NOT_STARTED;
import static com.azure.data.appconfiguration.implementation.models.State.RUNNING;
import static com.azure.data.appconfiguration.implementation.models.State.SUCCEEDED;

/**
 * A helper util client for creating a snapshot.
 */
public class CreateSnapshotUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(CreateSnapshotUtilClient.class);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    private final AzureAppConfigurationImpl service;

    public CreateSnapshotUtilClient(AzureAppConfigurationImpl service) {
        this.service = service;
    }

    public PollerFlux<PollOperationDetails, ConfigurationSnapshot> beginCreateSnapshot(String name,
        ConfigurationSnapshot snapshot) {
        try {
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL, activationOperation(
                service.createSnapshotWithResponseAsync(name, snapshot, Context.NONE).map(response -> {
                    final Map<String, String> pollResponse = new HashMap<>();
                    pollResponse.put("id", response.getDeserializedHeaders().getOperationLocation());
                    return BinaryData.fromObject(pollResponse).toObject(PollOperationDetails.class);
                })), pollingOperation(operationId -> service.getOperationDetailsWithResponseAsync(name, Context.NONE)),
                (pollingContext, activationResponse) -> Mono
                    .error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperation(
                    operationId -> service.getSnapshotWithResponseAsync(name, null, null, null, Context.NONE)
                        .flatMap(res -> Mono.justOrEmpty(res.getValue()))));
        } catch (Exception e) {
            return PollerFlux.error(e);
        }
    }

    public SyncPoller<PollOperationDetails, ConfigurationSnapshot> beginCreateSnapshot(String name,
        ConfigurationSnapshot snapshot, Context context) {
        try {
            final Context finalContext = getNotNullContext(context);
            return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
                cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    activationOperationSync(name, snapshot, finalContext).apply(cxt)),
                pollingOperationSync(operationId -> service.getOperationDetailsWithResponse(name, finalContext)),
                (pollingContext, activationResponse) -> {
                    throw LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported."));
                }, fetchingOperationSync(
                    operationId -> service.getSnapshotWithResponse(name, null, null, null, finalContext).getValue()));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    // Activation operation
    private Function<PollingContext<PollOperationDetails>, Mono<PollOperationDetails>>
        activationOperation(Mono<PollOperationDetails> operationResult) {
        return pollingContext -> {
            try {
                return operationResult;
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<PollOperationDetails>, PollOperationDetails> activationOperationSync(String name,
        ConfigurationSnapshot snapshot, Context context) {
        return pollingContext -> {
            try {
                final Context finalContext = getNotNullContext(context);
                final ResponseBase<CreateSnapshotHeaders, ConfigurationSnapshot> snapshotWithResponse
                    = service.createSnapshotWithResponse(name, snapshot, finalContext);
                final Map<String, String> pollResponse = new HashMap<>();
                pollResponse.put("id", snapshotWithResponse.getDeserializedHeaders().getOperationLocation());
                return BinaryData.fromObject(pollResponse).toObject(PollOperationDetails.class);
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        };
    }

    // Polling operation
    private Function<PollingContext<PollOperationDetails>, Mono<PollResponse<PollOperationDetails>>>
        pollingOperation(Function<String, Mono<Response<OperationDetails>>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<PollOperationDetails> pollResponse = pollingContext.getLatestResponse();
                final String operationId = pollResponse.getValue().getOperationId();
                return pollingFunction.apply(operationId)
                    .flatMap(modelResponse -> Mono.just(processResponse(modelResponse, pollResponse)));
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<PollOperationDetails>, PollResponse<PollOperationDetails>>
        pollingOperationSync(Function<String, Response<OperationDetails>> pollingFunction) {
        return pollingContext -> {
            try {
                final PollResponse<PollOperationDetails> pollResponse = pollingContext.getLatestResponse();
                return processResponse(pollingFunction.apply(pollResponse.getValue().getOperationId()), pollResponse);
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        };
    }

    // Fetching operation
    private Function<PollingContext<PollOperationDetails>, Mono<ConfigurationSnapshot>>
        fetchingOperation(Function<String, Mono<ConfigurationSnapshot>> fetchingFunction) {
        return pollingContext -> {
            try {
                String operationId = pollingContext.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(operationId);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<PollOperationDetails>, ConfigurationSnapshot>
        fetchingOperationSync(Function<String, ConfigurationSnapshot> fetchingFunction) {
        return pollingContext -> {
            try {
                String operationId = pollingContext.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(operationId);
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        };
    }

    private PollResponse<PollOperationDetails> processResponse(Response<OperationDetails> response,
        PollResponse<PollOperationDetails> operationResultPollResponse) {
        LongRunningOperationStatus status;
        State state = response.getValue().getStatus();
        if (NOT_STARTED.equals(state) || RUNNING.equals(state)) {
            status = LongRunningOperationStatus.IN_PROGRESS;
        } else if (SUCCEEDED.equals(state)) {
            status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else {
            status = LongRunningOperationStatus.fromString(response.getValue().toString(), true);
        }
        return new PollResponse<>(status, operationResultPollResponse.getValue());
    }

    /**
     * Get the non-null {@link Context}. The default value is {@link Context#NONE}.
     *
     * @param context It offers a means of passing arbitrary data (key-value pairs) to pipeline policies.
     * Most applications do not need to pass arbitrary data to the pipeline and can pass Context.NONE or null.
     *
     * @return The Context.
     */
    private static Context getNotNullContext(Context context) {
        return context == null ? Context.NONE : context;
    }
}
