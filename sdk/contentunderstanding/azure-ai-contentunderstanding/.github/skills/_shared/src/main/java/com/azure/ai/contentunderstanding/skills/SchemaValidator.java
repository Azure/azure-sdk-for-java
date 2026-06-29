// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Pure-Java validator for Content Understanding analyzer schema JSON.
 *
 * Catches structural mistakes (missing keys, unknown baseAnalyzerId values,
 * malformed contentCategories routes) BEFORE any call to the Content
 * Understanding service. Failing fast here gives users an actionable error
 * message and avoids a wasted service round-trip.
 *
 * Design rules (see README.md in this directory):
 *   * No com.azure.* imports — pure Jackson.
 *   * No network calls.
 *   * Self-contained — drop-in for any tool or test.
 *
 * Public surface:
 *   * SchemaValidator.validate(JsonNode)            — validate a parsed schema node
 *   * SchemaValidator.validateFile(Path)            — convenience wrapper that loads a JSON file
 *   * SchemaValidator.validateString(String)        — validate a raw JSON string
 *   * SchemaValidator.KNOWN_BASE_ANALYZER_IDS       — allow-list of baseAnalyzerId values
 */

package com.azure.ai.contentunderstanding.skills;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Pure-Java validator for Content Understanding analyzer schemas.
 */
public final class SchemaValidator {

    /**
     * Valid {@code baseAnalyzerId} values for custom analyzers. Only modality-level
     * prebuilts are accepted by the service for {@code baseAnalyzerId}; {@code *Search}
     * variants and task-specific prebuilts ({@code prebuilt-invoice}, {@code prebuilt-receipt})
     * return {@code InvalidBaseAnalyzerId} if used here. See
     * <a href="https://learn.microsoft.com/azure/ai-services/content-understanding/concepts/analyzer-reference#baseanalyzerid">
     * the analyzer reference docs</a>.
     */
    public static final Set<String> KNOWN_BASE_ANALYZER_IDS = Collections.unmodifiableSet(
        new LinkedHashSet<>(Arrays.asList(
            "prebuilt-document",
            "prebuilt-audio",
            "prebuilt-video",
            "prebuilt-image")));

    private static final Set<String> ALLOWED_FIELD_TYPES = Collections.unmodifiableSet(
        new LinkedHashSet<>(Arrays.asList(
            "string", "number", "integer", "boolean", "date", "time", "array", "object")));

