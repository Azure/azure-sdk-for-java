// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import spock.lang.ResourceLock
import spock.lang.Unroll

import java.nio.file.FileSystems

@ResourceLock("AzurePathTest")
class AzurePathTest extends APISpec {
    AzureFileSystem fs

    // Just need one fs instance for creating the paths.
    def setup() {
        def config = initializeConfigMap()
        config[AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL] = environment.primaryAccount.credential
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = "jtcazurepath1,jtcazurepath2"
        fs = new AzureFileSystem(new AzureFileSystemProvider(), environment.primaryAccount.blobEndpoint, config)
    }

    def "GetFileSystem"() {
        when:
        def path = fs.getPath("Foo")

        then:
        path.getFileSystem() == fs
    }

    @Unroll
    def "IsAbsolute getRoot"() {
        expect:
        fs.getPath(path).isAbsolute() == absolute
        fs.getPath(path).getRoot() == (root == null ? null : fs.getPath(root))

        where:
        path                 || absolute | root
        "foo"                || false    | null
        "foo/bar"            || false    | null
        "jtcazurepath1/foo"  || false    | null
        "jtcazurepath1:/foo" || true     | "jtcazurepath1:/"
        "fakeroot:/foo"      || true     | "fakeroot:/"
        "jtcazurepath2:/"    || true     | "jtcazurepath2:/"
        "jtcazurepath2:"     || true     | "jtcazurepath2:/"
        ""                   || false    | null
    }

    @Unroll
    def "GetFileName getParent getNameCount"() {
        expect:
        fs.getPath(path).getFileName() == (fileName == null ? null : fs.getPath(fileName))
        fs.getPath(path).getParent() == (parent == null ? null : fs.getPath(parent))
        fs.getPath(path).getNameCount() == nameCount

        where:
        path             || fileName | parent        | nameCount
        "root:/"         || null     | null          | 0
        "root:/foo"      || "foo"    | "root:"       | 1
        "root:/foo/bar"  || "bar"    | "root:/foo"   | 2
        "foo"            || "foo"    | null          | 1
        "foo/"           || "foo"    | null          | 1
        "/foo"           || "foo"    | null          | 1
        "foo/bar"        || "bar"    | "foo"         | 2
        "foo/bar/baz"    || "baz"    | "foo/bar"     | 3
        "foo/../bar/baz" || "baz"    | "foo/../bar/" | 4
        "foo/.."         || ".."     | "foo/"        | 2
        "foo/./bar"      || "bar"    | "foo/./"      | 3
        "foo/bar/."      || "."      | "foo/bar/"    | 3
        ""               || ""       | null          | 1
    }

    @Unroll
    def "GetName"() {
        setup:
        def rootPath = "root:/foo/bar/baz"
        def path = "foo/bar/baz"

        expect:
        fs.getPath(rootPath).getName(index) == fs.getPath(name)
        fs.getPath(path).getName(index) == fs.getPath(name)

        where:
        index || name
        0      | "foo"
        1      | "bar"
        2      | "baz"
    }

    @Unroll
    def "GetName fail"() {
        when:
        fs.getPath("foo/bar").getName(index)

        then:
        thrown(IllegalArgumentException)

        when:
        // Special case with no name elements
        fs.getPath("root:/").getName(0)

        then:
        thrown(IllegalArgumentException)

        where:
        index | _
        -1    | _
        2     | _
    }

    @Unroll
    def "SubPath"() {
        setup:
        def rootPath = "root:/foo/bar/fizz/buzz/dir"
        def path = "foo/bar/fizz/buzz/dir"

        expect:
        fs.getPath(rootPath).subpath(begin, end) == fs.getPath(resultPath)
        fs.getPath(path).subpath(begin, end) == fs.getPath(resultPath)

        where:
        begin | end || resultPath
        0     | 1   || "foo"
        0     | 3   || "foo/bar/fizz"
        0     | 5   || "foo/bar/fizz/buzz/dir"
        1     | 2   || "bar"
        1     | 4   || "bar/fizz/buzz"
        1     | 5   || "bar/fizz/buzz/dir"
        4     | 5   || "dir"
    }

    // The javadocs define an equivalence between these two methods in special cases.
    def "SubPath getParent"() {
        setup:
        def path = fs.getPath("foo/bar/fizz/buzz")

        expect:
        path.subpath(0, path.getNameCount() - 1) == path.getParent()
    }

    def "SubPath fail"() {
        when:
        fs.getPath("foo/bar/fizz/buzz/dir").subpath(begin, end)

        then:
        thrown(IllegalArgumentException)

        where:
        begin | end
        -1    | 1
        5     | 5
        3     | 3
        3     | 1
        3     | 6
    }

