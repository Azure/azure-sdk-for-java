// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.search.samples.ManageSearchService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SearchSampleTests extends SamplesTestBase {

    // secret in URL on API deleteQueryKey
    @DoNotRecord(skipInPlayback = true)
    @Test
    public void testManageSearchService() {
        Assertions.assertTrue(ManageSearchService.runSample(azureResourceManager));
    }
}
