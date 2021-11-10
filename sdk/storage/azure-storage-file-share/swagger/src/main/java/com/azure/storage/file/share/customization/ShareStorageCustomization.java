// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.customization;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.MethodCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import org.slf4j.Logger;

/**
 * Customization class for File Share Storage.
 */
public class ShareStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization implementation = customization.getPackage("com.azure.storage.file.share.implementation");

        ClassCustomization directoriesImpl = implementation.getClass("DirectoriesImpl");
        modifyUnexpectedResponseExceptionType(directoriesImpl.getMethod("create"));
        modifyUnexpectedResponseExceptionType(directoriesImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(directoriesImpl.getMethod("delete"));
        modifyUnexpectedResponseExceptionType(directoriesImpl.getMethod("setProperties"));
        modifyUnexpectedResponseExceptionType(directoriesImpl.getMethod("setMetadata"));
        modifyUnexpectedResponseExceptionType(directoriesImpl.getMethod("listFilesAndDirectoriesSegment"));
        modifyUnexpectedResponseExceptionType(directoriesImpl.getMethod("listHandles"));
        modifyUnexpectedResponseExceptionType(directoriesImpl.getMethod("forceCloseHandles"));

        ClassCustomization filesImpl = implementation.getClass("FilesImpl");
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("create"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("download"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("delete"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("setHttpHeaders"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("setMetadata"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("uploadRange"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("uploadRangeFromURL"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("getRangeList"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("startCopy"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("abortCopy"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("listHandles"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("forceCloseHandles"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("acquireLease"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("releaseLease"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("changeLease"));
        modifyUnexpectedResponseExceptionType(filesImpl.getMethod("breakLease"));

        ClassCustomization servicesImpl = implementation.getClass("ServicesImpl");
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("setProperties"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("listSharesSegment"));
        modifyUnexpectedResponseExceptionType(servicesImpl.getMethod("listSharesSegmentNext"));

        ClassCustomization sharesImpl = implementation.getClass("SharesImpl");
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("create"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("getProperties"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("delete"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("acquireLease"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("releaseLease"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("changeLease"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("renewLease"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("breakLease"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("createSnapshot"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("createPermission"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("getPermission"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("setProperties"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("setMetadata"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("getAccessPolicy"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("setAccessPolicy"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("getStatistics"));
        modifyUnexpectedResponseExceptionType(sharesImpl.getMethod("restore"));

        // Add these annotations since the default deserializer does not handle these cases correctly.
        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.file.share.implementation.models");
        implementationModels.getClass("FilesAndDirectoriesListSegment").addAnnotation("@JsonDeserialize(using = com.azure.storage.file.share.implementation.util.FilesAndDirectoriesListSegmentDeserializer.class)");
        implementationModels.getClass("CopyFileSmbInfo")
            .removeAnnotation("@JacksonXmlRootElement\\(localName = \"CopyFileSmbInfo\"\\)")
            .addAnnotation("@JacksonXmlRootElement(localName = \"copy-file-smb-info\")");

        PackageCustomization models = customization.getPackage("com.azure.storage.file.share.models");
        models.getClass("ShareFileRangeList").addAnnotation("@JsonDeserialize(using = ShareFileRangeListDeserializer.class)");

        // Replace JacksonXmlRootElement annotations that are causing a semantic breaking change.
        ClassCustomization shareFileHttpHeaders = models.getClass("ShareFileHttpHeaders");
        shareFileHttpHeaders.removeAnnotation("@JacksonXmlRootElement\\(localName = \"ShareFileHttpHeaders\"\\)")
            .addAnnotation("@JacksonXmlRootElement(localName = \"share-file-http-headers\")");

        ClassCustomization sourceModifiedAccessConditions = models.getClass("SourceModifiedAccessConditions");
        sourceModifiedAccessConditions.removeAnnotation("@JacksonXmlRootElement\\(localName = \"SourceModifiedAccessConditions\"\\)")
            .addAnnotation("@JacksonXmlRootElement(localName = \"source-modified-access-conditions\")");

        // Update incorrect JsonProperty of Metrics
        ClassCustomization shareServiceProperties = models.getClass("ShareServiceProperties");
        PropertyCustomization hourMetrics = shareServiceProperties.getProperty("hourMetrics");
        hourMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")")
            .addAnnotation("@JsonProperty(value = \"HourMetrics\")");
        PropertyCustomization minuteMetrics = shareServiceProperties.getProperty("minuteMetrics");
        minuteMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")")
            .addAnnotation("@JsonProperty(value = \"MinuteMetrics\")");

    }

    private void modifyUnexpectedResponseExceptionType(MethodCustomization method) {
        method.removeAnnotation("@UnexpectedResponseExceptionType(StorageErrorException.class)")
            .addAnnotation("@UnexpectedResponseExceptionType(com.azure.storage.file.share.models.ShareStorageException.class)");
    }
}
