// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Authentication Record represents the account information of the authenticated account.
 * This is helpful in scenarios where applications require brokered authentication via {@link DeviceCodeCredential} or
 * {@link InteractiveBrowserCredential}.</p>
 *
 * <p>Authentication record is returned when
 * {@link DeviceCodeCredential#authenticate()} or {@link InteractiveBrowserCredential#authenticate()} api is invoked.
 * The returned auth record can be stored/persisted in the user application. Further, this record can be configured on
 * the {@link DeviceCodeCredentialBuilder#authenticationRecord(AuthenticationRecord)} or
 * {@link InteractiveBrowserCredentialBuilder#authenticationRecord(AuthenticationRecord)} to proactively indicate that a
 * previously authenticated account should be used from the persisted cache instead of authenticating again.</p>
 *
 * @see DeviceCodeCredential
 * @see InteractiveBrowserCredential
 */
public final class AuthenticationRecord implements JsonSerializable<AuthenticationRecord> {
    private String authority;
    private String homeAccountId;
    private String tenantId;
    private String username;
    private String clientId;

    AuthenticationRecord(IAuthenticationResult authenticationResult, String tenantId, String clientId) {
        authority = authenticationResult.account().environment();
        homeAccountId = authenticationResult.account().homeAccountId();
        username = authenticationResult.account().username();
        this.tenantId = tenantId;
        this.clientId = clientId;
    }

    AuthenticationRecord(String authority, String homeAccountId, String tenantId, String username, String clientId) {
        this.authority = authority;
        this.homeAccountId = homeAccountId;
        this.tenantId = tenantId;
        this.username = username;
        this.clientId = clientId;
    }

    /**
     * Get the authority host used to authenticate the account.
     *
     * @return the authority host.
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * Get the unique identifier of the account.
     *
     * @return the account id.
     */
    public String getHomeAccountId() {
        return homeAccountId;
    }

    /**
     * Get the tenant, which the account authenticated in.
     *
     * @return the tenant id.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Get the client id of the application used for authentication.
     *
     * @return the client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the user principal name of the account.
     *
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Serializes the {@link AuthenticationRecord} to the specified {@link OutputStream}
     *
     * @param outputStream The {@link OutputStream} to which the serialized record will be written to.
     * @return A {@link Mono} containing {@link Void}
     */
    public Mono<OutputStream> serializeAsync(OutputStream outputStream) {
        return Mono.fromCallable(() -> {
            try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
                this.toJson(jsonWriter);
                return outputStream;
            }
        });
    }

    /**
     * Serializes the {@link AuthenticationRecord} to the specified {@link OutputStream}
     *
     * @param outputStream The {@link OutputStream} to which the serialized record will be written to.
     */
    public void serialize(OutputStream outputStream) {
        serializeAsync(outputStream).block();
    }

    /**
     * Deserializes the {@link AuthenticationRecord} from the specified {@link InputStream}
     *
     * @param inputStream The {@link InputStream} from which the serialized record will be read.
     * @return A {@link Mono} containing the {@link AuthenticationRecord} object.
     */
    public static Mono<AuthenticationRecord> deserializeAsync(InputStream inputStream) {
        return Mono.fromCallable(() -> {
            try (JsonReader jsonReader = JsonProviders.createReader(inputStream)) {
                return AuthenticationRecord.fromJson(jsonReader);
            }
        });
    }

    /**
     * Deserializes the {@link AuthenticationRecord} from the specified {@link InputStream}
     *
     * @param inputStream The {@link InputStream} from which the serialized record will be read.
     * @return the {@link AuthenticationRecord} object.
     */
    public static AuthenticationRecord deserialize(InputStream inputStream) {
        return deserializeAsync(inputStream).block();
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("authority", authority)
            .writeStringField("homeAccountId", homeAccountId)
            .writeStringField("tenantId", tenantId)
            .writeStringField("username", username)
            .writeStringField("clientId", clientId)
            .writeEndObject();
    }

    /**
     * Reads an instance of AuthenticationRecord from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AuthenticationRecord if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the AuthenticationRecord.
     */
    public static AuthenticationRecord fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String authority = null;
            String homeAccountId = null;
            String tenantId = null;
            String username = null;
            String clientId = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("authority".equals(fieldName)) {
                    authority = reader.getString();
                } else if ("homeAccountId".equals(fieldName)) {
                    homeAccountId = reader.getString();
                } else if ("tenantId".equals(fieldName)) {
                    tenantId = reader.getString();
                } else if ("username".equals(fieldName)) {
                    username = reader.getString();
                } else if ("clientId".equals(fieldName)) {
                    clientId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return new AuthenticationRecord(authority, homeAccountId, tenantId, username, clientId);
        });
    }
}
