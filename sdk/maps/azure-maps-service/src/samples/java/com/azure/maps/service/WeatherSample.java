package com.azure.maps.service;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.ResponseFormat;

public class WeatherSample {
    public static void main(String[] args) throws IOException {
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        System.out.println("Get Current Conditions");
        MapsCommon.print(client.getWeathers().getCurrentConditions(ResponseFormat.JSON, "47.641268,-122.125679"));

        System.out.println("Get Daily Forecast");
        MapsCommon.print(
                client.getWeathers().getDailyForecast(ResponseFormat.JSON, "62.6490341,30.0734812", null, 5, null));

        System.out.println("Get Daily Indices");
        MapsCommon.print(
                client.getWeathers().getDailyIndices(ResponseFormat.JSON, "43.84745,-79.37849", null, null, null, 11));

        System.out.println("Get Hourly Forecast");
        MapsCommon.print(
                client.getWeathers().getHourlyForecast(ResponseFormat.JSON, "47.632346,-122.138874", null, 12, null));

        System.out.println("Get Minute Forecast");
        MapsCommon
                .print(client.getWeathers().getMinuteForecast(ResponseFormat.JSON, "47.632346,-122.138874", 15, null));

        System.out.println("Get Quarter Day Forecast");
        MapsCommon.print(client.getWeathers().getQuarterDayForecast(ResponseFormat.JSON, "47.632346,-122.138874", null,
                1, null));

        System.out.println("Get Severe Weather Alerts");
        MapsCommon.print(client.getWeathers().getSevereWeatherAlerts(ResponseFormat.JSON, "48.057,-81.091"));

        System.out.println("Get Weather Along Route");
        MapsCommon.print(client.getWeathers().getWeatherAlongRoute(ResponseFormat.JSON,
                "38.907,-77.037,0:38.907,-77.009,10:38.926,-76.928,20:39.033,-76.852,30:39.168,-76.732,40:39.269,-76.634,50:39.287,-76.612,60"));

    }
}
