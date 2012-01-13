package com.microsoft.windowsazure.services.table.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.MultipartDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

public class MimeReaderWriter {

    @Inject
    public MimeReaderWriter() {
    }

    public MimeMultipart getMimeMultipart(List<String> bodyPartContents) {
        try {
            return getMimeMultipartCore(bodyPartContents);
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private MimeMultipart getMimeMultipartCore(List<String> bodyPartContents) throws MessagingException {
        // Create unique part boundary strings
        String batchId = String.format("batch_%s", UUID.randomUUID().toString());
        String changeSet = String.format("changeset_%s", UUID.randomUUID().toString());

        //
        // Build inner list of change sets containing the list of body part content
        //
        MimeMultipart changeSets = new MimeMultipart(new SetBoundaryMultipartDataSource(changeSet));

        for (String bodyPart : bodyPartContents) {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();

            mimeBodyPart.setContent(bodyPart, "application/http");

            //Note: Both content type and encoding need to be set *after* setting content, because
            //      MimeBodyPart implementation replaces them when calling "setContent".
            mimeBodyPart.setHeader("Content-Type", "application/http");
            mimeBodyPart.setHeader("Content-Transfer-Encoding", "binary");

            changeSets.addBodyPart(mimeBodyPart);
        }

        //
        // Build outer "batch" body part
        //
        MimeBodyPart batchbody = new MimeBodyPart();
        batchbody.setContent(changeSets);
        //Note: Both content type and encoding need to be set *after* setting content, because
        //      MimeBodyPart implementation replaces them when calling "setContent".
        batchbody.setHeader("Content-Type", changeSets.getContentType());

        //
        // Build outer "batch" multipart
        //
        MimeMultipart batch = new MimeMultipart(new SetBoundaryMultipartDataSource(batchId));
        batch.addBodyPart(batchbody);
        return batch;
    }

    /**
     * The only purpose of this class is to force the boundary of a MimeMultipart instance.
     * This is done by simple passing an instance of this class to the constructor of MimeMultipart.
     */
    private class SetBoundaryMultipartDataSource implements MultipartDataSource {

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
}
