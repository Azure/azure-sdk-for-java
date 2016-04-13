package com.microsoft.azure.management.resources.fluentcore.utils;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.models.implementation.api.PageImpl;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.ArrayList;

public abstract class PagedListConverter<U, V> {
    public abstract V typeConvert(U u);

    public PagedList<V> convert(final PagedList<U> uList) {
        Page<U> uPage = uList.currentPage();
        PageImpl<V> vPage = new PageImpl<>();
        vPage.setNextPageLink(uList.nextpageLink());
        vPage.setItems(new ArrayList<V>());
        for (U u : uPage.getItems()) {
            vPage.getItems().add(typeConvert(u));
        }
        return new PagedList<V>(vPage) {
            @Override
            public Page<V> nextPage(String nextPageLink) throws RestException, IOException {
                Page<U> uPage = uList.currentPage();
                PageImpl<V> vPage = new PageImpl<>();
                vPage.setNextPageLink(uList.nextpageLink());
                vPage.setItems(new ArrayList<V>());
                for (U u : uPage.getItems()) {
                    vPage.getItems().add(typeConvert(u));
                }
                return vPage;
            }
        };
    }
}
