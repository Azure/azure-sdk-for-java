package com.microsoft.windowsazure.services.table.models;

public class RawStringFilter extends Filter {
    private String rawString;

    public String getRawString() {
        return rawString;
    }

    public RawStringFilter setRawString(String rawString) {
        this.rawString = rawString;
        return this;
    }
}
