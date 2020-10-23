package com.azure.communication.sms;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.communication.administration.PhoneNumberClient;
import com.azure.communication.administration.PhoneNumberClientBuilder;
import com.azure.communication.administration.models.CreateSearchOptions;
import com.azure.communication.administration.models.PhoneNumberSearch;
import com.azure.communication.administration.models.PhoneNumberType;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PhoneNumberLiveTestSetup implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static boolean started = false;
    private static String LOCALE = "en-us";
    private static String COUNTRY_CODE = "en-us";
    private static String NAME_FOR_SEARCH = "SetupSearch";
    private static String DESCRIPTION_FOR_SEARCH = "Setup Phone Number Search for Live Tests";

    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started) {
            started = true;
            PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

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

            // // Create Search
            List<String> phonePlanIds = new ArrayList<>();
            phonePlanIds.add(phonePlanId);

            CreateSearchOptions createSearchOptions = new CreateSearchOptions();
            createSearchOptions
                .setAreaCode(areaCode)
                .setDescription(DESCRIPTION_FOR_SEARCH)
                .setDisplayName(NAME_FOR_SEARCH)
                .setPhonePlanIds(phonePlanIds)
                .setQuantity(2);

            Duration duration = Duration.ofSeconds(10);
            SyncPoller<PhoneNumberSearch, PhoneNumberSearch> res = phoneNumberClient.beginCreateSearch(createSearchOptions, duration);
            res.waitForCompletion();
            PhoneNumberSearch searchResult = res.getFinalResult();

            String phoneNumber = searchResult.getPhoneNumbers().get(0);

            Configuration.getGlobalConfiguration().put("SMS_SERVICE_PHONE_NUMBER", phoneNumber);

            phoneNumberClient.purchaseSearch(searchResult.getSearchId());
  
            // The following line registers a callback hook when the root test context is shut down
            // context.getRoot().getStore(GLOBAL).put("any unique name", this);
        }
    }

    @Override
    public void close() {
        // Your "after all tests" logic goes here
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