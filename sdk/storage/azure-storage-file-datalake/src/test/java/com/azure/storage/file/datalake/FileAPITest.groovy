package com.azure.storage.file.datalake

import com.azure.storage.file.datalake.models.PathAccessControl
import reactor.core.publisher.Flux


class FileAPITest extends APISpec {
    FileClient fc
    String fileName

    def setup() {
        fileName = generatePathName()
        fc = fsc.getFileClient(fileName)
    }

    def "Create min"() {
        when:
        def pathItem = fc.create()
        then:
        pathItem
    }

    def "Delete min"() {
        setup:
        fc.create()

        when:
        def resp = fc.deleteWithResponse(null, null, null)

        then:
        resp.getStatusCode() == 200
    }

    def "Append data min"() {
        setup:
        fc.create()

        when:
        fc.appendData(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)

        then:
        notThrown(Exception)
    }

    def "Flush data min"() {
        setup:
        fc.create()

        when:
        fc.appendData(new ByteArrayInputStream(defaultData.array()), 0, defaultDataSize)
        fc.flushData(defaultDataSize)

        then:
        notThrown(Exception)
    }

    def "Set access control min"() {
        setup:
        fc.create()

        when:
        fc.setAccessControl(new PathAccessControl().setPermissions("0777"))

        then:
        notThrown(Exception)
    }

    def "Get access control min"() {
        setup:
        fc.create()

        when:
        fc.setAccessControl(new PathAccessControl().setPermissions("0777"))
        fc.getAccessControl()

        then:
        notThrown(Exception)
    }
}
