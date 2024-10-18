package com.azure.storage.file.datalake.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

import static com.azure.core.util.FluxUtil.monoError;

public class OpenInputStream extends DataLakeScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(OpenInputStream.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final DataLakeFileClient syncClient;
    private final DataLakeFileAsyncClient asyncNoFaultClient;

    public OpenInputStream(StorageStressOptions options) {
        super(options);
        String fileName = generateFileName();
        this.syncClient = getSyncFileSystemClient().getFileClient(fileName);
        this.asyncNoFaultClient = getAsyncFileSystemClientNoFault().getFileAsyncClient(fileName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        try (InputStream stream = syncClient.openInputStream().getInputStream()) {
            try (CrcInputStream crcStream = new CrcInputStream(stream)) {
                byte[] buffer = new byte[8192];
                while (crcStream.read(buffer) != -1) {
                    // do nothing
                }
                originalContent.checkMatch(crcStream.getContentInfo(), span).block();
            }
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
            return monoError(LOGGER, new RuntimeException("openInputStream() does not exist on the async client"));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupFile(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists().then(super.cleanupAsync());
    }

}
