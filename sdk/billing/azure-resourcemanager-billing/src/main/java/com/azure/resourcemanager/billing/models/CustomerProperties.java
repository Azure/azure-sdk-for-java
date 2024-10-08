// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A partner's customer.
 */
@Fluent
public final class CustomerProperties implements JsonSerializable<CustomerProperties> {
    /*
     * The name of the billing profile.
     */
    private String billingProfileDisplayName;

    /*
     * The fully qualified ID that uniquely identifies a billing profile.
     */
    private String billingProfileId;

    /*
     * The name of the customer.
     */
    private String displayName;

    /*
     * The system generated unique identifier for a customer.
     */
    private String systemId;

    /*
     * Identifies the status of an customer. This is an upcoming property that will be populated in the future.
     */
    private CustomerStatus status;

    /*
     * Azure plans enabled for the customer.
     */
    private List<AzurePlan> enabledAzurePlans;

    /*
     * The list of resellers for which an Azure plan is enabled for the customer.
     */
    private List<Reseller> resellers;

    /*
     * Dictionary of metadata associated with the resource. Maximum key/value length supported of 256 characters.
     * Keys/value should not empty value nor null. Keys can not contain < > % & \ ? /
     */
    private Map<String, String> tags;

    /**
     * Creates an instance of CustomerProperties class.
     */
    public CustomerProperties() {
    }

    /**
     * Get the billingProfileDisplayName property: The name of the billing profile.
     * 
     * @return the billingProfileDisplayName value.
     */
    public String billingProfileDisplayName() {
        return this.billingProfileDisplayName;
    }

    /**
     * Get the billingProfileId property: The fully qualified ID that uniquely identifies a billing profile.
     * 
     * @return the billingProfileId value.
     */
    public String billingProfileId() {
        return this.billingProfileId;
    }

    /**
     * Get the displayName property: The name of the customer.
     * 
     * @return the displayName value.
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Get the systemId property: The system generated unique identifier for a customer.
     * 
     * @return the systemId value.
     */
    public String systemId() {
        return this.systemId;
    }

    /**
     * Get the status property: Identifies the status of an customer. This is an upcoming property that will be
     * populated in the future.
     * 
     * @return the status value.
     */
    public CustomerStatus status() {
        return this.status;
    }

    /**
     * Get the enabledAzurePlans property: Azure plans enabled for the customer.
     * 
     * @return the enabledAzurePlans value.
     */
    public List<AzurePlan> enabledAzurePlans() {
        return this.enabledAzurePlans;
    }

    /**
     * Set the enabledAzurePlans property: Azure plans enabled for the customer.
     * 
     * @param enabledAzurePlans the enabledAzurePlans value to set.
     * @return the CustomerProperties object itself.
     */
    public CustomerProperties withEnabledAzurePlans(List<AzurePlan> enabledAzurePlans) {
        this.enabledAzurePlans = enabledAzurePlans;
        return this;
    }

    /**
     * Get the resellers property: The list of resellers for which an Azure plan is enabled for the customer.
     * 
     * @return the resellers value.
     */
    public List<Reseller> resellers() {
        return this.resellers;
    }

    /**
     * Set the resellers property: The list of resellers for which an Azure plan is enabled for the customer.
     * 
     * @param resellers the resellers value to set.
     * @return the CustomerProperties object itself.
     */
    public CustomerProperties withResellers(List<Reseller> resellers) {
        this.resellers = resellers;
        return this;
    }

    /**
     * Get the tags property: Dictionary of metadata associated with the resource. Maximum key/value length supported of
     * 256 characters. Keys/value should not empty value nor null. Keys can not contain &lt; &gt; % &amp; \ ? /.
     * 
     * @return the tags value.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags property: Dictionary of metadata associated with the resource. Maximum key/value length supported of
     * 256 characters. Keys/value should not empty value nor null. Keys can not contain &lt; &gt; % &amp; \ ? /.
     * 
     * @param tags the tags value to set.
     * @return the CustomerProperties object itself.
     */
    public CustomerProperties withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (enabledAzurePlans() != null) {
            enabledAzurePlans().forEach(e -> e.validate());
        }
        if (resellers() != null) {
            resellers().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("enabledAzurePlans", this.enabledAzurePlans,
            (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("resellers", this.resellers, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeMapField("tags", this.tags, (writer, element) -> writer.writeString(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CustomerProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of CustomerProperties if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the CustomerProperties.
     */
    public static CustomerProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CustomerProperties deserializedCustomerProperties = new CustomerProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("billingProfileDisplayName".equals(fieldName)) {
                    deserializedCustomerProperties.billingProfileDisplayName = reader.getString();
                } else if ("billingProfileId".equals(fieldName)) {
                    deserializedCustomerProperties.billingProfileId = reader.getString();
                } else if ("displayName".equals(fieldName)) {
                    deserializedCustomerProperties.displayName = reader.getString();
                } else if ("systemId".equals(fieldName)) {
                    deserializedCustomerProperties.systemId = reader.getString();
                } else if ("status".equals(fieldName)) {
                    deserializedCustomerProperties.status = CustomerStatus.fromString(reader.getString());
                } else if ("enabledAzurePlans".equals(fieldName)) {
                    List<AzurePlan> enabledAzurePlans = reader.readArray(reader1 -> AzurePlan.fromJson(reader1));
                    deserializedCustomerProperties.enabledAzurePlans = enabledAzurePlans;
                } else if ("resellers".equals(fieldName)) {
                    List<Reseller> resellers = reader.readArray(reader1 -> Reseller.fromJson(reader1));
                    deserializedCustomerProperties.resellers = resellers;
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedCustomerProperties.tags = tags;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedCustomerProperties;
        });
    }
}
