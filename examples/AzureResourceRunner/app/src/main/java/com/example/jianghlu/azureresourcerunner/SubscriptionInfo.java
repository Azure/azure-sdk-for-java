package com.example.jianghlu.azureresourcerunner;

import java.io.Serializable;

/**
 * Created by jianghlu on 1/14/2016.
 */
public class SubscriptionInfo implements Serializable {
    private String tenantId;
    private String subscriptionId;
    private String subscriptionName;

    public SubscriptionInfo(String tenantId, String subscriptionId, String subscriptionName) {
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
        this.subscriptionName = subscriptionName;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }
}