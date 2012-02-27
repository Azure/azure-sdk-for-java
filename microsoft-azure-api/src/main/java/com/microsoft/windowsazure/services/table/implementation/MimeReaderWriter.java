/**
 * Copyright 2012 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.table.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.inject.Inject;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.MultipartDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePartDataSource;

public class MimeReaderWriter {

    @Inject
    public MimeReaderWriter() {
    }

    public MimeMultipart getMimeMultipart(List<DataSource> bodyPartContents) {
        try {
            return getMimeMultipartCore(bodyPartContents);
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MimeMultipart getMimeMultipartCore(List<DataSource> bodyPartContents) throws MessagingException,
            IOException {
        // Create unique part boundary strings
        String batchId = String.format("batch_%s", UUID.randomUUID().toString());
        String changeSet = String.format("changeset_%s", UUID.randomUUID().toString());

        //
        // Build inner list of change sets containing the list of body part content
        //
        MimeMultipart changeSets = new MimeMultipart(new SetBoundaryMultipartDataSource(changeSet));

        for (DataSource bodyPart : bodyPartContents) {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();

            mimeBodyPart.setDataHandler(new DataHandler(bodyPart));
            mimeBodyPart.setHeader("Content-Type", bodyPart.getContentType());
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

    public List<DataSource> parseParts(final InputStream entityInputStream, final String contentType) {
        try {
            return parsePartsCore(entityInputStream, contentType);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DataSource> parsePartsCore(InputStream entityInputStream, String contentType)
            throws MessagingException, IOException {
        DataSource ds = new InputStreamDataSource(entityInputStream, contentType);
        MimeMultipart batch = new MimeMultipart(ds);
        MimeBodyPart batchBody = (MimeBodyPart) batch.getBodyPart(0);

        MimeMultipart changeSets = new MimeMultipart(new MimePartDataSource(batchBody));

        List<DataSource> result = new ArrayList<DataSource>();
        for (int i = 0; i < changeSets.getCount(); i++) {
            BodyPart part = changeSets.getBodyPart(i);

            result.add(new InputStreamDataSource(part.getInputStream(), part.getContentType()));
        }
        return result;
    }
}
