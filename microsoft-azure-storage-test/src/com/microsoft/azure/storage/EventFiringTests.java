/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.Assert.*;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class EventFiringTests {

    @Test
    public void testSendingRequestEvents() throws URISyntaxException, StorageException {
        final ArrayList<Boolean> callList = new ArrayList<Boolean>();
        final ArrayList<Boolean> globalCallList = new ArrayList<Boolean>();

        OperationContext eventContext = new OperationContext();

        eventContext.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                callList.add(true);
            }
        });

        OperationContext.getGlobalSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                globalCallList.add(true);
            }
        });

        assertEquals(0, callList.size());
        assertEquals(0, globalCallList.size());

        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");

        // make sure both update
        container.exists(null, null, eventContext);
        assertEquals(1, callList.size());
        assertEquals(1, globalCallList.size());

        // make sure only global updates
        container.exists();
        assertEquals(1, callList.size());
        assertEquals(2, globalCallList.size());

        OperationContext
                .setGlobalSendingRequestEventHandler(new StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>>());
        eventContext
                .setSendingRequestEventHandler(new StorageEventMultiCaster<SendingRequestEvent, StorageEvent<SendingRequestEvent>>());

        // make sure neither update
        container.exists(null, null, eventContext);
        assertEquals(1, callList.size());
        assertEquals(2, globalCallList.size());
    }

    @Test
    public void testResponseReceivedEvents() throws URISyntaxException, StorageException {
        final ArrayList<Boolean> callList = new ArrayList<Boolean>();
        final ArrayList<Boolean> globalCallList = new ArrayList<Boolean>();

        OperationContext eventContext = new OperationContext();

        eventContext.getResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                callList.add(true);
            }
        });

        OperationContext.getGlobalResponseReceivedEventHandler().addListener(new StorageEvent<ResponseReceivedEvent>() {

            @Override
            public void eventOccurred(ResponseReceivedEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                globalCallList.add(true);
            }
        });

        assertEquals(0, callList.size());
        assertEquals(0, globalCallList.size());

        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");

        // make sure both update
        container.exists(null, null, eventContext);
        assertEquals(1, callList.size());
        assertEquals(1, globalCallList.size());

        // make sure only global updates
        container.exists();
        assertEquals(1, callList.size());
        assertEquals(2, globalCallList.size());

        OperationContext
                .setGlobalResponseReceivedEventHandler(new StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>>());
        eventContext
                .setResponseReceivedEventHandler(new StorageEventMultiCaster<ResponseReceivedEvent, StorageEvent<ResponseReceivedEvent>>());

        // make sure neither update
        container.exists(null, null, eventContext);
        assertEquals(1, callList.size());
        assertEquals(2, globalCallList.size());
    }

    @Test
    public void testRequestCompletedEvents() throws URISyntaxException, StorageException {
        final ArrayList<Boolean> callList = new ArrayList<Boolean>();
        final ArrayList<Boolean> globalCallList = new ArrayList<Boolean>();

        OperationContext eventContext = new OperationContext();

        eventContext.getRequestCompletedEventHandler().addListener(new StorageEvent<RequestCompletedEvent>() {

            @Override
            public void eventOccurred(RequestCompletedEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                callList.add(true);
            }
        });

        OperationContext.getGlobalRequestCompletedEventHandler().addListener(new StorageEvent<RequestCompletedEvent>() {

            @Override
            public void eventOccurred(RequestCompletedEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                globalCallList.add(true);
            }
        });

        assertEquals(0, callList.size());
        assertEquals(0, globalCallList.size());

        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");

        // make sure both update
        container.exists(null, null, eventContext);
        assertEquals(1, callList.size());
        assertEquals(1, globalCallList.size());

        // make sure only global updates
        container.exists();
        assertEquals(1, callList.size());
        assertEquals(2, globalCallList.size());

        OperationContext
                .setGlobalRequestCompletedEventHandler(new StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>>());
        eventContext
                .setRequestCompletedEventHandler(new StorageEventMultiCaster<RequestCompletedEvent, StorageEvent<RequestCompletedEvent>>());

        // make sure neither update
        container.exists(null, null, eventContext);
        assertEquals(1, callList.size());
        assertEquals(2, globalCallList.size());
    }

    @Test
    @Category(SecondaryTests.class)
    public void testRetryingEvents() throws URISyntaxException, StorageException {
        final ArrayList<Boolean> callList = new ArrayList<Boolean>();
        final ArrayList<Boolean> globalCallList = new ArrayList<Boolean>();

        OperationContext eventContext = new OperationContext();

        eventContext.getRetryingEventHandler().addListener(new StorageEvent<RetryingEvent>() {

            @Override
            public void eventOccurred(RetryingEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                callList.add(true);
            }
        });

        OperationContext.getGlobalRetryingEventHandler().addListener(new StorageEvent<RetryingEvent>() {

            @Override
            public void eventOccurred(RetryingEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                globalCallList.add(true);
            }
        });

        assertEquals(0, callList.size());
        assertEquals(0, globalCallList.size());

        // This will cause a retry to occur when a 404 is returned
        BlobRequestOptions eventOptions = new BlobRequestOptions();
        eventOptions.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);

        RetryLinearRetry retry = new RetryLinearRetry();
        retry.maximumAttempts = 1;
        eventOptions.setRetryPolicyFactory(retry);

        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");

        // make sure both update
        try {
            container.downloadAttributes(null, eventOptions, eventContext);
            fail();
        }
        catch (StorageException e) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getHttpStatusCode());
            assertEquals("The specified container does not exist.", e.getMessage());
            assertEquals(StorageErrorCode.RESOURCE_NOT_FOUND.toString(), e.getErrorCode());
        }
        assertEquals(1, callList.size());
        assertEquals(1, globalCallList.size());

        // make sure only global updates
        try {
            container.downloadAttributes(null, eventOptions, null);
            fail();
        }
        catch (StorageException e) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getHttpStatusCode());
            assertEquals("The specified container does not exist.", e.getMessage());
            assertEquals(StorageErrorCode.RESOURCE_NOT_FOUND.toString(), e.getErrorCode());
        }
        assertEquals(1, callList.size());
        assertEquals(2, globalCallList.size());

        OperationContext
                .setGlobalRetryingEventHandler(new StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>>());
        eventContext.setRetryingEventHandler(new StorageEventMultiCaster<RetryingEvent, StorageEvent<RetryingEvent>>());

        // make sure neither update
        try {
            container.downloadAttributes(null, eventOptions, eventContext);
            fail();
        }
        catch (StorageException e) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getHttpStatusCode());
            assertEquals("The specified container does not exist.", e.getMessage());
            assertEquals(StorageErrorCode.RESOURCE_NOT_FOUND.toString(), e.getErrorCode());
        }
        assertEquals(1, callList.size());
        assertEquals(2, globalCallList.size());
    }

    @Test
    @Category(SecondaryTests.class)
    public void testSendingRequestRequestCompletedPairs() throws URISyntaxException, StorageException {
        final ArrayList<Boolean> sendingCallList = new ArrayList<Boolean>();
        final ArrayList<Boolean> completedCallList = new ArrayList<Boolean>();

        OperationContext eventContext = new OperationContext();

        eventContext.getSendingRequestEventHandler().addListener(new StorageEvent<SendingRequestEvent>() {

            @Override
            public void eventOccurred(SendingRequestEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                sendingCallList.add(true);
            }
        });

        eventContext.getRequestCompletedEventHandler().addListener(new StorageEvent<RequestCompletedEvent>() {

            @Override
            public void eventOccurred(RequestCompletedEvent eventArg) {
                assertEquals(eventArg.getRequestResult(), eventArg.getOpContext().getLastResult());
                completedCallList.add(true);
            }
        });

        assertEquals(0, sendingCallList.size());
        assertEquals(0, completedCallList.size());

        // This will cause a retry to occur when a 404 is returned
        BlobRequestOptions eventOptions = new BlobRequestOptions();
        eventOptions.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);

        RetryLinearRetry retry = new RetryLinearRetry();
        retry.maximumAttempts = 1;
        eventOptions.setRetryPolicyFactory(retry);

        CloudBlobClient blobClient = TestHelper.createCloudBlobClient();
        CloudBlobContainer container = blobClient.getContainerReference("container1");

        // make sure both update even if an exception is thrown and a retry occurs
        try {
            container.downloadAttributes(null, eventOptions, eventContext);
            fail();
        }
        catch (StorageException e) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, e.getHttpStatusCode());
            assertEquals("The specified container does not exist.", e.getMessage());
            assertEquals(StorageErrorCode.RESOURCE_NOT_FOUND.toString(), e.getErrorCode());
        }

        assertEquals(2, sendingCallList.size());
        assertEquals(2, completedCallList.size());

        // make sure neither update if an exception is thrown before sending occurs
        try {
            eventOptions.setLocationMode(LocationMode.SECONDARY_ONLY);
            container.createIfNotExists(eventOptions, eventContext);
            fail();
        }
        catch (StorageException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals(SR.PRIMARY_ONLY_COMMAND, e.getCause().getMessage());
        }

        assertEquals(2, sendingCallList.size());
        assertEquals(2, completedCallList.size());
    }
}
