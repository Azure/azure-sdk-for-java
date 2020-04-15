// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//
// package com.azure.management;
//
//
// import com.azure.core.management.SubResource;
// import com.azure.management.network.PublicIPPrefixSku;
// import com.azure.management.resources.fluentcore.arm.Region;
//
/// **
// * Tests public IP Prefix.
// */
// public class TestPublicIPPrefix extends TestTemplate<PublicIPPrefix, PublicIPPrefixes> {
//    @Override
//    public PublicIPPrefix createResource(PublicIPPrefixes pips) throws Exception {
//        final String newPipName = "pip" + this.testId;
//        PublicIPPrefix pip = pips.define(newPipName)
//                .withRegion(Region.US_WEST)
//                .withNewResourceGroup()
//                .withPrefixLength(28)
//                .withSku(new PublicIPPrefixSku().withName(PublicIPPrefixSkuName.STANDARD))
//                .create();
//
//        Assert.assertEquals(pip.prefixLength(), (Integer) 28);
//        Assert.assertEquals(pip.sku().name().toString(), "Standard");
//        Assert.assertTrue(pip.publicIPAddressVersion() == IPVersion.IPV4);
//        return pip;
//    }
//
//    @Override
//    public PublicIPPrefix updateResource(PublicIPPrefix resource) throws Exception {
//        resource = resource.update()
//                .withTag("tag1", "value1")
//                .withTag("tag2", "value2")
//                .apply();
//        Assert.assertEquals("value1", resource.tags().get("tag1"));
//        Assert.assertEquals("value2", resource.tags().get("tag2"));
//
//        return resource;
//    }
//
//    @Override
//    public void print(PublicIPPrefix pip) {
//        TestPublicIPPrefix.printPIP(pip);
//    }
//
//    public static void printPIP(PublicIPPrefix resource) {
//        StringBuilder info = new StringBuilder().append("Public IP Address: ").append(resource.id())
//                .append("\n\tName: ").append(resource.name())
//                .append("\n\tResource group: ").append(resource.resourceGroupName())
//                .append("\n\tRegion: ").append(resource.region())
//                .append("\n\tTags: ").append(resource.tags())
//                .append("\n\tAvailability Zones: ").append(resource.availabilityZones())
//                .append("\n\tLeaf domain label: ").append(resource.sku())
//                .append("\n\tFQDN: ").append(resource.publicIPAddresses())
//                .append("\n\tReverse FQDN: ").append(resource.publicIPAddressVersion());
//
//        // Show the associated load balancer if any
//        info.append("\n\tLoad balancer association: ");
//        if (resource.loadBalancerFrontendIpConfiguration() != null) {
//            final SubResource frontend = resource.loadBalancerFrontendIpConfiguration();
//            info.append("\n\t\tLoad balancer ID: ").append(frontend.id());
//        } else {
//            info.append("(None)");
//        }
//
//        System.out.println(info.toString());
//    }
// }
