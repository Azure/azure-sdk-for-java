// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A read-only list of geometry coordinates.
 *
 * @param <T> The type of geometry coordinates.
 */
@Immutable
final class GeoArray<T> extends AbstractList<T> {
    private static final String NO_MUTATION_MESSAGE = "GeoArray cannot be mutated.";
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
        } else if (container instanceof GeoLineStringCollection) {
            return (T) ((GeoLineStringCollection) container).getLines().get(index).getCoordinates();
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
        } else if (container instanceof GeoLineStringCollection) {
            return ((GeoLineStringCollection) container).getLines().size();
        } else if (container instanceof GeoPolygon) {
            return ((GeoPolygon) container).getRings().size();
        } else if (container instanceof GeoPolygonCollection) {
            return ((GeoPolygonCollection) container).getPolygons().size();
        } else {
            throw logger.logExceptionAsError(new IllegalStateException());
        }
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
        throw logger.logExceptionAsError(new UnsupportedOperationException(NO_MUTATION_MESSAGE));
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
        throw logger.logExceptionAsError(new UnsupportedOperationException(NO_MUTATION_MESSAGE));
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
        throw logger.logExceptionAsError(new UnsupportedOperationException(NO_MUTATION_MESSAGE));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param operator The operator that determines which elements would be replaced.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(NO_MUTATION_MESSAGE));
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param c The comparator that determines sort order.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public void sort(Comparator<? super T> c) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(NO_MUTATION_MESSAGE));
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
        throw logger.logExceptionAsError(new UnsupportedOperationException(NO_MUTATION_MESSAGE));
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
}
