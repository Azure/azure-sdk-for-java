// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;

/**
 * A user delegation key.
 */
public final class UserDelegationKey {
    /*
     * The Azure Active Directory object ID in GUID format.
     */
    private String signedOid;

    /*
     * The Azure Active Directory tenant ID in GUID format
     */
    private String signedTid;

    /*
     * The date-time the key is active
     */
    private OffsetDateTime signedStart;

    /*
     * The date-time the key expires
     */
    private OffsetDateTime signedExpiry;

    /*
     * Abbreviation of the Azure Storage service that accepts the key
     */
    private String signedService;

    /*
     * The service version that created the key
     */
    private String signedVersion;

    /*
     * The key as a base64 string
     */
    private String value;

    /**
     * Get the signedOid property: The Azure Active Directory object ID in GUID
     * format.
     *
     * @return the signedOid value.
     */
    public String getSignedOid() {
        return this.signedOid;
    }

    /**
     * Set the signedOid property: The Azure Active Directory object ID in GUID
     * format.
     *
     * @param signedOid the signedOid value to set.
     * @return the UserDelegationKey object itself.
     */
    public UserDelegationKey setSignedOid(String signedOid) {
        this.signedOid = signedOid;
        return this;
    }

    /**
     * Get the signedTid property: The Azure Active Directory tenant ID in GUID
     * format.
     *
     * @return the signedTid value.
     */
    public String getSignedTid() {
        return this.signedTid;
    }

    /**
     * Set the signedTid property: The Azure Active Directory tenant ID in GUID
     * format.
     *
     * @param signedTid the signedTid value to set.
     * @return the UserDelegationKey object itself.
     */
    public UserDelegationKey setSignedTid(String signedTid) {
        this.signedTid = signedTid;
        return this;
    }

    /**
     * Get the signedStart property: The date-time the key is active.
     *
     * @return the signedStart value.
     */
    public OffsetDateTime getSignedStart() {
        return this.signedStart;
    }

    /**
     * Set the signedStart property: The date-time the key is active.
     *
     * @param signedStart the signedStart value to set.
     * @return the UserDelegationKey object itself.
     */
    public UserDelegationKey setSignedStart(OffsetDateTime signedStart) {
        this.signedStart = signedStart;
        return this;
    }

    /**
     * Get the signedExpiry property: The date-time the key expires.
     *
     * @return the signedExpiry value.
     */
    public OffsetDateTime getSignedExpiry() {
        return this.signedExpiry;
    }

    /**
     * Set the signedExpiry property: The date-time the key expires.
     *
     * @param signedExpiry the signedExpiry value to set.
     * @return the UserDelegationKey object itself.
     */
    public UserDelegationKey setSignedExpiry(OffsetDateTime signedExpiry) {
        this.signedExpiry = signedExpiry;
        return this;
    }

    /**
     * Get the signedService property: Abbreviation of the Azure Storage
     * service that accepts the key.
     *
     * @return the signedService value.
     */
    public String getSignedService() {
        return this.signedService;
    }

    /**
     * Set the signedService property: Abbreviation of the Azure Storage
     * service that accepts the key.
     *
     * @param signedService the signedService value to set.
     * @return the UserDelegationKey object itself.
     */
    public UserDelegationKey setSignedService(String signedService) {
        this.signedService = signedService;
        return this;
    }

    /**
     * Get the signedVersion property: The service version that created the
     * key.
     *
     * @return the signedVersion value.
     */
    public String getSignedVersion() {
        return this.signedVersion;
    }

    /**
     * Set the signedVersion property: The service version that created the
     * key.
     *
     * @param signedVersion the signedVersion value to set.
     * @return the UserDelegationKey object itself.
     */
    public UserDelegationKey setSignedVersion(String signedVersion) {
        this.signedVersion = signedVersion;
        return this;
    }

    /**
     * Get the value property: The key as a base64 string.
     *
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value property: The key as a base64 string.
     *
     * @param value the value value to set.
     * @return the UserDelegationKey object itself.
     */
    public UserDelegationKey setValue(String value) {
        this.value = value;
        return this;
    }
}
