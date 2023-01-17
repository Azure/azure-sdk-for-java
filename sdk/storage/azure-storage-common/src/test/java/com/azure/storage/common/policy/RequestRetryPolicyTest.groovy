package com.azure.storage.common.policy

import reactor.core.Exceptions
import spock.lang.Specification

import java.util.concurrent.TimeoutException

/**
 * Tests {@link RequestRetryPolicy}
 */
class RequestRetryPolicyTest extends Specification {
    def "should exception be retried"() {
        expect:
        RequestRetryPolicy.shouldErrorBeRetried(error, 0, 1).canBeRetried == shouldBeRetried

        where:
        error                                                       | shouldBeRetried
        new IOException()                                           | true
        new TimeoutException()                                      | true
        new RuntimeException()                                      | false
        Exceptions.propagate(new IOException())                     | true
        Exceptions.propagate(new TimeoutException())                | true
        Exceptions.propagate(new RuntimeException())                | false
        new Exception(new IOException())                            | true
        new Exception(new TimeoutException())                       | true
        new Exception(new RuntimeException())                       | false
        Exceptions.propagate(new Exception(new IOException()))      | true
        Exceptions.propagate(new Exception(new TimeoutException())) | true
        Exceptions.propagate(new Exception(new RuntimeException())) | false
    }

    def "should status code be retried"() {
        expect:
        RequestRetryPolicy.shouldStatusCodeBeRetried(statusCode, isPrimary) == shouldBeRetried

        where:
        statusCode | isPrimary | shouldBeRetried
        429        | true      | true
        429        | false     | true
        500        | true      | true
        500        | false     | true
        503        | true      | true
        503        | false     | true
        404        | true      | false
        404        | false     | true
    }
}
