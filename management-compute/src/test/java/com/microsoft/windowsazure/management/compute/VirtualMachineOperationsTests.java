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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.models.*;
import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.management.storage.models.*;
import com.microsoft.windowsazure.storage.*;
import com.microsoft.windowsazure.storage.blob.*;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

public class VirtualMachineOperationsTests extends ComputeManagementIntegrationTestBase {

    //lower case only for storage account name, this is existed storage account with vhd-store container, 
    //need to create your own storage account and create container there to store VM images 
    private static String storageAccountName;
    private static String storageAccountKey = "";
    private static String storageContainer = "vhd-store";    
    private static String hostedServiceName;   
    private static String deploymentName = testVMPrefix + "deploy1";    
    private static String virtualMachineName = testVMPrefix + "vm1";    
    private static String vmLocation = "West US";
    private static String hostedServiceLabel = testVMPrefix + "HostedServiceLabel1";
    private static String hostedServiceDescription = testVMPrefix +"HostedServiceDescription1";        
    private static String deploymentLabel = testVMPrefix + "deployLabel1";
    private static HostedServiceOperations hostedServicesOperations;

    @BeforeClass    
    public static void setup() throws Exception {
        storageAccountName = testStoragePrefix + randomString(10);
        hostedServiceName = testHostedServicePrefix + randomString(10);
        
        //create storage service for storage account creation
        createStorageManagementClient();
        //create compute management service for all compute management operation
        createComputeManagementClient();
        //create management service for accessing management operation
        createManagementClient();
        
        hostedServicesOperations = computeManagementClient.getHostedServicesOperations();
        
        //dynamic get location for vm storage/hosted service
        getLocation();
        //create a new storage account for vm .vhd storage.
        createStorageAccount(storageAccountName, storageContainer);
        //create a vm first for accessing non-creation vm operation first  
        createVMDeployment();
    }

    @AfterClass   
    public static void cleanup() {        
        cleanDeployment();
        cleanHostedService();
        cleanBlob(storageAccountName, storageContainer);
        cleanStorageAccount(storageAccountName);
    }
    
    @Test
    public void createVirtualMachines() throws Exception {
        int random = (int)(Math.random()* 100); 
        String roleName = testVMPrefix + "vm2";
        String computerName = testVMPrefix + "vm2";
        String adminuserPassword = testVMPrefix + "!12";
        String adminUserName = testVMPrefix;
        URI mediaLinkUriValue =  new URI("http://"+ storageAccountName + ".blob.core.windows.net/"+storageContainer+ "/" + testVMPrefix +random + ".vhd");
        String osVHarddiskName =testVMPrefix + "oshdname" + random;
        String operatingSystemName ="Windows";

        //required
        ArrayList<ConfigurationSet> configlist = new ArrayList<ConfigurationSet>();
        ConfigurationSet configset = new ConfigurationSet();
        configset.setConfigurationSetType(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION);
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
        createParameters.setRoleSize(VirtualMachineRoleSize.MEDIUM);
        createParameters.setProvisionGuestAgent(true);
        createParameters.setConfigurationSets(configlist);
        createParameters.setOSVirtualHardDisk(oSVirtualHardDisk);

        //Act
        OperationResponse operationResponse = computeManagementClient.getVirtualMachinesOperations().create(hostedServiceName, deploymentName, createParameters);

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }
    
    private static void createHostedService() throws InterruptedException, ExecutionException, ServiceException, IOException, ParserConfigurationException, SAXException, TransformerException, URISyntaxException {
        //hosted service required for vm deployment
        HostedServiceCreateParameters createParameters = new HostedServiceCreateParameters(); 
        //required
        createParameters.setLabel(hostedServiceLabel);
        //required
        createParameters.setServiceName(hostedServiceName);
        createParameters.setDescription(hostedServiceDescription);
        //required
        createParameters.setLocation(vmLocation);
        OperationResponse hostedServiceOperationResponse = hostedServicesOperations.create(createParameters);         
        Assert.assertEquals(201, hostedServiceOperationResponse.getStatusCode());
        Assert.assertNotNull(hostedServiceOperationResponse.getRequestId());
        
        System.out.println("hostedservice created: " + hostedServiceName);
    }
    
