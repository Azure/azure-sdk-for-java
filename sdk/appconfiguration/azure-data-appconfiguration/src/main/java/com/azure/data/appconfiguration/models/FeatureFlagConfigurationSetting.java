// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.util.logging.ClientLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonDeserializer.readFeatureFlagConfigurationSettingValue;
import static com.azure.data.appconfiguration.implementation.ConfigurationSettingJsonSerializer.writeFeatureFlagConfigurationSetting;

/**
 * {@link FeatureFlagConfigurationSetting} allows you to customize your own feature flags to dynamically administer a
 * feature's lifecycle. Feature flags can be used to enable or disable features.
 */
public final class FeatureFlagConfigurationSetting extends ConfigurationSetting {
    private static final ClientLogger LOGGER = new ClientLogger(FeatureFlagConfigurationSetting.class);
    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";

    private String featureId;
    private boolean isEnabled;
    private String description;
    private String displayName;
    private List<FeatureFlagFilter> clientFilters;

    /**
     * A prefix is used to construct a feature flag configuration setting's key.
     */
    public static final String KEY_PREFIX = ".appconfig.featureflag/";

    /**
     * The constructor for a feature flag configuration setting.
     *
     * @param featureId A feature flag identification value that used to construct in setting's key. The key of setting
     *   is {@code KEY_PREFIX} concatenate {@code featureId}.
     * @param isEnabled A boolean value to turn on/off the feature flag setting.
     */
    public FeatureFlagConfigurationSetting(String featureId, boolean isEnabled) {
        this.featureId = featureId;
        this.isEnabled = isEnabled;
        super.setKey(KEY_PREFIX + featureId);
        super.setContentType(FEATURE_FLAG_CONTENT_TYPE);
        clientFilters = new ArrayList<>();
    }

    /**
     * Sets the key of this setting.
     *
     * @param key The key to associate with this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     */
    @Override
    public FeatureFlagConfigurationSetting setKey(String key) {
        super.setKey(key);
        return this;
    }

    /**
     * Sets the value of this setting.
     *
     * @param value The value to associate with this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    @Override
    public FeatureFlagConfigurationSetting setValue(String value) {
        super.setValue(value);
        // update strongly-typed properties.
        final FeatureFlagConfigurationSetting updatedSetting = readFeatureFlagConfigurationSettingValue(value);
        this.featureId = updatedSetting.getFeatureId();
        this.description = updatedSetting.getDescription();
        this.isEnabled = updatedSetting.isEnabled();
        this.displayName = updatedSetting.getDisplayName();
        if (updatedSetting.getClientFilters() != null) {
            this.clientFilters = StreamSupport.stream(updatedSetting.getClientFilters().spliterator(), false)
                                     .collect(Collectors.toList());
        } else {
            this.clientFilters = null;
        }

        return this;
    }

    /**
     * Sets the label of this configuration setting. {@link #NO_LABEL} is the default label used when this value is not
     * set.
     *
     * @param label The label of this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     */
    @Override
    public FeatureFlagConfigurationSetting setLabel(String label) {
        super.setLabel(label);
        return this;
    }

    /**
     * Sets the content type. By default, the content type is null.
     *
     * @param contentType The content type of this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     */
    @Override
    public FeatureFlagConfigurationSetting setContentType(String contentType) {
        super.setContentType(contentType);
        return this;
    }

    /**
     * Sets the ETag for this configuration setting.
     *
     * @param etag The ETag for the configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     */
    @Override
    public FeatureFlagConfigurationSetting setETag(String etag) {
        super.setETag(etag);
        return this;
    }

    /**
     * Sets the tags for this configuration setting.
     *
     * @param tags The tags to add to this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     */
    @Override
    public FeatureFlagConfigurationSetting setTags(Map<String, String> tags) {
        super.setTags(tags);
        return this;
    }

    /**
     * Get the feature ID of this configuration setting.
     *
     * @return the feature ID of this configuration setting.
     */
    public String getFeatureId() {
        return featureId;
    }

    /**
     * Set the feature ID of this configuration setting.
     *
     * @param featureId the feature ID of this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public FeatureFlagConfigurationSetting setFeatureId(String featureId) {
        this.featureId = featureId;
        super.setKey(KEY_PREFIX + featureId);
        updateSettingValue();
        return this;
    }

    /**
     * Get the boolean indicator to show if the setting is turn on or off.
     *
     * @return the boolean indicator to show if the setting is turn on or off.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Set the boolean indicator to show if the setting is turn on or off.
     *
     * @param isEnabled the boolean indicator to show if the setting is turn on or off.

     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public FeatureFlagConfigurationSetting setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        updateSettingValue();
        return this;
    }

    /**
     * Get the description of this configuration setting.
     *
     * @return the description of this configuration setting.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of this configuration setting.
     *
     * @param description the description of this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public FeatureFlagConfigurationSetting setDescription(String description) {
        this.description = description;
        updateSettingValue();
        return this;
    }

    /**
     * Get the display name of this configuration setting.
     *
     * @return the display name of this configuration setting.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the display name of this configuration setting.
     *
     * @param displayName the display name of this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public FeatureFlagConfigurationSetting setDisplayName(String displayName) {
        this.displayName = displayName;
        updateSettingValue();
        return this;
    }

    /**
     * Gets the feature flag filters of this configuration setting.
     *
     * @return the feature flag filters of this configuration setting.
     */
    public List<FeatureFlagFilter> getClientFilters() {
        return clientFilters;
    }

    /**
     * Sets the feature flag filters of this configuration setting.
     *
     * @param clientFilters the feature flag filters of this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public FeatureFlagConfigurationSetting setClientFilters(List<FeatureFlagFilter> clientFilters) {
        this.clientFilters = clientFilters;
        updateSettingValue();
        return this;
    }

    /**
     * Add a feature flag filter to this configuration setting.
     *
     * @param clientFilter a feature flag filter to add to this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     */
    public FeatureFlagConfigurationSetting addClientFilter(FeatureFlagFilter clientFilter) {
        clientFilters.add(clientFilter);
        updateSettingValue();
        return this;
    }

    private void updateSettingValue() {
        try {
            super.setValue(writeFeatureFlagConfigurationSetting(this));
        } catch (IOException exception) {
            LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Can't parse Feature Flag configuration setting value.", exception));
        }
    }
}
