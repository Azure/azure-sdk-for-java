// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.exception.UnexpectedLengthException
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Retry
import spock.lang.Specification
import spock.lang.Unroll

// Tests for package-private functionality.
@Retry(count = 3, delay = 1000)
class RetryTest extends Specification {
    static URL retryTestURL = new URL("https://" + RequestRetryTestFactory.RETRY_TEST_PRIMARY_HOST)
    static RequestRetryOptions retryTestOptions = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 6, 2,
        1000L, 4000L, RequestRetryTestFactory.RETRY_TEST_SECONDARY_HOST)

    def "Retries until success"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 6
            }).verifyComplete()
    }

    def "Sync retries until success with primary & secondary host scenario"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS, retryTestOptions)

        when:
        def response = retryTestFactory.sendSync(retryTestURL)

        then:
        assert response.getStatusCode() == 200
        assert retryTestFactory.getTryNumber() == 6
    }

    def "Retries until max retries"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 503
                assert retryTestFactory.getTryNumber() == retryTestOptions.getMaxTries()
            }).verifyComplete()

    }

    def "Retries until max retries with exception"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES_WITH_EXCEPTION, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .expectErrorSatisfies({
                assert it instanceof IOException
                assert it.message == "Exception number " + retryTestOptions.getMaxTries()
                assert it.suppressed != null
                assert it.suppressed[0] instanceof IOException
                assert it.suppressed[0].message == "Exception number 1"
                assert it.suppressed[1] instanceof IOException
                assert it.suppressed[1].message == "Exception number 2"
                assert it.suppressed[2] instanceof IOException
                assert it.suppressed[2].message == "Exception number 3"
                assert it.suppressed[3] instanceof IOException
                assert it.suppressed[3].message == "Exception number 4"
                assert it.suppressed[4] instanceof IOException
                assert it.suppressed[4].message == "Exception number 5"
            }).verify()
    }

    def "Retries non retryable"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_RETRYABLE, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 400
                assert retryTestFactory.getTryNumber() == 1
            }).verifyComplete()
    }

    def "Retries non retryable secondary"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_RETRYABLE_SECONDARY, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 400
                assert retryTestFactory.getTryNumber() == 2
            }).verifyComplete()
    }

    def "Retries network error"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NETWORK_ERROR, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 3
            }).verifyComplete()
    }


    def "Retries wrapped network error"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_WRAPPED_NETWORK_ERROR, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 3
            }).verifyComplete()
    }

    def "Retries wrapped timeout error"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_WRAPPED_TIMEOUT_ERROR, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 3
            }).verifyComplete()
    }

    def "Retries try timeout"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_TRY_TIMEOUT, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 3
            }).verifyComplete()
    }

    def "Retries exponential delay"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 6
            }).verifyComplete()
    }

    def "Retries fixed delay"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_FIXED_TIMING, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 4
            }).verifyComplete()
    }

    def "Retries non replyable flux"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_REPLAYABLE_FLOWABLE, retryTestOptions)

        when:
        def responseMono = Mono.defer { retryTestFactory.send(retryTestURL) }

        then:
        StepVerifier.create(responseMono)
            .verifyErrorMatches({
                it instanceof IllegalStateException
                it.getMessage().startsWith("The request failed because")
                it.getCause() instanceof UnexpectedLengthException
            })
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
