// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.models.FunctionDefinition;
import com.azure.ai.openai.assistants.models.FunctionToolDefinition;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FunctionsToolCallHelper {

    public FunctionToolDefinition getFavoriteVacationDestinationDefinition() {
        return new FunctionToolDefinition(new FunctionDefinition(
            "getFavoriteVacationDestination",
            BinaryData.fromObject(new FavoriteVacationDestinationParameters()))
            .setDescription("Retrieves the user's unambiguously preferred location for vacations."));
    }

    public FunctionToolDefinition getPreferredAirlineForSeasonDefinition() {
        return new FunctionToolDefinition(new FunctionDefinition(
                "getPreferredAirlineForSeason",
                BinaryData.fromObject(new AirlineForSeasonParameters()))
            .setDescription("Given a season like winter or spring, retrieves the user's preferred airline carrier."));
    }

    public FunctionToolDefinition getAirlinePriceToDestinationForSeasonDefinition() {
        return new FunctionToolDefinition(new FunctionDefinition(
                "getAirlinePriceToDestinationForSeason",
                BinaryData.fromObject(new AirlinePriceFunctionParameters()))
            .setDescription("Given a desired location, airline, and approximate time of year, retrieves estimated prices."));
    }

    private class AirlineForSeasonParameters {
        @JsonProperty(value = "type")
        private String type = "object";

        @JsonProperty(value = "properties")
        private Map<String, FunctionEnumParameter> properties;

        @JsonCreator
        AirlineForSeasonParameters() {
            this.properties = new HashMap<>();

            this.properties.put("season", new FunctionEnumParameter(Arrays.asList("winter", "spring", "summer", "fall")));
        }
    }

    private class AirlinePriceFunctionParameters {

        @JsonProperty(value = "type")
        private String type = "object";

        @JsonProperty(value = "properties")
        private Map<String, FunctionStringParameter> properties;

        @JsonCreator
        AirlinePriceFunctionParameters() {
            this.properties = new HashMap<>();

            this.properties.put("destination", new FunctionStringParameter("A travel destination location."));
            this.properties.put("airline", new FunctionStringParameter("The name of an airline that flights can be booked on."));
            this.properties.put("time", new FunctionStringParameter("An approximate time of year at which travel is planned."));
        }
    }

    private class FavoriteVacationDestinationParameters {

        @JsonProperty(value = "type")
        private String type = "object";

        @JsonProperty(value = "properties")
        private Map<String, FunctionStringParameter> properties = new HashMap<>();

    }
}
