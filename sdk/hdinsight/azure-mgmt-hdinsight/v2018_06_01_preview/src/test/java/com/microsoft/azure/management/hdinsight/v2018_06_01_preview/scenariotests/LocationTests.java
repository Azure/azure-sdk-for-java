/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests;

import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.Usage;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.UsagesListResult;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationTests extends HDInsightManagementTestBase {

    @Override
    public void createResources() {
    }

    @Override
    public void cleanUpResources() {
    }

    @Test
    public void testGetUsages() {
        UsagesListResult usages = hdInsightManager.locations().listUsagesAsync(region).toBlocking().single();
        assertThat(usages).isNotNull();
        assertThat(usages.value()).isNotEmpty();
        for (Usage usage : usages.value()) {
            assertThat(usage).isNotNull();
            assertThat(usage.currentValue()).isNotNull();
            assertThat(usage.limit()).isNotNull();
            assertThat(usage.name()).isNotNull();
            assertThat(usage.unit()).isNotNull();
        }
    }
}
