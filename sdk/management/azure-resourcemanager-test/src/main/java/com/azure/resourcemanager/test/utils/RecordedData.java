package com.azure.resourcemanager.test.utils;

import java.util.LinkedList;

public class RecordedData {
    private LinkedList<NetworkCallRecord> networkCallRecords;

    private LinkedList<String> variables;

    public RecordedData() {
        networkCallRecords = new LinkedList<>();
        variables = new LinkedList<>();
    }

    public LinkedList<NetworkCallRecord> getNetworkCallRecords() {
        return networkCallRecords;
    }

    public LinkedList<String> getVariables() {
        return variables;
    }
}
