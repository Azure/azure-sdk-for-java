// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.customization;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;

/**
 * Customization class for Blob Storage.
 */
public class BlobStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization) {

        PackageCustomization impl = customization.getPackage("com.azure.storage.blob.implementation");

        ClassCustomization appendBlobsImpl = impl.getClass("AppendBlobsImpl");
        modifyUnexpectedResponseExceptionType(appendBlobsImpl.getMethod("create"));
        modifyUnexpectedResponseExceptionType(appendBlobsImpl.getMethod("appendBlock"));
        modifyUnexpectedResponseExceptionType(appendBlobsImpl.getMethod("appendBlockFromUrl"));
        modifyUnexpectedResponseExceptionType(appendBlobsImpl.getMethod("seal"));

        ClassCustomization blobsImpl = impl.getClass("BlobsImpl");
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("download"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("delete"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("setAccessControl"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("getAccessControl"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("rename"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("undelete"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("setExpiry"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("setHttpHeaders"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("setMetadata"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("acquireLease"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("releaseLease"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("renewLease"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("changeLease"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("breakLease"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("createSnapshot"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("renewLease"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("changeLease"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("startCopyFromURL"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("copyFromURL"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("abortCopyFromURL"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("setTier"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("getAccountInfo"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("query"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("getTags"));
        modifyUnexpectedResponseExceptionType(blobsImpl.getMethod("setTags"));

        ClassCustomization blockBlobsImpl = impl.getClass("BlockBlobsImpl");
        modifyUnexpectedResponseExceptionType(blockBlobsImpl.getMethod("upload"));
        modifyUnexpectedResponseExceptionType(blockBlobsImpl.getMethod("putBlobFromUrl"));
        modifyUnexpectedResponseExceptionType(blockBlobsImpl.getMethod("stageBlock"));
        modifyUnexpectedResponseExceptionType(blockBlobsImpl.getMethod("stageBlockFromURL"));
        modifyUnexpectedResponseExceptionType(blockBlobsImpl.getMethod("commitBlockList"));
        modifyUnexpectedResponseExceptionType(blockBlobsImpl.getMethod("getBlockList"));

        ClassCustomization containersImpl = impl.getClass("ContainersImpl");
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("create"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("delete"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("setMetadata"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("getAccessPolicy"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("setAccessPolicy"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("restore"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("rename"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("submitBatch"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("acquireLease"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("releaseLease"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("renewLease"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("breakLease"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("changeLease"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("listBlobFlatSegment"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("listBlobHierarchySegment"));
        modifyUnexpectedResponseExceptionType(containersImpl.getMethod("getAccountInfo"));

        ClassCustomization pageBlobsImpl = impl.getClass("PageBlobsImpl");
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("create"));
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("uploadPages"));
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("clearPages"));
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("uploadPagesFromURL"));
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("getPageRanges"));
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("getPageRangesDiff"));
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("resize"));
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("updateSequenceNumber"));
        modifyUnexpectedResponseExceptionType(pageBlobsImpl.getMethod("copyIncremental"));

        ClassCustomization servicesImpl = impl.getClass("ServicesImpl");
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("setProperties"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("getStatistics"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("listBlobContainersSegment"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("getUserDelegationKey"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("getAccountInfo"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("submitBatch"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("filterBlobs"));

        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.blob.implementation.models");
        implementationModels.getClass("BlobHierarchyListSegment").addAnnotation("@JsonDeserialize(using = com.azure.storage.blob.implementation.util.CustomHierarchicalListingDeserializer.class)");

        PackageCustomization models = customization.getPackage("com.azure.storage.blob.models");
        models.getClass("PageList").addAnnotation("@JsonDeserialize(using = PageListDeserializer.class)");

//        ClassCustomization block = models.getClass("Block");
//
//        MethodCustomization getSizeInt = block.getMethod("getSizeInt");
//        getSizeInt.addAnnotation("@Deprecated");
//        getSizeInt.getJavadoc().setDeprecated("Use {@link #getSizeLong()}");
////        getSizeInt.setReturnType("int", "(int) this.sizeLong", true);
//        getSizeInt.rename("getSize");
//
//        MethodCustomization setSizeInt = block.getMethod("setSizeInt");
//        setSizeInt.addAnnotation("@Deprecated");
//        setSizeInt.getJavadoc().setDeprecated("Use {@link #setSizeLong(long)}");
////        setSizeInt.setReturnType("Block", "setSizeLong(sizeInt)", true);
//        setSizeInt.rename("setSize");

        ClassCustomization blobServiceProperties = models.getClass("BlobServiceProperties");
        PropertyCustomization hourMetrics = blobServiceProperties.getProperty("hourMetrics");
        hourMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")");
        hourMetrics.addAnnotation("@JsonProperty(value = \"HourMetrics\")");
        PropertyCustomization minuteMetrics = blobServiceProperties.getProperty("minuteMetrics");
        minuteMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")");
        minuteMetrics.addAnnotation("@JsonProperty(value = \"MinuteMetrics\")");
        PropertyCustomization deleteRetentionPolicy = blobServiceProperties.getProperty("deleteRetentionPolicy");
        deleteRetentionPolicy.removeAnnotation("@JsonProperty(value = \"RetentionPolicy\")");
        deleteRetentionPolicy.addAnnotation("@JsonProperty(value = \"DeleteRetentionPolicy\")");

    }

    private void modifyUnexpectedResponseExceptionType(MethodCustomization method) {
        method.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        method.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.blob.models.BlobStorageException.class)");
    }
}
