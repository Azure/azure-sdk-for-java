// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Unit tests for SchemaValidator. Mirrors Python's
 * tests/test_skills_shared_schema_validator.py and the .NET
 * SkillSchemaValidatorTests.cs. Pure Jackson — no Azure.* deps,
 * enforced by a purity guard test below.
 */

package com.azure.ai.contentunderstanding.skills;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaValidatorTest {

    private static final ObjectMapper M = new ObjectMapper();

    private static JsonNode parse(String json) throws IOException {
        return M.readTree(json);
    }

    // -------------------------------------------------------------------
    // Valid schemas
    // -------------------------------------------------------------------

    @Test
    void validateValidSingleTypeSchemaReturnsOk() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "fieldSchema": {
                "fields": {
                  "invoiceNumber": {
                    "type": "string",
                    "method": "extract",
                    "description": "Invoice number printed at the top right."
                  }
                }
              }
            }
            """));
        assertTrue(r.isOk(), () -> "Errors: " + String.join("; ", r.getErrors()));
        assertTrue(r.getErrors().isEmpty());
    }

    @Test
    void validateValidClassifyRouteSchemaReturnsOk() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "config": {
                "enableSegment": true,
                "contentCategories": {
                  "invoice": {
                    "description": "Pages whose top heading is 'Invoice'.",
                    "analyzerId": "invoice_extractor_v1"
                  },
                  "bank_statement": {
                    "description": "Pages whose top heading is 'Bank Statement'.",
                    "analyzerId": "bank_statement_extractor_v1"
                  }
                }
              }
            }
            """));
        assertTrue(r.isOk(), () -> "Errors: " + String.join("; ", r.getErrors()));
    }

    @Test
    void validateClassifyRouteCategoryWithoutAnalyzerIdAllowedForOtherBucket() throws IOException {
        // Category without analyzerId classifies only; the route is fine and
        // is a documented pattern for an "other" catch-all.
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "config": {
                "enableSegment": true,
                "contentCategories": {
                  "invoice": { "description": "Invoices.", "analyzerId": "inv" },
                  "other":   { "description": "Anything else." }
                }
              }
            }
            """));
        assertTrue(r.isOk(), () -> "Errors: " + String.join("; ", r.getErrors()));
    }

    // -------------------------------------------------------------------
    // Single-type rejections
    // -------------------------------------------------------------------

    @Test
    void validateUnknownBaseAnalyzerIdRejected() throws IOException {
        // Catches the prebuilt-documentAnalyzer typo class — the service
        // returns InvalidBaseAnalyzerId without a useful message, so we
        // catch it locally with the actual allow-list.
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-documentAnalyzer",
              "fieldSchema": { "fields": { "x": { "type": "string" } } }
            }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("baseAnalyzerId")));
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("prebuilt-documentAnalyzer")));
    }

    @Test
    void validateMissingFieldSchemaOnNonClassifierRejected() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            { "baseAnalyzerId": "prebuilt-document" }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("fieldSchema")));
    }

    @Test
    void validateEmptyFieldsObjectRejected() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "fieldSchema": { "fields": {} }
            }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("at least one field")));
    }

    @Test
    void validateUnknownFieldTypeRejected() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "fieldSchema": { "fields": { "x": { "type": "float" } } }
            }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("'float'")));
    }

    @Test
    void validateUnknownFieldMethodRejected() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "fieldSchema": { "fields": { "x": { "type": "string", "method": "infer" } } }
            }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("'infer'")));
    }

    @Test
    void validateNestedObjectFieldRecursesIntoProperties() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "fieldSchema": {
                "fields": {
                  "billTo": {
                    "type": "object",
                    "properties": {
                      "name": { "type": "bogus" }
                    }
                  }
                }
              }
            }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("billTo")));
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("'bogus'")));
    }

    @Test
    void validateArrayFieldRecursesIntoItems() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "fieldSchema": {
                "fields": {
                  "lineItems": {
                    "type": "array",
                    "items": { "type": "nope" }
                  }
                }
              }
            }
            """));
        assertFalse(r.isOk());
        // Path uses bracketed notation: fieldSchema.fields['lineItems'].items.type
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("'lineItems'")));
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains(".items.type")));
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("'nope'")));
    }

    // -------------------------------------------------------------------
    // Classify-and-route rejections
    // -------------------------------------------------------------------

    @Test
    void validateClassifyRouteWithTopLevelFieldSchemaRejected() throws IOException {
        // Field extraction belongs in inner analyzers, not the outer
        // classifier — catch this before the service does.
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "fieldSchema": { "fields": { "x": { "type": "string" } } },
              "config": {
                "enableSegment": true,
                "contentCategories": {
                  "invoice": { "description": "d", "analyzerId": "a" }
                }
              }
            }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream()
            .anyMatch(e -> e.contains("fieldSchema") && e.contains("inner")));
    }

    @Test
    void validateClassifyRouteWithoutEnableSegmentRejected() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "config": {
                "contentCategories": {
                  "invoice": { "description": "d", "analyzerId": "a" }
                }
              }
            }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("enableSegment")));
    }

    @Test
    void validateClassifyRouteWithEmptyCategoryDescriptionRejected() throws IOException {
        SchemaValidator.Result r = SchemaValidator.validate(parse("""
            {
              "baseAnalyzerId": "prebuilt-document",
              "config": {
                "enableSegment": true,
                "contentCategories": {
                  "invoice": { "description": "  ", "analyzerId": "a" }
                }
              }
            }
            """));
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("description")));
    }

    // -------------------------------------------------------------------
    // validateFile
    // -------------------------------------------------------------------

    @Test
    void validateFileMissingFileReturnsError() {
        Path missing = Paths.get(
            System.getProperty("java.io.tmpdir"),
            "definitely-not-there-" + System.nanoTime() + ".json");
        SchemaValidator.Result r = SchemaValidator.validateFile(missing);
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("not found")));
    }

    @Test
    void validateFileInvalidJsonReturnsError(@TempDir Path tmp) throws IOException {
        Path p = tmp.resolve("broken.json");
        Files.writeString(p, "{ this is not json");
        SchemaValidator.Result r = SchemaValidator.validateFile(p);
        assertFalse(r.isOk());
        assertTrue(r.getErrors().stream().anyMatch(e -> e.contains("not valid JSON")));
    }

    @Test
    void validateFileValidSchemaOnDiskRoundTrips(@TempDir Path tmp) throws IOException {
        Path p = tmp.resolve("valid.json");
        Files.writeString(p, """
            {
              "baseAnalyzerId": "prebuilt-document",
              "fieldSchema": { "fields": { "x": { "type": "string" } } }
            }
            """);
        SchemaValidator.Result r = SchemaValidator.validateFile(p);
        assertTrue(r.isOk(), () -> "Errors: " + String.join("; ", r.getErrors()));
    }

    // -------------------------------------------------------------------
    // Allow-list surface
    // -------------------------------------------------------------------

    @Test
    void knownBaseAnalyzerIdsOnlyContainsModalityPrebuilts() {
        // Sanity check: the allow-list must NOT include `*Search` variants,
        // `prebuilt-invoice`, or `prebuilt-receipt` — these return
        // `InvalidBaseAnalyzerId` if used as `baseAnalyzerId` for a custom
        // analyzer. Only modality-level prebuilts are valid.
        Set<String> expected = new LinkedHashSet<>();
        expected.add("prebuilt-document");
        expected.add("prebuilt-audio");
        expected.add("prebuilt-video");
        expected.add("prebuilt-image");
        assertEquals(expected, SchemaValidator.KNOWN_BASE_ANALYZER_IDS);
    }

    // -------------------------------------------------------------------
    // Purity guard
    // -------------------------------------------------------------------

    @Test
    void schemaValidatorSourceDoesNotImportAzureOrHttpNamespaces() throws IOException {
        // The validator is intentionally pure-Java so it can be unit-tested
        // without spinning up the Azure SDK, and so it can be reused from
        // any context (CI, scripts, samples). Drift would creep in if a
        // future change accidentally pulls in com.azure.* or an HTTP client.
        Path src = locateSchemaValidatorSource();
        assertTrue(Files.exists(src), "SchemaValidator.java not found at " + src);
        String text = Files.readString(src);
        for (String forbidden : new String[] {
            "import com.azure.",
            "import java.net.http",
            "import java.net.HttpURLConnection",
            "import java.net.Socket"
        }) {
            assertFalse(text.contains(forbidden),
                "SchemaValidator.java must not contain `" + forbidden
                    + "` — see _shared/README.md");
        }
    }

    private static Path locateSchemaValidatorSource() {
        // Walk up from the test working directory to find the module root.
        Path dir = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 8 && dir != null; i++) {
            Path candidate = dir.resolve("src/main/java/com/azure/ai/contentunderstanding/skills/SchemaValidator.java");
            if (Files.exists(candidate)) {
                return candidate;
            }
            // Also check the known relative path under the package skill tree.
            Path nested = dir.resolve(".github/skills/_shared/src/main/java/com/azure/ai/contentunderstanding/skills/SchemaValidator.java");
            if (Files.exists(nested)) {
                return nested;
            }
            dir = dir.getParent();
        }
        return Paths.get("src/main/java/com/azure/ai/contentunderstanding/skills/SchemaValidator.java");
    }
}
