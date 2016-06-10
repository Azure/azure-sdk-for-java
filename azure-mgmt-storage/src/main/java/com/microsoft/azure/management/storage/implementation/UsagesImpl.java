package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.storage.Usage;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.azure.management.storage.implementation.api.StorageManagementClientImpl;
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
        PagedListConverter<com.microsoft.azure.management.storage.implementation.api.Usage, Usage> converter =
                new PagedListConverter<com.microsoft.azure.management.storage.implementation.api.Usage, Usage>() {
            @Override
            public Usage typeConvert(com.microsoft.azure.management.storage.implementation.api.Usage usageInner) {
                return new UsageImpl(usageInner);
            }
        };
        return converter.convert(toPagedList(client.usages().list().getBody().value()));
    }

    private PagedList<com.microsoft.azure.management.storage.implementation.api.Usage> toPagedList(List<com.microsoft.azure.management.storage.implementation.api.Usage> list) {
        PageImpl<com.microsoft.azure.management.storage.implementation.api.Usage> page = new PageImpl<>();
        page.setItems(list);
        page.setNextPageLink(null);
        return new PagedList<com.microsoft.azure.management.storage.implementation.api.Usage>(page) {
            @Override
            public Page<com.microsoft.azure.management.storage.implementation.api.Usage> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }
}
