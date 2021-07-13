package com.azure.maps.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.ResponseFormat;
import com.azure.maps.service.models.TimezoneOptions;
import com.fasterxml.jackson.core.JsonProcessingException;

public class TimezoneSample {
    public static void main(String[] args) throws JsonProcessingException {
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        System.out.println("Get Timezone By Coordinate");
        MapsCommon.print(client.getTimezones().getTimezoneByCoordinates(ResponseFormat.JSON, "47.0,-122", null,
                TimezoneOptions.ALL, null, null, null));

        System.out.println("Get Timezone By Id");
        MapsCommon.print(client.getTimezones().getTimezoneByID(ResponseFormat.JSON, "Asia/Bahrain", null,
                TimezoneOptions.ALL, null, null, null));

        System.out.println("Get Timezone Enum IANA");
        MapsCommon.print(client.getTimezones().getTimezoneEnumIana(ResponseFormat.JSON));

        System.out.println("Get Timezone Enum Windows");
        MapsCommon.print(client.getTimezones().getTimezoneEnumWindows(ResponseFormat.JSON));

        System.out.println("Get Timezone IANA Version");
        MapsCommon.print(client.getTimezones().getTimezoneIanaVersion(ResponseFormat.JSON));

        System.out.println("Get Timezone Windows to IANA");
        MapsCommon.print(client.getTimezones().getTimezoneWindowsToIana(ResponseFormat.JSON, "pacific standard time"));
    }
}
