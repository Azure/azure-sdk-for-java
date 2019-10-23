package com.azure.storage.blob.perfstress;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.perfstress.core.RandomBlobTest;

import reactor.core.publisher.Mono;

public class UploadFromFileTest extends RandomBlobTest<SizeOptions> {

    private static Path tempFile;

    public UploadFromFileTest(SizeOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalSetupAsync().then(CreateTempFile());
    }

    @Override
    public Mono<Void> GlobalCleanupAsync() {
        return DeleteTempFile().then(super.GlobalCleanupAsync());
    }

    private Mono<Void> CreateTempFile() {
        try {
            tempFile = Files.createTempFile(null, null);
            
            InputStream inputStream = RandomStream.create(Options.Size);
            OutputStream outputStream = new FileOutputStream(tempFile.toString());
            inputStream.transferTo(outputStream);
            outputStream.close();
            
            return Mono.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<Void> DeleteTempFile() {
        try {
            Files.delete(tempFile);
            return Mono.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void Run() {
        try {
            _blobClient.uploadFromFile(tempFile.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        return _blobAsyncClient.uploadFromFile(tempFile.toString());
    }
}