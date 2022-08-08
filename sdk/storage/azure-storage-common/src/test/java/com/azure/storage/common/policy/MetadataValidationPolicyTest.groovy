package com.azure.storage.common.policy

import com.azure.core.http.HttpHeaders
import spock.lang.Specification

class MetadataValidationPolicyTest extends Specification {
    def "invalid metadata whitespace"() {
        when:
        MetadataValidationPolicy.validateMetadataHeaders(headers)

        then:
        thrown(IllegalArgumentException)

        where:
        headers                                                      || _
        new HttpHeaders().add("x-ms-meta- ", "value")                || _
        new HttpHeaders().add("x-ms-meta- nameleadspace", "value")   || _
        new HttpHeaders().add("x-ms-meta-nametrailspace ", "value")  || _
        new HttpHeaders().add("x-ms-meta-valueleadspace", " value")  || _
        new HttpHeaders().add("x-ms-meta-valuetrailspace", "value ") || _
    }


    def "empty metadata name is valid"() { // sort of, this is passed to be handled by the service
        when:
        MetadataValidationPolicy.validateMetadataHeaders(new HttpHeaders().add("x-ms-meta-", "emptyname"))

        then:
        notThrown(IllegalArgumentException)
    }

    def "empty metadata value is valid"() {
        when:
        MetadataValidationPolicy.validateMetadataHeaders(new HttpHeaders().add("x-ms-meta-emptyvalue", ""))

        then:
        notThrown(IllegalArgumentException)
    }

    def "valid metadata"() {
        when:
        MetadataValidationPolicy.validateMetadataHeaders(headers)

        then:
        notThrown(IllegalArgumentException)

        where:
        headers                                                      || _
        new HttpHeaders().add("x-ms-meta-something", "value")        || _
        new HttpHeaders().add("x-ms-meta-anothersomething", "value") || _
    }

    def "no metadata values"() {
        when:
        MetadataValidationPolicy.validateMetadataHeaders(headers)

        then:
        notThrown(IllegalArgumentException)

        where:
        headers                                                               || _
        new HttpHeaders().add("", "notmetadataheaderemptyname")               || _
        new HttpHeaders().add(" ", "notmetadataheaderspacename")              || _
        new HttpHeaders().add(" notmetadataheaderwithleadingspace", "value")  || _
        new HttpHeaders().add("notmetadataheaderwithtrailingspace ", "value") || _
        new HttpHeaders().add("notmetadatavaluewithtrailingspace", " value")  || _
        new HttpHeaders().add("notmetadatavaluewithtrailingspace", "value ")  || _
        new HttpHeaders().add("notmetadatavaluewithspacevalue", " ")          || _
    }
}
