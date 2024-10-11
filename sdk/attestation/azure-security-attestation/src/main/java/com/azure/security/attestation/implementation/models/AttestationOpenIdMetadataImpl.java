// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation.implementation.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.attestation.models.AttestationOpenIdMetadata;
import com.nimbusds.jose.util.JSONObjectUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class AttestationOpenIdMetadataImpl
    implements AttestationOpenIdMetadata, JsonSerializable<AttestationOpenIdMetadataImpl> {
    private static final ClientLogger LOGGER = new ClientLogger(AttestationOpenIdMetadataImpl.class);

    private String jwksUri;
    private String issuer;
    private String[] responseTypesSupported;
    private String[] tokenSigningAlgorithmsSupported;
    private String[] supportedClaims;

    @Override
    public String getJsonWebKeySetUrl() {
        return jwksUri;
    }

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public String[] getResponseTypesSupported() {
        return responseTypesSupported.clone();
    }

    @Override
    public String[] getTokenSigningAlgorithmsSupported() {
        return tokenSigningAlgorithmsSupported.clone();
    }

    @Override
    public String[] getSupportedClaims() {
        return supportedClaims.clone();
    }

    @SuppressWarnings("unchecked")
    public static AttestationOpenIdMetadata fromGenerated(Object generated) {
        try (JsonReader jsonReader
            = JsonProviders.createReader(JSONObjectUtils.toJSONString((LinkedHashMap<String, Object>) generated))) {
            return AttestationOpenIdMetadataImpl.fromJson(jsonReader);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e.getMessage()));
        }
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("jwks_uri", jwksUri)
            .writeStringField("issuer", issuer)
            .writeArrayField("response_types_supported", responseTypesSupported, JsonWriter::writeString)
            .writeArrayField("id_token_signing_alg_values_supported", tokenSigningAlgorithmsSupported,
                JsonWriter::writeString)
            .writeArrayField("claims_supported", supportedClaims, JsonWriter::writeString)
            .writeEndObject();
    }

    public static AttestationOpenIdMetadataImpl fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AttestationOpenIdMetadataImpl deserialized = new AttestationOpenIdMetadataImpl();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("jwks_uri".equals(fieldName)) {
                    deserialized.jwksUri = reader.getString();
                } else if ("issuer".equals(fieldName)) {
                    deserialized.issuer = reader.getString();
                } else if ("response_types_supported".equals(fieldName)) {
                    List<String> list = reader.readArray(JsonReader::getString);
                    deserialized.responseTypesSupported = (list == null) ? null : list.toArray(new String[0]);
                } else if ("id_token_signing_alg_values_supported".equals(fieldName)) {
                    List<String> list = reader.readArray(JsonReader::getString);
                    deserialized.tokenSigningAlgorithmsSupported = (list == null) ? null : list.toArray(new String[0]);
                } else if ("claims_supported".equals(fieldName)) {
                    List<String> list = reader.readArray(JsonReader::getString);
                    deserialized.supportedClaims = (list == null) ? null : list.toArray(new String[0]);
                } else {
                    reader.skipChildren();
                }
            }

            return deserialized;
        });
    }
}
