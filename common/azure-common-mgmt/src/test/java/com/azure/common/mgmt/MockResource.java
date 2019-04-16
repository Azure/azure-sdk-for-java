// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.mgmt;

public class MockResource {
    public String name;

    public Properties properties;

    public static class Properties {
        public String provisioningState;
    }
}
