package com.microsoft.windowsazure.services.table.models;

public class UnaryFilter extends Filter {
    private String operator;
    private Filter operand;

    public String getOperator() {
        return operator;
    }

    public UnaryFilter setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public Filter getOperand() {
        return operand;
    }

    public UnaryFilter setOperand(Filter operand) {
        this.operand = operand;
        return this;
    }
}
