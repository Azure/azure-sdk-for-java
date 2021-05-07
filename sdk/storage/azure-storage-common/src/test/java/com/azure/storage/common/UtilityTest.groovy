// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common

import com.azure.core.exception.UnexpectedLengthException
import com.azure.storage.common.implementation.Constants
import reactor.core.scheduler.Schedulers
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets
import java.util.function.Supplier

class UtilityTest extends Specification {

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    static final String defaultText = "default"

    static final Supplier<InputStream> defaultInputStream = new Supplier<InputStream>() {
        @Override
        InputStream get() {
            return new ByteArrayInputStream(defaultText.getBytes(StandardCharsets.UTF_8))
        }
    }

    protected static int defaultDataSize = defaultText.length()

    @Unroll
    def "convertStreamToByteBuffer IA"() {
        when:
        Utility.convertStreamToByteBuffer(is.get(), dataSize, 4 * Constants.MB, true, true).subscribeOn(Schedulers.elastic()).blockLast()

        then:
        thrown(exceptionType)

        where:
        is                  | dataSize            || exceptionType
        defaultInputStream  | defaultDataSize + 1 || UnexpectedLengthException
        defaultInputStream  | defaultDataSize - 1 || UnexpectedLengthException
    }

}
