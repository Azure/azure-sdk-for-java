// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.aot.graalvm.support.implementation.features;

import com.azure.aot.graalvm.support.implementation.GraalVMFeature;
import com.oracle.svm.core.annotate.AutomaticFeature;

import java.util.Set;

import static com.azure.aot.graalvm.support.implementation.GraalVMFeatureUtils.interfaces;
import static com.azure.aot.graalvm.support.implementation.GraalVMFeatureUtils.setsOf;

@AutomaticFeature
public class StorageFileShareFeature implements GraalVMFeature {

    @Override
    public String getRootPackage() {
        return "com.azure.storage.file.share";
    }

    @Override
    public Set<String[]> getDynamicProxies() {
        return setsOf(
            interfaces("com.azure.storage.file.share.implementation.DirectoriesImpl$DirectoriesService"),
            interfaces("com.azure.storage.file.share.implementation.FilesImpl$FilesService"),
            interfaces("com.azure.storage.file.share.implementation.ServicesImpl$ServicesService"),
            interfaces("com.azure.storage.file.share.implementation.SharesImpl$SharesService")
        );
    }

    /*@Override
    public Set<ClassReflectionAttributes> getReflectionClasses() {
        return setOf(
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesSetMetadataResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.DirectoriesCreateResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.DirectoriesDeleteResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.DirectoriesForceCloseHandlesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.DirectoriesGetPropertiesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.DirectoriesListFilesAndDirectoriesSegmentResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.DirectoriesListHandlesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.DirectoriesSetMetadataResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.DirectoriesSetPropertiesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesAbortCopyResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesAcquireLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesBreakLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesChangeLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesCreateResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesDeleteResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesDownloadResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesForceCloseHandlesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesGetPropertiesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesGetRangeListResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesListHandlesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesReleaseLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesSetHttpHeadersResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesStartCopyResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesUploadRangeFromURLResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.FilesUploadRangeResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.ServicesGetPropertiesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.ServicesListSharesSegmentNextResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.ServicesListSharesSegmentResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.ServicesSetPropertiesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesAcquireLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesBreakLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesChangeLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesCreatePermissionResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesCreateResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesCreateSnapshotResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesDeleteResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesGetAccessPolicyResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesGetPermissionResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesGetPropertiesResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesGetStatisticsResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesReleaseLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesRenewLeaseResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesRestoreResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesSetAccessPolicyResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesSetMetadataResponse"),
            createWithAllDeclared("com.azure.storage.file.share.implementation.models.SharesSetPropertiesResponse")
        );
    }*/
}
