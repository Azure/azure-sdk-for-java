// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.resource.filesystem;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.resource.StorageResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LocalFileStorageResource implements StorageResource {

    private static final ClientLogger LOGGER = new ClientLogger(LocalFileStorageResource.class);

    private final Path path;
    private final List<String> abstractPath;

    LocalFileStorageResource(Path path, List<String> abstractPath) {
        if (path.toFile().exists() && !path.toFile().isFile()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("provided path isn't file"));
        }
        this.path = path;
        this.abstractPath = new ArrayList<>(abstractPath);
    }

    @Override
    public ReadableByteChannel openReadableByteChannel() {
        try {
            return new FileInputStream(path.toFile()).getChannel();
        } catch (FileNotFoundException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public long getLength() {
        return path.toFile().length();
    }

    @Override
    public void consumeReadableByteChannel(ReadableByteChannel channel, long length) {
        Path parent = path.getParent();
        if (parent != null) {
            if (!parent.toFile().exists()) {
                boolean created = parent.toFile().mkdirs();
                if (!created) {
                    throw LOGGER.logExceptionAsError(new UncheckedIOException(new IOException("Unable to create dir")));
                }
            }
        }
        try (OutputStream fos = new FileOutputStream(path.toFile())) {
            transfer(Channels.newInputStream(channel), fos);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public String getUrl() {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException());
    }

    @Override
    public void consumeUrl(String sasUri) {
        throw LOGGER.logExceptionAsError(new UnsupportedOperationException());
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
    public boolean canConsumeReadableByteChannel() {
        return true;
    }

    @Override
    public boolean canProduceReadableByteChannel() {
        return true;
    }

    @Override
    public boolean canConsumeUrl() {
        return false;
    }

    @Override
    public boolean canProduceUrl() {
        return false;
    }
}
