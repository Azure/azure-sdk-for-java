package com.microsoft.windowsazure.services.table.models;

public class BinaryFilterExpression extends FilterExpression {
    private String operator;
    private FilterExpression left;
    private FilterExpression right;

    public String getOperator() {
        return operator;
    }

    public BinaryFilterExpression setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public FilterExpression getLeft() {
        return left;
    }

    public BinaryFilterExpression setLeft(FilterExpression left) {
        this.left = left;
        return this;
    }

    public FilterExpression getRight() {
        return right;
    }

    public BinaryFilterExpression setRight(FilterExpression right) {
        this.right = right;
        return this;
    }
}
