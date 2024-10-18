// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.stress;

import com.azure.core.util.Context;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.options.ShareFileUploadRangeFromUrlOptions;
import com.azure.storage.file.share.sas.ShareSasPermission;
import com.azure.storage.file.share.sas.ShareServiceSasSignatureValues;
import com.azure.storage.file.share.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcOutputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;

public class UploadRangeFromUrl extends ShareScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final ShareFileClient destinationFileClient;
    private final ShareFileAsyncClient destinationFileAsyncClient;
    private  final ShareFileClient destinationFileNoFaultClient;
    private final ShareFileAsyncClient destinationFileAsyncNoFaultClient;
    private final ShareFileAsyncClient sourceFileClient;

    public UploadRangeFromUrl(StorageStressOptions options) {
        super(options);
        String sourceFileName = generateFileName();
        String destinationFileName = generateFileName();

        this.destinationFileClient = getSyncShareClient().getFileClient(destinationFileName);
        this.destinationFileAsyncClient = getAsyncShareClient().getFileClient(destinationFileName);
        this.destinationFileNoFaultClient = getSyncShareClientNoFault().getFileClient(destinationFileName);
        this.destinationFileAsyncNoFaultClient = getAsyncShareClientNoFault().getFileClient(destinationFileName);
        this.sourceFileClient = getAsyncShareClientNoFault().getFileClient(sourceFileName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        String sas = sourceFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new ShareSasPermission().setReadPermission(true)));

        destinationFileClient.uploadRangeFromUrlWithResponse(new ShareFileUploadRangeFromUrlOptions(options.getSize(),
            sourceFileClient.getFileUrl() + "?" + sas), null, span);
        // Download the file contents and compare it with the original content
        try (CrcOutputStream outputStream = new CrcOutputStream()) {
            destinationFileNoFaultClient.downloadWithResponse(outputStream, null, null, span);
            outputStream.close();
            originalContent.checkMatch(outputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        String sas = sourceFileClient.generateSas(new ShareServiceSasSignatureValues(OffsetDateTime.now().plusDays(1),
            new ShareSasPermission().setReadPermission(true)));

        return destinationFileAsyncClient.uploadRangeFromUrlWithResponse(
                new ShareFileUploadRangeFromUrlOptions(options.getSize(), sourceFileClient.getFileUrl() + "?" + sas))
            .then(destinationFileAsyncNoFaultClient.downloadWithResponse(null, null))
            .flatMap(response -> originalContent.checkMatch(response.getValue(), span));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(destinationFileAsyncNoFaultClient.create(options.getSize()))
            .then(originalContent.setupFile(sourceFileClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return destinationFileAsyncNoFaultClient.deleteIfExists()
            .then(sourceFileClient.deleteIfExists())
            .then(super.cleanupAsync());
    }
}
