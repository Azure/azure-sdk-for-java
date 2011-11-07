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
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.configuration.Configuration;

public class BlobServiceIntegrationTest extends IntegrationTestBase {

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
        service.createContainer("foo");
        service.deleteContainer("foo");

        // Assert
    }

    @Test
    public void createContainerWithMetadataWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createContainer("foo2", new CreateContainerOptions().setPublicAccess("blob").addMetadata("test", "bar").addMetadata("blah", "bleah"));

        ContainerProperties prop = service.getContainerMetadata("foo2");
        ContainerProperties prop2 = service.getContainerProperties("foo2");
        ContainerACL acl = service.getContainerACL("foo2");

        ListContainersResult results2 = service.listContainers(new ListContainersOptions().setPrefix("foo2").setListingDetails(
                EnumSet.of(ContainerListingDetails.METADATA)));

        service.deleteContainer("foo2");

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
        service.createContainer("foo3");

        HashMap<String, String> metadata = new HashMap<String, String>();
        metadata.put("test", "bar");
        metadata.put("blah", "bleah");
        service.setContainerMetadata("foo3", metadata);
        ContainerProperties prop = service.getContainerMetadata("foo3");

        service.deleteContainer("foo3");

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

        // Act
        service.createContainer("foo4");

        ContainerACL acl = new ContainerACL();
        acl.setPublicAccess("blob");
        acl.AddSignedIdentifier("test", "2010-01-01", "2020-01-01", "rwd");
        service.setContainerACL("foo4", acl);

        ContainerACL acl2 = service.getContainerACL("foo4");
        service.deleteContainer("foo4");

        // Assert
        assertNotNull(acl2);
        assertNotNull(acl2.getEtag());
        assertNotNull(acl2.getLastModified());
        assertNotNull(acl2.getPublicAccess());
        assertEquals("blob", acl2.getPublicAccess());
        assertEquals(1, acl2.getSignedIdentifiers().size());
        assertEquals("test", acl2.getSignedIdentifiers().get(0).getId());
        assertEquals("2010-01-01T00:00:00.0000000Z", acl2.getSignedIdentifiers().get(0).getAccessPolicy().getStart());
        assertEquals("2020-01-01T00:00:00.0000000Z", acl2.getSignedIdentifiers().get(0).getAccessPolicy().getExpiry());
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
        assertEquals(8, results.getContainers().size());
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

        // Assert
        assertNotNull(results);
        assertEquals(3, results.getContainers().size());
        assertNotNull(results.getNextMarker());
        assertEquals(3, results.getMaxResults());

        // Act
        ListContainersResult results2 = service.listContainers(new ListContainersOptions().setMarker(results.getNextMarker()));

        // Assert
        assertNotNull(results2);
        assertEquals(5, results2.getContainers().size());
        assertEquals("", results2.getNextMarker());
        assertEquals(0, results2.getMaxResults());
    }

    @Test
    public void listContainersWithPrefixWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ListContainersResult results = service.listContainers(new ListContainersOptions().setPrefix("mycontainer1"));

        // Assert
        assertNotNull(results);
        assertEquals(3, results.getContainers().size());
        assertNotNull(results.getNextMarker());
        assertEquals(0, results.getMaxResults());
    }

    @Test
    public void listBlobsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ListBlobsResult results = service.listBlobs("mycontainer11");

        // Assert
        assertNotNull(results);
        assertEquals(3, results.getBlobs().size());
    }

    @Test
    public void listBlobsWithPrefixWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        ListBlobsResult results = service.listBlobs("mycontainer11", new ListBlobsOptions().setPrefix("Create"));

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
        service.createPageBlob("mycontainer1", "test", 512);

        // Assert
    }

    @Test
    public void createPageBlobWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        service.createPageBlob("mycontainer1", "test", 512, new CreateBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8")
                .setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        BlobProperties props = service.getBlobProperties("mycontainer1", "test");

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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        service.createBlockBlob("mycontainer1", "test2", new ByteArrayInputStream("some content".getBytes()));

        // Assert
    }

    @Test
    public void createBlockBlobWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content";
        service.createBlockBlob("mycontainer1", "test2", new ByteArrayInputStream(content.getBytes("UTF-8")),
                new CreateBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8").setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        BlobProperties props = service.getBlobProperties("mycontainer1", "test2");

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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        service.createBlockBlob("mycontainer1", "test2", new ByteArrayInputStream(content.getBytes("UTF-8")),
                new CreateBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8").setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        Blob blob = service.getBlob("mycontainer1", "test2");
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
        service.createPageBlob("mycontainer1", "test", 4096, new CreateBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8")
                .setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        Blob blob = service.getBlob("mycontainer1", "test");
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
        service.createPageBlob("mycontainer1", "test", 4096);
        try {
            service.getBlob("mycontainer1", "test", new GetBlobOptions().setAccessCondition(AccessCondition.ifMatch("123")));
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
        service.createPageBlob("mycontainer1", "test", 4096);
        BlobProperties props = service.getBlobProperties("mycontainer1", "test");
        try {
            service.getBlob("mycontainer1", "test", new GetBlobOptions().setAccessCondition(AccessCondition.ifNoneMatch(props.getEtag())));
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
        service.createPageBlob("mycontainer1", "test", 4096);
        BlobProperties props = service.getBlobProperties("mycontainer1", "test");
        try {
            service.getBlob("mycontainer1", "test", new GetBlobOptions().setAccessCondition(AccessCondition.ifModifiedSince(props.getLastModified())));
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        String container = "mycontainer1";
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
        service.createBlockBlob("mycontainer1", "test2", new ByteArrayInputStream(content.getBytes("UTF-8")));

        service.deleteBlob("mycontainer1", "test2");

        // Assert
    }

    @Test
    public void copyBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService service = config.create(BlobService.class);

        // Act
        String content = "some content2";
        service.createBlockBlob("mycontainer2", "test6", new ByteArrayInputStream(content.getBytes("UTF-8")));
        service.copyBlob("mycontainer1", "test5", "mycontainer2", "test6");

        Blob blob = service.getBlob("mycontainer1", "test5");
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
        service.createBlockBlob("mycontainer2", "test6", new ByteArrayInputStream(content.getBytes("UTF-8")));
        String leaseId = service.acquireLease("mycontainer2", "test6");
        service.releaseLease("mycontainer2", "test6", leaseId);

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
        service.createBlockBlob("mycontainer2", "test6", new ByteArrayInputStream(content.getBytes("UTF-8")));
        String leaseId = service.acquireLease("mycontainer2", "test6");
        String leaseId2 = service.renewLease("mycontainer2", "test6", leaseId);
        service.releaseLease("mycontainer2", "test6", leaseId);

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
        service.createBlockBlob("mycontainer2", "test6", new ByteArrayInputStream(content.getBytes("UTF-8")));
        String leaseId = service.acquireLease("mycontainer2", "test6");
        service.breakLease("mycontainer2", "test6", leaseId);
        service.releaseLease("mycontainer2", "test6", leaseId);

        // Assert
        assertNotNull(leaseId);
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
