// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AzureVMMetadata {
    private Compute compute;

    public String getLocation() {
        return compute != null ? compute.getLocation() : StringUtils.EMPTY;
    }

    public String getSku() {
        return compute != null ? compute.getSku() : StringUtils.EMPTY;
    }

    public String getAzEnvironment() {
        return compute != null ? compute.getAzEnvironment() : StringUtils.EMPTY;
    }

    public String getOsType() {
        return compute != null ? compute.getOsType() : StringUtils.EMPTY;
    }

    public String getVmSize() {
        return compute != null ? compute.getVmSize() : StringUtils.EMPTY;
    }

    public Compute getCompute() {
        return compute;
    }

    public void setCompute(Compute compute) {
        this.compute = compute;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Compute {
        private String location;
        private String sku;
        private String azEnvironment;
        private String osType;
        private String vmSize;

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getAzEnvironment() {
            return azEnvironment;
        }

        public void setAzEnvironment(String azEnvironment) {
            this.azEnvironment = azEnvironment;
        }

        public String getOsType() {
            return osType;
        }

        public void setOsType(String osType) {
            this.osType = osType;
        }

        public String getVmSize() {
            return vmSize;
        }

        public void setVmSize(String vmSize) {
            this.vmSize = vmSize;
        }
    }
}
