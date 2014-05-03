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

import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.exception.ServiceException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class VirtualMachineDiskOperationsTests extends ComputeManagementIntegrationTestBase {
    static int random = (int)(Math.random()* 100);
    public static String virtualMachineDiskName = testVMPrefix + "Disk" + random;

    static String storageAccountName = testStoragePrefix + randomString(10);
    static String storageContainer = "disk-store";

    static String vhdfileName = "oneGBFixedWS2008R2.vhd";
    static String filePath = "D:\\test\\vhdfile\\";

    static VirtualMachineDiskOperations diskOperation;

    @BeforeClass
    public static void setup() throws Exception {
        //create storage service for storage account creation
        createStorageManagementClient();
        //create compute management service for all compute management operation
        createComputeManagementClient();
        diskOperation = getVMDisksOperations();
        //create management service for accessing management operation
        createManagementClient();
        //dynamic get location for vm storage/hosted service
        getLocation();
        //create a new storage account for vm .vhd storage.
        createStorageAccount(storageAccountName, storageContainer);
        uploadFileToBlob(storageAccountName, storageContainer, vhdfileName, filePath);
        createDisk();
    }

    @AfterClass
    public static void cleanup() {
        deletDisks();
        cleanBlob(storageAccountName, storageContainer);
        cleanStorageAccount(storageAccountName);
    }

    private static VirtualMachineDiskOperations getVMDisksOperations(){
       return computeManagementClient.getVirtualMachineDisksOperations();
    }

    private static void deletDisks() {
        try
        {
            VirtualMachineDiskListResponse VirtualMachineDiskListResponse = diskOperation.listDisks();
            ArrayList<VirtualMachineDiskListResponse.VirtualMachineDisk> virtualMachineDisklist = VirtualMachineDiskListResponse.getDisks();
            for (VirtualMachineDiskListResponse.VirtualMachineDisk VirtualMachineDisk : virtualMachineDisklist)
            {
                if (VirtualMachineDisk.getName().contains(virtualMachineDiskName))
                {
                    computeManagementClient.getVirtualMachineDisksOperations().deleteDisk(VirtualMachineDisk.getName(), true);
                }
            }
        }
        catch (ServiceException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createDisk() throws Exception {
        String virtualMachineDiskDescription =  virtualMachineDiskName + "Description";
        URI mediaLinkUriValue =  new URI("http://"+ blobhost+ "/" +storageContainer+ "/" + vhdfileName);

        //Arrange
        VirtualMachineDiskCreateParameters createParameters = new VirtualMachineDiskCreateParameters();
        createParameters.setName(virtualMachineDiskName);
        createParameters.setLabel(virtualMachineDiskDescription);
        createParameters.setMediaLinkUri(mediaLinkUriValue);
        createParameters.setOperatingSystemType(VirtualMachineOSImageOperatingSystemType.WINDOWS);

        //Act
        VirtualMachineDiskCreateResponse operationResponse = diskOperation.createDisk(createParameters);

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }

    @Test
    public void getDisk() throws Exception {
        //Act
        VirtualMachineDiskGetResponse VirtualMachineDiskResponse = diskOperation.getDisk(virtualMachineDiskName);

        //Assert
        Assert.assertEquals(200, VirtualMachineDiskResponse.getStatusCode());
        Assert.assertNotNull(VirtualMachineDiskResponse.getRequestId());
        Assert.assertEquals(virtualMachineDiskName, VirtualMachineDiskResponse.getName()); 
    }

    @Test
    public void listDisks() throws Exception {
        //Act
        VirtualMachineDiskListResponse virtualMachineDiskListResponse = diskOperation.listDisks();
        ArrayList<VirtualMachineDiskListResponse.VirtualMachineDisk> virtualMachineDisklist = virtualMachineDiskListResponse.getDisks();

        //Assert
        Assert.assertNotNull(virtualMachineDisklist);
        Assert.assertTrue(virtualMachineDisklist.size() >= 1);
        for (VirtualMachineDiskListResponse.VirtualMachineDisk virtualMachineDisk : virtualMachineDisklist)
        {
            Assert.assertNotNull(virtualMachineDisk.getName());
        }
    }

    @Test
    public void updateDisk() throws Exception {
        //Arrange
        String virtualMachineDiskLabel = virtualMachineDiskName + "Label";
        String expectedUpdatedVirtualMachineDiskLabel = virtualMachineDiskLabel + "updated";
        String expectedvirtualMachineDiskName = virtualMachineDiskName + "updated";

        //Act
        VirtualMachineDiskUpdateParameters updateParameters = new VirtualMachineDiskUpdateParameters();
        updateParameters.setLabel(expectedUpdatedVirtualMachineDiskLabel);
        updateParameters.setName(expectedvirtualMachineDiskName);
        VirtualMachineDiskUpdateResponse updateOperationResponse = diskOperation.updateDisk(virtualMachineDiskName, updateParameters);

        //Assert
        Assert.assertEquals(200, updateOperationResponse.getStatusCode());
        Assert.assertNotNull(updateOperationResponse.getRequestId());
    }
}