package com.microsoft.windowsazure.services.table.models;

public class LitteralFilterExpression extends FilterExpression {
    private String litteral;

    public String getLitteral() {
        return litteral;
    }

    public LitteralFilterExpression setLitteral(String litteral) {
        this.litteral = litteral;
        return this;
    }
}
