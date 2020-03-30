/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.apachecommons.collections.set;

import com.azure.cosmos.implementation.apachecommons.collections.AbstractCollectionDecorator;

import java.util.Set;

public abstract class AbstractSetDecorator<E> extends AbstractCollectionDecorator<E> implements
    Set<E> {
    /** Serialization version */
    private static final long serialVersionUID = 7970325904666333066L;

    /**
     * Constructor only used in deserialization, do not use otherwise.
     */
    protected AbstractSetDecorator() {
        super();
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param set  the set to decorate, must not be null
     * @throws NullPointerException if set is null
     */
    protected AbstractSetDecorator(final Set<E> set) {
        super(set);
    }

    /**
     * Gets the set being decorated.
     *
     * @return the decorated set
     */
    @Override
    protected Set<E> decorated() {
        return (Set<E>) super.decorated();
    }

    @Override
    public boolean equals(final Object object) {
        return object == this || decorated().equals(object);
    }

    @Override
    public int hashCode() {
        return decorated().hashCode();
    }
}