    @Unroll
    def "StartsWith"() {
        expect:
        fs.getPath(path).startsWith(fs.getPath(otherPath)) == startsWith
        fs.getPath(path).startsWith(otherPath) == startsWith

        // If the paths are not from the same file system, false is always returned
        !fs.getPath("foo/bar").startsWith(FileSystems.default.getPath("foo/bar"))

        where:
        path           | otherPath    || startsWith
        "root:/foo"    | "foo"        || false
        "foo"          | "root:/foo"  || false
        "foo"          | "foo"        || true
        "root:/foo"    | "root:/foo"  || true
        "root2:/foo"   | "root:/foo"  || false
        "root:/foo"    | "root2:/foo" || false
        "foo/bar"      | "foo"        || true
        "foo/bar"      | "foo/bar"    || true
        "foo/bar/fizz" | "foo"        || true
        "foo/bar/fizz" | "f"          || false
        "foo/bar/fizz" | "foo/bar/f"  || false
        "foo"          | "foo/bar"    || false
        ""             | "foo"        || false
        "foo"          | ""           || false
    }

    @Unroll
    def "EndsWith"() {
        expect:
        fs.getPath(path).endsWith(fs.getPath(otherPath)) == endsWith
        fs.getPath(path).endsWith(otherPath) == endsWith

        // If the paths are not from the same file system, false is always returned
        !fs.getPath("foo/bar").endsWith(FileSystems.default.getPath("foo/bar"))

        where:
        path           | otherPath    || endsWith
        "root:/foo"    | "foo"        || true
        "foo"          | "root:/foo"  || false
        "foo"          | "foo"        || true
        "root:/foo"    | "root:/foo"  || true
        "root2:/foo"   | "root:/foo"  || false
        "root:/foo"    | "root2:/foo" || false
        "foo/bar"      | "bar"        || true
        "foo/bar"      | "foo/bar"    || true
        "foo/bar/fizz" | "fizz"       || true
        "foo/bar/fizz" | "z"          || false
        "foo/bar/fizz" | "r/fizz"     || false
        "foo"          | "foo/bar"    || false
        ""             | "foo"        || false
        "foo"          | ""           || false
    }

    @Unroll
    def "Normalize"() {
        expect:
        fs.getPath(path).normalize() == fs.getPath(resultPath)

        where:
        path                 || resultPath
        "foo/bar"            || "foo/bar"
        "."                  || ""
        ".."                 || ".."
        "foo/.."             || ""
        "foo/bar/.."         || "foo"
        "foo/../bar"         || "bar"
        "foo/./bar"          || "foo/bar"
        "foo/bar/."          || "foo/bar"
        "foo/bar/fizz/../.." || "foo"
        "foo/bar/../fizz/."  || "foo/fizz"
        "foo/../.."          || ".."
        "foo/../../bar"      || "../bar"
        "root:/foo/bar"      || "root:/foo/bar"
        "root:/."            || "root:/"
        "root:/.."           || "root:/"
        "root:/../../.."     || "root:/"
        "root:/foo/.."       || "root:"
        ""                   || ""
    }

    @Unroll
    def "Resolve"() {
        expect:
        fs.getPath(path).resolve(fs.getPath(other)) == fs.getPath(resultPath)
        fs.getPath(path).resolve(other) == fs.getPath(resultPath)

        where:
        path              | other             || resultPath
        "foo/bar"         | "root:/fizz/buzz" || "root:/fizz/buzz"
        "root:/foo/bar"   | "root:/fizz/buzz" || "root:/fizz/buzz"
        "foo/bar"         | ""                || "foo/bar"
        "foo/bar"         | "fizz/buzz"       || "foo/bar/fizz/buzz"
        "foo/bar/.."      | "../../fizz/buzz" || "foo/bar/../../../fizz/buzz"
        "root:/../foo/./" | "fizz/../buzz"    || "root:/../foo/./fizz/../buzz"
        ""                | "foo/bar"         || "foo/bar"
    }

    @Unroll
    def "ResolveSibling"() {
        expect:
        fs.getPath(path).resolveSibling(fs.getPath(other)) == fs.getPath(resultPath)
        fs.getPath(path).resolveSibling(other) == fs.getPath(resultPath)

        where:
        path            | other        || resultPath
        "foo"           | "fizz"       || "fizz"
        "foo/bar"       | "root:/fizz" || "root:/fizz"
        "foo/bar"       | ""           || "foo"
        "foo"           | ""           || ""
        ""              | "foo"        || "foo"
        "foo/bar"       | "fizz"       || "foo/fizz"
        "foo/bar/fizz"  | "buzz/dir"   || "foo/bar/buzz/dir"
        "root:/foo/bar" | "fizz"       || "root:/foo/fizz"
        "root:/foo"     | "fizz"       || "root:/fizz"
        "root:/"        | "fizz"       || "fizz"
    }

