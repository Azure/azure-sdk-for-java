// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.rx.VectorSearchQueryTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceRegressionSuiteTest {
    @Test(groups = {"unit"})
    public void vectorRegressionIsExcludedFromRoutineLiveSuites() throws Exception {
        Test test = VectorSearchQueryTest.class.getMethod("testVectorSearchAcrossEmbeddingTypes")
            .getAnnotation(Test.class);
        assertThat(test.enabled()).isTrue();
        assertThat(test.groups()).containsExactly("service-regression");

        for (String suiteName : Arrays.asList("query", "thinclient", "thinclient-endpoint-probe")) {
            XmlSuite suite = readSuite(suiteName);
            suite.getTests().forEach(xmlTest -> {
                assertThat(xmlTest.getIncludedGroups()).isNotEmpty();
                for (String includedGroup : xmlTest.getIncludedGroups()) {
                    for (String group : test.groups()) {
                        assertThat(Pattern.matches(includedGroup, group))
                            .as("%s must not select the service regression", suiteName)
                            .isFalse();
                    }
                }
            });
        }
    }

    @Test(groups = {"unit"})
    public void dedicatedSuiteSelectsTheRegressionAndRetainsCleanup() throws Exception {
        XmlSuite suite = readSuite("service-regression");
        assertThat(suite.getTests()).hasSize(1);
        assertThat(suite.getTests().get(0).getIncludedGroups()).containsExactly("service-regression");
        assertThat(suite.getTests().get(0).getExcludedGroups()).isEmpty();
        assertThat(suite.getTests().get(0).getXmlPackages())
            .extracting(xmlPackage -> xmlPackage.getName()).containsExactly("com.azure.cosmos.*");
        assertThat(suite.getListeners()).contains(
            CosmosTestResourceJanitor.class.getName(), CosmosNettyLeakDetectorFactory.class.getName());

        BeforeClass setup = VectorSearchQueryTest.class.getMethod("beforeClass").getAnnotation(BeforeClass.class);
        assertThat(setup.groups()).containsExactly("service-regression");
        AfterClass cleanup = VectorSearchQueryTest.class.getMethod("afterClass").getAnnotation(AfterClass.class);
        assertThat(cleanup.groups()).containsExactly("service-regression");
        assertThat(cleanup.alwaysRun()).isTrue();
    }

    private static XmlSuite readSuite(String name) throws Exception {
        try (InputStream stream = ServiceRegressionSuiteTest.class.getClassLoader()
            .getResourceAsStream(name + "-testng.xml")) {
            assertThat(stream).as("TestNG suite %s", name).isNotNull();
            List<XmlSuite> suites = new Parser(stream).parseToList();
            assertThat(suites).hasSize(1);
            return suites.get(0);
        }
    }
}
