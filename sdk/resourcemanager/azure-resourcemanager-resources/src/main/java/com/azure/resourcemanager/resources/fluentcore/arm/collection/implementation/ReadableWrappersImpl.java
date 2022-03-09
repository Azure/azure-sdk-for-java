// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Base class for readable wrapper collections, i.e. those whose models can only be read, not created.
 * (Internal use only)
 *
 * @param <T> the individual resource type returned
 * @param <ImplT> the individual resource implementation
 * @param <InnerT> the wrapper inner type
 */
public abstract class ReadableWrappersImpl<
        T,
        ImplT extends T,
        InnerT> {

    protected ReadableWrappersImpl() {
    }

    protected abstract ImplT wrapModel(InnerT inner);

    protected PagedIterable<T> wrapList(PagedIterable<InnerT> pagedList) {
        return PagedConverter.mapPage(pagedList, innerT -> wrapModel(innerT));
    }

    protected PagedFlux<T> wrapPageAsync(PagedFlux<InnerT> innerPage) {
        return PagedConverter.mapPage(innerPage, innerT -> wrapModel(innerT));
    }

//    protected PagedIterable<T> wrapList(List<InnerT> list) {
//        return wrapList(ReadableWrappersImpl.convertToPagedList(list));
//    }
//
//    /**
//     * Converts the List to PagedList.
//     *
//     * @param list     list to be converted in to paged list
//     * @param <InnerT> the wrapper inner type
//     * @return the Paged list for the inner type.
//     */
//    public static <InnerT> PagedIterable<InnerT> convertToPagedList(List<InnerT> list) {
//        PageImpl<InnerT> page = new PageImpl<>();
//        page.setItems(list);
//        page.setNextPageLink(null);
//        return new PagedIterable<>(page) {
//            @Override
//            public Page<InnerT> nextPage(String nextPageLink) {
//                return null;
//            }
//        };
//    }
//
//
//    protected Flux<T> wrapPageAsync(Flux<Page<InnerT>> innerPage) {
//        return wrapModelAsync(convertPageToInnerAsync(innerPage));
//    }
//
//    protected Flux<T> wrapListAsync(Flux<List<InnerT>> innerList) {
//        return wrapModelAsync(convertListToInnerAsync(innerList));
//    }
//
//    /**
//     * Converts Flux of list to Flux of Inner.
//     *
//     * @param innerList list to be converted.
//     * @param <InnerT>  type of inner.
//     * @return Flux for list of inner.
//     */
//    public static <InnerT> Flux<InnerT> convertListToInnerAsync(Flux<List<InnerT>> innerList) {
//        return innerList.flatMap(list -> Flux.fromIterable(list));
//    }
//
//    /**
//     * Converts Flux of page to Flux of Inner.
//     *
//     * @param <InnerT>  type of inner.
//     * @param innerPage Page to be converted.
//     * @return Flux for list of inner.
//     */
//    public static <InnerT> Flux<InnerT> convertPageToInnerAsync(Flux<Page<InnerT>> innerPage) {
//        return innerPage.flatMap(page -> Flux.fromIterable(page.getItems()));
//    }
//
//    private Flux<T> wrapModelAsync(Flux<InnerT> inner) {
//        return inner.map(i -> wrapModel(i));
//    }
}
