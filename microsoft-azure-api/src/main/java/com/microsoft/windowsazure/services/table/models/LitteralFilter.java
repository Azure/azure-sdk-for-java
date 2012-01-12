package com.microsoft.windowsazure.services.table.models;

public class LitteralFilter extends Filter {
    private String litteral;

    public String getLitteral() {
        return litteral;
    }

    public LitteralFilter setLitteral(String litteral) {
        this.litteral = litteral;
        return this;
    }
}
