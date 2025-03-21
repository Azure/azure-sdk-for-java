// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.customerinsights.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Describes an entity.
 */
@Fluent
public class EntityTypeDefinition extends MetadataDefinitionBase {
    /*
     * The api entity set name. This becomes the odata entity set name for the entity Type being referred in this
     * object.
     */
    private String apiEntitySetName;

    /*
     * Type of entity.
     */
    private EntityTypes entityType;

    /*
     * The properties of the Profile.
     */
    private List<PropertyDefinition> fields;

    /*
     * The instance count.
     */
    private Integer instancesCount;

    /*
     * The last changed time for the type definition.
     */
    private OffsetDateTime lastChangedUtc;

    /*
     * Provisioning state.
     */
    private ProvisioningStates provisioningState;

    /*
     * The schema org link. This helps ACI identify and suggest semantic models.
     */
    private String schemaItemTypeLink;

    /*
     * The hub name.
     */
    private String tenantId;

    /*
     * The timestamp property name. Represents the time when the interaction or profile update happened.
     */
    private String timestampFieldName;

    /*
     * The name of the entity.
     */
    private String typeName;

    /**
     * Creates an instance of EntityTypeDefinition class.
     */
    public EntityTypeDefinition() {
    }

    /**
     * Get the apiEntitySetName property: The api entity set name. This becomes the odata entity set name for the entity
     * Type being referred in this object.
     * 
     * @return the apiEntitySetName value.
     */
    public String apiEntitySetName() {
        return this.apiEntitySetName;
    }

    /**
     * Set the apiEntitySetName property: The api entity set name. This becomes the odata entity set name for the entity
     * Type being referred in this object.
     * 
     * @param apiEntitySetName the apiEntitySetName value to set.
     * @return the EntityTypeDefinition object itself.
     */
    public EntityTypeDefinition withApiEntitySetName(String apiEntitySetName) {
        this.apiEntitySetName = apiEntitySetName;
        return this;
    }

    /**
     * Get the entityType property: Type of entity.
     * 
     * @return the entityType value.
     */
    public EntityTypes entityType() {
        return this.entityType;
    }

