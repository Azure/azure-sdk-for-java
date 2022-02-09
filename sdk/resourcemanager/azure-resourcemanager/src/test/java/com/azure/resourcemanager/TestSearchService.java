// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.Region;
import com.azure.resourcemanager.search.models.AdminKeyKind;
import com.azure.resourcemanager.search.models.AdminKeys;
import com.azure.resourcemanager.search.models.QueryKey;
import com.azure.resourcemanager.search.models.SearchService;
import com.azure.resourcemanager.search.models.SearchServices;
import com.azure.resourcemanager.search.models.SkuName;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Assertions;

public class TestSearchService {

    /**
     * Search service test.
     */
    public static class SearchServiceAnySku extends TestTemplate<SearchService, SearchServices> {

        @Override
        public SearchService createResource(SearchServices searchServices) throws Exception {
            final String newName = searchServices.manager().resourceManager().internalContext().randomResourceName("ssrv", 15);

            Assertions.assertTrue(searchServices.checkNameAvailability(newName).isNameAvailable());

            SearchService searchService = searchServices.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withSku(SkuName.STANDARD)
                .create();

            Assertions.assertEquals(SkuName.STANDARD, searchService.sku().name());
            Assertions.assertEquals(1, searchService.replicaCount());
            Assertions.assertEquals(1, searchService.partitionCount());

            AdminKeys adminKeys = searchService.getAdminKeys();
            Assertions.assertNotNull(adminKeys);
            Assertions.assertNotNull(adminKeys.primaryKey());
            Assertions.assertNotNull(adminKeys.secondaryKey());

            PagedIterable<QueryKey> queryKeys = searchService.listQueryKeys();
            Assertions.assertNotNull(queryKeys);
            Assertions.assertEquals(1, TestUtilities.getSize(queryKeys));

            return searchService;
        }

        @Override
        public SearchService updateResource(SearchService resource) throws Exception {
            resource.createQueryKey("testKey1");

            resource = resource.update()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .withReplicaCount(1)
                .withPartitionCount(1)
                .apply();
            Assertions.assertTrue(resource.tags().containsKey("tag2"));
            Assertions.assertTrue(!resource.tags().containsKey("tag1"));
            Assertions.assertEquals(1, resource.replicaCount());
            Assertions.assertEquals(1, resource.partitionCount());
            Assertions.assertEquals(2, TestUtilities.getSize(resource.listQueryKeys()));

            String adminKeyPrimary = resource.getAdminKeys().primaryKey();
            String adminKeySecondary = resource.getAdminKeys().secondaryKey();

            resource.deleteQueryKey(resource.listQueryKeys().iterator().next().key());
            resource.regenerateAdminKeys(AdminKeyKind.PRIMARY);
            resource.regenerateAdminKeys(AdminKeyKind.SECONDARY);
            Assertions.assertEquals(1, TestUtilities.getSize(resource.listQueryKeys()));
            if (TestUtils.isRecordMode()) {
                Assertions.assertNotEquals(adminKeyPrimary, resource.getAdminKeys().primaryKey());
                Assertions.assertNotEquals(adminKeySecondary, resource.getAdminKeys().secondaryKey());
            }

            return resource;
        }

        @Override
        public void print(SearchService resource) {
            TestSearchService.print(resource, "Search Service with standard SKU: ");
        }
    }

    /**
     * Search service test with free sku.
     */
    public static class SearchServiceFreeSku extends TestTemplate<SearchService, SearchServices> {

        @Override
        public SearchService createResource(SearchServices searchServices) throws Exception {
            final String newName = searchServices.manager().resourceManager().internalContext().randomResourceName("ssrv", 15);

            SearchService searchService = searchServices.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withFreeSku()
                .create();

            Assertions.assertEquals(SkuName.FREE, searchService.sku().name());
            Assertions.assertEquals(1, searchService.replicaCount());
            Assertions.assertEquals(1, searchService.partitionCount());

            return searchService;
        }

        @Override
        public SearchService updateResource(SearchService resource) throws Exception {
            resource = resource.update()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
            Assertions.assertTrue(resource.tags().containsKey("tag2"));
            Assertions.assertTrue(!resource.tags().containsKey("tag1"));
            return resource;
        }

