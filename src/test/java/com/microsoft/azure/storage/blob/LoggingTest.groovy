/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.blob

import com.microsoft.azure.storage.APISpec
import com.microsoft.rest.v2.http.HttpPipelineLogLevel
import com.microsoft.rest.v2.http.HttpPipelineLogger
import com.microsoft.rest.v2.http.HttpRequest
import com.microsoft.rest.v2.http.HttpResponse
import com.microsoft.rest.v2.policy.RequestPolicy
import com.microsoft.rest.v2.policy.RequestPolicyOptions
import io.reactivex.Single
import spock.lang.Unroll

class LoggingTest extends APISpec {
    /*
    This method returns a mock of an HttpPipelineLogger. We always want to return the same value for a given run. A mock
    also allows for defining the number and kind of interactions that the unit under test has with this mocked object.
    See below for more details.
     */
    def getMockLogger(HttpPipelineLogLevel level) {
        return Mock(HttpPipelineLogger) {
            minimumLogLevel() >> level
        }
    }

    @Unroll
    def "Successful fast response"() {
        setup:
        def factory = new LoggingFactory(new LoggingOptions(2000))

        def logger = getMockLogger(logLevel)
        def requestPolicyOptions = new RequestPolicyOptions(logger)
        /*
        By mocking a policy, we can simply call sendAsync on the policy under test directly instead of having to
        construct a pipeline
         */
        def mockDownstream = Mock(RequestPolicy) {
            sendAsync(_) >> Single.just(getStubResponse(200))
        }

        def policy = factory.create(mockDownstream, requestPolicyOptions)

        when:
        policy.sendAsync(new HttpRequest(null, null, null, null)).blockingGet()

        then:
        /*
        logCount1 * <method> means that we expect this method to be called with these parameters logCount1 number of
        times. '_' means we don't care what the value of that parameter is, so in both of these cases, we are specifying
        that log should be called with HttpPipelineLogLevel.INFO as the first argument, and the other arguments can
        be anything. The '>>' operator allows us to specify some behavior on the mocked logger when this method is
        called. Because there is lots of string formatting going on, we can't match against the log string in the
        argument list, so we perform some logic to see if it looks correct and throw if it looks incorrect to actually
        validate the logging behavior.
         */
        logCount1 * logger.log(HttpPipelineLogLevel.INFO, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!message.contains("OUTGOING REQUEST")) {
                        throw new IllegalArgumentException(message)
                    }
                }
        logCount2 * logger.log(HttpPipelineLogLevel.INFO, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!(message.contains("Success") && message.contains("Request try"))) {
                        throw new IllegalArgumentException(message)
                    }
                }
        0 * logger.log(HttpPipelineLogLevel.WARNING, _, _)
        0 * logger.log(HttpPipelineLogLevel.ERROR, _, _)

        where:
        logLevel                     | logCount1 | logCount2
        HttpPipelineLogLevel.INFO    | 1         | 1
        HttpPipelineLogLevel.WARNING | 0         | 0
        HttpPipelineLogLevel.ERROR   | 0         | 0
    }

    @Unroll
    def "Successful slow response"() {
        setup:
        def factory = new LoggingFactory(new LoggingOptions(500))

        def logger = getMockLogger(logLevel)
        def requestPolicyOptions = new RequestPolicyOptions(logger)
        def mockDownstream = Mock(RequestPolicy) {
            sendAsync(_) >> {
                sleep(600)
                Single.just(getStubResponse(200))
            }
        }

        def policy = factory.create(mockDownstream, requestPolicyOptions)

        when:
        policy.sendAsync(new HttpRequest(null, null, null, null)).blockingGet()

        then:
        logCount * logger.log(HttpPipelineLogLevel.WARNING, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!(message.startsWith("SLOW OPERATION") && message.contains("Request try"))) {
                        throw new IllegalArgumentException(message)
                    }
                }
        0 * logger.log(HttpPipelineLogLevel.ERROR, _, _)

        where:
        logLevel                     | logCount
        HttpPipelineLogLevel.INFO    | 1
        HttpPipelineLogLevel.WARNING | 1
        HttpPipelineLogLevel.ERROR   | 0
    }

    @Unroll
    def "Error response"() {
        setup:
        def factory = new LoggingFactory(new LoggingOptions(2000))

        def logger = getMockLogger(logLevel)
        def requestPolicyOptions = new RequestPolicyOptions(logger)
        def mockDownstream = Mock(RequestPolicy) {
            sendAsync(_) >> Single.just(getStubResponse(code))
        }

        def policy = factory.create(mockDownstream, requestPolicyOptions)

        when:
        policy.sendAsync(new HttpRequest(null, null, null, null)).blockingGet()

        then:
        1 * logger.log(HttpPipelineLogLevel.ERROR, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!message.startsWith("REQUEST ERROR")) {
                        throw new IllegalArgumentException(message)
                    }
                }

        where:
        logLevel                     | code
        HttpPipelineLogLevel.INFO    | 400
        HttpPipelineLogLevel.INFO    | 503
        HttpPipelineLogLevel.WARNING | 400
        HttpPipelineLogLevel.WARNING | 503
        HttpPipelineLogLevel.ERROR   | 400
        HttpPipelineLogLevel.ERROR   | 503
    }

    @Unroll
    def "Error responses expected"() {
        setup:
        def factory = new LoggingFactory(new LoggingOptions(2000))

        def logger = getMockLogger(logLevel)
        def requestPolicyOptions = new RequestPolicyOptions(logger)
        def mockDownstream = Mock(RequestPolicy) {
            sendAsync(_) >> Single.just(getStubResponse(code))
        }

        def policy = factory.create(mockDownstream, requestPolicyOptions)

        when:
        policy.sendAsync(new HttpRequest(null, null, null, null)).blockingGet()

        then:
        /*
        Because all of these "error" responses are potentially expected (perhaps from a createIfNotExist call), we
        don't want to say they are errors in the log. Therefore, we specify that we never log with log level ERROR in
        the case of these status codes.
         */
        0 * logger.log(HttpPipelineLogLevel.ERROR, _, _)

        /*
        Note that these where-tables usually have a column of '_' if we only need to test one variable. However, because
        '_' is used in some cases to specify method parameter behavior, the overload becomes confusing both for the
        reader and the IDE, so we just specify an extra variable that is constant.
         */
        where:
        logLevel                  | code
        HttpPipelineLogLevel.INFO | 404
        HttpPipelineLogLevel.INFO | 416
        HttpPipelineLogLevel.INFO | 412
        HttpPipelineLogLevel.INFO | 409
    }

    @Unroll
    def "Network error"() {
        setup:
        def factory = new LoggingFactory(new LoggingOptions(duration))

        def logger = getMockLogger(logLevel)
        def requestPolicyOptions = new RequestPolicyOptions(logger)
        def mockDownstream = Mock(RequestPolicy) {
            sendAsync(_) >> {
                Single.error(new SocketException("Check for me"))
            }
        }

        def policy = factory.create(mockDownstream, requestPolicyOptions)

        when:
        policy.sendAsync(new HttpRequest(null, null, null, null)).blockingGet()

        then:
        thrown(RuntimeException) // Because we return this from the downstream, it will be thrown when we blockingGet.
        1 * logger.log(HttpPipelineLogLevel.ERROR, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!(message.contains("Error message") && message.contains("Check for me"))) {
                        throw new IllegalArgumentException(message)
                    }
                }

        where:
        logLevel                     | duration
        HttpPipelineLogLevel.INFO    | 500
        HttpPipelineLogLevel.WARNING | 500
        HttpPipelineLogLevel.ERROR   | 500
    }

    def "Pipeline integration test"() {
        setup:
        def logger = getMockLogger(HttpPipelineLogLevel.INFO)
        def po = new PipelineOptions()
        po.withLogger(logger)

        cu = primaryServiceURL.createContainerURL(generateContainerName())
        cu = new ContainerURL(cu.toURL(), StorageURL.createPipeline(primaryCreds, po))

        when:
        cu.create(null, null, null).blockingGet()

        then:
        2 * logger.log(*_)
    }
}
