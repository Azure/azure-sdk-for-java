// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry.environment;

/**
 * the pojo of VcapPojo
 */
class VcapPojo {
    private String serviceBrokerName;

    private VcapServiceConfig serviceConfig;

    /**
     * Gets the service broker name.
     *
     * @return the service broker name
     */
    public String getServiceBrokerName() {
        return serviceBrokerName;
    }

    /**
     * Sets the service broker name.
     *
     * @param serviceBrokerName the service broker name
     */
    public void setServiceBrokerName(String serviceBrokerName) {
        this.serviceBrokerName = serviceBrokerName;
    }

    /**
     * Gets the service config.
     *
     * @return the service config
     */
    public VcapServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    /**
     * Sets the service config.
     *
     * @param serviceConfig the service config
     */
    public void setServiceConfig(VcapServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    @Override
    public String toString() {
        return "VcapPojo [serviceBrokerName=" + serviceBrokerName
                + ", serviceConfig=" + serviceConfig + "]";
    }

}
