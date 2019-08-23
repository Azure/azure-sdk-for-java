// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.spock

import com.azure.core.http.HttpClient
import com.azure.core.http.ProxyOptions
import com.azure.core.http.netty.implementation.ReactorNettyClientProvider
import com.azure.core.test.InterceptorManager
import com.azure.core.test.TestMode
import com.azure.core.test.utils.TestResourceNamer
import com.azure.core.util.configuration.ConfigurationManager
import com.azure.core.util.logging.ClientLogger
import com.azure.storage.file.DirectoryClientBuilder
import com.azure.storage.file.FileClientBuilder
import com.azure.storage.file.FileServiceAsyncClient
import com.azure.storage.file.FileServiceClient
import com.azure.storage.file.FileServiceClientBuilder
import com.azure.storage.file.ShareClientBuilder
import com.azure.storage.file.models.ListSharesOptions
import spock.lang.Specification

import java.util.function.Supplier

class APISpec extends Specification {
    // Field common used for all APIs.
    def logger = new ClientLogger(APISpec.class)
    def AZURE_TEST_MODE = "AZURE_TEST_MODE"
    def tmpFolder = getClass().getClassLoader().getResource("tmptestfiles")
    def testFolder = getClass().getClassLoader().getResource("testfiles")
    def interceptorManager
    def testResourceName

    // Primary Clients used for API tests
    FileServiceClient primaryFileServiceClient
    FileServiceAsyncClient primaryFileServiceAsyncClient


    // Test name for test method name.
    def methodName
    def testMode = getTestMode()
    def connectionString

    /**
     * Setup the File service clients commonly used for the API tests.
     */
    def setup() {
        String testName = reformat(specificationContext.currentIteration.getName())
        String className = specificationContext.getCurrentSpec().getName()
        methodName = className + testName
        logger.info("Test Mode: {}, Name: {}", testMode, methodName)
        interceptorManager = new InterceptorManager(methodName, testMode)
        testResourceName = new TestResourceNamer(methodName, testMode,
            interceptorManager.getRecordedData())
        if (getTestMode() == TestMode.RECORD) {
            connectionString = ConfigurationManager.getConfiguration().get("AZURE_STORAGE_FILE_CONNECTION_STRING")
        } else {
            connectionString = "DefaultEndpointsProtocol=https;AccountName=teststorage;" +
                "AccountKey=atestaccountkey;EndpointSuffix=core.windows.net"
        }
    }

    /**
     * Clean up the test shares, directories and files for the account.
     */
    def cleanup() {
        interceptorManager.close()
        if (getTestMode() == TestMode.RECORD) {
            FileServiceClient cleanupFileServiceClient = new FileServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
            cleanupFileServiceClient.listShares(new ListSharesOptions().prefix(methodName.toLowerCase())).each {
                cleanupFileServiceClient.deleteShare(it.name())
            }
        }
    }

    /**
     * Test mode is initialized whenever test is executed. Helper method which is used to determine what to do under
     * certain test mode.
     * @return The TestMode:
     * <ul>
     *     <li>Record</li>
     *     <li>Playback: (default if no test mode setup)</li>
     * </ul>
     */
    def getTestMode() {
        def azureTestMode = ConfigurationManager.getConfiguration().get(AZURE_TEST_MODE)

        if (azureTestMode != null) {
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US))
            } catch (IllegalArgumentException e) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode)
                return TestMode.PLAYBACK
            }
        }

        logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", AZURE_TEST_MODE)
        return TestMode.PLAYBACK
    }

    def fileServiceBuilderHelper(final InterceptorManager interceptorManager) {
        if (testMode == TestMode.RECORD) {
            return new FileServiceClientBuilder()
                .connectionString(connectionString)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new FileServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def shareBuilderHelper(final InterceptorManager interceptorManager, final String shareName) {
        if (testMode == TestMode.RECORD) {
            return new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new ShareClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def directoryBuilderHelper(final InterceptorManager interceptorManager, final String shareName, final String directoryPath) {
        if (testMode == TestMode.RECORD) {
            return new DirectoryClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .directoryPath(directoryPath)
                .addPolicy(interceptorManager.getRecordPolicy())
        } else {
            return new DirectoryClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .directoryPath(directoryPath)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    def fileBuilderHelper(final InterceptorManager interceptorManager, final String shareName, final String filePath) {
        if (testMode == TestMode.RECORD) {
            return new FileClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .filePath(filePath)
                .addPolicy(interceptorManager.getRecordPolicy())
                .httpClient(HttpClient.createDefault().wiretap(true).proxy(PROXY_OPTIONS))
        } else {
            return new FileClientBuilder()
                .connectionString(connectionString)
                .shareName(shareName)
                .filePath(filePath)
                .httpClient(interceptorManager.getPlaybackClient())
        }
    }

    private Supplier<ProxyOptions> PROXY_OPTIONS = new Supplier<ProxyOptions>() {
        @Override
        ProxyOptions get() {
            return new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888))
        }
    }

    private def reformat(String text) {
        def fullName = text.split(" ").collect { it.capitalize() }.join("")
        def matcher = (fullName =~ /(.*)(\[)(.*)(\])/)

        if (!matcher.find()) {
            return fullName
        }
        return matcher[0][1] + matcher[0][3]
    }
}
