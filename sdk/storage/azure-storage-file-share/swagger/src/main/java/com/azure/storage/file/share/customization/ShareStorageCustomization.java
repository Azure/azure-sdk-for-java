// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.customization;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;

/**
 * Customization class for File Share Storage.
 */
public class ShareStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization) {
        PackageCustomization implementation = customization.getPackage("com.azure.storage.file.share.implementation");

        ClassCustomization directoriesImpl = implementation.getClass("DirectoriesImpl");
        MethodCustomization create = directoriesImpl.getMethod("create");
        create.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        create.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization getProperties = directoriesImpl.getMethod("getProperties");
        getProperties.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getProperties.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization delete = directoriesImpl.getMethod("delete");
        delete.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        delete.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization setProperties = directoriesImpl.getMethod("setProperties");
        setProperties.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setProperties.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization setMetadata = directoriesImpl.getMethod("setMetadata");
        setMetadata.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setMetadata.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization listFilesAndDirectoriesSegment = directoriesImpl.getMethod("listFilesAndDirectoriesSegment");
        listFilesAndDirectoriesSegment.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        listFilesAndDirectoriesSegment.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization listHandles = directoriesImpl.getMethod("listHandles");
        listHandles.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        listHandles.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization forceCloseHandles = directoriesImpl.getMethod("forceCloseHandles");
        forceCloseHandles.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        forceCloseHandles.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");

        ClassCustomization filesImpl = implementation.getClass("FilesImpl");
        MethodCustomization create1 = filesImpl.getMethod("create");
        create1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        create1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization download = filesImpl.getMethod("download");
        download.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        download.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization getProperties1 = filesImpl.getMethod("getProperties");
        getProperties1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getProperties1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization delete1 = filesImpl.getMethod("delete");
        delete1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        delete1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization setHttpHeaders = filesImpl.getMethod("setHttpHeaders");
        setHttpHeaders.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setHttpHeaders.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization setMetadata1 = filesImpl.getMethod("setMetadata");
        setMetadata1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setMetadata1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization uploadRange = filesImpl.getMethod("uploadRange");
        uploadRange.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        uploadRange.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization uploadRangeFromURL = filesImpl.getMethod("uploadRangeFromURL");
        uploadRangeFromURL.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        uploadRangeFromURL.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization getRangeList = filesImpl.getMethod("getRangeList");
        getRangeList.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getRangeList.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization startCopy = filesImpl.getMethod("startCopy");
        startCopy.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        startCopy.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization abortCopy = filesImpl.getMethod("abortCopy");
        abortCopy.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        abortCopy.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization listHandles1 = filesImpl.getMethod("listHandles");
        listHandles1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        listHandles1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization forceCloseHandles1 = filesImpl.getMethod("forceCloseHandles");
        forceCloseHandles1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        forceCloseHandles1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization acquireLease = filesImpl.getMethod("acquireLease");
        acquireLease.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        acquireLease.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization releaseLease = filesImpl.getMethod("releaseLease");
        releaseLease.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        releaseLease.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization changeLease = filesImpl.getMethod("changeLease");
        changeLease.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        changeLease.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization breakLease = filesImpl.getMethod("breakLease");
        breakLease.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        breakLease.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");

        ClassCustomization servicesImpl = implementation.getClass("ServicesImpl");
        MethodCustomization setProperties1 = servicesImpl.getMethod("setProperties");
        setProperties1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setProperties1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization getProperties2 = servicesImpl.getMethod("getProperties");
        getProperties2.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getProperties2.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization listSharesSegment = servicesImpl.getMethod("listSharesSegment");
        listSharesSegment.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        listSharesSegment.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization listSharesSegmentNext = servicesImpl.getMethod("listSharesSegmentNext");
        listSharesSegmentNext.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        listSharesSegmentNext.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");

        ClassCustomization sharesImpl = implementation.getClass("SharesImpl");
        MethodCustomization create2 = sharesImpl.getMethod("create");
        create2.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        create2.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization getProperties3 = sharesImpl.getMethod("getProperties");
        getProperties3.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getProperties3.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization delete2 = sharesImpl.getMethod("delete");
        delete2.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        delete2.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization acquireLease1 = sharesImpl.getMethod("acquireLease");
        acquireLease1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        acquireLease1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization releaseLease1 = sharesImpl.getMethod("releaseLease");
        releaseLease1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        releaseLease1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization changeLease1 = sharesImpl.getMethod("changeLease");
        changeLease1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        changeLease1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization renewLease1 = sharesImpl.getMethod("renewLease");
        renewLease1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        renewLease1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization breakLease1 = sharesImpl.getMethod("breakLease");
        breakLease1.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        breakLease1.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization createSnapshot = sharesImpl.getMethod("createSnapshot");
        createSnapshot.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        createSnapshot.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization createPermission = sharesImpl.getMethod("createPermission");
        createPermission.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        createPermission.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization getPermission = sharesImpl.getMethod("getPermission");
        getPermission.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getPermission.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization setProperties2 = sharesImpl.getMethod("setProperties");
        setProperties2.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setProperties2.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization setMetadata2 = sharesImpl.getMethod("setMetadata");
        setMetadata2.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setMetadata2.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization getAccessPolicy = sharesImpl.getMethod("getAccessPolicy");
        getAccessPolicy.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getAccessPolicy.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization setAccessPolicy = sharesImpl.getMethod("setAccessPolicy");
        setAccessPolicy.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        setAccessPolicy.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization getStatistics = sharesImpl.getMethod("getStatistics");
        getStatistics.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        getStatistics.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        MethodCustomization restore = sharesImpl.getMethod("restore");
        restore.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)");
        restore.addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");

        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.file.share.implementation.models");
        implementationModels.getClass("FilesAndDirectoriesListSegment").addAnnotation("@JsonDeserialize(using = com.azure.storage.file.share.implementation.util.FilesAndDirectoriesListSegmentDeserializer.class)");

        PackageCustomization models = customization.getPackage("com.azure.storage.file.share.models");
        models.getClass("ShareFileRangeList").addAnnotation("@JsonDeserialize(using = ShareFileRangeListDeserializer.class)");

        // Replace JacksonXmlRootElement annotations that are causing a semantic breaking change.
        ClassCustomization shareFileHttpHeaders = models.getClass("ShareFileHttpHeaders");
        shareFileHttpHeaders.removeAnnotation("@JacksonXmlRootElement(localName = \"ShareFileHttpHeaders\")");
        shareFileHttpHeaders.addAnnotation("@JacksonXmlRootElement(localName = \"share-file-http-headers\")");

        ClassCustomization sourceModifiedAccessConditions = models.getClass("SourceModifiedAccessConditions");
        sourceModifiedAccessConditions.removeAnnotation("@JacksonXmlRootElement(localName = \"SourceModifiedAccessConditions\")");
        sourceModifiedAccessConditions.addAnnotation("@JacksonXmlRootElement(localName = \"source-modified-access-conditions\")");

        ClassCustomization shareServiceProperties = models.getClass("ShareServiceProperties");
        PropertyCustomization hourMetrics = shareServiceProperties.getProperty("hourMetrics");
        hourMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")");
        hourMetrics.addAnnotation("@JsonProperty(value = \"HourMetrics\")");
        PropertyCustomization minuteMetrics = shareServiceProperties.getProperty("minuteMetrics");
        minuteMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")");
        minuteMetrics.addAnnotation("@JsonProperty(value = \"MinuteMetrics\")");

    }
}
