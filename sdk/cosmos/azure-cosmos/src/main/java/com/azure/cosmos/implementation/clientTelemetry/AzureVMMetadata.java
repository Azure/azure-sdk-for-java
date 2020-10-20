// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clientTelemetry;

public class AzureVMMetadata {
    public Compute compute;
    public String getLocation() {
        return compute != null ? compute.location : null;
    }

    public class Compute{
        public String location;
    }
}
