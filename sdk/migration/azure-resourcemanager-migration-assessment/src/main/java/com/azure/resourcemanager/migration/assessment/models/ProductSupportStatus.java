// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.migration.assessment.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Class to represent the Product Support Status.
 */
@Immutable
public final class ProductSupportStatus implements JsonSerializable<ProductSupportStatus> {
    /*
     * Gets or sets current version of ServicePack.
     */
    private String currentVersion;

    /*
     * Gets or sets ServicePack of the product.
     */
    private String servicePackStatus;

    /*
     * Gets or sets the Extended Security Update ESU status.
     */
    private String esuStatus;

    /*
     * Gets or sets the support status of the product.
     */
    private String supportStatus;

    /*
     * Gets or sets the ETA.
     */
    private Integer eta;

    /*
     * Gets or sets the current ESU support year.
     */
    private String currentEsuYear;

    /*
     * Gets or sets the main stream end date of the product.
     */
    private OffsetDateTime mainstreamEndDate;

    /*
     * Gets or sets the extended support end date of the product.
     */
    private OffsetDateTime extendedSupportEndDate;

    /*
     * Gets or sets the extended security update year 1 end date of the product.
     */
    private OffsetDateTime extendedSecurityUpdateYear1EndDate;

    /*
     * Gets or sets the extended security update year 2 end date of the product.
     */
    private OffsetDateTime extendedSecurityUpdateYear2EndDate;

    /*
     * Gets or sets the extended security update year 3 end date of the product.
     */
    private OffsetDateTime extendedSecurityUpdateYear3EndDate;

    /**
     * Creates an instance of ProductSupportStatus class.
     */
    public ProductSupportStatus() {
    }

    /**
     * Get the currentVersion property: Gets or sets current version of ServicePack.
     * 
     * @return the currentVersion value.
     */
    public String currentVersion() {
        return this.currentVersion;
    }

    /**
     * Get the servicePackStatus property: Gets or sets ServicePack of the product.
     * 
     * @return the servicePackStatus value.
     */
    public String servicePackStatus() {
        return this.servicePackStatus;
    }

    /**
     * Get the esuStatus property: Gets or sets the Extended Security Update ESU status.
     * 
     * @return the esuStatus value.
     */
    public String esuStatus() {
        return this.esuStatus;
    }

    /**
     * Get the supportStatus property: Gets or sets the support status of the product.
     * 
     * @return the supportStatus value.
     */
    public String supportStatus() {
        return this.supportStatus;
    }

    /**
     * Get the eta property: Gets or sets the ETA.
     * 
     * @return the eta value.
     */
    public Integer eta() {
        return this.eta;
    }

    /**
     * Get the currentEsuYear property: Gets or sets the current ESU support year.
     * 
     * @return the currentEsuYear value.
     */
    public String currentEsuYear() {
        return this.currentEsuYear;
    }

    /**
     * Get the mainstreamEndDate property: Gets or sets the main stream end date of the product.
     * 
     * @return the mainstreamEndDate value.
     */
    public OffsetDateTime mainstreamEndDate() {
        return this.mainstreamEndDate;
    }

    /**
     * Get the extendedSupportEndDate property: Gets or sets the extended support end date of the product.
     * 
     * @return the extendedSupportEndDate value.
     */
    public OffsetDateTime extendedSupportEndDate() {
        return this.extendedSupportEndDate;
    }

    /**
     * Get the extendedSecurityUpdateYear1EndDate property: Gets or sets the extended security update year 1 end date of
     * the product.
     * 
     * @return the extendedSecurityUpdateYear1EndDate value.
     */
    public OffsetDateTime extendedSecurityUpdateYear1EndDate() {
        return this.extendedSecurityUpdateYear1EndDate;
    }

    /**
     * Get the extendedSecurityUpdateYear2EndDate property: Gets or sets the extended security update year 2 end date of
     * the product.
     * 
     * @return the extendedSecurityUpdateYear2EndDate value.
     */
    public OffsetDateTime extendedSecurityUpdateYear2EndDate() {
        return this.extendedSecurityUpdateYear2EndDate;
    }

    /**
     * Get the extendedSecurityUpdateYear3EndDate property: Gets or sets the extended security update year 3 end date of
     * the product.
     * 
     * @return the extendedSecurityUpdateYear3EndDate value.
     */
    public OffsetDateTime extendedSecurityUpdateYear3EndDate() {
        return this.extendedSecurityUpdateYear3EndDate;
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
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ProductSupportStatus from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ProductSupportStatus if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ProductSupportStatus.
     */
    public static ProductSupportStatus fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ProductSupportStatus deserializedProductSupportStatus = new ProductSupportStatus();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("currentVersion".equals(fieldName)) {
                    deserializedProductSupportStatus.currentVersion = reader.getString();
                } else if ("servicePackStatus".equals(fieldName)) {
                    deserializedProductSupportStatus.servicePackStatus = reader.getString();
                } else if ("esuStatus".equals(fieldName)) {
                    deserializedProductSupportStatus.esuStatus = reader.getString();
                } else if ("supportStatus".equals(fieldName)) {
                    deserializedProductSupportStatus.supportStatus = reader.getString();
                } else if ("eta".equals(fieldName)) {
                    deserializedProductSupportStatus.eta = reader.getNullable(JsonReader::getInt);
                } else if ("currentEsuYear".equals(fieldName)) {
                    deserializedProductSupportStatus.currentEsuYear = reader.getString();
                } else if ("mainstreamEndDate".equals(fieldName)) {
                    deserializedProductSupportStatus.mainstreamEndDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("extendedSupportEndDate".equals(fieldName)) {
                    deserializedProductSupportStatus.extendedSupportEndDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("extendedSecurityUpdateYear1EndDate".equals(fieldName)) {
                    deserializedProductSupportStatus.extendedSecurityUpdateYear1EndDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("extendedSecurityUpdateYear2EndDate".equals(fieldName)) {
                    deserializedProductSupportStatus.extendedSecurityUpdateYear2EndDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("extendedSecurityUpdateYear3EndDate".equals(fieldName)) {
                    deserializedProductSupportStatus.extendedSecurityUpdateYear3EndDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedProductSupportStatus;
        });
    }
}
