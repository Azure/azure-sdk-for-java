// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.samples;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.weather.WeatherAsyncClient;
import com.azure.maps.weather.WeatherClient;
import com.azure.maps.weather.WeatherClientBuilder;

public class WeatherSample {
    public WeatherClient createMapsSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.weather.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a client
        WeatherClient client = new WeatherClientBuilder() 
            .credential(keyCredential)
            .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();
        // END: com.azure.maps.weather.sync.builder.key.instantiation

        return client;
    }

    public WeatherClient createMapsSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.weather.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        WeatherClient client = new WeatherClientBuilder()
            .credential(tokenCredential)
            .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildClient();
        // END: com.azure.maps.weather.sync.builder.ad.instantiation

        return client;
    }

    public WeatherAsyncClient createMapsWeatherAsyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.weather.async.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a client
        WeatherAsyncClient asyncClient = new WeatherClientBuilder()
            .credential(keyCredential)
            .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // END: com.azure.maps.weather.async.builder.key.instantiation

        return asyncClient;
    }

    public WeatherAsyncClient createMapsWeatherAsyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.weather.async.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a client
        WeatherAsyncClient asyncClient = new WeatherClientBuilder()
            .credential(tokenCredential)
            .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
            .buildAsyncClient();

        // END: com.azure.maps.weather.async.builder.ad.instantiation

        return asyncClient;
    }
}
