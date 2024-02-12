// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.MatchConditions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConditionalRequestForSettingsPaginationAsync {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting by conditional
     * request asynchronously
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) throws InterruptedException {
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ConfigurationAsyncClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();

        List<MatchConditions> matchConditionsList = new ArrayList<>();

        // list all settings and get their etags
        client.listConfigurationSettings(null)
                .byPage()
                .subscribe(pagedResponse -> {
                    matchConditionsList.add(new MatchConditions().setIfNoneMatch(pagedResponse.getHeaders().getValue("ETag")));
                });

        PagedFlux<ConfigurationSetting> settings = client.listConfigurationSettings(
                new SettingSelector().setMatchConditions(matchConditionsList));

        settings.byPage().subscribe(pagedResponse -> {
            int statusCode = pagedResponse.getStatusCode();
            System.out.println("Status code = " + statusCode);
            if (statusCode == 304) {
                System.out.println("Settings have not changed. ");
                String continuationToken = pagedResponse.getContinuationToken();
                String etag = pagedResponse.getHeaders().getValue("ETag");
                System.out.println("Continuation Token: " + continuationToken);
                System.out.println("ETag: " + etag);
                return;
            }

            System.out.println("Settings:");
            pagedResponse.getElements().forEach(setting -> {
                System.out.println("Key: " + setting.getKey() + ", Value: " + setting.getValue());
            });
        });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
