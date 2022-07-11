package com.azure.storage.common.resource.filesystem;

import com.azure.storage.common.resource.StorageResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LocalFileStorageResource implements StorageResource {

    private final Path path;
    private final List<String> abstractPath;

    LocalFileStorageResource(Path path, List<String> abstractPath) {
        if (!path.toFile().isFile()) {
            throw new IllegalArgumentException("provided path isn't file");
        }
        this.path = path;
        this.abstractPath = new ArrayList<>(abstractPath);
    }

    @Override
    public InputStream openInputStream() {
        try {
            return new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public long getLength() {
        return path.toFile().length();
    }

    @Override
    public void consumeInputStream(InputStream inputStream, long length) {
        try (OutputStream fos = new FileOutputStream(path.toFile())) {
            transfer(inputStream, fos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void consumeUri(String sasUri) {
        throw new UnsupportedOperationException();
    }

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    // No Java 9+ on agent building apiview :-(
    private long transfer(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in, "in");
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    @Override
    public List<String> getPath() {
        return abstractPath;
    }

    @Override
    public boolean canConsumeStream() {
        return true;
    }

    @Override
    public boolean canProduceStream() {
        return true;
    }

    @Override
    public boolean canConsumeUri() {
        return false;
    }

    @Override
    public boolean canProduceUri() {
        return false;
    }
}
