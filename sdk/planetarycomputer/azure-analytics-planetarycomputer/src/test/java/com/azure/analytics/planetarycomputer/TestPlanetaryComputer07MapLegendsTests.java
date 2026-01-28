// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Map Legend operations (Test07).
 */
@Tag("Tiler")
@Tag("Legends")
public class TestPlanetaryComputer07MapLegendsTests extends PlanetaryComputerTestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Tag("ClassMapLegend")
    public void test07_01_GetClassMapLegend() throws Exception {
        DataClient dataClient = getDataClient();
        String classmapName = "mtbs-severity";

        System.out.println("Input - classmap_name: " + classmapName);

        BinaryData response = dataClient.getClassMapLegendWithResponse(classmapName, null).getValue();
        JsonNode root = objectMapper.readTree(response.toBytes());

        System.out.println("Response type: " + root.getNodeType());

        assertTrue(root.isObject(), "Response should be a JSON object");

        int classCount = root.size();
        assertTrue(classCount > 0, "Response should not be empty");
        System.out.println("Number of classes: " + classCount);

        String[] expectedClasses = { "0", "1", "2", "3", "4", "5", "6" };
        for (String expectedClass : expectedClasses) {
            assertTrue(root.has(expectedClass), "Class '" + expectedClass + "' should be in response");
        }

        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String classValue = fieldNames.next();
            JsonNode colorElement = root.get(classValue);

            assertTrue(colorElement.isArray(), "Color for class '" + classValue + "' should be an array");
            assertEquals(4, colorElement.size(), "Color for class '" + classValue + "' should have 4 RGBA values");

            for (int i = 0; i < 4; i++) {
                JsonNode component = colorElement.get(i);
                assertTrue(component.isNumber(), "Component should be a number");
                int value = component.asInt();
                assertTrue(value >= 0 && value <= 255, "Component should be 0-255");
            }

            if ("0".equals(classValue) || "4".equals(classValue)) {
                System.out.println(
                    String.format("Class %s color: RGBA(%d, %d, %d, %d)", classValue, colorElement.get(0).asInt(),
                        colorElement.get(1).asInt(), colorElement.get(2).asInt(), colorElement.get(3).asInt()));
            }
        }

        System.out.println("Class map legend validated successfully");
    }

    @Test
    @Tag("IntervalLegend")
    public void test07_02_GetIntervalLegend() throws Exception {
        DataClient dataClient = getDataClient();
        String colormapName = "modis-64A1";

        System.out.println("Input - colormap_name: " + colormapName);

        BinaryData response = dataClient.getIntervalLegendWithResponse(colormapName, null).getValue();
        JsonNode root = objectMapper.readTree(response.toBytes());

        assertTrue(root.isArray(), "Response should be a JSON array");
        int itemCount = root.size();
        assertTrue(itemCount > 0, "Response should not be empty");

        System.out.println("Number of intervals: " + itemCount);

        for (JsonNode item : root) {
            assertTrue(item.isArray(), "Each item should be an array");
            assertTrue(item.size() >= 2, "Each item should have at least 2 elements [value, color]");
        }

        System.out.println("Interval legend validated successfully");
    }
}
