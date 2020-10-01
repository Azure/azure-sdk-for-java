// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.CreateSearchOptions;
import com.azure.communication.administration.models.CreateSearchResponse;
import com.azure.communication.administration.models.LocationOptionsQueries;
import com.azure.communication.administration.models.LocationOptionsQuery;
import com.azure.communication.administration.models.LocationOptionsResponse;
import com.azure.communication.administration.models.NumberConfigurationPhoneNumber;
import com.azure.communication.administration.models.NumberConfigurationResponse;
import com.azure.communication.administration.models.NumberUpdateCapabilities;
import com.azure.communication.administration.models.PhoneNumberCountry;
import com.azure.communication.administration.models.PhoneNumberEntity;
import com.azure.communication.administration.models.PhoneNumberRelease;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.administration.models.PstnConfiguration;
import com.azure.communication.administration.models.ReleaseRequest;
import com.azure.communication.administration.models.ReleaseResponse;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesResponse;
import com.azure.communication.administration.models.NumberConfiguration;
import com.azure.communication.administration.models.PhoneNumberSearch;
import com.azure.communication.administration.models.SearchStatus;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesRequest;
import com.azure.communication.administration.models.UpdatePhoneNumberCapabilitiesResponse;
import com.azure.communication.common.PhoneNumber;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;


