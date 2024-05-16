// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.models.FunctionDefinition;
import com.azure.ai.openai.assistants.models.FunctionToolDefinition;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
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

    private static class AirlineForSeasonParameters implements JsonSerializable<AirlineForSeasonParameters> {

        private final String type = "object";

        private final Map<String, FunctionEnumParameter> properties;

        AirlineForSeasonParameters() {
            this.properties = new HashMap<>();

            this.properties.put("season", new FunctionEnumParameter(Arrays.asList("winter", "spring", "summer", "fall")));
        }


        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeFieldName("properties");
            jsonWriter.writeStartObject();
            for (Map.Entry<String, FunctionEnumParameter> entry : this.properties.entrySet()) {
                jsonWriter.writeFieldName(entry.getKey());
                entry.getValue().toJson(jsonWriter);
            }
            jsonWriter.writeEndObject();
            return jsonWriter.writeEndObject();
        }

        public static AirlineForSeasonParameters fromJson(JsonReader jsonReader) {
            // Deserialization logic not necessary for stub class
            return new AirlineForSeasonParameters();
        }
    }

    private static class AirlinePriceFunctionParameters implements JsonSerializable<AirlinePriceFunctionParameters> {

        private final String type = "object";
        private final Map<String, FunctionStringParameter> properties;

        AirlinePriceFunctionParameters() {
            this.properties = new HashMap<>();

            this.properties.put("destination", new FunctionStringParameter("A travel destination location."));
            this.properties.put("airline", new FunctionStringParameter("The name of an airline that flights can be booked on."));
            this.properties.put("time", new FunctionStringParameter("An approximate time of year at which travel is planned."));
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeFieldName("properties");
            jsonWriter.writeStartObject();
            for (Map.Entry<String, FunctionStringParameter> entry : this.properties.entrySet()) {
                jsonWriter.writeFieldName(entry.getKey());
                entry.getValue().toJson(jsonWriter);
            }
            jsonWriter.writeEndObject();
            return jsonWriter.writeEndObject();
        }

        public static AirlinePriceFunctionParameters fromJson(JsonReader jsonReader) {
            // Deserialization logic not necessary for stub class
            return new AirlinePriceFunctionParameters();
        }
    }

    private static class FavoriteVacationDestinationParameters implements JsonSerializable<FavoriteVacationDestinationParameters> {

        private final String type = "object";

        private final Map<String, FunctionStringParameter> properties = new HashMap<>();

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeFieldName("properties");
            jsonWriter.writeStartObject();
            for (Map.Entry<String, FunctionStringParameter> entry : this.properties.entrySet()) {
                jsonWriter.writeFieldName(entry.getKey());
                entry.getValue().toJson(jsonWriter);
            }
            jsonWriter.writeEndObject();
            return jsonWriter.writeEndObject();
        }

        public static FavoriteVacationDestinationParameters fromJson(JsonReader jsonReader) {
            // Deserialization logic not necessary for stub class
            return new FavoriteVacationDestinationParameters();
        }
    }
}
