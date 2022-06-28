package com.azure.storage.datamover.filesystem;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;

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

class FileResource extends StorageResource {

    private final Path path;
    private final List<String> abstractPath;

    FileResource(Path path, List<String> abstractPath) {
        if (!path.toFile().isFile()) {
            throw new IllegalArgumentException("provided path isn't file");
        }
        this.path = path;
        this.abstractPath = new ArrayList<>(abstractPath);
    }

    @Override
    protected TransferCapabilities getIncomingTransferCapabilities() {
        return new TransferCapabilitiesBuilder()
            .canStream(true)
            .build();
    }

    @Override
    protected TransferCapabilities getOutgoingTransferCapabilities() {
        return new TransferCapabilitiesBuilder()
            .canStream(true)
            .build();
    }

    @Override
    protected InputStream openInputStream() {
        try {
            return new FileInputStream(path.toFile());
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected long getLength() {
        return path.toFile().length();
    }

    @Override
    protected void consumeInputStream(InputStream inputStream, long length) {
        try (OutputStream fos = new FileOutputStream(path.toFile())) {
            transfer(inputStream, fos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
    protected String getSasUri() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void consumeSasUri(String sasUri) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<String> getPath() {
        return abstractPath;
    }
}
