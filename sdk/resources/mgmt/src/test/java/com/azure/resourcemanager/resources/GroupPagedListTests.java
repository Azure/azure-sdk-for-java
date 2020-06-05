// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

///**
// * Copyright (c) Microsoft Corporation. All rights reserved.
// * Licensed under the MIT License. See License.txt in the project root for
// * license information.
// */
//
//package com.azure.management.resources;
//
//import com.microsoft.azure.Page;
//import com.microsoft.azure.PagedList;
//import com.azure.resourcemanager.resources.fluentcore.arm.Region;
//import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupPagedList;
//import com.azure.resourcemanager.resources.implementation.PageImpl;
//import com.azure.resourcemanager.resources.implementation.ResourceGroupInner;
//import com.microsoft.rest.ServiceCallback;
//import com.microsoft.rest.ServiceFuture;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import rx.Observable;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//public class GroupPagedListTests {
//    @Test
//    public void isResourceLoadedLazily() {
//        final List<PageImpl<ResourceGroup>> pages = Arrays.asList(
//                new PageImpl<ResourceGroup>(),
//                new PageImpl<ResourceGroup>(),
//                new PageImpl<ResourceGroup>(),
//                new PageImpl<ResourceGroup>()
//        );
//
//        pages.get(0).setItems(Arrays.asList(resourceGroup("RG1"),
//                resourceGroup("RG2"),
//                resourceGroup("RG3"),
//                resourceGroup("RG4"),
//                resourceGroup("RG5")));
//        pages.get(0).setNextPageLink("1");
//
//        pages.get(1).setItems(Arrays.asList(resourceGroup("RG6"),
//                resourceGroup("RG7"),
//                resourceGroup("RG8"),
//                resourceGroup("RG9"),
//                resourceGroup("RG10")));
//        pages.get(1).setNextPageLink("2");
//
//        pages.get(2).setItems(Arrays.asList(resourceGroup("RG11"),
//                resourceGroup("RG12"),
//                resourceGroup("RG13"),
//                resourceGroup("RG14"),
//                resourceGroup("RG15")));
//        pages.get(2).setNextPageLink("3");
//
//        pages.get(3).setItems(Arrays.asList(resourceGroup("RG16"),
//                resourceGroup("RG17"),
//                resourceGroup("RG18"),
//                resourceGroup("RG19"),
//                resourceGroup("RG20")));
//        pages.get(3).setNextPageLink(null);
//
//        List<String> expected = Arrays.asList(
//                "1",
//                "RG1Vm1", "RG1Vm2",
//                "RG2Vm1", "RG2Vm2",
//                "RG3Vm1", "RG3Vm2",
//                "RG4Vm1", "RG4Vm2",
//                "RG5Vm1", "RG5Vm2",
//                "2",
//                "RG6Vm1", "RG6Vm2",
//                "RG7AVm1", "RG7Vm2",
//                "RG8AVm1", "RG8Vm2",
//                "RG9AVm1", "RG9Vm2",
//                "RG10AVm1", "RG10Vm2",
//                "3",
//                "RG11Vm1", "RG11Vm2",
//                "RG12Vm1", "RG12Vm2",
//                "RG13Vm1", "RG13Vm2",
//                "RG14Vm1", "RG14Vm2",
//                "RG15Vm1", "RG15Vm2",
//                "RG16Vm1", "RG16Vm2",
//                "RG17Vm1", "RG17Vm2",
//                "RG18Vm1", "RG18Vm2",
//                "RG19Vm1", "RG19Vm2",
//                "RG20Vm1", "RG20Vm2"
//        );
//        final Iterator<String> itr = expected.iterator();
//
//        PagedList<ResourceGroup> pagedResourceList = new PagedList<ResourceGroup>(pages.get(0)) {
//            @Override
//            public Page<ResourceGroup> nextPage(String nextLink) {
//                Assertions.assertSame(itr.next(), nextLink);
//                int index = Integer.parseInt(nextLink);
//                return pages.get(index);
//            }
//        };
//
//        GroupPagedList<String> groupedResourceList = new GroupPagedList<String>(pagedResourceList) {
//            @Override
//            public List<String> listNextGroup(String s) {
//                List<String> groupItems = new ArrayList<>();
//                groupItems.add(s + "Vm1");
//                groupItems.add(s + "Vm2");
//                itr.next();
//                itr.next();
//
//                return groupItems;
//            }
//        };
//    }
//
//    private static ResourceGroup resourceGroup(final String name) {
//        return new ResourceGroup() {
//            @Override
//            public Update update() {
//                return null;
//            }
//
//            @Override
//            public String name() {
//                return name;
//            }
//
//            @Override
//            public String provisioningState() {
//                return null;
//            }
//
//            @Override
//            public String regionName() {
//                return null;
//            }
//
//            @Override
//            public Region region() {
//                return null;
//            }
//
//            @Override
//            public String id() {
//                return null;
//            }
//
//            @Override
//            public String type() {
//                return null;
//            }
//
//            @Override
//            public Map<String, String> tags() {
//                return null;
//            }
//
//            @Override
//            public ResourceGroupExportResult exportTemplate(ResourceGroupExportTemplateOptions options) {
//                return null;
//            }
//
//            @Override
//            public Observable<ResourceGroupExportResult> exportTemplateAsync(ResourceGroupExportTemplateOptions options) {
//                return null;
//            }
//
//            @Override
//            public ServiceFuture<ResourceGroupExportResult> exportTemplateAsync(ResourceGroupExportTemplateOptions options, ServiceCallback<ResourceGroupExportResult> callback) {
//                return null;
//            }
//
//            @Override
//            public String key() {
//                return null;
//            }
//
//            @Override
//            public ResourceGroup refresh() {
//                return refreshAsync().toBlocking().last();
//            }
//
//            @Override
//            public Observable<ResourceGroup> refreshAsync() {
//                return Observable.just(null);
//            }
//
//            @Override
//            public ResourceGroupInner inner() {
//                return null;
//            }
//        };
//    }
//}
