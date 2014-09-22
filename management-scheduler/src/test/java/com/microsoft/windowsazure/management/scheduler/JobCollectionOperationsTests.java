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

package com.microsoft.windowsazure.management.scheduler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.scheduler.models.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class JobCollectionOperationsTests extends SchedulerIntegrationTestBase {
    private static String jobCollectionName;
    private static String cloudServiceName;

    @BeforeClass
    public static void setup() throws Exception {
        cloudServiceName = testJobCollectionPrefix + "jobcls" + randomString(5);
        jobCollectionName = testJobCollectionPrefix + "jobcl" + randomString(7);
        addRegexRule(testJobCollectionPrefix + "jobcls[a-z]{5}");
        addRegexRule(testJobCollectionPrefix + "jobcl[a-z]{7}");

        createManagementClient();
        createCloudServiceManagementService();
        createSchedulerManagementService();
        
        setupTest("JobCollectionOperationsTests");
        
        getLocation();
        createCloudService();
        createJobCollection();
        resetTest("JobCollectionOperationsTests");
    }

    @AfterClass
    public static void cleanup() throws Exception {
        setupTest("JobCollectionOperationsTestsCleanup");
        cleanJobCollection();
        cleanCloudService();
        resetTest("JobCollectionOperationsTestsCleanup");
    }

    private static void cleanJobCollection() {
        try {
            schedulerManagementClient.getJobCollectionsOperations().delete(cloudServiceName, jobCollectionName);
        } catch (IOException e) {
        } catch (ServiceException e) {
        } catch (ExecutionException e) {
        } catch (InterruptedException e) {
        }
    }

    private static void cleanCloudService() {
        CloudServiceGetResponse cloudServiceGetResponse = null;
        try {
            cloudServiceGetResponse = cloudServiceManagementClient.getCloudServicesOperations().get(cloudServiceName);
        } catch (ServiceException e) {
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        }

        if ((cloudServiceGetResponse != null ) &&(cloudServiceGetResponse.getResources().contains(cloudServiceName)))
        {
            CloudServiceOperationStatusResponse operationStatusResponse = null;
            try {
                operationStatusResponse = cloudServiceManagementClient.getCloudServicesOperations().delete(cloudServiceName);
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            } catch (ServiceException e) {
            } catch (IOException e) {
            }
            if (operationStatusResponse != null) {
                Assert.assertEquals(200, operationStatusResponse.getStatusCode());
                waitOperationToComplete(operationStatusResponse.getRequestId(), 20, 60);
            }
        }
    }

    private static void createCloudService() throws InterruptedException, ExecutionException, ServiceException, IOException, ParserConfigurationException, SAXException, TransformerException, URISyntaxException {
        String cloudServiceDesc = testJobCollectionPrefix + "Desc1";

        CloudServiceCreateParameters createParameters = new CloudServiceCreateParameters();
        //required
        createParameters.setLabel(cloudServiceName);
        createParameters.setDescription(cloudServiceDesc);
        createParameters.setGeoRegion(hostedLocation);

        OperationResponse CloudServiceOperationResponse = cloudServiceManagementClient.getCloudServicesOperations().create(cloudServiceName, createParameters);  
        Assert.assertEquals(200, CloudServiceOperationResponse.getStatusCode());
        Assert.assertNotNull(CloudServiceOperationResponse.getRequestId());
    }

    private static void createJobCollection() throws Exception {
        String jcLabels = "JcLabel01";

        //Arrange
        JobCollectionCreateParameters createParameters = new JobCollectionCreateParameters();
        createParameters.setLabel(jcLabels);

        //act
        OperationResponse operationResponse = schedulerManagementClient.getJobCollectionsOperations().create(cloudServiceName, jobCollectionName, createParameters); 

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
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
    public void listCloudServiceSuccess() throws Exception {
        //Act
        CloudServiceListResponse listResponse = cloudServiceManagementClient.getCloudServicesOperations().list();
        ArrayList<CloudServiceListResponse.CloudService> cloudServiceList =  listResponse.getCloudServices();

        //Assert
        Assert.assertNotNull(cloudServiceList);
        Assert.assertTrue(cloudServiceList.size() > 0);
    }

    @Test
    public void getJobCollectionSuccess() throws Exception {
        //Act
        JobCollectionGetResponse getResponse = schedulerManagementClient.getJobCollectionsOperations().get(cloudServiceName, jobCollectionName);

       //Assert
        Assert.assertEquals(200, getResponse.getStatusCode());
        Assert.assertNotNull(getResponse.getRequestId());
    }

    @Test
    public void checkAvailabilitySuccess() throws Exception {
        String checkJobCollectionName = testJobCollectionPrefix + "chk"+randomString(8);
        addRegexRule(testJobCollectionPrefix + "chk[a-z]{8}");

        //Act
        JobCollectionCheckNameAvailabilityResponse checkNameAvailabilityResponse = schedulerManagementClient.getJobCollectionsOperations().checkNameAvailability(cloudServiceName, checkJobCollectionName);

        //Assert
        Assert.assertEquals(true, checkNameAvailabilityResponse.isAvailable());
    }

    @Test
    public void updateJobCollectionSuccess() throws Exception {
        //Arrange
        String updatedJobCollectionLabel = "testJobCollectionUpdatedLabel3";

        JobCollectionGetResponse getResponse = schedulerManagementClient.getJobCollectionsOperations().get(cloudServiceName, jobCollectionName);

        JobCollectionUpdateParameters updateParameters = new JobCollectionUpdateParameters();
        updateParameters.setLabel(updatedJobCollectionLabel);
        updateParameters.setETag(getResponse.getETag());
        SchedulerOperationStatusResponse updateOperationResponse = schedulerManagementClient.getJobCollectionsOperations().update(cloudServiceName, jobCollectionName, updateParameters);

        //Assert
        Assert.assertEquals(200, updateOperationResponse.getStatusCode());
        Assert.assertNotNull(updateOperationResponse.getRequestId());
    }

    private static void waitOperationToComplete(String requestId, long waitTimeBetweenTriesInSeconds, int maximumNumberOfTries) {
        boolean operationCompleted = false;
        int tryCount =0;
        while ((!operationCompleted)&&(tryCount<maximumNumberOfTries))
        {
            CloudServiceOperationStatusResponse operationStatus = null;
            try {
                operationStatus = cloudServiceManagementClient.getOperationStatus(requestId);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            if ((operationStatus.getStatus() == CloudServiceOperationStatus.Failed) || (operationStatus.getStatus() == CloudServiceOperationStatus.Succeeded))
            {
                operationCompleted = true;
            }else{
                try {
                    Thread.sleep(waitTimeBetweenTriesInSeconds * 1000);
                    tryCount ++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}