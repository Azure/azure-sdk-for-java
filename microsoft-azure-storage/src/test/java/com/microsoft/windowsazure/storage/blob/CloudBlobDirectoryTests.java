package com.microsoft.windowsazure.storage.blob;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.microsoft.windowsazure.storage.ResultContinuation;
import com.microsoft.windowsazure.storage.ResultSegment;
import com.microsoft.windowsazure.storage.StorageException;
import com.microsoft.windowsazure.storage.TestBase;

@RunWith(Parameterized.class)
public class CloudBlobDirectoryTests extends BlobTestBase {

    private final String delimiter;
    protected CloudBlobContainer container;

    private static void setupWithDelimiter(CloudBlobContainer container, String delimiter) throws URISyntaxException,
            StorageException {
        for (int i = 1; i < 3; i++) {
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 3; k++) {
                    String directory = "TopDir" + i + delimiter + "MidDir" + j + delimiter + "EndDir" + k + delimiter
                            + "EndBlob" + k;
                    CloudPageBlob blob1 = container.getPageBlobReference(directory);
                    blob1.create(0);
                }
            }

            CloudPageBlob blob2 = container.getPageBlobReference("TopDir" + i + delimiter + "Blob" + i);
            blob2.create(0);
        }
    }

    /**
     * These parameters are passed to the constructor at the start of each test run. This includes TablePayloadFormat,
     * and if that format is JsonNoMetadata, whether or not to use a PropertyResolver
     * 
     * @return the parameters pass to the constructor
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays
                .asList(new Object[][] { { "$" }, { "@" }, { "-" }, { "%" }, { "/" }, { "|" }, { "//" }, { "%%" } });
    }

    public CloudBlobDirectoryTests(String delimiter) {
        this.delimiter = delimiter;
    }

    @Before
    public void directoryTestMethodSetup() throws URISyntaxException, StorageException {
        CloudBlobClient client = TestBase.createCloudBlobClient();
        client.setDirectoryDelimiter(delimiter);
        String name = BlobTestBase.generateRandomContainerName();
        container = client.getContainerReference(name);
        container.create();
    }

    @After
    public void directoryTestMethodTearDown() throws StorageException {
        container.deleteIfExists();
    }

    @Test
    public void testGetReferences() throws URISyntaxException, StorageException, IOException {
        CloudBlobDirectory dir1 = container.getDirectoryReference("Dir1");

        // get references to blobs, block blobs and directories from the directory and create them
        CloudPageBlob pageBlob = dir1.getPageBlobReference("PageBlob");
        pageBlob.create(0);
        assertTrue(pageBlob.exists());
        assertEquals(dir1.getContainer().getName(), pageBlob.getContainer().getName());
        assertEquals(dir1.getServiceClient().getEndpoint().toString(), pageBlob.getServiceClient().getEndpoint()
                .toString());
        assertEquals("Dir1" + delimiter + "PageBlob", pageBlob.getName());
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/Dir1" + delimiter + "PageBlob", null, null), pageBlob.getUri());
        CloudBlobDirectory pageParent = pageBlob.getParent();
        assertEquals("Dir1" + delimiter, pageParent.getPrefix());

        CloudBlockBlob blockBlob = dir1.getBlockBlobReference("BlockBlob");
        blockBlob.upload(BlobTestBase.getRandomDataStream(128), 128);
        assertTrue(blockBlob.exists());
        assertEquals(dir1.getContainer().getName(), pageBlob.getContainer().getName());
        assertEquals(dir1.getServiceClient().getEndpoint().toString(), blockBlob.getServiceClient().getEndpoint()
                .toString());
        assertEquals("Dir1" + delimiter + "BlockBlob", blockBlob.getName());
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/Dir1" + delimiter + "BlockBlob", null, null), blockBlob.getUri());
        CloudBlobDirectory blockParent = blockBlob.getParent();
        assertEquals("Dir1" + delimiter, blockParent.getPrefix());

        CloudBlobDirectory subDirectory = dir1.getSubDirectoryReference("SubDirectory");
        assertEquals(dir1.getContainer().getName(), subDirectory.getContainer().getName());
        assertEquals(dir1.getServiceClient().getEndpoint().toString(), subDirectory.getServiceClient().getEndpoint()
                .toString());
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/Dir1" + delimiter + "SubDirectory" + delimiter, null, null), subDirectory.getUri());
        CloudBlobDirectory subDirectoryParent = subDirectory.getParent();
        assertEquals("Dir1" + delimiter, subDirectoryParent.getPrefix());

        // create snapshots for page and block blobs and then get references to them using the directory
        CloudPageBlob pageSnapshot = (CloudPageBlob) pageBlob.createSnapshot();
        CloudPageBlob pageSnapshotDir1 = dir1.getPageBlobReference("PageBlob", pageSnapshot.getSnapshotID());
        assertEquals(pageSnapshot.getName(), pageSnapshotDir1.getName());
        assertEquals(dir1.getContainer().getName(), pageSnapshotDir1.getContainer().getName());
        assertEquals(dir1.getServiceClient().getEndpoint().toString(), pageSnapshotDir1.getServiceClient()
                .getEndpoint().toString());
        assertEquals(pageSnapshot.getUri().toString(), pageSnapshotDir1.getUri().toString());
        assertEquals(pageSnapshot.getParent().getPrefix(), pageSnapshotDir1.getParent().getPrefix());

        CloudBlockBlob blockSnapshot = (CloudBlockBlob) blockBlob.createSnapshot();
        CloudBlockBlob blockSnapshotDir1 = dir1.getBlockBlobReference("BlockBlob", blockSnapshot.getSnapshotID());
        assertEquals(blockSnapshot.getName(), blockSnapshotDir1.getName());
        assertEquals(dir1.getContainer().getName(), blockSnapshotDir1.getContainer().getName());
        assertEquals(dir1.getServiceClient().getEndpoint().toString(), blockSnapshotDir1.getServiceClient()
                .getEndpoint().toString());
        assertEquals(blockSnapshot.getUri().toString(), blockSnapshotDir1.getUri().toString());
        assertEquals(blockSnapshot.getParent().getPrefix(), blockSnapshotDir1.getParent().getPrefix());
    }

    @Test
    public void testGetParent() throws URISyntaxException, StorageException {
        CloudPageBlob blob = container.getPageBlobReference("Dir1" + delimiter + "Blob1");
        blob.create(0);
        assertTrue(blob.exists());
        assertEquals(blob.getName(), "Dir1" + delimiter + "Blob1");
        CloudBlobDirectory parent = blob.getParent();
        assertEquals("Dir1" + delimiter, parent.getPrefix());
        blob.delete();
    }

    @Test
    public void testFlatListingWithContainer() throws URISyntaxException, StorageException {
        setupWithDelimiter(container, delimiter);

        // check output of list blobs with TopDir1 and hierarchical
        Iterable<ListBlobItem> list1 = container.listBlobs("TopDir1" + delimiter, false,
                EnumSet.noneOf(BlobListingDetails.class), null, null);

        Iterator<ListBlobItem> iter = list1.iterator();
        ArrayList<ListBlobItem> simpleList1 = new ArrayList<ListBlobItem>();
        while (iter.hasNext()) {
            simpleList1.add(iter.next());
        }

        assertEquals(3, simpleList1.size());

        ListBlobItem get11 = simpleList1.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "Blob1", null, null), get11.getUri());

        ListBlobItem get12 = simpleList1.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter, null, null), get12.getUri());

        ListBlobItem get13 = simpleList1.get(2);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter, null, null), get13.getUri());

        // check output of list blobs with TopDir1 + delimiter + MidDir1
        Iterable<ListBlobItem> list2 = container.listBlobs("TopDir1" + delimiter + "MidDir1", true,
                EnumSet.noneOf(BlobListingDetails.class), null, null);

        iter = list2.iterator();
        ArrayList<ListBlobItem> simpleList2 = new ArrayList<ListBlobItem>();
        while (iter.hasNext()) {
            simpleList2.add(iter.next());
        }

        assertEquals(2, simpleList2.size());

        ListBlobItem get21 = simpleList2.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter + "EndBlob1", null,
                null), get21.getUri());

        ListBlobItem get22 = simpleList2.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir2" + delimiter + "EndBlob2", null,
                null), get22.getUri());

        // check output of list blobs with TopDir1 + delimiter + MidDir1 + delimiter
        Iterable<ListBlobItem> list3 = container.listBlobs("TopDir1" + delimiter + "MidDir1" + delimiter, false,
                EnumSet.noneOf(BlobListingDetails.class), null, null);

        iter = list3.iterator();
        ArrayList<ListBlobItem> simpleList3 = new ArrayList<ListBlobItem>();
        while (iter.hasNext()) {
            simpleList3.add(iter.next());
        }

        assertEquals(2, simpleList3.size());

        ListBlobItem get31 = simpleList3.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter, null, null),
                get31.getUri());

        ListBlobItem get32 = simpleList3.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir2" + delimiter, null, null),
                get32.getUri());

        // check output of list blobs by listing using midDir
        CloudBlobDirectory midDir2 = (CloudBlobDirectory) get13;

        Iterable<ListBlobItem> list4 = midDir2.listBlobs(null, true, EnumSet.noneOf(BlobListingDetails.class), null,
                null);

        iter = list4.iterator();
        ArrayList<ListBlobItem> simpleList4 = new ArrayList<ListBlobItem>();
        while (iter.hasNext()) {
            simpleList4.add(iter.next());
        }

        assertEquals(2, simpleList4.size());

        ListBlobItem get41 = simpleList4.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter + "EndDir1" + delimiter + "EndBlob1", null,
                null), get41.getUri());

        ListBlobItem get42 = simpleList4.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter + "EndDir2" + delimiter + "EndBlob2", null,
                null), get42.getUri());
    }

    @Test
    public void testListingWithDirectory() throws URISyntaxException, StorageException {
        setupWithDelimiter(container, delimiter);

        // check output of list blobs with TopDir1 with prefix
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1" + delimiter + "MidDir1" + delimiter);

        Iterable<ListBlobItem> results = directory.listBlobs();

        ArrayList<ListBlobItem> list1 = new ArrayList<ListBlobItem>();
        Iterator<ListBlobItem> iter = results.iterator();
        while (iter.hasNext()) {
            list1.add(iter.next());
        }

        assertEquals(2, list1.size());

        ListBlobItem get21 = list1.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter, null, null),
                get21.getUri());

        ListBlobItem get22 = list1.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir2" + delimiter, null, null),
                get22.getUri());
    }

    @Test
    public void testListingWithDirectorySegmented() throws URISyntaxException, StorageException {
        setupWithDelimiter(container, delimiter);

        ResultContinuation token = null;
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1");
        ArrayList<ListBlobItem> list1 = new ArrayList<ListBlobItem>();
        do {
            ResultSegment<ListBlobItem> result1 = directory.listBlobsSegmented("Mid");
            token = result1.getContinuationToken();
            list1.addAll(result1.getResults());
        } while (token != null);

        assertEquals(2, list1.size());

        ListBlobItem get21 = list1.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter, null, null), get21.getUri());

        ListBlobItem get22 = list1.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter, null, null), get22.getUri());
    }

    @Test
    public void testPrefixListingWithDirectory() throws URISyntaxException, StorageException {
        setupWithDelimiter(container, delimiter);

        // check output of list blobs with TopDir1 with prefix
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1");

        Iterable<ListBlobItem> results = directory.listBlobs("Mid");

        ArrayList<ListBlobItem> list1 = new ArrayList<ListBlobItem>();
        Iterator<ListBlobItem> iter = results.iterator();
        while (iter.hasNext()) {
            list1.add(iter.next());
        }

        assertEquals(2, list1.size());

        ListBlobItem get12 = list1.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter, null, null), get12.getUri());

        ListBlobItem get13 = list1.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter, null, null), get13.getUri());
    }

    @Test
    public void testPrefixListingWithDirectorySegmented() throws URISyntaxException, StorageException {
        setupWithDelimiter(container, delimiter);

        ResultContinuation token = null;
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1");
        ArrayList<ListBlobItem> list1 = new ArrayList<ListBlobItem>();
        do {
            ResultSegment<ListBlobItem> result1 = directory.listBlobsSegmented("Mid");
            token = result1.getContinuationToken();
            list1.addAll(result1.getResults());
        } while (token != null);

        assertEquals(2, list1.size());

        ListBlobItem get12 = list1.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter, null, null), get12.getUri());

        ListBlobItem get13 = list1.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter, null, null), get13.getUri());
    }

    @Test
    public void testFlatListingWithDirectory() throws URISyntaxException, StorageException {
        setupWithDelimiter(container, delimiter);

        // check output of list blobs with TopDir1 and flat listing
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1");

        Iterable<ListBlobItem> results = directory.listBlobs(null, false, EnumSet.noneOf(BlobListingDetails.class),
                null, null);

        ArrayList<ListBlobItem> list1 = new ArrayList<ListBlobItem>();
        Iterator<ListBlobItem> iter = results.iterator();
        while (iter.hasNext()) {
            list1.add(iter.next());
        }

        assertEquals(3, list1.size());

        ListBlobItem get11 = list1.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "Blob1", null, null), get11.getUri());

        ListBlobItem get12 = list1.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter, null, null), get12.getUri());

        ListBlobItem get13 = list1.get(2);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter, null, null), get13.getUri());

        CloudBlobDirectory midDir2 = (CloudBlobDirectory) get13;

        // check output of list blobs with TopDir1 + delimiter + MidDir1 and hierarchical listing
        results = midDir2.listBlobs(null, true, EnumSet.noneOf(BlobListingDetails.class), null, null);

        ArrayList<ListBlobItem> list2 = new ArrayList<ListBlobItem>();
        iter = results.iterator();
        while (iter.hasNext()) {
            list2.add(iter.next());
        }

        assertEquals(2, list2.size());

        ListBlobItem get21 = list2.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter + "EndDir1" + delimiter + "EndBlob1", null,
                null), get21.getUri());

        ListBlobItem get22 = list2.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter + "EndDir2" + delimiter + "EndBlob2", null,
                null), get22.getUri());
    }

    @Test
    public void testFlatListingWithDirectorySegmented() throws URISyntaxException, StorageException {
        setupWithDelimiter(container, delimiter);

        // check output of list blobs with TopDir1 and flat listing
        ResultContinuation token = null;
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1");
        ArrayList<ListBlobItem> list1 = new ArrayList<ListBlobItem>();
        do {
            ResultSegment<ListBlobItem> result1 = directory.listBlobsSegmented(null, false,
                    EnumSet.noneOf(BlobListingDetails.class), -1, token, null, null);
            token = result1.getContinuationToken();
            list1.addAll(result1.getResults());
        } while (token != null);

        assertEquals(3, list1.size());

        ListBlobItem get11 = list1.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "Blob1", null, null), get11.getUri());

        ListBlobItem get12 = list1.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter, null, null), get12.getUri());

        ListBlobItem get13 = list1.get(2);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter, null, null), get13.getUri());

        CloudBlobDirectory midDir2 = (CloudBlobDirectory) get13;

        // check output of list blobs with TopDir1 + delimiter + MidDir1
        ArrayList<ListBlobItem> list2 = new ArrayList<ListBlobItem>();
        do {
            ResultSegment<ListBlobItem> result1 = midDir2.listBlobsSegmented(null, true,
                    EnumSet.noneOf(BlobListingDetails.class), -1, token, null, null);
            token = result1.getContinuationToken();
            list2.addAll(result1.getResults());
        } while (token != null);

        assertEquals(2, list2.size());

        ListBlobItem get21 = list2.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter + "EndDir1" + delimiter + "EndBlob1", null,
                null), get21.getUri());

        ListBlobItem get22 = list2.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter + "EndDir2" + delimiter + "EndBlob2", null,
                null), get22.getUri());
    }

    @Test
    public void testGetSubdirectoryAndTraverseBackToParent() throws URISyntaxException, StorageException {
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1" + delimiter);
        CloudBlobDirectory subDirectory = directory.getSubDirectoryReference("MidDir1" + delimiter);
        CloudBlobDirectory parent = subDirectory.getParent();
        assertEquals(parent.getPrefix(), directory.getPrefix());
        assertEquals(parent.getUri(), directory.getUri());
    }

    @Test
    public void testGetParentOnRoot() throws URISyntaxException, StorageException {
        CloudBlobDirectory root = container.getDirectoryReference("TopDir1" + delimiter);
        CloudBlobDirectory parent = root.getParent();
        assertNull(parent);
    }

    @Test
    public void testHierarchicalTraversal() throws StorageException, URISyntaxException {
        // Traverse hierarchically starting with length 1
        CloudBlobDirectory directory1 = container.getDirectoryReference("Dir1" + delimiter);

        CloudBlobDirectory subdir1 = directory1.getSubDirectoryReference("Dir2");
        CloudBlobDirectory parent1 = subdir1.getParent();
        assertEquals(parent1.getPrefix(), directory1.getPrefix());

        CloudBlobDirectory subdir2 = subdir1.getSubDirectoryReference("Dir3");
        CloudBlobDirectory parent2 = subdir2.getParent();
        assertEquals(parent2.getPrefix(), subdir1.getPrefix());

        CloudBlobDirectory subdir3 = subdir2.getSubDirectoryReference("Dir4");
        CloudBlobDirectory parent3 = subdir3.getParent();
        assertEquals(parent3.getPrefix(), subdir2.getPrefix());

        CloudBlobDirectory subdir4 = subdir3.getSubDirectoryReference("Dir5");
        CloudBlobDirectory parent4 = subdir4.getParent();
        assertEquals(parent4.getPrefix(), subdir3.getPrefix());
    }

    @Test
    public void testParentValidate() throws StorageException, URISyntaxException {
        CloudBlockBlob blob = container.getBlockBlobReference("TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1"
                + delimiter + "EndBlob1");
        CloudBlobDirectory directory = blob.getParent();
        assertEquals(directory.getPrefix(), "TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter);
        assertEquals(directory.getUri(), new URI(container.getUri().getScheme(), container.getUri().getAuthority(),
                container.getUri().getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter,
                null, null));
    }

    @Test
    public void testValidateInRootContainer() throws URISyntaxException, StorageException {
        CloudBlobClient client = TestBase.createCloudBlobClient();
        client.setDirectoryDelimiter(delimiter);
        CloudBlobContainer container = client.getContainerReference("$root");

        CloudPageBlob pageBlob = container.getPageBlobReference("Dir1" + delimiter + "Blob1");
        if (delimiter.equals("/")) {
            try {
                pageBlob.create(0);
                fail("Try to create a CloudBlobDirectory/blob which has a slash in its name in the root container");
            }
            catch (StorageException e) {
                assertEquals("InvalidUri", e.getErrorCode());
                assertTrue(e.getExtendedErrorInformation().getErrorMessage()
                        .startsWith("The requested URI does not represent any resource on the server."));
            }
        }

        else {
            CloudPageBlob blob = container.getPageBlobReference("TopDir1" + delimiter + "MidDir1" + delimiter
                    + "EndDir1" + delimiter + "EndBlob1");
            CloudBlobDirectory directory = blob.getParent();
            assertEquals(directory.getPrefix(), "TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter);
            assertEquals(directory.getUri(), new URI(container.getUri().getScheme(), container.getUri().getAuthority(),
                    container.getUri().getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1"
                            + delimiter, null, null));
            assertEquals(blob.getUri(), new URI(container.getUri().getScheme(), container.getUri().getAuthority(),
                    container.getUri().getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1"
                            + delimiter + "EndBlob1", null, null));

            CloudBlobDirectory directory1 = container.getDirectoryReference("TopDir1" + delimiter);
            CloudBlobDirectory subdir1 = directory1.getSubDirectoryReference("MidDir" + delimiter);
            CloudBlobDirectory parent1 = subdir1.getParent();
            assertEquals(parent1.getPrefix(), directory1.getPrefix());
            assertEquals(parent1.getUri(), directory1.getUri());

            CloudBlobDirectory subdir2 = subdir1.getSubDirectoryReference("EndDir" + delimiter);
            CloudBlobDirectory parent2 = subdir2.getParent();
            assertEquals(parent2.getPrefix(), subdir1.getPrefix());
            assertEquals(parent2.getUri(), subdir1.getUri());
        }
    }

    @Test
    public void testDelimitersInARow() throws URISyntaxException, StorageException {
        CloudPageBlob blob = container.getPageBlobReference(delimiter + delimiter + delimiter + "Blob1");

        // Traverse from leaf to root
        CloudBlobDirectory directory1 = blob.getParent();
        assertEquals(directory1.getPrefix(), delimiter + delimiter + delimiter);

        CloudBlobDirectory directory2 = directory1.getParent();
        assertEquals(directory2.getPrefix(), delimiter + delimiter);

        CloudBlobDirectory directory3 = directory2.getParent();
        assertEquals(directory3.getPrefix(), delimiter);

        // Traverse from root to leaf
        CloudBlobDirectory directory4 = container.getDirectoryReference(delimiter);
        CloudBlobDirectory directory5 = directory4.getSubDirectoryReference(delimiter);
        assertEquals(directory5.getPrefix(), delimiter + delimiter);

        CloudBlobDirectory directory6 = directory5.getSubDirectoryReference(delimiter);
        assertEquals(directory6.getPrefix(), delimiter + delimiter + delimiter);

        CloudPageBlob blob2 = directory6.getPageBlobReference("Blob1");
        assertEquals(blob2.getName(), delimiter + delimiter + delimiter + "Blob1");
        assertEquals(blob2.getUri(), blob.getUri());
    }

    @Test
    public void testMultipleDelimiters() throws StorageException, URISyntaxException {
        setupWithDelimiter(container, delimiter);
        setupWithDelimiter(container, "*");
        setupWithDelimiter(container, "#");

        Iterable<ListBlobItem> list1 = container.listBlobs("TopDir1" + delimiter, false,
                EnumSet.noneOf(BlobListingDetails.class), null, null);

        Iterator<ListBlobItem> iter = list1.iterator();
        ArrayList<ListBlobItem> simpleList1 = new ArrayList<ListBlobItem>();
        while (iter.hasNext()) {
            simpleList1.add(iter.next());
        }

        assertEquals(3, simpleList1.size());

        ListBlobItem get11 = simpleList1.get(0);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "Blob1", null, null), get11.getUri());

        ListBlobItem get12 = simpleList1.get(1);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter, null, null), get12.getUri());

        ListBlobItem get13 = simpleList1.get(2);
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir2" + delimiter, null, null), get13.getUri());

        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1" + delimiter);
        CloudBlobDirectory subDirectory = directory.getSubDirectoryReference("MidDir1" + delimiter);
        CloudBlobDirectory parent = subDirectory.getParent();
        assertEquals(parent.getPrefix(), directory.getPrefix());
        assertEquals(parent.getUri(), directory.getUri());
    }
}
