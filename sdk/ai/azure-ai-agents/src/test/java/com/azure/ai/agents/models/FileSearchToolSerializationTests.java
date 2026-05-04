// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.openai.models.ComparisonFilter;
import com.openai.models.CompoundFilter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class FileSearchToolSerializationTests {

    // -----------------------------------------------------------------------
    // Basic serialization
    // -----------------------------------------------------------------------

    @Test
    public void testMinimalSerialization() throws IOException {
        FileSearchTool tool = new FileSearchTool(Collections.singletonList("vs_001"));

        String json = serialize(tool);

        assertTrue(json.contains("\"type\":\"file_search\""));
        assertTrue(json.contains("\"vector_store_ids\":[\"vs_001\"]"));
    }

    @Test
    public void testSerializationWithAllFields() throws IOException {
        FileSearchTool tool = new FileSearchTool(Arrays.asList("vs_a", "vs_b")).setMaxResults(25L)
            .setRankingOptions(new RankingOptions().setScoreThreshold(0.75)
                .setRanker(RankerVersionType.AUTO)
                .setHybridSearch(new HybridSearchOptions(0.6, 0.4)));

        String json = serialize(tool);

        assertTrue(json.contains("\"vector_store_ids\":[\"vs_a\",\"vs_b\"]"));
        assertTrue(json.contains("\"max_num_results\":25"));
        assertTrue(json.contains("\"score_threshold\":0.75"));
        assertTrue(json.contains("\"ranker\":\"auto\""));
        assertTrue(json.contains("\"embedding_weight\":0.6"));
        assertTrue(json.contains("\"text_weight\":0.4"));
    }

    // -----------------------------------------------------------------------
    // Deserialization
    // -----------------------------------------------------------------------

    @Test
    public void testMinimalDeserialization() throws IOException {
        String json = "{\"type\":\"file_search\",\"vector_store_ids\":[\"vs_xyz\"]}";

        FileSearchTool tool = deserialize(json);

        assertEquals(ToolType.FILE_SEARCH, tool.getType());
        assertEquals(Collections.singletonList("vs_xyz"), tool.getVectorStoreIds());
        assertNull(tool.getMaxResults());
        assertNull(tool.getRankingOptions());
        assertNull(tool.getFilters());
    }

    @Test
    public void testDeserializationWithAllFields() throws IOException {
        String json = "{\"type\":\"file_search\"," + "\"vector_store_ids\":[\"vs_1\",\"vs_2\"],"
            + "\"max_num_results\":10," + "\"ranking_options\":{\"ranker\":\"auto\",\"score_threshold\":0.5,"
            + "\"hybrid_search\":{\"embedding_weight\":0.7,\"text_weight\":0.3}},"
            + "\"filters\":{\"type\":\"eq\",\"key\":\"author\",\"value\":\"alice\"}}";

        FileSearchTool tool = deserialize(json);

        assertEquals(Arrays.asList("vs_1", "vs_2"), tool.getVectorStoreIds());
        assertEquals(10L, tool.getMaxResults());
        assertNotNull(tool.getRankingOptions());
        assertEquals(0.5, tool.getRankingOptions().getScoreThreshold());
        assertEquals(RankerVersionType.AUTO, tool.getRankingOptions().getRanker());
        assertNotNull(tool.getRankingOptions().getHybridSearch());
        assertEquals(0.7, tool.getRankingOptions().getHybridSearch().getEmbeddingWeight());
        assertEquals(0.3, tool.getRankingOptions().getHybridSearch().getTextWeight());
        assertNotNull(tool.getFilters());
    }

    // -----------------------------------------------------------------------
    // Round-trip
    // -----------------------------------------------------------------------

    @Test
    public void testRoundTripMinimal() throws IOException {
        FileSearchTool original = new FileSearchTool(Collections.singletonList("vs_rt"));

        String json = serialize(original);
        FileSearchTool deserialized = deserialize(json);

        assertEquals(original.getVectorStoreIds(), deserialized.getVectorStoreIds());
        assertEquals(original.getType(), deserialized.getType());
    }

    @Test
    public void testRoundTripWithRankingOptions() throws IOException {
        FileSearchTool original = new FileSearchTool(Collections.singletonList("vs_rt")).setMaxResults(15L)
            .setRankingOptions(new RankingOptions().setScoreThreshold(0.9)
                .setRanker(RankerVersionType.AUTO)
                .setHybridSearch(new HybridSearchOptions(0.5, 0.5)));

        String json = serialize(original);
        FileSearchTool deserialized = deserialize(json);

        assertEquals(original.getVectorStoreIds(), deserialized.getVectorStoreIds());
        assertEquals(original.getMaxResults(), deserialized.getMaxResults());
        assertEquals(original.getRankingOptions().getScoreThreshold(),
            deserialized.getRankingOptions().getScoreThreshold());
        assertEquals(original.getRankingOptions().getHybridSearch().getEmbeddingWeight(),
            deserialized.getRankingOptions().getHybridSearch().getEmbeddingWeight());

        // JSON should be identical
        String reserializedJson = serialize(deserialized);
        assertEquals(json, reserializedJson);
    }

    // -----------------------------------------------------------------------
    // Filters with openai-java ComparisonFilter
    // -----------------------------------------------------------------------

    @Test
    public void testSerializationWithComparisonFilter() throws IOException {
        ComparisonFilter filter = ComparisonFilter.builder()
            .key("category")
            .type(ComparisonFilter.Type.EQ)
            .value(ComparisonFilter.Value.ofString("science"))
            .build();

        FileSearchTool tool = new FileSearchTool(Collections.singletonList("vs_f1")).setComparisonFilter(filter);

        String json = serialize(tool);

        assertTrue(json.contains("\"key\":\"category\""), "Missing key, got: " + json);
        assertTrue(json.contains("\"value\":\"science\""), "Missing value, got: " + json);
        assertTrue(json.contains("\"vector_store_ids\":[\"vs_f1\"]"));
    }

    @Test
    public void testRoundTripWithComparisonFilter() throws IOException {
        ComparisonFilter filter = ComparisonFilter.builder()
            .key("year")
            .type(ComparisonFilter.Type.GTE)
            .value(ComparisonFilter.Value.ofNumber(2020.0))
            .build();

        FileSearchTool original
            = new FileSearchTool(Collections.singletonList("vs_f2")).setMaxResults(5L).setComparisonFilter(filter);

        String json = serialize(original);
        FileSearchTool deserialized = deserialize(json);

        assertEquals(original.getVectorStoreIds(), deserialized.getVectorStoreIds());
        assertEquals(original.getMaxResults(), deserialized.getMaxResults());
        assertNotNull(deserialized.getFilters());

        // Re-serialize and compare JSON
        String reserializedJson = serialize(deserialized);
        assertEquals(json, reserializedJson);
    }

    @Test
    public void testComparisonFilterAllOperators() throws IOException {
        ComparisonFilter.Type[] operators = {
            ComparisonFilter.Type.EQ,
            ComparisonFilter.Type.NE,
            ComparisonFilter.Type.GT,
            ComparisonFilter.Type.GTE,
            ComparisonFilter.Type.LT,
            ComparisonFilter.Type.LTE, };
        String[] expectedStrings = { "eq", "ne", "gt", "gte", "lt", "lte" };

        for (int i = 0; i < operators.length; i++) {
            ComparisonFilter filter = ComparisonFilter.builder()
                .key("k")
                .type(operators[i])
                .value(ComparisonFilter.Value.ofString("v"))
                .build();

            FileSearchTool tool = new FileSearchTool(Collections.singletonList("vs")).setComparisonFilter(filter);

            String json = serialize(tool);
            assertTrue(json.contains("\"type\":\"" + expectedStrings[i] + "\""),
                "Expected operator " + expectedStrings[i] + ", got: " + json);
        }
    }

    // -----------------------------------------------------------------------
    // Filters with openai-java CompoundFilter
    // -----------------------------------------------------------------------

    @Test
    public void testSerializationWithCompoundFilter() throws IOException {
        CompoundFilter filter = CompoundFilter.builder()
            .type(CompoundFilter.Type.AND)
            .addFilter(CompoundFilter.Filter.ofComparison(ComparisonFilter.builder()
                .key("status")
                .type(ComparisonFilter.Type.EQ)
                .value(ComparisonFilter.Value.ofString("active"))
                .build()))
            .addFilter(CompoundFilter.Filter.ofComparison(ComparisonFilter.builder()
                .key("priority")
                .type(ComparisonFilter.Type.GTE)
                .value(ComparisonFilter.Value.ofNumber(3.0))
                .build()))
            .build();

        FileSearchTool tool = new FileSearchTool(Collections.singletonList("vs_c1")).setCompoundFilter(filter);

        String json = serialize(tool);

        assertTrue(json.contains("\"key\":\"status\""), "Missing status key, got: " + json);
        assertTrue(json.contains("\"key\":\"priority\""), "Missing priority key, got: " + json);
        assertTrue(json.contains("\"active\""), "Missing active value, got: " + json);
    }

    @Test
    public void testRoundTripWithCompoundFilter() throws IOException {
        CompoundFilter filter = CompoundFilter.builder()
            .type(CompoundFilter.Type.OR)
            .addFilter(CompoundFilter.Filter.ofComparison(ComparisonFilter.builder()
                .key("tag")
                .type(ComparisonFilter.Type.EQ)
                .value(ComparisonFilter.Value.ofString("ai"))
                .build()))
            .build();

        FileSearchTool original = new FileSearchTool(Collections.singletonList("vs_c2")).setCompoundFilter(filter);

        String json = serialize(original);
        FileSearchTool deserialized = deserialize(json);

        assertNotNull(deserialized.getFilters());

        String reserializedJson = serialize(deserialized);
        assertEquals(json, reserializedJson);
    }

    // -----------------------------------------------------------------------
    // Filters with BinaryData (existing API still works)
    // -----------------------------------------------------------------------

    @Test
    public void testBinaryDataFilterStillWorks() throws IOException {
        // Use BinaryData.fromObject with an untyped map to ensure it serializes as a JSON object,
        // not a quoted string. BinaryData.fromString() would produce "filters":"{...}" (quoted).
        java.util.Map<String, Object> filterMap = new java.util.LinkedHashMap<>();
        filterMap.put("type", "eq");
        filterMap.put("key", "source");
        filterMap.put("value", "web");
        FileSearchTool tool
            = new FileSearchTool(Collections.singletonList("vs_bd")).setFilters(BinaryData.fromObject(filterMap));

        assertNotNull(tool.getFilters());

        String json = serialize(tool);
        // Verify the filter is written as a JSON object, not a quoted string
        assertTrue(json.contains("\"key\":\"source\""), "Filter should serialize as JSON object, got: " + json);
    }

    // -----------------------------------------------------------------------
    // Deserialization via Tool.fromJson (polymorphic discriminator)
    // -----------------------------------------------------------------------

    @Test
    public void testPolymorphicDeserialization() throws IOException {
        String json = "{\"type\":\"file_search\",\"vector_store_ids\":[\"vs_poly\"]," + "\"max_num_results\":3,"
            + "\"filters\":{\"type\":\"eq\",\"key\":\"lang\",\"value\":\"en\"}}";

        Tool tool;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            tool = Tool.fromJson(reader);
        }

        assertInstanceOf(FileSearchTool.class, tool, "Should deserialize as FileSearchTool");
        FileSearchTool fst = (FileSearchTool) tool;
        assertEquals(Collections.singletonList("vs_poly"), fst.getVectorStoreIds());
        assertEquals(3L, fst.getMaxResults());
        assertNotNull(fst.getFilters());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String serialize(FileSearchTool tool) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(out)) {
            tool.toJson(writer);
        }
        return out.toString("UTF-8");
    }

    private FileSearchTool deserialize(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return FileSearchTool.fromJson(reader);
        }
    }
}
