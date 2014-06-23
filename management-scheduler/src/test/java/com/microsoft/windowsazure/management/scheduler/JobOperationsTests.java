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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.scheduler.models.*;
import com.microsoft.windowsazure.scheduler.*;
import com.microsoft.windowsazure.scheduler.models.*;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class JobOperationsTests extends SchedulerIntegrationTestBase { 
    private static String jobName; 
    private static String jobId;
    private static String cloudServiceName; 
    private static String jobCollectionName; 
    
    @BeforeClass
    public static void setup() throws Exception {  
        cloudServiceName = testJobCollectionPrefix + "jobcls" + randomString(5); 
        jobCollectionName = testJobCollectionPrefix + "jobcl" + randomString(7);
        jobName = testSchedulerPrefix + "job" + randomString(7);  
         
        createManagementClient();
        getLocation();
        
        createCloudServiceManagementService(); 
        createCloudService();
        
        createSchedulerManagementService();
        createJobCollection();
        
        createSchedulerService(cloudServiceName, jobCollectionName);
        createjob(); 
    }

    @AfterClass
    public static void cleanup() { 
    	cleanjob();  
    	cleanJobCollection();
        cleanCloudService();
    } 
    
    private static void cleanjob() {  
        try {
        	schedulerClient.getJobsOperations().delete(jobId);
        } catch (IOException e) {
        } catch (ServiceException e){      
        }
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
        String cloudServiceDesc = testSchedulerPrefix + "Desc1";
        
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
        String jobCollectionLabel = "JobLabel01";        
        
        //Arrange
        JobCollectionCreateParameters createParameters = new JobCollectionCreateParameters();
        createParameters.setLabel(jobCollectionLabel);
        
        //act
        OperationResponse operationResponse = schedulerManagementClient.getJobCollectionsOperations().create(cloudServiceName, jobCollectionName, createParameters); 
        
        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }
   
    private static void createjob() throws Exception {
        String jobLabels = "JobLabel01"; 
        
        JobAction action = new JobAction();
        JobHttpRequest jobHttpRequest = new JobHttpRequest();        
        jobHttpRequest.setMethod("Get");        
        URI uri = new URI("http://www.microsoft.com");
        jobHttpRequest.setUri(uri);
       
        RetryPolicy  retryPolicy =  new RetryPolicy();
        retryPolicy.setRetryCount(3);
        retryPolicy.setRetryType(RetryType.Fixed);
        
        JobErrorAction jobErrorAction = new JobErrorAction();
        jobErrorAction.setType(JobActionType.Http);
        
        action.setRequest(jobHttpRequest);
        action.setType(JobActionType.Http);  
        action.setRetryPolicy(retryPolicy);
        action.setErrorAction(jobErrorAction);
        
       
        JobRecurrence jobRecurrence = new JobRecurrence();
        jobRecurrence.setFrequency(JobRecurrenceFrequency.Minute);
        jobRecurrence.setInterval(1);
        jobRecurrence.setCount(1); 
        
        Calendar now = Calendar.getInstance();
        
        //Arrange
        JobCreateParameters createParameters = new JobCreateParameters();
        createParameters.setStartTime(now);  
        createParameters.setRecurrence(jobRecurrence);
        createParameters.setAction(action);       
     
        //act
        System.out.println("svc name = " + schedulerClient.getCloudServiceName());
        System.out.println("jc name = " + schedulerClient.getJobCollectionName());
        schedulerClient.setCloudServiceName(cloudServiceName);
        schedulerClient.setJobCollectionName(jobCollectionName);
        JobCreateResponse operationResponse = schedulerClient.getJobsOperations().create(createParameters); 
        
        //Assert
        jobId = operationResponse.getJob().getId();
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    } 
   
    @Test
    public void getjobSuccess() throws Exception { 
        //Act
        JobGetResponse getResponse = schedulerClient.getJobsOperations().get(jobId);
        System.out.println("job recurrence = " + getResponse.getJob().getRecurrence());
        System.out.println("job state = " + getResponse.getJob().getState());
       //Assert
        Assert.assertEquals(200, getResponse.getStatusCode());
        Assert.assertNotNull(getResponse.getRequestId());         
    }
    
    @Test
    public void getHistorySuccess() throws Exception { 
        JobGetHistoryParameters jobGetHistoryParameters = new JobGetHistoryParameters();
        jobGetHistoryParameters.setTop(1);
        
        //Act
        JobGetHistoryResponse getResponse = schedulerClient.getJobsOperations().getHistory(jobId, jobGetHistoryParameters);     
       //Assert
        Assert.assertEquals(200, getResponse.getStatusCode());
        Assert.assertNotNull(getResponse.getRequestId());         
    }
    
    @Test
    public void getHistoryWithFilter() throws Exception { 
        JobGetHistoryWithFilterParameters jobGetHistoryWithFilterParameters = new JobGetHistoryWithFilterParameters();
        jobGetHistoryWithFilterParameters.setStatus(JobHistoryStatus.Completed);     
        jobGetHistoryWithFilterParameters.setTop(1);
        //Act
        JobGetHistoryResponse getResponse = schedulerClient.getJobsOperations().getHistoryWithFilter(jobId, jobGetHistoryWithFilterParameters);
        System.out.println("job recurrence = " + getResponse.getJobHistory().get(0).getStartTime());
        System.out.println("job state = " + getResponse.getJobHistory().get(0).getTimestamp());
        
       //Assert
        Assert.assertEquals(200, getResponse.getStatusCode());
        Assert.assertNotNull(getResponse.getRequestId());         
    }
    
    @Test
    public void listJobWithFilter() throws Exception {
        JobListWithFilterParameters jobListParameters = new JobListWithFilterParameters();
        jobListParameters.setState(JobState.Enabled); 
        jobListParameters.setTop(1);        
        
        //Act
        JobListResponse listResponse = schedulerClient.getJobsOperations().listWithFilter(jobListParameters);     
        ArrayList<Job> jobList =  listResponse.getJobs();
        
        //Assert
        Assert.assertNotNull(jobList);  
        Assert.assertTrue(jobList.size() > 0);
    }
    
    @Test
    public void listJobSuccess() throws Exception {
        JobListParameters jobListParameters = new JobListParameters();
        jobListParameters.setSkip(0);      
        
        //Act
        JobListResponse listResponse = schedulerClient.getJobsOperations().list(jobListParameters);     
        ArrayList<Job> jobList =  listResponse.getJobs();
        
        //Assert
        Assert.assertNotNull(jobList);  
        Assert.assertTrue(jobList.size() > 0);
    }
    
    @Test
    public void updatejobCollectionStateSuccess() throws Exception {  
        //Act       
        JobCollectionJobsUpdateStateParameters updateParameters = new JobCollectionJobsUpdateStateParameters();      
        updateParameters.setState(JobState.Disabled); 
        JobCollectionJobsUpdateStateResponse updateOperationResponse = schedulerClient.getJobsOperations().updateJobCollectionState(updateParameters);
                    
        //Assert
        Assert.assertEquals(200, updateOperationResponse.getStatusCode());
        Assert.assertNotNull(updateOperationResponse.getRequestId());           
    }
   
    @Test
    public void updatejobStateSuccess() throws Exception {  
        //Act       
        JobUpdateStateParameters updateParameters = new JobUpdateStateParameters();      
        updateParameters.setState(JobState.Disabled);
        updateParameters.setUpdateStateReason("just test");
        JobUpdateStateResponse updateOperationResponse = schedulerClient.getJobsOperations().updateState(jobId, updateParameters);
			        
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