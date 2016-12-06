/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.implementation.PageImpl;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The base class for converting {@link PagedList} of one type of resource to
 * another, without polling down all the items in a list.
 * This converter is useful in converting inner top level resources into fluent
 * top level resources.
 *
 * @param <U> the type of Resource to convert from
 * @param <V> the type of Resource to convert to
 */
public abstract class PagedListConverter<U, V> {
    /**
     * Override this method to define how to convert each Resource item
     * individually.
     *
     * @param u the resource to convert from
     * @return the converted resource
     */
    public abstract V typeConvert(U u);

    /**
     * Converts the paged list.
     *
     * @param uList the resource list to convert from
     * @return the converted list
     */
    public PagedList<V> convert(final PagedList<U> uList) {
        if (uList == null || uList.isEmpty()) {
            return new PagedList<V>() {
                @Override
                public Page<V> nextPage(String s) throws RestException, IOException {
                    return null;
                }
            };
        }
        Page<U> uPage = uList.currentPage();
        PageImpl<V> vPage = new PageImpl<>();
        vPage.setNextPageLink(uPage.getNextPageLink());
        vPage.setItems(new ArrayList<V>());
        for (U u : uPage.getItems()) {
            vPage.getItems().add(typeConvert(u));
        }
        return new PagedList<V>(vPage) {
            @Override
            public Page<V> nextPage(String nextPageLink) throws RestException, IOException {
                Page<U> uPage = uList.nextPage(nextPageLink);
                PageImpl<V> vPage = new PageImpl<>();
                vPage.setNextPageLink(uPage.getNextPageLink());
                vPage.setItems(new ArrayList<V>());
                for (U u : uPage.getItems()) {
                    vPage.getItems().add(typeConvert(u));
                }
                return vPage;
            }
        };
    }
}
