/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

import com.azure.common.http.rest.RestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Defines a list response from a paging operation. The pages are
 * lazy initialized when an instance of this class is iterated.
 *
 * @param <E> the element type.
 */
public abstract class PagedList<E> implements List<E> {
    /** The actual items in the list. */
    private List<E> items;
    /** Stores the latest page fetched. */
    private Page<E> currentPage;
    /** Cached page right after the current one. */
    private Page<E> cachedPage;

    /**
     * Creates an instance of Pagedlist.
     */
    public PagedList() {
        items = new ArrayList<>();
    }

    /**
     * Creates an instance of PagedList from a {@link Page} response.
     *
     * @param page the {@link Page} object.
     */
    public PagedList(Page<E> page) {
        this();
        if (page == null) {
            return;
        }
        List<E> retrievedItems = page.items();
        if (retrievedItems != null) {
            items.addAll(retrievedItems);
        }
        currentPage = page;
        cachePage(page.nextPageLink());
    }

    private void cachePage(String nextPageLink) {
        try {
            while (nextPageLink != null && nextPageLink != "") {
                cachedPage = nextPage(nextPageLink);
                if (cachedPage == null) {
                    break;
                }
                nextPageLink = cachedPage.nextPageLink();
                if (hasNextPage()) {
                    // a legit, non-empty page has been fetched, otherwise keep fetching
                    break;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Override this method to load the next page of items from a next page link.
     *
     * @param nextPageLink the link to get the next page of items.
     * @return the {@link Page} object storing a page of items and a link to the next page.
     * @throws RestException thrown if an error is raised from Azure.
     * @throws IOException thrown if there's any failure in deserialization.
     */
    public abstract Page<E> nextPage(String nextPageLink) throws RestException, IOException;

    /**
     * If there are more pages available.
     *
     * @return true if there are more pages to load. False otherwise.
     */
    public boolean hasNextPage() {
        return this.cachedPage != null && this.cachedPage.items() != null && !this.cachedPage.items().isEmpty();
    }

    /**
     * Loads a page from next page link.
     * The exceptions are wrapped into Java Runtime exceptions.
     */
    public void loadNextPage() {
        this.currentPage = cachedPage;
        cachedPage = null;
        this.items.addAll(currentPage.items());
        cachePage(currentPage.nextPageLink());
    }

    /**
     * Keep loading the next page from the next page link until all items are loaded.
     */
    public void loadAll() {
        while (hasNextPage()) {
            loadNextPage();
        }
    }

    /**
     * Gets the latest page fetched.
     *
     * @return the latest page.
     */
    public Page<E> currentPage() {
        return currentPage;
    }

    /**
     * Sets the current page.
     *
     * @param currentPage the current page.
     */
    protected void setCurrentPage(Page<E> currentPage) {
        this.currentPage = currentPage;
        List<E> retrievedItems = currentPage.items();
        if (retrievedItems != null) {
            items.addAll(retrievedItems);
        }
        cachePage(currentPage.nextPageLink());
    }

    /**
     * The implementation of {@link ListIterator} for PagedList.
     */
    private class ListItr implements ListIterator<E> {
        /**
         * index of next element to return.
         */
        private int nextIndex;
        /**
         * index of last element returned; -1 if no such action happened.
         */
        private int lastRetIndex = -1;

        /**
         * Creates an instance of the ListIterator.
         *
         * @param index the position in the list to start.
         */
        ListItr(int index) {
            this.nextIndex = index;
        }

        @Override
        public boolean hasNext() {
            return this.nextIndex != items.size() || hasNextPage();
        }

        @Override
        public E next() {
            if (this.nextIndex >= items.size()) {
                if (!hasNextPage()) {
                    throw new NoSuchElementException();
                } else {
                    loadNextPage();
                }
                // Recurse until we load a page with non-zero items.
                return next();
            } else {
                try {
                    E nextItem = items.get(this.nextIndex);
                    this.lastRetIndex = this.nextIndex;
                    this.nextIndex = this.nextIndex + 1;
                    return nextItem;
                } catch (IndexOutOfBoundsException ex) {
                    // The nextIndex got invalid means a different instance of iterator
                    // removed item from this index.
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public void remove() {
            if (this.lastRetIndex < 0) {
                throw new IllegalStateException();
            } else {
                try {
                    items.remove(this.lastRetIndex);
                    this.nextIndex = this.lastRetIndex;
                    this.lastRetIndex = -1;
                } catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean hasPrevious() {
            return this.nextIndex != 0;
        }

        @Override
        public E previous() {
            int i = this.nextIndex - 1;
            if (i < 0) {
                throw new NoSuchElementException();
            } else if (i >= items.size()) {
                    throw new ConcurrentModificationException();
            } else {
                try {
                    this.nextIndex = i;
                    this.lastRetIndex = i;
                    return items.get(this.lastRetIndex);
                } catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public int nextIndex() {
            return this.nextIndex;
        }

        @Override
        public int previousIndex() {
            return this.nextIndex - 1;
        }

        @Override
        public void set(E e) {
            if (this.lastRetIndex < 0) {
                throw new IllegalStateException();
            } else {
                try {
                    items.set(this.lastRetIndex, e);
                } catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public void add(E e) {
            try {
                items.add(this.nextIndex, e);
                this.nextIndex = this.nextIndex + 1;
                this.lastRetIndex = -1;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public int size() {
        loadAll();
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty() && !hasNextPage();
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new ListItr(0);
    }

    @Override
    public Object[] toArray() {
        loadAll();
        return items.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        loadAll();
        return items.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return items.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return items.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return items.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return items.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return items.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return items.retainAll(c);
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public E get(int index) {
        while (index >= items.size() && hasNextPage()) {
            loadNextPage();
        }
        return items.get(index);
    }

    @Override
    public E set(int index, E element) {
        return items.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        items.add(index, element);
    }

    @Override
    public E remove(int index) {
        return items.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (E item : this) {
                if (item == null) {
                    return index;
                }
                ++index;
            }
        } else {
            for (E item : this) {
                if (item == o) {
                    return index;
                }
                ++index;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        loadAll();
        return items.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        while (index >= items.size() && hasNextPage()) {
            loadNextPage();
        }
        return new ListItr(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        while ((fromIndex >= items.size()
                || toIndex >= items.size())
                && hasNextPage()) {
            loadNextPage();
        }
        return items.subList(fromIndex, toIndex);
    }
}
