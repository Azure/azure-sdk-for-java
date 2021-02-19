// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.models.BlobStorageException
import spock.lang.Unroll

import java.nio.file.ClosedFileSystemException

class NioBlobInputStreamTest extends APISpec {

    File sourceFile
    BlobClient bc
    NioBlobInputStream nioStream
    FileInputStream fileStream
    AzureFileSystem fs

    def setup() {
        sourceFile = getRandomFile(10 * 1024 * 1024)

        cc.create()
        bc = cc.getBlobClient(generateBlobName())
        bc.uploadFromFile(sourceFile.getPath())
        fs = createFS(initializeConfigMap())
        def path = ((AzurePath) fs.getPath(getNonDefaultRootDir(fs), bc.getBlobName()))

        nioStream = new NioBlobInputStream(bc.openInputStream(), path)
        fileStream = new FileInputStream(sourceFile)
    }

    def cleanup() {
        sourceFile.delete()
        cc.delete()
    }

    def "Read whole file"() {
        expect:
        compareInputStreams(nioStream, fileStream, sourceFile.size())
    }

    def "Read min"() {
        expect:
        for (i in 1..100) {
            assert nioStream.read() == fileStream.read()
        }
    }

    @Unroll
    def "Read buff"() {
        setup:
        def nioBytes = new byte[size]
        def fileBytes = new byte[size]

        when:
        nioStream.read(nioBytes)
        fileStream.read(fileBytes)

        then:
        nioBytes == fileBytes

        where:
        size            | _
        0               | _
        100             | _
        9 * 1024 * 1024 | _ // Test having to chunk downloads into multiple requests.
    }

    def "Read buff offset len"() {
        setup:
        def nioBytes = new byte[100]
        def fileBytes = new byte[100]

        when:
        nioStream.read(nioBytes, 5, 50)
        fileStream.read(fileBytes, 5, 50)

        then:
        nioBytes == fileBytes
    }

    @Unroll
    def "Read buff offset len fail"() {
        when:
        def b = new byte[10]
        nioStream.read(b, off, len)

        then:
        thrown(IndexOutOfBoundsException)

        where:
        off | len
        -1  | 5
        3   | -1
        0   | 11
        3   | 8
    }

    def "Read fail"() {
        setup:
        bc.delete()

        when:
        nioStream.read()

        then:
        def e = thrown(IOException)
        e.getCause() instanceof BlobStorageException

        when:
        nioStream.read(new byte[5])

        then:
        thrown(IOException)
        e.getCause() instanceof BlobStorageException

        when:
        nioStream.read(new byte[5], 0, 4)

        then:
        thrown(IOException)
        e.getCause() instanceof BlobStorageException
    }

    def "Read fs closed"() {
        when:
        fs.close()
        nioStream.read()

        then:
        thrown(ClosedFileSystemException)

        when:
        nioStream.read(new byte[1])

        then:
        thrown(ClosedFileSystemException)

        when:
        nioStream.read(new byte[10], 2, 5)

        then:
        thrown(ClosedFileSystemException)
    }


    @Unroll
    def "Mark and reset"() {
        setup:
        def b = new byte[markAfter]
        nioStream.read(b)
        fileStream.skip(markAfter) // Position the file stream where we expect to be after resetting.

        when: "Read some bytes past the mark"
        nioStream.mark(Integer.MAX_VALUE)

        nioStream.read(new byte[resetAfter])

        and: "Reset to the mark"
        nioStream.reset()

        then:
        compareInputStreams(nioStream, fileStream, sourceFile.length() - markAfter)

        where:
        markAfter       | resetAfter
        0               | 0
        0               | 50
        50              | 0
        50              | 50
        50              | 5 * 1024 * 1024 // Read enough such that we've made a new download call and refreshed the internal buffer.
        5 * 1024 * 1024 | 50
    }

    def "Mark read limit"() {
        setup:
        nioStream.mark(5)
        nioStream.read(new byte[6])

        when:
        nioStream.reset()

        then:
        thrown(IOException)
    }

    def "Reset fail"() {
        when: "Mark never set"
        nioStream.read()
        nioStream.reset()

        then:
        thrown(IOException)
    }

    def "Reset fs closed"() {
        setup:
        nioStream.mark(5)

        when:
        fs.close()
        nioStream.reset()

        then:
        thrown(ClosedFileSystemException)
    }

    def "Mark supported"() {
        expect:
        nioStream.markSupported()
    }

    @Unroll
    def "Skip"() {
        when:
        nioStream.skip(skip)
        fileStream.skip(skip)

        then:
        compareInputStreams(nioStream, fileStream, sourceFile.size() - skip)

        where:
        skip                   | _
        0                      | _
        10                     | _
        5 * 1024 * 1024        | _
        (10 * 1024 * 1024) - 1 | _
    }

    def "Skip fs closed"() {
        when:
        fs.close()
        nioStream.skip(5)

        then:
        thrown(ClosedFileSystemException)
    }

    def "Close"() {
        setup:
        nioStream.close()

        when:
        nioStream.read()

        then:
        thrown(IOException)

        when:
        nioStream.read(new byte[5])

        then:
        thrown(IOException)

        when:
        nioStream.read(new byte[5], 0, 4)

        then:
        thrown(IOException)
    }

    def "Close fs closed"() {
        when:
        fs.close()
        nioStream.close()

        then:
        thrown(ClosedFileSystemException)
    }

    @Unroll
    def "Available"() {
        when:
        nioStream.read(new byte[readAmount])

        then:
        nioStream.available() == available

        where:
        readAmount      | available
        0               | 0
        5               | (4 * 1024 * 1024) - 5
        5 * 1024 * 1024 | 3 * 1024 * 1024
    }

    def "Available fs closed"() {
        when:
        fs.close()
        nioStream.available()

        then:
        thrown(ClosedFileSystemException)
    }
}
