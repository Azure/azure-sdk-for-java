// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.exception.UnexpectedLengthException
import com.azure.storage.common.policy.RequestRetryOptions
import com.azure.storage.common.policy.RetryPolicyType
import reactor.test.StepVerifier
import spock.lang.Requires
import spock.lang.Unroll

import java.time.Duration
// Tests for package-private functionality.
@Requires( { playbackMode() }) // https://github.com/reactor/reactor-core/issues/1098
class RetryTest extends APISpec {
    static URL retryTestURL = new URL("https://" + RequestRetryTestFactory.RETRY_TEST_PRIMARY_HOST)
    static RequestRetryOptions retryTestOptions = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 6, 2,
        1000L, 4000L, RequestRetryTestFactory.RETRY_TEST_SECONDARY_HOST)

    def "Retries until success"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS, retryTestOptions)

        expect:
        StepVerifier.withVirtualTime({ retryTestFactory.send(retryTestURL) })
            .thenAwait(Duration.ofSeconds(60))
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 6
            }).verifyComplete()
    }

    def "Retries until max retries"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES, retryTestOptions)

        expect:
        StepVerifier.withVirtualTime({ retryTestFactory.send(retryTestURL) })
            .thenAwait(Duration.ofSeconds(60))
            .assertNext({
                assert it.getStatusCode() == 503
                assert retryTestFactory.getTryNumber() == retryTestOptions.getMaxTries()
            }).verifyComplete()
    }

    def "Retries non retryable"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_RETRYABLE, retryTestOptions)

        expect:
        StepVerifier.withVirtualTime({ retryTestFactory.send(retryTestURL) })
            .thenAwait(Duration.ofSeconds(60))
            .assertNext({
                assert it.getStatusCode() == 400
                assert retryTestFactory.getTryNumber() == 1
            }).verifyComplete()
    }

    def "Retries non retryable secondary"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_RETRYABLE_SECONDARY, retryTestOptions)

        expect:
        StepVerifier.withVirtualTime({ retryTestFactory.send(retryTestURL) })
            .thenAwait(Duration.ofSeconds(60))
            .assertNext({
                assert it.getStatusCode() == 400
                assert retryTestFactory.getTryNumber() == 2
            }).verifyComplete()
    }

    def "Retries network error"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NETWORK_ERROR, retryTestOptions)

        expect:
        StepVerifier.withVirtualTime({ retryTestFactory.send(retryTestURL) })
            .thenAwait(Duration.ofSeconds(60))
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 3
            }).verifyComplete()
    }

    def "Retries try timeout"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_TRY_TIMEOUT, retryTestOptions)

        expect:
        StepVerifier.withVirtualTime({ retryTestFactory.send(retryTestURL) })
            .thenAwait(Duration.ofSeconds(60))
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 3
            }).verifyComplete()
    }

    def "Retries exponential delay"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING, retryTestOptions)

        expect:
        StepVerifier.create(retryTestFactory.send(retryTestURL))
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 6
            }).verifyComplete()
    }

    def "Retries fixed delay"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_FIXED_TIMING, retryTestOptions)

        expect:
        StepVerifier.create(retryTestFactory.send(retryTestURL))
            .assertNext({
                assert it.getStatusCode() == 200
                assert retryTestFactory.getTryNumber() == 4
            }).verifyComplete()
    }

    def "Retries non replyable flux"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_REPLAYABLE_FLOWABLE, retryTestOptions)

        expect:
        StepVerifier.withVirtualTime({ retryTestFactory.send(retryTestURL) })
            .thenAwait(Duration.ofSeconds(60))
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
