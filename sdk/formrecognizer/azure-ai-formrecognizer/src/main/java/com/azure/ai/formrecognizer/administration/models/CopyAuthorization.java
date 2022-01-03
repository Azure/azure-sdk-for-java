// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.ai.formrecognizer.implementation.util.CopyAuthorizationHelper;

import java.time.OffsetDateTime;

/**
 * Authorization to copy a model to the specified target resource and modelId.
 */
public final class CopyAuthorization {
    /*
     * Id of the target Azure resource where the model should be copied to.
     */
    private String targetResourceId;

    /*
     * Location of the target Azure resource where the model should be copied
     * to.
     */
    private String targetResourceRegion;

    /*
     * Identifier of the target model.
     */
    private String targetModelId;

    /*
     * URL of the copied model in the target account.
     */
    private String targetModelLocation;

    /*
     * Token used to authorize the request.
     */
    private String accessToken;

    /*
     * Date/time when the access token expires.
     */
    private OffsetDateTime expirationDateTime;

    /**
     * Get the identifier of the target Azure resource where the model should be copied to.
     *
     * @return the targetResourceId value.
     */
    public String getTargetResourceId() {
        return this.targetResourceId;
    }

    /**
     * Set the identifier of the target Azure resource where the model should be copied to.
     *
     * @param targetResourceId the targetResourceId value to set.
     * @return the CopyAuthorization object itself.
     */
    void setTargetResourceId(String targetResourceId) {
        this.targetResourceId = targetResourceId;
    }

    /**
     * Get the targetResourceRegion property: Location of the target Azure resource where the model should be copied to.
     *
     * @return the targetResourceRegion value.
     */
    public String getTargetResourceRegion() {
        return this.targetResourceRegion;
    }

    /**
     * Set the targetResourceRegion property: Location of the target Azure resource where the model should be copied to.
     *
     * @param targetResourceRegion the targetResourceRegion value to set.
     * @return the CopyAuthorization object itself.
     */
    void setTargetResourceRegion(String targetResourceRegion) {
        this.targetResourceRegion = targetResourceRegion;
    }

    /**
     * Get the targetModelId property: Identifier of the target model.
     *
     * @return the targetModelId value.
     */
    public String getTargetModelId() {
        return this.targetModelId;
    }

    /**
     * Set the targetModelId property: Identifier of the target model.
     *
     * @param targetModelId the targetModelId value to set.
     * @return the CopyAuthorization object itself.
     */
    void setTargetModelId(String targetModelId) {
        this.targetModelId = targetModelId;
    }

    /**
     * Get the targetModelLocation property: URL of the copied model in the target account.
     *
     * @return the targetModelLocation value.
     */
    public String getTargetModelLocation() {
        return this.targetModelLocation;
    }

    /**
     * Set the targetModelLocation property: URL of the copied model in the target account.
     *
     * @param targetModelLocation the targetModelLocation value to set.
     * @return the CopyAuthorization object itself.
     */
    void setTargetModelLocation(String targetModelLocation) {
        this.targetModelLocation = targetModelLocation;
    }

    /**
     * Get the accessToken property: Token used to authorize the request.
     *
     * @return the accessToken value.
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * Set the accessToken property: Token used to authorize the request.
     *
     * @param accessToken the accessToken value to set.
     * @return the CopyAuthorization object itself.
     */
    void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Get the expirationDateTime property: Date/time when the access token expires.
     *
     * @return the expirationDateTime value.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expirationDateTime;
    }

    /**
     * Set the expirationDateTime property: Date/time when the access token expires.
     *
     * @param expirationDateTime the expirationDateTime value to set.
     * @return the CopyAuthorization object itself.
     */
    void setExpirationDateTime(OffsetDateTime expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    static {
        CopyAuthorizationHelper.setAccessor(new CopyAuthorizationHelper.CopyAuthorizationAccessor() {
            @Override
            public void setTargetResourceId(CopyAuthorization copyAuthorization, String targetResourceId) {
                copyAuthorization.setTargetResourceId(targetResourceId);
            }

            @Override
            public void setTargetResourceRegion(CopyAuthorization copyAuthorization, String targetResourceRegion) {
                copyAuthorization.setTargetResourceRegion(targetResourceRegion);
            }

            @Override
            public void setTargetModelId(CopyAuthorization copyAuthorization, String targetModelId) {
                copyAuthorization.setTargetModelId(targetModelId);
            }

            @Override
            public void setTargetModelLocation(CopyAuthorization copyAuthorization, String targetModelLocation) {
                copyAuthorization.setTargetModelLocation(targetModelLocation);
            }

            @Override
            public void setAccessToken(CopyAuthorization copyAuthorization, String accessToken) {
                copyAuthorization.setAccessToken(accessToken);
            }

            @Override
            public void setExpirationDateTime(CopyAuthorization copyAuthorization, OffsetDateTime expirationDateTime) {
                copyAuthorization.setExpirationDateTime(expirationDateTime
                );
            }
        });
    }
}
