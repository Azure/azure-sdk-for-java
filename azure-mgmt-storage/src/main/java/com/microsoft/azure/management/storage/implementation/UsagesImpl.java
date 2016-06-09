package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.storage.Usage;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
import com.microsoft.azure.management.storage.implementation.api.UsageInner;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.List;

/**
 * The implementation of Usage and its parent interfaces.
 */
class UsagesImpl
        implements Usages {
    private final StorageManagementClientImpl client;

    UsagesImpl(StorageManagementClientImpl client) {
        this.client = client;
    }

    @Override
    public PagedList<Usage> list() throws CloudException, IOException {
        PagedListConverter<UsageInner, Usage> converter = new PagedListConverter<UsageInner, Usage>() {
            @Override
            public Usage typeConvert(UsageInner resourceGroupInner) {
                return new UsageImpl(resourceGroupInner);
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
