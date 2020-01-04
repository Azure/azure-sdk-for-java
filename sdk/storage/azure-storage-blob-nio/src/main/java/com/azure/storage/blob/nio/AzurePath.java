// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

/**
 * {@inheritDoc}
 */
public class AzurePath implements Path {
    private final AzureFileSystem parentFileSystem;
    private final String pathString;

    AzurePath(AzureFileSystem parentFileSystem, String s, String... strings) {
        this.parentFileSystem = parentFileSystem;
        this.pathString = String.join(this.parentFileSystem.getSeparator(),
                Flux.just(s).concatWith(Flux.just(strings)).toIterable());
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
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getRoot() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getFileName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getParent() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameCount() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getName(int i) {
        return null;
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
     * {@inheritDoc}
     */
    @Override
    public Path toRealPath(LinkOption... linkOptions) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File toFile() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
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
}
