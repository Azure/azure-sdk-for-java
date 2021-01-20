// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.impl.models;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Objects;


/**
 * The entity and it's attributes from the data store.
 *
 * An object of this type can be created ONLY if we have the Entity data,
 * AND the attributes for the Entity.
 *
 * @param <T> the type of the Entity
 */
public class Entity<T> {
    private final T _data;
    private final EntityAttributes _attributes;

    public Entity(final T data, final EntityAttributes attributes) {
        _data = Preconditions.checkNotNull(data, "record cannot be null.");
        _attributes = Preconditions.checkNotNull(attributes, "attributes cannot be null.");
    }

    public T get() {
        return _data;
    }

    public EntityAttributes getAttributes() {
        return _attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Entity that = (Entity) o;
        return Objects.equals(_data, that._data) && Objects.equals(_attributes, that._attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_data, _attributes);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .omitNullValues()
            .add("data", get())
            .add("attributes", getAttributes())
            .toString();
    }
}
