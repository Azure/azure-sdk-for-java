// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.nio.implementation.util.Utility;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;

/**
 * The root component, if it is present, is the first element of the path and is denoted by a {@code ':'} as the last
 * character. Hence, only one instance of {@code ':'} may appear in a path string and it may only be the last character
 * of the first element in the path. The root component is used to identify which container a path belongs to.
 * <p>
 * Constructing a syntactically valid path does not ensure a resource exists at the given path. An error will
 * only be thrown for an invalid root when trying to access a file in that root if it does not exist and for an
 * nonexistent path when trying to access the resource at that location if it does not exist or is inaccessible.
 * <p>
 * Path names are case sensitive.
 * <p>
 * If a resource is accessed via a relative path, it will be resolved against the default directory of the file system.
 * The default directory is as defined in the {@link AzureFileSystem} docs.
 * <p>
 * Leading and trailing separators will be stripped. This has the effect of making "foo/" and "foo" equivalent paths.
 *
 * {@inheritDoc}
 */
public final class AzurePath implements Path {
    private final ClientLogger logger = new ClientLogger(AzurePath.class);
    private static final String ROOT_DIR_SUFFIX = ":";

    private final AzureFileSystem parentFileSystem;
    private final String pathString;

    AzurePath(AzureFileSystem parentFileSystem, String s, String... strings) {
        if (strings == null) {
            strings = new String[0]; // Just to make processing easier later. This wont affect the result.
        }
        this.parentFileSystem = parentFileSystem;
        Flux<String> elementFlux =
            // Strip any trailing, leading, or internal delimiters so there are no duplicates when we join.
            Flux.fromArray(s.split(this.parentFileSystem.getSeparator()))
                .concatWith(Flux.fromArray(strings)
                    .flatMap(str -> Flux.fromArray(str.split(this.parentFileSystem.getSeparator()))))
                .filter(str -> !str.isEmpty());

        this.pathString = String.join(this.parentFileSystem.getSeparator(), elementFlux.toIterable());

        // No element but the first may contain ":"
        elementFlux.skip(1)
            .flatMap(str -> str.contains(ROOT_DIR_SUFFIX)
                ? Mono.error(Utility.logError(logger, new InvalidPathException(this.pathString, ROOT_DIR_SUFFIX
                + " is an invalid character except to identify the root element of this path if there is one.")))
            : Mono.just(str)).blockLast();

        // There may only be at most one instance of ":" in the root component, and it must be the last character.
        elementFlux.take(1)
            .flatMap(str -> str.contains(ROOT_DIR_SUFFIX) && str.indexOf(ROOT_DIR_SUFFIX) < str.length() - 1
            ? Mono.error(Utility.logError(logger, new InvalidPathException(this.pathString, ROOT_DIR_SUFFIX + " may"
                + " only be used as the last character in the root component of a path")))
            : Mono.just(str)).blockLast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem getFileSystem() {
        return this.parentFileSystem;
    }

    /**
     * A path is considered absolute in this file system if it contains a root component.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean isAbsolute() {
        return this.getRoot() != null;
    }

    /**
     * The root component of this path also identifies the Azure Storage Container in which the file is stored. This
     * method will not validate that the root component corresponds to an actual file store/container in this
     * file system. It will simply return the root component of the path if one is present and syntactically valid.
     *
     * {@inheritDoc}
     */
    @Override
    public Path getRoot() {
        // Check if the first element of the path is formatted like a root directory.
        String firstElement = pathString.split(parentFileSystem.getSeparator())[0];
        if (firstElement.endsWith(ROOT_DIR_SUFFIX)) {
            return this.parentFileSystem.getPath(firstElement);
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
            return this.parentFileSystem.getPath(Flux.fromArray(this.splitToElements()).last().block());
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
        if (this.splitToElements().length == 1) {
            return null;
        }

        return this.parentFileSystem.getPath(
            this.pathString.substring(0, this.pathString.lastIndexOf(this.parentFileSystem.getSeparator())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameCount() {
        return this.splitToElements(this.withoutRoot()).length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getName(int i) {
        if (i < 0 || i >= this.getNameCount()) {
            throw new IllegalArgumentException();
        }
        return this.parentFileSystem.getPath(this.splitToElements(this.withoutRoot())[i]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path subpath(int begin, int end) {
        if (begin < 0 || begin >= this.getNameCount()
            || end <= begin || end > this.getNameCount()) {
            throw new IllegalArgumentException(String.format("Values of begin: %d and end: %d are invalid",
                begin, end));
        }

        Iterable<String> subnames = Flux.fromArray(this.splitToElements(this.withoutRoot()))
            .skip(begin)
            .take(end - begin)
            .toIterable();

        return this.parentFileSystem.getPath(String.join(this.parentFileSystem.getSeparator(), subnames));
    }

    /**
     * In this implementation, a root component starts with another root component if the two root components are
     * equivalent strings. In other words, if the files are stored in the same container.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(Path path) {
        /*
        There can only be one instance of a file system with a given id, so comparing object identity is equivalent
        to checking ids here.
         */
        if (path.getFileSystem() != this.parentFileSystem) {
            return false;
        }

        String[] thisPathElements = this.splitToElements();
        String[] otherPathElements = ((AzurePath) path).splitToElements();
        if (otherPathElements.length > thisPathElements.length) {
            return false;
        }
        for (int i = 0; i < otherPathElements.length; i++) {
            if (!otherPathElements[i].equals(thisPathElements[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(String s) {
        return this.startsWith(this.parentFileSystem.getPath(s));
    }

    /**
     * In this implementation, a root component ends with another root component if the two root components are
     * equivalent strings. In other words, if the files are stored in the same container.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(Path path) {
        /*
        There can only be one instance of a file system with a given id, so comparing object identity is equivalent
        to checking ids here.
         */
        if (path.getFileSystem() != this.parentFileSystem) {
            return false;
        }

        String[] thisPathElements = this.splitToElements();
        String[] otherPathElements = ((AzurePath) path).pathString.split(this.parentFileSystem.getSeparator());
        if (otherPathElements.length > thisPathElements.length) {
            return false;
        }
        // If the given path has a root component, the paths must be equal.
        if (path.getRoot() != null && otherPathElements.length != thisPathElements.length) {
            return false;
        }
        for (int i = 1; i <= otherPathElements.length; i++) {
            if (!otherPathElements[otherPathElements.length - i]
                .equals(thisPathElements[thisPathElements.length - i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(String s) {
        return this.endsWith(this.parentFileSystem.getPath(s));
    }

    /**
     * This file system follows the standard practice mentioned in the original docs.
     *
     * {@inheritDoc}
     */
    @Override
    public Path normalize() {
        Deque<String> stack = new ArrayDeque<>();
        String[] pathElements = this.splitToElements();
        Path root = this.getRoot(); // Refactor so this doesn't access the fs per docs
        String rootStr = root == null ? null : root.toString();
        for (String element : pathElements) {
            if (element.equals(".")) {
                continue;
            } else if (element.equals("..")) {
                if (rootStr != null) {
                    // Root path. We never push "..".
                    if (!stack.isEmpty() && stack.peekLast().equals(rootStr)) {
                        // Cannot go higher than root. Ignore.
                        continue;
                    } else {
                        stack.removeLast();
                    }
                } else {
                    // Relative paths can have an arbitrary number of ".." at the beginning.
                    if (stack.isEmpty()) {
                        stack.addLast(element);
                    } else if (stack.peek().equals("..")) {
                        stack.addLast(element);
                    } else {
                        stack.removeLast();
                    }
                }
            } else {
                stack.addLast(element);
            }
        }

        return this.parentFileSystem.getPath("", Arrays.copyOf(stack.toArray(), stack.size(), String[].class));
    }

    /**
     * If the other path has a root component, it is considered absolute, and it is returned.
     *
     * {@inheritDoc}
     */
    @Override
    public Path resolve(Path path) {
        if (path.isAbsolute()) {
            return path;
        }
        if (path.getNameCount() == 0) {
            return this;
        }
        return this.parentFileSystem.getPath(this.toString(), path.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolve(String s) {
        return this.resolve(this.parentFileSystem.getPath(s));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveSibling(Path path) {
        if (path.isAbsolute()) {
            return path;
        }

        Path parent = this.getParent();
        return parent == null ? path : parent.resolve(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveSibling(String s) {
        return this.resolveSibling(this.parentFileSystem.getPath(s));
    }

    /**
     * If both paths have a root component, it is still to relativize one against the other.
     *
     * {@inheritDoc}
     */
    @Override
    public Path relativize(Path path) {
        if (path.getRoot() == null ^ this.getRoot() == null) {
            throw Utility.logError(logger,
                new IllegalArgumentException("Both paths must be absolute or neither can be"));
        }

        AzurePath thisNormalized = (AzurePath) this.normalize();
        Path otherNormalized = path.normalize();

        Deque<String> deque = new ArrayDeque<>(
            Arrays.asList(otherNormalized.toString().split(this.parentFileSystem.getSeparator())));

        int i = 0;
        String[] thisElements = thisNormalized.splitToElements();
        while (i < thisElements.length && !deque.isEmpty() && thisElements[i].equals(deque.peekFirst())) {
            deque.removeFirst();
            i++;
        }
        while (i < thisElements.length) {
            deque.addFirst("..");
            i++;
        }

        return this.parentFileSystem.getPath("", Arrays.copyOf(deque.toArray(), deque.size(), String[].class));
    }

    /**
     * No authority component is defined for the {@code URI} returned by this method. This implementation offers the
     * same equivalence guarantee as the default provider.
     *
     * {@inheritDoc}
     */
    @Override
    public URI toUri() {
        try {
            return new URI(this.parentFileSystem.provider().getScheme(), null, "/" + this.toAbsolutePath().toString(),
                null, null);
        } catch (URISyntaxException e) {
            throw Utility.logError(logger, new IllegalStateException("Unable to create valid URI from path", e));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path toAbsolutePath() {
        if (this.isAbsolute()) {
            return this;
        }
        return this.parentFileSystem.getDefaultDirectory().resolve(this);
    }

    /**
     * Unsupported.
     * <p>
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
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>[] kinds, WatchEvent.Modifier... modifiers)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     * <p>
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
        return Flux.fromArray(this.splitToElements(this.withoutRoot()))
            .map(s -> this.parentFileSystem.getPath(s))
            .toIterable()
            .iterator();
    }

    /**
     * This result of this method is identical to a string comparison on the underlying path strings.
     *
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Path path) {
        if (!(path instanceof AzurePath)) {
            throw Utility.logError(logger, new ClassCastException("Other path is not an instance of AzurePath."));
        }

        return this.pathString.compareTo(((AzurePath) path).pathString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.pathString;
    }

    /**
     * A path is considered equal to another path if it is associated with the same file system instance and if the
     * path strings are equivalent.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AzurePath paths = (AzurePath) o;
        return Objects.equals(parentFileSystem, paths.parentFileSystem)
            && Objects.equals(pathString, paths.pathString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentFileSystem, pathString);
    }

    // Used to ensure we only try to access containers that are mounted.
    boolean validRoot(String fileStoreName) {
        Boolean validRootName = Flux.fromIterable(parentFileSystem.getFileStores())
            .map(FileStore::name)
            .hasElement(fileStoreName)
            .block();
        return validRootName != null && validRootName;
    }

    private String withoutRoot() {
        Path root = this.getRoot();
        String str = this.pathString;
        if (root != null) {
            str = this.pathString.substring(root.toString().length());
        }
        if (str.startsWith(this.parentFileSystem.getSeparator())) {
            str = str.substring(1);
        }

        return str;
    }

    private String[] splitToElements() {
        return this.splitToElements(this.pathString);
    }

    private String[] splitToElements(String str) {
        String[] arr = str.split(this.parentFileSystem.getSeparator());
        if (arr.length == 1 && arr[0].isEmpty()) {
            return new String[0];
        }
        return arr;
    }
}
