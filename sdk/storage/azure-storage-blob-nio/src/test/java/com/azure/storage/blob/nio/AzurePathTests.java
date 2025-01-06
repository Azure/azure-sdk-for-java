// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.storage.blob.BlobClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ResourceLock("AzurePathTest")
public class AzurePathTests extends BlobNioTestBase {
    private AzureFileSystem fs;

    // Just need one fs instance for creating the paths.
    @Override
    public void beforeTest() {
        super.beforeTest();
        Map<String, Object> config = initializeConfigMap();
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL, ENV.getPrimaryAccount().getCredential());
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, "jtcazurepath1,jtcazurepath2");
        try {
            fs = (AzureFileSystem) new AzureFileSystemProvider()
                .newFileSystem(new URI("azb://?endpoint=" + ENV.getPrimaryAccount().getBlobEndpoint()), config);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getFileSystem() {
        Path path = fs.getPath("Foo");

        assertEquals(fs, path.getFileSystem());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "foo,false,null",
            "foo/bar,false,null",
            "jtcazurepath1/foo,false,null",
            "jtcazurepath1:/foo,true,jtcazurepath1:/",
            "fakeroot:/foo,true,fakeroot:/",
            "jtcazurepath2:/,true,jtcazurepath2:/",
            "jtcazurepath2:,true,jtcazurepath2:/",
            "'',false,null" },
        nullValues = "null")
    public void isAbsoluteGetRoot(String path, boolean absolute, String root) {
        assertEquals(absolute, fs.getPath(path).isAbsolute());
        assertEquals((root == null ? null : fs.getPath(root)), fs.getPath(path).getRoot());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "root:/,null,null,0",
            "root:/foo,foo,root:,1",
            "root:/foo/bar,bar,root:/foo,2",
            "foo,foo,null,1",
            "foo/,foo,null,1",
            "/foo,foo,null,1",
            "foo/bar,bar,foo,2",
            "foo/bar/baz,baz,foo/bar,3",
            "foo/../bar/baz,baz,foo/../bar/,4",
            "foo/..,..,foo/,2",
            "foo/./bar,bar,foo/./,3",
            "foo/bar/.,.,foo/bar/,3",
            "'','',null,1" },
        nullValues = "null")
    public void getFileNameGetParentGetNameCount(String path, String fileName, String parent, int nameCount) {
        assertEquals((fileName == null ? null : fs.getPath(fileName)), fs.getPath(path).getFileName());
        assertEquals((parent == null ? null : fs.getPath(parent)), fs.getPath(path).getParent());
        assertEquals(nameCount, fs.getPath(path).getNameCount());
    }

    @ParameterizedTest
    @CsvSource(value = { "0,foo", "1,bar", "2,baz" })
    public void getName(int index, String name) {
        assertEquals(fs.getPath("root:/foo/bar/baz").getName(index), fs.getPath(name));
        assertEquals(fs.getPath("foo/bar/baz").getName(index), fs.getPath(name));
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 2 })
    public void getNameFail(int index) {
        assertThrows(IllegalArgumentException.class, () -> fs.getPath("foo/bar").getName(index));

        // Special case with no name elements
        assertThrows(IllegalArgumentException.class, () -> fs.getPath("root:/").getName(0));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "0,1,foo",
            "0,3,foo/bar/fizz",
            "0,5,foo/bar/fizz/buzz/dir",
            "1,2,bar",
            "1,4,bar/fizz/buzz",
            "1,5,bar/fizz/buzz/dir",
            "4,5,dir" })
    public void subPath(int begin, int end, String resultPath) {
        assertEquals(fs.getPath(resultPath), fs.getPath("root:/foo/bar/fizz/buzz/dir").subpath(begin, end));
        assertEquals(fs.getPath(resultPath), fs.getPath("foo/bar/fizz/buzz/dir").subpath(begin, end));
    }

    // The javadocs define an equivalence between these two methods in special cases.
    @Test
    public void subPathGetParent() {
        Path path = fs.getPath("foo/bar/fizz/buzz");

        assertEquals(path.getParent(), path.subpath(0, path.getNameCount() - 1));
    }

    @ParameterizedTest
    @CsvSource(value = { "-1,1", "5,5", "3,3", "3,1", "3,6" })
    public void subPathFail(int begin, int end) {
        assertThrows(IllegalArgumentException.class, () -> fs.getPath("foo/bar/fizz/buzz/dir").subpath(begin, end));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "root:/foo,foo,false",
            "foo,root:/foo,false",
            "foo,foo,true",
            "root:/foo,root:/foo,true",
            "root2:/foo,root:/foo,false",
            "root:/foo,root2:/foo,false",
            "foo/bar,foo,true",
            "foo/bar,foo/bar,true",
            "foo/bar/fizz,foo,true",
            "foo/bar/fizz,f,false",
            "foo/bar/fizz,foo/bar/f,false",
            "foo,foo/bar,false",
            "'',foo,false",
            "foo,'',false" })
    public void startsWith(String path, String otherPath, boolean startsWith) {
        assertEquals(startsWith, fs.getPath(path).startsWith(fs.getPath(otherPath)));
        assertEquals(startsWith, fs.getPath(path).startsWith(otherPath));

        // If the paths are not from the same file system, false is always returned
        assertFalse(fs.getPath("foo/bar").startsWith(FileSystems.getDefault().getPath("foo/bar")));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "root:/foo,foo,true",
            "foo,root:/foo,false",
            "foo,foo,true",
            "root:/foo,root:/foo,true",
            "root2:/foo,root:/foo,false",
            "root:/foo,root2:/foo,false",
            "foo/bar,bar,true",
            "foo/bar,foo/bar,true",
            "foo/bar/fizz,fizz,true",
            "foo/bar/fizz,z,false",
            "foo/bar/fizz,r/fizz,false",
            "foo,foo/bar,false",
            "'',foo,false",
            "foo,'',false" })
    public void endsWith(String path, String otherPath, boolean endsWith) {
        assertEquals(endsWith, fs.getPath(path).endsWith(fs.getPath(otherPath)));
        assertEquals(endsWith, fs.getPath(path).endsWith(otherPath));

        // If the paths are not from the same file system, false is always returned
        assertFalse(fs.getPath("foo/bar").endsWith(FileSystems.getDefault().getPath("foo/bar")));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "foo/bar,foo/bar",
            ".,''",
            "..,..",
            "foo/..,''",
            "foo/bar/..,foo",
            "foo/../bar,bar",
            "foo/./bar,foo/bar",
            "foo/bar/.,foo/bar",
            "foo/bar/fizz/../..,foo",
            "foo/bar/../fizz/.,foo/fizz",
            "foo/../..,..",
            "foo/../../bar,../bar",
            "root:/foo/bar,root:/foo/bar",
            "root:/.,root:/",
            "root:/..,root:/",
            "root:/../../..,root:/",
            "root:/foo/..,root:",
            "'',''" })
    public void normalize(String path, String resultPath) {
        assertEquals(fs.getPath(resultPath), fs.getPath(path).normalize());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "foo/bar,root:/fizz/buzz,root:/fizz/buzz",
            "root:/foo/bar,root:/fizz/buzz,root:/fizz/buzz",
            "foo/bar,'',foo/bar",
            "foo/bar,fizz/buzz,foo/bar/fizz/buzz",
            "foo/bar/..,../../fizz/buzz,foo/bar/../../../fizz/buzz",
            "root:/../foo/./,fizz/../buzz,root:/../foo/./fizz/../buzz",
            "'',foo/bar,foo/bar" })
    public void resolve(String path, String other, String resultPath) {
        assertEquals(fs.getPath(resultPath), fs.getPath(path).resolve(fs.getPath(other)));
        assertEquals(fs.getPath(resultPath), fs.getPath(path).resolve(other));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "foo,fizz,fizz",
            "foo/bar,root:/fizz,root:/fizz",
            "foo/bar,'',foo",
            "foo,'',''",
            "'',foo,foo",
            "foo/bar,fizz,foo/fizz",
            "foo/bar/fizz,buzz/dir,foo/bar/buzz/dir",
            "root:/foo/bar,fizz,root:/foo/fizz",
            "root:/foo,fizz,root:/fizz",
            "root:/,fizz,fizz" })
    public void resolveSibling(String path, String other, String resultPath) {
        assertEquals(fs.getPath(resultPath), fs.getPath(path).resolveSibling(fs.getPath(other)));
        assertEquals(fs.getPath(resultPath), fs.getPath(path).resolveSibling(other));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "foo/bar,foo/bar/fizz/buzz/,fizz/buzz,true",
            "foo/bar,foo/bar,'',true",
            "root:/foo/bar,root:/foo/bar/fizz,fizz,false",
            "foo/dir,foo/fizz/buzz,../fizz/buzz,true",
            "foo/bar/a/b/c,foo/bar/fizz,../../../fizz,true",
            "a/b/c,foo/bar/fizz,../../../foo/bar/fizz,true",
            "foo/../bar,bar/./fizz,fizz,false",
            "root:,root:/foo/bar,foo/bar,false",
            "'',foo,foo,true",
            "foo,'',..,true" })
    public void relativize(String path, String other, String result, boolean equivalence) {
        Path p = fs.getPath(path);
        Path otherP = fs.getPath(other);

        assertEquals(fs.getPath(result), p.relativize(otherP));
        if (equivalence) { // Only applies when neither path has a root and both are normalized.
            assertEquals(otherP, p.relativize(p.resolve(otherP)));
        }
    }

    @ParameterizedTest
    @CsvSource(value = { "root:/foo/bar,foo/bar/fizz/buzz", "foo/bar,root:/foo/bar/fizz" })
    public void relativizeFail(String path, String other) {
        assertThrows(IllegalArgumentException.class, () -> fs.getPath(path).relativize(fs.getPath(other)));
    }

    @ParameterizedTest
    @CsvSource(value = { "root:/foo/bar,root:/foo/bar", "foo/bar,jtcazurepath1:/foo/bar", "'',jtcazurepath1:" })
    public void toUriToAbsolute(String path, String expected) {
        assertEquals(expected, fs.getPath(path).toAbsolutePath().toString());
        assertEquals(fs.provider().getScheme() + ":/" + expected, fs.getPath(path).toUri().toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "root:/foo/bar", "foo/bar/fizz/buzz", "foo", "root:/" })
    public void iterator(String path) {
        Path p = fs.getPath(path);
        Iterator<Path> it = p.iterator();
        int i = 0;

        Iterator<Path> emptyIt = fs.getPath("").iterator();

        while (it.hasNext()) {
            assertEquals(p.getName(i), it.next());
            i++;
        }

        assertEquals("", emptyIt.next().toString());
        assertFalse(emptyIt.hasNext());
    }

    @ParameterizedTest
    @CsvSource(value = { "a/b/c,a/b,false", "a/b/c,foo/bar,false", "foo/bar,foo/bar,true", "'',foo,false" })
    public void compareToEquals(String path1, String path2, boolean equals) {
        assertEquals(path1.compareTo(path2), fs.getPath(path1).compareTo(fs.getPath(path2)));
        assertEquals(equals, fs.getPath(path1).equals(fs.getPath(path2)));
    }

    @Test
    public void compareToEqualsFails() {
        Path path1 = fs.getPath("a/b");
        Path path2 = FileSystems.getDefault().getPath("a/b");

        assertNotEquals(path1, path2);
        assertThrows(ClassCastException.class, () -> path1.compareTo(path2));
    }

    @Test
    public void getBlobClientRelative() throws IOException {
        BlobClient client = ((AzurePath) fs.getPath("foo/bar")).toBlobClient();

        assertEquals("foo/bar", client.getBlobName());
        assertEquals(rootNameToContainerName(getDefaultDir(fs)), client.getContainerName());
    }

    @Test
    public void getBlobClientEmpty() {
        assertThrows(IOException.class, () -> ((AzurePath) fs.getPath(getNonDefaultRootDir(fs))).toBlobClient());
        assertThrows(IOException.class, () -> ((AzurePath) fs.getPath("")).toBlobClient());
    }

    @Test
    public void getBlobClientAbsolute() throws IOException {
        Path path = fs.getPath(getNonDefaultRootDir(fs), "foo/bar");
        BlobClient client = ((AzurePath) path).toBlobClient();

        assertEquals("foo/bar", client.getBlobName());
        assertEquals(rootNameToContainerName(getNonDefaultRootDir(fs)), client.getContainerName());
    }

    @Test
    public void getBlobClientFail() {
        // Can't get a client to a nonexistent root/container.
        assertThrows(IOException.class, () -> ((AzurePath) fs.getPath("fakeRoot:", "foo/bar")).toBlobClient());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "://myaccount.blob.core.windows.net/containername/blobname,containername:/blobname",
            "://myaccount.blob.core.windows.net/containername/dirname/blobname,containername:/dirname/blobname",
            "://myaccount.blob.core.windows.net/containername,containername:",
            "://myaccount.blob.core.windows.net/,''", })
    public void fromBlobUrl(String url, String path) throws URISyntaxException {
        // Adjust the parameterized urls to point at real resources
        String scheme = ENV.getPrimaryAccount().getBlobEndpoint().startsWith("https") ? "https" : "http";
        url = scheme + url;
        url = url.replace("myaccount", ENV.getPrimaryAccount().getName());
        url = url.replace("containername", "jtcazurepath1");

        path = path.replace("myaccount", ENV.getPrimaryAccount().getName());
        path = path.replace("containername", "jtcazurepath1");

        AzurePath resultPath = AzurePath.fromBlobUrl((AzureFileSystemProvider) fs.provider(), url);

        assertEquals(fs, resultPath.getFileSystem());
        assertEquals(path, resultPath.toString());
    }

    @Test
    public void fromBlobUrlNoOpenFileSystem() {
        assertThrows(FileSystemNotFoundException.class, () -> AzurePath.fromBlobUrl(new AzureFileSystemProvider(),
            "http://myaccount.blob.core.windows.net/container/blob"));
    }
}
