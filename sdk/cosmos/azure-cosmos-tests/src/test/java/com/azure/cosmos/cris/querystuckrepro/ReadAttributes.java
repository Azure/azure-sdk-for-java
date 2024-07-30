package com.azure.cosmos.cris.querystuckrepro;

public class ReadAttributes {
    private final int numRowsToRead;
    private int numRowsRead = 0;

    public ReadAttributes(int numRowsToRead) {
        this.numRowsToRead = numRowsToRead;
    }

    public int getNumRowsToRead() {
        return this.numRowsToRead;
    }

    public void setNumRowsRead(int value) {
        this.numRowsRead = value;
    }
}