    private static ArrayList<Role> createRoleList() throws Exception {
        int random = (int)(Math.random()* 100);
        ArrayList<Role> roleList = new ArrayList<Role>();
        Role role = new Role();
        String roleName = virtualMachineName;
        String computerName = virtualMachineName;
        String adminUserPassword = testVMPrefix + "!12";
        String adminUserName = testVMPrefix;        
        URI mediaLinkUriValue =  new URI("http://"+ storageAccountName + ".blob.core.windows.net/"+storageContainer+ "/" + testVMPrefix + random +".vhd");
        String osVHarddiskName =testVMPrefix + "oshdname"+ random;
        String operatingSystemName ="Windows";

        //required
        ArrayList<ConfigurationSet> configurationSetList = new ArrayList<ConfigurationSet>();
        ConfigurationSet configurationSet = new ConfigurationSet();
         configurationSet.setConfigurationSetType(ConfigurationSetTypes.WINDOWSPROVISIONINGCONFIGURATION);
        //required
        configurationSet.setComputerName(computerName);
        //required
        configurationSet.setAdminPassword(adminUserPassword);
        //required
        configurationSet.setAdminUserName(adminUserName);
        configurationSet.setEnableAutomaticUpdates(false);
        configurationSet.setHostName(hostedServiceName + ".cloudapp.net");
        configurationSetList.add(configurationSet); 

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
        role.setRoleSize(VirtualMachineRoleSize.MEDIUM);
        role.setProvisionGuestAgent(true);
        role.setConfigurationSets(configurationSetList);
        role.setOSVirtualHardDisk(oSVirtualHardDisk);
        roleList.add(role);
        return roleList; 
    }
    
