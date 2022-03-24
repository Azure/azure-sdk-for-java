// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides support for basic file attributes.
 * <p>
 * The properties available on this type are a strict subset of {@link AzureBlobFileAttributes}, and the two types have
 * the same network behavior. Therefore, while this type is offered for compliance with the NIO spec,
 * {@link AzureBlobFileAttributes} is generally preferred.
 * <p>
 * Some attributes are not supported. Refer to the javadocs on each method for more information.
 * <p>
 * If the target file is a virtual directory, most attributes will be set to null.
 */
public final class AzureBasicFileAttributes implements BasicFileAttributes {
    // For verifying parameters on FileSystemProvider.readAttributes
    static final Set<String> ATTRIBUTE_STRINGS;
    static {
        Set<String> set = new HashSet<>();
        set.add("lastModifiedTime");
        set.add("isRegularFile");
        set.add("isDirectory");
        set.add("isVirtualDirectory");
        set.add("isSymbolicLink");
        set.add("isOther");
        set.add("size");
        set.add("creationTime");
        ATTRIBUTE_STRINGS = Collections.unmodifiableSet(set);
    }

    private final AzureBlobFileAttributes internalAttributes;

    /*
    In order to support Files.exist() and other methods like Files.walkFileTree() which depend on it, we have had to add
    support for virtual directories. This is not ideal as customers will have to now perform null checks when inspecting
    attributes (or at least check if it is a virtual directory before inspecting properties). It also incurs extra
    network requests as we have to call a checkDirectoryExists() after receiving the initial 404. This is two
    additional network requests, though they only happen in the case when a file doesn't exist or is virtual, so it
    shouldn't happen in the majority of api calls.
     */
    AzureBasicFileAttributes(Path path) throws IOException {
        this.internalAttributes = new AzureBlobFileAttributes(path);
    }

    /**
     * Returns the time of last modification or null if this is a virtual directory.
     *
     * @return the time of last modification or null if this is a virtual directory
     */
    @Override
    public FileTime lastModifiedTime() {
        return this.internalAttributes.lastModifiedTime();
    }

    /**
     * Returns the time of last modification or null if this is a virtual directory
     * <p>
     * Last access time is not supported by the blob service. In this case, it is typical for implementations to return
     * the {@link #lastModifiedTime()}.
     *
     * @return the time of last modification or null if this is a virtual directory
     */
    @Override
    public FileTime lastAccessTime() {
        return this.internalAttributes.lastAccessTime();
    }

    /**
     * Returns the creation time. The creation time is the time that the file was created. Returns null if this is a
     * virtual directory.
     *
     * @return The creation time or null if this is a virtual directory
     */
    @Override
    public FileTime creationTime() {
        return this.internalAttributes.creationTime();
    }

    /**
     * Tells whether the file is a regular file with opaque content.
     *
     * @return whether the file is a regular file.
     */
    @Override
    public boolean isRegularFile() {
        return this.internalAttributes.isRegularFile();
    }

    /**
     * Tells whether the file is a directory.
     * <p>
     * Will only return true if the directory is a concrete directory. See
     * {@link AzureFileSystemProvider#createDirectory(Path, FileAttribute[])} for more information on virtual and
     * concrete directories.
     *
     * @return whether the file is a directory
     */
    @Override
    public boolean isDirectory() {
        return this.internalAttributes.isDirectory();
    }

    /**
     * Tells whether the file is a virtual directory.
     * <p>
     * See {@link AzureFileSystemProvider#createDirectory(Path, FileAttribute[])} for more information on virtual and
     * concrete directories.
     *
     * @return whether the file is a virtual directory
     */
    public boolean isVirtualDirectory() {
        return this.internalAttributes.isVirtualDirectory();
    }

    /**
     * Tells whether the file is a symbolic link.
     *
     * @return false. Symbolic links are not supported.
     */
    @Override
    public boolean isSymbolicLink() {
        return this.internalAttributes.isSymbolicLink();
    }

    /**
     * Tells whether the file is something other than a regular file, directory, or symbolic link.
     *
     * @return false. No other object types are supported.
     */
    @Override
    public boolean isOther() {
        return this.internalAttributes.isOther();
    }

    /**
     * Returns the size of the file (in bytes).
     *
     * @return the size of the file
     */
    @Override
    public long size() {
        return this.internalAttributes.size();
    }

    /**
     * Returns the url of the resource.
     *
     * @return The file key, which is the url.
     */
    @Override
    public Object fileKey() {
        return this.internalAttributes.fileKey();
    }
}
