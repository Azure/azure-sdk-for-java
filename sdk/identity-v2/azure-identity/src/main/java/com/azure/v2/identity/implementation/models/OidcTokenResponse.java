// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;

/**
 * Represents OIDC token response and offers support to parse it from JSON.
 */
public class OidcTokenResponse implements JsonSerializable<OidcTokenResponse> {
    private String oidcToken;

    /**
     * Gets the oidc token.
     *
     * @return the oidc token
     */
    public String getOidcToken() {
        return oidcToken;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeStringField("oidcToken", oidcToken).writeEndObject();
    }

    /**
     * Parses OIDC token response from JSON.
     *
     * @param jsonReader the json reader
     * @return the parsed OIDC token response
     * @throws IOException if the parsing fails.
     */
    public static OidcTokenResponse fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            OidcTokenResponse oidcTokenResponse = new OidcTokenResponse();
            while (JsonToken.END_OBJECT != reader.nextToken()) {
                String fieldName = reader.getFieldName();
                if ("oidcToken".equals(fieldName)) {
                    oidcTokenResponse.oidcToken = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return oidcTokenResponse;
        });
    }
}
