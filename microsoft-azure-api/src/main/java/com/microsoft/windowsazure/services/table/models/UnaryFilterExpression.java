package com.microsoft.windowsazure.services.table.models;

public class UnaryFilterExpression extends FilterExpression {
    private String operator;
    private FilterExpression operand;

    public String getOperator() {
        return operator;
    }

    public UnaryFilterExpression setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public FilterExpression getOperand() {
        return operand;
    }

    public UnaryFilterExpression setOperand(FilterExpression operand) {
        this.operand = operand;
        return this;
    }
}
