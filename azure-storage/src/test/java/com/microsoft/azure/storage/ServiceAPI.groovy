package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.ListContainersOptions
import com.microsoft.azure.storage.blob.models.StorageServiceProperties

class ServiceAPI extends APISpec {
    StorageServiceProperties originalProps = primaryServiceURL.getProperties().blockingGet().body()

    def cleanup() {
        primaryServiceURL.setProperties(originalProps).blockingGet()
    }
    def "Service list containers"() {
        expect:
        primaryServiceURL.listContainersSegment(null, new ListContainersOptions(null,
                containerPrefix, null)).blockingGet().body().containers().size() == 1
    }

    def "Service set properties"() {
        expect:
        primaryServiceURL.setProperties(new StorageServiceProperties().
                withDefaultServiceVersion("2016-05-31")).blockingGet().statusCode() == 202
    }

    def "Service get properties"() {
        when:
        StorageServiceProperties props = primaryServiceURL.getProperties().blockingGet().body()

        then:
        props.logging() != null
        props.defaultServiceVersion() != null
    }
}
