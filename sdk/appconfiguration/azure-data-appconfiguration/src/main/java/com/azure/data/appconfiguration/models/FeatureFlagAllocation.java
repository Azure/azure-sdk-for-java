// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * The rules for allocating feature flag variants.
 */
@Fluent
public final class FeatureFlagAllocation implements JsonSerializable<FeatureFlagAllocation> {
    private String defaultWhenDisabled;
    private String defaultWhenEnabled;
    private List<PercentileAllocation> percentile;
    private List<UserAllocation> user;
    private List<GroupAllocation> group;
    private String seed;

    /**
     * Creates feature flag allocation rules.
     */
    public FeatureFlagAllocation() {
    }

    /**
     * Gets the default variant when the feature flag is disabled.
     * @return The default variant name.
     */
    public String getDefaultWhenDisabled() {
        return defaultWhenDisabled;
    }

    /**
     * Sets the default variant when the feature flag is disabled.
     * @param defaultWhenDisabled The default variant name.
     * @return The updated allocation.
     */
    public FeatureFlagAllocation setDefaultWhenDisabled(String defaultWhenDisabled) {
        this.defaultWhenDisabled = defaultWhenDisabled;
        return this;
    }

    /**
     * Gets the default variant when the feature flag is enabled.
     * @return The default variant name.
     */
    public String getDefaultWhenEnabled() {
        return defaultWhenEnabled;
    }

    /**
     * Sets the default variant when the feature flag is enabled.
     * @param defaultWhenEnabled The default variant name.
     * @return The updated allocation.
     */
    public FeatureFlagAllocation setDefaultWhenEnabled(String defaultWhenEnabled) {
        this.defaultWhenEnabled = defaultWhenEnabled;
        return this;
    }

    /**
     * Gets the percentile allocations.
     * @return The percentile allocations, or null if not specified.
     */
    public List<PercentileAllocation> getPercentile() {
        return percentile;
    }

    /**
     * Sets the percentile allocations.
     * @param percentile The percentile allocations.
     * @return The updated allocation.
     */
    public FeatureFlagAllocation setPercentile(List<PercentileAllocation> percentile) {
        this.percentile = percentile;
        return this;
    }

    /**
     * Gets the user allocations.
     * @return The user allocations, or null if not specified.
     */
    public List<UserAllocation> getUser() {
        return user;
    }

    /**
     * Sets the user allocations.
     * @param user The user allocations.
     * @return The updated allocation.
     */
    public FeatureFlagAllocation setUser(List<UserAllocation> user) {
        this.user = user;
        return this;
    }

    /**
     * Gets the group allocations.
     * @return The group allocations, or null if not specified.
     */
    public List<GroupAllocation> getGroup() {
        return group;
    }

    /**
     * Sets the group allocations.
     * @param group The group allocations.
     * @return The updated allocation.
     */
    public FeatureFlagAllocation setGroup(List<GroupAllocation> group) {
        this.group = group;
        return this;
    }

    /**
     * Gets the seed used for percentile allocation.
     * @return The seed.
     */
    public String getSeed() {
        return seed;
    }

    /**
     * Sets the seed used for percentile allocation.
     * @param seed The seed.
     * @return The updated allocation.
     */
    public FeatureFlagAllocation setSeed(String seed) {
        this.seed = seed;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("default_when_disabled", defaultWhenDisabled)
            .writeStringField("default_when_enabled", defaultWhenEnabled)
            .writeArrayField("percentile", percentile, (writer, value) -> writer.writeJson(value))
            .writeArrayField("user", user, (writer, value) -> writer.writeJson(value))
            .writeArrayField("group", group, (writer, value) -> writer.writeJson(value))
            .writeStringField("seed", seed)
            .writeEndObject();
    }

    /**
     * Reads feature flag allocation rules.
     * @param jsonReader The JSON reader.
     * @return The allocation, or null if the JSON value is null.
     * @throws IOException If reading fails.
     */
    public static FeatureFlagAllocation fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FeatureFlagAllocation result = new FeatureFlagAllocation();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("default_when_disabled".equals(fieldName)) {
                    result.defaultWhenDisabled = reader.getString();
                } else if ("default_when_enabled".equals(fieldName)) {
                    result.defaultWhenEnabled = reader.getString();
                } else if ("percentile".equals(fieldName)) {
                    result.percentile = reader.readArray(PercentileAllocation::fromJson);
                } else if ("user".equals(fieldName)) {
                    result.user = reader.readArray(UserAllocation::fromJson);
                } else if ("group".equals(fieldName)) {
                    result.group = reader.readArray(GroupAllocation::fromJson);
                } else if ("seed".equals(fieldName)) {
                    result.seed = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return result;
        });
    }
}
