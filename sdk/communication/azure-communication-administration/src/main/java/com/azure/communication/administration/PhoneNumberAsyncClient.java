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
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;

/**
 * Asynchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = true)
public final class PhoneNumberAsyncClient {
    private final ClientLogger logger = new ClientLogger(PhoneNumberAsyncClient.class);
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
        return listAllPhoneNumbers(locale, null);
    }

    PagedFlux<AcquiredPhoneNumber> listAllPhoneNumbers(String locale, Context context) {
        try {
            if (context == null) {
                return phoneNumberAdministrations.getAllPhoneNumbersAsync(locale, null, null);
            } else {
                return phoneNumberAdministrations.getAllPhoneNumbersAsync(locale, null, null, context);
            }
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
        return getAllAreaCodesWithResponse(locationType, countryCode, phonePlanId, locationOptions)
            .flatMap(FluxUtil::toMono);
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
        return getAllAreaCodesWithResponse(locationType, countryCode, phonePlanId, locationOptions, null);
    }

    Mono<Response<AreaCodes>> getAllAreaCodesWithResponse(
        String locationType, String countryCode, String phonePlanId, List<LocationOptionsQuery> locationOptions,
        Context context) {
        Objects.requireNonNull(locationType, "'locationType' cannot be null.");
        Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
        Objects.requireNonNull(phonePlanId, "'phonePlanId' cannot be null.");

        LocationOptionsQueries locationOptionsQueries = new LocationOptionsQueries();
        locationOptionsQueries.setLocationOptions(locationOptions);

        try {
            if (context == null) {
                return phoneNumberAdministrations.getAllAreaCodesWithResponseAsync(
                    locationType, countryCode, phonePlanId, locationOptionsQueries);
            } else {
                return phoneNumberAdministrations.getAllAreaCodesWithResponseAsync(
                    locationType, countryCode, phonePlanId, locationOptionsQueries, context);
            }
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
        return getCapabilitiesUpdateWithResponse(capabilitiesId).flatMap(FluxUtil::toMono);
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
        return getCapabilitiesUpdateWithResponse(capabilitiesId, null);
    }

    Mono<Response<UpdatePhoneNumberCapabilitiesResponse>> getCapabilitiesUpdateWithResponse(
        String capabilitiesId, Context context) {
        Objects.requireNonNull(capabilitiesId, "'capabilitiesId' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.getCapabilitiesUpdateWithResponseAsync(capabilitiesId);
            } else {
                return phoneNumberAdministrations.getCapabilitiesUpdateWithResponseAsync(capabilitiesId, context);
            }
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
        return updateCapabilitiesWithResponse(phoneNumberCapabilitiesUpdate).flatMap(FluxUtil::toMono);
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
        return updateCapabilitiesWithResponse(phoneNumberCapabilitiesUpdate, null);
    }

    Mono<Response<UpdateNumberCapabilitiesResponse>> updateCapabilitiesWithResponse(
        Map<PhoneNumber, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate, Context context) {
        Objects.requireNonNull(phoneNumberCapabilitiesUpdate, "'phoneNumberCapabilitiesUpdate' cannot be null.");

        Map<String, NumberUpdateCapabilities> capabilitiesMap = new HashMap<>();
        for (Map.Entry<PhoneNumber, NumberUpdateCapabilities> entry : phoneNumberCapabilitiesUpdate.entrySet()) {
            capabilitiesMap.put(entry.getKey().getValue(), entry.getValue());
        }

        UpdateNumberCapabilitiesRequest updateNumberCapabilitiesRequest = new UpdateNumberCapabilitiesRequest();
        updateNumberCapabilitiesRequest.setPhoneNumberCapabilitiesUpdate(capabilitiesMap);

        try {
            if (context == null) {
                return phoneNumberAdministrations.updateCapabilitiesWithResponseAsync(
                    updateNumberCapabilitiesRequest);
            } else {
                return phoneNumberAdministrations.updateCapabilitiesWithResponseAsync(
                    updateNumberCapabilitiesRequest, context);
            }
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
        return listAllSupportedCountries(locale, null);
    }

    PagedFlux<PhoneNumberCountry> listAllSupportedCountries(String locale, Context context) {
        try {
            if (context == null) {
                return phoneNumberAdministrations.getAllSupportedCountriesAsync(locale, null, null);
            } else {
                return phoneNumberAdministrations.getAllSupportedCountriesAsync(locale, null, null, context);
            }
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
        return getNumberConfigurationWithResponse(phoneNumber).flatMap(FluxUtil::toMono);
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
        return getNumberConfigurationWithResponse(phoneNumber, null);
    }

    Mono<Response<NumberConfigurationResponse>> getNumberConfigurationWithResponse(
        PhoneNumber phoneNumber, Context context) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");

        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());

        try {
            if (context == null) {
                return phoneNumberAdministrations.getNumberConfigurationWithResponseAsync(
                    configurationPhoneNumber);
            } else {
                return phoneNumberAdministrations.getNumberConfigurationWithResponseAsync(
                    configurationPhoneNumber, context);
            }
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
        return configureNumberWithResponse(phoneNumber, pstnConfiguration).flatMap(FluxUtil::toMono);
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
        return configureNumberWithResponse(phoneNumber, pstnConfiguration, null);
    }

    Mono<Response<Void>> configureNumberWithResponse(
        PhoneNumber phoneNumber, PstnConfiguration pstnConfiguration, Context context) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
        Objects.requireNonNull(pstnConfiguration, "'pstnConfiguration' cannot be null.");

        NumberConfiguration numberConfiguration = new NumberConfiguration();
        numberConfiguration.setPhoneNumber(phoneNumber.getValue()).setPstnConfiguration(pstnConfiguration);

        try {
            if (context == null) {
                return phoneNumberAdministrations.configureNumberWithResponseAsync(numberConfiguration);
            } else {
                return phoneNumberAdministrations.configureNumberWithResponseAsync(numberConfiguration, context);
            }
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
        return unconfigureNumberWithResponse(phoneNumber).flatMap(FluxUtil::toMono);
    }

    /**
     * Removes the PSTN Configuration from a phone number.
     *
     * @param phoneNumber A {@link PhoneNumber} representing the phone number.
     * @return A {@link Mono} containing a {@link Response} for the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> unconfigureNumberWithResponse(PhoneNumber phoneNumber) {
        return unconfigureNumberWithResponse(phoneNumber, null);
    }

    Mono<Response<Void>> unconfigureNumberWithResponse(PhoneNumber phoneNumber, Context context) {
        Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
        NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
        configurationPhoneNumber.setPhoneNumber(phoneNumber.getValue());

        try {
            if (context == null) {
                return phoneNumberAdministrations.unconfigureNumberWithResponseAsync(configurationPhoneNumber);
            } else {
                return phoneNumberAdministrations.unconfigureNumberWithResponseAsync(configurationPhoneNumber, context);
            }
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
        return listPhonePlanGroups(countryCode, locale, includeRateInformation, null);
    }

    PagedFlux<PhonePlanGroup> listPhonePlanGroups(
        String countryCode, String locale, Boolean includeRateInformation, Context context) {
        Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.getPhonePlanGroupsAsync(
                    countryCode, locale, includeRateInformation, null, null);
            } else {
                return phoneNumberAdministrations.getPhonePlanGroupsAsync(
                    countryCode, locale, includeRateInformation, null, null, context);
            }
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
        return listPhonePlans(countryCode, phonePlanGroupId, locale, null);
    }

    PagedFlux<PhonePlan> listPhonePlans(String countryCode, String phonePlanGroupId, String locale, Context context) {
        Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
        Objects.requireNonNull(phonePlanGroupId, "'phonePlanGroupId' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.getPhonePlansAsync(
                    countryCode, phonePlanGroupId, locale, null, null);
            } else {
                return phoneNumberAdministrations.getPhonePlansAsync(
                    countryCode, phonePlanGroupId, locale, null, null, context);
            }
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
        return getPhonePlanLocationOptionsWithResponse(countryCode, phonePlanGroupId, phonePlanId, locale)
            .flatMap(FluxUtil::toMono);
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
        return getPhonePlanLocationOptionsWithResponse(
            countryCode, phonePlanGroupId, phonePlanId, locale, null);
    }

    Mono<Response<LocationOptionsResponse>> getPhonePlanLocationOptionsWithResponse(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale, Context context) {
        Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
        Objects.requireNonNull(phonePlanGroupId, "'phonePlanGroupId' cannot be null.");
        Objects.requireNonNull(phonePlanId, "'phonePlanId' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.getPhonePlanLocationOptionsWithResponseAsync(
                    countryCode, phonePlanGroupId, phonePlanId, locale);
            } else {
                return phoneNumberAdministrations.getPhonePlanLocationOptionsWithResponseAsync(
                    countryCode, phonePlanGroupId, phonePlanId, locale, context);
            }
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
        return getReleaseByIdWithResponse(releaseId).flatMap(FluxUtil::toMono);
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
        return getReleaseByIdWithResponse(releaseId, null);
    }

    Mono<Response<PhoneNumberRelease>> getReleaseByIdWithResponse(String releaseId, Context context) {
        Objects.requireNonNull(releaseId, "'releaseId' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.getReleaseByIdWithResponseAsync(releaseId);
            } else {
                return phoneNumberAdministrations.getReleaseByIdWithResponseAsync(releaseId, context);
            }
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
        return releasePhoneNumbersWithResponse(phoneNumbers).flatMap(FluxUtil::toMono);
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
        return releasePhoneNumbersWithResponse(phoneNumbers, null);
    }

    Mono<Response<ReleaseResponse>> releasePhoneNumbersWithResponse(List<PhoneNumber> phoneNumbers, Context context) {
        Objects.requireNonNull(phoneNumbers, "'phoneNumbers' cannot be null.");

        List<String> phoneNumberStrings = phoneNumbers.stream().map(PhoneNumber::getValue).collect(Collectors.toList());
        ReleaseRequest releaseRequest = new ReleaseRequest();
        releaseRequest.setPhoneNumbers(phoneNumberStrings);

        try {
            if (context == null) {
                return phoneNumberAdministrations.releasePhoneNumbersWithResponseAsync(releaseRequest);
            } else {
                return phoneNumberAdministrations.releasePhoneNumbersWithResponseAsync(releaseRequest, context);
            }
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
        return listAllReleases(null);
    }

    PagedFlux<PhoneNumberEntity> listAllReleases(Context context) {
        try {
            if (context == null) {
                return phoneNumberAdministrations.getAllReleasesAsync(null, null);
            } else {
                return phoneNumberAdministrations.getAllReleasesAsync(null, null, context);
            }
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
        return getSearchByIdWithResponse(searchId).flatMap(FluxUtil::toMono);
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
        return getSearchByIdWithResponse(searchId, null);
    }

    Mono<Response<PhoneNumberSearch>> getSearchByIdWithResponse(String searchId, Context context) {
        Objects.requireNonNull(searchId, "'searchId' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.getSearchByIdWithResponseAsync(searchId);
            } else {
                return phoneNumberAdministrations.getSearchByIdWithResponseAsync(searchId, context);
            }
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
        return createSearchWithResponse(searchOptions).flatMap(FluxUtil::toMono);
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
        return createSearchWithResponse(searchOptions, null);
    }

    Mono<Response<CreateSearchResponse>> createSearchWithResponse(CreateSearchOptions searchOptions, Context context) {
        Objects.requireNonNull(searchOptions, "'searchOptions' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.createSearchWithResponseAsync(searchOptions);
            } else {
                return phoneNumberAdministrations.createSearchWithResponseAsync(searchOptions, context);
            }
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
        return listAllSearches(null);
    }

    PagedFlux<PhoneNumberEntity> listAllSearches(Context context) {
        try {
            if (context == null) {
                return phoneNumberAdministrations.getAllSearchesAsync(null, null);
            } else {
                return phoneNumberAdministrations.getAllSearchesAsync(null, null, context);
            }
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
        return cancelSearchWithResponse(searchId).flatMap(FluxUtil::toMono);
    }

    /**
     * Cancels the search. This means existing numbers in the search will be made available.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link Response} for the operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelSearchWithResponse(String searchId) {
        return cancelSearchWithResponse(searchId, null);
    }

    Mono<Response<Void>> cancelSearchWithResponse(String searchId, Context context) {
        Objects.requireNonNull(searchId, "'searchId' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.cancelSearchWithResponseAsync(searchId);
            } else {
                return phoneNumberAdministrations.cancelSearchWithResponseAsync(searchId, context);
            }
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
        return purchaseSearchWithResponse(searchId).flatMap(FluxUtil::toMono);
    }

    /**
     * Purchases the phone number search.
     *
     * @param searchId ID of the search
     * @return A {@link Mono} containing a {@link Response} for the operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> purchaseSearchWithResponse(String searchId) {
        return purchaseSearchWithResponse(searchId, null);
    }

    Mono<Response<Void>> purchaseSearchWithResponse(String searchId, Context context) {
        Objects.requireNonNull(searchId, "'searchId' cannot be null.");

        try {
            if (context == null) {
                return phoneNumberAdministrations.purchaseSearchWithResponseAsync(searchId);
            } else {
                return phoneNumberAdministrations.purchaseSearchWithResponseAsync(searchId, context);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Initiates a search and returns a {@link PhoneNumberSearch} usable by other functions
     * This function returns a Long Running Operation poller that allows you to 
     * wait indefinitely until the operation is complete.
     * 
     * @param options A {@link CreateSearchOptions} with the search options
     * @param pollInterval The time our long running operation will keep on polling 
     * until it gets a result from the server
     * @return A {@link PollerFlux} object with the search result
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PollerFlux<PhoneNumberSearch, PhoneNumberSearch> beginCreateSearch(
        CreateSearchOptions options, Duration pollInterval) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        return new PollerFlux<PhoneNumberSearch, PhoneNumberSearch>(pollInterval,
            createSearchActivationOperation(options),
            createSearchPollOperation(),
            cancelSearchOperation(),
            createSearchFetchResultOperation());
    }

    private Function<PollingContext<PhoneNumberSearch>, Mono<PhoneNumberSearch>> 
        createSearchActivationOperation(CreateSearchOptions options) {
        return (pollingContext) -> {
            Mono<PhoneNumberSearch> response = createSearch(options).flatMap(createSearchResponse -> {
                String searchId = createSearchResponse.getSearchId();
                Mono<PhoneNumberSearch> phoneNumberSearch = getSearchById(searchId);
                return phoneNumberSearch;
            });
            return response;
        };
    }

    private Function<PollingContext<PhoneNumberSearch>, Mono<PollResponse<PhoneNumberSearch>>> 
        createSearchPollOperation() {
        return pollingContext ->
            getSearchById(pollingContext.getLatestResponse().getValue().getSearchId())
                .flatMap(getSearchResponse -> {
                    SearchStatus status = getSearchResponse.getStatus();
                    if (status.equals(SearchStatus.EXPIRED) 
                        || status.equals(SearchStatus.CANCELLED) 
                        || status.equals(SearchStatus.RESERVED)) {
                        return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, getSearchResponse));
                    }
                    if (status.equals(SearchStatus.ERROR)) {
                        return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.FAILED, getSearchResponse));
                    }
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, getSearchResponse));
                });
    }

    private BiFunction<PollingContext<PhoneNumberSearch>,
        PollResponse<PhoneNumberSearch>, Mono<PhoneNumberSearch>> 
        cancelSearchOperation() {
        return (pollingContext, firstResponse) -> {
            cancelSearch(pollingContext.getLatestResponse().getValue().getSearchId());
            return Mono.just(pollingContext.getLatestResponse().getValue());
        };
    }

    private Function<PollingContext<PhoneNumberSearch>,
        Mono<PhoneNumberSearch>> createSearchFetchResultOperation() {
        return pollingContext -> {
            return Mono.just(pollingContext.getLatestResponse().getValue());
        };
    }
}
