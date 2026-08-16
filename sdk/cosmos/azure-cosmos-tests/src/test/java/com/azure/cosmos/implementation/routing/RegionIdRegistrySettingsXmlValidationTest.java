// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates that {@link RegionIdRegistry#CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS}
 * stays in sync with the authoritative regionToIdMapping from Settings.xml.
 *
 * <p>The source of truth is a checked-in copy of Settings.xml at
 * {@code src/test/resources/region-to-id-settings.xml}, fetched from:
 * <a href="https://msdata.visualstudio.com/CosmosDB/_git/CosmosDB?path=/Product/Services/Documents/ImageStore/Storage/SingleServiceMasterServerApplication/ServerServicePackage/Settings.xml">CosmosDB/Settings.xml</a>
 *
 * <p><b>Update workflow:</b> pull the latest Settings.xml from the CosmosDB repo,
 * overwrite {@code region-to-id-settings.xml}, run this test — it will report
 * exactly which regions are missing, extra, or have mismatched IDs.
 */
public class RegionIdRegistrySettingsXmlValidationTest {

    private static final Logger logger = LoggerFactory.getLogger(RegionIdRegistrySettingsXmlValidationTest.class);

    private static final String SETTINGS_XML_RESOURCE = "region-to-id-settings.xml";

    // Extracts the regionIdByRegion JSON object from the XML attribute value
    private static final Pattern REGION_MAPPING_PATTERN =
        Pattern.compile("\"regionIdByRegion\"\\s*:\\s*\\{([^}]+)}");

    @Test(groups = {"unit"})
    public void regionIdRegistryMappingMatchesSettingsXml() throws Exception {

        Map<String, Integer> settingsXmlMapping = parseRegionToIdMappingFromResource();

        Map<String, Integer> sdkMapping = RegionIdRegistry.CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS;

        List<String> errors = new ArrayList<>();

        // Check every Settings.xml entry exists in RegionIdRegistry with the correct ID
        for (Map.Entry<String, Integer> expected : settingsXmlMapping.entrySet()) {
            String region = expected.getKey();
            int expectedId = expected.getValue();

            if (!sdkMapping.containsKey(region)) {
                errors.add(String.format(
                    "MISSING in RegionIdRegistry: '%s' (ID=%d) exists in Settings.xml but not in RegionIdRegistry",
                    region, expectedId));
            } else if (!sdkMapping.get(region).equals(expectedId)) {
                errors.add(String.format(
                    "ID MISMATCH for '%s': Settings.xml has ID=%d, RegionIdRegistry has ID=%d",
                    region, expectedId, sdkMapping.get(region)));
            }
        }

        // Check RegionIdRegistry has no extra entries absent from Settings.xml
        for (Map.Entry<String, Integer> actual : sdkMapping.entrySet()) {
            if (!settingsXmlMapping.containsKey(actual.getKey())) {
                errors.add(String.format(
                    "EXTRA in RegionIdRegistry: '%s' (ID=%d) is not in Settings.xml — stale entry?",
                    actual.getKey(), actual.getValue()));
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("RegionIdRegistry is out of sync with Settings.xml (")
              .append(errors.size()).append(" issue(s)):\n");
            for (String error : errors) {
                sb.append("  - ").append(error).append("\n");
            }
            sb.append("\nFix: update CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS in RegionIdRegistry.java");

            assertThat(errors).as(sb.toString()).isEmpty();
        }

        logger.info("RegionIdRegistry validated against Settings.xml — {} region mappings match", sdkMapping.size());
    }

    /**
     * Parses {@code region-to-id-settings.xml} from test resources and returns
     * the regionIdByRegion mapping as a {@code Map<String, Integer>}.
     */
    private Map<String, Integer> parseRegionToIdMappingFromResource() throws Exception {

        InputStream is = getClass().getClassLoader().getResourceAsStream(SETTINGS_XML_RESOURCE);
        assertThat(is)
            .as("Test resource '%s' not found. Download Settings.xml from the CosmosDB repo "
                + "and place it at src/test/resources/%s", SETTINGS_XML_RESOURCE, SETTINGS_XML_RESOURCE)
            .isNotNull();

        // Scan line-by-line for the regionToIdMapping parameter to avoid loading the
        // entire 400KB XML into memory
        String regionMappingJson = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = REGION_MAPPING_PATTERN.matcher(line);
                if (matcher.find()) {
                    regionMappingJson = "{" + matcher.group(1) + "}";
                    break;
                }
            }
        }

        assertThat(regionMappingJson)
            .as("Could not find regionIdByRegion in %s — is the file a valid copy of Settings.xml?",
                SETTINGS_XML_RESOURCE)
            .isNotNull();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(regionMappingJson);

        Map<String, Integer> regionToId = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            regionToId.put(field.getKey(), field.getValue().asInt());
        }

        logger.info("Parsed {} region mappings from {}", regionToId.size(), SETTINGS_XML_RESOURCE);
        return regionToId;
    }
}
