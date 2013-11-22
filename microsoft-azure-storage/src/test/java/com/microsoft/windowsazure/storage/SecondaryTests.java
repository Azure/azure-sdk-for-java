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
package com.microsoft.windowsazure.storage;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.storage.LocationMode;
import com.microsoft.windowsazure.storage.OperationContext;
import com.microsoft.windowsazure.storage.RetryContext;
import com.microsoft.windowsazure.storage.RetryInfo;
import com.microsoft.windowsazure.storage.RetryNoRetry;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.StorageLocation;
import com.microsoft.windowsazure.storage.blob.BlobRequestOptions;
import com.microsoft.windowsazure.storage.blob.BlobTestBase;
import com.microsoft.windowsazure.storage.blob.CloudBlobClient;
import com.microsoft.windowsazure.storage.blob.CloudBlobContainer;
import com.microsoft.windowsazure.storage.blob.CloudBlockBlob;
import com.microsoft.windowsazure.storage.blob.CloudPageBlob;
import com.microsoft.windowsazure.storage.blob.DeleteSnapshotsOption;
import com.microsoft.windowsazure.storage.queue.CloudQueue;
import com.microsoft.windowsazure.storage.queue.CloudQueueClient;
import com.microsoft.windowsazure.storage.queue.QueueRequestOptions;
import com.microsoft.windowsazure.storage.queue.QueueTestBase;
import com.microsoft.windowsazure.storage.table.CloudTable;
import com.microsoft.windowsazure.storage.table.CloudTableClient;
import com.microsoft.windowsazure.storage.table.TableRequestOptions;
import com.microsoft.windowsazure.storage.table.TableTestBase;

public class SecondaryTests extends TestBase {

    @Test
    public void testLocationModeWithMissingUri() throws URISyntaxException, StorageException {
        CloudBlobClient client = createCloudBlobClient();
        CloudBlobClient primaryOnlyClient = new CloudBlobClient(client.getEndpoint(), client.getCredentials());
        CloudBlobContainer container = primaryOnlyClient.getContainerReference("nonexistingcontainer");

        BlobRequestOptions options = new BlobRequestOptions();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        options.setRetryPolicyFactory(new RetryNoRetry());

        try {
            container.downloadAttributes(null, options, null);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(UnsupportedOperationException.class, ex.getCause().getClass());
        }

        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);

