package com.azure.iot.deviceupdate;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;


public class GetUpdateNotFoundSample {
    public static void main(String[] args) {
        UpdatesAsyncClient client = new DeviceUpdateClientBuilder()
            .endpoint(TestData.ACCOUNT_ENDPOINT)
            .instanceId(TestData.INSTANCE_ID)
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildUpdatesAsyncClient();

        // BEGIN: com.azure.iot.deviceupdate.UpdatesAsyncClient.notfound
        try {
            client.getUpdateWithResponse("foo", "bar", "0.0.0.1", null).block();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                // update does not exist
            }
        }
        // END: com.azure.iot.deviceupdate.UpdatesAsyncClient.notfound

    }
}
