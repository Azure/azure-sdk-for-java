package com.azure.storage.blob.customization;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

/**
 * Customization class for Blob Storage.
 */
public class BlobStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization) {
        PackageCustomization impl = customization.getPackage("com.azure.storage.blob.implementation");

        ClassCustomization appendBlobsImpl = impl.getClass("AppendBlobsImpl");
        appendBlobsImpl.getMethod("create").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        appendBlobsImpl.getMethod("appendBlock").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        appendBlobsImpl.getMethod("appendBlockFromUrl").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        appendBlobsImpl.getMethod("seal").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");

        ClassCustomization blobsImpl = impl.getClass("BlobsImpl");
        blobsImpl.getMethod("download").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("delete").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("setAccessControl").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("getAccessControl").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("rename").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("undelete").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("setExpiry").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("setHttpHeaders").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("setMetadata").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("acquireLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("releaseLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("renewLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("changeLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("breakLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("createSnapshot").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("startCopyFromURL").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("copyFromURL").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("abortCopyFromURL").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("setTier").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("getAccountInfo").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("query").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("getTags").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blobsImpl.getMethod("setTags").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");

        ClassCustomization blockBlobsImpl = impl.getClass("BlockBlobsImpl");
        blockBlobsImpl.getMethod("upload").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blockBlobsImpl.getMethod("putBlobFromUrl").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blockBlobsImpl.getMethod("stageBlock").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blockBlobsImpl.getMethod("stageBlockFromURL").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blockBlobsImpl.getMethod("commitBlockList").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        blockBlobsImpl.getMethod("getBlockList").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");

        ClassCustomization containersImpl = impl.getClass("ContainersImpl");
        containersImpl.getMethod("create").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("delete").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("setMetadata").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("getAccessPolicy").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("setAccessPolicy").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("restore").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("rename").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("submitBatch").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("acquireLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("releaseLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("renewLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("breakLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("changeLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("listBlobFlatSegment").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("listBlobHierarchySegment").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        containersImpl.getMethod("getAccountInfo").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");

        ClassCustomization pageBlobsImpl = impl.getClass("PageBlobsImpl");
        pageBlobsImpl.getMethod("create").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        pageBlobsImpl.getMethod("uploadPages").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        pageBlobsImpl.getMethod("clearPages").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        pageBlobsImpl.getMethod("uploadPagesFromURL").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        pageBlobsImpl.getMethod("getPageRanges").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        pageBlobsImpl.getMethod("getPageRangesDiff").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        pageBlobsImpl.getMethod("resize").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        pageBlobsImpl.getMethod("updateSequenceNumber").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        pageBlobsImpl.getMethod("copyIncremental").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");

        ClassCustomization servicesImpl = impl.getClass("ServicesImpl");
        servicesImpl.getMethod("setProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        servicesImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        servicesImpl.getMethod("getStatistics").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        servicesImpl.getMethod("listBlobContainersSegment").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        servicesImpl.getMethod("getUserDelegationKey").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        servicesImpl.getMethod("getAccountInfo").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        servicesImpl.getMethod("submitBatch").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
        servicesImpl.getMethod("filterBlobs").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");

        PackageCustomization models = customization.getPackage("com.azure.storage.blob.models");
        ClassCustomization block = models.getClass("Block");
        block.getMethod("getSizeInt").rename("getSize");
        block.getMethod("setSizeInt").rename("setSize");
        // TODO add deprecation annotations and such

    }
}
