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
package com.microsoft.windowsazure.services.blob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;
import com.microsoft.windowsazure.core.pipeline.jersey.ExponentialRetryPolicy;
import com.microsoft.windowsazure.core.pipeline.jersey.RetryPolicyFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ServiceFilter;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.blob.models.BlobProperties;
import com.microsoft.windowsazure.services.blob.models.BlockList;
import com.microsoft.windowsazure.services.blob.models.CreateBlobOptions;
import com.microsoft.windowsazure.services.blob.models.CreateBlobResult;
import com.microsoft.windowsazure.services.blob.models.CreateContainerOptions;
import com.microsoft.windowsazure.services.blob.models.GetBlobPropertiesResult;
import com.microsoft.windowsazure.services.blob.models.GetBlobResult;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksOptions;
import com.microsoft.windowsazure.services.blob.models.ListBlobBlocksResult;
import com.microsoft.windowsazure.services.blob.models.ListContainersOptions;
import com.microsoft.windowsazure.services.blob.models.ListContainersResult;
import com.microsoft.windowsazure.services.blob.models.ListContainersResult.Container;

public class BlobServiceIntegrationTest extends IntegrationTestBase {
    private static final String testContainersPrefix = "sdktest-";
    private static final String createableContainersPrefix = "csdktest-";
    private static String CREATEABLE_CONTAINER_1;
    private static String TEST_CONTAINER_FOR_BLOBS;
    private static String[] creatableContainers;
    private static String[] testContainers;
    private static boolean createdRoot;

    @BeforeClass
    public static void setup() throws Exception {
        // Setup container names array (list of container names used by
        // integration tests)
        testContainers = new String[10];
        for (int i = 0; i < testContainers.length; i++) {
            testContainers[i] = String.format("%s%d", testContainersPrefix,
                    i + 1);
        }

        creatableContainers = new String[10];
        for (int i = 0; i < creatableContainers.length; i++) {
            creatableContainers[i] = String.format("%s%d",
                    createableContainersPrefix, i + 1);
        }

        CREATEABLE_CONTAINER_1 = creatableContainers[0];
        TEST_CONTAINER_FOR_BLOBS = testContainers[0];
        // Create all test containers and their content
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        createContainers(service, testContainersPrefix, testContainers);

        try {
            service.createContainer("$root");
            createdRoot = true;
        } catch (ServiceException e) {
            // e.printStackTrace();
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        deleteContainers(service, testContainersPrefix, testContainers);
        deleteContainers(service, createableContainersPrefix,
                creatableContainers);

        // If container was created, delete it
        if (createdRoot) {
            try {
                service.deleteContainer("$root");
                createdRoot = false;
            } catch (ServiceException e) {
                // e.printStackTrace();
            }
        }
    }

    private static void createContainers(BlobContract service, String prefix,
            String[] list) throws Exception {
        Set<String> containers = listContainers(service, prefix);
        for (String item : list) {
            if (!containers.contains(item)) {
                service.createContainer(item);
            }
        }
    }

    private static void deleteContainers(BlobContract service, String prefix,
            String[] list) throws Exception {
        Set<String> containers = listContainers(service, prefix);
        for (String item : list) {
            if (containers.contains(item)) {
                service.deleteContainer(item);
            }
        }
    }

    private static Set<String> listContainers(BlobContract service,
            String prefix) throws Exception {
        HashSet<String> result = new HashSet<String>();
        ListContainersResult list = service
                .listContainers(new ListContainersOptions().setPrefix(prefix));
        for (Container item : list.getContainers()) {
            result.add(item.getName());
        }
        return result;
    }

    @Test
    public void createContainerWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        service.createContainer(CREATEABLE_CONTAINER_1);

        // Assert
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullContainerFail() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        service.createContainer(null);

        // Assert
        assertTrue(false);
    }

