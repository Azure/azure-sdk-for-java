// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.phonenumbers.implementation.PhoneNumbersImpl;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersSearchAvailablePhoneNumbersResponse;
import com.azure.communication.phonenumbers.models.AcquiredPhoneNumber;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilitiesRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberOperationResult;
import com.azure.communication.phonenumbers.models.PhoneNumberOperationStatus;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberUpdateRequest;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcquiredPhoneNumber> getPhoneNumber(String phoneNumber) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            return client.getByNumberAsync(phoneNumber);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets information about an acquired phone number with response.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return {@link AcquiredPhoneNumber} representing the acquired telephone number.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcquiredPhoneNumber>> getPhoneNumberWithResponse(String phoneNumber) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            return client.getByNumberWithResponseAsync(phoneNumber);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
     * Update an acquired phone number.
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param update Update to an acquired phone number.
     * @return A {@link Mono} containing
     * a {@link AcquiredPhoneNumber} representing the acquired phone number
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcquiredPhoneNumber> updatePhoneNumber(String phoneNumber, PhoneNumberUpdateRequest update) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            Objects.requireNonNull(update, "'update' cannot be null.");
            return client.updateAsync(phoneNumber, update);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


    /**
     * Update an acquired phone number with response.
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param update Update to an acquired phone number.
     * @return A {@link Mono} containing
     * a {@link AcquiredPhoneNumber} representing the acquired phone number
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcquiredPhoneNumber>> 
        updatePhoneNumberWithResponse(String phoneNumber, PhoneNumberUpdateRequest update) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            Objects.requireNonNull(update, "'update' cannot be null.");
            return client.updateWithResponseAsync(phoneNumber, update);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Starts the search for available phone numbers to purchase.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param searchRequest {@link PhoneNumberSearchRequest} specifying the search request
     * @param pollInterval The time our long running operation will keep on polling
     * until it gets a result from the server
     * @return A {@link PollerFlux} object with the reservation result
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperationResult, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
        String countryCode, PhoneNumberSearchRequest searchRequest, Duration pollInterval) {
        Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
        Objects.requireNonNull(searchRequest, "'searchRequest' cannot be null.");
        
        if (pollInterval == null) {
            pollInterval = defaultPollInterval;
        }

        return new PollerFlux<>(pollInterval,
            searchAvailableNumbersInitOperation(countryCode, searchRequest),
            searchAvailableNumbersPollOperation(),
            cancelOperation(),
            fetchFinalResultOperation());
    }

    private Function<PollingContext<PhoneNumberOperationResult>, Mono<PhoneNumberOperationResult>>
        searchAvailableNumbersInitOperation(String countryCode, PhoneNumberSearchRequest searchRequest) {
        return (pollingContext) -> {
            return client.searchAvailablePhoneNumbersWithResponseAsync(countryCode, searchRequest)
                .flatMap((PhoneNumbersSearchAvailablePhoneNumbersResponse response) -> {
                    pollingContext.setData("operationId", parseIdFromUrl(response.getDeserializedHeaders().getOperationLocation()));
                    pollingContext.setData("searchId", parseIdFromUrl(response.getDeserializedHeaders().getLocation()));
                    return client.getOperationAsync(pollingContext.getData("operationId"))
                    .flatMap((PhoneNumberOperationResult result) -> {
                        return Mono.just(result);
                    });
                });
        };
    }

    private String parseIdFromUrl(String url) {
        String[] items = url.split("/");
        String id = items[items.length - 1];
        return id.substring(0, id.indexOf("?"));
    }

    private Function<PollingContext<PhoneNumberOperationResult>, Mono<PollResponse<PhoneNumberOperationResult>>>
        searchAvailableNumbersPollOperation() {
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

    private BiFunction<PollingContext<PhoneNumberOperationResult>,
        PollResponse<PhoneNumberOperationResult>, Mono<PhoneNumberOperationResult>>
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
    
    private Function<PollingContext<PhoneNumberOperationResult>, Mono<PhoneNumberSearchResult>>
        fetchFinalResultOperation() {
        return (pollingContext) -> {
            return client.getSearchResultAsync(pollingContext.getData("searchId"));
        };
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated with a given id.
     *
     * @param searchId ID of the search
     * @return A {@link PollerFlux} object.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<Void, Void> beginPurchasePhoneNumbers(String searchId) {
        Objects.requireNonNull(searchId, "'searchId' can not be null.");
        return null;
    }

    /**
     * Begins release of an acquired phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the 
     * operation is complete.
     *
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @return A {@link PollerFlux} object.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<Void, Void> beginReleasePhoneNumber(String phoneNumber) {
        Objects.requireNonNull(phoneNumber, "'phoneNumbers' cannot be null.");
        return null;
    }

    /**
     * Update capabilities of an acquired phone number.
     * @param phoneNumber The phone number id in E.164 format. The leading plus can be either + or encoded
     *                    as %2B.
     * @param capabilitiesUpdateRequest Update capabilities of an acquired phone number.
     * @return A {@link PollerFlux} object
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<AcquiredPhoneNumber, AcquiredPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest) {
        Objects.requireNonNull(phoneNumber, "'phoneNumbers' cannot be null.");
        return null;
    }
}
