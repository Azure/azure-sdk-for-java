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
package com.microsoft.azure.storage;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.TestRunners.SlowTests;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.FileServiceProperties;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.table.CloudTableClient;

@Category({ SlowTests.class, DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class ServicePropertiesTests {

    /**
     * Test Analytics Disable Service Properties
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsDisable() throws StorageException, InterruptedException {
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testAnalyticsDisable(client, props);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testAnalyticsDisable(client, props);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testAnalyticsDisable(client, props);
    }

    private void testAnalyticsDisable(ServiceClient client, ServiceProperties props) throws StorageException,
            InterruptedException {
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

        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
    }

    /**
     * Test Analytics Default Service Version
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsDefaultServiceVersion() throws StorageException, InterruptedException {
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testAnalyticsDefaultServiceVersion(client, props);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testAnalyticsDefaultServiceVersion(client, props);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testAnalyticsDefaultServiceVersion(client, props);
    }

    private void testAnalyticsDefaultServiceVersion(ServiceClient client, ServiceProperties props)
            throws StorageException, InterruptedException {
        if (client.getClass().equals(CloudBlobClient.class)) {
            props.setDefaultServiceVersion("2009-09-19");
            callUploadServiceProps(client, props, null);

            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

            props.setDefaultServiceVersion("2011-08-18");
            callUploadServiceProps(client, props, null);

            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

            props.setDefaultServiceVersion("2012-02-12");
            callUploadServiceProps(client, props, null);

            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

            props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
            callUploadServiceProps(client, props, null);

            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
        }
        else {
            try {
                props.setDefaultServiceVersion("2009-09-19");
                callUploadServiceProps(client, props, null);
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

    /**
     * Test Analytics Logging Operations
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsLoggingOperations() throws StorageException, InterruptedException {
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testAnalyticsLoggingOperations(client, props);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testAnalyticsLoggingOperations(client, props);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testAnalyticsLoggingOperations(client, props);
    }

    private void testAnalyticsLoggingOperations(ServiceClient client, ServiceProperties props) throws StorageException,
            InterruptedException {
        // None
        props.getLogging().setLogOperationTypes(EnumSet.noneOf(LoggingOperations.class));
        props.getLogging().setRetentionIntervalInDays(null);
        props.getLogging().setVersion("1.0");

        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

        // None
        props.getLogging().setLogOperationTypes(EnumSet.allOf(LoggingOperations.class));
        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
    }

    /**
     * Test Analytics Hour Metrics Level
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsHourMetricsLevel() throws StorageException, InterruptedException {
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testAnalyticsHourMetricsLevel(client, props, null);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testAnalyticsHourMetricsLevel(client, props, null);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testAnalyticsHourMetricsLevel(client, props, null);
        
        client = TestHelper.createCloudFileClient();
        FileServiceProperties fileProps = new FileServiceProperties();
        testAnalyticsHourMetricsLevel(client, null, fileProps);
    }

    private void testAnalyticsHourMetricsLevel(
            ServiceClient client, ServiceProperties props, FileServiceProperties fileProps)
            throws StorageException, InterruptedException {

        final MetricsProperties hours = (props == null) ? fileProps.getHourMetrics() : props.getHourMetrics();
        
        // None
        hours.setMetricsLevel(MetricsLevel.DISABLED);
        hours.setRetentionIntervalInDays(null);
        hours.setVersion("1.0");
        callUploadServiceProps(client, props, fileProps);

        if (props == null) {
            assertFileServicePropertiesAreEqual(fileProps, ((CloudFileClient) client).downloadServiceProperties());
        }
        else {
            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
        }

        // Service
        hours.setMetricsLevel(MetricsLevel.SERVICE);
        callUploadServiceProps(client, props, fileProps);

        if (props == null) {
            assertFileServicePropertiesAreEqual(fileProps, ((CloudFileClient) client).downloadServiceProperties());
        }
        else {
            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
        }

        // ServiceAndAPI
        hours.setMetricsLevel(MetricsLevel.SERVICE_AND_API);
        callUploadServiceProps(client, props, fileProps);

        if (props == null) {
            assertFileServicePropertiesAreEqual(fileProps, ((CloudFileClient) client).downloadServiceProperties());
        }
        else {
            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
        }
    }

    /**
     * Test Analytics Minute Metrics Level
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsMinuteMetricsLevel() throws StorageException, InterruptedException {
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testAnalyticsMinuteMetricsLevel(client, props, null);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testAnalyticsMinuteMetricsLevel(client, props, null);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testAnalyticsMinuteMetricsLevel(client, props, null);
        
        client = TestHelper.createCloudFileClient();
        FileServiceProperties fileProps = new FileServiceProperties();
        testAnalyticsMinuteMetricsLevel(client, null, fileProps);
    }

    private void testAnalyticsMinuteMetricsLevel(
            final ServiceClient client, final ServiceProperties props, final FileServiceProperties fileProps)
            throws StorageException, InterruptedException {
        
        final MetricsProperties minutes = (props == null) ? fileProps.getMinuteMetrics() : props.getMinuteMetrics();
        
        // None
        minutes.setMetricsLevel(MetricsLevel.DISABLED);
        minutes.setRetentionIntervalInDays(null);
        minutes.setVersion("1.0");
        callUploadServiceProps(client, props, fileProps);

        if (props == null) {
            assertFileServicePropertiesAreEqual(fileProps, ((CloudFileClient) client).downloadServiceProperties());
        }
        else {
            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
        }

        // Service
        minutes.setMetricsLevel(MetricsLevel.SERVICE);
        callUploadServiceProps(client, props, fileProps);

        if (props == null) {
            assertFileServicePropertiesAreEqual(fileProps, ((CloudFileClient) client).downloadServiceProperties());
        }
        else {
            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
        }

        // ServiceAndAPI
        minutes.setMetricsLevel(MetricsLevel.SERVICE_AND_API);
        callUploadServiceProps(client, props, fileProps);

        if (props == null) {
            assertFileServicePropertiesAreEqual(fileProps, ((CloudFileClient) client).downloadServiceProperties());
        }
        else {
            assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
        }
    }

    /**
     * Test Analytics Retention Policies
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testAnalyticsRetentionPolicies() throws StorageException, InterruptedException {
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testAnalyticsRetentionPolicies(client, props);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testAnalyticsRetentionPolicies(client, props);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testAnalyticsRetentionPolicies(client, props);
    }

    private void testAnalyticsRetentionPolicies(ServiceClient client, ServiceProperties props) throws StorageException,
            InterruptedException {
        // Set retention policy null with metrics disabled.
        props.getHourMetrics().setMetricsLevel(MetricsLevel.DISABLED);
        props.getHourMetrics().setRetentionIntervalInDays(null);
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.DISABLED);
        props.getMinuteMetrics().setRetentionIntervalInDays(null);
        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

        // Set retention policy not null with metrics enabled.
        props.getHourMetrics().setRetentionIntervalInDays(1);
        props.getHourMetrics().setMetricsLevel(MetricsLevel.SERVICE);
        props.getMinuteMetrics().setRetentionIntervalInDays(1);
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.SERVICE);
        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

        // Set retention policy not null with metrics enabled.
        props.getHourMetrics().setRetentionIntervalInDays(2);
        props.getHourMetrics().setMetricsLevel(MetricsLevel.SERVICE_AND_API);
        props.getMinuteMetrics().setRetentionIntervalInDays(2);
        props.getMinuteMetrics().setMetricsLevel(MetricsLevel.SERVICE_AND_API);
        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

        // Set retention policy null with logging disabled.
        props.getLogging().setRetentionIntervalInDays(null);
        props.getLogging().setLogOperationTypes(EnumSet.noneOf(LoggingOperations.class));
        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

        // Set retention policy not null with logging disabled.
        props.getLogging().setRetentionIntervalInDays(3);
        props.getLogging().setLogOperationTypes(EnumSet.noneOf(LoggingOperations.class));
        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

        // Set retention policy null with logging enabled.
        props.getLogging().setRetentionIntervalInDays(null);
        props.getLogging().setLogOperationTypes(EnumSet.allOf(LoggingOperations.class));
        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

        // Set retention policy not null with logging enabled.
        props.getLogging().setRetentionIntervalInDays(4);
        props.getLogging().setLogOperationTypes(EnumSet.allOf(LoggingOperations.class));
        callUploadServiceProps(client, props, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
    }

    /**
     * Test CORS with different rules.
     * 
     * @throws StorageException
     * @throws InterruptedException
     */
    @Test
    public void testCloudValidCorsRules() throws StorageException, InterruptedException {
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testCloudValidCorsRules(client, props, null);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testCloudValidCorsRules(client, props, null);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testCloudValidCorsRules(client, props, null);
        
        client = TestHelper.createCloudFileClient();
        testCloudValidCorsRules(client, null, new FileServiceProperties());
    }

    private void testCloudValidCorsRules(
            ServiceClient client, ServiceProperties props, FileServiceProperties fileServiceProperties)
            throws StorageException, InterruptedException {
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

        this.testCorsRules(ruleBasic, client, props, fileServiceProperties);

        this.testCorsRules(ruleMinRequired, client, props, fileServiceProperties);

        this.testCorsRules(ruleAllMethods, client, props, fileServiceProperties);

        this.testCorsRules(ruleSingleExposedHeader, client, props, fileServiceProperties);

        this.testCorsRules(ruleSingleExposedPrefixHeader, client, props, fileServiceProperties);

        this.testCorsRules(ruleSingleAllowedHeader, client, props, fileServiceProperties);

        this.testCorsRules(ruleSingleAllowedPrefixHeader, client, props, fileServiceProperties);

        this.testCorsRules(ruleAllowAll, client, props, fileServiceProperties);

        List<CorsRule> testList = new ArrayList<CorsRule>();

        // Empty rule set should delete all rules
        this.testCorsRules(testList, client, props, fileServiceProperties);

        // Test duplicate rules
        testList.add(ruleBasic);
        testList.add(ruleBasic);
        this.testCorsRules(testList, client, props, fileServiceProperties);

        // Test max number of rules (five)
        testList.clear();
        testList.add(ruleBasic);
        testList.add(ruleMinRequired);
        testList.add(ruleAllMethods);
        testList.add(ruleSingleExposedHeader);
        testList.add(ruleSingleExposedPrefixHeader);
        this.testCorsRules(testList, client, props, fileServiceProperties);

        // Test over max number of rules (six)
        testList.clear();
        testList.add(ruleBasic);
        testList.add(ruleMinRequired);
        testList.add(ruleAllMethods);
        testList.add(ruleSingleExposedHeader);
        testList.add(ruleSingleExposedPrefixHeader);
        testList.add(ruleSingleAllowedHeader);

        try {
            this.testCorsRules(testList, client, props, fileServiceProperties);
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
    public void testCorsExpectedExceptions() throws StorageException {
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testCorsExpectedExceptions(client, props, null);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testCorsExpectedExceptions(client, props, null);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testCorsExpectedExceptions(client, props, null);
        
        client = TestHelper.createCloudFileClient();
        testCorsExpectedExceptions(client, null, new FileServiceProperties());
    }

    private void testCorsExpectedExceptions(
            ServiceClient client, ServiceProperties props, FileServiceProperties fileServiceProperties) {
        CorsRule ruleEmpty = new CorsRule();

        CorsRule ruleInvalidMaxAge = new CorsRule();
        ruleInvalidMaxAge.getAllowedOrigins().add("www.xyz.com");
        ruleInvalidMaxAge.getAllowedMethods().add(CorsHttpMethods.GET);
        ruleInvalidMaxAge.setMaxAgeInSeconds(-1);

        try {
            this.testCorsRules(ruleEmpty, client, props, fileServiceProperties);
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
            this.testCorsRules(ruleInvalidMaxAge, client, props, fileServiceProperties);
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
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testCorsMaxOrigins(client, props, null);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testCorsMaxOrigins(client, props, null);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testCorsMaxOrigins(client, props, null);
        
        client = TestHelper.createCloudFileClient();
        testCorsMaxOrigins(client, null, new FileServiceProperties());
    }

    private void testCorsMaxOrigins(
            ServiceClient client, ServiceProperties props, FileServiceProperties fileServiceProperties)
            throws StorageException, InterruptedException {
        CorsRule ruleManyOrigins = new CorsRule();
        ruleManyOrigins.getAllowedMethods().add(CorsHttpMethods.GET);

        // Add maximum number of allowed origins
        for (int i = 0; i < 64; i++) {
            ruleManyOrigins.getAllowedOrigins().add("www.xyz" + i + ".com");
        }

        this.testCorsRules(ruleManyOrigins, client, props, fileServiceProperties);

        ruleManyOrigins.getAllowedOrigins().add("www.xyz64.com");

        try {
            this.testCorsRules(ruleManyOrigins, client, props, fileServiceProperties);
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
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testCorsMaxHeaders(client, props, null);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testCorsMaxHeaders(client, props, null);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testCorsMaxHeaders(client, props, null);
        
        client = TestHelper.createCloudFileClient();
        testCorsMaxHeaders(client, null, new FileServiceProperties());
    }

    private void testCorsMaxHeaders(
            ServiceClient client, ServiceProperties props, FileServiceProperties fileServiceProperties)
            throws StorageException, InterruptedException {
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

        this.testCorsRules(ruleManyHeaders, client, props, fileServiceProperties);

        // Test with too many Exposed Headers (65)
        ruleManyHeaders.getExposedHeaders().add("x-ms-meta-toomany");

        try {
            this.testCorsRules(ruleManyHeaders, client, props, fileServiceProperties);
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
            this.testCorsRules(ruleManyHeaders, client, props, fileServiceProperties);
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
            this.testCorsRules(ruleManyHeaders, client, props, fileServiceProperties);
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
            this.testCorsRules(ruleManyHeaders, client, props, fileServiceProperties);
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
        ServiceClient client = TestHelper.createCloudBlobClient();
        ServiceProperties props = new ServiceProperties();
        props.setDefaultServiceVersion(Constants.HeaderConstants.TARGET_STORAGE_VERSION);
        testOptionalServiceProperties(client, props);

        client = TestHelper.createCloudQueueClient();
        props = new ServiceProperties();
        testOptionalServiceProperties(client, props);

        client = TestHelper.createCloudTableClient();
        props = new ServiceProperties();
        testOptionalServiceProperties(client, props);
    }

    private void testOptionalServiceProperties(ServiceClient client, ServiceProperties props) throws StorageException,
            InterruptedException {
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

        callUploadServiceProps(client, props, null);

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

        callUploadServiceProps(client, newProps, null);

        props.setCors(newProps.getCors());
        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));

        newProps.setLogging(props.getLogging());
        newProps.setHourMetrics(props.getHourMetrics());
        newProps.setMinuteMetrics(props.getMinuteMetrics());
        newProps.setCors(null);
        callUploadServiceProps(client, newProps, null);

        assertServicePropertiesAreEqual(props, callDownloadServiceProperties(client));
    }

    private void callUploadServiceProps(
            ServiceClient client, ServiceProperties props, FileServiceProperties fileProps)
            throws StorageException, InterruptedException {
        
        if (client.getClass().equals(CloudBlobClient.class)) {
            ((CloudBlobClient) client).uploadServiceProperties(props);
        }
        else if (client.getClass().equals(CloudTableClient.class)) {
            ((CloudTableClient) client).uploadServiceProperties(props);
        }
        else if (client.getClass().equals(CloudQueueClient.class)) {
            ((CloudQueueClient) client).uploadServiceProperties(props);
        }
        else if (client.getClass().equals(CloudFileClient.class)) {
            ((CloudFileClient) client).uploadServiceProperties(fileProps);
        }
        else {
            fail();
        }
        
        Thread.sleep(30000);
    }

    private ServiceProperties callDownloadServiceProperties(ServiceClient client) throws StorageException {
        if (client.getClass().equals(CloudBlobClient.class)) {
            CloudBlobClient blobClient = (CloudBlobClient) client;
            return blobClient.downloadServiceProperties();
        }
        else if (client.getClass().equals(CloudTableClient.class)) {
            CloudTableClient tableClient = (CloudTableClient) client;
            return tableClient.downloadServiceProperties();
        }
        else if (client.getClass().equals(CloudQueueClient.class)) {
            CloudQueueClient queueClient = (CloudQueueClient) client;
            return queueClient.downloadServiceProperties();
        }
        else {
            fail();
        }
        return null;
    }

    /**
     * Takes a CorsRule and tries to upload it. Then tries to download it and compares it to the initial CorsRule.
     */
    private void testCorsRules(CorsRule rule, ServiceClient client, ServiceProperties properties,
            FileServiceProperties fileServiceProperties) throws StorageException, InterruptedException {
        CorsProperties cors = (fileServiceProperties == null) ? properties.getCors() : fileServiceProperties.getCors();
        cors.getCorsRules().clear();
        cors.getCorsRules().add(rule);

        if (fileServiceProperties == null) {
            callUploadServiceProps(client, properties, null);
            assertServicePropertiesAreEqual(properties, callDownloadServiceProperties(client));
        } else {
            CloudFileClient fileClient = ((CloudFileClient) client);
            fileClient.uploadServiceProperties(fileServiceProperties);
            Thread.sleep(30000);
            assertFileServicePropertiesAreEqual(fileServiceProperties, fileClient.downloadServiceProperties());
        }
    }

    /**
     * Takes a List of CorsRules and tries to upload them. Then tries to download them and compares the list to the
     * initial CorsRule List.
     */
    private void testCorsRules(List<CorsRule> corsRules, ServiceClient client, ServiceProperties properties,
            FileServiceProperties fileServiceProperties) throws StorageException, InterruptedException {
        CorsProperties cors = (fileServiceProperties == null) ? properties.getCors() : fileServiceProperties.getCors();
        cors.getCorsRules().clear();

        for (CorsRule rule : corsRules) {
            cors.getCorsRules().add(rule);
        }

        if (fileServiceProperties == null) {
            callUploadServiceProps(client, properties, null);
            assertServicePropertiesAreEqual(properties, callDownloadServiceProperties(client));
        } else {
            CloudFileClient fileClient = ((CloudFileClient) client);
            fileClient.uploadServiceProperties(fileServiceProperties);
            Thread.sleep(30000);
            assertFileServicePropertiesAreEqual(fileServiceProperties, fileClient.downloadServiceProperties());
        }
    }

    /**
     * Checks two ServiceProperties for equality
     */
    private static void assertServicePropertiesAreEqual(ServiceProperties propsA, ServiceProperties propsB) {
        if (propsA == null && propsB == null) {
            return;
        } else {
            assertNotNull(propsA);
            assertNotNull(propsB);
        }
        
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
    
    /**
     * Checks the CORS rules of two <code>FileServiceProperties</code> objects to ensure they match.
     * 
     * @param original
     *          The <code>FileServiceProperties</code> used as a point of comparison
     * @param target
     *          The generated <code>FileServiceProperties</code>, which should match the original
     */
    private static void assertFileServicePropertiesAreEqual(FileServiceProperties original, FileServiceProperties target) {
        if (original == null && target == null) {
            return;
        } else {
            assertNotNull(original);
            assertNotNull(target);
        }
        
        if (original.getCors() != null && target.getCors() != null) {
            assertEquals(original.getCors().getCorsRules().size(), target.getCors().getCorsRules().size());

            // Check that rules are equal and in the same order.
            for (int i = 0; i < original.getCors().getCorsRules().size(); i++) {
                CorsRule ruleOriginal = original.getCors().getCorsRules().get(i);
                CorsRule ruleTarget = target.getCors().getCorsRules().get(i);

                assertTrue(ruleOriginal.getAllowedOrigins().size() == ruleTarget.getAllowedOrigins().size());
                assertTrue(ruleOriginal.getAllowedOrigins().containsAll(ruleTarget.getAllowedOrigins()));

                assertTrue(ruleOriginal.getExposedHeaders().size() == ruleTarget.getExposedHeaders().size());
                assertTrue(ruleOriginal.getExposedHeaders().containsAll(ruleTarget.getExposedHeaders()));

                assertTrue(ruleOriginal.getAllowedHeaders().size() == ruleTarget.getAllowedHeaders().size());
                assertTrue(ruleOriginal.getAllowedHeaders().containsAll(ruleTarget.getAllowedHeaders()));

                assertTrue(ruleOriginal.getAllowedMethods().equals(ruleTarget.getAllowedMethods()));

                assertTrue(ruleOriginal.getMaxAgeInSeconds() == ruleTarget.getMaxAgeInSeconds());
            }
        }
        else {
            assertNull(original.getCors());
            assertNull(target.getCors());
        }
    }
}
