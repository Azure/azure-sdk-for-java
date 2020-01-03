// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

public class AzureFileSystemProvider extends FileSystemProvider {

    public String getScheme() {
        return null;
    }

    public FileSystem newFileSystem(URI uri, Map<String, ?> config) throws IOException {
        return null;
    }

    public FileSystem getFileSystem(URI uri) {
        return null;
    }

    public Path getPath(URI uri) {
        return getFileSystem(uri).getPath(uri.getPath());
    }

    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set,
            FileAttribute<?>... fileAttributes) throws IOException {
        return null;
    }

    public DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return null;
    }

    public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {

    }

    public void delete(Path path) throws IOException {

    }

    public void copy(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    public void move(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    public boolean isSameFile(Path path, Path path1) throws IOException {
        return false;
    }

    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    public FileStore getFileStore(Path path) throws IOException {
        return null;
    }

    public void checkAccess(Path path, AccessMode... accessModes) throws IOException {

    }

    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> aClass, LinkOption... linkOptions) {
        return null;
    }

    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> aClass, LinkOption... linkOptions) throws IOException {
        return null;
    }

    public Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        return null;
    }

    public void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {

    }
}
