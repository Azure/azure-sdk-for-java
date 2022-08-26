// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class FeatureFlagStoreTest {

    @Test
    public void validateAndInitTest() {
        // Starts out empty
        FeatureFlagStore featureStore = new FeatureFlagStore();
        assertEquals(0, featureStore.getSelects().size());

        // If disabled does't setup the select all f t
        featureStore = new FeatureFlagStore();
        featureStore.validateAndInit();
        assertEquals(0, featureStore.getSelects().size());

        // Enabled, with no selects, so selector is created t t
        featureStore = new FeatureFlagStore();
        featureStore.setEnabled(true);
        featureStore.validateAndInit();
        assertEquals(1, featureStore.getSelects().size());
        assertEquals("", featureStore.getSelects().get(0).getKeyFilter());

        featureStore = new FeatureFlagStore();
        featureStore.setEnabled(true);

        List<FeatureFlagKeyValueSelector> selectors = new ArrayList<>();
        FeatureFlagKeyValueSelector selector = new FeatureFlagKeyValueSelector();
        selector.setKeyFilter(".appconfig/Alpha");
        selectors.add(selector);
        featureStore.setSelects(selectors);

        featureStore.validateAndInit();
        assertEquals(1, featureStore.getSelects().size());
        assertEquals(".appconfig/Alpha", featureStore.getSelects().get(0).getKeyFilter());
    }
}
