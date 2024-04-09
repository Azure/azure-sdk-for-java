// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.implementation.converters.PhoneNumberErrorConverter;
import com.azure.communication.phonenumbers.implementation.models.CommunicationError;
import com.azure.communication.phonenumbers.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.phonenumbers.models.PhoneNumberAdministrativeDivision;
import com.azure.communication.phonenumbers.models.OperatorInformationResult;
import com.azure.communication.phonenumbers.models.PhoneNumberAreaCode;
import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilityType;
import com.azure.communication.phonenumbers.models.PhoneNumberCountry;
import com.azure.communication.phonenumbers.models.PhoneNumberError;
import com.azure.communication.phonenumbers.models.PhoneNumberLocality;
import com.azure.communication.phonenumbers.models.PhoneNumberOffering;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberOperationStatus;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchOptions;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.communication.phonenumbers.models.PurchasePhoneNumbersResult;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.phonenumbers.models.ReleasePhoneNumberResult;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class PhoneNumbersClientIntegrationTest extends PhoneNumbersIntegrationTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumber(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        PurchasedPhoneNumber number = this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberSync")
                .getPurchasedPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithAAD(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        PurchasedPhoneNumber number = this
                .getClientWithManagedIdentity(httpClient, "getPurchasedPhoneNumberWithAADSync")
                .getPurchasedPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithResponse(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        Response<PurchasedPhoneNumber> response = this
                .getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberWithResponseSync")
                .getPurchasedPhoneNumberWithResponse(phoneNumber, Context.NONE);
        PurchasedPhoneNumber number = response.getValue();
        assertEquals(200, response.getStatusCode());
        assertEquals(phoneNumber, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPurchasedPhoneNumbers(HttpClient httpClient) {
        PagedIterable<PurchasedPhoneNumber> numbers = this
                .getClientWithConnectionString(httpClient, "listPurchasedPhoneNumbersSync")
                .listPurchasedPhoneNumbers(Context.NONE);
        PurchasedPhoneNumber number = numbers.iterator().next();
        assertNotNull(number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPurchasedPhoneNumbersWithoutContext(HttpClient httpClient) {
        PagedIterable<PurchasedPhoneNumber> numbers = this
                .getClientWithConnectionString(httpClient, "listPurchasedPhoneNumbersSync").listPurchasedPhoneNumbers();
        PurchasedPhoneNumber number = numbers.iterator().next();
        assertNotNull(number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    public void beginSearchAvailablePhoneNumbers(HttpClient httpClient) {
        SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> poller = beginSearchAvailablePhoneNumbersHelper(
                httpClient, "beginSearchAvailablePhoneNumbersSync", true);
        PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();
        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PhoneNumberSearchResult searchResult = poller.getFinalResult();
            assertEquals(searchResult.getPhoneNumbers().size(), 1);
            assertNotNull(searchResult.getSearchId());
        } else {
            fail("Long Running Operation Status was not successfully completed");
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "SKIP_LIVE_TEST", matches = "(?i)(true)")
    public void beginPurchaseandReleasePhoneNumbersWithoutContext(HttpClient httpClient) {
        SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> poller = beginSearchAvailablePhoneNumbersHelper(
                httpClient, "beginPurchaseandReleasePhoneNumbers_beginSearchAvailablePhoneNumbersWithoutContextSync",
                false);
        PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();
        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PhoneNumberSearchResult searchResult = poller.getFinalResult();
            String phoneNumber = searchResult.getPhoneNumbers().get(0);
            PollResponse<PhoneNumberOperation> purchaseOperationResponse = beginPurchasePhoneNumbersHelper(httpClient,
                    searchResult.getSearchId(), "beginPurchasePhoneNumbersWithoutContextSync", false)
                    .waitForCompletion();
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, purchaseOperationResponse.getStatus());
            PollResponse<PhoneNumberOperation> releaseOperationResponse = beginReleasePhoneNumberHelper(httpClient,
                    phoneNumber, "beginReleasePhoneNumberWithoutContextSync", false).waitForCompletion();
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseOperationResponse.getStatus());
        } else {
            fail("Long Running Operation Status was not successfully completed");
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "SKIP_LIVE_TEST", matches = "(?i)(true)")
    public void beginPurchaseandReleasePhoneNumbers(HttpClient httpClient) {
        SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> poller = beginSearchAvailablePhoneNumbersHelper(
                httpClient, "beginPurchaseandReleasePhoneNumbers_beginSearchAvailablePhoneNumbersSync", true);
        PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();
        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PhoneNumberSearchResult searchResult = poller.getFinalResult();
            String phoneNumber = searchResult.getPhoneNumbers().get(0);
            PollResponse<PhoneNumberOperation> purchaseOperationResponse = beginPurchasePhoneNumbersHelper(httpClient,
                    searchResult.getSearchId(), "beginPurchasePhoneNumbersSync", true).waitForCompletion();
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, purchaseOperationResponse.getStatus());
            PollResponse<PhoneNumberOperation> releaseOperationResponse = beginReleasePhoneNumberHelper(httpClient,
                    phoneNumber, "beginReleasePhoneNumberSync", true).waitForCompletion();
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseOperationResponse.getStatus());
        } else {
            fail("Long Running Operation Status was not successfully completed");
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    public void beginUpdatePhoneNumberCapabilitiesWithoutContext(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber();
        PollResponse<PhoneNumberOperation> result = beginUpdatePhoneNumberCapabilitiesHelper(httpClient, phoneNumber,
                "beginUpdatePhoneNumberCapabilitiesWithoutContextSync", false).waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result.getStatus());
        assertEquals(PhoneNumberOperationStatus.SUCCEEDED, result.getValue().getStatus());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    @DisabledIfEnvironmentVariable(named = "SKIP_UPDATE_CAPABILITIES_LIVE_TESTS", matches = "(?i)(true)")
    public void beginUpdatePhoneNumberCapabilities(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber();
        PollResponse<PhoneNumberOperation> result = beginUpdatePhoneNumberCapabilitiesHelper(httpClient, phoneNumber,
                "beginUpdatePhoneNumberCapabilities", true).waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result.getStatus());
        assertEquals(PhoneNumberOperationStatus.SUCCEEDED, result.getValue().getStatus());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodesWithoutContext(HttpClient httpClient) {
        PagedIterable<PhoneNumberAreaCode> areaCodesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableTollFreeAreaCodes")
                .listAvailableTollFreeAreaCodes("US");
        List<String> expectedAreaCodes = Arrays.asList("888", "877", "866", "855", "844", "800", "833", "88");
        for (PhoneNumberAreaCode areaCode : areaCodesResult) {
            assertTrue(expectedAreaCodes.contains(areaCode.getAreaCode()));
        }
        assertNotNull(areaCodesResult);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodes(HttpClient httpClient) {
        PagedIterable<PhoneNumberAreaCode> areaCodesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableTollFreeAreaCodes")
                .listAvailableTollFreeAreaCodes("US", Context.NONE);
        List<String> expectedAreaCodes = Arrays.asList("888", "877", "866", "855", "844", "800", "833", "88");
        for (PhoneNumberAreaCode areaCode : areaCodesResult) {
            assertTrue(expectedAreaCodes.contains(areaCode.getAreaCode()));
        }
        assertNotNull(areaCodesResult);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodesWrongCountryCode(HttpClient httpClient) {
        PhoneNumbersClient client = this.getClientWithConnectionString(httpClient, "listAvailableAreaCodes");

        assertThrows(RuntimeException.class,
                () -> client.listAvailableTollFreeAreaCodes("XX", null).iterator().next(),
                "Unable to parse country code.");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodesWithoutContext(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null).iterator().next();
        PagedIterable<PhoneNumberAreaCode> areaCodesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableGeographicAreaCodes")
                .listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON, locality.getLocalizedName(),
                        locality.getAdministrativeDivision().getAbbreviatedName());
        PhoneNumberAreaCode areaCodes = areaCodesResult.iterator().next();
        assertNotNull(areaCodes);
        assertNotNull(areaCodes.getAreaCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodes(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null).iterator().next();
        PagedIterable<PhoneNumberAreaCode> areaCodesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableGeographicAreaCodes")
                .listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON, locality.getLocalizedName(),
                        locality.getAdministrativeDivision().getAbbreviatedName(), Context.NONE);
        PhoneNumberAreaCode areaCodes = areaCodesResult.iterator().next();
        assertNotNull(areaCodes);
        assertNotNull(areaCodes.getAreaCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodesWrongLocality(HttpClient httpClient) {
        PhoneNumbersClient client = this.getClientWithConnectionString(httpClient, "listAvailableAreaCodes");

        assertThrows(RuntimeException.class,
                () -> client.listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON, "XX",
                        "XX").iterator().next(),
                "No area codes were found for the given parameters");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCountriesWithoutContext(HttpClient httpClient) {
        PagedIterable<PhoneNumberCountry> countriesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableCountries").listAvailableCountries();
        PhoneNumberCountry country = countriesResult.iterator().next();
        assertNotNull(country);
        assertNotNull(country.getCountryCode());
        assertNotNull(country.getLocalizedName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCountries(HttpClient httpClient) {
        PagedIterable<PhoneNumberCountry> countriesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableCountries")
                .listAvailableCountries(Context.NONE);
        PhoneNumberCountry country = countriesResult.iterator().next();
        assertNotNull(country);
        assertNotNull(country.getCountryCode());
        assertNotNull(country.getLocalizedName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithoutContext(HttpClient httpClient) {
        PagedIterable<PhoneNumberLocality> localitiesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null);
        PhoneNumberLocality locality = localitiesResult.iterator().next();
        assertNotNull(locality);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalities(HttpClient httpClient) {
        PagedIterable<PhoneNumberLocality> localitiesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null, Context.NONE);
        PhoneNumberLocality locality = localitiesResult.iterator().next();
        assertNotNull(locality);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithInvalidAdministrativeDivision(HttpClient httpClient) {
        PhoneNumbersClient client = this.getClientWithConnectionString(httpClient, "listAvailableLocalities");

        assertThrows(CommunicationErrorResponseException.class,
                () -> client.listAvailableLocalities("US", "null").iterator().next(),
                "No localities were found for the given parameters");
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithAdministrativeDivision(HttpClient httpClient) {
        PhoneNumberAdministrativeDivision localityAdministraiveDivision = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null).iterator().next().getAdministrativeDivision();
        PagedIterable<PhoneNumberLocality> localitiesResult = this
                .getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", localityAdministraiveDivision.getAbbreviatedName());
        PhoneNumberLocality locality = localitiesResult.iterator().next();
        assertNotNull(locality);
        assertEquals(locality.getAdministrativeDivision().getAbbreviatedName(), localityAdministraiveDivision.getAbbreviatedName());
        assertEquals(locality.getAdministrativeDivision().getLocalizedName(), localityAdministraiveDivision.getLocalizedName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getOfferingsWithoutContext(HttpClient httpClient) {
        PagedIterable<PhoneNumberOffering> offeringsResult = this
                .getClientWithConnectionString(httpClient, "listAvailableOfferings")
                .listAvailableOfferings("US", null, null);
        PhoneNumberOffering offering = offeringsResult.iterator().next();
        assertNotNull(offering);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getOfferings(HttpClient httpClient) {
        PagedIterable<PhoneNumberOffering> offeringsResult = this
                .getClientWithConnectionString(httpClient, "listAvailableOfferings")
                .listAvailableOfferings("US", null, null, Context.NONE);
        PhoneNumberOffering offering = offeringsResult.iterator().next();
        assertNotNull(offering);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodesWithAAD(HttpClient httpClient) {
        PagedIterable<PhoneNumberAreaCode> areaCodesResult = this
                .getClientWithManagedIdentity(httpClient, "listAvailableTollFreeAreaCodes")
                .listAvailableTollFreeAreaCodes("US");
        List<String> expectedAreaCodes = Arrays.asList("888", "877", "866", "855", "844", "800", "833", "88");
        for (PhoneNumberAreaCode areaCode : areaCodesResult) {
            assertTrue(expectedAreaCodes.contains(areaCode.getAreaCode()));
        }
        assertNotNull(areaCodesResult);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodesWithAAD(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null).iterator().next();
        PagedIterable<PhoneNumberAreaCode> areaCodesResult = this
                .getClientWithManagedIdentity(httpClient, "listAvailableGeographicAreaCodes")
                .listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON, locality.getLocalizedName(),
                        locality.getAdministrativeDivision().getAbbreviatedName());
        PhoneNumberAreaCode areaCodes = areaCodesResult.iterator().next();
        assertNotNull(areaCodes);
        assertNotNull(areaCodes.getAreaCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCountriesWithAAD(HttpClient httpClient) {
        PagedIterable<PhoneNumberCountry> countriesResult = this
                .getClientWithManagedIdentity(httpClient, "listAvailableCountries").listAvailableCountries();
        PhoneNumberCountry country = countriesResult.iterator().next();
        assertNotNull(country);
        assertNotNull(country.getCountryCode());
        assertNotNull(country.getLocalizedName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithAAD(HttpClient httpClient) {
        PagedIterable<PhoneNumberLocality> localitiesResult = this
                .getClientWithManagedIdentity(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null);
        PhoneNumberLocality locality = localitiesResult.iterator().next();
        assertNotNull(locality);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithAdministrativeDivisionWithAAD(HttpClient httpClient) {
        PagedIterable<PhoneNumberLocality> localities = this
                .getClientWithManagedIdentity(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null);
        PhoneNumberLocality localityWithAD = localities.iterator().next();

        PagedIterable<PhoneNumberLocality> localitiesResult = this
                .getClientWithManagedIdentity(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", localityWithAD.getAdministrativeDivision().getAbbreviatedName());
        PhoneNumberLocality locality = localitiesResult.iterator().next();
        assertNotNull(locality);
        assertEquals(locality.getAdministrativeDivision().getAbbreviatedName(), localityWithAD.getAdministrativeDivision().getAbbreviatedName());
        assertEquals(locality.getAdministrativeDivision().getLocalizedName(), localityWithAD.getAdministrativeDivision().getLocalizedName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getOfferingsWithAAD(HttpClient httpClient) {
        PagedIterable<PhoneNumberOffering> offeringsResult = this
                .getClientWithManagedIdentity(httpClient, "listAvailableOfferings")
                .listAvailableOfferings("US", null, null);
        PhoneNumberOffering offering = offeringsResult.iterator().next();
        assertNotNull(offering);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void convertCommunicationError(HttpClient httpClient) {
        List<PhoneNumberError> details = new ArrayList<PhoneNumberError>();
        CommunicationError communicationError = new CommunicationError();
        communicationError.setCode("500");
        communicationError.setMessage("Communication Error");

        PhoneNumberError phoneNumberError = new PhoneNumberError(
            communicationError.getMessage(),
            communicationError.getCode(),
            communicationError.getTarget(),
            details
        );
        PhoneNumberError error = PhoneNumberErrorConverter.convert(communicationError);
        assertEquals(phoneNumberError.getCode(), error.getCode());
        assertEquals(phoneNumberError.getMessage(), error.getMessage());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void convertCommunicationErrorWithNull(HttpClient httpClient) {
        CommunicationError communicationError = null;
        PhoneNumberError error = PhoneNumberErrorConverter.convert(communicationError);
        assertEquals(null, error);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void searchOperatorInformationSucceeds(HttpClient httpClient) {
        List<String> phoneNumbers = new ArrayList<String>();
        phoneNumbers.add(redactIfPlaybackMode(getTestPhoneNumber()));
        OperatorInformationResult result = this.getClientWithConnectionString(httpClient, "searchOperatorInformation")
                .searchOperatorInformation(phoneNumbers);
        assertEquals(phoneNumbers.get(0), result.getValues().get(0).getPhoneNumber());
    }

    private SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbersHelper(
            HttpClient httpClient, String testName, boolean withContext) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setQuantity(1);

        if (withContext) {
            return setPollInterval(getClientWithConnectionString(httpClient, testName).beginSearchAvailablePhoneNumbers(
                    COUNTRY_CODE,
                    PhoneNumberType.TOLL_FREE,
                    PhoneNumberAssignmentType.APPLICATION,
                    capabilities,
                    searchOptions,
                    Context.NONE));
        }
        return setPollInterval(getClientWithConnectionString(httpClient, testName).beginSearchAvailablePhoneNumbers(
                COUNTRY_CODE,
                PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION,
                capabilities));
    }

    private SyncPoller<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbersHelper(
            HttpClient httpClient, String searchId, String testName, boolean withContext) {
        if (withContext) {
            return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
                    .beginPurchasePhoneNumbers(searchId, Context.NONE));
        }
        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
                .beginPurchasePhoneNumbers(searchId));
    }

    private SyncPoller<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumberHelper(
            HttpClient httpClient, String phoneNumber, String testName, boolean withContext) {
        if (getTestMode() == TestMode.PLAYBACK) {
            phoneNumber = "+REDACTED";
        }
        if (withContext) {
            return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
                    .beginReleasePhoneNumber(phoneNumber, Context.NONE));
        }
        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
                .beginReleasePhoneNumber(phoneNumber));
    }

    private SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilitiesHelper(
            HttpClient httpClient, String phoneNumber, String testName, boolean withContext) {
        PhoneNumbersClient client = this.getClientWithConnectionString(httpClient, testName);
        Response<PurchasedPhoneNumber> responseAcquiredPhone = client.getPurchasedPhoneNumberWithResponse(phoneNumber,
                Context.NONE);
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(
                responseAcquiredPhone.getValue().getCapabilities().getCalling() == PhoneNumberCapabilityType.INBOUND
                        ? PhoneNumberCapabilityType.OUTBOUND
                        : PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(responseAcquiredPhone.getValue().getCapabilities()
                .getSms() == PhoneNumberCapabilityType.INBOUND_OUTBOUND ? PhoneNumberCapabilityType.OUTBOUND
                        : PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        if (withContext) {
            return setPollInterval(client.beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities, Context.NONE));
        }
        return setPollInterval(client.beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities));
    }

    private <T, U> SyncPoller<T, U> setPollInterval(SyncPoller<T, U> syncPoller) {
        return interceptorManager.isPlaybackMode()
                ? syncPoller.setPollInterval(Duration.ofMillis(1))
                : syncPoller.setPollInterval(Duration.ofSeconds(1));
    }

    private PhoneNumbersClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }

    private PhoneNumbersClient getClientWithManagedIdentity(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderUsingManagedIdentity(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
