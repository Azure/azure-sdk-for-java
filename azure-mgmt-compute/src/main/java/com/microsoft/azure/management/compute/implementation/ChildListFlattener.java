package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

final class ChildListFlattener<T, U> {
    protected final String lastPageMarker = "LASTPAGE";
    protected Iterator<T> parentItr;
    protected PagedList<U> currentChildList;
    protected PagedList<U> nextChildList;
    private final ChildListLoader<T, U> childListLoader;

    interface ChildListLoader<T, U> {
        PagedList<U> loadList(T parent) throws CloudException, IOException;
    }

    public ChildListFlattener(PagedList<T> parentList, ChildListLoader<T, U> childListLoader) {
        this.parentItr = parentList.iterator();
        this.childListLoader = childListLoader;
    }

    public PagedList<U> flatten() throws CloudException, IOException {
        this.currentChildList = nextChildList();
        if (this.currentChildList == null) {
            return emptyPagedList();
        }
        this.nextChildList = nextChildList();
        return new PagedList<U>(childListPage(currentChildList.currentPage(), this.nextChildList)) {
            @Override
            public Page<U> nextPage(String nextPageLink) throws RestException, IOException {
                if (!nextPageLink.equalsIgnoreCase(lastPageMarker)) {
                    currentChildList.loadNextPage();
                    if (currentChildList.currentPage().getNextPageLink() == null) {
                        nextChildList = nextChildList();
                    }
                    return childListPage(currentChildList.currentPage(), nextChildList);
                } else {
                    currentChildList = nextChildList;
                    nextChildList = nextChildList();
                    return childListPage(currentChildList.currentPage(), nextChildList);
                }
            }
        };
    }

    private PagedList<U> nextChildList() throws CloudException, IOException {
        while (parentItr.hasNext()) {
            PagedList<U> nextChildList = childListLoader.loadList(parentItr.next());
            if (nextChildList.iterator().hasNext()) {
                return nextChildList;
            }
        }
        return null;
    }

    private Page<U> childListPage(final Page<U> page, final PagedList<U> nextChildList) {
        return new Page<U>() {
            @Override
            public String getNextPageLink() {
                if (page.getNextPageLink() == null) {
                    if (nextChildList != null) {
                        return lastPageMarker;
                    }
                    return null;
                }
                return page.getNextPageLink();
            }

            @Override
            public List<U> getItems() {
                return page.getItems();
            }
        };
    }

    private PagedList<U> emptyPagedList() {
        return new PagedList<U>() {
            @Override
            public Page<U> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }
}