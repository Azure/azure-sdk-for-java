// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

class CommunicationCloudEnvironmentModel {

    private static final String PUBLIC_VALUE = "public";
    private static final String DOD_VALUE = "dod";
    private static final String GCCH_VALUE = "gcch";

    private final String environmentValue;

    CommunicationCloudEnvironmentModel(String environmentValue) {
        java.util.Objects.requireNonNull(environmentValue);
        this.environmentValue = environmentValue;
    }

    public static final CommunicationCloudEnvironmentModel PUBLIC = new CommunicationCloudEnvironmentModel(PUBLIC_VALUE);
    public static final CommunicationCloudEnvironmentModel DOD = new CommunicationCloudEnvironmentModel(DOD_VALUE);
    public static final CommunicationCloudEnvironmentModel GCCH = new CommunicationCloudEnvironmentModel(GCCH_VALUE);

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
