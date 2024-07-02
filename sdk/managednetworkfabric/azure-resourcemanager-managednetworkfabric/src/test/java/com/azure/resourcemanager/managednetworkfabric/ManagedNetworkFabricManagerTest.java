// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.managednetworkfabric;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.managednetworkfabric.models.AccessControlList;
import com.azure.resourcemanager.managednetworkfabric.models.AccessControlListAction;
import com.azure.resourcemanager.managednetworkfabric.models.AccessControlListMatchCondition;
import com.azure.resourcemanager.managednetworkfabric.models.AccessControlListMatchConfiguration;
import com.azure.resourcemanager.managednetworkfabric.models.AccessControlListPortCondition;
import com.azure.resourcemanager.managednetworkfabric.models.AclActionType;
import com.azure.resourcemanager.managednetworkfabric.models.CommonDynamicMatchConfiguration;
import com.azure.resourcemanager.managednetworkfabric.models.ConfigurationType;
import com.azure.resourcemanager.managednetworkfabric.models.IpAddressType;
import com.azure.resourcemanager.managednetworkfabric.models.IpGroupProperties;
import com.azure.resourcemanager.managednetworkfabric.models.IpMatchCondition;
import com.azure.resourcemanager.managednetworkfabric.models.Layer4Protocol;
import com.azure.resourcemanager.managednetworkfabric.models.PortGroupProperties;
import com.azure.resourcemanager.managednetworkfabric.models.PortType;
import com.azure.resourcemanager.managednetworkfabric.models.PrefixType;
import com.azure.resourcemanager.managednetworkfabric.models.SourceDestinationType;
import com.azure.resourcemanager.managednetworkfabric.models.VlanGroupProperties;
import com.azure.resourcemanager.managednetworkfabric.models.VlanMatchCondition;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Random;

public class ManagedNetworkFabricManagerTest extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private ManagedNetworkFabricManager managedNetworkFabricManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        managedNetworkFabricManager = ManagedNetworkFabricManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(REGION)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testCreateAccessController() {
        AccessControlList acl = null;
        String randomPadding = randomPadding();
        try {
            String aclName = "acl" + randomPadding;
            String matchName = "match" + randomPadding;
            String vlgName = "vlg" + randomPadding;
            String ipgName = "ipg" + randomPadding;
            String pgName = "pg" + randomPadding;
            String counterName = "counter" + randomPadding;

            // @embedmeStart
            acl = managedNetworkFabricManager
                .accessControlLists()
                .define(aclName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withConfigurationType(ConfigurationType.FILE)
                .withMatchConfigurations(
                    Collections.singletonList(
                        new AccessControlListMatchConfiguration()
                            .withMatchConfigurationName(matchName)
                            .withSequenceNumber(123L)
                            .withIpAddressType(IpAddressType.IPV4)
                            .withMatchConditions(
                                Collections.singletonList(
                                    new AccessControlListMatchCondition()
                                        .withProtocolTypes(Collections.singletonList("TCP"))
                                        .withVlanMatchCondition(
                                            new VlanMatchCondition()
                                                .withVlans(Collections.singletonList("20-30"))
                                                .withInnerVlans(Collections.singletonList("30"))
                                                .withVlanGroupNames(Collections.singletonList(vlgName)))
                                        .withIpCondition(
                                            new IpMatchCondition()
                                                .withType(SourceDestinationType.SOURCE_IP)
                                                .withPrefixType(PrefixType.PREFIX)
                                                .withIpPrefixValues(Collections.singletonList("10.20.20.20/12"))
                                                .withIpGroupNames(Collections.singletonList(ipgName)))
                                        .withEtherTypes(Collections.singletonList("0x1"))
                                        .withFragments(Collections.singletonList("0xff00-0xffff"))
                                        .withIpLengths(Collections.singletonList("4094-9214"))
                                        .withTtlValues(Collections.singletonList("23"))
                                        .withDscpMarkings(Collections.singletonList("32"))
                                        .withPortCondition(
                                            new AccessControlListPortCondition()
                                                .withPortType(PortType.SOURCE_PORT)
                                                .withLayer4Protocol(Layer4Protocol.TCP)
                                                .withPorts(Collections.singletonList("1-20"))
                                                .withPortGroupNames(Collections.singletonList(pgName))
                                                .withFlags(Collections.singletonList("established")))))
                            .withActions(
                                Collections.singletonList(
                                    new AccessControlListAction()
                                        .withType(AclActionType.COUNT)
                                        .withCounterName(counterName)))))
                .withDynamicMatchConfigurations(
                    Collections.singletonList(
                        new CommonDynamicMatchConfiguration()
                            .withIpGroups(
                                Collections.singletonList(
                                    new IpGroupProperties()
                                        .withName(ipgName)
                                        .withIpAddressType(IpAddressType.IPV4)
                                        .withIpPrefixes(Collections.singletonList("10.20.3.1/20"))))
                            .withVlanGroups(
                                Collections.singletonList(
                                    new VlanGroupProperties()
                                        .withName(vlgName)
                                        .withVlans(Collections.singletonList("20-30"))))
                            .withPortGroups(
                                Collections.singletonList(
                                    new PortGroupProperties()
                                        .withName(pgName)
                                        .withPorts(Collections.singletonList("100-200"))))))
                .withAnnotation("annotation")
                .create();
            // @embedmeEnd
            acl.refresh();
            Assertions.assertEquals(acl.name(), aclName);
            Assertions.assertEquals(acl.name(), managedNetworkFabricManager.accessControlLists().getById(acl.id()).name());
            Assertions.assertTrue(managedNetworkFabricManager.accessControlLists().list().stream().findAny().isPresent());
        } finally {
            if (acl != null) {
                managedNetworkFabricManager.accessControlLists().deleteById(acl.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
