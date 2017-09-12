package com.microsoft.azure.v2;

import com.microsoft.azure.v2.http.MockAzureHttpClient;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.serializer.JacksonAdapter;
import com.microsoft.rest.v2.annotations.ExpectedResponses;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.annotations.PathParam;
import org.junit.Test;
import rx.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AzureProxyTests {


    @Host("https://mock.azure.com")
    private interface MockResourceService {
        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        MockResource get(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @GET("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        Single<MockResource> getAsync(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        MockResource create(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        MockResource createWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        MockResource createWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsRemaining);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        Single<MockResource> createAsync(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        Single<MockResource> createAsyncWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @PUT("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Single<MockResource> createAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);
    }

    @Test
    public void syncGet() {
        final MockResource resource = createMockService(MockResourceService.class)
                .get("1", "mine", "a");
        assertNotNull(resource);
        assertEquals("a", resource.name);
    }

    @Test
    public void asyncGet() {
        final MockResource resource = createMockService(MockResourceService.class)
                .getAsync("1", "mine", "b")
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("b", resource.name);
    }

    @Test
    public void syncCreate() {
        final MockResource resource = createMockService(MockResourceService.class)
                .create("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name);
    }

    @Test
    public void syncCreateWithLocation() {
        final MockResource resource = createMockService(MockResourceService.class)
                .createWithLocation("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name);
    }

    @Test
    public void syncCreateWithLocationAndPolls() {
        final MockResource resource = createMockService(MockResourceService.class)
                .createWithLocationAndPolls("1", "mine", "c", 2);
        assertNotNull(resource);
        assertEquals("c", resource.name);
    }

    @Test
    public void asyncCreate() {
        final MockResource resource = createMockService(MockResourceService.class)
                .createAsync("1", "mine", "c")
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);
    }

    @Test
    public void asyncCreateWithLocation() {
        final MockResource resource = createMockService(MockResourceService.class)
                .createAsyncWithLocation("1", "mine", "c")
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);
    }

    @Test
    public void asyncCreateWithLocationAndPolls() {
        final MockResource resource = createMockService(MockResourceService.class)
                .createAsyncWithLocationAndPolls("1", "mine", "c", 2)
                .toBlocking().value();
        assertNotNull(resource);
        assertEquals("c", resource.name);
    }

    private <T> T createMockService(Class<T> serviceClass) {
        final MockAzureHttpClient mockHttpClient = new MockAzureHttpClient();
        return AzureProxy.create(serviceClass, mockHttpClient, serializer);
    }

    private static final SerializerAdapter<?> serializer = new JacksonAdapter();
}
