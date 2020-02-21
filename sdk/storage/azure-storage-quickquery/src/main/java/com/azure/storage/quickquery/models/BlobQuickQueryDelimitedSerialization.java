package com.azure.storage.quickquery.models;

public class BlobQuickQueryDelimitedSerialization extends BlobQuickQuerySerialization<BlobQuickQueryDelimitedSerialization> {

    private char columnSeparator;
    private char fieldQuote;
    private char escapeChar;
    private boolean headersPresent;

    public char getColumnSeparator() {
        return columnSeparator;
    }

    public BlobQuickQueryDelimitedSerialization setColumnSeparator(char columnSeparator) {
        this.columnSeparator = columnSeparator;
        return this;
    }

    public char getFieldQuote() {
        return fieldQuote;
    }

    public BlobQuickQueryDelimitedSerialization setFieldQuote(char fieldQuote) {
        this.fieldQuote = fieldQuote;
        return this;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public BlobQuickQueryDelimitedSerialization setEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
        return this;
    }

    public boolean isHeadersPresent() {
        return headersPresent;
    }

    public BlobQuickQueryDelimitedSerialization setHeadersPresent(boolean headersPresent) {
        this.headersPresent = headersPresent;
        return this;
    }
}
