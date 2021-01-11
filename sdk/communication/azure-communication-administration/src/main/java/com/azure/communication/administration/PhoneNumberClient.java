// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.CreateReservationOptions;
import com.azure.communication.administration.models.LocationOptionsQuery;
import com.azure.communication.administration.models.LocationOptionsResponse;
import com.azure.communication.administration.models.NumberConfigurationResponse;
import com.azure.communication.administration.models.NumberUpdateCapabilities;
import com.azure.communication.administration.models.PhoneNumberCountry;
import com.azure.communication.administration.models.PhoneNumberEntity;
import com.azure.communication.administration.models.PhoneNumberRelease;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.administration.models.PstnConfiguration;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesResponse;
import com.azure.communication.administration.models.PhoneNumberReservation;
import com.azure.communication.administration.models.UpdatePhoneNumberCapabilitiesResponse;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;

import java.util.List;
import java.util.Map;

/**
 * Synchronous client for Communication service phone number operations
 */
@ServiceClient(builder = PhoneNumberClientBuilder.class, isAsync = false)
public final class PhoneNumberClient {

    private final PhoneNumberAsyncClient phoneNumberAsyncClient;

    PhoneNumberClient(PhoneNumberAsyncClient phoneNumberAsyncClient) {
        this.phoneNumberAsyncClient = phoneNumberAsyncClient;
    }

