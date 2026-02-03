// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.provider.Arguments;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

public class TestUtils {

    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients}
     * that should be tested.
     *
     * @return A stream of HttpClients to test.
     */
    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(
            httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V2025_05_15_PREVIEW)));
        return argumentsList.stream();
    }

    /**
     * Gets the path to a file in the test resources folder.
     *
     * @param fileName the name of the file in the test resources folder
     * @return Path to the test resource file
     * @throws RuntimeException if the resource file is not found
     */
    public static Path getTestResourcePath(String fileName) {
        try {
            URL resourceUrl = TestUtils.class.getClassLoader().getResource(fileName);
            if (resourceUrl == null) {
                throw new RuntimeException("Test resource file not found: " + fileName);
            }
            return Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI for test resource: " + fileName, e);
        }
    }

    /**
     * Gets the path to a file in the test resources folder as a string.
     *
     * @param fileName the name of the file in the test resources folder
     * @return String path to the test resource file
     */
    public static String getTestResourcePathString(String fileName) {
        return getTestResourcePath(fileName).toString();
    }

    /**
     * Gets BinaryData from a test resource file.
     *
     * @param fileName the name of the file in the test resources folder
     * @return BinaryData containing the file contents
     */
    public static BinaryData getTestResourceAsBinaryData(String fileName) {
        Path filePath = getTestResourcePath(fileName);
        return BinaryData.fromFile(filePath);
    }

    /**
     * Gets a map for begin workflow configuration from a test resource file.
     *
     * @param fileName the name of the workflow JSON file in test resources
     * @return Map containing the workflow configuration
     */
    public static Map<String, BinaryData> getBeginWorkflowMap(String fileName) {
        Map<String, BinaryData> workflowMap = new HashMap<>();
        workflowMap.put("TriggerBytes", getTestResourceAsBinaryData(fileName));
        return workflowMap;
    }
}
