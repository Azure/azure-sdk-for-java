// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import java.time.OffsetDateTime;

import static com.azure.storage.common.implementation.StorageImplUtils.assertNotNull;

/**
 * Extended options that may be passed when getting a user delegation key for a storage account.
 */
public class ShareGetUserDelegationKeyOptions {
    private final OffsetDateTime expiresOn;
    private OffsetDateTime startsOn;
    private String delegatedUserTenantId;

    /**
     * Creates a new instance of {@link ShareGetUserDelegationKeyOptions}.
     *
     * @param expiresOn Expiration of the key's validity. The time should be specified in UTC.
     * @throws NullPointerException If {@code tier} is null.
     */
    public ShareGetUserDelegationKeyOptions(OffsetDateTime expiresOn) {
        assertNotNull("expiresOn", expiresOn);
        this.expiresOn = expiresOn;
    }

    /**
     * Gets the expiration of the key's validity.
     *
     * @return The expiration time in UTC.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * <p> Optional. Sets the start time of the key's validity. Null indicates the key is valid immediately. </p>
     *
     * If you set the start time to the current time, failures might occur intermittently for the first few minutes.
     * This is due to different machines having slightly different current times, known as clock skew.
     *
     * @param startsOn The start time in UTC.
     * @return The updated {@link ShareGetUserDelegationKeyOptions} object.
     */
    public ShareGetUserDelegationKeyOptions setStartsOn(OffsetDateTime startsOn) {
        this.startsOn = startsOn;
        return this;
    }

    /**
     * Gets the start time of the key's validity.
     *
     * @return The start time in UTC.
     */
    public OffsetDateTime getStartsOn() {
        return startsOn;
    }

    /**
     * <p> Optional. Sets the tenant ID of the user to whom the delegation key is issued in Azure AD. </p>
     *
     * @param delegatedUserTenantId The tenant ID.
     * @return The updated {@link ShareGetUserDelegationKeyOptions} object.
     */
    public ShareGetUserDelegationKeyOptions setDelegatedUserTenantId(String delegatedUserTenantId) {
        this.delegatedUserTenantId = delegatedUserTenantId;
        return this;
    }

    /**
     * Gets the tenant ID of the user to whom the delegation key is issued in Azure AD.
     *
     * @return The tenant ID.
     */
    public String getDelegatedUserTenantId() {
        return delegatedUserTenantId;
    }
}
