package com.azure.storage.blob.perfstress;

import java.io.IOException;
import java.io.InputStream;

import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.perfstress.core.RandomBlobTest;
import com.azure.storage.blob.specialized.BlobOutputStream;

import reactor.core.publisher.Mono;

public class UploadOutputStreamTest extends RandomBlobTest<SizeOptions> {

    public UploadOutputStreamTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void Run() {
        try {
            InputStream inputStream = RandomStream.create(Options.Size);
            BlobOutputStream blobOutputStream = _blockBlobClient.getBlobOutputStream();
            inputStream.transferTo(blobOutputStream);
            blobOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        throw new UnsupportedOperationException();
    }
}