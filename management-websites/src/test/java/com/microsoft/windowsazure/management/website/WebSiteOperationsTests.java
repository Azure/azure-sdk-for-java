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

package com.microsoft.windowsazure.management.website;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.websites.models.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class WebSiteOperationsTests extends WebSiteManagementIntegrationTestBase {
    private static String websiteName = testWebsitePrefix + "01";
    private static String webSpaceName = WebSpaceNames.NORTHCENTRALUSWEBSPACE; 
    private static String hostName = ".azurewebsites.net";
    
    @BeforeClass
    public static void setup() throws Exception {
        createService();
        setupTest(WebSiteOperationsTests.class.getSimpleName());
        cleanup();
        createWebSite();
        resetTest(WebSiteOperationsTests.class.getSimpleName());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        setupTest(WebSiteOperationsTests.class.getSimpleName() + CLEANUP_SUFFIX);
        WebSiteListParameters  webSiteListParameters = new  WebSiteListParameters();
        ArrayList<String> propertiesToInclude = new ArrayList<String>();
        webSiteListParameters.setPropertiesToInclude(propertiesToInclude);

        WebSiteDeleteParameters webSiteDeleteParameters = new WebSiteDeleteParameters();
        webSiteDeleteParameters.setDeleteAllSlots(true);
        webSiteDeleteParameters.setDeleteEmptyServerFarm(true);
        webSiteDeleteParameters.setDeleteMetrics(true);

        WebSpacesListWebSitesResponse webSpacesListWebSitesResponse = null;
        try {
            webSpacesListWebSitesResponse = webSiteManagementClient.getWebSpacesOperations().listWebSites(webSpaceName, webSiteListParameters);
        } catch (Exception e1) {
        }

        if (webSpacesListWebSitesResponse != null) {
            ArrayList<WebSite> webSiteslist = webSpacesListWebSitesResponse.getWebSites(); 
            for (WebSite  webSite : webSiteslist)
            {
                if (webSite.getName().startsWith(testWebsitePrefix ))
                {
                    String websitename = webSite.getName().replaceFirst(hostName, "");
                    try {
                        webSiteManagementClient.getWebSitesOperations().delete(webSpaceName, websitename, webSiteDeleteParameters);
                    } catch (Exception e) {
                    }
                }
            }
        }

        try {
            webSiteManagementClient.getWebHostingPlansOperations().delete(webSpaceName, "DefaultSF");
        } catch (Exception e) {
            // fine if it fails.
        }
    }

    private static void createWebSite() throws Exception {
        ArrayList<String> hostNamesValue = new ArrayList<String>();
        hostNamesValue.add(websiteName + hostName); 

        WebHostingPlanCreateParameters params = new WebHostingPlanCreateParameters();
        params.setName("DefaultSF");
        params.setSKU(SkuOptions.Free);
        webSiteManagementClient.getWebHostingPlansOperations().create(webSpaceName, params);

        WebSiteCreateParameters.WebSpaceDetails webSpaceDetails = new WebSiteCreateParameters.WebSpaceDetails();
        webSpaceDetails.setGeoRegion(GeoRegionNames.NORTHCENTRALUS);
        webSpaceDetails.setPlan(WebSpacePlanNames.VIRTUALDEDICATEDPLAN);
        webSpaceDetails.setName(webSpaceName);

        //Arrange
        WebSiteCreateParameters createParameters = new WebSiteCreateParameters();
        createParameters.setName(websiteName);
        createParameters.setServerFarm("DefaultSF");
        createParameters.setWebSpace(webSpaceDetails);

        //Act
        WebSiteCreateResponse webSiteCreateResponse = webSiteManagementClient.getWebSitesOperations().create(webSpaceName, createParameters);
        Assert.assertEquals(200,  webSiteCreateResponse.getStatusCode());
        Assert.assertNotNull( webSiteCreateResponse.getRequestId());
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }

    @Test
    public void createWebSiteSuccess() throws Exception {
        String webSiteName = testWebsitePrefix  + "02";
        ArrayList<String> hostNamesValue = new ArrayList<String>();
        hostNamesValue.add(webSiteName + hostName); 

        WebSiteCreateParameters.WebSpaceDetails webSpaceDetails = new WebSiteCreateParameters.WebSpaceDetails();
        webSpaceDetails.setGeoRegion(GeoRegionNames.NORTHCENTRALUS);
        webSpaceDetails.setPlan(WebSpacePlanNames.VIRTUALDEDICATEDPLAN);
        webSpaceDetails.setName(webSpaceName);

        //Arrange
        WebSiteCreateParameters createParameters = new WebSiteCreateParameters();
        createParameters.setName(webSiteName);
        createParameters.setServerFarm("DefaultSF");
        createParameters.setWebSpace(webSpaceDetails);

        //Act
        WebSiteCreateResponse webSiteCreateResponse = webSiteManagementClient.getWebSitesOperations().create(webSpaceName, createParameters);

        //Assert
        Assert.assertEquals(200,  webSiteCreateResponse.getStatusCode());
        Assert.assertNotNull( webSiteCreateResponse.getRequestId());
        Assert.assertEquals(webSiteName, webSiteCreateResponse.getWebSite().getName());
    }

    @Test
    public void getWebSiteSuccess() throws Exception { 
        //Act
        WebSiteGetParameters webSiteGetParameters = new WebSiteGetParameters();
        WebSiteGetResponse webSiteGetResponse = webSiteManagementClient.getWebSitesOperations().get(webSpaceName, websiteName, webSiteGetParameters);

        //Assert
        Assert.assertEquals(200, webSiteGetResponse.getStatusCode());
        Assert.assertNotNull(webSiteGetResponse.getRequestId()); 
        Assert.assertEquals(websiteName, webSiteGetResponse.getWebSite().getName());
    }

    @Test
    public void updateWebSiteSuccess() throws Exception {
        //Arrange 
        WebSiteGetParameters webSiteGetParameters = new WebSiteGetParameters(); 

        //Act
        WebSiteGetResponse webSiteGetResponse = webSiteManagementClient.getWebSitesOperations().get(webSpaceName, websiteName, webSiteGetParameters);
        Assert.assertEquals(200, webSiteGetResponse.getStatusCode());

        ArrayList<String> hostNamesValue = new ArrayList<String>();
        hostNamesValue.add(websiteName+hostName);
        WebSiteUpdateParameters updateParameters = new WebSiteUpdateParameters();
        updateParameters.setHostNames(hostNamesValue);

        OperationResponse updateoperationResponse = webSiteManagementClient.getWebSitesOperations().update(webSpaceName, websiteName, updateParameters);            
        //Assert
        Assert.assertEquals(200, updateoperationResponse.getStatusCode());
        Assert.assertNotNull(updateoperationResponse.getRequestId());
    }

    @Test
    public void repositoryoperationSuccess() throws Exception { 
        //Act
        OperationResponse createResponse = webSiteManagementClient.getWebSitesOperations().createRepository(webSpaceName, websiteName);
        Assert.assertEquals(200, createResponse.getStatusCode());
        Assert.assertNotNull(createResponse.getRequestId());

        WebSiteGetRepositoryResponse  getResponse = webSiteManagementClient.getWebSitesOperations().getRepository(webSpaceName, websiteName);

        //Assert
        Assert.assertEquals(200, getResponse.getStatusCode());
        Assert.assertNotNull(getResponse.getRequestId());
        Assert.assertNotNull(getResponse.getUri());
    }

    @Test
    public void isHostnameAvailableSuccess() throws Exception {
        String webSiteNameInValid = websiteName;
        String webSiteNameValid =testWebsitePrefix + "invalidsite"; 

        //Act
        WebSiteIsHostnameAvailableResponse webSiteIsHostnameAvailableResponseInvalid = webSiteManagementClient.getWebSitesOperations().isHostnameAvailable(webSiteNameInValid);
        
        //Assert
        Assert.assertEquals(200, webSiteIsHostnameAvailableResponseInvalid.getStatusCode());
        Assert.assertNotNull(webSiteIsHostnameAvailableResponseInvalid.getRequestId()); 
        Assert.assertEquals(false, webSiteIsHostnameAvailableResponseInvalid.isAvailable());  
        
        WebSiteIsHostnameAvailableResponse webSiteIsHostnameAvailableResponseValid = webSiteManagementClient.getWebSitesOperations().isHostnameAvailable(webSiteNameValid);
        Assert.assertEquals(200, webSiteIsHostnameAvailableResponseValid.getStatusCode());
        Assert.assertNotNull(webSiteIsHostnameAvailableResponseValid.getRequestId()); 
        Assert.assertEquals(true, webSiteIsHostnameAvailableResponseValid.isAvailable());  
    }

    @Test
    public void restartWebSiteSuccess() throws Exception {
        OperationResponse  operationResponse = webSiteManagementClient.getWebSitesOperations().restart(webSpaceName, websiteName);

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId()); 
    }

    @Test
    public void generatePasswordSuccess() throws Exception {
        OperationResponse operationResponse = webSiteManagementClient.getWebSitesOperations().generatePassword(webSpaceName, websiteName);

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }

    @SuppressWarnings("static-access")
    @Test
    public void getConfigurationSuccess() throws Exception {
        //Act
        WebSiteGetConfigurationResponse  webSiteGetConfigurationResponse = webSiteManagementClient.getWebSitesOperations().getConfiguration(webSpaceName, websiteName);

        //Assert
        Assert.assertEquals(200, webSiteGetConfigurationResponse.getStatusCode());
        Assert.assertNotNull(webSiteGetConfigurationResponse.getRequestId()); 
        Assert.assertEquals(false, webSiteGetConfigurationResponse.isWebSocketsEnabled());  
        Assert.assertEquals(null, webSiteGetConfigurationResponse.getDocumentRoot()); 
        Assert.assertEquals(35, webSiteGetConfigurationResponse.getLogsDirectorySizeLimit().intValue());  

        Assert.assertEquals(Calendar.YEAR, webSiteGetConfigurationResponse.getRequestTracingExpirationTime().YEAR);
        Assert.assertEquals(null, webSiteGetConfigurationResponse.getRemoteDebuggingVersion()); 
        Assert.assertEquals(0, webSiteGetConfigurationResponse.getConnectionStrings().size());     
    }

    @Test
    public void getHistoricalUsageMetricsSuccess() throws Exception {
        WebSiteGetHistoricalUsageMetricsParameters parameters = new WebSiteGetHistoricalUsageMetricsParameters();
        ArrayList<String> list = new ArrayList<String>();
        list.add("test");
        parameters.setMetricNames(list);

        Calendar now = Calendar.getInstance();
        Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 5));            
        Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        endTime.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) , now.get(Calendar.DATE - 1));
        parameters.setStartTime(startTime);
        parameters.setEndTime(endTime);

        addRegexRule("StartTime=[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}%3A[0-9]{2}%3A[0-9]{2}\\.[0-9]+Z&EndTime=[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}%3A[0-9]{2}%3A[0-9]{2}\\.[0-9]+Z");
        //Act
        WebSiteGetHistoricalUsageMetricsResponse webSiteGetHistoricalUsageMetricsResponse = webSiteManagementClient.getWebSitesOperations().getHistoricalUsageMetrics(webSpaceName, websiteName, parameters);

        //Assert
        Assert.assertEquals(200, webSiteGetHistoricalUsageMetricsResponse.getStatusCode());
        Assert.assertNotNull(webSiteGetHistoricalUsageMetricsResponse.getRequestId());
    }

    protected static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
}