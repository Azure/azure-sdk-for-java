/**
 * Copyright Microsoft Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.utility.compute;

import com.microsoft.azure.management.compute.models.*;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ConsumerWrapper;
import com.microsoft.azure.utility.ResourceContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class VMOSProfileTests extends ComputeTestBase {


    private static final String CustomDataContent = "echo 'Hello World'";
    private static final String DefaultSshPublicKey =
            "MIIDszCCApugAwIBAgIJALBV9YJCF/tAMA0GCSqGSIb3DQEBBQUAMEUxCzAJBgNV" +
                    "BAYTAkFVMRMwEQYDVQQIEwpTb21lLVN0YXRlMSEwHwYDVQQKExhJbnRlcm5ldCBX" +
                    "aWRnaXRzIFB0eSBMdGQwHhcNMTUwMzIyMjI1NDQ5WhcNMTYwMzIxMjI1NDQ5WjBF" +
                    "MQswCQYDVQQGEwJBVTETMBEGA1UECBMKU29tZS1TdGF0ZTEhMB8GA1UEChMYSW50" +
                    "ZXJuZXQgV2lkZ2l0cyBQdHkgTHRkMIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIB" +
                    "CAKCAQEAxDC+OfmB+tQ+P1MLmuuW2hJLdcK8m4DLgAk5l8bQDNBcVezt+bt/ZFMs" +
                    "CHBhfTZG9O9yqMn8IRUh7/7jfQm6DmXCgtxj/uFiwT+F3out5uWvMV9SjFYvu9kJ" +
                    "NXiDC2u3l4lHV8eHde6SbKiZB9Jji9FYQV4YiWrBa91j9I3hZzbTL0UCiJ+1PPoL" +
                    "Rx/T1s9KT5Wn8m/z2EDrHWpetYu45OA7nzyIFOyQup5oWadWNnpk6HkCGutl9t9b" +
                    "cLdjXjXPm9wcy1yxIB3Dj/Y8Hnulr80GJlUtUboDm8TExGc4YaPJxdn0u5igo5gZ" +
                    "c6qnqH/BMd1nsyICx6AZnKBXBycoSQIBI6OBpzCBpDAdBgNVHQ4EFgQUzWhrCCDs" +
                    "ClANCGlKZ64rHp2BDn0wdQYDVR0jBG4wbIAUzWhrCCDsClANCGlKZ64rHp2BDn2h" +
                    "SaRHMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIEwpTb21lLVN0YXRlMSEwHwYDVQQK" +
                    "ExhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGSCCQCwVfWCQhf7QDAMBgNVHRMEBTAD" +
                    "AQH/MA0GCSqGSIb3DQEBBQUAA4IBAQCUaJnX0aBzwBkbJrBS5YvkZnNKLdc4oHgC" +
                    "/Nsr/9pwXzFYYXkdqpTw2nygH0C0WuPVVrG3Y3EGx/UIGDtLbwMvZJhQN9mZH3oX" +
                    "+c3HGqBnXGuDRrtsfsK1ywAofx9amZfKNk/04/Rt3POdbyD1/AOADw2zMokbIapX" +
                    "+nMDUtD/Tew9+0qU9+dcFMrFE1N4utlrFHlrLFbiCA/eSegP6gOeu9mqZv7UHIz2" +
                    "oe6IQTw7zJF7xuBIzTYwjOCM197GKW7xc4GU4JZIN+faZ7njl/fxfUNdlqvgZUUn" +
                    "kfdrzU3PZPl0w9NuncgEje/PZ+YtZvIsnH7MLSPeIGNQwW6V2kc8";

    private static String CustomData = null;

    static {
        log = LogFactory.getLog(VMOSProfileTests.class);
        try {
            CustomData = new String(Base64.encodeBase64(CustomDataContent.getBytes("UTF8")));
        } catch (UnsupportedEncodingException e) {
            log.fatal("CustomData encoding failed!");
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        log.debug("after class, clean resource group: " + m_rgName);
        cleanupResourceGroup();
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
    public void testVMWithLinuxProfile() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        ResourceContext context = createTestResourceContext(false);

        //set ImageRef to Linux
        context.setImageReference(
                ComputeHelper.getUbuntuServerDefaultImage(computeManagementClient, context.getLocation()));

        VirtualMachine vm = createVM(context, generateName("VM"), new ConsumerWrapper<VirtualMachine>() {
            @Override
            public void accept(VirtualMachine vm) {
                OSProfile osProfile = vm.getOSProfile();
                String sshPath = getSshPath(osProfile.getAdminUsername());
                osProfile.setCustomData(CustomData);

                // set linux configuration
                LinuxConfiguration linuxConfiguration = new LinuxConfiguration();
                linuxConfiguration.setDisablePasswordAuthentication(false);
                SshConfiguration sshConfiguration = new SshConfiguration();
                ArrayList<SshPublicKey> publicKeys = new ArrayList<SshPublicKey>(1);
                SshPublicKey sshPublicKey = new SshPublicKey();
                sshPublicKey.setPath(sshPath);
                sshPublicKey.setKeyData(DefaultSshPublicKey);
                publicKeys.add(sshPublicKey);
                sshConfiguration.setPublicKeys(publicKeys);
                linuxConfiguration.setSshConfiguration(sshConfiguration);
                osProfile.setLinuxConfiguration(linuxConfiguration);
            }
        });
        VirtualMachine vmInput = context.getVMInput();

        log.info("get created VM instance: " + vm.getName());
        VirtualMachineGetResponse vmInstanceResponse = computeManagementClient.getVirtualMachinesOperations()
                .getWithInstanceView(context.getResourceGroupName(), vmInput.getName());
        validateVMInstanceView(vmInput, vmInstanceResponse.getVirtualMachine());

        log.info("validate vm output");
        validateWinRMCustomDataAndUnattendContent(vm);
    }

    private void validateWinRMCustomDataAndUnattendContent(VirtualMachine vm) {
        OSProfile osProfile = vm.getOSProfile();
        Assert.assertEquals("os profile customData", CustomData, osProfile.getCustomData());
        Assert.assertNull("no windows configuration", osProfile.getWindowsConfiguration());
        Assert.assertNotNull("has Linux configuration", osProfile.getLinuxConfiguration());
        Assert.assertNotNull("has ssh configuration", osProfile.getLinuxConfiguration().getSshConfiguration());
        Assert.assertNotNull("has isDisablePasswordAuthentication",
                osProfile.getLinuxConfiguration().isDisablePasswordAuthentication());
        Assert.assertFalse("isDisablePasswordAuthentication is false",
                osProfile.getLinuxConfiguration().isDisablePasswordAuthentication());

        Assert.assertNotNull("has publicKeys", osProfile.getLinuxConfiguration().getSshConfiguration().getPublicKeys());
        ArrayList<SshPublicKey> publicKeys = osProfile.getLinuxConfiguration().getSshConfiguration().getPublicKeys();
        Assert.assertEquals("publicKeys size == 1", 1, publicKeys.size());
        Assert.assertEquals("ssh path", getSshPath(osProfile.getAdminUsername()), publicKeys.get(0).getPath());
        Assert.assertEquals("ssh key data", DefaultSshPublicKey, publicKeys.get(0).getKeyData());
    }

    private static String getSshPath(String adminUsername) {
        return String.format("%s%s%s",
                "/home/", adminUsername, "/.ssh/authorized_keys");
    }
}
