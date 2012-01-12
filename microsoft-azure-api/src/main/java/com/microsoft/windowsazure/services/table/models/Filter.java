package com.microsoft.windowsazure.services.table.models;

public class Filter {
    public static UnaryFilter not(Filter operand) {
        return new UnaryFilter().setOperator("not").setOperand(operand);
    }

    public static BinaryFilter and(Filter left, Filter right) {
        return new BinaryFilter().setOperator("and").setLeft(left).setRight(right);
    }

    public static BinaryFilter or(Filter left, Filter right) {
        return new BinaryFilter().setOperator("or").setLeft(left).setRight(right);
    }

    public static BinaryFilter eq(Filter left, Filter right) {
        return new BinaryFilter().setOperator("eq").setLeft(left).setRight(right);
    }

    public static BinaryFilter ne(Filter left, Filter right) {
        return new BinaryFilter().setOperator("ne").setLeft(left).setRight(right);
    }

    public static BinaryFilter ge(Filter left, Filter right) {
        return new BinaryFilter().setOperator("ge").setLeft(left).setRight(right);
    }

    public static BinaryFilter gt(Filter left, Filter right) {
        return new BinaryFilter().setOperator("gt").setLeft(left).setRight(right);
    }

    public static BinaryFilter lt(Filter left, Filter right) {
        return new BinaryFilter().setOperator("lt").setLeft(left).setRight(right);
    }

    public static BinaryFilter le(Filter left, Filter right) {
        return new BinaryFilter().setOperator("le").setLeft(left).setRight(right);
    }

    public static ConstantFilter constant(Object value) {
        return new ConstantFilter().setValue(value);
    }

    public static LitteralFilter litteral(String value) {
        return new LitteralFilter().setLitteral(value);
    }
}
