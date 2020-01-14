// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.core.http.policy.HttpLogDetailLevel
import com.azure.core.http.policy.HttpLogOptions
import com.azure.core.util.polling.SyncPoller
import com.azure.storage.common.implementation.Constants
import com.azure.storage.file.share.models.NtfsFileAttributes
import com.azure.storage.file.share.models.PermissionCopyModeType
import com.azure.storage.file.share.models.ShareFileCopyInfo
import com.azure.storage.file.share.specialized.ShareLeaseClientBuilder
import spock.lang.Unroll


class ShareServiceVersionPolicyTest extends APISpec {

    ShareFileClient fc
    ShareClient sc
    static FileSmbProperties smbProperties
    static String filePermission = "O:S-1-5-21-2127521184-1604012920-1887927527-21560751G:S-1-5-21-2127521184-1604012920-1887927527-513D:AI(A;;FA;;;SY)(A;;FA;;;BA)(A;;0x1200a9;;;S-1-5-21-397955417-626881126-188441444-3053964)S:NO_ACCESS_CONTROL"


    def setup() {
        sc = getShareClientBuilder(primaryFileServiceClient.getFileServiceUrl())
            .shareName(generateShareName())
            .serviceVersion(ShareServiceVersion.V2019_02_02)
            .httpClient(getHttpClient())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(primaryCredential)
            .buildClient()

        sc.create()

        fc = sc.getFileClient(generatePathName())
        fc.create(Constants.KB)

        smbProperties = new FileSmbProperties().setNtfsFileAttributes(EnumSet.<NtfsFileAttributes>of(NtfsFileAttributes.NORMAL))
    }

    def "File lease"() {
        setup:
        def lc = new ShareLeaseClientBuilder()
            .fileClient(fc)
            .leaseId("id")
            .buildClient()

        when:
        lc.acquireLease()

        then:
        def e = thrown(IllegalStateException)
        e.getMessage() == "x-ms-lease-duration is not supported for any file API in service version 2019-02-02"
    }

    @Unroll
    def "Start copy with args"() {
        given:
        def sourceURL = fc.getFileUrl()
        def filePermissionKey = sc.createPermission(filePermission)
        // We recreate file properties for each test since we need to store the times for the test with getUTCNow()
        smbProperties.setFileCreationTime(getUTCNow())
            .setFileLastWriteTime(getUTCNow())
        if (setFilePermissionKey) {
            smbProperties.setFilePermissionKey(filePermissionKey)
        }

        when:
        SyncPoller<ShareFileCopyInfo, Void> poller = fc.beginCopy(sourceURL, smbProperties,
            setFilePermission ? filePermission : null, permissionType, ignoreReadOnly,
            setArchiveAttribute, null, null, null)

        poller.poll()

        then:
        def e = thrown(IllegalStateException)
        e.getMessage() == exceptionKeyWord + " is not supported for copy file in service version 2019-02-02"

        where:
        setFilePermissionKey | setFilePermission | ignoreReadOnly | setArchiveAttribute | permissionType                    || exceptionKeyWord
        true                 | false             | null           | null                | PermissionCopyModeType.OVERRIDE   || "x-ms-file-permission-key"
        false                | true              | null           | null                | PermissionCopyModeType.OVERRIDE   || "x-ms-file-permission"
        false                | false             | null           | null                | PermissionCopyModeType.SOURCE     || "x-ms-file-permission-copy-mode"
        false                | false             | true           | null                | null                              || "x-ms-file-copy-ignore-read-only"
        false                | false             | null           | true                | null                              || "x-ms-file-copy-set-archive"
    }

}