    private static final Set<String> ALLOWED_FIELD_METHODS = Collections.unmodifiableSet(
        new LinkedHashSet<>(Arrays.asList(
            "extract", "generate", "classify")));

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Validation result: {@link #ok} is true when the schema is structurally valid. */
    public static final class Result {
        private final boolean ok;
        private final List<String> errors;

        Result(boolean ok, List<String> errors) {
            this.ok = ok;
            this.errors = Collections.unmodifiableList(errors);
        }

        public boolean isOk() {
            return ok;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    private SchemaValidator() {
        // utility class
    }

    /** Validate a parsed analyzer schema node. */
    public static Result validate(JsonNode schema) {
        List<String> errors = new ArrayList<>();

        if (schema == null || !schema.isObject()) {
            errors.add("schema must be a JSON object at the top level");
            return new Result(false, errors);
        }

        // baseAnalyzerId
        JsonNode baseEl = schema.get("baseAnalyzerId");
        if (baseEl == null || baseEl.isNull()) {
            errors.add("missing required key: baseAnalyzerId");
        } else if (!baseEl.isTextual()) {
            errors.add("baseAnalyzerId must be a string");
        } else {
            String baseValue = baseEl.asText();
            if (!KNOWN_BASE_ANALYZER_IDS.contains(baseValue)) {
                String known = String.join(", ", new TreeSet<>(KNOWN_BASE_ANALYZER_IDS)
                    .stream().map(s -> "'" + s + "'").toList());
                errors.add("unknown baseAnalyzerId: '" + baseValue + "'. Known values: [" + known + "]");
            }
        }

        // config (optional, but if present must be an object)
        JsonNode config = schema.get("config");
        if (config != null && !config.isNull()) {
            if (!config.isObject()) {
                errors.add("config, if present, must be an object");
                // Bail out — without a well-typed config we can't tell whether
                // this is a single-type or classify-and-route schema, and
                // falling through would emit a confusing cascade of
                // "missing fieldSchema" errors rooted in the same problem.
                return new Result(false, errors);
            }
        }

        boolean isClassifyRoute = config != null
            && config.isObject()
            && config.has("contentCategories");

        if (isClassifyRoute) {
            errors.addAll(validateClassifyRoute(config));
            if (schema.has("fieldSchema")) {
                errors.add("classify-and-route schemas should not declare fieldSchema at "
                    + "the top level; field extraction belongs in inner analyzers");
            }
        } else {
            errors.addAll(validateSingleType(schema));
        }

        return new Result(errors.isEmpty(), errors);
    }

    /** Validate a schema stored in a JSON file. */
    public static Result validateFile(Path path) {
        if (path == null || !Files.exists(path)) {
            return new Result(false, Collections.singletonList(
                "schema file not found: " + (path == null ? "<null>" : path.toString())));
        }
        String text;
        try {
            text = Files.readString(path);
        } catch (IOException ex) {
            return new Result(false, Collections.singletonList(
                "failed to read schema file " + path + ": " + ex.getMessage()));
        }
        return validateString(text, path.toString());
    }

    /** Validate a raw JSON string. */
    public static Result validateString(String json) {
        return validateString(json, "<inline>");
    }

    private static Result validateString(String json, String sourceLabel) {
        try {
            return validate(MAPPER.readTree(json));
        } catch (JsonProcessingException ex) {
            return new Result(false, Collections.singletonList(
                "schema file is not valid JSON (" + sourceLabel + "): " + ex.getOriginalMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private static List<String> validateSingleType(JsonNode schema) {
        List<String> errors = new ArrayList<>();
        JsonNode fieldSchema = schema.get("fieldSchema");
        if (fieldSchema == null || fieldSchema.isNull()) {
            errors.add("missing required key: fieldSchema "
                + "(single-type schemas must declare fields to extract)");
            return errors;
        }
        if (!fieldSchema.isObject()) {
            errors.add("fieldSchema must be an object");
            return errors;
        }
        JsonNode fields = fieldSchema.get("fields");
        if (fields == null || fields.isNull()) {
            errors.add("fieldSchema.fields is required");
            return errors;
        }
        if (!fields.isObject()) {
            errors.add("fieldSchema.fields must be an object mapping field names to definitions");
            return errors;
        }
        boolean any = false;
        Iterator<Map.Entry<String, JsonNode>> it = fields.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            any = true;
            errors.addAll(validateFieldDefinition(entry.getKey(), entry.getValue(), null));
        }
        if (!any) {
            errors.add("fieldSchema.fields must declare at least one field");
        }
        return errors;
    }

    private static List<String> validateFieldDefinition(String name, JsonNode definition, String path) {
        List<String> errors = new ArrayList<>();
        String prefix = path != null ? path : "fieldSchema.fields['" + name + "']";

        if (!definition.isObject()) {
            errors.add(prefix + " must be an object");
            return errors;
        }

        String fieldType = null;
        JsonNode typeEl = definition.get("type");
        if (typeEl == null || typeEl.isNull()) {
            errors.add(prefix + ".type is required");
        } else if (!typeEl.isTextual()) {
            errors.add(prefix + ".type must be a string");
        } else {
            fieldType = typeEl.asText();
            if (!ALLOWED_FIELD_TYPES.contains(fieldType)) {
                String allowed = String.join(", ", new TreeSet<>(ALLOWED_FIELD_TYPES)
                    .stream().map(s -> "'" + s + "'").toList());
                errors.add(prefix + ".type '" + fieldType + "' is not one of [" + allowed + "]");
            }
        }

        JsonNode methodEl = definition.get("method");
        if (methodEl != null && !methodEl.isNull()) {
            if (!methodEl.isTextual()) {
                errors.add(prefix + ".method must be a string");
            } else {
                String method = methodEl.asText();
                if (!ALLOWED_FIELD_METHODS.contains(method)) {
                    String allowed = String.join(", ", new TreeSet<>(ALLOWED_FIELD_METHODS)
                        .stream().map(s -> "'" + s + "'").toList());
                    errors.add(prefix + ".method '" + method + "' is not one of [" + allowed + "]");
                }
            }
        }

        JsonNode descEl = definition.get("description");
        if (descEl != null && !descEl.isNull() && !descEl.isTextual()) {
            errors.add(prefix + ".description must be a string");
        }

        if ("object".equals(fieldType)) {
            JsonNode propsEl = definition.get("properties");
            if (propsEl != null && !propsEl.isNull()) {
                if (!propsEl.isObject()) {
                    errors.add(prefix + ".properties must be an object");
                } else {
                    Iterator<Map.Entry<String, JsonNode>> it = propsEl.fields();
                    while (it.hasNext()) {
                        Map.Entry<String, JsonNode> child = it.next();
                        errors.addAll(validateFieldDefinition(
                            child.getKey(), child.getValue(),
                            prefix + ".properties['" + child.getKey() + "']"));
                    }
                }
            }
        } else if ("array".equals(fieldType)) {
            JsonNode itemsEl = definition.get("items");
            if (itemsEl != null && !itemsEl.isNull()) {
                if (!itemsEl.isObject()) {
                    errors.add(prefix + ".items must be an object");
                } else {
                    errors.addAll(validateFieldDefinition("items", itemsEl, prefix + ".items"));
                }
            }
        }

        return errors;
    }

    private static List<String> validateClassifyRoute(JsonNode config) {
        List<String> errors = new ArrayList<>();

        JsonNode enableEl = config.get("enableSegment");
        boolean hasEnableSegment = enableEl != null && enableEl.isBoolean() && enableEl.asBoolean();
        if (!hasEnableSegment) {
            errors.add("classify-and-route schemas must set config.enableSegment = true");
        }

        JsonNode categories = config.get("contentCategories");
        if (categories == null || !categories.isObject()) {
            errors.add("config.contentCategories must be an object");
            return errors;
        }

        boolean any = false;
        Iterator<Map.Entry<String, JsonNode>> it = categories.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> cat = it.next();
            any = true;
            String prefix = "config.contentCategories['" + cat.getKey() + "']";
            JsonNode entry = cat.getValue();

            if (!entry.isObject()) {
                errors.add(prefix + " must be an object");
                continue;
            }

            JsonNode descEl = entry.get("description");
            if (descEl == null || !descEl.isTextual() || descEl.asText().isBlank()) {
                errors.add(prefix + ".description is required and must be a non-empty string");
            }

            JsonNode analyzerIdEl = entry.get("analyzerId");
            if (analyzerIdEl != null && !analyzerIdEl.isNull() && !analyzerIdEl.isTextual()) {
                errors.add(prefix + ".analyzerId, if present, must be a string");
            }
        }

        if (!any) {
            errors.add("config.contentCategories must declare at least one category");
        }

        return errors;
    }
}
