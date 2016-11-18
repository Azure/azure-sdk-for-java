package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class ResourceUtilsTests {
    @Test
    public void canExtractGroupFromId() throws Exception {
        Assert.assertEquals("foo", ResourceUtils.groupFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
        Assert.assertEquals("foo", ResourceUtils.groupFromResourceId("subscriptions/123/resourcegroups/foo/providers/Microsoft.Bar/bars/bar1"));
    }

    @Test
    public void canExtractParentPathFromId() throws Exception {
        Assert.assertEquals("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1", ResourceUtils.parentResourceIdFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1/bazs/baz1"));
        Assert.assertNull(ResourceUtils.parentResourceIdFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
    }

    @Test
    public void canExtractRelativePathFromid() throws Exception {
        Assert.assertEquals("bars/bar1", ResourceUtils.relativePathFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
        Assert.assertEquals("", ResourceUtils.parentRelativePathFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1"));
        Assert.assertEquals("bars/bar1/providers/provider1", ResourceUtils.relativePathFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/bars/bar1/providers/provider1"));
        Assert.assertEquals("providers/provider1/bars/bar1", ResourceUtils.relativePathFromResourceId("subscriptions/123/resourceGroups/foo/providers/Microsoft.Bar/providers/provider1/bars/bar1"));
    }

    @Test
    public void canDownloadFile() throws Exception {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://microsoft.com").addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build();
        byte[] content = Utils.downloadFileAsync("http://google.com/humans.txt", retrofit).toBlocking().single();
        String contentString = new String(content);
        Assert.assertNotNull(contentString);
    }
}
