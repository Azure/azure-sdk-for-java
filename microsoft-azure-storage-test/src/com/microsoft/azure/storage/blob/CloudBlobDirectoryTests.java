package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

import static org.junit.Assert.*;

@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class CloudBlobDirectoryTests {

    private static final String[] delimiters = { "$", ":", "@", "-", "%", "/", "|", "//", "%%", " " };

    @Test
    public void testGetDirectoryAbsoluteUriAppended() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testGetDirectoryAbsoluteUriAppended(delimiters[i], createContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testGetDirectoryAbsoluteUriAppended(String delimiter, CloudBlobContainer container)
            throws URISyntaxException {
        CloudBlobDirectory dir = container.getDirectoryReference(container.getUri().toString());
        assertEquals(PathUtility.appendPathToSingleUri(container.getUri(), container.getUri().toString() + delimiter),
                dir.getUri());

        dir = container.getDirectoryReference(container.getUri().toString() + "/" + "TopDir1" + delimiter);
        assertEquals(
                PathUtility.appendPathToSingleUri(container.getUri(), container.getUri().toString() + "/" + "TopDir1"
                        + delimiter), dir.getUri());
    }

    @Test
    public void testFlatListingWithContainer() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testFlatListingWithContainer(delimiters[i], createAndPopulateContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testFlatListingWithContainer(String delimiter, CloudBlobContainer container)
            throws URISyntaxException, StorageException {
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
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testListingWithDirectory(delimiters[i], createAndPopulateContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testListingWithDirectory(String delimiter, CloudBlobContainer container) throws URISyntaxException,
            StorageException {
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
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testListingWithDirectorySegmented(delimiters[i], createAndPopulateContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testListingWithDirectorySegmented(String delimiter, CloudBlobContainer container)
            throws URISyntaxException, StorageException {
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
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testPrefixListingWithDirectory(delimiters[i], createAndPopulateContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testPrefixListingWithDirectory(String delimiter, CloudBlobContainer container)
            throws URISyntaxException, StorageException {
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
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testPrefixListingWithDirectorySegmented(delimiters[i], createAndPopulateContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testPrefixListingWithDirectorySegmented(String delimiter, CloudBlobContainer container)
            throws URISyntaxException, StorageException {
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
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testFlatListingWithDirectory(delimiters[i], createAndPopulateContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testFlatListingWithDirectory(String delimiter, CloudBlobContainer container)
            throws URISyntaxException, StorageException {
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
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testFlatListingWithDirectorySegmented(delimiters[i], createAndPopulateContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testFlatListingWithDirectorySegmented(String delimiter, CloudBlobContainer container)
            throws URISyntaxException, StorageException {
        // check output of list blobs with TopDir1 and flat listing
        ResultContinuation token = null;
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1");
        ArrayList<ListBlobItem> list1 = new ArrayList<ListBlobItem>();
        do {
            ResultSegment<ListBlobItem> result1 = directory.listBlobsSegmented(null, false,
                    EnumSet.noneOf(BlobListingDetails.class), null, token, null, null);
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
                    EnumSet.noneOf(BlobListingDetails.class), null, token, null, null);
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
    public void testGetParent() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testGetParent(delimiters[i], createContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testGetParent(String delimiter, CloudBlobContainer container) throws URISyntaxException,
            StorageException {
        CloudPageBlob blob = container.getPageBlobReference("Dir1" + delimiter + "Blob1");
        blob.create(0);
        try {
            assertTrue(blob.exists());
            assertEquals(blob.getName(), "Dir1" + delimiter + "Blob1");
            CloudBlobDirectory parent = blob.getParent();
            assertEquals("Dir1" + delimiter, parent.getPrefix());
        }
        finally {
            blob.deleteIfExists();
        }
    }

    @Test
    public void testGetReferences() throws URISyntaxException, StorageException, IOException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testGetReferences(delimiters[i], createContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testGetReferences(String delimiter, CloudBlobContainer container) throws URISyntaxException,
            StorageException, IOException {
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
        blockBlob.upload(BlobTestHelper.getRandomDataStream(128), 128);
        assertTrue(blockBlob.exists());
        assertEquals(dir1.getContainer().getName(), pageBlob.getContainer().getName());
        assertEquals(dir1.getServiceClient().getEndpoint().toString(), blockBlob.getServiceClient().getEndpoint()
                .toString());
        assertEquals("Dir1" + delimiter + "BlockBlob", blockBlob.getName());
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/Dir1" + delimiter + "BlockBlob", null, null), blockBlob.getUri());
        CloudBlobDirectory blockParent = blockBlob.getParent();
        assertEquals("Dir1" + delimiter, blockParent.getPrefix());

        CloudBlobDirectory subDirectory = dir1.getDirectoryReference("SubDirectory");
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
    public void testGetSubdirectoryAndTraverseBackToParent() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testGetSubdirectoryAndTraverseBackToParent(delimiters[i], createContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testGetSubdirectoryAndTraverseBackToParent(String delimiter, CloudBlobContainer container)
            throws URISyntaxException, StorageException {
        CloudBlobDirectory directory = container.getDirectoryReference("TopDir1" + delimiter);
        CloudBlobDirectory subDirectory = directory.getDirectoryReference("MidDir1" + delimiter);
        CloudBlobDirectory parent = subDirectory.getParent();
        assertEquals(parent.getPrefix(), directory.getPrefix());
        assertEquals(parent.getUri(), directory.getUri());
    }

    @Test
    public void testGetParentOnRoot() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testGetParentOnRoot(delimiters[i], createAndPopulateContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testGetParentOnRoot(String delimiter, CloudBlobContainer container) throws URISyntaxException,
            StorageException {
        // get container as parent
        CloudBlobDirectory topDir = container.getDirectoryReference("TopDir1" + delimiter);
        CloudBlobDirectory root = topDir.getParent();
        assertEquals("", root.getPrefix());
        assertEquals(container.getUri().toString(), root.getUri().toString());

        // make sure the parent of the container dir is null
        CloudBlobDirectory empty = root.getParent();
        assertNull(empty);

        // from container, get directory reference to container
        root = container.getDirectoryReference("");
        assertEquals("", root.getPrefix());
        assertEquals(container.getUri().toString(), root.getUri().toString());

        Iterable<ListBlobItem> results = root.listBlobs();

        ArrayList<ListBlobItem> list1 = new ArrayList<ListBlobItem>();
        Iterator<ListBlobItem> iter = results.iterator();
        while (iter.hasNext()) {
            list1.add(iter.next());
        }
        assertEquals(2, list1.size());

        // make sure the parent of the container dir is null
        empty = root.getParent();
        assertNull(empty);
    }

    @Test
    public void testHierarchicalTraversal() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testHierarchicalTraversal(delimiters[i], createContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testHierarchicalTraversal(String delimiter, CloudBlobContainer container) throws StorageException,
            URISyntaxException {
        // Traverse hierarchically starting with length 1
        CloudBlobDirectory directory1 = container.getDirectoryReference("Dir1" + delimiter);

        CloudBlobDirectory subdir1 = directory1.getDirectoryReference("Dir2");
        CloudBlobDirectory parent1 = subdir1.getParent();
        assertEquals(parent1.getPrefix(), directory1.getPrefix());

        CloudBlobDirectory subdir2 = subdir1.getDirectoryReference("Dir3");
        CloudBlobDirectory parent2 = subdir2.getParent();
        assertEquals(parent2.getPrefix(), subdir1.getPrefix());

        CloudBlobDirectory subdir3 = subdir2.getDirectoryReference("Dir4");
        CloudBlobDirectory parent3 = subdir3.getParent();
        assertEquals(parent3.getPrefix(), subdir2.getPrefix());

        CloudBlobDirectory subdir4 = subdir3.getDirectoryReference("Dir5");
        CloudBlobDirectory parent4 = subdir4.getParent();
        assertEquals(parent4.getPrefix(), subdir3.getPrefix());
    }

    @Test
    public void testParentValidate() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testParentValidate(delimiters[i], createContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testParentValidate(String delimiter, CloudBlobContainer container) throws StorageException,
            URISyntaxException {
        CloudBlockBlob blob = container.getBlockBlobReference("TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1"
                + delimiter + "EndBlob1");
        CloudBlobDirectory directory = blob.getParent();
        assertEquals("TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter, directory.getPrefix());
        assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter, null, null),
                directory.getUri());
    }

    @Test
    public void testValidateInRootContainer() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            testValidateInRootContainer(delimiters[i]);
        }
    }

    private void testValidateInRootContainer(String delimiter) throws URISyntaxException, StorageException {
        CloudBlobClient client = BlobTestHelper.createCloudBlobClient();
        client.setDirectoryDelimiter(delimiter);
        CloudBlobContainer container = client.getContainerReference("$root");

        CloudPageBlob pageBlob = container.getPageBlobReference("Dir1" + delimiter + "Blob1");
        if (delimiter.equals("/")) {
            try {
                pageBlob.create(0);
                pageBlob.deleteIfExists();
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
            assertEquals("TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter, directory.getPrefix());
            assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                    .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter, null, null),
                    directory.getUri());
            assertEquals(new URI(container.getUri().getScheme(), container.getUri().getAuthority(), container.getUri()
                    .getPath() + "/TopDir1" + delimiter + "MidDir1" + delimiter + "EndDir1" + delimiter + "EndBlob1",
                    null, null), blob.getUri());

            CloudBlobDirectory directory1 = container.getDirectoryReference("TopDir1" + delimiter);
            CloudBlobDirectory root = directory1.getParent();
            assertEquals("", root.getPrefix());
            assertEquals(container.getUri(), root.getUri());

            CloudBlobDirectory subdir1 = directory1.getDirectoryReference("MidDir" + delimiter);
            CloudBlobDirectory parent1 = subdir1.getParent();
            assertEquals(directory1.getPrefix(), parent1.getPrefix());
            assertEquals(directory1.getUri(), parent1.getUri());

            CloudBlobDirectory subdir2 = subdir1.getDirectoryReference("EndDir" + delimiter);
            CloudBlobDirectory parent2 = subdir2.getParent();
            assertEquals(subdir1.getPrefix(), parent2.getPrefix());
            assertEquals(subdir1.getUri(), parent2.getUri());
        }
    }

    @Test
    public void testDelimitersInARow() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testDelimitersInARow(delimiters[i], createContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testDelimitersInARow(String delimiter, CloudBlobContainer container) throws URISyntaxException,
            StorageException {
        CloudPageBlob blob = container.getPageBlobReference(delimiter + delimiter + delimiter + "Blob1");

        // Traverse from leaf to root
        CloudBlobDirectory directory1 = blob.getParent();
        assertEquals(delimiter + delimiter + delimiter, directory1.getPrefix());

        CloudBlobDirectory directory2 = directory1.getParent();
        assertEquals(delimiter + delimiter, directory2.getPrefix());

        CloudBlobDirectory directory3 = directory2.getParent();
        assertEquals(delimiter, directory3.getPrefix());

        // Traverse from root to leaf
        CloudBlobDirectory directory4 = container.getDirectoryReference(delimiter);
        CloudBlobDirectory directory5 = directory4.getDirectoryReference(delimiter);
        assertEquals(delimiter + delimiter, directory5.getPrefix());

        CloudBlobDirectory directory6 = directory5.getDirectoryReference(delimiter);
        assertEquals(delimiter + delimiter + delimiter, directory6.getPrefix());

        CloudPageBlob blob2 = directory6.getPageBlobReference("Blob1");
        assertEquals(delimiter + delimiter + delimiter + "Blob1", blob2.getName());
        assertEquals(blob.getUri(), blob2.getUri());
    }

    @Test
    public void testMultipleDelimiters() throws URISyntaxException, StorageException {
        for (int i = 0; i < delimiters.length; i++) {
            CloudBlobContainer container = null;
            try {
                container = createContainer(delimiters[i]);
                testMultipleDelimiters(delimiters[i], createContainer(delimiters[i]));
            }
            finally {
                container.deleteIfExists();
            }
        }
    }

    private void testMultipleDelimiters(String delimiter, CloudBlobContainer container) throws StorageException,
            URISyntaxException {
        populateContainer(container, delimiter);
        populateContainer(container, "*");
        populateContainer(container, "#");

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
        CloudBlobDirectory subDirectory = directory.getDirectoryReference("MidDir1" + delimiter);
        CloudBlobDirectory parent = subDirectory.getParent();
        assertEquals(parent.getPrefix(), directory.getPrefix());
        assertEquals(parent.getUri(), directory.getUri());
    }

    private static CloudBlobContainer createAndPopulateContainer(String delimiter) throws URISyntaxException,
            StorageException {
        CloudBlobContainer container = createContainer(delimiter);
        populateContainer(container, delimiter);
        return container;
    }

    private static void populateContainer(CloudBlobContainer container, String delimiter) throws URISyntaxException,
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

    private static CloudBlobContainer createContainer(String delimiter) throws URISyntaxException, StorageException {
        CloudBlobClient client = BlobTestHelper.createCloudBlobClient();
        client.setDirectoryDelimiter(delimiter);
        String name = BlobTestHelper.generateRandomContainerName();
        CloudBlobContainer container = client.getContainerReference(name);
        container.create();
        return container;
    }
}