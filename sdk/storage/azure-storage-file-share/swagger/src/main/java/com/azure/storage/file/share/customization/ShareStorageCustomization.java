// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.customization;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

/**
 * Customization class for File Share Storage.
 */
public class ShareStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization) {
        PackageCustomization implementation = customization.getPackage("com.azure.storage.file.share.implementation");

        ClassCustomization directoriesImpl = implementation.getClass("DirectoriesImpl");
        directoriesImpl.getMethod("create").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        directoriesImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        directoriesImpl.getMethod("delete").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        directoriesImpl.getMethod("setProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        directoriesImpl.getMethod("setMetadata").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        directoriesImpl.getMethod("listFilesAndDirectoriesSegment").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        directoriesImpl.getMethod("listHandles").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        directoriesImpl.getMethod("forceCloseHandles").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");

        ClassCustomization filesImpl = implementation.getClass("FilesImpl");
        filesImpl.getMethod("create").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("download").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("delete").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("setHttpHeaders").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("setMetadata").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("uploadRange").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("uploadRangeFromURL").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("getRangeList").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("startCopy").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("abortCopy").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("listHandles").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("forceCloseHandles").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("acquireLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("releaseLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("changeLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        filesImpl.getMethod("breakLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");

        ClassCustomization servicesImpl = implementation.getClass("ServicesImpl");
        servicesImpl.getMethod("setProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        servicesImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        servicesImpl.getMethod("listSharesSegment").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        servicesImpl.getMethod("listSharesSegmentNext").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");

        ClassCustomization sharesImpl = implementation.getClass("SharesImpl");
        sharesImpl.getMethod("create").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("getProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("delete").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("acquireLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("releaseLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("changeLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("renewLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("breakLease").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("createSnapshot").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("createPermission").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("getPermission").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("setProperties").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("setMetadata").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("getAccessPolicy").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("setAccessPolicy").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("getStatistics").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
        sharesImpl.getMethod("restore").addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");

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

    }
}
