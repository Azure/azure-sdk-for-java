// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The CopyAuthorization model.
 */
@Fluent
public final class CopyAuthorization {
    private static final ClientLogger LOGGER = new ClientLogger(CopyAuthorization.class);

    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();

    CopyAuthorization() {
        super();
    }

    /*
     * Model identifier.
     */
    private String modelId;

    /*
     * Token claim used to authorize the request.
     */
    private String accessToken;

    /*
     * Resource Identifier.
     */
    private String resourceId;

    /*
     * Region of the resource.
     */
    private String resourceRegion;

    /*
     * The date-time when the access token expires.
     */
    @JsonProperty("expirationDateTimeTicks")
    private long expirationDateTimeTicks;

    @JsonIgnore
    private OffsetDateTime expiresOn;

    /**
     * Create a CopyAuthorization object
     *
     * @param modelId The model identifier
     * @param accessToken The token used to authorize the request
     * @param resourceId The resource identifier
     * @param resourceRegion The region of the resource
     * @param expiresOn The expiry time of the token
     */
    public CopyAuthorization(final String modelId, final String accessToken, final String resourceId,
        final String resourceRegion, final long expiresOn) {
        this.modelId = modelId;
        this.accessToken = accessToken;
        this.resourceId = resourceId;
        this.resourceRegion = resourceRegion;
        this.expirationDateTimeTicks = expiresOn;
        this.expiresOn = OffsetDateTime.ofInstant(Instant.ofEpochSecond(expiresOn), ZoneOffset.UTC);
    }

    /**
     * Get the modelId property.
     *
     * @return the {@code modelId} value.
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * Get the token claim used to authorize the request.
     *
     * @return the {@code accessToken} value.
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * Get the date-time when the access token expires.
     *
     * @return the date-time value when the access token expires.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Get the Azure Resource Id of the target Form Recognizer resource
     * where the model will be copied to.
     *
     * @return the {@code resourceId} value.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Get the location of the target Form Recognizer resource.
     *
     * @return the {@code resourceRegion} value.
     */
    public String getResourceRegion() {
        return resourceRegion;
    }

    /**
     * Converts the CopyAuthorization object to its equivalent json string representation.
     *
     * @return the json string representation of the CopyAuthorization object.
     * @throws IllegalStateException exception if the serialization failed
     */
    public String toJson() {
        try {
            return SERIALIZER.serialize(this, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Serialization Failed.", e));
        }
    }

    /**
     * Converts the json string representation to its equivalent CopyAuthorization object.
     *
     * @param copyAuthorization the json string representation of the object.
     *
     * @return the CopyAuthorization object equivalent of the json string.
     * @throws IllegalStateException exception if the deserialization failed
     */
    public static CopyAuthorization fromJson(String copyAuthorization) {
        CopyAuthorization copyAuthorizationObj;
        try {
            copyAuthorizationObj = SERIALIZER.deserialize(copyAuthorization, CopyAuthorization.class,
                SerializerEncoding.JSON);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Deserialization Failed.", e));
        }
        return new CopyAuthorization(
            copyAuthorizationObj.getModelId(), copyAuthorizationObj.getAccessToken(),
            copyAuthorizationObj.getResourceId(), copyAuthorizationObj.getResourceRegion(),
            copyAuthorizationObj.getExpirationDateTimeTicks()
        );
    }

    long getExpirationDateTimeTicks() {
        return expirationDateTimeTicks;
    }
}
