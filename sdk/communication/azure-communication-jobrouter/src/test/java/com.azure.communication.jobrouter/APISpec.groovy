package com.azure.communication.jobrouter

import com.azure.core.test.InterceptorManager
import com.azure.core.util.logging.ClientLogger
import spock.lang.Specification
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

@Timeout(value = 5, unit = TimeUnit.MINUTES)
class APISpec extends Specification {
    private static final ClientLogger LOGGER = new ClientLogger(APISpec.class)
    private static final TestEnvironment ENVIRONMENT = new TestEnvironment()

    private InterceptorManager interceptorManager

    RouterClient jrc

    def setup() {
        def testName = TestNameProvider.getTestName(specificationContext.getCurrentIteration());
        interceptorManager = new InterceptorManager(testName, ENVIRONMENT.testMode)
        jrc = new RouterClientBuilder()
            .connectionString(ENVIRONMENT.connectionString)
            .buildClient()
    }
}