        @Override
        public void print(SearchService resource) {
            TestSearchService.print(resource, "Search Service with free SKU: ");
        }
    }

    /**
     * Search service test with basic sku.
     */
    public static class SearchServiceBasicSku extends TestTemplate<SearchService, SearchServices> {

        @Override
        public SearchService createResource(SearchServices searchServices) throws Exception {
            final String newName = searchServices.manager().resourceManager().internalContext().randomResourceName("ssrv", 15);

            SearchService searchService = searchServices.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withBasicSku()
                .withReplicaCount(1)
                .create();

            Assertions.assertEquals(SkuName.BASIC, searchService.sku().name());
            Assertions.assertEquals(1, searchService.replicaCount());
            Assertions.assertEquals(1, searchService.partitionCount());

            return searchService;
        }

        @Override
        public SearchService updateResource(SearchService resource) throws Exception {
            resource = resource.update()
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
            Assertions.assertTrue(resource.tags().containsKey("tag2"));
            Assertions.assertTrue(!resource.tags().containsKey("tag1"));
            return resource;
        }

        @Override
        public void print(SearchService resource) {
            TestSearchService.print(resource, "Search Service with basic SKU: ");
        }
    }

    /**
     * Search service test with standard sku.
     */
    public static class SearchServiceStandardSku extends TestTemplate<SearchService, SearchServices> {

        @Override
        public SearchService createResource(SearchServices searchServices) throws Exception {
            final String newName = searchServices.manager().resourceManager().internalContext().randomResourceName("ssrv", 15);

            SearchService searchService = searchServices.define(newName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup()
                .withStandardSku()
                .withPartitionCount(2)
                .withReplicaCount(1)
                .create();

            Assertions.assertEquals(SkuName.STANDARD, searchService.sku().name());
            Assertions.assertEquals(1, searchService.replicaCount());
            Assertions.assertEquals(2, searchService.partitionCount());

            return searchService;
        }

        @Override
        public SearchService updateResource(SearchService resource) throws Exception {
            resource = resource.update()
                .withPartitionCount(1)
                .withReplicaCount(1)
                .withTag("tag2", "value2")
                .withTag("tag3", "value3")
                .withoutTag("tag1")
                .apply();
            Assertions.assertTrue(resource.tags().containsKey("tag2"));
            Assertions.assertTrue(!resource.tags().containsKey("tag1"));
            Assertions.assertEquals(SkuName.STANDARD, resource.sku().name());
            Assertions.assertEquals(1, resource.replicaCount());
            Assertions.assertEquals(1, resource.partitionCount());

            return resource;
        }

        @Override
        public void print(SearchService resource) {
            TestSearchService.print(resource, "Search Service with standard SKU: ");
        }
    }

    /**
     * Common print method.
     *
     * @param resource Search service resource
     * @param header String to be printed first
     */
    public static void print(SearchService resource, String header) {
        AdminKeys adminKeys = resource.getAdminKeys();
        PagedIterable<QueryKey> queryKeys = resource.listQueryKeys();

        StringBuilder stringBuilder = new StringBuilder().append(header).append(resource.id())
            .append("Name: ").append(resource.name())
            .append("\n\tResource group: ").append(resource.resourceGroupName())
            .append("\n\tRegion: ").append(resource.region())
            .append("\n\tTags: ").append(resource.tags())
            .append("\n\tSku: ").append(resource.sku().name())
            .append("\n\tStatus: ").append(resource.status())
            .append("\n\tStatus Details: ").append(resource.statusDetails())
            .append("\n\tProvisioning State: ").append(resource.provisioningState())
            .append("\n\tHosting Mode: ").append(resource.hostingMode())
            .append("\n\tReplicas: ").append(resource.replicaCount())
            .append("\n\tPartitions: ").append(resource.partitionCount())
            .append("\n\tPrimary Admin Key: ").append(adminKeys.primaryKey())
            .append("\n\tSecondary Admin Key: ").append(adminKeys.secondaryKey())
            .append("\n\tQuery keys:");

        for (QueryKey queryKey : queryKeys) {
            stringBuilder.append("\n\t  Key name: ").append(queryKey.name());
            stringBuilder.append("\n\t  Key value: ").append(queryKey.key());
        }

        System.out.println(stringBuilder);
    }

}
