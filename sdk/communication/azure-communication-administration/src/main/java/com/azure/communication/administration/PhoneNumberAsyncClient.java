// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.implementation.PhoneNumberAdminClientImpl;
import com.azure.communication.administration.implementation.PhoneNumberAdministrationsImpl;
import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.CreateReservationOptions;
import com.azure.communication.administration.models.CreateReservationResponse;
import com.azure.communication.administration.models.LocationOptionsQuery;
import com.azure.communication.administration.models.LocationOptionsQueries;
import com.azure.communication.administration.models.LocationOptionsResponse;
import com.azure.communication.administration.models.NumberConfiguration;
import com.azure.communication.administration.models.NumberConfigurationPhoneNumber;
import com.azure.communication.administration.models.NumberConfigurationResponse;
import com.azure.communication.administration.models.NumberUpdateCapabilities;
import com.azure.communication.administration.models.PhoneNumberCountry;
import com.azure.communication.administration.models.PhoneNumberEntity;
import com.azure.communication.administration.models.PhoneNumberRelease;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.administration.models.PstnConfiguration;
import com.azure.communication.administration.models.ReleaseResponse;
import com.azure.communication.administration.models.ReleaseRequest;
import com.azure.communication.administration.models.ReleaseStatus;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesRequest;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesResponse;
import com.azure.communication.administration.models.PhoneNumberReservation;
import com.azure.communication.administration.models.UpdatePhoneNumberCapabilitiesResponse;
import com.azure.communication.common.PhoneNumberIdentifier;
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
    private final Duration defaultPollInterval = Duration.ofSeconds(1);

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

        LocationOptionsQueries locationOptionsQueries = new LocationOptionsQueries();
        locationOptionsQueries.setLocationOptions(locationOptions);

        try {

            Objects.requireNonNull(locationType, "'locationType' cannot be null.");
            Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
            Objects.requireNonNull(phonePlanId, "'phonePlanId' cannot be null.");

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
        try {
            Objects.requireNonNull(capabilitiesId, "'capabilitiesId' cannot be null.");
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
        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate) {
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
        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate) {
        return updateCapabilitiesWithResponse(phoneNumberCapabilitiesUpdate, null);
    }

    Mono<Response<UpdateNumberCapabilitiesResponse>> updateCapabilitiesWithResponse(
        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate, Context context) {
        try {
            Objects.requireNonNull(phoneNumberCapabilitiesUpdate, "'phoneNumberCapabilitiesUpdate' cannot be null.");
            Map<String, NumberUpdateCapabilities> capabilitiesMap = new HashMap<>();
            for (Map.Entry<PhoneNumberIdentifier, NumberUpdateCapabilities> entry
                : phoneNumberCapabilitiesUpdate.entrySet()) {
                capabilitiesMap.put(entry.getKey().getPhoneNumber(), entry.getValue());
            }
            UpdateNumberCapabilitiesRequest updateNumberCapabilitiesRequest = new UpdateNumberCapabilitiesRequest();
            updateNumberCapabilitiesRequest.setPhoneNumberCapabilitiesUpdate(capabilitiesMap);

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
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @return A {@link Mono} containing a {@link NumberConfigurationResponse} representing the configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NumberConfigurationResponse> getNumberConfiguration(PhoneNumberIdentifier phoneNumber) {
        return getNumberConfigurationWithResponse(phoneNumber).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets the configuration of a given phone number.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link NumberConfigurationResponse} representing the configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NumberConfigurationResponse>> getNumberConfigurationWithResponse(
        PhoneNumberIdentifier phoneNumber) {
        return getNumberConfigurationWithResponse(phoneNumber, null);
    }

    Mono<Response<NumberConfigurationResponse>> getNumberConfigurationWithResponse(
        PhoneNumberIdentifier phoneNumber, Context context) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
            configurationPhoneNumber.setPhoneNumber(phoneNumber.getPhoneNumber());

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
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @param pstnConfiguration A {@link PstnConfiguration} containing the pstn number configuration options.
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> configureNumber(PhoneNumberIdentifier phoneNumber, PstnConfiguration pstnConfiguration) {
        return configureNumberWithResponse(phoneNumber, pstnConfiguration).flatMap(FluxUtil::toMono);
    }

    /**
     * Associates a phone number with a PSTN Configuration.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @param pstnConfiguration A {@link PstnConfiguration} containing the pstn number configuration options.
     * @return A {@link Mono} containing a {@link Response} for the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> configureNumberWithResponse(
        PhoneNumberIdentifier phoneNumber, PstnConfiguration pstnConfiguration) {
        return configureNumberWithResponse(phoneNumber, pstnConfiguration, null);
    }

    Mono<Response<Void>> configureNumberWithResponse(
        PhoneNumberIdentifier phoneNumber, PstnConfiguration pstnConfiguration, Context context) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            Objects.requireNonNull(pstnConfiguration, "'pstnConfiguration' cannot be null.");

            NumberConfiguration numberConfiguration = new NumberConfiguration();
            numberConfiguration.setPhoneNumber(phoneNumber.getPhoneNumber()).setPstnConfiguration(pstnConfiguration);

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
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> unconfigureNumber(PhoneNumberIdentifier phoneNumber) {
        return unconfigureNumberWithResponse(phoneNumber).flatMap(FluxUtil::toMono);
    }

    /**
     * Removes the PSTN Configuration from a phone number.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @return A {@link Mono} containing a {@link Response} for the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> unconfigureNumberWithResponse(PhoneNumberIdentifier phoneNumber) {
        return unconfigureNumberWithResponse(phoneNumber, null);
    }

    Mono<Response<Void>> unconfigureNumberWithResponse(PhoneNumberIdentifier phoneNumber, Context context) {
        try {
            Objects.requireNonNull(phoneNumber, "'phoneNumber' cannot be null.");
            NumberConfigurationPhoneNumber configurationPhoneNumber = new NumberConfigurationPhoneNumber();
            configurationPhoneNumber.setPhoneNumber(phoneNumber.getPhoneNumber());

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
        try {
            Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
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
        try {
            Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
            Objects.requireNonNull(phonePlanGroupId, "'phonePlanGroupId' cannot be null.");
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
        try {
            Objects.requireNonNull(countryCode, "'countryCode' cannot be null.");
            Objects.requireNonNull(phonePlanGroupId, "'phonePlanGroupId' cannot be null.");
            Objects.requireNonNull(phonePlanId, "'phonePlanId' cannot be null.");

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
        try {
            Objects.requireNonNull(releaseId, "'releaseId' cannot be null.");
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
     * @param phoneNumbers {@link List} of {@link PhoneNumberIdentifier} objects with the phone numbers.
     * @return A {@link Mono} containing a {@link ReleaseResponse} representing the release.
     */
    private Mono<ReleaseResponse> releasePhoneNumbers(List<PhoneNumberIdentifier> phoneNumbers) {
        return releasePhoneNumbersWithResponse(phoneNumbers).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a release for the given phone numbers.
     *
     * @param phoneNumbers {@link List} of {@link PhoneNumberIdentifier} objects with the phone numbers.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link ReleaseResponse} representing the release.
     */
    private Mono<Response<ReleaseResponse>> releasePhoneNumbersWithResponse(List<PhoneNumberIdentifier> phoneNumbers) {
        return releasePhoneNumbersWithResponse(phoneNumbers, null);
    }

    private Mono<Response<ReleaseResponse>> releasePhoneNumbersWithResponse(
        List<PhoneNumberIdentifier> phoneNumbers, Context context) {
        Objects.requireNonNull(phoneNumbers, "'phoneNumbers' cannot be null.");

        List<String> phoneNumberStrings = phoneNumbers
            .stream()
            .map(PhoneNumberIdentifier::getPhoneNumber)
            .collect(Collectors.toList());
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
     * Gets a reservation by ID.
     *
     * @param reservationId ID of the reservation
     * @return A {@link Mono} containing a {@link PhoneNumberReservation} representing the reservation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PhoneNumberReservation> getReservationById(String reservationId) {
        return getReservationByIdWithResponse(reservationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a reservation by ID.
     *
     * @param reservationId ID of the reservation
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link PhoneNumberReservation} representing the reservation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PhoneNumberReservation>> getReservationByIdWithResponse(String reservationId) {
        return getReservationByIdWithResponse(reservationId, null);
    }

    Mono<Response<PhoneNumberReservation>> getReservationByIdWithResponse(String reservationId, Context context) {
        try {
            Objects.requireNonNull(reservationId, "'reservationId' cannot be null.");
            if (context == null) {
                return phoneNumberAdministrations.getSearchByIdWithResponseAsync(reservationId);
            } else {
                return phoneNumberAdministrations.getSearchByIdWithResponseAsync(reservationId, context);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a phone number reservation.
     *
     * @param reservationOptions A {@link CreateReservationOptions} with the reservation options
     * @return A {@link Mono} containing a {@link CreateReservationResponse} representing the reservation.
     */
    private Mono<CreateReservationResponse> createReservation(CreateReservationOptions reservationOptions) {
        return createReservationWithResponse(reservationOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a phone number reservation.
     *
     * @param reservationOptions A {@link CreateReservationOptions} with the reservation options
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue()} value returns
     * a {@link CreateReservationResponse} representing the reservation.
     */
    private Mono<Response<CreateReservationResponse>> createReservationWithResponse(
        CreateReservationOptions reservationOptions) {
        return createReservationWithResponse(reservationOptions, null);
    }

    private Mono<Response<CreateReservationResponse>> createReservationWithResponse(
        CreateReservationOptions reservationOptions, Context context) {
        try {
            Objects.requireNonNull(reservationOptions, "'reservationOptions' cannot be null.");

            if (context == null) {
                return phoneNumberAdministrations.createSearchWithResponseAsync(reservationOptions);
            } else {
                return phoneNumberAdministrations.createSearchWithResponseAsync(reservationOptions, context);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the list of all reservations
     *
     * @return A {@link PagedFlux} of {@link PhoneNumberEntity} instances representing reservations.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<PhoneNumberEntity> listAllReservations() {
        return listAllReservations(null);
    }

    PagedFlux<PhoneNumberEntity> listAllReservations(Context context) {
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
     * Cancels the reservation. This means existing numbers in the reservation will be made available.
     *
     * @param reservationId ID of the reservation
     * @return A {@link Mono} for the asynchronous return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelReservation(String reservationId) {
        return cancelReservationWithResponse(reservationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Cancels the reservation. This means existing numbers in the reservation will be made available.
     *
     * @param reservationId ID of the reservation
     * @return A {@link Mono} containing a {@link Response} for the operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelReservationWithResponse(String reservationId) {
        return cancelReservationWithResponse(reservationId, null);
    }

    Mono<Response<Void>> cancelReservationWithResponse(String reservationId, Context context) {
        try {
            Objects.requireNonNull(reservationId, "'ReservationId' cannot be null.");

            if (context == null) {
                return phoneNumberAdministrations.cancelSearchWithResponseAsync(reservationId);
            } else {
                return phoneNumberAdministrations.cancelSearchWithResponseAsync(reservationId, context);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Purchases the phone number reservation.
     *
     * @param reservationId ID of the reservation
     * @return A {@link Mono} for the asynchronous return
     */
    private Mono<Void> purchaseReservation(String reservationId) {
        return purchaseReservationWithResponse(reservationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Purchases the phone number reservation.
     *
     * @param reservationId ID of the reservation
     * @return A {@link Mono} containing a {@link Response} for the operation
     */
    private Mono<Response<Void>> purchaseReservationWithResponse(String reservationId) {
        return purchaseReservationWithResponse(reservationId, null);
    }

    private Mono<Response<Void>> purchaseReservationWithResponse(String reservationId, Context context) {
        try {
            Objects.requireNonNull(reservationId, "'reservationId' cannot be null.");
            if (context == null) {
                return phoneNumberAdministrations.purchaseSearchWithResponseAsync(reservationId);
            } else {
                return phoneNumberAdministrations.purchaseSearchWithResponseAsync(reservationId, context);
            }
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Initiates a reservation and returns a {@link PhoneNumberReservation} usable by other functions
     * This function returns a Long Running Operation poller that allows you to
     * wait indefinitely until the operation is complete.
     *
     * @param options A {@link CreateReservationOptions} with the reservation options
     * @param pollInterval The time our long running operation will keep on polling
     * until it gets a result from the server
     * @return A {@link PollerFlux} object with the reservation result
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberReservation, PhoneNumberReservation> beginCreateReservation(
        CreateReservationOptions options, Duration pollInterval) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        if (pollInterval == null) {
            pollInterval = defaultPollInterval;
        }

        return new PollerFlux<PhoneNumberReservation, PhoneNumberReservation>(pollInterval,
            createReservationActivationOperation(options),
            createReservationPollOperation(),
            cancelReservationOperation(),
            createReservationFetchResultOperation());
    }

    private Function<PollingContext<PhoneNumberReservation>, Mono<PhoneNumberReservation>>
        createReservationActivationOperation(CreateReservationOptions options) {
        return (pollingContext) -> {
            Mono<PhoneNumberReservation> response = createReservation(options).flatMap(createReservationResponse -> {
                String reservationId = createReservationResponse.getReservationId();
                Mono<PhoneNumberReservation> phoneNumberReservation = getReservationById(reservationId);
                return phoneNumberReservation;
            });
            return response;
        };
    }

    private Function<PollingContext<PhoneNumberReservation>, Mono<PollResponse<PhoneNumberReservation>>>
        createReservationPollOperation() {
        return pollingContext ->
            getReservationById(pollingContext.getLatestResponse().getValue().getReservationId())
                .flatMap(getReservationResponse -> {
                    ReservationStatus status =
                        ReservationStatus.fromString(getReservationResponse.getStatus().toString());
                    if (status.equals(ReservationStatus.EXPIRED)
                        || status.equals(ReservationStatus.CANCELLED)
                        || status.equals(ReservationStatus.RESERVED)) {
                        return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, getReservationResponse));
                    }
                    if (status.equals(ReservationStatus.ERROR)) {
                        return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.FAILED, getReservationResponse));
                    }
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                        getReservationResponse));
                });
    }

    private BiFunction<PollingContext<PhoneNumberReservation>,
        PollResponse<PhoneNumberReservation>, Mono<PhoneNumberReservation>>
        cancelReservationOperation() {
        return (pollingContext, firstResponse) -> {
            cancelReservation(pollingContext.getLatestResponse().getValue().getReservationId());
            return Mono.just(pollingContext.getLatestResponse().getValue());
        };
    }

    private Function<PollingContext<PhoneNumberReservation>,
        Mono<PhoneNumberReservation>> createReservationFetchResultOperation() {
        return pollingContext -> {
            return Mono.just(pollingContext.getLatestResponse().getValue());
        };
    }

    /**
     * Initiates a purchase process and polls until a terminal state is reached
     * This function returns a Long Running Operation poller that allows you to
     * wait indefinitely until the operation is complete.
     *
     * @param reservationId ID of the reservation
     * @param pollInterval The time our long running operation will keep on polling
     * until it gets a result from the server
     * @return A {@link PollerFlux} object.
     */

    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<Void, Void> beginPurchaseReservation(String reservationId, Duration pollInterval) {
        Objects.requireNonNull(reservationId, "'ReservationId' can not be null.");

        if (pollInterval == null) {
            pollInterval = defaultPollInterval;
        }

        return new PollerFlux<Void, Void>(pollInterval,
            purchaseReservationActivationOperation(reservationId),
            purchaseReservationPollOperation(reservationId),
            (activationResponse, pollingContext) -> Mono.error(new RuntimeException("Cancellation is not supported")),
            purchaseReservationFetchResultOperation());
    }

    private Function<PollingContext<Void>,
        Mono<Void>> purchaseReservationActivationOperation(String reservationId) {
        return (pollingContext) -> {
            return purchaseReservation(reservationId);
        };
    }

    private Function<PollingContext<Void>, Mono<PollResponse<Void>>>
        purchaseReservationPollOperation(String reservationId) {
        return (pollingContext) -> getReservationById(reservationId)
            .flatMap(getReservationResponse -> {
                ReservationStatus statusResponse =
                    ReservationStatus.fromString(getReservationResponse.getStatus().toString());
                if (statusResponse.equals(ReservationStatus.SUCCESS)) {
                    return Mono.just(new PollResponse<>(
                    LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, null));
                }
                if (statusResponse.equals(ReservationStatus.ERROR)
                    || statusResponse.equals(ReservationStatus.EXPIRED)) {
                    return Mono.just(new PollResponse<>(
                    LongRunningOperationStatus.FAILED, null));
                }
                return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, null));
            });
    }

    private Function<PollingContext<Void>,
        Mono<Void>> purchaseReservationFetchResultOperation() {
        return pollingContext -> {
            return Mono.empty();
        };

    }

 /**
     * Releases the given phone numbers.
     * This function returns a Long Running Operation poller that allows you to
     * wait indefinitely until the operation is complete.
     *
     * @param phoneNumbers A list of {@link PhoneNumberIdentifier} with the desired numbers to release
     * @param pollInterval The time our long running operation will keep on polling
     * until it gets a result from the server
     * @return A {@link PollerFlux} object with the release entity
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<PhoneNumberRelease, PhoneNumberRelease>
        beginReleasePhoneNumbers(List<PhoneNumberIdentifier> phoneNumbers, Duration pollInterval) {
        Objects.requireNonNull(phoneNumbers, "'phoneNumbers' cannot be null.");

        if (pollInterval == null) {
            pollInterval = defaultPollInterval;
        }

        return new PollerFlux<PhoneNumberRelease, PhoneNumberRelease>(pollInterval,
            releaseNumbersActivationOperation(phoneNumbers),
            releaseNumbersPollOperation(),
            (activationResponse, pollingContext) ->
            monoError(logger, new RuntimeException("Cancellation is not supported")),
            releaseNumbersFetchResultOperation());
    }

    private Function<PollingContext<PhoneNumberRelease>, Mono<PhoneNumberRelease>>
        releaseNumbersActivationOperation(List<PhoneNumberIdentifier> phoneNumbers) {
        return (pollingContext) -> {
            Mono<PhoneNumberRelease> response = releasePhoneNumbers(phoneNumbers)
                .flatMap(releaseNumberResponse -> {
                    String releaseId = releaseNumberResponse.getReleaseId();
                    Mono<PhoneNumberRelease> phoneNumberRelease = getReleaseById(releaseId);
                    return phoneNumberRelease;
                });
            return response;
        };
    }

    private Function<PollingContext<PhoneNumberRelease>, Mono<PollResponse<PhoneNumberRelease>>>
        releaseNumbersPollOperation() {
        return pollingContext ->
            getReleaseById(pollingContext.getLatestResponse().getValue().getReleaseId())
                .flatMap(getReleaseResponse -> {
                    ReleaseStatus status = getReleaseResponse.getStatus();
                    if (status.equals(ReleaseStatus.COMPLETE)
                        || status.equals(ReleaseStatus.EXPIRED)) {
                        return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, getReleaseResponse));
                    }
                    if (status.equals(ReleaseStatus.FAILED)) {
                        return Mono.just(new PollResponse<>(
                        LongRunningOperationStatus.FAILED, getReleaseResponse));
                    }
                    return Mono.just(new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS, getReleaseResponse));
                });
    }

    private Function<PollingContext<PhoneNumberRelease>,
        Mono<PhoneNumberRelease>> releaseNumbersFetchResultOperation() {
        return pollingContext -> {
            return Mono.just(pollingContext.getLatestResponse().getValue());
        };
    }
}
