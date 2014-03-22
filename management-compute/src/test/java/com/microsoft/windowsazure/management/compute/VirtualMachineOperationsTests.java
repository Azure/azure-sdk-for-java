/*
 * Copyright Microsoft.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management.compute;

import java.net.URI;
import java.util.ArrayList;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.management.models.LocationNames;
import com.microsoft.windowsazure.management.storage.models.*;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class VirtualMachineOperationsTests extends ComputeManagementIntegrationTestBase {    
	private static String testVMPrefix = "azuresdktest";
	//lower case only for storage account name, this is existed storage account with vhd-store container, 
	//you can create your own storage account and create container there to store VM images 
	private static String storageAccountName = testVMPrefix + "08";	

    @BeforeClass
    public static void setup() throws Exception {
    	createStorageService();
        createService();
        
        //create a new storage account if did not use existed storage account for vm .vhd storage.
        //createStorageAccount();
        
        //create a vm first for accessing non-creation vm operation first  
        //createVMDeployment();
    }
    
    @AfterClass
    public static void Cleanup() throws Exception 
    { 
    	//delete all vm 
    	String serviceName = testVMPrefix + "HostedService1";
    	String deploymentName = testVMPrefix + "deploy1";
    	
    	HostedServiceListResponse hostedServiceListResponse = computeManagementClient.getHostedServicesOperations().list();
    	ArrayList<HostedServiceListResponse.HostedService> hostedServicelist = hostedServiceListResponse.getHostedServices();    	 
    	Assert.assertNotNull(hostedServicelist);
    	for (HostedServiceListResponse.HostedService hostedService : hostedServicelist)
    	{ 
    		if (hostedService.getServiceName().contains(testVMPrefix))
    		{    			
    			HostedServiceGetDetailedResponse hostedServiceGetDetailedResponse = computeManagementClient.getHostedServicesOperations().getDetailed(hostedService.getServiceName());
    			ArrayList<HostedServiceGetDetailedResponse.Deployment> deploymentlist = hostedServiceGetDetailedResponse.getDeployments();
    			
    			Assert.assertNotNull(deploymentlist);
    			
    			for (HostedServiceGetDetailedResponse.Deployment deployment : deploymentlist)
    			{ 
    				ArrayList<Role> rolelist = deployment.getRoles();
    				Assert.assertNotNull(rolelist);  
    				
    				for (Role role : rolelist)
    				{
    					if ((role.getRoleType()!=null) && (role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PersistentVMRole.toString())))
    					{
    						 Assert.assertTrue(role.getRoleName().contains(testVMPrefix));
    						 OperationStatusResponse deleteResponse = computeManagementClient.getVirtualMachinesOperations().delete(serviceName, deploymentName, role.getRoleName(), false);
    					}    					
    				}
    			}
        	} 
    	}
    	
    	//clean host service
    	HostedServiceGetResponse hostedServiceGetResponse = computeManagementClient.getHostedServicesOperations().get(serviceName);
    	if (hostedServiceGetResponse.getServiceName().contains(serviceName))
    	{
    		computeManagementClient.getHostedServicesOperations().delete(hostedServiceGetResponse.getServiceName());
    	}
    }
      
    @Test
    public void createVirtualMachines() throws Exception { 
      int random = (int)(Math.random()* 100);
    	String serviceName = testVMPrefix + "HostedService1";	
    	String deploymentName = testVMPrefix + "deploy1";
    	String roleName = testVMPrefix + "vm2";
    	String computerName = testVMPrefix + "vm2";
    	String adminuserPassword = testVMPrefix + "!12";
    	String adminUserName = testVMPrefix;    	
    	URI mediaLinkUriValue =  new URI("http://"+ storageAccountName + ".blob.core.windows.net/vhd-store/" + testVMPrefix +random + ".vhd");
       	String osVHarddiskName =testVMPrefix + "oshdname" + random;
    	String operatingSystemName ="Windows";    	 
    	 
    	//required
    	ArrayList<ConfigurationSet> configlist = new ArrayList<ConfigurationSet>();
    	ConfigurationSet configset = new ConfigurationSet();
    	configset.setConfigurationSetType(ConfigurationSetTypes.WindowsProvisioningConfiguration);
    	//required
    	configset.setComputerName(computerName);
    	//required
    	configset.setAdminPassword(adminuserPassword);
    	//required
    	configset.setAdminUserName(adminUserName);
    	configset.setEnableAutomaticUpdates(false);  
    	configlist.add(configset);
    	 
    	//required
    	String sourceImageName = getOSSourceImage();    
    	OSVirtualHardDisk oSVirtualHardDisk = new OSVirtualHardDisk(); 
    	//required
    	oSVirtualHardDisk.setName(osVHarddiskName);
    	oSVirtualHardDisk.setHostCaching(VirtualHardDiskHostCaching.ReadWrite);    	
    	oSVirtualHardDisk.setOperatingSystem(operatingSystemName);
    	//required
    	oSVirtualHardDisk.setMediaLink(mediaLinkUriValue);
    	//required
    	oSVirtualHardDisk.setSourceImageName(sourceImageName);
      
        VirtualMachineCreateParameters createParameters = new VirtualMachineCreateParameters();
       //required
        createParameters.setRoleName(roleName);       
        createParameters.setRoleSize(VirtualMachineRoleSize.Medium);
        createParameters.setProvisionGuestAgent(true);
        createParameters.setConfigurationSets(configlist);
        createParameters.setOSVirtualHardDisk(oSVirtualHardDisk);       
        
        //Act
        OperationResponse operationResponse = computeManagementClient.getVirtualMachinesOperations().create(serviceName, deploymentName, createParameters);
        
        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    @Test
    public void createVMDeployment() throws Exception {
    	int random = (int)(Math.random()* 100); 
    	String hostedServiceName = testVMPrefix + "HostedService1";
    	String hostedServiceLabel = testVMPrefix + "HostedServiceLabel1";
    	String hostedServiceDescription = testVMPrefix +"HostedServiceDescription1";    	
    	String deploymentName = testVMPrefix + "deploy1";
     	String deploymentLabel = testVMPrefix + "deployLabel1";
     	
        //hosted service required for vm deployment
        HostedServiceCreateParameters createParameters = new HostedServiceCreateParameters(); 
        //required
        createParameters.setLabel(hostedServiceLabel);
        //required
        createParameters.setServiceName(hostedServiceName);        
        createParameters.setDescription(hostedServiceDescription);
        //required
        createParameters.setLocation(LocationNames.WestUS);
        OperationResponse hostedServiceOperationResponse = computeManagementClient.getHostedServicesOperations().create(createParameters);         
        Assert.assertEquals(201, hostedServiceOperationResponse.getStatusCode());
        Assert.assertNotNull(hostedServiceOperationResponse.getRequestId());
        
        VirtualMachineCreateDeploymentParameters deploymentParameters = new VirtualMachineCreateDeploymentParameters();
        //required
        deploymentParameters.setDeploymentSlot(DeploymentSlot.Staging);
        //required
        deploymentParameters.setName(deploymentName); 
        //required
        deploymentParameters.setLabel(deploymentLabel);
        //required
        ArrayList<Role> rolelist = new ArrayList<Role>();
        Role role = new Role();
       
    	String roleName = testVMPrefix + "vm1";
    	String computerName = testVMPrefix + "vm1";
    	String adminuserPassword = testVMPrefix + "!12";
    	String adminUserName = testVMPrefix;    	
    	URI mediaLinkUriValue =  new URI("http://"+ storageAccountName + ".blob.core.windows.net/vhd-store/" + testVMPrefix + random +".vhd");    	
    	String osVHarddiskName =testVMPrefix + "oshdname"+ random;
    	String operatingSystemName ="Windows";    	 
    	 
    	//required
    	ArrayList<ConfigurationSet> configlist = new ArrayList<ConfigurationSet>();
    	ConfigurationSet configset = new ConfigurationSet();
    	configset.setConfigurationSetType(ConfigurationSetTypes.WindowsProvisioningConfiguration);
    	//required
    	configset.setComputerName(computerName);
    	//required
    	configset.setAdminPassword(adminuserPassword);
    	//required
    	configset.setAdminUserName(adminUserName);
    	configset.setEnableAutomaticUpdates(false);  
    	configlist.add(configset); 
    	
    	String sourceImageName = getOSSourceImage();    	 
    	OSVirtualHardDisk oSVirtualHardDisk = new OSVirtualHardDisk(); 
    	//required
    	oSVirtualHardDisk.setName(osVHarddiskName);
    	oSVirtualHardDisk.setHostCaching(VirtualHardDiskHostCaching.ReadWrite);     	
    	oSVirtualHardDisk.setOperatingSystem(operatingSystemName);
    	//required
    	oSVirtualHardDisk.setMediaLink(mediaLinkUriValue);
    	//required
    	oSVirtualHardDisk.setSourceImageName(sourceImageName);    	     
       
    	//required
        role.setRoleName(roleName);
        //required
        role.setRoleType(VirtualMachineRoleType.PersistentVMRole.toString());       
        role.setRoleSize(VirtualMachineRoleSize.Medium);       
        role.setProvisionGuestAgent(true);
        role.setConfigurationSets(configlist);
        role.setOSVirtualHardDisk(oSVirtualHardDisk);              
         
        rolelist.add(role);
        deploymentParameters.setRoles(rolelist);
        
        // Act
        OperationResponse operationResponse = computeManagementClient.getVirtualMachinesOperations().createDeployment(hostedServiceName, deploymentParameters);        
        // Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    @Test
    public void listVirtualMachines() throws Exception {
    	//there is no dedicated vm list methods, has to filter through hosted service, and deployment, rolelist to find out the vm list
    	//role that has VirtualMachineRoleType.PersistentVMRole property is a vm
    	ArrayList<Role> vmlist = new ArrayList<Role>();
    	HostedServiceListResponse hostedServiceListResponse = computeManagementClient.getHostedServicesOperations().list();
    	ArrayList<HostedServiceListResponse.HostedService> hostedServicelist = hostedServiceListResponse.getHostedServices();    	 
    	Assert.assertNotNull(hostedServicelist); 
    	
    	for (HostedServiceListResponse.HostedService hostedService : hostedServicelist)
    	{ 
    		if (hostedService.getServiceName().contains(testVMPrefix))
    		{    			
    			HostedServiceGetDetailedResponse hostedServiceGetDetailedResponse = computeManagementClient.getHostedServicesOperations().getDetailed(hostedService.getServiceName());
    			ArrayList<HostedServiceGetDetailedResponse.Deployment> deploymentlist = hostedServiceGetDetailedResponse.getDeployments();    			
    			Assert.assertNotNull(deploymentlist);
    			
    			for (HostedServiceGetDetailedResponse.Deployment deployment : deploymentlist)
    			{ 
    				ArrayList<Role> rolelist = deployment.getRoles();
    				Assert.assertNotNull(rolelist);  
    				
    				for (Role role : rolelist)
    				{
    					if ((role.getRoleType()!=null) && (role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PersistentVMRole.toString())))
    					{
    						 Assert.assertTrue(role.getRoleName().contains(testVMPrefix));
    						 vmlist.add(role);
    					}    					
    				}
    			}
        	} 
    	}    	
    }
    
    @Test
    public void getVirtualMachines() throws Exception {
    	
    	 String virtualMachineName = testVMPrefix + "vm1";
    	 String serviceName = testVMPrefix + "HostedService1";
	     String deploymentName = testVMPrefix + "deploy1";	     
	    
	     //Act
	     VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(serviceName, deploymentName, virtualMachineName);

	     //Assert
	     Assert.assertEquals(200, virtualMachinesGetResponse.getStatusCode());
	     Assert.assertNotNull(virtualMachinesGetResponse.getRequestId());	     
	     //vm always has VirtualMachineRoleType.PersistentVMRole property   
	     Assert.assertEquals(VirtualMachineRoleType.PersistentVMRole, virtualMachinesGetResponse.getRoleType());
        
	     OSVirtualHardDisk osharddisk = virtualMachinesGetResponse.getOSVirtualHardDisk();	    
	     Assert.assertTrue(osharddisk.getOperatingSystem().contains("Window"));
	     Assert.assertTrue(osharddisk.getSourceImageName().contains("Win"));
	     Assert.assertEquals(VirtualHardDiskHostCaching.ReadWrite, osharddisk.getHostCaching());	         
    }
    
    @Test
    public void restartVirtualMachine() throws Exception {
    	//test for stop, start and restart
    	String virtualMachineName = testVMPrefix + "vm1";
    	String serviceName = testVMPrefix + "HostedService1";
    	String deploymentName = testVMPrefix + "deploy1";	     
	   
        //Act
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(serviceName, deploymentName, virtualMachineName);
       
        //Assert
        Assert.assertEquals(200, virtualMachinesGetResponse.getStatusCode());
        Assert.assertNotNull(virtualMachinesGetResponse.getRequestId());
        
        String vmName = virtualMachinesGetResponse.getRoleName();
        
        VirtualMachineShutdownParameters stopParameters = new VirtualMachineShutdownParameters();
	    stopParameters.setPostShutdownAction(PostShutdownAction.Stopped);	    
        OperationStatusResponse shutdownresponse = computeManagementClient.getVirtualMachinesOperations().shutdown(serviceName, deploymentName, vmName, stopParameters);
        Assert.assertEquals(200, shutdownresponse.getStatusCode());
        Assert.assertNotNull(shutdownresponse.getRequestId());
        
        OperationStatusResponse startresponse = computeManagementClient.getVirtualMachinesOperations().start(serviceName, deploymentName, vmName);
        Assert.assertEquals(200, startresponse.getStatusCode());
        Assert.assertNotNull(startresponse.getRequestId());
        
        OperationStatusResponse restartresponse = computeManagementClient.getVirtualMachinesOperations().restart(serviceName, deploymentName, vmName);
        Assert.assertEquals(200, restartresponse.getStatusCode());
        Assert.assertNotNull(restartresponse.getRequestId());
    }
    
    //@Test
    private void deleteVirtualMachines() throws Exception {
    	
    	String virtualMachineName = testVMPrefix + "vm1";
    	String serviceName = testVMPrefix + "HostedService1";
    	String deploymentName = testVMPrefix + "deploy1";	       
	 
        //Act
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(serviceName, deploymentName, virtualMachineName);

        //Assert
        Assert.assertEquals(200, virtualMachinesGetResponse.getStatusCode());
        Assert.assertNotNull(virtualMachinesGetResponse.getRequestId());        
      
        OperationStatusResponse deleteResponse = computeManagementClient.getVirtualMachinesOperations().delete(serviceName, deploymentName, virtualMachinesGetResponse.getRoleName(), false);
        Assert.assertEquals(200, deleteResponse.getStatusCode());
        Assert.assertNotNull(deleteResponse.getRequestId());      
    }
    
    @Test
    public void updateVMInputEndPoint() throws Exception {
    	
    	String virtualMachineName = testVMPrefix + "vm1";
    	String serviceName = testVMPrefix + "HostedService1";
    	String deploymentName = testVMPrefix + "deploy1";	  
	   
	    //Act
    	VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(serviceName, deploymentName, virtualMachineName);
	    //Assert
    	Assert.assertEquals(200, virtualMachinesGetResponse.getStatusCode());
    	Assert.assertNotNull(virtualMachinesGetResponse.getRequestId()); 
        
    	VirtualMachineUpdateParameters updateParameters = new VirtualMachineUpdateParameters(); 
    	//get the configuration list
    	ArrayList<ConfigurationSet> configlist = virtualMachinesGetResponse.getConfigurationSets();
    	//get inputendpoint list and update it 
    	ArrayList<InputEndpoint> endpointlist = configlist.get(0).getInputEndpoints();    	
    	InputEndpoint inputEndpoint = new InputEndpoint();
    	inputEndpoint.setEnableDirectServerReturn(false);
    	inputEndpoint.setPort(5987);
    	inputEndpoint.setLocalPort(5987);
    	inputEndpoint.setName("RDP");
    	inputEndpoint.setProtocol(InputEndpointTransportProtocol.Tcp); 	    	 
    	endpointlist.add(inputEndpoint);	    	 
    	updateParameters.setConfigurationSets(configlist);
	    	 
    	//required for update
    	OSVirtualHardDisk osVirtualHardDisk = virtualMachinesGetResponse.getOSVirtualHardDisk();	        
    	updateParameters.setOSVirtualHardDisk(osVirtualHardDisk);
    	updateParameters.setRoleName(virtualMachinesGetResponse.getRoleName());
	    	 
    	OperationResponse updateoperationResponse = computeManagementClient.getVirtualMachinesOperations().update(serviceName, deploymentName, virtualMachinesGetResponse.getRoleName(), updateParameters);
	        
	    //Assert
    	Assert.assertEquals(200, updateoperationResponse.getStatusCode());
    	Assert.assertNotNull(updateoperationResponse.getRequestId());
    }
    
    @Test
    public void updateVMSize() throws Exception {
     		
    	String virtualMachineName = testVMPrefix + "vm1";
    	String serviceName = testVMPrefix + "HostedService1";
    	String deploymentName = testVMPrefix + "deploy1";
    		   
   	 	//Act
    	VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(serviceName, deploymentName, virtualMachineName);

   	 	//Assert
    	Assert.assertEquals(200, virtualMachinesGetResponse.getStatusCode());
    	Assert.assertNotNull(virtualMachinesGetResponse.getRequestId()); 
       
    	VirtualMachineUpdateParameters updateParameters = new VirtualMachineUpdateParameters(); 
    	updateParameters.setRoleName(virtualMachinesGetResponse.getRoleName());    	
    	//update the role size
    	updateParameters.setRoleSize(VirtualMachineRoleSize.Small);
	        
    	//this is required parameters for update
    	OSVirtualHardDisk osVirtualHardDisk = virtualMachinesGetResponse.getOSVirtualHardDisk();	        
    	updateParameters.setOSVirtualHardDisk(osVirtualHardDisk);
	        
    	//update
    	OperationResponse updateoperationResponse = computeManagementClient.getVirtualMachinesOperations().update(serviceName, deploymentName, virtualMachineName, updateParameters);
	        
    	//Assert
    	Assert.assertEquals(200, updateoperationResponse.getStatusCode());
    	Assert.assertNotNull(updateoperationResponse.getRequestId());
    }
     
    private static String getOSSourceImage() throws Exception {
    	String sourceImageName = "";
    	VirtualMachineOSImageListResponse virtualMachineImageListResponse = computeManagementClient.getVirtualMachineOSImagesOperations().list();
    	ArrayList<VirtualMachineOSImageListResponse.VirtualMachineOSImage> virtualMachineOSImagelist = virtualMachineImageListResponse.getImages();
    	
    	Assert.assertNotNull(virtualMachineOSImagelist);    	       
    	for (VirtualMachineOSImageListResponse.VirtualMachineOSImage virtualMachineImage : virtualMachineOSImagelist)
    	{ 
    		//only need one window image for testing
    		if (virtualMachineImage.getName().contains("JDK-1.6.0_71-0314-Win-GA"))
    		{
    			sourceImageName = virtualMachineImage.getName();    			
    		}    	    		   
    	}
    	return sourceImageName;
    }
        
    private static void createStorageAccount() throws Exception { 
    	String storageAccountCreateName = testVMPrefix + "storage01";
    	String storageAccountLabel = testVMPrefix + "Label";    	     
    	        
    	//Arrange
    	StorageAccountCreateParameters createParameters = new StorageAccountCreateParameters();
    	//required
    	createParameters.setName(storageAccountCreateName); 
    	//required
    	createParameters.setLabel(storageAccountLabel);
    	//required if no affinity group has set
    	createParameters.setLocation(GeoRegionNames.NorthCentralUS);
    	
    	//act
    	OperationResponse operationResponse = storageManagementClient.getStorageAccountsOperations().create(createParameters); 
    	        
    	//Assert
    	Assert.assertEquals(200, operationResponse.getStatusCode()); 
    	storageAccountName = storageAccountCreateName;    	
    }
}