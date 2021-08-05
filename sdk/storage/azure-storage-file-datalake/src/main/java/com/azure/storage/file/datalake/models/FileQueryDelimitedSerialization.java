// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines the input or output delimited (CSV) serialization for a blob quick query request.
 */
public class FileQueryDelimitedSerialization implements FileQuerySerialization {

    private char columnSeparator;
    private char fieldQuote;
    private char escapeChar;
    private boolean headersPresent;
    private char recordSeparator;

    /**
     * Gets the column separator.
     * @return the column separator.
     */
    public char getColumnSeparator() {
        return columnSeparator;
    }

    /**
     * Sets the column separator.
     * @param columnSeparator the column separator.
     * @return the updated FileQueryDelimitedSerialization object.
     */
    public FileQueryDelimitedSerialization setColumnSeparator(char columnSeparator) {
        this.columnSeparator = columnSeparator;
        return this;
    }

    /**
     * Gets the field quote.
     * @return the field quote.
     */
    public char getFieldQuote() {
        return fieldQuote;
    }

    /**
     * Sets the field quote.
     * @param fieldQuote the field quote.
     * @return the updated FileQueryDelimitedSerialization object.
     */
    public FileQueryDelimitedSerialization setFieldQuote(char fieldQuote) {
        this.fieldQuote = fieldQuote;
        return this;
    }

    /**
     * Gets the escape character.
     * @return the escape character.
     */
    public char getEscapeChar() {
        return escapeChar;
    }

    /**
     * Sets the escape character.
     * @param escapeChar the escape character.
     * @return the updated FileQueryDelimitedSerialization object.
     */
    public FileQueryDelimitedSerialization setEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
        return this;
    }

    /**
     * Gets whether or not headers are present.
     * @return Whether or not headers are present.
     */
    public boolean isHeadersPresent() {
        return headersPresent;
    }

    /**
     * Sets whether or not headers are present.
     * @param headersPresent Whether or not headers are present.
     * @return the updated FileQueryDelimitedSerialization object.
     */
    public FileQueryDelimitedSerialization setHeadersPresent(boolean headersPresent) {
        this.headersPresent = headersPresent;
        return this;
    }

    /**
     * Gets the record separator.
     *
     * @return the record separator.
     */
    public char getRecordSeparator() {
        return recordSeparator;
    }

    /**
     * Sets the record separator.
     * @param recordSeparator the record separator.
     * @return the updated FileQueryDelimitedSerialization object.
     */
    public FileQueryDelimitedSerialization setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }
}
