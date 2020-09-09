// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Defines a list response from a paging operation. The pages are
 * lazy initialized when an instance of this class is iterated.
 *
 * @deprecated Use {@link PagedIterable} instead.
 *
 * @param <E> the element type.
 */
@Deprecated
public final class PagedList<E> implements List<E> {

    // The invariant is that before / after read-only method,
    // items + pagedResponseIterator is the complete pagedIterable.

    /** The items retrieved. */
    private final List<E> items;

    /** The paged response iterator for not retrieved items. */
    private Iterator<PagedResponse<E>> pagedResponseIterator;

    /**
     * Creates an instance of PagedList.
     */
    public PagedList() {
        items = new ArrayList<>();
        pagedResponseIterator = Collections.emptyIterator();
    }

    /**
     * Creates an instance of PagedList from a {@link PagedIterable}.
     *
     * @param pagedIterable the {@link PagedIterable} object.
     */
    public PagedList(PagedIterable<E> pagedIterable) {
        items = new ArrayList<>();
        Objects.requireNonNull(pagedIterable, "'pagedIterable' cannot be null.");
        this.pagedResponseIterator = pagedIterable.iterableByPage().iterator();
    }

    /**
     * If there are more pages available.
     *
     * @return true if there are more pages to load. False otherwise.
     */
    protected boolean hasNextPage() {
        return pagedResponseIterator.hasNext();
    }

    /**
     * Loads a page from next page link.
     * The exceptions are wrapped into Java Runtime exceptions.
     */
    protected void loadNextPage() {
        this.items.addAll(pagedResponseIterator.next().getValue());
    }

    /**
     * Keep loading the next page from the next page link until all items are loaded.
     */
    public void loadAll() {
        while (hasNextPage()) {
            loadNextPage();
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
        loadAll();
        return items.add(e);
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        if (index != -1) {
            items.remove(index);
            return true;
        } else {
            return false;
        }
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
        pagedResponseIterator = Collections.emptyIterator();
    }

    @Override
    public E get(int index) {
        tryLoadToIndex(index);
        return items.get(index);
    }

    @Override
    public E set(int index, E element) {
        tryLoadToIndex(index);
        return items.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        items.add(index, element);
    }

    @Override
    public E remove(int index) {
        tryLoadToIndex(index);
        return items.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        int index = items.indexOf(o);
        if (index != -1) {
            return index;
        }
        while (hasNextPage()) {
            int itemsSize = items.size();
            List<E> nextPageItems = pagedResponseIterator.next().getValue();
            this.items.addAll(nextPageItems);

            index = nextPageItems.indexOf(o);
            if (index != -1) {
                index = itemsSize + index;
                return index;
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
        tryLoadToIndex(index);
        return new ListItr(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        while ((fromIndex >= items.size() || toIndex >= items.size()) && hasNextPage()) {
            loadNextPage();
        }
        return items.subList(fromIndex, toIndex);
    }

    private void tryLoadToIndex(int index) {
        while (index >= items.size() && hasNextPage()) {
            loadNextPage();
        }
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

        private final ClientLogger logger = new ClientLogger(getClass());

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
                    throw logger.logExceptionAsError(new NoSuchElementException());
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
                    throw logger.logExceptionAsError(new ConcurrentModificationException());
                }
            }
        }

        @Override
        public void remove() {
            if (this.lastRetIndex < 0) {
                throw logger.logExceptionAsError(new IllegalStateException());
            } else {
                try {
                    items.remove(this.lastRetIndex);
                    this.nextIndex = this.lastRetIndex;
                    this.lastRetIndex = -1;
                } catch (IndexOutOfBoundsException ex) {
                    throw logger.logExceptionAsError(new ConcurrentModificationException());
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
                throw logger.logExceptionAsError(new NoSuchElementException());
            } else if (i >= items.size()) {
                throw logger.logExceptionAsError(new ConcurrentModificationException());
            } else {
                try {
                    this.nextIndex = i;
                    this.lastRetIndex = i;
                    return items.get(this.lastRetIndex);
                } catch (IndexOutOfBoundsException ex) {
                    throw logger.logExceptionAsError(new ConcurrentModificationException());
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
                throw logger.logExceptionAsError(new IllegalStateException());
            } else {
                try {
                    items.set(this.lastRetIndex, e);
                } catch (IndexOutOfBoundsException ex) {
                    throw logger.logExceptionAsError(new ConcurrentModificationException());
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
                throw logger.logExceptionAsError(new ConcurrentModificationException());
            }
        }
    }
}
