package com.azure.spring.cloud.feature.manager.testobjects;

import java.util.Map;

import com.azure.spring.cloud.feature.manager.IDynamicFeatureProperties;

public class MockableProperties implements IDynamicFeatureProperties {

    private Map<String, DiscountBanner> discountBanner;

    /**
     * @return the discountBanner
     */
    public Map<String, DiscountBanner> getDiscountBanner() {
        return discountBanner;
    }

    /**
     * @param discountBanner the discountBanner to set
     */
    public void setDiscountBanner(Map<String, DiscountBanner> discountBanner) {
        this.discountBanner = discountBanner;
    }

}
