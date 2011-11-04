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
import java.util.EnumSet;
import java.util.HashMap;

import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;

public class BlobServiceIntegrationTest extends IntegrationTestBase {

    @Test
    public void createContainerWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        contract.createContainer("foo");
        contract.deleteContainer("foo");

        // Assert
    }

    @Test
    public void createContainerWithMetadataWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        contract.createContainer("foo2", new CreateContainerOptions().setPublicAccess("blob").addMetadata("test", "bar").addMetadata("blah", "bleah"));

        ContainerProperties prop = contract.getContainerMetadata("foo2");
        ContainerProperties prop2 = contract.getContainerProperties("foo2");
        ContainerACL acl = contract.getContainerACL("foo2");

        ListContainersResults results2 = contract.listContainers(new ListContainersOptions().setPrefix("foo2").setListingDetails(
                EnumSet.of(ContainerListingDetails.METADATA)));

        contract.deleteContainer("foo2");

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
        BlobService contract = config.create(BlobService.class);

        // Act
        contract.createContainer("foo3");

        HashMap<String, String> metadata = new HashMap<String, String>();
        metadata.put("test", "bar");
        metadata.put("blah", "bleah");
        contract.setContainerMetadata("foo3", metadata);
        ContainerProperties prop = contract.getContainerMetadata("foo3");

        contract.deleteContainer("foo3");

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
        BlobService contract = config.create(BlobService.class);

        // Act
        contract.createContainer("foo4");

        ContainerACL acl = new ContainerACL();
        acl.setPublicAccess("blob");
        acl.AddSignedIdentifier("test", "2010-01-01", "2020-01-01", "rwd");
        contract.setContainerACL("foo4", acl);

        ContainerACL acl2 = contract.getContainerACL("foo4");
        contract.deleteContainer("foo4");

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
        BlobService contract = config.create(BlobService.class);

        // Act
        ListContainersResults results = contract.listContainers();

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
        BlobService contract = config.create(BlobService.class);

        // Act
        ListContainersResults results = contract.listContainers(new ListContainersOptions().setMaxResults(3));

        // Assert
        assertNotNull(results);
        assertEquals(3, results.getContainers().size());
        assertNotNull(results.getNextMarker());
        assertEquals(3, results.getMaxResults());

        // Act
        ListContainersResults results2 = contract.listContainers(new ListContainersOptions().setMarker(results.getNextMarker()));

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
        BlobService contract = config.create(BlobService.class);

        // Act
        ListContainersResults results = contract.listContainers(new ListContainersOptions().setPrefix("mycontainer1"));

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
        BlobService contract = config.create(BlobService.class);

        // Act
        ListBlobsResults results = contract.listBlobs("mycontainer11");

        // Assert
        assertNotNull(results);
        assertEquals(3, results.getBlobs().size());
    }

    @Test
    public void listBlobsWithPrefixWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        ListBlobsResults results = contract.listBlobs("mycontainer11", new ListBlobsOptions().setPrefix("Create"));

        // Assert
        assertNotNull(results);
        assertEquals(2, results.getBlobs().size());
    }

    @Test
    public void putPageBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        contract.putPageBlob("mycontainer1", "test", 512);

        // Assert
    }

    @Test
    public void putPageBlobWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        contract.putPageBlob("mycontainer1", "test", 512, new PutBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8")
                .setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        BlobProperties props = contract.getBlobProperties("mycontainer1", "test");

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
    public void putBlockBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        contract.putBlockBlob("mycontainer1", "test2", new ByteArrayInputStream("some content".getBytes()));

        // Assert
    }

    @Test
    public void putBlockBlobWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        String content = "some content";
        contract.putBlockBlob("mycontainer1", "test2", new ByteArrayInputStream(content.getBytes("UTF-8")), new PutBlobOptions().setBlobCacheControl("test")
                .setBlobContentEncoding("UTF-8").setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        BlobProperties props = contract.getBlobProperties("mycontainer1", "test2");

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
    public void getBlockBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        String content = "some content";
        contract.putBlockBlob("mycontainer1", "test2", new ByteArrayInputStream(content.getBytes("UTF-8")), new PutBlobOptions().setBlobCacheControl("test")
                .setBlobContentEncoding("UTF-8").setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        Blob blob = contract.getBlob("mycontainer1", "test2");
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
        BlobService contract = config.create(BlobService.class);

        // Act
        contract.putPageBlob("mycontainer1", "test", 4096, new PutBlobOptions().setBlobCacheControl("test").setBlobContentEncoding("UTF-8")
                .setBlobContentLanguage("en-us")
                /* .setBlobContentMD5("1234") */.setBlobContentType("text/plain").setCacheControl("test").setContentEncoding("UTF-8")
                /* .setContentMD5("1234") */.setContentType("text/plain"));

        Blob blob = contract.getBlob("mycontainer1", "test");
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
    public void deleteBlobWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobService contract = config.create(BlobService.class);

        // Act
        String content = "some content";
        contract.putBlockBlob("mycontainer1", "test2", new ByteArrayInputStream(content.getBytes("UTF-8")));

        contract.deleteBlob("mycontainer1", "test2");

        // Assert
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
