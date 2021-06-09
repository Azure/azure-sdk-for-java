// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common

import com.azure.core.exception.UnexpectedLengthException
import com.azure.storage.common.test.shared.TestDataFactory
import org.junit.jupiter.api.RepeatedTest
import spock.lang.Specification
import spock.lang.Unroll

class UtilityTest extends Specification {

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    protected getData() {
        return TestDataFactory.getInstance();
    }

    @Unroll
    def "convertStreamToByteBuffer IA"() {
        when:
        Utility.convertStreamToByteBuffer(data.defaultInputStream, size, 100, true).blockLast()

        then:
        thrown(UnexpectedLengthException)

        where:
        size                     || _
        data.defaultDataSize + 1 || _
        data.defaultDataSize - 1 || _
    }

}
