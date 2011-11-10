package com.microsoft.azure.services.blob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.common.ExponentialRetryPolicyFilter;
import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.http.ServiceFilter;

public class BlobServiceIntegrationTest extends IntegrationTestBase {
    private static final String testContainersPrefix = "sdktest-";
    private static final String createableContainersPrefix = "csdktest-";
    private static String CREATEABLE_CONTAINER_1;
    private static String CREATEABLE_CONTAINER_2;
    private static String CREATEABLE_CONTAINER_3;
    private static String CREATEABLE_CONTAINER_4;
    private static String TEST_CONTAINER_FOR_BLOBS;
    private static String TEST_CONTAINER_FOR_BLOBS_2;
    private static String TEST_CONTAINER_FOR_LISTING;
    private static String[] creatableContainers;
    private static String[] testContainers;

    @BeforeClass
    public static void setup() throws Exception {
        // Setup container names array (list of container names used by
        // integration tests)
        testContainers = new String[10];
        for (int i = 0; i < testContainers.length; i++) {
            testContainers[i] = String.format("%s%d", testContainersPrefix, i + 1);
        }

        creatableContainers = new String[10];
        for (int i = 0; i < creatableContainers.length; i++) {
            creatableContainers[i] = String.format("%s%d", createableContainersPrefix, i + 1);
        }

        CREATEABLE_CONTAINER_1 = creatableContainers[0];
        CREATEABLE_CONTAINER_2 = creatableContainers[1];
        CREATEABLE_CONTAINER_3 = creatableContainers[2];
        CREATEABLE_CONTAINER_4 = creatableContainers[3];

        TEST_CONTAINER_FOR_BLOBS = testContainers[0];
        TEST_CONTAINER_FOR_BLOBS_2 = testContainers[1];
        TEST_CONTAINER_FOR_LISTING = testContainers[2];

        // Create all test containers and their content
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);
        for (int i = 0; i < testContainers.length; i++) {
            try {
                service.createContainer(testContainers[i]);
            }
            catch (ServiceException e) {
                // Ignore exception as the containers might not exists
            }
        }

