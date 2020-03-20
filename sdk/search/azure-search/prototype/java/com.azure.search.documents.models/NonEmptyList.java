package com.azure.search.documents.models;

import java.util.List;

public class NonEmptyList<T> {
    private T head;
    private List<T> tail;

    public NonEmptyList(final T head, final List<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    public List<T> asList() {
        tail.add(head);
        return tail;
    }

}
