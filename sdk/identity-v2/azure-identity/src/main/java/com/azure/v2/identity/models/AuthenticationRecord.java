// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.models;

import com.azure.v2.identity.InteractiveBrowserCredential;
import com.azure.v2.identity.InteractiveBrowserCredentialBuilder;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Authentication Record represents the account information of the authenticated account.
 * This is helpful in scenarios where applications require brokered authentication via
 * {@link InteractiveBrowserCredential}.</p>
 *
 * <p>Authentication record is returned when {@link InteractiveBrowserCredential#authenticate()} api is invoked.
 * The returned auth record can be stored/persisted in the user application. Further, this record can be configured on
 * the {@link InteractiveBrowserCredentialBuilder#authenticationRecord(AuthenticationRecord)} to proactively indicate
 * that a previously authenticated account should be used from the persisted cache instead of authenticating again.</p>
 *
 * @see InteractiveBrowserCredential
 */
public final class AuthenticationRecord {

    static final ClientLogger LOGGER = new ClientLogger(AuthenticationRecord.class);

    private String authority;

    private String homeAccountId;

    private String tenantId;

    private String username;

    private String clientId;

    /**
     * Creates an instance of authentication record.
     *
     * @param authority the authority used to authenticate
     * @param homeAccountId the account ID
     * @param userName the username
     * @param tenantId the tenant ID
     * @param clientId the tenant ID
     */
    public AuthenticationRecord(String authority, String homeAccountId, String userName, String tenantId,
        String clientId) {
        this.authority = authority;
        this.homeAccountId = homeAccountId;
        this.tenantId = tenantId;
        this.username = userName;
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
     */
    public void serialize(OutputStream outputStream) {
        try (JsonWriter writer = JsonWriter.toStream(outputStream)) {
            writer.writeStartObject();
            writer.writeStringField("authority", authority);
            writer.writeStringField("homeAccountId", homeAccountId);
            writer.writeStringField("tenantId", tenantId);
            writer.writeStringField("username", username);
            writer.writeStringField("clientId", clientId);
            writer.writeEndObject();
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    /**
     * Deserializes the {@link AuthenticationRecord} from the specified {@link InputStream}
     *
     * @param inputStream The {@link InputStream} from which the serialized record will be read.
     * @return the {@link AuthenticationRecord} object.
     */
    public static AuthenticationRecord deserialize(InputStream inputStream) {
        try (JsonReader jsonReader = JsonReader.fromStream(inputStream)) {
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
                return new AuthenticationRecord(authority, homeAccountId, username, tenantId, clientId);
            });
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }
}
