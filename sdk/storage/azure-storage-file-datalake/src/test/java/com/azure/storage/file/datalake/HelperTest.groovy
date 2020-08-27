// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake

import com.azure.storage.file.datalake.models.FileRange
import spock.lang.Specification
import spock.lang.Unroll

class HelperTest extends Specification {

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    @Unroll
    def "File range"() {
        expect:
        if (count == null) {
            assert new FileRange(offset).toHeaderValue() == result
        } else {
            assert new FileRange(offset, count).toHeaderValue() == result
        }


        where:
        offset | count || result
        0      | null  || null
        0      | 5     || "bytes=0-4"
        5      | 10    || "bytes=5-14"
    }

    @Unroll
    def "File range IA"() {
        when:
        new FileRange(offset, count)

        then:
        thrown(IllegalArgumentException)

        where:
        offset | count
        -1     | 5
        0      | -1
    }
}
