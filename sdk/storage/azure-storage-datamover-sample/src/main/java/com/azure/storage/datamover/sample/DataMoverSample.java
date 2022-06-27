package com.azure.storage.datamover.sample;

import com.azure.storage.datamover.DataMover;
import com.azure.storage.datamover.DataMoverBuilder;

public class DataMoverSample {

    public static void main(String[] args) {
        DataMover dataMover = new DataMoverBuilder().build();
    }
}
