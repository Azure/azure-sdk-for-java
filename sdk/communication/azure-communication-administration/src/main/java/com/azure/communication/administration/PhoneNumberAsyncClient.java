// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.administration.implementation.PhoneNumberAdministrationsImpl;
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
import com.azure.communication.administration.models.UpdateNumberCapabilitiesRequest;
import com.azure.communication.administration.models.UpdatePhoneNumberCapabilitiesResponse;
import com.azure.communication.common.PhoneNumber;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;

/**
 * Asynchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = true)
public final class PhoneNumberAsyncClient {
    private final ClientLogger logger = new ClientLogger(PhoneNumberClientBuilder.class);

    private final PhoneNumberAdministrationsImpl phoneNumberAdministrations;

    PhoneNumberAsyncClient(PhoneNumberAdminClientImpl phoneNumberAdminClient) {
        this.phoneNumberAdministrations = phoneNumberAdminClient.getPhoneNumberAdministrations();
    }

    /**
     * Gets the list of the acquired phone numbers.
     *
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link PagedFlux} of {@link AcquiredPhoneNumber} instances representing acquired telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AcquiredPhoneNumber> listAllPhoneNumbers(String locale) {
        try {
            return phoneNumberAdministrations.getAllPhoneNumbersAsync(locale, null, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<AcquiredPhoneNumber> listAllPhoneNumbers(String locale, Context context) {
        try {
            return phoneNumberAdministrations.getAllPhoneNumbersAsync(locale, null, null, context);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.getAllAreaCodesAsync(
                locationType, countryCode, phonePlanId, locationOptionsQueries);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.getAllAreaCodesWithResponseAsync(
                locationType, countryCode, phonePlanId, locationOptionsQueries);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AreaCodes>> getAllAreaCodesWithResponse(
        String locationType, String countryCode, String phonePlanId, List<LocationOptionsQuery> locationOptions,
        Context context) {
        LocationOptionsQueries locationOptionsQueries = new LocationOptionsQueries();
        locationOptionsQueries.setLocationOptions(locationOptions);

        try {
            return phoneNumberAdministrations.getAllAreaCodesWithResponseAsync(
                locationType, countryCode, phonePlanId, locationOptionsQueries, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.getCapabilitiesUpdateAsync(capabilitiesId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.getCapabilitiesUpdateWithResponseAsync(capabilitiesId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<UpdatePhoneNumberCapabilitiesResponse>> getCapabilitiesUpdateWithResponse(
        String capabilitiesId, Context context) {
        try {
            return phoneNumberAdministrations.getCapabilitiesUpdateWithResponseAsync(capabilitiesId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.updateCapabilitiesAsync(updateNumberCapabilitiesRequest);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.updateCapabilitiesWithResponseAsync(updateNumberCapabilitiesRequest);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    Mono<Response<UpdateNumberCapabilitiesResponse>> updateCapabilitiesWithResponse(
        Map<PhoneNumber, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate, Context context) {
        Map<String, NumberUpdateCapabilities> capabilitiesMap = new HashMap<>();
        for (Map.Entry<PhoneNumber, NumberUpdateCapabilities> entry : phoneNumberCapabilitiesUpdate.entrySet()) {
            capabilitiesMap.put(entry.getKey().getValue(), entry.getValue());
        }

        UpdateNumberCapabilitiesRequest updateNumberCapabilitiesRequest = new UpdateNumberCapabilitiesRequest();
        updateNumberCapabilitiesRequest.setPhoneNumberCapabilitiesUpdate(capabilitiesMap);

        try {
            return phoneNumberAdministrations.updateCapabilitiesWithResponseAsync(
                updateNumberCapabilitiesRequest, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a list of supported countries.
     *
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link PagedFlux} of {@link PhoneNumberCountry} instances representing supported countries.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberCountry> listAllSupportedCountries(String locale) {
        try {
            return phoneNumberAdministrations.getAllSupportedCountriesAsync(locale, null, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<PhoneNumberCountry> listAllSupportedCountries(String locale, Context context) {
        try {
            return phoneNumberAdministrations.getAllSupportedCountriesAsync(locale, null, null, context);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.getNumberConfigurationAsync(configurationPhoneNumber);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.getNumberConfigurationWithResponseAsync(configurationPhoneNumber);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<NumberConfigurationResponse>> getNumberConfigurationWithResponse(
        PhoneNumber phoneNumber, Context context) {
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());

        try {
            return phoneNumberAdministrations.getNumberConfigurationWithResponseAsync(
                configurationPhoneNumber, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.configureNumberAsync(numberConfiguration);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.configureNumberWithResponseAsync(numberConfiguration);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> configureNumberWithResponse(
        PhoneNumber phoneNumber, PstnConfiguration pstnConfiguration, Context context) {
        NumberConfiguration numberConfiguration = new NumberConfiguration();
        numberConfiguration.setPhoneNumber(phoneNumber.getValue()).setPstnConfiguration(pstnConfiguration);

        try {
            return phoneNumberAdministrations.configureNumberWithResponseAsync(numberConfiguration, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.unconfigureNumberAsync(configurationPhoneNumber);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.unconfigureNumberWithResponseAsync(configurationPhoneNumber);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> unconfigureNumberWithResponse(PhoneNumber phoneNumber, Context context) {
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());

        try {
            return phoneNumberAdministrations.unconfigureNumberWithResponseAsync(configurationPhoneNumber, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.getPhonePlanGroupsAsync(
                countryCode, locale, includeRateInformation, null, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<PhonePlanGroup> listPhonePlanGroups(
        String countryCode, String locale, Boolean includeRateInformation, Context context) {
        try {
            return phoneNumberAdministrations.getPhonePlanGroupsAsync(
                countryCode, locale, includeRateInformation, null, null, context);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.getPhonePlansAsync(
                countryCode, phonePlanGroupId, locale, null, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<PhonePlan> listPhonePlans(String countryCode, String phonePlanGroupId, String locale, Context context) {
        try {
            return phoneNumberAdministrations.getPhonePlansAsync(
                countryCode, phonePlanGroupId, locale, null, null, context);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.getPhonePlanLocationOptionsAsync(
                countryCode, phonePlanGroupId, phonePlanId, locale);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<LocationOptionsResponse> getPhonePlanLocationOptions(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale, Context context) {
        try {
            return phoneNumberAdministrations.getPhonePlanLocationOptionsAsync(
                countryCode, phonePlanGroupId, phonePlanId, locale, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.getPhonePlanLocationOptionsWithResponseAsync(
                countryCode, phonePlanGroupId, phonePlanId, locale);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<LocationOptionsResponse>> getPhonePlanLocationOptionsWithResponse(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale, Context context) {
        try {
            return phoneNumberAdministrations.getPhonePlanLocationOptionsWithResponseAsync(
                countryCode, phonePlanGroupId, phonePlanId, locale, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a release by ID.
     *
     * @param releaseId ID of the Release
     * @return A {@link Mono} containing a {@link PhoneNumberRelease} representing the release.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PhoneNumberRelease> getReleaseById(String releaseId) {
        try {
            return phoneNumberAdministrations.getReleaseByIdAsync(releaseId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.getReleaseByIdWithResponseAsync(releaseId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PhoneNumberRelease>> getReleaseByIdWithResponse(String releaseId, Context context) {
        try {
            return phoneNumberAdministrations.getReleaseByIdWithResponseAsync(releaseId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.releasePhoneNumbersAsync(releaseRequest);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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

        try {
            return phoneNumberAdministrations.releasePhoneNumbersWithResponseAsync(releaseRequest);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<ReleaseResponse>> releasePhoneNumbersWithResponse(List<PhoneNumber> phoneNumbers, Context context) {
        List<String> phoneNumberStrings = phoneNumbers.stream().map(PhoneNumber::getValue).collect(Collectors.toList());
        ReleaseRequest releaseRequest = new ReleaseRequest();
        releaseRequest.setPhoneNumbers(phoneNumberStrings);

        try {
            return phoneNumberAdministrations.releasePhoneNumbersWithResponseAsync(releaseRequest, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the list of all releases
     *
     * @return A {@link PagedFlux} of {@link PhoneNumberEntity} instances representing releases.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberEntity> listAllReleases() {
        try {
            return phoneNumberAdministrations.getAllReleasesAsync(null, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<PhoneNumberEntity> listAllReleases(Context context) {
        try {
            return phoneNumberAdministrations.getAllReleasesAsync(null, null, context);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Gets a search by ID.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link PhoneNumberSearch} representing the search.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PhoneNumberSearch> getSearchById(String searchId) {
        try {
            return phoneNumberAdministrations.getSearchByIdAsync(searchId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.getSearchByIdWithResponseAsync(searchId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<PhoneNumberSearch>> getSearchByIdWithResponse(String searchId, Context context) {
        try {
            return phoneNumberAdministrations.getSearchByIdWithResponseAsync(searchId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a phone number search.
     *
     * @param searchOptions A {@link CreateSearchOptions} with the search options
     * @return A {@link Mono} containing a {@link CreateSearchResponse} representing the search.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateSearchResponse> createSearch(CreateSearchOptions searchOptions) {
        try {
            return phoneNumberAdministrations.createSearchAsync(searchOptions);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
        try {
            return phoneNumberAdministrations.createSearchWithResponseAsync(searchOptions);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CreateSearchResponse>> createSearchWithResponse(CreateSearchOptions searchOptions, Context context) {
        try {
            return phoneNumberAdministrations.createSearchWithResponseAsync(searchOptions, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the list of all searches
     *
     * @return A {@link PagedFlux} of {@link PhoneNumberEntity} instances representing searches.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberEntity> listAllSearches() {
        try {
            return phoneNumberAdministrations.getAllSearchesAsync(null, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    PagedFlux<PhoneNumberEntity> listAllSearches(Context context) {
        try {
            return phoneNumberAdministrations.getAllSearchesAsync(null, null, context);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    /**
     * Cancels the search. This means existing numbers in the search will be made available.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelSearch(String searchId) {
        try {
            return phoneNumberAdministrations.cancelSearchAsync(searchId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancels the search. This means existing numbers in the search will be made available.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link Response} for the operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelSearchWithResponse(String searchId) {
        try {
            return phoneNumberAdministrations.cancelSearchWithResponseAsync(searchId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> cancelSearchWithResponse(String searchId, Context context) {
        try {
            return phoneNumberAdministrations.cancelSearchWithResponseAsync(searchId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Purchases the phone number search.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> purchaseSearch(String searchId) {
        try {
            return phoneNumberAdministrations.purchaseSearchAsync(searchId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Purchases the phone number search.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link Response} for the operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purchaseSearchWithResponse(String searchId) {
        try {
            return phoneNumberAdministrations.purchaseSearchWithResponseAsync(searchId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> purchaseSearchWithResponse(String searchId, Context context) {
        try {
            return phoneNumberAdministrations.purchaseSearchWithResponseAsync(searchId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
