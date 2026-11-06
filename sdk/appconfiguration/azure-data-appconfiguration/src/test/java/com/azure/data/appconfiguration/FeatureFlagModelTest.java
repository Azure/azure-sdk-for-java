// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration;

import com.azure.core.util.BinaryData;
import com.azure.data.appconfiguration.models.FeatureFilter;
import com.azure.data.appconfiguration.models.FeatureFlag;
import com.azure.data.appconfiguration.models.FeatureFlagAllocation;
import com.azure.data.appconfiguration.models.FeatureFlagConditions;
import com.azure.data.appconfiguration.models.FeatureFlagFields;
import com.azure.data.appconfiguration.models.FeatureFlagTelemetryConfiguration;
import com.azure.data.appconfiguration.models.FeatureFlagVariantDefinition;
import com.azure.data.appconfiguration.models.GroupAllocation;
import com.azure.data.appconfiguration.models.PercentileAllocation;
import com.azure.data.appconfiguration.models.RequirementType;
import com.azure.data.appconfiguration.models.StatusOverride;
import com.azure.data.appconfiguration.models.UserAllocation;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureFlagModelTest {
    @Test
    void roundTripAllFields() {
        FeatureFlag original = new FeatureFlag("checkout", false).setName("checkout-v2")
            .setEnabled(true)
            .setLabel("production")
            .setETag("version-1")
            .setDescription("Checkout experience")
            .setConditions(new FeatureFlagConditions().setRequirementType(RequirementType.ALL)
                .setFilters(Collections.singletonList(
                    new FeatureFilter("Microsoft.Percentage").setParameters(Collections.singletonMap("Value", "50")))))
            .setVariants(Arrays.asList(
                new FeatureFlagVariantDefinition("control").setValue("false")
                    .setContentType("application/json")
                    .setStatusOverride(StatusOverride.DISABLED),
                new FeatureFlagVariantDefinition("treatment").setValue("true")
                    .setContentType("application/json")
                    .setStatusOverride(StatusOverride.ENABLED),
                new FeatureFlagVariantDefinition("unchanged").setStatusOverride(StatusOverride.NONE)))
            .setAllocation(new FeatureFlagAllocation().setDefaultWhenDisabled("control")
                .setDefaultWhenEnabled("treatment")
                .setSeed("checkout-seed")
                .setPercentile(Collections.singletonList(new PercentileAllocation("treatment", 0.5, 75.5)))
                .setUser(Collections.singletonList(new UserAllocation("treatment", Arrays.asList("alice", "bob"))))
                .setGroup(Collections.singletonList(new GroupAllocation("control", Arrays.asList("beta", "staff")))))
            .setTelemetry(new FeatureFlagTelemetryConfiguration(true)
                .setMetadata(Collections.singletonMap("environment", "production")))
            .setTags(Collections.singletonMap("team", "checkout"));

        BinaryData serialized = BinaryData.fromObject(original);
        FeatureFlag actual = serialized.toObject(FeatureFlag.class);

        assertEquals("checkout-v2", actual.getName());
        assertTrue(actual.isEnabled());
        assertEquals("production", actual.getLabel());
        assertEquals("version-1", actual.getETag());
        assertEquals("Checkout experience", actual.getDescription());
        assertEquals(RequirementType.ALL, actual.getConditions().getRequirementType());
        assertEquals("Microsoft.Percentage", actual.getConditions().getFilters().get(0).getName());
        assertEquals(Collections.singletonMap("Value", "50"),
            actual.getConditions().getFilters().get(0).getParameters());
        assertEquals(3, actual.getVariants().size());
        assertEquals("control", actual.getVariants().get(0).getName());
        assertEquals("false", actual.getVariants().get(0).getValue());
        assertEquals("application/json", actual.getVariants().get(0).getContentType());
        assertEquals(StatusOverride.DISABLED, actual.getVariants().get(0).getStatusOverride());
        assertEquals("treatment", actual.getVariants().get(1).getName());
        assertEquals("true", actual.getVariants().get(1).getValue());
        assertEquals(StatusOverride.ENABLED, actual.getVariants().get(1).getStatusOverride());
        assertEquals(StatusOverride.NONE, actual.getVariants().get(2).getStatusOverride());
        assertEquals("control", actual.getAllocation().getDefaultWhenDisabled());
        assertEquals("treatment", actual.getAllocation().getDefaultWhenEnabled());
        assertEquals("checkout-seed", actual.getAllocation().getSeed());
        PercentileAllocation percentile = actual.getAllocation().getPercentile().get(0);
        assertEquals("treatment", percentile.getVariant());
        assertEquals(0.5, percentile.getFrom());
        assertEquals(75.5, percentile.getTo());
        assertEquals("treatment", actual.getAllocation().getUser().get(0).getVariant());
        assertEquals(Arrays.asList("alice", "bob"), actual.getAllocation().getUser().get(0).getUsers());
        assertEquals("control", actual.getAllocation().getGroup().get(0).getVariant());
        assertEquals(Arrays.asList("beta", "staff"), actual.getAllocation().getGroup().get(0).getGroups());
        assertTrue(actual.getTelemetry().isEnabled());
        assertEquals(Collections.singletonMap("environment", "production"), actual.getTelemetry().getMetadata());
        assertEquals(Collections.singletonMap("team", "checkout"), actual.getTags());
        assertNull(actual.getLastModified());

        Map<?, ?> wire = serialized.toObject(Map.class);
        assertEquals("checkout-v2", wire.get("name"));
        assertEquals(true, wire.get("enabled"));
        assertEquals("production", wire.get("label"));
        assertEquals("version-1", wire.get("etag"));
        Map<?, ?> conditions = (Map<?, ?>) wire.get("conditions");
        assertEquals("All", conditions.get("requirement_type"));
        assertTrue(conditions.containsKey("filters"));
        assertFalse(conditions.containsKey("client_filters"));
        assertFalse(wire.containsKey("id"));
        assertFalse(wire.containsKey("key"));
    }

    @Test
    void readsAndRoundTripsMetadata() {
        FeatureFlag flag = BinaryData
            .fromString("{\"name\":\"checkout\",\"enabled\":true,\"label\":\"prod\","
                + "\"last_modified\":\"2026-01-02T03:04:05Z\",\"etag\":\"\\\"version-2\\\"\"}")
            .toObject(FeatureFlag.class);
        assertEquals(OffsetDateTime.parse("2026-01-02T03:04:05Z"), flag.getLastModified());
        assertEquals("\"version-2\"", flag.getETag());

        FeatureFlag actual = BinaryData.fromObject(flag).toObject(FeatureFlag.class);
        assertEquals(flag.getLastModified(), actual.getLastModified());
        assertEquals(flag.getETag(), actual.getETag());
        assertEquals(flag.getName(), actual.getName());
        assertEquals(flag.getLabel(), actual.getLabel());
        assertTrue(actual.isEnabled());
    }

    @Test
    void preservesAbsentAndNullOptionalCollections() {
        FeatureFlag flag = BinaryData.fromObject(new FeatureFlag("minimal", false)).toObject(FeatureFlag.class);
        assertFalse(flag.isEnabled());
        assertNull(flag.getVariants());
        assertNull(flag.getTags());
        assertNull(flag.getConditions());
        assertNull(flag.getAllocation());
        assertNull(flag.getTelemetry());

        String json = "{\"conditions\":{\"filters\":null},\"variants\":null,\"tags\":null,"
            + "\"allocation\":{\"percentile\":null,\"user\":null,\"group\":null},"
            + "\"telemetry\":{\"enabled\":false,\"metadata\":null},\"last_modified\":null}";
        FeatureFlag actual = BinaryData.fromString(json).toObject(FeatureFlag.class);
        assertNull(actual.getConditions().getFilters());
        assertNull(actual.getVariants());
        assertNull(actual.getTags());
        assertNull(actual.getAllocation().getPercentile());
        assertNull(actual.getAllocation().getUser());
        assertNull(actual.getAllocation().getGroup());
        assertNull(actual.getTelemetry().getMetadata());
        assertNull(actual.getLastModified());
        assertNull(BinaryData.fromObject(new FeatureFilter("filter")).toObject(FeatureFilter.class).getParameters());
        assertNull(
            BinaryData.fromObject(new GroupAllocation("variant", null)).toObject(GroupAllocation.class).getGroups());
        assertNull(
            BinaryData.fromObject(new UserAllocation("variant", null)).toObject(UserAllocation.class).getUsers());

        FeatureFlag empty = BinaryData.fromString("{}").toObject(FeatureFlag.class);
        assertNull(empty.getName());
        assertFalse(empty.isEnabled());
        assertNull(empty.getVariants());
        assertNull(empty.getTags());
    }

    @Test
    void preservesEmptyCollections() {
        FeatureFlag flag = new FeatureFlag("empty", true).setVariants(Collections.emptyList())
            .setTags(Collections.emptyMap())
            .setConditions(new FeatureFlagConditions().setFilters(Collections.emptyList()))
            .setAllocation(new FeatureFlagAllocation().setPercentile(Collections.emptyList())
                .setUser(Collections.emptyList())
                .setGroup(Collections.emptyList()));
        FeatureFlag actual = BinaryData.fromObject(flag).toObject(FeatureFlag.class);
        assertEquals(Collections.emptyList(), actual.getVariants());
        assertEquals(Collections.emptyMap(), actual.getTags());
        assertEquals(Collections.emptyList(), actual.getConditions().getFilters());
        assertEquals(Collections.emptyList(), actual.getAllocation().getPercentile());
        assertEquals(Collections.emptyList(), actual.getAllocation().getUser());
        assertEquals(Collections.emptyList(), actual.getAllocation().getGroup());
    }

    @Test
    void readsSelectedFieldsAndSkipsUnknownFields() {
        String json = "{\"unknown\":{\"nested\":[1,2]},\"label\":\"selected\","
            + "\"conditions\":{\"unknown\":[],\"requirement_type\":\"Any\","
            + "\"filters\":[{\"unknown\":{},\"name\":\"filter\",\"parameters\":{\"value\":\"1\"}}]},"
            + "\"variants\":[{\"unknown\":{},\"name\":\"variant\",\"status_override\":\"FutureStatus\"}],"
            + "\"allocation\":{\"unknown\":{},"
            + "\"percentile\":[{\"unknown\":[],\"variant\":\"variant\",\"from\":0,\"to\":100}],"
            + "\"user\":[{\"unknown\":{},\"variant\":\"variant\",\"users\":[\"user\"]}],"
            + "\"group\":[{\"unknown\":{},\"variant\":\"variant\",\"groups\":[\"group\"]}]},"
            + "\"telemetry\":{\"unknown\":{},\"enabled\":true}}";
        FeatureFlag actual = BinaryData.fromString(json).toObject(FeatureFlag.class);
        assertNull(actual.getName());
        assertFalse(actual.isEnabled());
        assertEquals("selected", actual.getLabel());
        assertEquals(RequirementType.ANY, actual.getConditions().getRequirementType());
        assertEquals("filter", actual.getConditions().getFilters().get(0).getName());
        assertEquals("1", actual.getConditions().getFilters().get(0).getParameters().get("value"));
        assertEquals("FutureStatus", actual.getVariants().get(0).getStatusOverride().toString());
        assertEquals(100, actual.getAllocation().getPercentile().get(0).getTo());
        assertEquals(Collections.singletonList("user"), actual.getAllocation().getUser().get(0).getUsers());
        assertEquals(Collections.singletonList("group"), actual.getAllocation().getGroup().get(0).getGroups());
        assertTrue(actual.getTelemetry().isEnabled());

        FeatureFlag partial = BinaryData.fromString("{\"conditions\":{},\"telemetry\":{},\"allocation\":{}}")
            .toObject(FeatureFlag.class);
        assertNull(partial.getConditions().getFilters());
        assertNull(partial.getAllocation().getPercentile());
        assertFalse(partial.getTelemetry().isEnabled());
    }

    @Test
    void validatesNameForUserCreatedFlags() {
        assertThrows(NullPointerException.class, () -> new FeatureFlag(null, true));
        assertThrows(NullPointerException.class, () -> new FeatureFlag("flag", true).setName(null));
    }

    @Test
    void enumsPreserveWireValues() {
        for (RequirementType value : Arrays.asList(RequirementType.ANY, RequirementType.ALL,
            RequirementType.fromString("FutureRequirement"))) {
            FeatureFlagConditions actual = BinaryData.fromObject(new FeatureFlagConditions().setRequirementType(value))
                .toObject(FeatureFlagConditions.class);
            assertEquals(value, actual.getRequirementType());
        }
        for (StatusOverride value : Arrays.asList(StatusOverride.NONE, StatusOverride.ENABLED, StatusOverride.DISABLED,
            StatusOverride.fromString("FutureStatus"))) {
            FeatureFlagVariantDefinition actual
                = BinaryData.fromObject(new FeatureFlagVariantDefinition("variant").setStatusOverride(value))
                    .toObject(FeatureFlagVariantDefinition.class);
            assertEquals(value, actual.getStatusOverride());
        }
        assertEquals(
            Arrays.asList("name", "enabled", "label", "description", "conditions", "variants", "allocation",
                "telemetry", "tags", "last_modified", "etag"),
            Arrays.asList(FeatureFlagFields.NAME.toString(), FeatureFlagFields.ENABLED.toString(),
                FeatureFlagFields.LABEL.toString(), FeatureFlagFields.DESCRIPTION.toString(),
                FeatureFlagFields.CONDITIONS.toString(), FeatureFlagFields.VARIANTS.toString(),
                FeatureFlagFields.ALLOCATION.toString(), FeatureFlagFields.TELEMETRY.toString(),
                FeatureFlagFields.TAGS.toString(), FeatureFlagFields.LAST_MODIFIED.toString(),
                FeatureFlagFields.ETAG.toString()));
        assertEquals("future_field", FeatureFlagFields.fromString("future_field").toString());
    }
}
