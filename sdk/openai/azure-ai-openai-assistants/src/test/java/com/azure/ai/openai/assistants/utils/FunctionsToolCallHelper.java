package com.azure.ai.openai.assistants.utils;

import com.azure.ai.openai.assistants.models.FunctionDefinition;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionsToolCallHelper {

    public FunctionDefinition getFavoriteVacationDestinationDefinition() {
        return new FunctionDefinition("getFavoriteVacationDestination", null)
            .setDescription("Retrieves the user's unambiguously preferred location for vacations.");
    }

    public FunctionDefinition getPreferredAirlineForSeasonDefinition() {
        return new FunctionDefinition(
                "getPreferredAirlineForSeason",
                BinaryData.fromObject(new AirlineForSeasonParameters()))
            .setDescription("Given a season like winter or spring, retrieves the user's preferred airline carrier.");
    }

    public FunctionDefinition getAirlinePriceToDestinationForSeasonDefinition() {
        return new FunctionDefinition(
                "getAirlinePriceToDestinationForSeason",
                BinaryData.fromObject(new AirlinePriceFunctionParameters()))
            .setDescription("Given a desired location, airline, and approximate time of year, retrieves estimated prices.");
    }

    private class AirlineForSeasonParameters {
        @JsonProperty(value = "type")
        private String type = "object";

        @JsonProperty(value = "properties")
        private Map<String, FunctionEnumParameter> properties;

        @JsonCreator
        public AirlineForSeasonParameters() {
            this.properties = new HashMap<>();

            this.properties.put("season", new FunctionEnumParameter(List.of("winter", "spring", "summer", "fall")));
        }
    }

    private class AirlinePriceFunctionParameters {

        @JsonProperty(value = "type")
        private String type = "object";

        @JsonProperty(value = "properties")
        private Map<String, FunctionStringParameter> properties;

        @JsonCreator
        public AirlinePriceFunctionParameters() {
            this.properties = new HashMap<>();

            this.properties.put("destination", new FunctionStringParameter("A travel destination location."));
            this.properties.put("airline", new FunctionStringParameter("The name of an airline that flights can be booked on."));
            this.properties.put("time", new FunctionStringParameter("An approximate time of year at which travel is planned."));
        }
    }
}
