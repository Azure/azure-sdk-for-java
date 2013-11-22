/**
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
package com.microsoft.windowsazure.storage;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.microsoft.windowsazure.storage.blob.CloudBlobClient;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.queue.CloudQueueClient;
import com.microsoft.windowsazure.storage.table.CloudTableClient;

@RunWith(Parameterized.class)
public class ServicePropertiesTests extends TestBase {

    private final String clientType;
    private ServiceClient client;
    private ServiceProperties props;

    /**
     * These parameters are passed to the constructor at the start of each test run. These tests will be run once with a
     * BlobClient, once with a QueueClient and once with a TableClient
     * 
     * @return the type of ServiceClient with which to run the tests
     * @throws URISyntaxException
     */
    @Parameters
    public static Collection<Object> data() throws URISyntaxException {

        return Arrays
                .asList(new Object[] { new String[] { "blob" }, new String[] { "queue" }, new String[] { "table" } });
    }

    /**
     * Takes a parameter from @Parameters to use for this run of the tests. Based on its value, creates a new Service
     * Client to use for this test run.
     * 
     * @param clientType
     *            the type of the ServiceClient to use, as specified in @Parameters
     */
    public ServicePropertiesTests(String clientType) {
        this.clientType = clientType;
        if (clientType.equals("blob")) {
            client = createCloudBlobClient();
        }
        else if (clientType.equals("queue")) {
            client = createCloudQueueClient();
        }
        else if (clientType.equals("table")) {
            client = createCloudTableClient();
        }
        else {
            client = null;
        }
    }

    /**
     * Resets the Service Properties to defaults before each test
     * 
     * @throws StorageException
     */
    @Before
    public void methodSetup() throws StorageException {
        if (clientType.equals("blob")) {
            props = new ServiceProperties();
            props.setDefaultServiceVersion("2013-08-15");
        }
        else if (clientType.equals("queue")) {
            props = new ServiceProperties();
        }
        else if (clientType.equals("table")) {
            props = new ServiceProperties();
        }
        else {
            client = null;
        }
    }

    /**
     * Test Analytics Disable Service Properties
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsDisable() throws StorageException, InterruptedException {
        props.getLogging().setLogOperationTypes(EnumSet.noneOf(LoggingOperations.class));
        props.getLogging().setRetentionIntervalInDays(null);
        props.getLogging().setVersion("1.0");

        props.getHourMetrics().setMetricsLevel(MetricsLevel.DISABLED);
        props.getHourMetrics().setRetentionIntervalInDays(null);
        props.getHourMetrics().setVersion("1.0");

        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.DISABLED);
        props.getMinuteMetrics().setRetentionIntervalInDays(null);
        props.getMinuteMetrics().setVersion("1.0");

        props.getCors().getCorsRules().clear();

        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
    }

    /**
     * Test Analytics Default Service Version
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsDefaultServiceVersion() throws StorageException, InterruptedException {
        if (clientType.equals("blob")) {
            props.setDefaultServiceVersion("2009-09-19");
            callUploadServiceProps(client, props, clientType);

            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

            props.setDefaultServiceVersion("2011-08-18");
            callUploadServiceProps(client, props, clientType);

            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

            props.setDefaultServiceVersion("2012-02-12");
            callUploadServiceProps(client, props, clientType);

            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

            props.setDefaultServiceVersion("2013-08-15");
            callUploadServiceProps(client, props, clientType);

            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
        }
        else {
            try {
                props.setDefaultServiceVersion("2009-09-19");
                callUploadServiceProps(client, props, clientType);
                fail("Should not be able to set default Service Version for non Blob Client");
            }
            catch (IllegalArgumentException e) {
                assertEquals(e.getMessage(), SR.DEFAULT_SERVICE_VERSION_ONLY_SET_FOR_BLOB_SERVICE);
            }
            catch (Exception e) {
                fail();
            }
        }
    }

    private void callUploadServiceProps(ServiceClient client, ServiceProperties props, String clientType)
            throws StorageException, InterruptedException {
        if (clientType.equals("blob")) {
            CloudBlobClient blobClient = (CloudBlobClient) client;
            blobClient.uploadServiceProperties(props);
            Thread.sleep(30000);
        }
        else if (clientType.equals("table")) {
            CloudTableClient tableClient = (CloudTableClient) client;
            tableClient.uploadServiceProperties(props);
            Thread.sleep(30000);
        }
        else if (clientType.equals("queue")) {
            CloudQueueClient queueClient = (CloudQueueClient) client;
            queueClient.uploadServiceProperties(props);
            Thread.sleep(30000);
        }
        else {
            fail();
        }
    }

    private ServiceProperties callDownloadServiceProperties(ServiceClient client, String clientType)
            throws StorageException {
        if (clientType.equals("blob")) {
            CloudBlobClient blobClient = (CloudBlobClient) client;
            return blobClient.downloadServiceProperties();
        }
        else if (clientType.equals("table")) {
            CloudTableClient tableClient = (CloudTableClient) client;
            return tableClient.downloadServiceProperties();
        }
        else if (clientType.equals("queue")) {
            CloudQueueClient queueClient = (CloudQueueClient) client;
            return queueClient.downloadServiceProperties();
        }
        else {
            fail();
        }
        return null;
    }

    /**
     * Test Analytics Logging Operations
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsLoggingOperations() throws StorageException, InterruptedException {
        // None
        props.getLogging().setLogOperationTypes(EnumSet.noneOf(LoggingOperations.class));
        props.getLogging().setRetentionIntervalInDays(null);
        props.getLogging().setVersion("1.0");

        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // None
        props.getLogging().setLogOperationTypes(EnumSet.allOf(LoggingOperations.class));
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
    }

    /**
     * Test Analytics Hour Metrics Level
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsMetricsLevel() throws StorageException, InterruptedException {
        // None
        props.getHourMetrics().setMetricsLevel(MetricsLevel.DISABLED);
        props.getHourMetrics().setRetentionIntervalInDays(null);
        props.getHourMetrics().setVersion("1.0");
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // Service
        props.getHourMetrics().setMetricsLevel(MetricsLevel.SERVICE);
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // ServiceAndAPI
        props.getHourMetrics().setMetricsLevel(MetricsLevel.SERVICE_AND_API);
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
    }

    /**
     * Test Analytics Minute Metrics Level
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    public void testAnalyticsMinuteMetricsLevel() throws StorageException, InterruptedException {
        // None
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.DISABLED);
        props.getMinuteMetrics().setRetentionIntervalInDays(null);
        props.getMinuteMetrics().setVersion("1.0");
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // Service
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.SERVICE);
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // ServiceAndAPI
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.SERVICE_AND_API);
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
    }

    /**
     * Test Analytics Retention Policies
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsRetentionPolicies() throws StorageException, InterruptedException {
        // Set retention policy null with metrics disabled.
        props.getHourMetrics().setMetricsLevel(MetricsLevel.DISABLED);
        props.getHourMetrics().setRetentionIntervalInDays(null);
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.DISABLED);
        props.getMinuteMetrics().setRetentionIntervalInDays(null);
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // Set retention policy not null with metrics enabled.
        props.getHourMetrics().setRetentionIntervalInDays(1);
        props.getHourMetrics().setMetricsLevel(MetricsLevel.SERVICE);
        props.getMinuteMetrics().setRetentionIntervalInDays(1);
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.SERVICE);
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // Set retention policy not null with metrics enabled.
        props.getHourMetrics().setRetentionIntervalInDays(2);
        props.getHourMetrics().setMetricsLevel(MetricsLevel.SERVICE_AND_API);
        props.getMinuteMetrics().setRetentionIntervalInDays(2);
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.SERVICE_AND_API);
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // Set retention policy null with logging disabled.
        props.getLogging().setRetentionIntervalInDays(null);
        props.getLogging().setLogOperationTypes(EnumSet.noneOf(LoggingOperations.class));
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // Set retention policy not null with logging disabled.
        props.getLogging().setRetentionIntervalInDays(3);
        props.getLogging().setLogOperationTypes(EnumSet.noneOf(LoggingOperations.class));
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // Set retention policy null with logging enabled.
        props.getLogging().setRetentionIntervalInDays(null);
        props.getLogging().setLogOperationTypes(EnumSet.allOf(LoggingOperations.class));
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        // Set retention policy not null with logging enabled.
        props.getLogging().setRetentionIntervalInDays(4);
        props.getLogging().setLogOperationTypes(EnumSet.allOf(LoggingOperations.class));
        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
    }

    /**
     * Test CORS with different rules.
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testCloudValidCorsRules() throws StorageException, InterruptedException {
        CorsRule ruleMinRequired = new CorsRule();
        ruleMinRequired.getAllowedOrigins().add("www.xyz.com");
        ruleMinRequired.getAllowedMethods().add(CorsHttpMethods.GET);

        final CorsRule ruleBasic = new CorsRule();
        ruleBasic.getAllowedOrigins().addAll(Arrays.asList("www.ab.com", "www.bc.com"));
        ruleBasic.getAllowedMethods().addAll(EnumSet.of(CorsHttpMethods.GET, CorsHttpMethods.PUT));
        ruleBasic.getAllowedHeaders().addAll(
                Arrays.asList("x-ms-meta-data*", "x-ms-meta-target*", "x-ms-meta-xyz", "x-ms-meta-foo"));
        ruleBasic.getExposedHeaders().addAll(
                Arrays.asList("x-ms-meta-data*", "x-ms-meta-source*", "x-ms-meta-abc", "x-ms-meta-bcd"));
        ruleBasic.setMaxAgeInSeconds(500);

        CorsRule ruleAllMethods = new CorsRule();
        ruleAllMethods.getAllowedOrigins().addAll(Arrays.asList("www.ab.com", "www.bc.com"));
        ruleAllMethods.getAllowedMethods().addAll(EnumSet.allOf(CorsHttpMethods.class));

        CorsRule ruleSingleExposedHeader = new CorsRule();
        ruleSingleExposedHeader.getAllowedOrigins().add("www.ab.com");
        ruleSingleExposedHeader.getAllowedMethods().add(CorsHttpMethods.GET);
        ruleSingleExposedHeader.getExposedHeaders().add("x-ms-meta-bcd");

        CorsRule ruleSingleExposedPrefixHeader = new CorsRule();
        ruleSingleExposedPrefixHeader.getAllowedOrigins().add("www.ab.com");
        ruleSingleExposedPrefixHeader.getAllowedMethods().add(CorsHttpMethods.GET);
        ruleSingleExposedPrefixHeader.getExposedHeaders().add("x-ms-meta-data*");

        CorsRule ruleSingleAllowedHeader = new CorsRule();
        ruleSingleAllowedHeader.getAllowedOrigins().add("www.ab.com");
        ruleSingleAllowedHeader.getAllowedMethods().add(CorsHttpMethods.GET);
        ruleSingleAllowedHeader.getAllowedHeaders().add("x-ms-meta-xyz");

        CorsRule ruleSingleAllowedPrefixHeader = new CorsRule();
        ruleSingleAllowedPrefixHeader.getAllowedOrigins().add("www.ab.com");
        ruleSingleAllowedPrefixHeader.getAllowedMethods().add(CorsHttpMethods.GET);
        ruleSingleAllowedPrefixHeader.getAllowedHeaders().add("x-ms-meta-target*");

        CorsRule ruleAllowAll = new CorsRule();
        ruleAllowAll.getAllowedOrigins().add("*");
        ruleAllowAll.getAllowedMethods().add(CorsHttpMethods.GET);
        ruleAllowAll.getAllowedHeaders().add("*");
        ruleAllowAll.getExposedHeaders().add("*");

        this.testCorsRules(ruleBasic);

        this.testCorsRules(ruleMinRequired);

        this.testCorsRules(ruleAllMethods);

        this.testCorsRules(ruleSingleExposedHeader);

        this.testCorsRules(ruleSingleExposedPrefixHeader);

        this.testCorsRules(ruleSingleAllowedHeader);

        this.testCorsRules(ruleSingleAllowedPrefixHeader);

        this.testCorsRules(ruleAllowAll);

        List<CorsRule> testList = new ArrayList<CorsRule>();

        // Empty rule set should delete all rules
        this.testCorsRules(testList);

        // Test duplicate rules
        testList.add(ruleBasic);
        testList.add(ruleBasic);
        this.testCorsRules(testList);

        // Test max number of  rules (five)
        testList.clear();
        testList.add(ruleBasic);
        testList.add(ruleMinRequired);
        testList.add(ruleAllMethods);
        testList.add(ruleSingleExposedHeader);
        testList.add(ruleSingleExposedPrefixHeader);
        this.testCorsRules(testList);

        // Test max number of  rules (six)
        testList.clear();
        testList.add(ruleBasic);
        testList.add(ruleMinRequired);
        testList.add(ruleAllMethods);
        testList.add(ruleSingleExposedHeader);
        testList.add(ruleSingleExposedPrefixHeader);
        testList.add(ruleSingleAllowedHeader);

        try {
            this.testCorsRules(testList);
            fail("Expecting exception but no exception received. Services are limited to a maximum of five CORS rules.");
        }
        catch (StorageException e) {
        }
        catch (Exception e) {
            fail("Invalid exception " + e + " received when expecting StorageException");
        }
    }

    /**
     * Test CORS with invalid values.
     */
    @Test
    public void testCorsExpectedExceptions() {
        CorsRule ruleEmpty = new CorsRule();

        CorsRule ruleInvalidMaxAge = new CorsRule();
        ruleInvalidMaxAge.getAllowedOrigins().add("www.xyz.com");
        ruleInvalidMaxAge.getAllowedMethods().add(CorsHttpMethods.GET);
        ruleInvalidMaxAge.setMaxAgeInSeconds(-1);

        try {
            this.testCorsRules(ruleEmpty);
            fail("No exception received. A CORS rule must contain at least one allowed origin and allowed method.");
        }
        catch (StorageException e) {
            assertEquals(e.getCause().getClass(), IllegalArgumentException.class);
            assertEquals(e.getCause().getMessage(),
                    "A CORS rule must contain at least one allowed origin and allowed method, and "
                            + "MaxAgeInSeconds cannot have a value less than zero.");
        }
        catch (Exception e) {
            fail("Invalid exception " + e.getClass() + " received when expecting StorageException");
        }

        try {
            this.testCorsRules(ruleInvalidMaxAge);
            fail("No exception received. MaxAgeInSeconds cannot have a value less than 0.");
        }
        catch (StorageException e) {
            assertEquals(e.getCause().getClass(), IllegalArgumentException.class);
            assertEquals(e.getCause().getMessage(),
                    "A CORS rule must contain at least one allowed origin and allowed method, and "
                            + "MaxAgeInSeconds cannot have a value less than zero.");
        }
        catch (Exception e) {
            fail("Invalid exception " + e + " received when expecting StorageException");
        }
    }

    /**
     * Test CORS with a valid and invalid number of origin values sent to server.
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testCorsMaxOrigins() throws StorageException, InterruptedException {
        CorsRule ruleManyOrigins = new CorsRule();
        ruleManyOrigins.getAllowedMethods().add(CorsHttpMethods.GET);

        // Add maximum number of allowed origins
        for (int i = 0; i < 64; i++) {
            ruleManyOrigins.getAllowedOrigins().add("www.xyz" + i + ".com");
        }

        this.testCorsRules(ruleManyOrigins);

        ruleManyOrigins.getAllowedOrigins().add("www.xyz64.com");

        try {
            this.testCorsRules(ruleManyOrigins);
            fail("No exception received. A maximum of 64 origins are allowed.");
        }
        catch (StorageException e) {
        }
        catch (Exception e) {
            fail("Invalid exception " + e + " received when expecting StorageException");
        }
    }

    /**
     * Test CORS with a valid and invalid number of header values sent to server.
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testCorsMaxHeaders() throws StorageException, InterruptedException {
        CorsRule ruleManyHeaders = new CorsRule();
        ruleManyHeaders.getAllowedOrigins().add("www.xyz.com");
        ruleManyHeaders.getAllowedMethods().add(CorsHttpMethods.GET);
        ruleManyHeaders.getAllowedHeaders().addAll(Arrays.asList("x-ms-meta-target*", "x-ms-meta-other*"));
        ruleManyHeaders.getExposedHeaders().addAll(Arrays.asList("x-ms-meta-data*", "x-ms-meta-source*"));

        // Add maximum number of non-prefixed headers
        for (int i = 0; i < 64; i++) {
            ruleManyHeaders.getAllowedHeaders().add("x-ms-meta-" + i);
            ruleManyHeaders.getExposedHeaders().add("x-ms-meta-" + i);
        }

        this.testCorsRules(ruleManyHeaders);

        // Test with too many Exposed Headers (65)
        ruleManyHeaders.getExposedHeaders().add("x-ms-meta-toomany");

        try {
            this.testCorsRules(ruleManyHeaders);
            fail("No exception received. A maximum of 64 exposed headers are allowed.");
        }
        catch (StorageException e) {
        }
        catch (Exception e) {
            fail("Invalid exception " + e + " received when expecting StorageException");
        }

        ruleManyHeaders.getExposedHeaders().remove("x-ms-meta-toomany");

        // Test with too many Allowed Headers (65)
        ruleManyHeaders.getAllowedHeaders().add("x-ms-meta-toomany");

        try {
            this.testCorsRules(ruleManyHeaders);
            fail("No exception received. A maximum of 64 allowed headers are allowed.");
        }
        catch (StorageException e) {
        }
        catch (Exception e) {
            fail("Invalid exception " + e + " received when expecting StorageException");
        }

        ruleManyHeaders.getExposedHeaders().remove("x-ms-meta-toomany");

        // Test with too many Exposed Prefixed Headers (three)
        ruleManyHeaders.getExposedHeaders().add("x-ms-meta-toomany*");

        try {
            this.testCorsRules(ruleManyHeaders);
            fail("No exception received. A maximum of 2 exposed headers are allowed.");
        }
        catch (StorageException e) {
        }
        catch (Exception e) {
            fail("Invalid exception " + e + " received when expecting StorageException");
        }

        ruleManyHeaders.getExposedHeaders().remove("x-ms-meta-toomany*");

        // Test with too many Allowed Prefixed Headers (three)
        ruleManyHeaders.getAllowedHeaders().add("x-ms-meta-toomany*");

        try {
            this.testCorsRules(ruleManyHeaders);
            fail("No exception received. A maximum of 64 allowed headers are allowed.");
        }
        catch (StorageException e) {
        }
        catch (Exception e) {
            fail("Invalid exception " + e + " received when expecting StorageException");
        }

        ruleManyHeaders.getExposedHeaders().remove("x-ms-meta-toomany*");
    }

    /**
     * Test Optional Service Properties
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testOptionalServiceProperties() throws StorageException, InterruptedException {
        // None
        props.getLogging().setLogOperationTypes(EnumSet.of(LoggingOperations.READ, LoggingOperations.WRITE));
        props.getLogging().setRetentionIntervalInDays(5);
        props.getLogging().setVersion("1.0");

        // None
        props.getHourMetrics().setMetricsLevel(MetricsLevel.SERVICE);
        props.getHourMetrics().setRetentionIntervalInDays(6);
        props.getHourMetrics().setVersion("1.0");

        // None
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.SERVICE);
        props.getMinuteMetrics().setRetentionIntervalInDays(6);
        props.getMinuteMetrics().setVersion("1.0");

        props.getCors().getCorsRules().clear();

        callUploadServiceProps(client, props, clientType);

        ServiceProperties newProps = new ServiceProperties();

        newProps.setLogging(null);
        newProps.setHourMetrics(null);
        newProps.setMinuteMetrics(null);

        final CorsRule ruleBasic = new CorsRule();
        ruleBasic.getAllowedOrigins().addAll(Arrays.asList("www.ab.com", "www.bc.com"));
        ruleBasic.getAllowedMethods().addAll(EnumSet.of(CorsHttpMethods.GET, CorsHttpMethods.PUT));
        ruleBasic.getAllowedHeaders().addAll(
                Arrays.asList("x-ms-meta-data*", "x-ms-meta-target*", "x-ms-meta-xyz", "x-ms-meta-foo"));
        ruleBasic.getExposedHeaders().addAll(
                Arrays.asList("x-ms-meta-data*", "x-ms-meta-source*", "x-ms-meta-abc", "x-ms-meta-bcd"));
        ruleBasic.setMaxAgeInSeconds(500);
        newProps.getCors().getCorsRules().add(ruleBasic);

        callUploadServiceProps(client, newProps, clientType);

        props.setCors(newProps.getCors());
        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));

        newProps.setLogging(props.getLogging());
        newProps.setHourMetrics(props.getHourMetrics());
        newProps.setMinuteMetrics(props.getMinuteMetrics());
        newProps.setCors(null);
        callUploadServiceProps(client, newProps, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
    }

    /**
     * Takes a CorsRule and tries to upload it. Then tries to download it and compares it to the initial CorsRule.
     * 
     * @param rule
     * @throws StorageException
     * @throws InterruptedException
     */
    private void testCorsRules(CorsRule rule) throws StorageException, InterruptedException {
        props.getCors().getCorsRules().clear();
        props.getCors().getCorsRules().add(rule);

        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
    }

    /**
     * Takes a List of CorsRules and tries to upload them. Then tries to download them and compares the list to the
     * initial CorsRule List.
     * 
     * @param rule
     * @throws StorageException
     * @throws InterruptedException
     */
    private void testCorsRules(List<CorsRule> corsRules) throws StorageException, InterruptedException {
        props.getCors().getCorsRules().clear();

        for (CorsRule rule : corsRules) {
            props.getCors().getCorsRules().add(rule);
        }

        callUploadServiceProps(client, props, clientType);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client, clientType));
    }

    /**
     * Checks two ServiceProperties for equality
     * 
     * @param propsA
     * @param propsB
     */
    private static void assertServicePropertiesAreEqual(ServiceProperties propsA, ServiceProperties propsB) {
        if (propsA.getLogging() != null && propsB.getLogging() != null) {
            assertTrue(propsA.getLogging().getLogOperationTypes().equals(propsB.getLogging().getLogOperationTypes()));
            assertEquals(propsA.getLogging().getRetentionIntervalInDays(), propsB.getLogging()
                    .getRetentionIntervalInDays());
            assertEquals(propsA.getLogging().getVersion(), propsB.getLogging().getVersion());
        }
        else {
            assertNull(propsA.getLogging());
            assertNull(propsB.getLogging());
        }

        if (propsA.getHourMetrics() != null && propsB.getHourMetrics() != null) {
            assertTrue(propsA.getHourMetrics().getMetricsLevel().equals(propsB.getHourMetrics().getMetricsLevel()));
            assertEquals(propsA.getHourMetrics().getRetentionIntervalInDays(), propsB.getHourMetrics()
                    .getRetentionIntervalInDays());
            assertEquals(propsA.getHourMetrics().getVersion(), propsB.getHourMetrics().getVersion());
        }
        else {
            assertNull(propsA.getHourMetrics());
            assertNull(propsB.getHourMetrics());
        }

        if (propsA.getMinuteMetrics() != null && propsB.getMinuteMetrics() != null) {
            assertTrue(propsA.getMinuteMetrics().getMetricsLevel().equals(propsB.getMinuteMetrics().getMetricsLevel()));
            assertEquals(propsA.getMinuteMetrics().getRetentionIntervalInDays(), propsB.getMinuteMetrics()
                    .getRetentionIntervalInDays());
            assertEquals(propsA.getMinuteMetrics().getVersion(), propsB.getMinuteMetrics().getVersion());
        }
        else {
            assertNull(propsA.getMinuteMetrics());
            assertNull(propsB.getMinuteMetrics());
        }

        if (propsA.getDefaultServiceVersion() != null && propsB.getDefaultServiceVersion() != null) {
            assertEquals(propsA.getDefaultServiceVersion(), propsB.getDefaultServiceVersion());
        }
        else {
            assertNull(propsA.getDefaultServiceVersion());
            assertNull(propsB.getDefaultServiceVersion());
        }

        if (propsA.getCors() != null && propsB.getCors() != null) {
            assertEquals(propsA.getCors().getCorsRules().size(), propsB.getCors().getCorsRules().size());

            // Check that rules are equal and in the same order.
            for (int i = 0; i < propsA.getCors().getCorsRules().size(); i++) {
                CorsRule ruleA = propsA.getCors().getCorsRules().get(i);
                CorsRule ruleB = propsB.getCors().getCorsRules().get(i);

                assertTrue(ruleA.getAllowedOrigins().size() == ruleB.getAllowedOrigins().size()
                        && ruleA.getAllowedOrigins().containsAll(ruleB.getAllowedOrigins()));

                assertTrue(ruleA.getExposedHeaders().size() == ruleB.getExposedHeaders().size()
                        && ruleA.getExposedHeaders().containsAll(ruleB.getExposedHeaders()));

                assertTrue(ruleA.getAllowedHeaders().size() == ruleB.getAllowedHeaders().size()
                        && ruleA.getAllowedHeaders().containsAll(ruleB.getAllowedHeaders()));

                assertTrue(ruleA.getAllowedMethods().equals(ruleB.getAllowedMethods()));

                assertTrue(ruleA.getMaxAgeInSeconds() == ruleB.getMaxAgeInSeconds());
            }
        }
        else {
            assertNull(propsA.getCors());
            assertNull(propsB.getCors());
        }
    }
}
