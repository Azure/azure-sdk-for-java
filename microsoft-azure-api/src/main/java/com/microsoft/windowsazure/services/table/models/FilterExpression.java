package com.microsoft.windowsazure.services.table.models;

public class FilterExpression {
    public static UnaryFilterExpression not(FilterExpression operand) {
        return new UnaryFilterExpression().setOperator("not").setOperand(operand);
    }

    public static BinaryFilterExpression and(FilterExpression left, FilterExpression right) {
        return new BinaryFilterExpression().setOperator("and").setLeft(left).setRight(right);
    }

    public static BinaryFilterExpression or(FilterExpression left, FilterExpression right) {
        return new BinaryFilterExpression().setOperator("or").setLeft(left).setRight(right);
    }

    public static BinaryFilterExpression eq(FilterExpression left, FilterExpression right) {
        return new BinaryFilterExpression().setOperator("eq").setLeft(left).setRight(right);
    }

    public static BinaryFilterExpression ne(FilterExpression left, FilterExpression right) {
        return new BinaryFilterExpression().setOperator("ne").setLeft(left).setRight(right);
    }

    public static BinaryFilterExpression ge(FilterExpression left, FilterExpression right) {
        return new BinaryFilterExpression().setOperator("ge").setLeft(left).setRight(right);
    }

    public static BinaryFilterExpression gt(FilterExpression left, FilterExpression right) {
        return new BinaryFilterExpression().setOperator("gt").setLeft(left).setRight(right);
    }

    public static BinaryFilterExpression lt(FilterExpression left, FilterExpression right) {
        return new BinaryFilterExpression().setOperator("lt").setLeft(left).setRight(right);
    }

    public static BinaryFilterExpression le(FilterExpression left, FilterExpression right) {
        return new BinaryFilterExpression().setOperator("le").setLeft(left).setRight(right);
    }

    public static ConstantFilterExpression constant(Object value) {
        return new ConstantFilterExpression().setValue(value);
    }

    public static LitteralFilterExpression litteral(String value) {
        return new LitteralFilterExpression().setLitteral(value);
    }
}
