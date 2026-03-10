// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for FileSearchTool serialization, focusing on the filters union type handling.
 * The filters property is a union type: ComparisonFilter | CompoundFilter.
 */
public class FileSearchToolSerializationTests {

    /**
     * Tests serialization when filters is not set (null).
     */
    @Test
    public void testSerializationWithoutFilters() throws IOException {
        FileSearchTool tool = new FileSearchTool(Arrays.asList("vs-1"));

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"vector_store_ids\""));
        assertFalse(json.contains("\"filters\""));
    }

    /**
     * Tests serialization with filters set to a ComparisonFilter.
     */
    @Test
    public void testSerializationWithComparisonFilter() throws IOException {
        ComparisonFilter compFilter = new ComparisonFilter(ComparisonFilterType.EQ, "status", "active");
        FileSearchTool tool = new FileSearchTool(Arrays.asList("vs-1")).setFilters(compFilter);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"filters\""));
        assertTrue(json.contains("\"type\":\"eq\""));
        assertTrue(json.contains("\"key\":\"status\""));
        assertTrue(json.contains("active"));
    }

    /**
     * Tests serialization with filters set to a CompoundFilter.
     */
    @Test
    public void testSerializationWithCompoundFilter() throws IOException {
        ComparisonFilter filter1 = new ComparisonFilter(ComparisonFilterType.EQ, "status", "active");
        ComparisonFilter filter2 = new ComparisonFilter(ComparisonFilterType.GT, "score", 50.0);

        CompoundFilter compoundFilter = new CompoundFilter(CompoundFilterType.AND,
            Arrays.asList(BinaryData.fromObject(filter1), BinaryData.fromObject(filter2)));

        FileSearchTool tool = new FileSearchTool(Arrays.asList("vs-1")).setFilters(compoundFilter);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"filters\""));
        assertTrue(json.contains("\"type\":\"and\""));
    }

    /**
     * Tests deserialization with filters set to a ComparisonFilter.
     */
    @Test
    public void testDeserializationWithComparisonFilter() throws IOException {
        String json
            = "{\"vector_store_ids\":[\"vs-1\"],\"type\":\"file_search\",\"filters\":{\"type\":\"eq\",\"key\":\"status\",\"value\":\"active\"}}";

        FileSearchTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        ComparisonFilter filter = tool.getFiltersAsComparisonFilter();
        assertNotNull(filter);
        assertEquals(ComparisonFilterType.EQ, filter.getType());
        assertEquals("status", filter.getKey());
    }

    /**
     * Tests deserialization with filters set to a CompoundFilter.
     */
    @Test
    public void testDeserializationWithCompoundFilter() throws IOException {
        String json
            = "{\"vector_store_ids\":[\"vs-1\"],\"type\":\"file_search\",\"filters\":{\"type\":\"and\",\"filters\":[{\"type\":\"eq\",\"key\":\"status\",\"value\":\"active\"}]}}";

        FileSearchTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        CompoundFilter filter = tool.getFiltersAsCompoundFilter();
        assertNotNull(filter);
        assertEquals(CompoundFilterType.AND, filter.getType());
        assertNotNull(filter.getFilters());
        assertEquals(1, filter.getFilters().size());
    }

    /**
     * Tests deserialization with filters absent.
     */
    @Test
    public void testDeserializationWithoutFilters() throws IOException {
        String json = "{\"vector_store_ids\":[\"vs-1\"],\"type\":\"file_search\"}";

        FileSearchTool tool = deserializeFromJson(json);

        assertNotNull(tool);
        assertNull(tool.getFiltersAsComparisonFilter());
        assertNull(tool.getFiltersAsCompoundFilter());
    }

    /**
     * Tests round-trip serialization/deserialization with a ComparisonFilter.
     */
    @Test
    public void testRoundTripWithComparisonFilter() throws IOException {
        ComparisonFilter compFilter = new ComparisonFilter(ComparisonFilterType.NE, "category", "archived");
        FileSearchTool original = new FileSearchTool(Arrays.asList("vs-1", "vs-2")).setFilters(compFilter);

        String json = serializeToJson(original);
        FileSearchTool deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals(2, deserialized.getVectorStoreIds().size());
        ComparisonFilter deserializedFilter = deserialized.getFiltersAsComparisonFilter();
        assertNotNull(deserializedFilter);
        assertEquals(ComparisonFilterType.NE, deserializedFilter.getType());
        assertEquals("category", deserializedFilter.getKey());
    }

    /**
     * Tests serialization with all FileSearchTool fields populated.
     */
    @Test
    public void testFullSerializationWithAllFields() throws IOException {
        ComparisonFilter compFilter = new ComparisonFilter(ComparisonFilterType.EQ, "type", "document");
        FileSearchTool tool = new FileSearchTool(Arrays.asList("vs-1")).setMaxResults(10L).setFilters(compFilter);

        String json = serializeToJson(tool);

        assertNotNull(json);
        assertTrue(json.contains("\"vector_store_ids\""));
        assertTrue(json.contains("\"max_num_results\":10"));
        assertTrue(json.contains("\"filters\""));
    }

    // Helper method to serialize to JSON string
    private String serializeToJson(FileSearchTool tool) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            tool.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    // Helper method to deserialize from JSON string
    private FileSearchTool deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return FileSearchTool.fromJson(jsonReader);
        }
    }
}
