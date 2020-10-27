package com.azure.communication.sms;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.communication.administration.PhoneNumberClient;
import com.azure.communication.administration.PhoneNumberClientBuilder;
import com.azure.communication.administration.models.CreateSearchOptions;
import com.azure.communication.administration.models.PhoneNumberSearch;
import com.azure.communication.administration.models.PhoneNumberType;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.administration.models.SearchStatus;
import com.azure.communication.common.PhoneNumber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PhoneNumberLiveTestSetup implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static boolean started = false;
    private static String LOCALE = "en-us";
    private static String COUNTRY_CODE = "US";
    private static String NAME_FOR_SEARCH = "TestSearch";
    private static String DESCRIPTION_FOR_SEARCH = "Setup Phone Number Search for Live Tests";
    private PhoneNumberClient phoneNumberClient;
    private List<String> phoneNumbers;

    @Override
    public void beforeAll(ExtensionContext context) {
        System.out.println("Test Mode is: " + Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE"));
        if (!started && TestMode.LIVE.name().equals(Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE"))) {
            System.out.println("Begin Running Phone Number Setup for Live Tests");
            started = true;

            // Set up for creating phone number search
            phoneNumberClient = createPhoneNumberClient();
            PagedIterable<PhonePlanGroup> phonePlanGroups = phoneNumberClient
                .listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
            
            String phonePlanGroupId = "";
            for(PhonePlanGroup phonePlanGroup: phonePlanGroups) {
                if (phonePlanGroup.getPhoneNumberType() == PhoneNumberType.TOLL_FREE) {
                    phonePlanGroupId = phonePlanGroup.getPhonePlanGroupId();
                }
            }

            PagedIterable<PhonePlan> phonePlans = phoneNumberClient
                .listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
            
            PhonePlan phonePlan = phonePlans.iterator().next();
            String phonePlanId = phonePlan.getPhonePlanId();
            String areaCode = phonePlan.getAreaCodes().get(0);

            // Create phone number search
            List<String> phonePlanIds = new ArrayList<>();
            phonePlanIds.add(phonePlanId);

            CreateSearchOptions createSearchOptions = new CreateSearchOptions();
            createSearchOptions
                .setAreaCode(areaCode)
                .setDescription(DESCRIPTION_FOR_SEARCH)
                .setDisplayName(NAME_FOR_SEARCH)
                .setPhonePlanIds(phonePlanIds)
                .setQuantity(1);

            Duration duration = Duration.ofSeconds(10);
            SyncPoller<PhoneNumberSearch, PhoneNumberSearch> res = phoneNumberClient.beginCreateSearch(createSearchOptions, duration);
            res.waitForCompletion();
            PhoneNumberSearch searchResult = res.getFinalResult();
            phoneNumbers = searchResult.getPhoneNumbers();
            if (phoneNumbers.size() > 0) {
                Configuration.getGlobalConfiguration().put("SMS_SERVICE_PHONE_NUMBER", phoneNumbers.get(0));
            }

            // Purchase phone number
            SyncPoller<Void, Void> result = phoneNumberClient.beginPurchaseSearch(searchResult.getSearchId(), duration);
            result.waitForCompletion();

            System.out.println("The connection string is: " +  Configuration.getGlobalConfiguration().get("COMMUNICATION_CONNECTION_STRING"));
            System.out.println("The Phone Number Search id is: " + searchResult.getSearchId());
            System.out.println("The Phone Number Search error code is: " + searchResult.getErrorCode());
            System.out.println("The Phone Number Search status is: " + phoneNumberClient.getSearchById(searchResult.getSearchId()).getStatus());

            assertEquals(SearchStatus.SUCCESS, phoneNumberClient.getSearchById(searchResult.getSearchId()).getStatus());
            System.out.println("Finished Running Phone Number Setup for Live Tests");
            System.out.println("Using phone number: " + phoneNumbers.get(0));
        }
    }

    @Override
    public void close() {

        // Release phone numbers after tests end
        // TODO: Blocked on Release Phone Numbers LRO
        final List<PhoneNumber> releasedPhoneNumbers = new ArrayList<>();
        for(String phoneNumber : phoneNumbers) {
            releasedPhoneNumbers.add(new PhoneNumber(phoneNumber));
        }
        phoneNumberClient.releasePhoneNumbers(releasedPhoneNumbers);
        System.out.println("Release and clean up phone numbers for Live Tests");

    }

    private PhoneNumberClient createPhoneNumberClient() {
        final String ACCESSKEYRAW = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        final String ACCESSKEYENCODED = Base64.getEncoder().encodeToString(ACCESSKEYRAW.getBytes());
        final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
            .get("COMMUNICATION_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=" + ACCESSKEYENCODED);

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        return new PhoneNumberClientBuilder()
            .connectionString(CONNECTION_STRING)
            .httpClient(httpClient)
            .buildClient();
        }
}