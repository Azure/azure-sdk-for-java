// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.geo;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * <p>Represents a read-only list of geometry coordinates.</p>
 *
 * <p>This class encapsulates a list of geometry coordinates and provides methods to access these coordinates.
 * The coordinates can be of any type {@code T}.</p>
 *
 * @param <T> The type of geometry coordinates.
 */
@Metadata(properties = MetadataProperties.IMMUTABLE)
final class GeoArray<T> extends AbstractList<T> {
    private static final String NO_MUTATION_MESSAGE = "GeoArray cannot be mutated.";
    // GeoArray is a commonly used model, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(GeoArray.class);

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
            throw LOGGER.throwableAtError().log(IllegalStateException::new);
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
            throw LOGGER.throwableAtError().log(IllegalStateException::new);
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
        throw LOGGER.throwableAtError().log(NO_MUTATION_MESSAGE, UnsupportedOperationException::new);
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
        throw LOGGER.throwableAtError().log(NO_MUTATION_MESSAGE, UnsupportedOperationException::new);
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
        throw LOGGER.throwableAtError().log(NO_MUTATION_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param operator The operator that determines which elements would be replaced.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw LOGGER.throwableAtError().log(NO_MUTATION_MESSAGE, UnsupportedOperationException::new);
    }

    /**
     * Throws {@link UnsupportedOperationException} as GeoArray doesn't support mutation.
     *
     * @param c The comparator that determines sort order.
     * @throws UnsupportedOperationException GeoArray doesn't support mutation.
     */
    @Override
    public void sort(Comparator<? super T> c) {
        throw LOGGER.throwableAtError().log(NO_MUTATION_MESSAGE, UnsupportedOperationException::new);
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
        throw LOGGER.throwableAtError().log(NO_MUTATION_MESSAGE, UnsupportedOperationException::new);
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
        throw LOGGER.throwableAtError().log("GeoArray does not support sub lists.", UnsupportedOperationException::new);
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
