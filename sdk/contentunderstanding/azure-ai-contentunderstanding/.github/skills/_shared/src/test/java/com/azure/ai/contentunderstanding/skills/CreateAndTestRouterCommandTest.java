// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Unit tests for pure helpers in CreateAndTestRouterCommand. Mirrors the
 * portion of Python's tests/test_skills_classify_route_router.py that does
 * not require mocking the Azure client.
 */

package com.azure.ai.contentunderstanding.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateAndTestRouterCommandTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static ObjectNode field(String value, double confidence) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("valueString", value);
        n.put("confidence", confidence);
        return n;
    }

    private static ObjectNode segment(String category, Map<String, ObjectNode> fields) {
        ObjectNode n = MAPPER.createObjectNode();
        n.put("category", category);
        ObjectNode fieldsObj = n.putObject("fields");
        for (Map.Entry<String, ObjectNode> e : fields.entrySet()) {
            fieldsObj.set(e.getKey(), e.getValue());
        }
        return n;
    }

    private static ObjectNode docWithSegments(List<ObjectNode> segments) {
        ObjectNode doc = MAPPER.createObjectNode();
        ArrayNode contents = doc.putArray("contents");
        for (ObjectNode s : segments) {
            contents.add(s);
        }
        return doc;
    }

    @Test
    void summarizeRoutedUsesPerCategoryDenominator() {
        // Three invoice segments (all filled) must report 100%, not be
        // diluted by other categories' segments.
        ObjectNode doc = docWithSegments(List.of(
            segment("invoice", Map.of("InvoiceNumber", field("INV-1", 0.9))),
            segment("invoice", Map.of("InvoiceNumber", field("INV-2", 0.91))),
            segment("invoice", Map.of("InvoiceNumber", field("INV-3", 0.92))),
            segment("bank_statement", Map.of("AccountNumber", field("12345", 0.8)))));

        List<CreateAndTestCommand.NamedDoc> results = new ArrayList<>();
        results.add(new CreateAndTestCommand.NamedDoc("packet_a", doc));

        String text = CreateAndTestRouterCommand.summarizeRouted(results);

        // Invoice: 3 segments, 3 filled → 100%
        assertTrue(text.contains("category: invoice  (3 segments)"),
            "invoice segment count wrong: " + text);
        assertTrue(text.contains("InvoiceNumber") && text.contains("100.0%"),
            "invoice not at 100%: " + text);
        // Bank statement: 1 segment, 1 filled → 100%
        assertTrue(
            text.contains("category: bank_statement  (1 segment)") ||
                text.contains("category: bank_statement  (1 segments)"),
            "bank_statement segment count wrong: " + text);
        // Packet-wide denominator must NOT leak through.
        assertFalse(text.contains("33.3%"), "33.3% leaked: " + text);
        assertFalse(text.contains("25.0%"), "25.0% leaked: " + text);
    }

    @Test
    void summarizeRoutedReportsZeroFillForMissingFieldInSomeSegments() {
        // Two invoice segments, only one has TotalAmount → 50% fill.
        Map<String, ObjectNode> segWithBoth = new HashMap<>();
        segWithBoth.put("InvoiceNumber", field("INV-1", 0.9));
        segWithBoth.put("TotalAmount", field("$100", 0.7));

        ObjectNode doc = docWithSegments(List.of(
            segment("invoice", segWithBoth),
            segment("invoice", Map.of("InvoiceNumber", field("INV-2", 0.91)))));

        List<CreateAndTestCommand.NamedDoc> results = new ArrayList<>();
        results.add(new CreateAndTestCommand.NamedDoc("packet", doc));

        String text = CreateAndTestRouterCommand.summarizeRouted(results);
        assertTrue(text.contains("category: invoice  (2 segments)"),
            "segment count wrong: " + text);
        // InvoiceNumber appears in both segments → 100%
        assertTrue(text.contains("InvoiceNumber") && text.contains("100.0%"),
            "InvoiceNumber not 100%: " + text);
        // TotalAmount appears in 1 of 2 → 50%
        assertTrue(text.contains("TotalAmount") && text.contains(" 50.0%"),
            "TotalAmount not 50%: " + text);
    }

    @Test
    void wireInnerIdsSubstitutesMatchingAliases() {
        // Category name is deliberately different from the analyzerId value
        // to catch the "keyed off cat name instead of alias" regression.
        ObjectNode outer = MAPPER.createObjectNode();
        outer.put("baseAnalyzerId", "prebuilt-document");
        ObjectNode config = outer.putObject("config");
        config.put("enableSegment", true);
        ObjectNode cats = config.putObject("contentCategories");
        ObjectNode invCat = cats.putObject("invoice_bucket");
        invCat.put("description", "d");
        invCat.put("analyzerId", "invoice");
        ObjectNode loanCat = cats.putObject("loan_bucket");
        loanCat.put("description", "d");
        loanCat.put("analyzerId", "loan_application");

        Map<String, String> aliasToId = new LinkedHashMap<>();
        aliasToId.put("invoice", "real-invoice-id");
        aliasToId.put("loan_application", "real-loan-id");

        CreateAndTestRouterCommand.WireResult wired =
            CreateAndTestRouterCommand.wireInnerIds(outer, aliasToId);

        assertTrue(wired.errors.isEmpty(), "expected no errors, got: " + wired.errors);
        ObjectNode patchedCats = (ObjectNode) wired.patched.get("config").get("contentCategories");
        assertEquals("real-invoice-id",
            patchedCats.get("invoice_bucket").get("analyzerId").asText(),
            "invoice_bucket should be patched to the aliasToId value for 'invoice'");
        assertEquals("real-loan-id",
            patchedCats.get("loan_bucket").get("analyzerId").asText(),
            "loan_bucket should be patched to the aliasToId value for 'loan_application'");
        // Input must not be mutated.
        assertEquals("invoice",
            outer.get("config").get("contentCategories").get("invoice_bucket").get("analyzerId").asText(),
            "wireInnerIds mutated its input");
    }

    @Test
    void wireInnerIdsKeepsPrebuiltAnalyzerIdsAsIs() {
        // analyzerId values starting with "prebuilt-" resolve to Azure
        // service prebuilts and must NOT require a matching --inner-schema.
        ObjectNode outer = MAPPER.createObjectNode();
        outer.put("baseAnalyzerId", "prebuilt-document");
        ObjectNode config = outer.putObject("config");
        config.put("enableSegment", true);
        ObjectNode cats = config.putObject("contentCategories");
        ObjectNode invCat = cats.putObject("invoice");
        invCat.put("description", "d");
        invCat.put("analyzerId", "prebuilt-invoice");
        ObjectNode receiptCat = cats.putObject("receipt");
        receiptCat.put("description", "d");
        receiptCat.put("analyzerId", "prebuilt-receipt");

        // No --inner-schema needed; both categories use service prebuilts.
        Map<String, String> aliasToId = new LinkedHashMap<>();

        CreateAndTestRouterCommand.WireResult wired =
            CreateAndTestRouterCommand.wireInnerIds(outer, aliasToId);

        assertTrue(wired.errors.isEmpty(), "prebuilt-* values must not need aliases; got: " + wired.errors);
        ObjectNode patchedCats = (ObjectNode) wired.patched.get("config").get("contentCategories");
        assertEquals("prebuilt-invoice",
            patchedCats.get("invoice").get("analyzerId").asText(),
            "prebuilt-invoice must be preserved as-is");
        assertEquals("prebuilt-receipt",
            patchedCats.get("receipt").get("analyzerId").asText(),
            "prebuilt-receipt must be preserved as-is");
    }

    @Test
    void wireInnerIdsReportsMissingAliasAsError() {
        ObjectNode outer = MAPPER.createObjectNode();
        outer.put("baseAnalyzerId", "prebuilt-document");
        ObjectNode config = outer.putObject("config");
        config.put("enableSegment", true);
        ObjectNode cats = config.putObject("contentCategories");
        ObjectNode invCat = cats.putObject("invoice");
        invCat.put("description", "d");
        invCat.put("analyzerId", "invoice");
        ObjectNode loanCat = cats.putObject("loan");
        loanCat.put("description", "d");
        loanCat.put("analyzerId", "loan_application");

        Map<String, String> aliasToId = new LinkedHashMap<>();
        aliasToId.put("invoice", "real-invoice-id");
        // "loan_application" alias intentionally missing.

        CreateAndTestRouterCommand.WireResult wired =
            CreateAndTestRouterCommand.wireInnerIds(outer, aliasToId);

        assertFalse(wired.errors.isEmpty(), "expected an error for missing alias");
        assertTrue(
            wired.errors.stream().anyMatch(e -> e.contains("loan_application") && e.contains("loan")),
            "expected error to name the missing alias and category, got: " + wired.errors);
    }

    @Test
    void wireInnerIdsReportsUnusedAliasAsError() {
        ObjectNode outer = MAPPER.createObjectNode();
        outer.put("baseAnalyzerId", "prebuilt-document");
        ObjectNode config = outer.putObject("config");
        config.put("enableSegment", true);
        ObjectNode cats = config.putObject("contentCategories");
        ObjectNode invCat = cats.putObject("invoice");
        invCat.put("description", "d");
        invCat.put("analyzerId", "invoice");

        Map<String, String> aliasToId = new LinkedHashMap<>();
        aliasToId.put("invoice", "real-invoice-id");
        // "extra" is supplied but not referenced by any category — likely a typo.
        aliasToId.put("extra", "real-extra-id");

        CreateAndTestRouterCommand.WireResult wired =
            CreateAndTestRouterCommand.wireInnerIds(outer, aliasToId);

        assertFalse(wired.errors.isEmpty(), "expected an error for unused alias");
        assertTrue(
            wired.errors.stream().anyMatch(e -> e.contains("extra") && e.contains("no category")),
            "expected error to name the unused alias, got: " + wired.errors);
    }

    @Test
    void wireInnerIdsAllowsCategoriesWithoutAnalyzerId() {
        // Classification-only "other" bucket must not cause a wire error.
        ObjectNode outer = MAPPER.createObjectNode();
        outer.put("baseAnalyzerId", "prebuilt-document");
        ObjectNode config = outer.putObject("config");
        config.put("enableSegment", true);
        ObjectNode cats = config.putObject("contentCategories");
        ObjectNode invCat = cats.putObject("invoice");
        invCat.put("description", "d");
        invCat.put("analyzerId", "invoice");
        ObjectNode otherCat = cats.putObject("other");
        otherCat.put("description", "catch-all classification bucket");
        // no analyzerId

        Map<String, String> aliasToId = new LinkedHashMap<>();
        aliasToId.put("invoice", "real-invoice-id");

        CreateAndTestRouterCommand.WireResult wired =
            CreateAndTestRouterCommand.wireInnerIds(outer, aliasToId);

        assertTrue(wired.errors.isEmpty(),
            "categories without analyzerId must be allowed; got: " + wired.errors);
        ObjectNode patchedCats = (ObjectNode) wired.patched.get("config").get("contentCategories");
        assertFalse(patchedCats.get("other").has("analyzerId"),
            "'other' bucket must remain analyzerId-less");
    }
}