    /**
     * Gets the list of the acquired phone numbers.
     *
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link PagedIterable} of {@link AcquiredPhoneNumber} instances representing acquired telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AcquiredPhoneNumber> listAllPhoneNumbers(String locale) {
        return new PagedIterable<>(phoneNumberAsyncClient.listAllPhoneNumbers(locale));
    }

    /**
     * Gets the list of the acquired phone numbers.
     *
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link AcquiredPhoneNumber} instances representing acquired telephone numbers.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AcquiredPhoneNumber> listAllPhoneNumbers(String locale, Context context) {
        return new PagedIterable<>(phoneNumberAsyncClient.listAllPhoneNumbers(locale, context));
    }

    /**
     * Gets a list of the supported area codes.
     *
     * @param locationType The type of location information required by the plan.
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanId The plan id from which to search area codes.
     * @param locationOptions A {@link List} of {@link LocationOptionsQuery} for querying the area codes.
     * @return A {@link AreaCodes} representing area codes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AreaCodes getAllAreaCodes(
        String locationType, String countryCode, String phonePlanId, List<LocationOptionsQuery> locationOptions) {
        return phoneNumberAsyncClient.getAllAreaCodes(locationType, countryCode, phonePlanId, locationOptions).block();
    }

    /**
     * Gets a list of the supported area codes.
     *
     * @param locationType The type of location information required by the plan.
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanId The plan id from which to search area codes.
     * @param locationOptions A {@link List} of {@link LocationOptionsQuery} for querying the area codes.
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} whose {@link Response#getValue()} value returns
     * a {@link AreaCodes} representing area codes.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AreaCodes> getAllAreaCodesWithResponse(
        String locationType, String countryCode, String phonePlanId, List<LocationOptionsQuery> locationOptions,
        Context context) {
        return phoneNumberAsyncClient.getAllAreaCodesWithResponse(
            locationType, countryCode, phonePlanId, locationOptions, context).block();
    }

    /**
     * Gets the information for a phone number capabilities update
     *
     * @param capabilitiesId ID of the capabilities update.
     * @return A {@link UpdatePhoneNumberCapabilitiesResponse} representing the capabilities update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UpdatePhoneNumberCapabilitiesResponse getCapabilitiesUpdate(String capabilitiesId) {
        return phoneNumberAsyncClient.getCapabilitiesUpdate(capabilitiesId).block();
    }

    /**
     * Gets the information for a phone number capabilities update
     *
     * @param capabilitiesId ID of the capabilities update.
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} whose {@link Response#getValue()} value returns
     * a {@link UpdatePhoneNumberCapabilitiesResponse} representing the capabilities update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UpdatePhoneNumberCapabilitiesResponse> getCapabilitiesUpdateWithResponse(
        String capabilitiesId, Context context) {
        return phoneNumberAsyncClient.getCapabilitiesUpdateWithResponse(capabilitiesId, context).block();
    }

    /**
     * Adds or removes phone number capabilities.
     *
     * @param phoneNumberCapabilitiesUpdate {@link Map} with the updates to perform
     * @return A {@link UpdatePhoneNumberCapabilitiesResponse} representing the capabilities update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UpdateNumberCapabilitiesResponse updateCapabilities(
        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate) {
        return phoneNumberAsyncClient.updateCapabilities(phoneNumberCapabilitiesUpdate).block();
    }

    /**
     * Adds or removes phone number capabilities.
     *
     * @param phoneNumberCapabilitiesUpdate {@link Map} with the updates to perform
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} whose {@link Response#getValue()} value returns
     * a {@link UpdatePhoneNumberCapabilitiesResponse} representing the capabilities update.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UpdateNumberCapabilitiesResponse> updateCapabilitiesWithResponse(
        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> phoneNumberCapabilitiesUpdate, Context context) {
        return phoneNumberAsyncClient.updateCapabilitiesWithResponse(phoneNumberCapabilitiesUpdate, context).block();
    }

    /**
     * Gets a list of supported countries.
     *
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link PagedIterable} of {@link PhoneNumberCountry} instances representing supported countries.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberCountry> listAllSupportedCountries(String locale) {
        return new PagedIterable<>(phoneNumberAsyncClient.listAllSupportedCountries(locale));
    }

    /**
     * Gets a list of supported countries.
     *
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhoneNumberCountry} instances representing supported countries.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberCountry> listAllSupportedCountries(String locale, Context context) {
        return new PagedIterable<>(phoneNumberAsyncClient.listAllSupportedCountries(locale, context));
    }

    /**
     * Gets the configuration of a given phone number.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @return A {@link NumberConfigurationResponse} representing the configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NumberConfigurationResponse getNumberConfiguration(PhoneNumberIdentifier phoneNumber) {
        return phoneNumberAsyncClient.getNumberConfiguration(phoneNumber).block();
    }

    /**
     * Gets the configuration of a given phone number.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} whose {@link Response#getValue()} value returns
     * a {@link NumberConfigurationResponse} representing the configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NumberConfigurationResponse> getNumberConfigurationWithResponse(
        PhoneNumberIdentifier phoneNumber, Context context) {
        return phoneNumberAsyncClient.getNumberConfigurationWithResponse(phoneNumber, context).block();
    }

    /**
     * Associates a phone number with a PSTN Configuration.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @param pstnConfiguration A {@link PstnConfiguration} containing the pstn number configuration options.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void configureNumber(PhoneNumberIdentifier phoneNumber, PstnConfiguration pstnConfiguration) {
        phoneNumberAsyncClient.configureNumber(phoneNumber, pstnConfiguration).block();
    }

    /**
     * Associates a phone number with a PSTN Configuration.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @param pstnConfiguration A {@link PstnConfiguration} containing the pstn number configuration options.
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} for the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> configureNumberWithResponse(
        PhoneNumberIdentifier phoneNumber, PstnConfiguration pstnConfiguration, Context context) {
        return phoneNumberAsyncClient.configureNumberWithResponse(phoneNumber, pstnConfiguration, context).block();
    }

    /**
     * Removes the PSTN Configuration from a phone number.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void unconfigureNumber(PhoneNumberIdentifier phoneNumber) {
        phoneNumberAsyncClient.unconfigureNumber(phoneNumber).block();
    }

    /**
     * Removes the PSTN Configuration from a phone number.
     *
     * @param phoneNumber A {@link PhoneNumberIdentifier} representing the phone number.
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} for the operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> unconfigureNumberWithResponse(PhoneNumberIdentifier phoneNumber, Context context) {
        return phoneNumberAsyncClient.unconfigureNumberWithResponse(phoneNumber, context).block();
    }

    /**
     * Gets a list of phone plan groups for the given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @param includeRateInformation Flag to indicate if rate information should be returned.
     * @return A {@link PagedIterable} of {@link PhonePlanGroup} instances representing phone plan groups
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhonePlanGroup> listPhonePlanGroups(
        String countryCode, String locale, Boolean includeRateInformation) {
        return new PagedIterable<>(
            phoneNumberAsyncClient.listPhonePlanGroups(countryCode, locale, includeRateInformation));
    }

    /**
     * Gets a list of phone plan groups for the given country.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @param includeRateInformation Flag to indicate if rate information should be returned.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhonePlanGroup} instances representing phone plan groups
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhonePlanGroup> listPhonePlanGroups(
        String countryCode, String locale, Boolean includeRateInformation, Context context) {
        return new PagedIterable<>(
            phoneNumberAsyncClient.listPhonePlanGroups(countryCode, locale, includeRateInformation, context));
    }

    /**
     * Gets a list of phone plans for a phone plan group
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanGroupId ID of the Phone Plan Group
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link PagedIterable} of {@link PhonePlan} instances representing phone plans
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhonePlan> listPhonePlans(String countryCode, String phonePlanGroupId, String locale) {
        return new PagedIterable<>(phoneNumberAsyncClient.listPhonePlans(countryCode, phonePlanGroupId, locale));
    }

    /**
     * Gets a list of phone plans for a phone plan group
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanGroupId ID of the Phone Plan Group
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhonePlan} instances representing phone plans
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhonePlan> listPhonePlans(
        String countryCode, String phonePlanGroupId, String locale, Context context) {
        return new PagedIterable<>(phoneNumberAsyncClient.listPhonePlans(
            countryCode, phonePlanGroupId, locale, context));
    }

    /**
     * Gets the location options for a phone plan.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanGroupId ID of the Phone Plan Group
     * @param phonePlanId ID of the Phone Plan
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @return A {@link LocationOptionsResponse} representing the location options
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public LocationOptionsResponse getPhonePlanLocationOptions(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale) {
        return phoneNumberAsyncClient.getPhonePlanLocationOptions(countryCode, phonePlanGroupId, phonePlanId, locale)
            .block();
    }

    /**
     * Gets the location options for a phone plan.
     *
     * @param countryCode The ISO 3166-2 country code.
     * @param phonePlanGroupId ID of the Phone Plan Group
     * @param phonePlanId ID of the Phone Plan
     * @param locale A language-locale pairing which will be used to localise the names of countries.
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} whose {@link Response#getValue()} value returns
     * a {@link LocationOptionsResponse} representing the location options
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<LocationOptionsResponse> getPhonePlanLocationOptionsWithResponse(
        String countryCode, String phonePlanGroupId, String phonePlanId, String locale, Context context) {
        return phoneNumberAsyncClient.getPhonePlanLocationOptionsWithResponse(
            countryCode, phonePlanGroupId, phonePlanId, locale, context).block();
    }

    /**
     * Gets a release by ID.
     *
     * @param releaseId ID of the Release
     * @return A {@link PhoneNumberRelease} representing the release.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PhoneNumberRelease getReleaseById(String releaseId) {
        return phoneNumberAsyncClient.getReleaseById(releaseId).block();
    }

    /**
     * Gets a release by ID.
     *
     * @param releaseId ID of the Release
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} whose {@link Response#getValue()} value returns
     * a {@link PhoneNumberRelease} representing the release.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PhoneNumberRelease> getReleaseByIdWithResponse(String releaseId, Context context) {
        return phoneNumberAsyncClient.getReleaseByIdWithResponse(releaseId, context).block();
    }

    /**
     * Gets the list of all releases
     *
     * @return A {@link PagedIterable} of {@link PhoneNumberEntity} instances representing releases.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberEntity> listAllReleases() {
        return new PagedIterable<>(phoneNumberAsyncClient.listAllReleases());
    }

    /**
     * Gets the list of all releases
     *
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhoneNumberEntity} instances representing releases.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberEntity> listAllReleases(Context context) {
        return new PagedIterable<>(phoneNumberAsyncClient.listAllReleases(context));
    }

    /**
     * Gets a reservation by ID.
     *
     * @param reservationId ID of the reservation
     * @return A {@link PhoneNumberReservation} representing the reservation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PhoneNumberReservation getReservationById(String reservationId) {
        return phoneNumberAsyncClient.getReservationById(reservationId).block();
    }

    /**
     * Gets a reservation by ID.
     *
     * @param reservationId ID of the reservation
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} whose {@link Response#getValue()} value returns
     * a {@link PhoneNumberReservation} representing the reservation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PhoneNumberReservation> getReservationByIdWithResponse(String reservationId, Context context) {
        return phoneNumberAsyncClient.getReservationByIdWithResponse(reservationId, context).block();
    }

    /**
     * Gets the list of all reservations
     *
     * @return A {@link PagedIterable} of {@link PhoneNumberEntity} instances representing reservations.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberEntity> listAllReservations() {
        return new PagedIterable<>(phoneNumberAsyncClient.listAllReservations());
    }

    /**
     * Gets the list of all reservationes
     *
     * @param context A {@link Context} representing the request context.
     * @return A {@link PagedIterable} of {@link PhoneNumberEntity} instances representing reservations.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<PhoneNumberEntity> listAllReservations(Context context) {
        return new PagedIterable<>(phoneNumberAsyncClient.listAllReservations(context));
    }

    /**
     * Cancels the reservation. This means existing numbers in the reservation will be made available.
     *
     * @param reservationId ID of the reservation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void cancelReservation(String reservationId) {
        phoneNumberAsyncClient.cancelReservation(reservationId).block();
    }

    /**
     * Cancels the reservation. This means existing numbers in the reservation will be made available.
     *
     * @param reservationId ID of the reservation
     * @param context A {@link Context} representing the request context.
     * @return A {@link Response} for the operation
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelReservationWithResponse(String reservationId, Context context) {
        return phoneNumberAsyncClient.cancelReservationWithResponse(reservationId, context).block();
    }

    /**
     * Initiates a reservation and returns a {@link PhoneNumberReservation} usable by other functions
     * This function returns a Long Running Operation poller.
     *
     * @param options A {@link CreateReservationOptions} with the reservation options
     * @param pollInterval The time our long running operation will keep on polling
     * until it gets a result from the server
     * @return A {@link SyncPoller} object with the reservation result
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<PhoneNumberReservation, PhoneNumberReservation> beginCreateReservation(
        CreateReservationOptions options, Duration pollInterval) {
        return phoneNumberAsyncClient.beginCreateReservation(options, pollInterval).getSyncPoller();
    }

    /**
     * Initiates a purchase process and polls until a terminal state is reached
     * This function returns a Long Running Operation poller
     *
     * @param reservationId ID of the reservation
     * @param pollInterval The time our long running operation will keep on polling
     * until it gets a result from the server
     * @return A {@link SyncPoller} object with the reservation result
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<Void, Void> beginPurchaseReservation(
        String reservationId, Duration pollInterval) {
        return phoneNumberAsyncClient.beginPurchaseReservation(reservationId, pollInterval).getSyncPoller();
    }

 /**
     * Releases the given phone numbers.
     * This function returns a Long Running Operation poller
     *
     * @param phoneNumbers A list of {@link PhoneNumberIdentifier} with the desired numbers to release
     * @param pollInterval The time our long running operation will keep on polling
     * until it gets a result from the server
     * @return A {@link SyncPoller} object with the reservation result
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<PhoneNumberRelease, PhoneNumberRelease> beginReleasePhoneNumbers(
        List<PhoneNumberIdentifier> phoneNumbers, Duration pollInterval) {
        return phoneNumberAsyncClient.beginReleasePhoneNumbers(phoneNumbers, pollInterval).getSyncPoller();
    }
}
