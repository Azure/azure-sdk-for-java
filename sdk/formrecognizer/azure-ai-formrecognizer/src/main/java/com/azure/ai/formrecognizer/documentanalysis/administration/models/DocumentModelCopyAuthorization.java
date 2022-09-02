// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * Authorization to copy a document model to the specified target resource and modelId.
 */
@Immutable
public final class DocumentModelCopyAuthorization {
    /*
     * ID of the target Azure resource where the document model should be copied to.
     */
    private final String targetResourceId;

    /*
     * Location of the target Azure resource where the document model should be copied
     * to.
     */
    private final String targetResourceRegion;

    /*
     * Identifier of the target model.
     */
    private final String targetModelId;

    /*
     * URL of the copied document model in the target account.
     */
    private final String targetModelLocation;

    /*
     * Token used to authorize the request.
     */
    private final String accessToken;

    /*
     * Date/time when the access token expires.
     */
    private final OffsetDateTime expiresOn;

    /**
     * Creates an instance of a {@link DocumentModelCopyAuthorization} model.
     *
     * @param targetResourceId the identifier of the target Azure resource where the model should be copied to.
     * @param targetResourceRegion the location of the target Azure resource where the model should be copied to.
     * @param targetModelId the identifier of the target model.
     * @param targetModelLocation the URL of the copied model in the target account.
     * @param accessToken the token used to authorize the request.
     * @param expiresOn the Date/time when the access token expires.
     */
    public DocumentModelCopyAuthorization(String targetResourceId, String targetResourceRegion, String targetModelId,
                                          String targetModelLocation, String accessToken, OffsetDateTime expiresOn) {
        this.targetResourceId = targetResourceId;
        this.targetResourceRegion = targetResourceRegion;
        this.targetModelId = targetModelId;
        this.targetModelLocation = targetModelLocation;
        this.accessToken = accessToken;
        this.expiresOn = expiresOn;
    }

    /**
     * Get the identifier of the target Azure resource where the document model should be copied to.
     *
     * @return the targetResourceId value.
     */
    public String getTargetResourceId() {
        return this.targetResourceId;
    }

    /**
     * Get the location of the target Azure resource where the document model should be copied to.
     *
     * @return the targetResourceRegion value.
     */
    public String getTargetResourceRegion() {
        return this.targetResourceRegion;
    }

    /**
     * Get the identifier of the target document model.
     *
     * @return the targetModelId value.
     */
    public String getTargetModelId() {
        return this.targetModelId;
    }

    /**
     * Get the URL of the copied document model in the target account.
     *
     * @return the targetModelLocation value.
     */
    public String getTargetModelLocation() {
        return this.targetModelLocation;
    }

    /**
     * Get the token used to authorize the request.
     *
     * @return the accessToken value.
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * Get the Date/time when the access token expires.
     *
     * @return the expiresOn value.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }
}
