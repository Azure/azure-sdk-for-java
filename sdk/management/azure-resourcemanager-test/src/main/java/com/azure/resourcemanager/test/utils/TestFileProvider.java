package com.azure.resourcemanager.test.utils;

import com.azure.resourcemanager.base.utils.FileProvider;

import java.io.File;
import java.io.IOException;

public class TestFileProvider extends FileProvider {

    private boolean isRecordMode;

    /**
     * Constructor.
     * @param isRecordMode whether test case is in recording mode.
     */
    public TestFileProvider(boolean isRecordMode) {
        this.isRecordMode = isRecordMode;
    }

    @Override
    public void prepareFileLocation(File file) throws IOException {
        if (isRecordMode) {
            // delete existing file so new file can be recorded during recording.
            file.delete();
        }
        super.prepareFileLocation(file);
    }
}
