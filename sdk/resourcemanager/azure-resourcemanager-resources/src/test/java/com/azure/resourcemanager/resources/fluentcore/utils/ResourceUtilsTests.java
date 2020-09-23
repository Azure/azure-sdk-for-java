// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.management.AzureEnvironment;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceUtilsTests {
    @Test
    public void canExtractGroupFromId() throws Exception {
        Assertions.assertEquals("foo", ResourceUtils.groupFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
        Assertions.assertEquals("foo", ResourceUtils.groupFromResourceId("subscriptions/123/resourcegroups/foo/providers/Microsoft.Bar/bars/bar1"));
        Assertions.assertNull(ResourceUtils.groupFromResourceId(null));
    }

    @Test
    public void canExtractResourceProviderFromResourceId() {
        Assertions.assertEquals("Microsoft.Bar", ResourceUtils.resourceProviderFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
        Assertions.assertNull(ResourceUtils.resourceProviderFromResourceId(null));
    }

    @Test
    public void canExtractParentPathFromId() throws Exception {
        Assertions.assertEquals("/subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1", ResourceUtils.parentResourceIdFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1/bazs/baz1"));
        Assertions.assertNull(ResourceUtils.parentResourceIdFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
    }

    @Test
    public void canExtractRelativePathFromId() throws Exception {
        Assertions.assertEquals("bars/bar1", ResourceUtils.relativePathFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
        Assertions.assertEquals("", ResourceUtils.parentRelativePathFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
        Assertions.assertEquals("bars/bar1/providers/provider1", ResourceUtils.relativePathFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1/providers/provider1"));
        Assertions.assertEquals("providers/provider1/bars/bar1", ResourceUtils.relativePathFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/providers/provider1/bars/bar1"));
    }

    @Test
    public void canGetDefaultScopeFromUrl() throws Exception {
        Assertions.assertEquals("https://graph.windows.net/.default", ResourceManagerUtils.getDefaultScopeFromUrl("https://graph.windows.net/random", AzureEnvironment.AZURE));
        Assertions.assertEquals("https://vault.azure.net/.default", ResourceManagerUtils.getDefaultScopeFromUrl("https://random.vault.azure.net/random", AzureEnvironment.AZURE));
        Assertions.assertEquals("https://api.applicationinsights.io/.default", ResourceManagerUtils.getDefaultScopeFromUrl("https://api.applicationinsights.io/random", AzureEnvironment.AZURE));
        Assertions.assertEquals("https://api.loganalytics.io/.default", ResourceManagerUtils.getDefaultScopeFromUrl("https://api.loganalytics.io/random", AzureEnvironment.AZURE));
    }
}
