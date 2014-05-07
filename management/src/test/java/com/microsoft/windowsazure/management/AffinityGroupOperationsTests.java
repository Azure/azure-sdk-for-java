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

package com.microsoft.windowsazure.management;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.models.AffinityGroupCreateParameters;
import com.microsoft.windowsazure.management.models.AffinityGroupGetResponse;
import com.microsoft.windowsazure.management.models.AffinityGroupListResponse;
import com.microsoft.windowsazure.management.models.AffinityGroupUpdateParameters;

public class AffinityGroupOperationsTests extends ManagementIntegrationTestBase { 
    private static final String affinityGroupName1 = "testAffinityGroup1";
    private static final String affinityGroupName2 = "testAffinityGroup2";
    private static final String affinityGroupLocation1 = "West US";
    private static final String affinityGroupLocation2 = "East US";
    private static final String affinityGrouplabel1 = "testAffinityGroup1 Label";
    private static final String affinityGroupLabel2 = "testAffinityGroup2 Label"; 
    private static final String affinityGroupDescription1 = "testAffinityGroupDescription1";
    private static final String affinityGroupDescription2 = "testAffinityGroupDescription2";
    
    @BeforeClass
    public static void setup() throws Exception {
        createService();
        cleanup();
        
        AffinityGroupCreateParameters createParameters = new AffinityGroupCreateParameters();
        createParameters.setName(affinityGroupName1);        
        createParameters.setLocation(affinityGroupLocation1);
        createParameters.setLabel(affinityGrouplabel1);
        createParameters.setDescription(affinityGroupDescription1);

        managementClient.getAffinityGroupsOperations().create(createParameters);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
        	 AffinityGroupListResponse affinityGroupListResponse = managementClient.getAffinityGroupsOperations().list();
        	 ArrayList<AffinityGroupListResponse.AffinityGroup> affinityGrouplist = affinityGroupListResponse.getAffinityGroups();
        	 for (AffinityGroupListResponse.AffinityGroup affinitygroup : affinityGrouplist) { 
            	 if (affinitygroup.getName().contains("testAffinityGroup")) {
                    managementClient.getAffinityGroupsOperations().delete(affinitygroup.getName());
            	 }
        	 }
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createAffinityGroup() throws Exception {
        // Arrange
        AffinityGroupCreateParameters createParameters = new AffinityGroupCreateParameters();
        createParameters.setName(affinityGroupName2);
        createParameters.setLocation(affinityGroupLocation2);
        createParameters.setLabel(affinityGroupLabel2);
        createParameters.setDescription(affinityGroupDescription2);
  
        // Act
        OperationResponse operationResponse = managementClient
                .getAffinityGroupsOperations().create(createParameters);

        // Assert
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }

    @Test
    public void getAffinityGroups() throws Exception {
        // Act
        AffinityGroupGetResponse affinityGroupResponse = managementClient
                .getAffinityGroupsOperations().get(affinityGroupName1);

        // Assert
        Assert.assertEquals(200, affinityGroupResponse.getStatusCode());
        Assert.assertNotNull(affinityGroupResponse.getRequestId());
        Assert.assertNotNull(affinityGroupResponse.getCapabilities());    
        Assert.assertEquals(affinityGroupName1, affinityGroupResponse.getName());  
        Assert.assertEquals(affinityGroupLocation1, affinityGroupResponse.getLocation());
        Assert.assertEquals(affinityGrouplabel1, affinityGroupResponse.getLabel());
        Assert.assertEquals(affinityGroupDescription1, affinityGroupResponse.getDescription()); 
        Assert.assertNotNull(affinityGroupResponse.getHostedServices());
        Assert.assertNotNull(affinityGroupResponse.getStorageServices());
    }
    
    @Test
    public void listAffinityGroupsSuccess() throws Exception {
        // Arrange  
    	 AffinityGroupListResponse affinityGroupListResponse = managementClient.getAffinityGroupsOperations().list();
    	 ArrayList<AffinityGroupListResponse.AffinityGroup> affinityGrouplist = affinityGroupListResponse.getAffinityGroups();
         Assert.assertNotNull(affinityGrouplist);        
    }
    
    @Test
    public void updateAffinityGroupSuccess() throws Exception {
	        // Arrange 
	    	String expectedAffinityGroupName = "testAffinityGroupUpdateSuccess";
	        String expectedAffinityGroupLabel = "testAffinityGroupUpdateSuccessLabel";
	        String expectedUpdatedAffinityGroupLabel = "testAffinityGroupUpdatedSuccessLabel";
	        String expectedLocation = "West US";
	        String expectedDescription = "updateAffinityGroupSuccess";
	         
		    AffinityGroupCreateParameters createParameters = new AffinityGroupCreateParameters();
		    createParameters.setName(expectedAffinityGroupName);
		    createParameters.setLocation(expectedLocation);
		    createParameters.setLabel(expectedAffinityGroupLabel );
	        
	        // Act
	        OperationResponse operationResponse = managementClient.getAffinityGroupsOperations().create(createParameters); 
	        Assert.assertEquals(201, operationResponse.getStatusCode());
	        
	        AffinityGroupUpdateParameters updateParameters = new AffinityGroupUpdateParameters();      
	        updateParameters.setLabel(expectedUpdatedAffinityGroupLabel);
	        updateParameters.setDescription(expectedDescription);
	        OperationResponse updateoperationResponse = managementClient.getAffinityGroupsOperations().update(expectedAffinityGroupName, updateParameters);
	        
	        // Assert
	        Assert.assertEquals(200, updateoperationResponse.getStatusCode());
	        Assert.assertNotNull(updateoperationResponse.getRequestId());
    }
}