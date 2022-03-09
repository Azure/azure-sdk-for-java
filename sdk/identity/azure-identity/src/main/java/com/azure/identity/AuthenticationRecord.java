// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Represents the account information relating to an authentication request
 */
public final class AuthenticationRecord {
    private static final JsonFactory JSON_FACTORY = JsonFactory.builder().build();

    @JsonProperty("authority")
    private String authority;

    @JsonProperty("homeAccountId")
    private String homeAccountId;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("clientId")
    private String clientId;


    AuthenticationRecord() { }

    AuthenticationRecord(IAuthenticationResult authenticationResult, String tenantId, String clientId) {
        authority = authenticationResult.account().environment();
        homeAccountId = authenticationResult.account().homeAccountId();
        username = authenticationResult.account().username();
        this.tenantId = tenantId;
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
            serialize(outputStream);
            return outputStream;
        });
    }

    /**
     * Serializes the {@link AuthenticationRecord} to the specified {@link OutputStream}
     *
     * @param outputStream The {@link OutputStream} to which the serialized record will be written to.
     */
    public void serialize(OutputStream outputStream) {
        try (JsonGenerator generator = JSON_FACTORY.createGenerator(outputStream)) {
            if (authority != null) {
                generator.writeStringField("authority", authority);
            }

            if (homeAccountId != null) {
                generator.writeStringField("homeAccountId", homeAccountId);
            }

            if (tenantId != null) {
                generator.writeStringField("tenantId", tenantId);
            }

            if (username != null) {
                generator.writeStringField("username", username);
            }

            if (clientId != null) {
                generator.writeStringField("clientId", clientId);
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Deserializes the {@link AuthenticationRecord} from the specified {@link InputStream}
     *
     * @param inputStream The {@link InputStream} from which the serialized record will be read.
     * @return A {@link Mono} containing the {@link AuthenticationRecord} object.
     */
    public static Mono<AuthenticationRecord> deserializeAsync(InputStream inputStream) {
        return Mono.fromCallable(() -> deserialize(inputStream));
    }

    /**
     * Deserializes the {@link AuthenticationRecord} from the specified {@link InputStream}
     *
     * @param inputStream The {@link InputStream} from which the serialized record will be read.
     * @return the {@link AuthenticationRecord} object.
     */
    public static AuthenticationRecord deserialize(InputStream inputStream) {
        AuthenticationRecord record = new AuthenticationRecord();

        try (JsonParser parser = JSON_FACTORY.createParser(inputStream)) {
            if (parser.currentToken() == null) {
                parser.nextToken();
            }

            String fieldName;
            while ((fieldName = parser.nextFieldName()) != null) {
                switch (fieldName) {
                    case "authority":
                        record.authority = parser.nextTextValue();
                        break;

                    case "homeAccountId":
                        record.homeAccountId = parser.nextTextValue();
                        break;

                    case "tenantId":
                        record.tenantId = parser.nextTextValue();
                        break;

                    case "username":
                        record.username = parser.nextTextValue();
                        break;

                    case "clientId":
                        record.clientId = parser.nextTextValue();
                        break;

                    default:
                        parser.nextToken();
                        break;
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return record;
    }
}
