// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob


import com.azure.core.http.HttpResponse
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import spock.lang.Unroll
// Tests for package-private functionality.
class RetryTest extends APISpec {
    static URL retryTestURL = new URL("https://" + RequestRetryTestFactory.RETRY_TEST_PRIMARY_HOST)
    static RequestRetryOptions retryTestOptions = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 6, 2,
        1000L, 4000L, RequestRetryTestFactory.RETRY_TEST_SECONDARY_HOST)

    def "Retries until success"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS, retryTestOptions)

        when:
        HttpResponse response = retryTestFactory.send(retryTestURL).block()

        then:
        response.statusCode() == 200
        retryTestFactory.getTryNumber() == 6
    }

    def "Retries until max retries"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES, retryTestOptions)

        when:
        HttpResponse response = retryTestFactory.send(retryTestURL).block()

        then:
        response.statusCode() == 503
        retryTestFactory.getTryNumber() == retryTestOptions.maxTries()
    }

    def "Retries non retryable"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_RETRYABLE, retryTestOptions)

        when:
        HttpResponse response = retryTestFactory.send(retryTestURL).block()

        then:
        response.statusCode() == 400
        retryTestFactory.getTryNumber() == 1
    }

    def "Retries non retryable secondary"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_RETRYABLE_SECONDARY, retryTestOptions)

        when:
        HttpResponse response = retryTestFactory.send(retryTestURL).block()

        then:
        response.statusCode() == 400
        retryTestFactory.getTryNumber() == 2
    }

    def "Retries network error"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NETWORK_ERROR, retryTestOptions)

        when:
        HttpResponse response = retryTestFactory.send(retryTestURL).block()

        then:
        response.statusCode() == 200
        retryTestFactory.getTryNumber() == 3
    }

    def "Retries try timeout"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_TRY_TIMEOUT, retryTestOptions)

        when:
        HttpResponse response = retryTestFactory.send(retryTestURL).block()

        then:
        response.statusCode() == 200
        retryTestFactory.getTryNumber() == 3
    }

    def "Retries exponential delay"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING, retryTestOptions)

        when:
        HttpResponse response = retryTestFactory.send(retryTestURL).block()

        then:
        response.statusCode() == 200
        retryTestFactory.getTryNumber() == 6
    }

    def "Retries fixed delay"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_FIXED_TIMING, retryTestOptions)

        when:
        HttpResponse response = retryTestFactory.send(retryTestURL).block()

        then:
        response.statusCode() == 200
        retryTestFactory.getTryNumber() == 4
    }

    def "Retries non replyable flux"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_REPLAYABLE_FLOWABLE, retryTestOptions)

        when:
        retryTestFactory.send(retryTestURL).block()

        then:
        def e = thrown(IllegalStateException)
        e.getMessage().startsWith("The request failed because")
        e.getCause() instanceof UnexpectedLengthException
    }

    @Unroll
    def "Retries options invalid"() {
        when:
        new RequestRetryOptions(null, maxTries, tryTimeout, retryDelayInMs, maxRetryDelayInMs, null)

        then:
        thrown(IllegalArgumentException)

        where:
        maxTries | tryTimeout | retryDelayInMs | maxRetryDelayInMs
        0        | null       | null           | null
        null     | 0          | null           | null
        null     | null       | 0              | 1
        null     | null       | 1              | 0
        null     | null       | null           | 1
        null     | null       | 1              | null
        null     | null       | 5              | 4
    }
}
