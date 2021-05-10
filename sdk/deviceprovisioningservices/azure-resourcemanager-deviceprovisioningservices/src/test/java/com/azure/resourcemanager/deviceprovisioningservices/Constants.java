// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

public class Constants
{
    public static final String ACCESS_KEY_NAME = "provisioningserviceowner";

    public static final String DEFAULT_LOCATION = "WestUS2";

    public static class DefaultSku
    {
        public static final String NAME = "S1";
        public static final int CAPACITY = 1;
        public static final String TIER = "S1";
    }

    public static final String[] ALLOCATION_POLICIES = { "Hashed", "GeoLatency", "Static" };

    public static final int ARM_ATTEMPT_WAIT_MS = 500;
    public static final int RANDOM_ALLOCATION_WEIGHT = 870084357;
    public static final int ARM_ATTEMPT_LIMIT = 5;
}
