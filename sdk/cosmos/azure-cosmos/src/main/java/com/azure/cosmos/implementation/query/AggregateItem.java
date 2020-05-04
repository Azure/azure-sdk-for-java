package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Document;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AggregateItem {
    private static final String ITEM_NAME_1 = "item";
    private static final String ITEM_NAME_2 = "item2";

    private final Object document;

    public AggregateItem(Object document) {
        this.document = document;
    }

    public Object getItem() {
        Object object = null;
        if (document instanceof ObjectNode) {
            ObjectNode documentNode = (ObjectNode) document;
            if (documentNode.hasNonNull(ITEM_NAME_2)) {
                object = documentNode.get(ITEM_NAME_2);
            } else if (documentNode.hasNonNull(ITEM_NAME_1)) {
                object = documentNode.get(ITEM_NAME_1);
            } else {
                object = null;
            }
            return object;
        } else {
            return document;
        }
    }
}
