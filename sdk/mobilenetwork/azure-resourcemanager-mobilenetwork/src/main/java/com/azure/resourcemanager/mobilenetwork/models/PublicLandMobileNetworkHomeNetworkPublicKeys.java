// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.mobilenetwork.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * Configuration relating to SUPI concealment.
 */
@Fluent
public final class PublicLandMobileNetworkHomeNetworkPublicKeys
    implements JsonSerializable<PublicLandMobileNetworkHomeNetworkPublicKeys> {
    /*
     * This provides a mapping to identify which public key has been used for SUPI concealment using the Profile A
     * Protection Scheme.
     */
    private List<HomeNetworkPublicKey> profileA;

    /*
     * This provides a mapping to identify which public key has been used for SUPI concealment using the Profile B
     * Protection Scheme.
     */
    private List<HomeNetworkPublicKey> profileB;

    /**
     * Creates an instance of PublicLandMobileNetworkHomeNetworkPublicKeys class.
     */
    public PublicLandMobileNetworkHomeNetworkPublicKeys() {
    }

    /**
     * Get the profileA property: This provides a mapping to identify which public key has been used for SUPI
     * concealment using the Profile A Protection Scheme.
     * 
     * @return the profileA value.
     */
    public List<HomeNetworkPublicKey> profileA() {
        return this.profileA;
    }

    /**
     * Set the profileA property: This provides a mapping to identify which public key has been used for SUPI
     * concealment using the Profile A Protection Scheme.
     * 
     * @param profileA the profileA value to set.
     * @return the PublicLandMobileNetworkHomeNetworkPublicKeys object itself.
     */
    public PublicLandMobileNetworkHomeNetworkPublicKeys withProfileA(List<HomeNetworkPublicKey> profileA) {
        this.profileA = profileA;
        return this;
    }

    /**
     * Get the profileB property: This provides a mapping to identify which public key has been used for SUPI
     * concealment using the Profile B Protection Scheme.
     * 
     * @return the profileB value.
     */
    public List<HomeNetworkPublicKey> profileB() {
        return this.profileB;
    }

    /**
     * Set the profileB property: This provides a mapping to identify which public key has been used for SUPI
     * concealment using the Profile B Protection Scheme.
     * 
     * @param profileB the profileB value to set.
     * @return the PublicLandMobileNetworkHomeNetworkPublicKeys object itself.
     */
    public PublicLandMobileNetworkHomeNetworkPublicKeys withProfileB(List<HomeNetworkPublicKey> profileB) {
        this.profileB = profileB;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (profileA() != null) {
            profileA().forEach(e -> e.validate());
        }
        if (profileB() != null) {
            profileB().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("profileA", this.profileA, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("profileB", this.profileB, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PublicLandMobileNetworkHomeNetworkPublicKeys from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PublicLandMobileNetworkHomeNetworkPublicKeys if the JsonReader was pointing to an instance
     * of it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the PublicLandMobileNetworkHomeNetworkPublicKeys.
     */
    public static PublicLandMobileNetworkHomeNetworkPublicKeys fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PublicLandMobileNetworkHomeNetworkPublicKeys deserializedPublicLandMobileNetworkHomeNetworkPublicKeys
                = new PublicLandMobileNetworkHomeNetworkPublicKeys();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("profileA".equals(fieldName)) {
                    List<HomeNetworkPublicKey> profileA
                        = reader.readArray(reader1 -> HomeNetworkPublicKey.fromJson(reader1));
                    deserializedPublicLandMobileNetworkHomeNetworkPublicKeys.profileA = profileA;
                } else if ("profileB".equals(fieldName)) {
                    List<HomeNetworkPublicKey> profileB
                        = reader.readArray(reader1 -> HomeNetworkPublicKey.fromJson(reader1));
                    deserializedPublicLandMobileNetworkHomeNetworkPublicKeys.profileB = profileB;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPublicLandMobileNetworkHomeNetworkPublicKeys;
        });
    }
}
