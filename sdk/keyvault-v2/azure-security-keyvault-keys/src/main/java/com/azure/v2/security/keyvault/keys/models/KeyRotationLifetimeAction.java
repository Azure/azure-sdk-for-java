// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.models;

import com.azure.v2.security.keyvault.keys.implementation.KeyRotationLifetimeActionHelper;
import com.azure.v2.security.keyvault.keys.implementation.models.LifetimeActionsTrigger;
import com.azure.v2.security.keyvault.keys.implementation.models.LifetimeActionsType;
import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;

/**
 * Represents an action that will be performed by Key Vault over the lifetime of a key.
 */
@Metadata(properties = { MetadataProperties.FLUENT })
public final class KeyRotationLifetimeAction implements JsonSerializable<KeyRotationLifetimeAction> {
    static {
        KeyRotationLifetimeActionHelper
            .setAccessor(new KeyRotationLifetimeActionHelper.KeyRotationLifetimeActionAccessor() {
                @Override
                public KeyRotationLifetimeAction createLifetimeAction(LifetimeActionsTrigger trigger,
                    LifetimeActionsType actionsType) {
                    return new KeyRotationLifetimeAction(trigger, actionsType);
                }

                @Override
                public LifetimeActionsTrigger getTrigger(KeyRotationLifetimeAction lifetimeAction) {
                    return lifetimeAction.trigger;
                }

                @Override
                public LifetimeActionsType getActionType(KeyRotationLifetimeAction lifetimeAction) {
                    return lifetimeAction.actionType;
                }
            });
    }

    private final LifetimeActionsTrigger trigger;

    private final LifetimeActionsType actionType;

    /**
     * Creates a {@link KeyRotationLifetimeAction}.
     *
     * @param action The {@link KeyRotationPolicyAction policy action}.
     */
    public KeyRotationLifetimeAction(KeyRotationPolicyAction action) {
        this.actionType = new LifetimeActionsType().setType(action);
        this.trigger = new LifetimeActionsTrigger();
    }

    private KeyRotationLifetimeAction(LifetimeActionsTrigger trigger, LifetimeActionsType actionsType) {
        this.trigger = trigger;
        this.actionType = actionsType;
    }

    /**
     * Get the {@link KeyRotationPolicyAction policy action}.
     *
     * @return The {@link KeyRotationPolicyAction policy action}.
     */
    public KeyRotationPolicyAction getAction() {
        return this.actionType.getType();
    }

    /**
     * Get the time after creation to attempt to rotate in ISO 8601 duration format. For example, 90 days would be
     * "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @return The time after creation to attempt to rotate in ISO duration format.
     */
    public String getTimeAfterCreate() {
        return this.trigger.getTimeAfterCreate();
    }

    /**
     * Set the time after creation to attempt to rotate in ISO 8601 duration format. For example, 90 days would be
     * "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @param timeAfterCreate The time after creation to attempt to rotate in ISO duration format.
     *
     * @return The updated {@link KeyRotationLifetimeAction} object.
     */
    public KeyRotationLifetimeAction setTimeAfterCreate(String timeAfterCreate) {
        this.trigger.setTimeAfterCreate(timeAfterCreate);

        return this;
    }

    /**
     * Get the time before expiry to attempt to rotate or notify in ISO 8601 duration format. For example, 90 days would
     * be "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @return The time before expiry to attempt to rotate or notify in ISO duration format.
     */
    public String getTimeBeforeExpiry() {
        return this.trigger.getTimeBeforeExpiry();
    }

    /**
     * Set the time before expiry to attempt to rotate or notify in ISO 8601 duration format. For example, 90 days would
     * be "P90D", 3 months would be "P3M" and 1 year and 10 days would be "P1Y10D". See
     * <a href="https://wikipedia.org/wiki/ISO_8601#Durations">Wikipedia</a> for more information on ISO 8601 durations.
     *
     * @param timeBeforeExpiry The time before expiry to attempt to rotate or notify in ISO duration format.
     *
     * @return The updated {@link KeyRotationLifetimeAction} object.
     */
    public KeyRotationLifetimeAction setTimeBeforeExpiry(String timeBeforeExpiry) {
        this.trigger.setTimeBeforeExpiry(timeBeforeExpiry);

        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeJsonField("trigger", trigger)
            .writeJsonField("action", actionType)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link KeyReleasePolicy}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link KeyReleasePolicy} that the JSON stream represented, may return null.
     * @throws IOException If a {@link KeyReleasePolicy} fails to be read from the {@code jsonReader}.
     */
    public static KeyRotationLifetimeAction fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            LifetimeActionsTrigger trigger = null;
            LifetimeActionsType actionType = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("trigger".equals(fieldName)) {
                    trigger = LifetimeActionsTrigger.fromJson(reader);
                } else if ("action".equals(fieldName)) {
                    actionType = LifetimeActionsType.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return new KeyRotationLifetimeAction(trigger, actionType);
        });
    }
}