    @Test
    public void deleteContainerWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);
        String containerName = "deletecontainerworks";
        service.createContainer(containerName, new CreateContainerOptions()
                .setPublicAccess("blob").addMetadata("test", "bar")
                .addMetadata("blah", "bleah"));

        // Act
        service.deleteContainer(containerName);
        ListContainersResult listContainerResult = service.listContainers();

        // Assert
        for (Container container : listContainerResult.getContainers()) {
            assertTrue(!container.getName().equals(containerName));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullContainerFail() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        service.deleteContainer(null);

        // Assert
        assertTrue(false);
    }

    @Test
    public void listContainersWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        ListContainersResult results = service.listContainers();

        // Assert
        assertNotNull(results);
        assertTrue(testContainers.length <= results.getContainers().size());
        assertNotNull(results.getContainers().get(0).getName());
        assertNotNull(results.getContainers().get(0).getMetadata());
        assertNotNull(results.getContainers().get(0).getProperties());
        assertNotNull(results.getContainers().get(0).getProperties().getEtag());
        assertNotNull(results.getContainers().get(0).getProperties()
                .getLastModified());
        assertNotNull(results.getContainers().get(0).getUrl());
    }

    @Test
    public void listContainersWithPaginationWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        ListContainersResult results = service
                .listContainers(new ListContainersOptions().setMaxResults(3));
        ListContainersResult results2 = service
                .listContainers(new ListContainersOptions().setMarker(results
                        .getNextMarker()));

        // Assert
        assertNotNull(results);
        assertEquals(3, results.getContainers().size());
        assertNotNull(results.getNextMarker());
        assertEquals(3, results.getMaxResults());

        assertNotNull(results2);
        assertTrue(testContainers.length - 3 <= results2.getContainers().size());
        assertEquals("", results2.getNextMarker());
        assertEquals(0, results2.getMaxResults());
    }

    @Test
    public void listContainersWithPrefixWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        ListContainersResult results = service
                .listContainers(new ListContainersOptions().setPrefix(
                        testContainersPrefix).setMaxResults(3));
        // Assert
        assertNotNull(results);
        assertEquals(3, results.getContainers().size());
        assertNotNull(results.getNextMarker());
        assertEquals(3, results.getMaxResults());

        // Act
        ListContainersResult results2 = service
                .listContainers(new ListContainersOptions().setPrefix(
                        testContainersPrefix)
                        .setMarker(results.getNextMarker()));

        // Assert
        assertNotNull(results2);
        assertNotNull(results2.getNextMarker());
        assertEquals(0, results2.getMaxResults());

        // Act
        ListContainersResult results3 = service
                .listContainers(new ListContainersOptions()
                        .setPrefix(testContainersPrefix));

        // Assert
        assertEquals(results.getContainers().size()
                + results2.getContainers().size(), results3.getContainers()
                .size());
    }

    @Test
    public void listBlockBlobWithNoCommittedBlocksWorks() throws Exception {
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "listBlockBlobWithNoCommittedBlocksWorks";

        service.createBlockBlob(container, blob, null);
        service.createBlobBlock(container, blob, "01",
                new ByteArrayInputStream(new byte[] { 0x00 }));
        service.deleteBlob(container, blob);

        try {
            // Note: This next two lines should give a 404, because the blob no
            // longer
            // exists. However, the service sometimes allow this improper
            // access, so
            // the SDK has to handle the situation gracefully.
            service.createBlobBlock(container, blob, "01",
                    new ByteArrayInputStream(new byte[] { 0x00 }));
            ListBlobBlocksResult result = service.listBlobBlocks(container,
                    blob);
            assertEquals(0, result.getCommittedBlocks().size());
        } catch (ServiceException ex) {
            assertEquals(404, ex.getHttpStatusCode());
        }
    }

    @Test
    public void listBlobBlocksOnEmptyBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test13";
        String content = new String(new char[512]);
        service.createBlockBlob(container, blob, new ByteArrayInputStream(
                content.getBytes("UTF-8")));

        ListBlobBlocksResult result = service.listBlobBlocks(container, blob);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLastModified());
        assertNotNull(result.getEtag());
        assertEquals(512, result.getContentLength());
        assertNotNull(result.getCommittedBlocks());
        assertEquals(0, result.getCommittedBlocks().size());
        assertNotNull(result.getUncommittedBlocks());
        assertEquals(0, result.getUncommittedBlocks().size());
    }

    @Test
    public void listBlobBlocksWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test14";
        service.createBlockBlob(container, blob, null);
        service.createBlobBlock(container, blob, "123",
                new ByteArrayInputStream(new byte[256]));
        service.createBlobBlock(container, blob, "124",
                new ByteArrayInputStream(new byte[512]));
        service.createBlobBlock(container, blob, "125",
                new ByteArrayInputStream(new byte[195]));

        ListBlobBlocksResult result = service.listBlobBlocks(container, blob,
                new ListBlobBlocksOptions().setCommittedList(true)
                        .setUncommittedList(true));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLastModified());
        assertNotNull(result.getEtag());
        assertEquals(0, result.getContentLength());
        assertNotNull(result.getCommittedBlocks());
        assertEquals(0, result.getCommittedBlocks().size());
        assertNotNull(result.getUncommittedBlocks());
        assertEquals(3, result.getUncommittedBlocks().size());
        assertEquals("123", result.getUncommittedBlocks().get(0).getBlockId());
        assertEquals(256, result.getUncommittedBlocks().get(0).getBlockLength());
        assertEquals("124", result.getUncommittedBlocks().get(1).getBlockId());
        assertEquals(512, result.getUncommittedBlocks().get(1).getBlockLength());
        assertEquals("125", result.getUncommittedBlocks().get(2).getBlockId());
        assertEquals(195, result.getUncommittedBlocks().get(2).getBlockLength());
    }

    @Test
    public void listBlobBlocksWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test14";
        service.createBlockBlob(container, blob, null);
        service.createBlobBlock(container, blob, "123",
                new ByteArrayInputStream(new byte[256]));

        BlockList blockList = new BlockList();
        blockList.addUncommittedEntry("123");
        service.commitBlobBlocks(container, blob, blockList);

        service.createBlobBlock(container, blob, "124",
                new ByteArrayInputStream(new byte[512]));
        service.createBlobBlock(container, blob, "125",
                new ByteArrayInputStream(new byte[195]));

        ListBlobBlocksResult result1 = service.listBlobBlocks(container, blob,
                new ListBlobBlocksOptions().setCommittedList(true)
                        .setUncommittedList(true));
        ListBlobBlocksResult result2 = service.listBlobBlocks(container, blob,
                new ListBlobBlocksOptions().setCommittedList(true));
        ListBlobBlocksResult result3 = service.listBlobBlocks(container, blob,
                new ListBlobBlocksOptions().setUncommittedList(true));

        // Assert
        assertEquals(1, result1.getCommittedBlocks().size());
        assertEquals(2, result1.getUncommittedBlocks().size());

        assertEquals(1, result2.getCommittedBlocks().size());
        assertEquals(0, result2.getUncommittedBlocks().size());

        assertEquals(0, result3.getCommittedBlocks().size());
        assertEquals(2, result3.getUncommittedBlocks().size());
    }

    @Test
    public void commitBlobBlocksWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test14";
        String blockId1 = "1fedcba";
        String blockId2 = "2abcdef";
        String blockId3 = "3zzzzzz";
        service.createBlockBlob(container, blob, null);
        service.createBlobBlock(container, blob, blockId1,
                new ByteArrayInputStream(new byte[256]));
        service.createBlobBlock(container, blob, blockId2,
                new ByteArrayInputStream(new byte[512]));
        service.createBlobBlock(container, blob, blockId3,
                new ByteArrayInputStream(new byte[195]));

        BlockList blockList = new BlockList();
        blockList.addUncommittedEntry(blockId1).addLatestEntry(blockId3);
        service.commitBlobBlocks(container, blob, blockList);

        ListBlobBlocksResult result = service.listBlobBlocks(container, blob,
                new ListBlobBlocksOptions().setCommittedList(true)
                        .setUncommittedList(true));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLastModified());
        assertNotNull(result.getEtag());
        assertEquals(256 + 195, result.getContentLength());

        assertNotNull(result.getCommittedBlocks());
        assertEquals(2, result.getCommittedBlocks().size());
        assertEquals(blockId1, result.getCommittedBlocks().get(0).getBlockId());
        assertEquals(256, result.getCommittedBlocks().get(0).getBlockLength());
        assertEquals(blockId3, result.getCommittedBlocks().get(1).getBlockId());
        assertEquals(195, result.getCommittedBlocks().get(1).getBlockLength());

        assertNotNull(result.getUncommittedBlocks());
        assertEquals(0, result.getUncommittedBlocks().size());
    }

    @Test
    public void createBlobBlockWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test13";
        String content = new String(new char[512]);
        service.createBlockBlob(container, blob, new ByteArrayInputStream(
                content.getBytes("UTF-8")));
        service.createBlobBlock(container, blob, "123",
                new ByteArrayInputStream(content.getBytes("UTF-8")));
        service.createBlobBlock(container, blob, "124",
                new ByteArrayInputStream(content.getBytes("UTF-8")));

        // Assert
    }

    @Test
    public void createBlobBlockNullContainerWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String container = null;
        String blob = "createblobblocknullcontainerworks";
        String content = new String(new char[512]);
        service.createBlockBlob(container, blob, new ByteArrayInputStream(
                content.getBytes("UTF-8")));
        GetBlobPropertiesResult result = service.getBlobProperties(null, blob);
        GetBlobResult getBlobResult = service.getBlob(null, blob);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMetadata());
        assertEquals(0, result.getMetadata().size());
        BlobProperties props = result.getProperties();
        assertNotNull(props);

        assertEquals(content,
                inputStreamToString(getBlobResult.getContentStream(), "UTF-8"));
    }

    @Test
    public void createBlockBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test2",
                new ByteArrayInputStream("some content".getBytes()));

        // Assert
    }

    @Test
    public void createBlockBlobWithValidEtag() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        CreateBlobResult createBlobResult = service.createBlockBlob(
                TEST_CONTAINER_FOR_BLOBS, "test2", new ByteArrayInputStream(
                        "some content".getBytes()));

        // Assert
        assertNotNull(createBlobResult);
        assertNotNull(createBlobResult.getEtag());
    }

    @Test
    public void createBlockBlobWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String content = "some content";
        service.createBlockBlob(
                TEST_CONTAINER_FOR_BLOBS,
                "test2",
                new ByteArrayInputStream(content.getBytes("UTF-8")),
                new CreateBlobOptions()
                        .setBlobCacheControl("test")
                        .setBlobContentEncoding("UTF-8")
                        .setBlobContentLanguage("en-us")
                        /* .setBlobContentMD5("1234") */.setBlobContentType(
                                "text/plain")
                        .setCacheControl("test")
                        .setContentEncoding("UTF-8")
                        /* .setContentMD5("1234") */.setContentType(
                                "text/plain"));

        GetBlobPropertiesResult result = service.getBlobProperties(
                TEST_CONTAINER_FOR_BLOBS, "test2");

        // Assert
        assertNotNull(result);

        assertNotNull(result.getMetadata());
        assertEquals(0, result.getMetadata().size());

        BlobProperties props = result.getProperties();
        assertNotNull(props);
        assertEquals("test", props.getCacheControl());
        assertEquals("UTF-8", props.getContentEncoding());
        assertEquals("en-us", props.getContentLanguage());
        assertEquals("text/plain", props.getContentType());
        assertEquals(content.length(), props.getContentLength());
        assertNotNull(props.getEtag());
        assertNull(props.getContentMD5());
        assertNotNull(props.getLastModified());
        assertEquals("BlockBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(0, props.getSequenceNumber());
    }

    @Test
    public void getBlockBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String content = "some content";
        service.createBlockBlob(
                TEST_CONTAINER_FOR_BLOBS,
                "test2",
                new ByteArrayInputStream(content.getBytes("UTF-8")),
                new CreateBlobOptions()
                        .setBlobCacheControl("test")
                        .setBlobContentEncoding("UTF-8")
                        .setBlobContentLanguage("en-us")
                        /* .setBlobContentMD5("1234") */.setBlobContentType(
                                "text/plain")
                        .setCacheControl("test")
                        .setContentEncoding("UTF-8")
                        /* .setContentMD5("1234") */.setContentType(
                                "text/plain"));

        GetBlobResult result = service.getBlob(TEST_CONTAINER_FOR_BLOBS,
                "test2");

        // Assert
        assertNotNull(result);

        assertNotNull(result.getMetadata());
        assertEquals(0, result.getMetadata().size());

        BlobProperties props = result.getProperties();
        assertNotNull(props);
        assertEquals("test", props.getCacheControl());
        assertEquals("UTF-8", props.getContentEncoding());
        assertEquals("en-us", props.getContentLanguage());
        assertEquals("text/plain", props.getContentType());
        assertEquals(content.length(), props.getContentLength());
        assertNotNull(props.getEtag());
        assertNull(props.getContentMD5());
        assertNotNull(props.getLastModified());
        assertEquals("BlockBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(0, props.getSequenceNumber());
        assertEquals(content,
                inputStreamToString(result.getContentStream(), "UTF-8"));
    }

    @Test
    public void deleteBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        String content = "some content";
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test2",
                new ByteArrayInputStream(content.getBytes("UTF-8")));

        service.deleteBlob(TEST_CONTAINER_FOR_BLOBS, "test2");

        // Assert
    }

    class RetryPolicyObserver implements ServiceFilter {
        public int requestCount;

        @Override
        public ServiceResponseContext handle(ServiceRequestContext request,
                Next next) throws Exception {
            requestCount++;
            return next.handle(request);
        }
    }

    private class NonResetableInputStream extends FilterInputStream {

        protected NonResetableInputStream(InputStream in) {
            super(in);
        }

        @Override
        public boolean markSupported() {
            return false;
        }
    }

    @Test
    public void retryPolicyThrowsOnInvalidInputStream() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        service = service.withFilter(new RetryPolicyFilter(
                new ExponentialRetryPolicy(100/* deltaBackoff */,
                        3/* maximumAttempts */, new int[] { 400, 500, 503 })));

        Exception error = null;
        try {
            String content = "foo";
            InputStream contentStream = new ByteArrayInputStream(
                    content.getBytes("UTF-8"));
            InputStream stream = new NonResetableInputStream(contentStream);

            service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "testretry",
                    stream);
        } catch (Exception e) {
            error = e;
        }

        // Assert
        assertNotNull(error);
    }

    private class ResetableInputStream extends FilterInputStream {
        private boolean resetCalled;

        protected ResetableInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void reset() throws IOException {
            super.reset();
            setResetCalled(true);
        }

        public boolean isResetCalled() {
            return resetCalled;
        }

        public void setResetCalled(boolean resetCalled) {
            this.resetCalled = resetCalled;
        }
    }

    @Test
    public void retryPolicyCallsResetOnValidInputStream() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract service = BlobService.create(config);

        // Act
        service = service.withFilter(new RetryPolicyFilter(
                new ExponentialRetryPolicy(100/* deltaBackoff */,
                        3/* maximumAttempts */, new int[] { 403 })));

        ServiceException error = null;
        ResetableInputStream stream = null;
        try {
            String content = "foo";
            InputStream contentStream = new ByteArrayInputStream(
                    content.getBytes("UTF-8"));
            stream = new ResetableInputStream(contentStream);

            service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS,
                    "invalidblobname @#$#@$@", stream);
        } catch (ServiceException e) {
            error = e;
        }

        // Assert
        assertNotNull(error);
        assertEquals(403, error.getHttpStatusCode());
        assertNotNull(stream);
        assertTrue(stream.isResetCalled());
    }

    private String inputStreamToString(InputStream inputStream, String encoding)
            throws IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(
                    inputStream, encoding));
            while (true) {
                int n = reader.read(buffer);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            }
        } finally {
            inputStream.close();
        }
        return writer.toString();
    }
}
