// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//
// package com.azure.management;
//
// import com.microsoft.azure.management.resources.fluentcore.arm.Region;
// import com.microsoft.azure.management.search.*;
// import org.junit.Assert;
//
// import java.util.List;
//
// public class TestSearchService {
//
//    /**
//     * Search service test.
//     */
//    public static class SearchServiceAnySku extends TestTemplate<SearchService, SearchServices> {
//
//        @Override
//        public SearchService createResource(SearchServices searchServices) throws Exception {
//            final String newName = "ssrv" + this.testId;
//            String rgName = "rgSearch" + this.testId;
//
//            Assert.assertTrue(searchServices.checkNameAvailability(newName).isAvailable());
//
//            SearchService searchService = searchServices.define(newName)
//                .withRegion(Region.US_WEST)
//                .withNewResourceGroup()
//                .withSku(SkuName.STANDARD)
//                .create();
//
//            Assert.assertEquals(SkuName.STANDARD, searchService.sku().name());
//            Assert.assertEquals(1, searchService.replicaCount());
//            Assert.assertEquals(1, searchService.partitionCount());
//
//            AdminKeys adminKeys = searchService.getAdminKeys();
//            Assert.assertNotNull(adminKeys);
//            Assert.assertNotNull(adminKeys.primaryKey());
//            Assert.assertNotNull(adminKeys.secondaryKey());
//
//            List<QueryKey> queryKeys = searchService.listQueryKeys();
//            Assert.assertNotNull(queryKeys);
//            Assert.assertEquals(1, queryKeys.size());
//
//            return searchService;
//        }
//
//        @Override
//        public SearchService updateResource(SearchService resource) throws Exception {
//            resource.createQueryKey("testKey1");
//
//            resource = resource.update()
//                .withTag("tag2", "value2")
//                .withTag("tag3", "value3")
//                .withoutTag("tag1")
//                .withReplicaCount(1)
//                .withPartitionCount(1)
//                .apply();
//            Assert.assertTrue(resource.tags().containsKey("tag2"));
//            Assert.assertTrue(!resource.tags().containsKey("tag1"));
//            Assert.assertEquals(1, resource.replicaCount());
//            Assert.assertEquals(1, resource.partitionCount());
//            Assert.assertEquals(2, resource.listQueryKeys().size());
//
//            String adminKeyPrimary = resource.getAdminKeys().primaryKey();
//            String adminKeySecondary = resource.getAdminKeys().secondaryKey();
//
//            resource.deleteQueryKey(resource.listQueryKeys().get(1).key());
//            resource.regenerateAdminKeys(AdminKeyKind.PRIMARY);
//            resource.regenerateAdminKeys(AdminKeyKind.SECONDARY);
//            Assert.assertEquals(1, resource.listQueryKeys().size());
//            Assert.assertNotEquals(adminKeyPrimary, resource.getAdminKeys().primaryKey());
//            Assert.assertNotEquals(adminKeySecondary, resource.getAdminKeys().secondaryKey());
//
//            return resource;
//        }
//
//        @Override
//        public void print(SearchService resource) {
//            TestSearchService.print(resource, "Search Service with standard SKU: ");
//        }
//    }
//
//    /**
//     * Search service test with free sku.
//     */
//    public static class SearchServiceFreeSku extends TestTemplate<SearchService, SearchServices> {
//
//        @Override
//        public SearchService createResource(SearchServices searchServices) throws Exception {
//            final String newName = "ssrv" + this.testId;
//            String rgName = "rgSearch" + this.testId;
//            SearchService searchService = searchServices.define(newName)
//                .withRegion(Region.US_WEST)
//                .withNewResourceGroup()
//                .withFreeSku()
//                .create();
//
//            Assert.assertEquals(SkuName.FREE, searchService.sku().name());
//            Assert.assertEquals(1, searchService.replicaCount());
//            Assert.assertEquals(1, searchService.partitionCount());
//
//            return searchService;
//        }
//
//        @Override
//        public SearchService updateResource(SearchService resource) throws Exception {
//            resource = resource.update()
//                .withTag("tag2", "value2")
//                .withTag("tag3", "value3")
//                .withoutTag("tag1")
//                .apply();
//            Assert.assertTrue(resource.tags().containsKey("tag2"));
//            Assert.assertTrue(!resource.tags().containsKey("tag1"));
//            return resource;
//        }
//
//        @Override
//        public void print(SearchService resource) {
//            TestSearchService.print(resource, "Search Service with free SKU: ");
//        }
//    }
//
//    /**
//     * Search service test with basic sku.
//     */
//    public static class SearchServiceBasicSku extends TestTemplate<SearchService, SearchServices> {
//
//        @Override
//        public SearchService createResource(SearchServices searchServices) throws Exception {
//            final String newName = "ssrv" + this.testId;
//            String rgName = "rgSearch" + this.testId;
//            SearchService searchService = searchServices.define(newName)
//                .withRegion(Region.US_WEST)
//                .withNewResourceGroup()
//                .withBasicSku()
//                .withReplicaCount(1)
//                .create();
//
//            Assert.assertEquals(SkuName.BASIC, searchService.sku().name());
//            Assert.assertEquals(1, searchService.replicaCount());
//            Assert.assertEquals(1, searchService.partitionCount());
//
//            return searchService;
//        }
//
//        @Override
//        public SearchService updateResource(SearchService resource) throws Exception {
//            resource = resource.update()
//                .withTag("tag2", "value2")
//                .withTag("tag3", "value3")
//                .withoutTag("tag1")
//                .apply();
//            Assert.assertTrue(resource.tags().containsKey("tag2"));
//            Assert.assertTrue(!resource.tags().containsKey("tag1"));
//            return resource;
//        }
//
//        @Override
//        public void print(SearchService resource) {
//            TestSearchService.print(resource, "Search Service with basic SKU: ");
//        }
//    }
//
//    /**
//     * Search service test with standard sku.
//     */
//    public static class SearchServiceStandardSku extends TestTemplate<SearchService, SearchServices> {
//
//        @Override
//        public SearchService createResource(SearchServices searchServices) throws Exception {
//            final String newName = "ssrv" + this.testId;
//            String rgName = "rgSearch" + this.testId;
//            SearchService searchService = searchServices.define(newName)
//                .withRegion(Region.US_WEST)
//                .withNewResourceGroup()
//                .withStandardSku()
//                .withPartitionCount(2)
//                .withReplicaCount(1)
//                .create();
//
//            Assert.assertEquals(SkuName.STANDARD, searchService.sku().name());
//            Assert.assertEquals(1, searchService.replicaCount());
//            Assert.assertEquals(2, searchService.partitionCount());
//
//            return searchService;
//        }
//
//        @Override
//        public SearchService updateResource(SearchService resource) throws Exception {
//            resource = resource.update()
//                .withPartitionCount(1)
//                .withReplicaCount(1)
//                .withTag("tag2", "value2")
//                .withTag("tag3", "value3")
//                .withoutTag("tag1")
//                .apply();
//            Assert.assertTrue(resource.tags().containsKey("tag2"));
//            Assert.assertTrue(!resource.tags().containsKey("tag1"));
//            Assert.assertEquals(SkuName.STANDARD, resource.sku().name());
//            Assert.assertEquals(1, resource.replicaCount());
//            Assert.assertEquals(1, resource.partitionCount());
//
//            return resource;
//        }
//
//        @Override
//        public void print(SearchService resource) {
//            TestSearchService.print(resource, "Search Service with standard SKU: ");
//        }
//    }
//
//    /**
//     * Common print method.
//     *
//     * @param resource Search service resource
//     * @param header String to be printed first
//     */
//    public static void print(SearchService resource, String header) {
//        AdminKeys adminKeys = resource.getAdminKeys();
//        List<QueryKey> queryKeys = resource.listQueryKeys();
//
//        StringBuilder stringBuilder = new StringBuilder().append(header).append(resource.id())
//            .append("Name: ").append(resource.name())
//            .append("\n\tResource group: ").append(resource.resourceGroupName())
//            .append("\n\tRegion: ").append(resource.region())
//            .append("\n\tTags: ").append(resource.tags())
//            .append("\n\tSku: ").append(resource.sku().name())
//            .append("\n\tStatus: ").append(resource.status())
//            .append("\n\tStatus Details: ").append(resource.statusDetails())
//            .append("\n\tProvisioning State: ").append(resource.provisioningState())
//            .append("\n\tHosting Mode: ").append(resource.hostingMode())
//            .append("\n\tReplicas: ").append(resource.replicaCount())
//            .append("\n\tPartitions: ").append(resource.partitionCount())
//            .append("\n\tPrimary Admin Key: ").append(adminKeys.primaryKey())
//            .append("\n\tSecondary Admin Key: ").append(adminKeys.secondaryKey())
//            .append("\n\tQuery keys:");
//
//        for (QueryKey queryKey : queryKeys) {
//            stringBuilder.append("\n\t  Key name: ").append(queryKey.name());
//            stringBuilder.append("\n\t  Key value: ").append(queryKey.key());
//        }
//
//        System.out.println(stringBuilder);
//    }
//
// }
