// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Additional UEFI key signatures that will be added to the image in addition to the signature templates.
 */
@Fluent
public final class UefiKeySignatures implements JsonSerializable<UefiKeySignatures> {
    /*
     * The Platform Key of this image version.
     */
    private UefiKey pk;

    /*
     * The Key Encryption Keys of this image version.
     */
    private List<UefiKey> kek;

    /*
     * The database of UEFI keys for this image version.
     */
    private List<UefiKey> db;

    /*
     * The database of revoked UEFI keys for this image version.
     */
    private List<UefiKey> dbx;

    /**
     * Creates an instance of UefiKeySignatures class.
     */
    public UefiKeySignatures() {
    }

    /**
     * Get the pk property: The Platform Key of this image version.
     * 
     * @return the pk value.
     */
    public UefiKey pk() {
        return this.pk;
    }

    /**
     * Set the pk property: The Platform Key of this image version.
     * 
     * @param pk the pk value to set.
     * @return the UefiKeySignatures object itself.
     */
    public UefiKeySignatures withPk(UefiKey pk) {
        this.pk = pk;
        return this;
    }

    /**
     * Get the kek property: The Key Encryption Keys of this image version.
     * 
     * @return the kek value.
     */
    public List<UefiKey> kek() {
        return this.kek;
    }

    /**
     * Set the kek property: The Key Encryption Keys of this image version.
     * 
     * @param kek the kek value to set.
     * @return the UefiKeySignatures object itself.
     */
    public UefiKeySignatures withKek(List<UefiKey> kek) {
        this.kek = kek;
        return this;
    }

    /**
     * Get the db property: The database of UEFI keys for this image version.
     * 
     * @return the db value.
     */
    public List<UefiKey> db() {
        return this.db;
    }

    /**
     * Set the db property: The database of UEFI keys for this image version.
     * 
     * @param db the db value to set.
     * @return the UefiKeySignatures object itself.
     */
    public UefiKeySignatures withDb(List<UefiKey> db) {
        this.db = db;
        return this;
    }

    /**
     * Get the dbx property: The database of revoked UEFI keys for this image version.
     * 
     * @return the dbx value.
     */
    public List<UefiKey> dbx() {
        return this.dbx;
    }

    /**
     * Set the dbx property: The database of revoked UEFI keys for this image version.
     * 
     * @param dbx the dbx value to set.
     * @return the UefiKeySignatures object itself.
     */
    public UefiKeySignatures withDbx(List<UefiKey> dbx) {
        this.dbx = dbx;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (pk() != null) {
            pk().validate();
        }
        if (kek() != null) {
            kek().forEach(e -> e.validate());
        }
        if (db() != null) {
            db().forEach(e -> e.validate());
        }
        if (dbx() != null) {
            dbx().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("pk", this.pk);
        jsonWriter.writeArrayField("kek", this.kek, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("db", this.db, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("dbx", this.dbx, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of UefiKeySignatures from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of UefiKeySignatures if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the UefiKeySignatures.
     */
    public static UefiKeySignatures fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            UefiKeySignatures deserializedUefiKeySignatures = new UefiKeySignatures();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("pk".equals(fieldName)) {
                    deserializedUefiKeySignatures.pk = UefiKey.fromJson(reader);
                } else if ("kek".equals(fieldName)) {
                    List<UefiKey> kek = reader.readArray(reader1 -> UefiKey.fromJson(reader1));
                    deserializedUefiKeySignatures.kek = kek;
                } else if ("db".equals(fieldName)) {
                    List<UefiKey> db = reader.readArray(reader1 -> UefiKey.fromJson(reader1));
                    deserializedUefiKeySignatures.db = db;
                } else if ("dbx".equals(fieldName)) {
                    List<UefiKey> dbx = reader.readArray(reader1 -> UefiKey.fromJson(reader1));
                    deserializedUefiKeySignatures.dbx = dbx;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedUefiKeySignatures;
        });
    }
}
