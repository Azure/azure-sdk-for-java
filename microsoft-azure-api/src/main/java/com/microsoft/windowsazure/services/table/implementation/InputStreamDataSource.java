package com.microsoft.windowsazure.services.table.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class InputStreamDataSource implements DataSource {
    private final InputStream stream;
    private final String contentType;

    public InputStreamDataSource(InputStream stream, String contentType) {
        this.stream = stream;
        this.contentType = contentType;

    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return stream;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}