    @Unroll
    def "Relativize"() {
        setup:
        def p = fs.getPath(path)
        def otherP = fs.getPath(other)

        expect:
        p.relativize(otherP) == fs.getPath(result)
        if (equivalence) { // Only applies when neither path has a root and both are normalized.
            assert p.relativize(p.resolve(otherP)) == otherP
        }

        where:
        path            | other                || result                  | equivalence
        "foo/bar"       | "foo/bar/fizz/buzz/" || "fizz/buzz"             | true
        "foo/bar"       | "foo/bar"            || ""                      | true
        "root:/foo/bar" | "root:/foo/bar/fizz" || "fizz"                  | false
        "foo/dir"       | "foo/fizz/buzz"      || "../fizz/buzz"          | true
        "foo/bar/a/b/c" | "foo/bar/fizz"       || "../../../fizz"         | true
        "a/b/c"         | "foo/bar/fizz"       || "../../../foo/bar/fizz" | true
        "foo/../bar"    | "bar/./fizz"         || "fizz"                  | false
        "root:"         | "root:/foo/bar"      || "foo/bar"               | false
        ""              | "foo"                || "foo"                   | true
        "foo"           | ""                   || ".."                    | true
    }

    def "Relativize fail"() {
        when:
        fs.getPath(path).relativize(fs.getPath(other))

        then:
        thrown(IllegalArgumentException)

        where:
        path            | other
        "root:/foo/bar" | "foo/bar/fizz/buzz"
        "foo/bar"       | "root:/foo/bar/fizz"
    }

    @Unroll
    def "ToUri toAbsolute"() {
        expect:
        fs.getPath(path).toAbsolutePath().toString() == expected
        fs.getPath(path).toUri().toString() == fs.provider().getScheme() + ":/" + expected

        where:
        path            | expected
        "root:/foo/bar" | "root:/foo/bar"
        "foo/bar"       | "jtcazurepath1:/foo/bar"
        ""              | "jtcazurepath1:"
    }

    @Unroll
    def "Iterator"() {
        setup:
        def p = fs.getPath(path)
        def it = p.iterator()
        def i = 0

        def emptyIt = fs.getPath("").iterator()

        expect:
        while (it.hasNext()) {
            assert it.next() == p.getName(i)
            i++
        }

        emptyIt.next().toString() == ""
        !emptyIt.hasNext()

        where:
        path                | _
        "root:/foo/bar"     | _
        "foo/bar/fizz/buzz" | _
        "foo"               | _
        "root:/"            | _
    }

    @Unroll
    def "CompareTo equals"() {
        expect:
        fs.getPath(path1).compareTo(fs.getPath(path2)) == path1.compareTo(path2)
        fs.getPath(path1).equals(fs.getPath(path2)) == equals

        where:
        path1     | path2     | equals
        "a/b/c"   | "a/b"     | false
        "a/b/c"   | "foo/bar" | false
        "foo/bar" | "foo/bar" | true
        ""        | "foo"     | false
    }

    def "CompareTo equals fails"() {
        when:
        def path1 = fs.getPath("a/b")
        def path2 = FileSystems.default.getPath("a/b")

        then:
        !path1.equals(path2)

        when:
        path1.compareTo(path2)

        then:
        thrown(ClassCastException)
    }

    def "GetBlobClient relative"() {
        when:
        def path = fs.getPath("foo/bar")
        def client = ((AzurePath) path).toBlobClient()

        then:
        client.getBlobName() == "foo/bar"
        client.getContainerName() == rootNameToContainerName(getDefaultDir(fs))
    }

    def "GetBlobClient empty"() {
        when:
        def path = fs.getPath(getNonDefaultRootDir(fs))
        ((AzurePath) path).toBlobClient()

        then:
        thrown(IOException)

        when:
        path = fs.getPath("")
        ((AzurePath) path).toBlobClient()

        then:
        thrown(IOException)
    }

    def "GetBlobClient absolute"() {
        when:
        def path = fs.getPath(getNonDefaultRootDir(fs), "foo/bar")
        def client = ((AzurePath) path).toBlobClient()

        then:
        client.getBlobName() == "foo/bar"
        client.getContainerName() == rootNameToContainerName(getNonDefaultRootDir(fs))
    }

    def "GetBlobClient fail"() {
        when:
        // Can't get a client to a nonexistent root/container.
        ((AzurePath) fs.getPath("fakeRoot:", "foo/bar")).toBlobClient()

        then:
        thrown(IOException)
    }
}
