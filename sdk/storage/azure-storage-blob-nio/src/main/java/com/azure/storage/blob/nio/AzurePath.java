// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.nio.implementation.util.Utility;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The root component, if it is present, is the first element of the path and is denoted by a {@code ':'} as the last
 * character. Hence, only one instance of {@code ':'} may appear in a path string and it may only be the last character
 * of the first element in the path. The root component is used to identify which container a path belongs to.
 * <p>
 * Constructing a syntactically valid path does not ensure a resource exists at the given path. An error will
 * not be thrown until trying to access an invalid resource, e.g. trying to access a resource that does not exist.
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
    static final String ROOT_DIR_SUFFIX = ":";

    private final AzureFileSystem parentFileSystem;
    private final String pathString;

    AzurePath(AzureFileSystem parentFileSystem, String first, String... more) {
        this.parentFileSystem = parentFileSystem;

        /*
        Break all strings into their respective elements and remove empty elements. This has the effect of stripping
        any trailing, leading, or internal delimiters so there are no duplicates/empty elements when we join.
         */
        List<String> elements = new ArrayList<>(Arrays.asList(first.split(parentFileSystem.getSeparator())));
        if (more != null) {
            for (String next : more) {
                elements.addAll(Arrays.asList(next.split(parentFileSystem.getSeparator())));
            }
        }
        elements.removeIf(String::isEmpty);

        this.pathString = String.join(this.parentFileSystem.getSeparator(), elements);

        // Validate the path string by checking usage of the reserved character ROOT_DIR_SUFFIX.
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);
            /*
            If there is a root component, it must be the first element. A root component takes the format of
            "<fileStoreName>:". The ':', or ROOT_DIR_SUFFIX, if present, can only appear once, and can only be the last
            character of the first element.
             */
            if (i == 0) {
                if (element.contains(ROOT_DIR_SUFFIX) && element.indexOf(ROOT_DIR_SUFFIX) < element.length() - 1) {
                    throw Utility.logError(logger, new InvalidPathException(this.pathString, ROOT_DIR_SUFFIX + " may"
                        + " only be used as the last character in the root component of a path"));
                }
            // No element besides the first may contain the ROOT_DIR_SUFFIX, as only the first element may be the root.
            } else if (element.contains(ROOT_DIR_SUFFIX)) {
                throw Utility.logError(logger, new InvalidPathException(this.pathString, ROOT_DIR_SUFFIX + " is an "
                    + "invalid character except to identify the root element of this path if there is one."));
            }
        }
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
        String firstElement = this.splitToElements()[0];
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
            List<String> elements = Arrays.asList(this.splitToElements());
            return this.parentFileSystem.getPath(elements.get(elements.size() - 1));
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
            throw Utility.logError(logger, new IllegalArgumentException(String.format("Index %d is out of bounds", i)));
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
            throw Utility.logError(logger,
                new IllegalArgumentException(String.format("Values of begin: %d and end: %d are invalid", begin, end)));
        }

        String[] subnames = Stream.of(this.splitToElements(this.withoutRoot()))
            .skip(begin)
            .limit(end - begin)
            .toArray(String[]::new);

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
        if (!path.getFileSystem().equals(this.parentFileSystem)) {
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
        Path root = this.getRoot();
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

        return this.parentFileSystem.getPath("", stack.toArray(new String[0]));
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

        return this.parentFileSystem.getPath("", deque.toArray(new String[0]));
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
        throw new UnsupportedOperationException("Symbolic links are not supported.");
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
        throw new UnsupportedOperationException("WatchEvents are not supported.");
    }

    /**
     * Unsupported.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watchService, WatchEvent.Kind<?>... kinds) throws IOException {
        throw new UnsupportedOperationException("WatchEvents are not supported.");
    }

    /**
     * Unsupported
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Iterator<Path> iterator() {
        return Arrays.asList(Stream.of(this.splitToElements(this.withoutRoot()))
            .map(s -> this.parentFileSystem.getPath(s))
            .toArray(Path[]::new))
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

    /*
    We don't store the blob client because unlike other types in this package, a Path does not actually indicate the
    existence or even validity of any remote resource. It is purely a representation of a path. Therefore, we do not
    construct the client or perform any validation until it is requested.
     */
    BlobClient toBlobClient() throws IOException {
        // Converting to an absolute path ensures there is a container to operate on even if it is the default.
        // Normalizing ensures the path is clean.
        Path root = this.normalize().toAbsolutePath().getRoot();
        if (root == null) {
            throw Utility.logError(logger,
                new IllegalStateException("Root should never be null after calling toAbsolutePath."));
        }
        String fileStoreName = this.rootToFileStore(root.toString());

        BlobContainerClient containerClient =
            ((AzureFileStore) this.parentFileSystem.getFileStore(fileStoreName)).getContainerClient();

        return containerClient.getBlobClient(this.withoutRoot());
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
        /*
        This is a special case where we split after removing the root from a path that is just the root. Or otherwise
        have an empty path.
         */
        if (arr.length == 1 && arr[0].isEmpty()) {
            return new String[0];
        }
        return arr;
    }

    private String rootToFileStore(String root) {
        return root.substring(0, root.length() - 1); // Remove the ROOT_DIR_SUFFIX
    }
}
