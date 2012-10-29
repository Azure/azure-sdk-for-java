package com.microsoft.windowsazure.services.media.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.MultipartDataSource;

public class SetBoundaryMultipartDataSource implements MultipartDataSource {

    private final String boundary;

    public SetBoundaryMultipartDataSource(String boundary) {
        this.boundary = boundary;
    }

    @Override
    public String getContentType() {
        return "multipart/mixed; boundary=" + boundary;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public BodyPart getBodyPart(int index) throws MessagingException {
        return null;
    }
}
