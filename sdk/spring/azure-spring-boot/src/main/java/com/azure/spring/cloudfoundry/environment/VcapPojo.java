// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloudfoundry.environment;

/**
 * the pojo of VcapPojo
 */
public class VcapPojo {
    private String serviceBrokerName;

    private VcapServiceConfig serviceConfig;

    public String getServiceBrokerName() {
        return serviceBrokerName;
    }

    public void setServiceBrokerName(String serviceBrokerName) {
        this.serviceBrokerName = serviceBrokerName;
    }

    public VcapServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(VcapServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    @Override
    public String toString() {
        return "VcapPojo [serviceBrokerName=" + serviceBrokerName
                + ", serviceConfig=" + serviceConfig + "]";
    }

}
