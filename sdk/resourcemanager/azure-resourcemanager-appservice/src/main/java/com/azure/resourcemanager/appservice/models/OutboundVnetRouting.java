// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Outbound traffic options over virtual network.
 */
@Fluent
public final class OutboundVnetRouting implements JsonSerializable<OutboundVnetRouting> {
    /*
     * Enables all other routing options defined in OutboundVnetRouting if this setting is set to true.
     */
    private Boolean allTraffic;

    /*
     * This causes all outbound traffic to have Virtual Network Security Groups and User Defined Routes applied.
     * Previously called VnetRouteAllEnabled.
     */
    private Boolean applicationTraffic;

    /*
     * Enables accessing content over virtual network. Previously called VnetContentShareEnabled
     */
    private Boolean contentShareTraffic;

    /*
     * Enables pulling image over Virtual Network. Previously called VnetImagePullEnabled.
     */
    private Boolean imagePullTraffic;

    /*
     * Enables Backup and Restore operations over virtual network. Previously called VnetBackupRestoreEnabled
     */
    private Boolean backupRestoreTraffic;

    /**
     * Creates an instance of OutboundVnetRouting class.
     */
    public OutboundVnetRouting() {
    }

    /**
     * Get the allTraffic property: Enables all other routing options defined in OutboundVnetRouting if this setting is
     * set to true.
     * 
     * @return the allTraffic value.
     */
    public Boolean allTraffic() {
        return this.allTraffic;
    }

    /**
     * Set the allTraffic property: Enables all other routing options defined in OutboundVnetRouting if this setting is
     * set to true.
     * 
     * @param allTraffic the allTraffic value to set.
     * @return the OutboundVnetRouting object itself.
     */
    public OutboundVnetRouting withAllTraffic(Boolean allTraffic) {
        this.allTraffic = allTraffic;
        return this;
    }

    /**
     * Get the applicationTraffic property: This causes all outbound traffic to have Virtual Network Security Groups and
     * User Defined Routes applied. Previously called VnetRouteAllEnabled.
     * 
     * @return the applicationTraffic value.
     */
    public Boolean applicationTraffic() {
        return this.applicationTraffic;
    }

    /**
     * Set the applicationTraffic property: This causes all outbound traffic to have Virtual Network Security Groups and
     * User Defined Routes applied. Previously called VnetRouteAllEnabled.
     * 
     * @param applicationTraffic the applicationTraffic value to set.
     * @return the OutboundVnetRouting object itself.
     */
    public OutboundVnetRouting withApplicationTraffic(Boolean applicationTraffic) {
        this.applicationTraffic = applicationTraffic;
        return this;
    }

    /**
     * Get the contentShareTraffic property: Enables accessing content over virtual network. Previously called
     * VnetContentShareEnabled.
     * 
     * @return the contentShareTraffic value.
     */
    public Boolean contentShareTraffic() {
        return this.contentShareTraffic;
    }

    /**
     * Set the contentShareTraffic property: Enables accessing content over virtual network. Previously called
     * VnetContentShareEnabled.
     * 
     * @param contentShareTraffic the contentShareTraffic value to set.
     * @return the OutboundVnetRouting object itself.
     */
    public OutboundVnetRouting withContentShareTraffic(Boolean contentShareTraffic) {
        this.contentShareTraffic = contentShareTraffic;
        return this;
    }

    /**
     * Get the imagePullTraffic property: Enables pulling image over Virtual Network. Previously called
     * VnetImagePullEnabled.
     * 
     * @return the imagePullTraffic value.
     */
    public Boolean imagePullTraffic() {
        return this.imagePullTraffic;
    }

    /**
     * Set the imagePullTraffic property: Enables pulling image over Virtual Network. Previously called
     * VnetImagePullEnabled.
     * 
     * @param imagePullTraffic the imagePullTraffic value to set.
     * @return the OutboundVnetRouting object itself.
     */
    public OutboundVnetRouting withImagePullTraffic(Boolean imagePullTraffic) {
        this.imagePullTraffic = imagePullTraffic;
        return this;
    }

    /**
     * Get the backupRestoreTraffic property: Enables Backup and Restore operations over virtual network. Previously
     * called VnetBackupRestoreEnabled.
     * 
     * @return the backupRestoreTraffic value.
     */
    public Boolean backupRestoreTraffic() {
        return this.backupRestoreTraffic;
    }

    /**
     * Set the backupRestoreTraffic property: Enables Backup and Restore operations over virtual network. Previously
     * called VnetBackupRestoreEnabled.
     * 
     * @param backupRestoreTraffic the backupRestoreTraffic value to set.
     * @return the OutboundVnetRouting object itself.
     */
    public OutboundVnetRouting withBackupRestoreTraffic(Boolean backupRestoreTraffic) {
        this.backupRestoreTraffic = backupRestoreTraffic;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeBooleanField("allTraffic", this.allTraffic);
        jsonWriter.writeBooleanField("applicationTraffic", this.applicationTraffic);
        jsonWriter.writeBooleanField("contentShareTraffic", this.contentShareTraffic);
        jsonWriter.writeBooleanField("imagePullTraffic", this.imagePullTraffic);
        jsonWriter.writeBooleanField("backupRestoreTraffic", this.backupRestoreTraffic);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of OutboundVnetRouting from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of OutboundVnetRouting if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the OutboundVnetRouting.
     */
    public static OutboundVnetRouting fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            OutboundVnetRouting deserializedOutboundVnetRouting = new OutboundVnetRouting();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("allTraffic".equals(fieldName)) {
                    deserializedOutboundVnetRouting.allTraffic = reader.getNullable(JsonReader::getBoolean);
                } else if ("applicationTraffic".equals(fieldName)) {
                    deserializedOutboundVnetRouting.applicationTraffic = reader.getNullable(JsonReader::getBoolean);
                } else if ("contentShareTraffic".equals(fieldName)) {
                    deserializedOutboundVnetRouting.contentShareTraffic = reader.getNullable(JsonReader::getBoolean);
                } else if ("imagePullTraffic".equals(fieldName)) {
                    deserializedOutboundVnetRouting.imagePullTraffic = reader.getNullable(JsonReader::getBoolean);
                } else if ("backupRestoreTraffic".equals(fieldName)) {
                    deserializedOutboundVnetRouting.backupRestoreTraffic = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedOutboundVnetRouting;
        });
    }
}
