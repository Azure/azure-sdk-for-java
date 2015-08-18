/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class LazyArrayList<E> extends ArrayList<E> implements LazyCollection {
    private static final long serialVersionUID = 1L;
    
    private boolean initialized;
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void add(int index, E element) {
        initialized = true;
        super.add(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        initialized = true;
        return super.addAll(index, c);
    }

    @Override
    public E get(int index) {
        initialized = true;
        return super.get(index);
    }

    @Override
    public int indexOf(Object o) {
        initialized = true;
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        initialized = true;
        return super.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        initialized = true;
        return super.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        initialized = true;
        return super.listIterator(index);
    }

    @Override
    public E remove(int index) {
        initialized = true;
        return super.remove(index);
    }

    @Override
    public E set(int index, E element) {
        initialized = true;
        return super.set(index, element);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        initialized = true;
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public boolean add(E e) {
        initialized = true;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        initialized = true;
        return super.addAll(c);
    }

    @Override
    public void clear() {
        initialized = true;
        super.clear();
    }

    @Override
    public boolean contains(Object o) {
        initialized = true;
        return super.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        initialized = true;
        return super.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        initialized = true;
        return super.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        initialized = true;
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        initialized = true;
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        initialized = true;
        return super.retainAll(c);
    }

    @Override
    public int size() {
        initialized = true;
        return super.size();
    }

    @Override
    public Object[] toArray() {
        initialized = true;
        return super.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        initialized = true;
        return super.toArray(a);
    }

    @Override
    public Iterator<E> iterator() {
        initialized = true;
        return super.iterator();
    }
}
