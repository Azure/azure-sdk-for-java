// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.phonenumbers.implementation.PhoneNumbersImpl;
import com.azure.communication.phonenumbers.implementation.converters.PhoneNumberErrorConverter;
import com.azure.communication.phonenumbers.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersPurchasePhoneNumbersResponse;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumberPurchaseRequest;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumberRawOperation;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumberSearchRequest;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersSearchAvailablePhoneNumbersResponse;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersReleasePhoneNumberResponse;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumberCapabilitiesRequest;
import com.azure.communication.phonenumbers.implementation.models.PhoneNumbersUpdateCapabilitiesResponse;
import com.azure.communication.phonenumbers.models.PhoneNumberAreaCode;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.phonenumbers.models.ReleasePhoneNumberResult;
import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCountry;
import com.azure.communication.phonenumbers.models.PhoneNumberError;
import com.azure.communication.phonenumbers.models.PhoneNumberErrorResponseException;
import com.azure.communication.phonenumbers.models.PhoneNumberLocality;
import com.azure.communication.phonenumbers.models.PhoneNumberOffering;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberOperationStatus;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchOptions;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.communication.phonenumbers.models.PurchasePhoneNumbersResult;
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
 * Asynchronous client for Communication service phone number operations.
 *
 * <p>
 * <strong>Instantiating an asynchronous Phone Numbers Client</strong>
 * </p>
 *
 * <!-- src_embed com.azure.communication.phonenumbers.asyncclient.instantiation
 * -->
 * 
 * <pre>
 * PhoneNumbersAsyncClient phoneNumberAsyncClient = new PhoneNumbersClientBuilder&#40;&#41;
 *         .endpoint&#40;endpoint&#41;
 *         .credential&#40;keyCredential&#41;
 *         .httpClient&#40;httpClient&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * 
 * <!-- end com.azure.communication.phonenumbers.asyncclient.instantiation -->
 *
 * @see PhoneNumbersClientBuilder
 */
@ServiceClient(builder = PhoneNumbersClientBuilder.class, isAsync = true)
public final class PhoneNumbersAsyncClient {
    private final ClientLogger logger = new ClientLogger(PhoneNumbersAsyncClient.class);
    private final PhoneNumbersImpl client;
    private final Duration defaultPollInterval = Duration.ofSeconds(1);
    private final String acceptLanguage;

    PhoneNumbersAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        this(phoneNumberAdminClient, null);
    }

    PhoneNumbersAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient, String acceptLanguage) {
        this.client = phoneNumberAdminClient.getPhoneNumbers();
        this.acceptLanguage = acceptLanguage;
    }

    /**
     * Gets information about a purchased phone number.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.asyncclient.getPurchased
     * -->
     * 
     * <pre>
     * PurchasedPhoneNumber phoneNumber = phoneNumberAsyncClient.getPurchasedPhoneNumber&#40;&quot;+18001234567&quot;&#41;.block&#40;&#41;;
     * System.out.println&#40;&quot;Phone Number Value: &quot; + phoneNumber.getPhoneNumber&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Phone Number Country Code: &quot; + phoneNumber.getCountryCode&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.asyncclient.getPurchased -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                    be either + or encoded
     *                    as %2B.
     * @return {@link PurchasedPhoneNumber} representing the purchased telephone
     *         number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PurchasedPhoneNumber> getPurchasedPhoneNumber(String phoneNumber) {
        if (Objects.isNull(phoneNumber)) {
            return monoError(logger, new NullPointerException("'phoneNumber' cannot be null."));
        }
        return client.getByNumberAsync(phoneNumber)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
    }

    /**
     * Gets information about a purchased phone number with response.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.asyncclient.getPurchasedWithResponse -->
     * 
     * <pre>
     * Response&lt;PurchasedPhoneNumber&gt; response = phoneNumberAsyncClient
     *         .getPurchasedPhoneNumberWithResponse&#40;&quot;+18001234567&quot;&#41;.block&#40;&#41;;
     * PurchasedPhoneNumber phoneNumber = response.getValue&#40;&#41;;
     * System.out.println&#40;&quot;Phone Number Value: &quot; + phoneNumber.getPhoneNumber&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Phone Number Country Code: &quot; + phoneNumber.getCountryCode&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end
     * com.azure.communication.phonenumbers.asyncclient.getPurchasedWithResponse -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                    be either + or encoded
     *                    as %2B.
     * @return {@link PurchasedPhoneNumber} representing the purchased telephone
     *         number.
     * @throws NullPointerException if {@code phoneNumber} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PurchasedPhoneNumber>> getPurchasedPhoneNumberWithResponse(String phoneNumber) {
        if (Objects.isNull(phoneNumber)) {
            return monoError(logger, new NullPointerException("'phoneNumber' cannot be null."));
        }
        return client.getByNumberWithResponseAsync(phoneNumber)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
    }

    /**
     * Gets the list of the purchased phone numbers.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.asyncclient.listPurchased
     * -->
     * 
     * <pre>
     * PagedFlux&lt;PurchasedPhoneNumber&gt; phoneNumbers = phoneNumberAsyncClient.listPurchasedPhoneNumbers&#40;&#41;;
     * PurchasedPhoneNumber phoneNumber = phoneNumbers.blockFirst&#40;&#41;;
     * System.out.println&#40;&quot;Phone Number Value: &quot; + phoneNumber.getPhoneNumber&#40;&#41;&#41;;
     * System.out.println&#40;&quot;Phone Number Country Code: &quot; + phoneNumber.getCountryCode&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.asyncclient.listPurchased -->
     *
     * @return A {@link PagedFlux} of {@link PurchasedPhoneNumber} instances
     *         representing a purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PurchasedPhoneNumber> listPurchasedPhoneNumbers() {
        try {
            return client.listPhoneNumbersAsync(null, null);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Starts the search for available phone numbers to purchase.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.asyncclient.beginSearchAvailable -->
     * 
     * <pre>
     * PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities&#40;&#41;
     *         .setCalling&#40;PhoneNumberCapabilityType.INBOUND&#41;
     *         .setSms&#40;PhoneNumberCapabilityType.INBOUND_OUTBOUND&#41;;
     *
     * PollerFlux&lt;PhoneNumberOperation, PhoneNumberSearchResult&gt; poller = phoneNumberAsyncClient
     *         .beginSearchAvailablePhoneNumbers&#40;&quot;US&quot;, PhoneNumberType.TOLL_FREE,
     *                 PhoneNumberAssignmentType.APPLICATION, capabilities&#41;;
     * AsyncPollResponse&lt;PhoneNumberOperation, PhoneNumberSearchResult&gt; response = poller.blockFirst&#40;&#41;;
     * String searchId = &quot;&quot;;
     *
     * if &#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus&#40;&#41;&#41; &#123;
     *     PhoneNumberSearchResult searchResult = response.getFinalResult&#40;&#41;.block&#40;&#41;;
     *     searchId = searchResult.getSearchId&#40;&#41;;
     *     System.out.println&#40;&quot;Searched phone numbers: &quot; + searchResult.getPhoneNumbers&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Search expires by: &quot; + searchResult.getSearchExpiresBy&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Phone number costs:&quot; + searchResult.getCost&#40;&#41;.getAmount&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * 
     * <!-- end
     * com.azure.communication.phonenumbers.asyncclient.beginSearchAvailable -->
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} The phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number
     *                        assignment type.
     * @param capabilities {@link PhoneNumberCapabilities} The phone number
     *                        capabilities.
     * @return A {@link PollerFlux} object with the reservation result.
     * @throws NullPointerException if {@code countryCode} or {@code searchRequest}
     *                              is null.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
            String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType,
            PhoneNumberCapabilities capabilities) {
        return beginSearchAvailablePhoneNumbers(countryCode, phoneNumberType, assignmentType, capabilities, null, null);
    }

    /**
     * Starts the search for available phone numbers to purchase.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.asyncclient.beginSearchAvailableWithOptions
     * -->
     * 
     * <pre>
     * PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities&#40;&#41;
     *         .setCalling&#40;PhoneNumberCapabilityType.INBOUND&#41;
     *         .setSms&#40;PhoneNumberCapabilityType.INBOUND_OUTBOUND&#41;;
     * PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions&#40;&#41;.setAreaCode&#40;&quot;800&quot;&#41;.setQuantity&#40;1&#41;;
     *
     * PollerFlux&lt;PhoneNumberOperation, PhoneNumberSearchResult&gt; poller = phoneNumberAsyncClient
     *         .beginSearchAvailablePhoneNumbers&#40;&quot;US&quot;, PhoneNumberType.TOLL_FREE,
     *                 PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions&#41;;
     * AsyncPollResponse&lt;PhoneNumberOperation, PhoneNumberSearchResult&gt; response = poller.blockFirst&#40;&#41;;
     *
     * if &#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus&#40;&#41;&#41; &#123;
     *     PhoneNumberSearchResult searchResult = response.getFinalResult&#40;&#41;.block&#40;&#41;;
     *     String searchId = searchResult.getSearchId&#40;&#41;;
     *     System.out.println&#40;&quot;Searched phone numbers: &quot; + searchResult.getPhoneNumbers&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Search expires by: &quot; + searchResult.getSearchExpiresBy&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Phone number costs:&quot; + searchResult.getCost&#40;&#41;.getAmount&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * 
     * <!-- end
     * com.azure.communication.phonenumbers.asyncclient.beginSearchAvailableWithOptions
     * -->
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} The phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number
     *                        assignment type.
     * @param capabilities {@link PhoneNumberCapabilities} The phone number
     *                        capabilities.
     * @param searchOptions The phone number search options.
     * @return A {@link PollerFlux} object with the reservation result.
     * @throws NullPointerException if {@code countryCode} or {@code searchRequest}
     *                              is null.
     * @throws RuntimeException if search operation fails.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
            String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType,
            PhoneNumberCapabilities capabilities, PhoneNumberSearchOptions searchOptions) {
        return beginSearchAvailablePhoneNumbers(countryCode, phoneNumberType, assignmentType, capabilities,
                searchOptions, null);
    }

    PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbers(
            String countryCode, PhoneNumberType phoneNumberType, PhoneNumberAssignmentType assignmentType,
            PhoneNumberCapabilities capabilities, PhoneNumberSearchOptions searchOptions, Context context) {
        try {
            Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
            Objects.requireNonNull(phoneNumberType, "'phoneNumberType' cannot be null.");
            Objects.requireNonNull(assignmentType, "'assignmentType' cannot be null.");
            Objects.requireNonNull(capabilities, "'capabilities' cannot be null.");

            String areaCode = null;
            Integer quantity = null;

            if (searchOptions != null) {
                areaCode = searchOptions.getAreaCode();
                quantity = searchOptions.getQuantity();
            }
            PhoneNumberSearchRequest searchRequest = new PhoneNumberSearchRequest();
            searchRequest
                    .setPhoneNumberType(phoneNumberType)
                    .setAssignmentType(assignmentType)
                    .setCapabilities(capabilities)
                    .setAreaCode(areaCode)
                    .setQuantity(quantity);

            return new PollerFlux<>(defaultPollInterval,
                    searchAvailableNumbersInitOperation(countryCode, searchRequest, context),
                    pollOperation(),
                    cancelOperation(),
                    searchAvailableNumbersFetchFinalResultOperation());

        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberOperation>> searchAvailableNumbersInitOperation(
            String countryCode, PhoneNumberSearchRequest searchRequest, Context context) {
        return (pollingContext) -> {
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return client.searchAvailablePhoneNumbersWithResponseAsync(countryCode, searchRequest, contextValue)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                        .flatMap((PhoneNumbersSearchAvailablePhoneNumbersResponse response) -> {
                            pollingContext.setData("operationId", response.getDeserializedHeaders().getOperationId());
                            pollingContext.setData("searchId", response.getDeserializedHeaders().getSearchId());
                            return getOperation(pollingContext.getData("operationId"));
                        });
            });
        };
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PollResponse<PhoneNumberOperation>>> pollOperation() {
        return (pollingContext) -> {
            return getOperation(pollingContext.getData("operationId"))
                    .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                    .flatMap(operation -> {
                        if (operation.getStatus().toString()
                                .equalsIgnoreCase(PhoneNumberOperationStatus.SUCCEEDED.toString())) {
                            return Mono.just(new PollResponse<>(
                                    LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, operation));
                        } else if (operation.getStatus().toString()
                                .equalsIgnoreCase(PhoneNumberOperationStatus.FAILED.toString())) {
                            return Mono.just(new PollResponse<>(
                                    LongRunningOperationStatus.FAILED, operation));
                        } else if (operation.getStatus().toString()
                                .equalsIgnoreCase(PhoneNumberOperationStatus.NOT_STARTED.toString())) {
                            return Mono.just(new PollResponse<>(
                                    LongRunningOperationStatus.NOT_STARTED, operation));
                        }
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, operation));
                    });
        };
    }

    private BiFunction<PollingContext<PhoneNumberOperation>, PollResponse<PhoneNumberOperation>, Mono<PhoneNumberOperation>> cancelOperation() {
        return (pollingContext, firstResponse) -> {
            if (firstResponse == null || firstResponse.getValue() == null) {
                return Mono.error(logger.logExceptionAsError(
                        new IllegalArgumentException("Cannot cancel a poll response that never started.")));
            }
            String operationId = firstResponse.getValue().getId();
            if (!CoreUtils.isNullOrEmpty(operationId)) {
                logger.info("Cancelling search available phone numbers operation for operation id: {}", operationId);
                return client.cancelOperationAsync(operationId).thenReturn(firstResponse.getValue())
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
            }
            return Mono.empty();
        };
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberSearchResult>> searchAvailableNumbersFetchFinalResultOperation() {
        return (pollingContext) -> {
            return client.getSearchResultAsync(pollingContext.getData("searchId"))
                    .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        };
    }

    /**
     * Starts the purchase of the phone number(s) in the search result associated
     * with a given id.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.asyncclient.beginPurchase
     * -->
     * 
     * <pre>
     * AsyncPollResponse&lt;PhoneNumberOperation, PurchasePhoneNumbersResult&gt; purchaseResponse = phoneNumberAsyncClient
     *         .beginPurchasePhoneNumbers&#40;searchId&#41;.blockFirst&#40;&#41;;
     * System.out.println&#40;&quot;Purchase phone numbers is complete: &quot; + purchaseResponse.getStatus&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.asyncclient.beginPurchase -->
     *
     * @param searchId ID of the search.
     * @return A {@link PollerFlux} object.
     * @throws NullPointerException if {@code searchId} is null.
     * @throws RuntimeException if purchase operation fails.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers(String searchId) {
        return beginPurchasePhoneNumbers(searchId, null);
    }

    PollerFlux<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbers(String searchId,
            Context context) {
        try {
            Objects.requireNonNull(searchId, "'searchId' cannot be null.");
            return new PollerFlux<>(defaultPollInterval,
                    purchaseNumbersInitOperation(searchId, context),
                    pollOperation(),
                    (pollingContext, firstResponse) -> Mono
                            .error(logger.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
                    (pollingContext) -> Mono.just(new PurchasePhoneNumbersResult()));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberOperation>> purchaseNumbersInitOperation(
            String searchId, Context context) {
        return (pollingContext) -> {
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return client
                        .purchasePhoneNumbersWithResponseAsync(new PhoneNumberPurchaseRequest().setSearchId(searchId),
                                contextValue)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                        .flatMap((PhoneNumbersPurchasePhoneNumbersResponse response) -> {
                            pollingContext.setData("operationId", response.getDeserializedHeaders().getOperationId());
                            return getOperation(pollingContext.getData("operationId"));
                        });
            });
        };
    }

    /**
     * Begins release of a purchased phone number.
     *
     * This function returns a Long Running Operation poller that allows you to wait
     * indefinitely until the
     * operation is complete.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed com.azure.communication.phonenumbers.asyncclient.beginRelease
     * -->
     * 
     * <pre>
     * AsyncPollResponse&lt;PhoneNumberOperation, ReleasePhoneNumberResult&gt; releaseResponse = phoneNumberAsyncClient
     *         .beginReleasePhoneNumber&#40;&quot;+18001234567&quot;&#41;.blockFirst&#40;&#41;;
     * System.out.println&#40;&quot;Release phone number is complete: &quot; + releaseResponse.getStatus&#40;&#41;&#41;;
     * </pre>
     * 
     * <!-- end com.azure.communication.phonenumbers.asyncclient.beginRelease -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                    be either + or encoded
     *                    as %2B.
     * @return A {@link PollerFlux} object.
     * @throws NullPointerException if {@code phoneNumber} is null.
     * @throws RuntimeException if release operation fails.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber(String phoneNumber) {
        return beginReleasePhoneNumber(phoneNumber, null);
    }

    PollerFlux<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumber(String phoneNumber,
            Context context) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            return new PollerFlux<>(defaultPollInterval,
                    releaseNumberInitOperation(phoneNumber, context),
                    pollOperation(),
                    (pollingContext, firstResponse) -> Mono
                            .error(logger.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
                    (pollingContext) -> Mono.just(new ReleasePhoneNumberResult()));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberOperation>> releaseNumberInitOperation(
            String phoneNumber, Context context) {
        return (pollingContext) -> {
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return client.releasePhoneNumberWithResponseAsync(phoneNumber, contextValue)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                        .flatMap((PhoneNumbersReleasePhoneNumberResponse response) -> {
                            pollingContext.setData("operationId", response.getDeserializedHeaders().getOperationId());
                            return getOperation(pollingContext.getData("operationId"));
                        });
            });
        };
    }

    /**
     * Update capabilities of a purchased phone number.
     *
     * <p>
     * <strong>Code Samples</strong>
     * </p>
     *
     * <!-- src_embed
     * com.azure.communication.phonenumbers.asyncclient.beginUpdateCapabilities -->
     * 
     * <pre>
     * PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities&#40;&#41;;
     * capabilities
     *         .setCalling&#40;PhoneNumberCapabilityType.INBOUND&#41;
     *         .setSms&#40;PhoneNumberCapabilityType.INBOUND_OUTBOUND&#41;;
     *
     * PollerFlux&lt;PhoneNumberOperation, PurchasedPhoneNumber&gt; poller = phoneNumberAsyncClient
     *         .beginUpdatePhoneNumberCapabilities&#40;&quot;+18001234567&quot;, capabilities&#41;;
     * AsyncPollResponse&lt;PhoneNumberOperation, PurchasedPhoneNumber&gt; response = poller.blockFirst&#40;&#41;;
     *
     * if &#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus&#40;&#41;&#41; &#123;
     *     PurchasedPhoneNumber phoneNumber = response.getFinalResult&#40;&#41;.block&#40;&#41;;
     *     System.out.println&#40;&quot;Phone Number Calling capabilities: &quot; + phoneNumber.getCapabilities&#40;&#41;.getCalling&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Phone Number SMS capabilities: &quot; + phoneNumber.getCapabilities&#40;&#41;.getSms&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * 
     * <!-- end
     * com.azure.communication.phonenumbers.asyncclient.beginUpdateCapabilities -->
     *
     * @param phoneNumber The phone number id in E.164 format. The leading plus can
     *                     be either + or encoded
     *                     as %2B.
     * @param capabilities Update capabilities of a purchased phone number.
     * @return A {@link PollerFlux} object.
     * @throws NullPointerException if {@code phoneNumber} or {@code capabilities}
     *                              is null.
     * @throws RuntimeException if update capabilities operation fails.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber,
            PhoneNumberCapabilities capabilities) {
        return beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities, null);
    }

    PollerFlux<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilities(String phoneNumber,
            PhoneNumberCapabilities capabilities, Context context) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            Objects.requireNonNull(capabilities, "'capabilities' cannot be null.");
            PhoneNumberCapabilitiesRequest capabilitiesRequest = new PhoneNumberCapabilitiesRequest()
                    .setCalling(capabilities.getCalling())
                    .setSms(capabilities.getSms());
            return new PollerFlux<>(defaultPollInterval,
                    updateNumberCapabilitiesInitOperation(phoneNumber, capabilitiesRequest, context),
                    pollOperation(),
                    (pollingContext, firstResponse) -> Mono
                            .error(logger.logExceptionAsError(new RuntimeException("Cancellation is not supported"))),
                    updateNumberCapabilitiesFetchFinalResultOperation(phoneNumber));
        } catch (RuntimeException ex) {
            return PollerFlux.error(ex);
        }
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PhoneNumberOperation>> updateNumberCapabilitiesInitOperation(
            String phoneNumber, PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest, Context context) {
        return (pollingContext) -> {
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return client.updateCapabilitiesWithResponseAsync(phoneNumber, capabilitiesUpdateRequest, contextValue)
                        .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                        .flatMap((PhoneNumbersUpdateCapabilitiesResponse response) -> {
                            pollingContext.setData("operationId", response.getDeserializedHeaders().getOperationId());
                            return getOperation(pollingContext.getData("operationId"));
                        });
            });
        };
    }

    private Function<PollingContext<PhoneNumberOperation>, Mono<PurchasedPhoneNumber>> updateNumberCapabilitiesFetchFinalResultOperation(
            String phoneNumber) {
        return (pollingContext) -> {
            return client.getByNumberAsync(phoneNumber)
                    .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        };
    }

    /**
     * Gets the list of the available countries with context.
     *
     * @return A {@link PagedFlux} of {@link PhoneNumberCountry} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberCountry> listAvailableCountries() {
        try {
            return client.listAvailableCountriesAsync(null, null, acceptLanguage);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets the list of the available localities. I.e. cities, towns.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param administrativeDivision An optional parameter. The name or short name
     *                               of the state/province within which to list the
     *                               localities.
     * @return A {@link PagedFlux} of {@link PhoneNumberLocality} instances
     *         representing available localities with phone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberLocality> listAvailableLocalities(String countryCode, String administrativeDivision) {
        try {
            return client.listAvailableLocalitiesAsync(countryCode, null, null, administrativeDivision, acceptLanguage);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets the list of the available Toll-Free area codes for a given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone number
     *                       assignment type.
     * @return A {@link PagedFlux} of {@link PhoneNumberAreaCode} instances
     *         representing available area codes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberAreaCode> listAvailableTollFreeAreaCodes(String countryCode,
            PhoneNumberAssignmentType assignmentType) {
        try {
            return client.listAreaCodesAsync(countryCode, PhoneNumberType.TOLL_FREE, null, null, assignmentType, null,
                    null, acceptLanguage);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets the list of the available Geographic area codes for a given country and
     * locality.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param assignmentType {@link PhoneNumberAssignmentType} The phone
     *                               number assignment type.
     * @param locality The name of the locality (e.g. city or town
     *                               name) in which to fetch area codes.
     * @param administrativeDivision An optional parameter. The name of the
     *                               administrative division (e.g. state or
     *                               province) of the locality.
     * @return A {@link PagedFlux} of {@link PhoneNumberAreaCode} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberAreaCode> listAvailableGeographicAreaCodes(String countryCode,
            PhoneNumberAssignmentType assignmentType, String locality, String administrativeDivision) {
        try {
            return client.listAreaCodesAsync(countryCode, PhoneNumberType.GEOGRAPHIC, null, null, assignmentType,
                    locality, administrativeDivision, acceptLanguage);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    /**
     * Gets the list of the available phone number offerings for the given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phoneNumberType {@link PhoneNumberType} Optional parameter. Restrict
     *                        the offerings to the phone number type.
     * @param assignmentType {@link PhoneNumberAssignmentType} Optional parameter.
     *                        Restrict the offerings to the assignment type.
     * @return A {@link PagedFlux} of {@link PurchasedPhoneNumber} instances
     *         representing purchased telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberOffering> listAvailableOfferings(String countryCode, PhoneNumberType phoneNumberType,
            PhoneNumberAssignmentType assignmentType) {
        try {
            return client.listOfferingsAsync(countryCode, null, null, phoneNumberType, assignmentType, acceptLanguage);
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    private Mono<PhoneNumberOperation> getOperation(String operationId) {
        return client.getOperationAsync(operationId)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap((PhoneNumberRawOperation rawOperation) -> {
                    if (rawOperation.getError() != null) {
                        return monoError(logger, new RuntimeException(rawOperation.getError().getMessage()));
                    }
                    PhoneNumberOperation operation = new PhoneNumberOperation(
                            rawOperation.getStatus(),
                            rawOperation.getResourceLocation(),
                            rawOperation.getCreatedDateTime(),
                            rawOperation.getId(),
                            rawOperation.getOperationType(),
                            rawOperation.getLastActionDateTime());
                    return Mono.just(operation);
                });
    }

    private PhoneNumberErrorResponseException translateException(CommunicationErrorResponseException exception) {
        PhoneNumberError error = null;
        if (exception.getValue() != null) {
            error = PhoneNumberErrorConverter.convert(exception.getValue().getError());
        }
        return new PhoneNumberErrorResponseException(exception.getMessage(), exception.getResponse(), error);
    }
}
