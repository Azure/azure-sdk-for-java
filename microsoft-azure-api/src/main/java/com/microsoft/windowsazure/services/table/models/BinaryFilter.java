package com.microsoft.windowsazure.services.table.models;

public class BinaryFilter extends Filter {
    private String operator;
    private Filter left;
    private Filter right;

    public String getOperator() {
        return operator;
    }

    public BinaryFilter setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public Filter getLeft() {
        return left;
    }

    public BinaryFilter setLeft(Filter left) {
        this.left = left;
        return this;
    }

    public Filter getRight() {
        return right;
    }

    public BinaryFilter setRight(Filter right) {
        this.right = right;
        return this;
    }
}
