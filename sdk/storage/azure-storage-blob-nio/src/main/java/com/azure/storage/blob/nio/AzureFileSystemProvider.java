// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@inheritDoc}
 */
public class AzureFileSystemProvider extends FileSystemProvider {
    // TODO: Add logger
    private static final String ACCOUNT_QUERY_KEY = "account";

    private ConcurrentMap<String, FileSystem> openFileSystems;

    // Specs require a public zero argument constructor.

    /**
     * Creates an AzureFileSystemProvider.
     */
    public AzureFileSystemProvider() {
        this.openFileSystems = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getScheme() {
        return "azb";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> config) throws IOException {
        String accountName = extractAccountName(uri);

        if (this.openFileSystems.containsKey(accountName)) {
            throw new FileSystemAlreadyExistsException();
        }

        AzureFileSystem afs = new AzureFileSystem(this, accountName, config);
        this.openFileSystems.put(accountName, afs);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem getFileSystem(URI uri) {
        String accountName = extractAccountName(uri);
        if (!this.openFileSystems.containsKey(accountName)) {
            throw new FileSystemNotFoundException();
        }
        return this.openFileSystems.get(accountName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getPath(URI uri) {
        return getFileSystem(uri).getPath(uri.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set,
            FileAttribute<?>... fileAttributes) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDirectory(Path path, FileAttribute<?>... fileAttributes) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Path path) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void copy(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(Path path, Path path1, CopyOption... copyOptions) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSameFile(Path path, Path path1) throws IOException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkAccess(Path path, AccessMode... accessModes) throws IOException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> aClass, LinkOption... linkOptions) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> aClass, LinkOption... linkOptions) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String s, LinkOption... linkOptions) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(Path path, String s, Object o, LinkOption... linkOptions) throws IOException {

    }

    void closeFileSystem(String fileSystemName) {
        this.openFileSystems.remove(fileSystemName);
    }

    private String extractAccountName(URI uri) {
        if (!uri.getScheme().equals(this.getScheme())) {
            throw new IllegalArgumentException("URI scheme does not match this provider");
        }
        if (CoreUtils.isNullOrEmpty(uri.getQuery())) {
            throw new IllegalArgumentException("URI does not contain a query component. FileSystems require a URI of " +
                    "the format \"azb://?account=<account_name>\".");
        }

        String accountName = Flux.fromArray(uri.getQuery().split("&"))
                .filter(s -> s.startsWith(ACCOUNT_QUERY_KEY + "="))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "URI does not contain an \"" + ACCOUNT_QUERY_KEY + "=\" parameter. FileSystems require a URI " +
                                "of the format \"azb://?account=<account_name>\"")))
                .map(s -> s.substring(ACCOUNT_QUERY_KEY.length() + 1))
                .blockLast();

        if (CoreUtils.isNullOrEmpty(accountName)) {
            throw new IllegalArgumentException("No account name provided in URI query.");
        }

        return accountName;
    }
}
