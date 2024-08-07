// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.resources.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.resources.models.ManagedByTenant;
import com.azure.resourcemanager.resources.models.SubscriptionPolicies;
import com.azure.resourcemanager.resources.models.SubscriptionState;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Subscription information.
 */
@Fluent
public final class SubscriptionInner implements JsonSerializable<SubscriptionInner> {
    /*
     * The fully qualified ID for the subscription. For example, /subscriptions/8d65815f-a5b6-402f-9298-045155da7d74
     */
    private String id;

    /*
     * The subscription ID.
     */
    private String subscriptionId;

    /*
     * The subscription display name.
     */
    private String displayName;

    /*
     * The subscription tenant ID.
     */
    private String tenantId;

    /*
     * The subscription state. Possible values are Enabled, Warned, PastDue, Disabled, and Deleted.
     */
    private SubscriptionState state;

    /*
     * The subscription policies.
     */
    private SubscriptionPolicies subscriptionPolicies;

    /*
     * The authorization source of the request. Valid values are one or more combinations of Legacy, RoleBased,
     * Bypassed, Direct and Management. For example, 'Legacy, RoleBased'.
     */
    private String authorizationSource;

    /*
     * An array containing the tenants managing the subscription.
     */
    private List<ManagedByTenant> managedByTenants;

    /*
     * The tags attached to the subscription.
     */
    private Map<String, String> tags;

    /**
     * Creates an instance of SubscriptionInner class.
     */
    public SubscriptionInner() {
    }

    /**
     * Get the id property: The fully qualified ID for the subscription. For example,
     * /subscriptions/8d65815f-a5b6-402f-9298-045155da7d74.
     * 
     * @return the id value.
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the subscriptionId property: The subscription ID.
     * 
     * @return the subscriptionId value.
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Get the displayName property: The subscription display name.
     * 
     * @return the displayName value.
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Get the tenantId property: The subscription tenant ID.
     * 
     * @return the tenantId value.
     */
    public String tenantId() {
        return this.tenantId;
    }

    /**
     * Get the state property: The subscription state. Possible values are Enabled, Warned, PastDue, Disabled, and
     * Deleted.
     * 
     * @return the state value.
     */
    public SubscriptionState state() {
        return this.state;
    }

    /**
     * Get the subscriptionPolicies property: The subscription policies.
     * 
     * @return the subscriptionPolicies value.
     */
    public SubscriptionPolicies subscriptionPolicies() {
        return this.subscriptionPolicies;
    }

    /**
     * Set the subscriptionPolicies property: The subscription policies.
     * 
     * @param subscriptionPolicies the subscriptionPolicies value to set.
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withSubscriptionPolicies(SubscriptionPolicies subscriptionPolicies) {
        this.subscriptionPolicies = subscriptionPolicies;
        return this;
    }

    /**
     * Get the authorizationSource property: The authorization source of the request. Valid values are one or more
     * combinations of Legacy, RoleBased, Bypassed, Direct and Management. For example, 'Legacy, RoleBased'.
     * 
     * @return the authorizationSource value.
     */
    public String authorizationSource() {
        return this.authorizationSource;
    }

    /**
     * Set the authorizationSource property: The authorization source of the request. Valid values are one or more
     * combinations of Legacy, RoleBased, Bypassed, Direct and Management. For example, 'Legacy, RoleBased'.
     * 
     * @param authorizationSource the authorizationSource value to set.
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withAuthorizationSource(String authorizationSource) {
        this.authorizationSource = authorizationSource;
        return this;
    }

    /**
     * Get the managedByTenants property: An array containing the tenants managing the subscription.
     * 
     * @return the managedByTenants value.
     */
    public List<ManagedByTenant> managedByTenants() {
        return this.managedByTenants;
    }

    /**
     * Set the managedByTenants property: An array containing the tenants managing the subscription.
     * 
     * @param managedByTenants the managedByTenants value to set.
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withManagedByTenants(List<ManagedByTenant> managedByTenants) {
        this.managedByTenants = managedByTenants;
        return this;
    }

    /**
     * Get the tags property: The tags attached to the subscription.
     * 
     * @return the tags value.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags property: The tags attached to the subscription.
     * 
     * @param tags the tags value to set.
     * @return the SubscriptionInner object itself.
     */
    public SubscriptionInner withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (subscriptionPolicies() != null) {
            subscriptionPolicies().validate();
        }
        if (managedByTenants() != null) {
            managedByTenants().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("subscriptionPolicies", this.subscriptionPolicies);
        jsonWriter.writeStringField("authorizationSource", this.authorizationSource);
        jsonWriter.writeArrayField("managedByTenants", this.managedByTenants,
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeMapField("tags", this.tags, (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SubscriptionInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SubscriptionInner if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the SubscriptionInner.
     */
    public static SubscriptionInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SubscriptionInner deserializedSubscriptionInner = new SubscriptionInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedSubscriptionInner.id = reader.getString();
                } else if ("subscriptionId".equals(fieldName)) {
                    deserializedSubscriptionInner.subscriptionId = reader.getString();
                } else if ("displayName".equals(fieldName)) {
                    deserializedSubscriptionInner.displayName = reader.getString();
                } else if ("tenantId".equals(fieldName)) {
                    deserializedSubscriptionInner.tenantId = reader.getString();
                } else if ("state".equals(fieldName)) {
                    deserializedSubscriptionInner.state = SubscriptionState.fromString(reader.getString());
                } else if ("subscriptionPolicies".equals(fieldName)) {
                    deserializedSubscriptionInner.subscriptionPolicies = SubscriptionPolicies.fromJson(reader);
                } else if ("authorizationSource".equals(fieldName)) {
                    deserializedSubscriptionInner.authorizationSource = reader.getString();
                } else if ("managedByTenants".equals(fieldName)) {
                    List<ManagedByTenant> managedByTenants
                        = reader.readArray(reader1 -> ManagedByTenant.fromJson(reader1));
                    deserializedSubscriptionInner.managedByTenants = managedByTenants;
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedSubscriptionInner.tags = tags;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSubscriptionInner;
        });
    }
}
