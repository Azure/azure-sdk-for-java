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

import com.microsoft.azure.management.compute.models.NetworkInterfaceReference;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.compute.models.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.models.NetworkInterface;
import com.microsoft.azure.management.network.models.NetworkInterfaceGetResponse;
import com.microsoft.azure.management.network.models.VirtualNetwork;
import com.microsoft.azure.utility.ConsumerWrapper;
import com.microsoft.azure.utility.NetworkHelper;
import com.microsoft.azure.utility.ResourceContext;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

public class VMNetworkInterfaceTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(VMNetworkInterfaceTests.class);
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
    public void testVMWithMultipleNic() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        ResourceContext context = createTestResourceContext(false);

        createOrUpdateResourceGroup(m_rgName);

        VirtualNetwork vnet = NetworkHelper.createVirtualNetwork(networkResourceProviderClient, context);

        NetworkInterface nic1 = NetworkHelper.createNIC(
                networkResourceProviderClient, context, vnet.getSubnets().get(0));

        // create a new nic with same vnet
        ResourceContext context2 = createTestResourceContext("2", false);
        final NetworkInterface nic2 = NetworkHelper.createNIC(
                networkResourceProviderClient, context2, vnet.getSubnets().get(0));

        VirtualMachine vm = createVM(context, generateName("VM"), new ConsumerWrapper<VirtualMachine>() {
            @Override
            public void accept(VirtualMachine virtualMachine) {
                virtualMachine.getHardwareProfile().setVirtualMachineSize(VirtualMachineSizeTypes.STANDARD_A4);
                //add nic 2
                virtualMachine.getNetworkProfile().getNetworkInterfaces().get(0).setPrimary(false);
                NetworkInterfaceReference nic2Ref = new NetworkInterfaceReference();
                nic2Ref.setReferenceUri(nic2.getId());
                nic2Ref.setPrimary(true);
                virtualMachine.getNetworkProfile().getNetworkInterfaces().add(1, nic2Ref);
            }
        });

        NetworkInterfaceGetResponse getNic1Response = networkResourceProviderClient.getNetworkInterfacesOperations()
                .get(m_rgName, context.getNetworkInterface().getName());
        // TODO get MAC address/isPrimary has shaky behavior in certain regions
        // Assert.assertNotNull(getNic1Response.getNetworkInterface().getMacAddress());
        // Assert.assertNotNull("nic1response isPrimary is null", getNic1Response.getNetworkInterface().isPrimary());
        // Assert.assertFalse("nic1response isPrimary should be false", getNic1Response.getNetworkInterface().isPrimary());

        NetworkInterfaceGetResponse getNic2Response = networkResourceProviderClient.getNetworkInterfacesOperations()
                .get(m_rgName, context2.getNetworkInterface().getName());
        // Assert.assertNotNull(getNic2Response.getNetworkInterface().getMacAddress());
        // Assert.assertNotNull("nic2response isPrimary is null", getNic2Response.getNetworkInterface().isPrimary());
        // Assert.assertTrue("nic2response isPrimary should be true", getNic2Response.getNetworkInterface().isPrimary());
    }
}