/**
 * Asynchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = true)
public final class PhoneNumberAsyncClient {

    private final PhoneNumberAdminClientImpl phoneNumberAdminClient;
    private static final Duration DEFAULT_POLL_DURATION = Duration.ofSeconds(1);

    PhoneNumberAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        this.phoneNumberAdminClient = phoneNumberAdminClient;
    }

    /**
     * Gets the list of the acquired phone numbers.
     *
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link PagedFlux} of {@link AcquiredPhoneNumber} instances representing acquired telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AcquiredPhoneNumber> listAllPhoneNumbers(String locale) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getAllPhoneNumbersAsync(
            locale, null, null);
    }

    PagedFlux<AcquiredPhoneNumber> listAllPhoneNumbers(String locale, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getAllPhoneNumbersAsync(
            locale, null, null, context);
    }

    /**
     * Gets a list of the supported area codes.
     *
     * @param locationType The type of location information required by the plan.
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanId The plan id from which to search area codes.
     * @param locationOptions A {@link List} of {@link LocationOptionsQuery} for querying the area codes.
     * @return A {@link Mono} containing a {@link AreaCodes} representing area codes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AreaCodes> getAllAreaCodes(
        String locationType, String countryCode, String phonePlanId, List<LocationOptionsQuery> locationOptions) {
        LocationOptionsQueries locationOptionsQueries = new LocationOptionsQueries();
        locationOptionsQueries.setLocationOptions(locationOptions);
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getAllAreaCodesAsync(
            locationType, countryCode, phonePlanId, locationOptionsQueries);
    }

    /**
     * Gets a list of the supported area codes.
     *
     * @param locationType The type of location information required by the plan.
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanId The plan id from which to search area codes.
     * @param locationOptions A {@link List} of {@link LocationOptionsQuery} for querying the area codes.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link AreaCodes} representing area codes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AreaCodes>> getAllAreaCodesWithResponse(
        String locationType, String countryCode, String phonePlanId, List<LocationOptionsQuery> locationOptions) {
        LocationOptionsQueries locationOptionsQueries = new LocationOptionsQueries();
        locationOptionsQueries.setLocationOptions(locationOptions);
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getAllAreaCodesWithResponseAsync(
            locationType, countryCode, phonePlanId, locationOptionsQueries);
    }

    Mono<Response<AreaCodes>> getAllAreaCodesWithResponse(
        String locationType, String countryCode, String phonePlanId, List<LocationOptionsQuery> locationOptions,
        Context context) {
        LocationOptionsQueries locationOptionsQueries = new LocationOptionsQueries();
        locationOptionsQueries.setLocationOptions(locationOptions);
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getAllAreaCodesWithResponseAsync(
            locationType, countryCode, phonePlanId, locationOptionsQueries, context);
    }

    /**
     * Gets the information for a phone number capabilities update
     *
     * @param capabilitiesId ID of the capabilities update.
     * @return A {@link Mono} containing
     * a {@link UpdatePhoneNumberCapabilitiesResponse} representing the capabilities update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UpdatePhoneNumberCapabilitiesResponse> getCapabilitiesUpdate(String capabilitiesId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getCapabilitiesUpdateAsync(capabilitiesId);
    }

    /**
     * Gets the information for a phone number capabilities update
     *
     * @param capabilitiesId ID of the capabilities update.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link UpdatePhoneNumberCapabilitiesResponse} representing the capabilities update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UpdatePhoneNumberCapabilitiesResponse>> getCapabilitiesUpdateWithResponse(
        String capabilitiesId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getCapabilitiesUpdateWithResponseAsync(capabilitiesId);
    }

    Mono<Response<UpdatePhoneNumberCapabilitiesResponse>> getCapabilitiesUpdateWithResponse(
        String capabilitiesId, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getCapabilitiesUpdateWithResponseAsync(capabilitiesId, context);
    }

    /**
     * Adds or removes phone number capabilities.
     *
     * @param phoneNumberCapabilitiesUpdate {@link Map} with the updates to perform
     * @return A {@link Mono} containing
     * a {@link UpdatePhoneNumberCapabilitiesResponse} representing the capabilities update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UpdateNumberCapabilitiesResponse> updateCapabilities(
        Map<PhoneNumber, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate) {
        Map<String, NumberUpdateCapabilities> capabilitiesMap = new HashMap<>();
        for (Map.Entry<PhoneNumber, NumberUpdateCapabilities> entry : phoneNumberCapabilitiesUpdate.entrySet()) {
            capabilitiesMap.put(entry.getKey().getValue(), entry.getValue());
        }

        UpdateNumberCapabilitiesRequest updateNumberCapabilitiesRequest = new UpdateNumberCapabilitiesRequest();
        updateNumberCapabilitiesRequest.setPhoneNumberCapabilitiesUpdate(capabilitiesMap);
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .updateCapabilitiesAsync(updateNumberCapabilitiesRequest);
    }

    /**
     * Adds or removes phone number capabilities.
     *
     * @param phoneNumberCapabilitiesUpdate {@link Map} with the updates to perform
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link UpdatePhoneNumberCapabilitiesResponse} representing the capabilities update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<UpdateNumberCapabilitiesResponse>> updateCapabilitiesWithResponse(
        Map<PhoneNumber, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate) {
        Map<String, NumberUpdateCapabilities> capabilitiesMap = new HashMap<>();
        for (Map.Entry<PhoneNumber, NumberUpdateCapabilities> entry : phoneNumberCapabilitiesUpdate.entrySet()) {
            capabilitiesMap.put(entry.getKey().getValue(), entry.getValue());
        }

        UpdateNumberCapabilitiesRequest updateNumberCapabilitiesRequest = new UpdateNumberCapabilitiesRequest();
        updateNumberCapabilitiesRequest.setPhoneNumberCapabilitiesUpdate(capabilitiesMap);
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .updateCapabilitiesWithResponseAsync(updateNumberCapabilitiesRequest);
    }

    Mono<Response<UpdateNumberCapabilitiesResponse>> updateCapabilitiesWithResponse(
        Map<PhoneNumber, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate, Context context) {
        Map<String, NumberUpdateCapabilities> capabilitiesMap = new HashMap<>();
        for (Map.Entry<PhoneNumber, NumberUpdateCapabilities> entry : phoneNumberCapabilitiesUpdate.entrySet()) {
            capabilitiesMap.put(entry.getKey().getValue(), entry.getValue());
        }

        UpdateNumberCapabilitiesRequest updateNumberCapabilitiesRequest = new UpdateNumberCapabilitiesRequest();
        updateNumberCapabilitiesRequest.setPhoneNumberCapabilitiesUpdate(capabilitiesMap);
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .updateCapabilitiesWithResponseAsync(updateNumberCapabilitiesRequest, context);
    }

    /**
     * Gets a list of supported countries.
     *
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link PagedFlux} of {@link PhoneNumberCountry} instances representing supported countries.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberCountry> listAllSupportedCountries(String locale) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getAllSupportedCountriesAsync(locale, null, null);
    }

    PagedFlux<PhoneNumberCountry> listAllSupportedCountries(String locale, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getAllSupportedCountriesAsync(locale, null, null, context);
    }

    /**
     * Initiates a search and returns a {@link PhoneNumberSearch} usable by other functions
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the operation is complete.
     * 
     * @param options A {@link CreateSearchOptions} with the search options
     * @param lroDuration The time our long running operation will keep on polling until it gets a result from the server
     * @return A {@link PagedFlux} of {@link PhoneNumberCountry} instances representing supported countries.
     */
    public PollerFlux<CreateSearchResponse, PhoneNumberSearch> beginCreateSearch(
        CreateSearchOptions options, Duration lroDuration) {
        final Duration interval = lroDuration != null ? lroDuration : DEFAULT_POLL_DURATION;
        return new PollerFlux<CreateSearchResponse, PhoneNumberSearch>(interval,
            activationOperation(options),
            createPollOperation(),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchResultOperation());
    }

    private Function<PollingContext<CreateSearchResponse>, 
        Mono<CreateSearchResponse>> activationOperation(CreateSearchOptions options) {
        return (pollingContext) -> withContext(context -> createSearchWithResponse(options, context))
            .flatMap(response -> Mono.just(response.getValue()));
    }

    private Function<PollingContext<CreateSearchResponse>, Mono<PollResponse<CreateSearchResponse>>> createPollOperation() {
        return pollingContext ->
            withContext(context -> getSearchByIdWithResponse(pollingContext.getLatestResponse().getValue().getSearchId(), context))
                .flatMap(getSearchResponse -> {
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.SUCCESS)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.EXPIRED)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.CANCELLED)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.RESERVED)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.ERROR)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getActivationResponse().getValue()));
                });
    }

    private Function<PollingContext<CreateSearchResponse>,
        Mono<PhoneNumberSearch>> fetchResultOperation() {
            return pollingContext ->
            withContext(context -> getSearchByIdWithResponse(pollingContext.getActivationResponse().getValue().getSearchId(), context))
                .flatMap(getSearchResponse -> Mono.just(getSearchResponse.getValue()));
    }

    /**
     * Cancels the search associated with a given id. This means existing numbers in the search will be made available.
     * This function returns a Long Running Operation poller that allows you to wait indefinitely until the operation is complete.
     *
     * @param searchId ID of the search
     * @param lroDuration The time our long running operation will keep on polling until it gets a result from the server
     * @return A {@link PagedFlux} of {@link PhoneNumberCountry} instances representing supported countries.
     */
    public PollerFlux<Void, PhoneNumberSearch> beginCancelSearch(
        String searchId, Duration lroDuration) {
        final Duration interval = lroDuration != null ? lroDuration : DEFAULT_POLL_DURATION;
        return new PollerFlux<Void, PhoneNumberSearch>(interval,
            activationOperation(searchId),
            createPollOperation(searchId),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            fetchResultOperation(searchId));
    }

    private Function<PollingContext<Void>, 
        Mono<Void>> activationOperation(String searchId) {
        return (pollingContext) -> withContext(context -> cancelSearchWithResponse(searchId, context))
            .flatMap(response -> Mono.just(response.getValue()));
    }

    private Function<PollingContext<Void>, Mono<PollResponse<Void>>> createPollOperation(String searchId) {
        return pollingContext ->
            withContext(context -> getSearchByIdWithResponse(searchId, context))
                .flatMap(getSearchResponse -> {
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.SUCCESS)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.EXPIRED)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.CANCELLED)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.RESERVED)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    if (getSearchResponse.getValue().getStatus().equals(SearchStatus.ERROR)) {
                        return Mono.just(new PollResponse<>(LongRunningOperationStatus.FAILED,
                            pollingContext.getActivationResponse().getValue()));
                    }
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        pollingContext.getActivationResponse().getValue()));
                });
    }

    private Function<PollingContext<Void>,
        Mono<PhoneNumberSearch>> fetchResultOperation(String searchId) {
            return pollingContext ->
            withContext(context -> getSearchByIdWithResponse(searchId, context))
                .flatMap(getSearchResponse -> Mono.just(getSearchResponse.getValue()));
    }


    /**
     * Gets the configuration of a given phone number.
     *
     * @param phoneNumber A {@link PhoneNumber} representing the phone number.
     * @return A {@link Mono} containing a {@link NumberConfigurationResponse} representing the configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NumberConfigurationResponse> getNumberConfiguration(PhoneNumber phoneNumber) {
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getNumberConfigurationAsync(configurationPhoneNumber);
    }

    /**
     * Gets the configuration of a given phone number.
     *
     * @param phoneNumber A {@link PhoneNumber} representing the phone number.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link NumberConfigurationResponse} representing the configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NumberConfigurationResponse>> getNumberConfigurationWithResponse(PhoneNumber phoneNumber) {
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getNumberConfigurationWithResponseAsync(configurationPhoneNumber);
    }

    Mono<Response<NumberConfigurationResponse>> getNumberConfigurationWithResponse(
        PhoneNumber phoneNumber, Context context) {
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getNumberConfigurationWithResponseAsync(configurationPhoneNumber, context);
    }

    /**
     * Associates a phone number with a PSTN Configuration.
     *
     * @param phoneNumber A {@link PhoneNumber} representing the phone number.
     * @param pstnConfiguration A {@link PstnConfiguration} containing the pstn number configuration options.
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> configureNumber(PhoneNumber phoneNumber, PstnConfiguration pstnConfiguration) {
        NumberConfiguration numberConfiguration = new NumberConfiguration();
        numberConfiguration.setPhoneNumber(phoneNumber.getValue()).setPstnConfiguration(pstnConfiguration);
        return phoneNumberAdminClient.getPhoneNumberAdministrations().configureNumberAsync(numberConfiguration);
    }

    /**
     * Associates a phone number with a PSTN Configuration.
     *
     * @param phoneNumber A {@link PhoneNumber} representing the phone number.
     * @param pstnConfiguration A {@link PstnConfiguration} containing the pstn number configuration options.
     * @return A {@link Mono} containing a {@link Response} for the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> configureNumberWithResponse(
        PhoneNumber phoneNumber, PstnConfiguration pstnConfiguration) {
        NumberConfiguration numberConfiguration = new NumberConfiguration();
        numberConfiguration.setPhoneNumber(phoneNumber.getValue()).setPstnConfiguration(pstnConfiguration);
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .configureNumberWithResponseAsync(numberConfiguration);
    }

    Mono<Response<Void>> configureNumberWithResponse(
        PhoneNumber phoneNumber, PstnConfiguration pstnConfiguration, Context context) {
        NumberConfiguration numberConfiguration = new NumberConfiguration();
        numberConfiguration.setPhoneNumber(phoneNumber.getValue()).setPstnConfiguration(pstnConfiguration);
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .configureNumberWithResponseAsync(numberConfiguration, context);
    }

    /**
     * Removes the PSTN Configuration from a phone number.
     *
     * @param phoneNumber A {@link PhoneNumber} representing the phone number.
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> unconfigureNumber(PhoneNumber phoneNumber) {
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());
        return phoneNumberAdminClient.getPhoneNumberAdministrations().unconfigureNumberAsync(configurationPhoneNumber);
    }

    /**
     * Removes the PSTN Configuration from a phone number.
     *
     * @param phoneNumber A {@link PhoneNumber} representing the phone number.
     * @return A {@link Mono} containing a {@link Response} for the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> unconfigureNumberWithResponse(PhoneNumber phoneNumber) {
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .unconfigureNumberWithResponseAsync(configurationPhoneNumber);
    }

    Mono<Response<Void>> unconfigureNumberWithResponse(PhoneNumber phoneNumber, Context context) {
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .unconfigureNumberWithResponseAsync(configurationPhoneNumber, context);
    }

    /**
     * Gets a list of phone plan groups for the given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @param includeRateInformation Flag to indicate if rate information should be returned.
     * @return A {@link PagedFlux} of {@link PhonePlanGroup} instances representing phone plan groups
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhonePlanGroup> listPhonePlanGroups(
        String countryCode, String locale, Boolean includeRateInformation) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getPhonePlanGroupsAsync(
            countryCode, locale, includeRateInformation, null, null);
    }

    PagedFlux<PhonePlanGroup> listPhonePlanGroups(
        String countryCode, String locale, Boolean includeRateInformation, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getPhonePlanGroupsAsync(
            countryCode, locale, includeRateInformation, null, null, context);
    }

    /**
     * Gets a list of phone plans for a phone plan group
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanGroupId ID of the Phone Plan Group
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link PagedFlux} of {@link PhonePlan} instances representing phone plans
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhonePlan> listPhonePlans(String countryCode, String phonePlanGroupId, String locale) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getPhonePlansAsync(
            countryCode, phonePlanGroupId, locale, null, null);
    }

    PagedFlux<PhonePlan> listPhonePlans(String countryCode, String phonePlanGroupId, String locale, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getPhonePlansAsync(
            countryCode, phonePlanGroupId, locale, null, null, context);
    }

    /**
     * Gets the location options for a phone plan.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanGroupId ID of the Phone Plan Group
     * @param phonePlanId ID of the Phone Plan
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link Mono} containing a {@link LocationOptionsResponse} representing the location options
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<LocationOptionsResponse> getPhonePlanLocationOptions(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getPhonePlanLocationOptionsAsync(
            countryCode, phonePlanGroupId, phonePlanId, locale);
    }

    Mono<LocationOptionsResponse> getPhonePlanLocationOptions(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getPhonePlanLocationOptionsAsync(
            countryCode, phonePlanGroupId, phonePlanId, locale, context);
    }

    /**
     * Gets the location options for a phone plan.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanGroupId ID of the Phone Plan Group
     * @param phonePlanId ID of the Phone Plan
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link LocationOptionsResponse} representing the location options
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<LocationOptionsResponse>> getPhonePlanLocationOptionsWithResponse(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getPhonePlanLocationOptionsWithResponseAsync(
            countryCode, phonePlanGroupId, phonePlanId, locale);
    }

    Mono<Response<LocationOptionsResponse>> getPhonePlanLocationOptionsWithResponse(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getPhonePlanLocationOptionsWithResponseAsync(
            countryCode, phonePlanGroupId, phonePlanId, locale, context);
    }

    /**
     * Gets a release by ID.
     *
     * @param releaseId ID of the Release
     * @return A {@link Mono} containing a {@link PhoneNumberRelease} representing the release.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PhoneNumberRelease> getReleaseById(String releaseId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getReleaseByIdAsync(releaseId);
    }

    /**
     * Gets a release by ID.
     *
     * @param releaseId ID of the Release
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link PhoneNumberRelease} representing the release.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PhoneNumberRelease>> getReleaseByIdWithResponse(String releaseId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getReleaseByIdWithResponseAsync(releaseId);
    }

    Mono<Response<PhoneNumberRelease>> getReleaseByIdWithResponse(String releaseId, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getReleaseByIdWithResponseAsync(releaseId, context);
    }

    /**
     * Creates a release for the given phone numbers.
     *
     * @param phoneNumbers {@link List} of {@link PhoneNumber} objects with the phone numbers.
     * @return A {@link Mono} containing a {@link ReleaseResponse} representing the release.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReleaseResponse> releasePhoneNumbers(List<PhoneNumber> phoneNumbers) {
        List<String> phoneNumberStrings = phoneNumbers.stream().map(PhoneNumber::getValue).collect(Collectors.toList());
        ReleaseRequest releaseRequest = new ReleaseRequest();
        releaseRequest.setPhoneNumbers(phoneNumberStrings);
        return phoneNumberAdminClient.getPhoneNumberAdministrations().releasePhoneNumbersAsync(releaseRequest);
    }

    /**
     * Creates a release for the given phone numbers.
     *
     * @param phoneNumbers {@link List} of {@link PhoneNumber} objects with the phone numbers.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link ReleaseResponse} representing the release.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ReleaseResponse>> releasePhoneNumbersWithResponse(List<PhoneNumber> phoneNumbers) {
        List<String> phoneNumberStrings = phoneNumbers.stream().map(PhoneNumber::getValue).collect(Collectors.toList());
        ReleaseRequest releaseRequest = new ReleaseRequest();
        releaseRequest.setPhoneNumbers(phoneNumberStrings);
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .releasePhoneNumbersWithResponseAsync(releaseRequest);
    }

    Mono<Response<ReleaseResponse>> releasePhoneNumbersWithResponse(List<PhoneNumber> phoneNumbers, Context context) {
        List<String> phoneNumberStrings = phoneNumbers.stream().map(PhoneNumber::getValue).collect(Collectors.toList());
        ReleaseRequest releaseRequest = new ReleaseRequest();
        releaseRequest.setPhoneNumbers(phoneNumberStrings);
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .releasePhoneNumbersWithResponseAsync(releaseRequest, context);
    }

    /**
     * Gets the list of all releases
     *
     * @return A {@link PagedFlux} of {@link PhoneNumberEntity} instances representing releases.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberEntity> listAllReleases() {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getAllReleasesAsync(null, null);
    }

    PagedFlux<PhoneNumberEntity> listAllReleases(Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getAllReleasesAsync(null, null, context);
    }

    /**
     * Gets a search by ID.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link PhoneNumberSearch} representing the search.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PhoneNumberSearch> getSearchById(String searchId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getSearchByIdAsync(searchId);
    }

    /**
     * Gets a search by ID.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link PhoneNumberSearch} representing the search.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PhoneNumberSearch>> getSearchByIdWithResponse(String searchId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getSearchByIdWithResponseAsync(searchId);
    }

    Mono<Response<PhoneNumberSearch>> getSearchByIdWithResponse(String searchId, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getSearchByIdWithResponseAsync(searchId, context);
    }

    /**
     * Create a phone number search.
     *
     * @param searchOptions A {@link CreateSearchOptions} with the search options
     * @return A {@link Mono} containing a {@link CreateSearchResponse} representing the search.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateSearchResponse> createSearch(CreateSearchOptions searchOptions) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().createSearchAsync(searchOptions);
    }

    /**
     * Create a phone number search.
     *
     * @param searchOptions A {@link CreateSearchOptions} with the search options
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link CreateSearchResponse} representing the search.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CreateSearchResponse>> createSearchWithResponse(CreateSearchOptions searchOptions) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().createSearchWithResponseAsync(searchOptions);
    }

    Mono<Response<CreateSearchResponse>> createSearchWithResponse(CreateSearchOptions searchOptions, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .createSearchWithResponseAsync(searchOptions, context);
    }

    /**
     * Gets the list of all searches
     *
     * @return A {@link PagedFlux} of {@link PhoneNumberEntity} instances representing searches.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberEntity> listAllSearches() {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().getAllSearchesAsync(null, null);
    }

    PagedFlux<PhoneNumberEntity> listAllSearches(Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .getAllSearchesAsync(null, null, context);
    }

    /**
     * Cancels the search. This means existing numbers in the search will be made available.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelSearch(String searchId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().cancelSearchAsync(searchId);
    }

    /**
     * Cancels the search. This means existing numbers in the search will be made available.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link Response} for the operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelSearchWithResponse(String searchId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().cancelSearchWithResponseAsync(searchId);
    }

    Mono<Response<Void>> cancelSearchWithResponse(String searchId, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().cancelSearchWithResponseAsync(searchId, context);
    }

    /**
     * Purchases the phone number search.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purchaseSearch(String searchId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().purchaseSearchAsync(searchId);
    }

    /**
     * Purchases the phone number search.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link Response} for the operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purchaseSearchWithResponse(String searchId) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations().purchaseSearchWithResponseAsync(searchId);
    }

    Mono<Response<Void>> purchaseSearchWithResponse(String searchId, Context context) {
        return phoneNumberAdminClient.getPhoneNumberAdministrations()
            .purchaseSearchWithResponseAsync(searchId, context);
    }
}
