package com.azure.storage.file.datalake.implementation.util

import com.azure.storage.file.datalake.DataLakeServiceVersion
import spock.lang.Specification
import spock.lang.Unroll

class TransformUtilsTest extends Specification {

    @Unroll
    def "can transform all service versions"() {
        when:
        def blobServiceVersion = TransformUtils.toBlobServiceVersion(dataLakeServiceVersion)

        then:
        blobServiceVersion != null
        blobServiceVersion.getVersion() == dataLakeServiceVersion.getVersion()

        where:
        dataLakeServiceVersion << DataLakeServiceVersion.values()
    }
}
