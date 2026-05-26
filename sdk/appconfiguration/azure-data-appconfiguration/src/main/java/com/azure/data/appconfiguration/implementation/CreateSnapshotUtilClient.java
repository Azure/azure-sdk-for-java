// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A helper util client for creating a snapshot.
 *
 * <p>Mirrors the original polling shape on top of the new protocol-style
 * {@link AzureAppConfigurationImpl#createSnapshotWithResponse} / {@code Async} +
 * {@link AzureAppConfigurationImpl#getOperationDetailsWithResponse} / {@code Async} (BinaryData) methods.</p>
 */
public class CreateSnapshotUtilClient {
    private static final ClientLogger LOGGER = new ClientLogger(CreateSnapshotUtilClient.class);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    private static final String CREATE_SNAPSHOT_CONTENT_TYPE = "application/vnd.microsoft.appconfig.snapshot+json";
    private static final HttpHeaderName OPERATION_LOCATION = HttpHeaderName.fromString("Operation-Location");
    private static final String NOT_STARTED = "NotStarted";
    private static final String RUNNING = "Running";
    private static final String SUCCEEDED = "Succeeded";

    private final AzureAppConfigurationImpl service;

    public CreateSnapshotUtilClient(AzureAppConfigurationImpl service) {
        this.service = service;
    }

    public PollerFlux<PollOperationDetails, ConfigurationSnapshot> beginCreateSnapshot(String name,
        ConfigurationSnapshot snapshot) {
        try {
            final BinaryData body = BinaryData.fromObject(snapshot);
            return new PollerFlux<>(DEFAULT_POLL_INTERVAL,
                activationOperationAsync(service
                    .createSnapshotWithResponseAsync(CREATE_SNAPSHOT_CONTENT_TYPE, name, body, new RequestOptions())
                    .map(response -> toPollOperationDetails(response.getHeaders().getValue(OPERATION_LOCATION)))),
                pollingOperationAsync(opId -> service.getOperationDetailsWithResponseAsync(name, new RequestOptions())),
                (ctx, activationResponse) -> Mono.error(new RuntimeException("Cancellation is not supported.")),
                fetchingOperationAsync(opId -> service.getSnapshotWithResponseAsync(name, new RequestOptions())
                    .flatMap(res -> Mono.justOrEmpty(deserializeSnapshot(res.getValue())))));
        } catch (Exception e) {
            return PollerFlux.error(e);
        }
    }

    public SyncPoller<PollOperationDetails, ConfigurationSnapshot> beginCreateSnapshot(String name,
        ConfigurationSnapshot snapshot, Context context) {
        try {
            final Context finalContext = context == null ? Context.NONE : context;
            return SyncPoller.createPoller(DEFAULT_POLL_INTERVAL,
                ctx -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    activationOperationSync(name, snapshot, finalContext).apply(ctx)),
                pollingOperationSync(opId -> service.getOperationDetailsWithResponse(name, withContext(finalContext))),
                (ctx, activationResponse) -> {
                    throw LOGGER.logExceptionAsError(new RuntimeException("Cancellation is not supported."));
                }, fetchingOperationSync(opId -> deserializeSnapshot(
                    service.getSnapshotWithResponse(name, withContext(finalContext)).getValue())));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private Function<PollingContext<PollOperationDetails>, Mono<PollOperationDetails>>
        activationOperationAsync(Mono<PollOperationDetails> activationResult) {
        return ctx -> {
            try {
                return activationResult;
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<PollOperationDetails>, PollOperationDetails> activationOperationSync(String name,
        ConfigurationSnapshot snapshot, Context context) {
        return ctx -> {
            try {
                final BinaryData body = BinaryData.fromObject(snapshot);
                Response<BinaryData> response = service.createSnapshotWithResponse(CREATE_SNAPSHOT_CONTENT_TYPE, name,
                    body, withContext(context));
                return toPollOperationDetails(response.getHeaders().getValue(OPERATION_LOCATION));
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        };
    }

    private Function<PollingContext<PollOperationDetails>, Mono<PollResponse<PollOperationDetails>>>
        pollingOperationAsync(Function<String, Mono<Response<BinaryData>>> pollingFunction) {
        return ctx -> {
            try {
                final PollResponse<PollOperationDetails> last = ctx.getLatestResponse();
                String opId = last.getValue() == null ? null : last.getValue().getOperationId();
                return pollingFunction.apply(opId).map(response -> processOperationDetails(response, last));
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<PollOperationDetails>, PollResponse<PollOperationDetails>>
        pollingOperationSync(Function<String, Response<BinaryData>> pollingFunction) {
        return ctx -> {
            try {
                final PollResponse<PollOperationDetails> last = ctx.getLatestResponse();
                String opId = last.getValue() == null ? null : last.getValue().getOperationId();
                return processOperationDetails(pollingFunction.apply(opId), last);
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        };
    }

    private Function<PollingContext<PollOperationDetails>, Mono<ConfigurationSnapshot>>
        fetchingOperationAsync(Function<String, Mono<ConfigurationSnapshot>> fetchingFunction) {
        return ctx -> {
            try {
                String opId = ctx.getLatestResponse().getValue() == null
                    ? null
                    : ctx.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(opId);
            } catch (RuntimeException ex) {
                return monoError(LOGGER, ex);
            }
        };
    }

    private Function<PollingContext<PollOperationDetails>, ConfigurationSnapshot>
        fetchingOperationSync(Function<String, ConfigurationSnapshot> fetchingFunction) {
        return ctx -> {
            try {
                String opId = ctx.getLatestResponse().getValue() == null
                    ? null
                    : ctx.getLatestResponse().getValue().getOperationId();
                return fetchingFunction.apply(opId);
            } catch (RuntimeException ex) {
                throw LOGGER.logExceptionAsError(ex);
            }
        };
    }

    private static PollResponse<PollOperationDetails> processOperationDetails(Response<BinaryData> response,
        PollResponse<PollOperationDetails> previous) {
        LongRunningOperationStatus status = LongRunningOperationStatus.IN_PROGRESS;
        if (response != null && response.getValue() != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = response.getValue().toObject(Map.class);
                Object statusObj = body == null ? null : body.get("status");
                if (statusObj != null) {
                    String s = statusObj.toString();
                    if (NOT_STARTED.equalsIgnoreCase(s) || RUNNING.equalsIgnoreCase(s)) {
                        status = LongRunningOperationStatus.IN_PROGRESS;
                    } else if (SUCCEEDED.equalsIgnoreCase(s)) {
                        status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                    } else {
                        status = LongRunningOperationStatus.fromString(s, true);
                    }
                }
            } catch (RuntimeException ignored) {
                // Fall back to IN_PROGRESS; the next poll will retry.
            }
        }
        return new PollResponse<>(status, previous.getValue());
    }

    private static PollOperationDetails toPollOperationDetails(String operationLocation) {
        // PollOperationDetails has no public no-arg constructor; round-trip a Map through BinaryData to populate it.
        Map<String, String> map = new HashMap<>();
        map.put("id", operationLocation);
        return BinaryData.fromObject(map).toObject(PollOperationDetails.class);
    }

    private static ConfigurationSnapshot deserializeSnapshot(BinaryData body) {
        return body == null ? null : body.toObject(ConfigurationSnapshot.class);
    }

    private static RequestOptions withContext(Context context) {
        RequestOptions options = new RequestOptions();
        if (context != null && context != Context.NONE) {
            options.setContext(context);
        }
        return options;
    }
}
