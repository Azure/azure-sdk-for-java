// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a collection of SQL parameters to for a SQL query  in the Azure Cosmos DB database service.
 */
public final class SqlParameterList implements List<SqlParameter> {

    private List<SqlParameter> parameters;

    /**
     * Initializes a new instance of the SqlParameterList class.
     */
    public SqlParameterList() {
        this.parameters = new ArrayList<SqlParameter>();
    }

    /**
     * Initializes a new instance of the SqlParameterList class from an array of parameters.
     *
     * @param parameters the array of parameters.
     */
    public SqlParameterList(SqlParameter... parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters");
        }

        this.parameters = Arrays.asList(parameters);
    }

    /**
     * Initializes a new instance of the SqlParameterList class from a collection of parameters.
     *
     * @param parameters the collection of parameters.
     */
    public SqlParameterList(Collection<SqlParameter> parameters) {
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
    public boolean addAll(int index, Collection<? extends SqlParameter> c) {
        return this.parameters.addAll(index, c);
    }

    @Override
    public void clear() {
        this.parameters.clear();
    }

    @Override
    public SqlParameter get(int index) {
        return this.parameters.get(index);
    }

    @Override
    public SqlParameter set(int index, SqlParameter element) {
        return this.parameters.set(index, element);
    }

    @Override
    public void add(int index, SqlParameter element) {
        this.parameters.add(index, element);
    }

    @Override
    public SqlParameter remove(int index) {
        return this.parameters.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.parameters.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.parameters.lastIndexOf(o);
    }

    @Override
    public ListIterator<SqlParameter> listIterator() {
        return this.listIterator();
    }

    @Override
    public ListIterator<SqlParameter> listIterator(int index) {
        return this.listIterator(index);
    }

    @Override
    public List<SqlParameter> subList(int fromIndex, int toIndex) {
        return this.parameters.subList(fromIndex, toIndex);
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
