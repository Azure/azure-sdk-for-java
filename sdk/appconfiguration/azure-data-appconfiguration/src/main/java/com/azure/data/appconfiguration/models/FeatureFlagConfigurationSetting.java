// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.implementation.Conditions;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.azure.data.appconfiguration.implementation.ConfigurationSettingDeserializationHelper.readConditions;
import static com.azure.data.appconfiguration.implementation.Utility.CLIENT_FILTERS;
import static com.azure.data.appconfiguration.implementation.Utility.CONDITIONS;
import static com.azure.data.appconfiguration.implementation.Utility.DESCRIPTION;
import static com.azure.data.appconfiguration.implementation.Utility.DISPLAY_NAME;
import static com.azure.data.appconfiguration.implementation.Utility.ENABLED;
import static com.azure.data.appconfiguration.implementation.Utility.ID;
import static com.azure.data.appconfiguration.implementation.Utility.NAME;
import static com.azure.data.appconfiguration.implementation.Utility.PARAMETERS;

/**
 * {@link FeatureFlagConfigurationSetting} allows you to customize your own feature flags to dynamically administer a
 * feature's lifecycle. Feature flags can be used to enable or disable features.
 */
public final class FeatureFlagConfigurationSetting extends ConfigurationSetting {
    private static final ClientLogger LOGGER = new ClientLogger(FeatureFlagConfigurationSetting.class);
    private static final String FEATURE_FLAG_CONTENT_TYPE = "application/vnd.microsoft.appconfig.ff+json;charset=utf-8";

    /**
     * A prefix is used to construct a feature flag configuration setting's key.
     */
    public static final String KEY_PREFIX = ".appconfig.featureflag/";
    private String featureId;
    private boolean isEnabled;
    private String description;
    private String displayName;
    private List<FeatureFlagFilter> clientFilters;

    // The flag to indicate if the 'value' field is valid. It is a temporary field to store the flag.
    // If the 'value' field is not valid, we will throw an exception when user try to access the strongly-typed
    // properties.
    private boolean isValidFeatureFlagValue;

    // This used to store the parsed properties from the 'value' field. Given initial capacity is 5, it is enough for
    // current json schema. It should be equal to the number of properties defined in the swagger schema at first level.
    private final Map<String, Object> parsedProperties = new LinkedHashMap<>(5);

    // The required properties defined in the swagger schema.
    private final List<String> requiredJsonProperties = Arrays.asList(ID, ENABLED, CONDITIONS);

    // Swagger schema defined properties at first level of FeatureFlagConfigurationSetting.
    private final List<String> requiredOrOptionalJsonProperties =
        Arrays.asList(ID, DESCRIPTION, DISPLAY_NAME, ENABLED, CONDITIONS);

    /**
     * The constructor for a feature flag configuration setting.
     *
     * @param featureId A feature flag identification value that used to construct in setting's key. The key of setting
     *   is {@code KEY_PREFIX} concatenate {@code featureId}.
     * @param isEnabled A boolean value to turn on/off the feature flag setting.
     */
    public FeatureFlagConfigurationSetting(String featureId, boolean isEnabled) {
        isValidFeatureFlagValue = true;

        this.featureId = featureId;
        this.isEnabled = isEnabled;
        super.setKey(KEY_PREFIX + featureId);
        super.setContentType(FEATURE_FLAG_CONTENT_TYPE);
    }

    @Override
    public String getValue() {
        // Lazily update: Update 'value' by all latest property values when this getValue() method is called.
        String newValue = null;
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final JsonWriter writer = JsonProviders.createWriter(outputStream);

            final Set<String> knownProperties = new LinkedHashSet<>(requiredOrOptionalJsonProperties);

            writer.writeStartObject();
            // If 'value' has value, and it is a valid JSON, we need to parse it and write it back.
            for (Map.Entry<String, Object> entry : parsedProperties.entrySet()) {
                final String name = entry.getKey();
                final Object jsonValue = entry.getValue();
                try {
                    // Try to write the known property. If it is a known property, we need to remove it from the
                    // temporary 'knownProperties' bag.
                    if (tryWriteKnownProperty(name, jsonValue, writer, true)) {
                        knownProperties.remove(name);
                    } else {
                        // Unknown extension property. We need to keep it.
                        writer.writeUntypedField(name, jsonValue);
                    }
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(e));
                }
            }
            // Remaining known properties we are not processed yet after 'parsedProperties'.
            for (final String propertyName : knownProperties) {
                tryWriteKnownProperty(propertyName, null, writer, false);
            }
            writer.writeEndObject();

