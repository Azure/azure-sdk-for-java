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

package com.microsoft.windowsazure.services.media.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Wrapper class for all returned lists from Media Services
 * 
 */
public class ListResult<T> implements List<T> {

    private final List<T> contents;

    public ListResult(Collection<T> contentList) {
        contents = Collections.unmodifiableList(new ArrayList<T>(contentList));
    }

    @Override
    public boolean add(T element) {
        return contents.add(element);
    }

    @Override
    public void add(int index, T element) {
        contents.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return contents.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return contents.addAll(index, c);
    }

    @Override
    public void clear() {
        contents.clear();
    }

    @Override
    public boolean contains(Object object) {
        return contents.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return contents.containsAll(c);
    }

    @Override
    public T get(int index) {
        return contents.get(index);
    }

    @Override
    public int indexOf(Object object) {
        return contents.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return contents.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return contents.lastIndexOf(object);
    }

    @Override
    public ListIterator<T> listIterator() {
        return contents.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return contents.listIterator(index);

    }

    @Override
    public boolean remove(Object o) {
        return contents.remove(o);
    }

    @Override
    public T remove(int index) {
        return contents.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return contents.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return contents.retainAll(c);
    }

    @Override
    public T set(int index, T element) {
        return contents.set(index, element);
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return contents.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return contents.toArray();
    }

    @Override
    public <U> U[] toArray(U[] a) {
        return contents.toArray(a);
    }
}
