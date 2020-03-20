package com.azure.search.documents.models;

public class Pair<L, R> {
    private final L lhs;
    private final R rhs;
    public Pair(L lhs, R rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public L getLhs() {
        return lhs;
    }

    public R getRhs() {
        return rhs;
    }
}
