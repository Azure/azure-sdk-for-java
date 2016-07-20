package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.PageImpl;
import com.microsoft.azure.management.storage.StorageUsage;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.List;

/**
 * The implementation of {@link Usages}.
 */
class UsagesImpl
        implements Usages {
    private final StorageManagementClientImpl client;

    UsagesImpl(StorageManagementClientImpl client) {
        this.client = client;
    }

    @Override
    public PagedList<StorageUsage> list() throws CloudException, IOException {
        PagedListConverter<UsageInner, StorageUsage> converter =
                new PagedListConverter<UsageInner, StorageUsage>() {
            @Override
            public StorageUsage typeConvert(UsageInner usageInner) {
                return new UsageImpl(usageInner);
            }
        };
        return converter.convert(toPagedList(client.usages().list().getBody()));
    }

    private PagedList<UsageInner> toPagedList(List<UsageInner> list) {
        PageImpl<UsageInner> page = new PageImpl<>();
        page.setItems(list);
        page.setNextPageLink(null);
        return new PagedList<UsageInner>(page) {
            @Override
            public Page<UsageInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }
}
