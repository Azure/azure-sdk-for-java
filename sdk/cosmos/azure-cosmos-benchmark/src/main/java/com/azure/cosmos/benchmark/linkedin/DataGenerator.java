package com.azure.cosmos.benchmark.linkedin;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;


public class DataGenerator {

    private static final JsonNodeFactory JSON_NODE_FACTORY_INSTANCE = JsonNodeFactory.withExactBigDecimals(true);

    public DataGenerator() {

    }
}
