/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a collection of SQL parameters to for a SQL query  in the Azure Cosmos DB database service.
 */
public final class SqlParameterCollection implements Collection<SqlParameter> {

    private List<SqlParameter> parameters;

    /**
     * Initializes a new instance of the SqlParameterCollection class.
     */
    public SqlParameterCollection() {
        this.parameters = new ArrayList<SqlParameter>();
    }

    /**
     * Initializes a new instance of the SqlParameterCollection class from an array of parameters.
     *
     * @param parameters the array of parameters.
     */
    public SqlParameterCollection(SqlParameter... parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters");
        }

        this.parameters = Arrays.asList(parameters);
    }

    /**
     * Initializes a new instance of the SqlParameterCollection class from a collection of parameters.
     *
     * @param parameters the collection of parameters.
     */
    public SqlParameterCollection(Collection<SqlParameter> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters");
        }

        this.parameters = new ArrayList<SqlParameter>(parameters);
    }

    @Override
    public boolean add(SqlParameter parameter) {
        return this.parameters.add(parameter);
    }

    @Override
    public boolean addAll(Collection<? extends SqlParameter> parameters) {
        return this.parameters.addAll(parameters);
    }

    @Override
    public void clear() {
        this.parameters.clear();
    }

    @Override
    public boolean contains(Object parameter) {
        return this.parameters.contains(parameter);
    }

    @Override
    public boolean containsAll(Collection<?> parameters) {
        return this.parameters.containsAll(parameters);
    }

    @Override
    public boolean isEmpty() {
        return this.parameters.isEmpty();
    }

    @Override
    public Iterator<SqlParameter> iterator() {
        return this.parameters.iterator();
    }

    @Override
    public boolean remove(Object parameter) {
        return this.parameters.remove(parameter);
    }

    @Override
    public boolean removeAll(Collection<?> parameters) {
        return this.parameters.removeAll(parameters);
    }

    @Override
    public boolean retainAll(Collection<?> parameters) {
        return this.parameters.retainAll(parameters);
    }

    @Override
    public int size() {
        return this.parameters.size();
    }

    @Override
    public Object[] toArray() {
        return this.parameters.toArray();
    }

    @Override
    public <T> T[] toArray(T[] parameters) {
        return this.parameters.toArray(parameters);
    }
}
