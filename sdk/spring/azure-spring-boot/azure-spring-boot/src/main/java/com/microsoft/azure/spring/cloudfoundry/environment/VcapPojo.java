/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloudfoundry.environment;

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
