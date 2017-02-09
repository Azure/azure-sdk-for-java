/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ChildListFlattener that can take a paged list of parents and flatten their child lists
 * as a single lazy paged list.
 *
 * @param <ParentT> the type of parent paged list item
 * @param <ChildT> the type of child paged list item
 */
final class ChildListFlattener<ParentT, ChildT> {
    private final String switchToCousin = "switchToCousin";
    private Iterator<ParentT> parentItr;
    private PagedList<ChildT> currentChildList;
    private final ChildListLoader<ParentT, ChildT> childListLoader;

    /**
     * Interface that will be implemented by the consumer of {@link ChildListFlattener}.
     * <p>
     * implementation will be used by {@link ChildListFlattener#flatten()} to load child
     * paged list of parents in the parent paged list.
     *
     * @param <T> the parent type
     * @param <U> the type of items in the child list
     */
    interface ChildListLoader<T, U> {
        /**
         * Get the child paged list associated with the given parent.
         *
         * @param parent the parent
         * @return child paged list associated with the parent
         * @throws CloudException exceptions thrown from the cloud
         * @throws IOException exceptions thrown from serialization/deserialization
         */
        PagedList<U> loadList(T parent);
    }

    /**
     * Creates ChildListFlattener.
     *
     * @param parentList a paged list of parents
     * @param childListLoader {@link ChildListLoader} for fetching child paged list associated any parent
     */
    ChildListFlattener(PagedList<ParentT> parentList, ChildListLoader<ParentT, ChildT> childListLoader) {
        this.parentItr = parentList.iterator();
        this.childListLoader = childListLoader;
    }

    /**
     * flatten the  child paged lists.
     *
     * @return the lazy flattened paged list from the child paged lists
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    public PagedList<ChildT> flatten() {
        this.currentChildList = nextChildList();
        if (this.currentChildList == null) {
            return emptyPagedList();
        }
        return new PagedList<ChildT>(childListPage(currentChildList.currentPage())) {
            @Override
            public Page<ChildT> nextPage(String nextPageLink) throws RestException, IOException {
                if (nextPageLink.equalsIgnoreCase(switchToCousin)) {
                    // Reached end of current child paged list, make next child list(cousin) as current
                    // paged list and return it's first page.
                    currentChildList = nextChildList();
                    return childListPage(currentChildList.currentPage());
                } else {
                    currentChildList.loadNextPage();
                    return childListPage(currentChildList.currentPage());
                }
            }
        };
    }

    /**
     * Returns the next child paged list containing at least one item.
     * <p>
     * This method iterate the parent list from where it stopped last time and return
     * a non-empty child paged list of a parent. If there is no parent with non-empty child list
     * or if the parent list iteration is finished then this method returns null.
     *
     * @return a child paged list {@link PagedList}
     * @throws CloudException exceptions thrown from the cloud
     * @throws IOException exceptions thrown from serialization/deserialization
     */
    private PagedList<ChildT> nextChildList() {
        while (parentItr.hasNext()) {
            PagedList<ChildT> nextChildList = childListLoader.loadList(parentItr.next());
            if (nextChildList.iterator().hasNext()) {
                return nextChildList;
            }
        }
        return emptyPagedList();
    }

    /**
     * Method returns a {@link Page} with the same items as in the given page, if the given
     * page is last page of the current paged child list and if there is a cousin list then
     * returned page's next-link will be set to a predefined value indicating presence of
     * cousin list.
     *
     * @param page the page
     * @return page with next-link updated if there is a cousin
     */
    private Page<ChildT> childListPage(final Page<ChildT> page) {
        return new Page<ChildT>() {
            @Override
            public String nextPageLink() {
                if (page.nextPageLink() != null) {
                   // The current child paged list has more pages.
                   return page.nextPageLink();
                }

                if (parentItr.hasNext()) {
                    // The current child paged list has no more pages so switch to it's cousin list
                    return switchToCousin;
                }
                // reached end of child paged list of last parent, iteration will be stopped.
                return null;
            }

            @Override
            public List<ChildT> items() {
                return page.items();
            }
        };
    }

    /**
     * @return an empty paged list
     */
    private PagedList<ChildT> emptyPagedList() {
        return new PagedList<ChildT>(emptyPage()) {
            @Override
            public Page<ChildT> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }

    private Page<ChildT> emptyPage() {
        return new Page<ChildT>() {
            @Override
            public String nextPageLink() {
                return null;
            }

            @Override
            public List<ChildT> items() {
                return new ArrayList<>();
            }
        };
    }
}