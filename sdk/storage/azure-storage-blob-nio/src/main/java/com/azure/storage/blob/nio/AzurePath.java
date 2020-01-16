// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * {@inheritDoc}
 */
public final class AzurePath implements Path {
    private static final String ROOT_DIR_SUFFIX = ":";

    private final AzureFileSystem parentFileSystem;
    private final String pathString;

    AzurePath(AzureFileSystem parentFileSystem, String s, String... strings) {
        this.parentFileSystem = parentFileSystem;
        this.pathString = String.join(this.parentFileSystem.getSeparator(),
                Flux.just(s).concatWith(Flux.just(strings))
                    // Strip any trailing or leading delimiters so there are no duplicates when we join.
                    .map(str-> {
                        while (str.startsWith(this.parentFileSystem.getSeparator())){
                            str = str.substring(1);
                        }
                        while (str.endsWith(this.parentFileSystem.getSeparator())) {
                            str = str.substring(0, str.length()-1);
                        }
                        return str;
                    }).toIterable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem getFileSystem() {
        return this.parentFileSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbsolute() {
        return this.getRoot() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getRoot() {
        /*
        Check if the first element of the path is formatted like a root directory and if the name of the directory
        matches one of the file stores. (No directory should be formatted like a root dir and not match a file store
        name, but we validate just in case.)
         */
        String firstElement = pathString.split(parentFileSystem.getSeparator())[0];
        if (firstElement.endsWith(ROOT_DIR_SUFFIX)) {
            String fileStoreName = firstElement.substring(0, firstElement.length()-1);
            Boolean validRootName = Flux.fromIterable(parentFileSystem.getFileStores())
                .map(FileStore::name)
                .hasElement(fileStoreName)
                .block();
            if (validRootName != null && validRootName) {
                return this.parentFileSystem.getPath(firstElement + this.parentFileSystem.getSeparator());
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getFileName() {
        if (this.withoutRoot().isEmpty()) {
            return null;
        } else {
          return this.parentFileSystem.getPath(
              Flux.fromArray(this.pathString.split(this.parentFileSystem.getSeparator())).last().block());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getParent() {
        /*
        If this path only has one element, there is no parent. Note the root is included in the parent, so we don't
        use getNameCount here.
         */
        if (this.pathString.split(this.parentFileSystem.getSeparator()).length == 1) {
            return null;
        }

        /*
        This method may seem a bit circuitous, but using the javadocs define the behavior of this method in terms of
        the subpath method, so this is the best way to guarantee correctness.
         */
        Path parentNoRoot = this.subpath(0, getNameCount()-1);
        Path root = getRoot();
        if (root != null) {
            return this.parentFileSystem.getPath(root.toString() +  parentNoRoot.toString());
        }
        return parentNoRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameCount() {
        return this.withoutRoot().split(this.parentFileSystem.getSeparator()).length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getName(int i) {
        if (i < 0 || i >= this.getNameCount()) {
            throw new IllegalArgumentException();
        }
        return this.parentFileSystem.getPath(this.withoutRoot().split(this.parentFileSystem.getSeparator())[i]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path subpath(int i, int i1) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(Path path) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(String s) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(Path path) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(String s) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path normalize() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolve(Path path) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolve(String s) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveSibling(Path path) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveSibling(String s) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path relativize(Path path) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI toUri() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path toAbsolutePath() {
        return null;
    }

    /**
     * Unsupported.
     *
     * {@inheritDoc}
     */
    @Override
    public Path toRealPath(LinkOption... linkOptions) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File toFile() {
        return null;
    }

    /**
     * Unsupported.
     *
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     *
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>... kinds) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Path> iterator() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Path path) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.pathString;
    }

    private String withoutRoot() {
        Path root = this.getRoot();
        if (root != null) {
            return this.pathString.substring(root.toString().length());
        }

        return this.pathString;
    }
}