        service.createPageBlob(TEST_CONTAINER_FOR_LISTING, "myblob1", 512);
        service.createPageBlob(TEST_CONTAINER_FOR_LISTING, "myblob2", 512);
        service.createPageBlob(TEST_CONTAINER_FOR_LISTING, "otherblob1", 512);
        service.createPageBlob(TEST_CONTAINER_FOR_LISTING, "otherblob2", 512);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);
        for (int i = 0; i < testContainers.length; i++) {
            try {
                service.deleteContainer(testContainers[i]);
            }
            catch (ServiceException e) {
                // Ignore exception as the containers might not exists
            }
        }

        for (int i = 0; i < creatableContainers.length; i++) {
            try {
                service.deleteContainer(creatableContainers[i]);
            }
            catch (ServiceException e) {
                // Ignore exception as the containers might not exists
            }
        }
    }

    @Test
    public void getServiceProppertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ServiceProperties props = service.getServiceProperties();

        // Assert
        assertNotNull(props);
        assertNotNull(props.getLogging());
        assertNotNull(props.getLogging().getRetentionPolicy());
        assertNotNull(props.getLogging().getVersion());
        assertNotNull(props.getMetrics().getRetentionPolicy());
        assertNotNull(props.getMetrics().getVersion());
    }

    @Test
    public void setServiceProppertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ServiceProperties props = service.getServiceProperties();

        props.setDefaultServiceVersion("2009-09-19");
        props.getLogging().setRead(true);
        service.setServiceProperties(props);

        props = service.getServiceProperties();

        // Assert
        assertNotNull(props);
        assertEquals("2009-09-19", props.getDefaultServiceVersion());
        assertNotNull(props.getLogging());
        assertNotNull(props.getLogging().getRetentionPolicy());
        assertNotNull(props.getLogging().getVersion());
        assertTrue(props.getLogging().isRead());
        assertNotNull(props.getMetrics().getRetentionPolicy());
        assertNotNull(props.getMetrics().getVersion());
    }

    @Test
    public void createContainerWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createContainer(CREATEABLE_CONTAINER_1);

        // Assert
    }

    @Test
    public void createContainerWithMetadataWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createContainer(CREATEABLE_CONTAINER_2,
                new CreateContainerOptions().setPublicAccess("blob").addMetadata("test", "bar").addMetadata("blah", "bleah"));

        ContainerProperties prop = service.getContainerMetadata(CREATEABLE_CONTAINER_2);
        ContainerProperties prop2 = service.getContainerProperties(CREATEABLE_CONTAINER_2);
        ContainerACL acl = service.getContainerACL(CREATEABLE_CONTAINER_2);

        ListContainersResult results2 = service.listContainers(new ListContainersOptions().setPrefix(CREATEABLE_CONTAINER_2).setListingDetails(
                EnumSet.of(ContainerListingDetails.METADATA)));

        service.deleteContainer(CREATEABLE_CONTAINER_2);

        // Assert
        assertNotNull(prop);
        assertNotNull(prop.getEtag());
        assertNotNull(prop.getLastModified());
        assertNotNull(prop.getMetadata());
        assertEquals(2, prop.getMetadata().size());
        assertTrue(prop.getMetadata().containsKey("test"));
        assertTrue(prop.getMetadata().containsValue("bar"));
        assertTrue(prop.getMetadata().containsKey("blah"));
        assertTrue(prop.getMetadata().containsValue("bleah"));

        assertNotNull(prop2);
        assertNotNull(prop2.getEtag());
        assertNotNull(prop2.getLastModified());
        assertNotNull(prop2.getMetadata());
        assertEquals(2, prop2.getMetadata().size());
        assertTrue(prop2.getMetadata().containsKey("test"));
        assertTrue(prop2.getMetadata().containsValue("bar"));
        assertTrue(prop2.getMetadata().containsKey("blah"));
        assertTrue(prop2.getMetadata().containsValue("bleah"));

        assertNotNull(results2);
        assertEquals(1, results2.getContainers().size());
        assertTrue(results2.getContainers().get(0).getMetadata().containsKey("test"));
        assertTrue(results2.getContainers().get(0).getMetadata().containsValue("bar"));
        assertTrue(results2.getContainers().get(0).getMetadata().containsKey("blah"));
        assertTrue(results2.getContainers().get(0).getMetadata().containsValue("bleah"));

        assertNotNull(acl);

    }

    @Test
    public void setContainerMetadataWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createContainer(CREATEABLE_CONTAINER_3);

        HashMap<String, String> metadata = new HashMap<String, String>();
        metadata.put("test", "bar");
        metadata.put("blah", "bleah");
        service.setContainerMetadata(CREATEABLE_CONTAINER_3, metadata);
        ContainerProperties prop = service.getContainerMetadata(CREATEABLE_CONTAINER_3);

        // Assert
        assertNotNull(prop);
        assertNotNull(prop.getEtag());
        assertNotNull(prop.getLastModified());
        assertNotNull(prop.getMetadata());
        assertEquals(2, prop.getMetadata().size());
        assertTrue(prop.getMetadata().containsKey("test"));
        assertTrue(prop.getMetadata().containsValue("bar"));
        assertTrue(prop.getMetadata().containsKey("blah"));
        assertTrue(prop.getMetadata().containsValue("bleah"));
    }

    @Test
    public void setContainerACLWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        String container = CREATEABLE_CONTAINER_4;
        Date expiryStartDate = new GregorianCalendar(2010, 1, 1).getTime();
        Date expiryEndDate = new GregorianCalendar(2020, 1, 1).getTime();

        // Act
        service.createContainer(container);

        ContainerACL acl = new ContainerACL();
        acl.setPublicAccess("blob");
        acl.addSignedIdentifier("test", expiryStartDate, expiryEndDate, "rwd");
        service.setContainerACL(container, acl);

        ContainerACL acl2 = service.getContainerACL(container);
        service.deleteContainer(container);

        // Assert
        assertNotNull(acl2);
        assertNotNull(acl2.getEtag());
        assertNotNull(acl2.getLastModified());
        assertNotNull(acl2.getPublicAccess());
        assertEquals("blob", acl2.getPublicAccess());
        assertEquals(1, acl2.getSignedIdentifiers().size());
        assertEquals("test", acl2.getSignedIdentifiers().get(0).getId());
        assertEquals(expiryStartDate, acl2.getSignedIdentifiers().get(0).getAccessPolicy().getStart());
        assertEquals(expiryEndDate, acl2.getSignedIdentifiers().get(0).getAccessPolicy().getExpiry());
        assertEquals("rwd", acl2.getSignedIdentifiers().get(0).getAccessPolicy().getPermission());
    }

    @Test
    public void listContainersWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ListContainersResult results = service.listContainers();

        // Assert
        assertNotNull(results);
        assertTrue(testContainers.length <= results.getContainers().size());
        assertNotNull(results.getContainers().get(0).getName());
        assertNotNull(results.getContainers().get(0).getMetadata());
        assertNotNull(results.getContainers().get(0).getProperties());
        assertNotNull(results.getContainers().get(0).getProperties().getEtag());
        assertNotNull(results.getContainers().get(0).getProperties().getLastModified());
        assertNotNull(results.getContainers().get(0).getUrl());
    }

    @Test
    public void listContainersWithPaginationWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ListContainersResult results = service.listContainers(new ListContainersOptions().setMaxResults(3));
        ListContainersResult results2 = service.listContainers(new ListContainersOptions().setMarker(results.getNextMarker()));

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
        BlobService service = config.create(BlobService.class);

        // Act
        ListContainersResult results = service.listContainers(new ListContainersOptions().setPrefix(testContainersPrefix).setMaxResults(3));
        // Assert
        assertNotNull(results);
        assertEquals(3, results.getContainers().size());
        assertNotNull(results.getNextMarker());
        assertEquals(3, results.getMaxResults());

        // Act
        ListContainersResult results2 = service.listContainers(new ListContainersOptions().setMarker(results.getNextMarker()));

        // Assert
        assertNotNull(results2);
        assertEquals(testContainers.length - 3, results2.getContainers().size());
        assertNotNull(results2.getNextMarker());
        assertEquals(0, results2.getMaxResults());
    }

    @Test
    public void listBlobsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ListBlobsResult results = service.listBlobs(TEST_CONTAINER_FOR_LISTING);

        // Assert
        assertNotNull(results);
        assertEquals(4, results.getBlobs().size());
    }

    @Test
    public void listBlobsWithPrefixWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ListBlobsResult results = service.listBlobs(TEST_CONTAINER_FOR_LISTING, new ListBlobsOptions().setPrefix("myblob"));

        // Assert
        assertNotNull(results);
        assertEquals(2, results.getBlobs().size());
    }

    @Test
    public void createPageBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createPageBlob(TEST_CONTAINER_FOR_BLOBS, "test", 512);

        // Assert
    }

    @Test
    public void createPageBlobWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createPageBlob(TEST_CONTAINER_FOR_BLOBS, "test", 512, new CreateBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8")
                .setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        BlobProperties props = service.getBlobProperties(TEST_CONTAINER_FOR_BLOBS, "test");

        // Assert
        assertNotNull(props);
        assertEquals("test", props.getCacheControl());
        assertEquals("UTF-8", props.getContentEncoding());
        assertEquals("en-us", props.getContentLanguage());
        assertEquals("text/plain", props.getContentType());
        assertEquals(512, props.getContentLength());
        assertNotNull(props.getEtag());
        assertNull(props.getContentMD5());
        assertNotNull(props.getMetadata());
        assertEquals(0, props.getMetadata().size());
        assertNotNull(props.getLastModified());
        assertEquals("PageBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(0, props.getSequenceNumber());
    }

    @Test
    public void clearBlobPagesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test";
        service.createPageBlob(container, blob, 512);

        CreateBlobPagesResult result = service.clearBlobPages(container, blob, 0, 511);

        // Assert
        assertNotNull(result);
        assertNull(result.getContentMD5());
        assertNotNull(result.getLastModified());
        assertNotNull(result.getEtag());
        assertEquals(0, result.getSequenceNumber());
    }

    @Test
    public void createBlobPagesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test";
        String content = new String(new char[512]);
        service.createPageBlob(container, blob, 512);

        CreateBlobPagesResult result = service.createBlobPages(container, blob, 0, 511, content.length(), new ByteArrayInputStream(content.getBytes("UTF-8")));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getContentMD5());
        assertNotNull(result.getLastModified());
        assertNotNull(result.getEtag());
        assertEquals(0, result.getSequenceNumber());
    }

    @Test
    public void listBlobRegionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test";
        String content = new String(new char[512]);
        service.createPageBlob(container, blob, 16384 + 512);

        service.createBlobPages(container, blob, 0, 511, content.length(), new ByteArrayInputStream(content.getBytes("UTF-8")));
        service.createBlobPages(container, blob, 1024, 1024 + 511, content.length(), new ByteArrayInputStream(content.getBytes("UTF-8")));
        service.createBlobPages(container, blob, 8192, 8192 + 511, content.length(), new ByteArrayInputStream(content.getBytes("UTF-8")));
        service.createBlobPages(container, blob, 16384, 16384 + 511, content.length(), new ByteArrayInputStream(content.getBytes("UTF-8")));

        ListBlobRegionsResult result = service.listBlobRegions(container, blob);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getLastModified());
        assertNotNull(result.getEtag());
        assertEquals(16384 + 512, result.getContentLength());
        assertNotNull(result.getPageRanges());
        assertEquals(4, result.getPageRanges().size());
        assertEquals(0, result.getPageRanges().get(0).getStart());
        assertEquals(511, result.getPageRanges().get(0).getEnd());
        assertEquals(1024, result.getPageRanges().get(1).getStart());
        assertEquals(1024 + 511, result.getPageRanges().get(1).getEnd());
        assertEquals(8192, result.getPageRanges().get(2).getStart());
        assertEquals(8192 + 511, result.getPageRanges().get(2).getEnd());
        assertEquals(16384, result.getPageRanges().get(3).getStart());
        assertEquals(16384 + 511, result.getPageRanges().get(3).getEnd());
    }

    @Test
    public void listBlobBlocksOnEmptyBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test13";
        String content = new String(new char[512]);
        service.createBlockBlob(container, blob, new ByteArrayInputStream(content.getBytes("UTF-8")));

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
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test14";
        service.createBlockBlob(container, blob, null);
        service.createBlobBlock(container, blob, "123", new ByteArrayInputStream(new byte[256]));
        service.createBlobBlock(container, blob, "124", new ByteArrayInputStream(new byte[512]));
        service.createBlobBlock(container, blob, "125", new ByteArrayInputStream(new byte[195]));

        ListBlobBlocksResult result = service.listBlobBlocks(container, blob, new ListBlobBlocksOptions().setListType("all"));

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
    public void commitBlobBlocksWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test14";
        String blockId1 = "1fedcba";
        String blockId2 = "2abcdef";
        String blockId3 = "3zzzzzz";
        service.createBlockBlob(container, blob, null);
        service.createBlobBlock(container, blob, blockId1, new ByteArrayInputStream(new byte[256]));
        service.createBlobBlock(container, blob, blockId2, new ByteArrayInputStream(new byte[512]));
        service.createBlobBlock(container, blob, blockId3, new ByteArrayInputStream(new byte[195]));

        BlockList blockList = new BlockList();
        blockList.addUncommittedEntry(blockId1).addLatestEntry(blockId3);
        service.commitBlobBlocks(container, blob, blockList);

        ListBlobBlocksResult result = service.listBlobBlocks(container, blob, new ListBlobBlocksOptions().setListType("all"));

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
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test13";
        String content = new String(new char[512]);
        service.createBlockBlob(container, blob, new ByteArrayInputStream(content.getBytes("UTF-8")));
        service.createBlobBlock(container, blob, "123", new ByteArrayInputStream(content.getBytes("UTF-8")));
        service.createBlobBlock(container, blob, "124", new ByteArrayInputStream(content.getBytes("UTF-8")));

        // Assert
    }

    @Test
    public void createBlockBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test2", new ByteArrayInputStream("some content".getBytes()));

        // Assert
    }

    @Test
    public void createBlockBlobWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content";
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test2", new ByteArrayInputStream(content.getBytes("UTF-8")),
                new CreateBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8").setBlobContentLanguage("en-us")
                        /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                        /* .setContentMD5("1234") */.setContentType("text/plain"));

        BlobProperties props = service.getBlobProperties(TEST_CONTAINER_FOR_BLOBS, "test2");

        // Assert
        assertNotNull(props);
        assertEquals("test", props.getCacheControl());
        assertEquals("UTF-8", props.getContentEncoding());
        assertEquals("en-us", props.getContentLanguage());
        assertEquals("text/plain", props.getContentType());
        assertEquals(content.length(), props.getContentLength());
        assertNotNull(props.getEtag());
        assertNull(props.getContentMD5());
        assertNotNull(props.getMetadata());
        assertEquals(0, props.getMetadata().size());
        assertNotNull(props.getLastModified());
        assertEquals("BlockBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(0, props.getSequenceNumber());
    }

    @Test
    public void createBlobSnapshotWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test3";
        service.createBlockBlob(container, blob, new ByteArrayInputStream("some content".getBytes()));
        BlobSnapshot snapshot = service.createBlobSnapshot(container, blob);

        // Assert
        assertNotNull(snapshot);
        assertNotNull(snapshot.getEtag());
        assertNotNull(snapshot.getLastModified());
        assertNotNull(snapshot.getSnapshot());
    }

    @Test
    public void createBlobSnapshotWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test3";
        service.createBlockBlob(container, blob, new ByteArrayInputStream("some content".getBytes()));
        BlobSnapshot snapshot = service.createBlobSnapshot(container, blob,
                new CreateBlobSnapshotOptions().addMetadata("test", "bar").addMetadata("blah", "bleah"));

        BlobProperties props = service.getBlobProperties(container, blob, new GetBlobPropertiesOptions().setSnapshot(snapshot.getSnapshot()));

        // Assert
        assertNotNull(props);
        assertEquals(snapshot.getEtag(), props.getEtag());
        assertEquals(snapshot.getLastModified(), props.getLastModified());
        assertTrue(props.getMetadata().containsKey("test"));
        assertTrue(props.getMetadata().containsValue("bar"));
        assertTrue(props.getMetadata().containsKey("blah"));
        assertTrue(props.getMetadata().containsValue("bleah"));
    }

    @Test
    public void getBlockBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content";
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test2", new ByteArrayInputStream(content.getBytes("UTF-8")),
                new CreateBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8").setBlobContentLanguage("en-us")
                        /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                        /* .setContentMD5("1234") */.setContentType("text/plain"));

        GetBlobResult blob = service.getBlob(TEST_CONTAINER_FOR_BLOBS, "test2");
        BlobProperties props = blob.getProperties();

        // Assert
        assertNotNull(props);
        assertEquals("test", props.getCacheControl());
        assertEquals("UTF-8", props.getContentEncoding());
        assertEquals("en-us", props.getContentLanguage());
        assertEquals("text/plain", props.getContentType());
        assertEquals(content.length(), props.getContentLength());
        assertNotNull(props.getEtag());
        assertNull(props.getContentMD5());
        assertNotNull(props.getMetadata());
        assertEquals(0, props.getMetadata().size());
        assertNotNull(props.getLastModified());
        assertEquals("BlockBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(0, props.getSequenceNumber());
        assertEquals(content, inputStreamToString(blob.getContentStream(), "UTF-8"));
    }

    @Test
    public void getPageBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createPageBlob(TEST_CONTAINER_FOR_BLOBS, "test", 4096, new CreateBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8")
                .setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        GetBlobResult blob = service.getBlob(TEST_CONTAINER_FOR_BLOBS, "test");
        BlobProperties props = blob.getProperties();

        // Assert
        assertNotNull(props);
        assertEquals("test", props.getCacheControl());
        assertEquals("UTF-8", props.getContentEncoding());
        assertEquals("en-us", props.getContentLanguage());
        assertEquals("text/plain", props.getContentType());
        assertEquals(4096, props.getContentLength());
        assertNotNull(props.getEtag());
        assertNull(props.getContentMD5());
        assertNotNull(props.getMetadata());
        assertEquals(0, props.getMetadata().size());
        assertNotNull(props.getLastModified());
        assertEquals("PageBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(0, props.getSequenceNumber());
        assertEquals(4096, inputStreamToByteArray(blob.getContentStream()).length);
    }

    @Test
    public void getBlobWithIfMatchETagAccessConditionWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createPageBlob(TEST_CONTAINER_FOR_BLOBS, "test", 4096);
        try {
            service.getBlob(TEST_CONTAINER_FOR_BLOBS, "test", new GetBlobOptions().setAccessCondition(AccessCondition.ifMatch("123")));
            Assert.fail("getBlob should throw an exception");
        }
        catch (ServiceException e) {
        }

        // Assert
    }

    @Test
    public void getBlobWithIfNoneMatchETagAccessConditionWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createPageBlob(TEST_CONTAINER_FOR_BLOBS, "test", 4096);
        BlobProperties props = service.getBlobProperties(TEST_CONTAINER_FOR_BLOBS, "test");
        try {
            service.getBlob(TEST_CONTAINER_FOR_BLOBS, "test", new GetBlobOptions().setAccessCondition(AccessCondition.ifNoneMatch(props.getEtag())));
            Assert.fail("getBlob should throw an exception");
        }
        catch (ServiceException e) {
        }

        // Assert
    }

    @Test
    public void getBlobWithIfModifiedSinceAccessConditionWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createPageBlob(TEST_CONTAINER_FOR_BLOBS, "test", 4096);
        BlobProperties props = service.getBlobProperties(TEST_CONTAINER_FOR_BLOBS, "test");
        try {
            service.getBlob(TEST_CONTAINER_FOR_BLOBS, "test", new GetBlobOptions().setAccessCondition(AccessCondition.ifModifiedSince(props.getLastModified())));
            Assert.fail("getBlob should throw an exception");
        }
        catch (ServiceException e) {
        }

        // Assert
    }

    @Test
    public void getBlobWithIfNotModifiedSinceAccessConditionWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test";
        service.createPageBlob(container, blob, 4096);
        BlobProperties props = service.getBlobProperties(container, blob);

        // To test for "IfNotModifiedSince", we need to make updates to the blob
        // until at least 1 second has passed since the blob creation
        Date lastModifiedBase = (Date) props.getLastModified().clone();

        // +1 second
        Date lastModifiedNext = new Date(lastModifiedBase.getTime() + 1 * 1000);

        while (true) {
            HashMap<String, String> metadata = new HashMap<String, String>();
            metadata.put("test", "test1");
            SetBlobMetadataResult result = service.setBlobMetadata(container, blob, metadata);
            if (result.getLastModified().compareTo(lastModifiedNext) >= 0)
                break;
        }
        try {
            service.getBlob(container, blob, new GetBlobOptions().setAccessCondition(AccessCondition.ifNotModifiedSince(lastModifiedBase)));
            Assert.fail("getBlob should throw an exception");
        }
        catch (ServiceException e) {
        }

        // Assert
    }

    @Test
    public void getBlobPropertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test";
        service.createPageBlob(container, blob, 4096);
        BlobProperties props = service.getBlobProperties(container, blob);

        // Assert
        // Assert
        assertNotNull(props);
        assertNull(props.getCacheControl());
        assertNull(props.getContentEncoding());
        assertNull(props.getContentLanguage());
        assertEquals("application/octet-stream", props.getContentType());
        assertEquals(4096, props.getContentLength());
        assertNotNull(props.getEtag());
        assertNull(props.getContentMD5());
        assertNotNull(props.getMetadata());
        assertEquals(0, props.getMetadata().size());
        assertNotNull(props.getLastModified());
        assertEquals("PageBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(0, props.getSequenceNumber());
    }

    @Test
    public void getBlobMetadataWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test";
        service.createPageBlob(container, blob, 4096, new CreateBlobOptions().addMetadata("test", "bar").addMetadata("blah", "bleah"));
        GetBlobMetadataResult props = service.getBlobMetadata(container, blob);

        // Assert
        // Assert
        assertNotNull(props);
        assertNotNull(props.getEtag());
        assertNotNull(props.getMetadata());
        assertEquals(2, props.getMetadata().size());
        assertTrue(props.getMetadata().containsKey("test"));
        assertTrue(props.getMetadata().containsValue("bar"));
        assertTrue(props.getMetadata().containsKey("blah"));
        assertTrue(props.getMetadata().containsValue("bleah"));
        assertNotNull(props.getLastModified());
    }

    @Test
    public void setBlobPropertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test10";
        service.createPageBlob(container, blob, 4096);
        SetBlobPropertiesResult result = service.setBlobProperties(container, blob,
                new SetBlobPropertiesOptions().setCacheControl("test").setContentEncoding("UTF-8").setContentLanguage("en-us").setContentLength(512L)
                        .setContentMD5(null).setContentType("text/plain").setSequenceNumberAction("increment"));

        BlobProperties props = service.getBlobProperties(container, blob);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEtag());
        assertNotNull(result.getLastModified());
        assertNotNull(result.getSequenceNumber());
        assertEquals(1, result.getSequenceNumber().longValue());

        assertNotNull(props);
        assertEquals("test", props.getCacheControl());
        assertEquals("UTF-8", props.getContentEncoding());
        assertEquals("en-us", props.getContentLanguage());
        assertEquals("text/plain", props.getContentType());
        assertEquals(512, props.getContentLength());
        assertNull(props.getContentMD5());
        assertNotNull(props.getMetadata());
        assertEquals(0, props.getMetadata().size());
        assertNotNull(props.getLastModified());
        assertEquals("PageBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(1, props.getSequenceNumber());
    }

    @Test
    public void setBlobMetadataWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String container = TEST_CONTAINER_FOR_BLOBS;
        String blob = "test11";
        HashMap<String, String> metadata = new HashMap<String, String>();
        metadata.put("test", "bar");
        metadata.put("blah", "bleah");

        service.createPageBlob(container, blob, 4096);
        SetBlobMetadataResult result = service.setBlobMetadata(container, blob, metadata);
        BlobProperties props = service.getBlobProperties(container, blob);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEtag());
        assertNotNull(result.getLastModified());

        assertNotNull(props);
        assertNotNull(props.getMetadata());
        assertEquals(2, props.getMetadata().size());
        assertTrue(props.getMetadata().containsKey("test"));
        assertTrue(props.getMetadata().containsValue("bar"));
        assertTrue(props.getMetadata().containsKey("blah"));
        assertTrue(props.getMetadata().containsValue("bleah"));
    }

    @Test
    public void deleteBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content";
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test2", new ByteArrayInputStream(content.getBytes("UTF-8")));

        service.deleteBlob(TEST_CONTAINER_FOR_BLOBS, "test2");

        // Assert
    }

    @Test
    public void copyBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content2";
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test6", new ByteArrayInputStream(content.getBytes("UTF-8")));
        service.copyBlob(TEST_CONTAINER_FOR_BLOBS_2, "test5", TEST_CONTAINER_FOR_BLOBS, "test6");

        GetBlobResult blob = service.getBlob(TEST_CONTAINER_FOR_BLOBS_2, "test5");
        BlobProperties props = blob.getProperties();

        // Assert
        assertNotNull(props);
        assertEquals(content.length(), props.getContentLength());
        assertNotNull(props.getEtag());
        assertNull(props.getContentMD5());
        assertNotNull(props.getMetadata());
        assertEquals(0, props.getMetadata().size());
        assertNotNull(props.getLastModified());
        assertEquals("BlockBlob", props.getBlobType());
        assertEquals("unlocked", props.getLeaseStatus());
        assertEquals(0, props.getSequenceNumber());
        assertEquals(content, inputStreamToString(blob.getContentStream(), "UTF-8"));
    }

    @Test
    public void acquireLeaseWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content2";
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test6", new ByteArrayInputStream(content.getBytes("UTF-8")));
        String leaseId = service.acquireLease(TEST_CONTAINER_FOR_BLOBS, "test6");
        service.releaseLease(TEST_CONTAINER_FOR_BLOBS, "test6", leaseId);

        // Assert
        assertNotNull(leaseId);
    }

    @Test
    public void renewLeaseWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content2";
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test6", new ByteArrayInputStream(content.getBytes("UTF-8")));
        String leaseId = service.acquireLease(TEST_CONTAINER_FOR_BLOBS, "test6");
        String leaseId2 = service.renewLease(TEST_CONTAINER_FOR_BLOBS, "test6", leaseId);
        service.releaseLease(TEST_CONTAINER_FOR_BLOBS, "test6", leaseId);

        // Assert
        assertNotNull(leaseId);
        assertNotNull(leaseId2);
    }

    @Test
    public void breakLeaseWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content2";
        service.createBlockBlob(TEST_CONTAINER_FOR_BLOBS, "test6", new ByteArrayInputStream(content.getBytes("UTF-8")));
        String leaseId = service.acquireLease(TEST_CONTAINER_FOR_BLOBS, "test6");
        service.breakLease(TEST_CONTAINER_FOR_BLOBS, "test6", leaseId);
        service.releaseLease(TEST_CONTAINER_FOR_BLOBS, "test6", leaseId);

        // Assert
        assertNotNull(leaseId);
    }

    class RetryPolicyObserver implements ServiceFilter {
        public int requestCount;

        public Response handle(Request request, Next next) {
            requestCount++;
            return next.handle(request);
        }
    }

    @Test
    public void retryPolicyWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);
        RetryPolicyObserver observer = new RetryPolicyObserver();
        service = service.withFilter(observer);

        // Act
        service = service.withFilter(new ExponentialRetryPolicyFilter(ExponentialRetryPolicyFilter.DEFAULT_MIN_BACKOFF, 3, new int[] { 400, 500, 503 }));

        ServiceException Error = null;
        try {
            service.createPageBlob(TEST_CONTAINER_FOR_BLOBS, "test", 12);
        }
        catch (ServiceException e) {
            Error = e;
        }

        // Assert
        assertNotNull(Error);
        assertEquals(400, Error.getHttpStatusCode());
        assertEquals(4, observer.requestCount);
    }

    @Test
    public void retryPolicyCompositionWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);
        RetryPolicyObserver observer = new RetryPolicyObserver();
        service = service.withFilter(observer);

        // Act
        service = service.withFilter(new ExponentialRetryPolicyFilter(ExponentialRetryPolicyFilter.DEFAULT_MIN_BACKOFF, 3, new int[] { 400, 500, 503 }));
        service = service.withFilter(new ExponentialRetryPolicyFilter(ExponentialRetryPolicyFilter.DEFAULT_MIN_BACKOFF, 2, new int[] { 400, 500, 503 }));

        ServiceException Error = null;
        try {
            service.createPageBlob(TEST_CONTAINER_FOR_BLOBS, "test", 12);
        }
        catch (ServiceException e) {
            Error = e;
        }

        // Assert
        assertNotNull(Error);
        assertEquals(400, Error.getHttpStatusCode());
        assertEquals(3, observer.requestCount);
    }

    private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        try {
            while (true) {
                int n = inputStream.read(buffer);
                if (n == -1)
                    break;
                outputStream.write(buffer, 0, n);
            }
        }
        finally {
            inputStream.close();
        }
        return outputStream.toByteArray();
    }

    private String inputStreamToString(InputStream inputStream, String encoding) throws IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
            while (true) {
                int n = reader.read(buffer);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            }
        }
        finally {
            inputStream.close();
        }
        return writer.toString();
    }
}