    private static void createVMDeployment() throws Exception {
        createHostedService();
        
        ArrayList<Role> rolelist = createRoleList(); 
        
        VirtualMachineCreateDeploymentParameters deploymentParameters = new VirtualMachineCreateDeploymentParameters();
        deploymentParameters.setDeploymentSlot(DeploymentSlot.Staging);
        deploymentParameters.setName(deploymentName); 
        deploymentParameters.setLabel(deploymentLabel);        
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

        for (HostedServiceListResponse.HostedService hostedService : hostedServicelist) {
            if (hostedService.getServiceName().contains(testVMPrefix)) {
                HostedServiceGetDetailedResponse hostedServiceGetDetailedResponse = computeManagementClient.getHostedServicesOperations().getDetailed(hostedService.getServiceName());

                int year = hostedServiceGetDetailedResponse.getProperties().getDateLastModified().get(Calendar.YEAR);
                Assert.assertTrue(year > 2000);
                
                ArrayList<HostedServiceGetDetailedResponse.Deployment> deploymentlist = hostedServiceGetDetailedResponse.getDeployments();
                Assert.assertNotNull(deploymentlist);

                for (HostedServiceGetDetailedResponse.Deployment deployment : deploymentlist) {
                    ArrayList<Role> rolelist = deployment.getRoles();
                    Assert.assertNotNull(rolelist);

                    for (Role role : rolelist) {
                        if ((role.getRoleType()!=null) && (role.getRoleType().equalsIgnoreCase(VirtualMachineRoleType.PersistentVMRole.toString()))) {
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
        //Act
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(hostedServiceName, deploymentName, virtualMachineName);

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
        //Act
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(hostedServiceName, deploymentName, virtualMachineName);

        //Assert
        Assert.assertEquals(200, virtualMachinesGetResponse.getStatusCode());
        Assert.assertNotNull(virtualMachinesGetResponse.getRequestId());

        String vmName = virtualMachinesGetResponse.getRoleName();

        VirtualMachineShutdownParameters stopParameters = new VirtualMachineShutdownParameters();
        stopParameters.setPostShutdownAction(PostShutdownAction.Stopped);
        OperationStatusResponse shutdownresponse = computeManagementClient.getVirtualMachinesOperations().shutdown(hostedServiceName, deploymentName, vmName, stopParameters);
        Assert.assertEquals(200, shutdownresponse.getStatusCode());
        Assert.assertNotNull(shutdownresponse.getRequestId());

        OperationStatusResponse startresponse = computeManagementClient.getVirtualMachinesOperations().start(hostedServiceName, deploymentName, vmName);
        Assert.assertEquals(200, startresponse.getStatusCode());
        Assert.assertNotNull(startresponse.getRequestId());

        OperationStatusResponse restartresponse = computeManagementClient.getVirtualMachinesOperations().restart(hostedServiceName, deploymentName, vmName);
        Assert.assertEquals(200, restartresponse.getStatusCode());
        Assert.assertNotNull(restartresponse.getRequestId());
    }
    
    @Test
    @Ignore("Because it takes too long to run")
    public void deleteVirtualMachines() throws Exception {        
        //Act
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(hostedServiceName, deploymentName, virtualMachineName);
        //Assert
        Assert.assertEquals(200, virtualMachinesGetResponse.getStatusCode());
        Assert.assertNotNull(virtualMachinesGetResponse.getRequestId());
      
        OperationStatusResponse deleteResponse = computeManagementClient.getVirtualMachinesOperations().delete(hostedServiceName, deploymentName, virtualMachinesGetResponse.getRoleName(), true);
        //Assert
        Assert.assertEquals(200, deleteResponse.getStatusCode());
        Assert.assertNotNull(deleteResponse.getRequestId());
    }
    
    @Test
    public void updateVMInputEndPoint() throws Exception {       
        //Act
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(hostedServiceName, deploymentName, virtualMachineName);
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
        inputEndpoint.setProtocol(InputEndpointTransportProtocol.TCP);
        endpointlist.add(inputEndpoint);
        updateParameters.setConfigurationSets(configlist);

        //required for update
        OSVirtualHardDisk osVirtualHardDisk = virtualMachinesGetResponse.getOSVirtualHardDisk();
        updateParameters.setOSVirtualHardDisk(osVirtualHardDisk);
        updateParameters.setRoleName(virtualMachinesGetResponse.getRoleName());

        OperationResponse updateoperationResponse = computeManagementClient.getVirtualMachinesOperations().update(hostedServiceName, deploymentName, virtualMachinesGetResponse.getRoleName(), updateParameters);

        //Assert
        Assert.assertEquals(200, updateoperationResponse.getStatusCode());
        Assert.assertNotNull(updateoperationResponse.getRequestId());
    }
    
    @Test
    public void updateVMSize() throws Exception {      
        //Act
        VirtualMachineGetResponse virtualMachinesGetResponse = computeManagementClient.getVirtualMachinesOperations().get(hostedServiceName, deploymentName, virtualMachineName);

        //Assert
        Assert.assertEquals(200, virtualMachinesGetResponse.getStatusCode());
        Assert.assertNotNull(virtualMachinesGetResponse.getRequestId()); 

        VirtualMachineUpdateParameters updateParameters = new VirtualMachineUpdateParameters(); 
        updateParameters.setRoleName(virtualMachinesGetResponse.getRoleName());
        //update the role size
        updateParameters.setRoleSize(VirtualMachineRoleSize.SMALL);

        //this is required parameters for update
        OSVirtualHardDisk osVirtualHardDisk = virtualMachinesGetResponse.getOSVirtualHardDisk();
        updateParameters.setOSVirtualHardDisk(osVirtualHardDisk);

        //update
        OperationResponse updateoperationResponse = computeManagementClient.getVirtualMachinesOperations().update(hostedServiceName, deploymentName, virtualMachineName, updateParameters);

        //Assert
        Assert.assertEquals(200, updateoperationResponse.getStatusCode());
        Assert.assertNotNull(updateoperationResponse.getRequestId());
    }

    private static String getOSSourceImage() throws Exception {
        String sourceImageName = null;
        VirtualMachineOSImageListResponse virtualMachineImageListResponse = computeManagementClient.getVirtualMachineOSImagesOperations().list();
        ArrayList<VirtualMachineOSImageListResponse.VirtualMachineOSImage> virtualMachineOSImagelist = virtualMachineImageListResponse.getImages();

        Assert.assertNotNull(virtualMachineOSImagelist);
        for (VirtualMachineOSImageListResponse.VirtualMachineOSImage virtualMachineImage : virtualMachineOSImagelist) {
            if ((virtualMachineImage.getName().contains("Win-GA")) && (virtualMachineImage.getName().contains("JDK"))) {
                sourceImageName = virtualMachineImage.getName();
                break;
            }
        }
        Assert.assertNotNull(sourceImageName);
        return sourceImageName;
    }
    
    private static void cleanHostedService() {
        HostedServiceGetResponse hostedServiceGetResponse = null;
        
        try {
            hostedServiceGetResponse = computeManagementClient.getHostedServicesOperations().get(hostedServiceName); 
        } catch (ServiceException e) {
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (URISyntaxException e) {
        }
        
        if ((hostedServiceGetResponse != null ) &&(hostedServiceGetResponse.getServiceName().contains(hostedServiceName)))
        {                
            OperationStatusResponse operationStatusResponse = null;
            try {
                operationStatusResponse = computeManagementClient.getHostedServicesOperations().deleteAll(hostedServiceName);
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            } catch (ServiceException e) {
            } catch (IOException e) {
            } 
            if (operationStatusResponse != null) {
                Assert.assertEquals(200, operationStatusResponse.getStatusCode());
            }
        }
    }
    
    private static void cleanDeployment() {
        DeploymentGetResponse  deploymentGetResponse = null;
        try {
                deploymentGetResponse = computeManagementClient.getDeploymentsOperations().getByName(hostedServiceName, deploymentName);
        } catch (ServiceException e) {
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (URISyntaxException e) {
        }
        
        if ((deploymentGetResponse != null) && (deploymentGetResponse.getName().contains(deploymentName) == true)) {
            OperationStatusResponse operationStatusResponse = null;
            try {
                operationStatusResponse = computeManagementClient.getDeploymentsOperations().deleteByName(hostedServiceName, deploymentName, true);
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            } catch (ServiceException e) {
            } catch (IOException e) {
            }
            if (operationStatusResponse != null) {
                Assert.assertEquals(200, operationStatusResponse.getStatusCode());
            }
        }
    }
}