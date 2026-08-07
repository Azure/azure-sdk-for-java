// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Unit tests for the pure helpers in CreateAndTestCommand. Mirrors Python's
 * tests/test_skills_create_and_test.py. The CLI entry point (run) and the
 * network-dependent helpers are not covered here — they require an Azure
 * client and live recording infrastructure.
 */

package com.azure.ai.contentunderstanding.skills;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateAndTestCommandTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static ObjectNode scalar(String value, double conf) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "string");
        n.put("valueString", value);
        n.put("confidence", conf);
        return n;
    }

    private static ObjectNode number(double value, double conf) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "number");
        n.put("valueNumber", value);
        n.put("confidence", conf);
        return n;
    }

    private static ObjectNode arrayOfObjects(List<ObjectNode> items) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "array");
        n.putArray("valueArray").addAll(items);
        return n;
    }

    private static ObjectNode objectField(ObjectNode inner) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("type", "object");
        n.set("valueObject", inner);
        return n;
    }

    @Test
    void summarizeFlattensNestedArrayAndObjectFieldsToLeafRows() {
        ObjectNode lineItem1 = MAPPER.createObjectNode();
        lineItem1.set("itemCode", scalar("A123", 0.80));
        lineItem1.set("amount", number(60.0, 0.92));

        ObjectNode lineItem2 = MAPPER.createObjectNode();
        lineItem2.set("itemCode", scalar("B456", 0.70));
        lineItem2.set("amount", number(30.0, 0.90));

        ObjectNode addressInner = MAPPER.createObjectNode();
        addressInner.set("street", scalar("123 Main St", 0.88));

        ObjectNode fields = MAPPER.createObjectNode();
        fields.set("invoiceNumber", scalar("INV-100", 0.95));
        fields.set("lineItems", arrayOfObjects(List.of(
            objectField(lineItem1),
            objectField(lineItem2))));
        fields.set("address", objectField(addressInner));

        ObjectNode content = MAPPER.createObjectNode();
        content.set("fields", fields);

        ObjectNode doc = MAPPER.createObjectNode();
        doc.putArray("contents").add(content);

        List<CreateAndTestCommand.NamedDoc> results = new ArrayList<>();
        results.add(new CreateAndTestCommand.NamedDoc("docX", doc));

        String out = CreateAndTestCommand.summarize(results);

        // Leaf rows present.
        assertTrue(out.contains("lineItems[].itemCode"), "leaf row lineItems[].itemCode missing");
        assertTrue(out.contains("lineItems[].amount"), "leaf row lineItems[].amount missing");
        assertTrue(out.contains("address.street"), "leaf row address.street missing");
        assertTrue(out.contains("invoiceNumber"), "leaf row invoiceNumber missing");

        // The old aggregate-only behaviour would emit a bare `lineItems` or
        // `address` row with `n/a` confidence and no children. The new
        // behaviour must not emit those.
        for (String line : out.split("\n")) {
            String stripped = line.trim();
            assertFalse(stripped.startsWith("lineItems ") || stripped.startsWith("address "),
                "aggregate-only row leaked: " + line);
        }

        // Lowest-confidence list should surface the 0.700 leaf.
        assertTrue(out.contains("0.700"), "missing 0.700 confidence row");
        int lowIdx = out.indexOf("lowest-confidence");
        assertTrue(lowIdx >= 0, "no 'lowest-confidence' section found");
        String lowSection = out.substring(lowIdx);
        assertTrue(lowSection.contains("lineItems[].itemCode"),
            "lowest-confidence section should mention lineItems[].itemCode");
    }
}
