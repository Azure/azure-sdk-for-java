package com.microsoft.azure.management.resources.fluentcore.utils;

import java.util.*;

public class WrappedList<SourceT, WrappedT> implements List<WrappedT> {
    private List<SourceT> sourceList;
    private WrappedItemTransformer<SourceT, WrappedT> transformer;

    public WrappedList(List<SourceT> sourceList, WrappedItemTransformer<SourceT, WrappedT> transformer) {
        this.sourceList = sourceList;
        this.transformer = transformer;
    }

    @Override
    public int size() {
        return sourceList.size();
    }

    @Override
    public boolean isEmpty() {
        return sourceList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return sourceList.contains(transformer.toSource((WrappedT) o));
    }

    @Override
    public Iterator<WrappedT> iterator() {
        return new WrappedIterator();
    }

    @Override
    public Object[] toArray() {
        Object [] array = new Object[size()];
        int i = 0;
        for (SourceT item: sourceList) {
            array[i] = transformer.toWrapped(item);
            i++;
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (size() > a.length) {
            a =  (T[]) new Object[size()];
        }

        int i = 0;
        for (SourceT item: sourceList) {
            a[i] = (T)transformer.toWrapped(item);
            i++;
        }

        for (; i < a.length; i++) {
            a[i] = null;
        }
        return a;
    }

    @Override
    public boolean add(WrappedT wrapped) {
        return sourceList.add(transformer.toSource(wrapped));
    }

    @Override
    public boolean remove(Object o) {
        return sourceList.remove(transformer.toSource((WrappedT) o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends WrappedT> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends WrappedT> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        sourceList.clear();
    }

    @Override
    public WrappedT get(int index) {
        return transformer.toWrapped(sourceList.get(index));
    }

    @Override
    public WrappedT set(int index, WrappedT element) {
        SourceT item = transformer.toSource(element);
        SourceT oldItem = sourceList.set(index, item);
        return transformer.toWrapped(oldItem);
    }

    @Override
    public void add(int index, WrappedT element) {
        SourceT item = transformer.toSource(element);
        sourceList.add(index, item);
    }

    @Override
    public WrappedT remove(int index) {
        return transformer.toWrapped(sourceList.remove(index));
    }

    @Override
    public int indexOf(Object o) {
        return sourceList.indexOf(transformer.toSource((WrappedT) o));
    }

    @Override
    public int lastIndexOf(Object o) {
        return sourceList.lastIndexOf(transformer.toSource((WrappedT) o));
    }

    @Override
    public ListIterator<WrappedT> listIterator() {
        return new WrappedListIterator(0);
    }

    @Override
    public ListIterator<WrappedT> listIterator(int index) {
        return new WrappedListIterator(index);
    }

    @Override
    public List<WrappedT> subList(int fromIndex, int toIndex) {
        List<WrappedT> list = new ArrayList<>();
        for(SourceT item: sourceList.subList(fromIndex, toIndex)) {
            list.add(transformer.toWrapped(item));
        }
        return list;
    }

    class WrappedIterator implements Iterator<WrappedT> {
        private Iterator<SourceT> sourceIterator;
        public WrappedIterator() {
            sourceIterator = sourceList.iterator();
        }

        @Override
        public boolean hasNext() {
            return sourceIterator.hasNext();
        }

        @Override
        public WrappedT next() {
            return transformer.toWrapped(sourceIterator.next());
        }

        @Override
        public void remove() {
            sourceIterator.remove();
        }
    }

    class WrappedListIterator implements ListIterator<WrappedT> {
        private ListIterator<SourceT> sourceListIterator;

        public WrappedListIterator(int index) {
            sourceListIterator = sourceList.listIterator(index);
        }

        @Override
        public boolean hasNext() {
            return sourceListIterator.hasNext();
        }

        @Override
        public WrappedT next() {
            return transformer.toWrapped(sourceListIterator.next());
        }

        @Override
        public boolean hasPrevious() {
            return sourceListIterator.hasPrevious();
        }

        @Override
        public WrappedT previous() {
            return transformer.toWrapped(sourceListIterator.previous());
        }

        @Override
        public int nextIndex() {
            return sourceListIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return sourceListIterator.previousIndex();
        }

        @Override
        public void remove() {
            sourceListIterator.remove();
        }

        @Override
        public void set(WrappedT wrapped) {
            sourceListIterator.set(transformer.toSource(wrapped));
        }

        @Override
        public void add(WrappedT wrapped) {
            sourceListIterator.add(transformer.toSource(wrapped));
        }
    }
}
