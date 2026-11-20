// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

final class SelectionTestData {

    static final String KEY_PREFIX = "/application/selection.entries.";
    static final String FIRST_PREFIX = "/application/selection-first/";
    static final String SECOND_PREFIX = "/application/selection-second/";
    static final String BASE_LABEL = "selection-base";
    static final String OVERRIDE_LABEL = "selection-override";

    private SelectionTestData() {
    }

    static Stream<Scenario> scenarios() {
        Map<String, String> unlabeled = Map.of("shared", "unlabeled", "unlabeled-only", "unlabeled-only");
        Map<String, String> base = Map.of("shared", "base", "base-only", "base-only");
        return Stream.of(
            scenario("No label excludes labeled settings", unlabeled, select("")),
            scenario("Explicit label excludes unlabeled settings", base, select(BASE_LABEL)),
            scenario("Last comma-separated label wins", labeledEntries("override"),
                select(BASE_LABEL + "," + OVERRIDE_LABEL)),
            scenario("Reversing comma-separated labels reverses the winner", labeledEntries("base"),
                select(OVERRIDE_LABEL + "," + BASE_LABEL)),
            scenario("Leading empty label loads defaults before the named label",
                Map.of("shared", "base", "unlabeled-only", "unlabeled-only", "base-only", "base-only"),
                select("," + BASE_LABEL)),
            scenario("Trailing empty label overrides the named label",
                Map.of("shared", "unlabeled", "unlabeled-only", "unlabeled-only", "base-only", "base-only"),
                select(BASE_LABEL + ",")),
            scenario("Last select wins for overlapping keys", labeledEntries("override"),
                select(BASE_LABEL), select(OVERRIDE_LABEL)),
            scenario("Reversing selects reverses the winner", labeledEntries("base"),
                select(OVERRIDE_LABEL), select(BASE_LABEL)),
            scenario("Different key filters merge and the last select wins", distinctFilterEntries("second-select"),
                new Select(FIRST_PREFIX, BASE_LABEL), new Select(SECOND_PREFIX, OVERRIDE_LABEL)),
            scenario("Reversing different key filters reverses the winner", distinctFilterEntries("first-select"),
                new Select(SECOND_PREFIX, OVERRIDE_LABEL), new Select(FIRST_PREFIX, BASE_LABEL)),
            new Scenario("Active profiles supply labels in priority order", List.of(select(null)),
                List.of(BASE_LABEL, OVERRIDE_LABEL), labeledEntries("override")),
            new Scenario("Explicit label overrides active profile selection", List.of(select(BASE_LABEL)),
                List.of(OVERRIDE_LABEL), base));
    }

    static List<Map<String, Object>> settings(String keyFilter, String label) {
        if (!keyFilter.endsWith("*")) {
            throw new IllegalArgumentException("Expected a prefix key filter: " + keyFilter);
        }
        String prefix = keyFilter.substring(0, keyFilter.length() - 1);
        String selectedLabel = "\0".equals(label) ? "" : label;
        try (InputStream input = Objects.requireNonNull(
            SelectionTestData.class.getResourceAsStream("/test-resources.json"), "Missing test resource template.")) {
            JsonNode fixtures = JsonMapper.builder().build().readTree(input).get("variables").get("selectionSettings");
            if (fixtures == null || !fixtures.isArray()) {
                throw new IllegalStateException("The resource template must define selectionSettings.");
            }
            List<Map<String, Object>> settings = new ArrayList<>();
            for (JsonNode fixture : fixtures) {
                String key = fixture.get("key").asString();
                String fixtureLabel = fixture.get("label").asString();
                if (key.startsWith(prefix) && selectedLabel.equals(fixtureLabel)) {
                    Map<String, Object> setting = new LinkedHashMap<>();
                    setting.put("key", key);
                    setting.put("label", fixtureLabel.isEmpty() ? null : fixtureLabel);
                    setting.put("value", fixture.get("value").asString());
                    settings.add(setting);
                }
            }
            return settings;
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read selection fixtures from the resource template.", exception);
        }
    }

    private static Select select(String label) {
        return new Select(KEY_PREFIX, label);
    }

    private static Scenario scenario(String name, Map<String, String> expected, Select... selects) {
        return new Scenario(name, List.of(selects), List.of(), expected);
    }

    private static Map<String, String> labeledEntries(String shared) {
        return Map.of("shared", shared, "base-only", "base-only", "override-only", "override-only");
    }

    private static Map<String, String> distinctFilterEntries(String shared) {
        return Map.of("shared", shared, "first-only", "first-only", "second-only", "second-only");
    }

    record Select(String keyFilter, String labelFilter) {
    }

    record Scenario(String name, List<Select> selects, List<String> profiles, Map<String, String> expectedEntries) {

        String[] arguments(String endpoint) {
            String store = "--spring.cloud.azure.appconfiguration.stores[0].";
            List<String> arguments = new ArrayList<>(List.of(
                "--spring.config.import=azureAppConfiguration",
                store + "endpoint=" + endpoint,
                store + "replica-discovery-enabled=false",
                store + "trim-key-prefix[0]=" + FIRST_PREFIX,
                store + "trim-key-prefix[1]=" + SECOND_PREFIX,
                store + "trim-key-prefix[2]=/application/"));
            for (int index = 0; index < selects.size(); index++) {
                Select select = selects.get(index);
                arguments.add(store + "selects[" + index + "].key-filter=" + select.keyFilter());
                if (select.labelFilter() != null) {
                    arguments.add(store + "selects[" + index + "].label-filter=" + select.labelFilter());
                }
            }
            if (!profiles.isEmpty()) {
                arguments.add("--spring.profiles.active=" + String.join(",", profiles));
            }
            return arguments.toArray(String[]::new);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
