// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.jsonschema;

/**
 * Test person.
 */
public class Address {
    public static final String JSON_SCHEMA = "{\n" + "  \"type\": \"object\",\n" + "  \"properties\": {\n"
        + "    \"number\": { \"type\": \"number\" },\n" + "     \"streetName\": { \"type\": \"string\" },\n"
        + "    \"streetType\": { \"enum\": [\"Street\", \"Avenue\", \"Boulevard\"] }\n" + "  }\n" + "}";

    private String streetName;
    private String streetType;
    private int number;

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getStreetType() {
        return streetType;
    }

    public void setStreetType(String streetType) {
        this.streetType = streetType;
    }
}
