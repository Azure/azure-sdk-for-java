// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.implementation.converters.PhoneNumberErrorConverter;
import com.azure.communication.phonenumbers.implementation.models.CommunicationError;
import com.azure.communication.phonenumbers.models.AvailablePhoneNumber;
import com.azure.communication.phonenumbers.models.BillingFrequency;
import com.azure.communication.phonenumbers.models.PhoneNumberAdministrativeDivision;
import com.azure.communication.phonenumbers.models.OperatorInformationResult;
import com.azure.communication.phonenumbers.models.OperatorInformationOptions;
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
import com.azure.communication.phonenumbers.models.PhoneNumbersBrowseResult;
import com.azure.communication.phonenumbers.models.BrowsePhoneNumbersOptions;
import com.azure.communication.phonenumbers.models.CreateOrUpdateReservationOptions;
import com.azure.communication.phonenumbers.models.PhoneNumbersReservation;
import com.azure.communication.phonenumbers.models.PurchasePhoneNumbersResult;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.phonenumbers.models.ReleasePhoneNumberResult;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhoneNumbersAsyncClientIntegrationTest extends PhoneNumbersIntegrationTestBase {

    private String reservationId = getReservationId();

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumber(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumber")
            .getPurchasedPhoneNumber(phoneNumber)).assertNext((PurchasedPhoneNumber number) -> {
                assertEquals(phoneNumber, number.getPhoneNumber());
                assertEquals(COUNTRY_CODE, number.getCountryCode());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithAAD(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "getPurchasedPhoneNumberWithAAD")
            .getPurchasedPhoneNumber(phoneNumber)).assertNext((PurchasedPhoneNumber number) -> {
                assertEquals(phoneNumber, number.getPhoneNumber());
                assertEquals(COUNTRY_CODE, number.getCountryCode());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithResponse(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberWithResponse")
                .getPurchasedPhoneNumberWithResponse(phoneNumber))
            .assertNext((Response<PurchasedPhoneNumber> response) -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(phoneNumber, response.getValue().getPhoneNumber());
                assertEquals(COUNTRY_CODE, response.getValue().getCountryCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPurchasedPhoneNumbers(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listPurchasedPhoneNumbers")
            .listPurchasedPhoneNumbers()
            .next()).assertNext((PurchasedPhoneNumber number) -> {
                assertNotNull(number.getPhoneNumber());
                assertEquals(COUNTRY_CODE, number.getCountryCode());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    public void beginSearchAvailablePhoneNumbers(HttpClient httpClient) {
        PhoneNumbersAsyncClient client
            = this.getClientWithConnectionString(httpClient, "beginSearchAvailablePhoneNumbers");
        StepVerifier.create(beginSearchAvailablePhoneNumbersHelper(client, true).last()
            .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                return result.getFinalResult();
            })).assertNext((PhoneNumberSearchResult searchResult) -> {
                assertEquals(searchResult.getPhoneNumbers().size(), 1);
                assertNotNull(searchResult.getSearchId());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    public void beginSearchAvailablePhoneNumbersWithoutOptions(HttpClient httpClient) {
        PhoneNumbersAsyncClient client
            = this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberForCapabilities");
        StepVerifier.create(beginSearchAvailablePhoneNumbersHelper(client, false).last()
            .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                return result.getFinalResult();
            })).assertNext((PhoneNumberSearchResult searchResult) -> {
                assertEquals(searchResult.getPhoneNumbers().size(), 1);
                assertNotNull(searchResult.getSearchId());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "SKIP_LIVE_TEST", matches = "(?i)(true)")
    public void beginPurchaseandReleasePhoneNumbers(HttpClient httpClient) {
        PhoneNumbersAsyncClient client
            = this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberForCapabilities");
        StepVerifier.create(beginSearchAvailablePhoneNumbersHelper(client, true).last()
            .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                return result.getFinalResult().flatMap((PhoneNumberSearchResult searchResult) -> {
                    String phoneNumber = searchResult.getPhoneNumbers().get(0);
                    return beginPurchasePhoneNumbersHelper(client, searchResult.getSearchId()).last()
                        .flatMap(
                            (AsyncPollResponse<PhoneNumberOperation, PurchasePhoneNumbersResult> purchaseResult) -> {
                                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                                    purchaseResult.getStatus());
                                return beginReleasePhoneNumberHelper(client, phoneNumber).last();
                            });
                });
            })).assertNext((AsyncPollResponse<PhoneNumberOperation, ReleasePhoneNumberResult> releaseResult) -> {
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseResult.getStatus());

            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    @DisabledIfEnvironmentVariable(named = "SKIP_UPDATE_CAPABILITIES_LIVE_TESTS", matches = "(?i)(true)")
    public void beginUpdatePhoneNumberCapabilities(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber();
        PhoneNumbersAsyncClient client
            = this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberForCapabilities");
        StepVerifier.create(client.getPurchasedPhoneNumberWithResponse(phoneNumber).flatMap(responseAcquiredPhone -> {
            PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
            capabilities.setCalling(
                responseAcquiredPhone.getValue().getCapabilities().getCalling() == PhoneNumberCapabilityType.INBOUND
                    ? PhoneNumberCapabilityType.OUTBOUND
                    : PhoneNumberCapabilityType.INBOUND);
            capabilities.setSms(responseAcquiredPhone.getValue().getCapabilities().getSms()
                == PhoneNumberCapabilityType.INBOUND_OUTBOUND
                    ? PhoneNumberCapabilityType.OUTBOUND
                    : PhoneNumberCapabilityType.INBOUND_OUTBOUND);
            return beginUpdatePhoneNumberCapabilitiesHelper(client, phoneNumber, capabilities).last()
                .flatMap((AsyncPollResponse<PhoneNumberOperation, PurchasedPhoneNumber> result) -> {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result.getStatus());
                    assertEquals(PhoneNumberOperationStatus.SUCCEEDED, result.getValue().getStatus());
                    return result.getFinalResult();
                });
        })).assertNext(Assertions::assertNotNull).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberNullNumber(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberNullNumber")
            .getPurchasedPhoneNumber(null)).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithResponseNullNumber(HttpClient httpClient) {
        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberWithResponseNullNumber")
                .getPurchasedPhoneNumberWithResponse(null))
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginSearchAvailablePhoneNumbersNullCountryCode(HttpClient httpClient) {
        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "beginSearchAvailablePhoneNumbersNullCountryCode")
                .beginSearchAvailablePhoneNumbers(null, PhoneNumberType.TOLL_FREE,
                    PhoneNumberAssignmentType.APPLICATION, null, null))
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesNullPhoneNumber(HttpClient httpClient) {
        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "beginUpdatePhoneNumberCapabilitiesNullPhoneNumber")
                .beginUpdatePhoneNumberCapabilities(null, new PhoneNumberCapabilities()))
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesUnauthorizedPhoneNumber(HttpClient httpClient) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "beginUpdatePhoneNumberCapabilitiesUnauthorizedPhoneNumber")
                .beginUpdatePhoneNumberCapabilities("+14255555111", capabilities))
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesInvalidPhoneNumber(HttpClient httpClient) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        StepVerifier
            .create(
                this.getClientWithConnectionString(httpClient, "beginUpdatePhoneNumberCapabilitiesInvalidPhoneNumber")
                    .beginUpdatePhoneNumberCapabilities("invalid-phone-number", capabilities))
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodesWithAAD(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "listAvailableTollFreeAreaCodes")
            .listAvailableTollFreeAreaCodes("US", PhoneNumberAssignmentType.APPLICATION)
            .next()).expectAccessibleContext();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodesWithAAD(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("US", null)
            .blockFirst();
        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "listAvailableGeographicAreaCodes")
            .listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON, locality.getLocalizedName(),
                locality.getAdministrativeDivision().getAbbreviatedName())
            .next()).assertNext((PhoneNumberAreaCode areaCodes) -> {
                assertNotNull(areaCodes);
                assertNotNull(areaCodes.getAreaCode());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getMobileAreaCodesWithAAD(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("IE", null, PhoneNumberType.MOBILE)
            .blockFirst();
        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "listAvailableMobileAreaCodes")
            .listAvailableMobileAreaCodes("IE", PhoneNumberAssignmentType.APPLICATION, locality.getLocalizedName())
            .next()).assertNext((PhoneNumberAreaCode areaCodes) -> {
                assertNotNull(areaCodes);
                assertNotNull(areaCodes.getAreaCode());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCountriesWithAAD(HttpClient httpClient) {
        StepVerifier
            .create(
                this.getClientWithManagedIdentity(httpClient, "listAvailableCountries").listAvailableCountries().next())
            .assertNext((PhoneNumberCountry country) -> {
                assertNotNull(country);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithAAD(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("US", null)
            .next()).assertNext((PhoneNumberLocality locality) -> {
                assertNotNull(locality);
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesAdministrativeDivisionWithAAD(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("US", null)
            .blockFirst();
        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("US", locality.getAdministrativeDivision().getAbbreviatedName())
            .next()).assertNext((PhoneNumberLocality localityWithAD) -> {
                assertNotNull(localityWithAD);
                assertEquals(localityWithAD.getAdministrativeDivision().getAbbreviatedName(),
                    locality.getAdministrativeDivision().getAbbreviatedName());
                assertEquals(localityWithAD.getAdministrativeDivision().getLocalizedName(),
                    locality.getAdministrativeDivision().getLocalizedName());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithPhoneNumberTypeWithAAD(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("IE", null, PhoneNumberType.MOBILE)
            .next()).assertNext((PhoneNumberLocality locality) -> {
                assertNotNull(locality);
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getOfferingsWithAAD(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "listAvailableOfferings")
            .listAvailableOfferings("US", null, null)
            .next()).assertNext((PhoneNumberOffering offering) -> {
                assertNotNull(offering);
                offering.getCost().getBillingFrequency();
                assertNotNull(BillingFrequency.values());
                assertNotNull(offering.getCost().getCurrencyCode());
                assertNotNull(offering.getCost().getAmount());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodes(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableTollFreeAreaCodes")
            .listAvailableTollFreeAreaCodes("US", PhoneNumberAssignmentType.APPLICATION)
            .next()).expectAccessibleContext();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodesWrongCountryCode(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableTollFreeAreaCodes")
            .listAvailableTollFreeAreaCodes("XX", PhoneNumberAssignmentType.APPLICATION)
            .next()).expectError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodes(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("US", null)
            .blockFirst();
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableGeographicAreaCodes")
            .listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON, locality.getLocalizedName(),
                locality.getAdministrativeDivision().getAbbreviatedName())
            .next()).assertNext((PhoneNumberAreaCode areaCodes) -> {
                assertNotNull(areaCodes);
                assertNotNull(areaCodes.getAreaCode());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodesWronglocality(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableGeographicAreaCodes")
            .listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON, "XX", "XX")
            .next()).expectError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getMobileAreaCodes(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("IE", null, PhoneNumberType.MOBILE)
            .blockFirst();
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableMobileAreaCodes")
            .listAvailableMobileAreaCodes("IE", PhoneNumberAssignmentType.APPLICATION, locality.getLocalizedName())
            .next()).assertNext((PhoneNumberAreaCode areaCodes) -> {
                assertNotNull(areaCodes);
                assertNotNull(areaCodes.getAreaCode());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCountries(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "listAvailableCountries").listAvailableCountries().next())
            .assertNext((PhoneNumberCountry country) -> {
                assertNotNull(country);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalities(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("US", null)
            .next()).assertNext((PhoneNumberLocality locality) -> {
                assertNotNull(locality);
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesAdministrativeDivision(HttpClient httpClient) {
        PhoneNumberAdministrativeDivision localityAdministraiveDivision
            = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null)
                .blockFirst()
                .getAdministrativeDivision();
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("US", localityAdministraiveDivision.getAbbreviatedName())
            .next()).assertNext((PhoneNumberLocality locality) -> {
                assertNotNull(locality);
                assertEquals(locality.getAdministrativeDivision().getAbbreviatedName(),
                    localityAdministraiveDivision.getAbbreviatedName());
                assertEquals(locality.getAdministrativeDivision().getLocalizedName(),
                    localityAdministraiveDivision.getLocalizedName());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithPhoneNumberType(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("IE", null, PhoneNumberType.MOBILE)
            .next()).assertNext((PhoneNumberLocality locality) -> {
                assertNotNull(locality);
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesInvalidAdministrativeDivision(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
            .listAvailableLocalities("US", "null")
            .next()).expectError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getOfferings(HttpClient httpClient) {
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "listAvailableOfferings")
            .listAvailableOfferings("US", null, null)
            .next()).assertNext((PhoneNumberOffering offering) -> {
                assertNotNull(offering);
                offering.getCost().getBillingFrequency();
                assertNotNull(BillingFrequency.values());
                assertNotNull(offering.getCost().getCurrencyCode());
                assertNotNull(offering.getCost().getAmount());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void convertCommunicationError(HttpClient httpClient) {
        List<PhoneNumberError> details = new ArrayList<PhoneNumberError>();
        CommunicationError communicationError = new CommunicationError();
        communicationError.setCode("500");
        communicationError.setMessage("Communication Error");

        PhoneNumberError phoneNumberError = new PhoneNumberError(communicationError.getMessage(),
            communicationError.getCode(), communicationError.getTarget(), details);
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
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "searchOperatorInformation")
            .searchOperatorInformation(phoneNumbers)).assertNext((OperatorInformationResult result) -> {
                assertEquals(phoneNumbers.get(0), result.getValues().get(0).getPhoneNumber());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void searchOperatorInformationOnlyAcceptsOnePhoneNumber(HttpClient httpClient) {
        List<String> phoneNumbers = new ArrayList<String>();
        phoneNumbers.add(redactIfPlaybackMode(getTestPhoneNumber()));
        phoneNumbers.add(redactIfPlaybackMode(getTestPhoneNumber()));
        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "searchOperatorInformationOnlyAcceptsOnePhoneNumber")
                .searchOperatorInformation(phoneNumbers))
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void searchOperatorInformationRespectsSearchOptions(HttpClient httpClient) {
        List<String> phoneNumbers = new ArrayList<String>();
        phoneNumbers.add(redactIfPlaybackMode(getTestPhoneNumber()));
        OperatorInformationOptions requestOptions = new OperatorInformationOptions();
        requestOptions.setIncludeAdditionalOperatorDetails(false);
        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "searchOperatorInformation")
                .searchOperatorInformationWithResponse(phoneNumbers, requestOptions))
            .assertNext((Response<OperatorInformationResult> result) -> {
                assertEquals(phoneNumbers.get(0), result.getValue().getValues().get(0).getPhoneNumber());
                assertNotNull(result.getValue().getValues().get(0).getNationalFormat());
                assertNotNull(result.getValue().getValues().get(0).getInternationalFormat());
                assertEquals(null, result.getValue().getValues().get(0).getNumberType());
                assertEquals(null, result.getValue().getValues().get(0).getIsoCountryCode());
                assertEquals(null, result.getValue().getValues().get(0).getOperatorDetails());
            })
            .verifyComplete();

        requestOptions.setIncludeAdditionalOperatorDetails(true);
        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "searchOperatorInformation")
                .searchOperatorInformationWithResponse(phoneNumbers, requestOptions))
            .assertNext((Response<OperatorInformationResult> result) -> {
                assertEquals(phoneNumbers.get(0), result.getValue().getValues().get(0).getPhoneNumber());
                assertNotNull(result.getValue().getValues().get(0).getNationalFormat());
                assertNotNull(result.getValue().getValues().get(0).getInternationalFormat());
                assertNotNull(result.getValue().getValues().get(0).getNumberType());
                assertNotNull(result.getValue().getValues().get(0).getIsoCountryCode());
                assertNotNull(result.getValue().getValues().get(0).getOperatorDetails());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void browseAvailablePhoneNumberSucceeds(HttpClient httpClient) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions("US", PhoneNumberType.TOLL_FREE)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION)
            .setCapabilities(new PhoneNumberCapabilities().setCalling(PhoneNumberCapabilityType.INBOUND_OUTBOUND)
                .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND));

        StepVerifier.create(this.getClientWithConnectionString(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)).assertNext((result) -> {
                assertEquals(PhoneNumberType.TOLL_FREE, result.getPhoneNumbers().get(0).getPhoneNumberType());
                assertEquals(PhoneNumberAssignmentType.APPLICATION,
                    result.getPhoneNumbers().get(0).getAssignmentType());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void browseAvailablePhoneNumberWrongCountryCode(HttpClient httpClient) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions("INVALID", PhoneNumberType.TOLL_FREE)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION)
            .setCapabilities(new PhoneNumberCapabilities().setCalling(PhoneNumberCapabilityType.INBOUND_OUTBOUND)
                .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND));

        StepVerifier.create(this.getClientWithConnectionString(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void browseAvailablePhoneNumberSucceedsWithAAD(HttpClient httpClient) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions("US", PhoneNumberType.TOLL_FREE)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION)
            .setCapabilities(new PhoneNumberCapabilities().setCalling(PhoneNumberCapabilityType.INBOUND_OUTBOUND)
                .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND));

        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)).assertNext((result) -> {
                assertEquals(PhoneNumberType.TOLL_FREE, result.getPhoneNumbers().get(0).getPhoneNumberType());
                assertEquals(PhoneNumberAssignmentType.APPLICATION,
                    result.getPhoneNumbers().get(0).getAssignmentType());
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void browseAvailablePhoneNumberWrongCountryCodeWithAAD(HttpClient httpClient) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions("INVALID", PhoneNumberType.TOLL_FREE)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION)
            .setCapabilities(new PhoneNumberCapabilities().setCalling(PhoneNumberCapabilityType.INBOUND_OUTBOUND)
                .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND));

        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updatePhoneNumbersReservation(HttpClient httpClient) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions("US", PhoneNumberType.TOLL_FREE)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION);

        PhoneNumbersBrowseResult result = this.getClientWithConnectionString(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)
            .block(); // Blocking to retrieve the result synchronously

        assertNotNull(result);
        assertEquals(PhoneNumberType.TOLL_FREE, result.getPhoneNumbers().get(0).getPhoneNumberType());
        assertEquals(PhoneNumberAssignmentType.APPLICATION, result.getPhoneNumbers().get(0).getAssignmentType());

        List<AvailablePhoneNumber> numbersToAdd = new ArrayList<>();
        numbersToAdd.add(result.getPhoneNumbers().get(0));

        PhoneNumbersReservation reservationResponse
            = this.getClientWithConnectionString(httpClient, "updatePhoneNumberReservation")
                .createOrUpdateReservation(
                    new CreateOrUpdateReservationOptions(reservationId).setPhoneNumbersToAdd(numbersToAdd))
                .block();

        assertEquals(reservationId, reservationResponse.getId().toString());
        assertNotNull(reservationResponse.getPhoneNumbers());
        assertTrue(reservationResponse.getPhoneNumbers().containsKey(result.getPhoneNumbers().get(0).getId()));

        reservationResponse = this.getClientWithConnectionString(httpClient, "getPhoneNumberReservation")
            .getReservation(reservationId)
            .block();

        assertEquals(reservationId, reservationResponse.getId().toString());

        PagedFlux<PhoneNumbersReservation> reservationsList
            = this.getClientWithConnectionString(httpClient, "listPhoneNumberReservations").listReservations();

        StepVerifier.create(reservationsList.collectList()).assertNext(reservations -> {
            boolean containsReservation
                = reservations.stream().anyMatch(reservation -> reservation.getId().toString().equals(reservationId));
            assertTrue(containsReservation, "The reservations list does not contain the expected reservation.");
        }).verifyComplete();

        List<String> numbersToRemove = new ArrayList<>();
        numbersToRemove.add(result.getPhoneNumbers().get(0).getId());

        reservationResponse = this.getClientWithConnectionString(httpClient, "updatePhoneNumberReservation")
            .createOrUpdateReservation(
                new CreateOrUpdateReservationOptions(reservationId).setPhoneNumbersToRemove(numbersToRemove))
            .block();
        assertEquals(reservationId, reservationResponse.getId().toString());

        this.getClientWithConnectionString(httpClient, "deletePhoneNumberReservation")
            .deleteReservation(reservationId)
            .block();

        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "getPhoneNumberReservation")
                .getReservation(reservationId))
            .verifyError();
    }

    private static Stream<Arguments> httpClientAndPhoneNumberTypeProvider() {
        return com.azure.core.test.TestBase.getHttpClients()
            .flatMap(httpClient -> Stream.of(Arguments.of(httpClient, PhoneNumberType.TOLL_FREE, "US"),
                Arguments.of(httpClient, PhoneNumberType.GEOGRAPHIC, "US"),
                Arguments.of(httpClient, PhoneNumberType.MOBILE, "IE")));
    }

    @ParameterizedTest
    @MethodSource("httpClientAndPhoneNumberTypeProvider")
    public void updatePhoneNumbersReservationWithAAD(HttpClient httpClient, PhoneNumberType phoneNumberType,
        String countryCode) {
        runUpdatePhoneNumbersReservationWithAADTest(httpClient, phoneNumberType, countryCode);
    }

    private void runUpdatePhoneNumbersReservationWithAADTest(HttpClient httpClient, PhoneNumberType phoneNumberType,
        String countryCode) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions(countryCode, phoneNumberType)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION);

        PhoneNumbersBrowseResult result = this.getClientWithManagedIdentity(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)
            .block();

        assertNotNull(result);
        assertEquals(phoneNumberType, result.getPhoneNumbers().get(0).getPhoneNumberType());
        assertEquals(PhoneNumberAssignmentType.APPLICATION, result.getPhoneNumbers().get(0).getAssignmentType());

        List<AvailablePhoneNumber> numbersToAdd = new ArrayList<>();
        numbersToAdd.add(result.getPhoneNumbers().get(0));

        PhoneNumbersReservation reservationResponse
            = this.getClientWithManagedIdentity(httpClient, "updatePhoneNumberReservation")
                .createOrUpdateReservation(
                    new CreateOrUpdateReservationOptions(reservationId).setPhoneNumbersToAdd(numbersToAdd))
                .block();

        assertEquals(reservationId, reservationResponse.getId().toString());
        assertNotNull(reservationResponse.getPhoneNumbers());
        assertTrue(reservationResponse.getPhoneNumbers().containsKey(result.getPhoneNumbers().get(0).getId()));

        reservationResponse = this.getClientWithManagedIdentity(httpClient, "getPhoneNumberReservation")
            .getReservation(reservationId)
            .block();

        assertEquals(reservationId, reservationResponse.getId().toString());

        PagedFlux<PhoneNumbersReservation> reservationsList
            = this.getClientWithManagedIdentity(httpClient, "listPhoneNumberReservations").listReservations();

        StepVerifier.create(reservationsList.collectList()).assertNext(reservations -> {
            boolean containsReservation
                = reservations.stream().anyMatch(reservation -> reservation.getId().toString().equals(reservationId));
            assertTrue(containsReservation, "The reservations list does not contain the expected reservation.");
        }).verifyComplete();

        List<String> numbersToRemove = new ArrayList<>();
        numbersToRemove.add(result.getPhoneNumbers().get(0).getId());

        reservationResponse = this.getClientWithManagedIdentity(httpClient, "updatePhoneNumberReservation")
            .createOrUpdateReservation(
                new CreateOrUpdateReservationOptions(reservationId).setPhoneNumbersToRemove(numbersToRemove))
            .block();
        assertEquals(reservationId, reservationResponse.getId().toString());

        this.getClientWithManagedIdentity(httpClient, "deletePhoneNumberReservation")
            .deleteReservation(reservationId)
            .block();

        StepVerifier
            .create(this.getClientWithManagedIdentity(httpClient, "getPhoneNumberReservation")
                .getReservation(reservationId))
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("httpClientAndPhoneNumberTypeProvider")
    public void updatePhoneNumbersReservation(HttpClient httpClient, PhoneNumberType phoneNumberType,
        String countryCode) {
        runUpdatePhoneNumbersReservationTest(httpClient, phoneNumberType, countryCode);
    }

    private void runUpdatePhoneNumbersReservationTest(HttpClient httpClient, PhoneNumberType phoneNumberType,
        String countryCode) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions(countryCode, phoneNumberType)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION);

        PhoneNumbersBrowseResult result = this.getClientWithConnectionString(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)
            .block();

        assertNotNull(result);
        assertEquals(phoneNumberType, result.getPhoneNumbers().get(0).getPhoneNumberType());
        assertEquals(PhoneNumberAssignmentType.APPLICATION, result.getPhoneNumbers().get(0).getAssignmentType());

        List<AvailablePhoneNumber> numbersToAdd = new ArrayList<>();
        numbersToAdd.add(result.getPhoneNumbers().get(0));

        PhoneNumbersReservation reservationResponse
            = this.getClientWithConnectionString(httpClient, "updatePhoneNumberReservation")
                .createOrUpdateReservation(
                    new CreateOrUpdateReservationOptions(reservationId).setPhoneNumbersToAdd(numbersToAdd))
                .block();

        assertEquals(reservationId, reservationResponse.getId().toString());
        assertNotNull(reservationResponse.getPhoneNumbers());
        assertTrue(reservationResponse.getPhoneNumbers().containsKey(result.getPhoneNumbers().get(0).getId()));

        reservationResponse = this.getClientWithConnectionString(httpClient, "getPhoneNumberReservation")
            .getReservation(reservationId)
            .block();

        assertEquals(reservationId, reservationResponse.getId().toString());

        PagedFlux<PhoneNumbersReservation> reservationsList
            = this.getClientWithConnectionString(httpClient, "listPhoneNumberReservations").listReservations();

        StepVerifier.create(reservationsList.collectList()).assertNext(reservations -> {
            boolean containsReservation
                = reservations.stream().anyMatch(reservation -> reservation.getId().toString().equals(reservationId));
            assertTrue(containsReservation, "The reservations list does not contain the expected reservation.");
        }).verifyComplete();

        List<String> numbersToRemove = new ArrayList<>();
        numbersToRemove.add(result.getPhoneNumbers().get(0).getId());

        reservationResponse = this.getClientWithConnectionString(httpClient, "updatePhoneNumberReservation")
            .createOrUpdateReservation(
                new CreateOrUpdateReservationOptions(reservationId).setPhoneNumbersToRemove(numbersToRemove))
            .block();
        assertEquals(reservationId, reservationResponse.getId().toString());

        this.getClientWithConnectionString(httpClient, "deletePhoneNumberReservation")
            .deleteReservation(reservationId)
            .block();

        StepVerifier
            .create(this.getClientWithConnectionString(httpClient, "getPhoneNumberReservation")
                .getReservation(reservationId))
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void purchaseWithoutAgreementToNotResellFails(HttpClient httpClient) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions("FR", PhoneNumberType.TOLL_FREE)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION);

        PhoneNumbersBrowseResult result = this.getClientWithConnectionString(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)
            .block(); // Blocking to retrieve the result synchronously

        List<AvailablePhoneNumber> numbersToAdd = new ArrayList<>();
        numbersToAdd.add(result.getPhoneNumbers().get(0));
        PhoneNumbersReservation reservationResponse
            = this.getClientWithManagedIdentity(httpClient, "updatePhoneNumberReservation")
                .createOrUpdateReservation(
                    new CreateOrUpdateReservationOptions(reservationId).setPhoneNumbersToAdd(numbersToAdd))
                .block();
        StepVerifier.create(this.getClientWithConnectionString(httpClient, "purchasePhoneNumberReservation")
            .beginPurchaseReservation(reservationResponse.getId().toString())).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void purchaseWithoutAgreementToNotResellFailsWithAAD(HttpClient httpClient) {
        BrowsePhoneNumbersOptions browseRequest = new BrowsePhoneNumbersOptions("FR", PhoneNumberType.TOLL_FREE)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION);

        PhoneNumbersBrowseResult result = this.getClientWithManagedIdentity(httpClient, "browseAvailableNumbers")
            .browseAvailableNumbers(browseRequest)
            .block(); // Blocking to retrieve the result synchronously

        List<AvailablePhoneNumber> numbersToAdd = new ArrayList<>();
        numbersToAdd.add(result.getPhoneNumbers().get(0));
        PhoneNumbersReservation reservationResponse
            = this.getClientWithManagedIdentity(httpClient, "updatePhoneNumberReservation")
                .createOrUpdateReservation(
                    new CreateOrUpdateReservationOptions(reservationId).setPhoneNumbersToAdd(numbersToAdd))
                .block();

        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "purchasePhoneNumberReservation")
            .beginPurchaseReservation(reservationResponse.getId().toString())).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void purchaseWithSearchWithoutAgreementToNotResellFails(HttpClient httpClient) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityType.NONE);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setQuantity(1);
        PhoneNumbersAsyncClient client
            = this.getClientWithConnectionString(httpClient, "beginSearchAvailablePhoneNumbers");
        PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> poller
            = setPollInterval(client.beginSearchAvailablePhoneNumbers("FR", PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions));
        PhoneNumberSearchResult searchResult = poller.blockLast().getFinalResult().block();

        StepVerifier.create(this.getClientWithConnectionString(httpClient, "beginPurchasePhoneNumbers")
            .beginPurchasePhoneNumbers(searchResult.getSearchId())).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void purchaseWithSearchWithoutAgreementToNotResellWithAADFails(HttpClient httpClient) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityType.NONE);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setQuantity(1);
        PhoneNumbersAsyncClient client
            = this.getClientWithManagedIdentity(httpClient, "beginSearchAvailablePhoneNumbers");
        PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> poller
            = setPollInterval(client.beginSearchAvailablePhoneNumbers("FR", PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions));
        PhoneNumberSearchResult searchResult = poller.blockLast().getFinalResult().block();

        StepVerifier.create(this.getClientWithManagedIdentity(httpClient, "beginPurchasePhoneNumbers")
            .beginPurchasePhoneNumbers(searchResult.getSearchId())).verifyError();
    }

    private PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult>
        beginSearchAvailablePhoneNumbersHelper(PhoneNumbersAsyncClient client, boolean withOptions) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setQuantity(1);

        if (withOptions) {
            return setPollInterval(client.beginSearchAvailablePhoneNumbers(COUNTRY_CODE, PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions));
        }
        return setPollInterval(client.beginSearchAvailablePhoneNumbers(COUNTRY_CODE, PhoneNumberType.TOLL_FREE,
            PhoneNumberAssignmentType.APPLICATION, capabilities));
    }

    private PollerFlux<PhoneNumberOperation, PurchasePhoneNumbersResult>
        beginPurchasePhoneNumbersHelper(PhoneNumbersAsyncClient client, String searchId) {
        return setPollInterval(client.beginPurchasePhoneNumbers(searchId));
    }

    private PollerFlux<PhoneNumberOperation, ReleasePhoneNumberResult>
        beginReleasePhoneNumberHelper(PhoneNumbersAsyncClient client, String phoneNumber) {
        return setPollInterval(client.beginReleasePhoneNumber(phoneNumber));
    }

    private PollerFlux<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilitiesHelper(
        PhoneNumbersAsyncClient client, String phoneNumber, PhoneNumberCapabilities capabilities) {

        return setPollInterval(client.beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities));
    }

    private <T, U> PollerFlux<T, U> setPollInterval(PollerFlux<T, U> pollerFlux) {
        return interceptorManager.isPlaybackMode()
            ? pollerFlux.setPollInterval(Duration.ofMillis(1))
            : pollerFlux.setPollInterval(Duration.ofSeconds(1));
    }

    private PhoneNumbersAsyncClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    private PhoneNumbersAsyncClient getClientWithManagedIdentity(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderUsingManagedIdentity(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
