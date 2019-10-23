package com.azure.storage.blob.v8.perfstress;

import java.io.IOException;
import java.io.InputStream;

import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.v8.perfstress.core.RandomBlobTest;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobOutputStream;

import reactor.core.publisher.Mono;

public class UploadOutputStreamTest extends RandomBlobTest<SizeOptions> {

    public UploadOutputStreamTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void Run() {
        try {
            InputStream inputStream = RandomStream.create(Options.Size);
            BlobOutputStream outputStream = _cloudBlockBlob.openOutputStream();
            inputStream.transferTo(outputStream);
            outputStream.close();
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        throw new UnsupportedOperationException();
    }
}