        try {
            container.downloadAttributes(null, options, null);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(UnsupportedOperationException.class, ex.getCause().getClass());
        }

        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);

        try {
            container.downloadAttributes(null, options, null);
            fail();
        }
        catch (StorageException ex) {
            assertEquals(UnsupportedOperationException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testBlobIfExistsShouldNotHitSecondary() throws StorageException, URISyntaxException {
        CloudBlobContainer container = BlobTestBase.getRandomContainerReference();

        BlobRequestOptions options = new BlobRequestOptions();

        // CreateIfNotExists
        OperationContext context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_ONLY);
        container.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        container.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        container.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        try {
            container.createIfNotExists(options, context);
        }
        catch (StorageException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        }

        // DeleteIfExists
        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_ONLY);
        container.deleteIfExists(null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        container.deleteIfExists(null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        container.deleteIfExists(null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        try {
            container.deleteIfExists(null, options, context);
        }
        catch (StorageException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        }

        CloudBlockBlob blockBlob = container.getBlockBlobReference("blob1");

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_ONLY);
        blockBlob.deleteIfExists(DeleteSnapshotsOption.NONE, null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        blockBlob.deleteIfExists(DeleteSnapshotsOption.NONE, null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        blockBlob.deleteIfExists(DeleteSnapshotsOption.NONE, null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        try {
            blockBlob.deleteIfExists(DeleteSnapshotsOption.NONE, null, options, context);
        }
        catch (StorageException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        }

        CloudPageBlob pageBlob = container.getPageBlobReference("blob2");

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_ONLY);
        pageBlob.deleteIfExists(DeleteSnapshotsOption.NONE, null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        pageBlob.deleteIfExists(DeleteSnapshotsOption.NONE, null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        pageBlob.deleteIfExists(DeleteSnapshotsOption.NONE, null, options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        try {
            pageBlob.deleteIfExists(DeleteSnapshotsOption.NONE, null, options, context);
        }
        catch (StorageException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testQueueIfExistsShouldNotHitSecondary() throws StorageException, URISyntaxException {

        CloudQueueClient client = createCloudQueueClient();

        CloudQueue queue = client.getQueueReference("nonexistantqueue");

        QueueRequestOptions options = new QueueRequestOptions();

        // CreateIfNotExists
        OperationContext context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_ONLY);
        queue.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        queue.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        queue.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        try {
            queue.createIfNotExists(options, context);
        }
        catch (StorageException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        }

        // DeleteIfExists
        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_ONLY);
        queue.deleteIfExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        queue.deleteIfExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        queue.deleteIfExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        try {
            queue.deleteIfExists(options, context);
        }
        catch (StorageException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testTableIfExistsShouldNotHitSecondary() throws StorageException, URISyntaxException {
        CloudTableClient client = createCloudTableClient();
        CloudTable table = client.getTableReference("nonexistanttable");

        TableRequestOptions options = new TableRequestOptions();

        // CreateIfNotExists
        OperationContext context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_ONLY);
        table.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        table.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        table.createIfNotExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        try {
            table.createIfNotExists(options, context);
        }
        catch (StorageException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        }

        // DeleteIfExists
        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_ONLY);
        table.deleteIfExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.PRIMARY_THEN_SECONDARY);
        table.deleteIfExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_THEN_PRIMARY);
        table.deleteIfExists(options, context);
        assertEquals(StorageLocation.PRIMARY, context.getRequestResults().get(0).getTargetLocation());

        context = new OperationContext();
        options.setLocationMode(LocationMode.SECONDARY_ONLY);
        try {
            table.deleteIfExists(options, context);
        }
        catch (StorageException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void testMultiLocationRetriesBlob() throws URISyntaxException, StorageException {
        List<RetryInfo> retryInfoList = new ArrayList<RetryInfo>();
        List<RetryContext> retryContextList = new ArrayList<RetryContext>();

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));

        // Next location is set to primary and location mode is primary only.
        testContainerDownloadAttributes(LocationMode.PRIMARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);
        testContainerDownloadAttributes(null, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));

        // Next location is set to secondary and location mode is secondary only.
        testContainerDownloadAttributes(LocationMode.SECONDARY_ONLY, LocationMode.PRIMARY_ONLY,
                StorageLocation.SECONDARY, retryContextList, retryInfoList);
        testContainerDownloadAttributes(null, LocationMode.SECONDARY_ONLY, StorageLocation.SECONDARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));

        // Next location is set to secondary and location mode is PRIMARY_THEN_SECONDARY.
        testContainerDownloadAttributes(LocationMode.PRIMARY_THEN_SECONDARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.PRIMARY, retryContextList, retryInfoList);
        testContainerDownloadAttributes(null, LocationMode.PRIMARY_THEN_SECONDARY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));

        // Next location is set to primary and location mode is SECONDARY_THEN_PRIMARY.
        testContainerDownloadAttributes(LocationMode.SECONDARY_THEN_PRIMARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.SECONDARY, retryContextList, retryInfoList);
        testContainerDownloadAttributes(null, LocationMode.SECONDARY_THEN_PRIMARY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testContainerDownloadAttributes(LocationMode.PRIMARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);
        testContainerDownloadAttributes(null, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY,
                6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY,
                1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testContainerDownloadAttributes(LocationMode.SECONDARY_ONLY, LocationMode.PRIMARY_ONLY,
                StorageLocation.SECONDARY, retryContextList, retryInfoList);
        testContainerDownloadAttributes(null, LocationMode.SECONDARY_ONLY, StorageLocation.SECONDARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY,
                LocationMode.PRIMARY_THEN_SECONDARY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_THEN_SECONDARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY,
                LocationMode.PRIMARY_THEN_SECONDARY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testContainerDownloadAttributes(LocationMode.PRIMARY_THEN_SECONDARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.PRIMARY, retryContextList, retryInfoList);
        testContainerDownloadAttributes(null, LocationMode.PRIMARY_THEN_SECONDARY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY,
                LocationMode.SECONDARY_THEN_PRIMARY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_THEN_PRIMARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY,
                LocationMode.SECONDARY_THEN_PRIMARY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testContainerDownloadAttributes(LocationMode.SECONDARY_THEN_PRIMARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.SECONDARY, retryContextList, retryInfoList);
        testContainerDownloadAttributes(null, LocationMode.SECONDARY_THEN_PRIMARY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);
    }

    @Test
    public void testMultiLocationRetriesQueue() throws URISyntaxException, StorageException {
        List<RetryInfo> retryInfoList = new ArrayList<RetryInfo>();
        List<RetryContext> retryContextList = new ArrayList<RetryContext>();

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));

        // Next location is set to primary and location mode is primary only.
        testQueueDownloadAttributes(LocationMode.PRIMARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);
        testQueueDownloadAttributes(null, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));

        // Next location is set to secondary and location mode is secondary only.
        testQueueDownloadAttributes(LocationMode.SECONDARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);
        testQueueDownloadAttributes(null, LocationMode.SECONDARY_ONLY, StorageLocation.SECONDARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));

        // Next location is set to secondary and location mode is PRIMARY_THEN_SECONDARY.
        testQueueDownloadAttributes(LocationMode.PRIMARY_THEN_SECONDARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.PRIMARY, retryContextList, retryInfoList);
        testQueueDownloadAttributes(null, LocationMode.PRIMARY_THEN_SECONDARY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));

        // Next location is set to primary and location mode is SECONDARY_THEN_PRIMARY.
        testQueueDownloadAttributes(LocationMode.SECONDARY_THEN_PRIMARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.SECONDARY, retryContextList, retryInfoList);
        testQueueDownloadAttributes(null, LocationMode.SECONDARY_THEN_PRIMARY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testQueueDownloadAttributes(LocationMode.PRIMARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);
        testQueueDownloadAttributes(null, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY,
                6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY,
                1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testQueueDownloadAttributes(LocationMode.SECONDARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);
        testQueueDownloadAttributes(null, LocationMode.SECONDARY_ONLY, StorageLocation.SECONDARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY,
                LocationMode.PRIMARY_THEN_SECONDARY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_THEN_SECONDARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY,
                LocationMode.PRIMARY_THEN_SECONDARY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testQueueDownloadAttributes(LocationMode.PRIMARY_THEN_SECONDARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.PRIMARY, retryContextList, retryInfoList);
        testQueueDownloadAttributes(null, LocationMode.PRIMARY_THEN_SECONDARY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY,
                LocationMode.SECONDARY_THEN_PRIMARY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_THEN_PRIMARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY,
                LocationMode.SECONDARY_THEN_PRIMARY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testQueueDownloadAttributes(LocationMode.SECONDARY_THEN_PRIMARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.SECONDARY, retryContextList, retryInfoList);
        testQueueDownloadAttributes(null, LocationMode.SECONDARY_THEN_PRIMARY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);
    }

    @Test
    public void testMultiLocationRetriesTable() throws URISyntaxException, StorageException {
        List<RetryInfo> retryInfoList = new ArrayList<RetryInfo>();
        List<RetryContext> retryContextList = new ArrayList<RetryContext>();

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));

        // Next location is set to primary and location mode is primary only.
        testTableDownloadPermissions(LocationMode.PRIMARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);
        testTableDownloadPermissions(null, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));

        // Next location is set to secondary and location mode is secondary only.
        testTableDownloadPermissions(LocationMode.SECONDARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);
        testTableDownloadPermissions(null, LocationMode.SECONDARY_ONLY, StorageLocation.SECONDARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));

        // Next location is set to secondary and location mode is PRIMARY_THEN_SECONDARY.
        testTableDownloadPermissions(LocationMode.PRIMARY_THEN_SECONDARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.PRIMARY, retryContextList, retryInfoList);
        testTableDownloadPermissions(null, LocationMode.PRIMARY_THEN_SECONDARY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));

        // Next location is set to primary and location mode is SECONDARY_THEN_PRIMARY.
        testTableDownloadPermissions(LocationMode.SECONDARY_THEN_PRIMARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.SECONDARY, retryContextList, retryInfoList);
        testTableDownloadPermissions(null, LocationMode.SECONDARY_THEN_PRIMARY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testTableDownloadPermissions(LocationMode.PRIMARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);
        testTableDownloadPermissions(null, LocationMode.PRIMARY_ONLY, StorageLocation.PRIMARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY,
                6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY,
                1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testTableDownloadPermissions(LocationMode.SECONDARY_ONLY, LocationMode.PRIMARY_ONLY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);
        testTableDownloadPermissions(null, LocationMode.SECONDARY_ONLY, StorageLocation.SECONDARY, retryContextList,
                retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY,
                LocationMode.PRIMARY_THEN_SECONDARY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_THEN_SECONDARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY,
                LocationMode.PRIMARY_THEN_SECONDARY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.PRIMARY_THEN_SECONDARY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testTableDownloadPermissions(LocationMode.PRIMARY_THEN_SECONDARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.PRIMARY, retryContextList, retryInfoList);
        testTableDownloadPermissions(null, LocationMode.PRIMARY_THEN_SECONDARY, StorageLocation.PRIMARY,
                retryContextList, retryInfoList);

        retryContextList.clear();
        retryInfoList.clear();
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY,
                LocationMode.SECONDARY_THEN_PRIMARY, 6000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_THEN_PRIMARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY,
                LocationMode.SECONDARY_THEN_PRIMARY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));
        AddUpdatedLocationModes(retryContextList, retryInfoList);

        // Use AlwaysRetry and retry the request multiple times based on the retry context and retry info lists created above.
        testTableDownloadPermissions(LocationMode.SECONDARY_THEN_PRIMARY, LocationMode.PRIMARY_ONLY,
                StorageLocation.SECONDARY, retryContextList, retryInfoList);
        testTableDownloadPermissions(null, LocationMode.SECONDARY_THEN_PRIMARY, StorageLocation.SECONDARY,
                retryContextList, retryInfoList);
    }

    private static void AddUpdatedLocationModes(List<RetryContext> retryContextList, List<RetryInfo> retryInfoList) {
        retryInfoList
                .add(populateRetryInfo(new RetryInfo(), StorageLocation.PRIMARY, LocationMode.SECONDARY_ONLY, 4000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.SECONDARY, LocationMode.SECONDARY_ONLY));
        retryInfoList
                .add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY, LocationMode.PRIMARY_ONLY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_ONLY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY,
                LocationMode.PRIMARY_THEN_SECONDARY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.PRIMARY_THEN_SECONDARY));
        retryInfoList.add(populateRetryInfo(new RetryInfo(), StorageLocation.SECONDARY,
                LocationMode.SECONDARY_THEN_PRIMARY, 1000));
        retryContextList.add(new RetryContext(0, null, StorageLocation.PRIMARY, LocationMode.SECONDARY_THEN_PRIMARY));
    }

    private static RetryInfo populateRetryInfo(RetryInfo retryInfo, StorageLocation location,
            LocationMode locationMode, int interval) {
        retryInfo.setTargetLocation(location);
        retryInfo.setUpdatedLocationMode(locationMode);
        retryInfo.setRetryInterval(interval);
        return retryInfo;
    }

    private static void testContainerDownloadAttributes(LocationMode optionsLocationMode,
            LocationMode clientLocationMode, StorageLocation initialLocation, List<RetryContext> retryContextList,
            List<RetryInfo> retryInfoList) throws URISyntaxException, StorageException {
        CloudBlobContainer container = BlobTestBase.getRandomContainerReference();
        MultiLocationTestHelper helper = new MultiLocationTestHelper(container.getServiceClient().getStorageUri(),
                initialLocation, retryContextList, retryInfoList);

        container.getServiceClient().setLocationMode(clientLocationMode);
        BlobRequestOptions options = new BlobRequestOptions();

        options.setLocationMode(optionsLocationMode);
        options.setRetryPolicyFactory(helper.retryPolicy);

        try {
            container.downloadAttributes(null, options, helper.operationContext);
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
        }
        finally {
            helper.close();
        }
    }

    private static void testQueueDownloadAttributes(LocationMode optionsLocationMode, LocationMode clientLocationMode,
            StorageLocation initialLocation, List<RetryContext> retryContextList, List<RetryInfo> retryInfoList)
            throws URISyntaxException, StorageException {
        CloudQueueClient client = createCloudQueueClient();
        CloudQueue queue = client.getQueueReference(QueueTestBase.generateRandomQueueName());

        MultiLocationTestHelper helper = new MultiLocationTestHelper(queue.getServiceClient().getStorageUri(),
                initialLocation, retryContextList, retryInfoList);

        queue.getServiceClient().setLocationMode(clientLocationMode);
        QueueRequestOptions options = new QueueRequestOptions();

        options.setLocationMode(optionsLocationMode);
        options.setRetryPolicyFactory(helper.retryPolicy);

        try {
            queue.downloadAttributes(options, helper.operationContext);
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
        }
        finally {
            helper.close();
        }
    }

    private static void testTableDownloadPermissions(LocationMode optionsLocationMode, LocationMode clientLocationMode,
            StorageLocation initialLocation, List<RetryContext> retryContextList, List<RetryInfo> retryInfoList)
            throws URISyntaxException, StorageException {
        CloudTableClient client = createCloudTableClient();
        CloudTable table = client.getTableReference(TableTestBase.generateRandomTableName());

        MultiLocationTestHelper helper = new MultiLocationTestHelper(table.getServiceClient().getStorageUri(),
                initialLocation, retryContextList, retryInfoList);

        table.getServiceClient().setLocationMode(clientLocationMode);
        TableRequestOptions options = new TableRequestOptions();

        options.setLocationMode(optionsLocationMode);
        options.setRetryPolicyFactory(helper.retryPolicy);

        try {
            table.downloadPermissions(options, helper.operationContext);
        }
        catch (StorageException ex) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getHttpStatusCode());
        }
        finally {
            helper.close();
        }
    }
}
