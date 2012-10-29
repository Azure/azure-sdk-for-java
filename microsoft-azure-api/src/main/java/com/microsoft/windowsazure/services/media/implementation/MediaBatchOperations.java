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

package com.microsoft.windowsazure.services.media.implementation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.windowsazure.services.table.implementation.InputStreamDataSource;

/**
 * The Class MediaBatchOperations.
 */
public class MediaBatchOperations {

    /** The operations. */
    private final List<Operation> operations;

    /** The service uri. */
    private final URI serviceURI;

    /** The Odata atom marshaller. */
    private final ODataAtomMarshaller oDataAtomMarshaller;

    /**
     * Instantiates a new media batch operations.
     * 
     * @param serviceURI
     *            the service uri
     * @throws JAXBException
     *             the jAXB exception
     * @throws ParserConfigurationException
     *             the parser configuration exception
     */
    public MediaBatchOperations(URI serviceURI) throws JAXBException, ParserConfigurationException {
        this.serviceURI = serviceURI;
        this.oDataAtomMarshaller = new ODataAtomMarshaller();
        this.operations = new ArrayList<Operation>();
    }

    /**
     * Gets the mime multipart.
     * 
     * @return the mime multipart
     * @throws MessagingException
     *             the messaging exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JAXBException
     *             the jAXB exception
     */
    public MimeMultipart getMimeMultipart() throws MessagingException, IOException, JAXBException {
        List<DataSource> bodyPartContents = new ArrayList<DataSource>();
        int contentId = 1;

        int jobContentId = addJobPart(bodyPartContents, contentId);
        URI taskURI = UriBuilder.fromUri(serviceURI).path(String.format("$%d", jobContentId)).path("Tasks").build();
        addTaskPart(bodyPartContents, taskURI, contentId);

        return toMimeMultipart(bodyPartContents);

    }

    /**
     * Adds the job part.
     * 
     * @param bodyPartContents
     *            the body part contents
     * @param contentId
     *            the content id
     * @return the int
     * @throws JAXBException
     *             the jAXB exception
     */
    private int addJobPart(List<DataSource> bodyPartContents, int contentId) throws JAXBException {
        int jobContentId = contentId;
        ValidateJobOperation();

        for (Operation operation : operations) {
            DataSource bodyPartContent = null;
            if (operation instanceof CreateJobOperation) {
                CreateJobOperation createJobOperation = (CreateJobOperation) operation;
                jobContentId = contentId;
                URI jobUri = null;
                bodyPartContent = createBatchCreateEntityPart("Jobs", createJobOperation.getJob(), jobUri, contentId);
                contentId++;
                if (bodyPartContent != null) {
                    bodyPartContents.add(bodyPartContent);
                    break;
                }
            }
        }
        return jobContentId;
    }

    private void ValidateJobOperation() {
        int jobCount = 0;
        for (Operation operation : operations) {
            if (operation instanceof CreateJobOperation) {
                jobCount++;
            }
        }

        if (jobCount != 1) {
            throw new IllegalArgumentException(String.format(
                    "The Job operation is invalid, expect 1 but get %s job(s). ", jobCount));
        }
    }

    /**
     * Adds the task part.
     * 
     * @param bodyPartContents
     *            the body part contents
     * @param taskURI
     *            the task uri
     * @param contentId
     *            the content id
     * @throws JAXBException
     *             the jAXB exception
     */
    private void addTaskPart(List<DataSource> bodyPartContents, URI taskURI, int contentId) throws JAXBException {
        for (Operation operation : operations) {
            DataSource bodyPartContent = null;
            if (operation instanceof CreateTaskOperation) {
                CreateTaskOperation createTaskOperation = (CreateTaskOperation) operation;
                bodyPartContent = createBatchCreateEntityPart("Tasks", createTaskOperation, taskURI, contentId);
                contentId++;
            }

            if (bodyPartContent != null) {
                bodyPartContents.add(bodyPartContent);
            }
        }
    }

    /**
     * To mime multipart.
     * 
     * @param bodyPartContents
     *            the body part contents
     * @return the mime multipart
     * @throws MessagingException
     *             the messaging exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private MimeMultipart toMimeMultipart(List<DataSource> bodyPartContents) throws MessagingException, IOException {
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
     * Creates the batch create entity part.
     * 
     * @param entityName
     *            the entity name
     * @param entity
     *            the entity
     * @param uri
     *            the uri
     * @param contentId
     *            the content id
     * @return the data source
     * @throws JAXBException
     *             the jAXB exception
     */
    private DataSource createBatchCreateEntityPart(String entityName, Object entity, URI uri, int contentId)
            throws JAXBException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.oDataAtomMarshaller.marshalEntry(entity, stream);
        byte[] bytes = stream.toByteArray();

        // adds header
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("Content-ID", Integer.toString(contentId));
        headers.addHeader("Content-Type", "application/atom+xml;type=entry");
        headers.addHeader("Content-Length", Integer.toString(bytes.length));

        // adds body
        ByteArrayOutputStream httpRequest = new ByteArrayOutputStream();
        addHttpMethod(httpRequest, "POST", uri);
        appendHeaders(httpRequest, headers);
        appendEntity(httpRequest, new ByteArrayInputStream(bytes));

        DataSource bodyPartContent = new InputStreamDataSource(new ByteArrayInputStream(httpRequest.toByteArray()),
                "application/http");
        return bodyPartContent;
    }

    /**
     * Adds the operation.
     * 
     * @param operation
     *            the operation
     */
    public void addOperation(Operation operation) {
        this.operations.add(operation);
    }

    /**
     * Adds the http method.
     * 
     * @param outputStream
     *            the output stream
     * @param verb
     *            the verb
     * @param uri
     *            the uri
     */
    private void addHttpMethod(ByteArrayOutputStream outputStream, String verb, URI uri) {
        try {
            String method = String.format("%s %s HTTP/1.1\r\n", verb, uri);
            outputStream.write(method.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Append headers.
     * 
     * @param outputStream
     *            the output stream
     * @param internetHeaders
     *            the internet headers
     */
    private void appendHeaders(OutputStream outputStream, InternetHeaders internetHeaders) {
        try {
            // Headers
            @SuppressWarnings("unchecked")
            Enumeration<Header> headers = internetHeaders.getAllHeaders();
            while (headers.hasMoreElements()) {
                Header header = headers.nextElement();
                String headerLine = String.format("%s: %s\r\n", header.getName(), header.getValue());
                outputStream.write(headerLine.getBytes("UTF-8"));
            }

            // Empty line
            outputStream.write("\r\n".getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Append entity.
     * 
     * @param outputStream
     *            the output stream
     * @param byteArrayInputStream
     *            the byte array input stream
     */
    private void appendEntity(OutputStream outputStream, ByteArrayInputStream byteArrayInputStream) {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int bytesRead = byteArrayInputStream.read(buffer);
                if (bytesRead <= 0)
                    break;
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
