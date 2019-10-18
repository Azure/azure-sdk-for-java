package com.azure.storage.file.datalake

import com.azure.storage.file.datalake.implementation.models.StorageErrorException
import com.azure.storage.file.datalake.models.PathAccessControl

class DirectoryAPITest extends APISpec {
    DirectoryClient dc
    String directoryName


    // TODO (gapra): Add tests to async create file and subdirectory
    def setup() {
        directoryName = generatePathName()
        dc = fsc.getDirectoryClient(directoryName)
    }

    def "Create min"() {
        when:
        dc.create()
        then:
        notThrown(StorageErrorException)
    }

    def "Delete min"() {
        setup:
        dc.create()

        when:
        def resp = dc.deleteWithResponse(false, null, null, null)

        then:
        resp.getStatusCode() == 200
    }

    def "Create sub directory"() {
        setup:
        dc.create()

        when:
        def subdc = dc.createSubDirectory(generatePathName())

        then:
        subdc.delete()
        notThrown(Exception)
    }

    def "Create file"() {
        setup:
        dc.create()

        when:

        def fc = dc.createFile(generatePathName())

        then:
        fc.delete()
        notThrown(Exception)
    }

    def "Delete file"() {
        setup:
        dc.create()
        def pathName = generatePathName()
        dc.createFile(pathName)

        when:
        dc.deleteFile(pathName)

        then:
        notThrown(Exception)
    }

    def "Delete sub directory"() {
        setup:
        dc.create()
        def pathName = generatePathName()
        dc.createSubDirectory(pathName)

        when:
        dc.deleteSubDirectory(pathName)

        then:
        notThrown(Exception)
    }

    def "Set access control min"() {
        setup:
        dc.create()

        when:
        dc.setAccessControl(new PathAccessControl().setPermissions("0777"))

        then:
        notThrown(Exception)
    }

    def "Get access control min"() {
        setup:
        dc.create()

        when:
        dc.setAccessControl(new PathAccessControl().setPermissions("0777"))
        dc.getAccessControl()

        then:
        notThrown(Exception)
    }

    def "move async"() {
        setup:
        def dac = fscAsync.getDirectoryAsyncClient("dir")
        dac.create().block()

        when:
        dac.move("dir2").block()

        then:
        notThrown(Exception)
    }
}
