package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import org.junit.Assert;
import org.junit.Test;

public class ResourceUtilsTests {
    @Test
    public void canExtractGroupFromId() throws Exception {
        Assert.assertEquals("foo", ResourceUtils.groupFromResourceId("subscriptions/123/resourceGroups/foo/Microsoft.Bar/bars/bar1"));
        Assert.assertEquals("foo", ResourceUtils.groupFromResourceId("subscriptions/123/resourcegroups/foo/Microsoft.Bar/bars/bar1"));
    }
}
