// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.implementation.annotation.Delete;
import com.azure.core.implementation.annotation.ExpectedResponses;
import com.azure.core.implementation.annotation.Get;
import com.azure.core.implementation.annotation.Host;
import com.azure.core.implementation.annotation.Put;
import com.azure.core.implementation.annotation.PathParam;
import com.azure.core.implementation.annotation.ResumeOperation;
import com.azure.core.implementation.annotation.ServiceInterface;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.OperationDescription;
import com.azure.core.implementation.exception.InvalidReturnTypeException;
import com.azure.core.management.MockResource;
import com.azure.core.management.http.MockAzureHttpClient;
import com.azure.core.test.http.MockHttpResponse;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AzureProxyTests {
    private long delayInMillisecondsBackup;

    @Before
    public void beforeTest() {
        delayInMillisecondsBackup = AzureProxy.defaultDelayInMilliseconds();
        AzureProxy.setDefaultPollingDelayInMilliseconds(0);
    }

    @After
    public void afterTest() {
        AzureProxy.setDefaultPollingDelayInMilliseconds(delayInMillisecondsBackup);
    }

    @Host("https://mock.azure.com")
    @ServiceInterface(name = "MockResourceService")
    private interface MockResourceService {
        @Get("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        MockResource get(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Get("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        Mono<MockResource> getAsync(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        MockResource create(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        MockResource createWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        MockResource createWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsRemaining);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation")
        @ExpectedResponses({200})
        MockResource createWithAzureAsyncOperation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        MockResource createWithAzureAsyncOperationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsRemaining);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState")
        @ExpectedResponses({200})
        MockResource createWithProvisioningState(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        MockResource createWithProvisioningStateAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsRemaining);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        Mono<MockResource> createAsync(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        Mono<MockResource> createAsyncWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Mono<MockResource> createAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation")
        @ExpectedResponses({200})
        Mono<MockResource> createAsyncWithAzureAsyncOperation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Mono<MockResource> createAsyncWithAzureAsyncOperationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState")
        @ExpectedResponses({200})
        Mono<MockResource> createAsyncWithProvisioningState(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Mono<MockResource> createAsyncWithProvisioningStateAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Flux<MockResource> beginCreateAsyncWithBadReturnType(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining=1&InitialResponseStatusCode=294")
        @ExpectedResponses({200})
        Flux<OperationStatus<MockResource>> beginCreateAsyncWithLocationAndPollsAndUnexpectedStatusCode(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Flux<OperationStatus<MockResource>> beginCreateAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @ExpectedResponses({200})
        @ResumeOperation
        Flux<OperationStatus<MockResource>> resumeCreateAsyncWithLocationAndPolls(OperationDescription operationDescription);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Azure-AsyncOperation&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Flux<OperationStatus<MockResource>> beginCreateAsyncWithAzureAsyncOperationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Put("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=ProvisioningState&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Flux<OperationStatus<MockResource>> beginCreateAsyncWithProvisioningStateAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Delete("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        void delete(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Delete("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        void deleteWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Delete("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        void deleteWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Delete("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}")
        @ExpectedResponses({200})
        Mono<Void> deleteAsync(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Delete("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location")
        @ExpectedResponses({200})
        Mono<Void> deleteAsyncWithLocation(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName);

        @Delete("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Mono<Void> deleteAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Delete("subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/mockprovider/mockresources/{mockResourceName}?PollType=Location&PollsRemaining={pollsRemaining}")
        @ExpectedResponses({200})
        Flux<OperationStatus<Void>> beginDeleteAsyncWithLocationAndPolls(@PathParam("subscriptionId") String subscriptionId, @PathParam("resourceGroupName") String resourceGroupName, @PathParam("mockResourceName") String mockResourceName, @PathParam("pollsRemaining") int pollsUntilResource);

        @Delete("errors/403")
        @ExpectedResponses({200})
        Mono<Void> deleteAsyncWithForbiddenResponse();
    }

    @Test
    public void get() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .get("1", "mine", "a");
        assertNotNull(resource);
        assertEquals("a", resource.name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void getAsync() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .getAsync("1", "mine", "b")
                .block();
        assertNotNull(resource);
        assertEquals("b", resource.name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void create() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .create("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createWithLocation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResourceService mockService = createMockService(MockResourceService.class, httpClient);
        final MockResource resource = mockService
                .createWithLocation("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void createWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithLocationAndPolls("1", "mine", "c", 2);
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(2, httpClient.pollRequests());
    }

    @Test
    public void createWithAzureAsyncOperation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithAzureAsyncOperation("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void createWithAzureAsyncOperationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithAzureAsyncOperationAndPolls("1", "mine", "c", 2);
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(2, httpClient.pollRequests());
    }

    @Test
    public void createWithProvisioningState() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithProvisioningState("1", "mine", "c");
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createWithProvisioningStateAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createWithProvisioningStateAndPolls("1", "mine", "c", 3);
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(3, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createAsync() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsync("1", "mine", "c")
                .block();
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithLocation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithLocation("1", "mine", "c")
                .block();
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithLocationAndPolls("1", "mine", "c", 3)
                .block();
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithAzureAsyncOperation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithAzureAsyncOperation("1", "mine", "c")
                .block();
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithAzureAsyncOperationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithAzureAsyncOperationAndPolls("1", "mine", "c", 3)
                .block();
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    @Ignore("Test does not run in a stable fashion across Windows, MacOS, and Linux")
    public void createAsyncWithAzureAsyncOperationAndPollsWithDelay() throws InterruptedException {
        final long delayInMilliseconds = 100;
        AzureProxy.setDefaultPollingDelayInMilliseconds(delayInMilliseconds);

        final MockAzureHttpClient httpClient = new MockAzureHttpClient();
        final int pollsUntilResource = 3;
        createMockService(MockResourceService.class, httpClient)
                .createAsyncWithAzureAsyncOperationAndPolls("1", "mine", "c", pollsUntilResource)
                .subscribe();

        Thread.sleep((long) (delayInMilliseconds * 0.75));

        for (int i = 0; i < pollsUntilResource; ++i) {
            assertEquals(0, httpClient.getRequests());
            assertEquals(1, httpClient.createRequests());
            assertEquals(0, httpClient.deleteRequests());
            assertEquals(i, httpClient.pollRequests());

            Thread.sleep(delayInMilliseconds);
        }

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(pollsUntilResource, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithProvisioningState() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithProvisioningState("1", "mine", "c")
                .block();
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void createAsyncWithProvisioningStateAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResource resource = createMockService(MockResourceService.class, httpClient)
                .createAsyncWithProvisioningStateAndPolls("1", "mine", "c", 5)
                .block();
        assertNotNull(resource);
        assertEquals("c", resource.name());

        assertEquals(5, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithBadReturnType() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final MockResourceService service = createMockService(MockResourceService.class, httpClient);
        try {
            service.beginCreateAsyncWithBadReturnType("1", "mine", "c", 2);
            fail("Expected exception.");
        } catch (InvalidReturnTypeException e) {
            assertContains(e.getMessage(), "AzureProxyTests$MockResourceService.beginCreateAsyncWithBadReturnType()");
            assertContains(e.getMessage(), "reactor.core.publisher.Flux<com.azure.core.management.MockResource>");
        }

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests()); // Request won't reach HttpClient and fail in AzureProxy
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithLocationAndPollsAndUnexpectedStatusCode() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithLocationAndPollsAndUnexpectedStatusCode("1", "mine", "c")
                .subscribe(
                        new Consumer<OperationStatus<MockResource>>() {
                            @Override
                            public void accept(OperationStatus<MockResource> mockResourceOperationStatus) {
                                fail();
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                assertEquals("Status code 294, (empty body)", throwable.getMessage());
                                assertEquals(HttpResponseException.class, throwable.getClass());
                            }
                        });

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithLocationAndPolls("1", "mine", "c", 3)
                .subscribe(new Consumer<OperationStatus<MockResource>>() {
                    @Override
                    public void accept(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        } else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        assertEquals(2, inProgressCount.get());
        assertNotNull(resource.get());
        assertEquals("c", resource.get().name());

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void beginAndResumeCreateAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();
        final StringBuffer data = new StringBuffer();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithLocationAndPolls("1", "mine", "c", 10)
                .take(2)
                .subscribe(new Consumer<OperationStatus<MockResource>>() {
                    @Override
                    public void accept(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            OperationDescription operationDescription = operationStatus.buildDescription();
                            try {
                                data.append(mapper.writeValueAsString(operationDescription));
                            } catch (JsonProcessingException e) {
                                fail("Error serializing OperationDescription object");
                                e.printStackTrace();
                            }
                            inProgressCount.incrementAndGet();
                        } else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        OperationDescription operationDescription = null;
        PollStrategy.PollStrategyData pollData = null;
        try {
            operationDescription = mapper.readValue(data.toString(), OperationDescription.class);
            pollData = (PollStrategy.PollStrategyData) operationDescription.pollStrategyData();
        } catch (IOException e) {
            fail("Error deserializing OperationDescription object");
            e.printStackTrace();
        }

        assertNotNull(operationDescription);
        assertNotNull(pollData);

        createMockService(MockResourceService.class, httpClient)
                .resumeCreateAsyncWithLocationAndPolls(operationDescription)
                .subscribe(new Consumer<OperationStatus<MockResource>>() {
                    @Override
                    public void accept(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            OperationDescription operationDescription = operationStatus.buildDescription();
                            try {
                                data.append(mapper.writeValueAsString(operationDescription));
                            } catch (JsonProcessingException e) {
                                fail("Error serializing OperationDescription object");
                                e.printStackTrace();
                            }
                            inProgressCount.incrementAndGet();
                        } else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

    }

    @Test
    public void beginCreateAsyncWithAzureAsyncOperationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithAzureAsyncOperationAndPolls("1", "mine", "c", 3)
                .subscribe(new Consumer<OperationStatus<MockResource>>() {
                    @Override
                    public void accept(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        } else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        assertEquals(3, inProgressCount.get());
        assertNotNull(resource.get());
        assertEquals("c", resource.get().name());

        assertEquals(1, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithProvisioningStateAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithProvisioningStateAndPolls("1", "mine", "c", 4)
                .subscribe(new Consumer<OperationStatus<MockResource>>() {
                    @Override
                    public void accept(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        } else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        assertEquals(3, inProgressCount.get());
        assertNotNull(resource.get());
        assertEquals("c", resource.get().name());

        assertEquals(4, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void beginCreateAsyncWithLocationAndPollsWhenPollsUntilResourceIs0() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<MockResource> resource = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginCreateAsyncWithLocationAndPolls("1", "mine", "c", 0)
                .subscribe(new Consumer<OperationStatus<MockResource>>() {
                    @Override
                    public void accept(OperationStatus<MockResource> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        } else {
                            resource.set(operationStatus.result());
                        }
                    }
                });

        assertEquals(0, inProgressCount.get());
        assertNotNull(resource.get());
        assertEquals("c", resource.get().name());

        assertEquals(0, httpClient.getRequests());
        assertEquals(1, httpClient.createRequests());
        assertEquals(0, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void delete() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .delete("1", "mine", "c");

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void deleteWithLocation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteWithLocation("1", "mine", "c");

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void deleteWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteWithLocationAndPolls("1", "mine", "c", 4);

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(4, httpClient.pollRequests());
    }

    @Test
    public void deleteAsync() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteAsync("1", "mine", "c")
                .block();

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void deleteAsyncWithLocation() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteAsyncWithLocation("1", "mine", "c")
                .block();

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(1, httpClient.pollRequests());
    }

    @Test
    public void deleteAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        createMockService(MockResourceService.class, httpClient)
                .deleteAsyncWithLocationAndPolls("1", "mine", "c", 10)
                .block();

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(10, httpClient.pollRequests());
    }

    @Test
    public void beginDeleteAsyncWithLocationAndPolls() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<Boolean> completed = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginDeleteAsyncWithLocationAndPolls("1", "mine", "c", 3)
                .subscribe(new Consumer<OperationStatus<Void>>() {
                    @Override
                    public void accept(OperationStatus<Void> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        } else {
                            completed.set(true);
                        }
                    }
                });

        assertEquals(2, inProgressCount.get());
        assertNotNull(completed.get());
        assertTrue(completed.get());

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(3, httpClient.pollRequests());
    }

    @Test
    public void beginDeleteAsyncWithLocationAndPollsWhenPollsUntilResourceIs0() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient();

        final AtomicInteger inProgressCount = new AtomicInteger();
        final Value<Boolean> completed = new Value<>();

        createMockService(MockResourceService.class, httpClient)
                .beginDeleteAsyncWithLocationAndPolls("1", "mine", "c", 0)
                .subscribe(new Consumer<OperationStatus<Void>>() {
                    @Override
                    public void accept(OperationStatus<Void> operationStatus) {
                        if (!operationStatus.isDone()) {
                            inProgressCount.incrementAndGet();
                        } else {
                            completed.set(true);
                        }
                    }
                });

        assertEquals(0, inProgressCount.get());
        assertNotNull(completed.get());
        assertTrue(completed.get());

        assertEquals(0, httpClient.getRequests());
        assertEquals(0, httpClient.createRequests());
        assertEquals(1, httpClient.deleteRequests());
        assertEquals(0, httpClient.pollRequests());
    }

    @Test
    public void deleteAsyncWithForbiddenResponse() {
        final MockAzureHttpClient httpClient = new MockAzureHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.<HttpResponse>just(new MockHttpResponse(request, 403, MockAzureHttpClient.responseHeaders(), new byte[0]));
            }
        };

        final MockResourceService service = createMockService(MockResourceService.class, httpClient);
        try {
            service.deleteAsyncWithForbiddenResponse().block();
            fail("Expected RestException to be thrown.");
        } catch (HttpResponseException e) {
            assertEquals(403, e.response().statusCode());
            assertEquals("Status code 403, (empty body)", e.getMessage());
        }
    }

    private static <T> T createMockService(Class<T> serviceClass, MockAzureHttpClient httpClient) {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .build();

        return AzureProxy.create(serviceClass, null, pipeline);
    }

    private static void assertContains(String value, String expectedSubstring) {
        assertTrue("Expected \"" + value + "\" to contain \"" + expectedSubstring + "\".", value.contains(expectedSubstring));
    }

}
