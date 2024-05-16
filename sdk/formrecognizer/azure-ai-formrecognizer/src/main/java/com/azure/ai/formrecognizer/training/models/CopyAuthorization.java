// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The CopyAuthorization model.
 */
@Immutable
public final class CopyAuthorization {
    private static final ClientLogger LOGGER = new ClientLogger(CopyAuthorization.class);

    /*
     * Model identifier.
     */
    private final String modelId;

    /*
     * Token claim used to authorize the request.
     */
    private final String accessToken;

    /*
     * Resource Identifier.
     */
    private final String resourceId;

    /*
     * Region of the resource.
     */
    private final String resourceRegion;

    /*
     * The date-time when the access token expires.
     */
    private final long expirationDateTimeTicks;

    private final OffsetDateTime expiresOn;

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
     * Get the Azure Resource Id of the target Form Recognizer resource where the model will be copied to.
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
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {

            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("modelId", modelId);
            jsonWriter.writeStringField("accessToken", accessToken);
            jsonWriter.writeStringField("resourceId", resourceId);
            jsonWriter.writeStringField("resourceRegion", resourceRegion);
            jsonWriter.writeLongField("expirationDateTimeTicks", expirationDateTimeTicks);
            jsonWriter.writeEndObject();
            jsonWriter.flush();

            return outputStream.toString();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Serialization Failed.", e));
        }
    }

    /**
     * Converts the json string representation to its equivalent CopyAuthorization object.
     *
     * @param copyAuthorization the json string representation of the object.
     * @return the CopyAuthorization object equivalent of the json string.
     * @throws IllegalStateException exception if the deserialization failed
     */
    public static CopyAuthorization fromJson(String copyAuthorization) {
        try (JsonReader jsonReader = JsonProviders.createReader(copyAuthorization)) {
            return jsonReader.readObject(reader -> {
                String modelId = null;
                String accessToken = null;
                String resourceId = null;
                String resourceRegion = null;
                long expirationDateTimeTicks = 0;

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("modelId".equals(fieldName)) {
                        modelId = reader.getString();
                    } else if ("accessToken".equals(fieldName)) {
                        accessToken = reader.getString();
                    } else if ("resourceId".equals(fieldName)) {
                        resourceId = reader.getString();
                    } else if ("resourceRegion".equals(fieldName)) {
                        resourceRegion = reader.getString();
                    } else if ("expirationDateTimeTicks".equals(fieldName)) {
                        expirationDateTimeTicks = reader.getLong();
                    } else {
                        reader.skipChildren();
                    }
                }

                return new CopyAuthorization(modelId, accessToken, resourceId, resourceRegion, expirationDateTimeTicks);
            });
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("Deserialization Failed.", e));
        }
    }
}
