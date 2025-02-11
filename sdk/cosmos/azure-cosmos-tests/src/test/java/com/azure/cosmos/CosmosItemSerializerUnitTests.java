package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DefaultCosmosItemSerializer;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CosmosItemSerializerUnitTests {
    protected static Logger logger = LoggerFactory.getLogger(CosmosItemSerializerUnitTests.class.getSimpleName());
    @DataProvider
    public static Object[][] serializationInclusionModeDataProvider() {
        List<Object[]> providers = new ArrayList<>();

        providers.add(
            new Object[] {
                "",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"nullValueProperty\":null,\"emptyValueProperty\":\"\"}"
            });
        providers.add(
            new Object[] {
                null,
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"nullValueProperty\":null,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "Always",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"nullValueProperty\":null,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "NonNull",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "NOnNULl",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "NonDefault",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "NonEmpty",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0}"
            }
        );

        Object[][] array = new Object[providers.size()][];

        return providers.toArray(array);
    }

    @DataProvider
    public static Object[][] serializationInclusionModePojoDataProvider() {
        List<Object[]> providers = new ArrayList<>();

        providers.add(
            new Object[] {
                "",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"nullValueProperty\":null,\"emptyValueProperty\":\"\"}"
            });
        providers.add(
            new Object[] {
                null,
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"nullValueProperty\":null,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "Always",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"nullValueProperty\":null,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "NonNull",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "NOnNULl",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0,\"emptyValueProperty\":\"\"}"
            }
        );
        providers.add(
            new Object[] {
                "NonDefault",
                "{\"id\":\"SomeId\"}"
            }
        );
        providers.add(
            new Object[] {
                "NonEmpty",
                "{\"id\":\"SomeId\",\"defaultValueProperty\":0}"
            }
        );

        Object[][] array = new Object[providers.size()][];

        return providers.toArray(array);
    }

    @Test(groups = { "unit" }, dataProvider = "serializationInclusionModeDataProvider")
    public void serializationInclusionModesTests(
        String systemPropertyValue,
        String expectedOutcome
    ) {
        try {
            if (systemPropertyValue != null) {
                System.setProperty(Configs.ITEM_SERIALIZATION_INCLUSION_MODE, systemPropertyValue);
            }
            runSerializationInclusionModeTest(expectedOutcome);
        } finally {
            System.clearProperty(Configs.ITEM_SERIALIZATION_INCLUSION_MODE);
        }
    }

    @Test(groups = { "unit" }, dataProvider = "serializationInclusionModePojoDataProvider")
    public void serializationInclusionModesPojoTests(
        String systemPropertyValue,
        String expectedOutcome
    ) {
        try {
            if (systemPropertyValue != null) {
                System.setProperty(Configs.ITEM_SERIALIZATION_INCLUSION_MODE, systemPropertyValue);
            }
            runSerializationInclusionModePojoTest(expectedOutcome);
        } finally {
            System.clearProperty(Configs.ITEM_SERIALIZATION_INCLUSION_MODE);
        }
    }

    private void runSerializationInclusionModeTest(String expectedOutcome) {

        ObjectMapper mapper = Utils.getDocumentObjectMapper(Configs.getItemSerializationInclusionMode());
        CosmosItemSerializer serializer = new DefaultCosmosItemSerializer(mapper);

        ObjectNode node = Utils.getSimpleObjectMapper().createObjectNode();
        node.put("id", "SomeId");
        node.put("defaultValueProperty", 0);
        node.put("nullValueProperty", (String)null);
        node.put("emptyValueProperty", "");
        Map<String, Object> jsonMap = serializer.serialize(node);
        ByteBuffer buffer = Utils.serializeJsonToByteBuffer(
            serializer,
            jsonMap,
            null,
            false);
        String json = new String(buffer.array(), 0, buffer.limit(), StandardCharsets.UTF_8);
        logger.info("Actual: {}, Expected: {}", json, expectedOutcome);
        assertThat(json).isEqualTo(expectedOutcome);
    }

    private void runSerializationInclusionModePojoTest(String expectedOutcome) {

        ObjectMapper mapper = Utils.getDocumentObjectMapper(Configs.getItemSerializationInclusionMode());
        CosmosItemSerializer serializer = new DefaultCosmosItemSerializer(mapper);

        PojoItem node = new PojoItem();
        Map<String, Object> jsonMap = serializer.serialize(node);
        ByteBuffer buffer = Utils.serializeJsonToByteBuffer(
            serializer,
            jsonMap,
            null,
            false);
        String json = new String(buffer.array(), 0, buffer.limit(), StandardCharsets.UTF_8);
        logger.info("Actual: {}, Expected: {}", json, expectedOutcome);
        assertThat(json).isEqualTo(expectedOutcome);
    }

    private static class PojoItem
    {
        public String id = "SomeId";
        public int defaultValueProperty = 0;
        public String nullValueProperty = null;
        public String emptyValueProperty = "";
    }
}