    /**
     * Set the entityType property: Type of entity.
     * 
     * @param entityType the entityType value to set.
     * @return the EntityTypeDefinition object itself.
     */
    public EntityTypeDefinition withEntityType(EntityTypes entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Get the fields property: The properties of the Profile.
     * 
     * @return the fields value.
     */
    public List<PropertyDefinition> fields() {
        return this.fields;
    }

    /**
     * Set the fields property: The properties of the Profile.
     * 
     * @param fields the fields value to set.
     * @return the EntityTypeDefinition object itself.
     */
    public EntityTypeDefinition withFields(List<PropertyDefinition> fields) {
        this.fields = fields;
        return this;
    }

    /**
     * Get the instancesCount property: The instance count.
     * 
     * @return the instancesCount value.
     */
    public Integer instancesCount() {
        return this.instancesCount;
    }

    /**
     * Set the instancesCount property: The instance count.
     * 
     * @param instancesCount the instancesCount value to set.
     * @return the EntityTypeDefinition object itself.
     */
    public EntityTypeDefinition withInstancesCount(Integer instancesCount) {
        this.instancesCount = instancesCount;
        return this;
    }

    /**
     * Get the lastChangedUtc property: The last changed time for the type definition.
     * 
     * @return the lastChangedUtc value.
     */
    public OffsetDateTime lastChangedUtc() {
        return this.lastChangedUtc;
    }

    /**
     * Set the lastChangedUtc property: The last changed time for the type definition.
     * 
     * @param lastChangedUtc the lastChangedUtc value to set.
     * @return the EntityTypeDefinition object itself.
     */
    EntityTypeDefinition withLastChangedUtc(OffsetDateTime lastChangedUtc) {
        this.lastChangedUtc = lastChangedUtc;
        return this;
    }

    /**
     * Get the provisioningState property: Provisioning state.
     * 
     * @return the provisioningState value.
     */
    public ProvisioningStates provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState property: Provisioning state.
     * 
     * @param provisioningState the provisioningState value to set.
     * @return the EntityTypeDefinition object itself.
     */
    EntityTypeDefinition withProvisioningState(ProvisioningStates provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the schemaItemTypeLink property: The schema org link. This helps ACI identify and suggest semantic models.
     * 
     * @return the schemaItemTypeLink value.
     */
    public String schemaItemTypeLink() {
        return this.schemaItemTypeLink;
    }

    /**
     * Set the schemaItemTypeLink property: The schema org link. This helps ACI identify and suggest semantic models.
     * 
     * @param schemaItemTypeLink the schemaItemTypeLink value to set.
     * @return the EntityTypeDefinition object itself.
     */
    public EntityTypeDefinition withSchemaItemTypeLink(String schemaItemTypeLink) {
        this.schemaItemTypeLink = schemaItemTypeLink;
        return this;
    }

    /**
     * Get the tenantId property: The hub name.
     * 
     * @return the tenantId value.
     */
    public String tenantId() {
        return this.tenantId;
    }

    /**
     * Set the tenantId property: The hub name.
     * 
     * @param tenantId the tenantId value to set.
     * @return the EntityTypeDefinition object itself.
     */
    EntityTypeDefinition withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Get the timestampFieldName property: The timestamp property name. Represents the time when the interaction or
     * profile update happened.
     * 
     * @return the timestampFieldName value.
     */
    public String timestampFieldName() {
        return this.timestampFieldName;
    }

    /**
     * Set the timestampFieldName property: The timestamp property name. Represents the time when the interaction or
     * profile update happened.
     * 
     * @param timestampFieldName the timestampFieldName value to set.
     * @return the EntityTypeDefinition object itself.
     */
    public EntityTypeDefinition withTimestampFieldName(String timestampFieldName) {
        this.timestampFieldName = timestampFieldName;
        return this;
    }

    /**
     * Get the typeName property: The name of the entity.
     * 
     * @return the typeName value.
     */
    public String typeName() {
        return this.typeName;
    }

    /**
     * Set the typeName property: The name of the entity.
     * 
     * @param typeName the typeName value to set.
     * @return the EntityTypeDefinition object itself.
     */
    public EntityTypeDefinition withTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityTypeDefinition withAttributes(Map<String, List<String>> attributes) {
        super.withAttributes(attributes);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityTypeDefinition withDescription(Map<String, String> description) {
        super.withDescription(description);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityTypeDefinition withDisplayName(Map<String, String> displayName) {
        super.withDisplayName(displayName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityTypeDefinition withLocalizedAttributes(Map<String, Map<String, String>> localizedAttributes) {
        super.withLocalizedAttributes(localizedAttributes);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityTypeDefinition withSmallImage(String smallImage) {
        super.withSmallImage(smallImage);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityTypeDefinition withMediumImage(String mediumImage) {
        super.withMediumImage(mediumImage);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityTypeDefinition withLargeImage(String largeImage) {
        super.withLargeImage(largeImage);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (fields() != null) {
            fields().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeMapField("attributes", attributes(),
            (writer, element) -> writer.writeArray(element, (writer1, element1) -> writer1.writeString(element1)));
        jsonWriter.writeMapField("description", description(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeMapField("displayName", displayName(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeMapField("localizedAttributes", localizedAttributes(),
            (writer, element) -> writer.writeMap(element, (writer1, element1) -> writer1.writeString(element1)));
        jsonWriter.writeStringField("smallImage", smallImage());
        jsonWriter.writeStringField("mediumImage", mediumImage());
        jsonWriter.writeStringField("largeImage", largeImage());
        jsonWriter.writeStringField("apiEntitySetName", this.apiEntitySetName);
        jsonWriter.writeStringField("entityType", this.entityType == null ? null : this.entityType.toString());
        jsonWriter.writeArrayField("fields", this.fields, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeNumberField("instancesCount", this.instancesCount);
        jsonWriter.writeStringField("schemaItemTypeLink", this.schemaItemTypeLink);
        jsonWriter.writeStringField("timestampFieldName", this.timestampFieldName);
        jsonWriter.writeStringField("typeName", this.typeName);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of EntityTypeDefinition from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of EntityTypeDefinition if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the EntityTypeDefinition.
     */
    public static EntityTypeDefinition fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            EntityTypeDefinition deserializedEntityTypeDefinition = new EntityTypeDefinition();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("attributes".equals(fieldName)) {
                    Map<String, List<String>> attributes
                        = reader.readMap(reader1 -> reader1.readArray(reader2 -> reader2.getString()));
                    deserializedEntityTypeDefinition.withAttributes(attributes);
                } else if ("description".equals(fieldName)) {
                    Map<String, String> description = reader.readMap(reader1 -> reader1.getString());
                    deserializedEntityTypeDefinition.withDescription(description);
                } else if ("displayName".equals(fieldName)) {
                    Map<String, String> displayName = reader.readMap(reader1 -> reader1.getString());
                    deserializedEntityTypeDefinition.withDisplayName(displayName);
                } else if ("localizedAttributes".equals(fieldName)) {
                    Map<String, Map<String, String>> localizedAttributes
                        = reader.readMap(reader1 -> reader1.readMap(reader2 -> reader2.getString()));
                    deserializedEntityTypeDefinition.withLocalizedAttributes(localizedAttributes);
                } else if ("smallImage".equals(fieldName)) {
                    deserializedEntityTypeDefinition.withSmallImage(reader.getString());
                } else if ("mediumImage".equals(fieldName)) {
                    deserializedEntityTypeDefinition.withMediumImage(reader.getString());
                } else if ("largeImage".equals(fieldName)) {
                    deserializedEntityTypeDefinition.withLargeImage(reader.getString());
                } else if ("apiEntitySetName".equals(fieldName)) {
                    deserializedEntityTypeDefinition.apiEntitySetName = reader.getString();
                } else if ("entityType".equals(fieldName)) {
                    deserializedEntityTypeDefinition.entityType = EntityTypes.fromString(reader.getString());
                } else if ("fields".equals(fieldName)) {
                    List<PropertyDefinition> fields = reader.readArray(reader1 -> PropertyDefinition.fromJson(reader1));
                    deserializedEntityTypeDefinition.fields = fields;
                } else if ("instancesCount".equals(fieldName)) {
                    deserializedEntityTypeDefinition.instancesCount = reader.getNullable(JsonReader::getInt);
                } else if ("lastChangedUtc".equals(fieldName)) {
                    deserializedEntityTypeDefinition.lastChangedUtc = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("provisioningState".equals(fieldName)) {
                    deserializedEntityTypeDefinition.provisioningState
                        = ProvisioningStates.fromString(reader.getString());
                } else if ("schemaItemTypeLink".equals(fieldName)) {
                    deserializedEntityTypeDefinition.schemaItemTypeLink = reader.getString();
                } else if ("tenantId".equals(fieldName)) {
                    deserializedEntityTypeDefinition.tenantId = reader.getString();
                } else if ("timestampFieldName".equals(fieldName)) {
                    deserializedEntityTypeDefinition.timestampFieldName = reader.getString();
                } else if ("typeName".equals(fieldName)) {
                    deserializedEntityTypeDefinition.typeName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedEntityTypeDefinition;
        });
    }
}
