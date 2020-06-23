package com.azure.storage.blob.nio;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class AzureBlobFileAttributeView implements BasicFileAttributeView {

    private final Path path;

    AzureBlobFileAttributeView(Path path) {
        this.path = path;
    }

    @Override
    public String name() {
        return "azureBlob";
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException {
        return new AzureBlobFileAttributes(path);
    }

    @Override
    public void setTimes(FileTime fileTime, FileTime fileTime1, FileTime fileTime2) throws IOException {
        throw new UnsupportedOperationException();
    }
}
