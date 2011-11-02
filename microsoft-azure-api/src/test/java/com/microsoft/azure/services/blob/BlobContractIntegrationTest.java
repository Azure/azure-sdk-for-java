package com.microsoft.azure.services.blob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.blob.BlobContract;
import com.microsoft.azure.services.blob.ContainerListingDetails;
import com.microsoft.azure.services.blob.ContainerProperties;
import com.microsoft.azure.services.blob.CreateContainerOptions;
import com.microsoft.azure.services.blob.ListBlobsOptions;
import com.microsoft.azure.services.blob.ListBlobsResults;
import com.microsoft.azure.services.blob.ListContainersOptions;
import com.microsoft.azure.services.blob.ListContainersResults;

public class BlobContractIntegrationTest extends IntegrationTestBase {

    @Test
    public void createContainerWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract contract = config.create(BlobContract.class);

        // Act
        contract.createContainer("foo");
        contract.deleteContainer("foo");

        // Assert
    }

    @Test
    public void createContainerWithMetadataWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract contract = config.create(BlobContract.class);

        // Act
        contract.createContainer("foo2", new CreateContainerOptions()
                .setPublicAccess("blob").addMetadata("test", "bar")
                .addMetadata("blah", "bleah"));

        ContainerProperties prop = contract.getContainerMetadata("foo2");
        ContainerProperties prop2 = contract.getContainerProperties("foo2");

        ListContainersResults results2 = contract
                .listContainers(new ListContainersOptions().setPrefix("foo2")
                        .setListingDetails(
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
        assertTrue(results2.getContainers().get(0).getMetadata()
                .containsKey("test"));
        assertTrue(results2.getContainers().get(0).getMetadata()
                .containsValue("bar"));
        assertTrue(results2.getContainers().get(0).getMetadata()
                .containsKey("blah"));
        assertTrue(results2.getContainers().get(0).getMetadata()
                .containsValue("bleah"));
    }

    @Test
    public void listContainersWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract contract = config.create(BlobContract.class);

        // Act
        ListContainersResults results = contract.listContainers();

        // Assert
        assertNotNull(results);
        assertEquals(8, results.getContainers().size());
    }

    @Test
    public void listContainersWithPaginationWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        BlobContract contract = config.create(BlobContract.class);

        // Act
        ListContainersResults results = contract
                .listContainers(new ListContainersOptions().setMaxResults(3));

        // Assert
        assertNotNull(results);
        assertEquals(3, results.getContainers().size());
        assertNotNull(results.getNextMarker());
        assertEquals(3, results.getMaxResults());

        // Act
        ListContainersResults results2 = contract
                .listContainers(new ListContainersOptions().setMarker(results
                        .getNextMarker()));

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
        BlobContract contract = config.create(BlobContract.class);

        // Act
        ListContainersResults results = contract
                .listContainers(new ListContainersOptions()
                        .setPrefix("mycontainer1"));

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
        BlobContract contract = config.create(BlobContract.class);

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
        BlobContract contract = config.create(BlobContract.class);

        // Act
        ListBlobsResults results = contract.listBlobs("mycontainer11",
                new ListBlobsOptions().setPrefix("Create"));

        // Assert
        assertNotNull(results);
        assertEquals(2, results.getBlobs().size());
    }
}
