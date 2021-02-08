// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import java.util.Objects;

/**
 * The cloud that the identifier belongs to.
 */
public class CommunicationCloudEnvironment {
    private static final String PUBLIC_VALUE = "public";
    private static final String DOD_VALUE = "dod";
    private static final String GCCH_VALUE = "gcch";

    private final String environmentValue;

    /**
     * Create CommunicationCloudEnvironment with name string
     * @param environmentValue name of hte cloud environment
     */
    public CommunicationCloudEnvironment(String environmentValue) {
        Objects.requireNonNull(environmentValue);
        this.environmentValue = environmentValue;
    }

    static CommunicationCloudEnvironment fromModel(CommunicationCloudEnvironmentModel environmentModel) {
        return new CommunicationCloudEnvironment(environmentModel.toString());
    }

    /**
     * Represent Azure public cloud
     */
    public static final CommunicationCloudEnvironment PUBLIC = new CommunicationCloudEnvironment(PUBLIC_VALUE);

    /**
     * Represent Azure Dod cloud
     */
    public static final CommunicationCloudEnvironment DOD = new CommunicationCloudEnvironment(DOD_VALUE);

    /**
     * Represent Azure Gcch cloud
     */
    public static final CommunicationCloudEnvironment GCCH = new CommunicationCloudEnvironment(GCCH_VALUE);

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        return that != null && this.environmentValue.equals(that.toString());
    }

    @Override
    public String toString() {
        return environmentValue;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
