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
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.windowsazure.exception.ServiceException;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.util.function.Consumer;

public class VMMarketplaceTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(VMMarketplaceTests.class);
    }

    public static final String VMMPublisherName = "datastax";
    public static final String VMMOfferName = "datastax-enterprise-non-production-use-only";
    public static final String VMMSku = "sandbox_single-node";

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        log.info("after class, clean resource group: " + rgName);
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
    public void testVMMarketplace() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        ResourceContext context = createTestResourceContext(false);

        final VirtualMachineImage image = ComputeHelper.getMarketplaceVMImage(
                computeManagementClient, context.getLocation(), VMMPublisherName, VMMOfferName, VMMSku);

        try
        {
        VirtualMachine vm = createVM(context, generateName("VM"), new Consumer<VirtualMachine>() {
            @Override
            public void accept(VirtualMachine vm) {
                vm.getStorageProfile().setDataDisks(null);

                ImageReference ir = new ImageReference();
                ir.setPublisher(VMMPublisherName);
                ir.setOffer(VMMOfferName);
                ir.setSku(VMMSku);
                ir.setVersion(image.getName());
                vm.getStorageProfile().setImageReference(ir);

                Plan plan = new Plan();
                plan.setName(image.getPurchasePlan().getName());
                plan.setProduct(image.getPurchasePlan().getProduct());
                plan.setPromotionCode(null);
                plan.setPublisher(image.getPurchasePlan().getPublisher());
                vm.setPlan(plan);

            }
        });
        } catch (ServiceException se) {
            log.info("Get ServiceException response.");
            Assert.assertTrue("Catch ResourcePurchaseValidationFailed",
                    se.getMessage().contains("ResourcePurchaseValidationFailed"));
        }

        // TODO find a way to set a valid image with purchase plan
        // validateMarketplaceVMPlanField(vm, image);
    }

    private void validateMarketplaceVMPlanField(VirtualMachine vm, VirtualMachineImage image) {
        Assert.assertNotNull(vm.getPlan());
        Assert.assertEquals(image.getPurchasePlan().getPublisher(), vm.getPlan().getPublisher());
        Assert.assertEquals(image.getPurchasePlan().getProduct(), vm.getPlan().getProduct());
        Assert.assertEquals(image.getPurchasePlan().getName(), vm.getPlan().getName());
    }
}
