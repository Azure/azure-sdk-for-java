// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.phonenumbers.implementation.PhoneNumbersImpl;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersPurchasePhoneNumbersResponse;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumberPurchaseRequest;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersSearchAvailablePhoneNumbersResponse;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersReleasePhoneNumberResponse;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersUpdateCapabilitiesResponse;
import com.azure.communication.phonenumbers.models.AcquiredPhoneNumber;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilitiesRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberOperationStatus;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumbersClientBuilder.class, isAsync = true)
public final class PhoneNumbersAsyncClient {
    private final ClientLogger logger = new ClientLogger(PhoneNumbersAsyncClient.class);
    private final PhoneNumbersImpl client;
    private final Duration defaultPollInterval = Duration.ofSeconds(1);

    PhoneNumbersAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        this.client = phoneNumberAdminClient.getPhoneNumbers();
    }

    /**
     * Gets information about an acquired phone number.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return {@link AcquiredPhoneNumber} representing the acquired telephone number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcquiredPhoneNumber> getPhoneNumber(String phoneNumber) {
        if (Objects.isNull(phoneNumber)) {
            return monoError(logger, new NullPointerException("'phoneNumber' cannot be null."));
        }
        return client.getByNumberAsync(phoneNumber);
    }

    /**
     * Gets information about an acquired phone number with response.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return {@link AcquiredPhoneNumber} representing the acquired telephone number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcquiredPhoneNumber>> getPhoneNumberWithResponse(String phoneNumber) {
        if (Objects.isNull(phoneNumber)) {
            return monoError(logger, new NullPointerException("'phoneNumber' cannot be null."));
        }
        return client.getByNumberWithResponseAsync(phoneNumber);
    }

    /**
     * Gets the list of the acquired phone numbers.
     *
     * @return A {@link PagedFlux} of {@link AcquiredPhoneNumber} instances representing acquired telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AcquiredPhoneNumber> listPhoneNumbers() {
        try {
            return client.listPhoneNumbersAsync(null, null);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Starts the search for available phone numbers to purchase.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param searchRequest {@link PhoneNumberSearchRequest} specifying the search request
     * until it gets a result from the server
     * @return A {@link PollerFlux} object with the reservation result
     * @throws NullPointerException if {@code countryCode} or {@code searchRequest} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
        String countryCode, PhoneNumberSearchRequest searchRequest) {
        return beginSearchAvailablePhoneNumbers(countryCode, searchRequest, null);
    }

    PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
        String countryCode, PhoneNumberSearchRequest searchRequest, Context context) {
        try {
            Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
            Objects.requireNonNull(searchRequest, "'searchRequest' cannot be null.");

            return new PollerFlux<>(defaultPollInterval,
                searchAvailableNumbersInitOperation(countryCode, searchRequest, context),
                pollOperation(),
                cancelOperation(),
                searchAvailableNumbersFetchFinalResultOperation());

        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberOperation>>
        searchAvailableNumbersInitOperation(String countryCode, PhoneNumberSearchRequest searchRequest, Context context) {
        return (pollingContext) -> {
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return client.searchAvailablePhoneNumbersWithResponseAsync(countryCode, searchRequest, contextValue)
                .flatMap((PhoneNumbersSearchAvailablePhoneNumbersResponse response) -> {
                    pollingContext.setData("operationId", response.getDeserializedHeaders().getOperationId());
                    pollingContext.setData("searchId", response.getDeserializedHeaders().getSearchId());
                    return client.getOperationAsync(pollingContext.getData("operationId"));
                });
            });
        };
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PollResponse<PhoneNumberOperation>>>
        pollOperation() {
        return (pollingContext) -> { 
            return client.getOperationAsync(pollingContext.getData("operationId"))
            .flatMap(operation -> {
                if (operation.getStatus().toString().equalsIgnoreCase(PhoneNumberOperationStatus.SUCCEEDED.toString())) {
                    return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, operation));
                } else if (operation.getStatus().toString().equalsIgnoreCase(PhoneNumberOperationStatus.FAILED.toString())) {
                    return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.FAILED, operation));
                } else if (operation.getStatus().toString().equalsIgnoreCase(PhoneNumberOperationStatus.NOT_STARTED.toString())) {
                    return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.NOT_STARTED, operation));
                }
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, operation));
            });
        };
    }

    private BiFunction<PollingContext<PhoneNumberOperation>,
        PollResponse<PhoneNumberOperation>, Mono<PhoneNumberOperation>>
        cancelOperation() {
        return (pollingContext, firstResponse) -> {
            if (firstResponse == null || firstResponse.getValue() == null) {
                return Mono.error(logger.logExceptionAsError(
                    new IllegalArgumentException("Cannot cancel a poll response that never started.")));
            }
            String operationId = firstResponse.getValue().getId();
            if (!CoreUtils.isNullOrEmpty(operationId)) {
                logger.info("Cancelling search available phone numbers operation for operation id: {}", operationId);
                return client.cancelOperationAsync(operationId).thenReturn(firstResponse.getValue());
            }
            return Mono.empty();
        };
    }
    
    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberSearchResult>>
        searchAvailableNumbersFetchFinalResultOperation() {
        return (pollingContext) -> {
            return client.getSearchResultAsync(pollingContext.getData("searchId"));
        };
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated with a given id.
     *
     * @param searchId ID of the search
     * @return A {@link PollerFlux} object.
     * @throws NullPointerException if {@code searchId} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, Void> beginPurchasePhoneNumbers(String searchId) {
        return beginPurchasePhoneNumbers(searchId, null);
    }

    PollerFlux<PhoneNumberOperation, Void> beginPurchasePhoneNumbers(String searchId, Context context) {
        try {
            Objects.requireNonNull(searchId, "'searchId' cannot be null.");
            return new PollerFlux<>(defaultPollInterval,
                purchaseNumbersInitOperation(searchId, context),
                pollOperation(),
                (pollingContext, firstResponse) -> Mono.error(logger.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
                (pollingContext) -> Mono.empty());
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberOperation>> 
        purchaseNumbersInitOperation(String searchId, Context context) {
        return (pollingContext) -> {
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return client.purchasePhoneNumbersWithResponseAsync(new PhoneNumberPurchaseRequest().setSearchId(searchId), contextValue)
                .flatMap((PhoneNumbersPurchasePhoneNumbersResponse response) -> {
                    pollingContext.setData("operationId", response.getDeserializedHeaders().getOperationId());
                    return client.getOperationAsync(pollingContext.getData("operationId"));
                });
            });
        };
    }

    /**
     * Begins release of an acquired phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return A {@link PollerFlux} object.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, Void> beginReleasePhoneNumber(String phoneNumber) {
        return beginReleasePhoneNumber(phoneNumber, null);
    }

    PollerFlux<PhoneNumberOperation, Void> beginReleasePhoneNumber(String phoneNumber, Context context) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            return new PollerFlux<>(defaultPollInterval,
                releaseNumberInitOperation(phoneNumber, context),
                pollOperation(),
                (pollingContext, firstResponse) -> Mono.error(logger.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
                (pollingContext) -> Mono.empty());
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberOperation>> 
        releaseNumberInitOperation(String phoneNumber, Context context) {
        return (pollingContext) -> {
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return client.releasePhoneNumberWithResponseAsync(phoneNumber, contextValue)
                .flatMap((PhoneNumbersReleasePhoneNumberResponse response) -> {
                    pollingContext.setData("operationId", response.getDeserializedHeaders().getOperationId());
                    return client.getOperationAsync(pollingContext.getData("operationId"));
                });
            });
        };
    }

    /**
     * Update capabilities of an acquired phone number.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param capabilitiesUpdateRequest Update capabilities of an acquired phone number.
     * @return A {@link PollerFlux} object.
     * @throws NullPointerException if {@code phoneNumber} or {@code capabilitiesUpdateRequest} is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, AcquiredPhoneNumber> 
        beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest) {
        return beginUpdatePhoneNumberCapabilities(phoneNumber, capabilitiesUpdateRequest, null);
    }

    PollerFlux<PhoneNumberOperation, AcquiredPhoneNumber> 
        beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest, Context context) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            Objects.requireNonNull(capabilitiesUpdateRequest, "'capabilitiesUpdateRequest' cannot be null.");

            return new PollerFlux<>(defaultPollInterval,
                updateNumberCapabilitiesInitOperation(phoneNumber, capabilitiesUpdateRequest, context),
                pollOperation(),
                (pollingContext, firstResponse) -> Mono.error(logger.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
                updateNumberCapabilitiesFetchFinalResultOperation(phoneNumber));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberOperation>> 
        updateNumberCapabilitiesInitOperation(String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest, Context context) {
        return (pollingContext) -> {
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return client.updateCapabilitiesWithResponseAsync(phoneNumber, capabilitiesUpdateRequest, contextValue)
                .flatMap((PhoneNumbersUpdateCapabilitiesResponse response) -> {
                    pollingContext.setData("operationId", response.getDeserializedHeaders().getOperationId());
                    return client.getOperationAsync(pollingContext.getData("operationId"));
                });
            });
        };
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<AcquiredPhoneNumber>>
        updateNumberCapabilitiesFetchFinalResultOperation(String phoneNumber) {
        return (pollingContext) -> {
            return client.getByNumberAsync(phoneNumber);
        };
    }

}
