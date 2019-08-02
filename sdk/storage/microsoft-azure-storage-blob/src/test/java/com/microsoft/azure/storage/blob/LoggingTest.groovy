// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob

import com.microsoft.azure.storage.APISpec
import com.microsoft.rest.v2.http.HttpHeaders
import com.microsoft.rest.v2.http.HttpMethod
import com.microsoft.rest.v2.http.HttpPipelineLogLevel
import com.microsoft.rest.v2.http.HttpPipelineLogger
import com.microsoft.rest.v2.http.HttpRequest
import com.microsoft.rest.v2.policy.RequestPolicy
import com.microsoft.rest.v2.policy.RequestPolicyOptions
import io.reactivex.Single
import org.slf4j.LoggerFactory
import spock.lang.Unroll
import uk.org.lidalia.slf4jtest.TestLogger
import uk.org.lidalia.slf4jtest.TestLoggerFactory

import java.util.logging.Logger

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


    def setupSpec() {
    }

    /*
    Clean out the logs directory so we can validate that it grows, which is how we test default logging. We only
    need to do this once per test past rather than per test, and we don't have to be entirely successful. This should
    just keep it from growing too large.
     */
    def cleanupSpec() {
        File logsDir = new File(System.getProperty("java.io.tmpdir") + "AzureStorageJavaSDKLogs")
        for (File file : logsDir.listFiles()) {
            file.delete()
        }
    }

    /*
    We test that default logging is on by checking that the size of the logging folder has grown in Warning and Error
    cases when we expect default logging. We cannot check a specific file because we have no way of retrieving the
    filename, and there is some randomness involved. We can rely on a fairly naive implementation of this method
    as we know the directory will exist and that there will be no subdirectories.
     */
    def calculateLogsDirectorySize() {
        File logsDir = new File(System.getProperty("java.io.tmpdir") + "AzureStorageJavaSDKLogs")
        long length = 0

        for (File file : logsDir.listFiles()){
            length += file.size()
        }
        return length
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
        def logDirectorySize = calculateLogsDirectorySize()
        def slf4jLogger = TestLoggerFactory.getTestLogger(LoggingFactory.class.getName())
        slf4jLogger.clearAll()

        when:
        policy.sendAsync(getMockRequest()).blockingGet()

        then:
        /*
        logCount1 * <method> means that we expect this method to be called with these parameters logCount1 number of
        times. '_' means we don't care what the value of that parameter is, so in both of these cases, we are specifying
        that log should be called with HttpPipelineLogLevel.INFO as the first argument, and the other arguments can
        be anything. The '>>' operator allows us to specify some behavior on the mocked FORCE_LOGGER when this method is
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
        logDirectorySize == calculateLogsDirectorySize()
        slf4jLogger.getAllLoggingEvents().size() == 2 // The slf4j test logger is always set to info.

        where:
        logLevel                     | logCount1 | logCount2
        HttpPipelineLogLevel.INFO    | 1         | 1
        HttpPipelineLogLevel.WARNING | 0         | 0
        HttpPipelineLogLevel.ERROR   | 0         | 0
    }

    @Unroll
    def "Successful slow response"() {
        setup:
        def factory = new LoggingFactory(new LoggingOptions(500, disableDefault))

        def logger = getMockLogger(logLevel)
        def requestPolicyOptions = new RequestPolicyOptions(logger)
        def mockDownstream = Mock(RequestPolicy) {
            sendAsync(_) >> {
                sleep(600)
                Single.just(getStubResponse(200))
            }
        }

        def policy = factory.create(mockDownstream, requestPolicyOptions)
        int logDirectorySize = calculateLogsDirectorySize()
        def slf4jLogger = TestLoggerFactory.getTestLogger(LoggingFactory.class.getName())
        slf4jLogger.clearAll()

        when:
        policy.sendAsync(getMockRequest()).blockingGet()

        then:
        logCount * logger.log(HttpPipelineLogLevel.WARNING, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!(message.startsWith("SLOW OPERATION") && message.contains("Request try"))) {
                        throw new IllegalArgumentException(message)
                    }
                }
        0 * logger.log(HttpPipelineLogLevel.ERROR, _, _)
        calculateLogsDirectorySize().compareTo(logDirectorySize) == result
        slf4jLogger.getAllLoggingEvents().size() == 2 // The slf4j test logger is always set to info.

        where:
        logLevel                     | logCount | disableDefault || result
        HttpPipelineLogLevel.INFO    | 1        | false          || 1
        HttpPipelineLogLevel.WARNING | 1        | true           || 0
        HttpPipelineLogLevel.ERROR   | 0        | false          || 1
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
        def logDirectorySize = calculateLogsDirectorySize()
        def slf4jLogger = TestLoggerFactory.getTestLogger(LoggingFactory.class.getName())
        slf4jLogger.clearAll()

        when:
        policy.sendAsync(getMockRequest()).blockingGet()

        then:
        1 * logger.log(HttpPipelineLogLevel.ERROR, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!message.startsWith("REQUEST ERROR")) {
                        throw new IllegalArgumentException(message)
                    }
                }
        calculateLogsDirectorySize() > logDirectorySize
        slf4jLogger.getAllLoggingEvents().size() == 2 // The slf4j test logger is always set to info.

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
        def logDirectorySize = calculateLogsDirectorySize()
        def slf4jLogger = TestLoggerFactory.getTestLogger(LoggingFactory.class.getName())
        slf4jLogger.clearAll()

        when:
        policy.sendAsync(getMockRequest()).blockingGet()

        then:
        /*
        Because all of these "error" responses are potentially expected (perhaps from a createIfNotExist call), we
        don't want to say they are errors in the log. Therefore, we specify that we never log with log level ERROR in
        the case of these status codes.
         */
        0 * logger.log(HttpPipelineLogLevel.ERROR, _, _)
        calculateLogsDirectorySize() == logDirectorySize
        slf4jLogger.getAllLoggingEvents().size() == 2 // The slf4j test logger is always set to info.

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
        def logDirectorySize = calculateLogsDirectorySize()
        def slf4jLogger = TestLoggerFactory.getTestLogger(LoggingFactory.class.getName())
        slf4jLogger.clearAll()

        when:
        policy.sendAsync(getMockRequest()).blockingGet()

        then:
        thrown(RuntimeException) // Because we return this from the downstream, it will be thrown when we blockingGet.
        1 * logger.log(HttpPipelineLogLevel.ERROR, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!(message.contains("Error message") && message.contains("Check for me"))) {
                        throw new IllegalArgumentException(message)
                    }
                }
        calculateLogsDirectorySize() > logDirectorySize
        slf4jLogger.getAllLoggingEvents().size() == 2 // The slf4j test logger is always set to info.

        where:
        logLevel                     | duration
        HttpPipelineLogLevel.INFO    | 500
        HttpPipelineLogLevel.WARNING | 500
        HttpPipelineLogLevel.ERROR   | 500
    }

    /*
    This is a basic test to validate that a basic scenario works in the context of an actual Pipeline.
     */
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

    /*
    This test validates the content of the logs when shared key is used. Note that the Auth header is redacted.
     */
    def "Shared key logs"() {
        setup:
        def factory = new LoggingFactory(new LoggingOptions(2000))

        def logger = getMockLogger(HttpPipelineLogLevel.INFO)
        def requestPolicyOptions = new RequestPolicyOptions(logger)

        def mockDownstream = Mock(RequestPolicy) {
            sendAsync(_) >> Single.just(getStubResponse(200))
        }

        def policy = factory.create(mockDownstream, requestPolicyOptions)

        def userAgentValue = "Azure-Storage/0.1 "
        def authorizationValue = "authorizationValue"
        def dateValue = "Mon, 29 Oct 2018 21:12:12 GMT"
        def requestId = UUID.randomUUID().toString()
        def httpHeaders = new HttpHeaders()
        httpHeaders.set(Constants.HeaderConstants.VERSION, Constants.HeaderConstants.TARGET_STORAGE_VERSION)
        httpHeaders.set(Constants.HeaderConstants.USER_AGENT, userAgentValue)
        httpHeaders.set(Constants.HeaderConstants.AUTHORIZATION, authorizationValue)
        httpHeaders.set(Constants.HeaderConstants.DATE, dateValue)
        httpHeaders.set(Constants.HeaderConstants.CLIENT_REQUEST_ID_HEADER, requestId)
        def urlString = "http://devtest.blob.core.windows.net/test-container/test-blob"
        def url = new URL(urlString)

        when:
        policy.sendAsync(new HttpRequest(null, HttpMethod.HEAD, url, httpHeaders, null, null)).blockingGet()

        then:
        1 * logger.log(HttpPipelineLogLevel.INFO, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!message.contains("OUTGOING REQUEST")) {
                        throw new IllegalArgumentException(message)
                    }
                }
        1 * logger.log(HttpPipelineLogLevel.INFO, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!(message.contains("Success")
                            && message.contains("Request try")
                            && message.contains(HttpMethod.HEAD.toString())
                            && message.contains(urlString)
                            && message.contains(url.toString())
                            && message.contains(Constants.HeaderConstants.VERSION)
                            && message.contains(Constants.HeaderConstants.TARGET_STORAGE_VERSION)
                            && message.contains(Constants.HeaderConstants.DATE)
                            && message.contains(dateValue)
                            && message.contains(Constants.HeaderConstants.CLIENT_REQUEST_ID_HEADER)
                            && message.contains(requestId)
                            && message.contains(Constants.HeaderConstants.USER_AGENT)
                            && message.contains(userAgentValue)
                            && message.contains(Constants.HeaderConstants.AUTHORIZATION)
                            && message.contains(Constants.REDACTED)
                            && !message.contains(authorizationValue))) {
                        throw new IllegalArgumentException(message)
                    }
                }
    }

    /*
    This test validates the contents of the logs when sas is used. Note that the signatures are redacted.
     */
    def "SAS logs"() {
        setup:
        def factory = new LoggingFactory(new LoggingOptions(2000))

        def logger = getMockLogger(HttpPipelineLogLevel.INFO)
        def requestPolicyOptions = new RequestPolicyOptions(logger)

        def mockDownstream = Mock(RequestPolicy) {
            sendAsync(_) >> Single.just(getStubResponse(200))
        }

        def policy = factory.create(mockDownstream, requestPolicyOptions)

        def userAgentValue = "Azure-Storage/0.1 "
        def dateValue = "Mon, 29 Oct 2018 21:12:12 GMT"
        def requestId = UUID.randomUUID().toString()
        def copySource = "http://dev.blob.core.windows.net/test-container/test-blob?snapshot=2018-10-30T19:19:22.1016437Z&sv=2018-03-28&ss=b&srt=co&st=2018-10-29T20:45:11Z&se=2018-10-29T22:45:11Z&sp=rwdlac&sig=copySourceSignature"
        def httpHeaders = new HttpHeaders()
        httpHeaders.set(Constants.HeaderConstants.VERSION, Constants.HeaderConstants.TARGET_STORAGE_VERSION)
        httpHeaders.set(Constants.HeaderConstants.USER_AGENT, userAgentValue)
        httpHeaders.set(Constants.HeaderConstants.DATE, dateValue)
        httpHeaders.set(Constants.HeaderConstants.CLIENT_REQUEST_ID_HEADER, requestId)
        httpHeaders.set(Constants.HeaderConstants.COPY_SOURCE, copySource)
        def urlString = "http://dev.blob.core.windows.net/test-container/test-blob?sv=2018-03-29&ss=f&srt=s&st=2018-10-30T20%3A45%3A11Z&se=2019-10-29T22%3A45%3A11Z&sp=rw&sig=urlSignature&comp=incrementalcopy"
        def url = new URL(urlString)

        when:
        policy.sendAsync(new HttpRequest(null, HttpMethod.PUT, url, httpHeaders, null, null)).blockingGet()

        then:
        1 * logger.log(HttpPipelineLogLevel.INFO, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!message.contains("OUTGOING REQUEST") || message.contains("urlSignature")) {
                        throw new IllegalArgumentException(message)
                    }
                }
        1 * logger.log(HttpPipelineLogLevel.INFO, _, _) >>
                { HttpPipelineLogLevel level, String message, Object[] params ->
                    if (!(message.contains("Success")
                            && message.contains("Request try")
                            && message.contains(HttpMethod.PUT.toString())
                            && message.contains(Constants.HeaderConstants.VERSION)
                            && message.contains(Constants.HeaderConstants.TARGET_STORAGE_VERSION)
                            && message.contains(Constants.HeaderConstants.DATE)
                            && message.contains(dateValue)
                            && message.contains(Constants.HeaderConstants.CLIENT_REQUEST_ID_HEADER)
                            && message.contains(requestId)
                            && message.contains(Constants.HeaderConstants.USER_AGENT)
                            && message.contains(userAgentValue)

                            // SAS URL parameters
                            && message.contains("sv=2018-03-29")
                            && message.contains("ss=f")
                            && message.contains("srt=s")
                            && message.contains("st=2018-10-30T20%3A45%3A11Z")
                            && message.contains("se=2019-10-29T22%3A45%3A11Z")
                            && message.contains("sp=rw")
                            && !message.contains("sig=urlSignature")

                            // Copy Source URL parameters
                            && message.contains("sv=2018-03-28")
                            && message.contains("ss=b")
                            && message.contains("srt=co")
                            && message.contains("st=2018-10-29T20%3A45%3A11Z")
                            && message.contains("se=2018-10-29T22%3A45%3A11Z")
                            && message.contains("sp=rwdlac")
                            && message.contains("sig=REDACTED")
                            && !message.contains("copySourceSignature")
                    )) {
                        throw new IllegalArgumentException(message)
                    }
                }
    }
}
