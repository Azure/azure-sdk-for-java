// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.geojson;

import com.azure.core.util.logging.ClientLogger;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A read-only list of geometry coordinates.
 *
 * @param <T> The type of geometry coordinates.
 */
public class GeoArray<T> extends AbstractList<T> {
    private final ClientLogger logger = new ClientLogger(GeoArray.class);

    private final Object container;

    GeoArray(Object container) {
        this.container = container;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (container instanceof List) {
            return (T) ((List<?>) container).get(index);
        } else if (container instanceof GeoPointCollection) {
            return (T) ((GeoPointCollection) container).getPoints().get(index).getCoordinates();
        } else if (container instanceof GeoLineCollection) {
            return (T) ((GeoLineCollection) container).getLines().get(index).getCoordinates();
        } else if (container instanceof GeoPolygon) {
            return (T) ((GeoPolygon) container).getRings().get(index).getCoordinates();
        } else if (container instanceof GeoPolygonCollection) {
            return (T) ((GeoPolygonCollection) container).getPolygons().get(index).getCoordinates();
        } else {
            throw logger.logExceptionAsError(new IllegalStateException());
        }
    }

    @Override
    public int size() {
        if (container instanceof List) {
            return ((List<?>) container).size();
        } else if (container instanceof GeoPointCollection) {
            return ((GeoPointCollection) container).getPoints().size();
        } else if (container instanceof GeoLineCollection) {
            return ((GeoLineCollection) container).getLines().size();
        } else if (container instanceof GeoPolygon) {
            return ((GeoPolygon) container).getRings().size();
        } else if (container instanceof GeoPolygonCollection) {
            return ((GeoPolygonCollection) container).getPolygons().size();
        } else {
            throw logger.logExceptionAsError(new IllegalStateException());
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new GeoArrayIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new GeoArrayListIterator(index);
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param t The element that would be added.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public boolean add(T t) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param index The index where the element would be added.
     * @param element The element that would be added.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public void add(int index, T element) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param index The index where the element would be added.
     * @param element The element that would be added.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public T set(int index, T element) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param index The index where the element would be removed.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public T remove(int index) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public void clear() {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param index The index where the element would be added.
     * @param c The collection of elements that would be added.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param o The object that would be removed.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public boolean remove(Object o) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param c The collection of elements that would be added.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param c The collection of elements that would be removed.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param c The collection of elements that would be retained.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param operator The operator that determines which elements would be replaced.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param c The comparator that determines sort order.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public void sort(Comparator<? super T> c) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param filter The predicate that determines which elements would be removed.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param fromIndex The index where the sub list would begin.
     * @param toIndex The index where the sub list would end.
     * @return Throws an exception.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray does not support sub lists."));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GeoArray)) {
            return false;
        }

        if (this == o) {
            return true;
        }

        GeoArray<?> other = (GeoArray<?>) o;
        return Objects.equals(container, other.container);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(container);
    }

    private class GeoArrayIterator implements Iterator<T> {
        transient int cursor;

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public T next() {
            try {
                int i = cursor;
                T value = get(i);
                cursor = i + 1;

                return value;
            } catch (IndexOutOfBoundsException ex) {
                throw logger.logExceptionAsError(new NoSuchElementException());
            }
        }
    }

    private final class GeoArrayListIterator extends GeoArrayIterator implements ListIterator<T> {
        private GeoArrayListIterator(int index) {
            cursor = index;
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public T previous() {
            try {
                int i = cursor - 1;
                T value = get(i);
                cursor = i;

                return value;
            } catch (IndexOutOfBoundsException ex) {
                throw logger.logExceptionAsError(new NoSuchElementException());
            }
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
        }

        @Override
        public void set(T t) {
            throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
        }

        @Override
        public void add(T t) {
            throw logger.logExceptionAsError(new UnsupportedOperationException("GeoArray cannot be mutated."));
        }
    }
}