            writer.flush();
            newValue = outputStream.toString(StandardCharsets.UTF_8.name());
            outputStream.close();
        } catch (IOException exception) {
            LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Can't parse Feature Flag configuration setting value.", exception));
        }

        super.setValue(newValue);
        return newValue;
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
        if (key != null && !key.startsWith(KEY_PREFIX)) {
            super.setKey(KEY_PREFIX + key);
        } else {
            super.setKey(key);
        }

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
        tryParseValue(value);
        isValidFeatureFlagValue = true;
        super.setValue(value);
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
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public String getFeatureId() {
        checkValid();
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
        checkValid();
        this.featureId = featureId;
        super.setKey(KEY_PREFIX + featureId);
        return this;
    }

    /**
     * Get the boolean indicator to show if the setting is turn on or off.
     *
     * @return the boolean indicator to show if the setting is turn on or off.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public boolean isEnabled() {
        checkValid();
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
        checkValid();
        this.isEnabled = isEnabled;
        return this;
    }

    /**
     * Get the description of this configuration setting.
     *
     * @return the description of this configuration setting.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public String getDescription() {
        checkValid();
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
        checkValid();
        this.description = description;
        return this;
    }

    /**
     * Get the display name of this configuration setting.
     *
     * @return the display name of this configuration setting.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public String getDisplayName() {
        checkValid();
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
        checkValid();
        this.displayName = displayName;
        return this;
    }

    /**
     * Gets the feature flag filters of this configuration setting.
     *
     * @return the feature flag filters of this configuration setting.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public List<FeatureFlagFilter> getClientFilters() {
        checkValid();
        if (clientFilters == null) {
            clientFilters = new ArrayList<>();
        }
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
        checkValid();
        this.clientFilters = clientFilters;
        return this;
    }

    /**
     * Add a feature flag filter to this configuration setting.
     *
     * @param clientFilter a feature flag filter to add to this configuration setting.
     *
     * @return The updated {@link FeatureFlagConfigurationSetting} object.
     * @throws IllegalArgumentException if the setting's {@code value} is an invalid JSON format.
     */
    public FeatureFlagConfigurationSetting addClientFilter(FeatureFlagFilter clientFilter) {
        checkValid();
        if (clientFilters == null) {
            clientFilters = new ArrayList<>();
        }
        clientFilters.add(clientFilter);
        return this;
    }

    private void checkValid() {
        if (!isValidFeatureFlagValue) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The content of the " + super.getValue()
                + " property do not represent a valid feature flag configuration setting."));
        }
    }

    // Try to write the known property. If it is a known property, return true. Otherwise, return false.
    private boolean tryWriteKnownProperty(String propertyName, Object propertyValue, JsonWriter writer,
                                          boolean includeOptionalWhenNull) throws IOException {
        switch (propertyName) {
            case ID:
                writer.writeStringField(ID, featureId);
                break;
            case DESCRIPTION:
                if (includeOptionalWhenNull || description != null) {
                    writer.writeStringField(DESCRIPTION, description);
                }
                break;
            case DISPLAY_NAME:
                if (includeOptionalWhenNull || displayName != null) {
                    writer.writeStringField(DISPLAY_NAME, displayName);
                }
                break;
            case ENABLED:
                writer.writeBooleanField(ENABLED, isEnabled);
                break;
            case CONDITIONS:
                tryWriteConditions(propertyValue, writer);
                break;
            default:
                return false;
        }
        return true;
    }

    // Helper method: try to write the 'conditions' property.
    private void tryWriteConditions(Object propertyValue, JsonWriter writer) throws IOException {
        writer.writeStartObject(CONDITIONS);

        if (propertyValue != null && propertyValue instanceof Conditions) {
            Conditions propertyValueClone = (Conditions) propertyValue;
            for (Map.Entry<String, Object> entry : propertyValueClone.getUnknownConditions().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                writer.writeUntypedField(key, value);
            }
        }

        writer.writeArrayField(CLIENT_FILTERS, this.clientFilters, (jsonWriter, filter) -> {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField(NAME, filter.getName());
            jsonWriter.writeMapField(PARAMETERS, filter.getParameters(), JsonWriter::writeUntyped);
            jsonWriter.writeEndObject(); // each filter object
        });

        writer.writeEndObject();
    }

    // Given JSON string value, try to parse it and store the parsed properties to the 'parsedProperties' field.
    // If the parsing is successful, updates the strongly-type property and preserves the unknown properties to
    // 'parsedProperties' which we will use later in getValue() to get the unknown properties.
    // Otherwise, set the flag variable 'isValidFeatureFlagValue' = false and throw an exception.
    private void tryParseValue(String value) {
        parsedProperties.clear();

        try (JsonReader jsonReader = JsonProviders.createReader(value)) {
            jsonReader.readObject(reader -> {
                final Set<String> requiredPropertiesCopy = new LinkedHashSet<>(requiredJsonProperties);
                String featureIdCopy = this.featureId;
                String descriptionCopy = this.description;
                String displayNameCopy = this.displayName;
                boolean isEnabledCopy = this.isEnabled;
                List<FeatureFlagFilter> featureFlagFiltersCopy = this.clientFilters;

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    final String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if (ID.equals(fieldName)) {
                        final String id = reader.getString();
                        featureIdCopy = id;
                        parsedProperties.put(ID, id);
                    } else if (DESCRIPTION.equals(fieldName)) {
                        final String description = reader.getString();
                        descriptionCopy = description;
                        parsedProperties.put(DESCRIPTION, description);
                    } else if (DISPLAY_NAME.equals(fieldName)) {
                        final String displayName = reader.getString();
                        displayNameCopy = displayName;
                        parsedProperties.put(DISPLAY_NAME, displayName);
                    } else if (ENABLED.equals(fieldName)) {
                        final boolean isEnabled = reader.getBoolean();
                        isEnabledCopy = isEnabled;
                        parsedProperties.put(ENABLED, isEnabled);
                    } else if (CONDITIONS.equals(fieldName)) {
                        final Conditions conditions = readConditions(reader);
                        if (conditions != null) {
                            List<FeatureFlagFilter> featureFlagFilters = conditions.getFeatureFlagFilters();
                            featureFlagFiltersCopy = featureFlagFilters;
                            parsedProperties.put(CONDITIONS, conditions);
                        }
                    } else {
                        // The extension property is possible, we should not skip it.
                        parsedProperties.put(fieldName, reader.readUntyped());
                    }
                    requiredPropertiesCopy.remove(fieldName);
                }

                this.featureId = featureIdCopy;
                this.description = descriptionCopy;
                this.displayName = displayNameCopy;
                this.isEnabled = isEnabledCopy;
                this.clientFilters = featureFlagFiltersCopy;

                return requiredPropertiesCopy.isEmpty();
            });
        } catch (IOException e) {
            isValidFeatureFlagValue = false;
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(e));
        }
    }
}
