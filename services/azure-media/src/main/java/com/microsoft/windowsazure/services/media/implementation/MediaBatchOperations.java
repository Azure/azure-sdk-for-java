/**
 * Copyright Microsoft Corporation
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePartDataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.windowsazure.core.utils.InputStreamDataSource;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.EntityBatchOperation;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.JobInfo;
import com.microsoft.windowsazure.services.media.models.Task;
import com.microsoft.windowsazure.services.media.models.TaskInfo;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.util.ReaderWriter;

/**
 * The Class MediaBatchOperations.
 */
public class MediaBatchOperations {

    private static final int BUFFER_SIZE = 1024;

    private static final int HTTP_ERROR = 400;

    /** The operations. */
    private final List<EntityBatchOperation> entityBatchOperations;

    /** The service uri. */
    private final URI serviceURI;

    /** The Odata atom marshaller. */
    private final ODataAtomMarshaller oDataAtomMarshaller;

    /** The o data atom unmarshaller. */
    private final ODataAtomUnmarshaller oDataAtomUnmarshaller;

    private final String batchId;

    /**
     * Instantiates a new media batch operations.
     * 
     * @param serviceURI
     *            the service uri
     * @throws ParserConfigurationException
     * @throws JAXBException
     */
    public MediaBatchOperations(URI serviceURI) throws JAXBException,
            ParserConfigurationException {
        if (serviceURI == null) {
            throw new IllegalArgumentException(
                    "The service URI cannot be null.");
        }
        this.serviceURI = serviceURI;
        this.oDataAtomMarshaller = new ODataAtomMarshaller();
        this.oDataAtomUnmarshaller = new ODataAtomUnmarshaller();
        this.entityBatchOperations = new ArrayList<EntityBatchOperation>();
        batchId = String.format("batch_%s", UUID.randomUUID().toString());
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
    public MimeMultipart getMimeMultipart() throws MessagingException,
            IOException, JAXBException {
        List<DataSource> bodyPartContents = createRequestBody();
        return toMimeMultipart(bodyPartContents);

    }

    private List<DataSource> createRequestBody() throws JAXBException {
        List<DataSource> bodyPartContents = new ArrayList<DataSource>();
        int contentId = 1;

        URI jobURI = UriBuilder.fromUri(serviceURI).path("Jobs").build();
        int jobContentId = addJobPart(bodyPartContents, jobURI, contentId);
        contentId++;

        URI taskURI = UriBuilder.fromUri(serviceURI)
                .path(String.format("$%d", jobContentId)).path("Tasks").build();
        addTaskPart(bodyPartContents, taskURI, contentId);
        return bodyPartContents;

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
    private int addJobPart(List<DataSource> bodyPartContents, URI jobURI,
            int contentId) throws JAXBException {
        int jobContentId = contentId;
        validateJobOperation();

        for (EntityBatchOperation entityBatchOperation : entityBatchOperations) {
            DataSource bodyPartContent = null;
            if (entityBatchOperation instanceof Job.CreateBatchOperation) {
                Job.CreateBatchOperation jobCreateBatchOperation = (Job.CreateBatchOperation) entityBatchOperation;
                jobContentId = contentId;
                bodyPartContent = createBatchCreateEntityPart(
                        jobCreateBatchOperation.getVerb(), "Jobs",
                        jobCreateBatchOperation.getEntryType(), jobURI,
                        contentId);
                contentId++;
                if (bodyPartContent != null) {
                    bodyPartContents.add(bodyPartContent);
                    break;
                }
            }
        }
        return jobContentId;
    }

    private void validateJobOperation() {
        int jobCount = 0;
        for (EntityBatchOperation entityBatchOperation : entityBatchOperations) {
            if (entityBatchOperation instanceof Job.CreateBatchOperation) {
                jobCount++;
            }
        }

        if (jobCount != 1) {
            throw new IllegalArgumentException(
                    String.format(
                            "The Job operation is invalid, expect 1 but get %s job(s). ",
                            jobCount));
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
    private void addTaskPart(List<DataSource> bodyPartContents, URI taskURI,
            int contentId) throws JAXBException {
        for (EntityBatchOperation entityBatchOperation : entityBatchOperations) {
            DataSource bodyPartContent = null;
            if (entityBatchOperation instanceof Task.CreateBatchOperation) {
                Task.CreateBatchOperation createTaskOperation = (Task.CreateBatchOperation) entityBatchOperation;
                bodyPartContent = createBatchCreateEntityPart(
                        createTaskOperation.getVerb(), "Tasks",
                        createTaskOperation.getEntryType(), taskURI, contentId);
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
    private MimeMultipart toMimeMultipart(List<DataSource> bodyPartContents)
            throws MessagingException, IOException {
        String changeSetId = String.format("changeset_%s", UUID.randomUUID()
                .toString());
        MimeMultipart changeSets = createChangeSets(bodyPartContents,
                changeSetId);
        MimeBodyPart mimeBodyPart = createMimeBodyPart(changeSets, changeSetId);

        MimeMultipart mimeMultipart = new BatchMimeMultipart(
                new SetBoundaryMultipartDataSource(batchId));
        mimeMultipart.addBodyPart(mimeBodyPart);
        return mimeMultipart;

    }

    private MimeBodyPart createMimeBodyPart(MimeMultipart changeSets,
            String changeSetId) throws MessagingException {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(changeSets);
        String contentType = String.format("multipart/mixed; boundary=%s",
                changeSetId);
        mimeBodyPart.setHeader("Content-Type", contentType);
        return mimeBodyPart;
    }

    private MimeMultipart createChangeSets(List<DataSource> bodyPartContents,
            String changeSetId) throws MessagingException {

        MimeMultipart changeSets = new MimeMultipart(
                new SetBoundaryMultipartDataSource(changeSetId));

        for (DataSource bodyPart : bodyPartContents) {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();

            mimeBodyPart.setDataHandler(new DataHandler(bodyPart));
            mimeBodyPart.setHeader("Content-Type", bodyPart.getContentType());
            mimeBodyPart.setHeader("Content-Transfer-Encoding", "binary");

            changeSets.addBodyPart(mimeBodyPart);
        }

        return changeSets;
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
    private DataSource createBatchCreateEntityPart(String verb,
            String entityName, EntryType entryType, URI uri, int contentId)
            throws JAXBException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.oDataAtomMarshaller.marshalEntryType(entryType, stream);
        byte[] bytes = stream.toByteArray();

        // adds header
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("Content-ID", Integer.toString(contentId));
        headers.addHeader("Content-Type", "application/atom+xml;type=entry");
        headers.addHeader("Content-Length", Integer.toString(bytes.length));
        headers.addHeader("DataServiceVersion", "3.0;NetFx");
        headers.addHeader("MaxDataServiceVersion", "3.0;NetFx");

        // adds body
        ByteArrayOutputStream httpRequest = new ByteArrayOutputStream();
        addHttpMethod(httpRequest, verb, uri);
        appendHeaders(httpRequest, headers);
        appendEntity(httpRequest, new ByteArrayInputStream(bytes));

        DataSource bodyPartContent = new InputStreamDataSource(
                new ByteArrayInputStream(httpRequest.toByteArray()),
                "application/http");
        return bodyPartContent;
    }

    /**
     * Parses the batch result.
     * 
     * @param response
     *            the response
     * @param mediaBatchOperations
     *            the media batch operations
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ServiceException
     *             the service exception
     */
    public void parseBatchResult(ClientResponse response) throws IOException,
            ServiceException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = response.getEntityInputStream();
        ReaderWriter.writeTo(inputStream, byteArrayOutputStream);
        response.setEntityInputStream(new ByteArrayInputStream(
                byteArrayOutputStream.toByteArray()));
        JobInfo jobInfo;

        List<DataSource> parts = parseParts(response.getEntityInputStream(),
                response.getHeaders().getFirst("Content-Type"));

        if (parts.size() == 0 || parts.size() > entityBatchOperations.size()) {
            throw new UniformInterfaceException(String.format(
                    "Batch response from server does not contain the correct amount "
                            + "of parts (expecting %d, received %d instead)",
                    parts.size(), entityBatchOperations.size()), response);
        }

        for (int i = 0; i < parts.size(); i++) {
            DataSource ds = parts.get(i);
            EntityBatchOperation entityBatchOperation = entityBatchOperations
                    .get(i);

            StatusLine status = StatusLine.create(ds);
            InternetHeaders headers = parseHeaders(ds);
            InputStream content = parseEntity(ds);

            if (status.getStatus() >= HTTP_ERROR) {

                InBoundHeaders inBoundHeaders = new InBoundHeaders();
                @SuppressWarnings("unchecked")
                Enumeration<Header> e = headers.getAllHeaders();
                while (e.hasMoreElements()) {
                    Header header = e.nextElement();
                    inBoundHeaders.putSingle(header.getName(),
                            header.getValue());
                }

                ClientResponse clientResponse = new ClientResponse(
                        status.getStatus(), inBoundHeaders, content, null);

                UniformInterfaceException uniformInterfaceException = new UniformInterfaceException(
                        clientResponse);
                throw uniformInterfaceException;
            } else if (entityBatchOperation instanceof Job.CreateBatchOperation) {

                try {
                    jobInfo = oDataAtomUnmarshaller.unmarshalEntry(content,
                            JobInfo.class);
                    Job.CreateBatchOperation jobCreateBatchOperation = (Job.CreateBatchOperation) entityBatchOperation;
                    jobCreateBatchOperation.setJobInfo(jobInfo);
                } catch (JAXBException e) {
                    throw new ServiceException(e);
                }
            } else if (entityBatchOperation instanceof Task.CreateBatchOperation) {
                try {
                    oDataAtomUnmarshaller.unmarshalEntry(content,
                            TaskInfo.class);
                } catch (JAXBException e) {
                    throw new ServiceException(e);
                }
            }
        }
    }

    /**
     * Parses the headers.
     * 
     * @param ds
     *            the ds
     * @return the internet headers
     */
    private InternetHeaders parseHeaders(DataSource ds) {
        try {
            return new InternetHeaders(ds.getInputStream());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the entity.
     * 
     * @param ds
     *            the ds
     * @return the input stream
     */
    private InputStream parseEntity(DataSource ds) {
        try {
            return ds.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the parts.
     * 
     * @param entityInputStream
     *            the entity input stream
     * @param contentType
     *            the content type
     * @return the list
     */
    private List<DataSource> parseParts(final InputStream entityInputStream,
            final String contentType) {
        try {
            return parsePartsCore(entityInputStream, contentType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses the parts core.
     * 
     * @param entityInputStream
     *            the entity input stream
     * @param contentType
     *            the content type
     * @return the list
     * @throws MessagingException
     *             the messaging exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private List<DataSource> parsePartsCore(InputStream entityInputStream,
            String contentType) throws MessagingException, IOException {
        DataSource dataSource = new InputStreamDataSource(entityInputStream,
                contentType);
        MimeMultipart batch = new MimeMultipart(dataSource);
        MimeBodyPart batchBody = (MimeBodyPart) batch.getBodyPart(0);

        MimeMultipart changeSets = new MimeMultipart(new MimePartDataSource(
                batchBody));

        List<DataSource> result = new ArrayList<DataSource>();
        for (int i = 0; i < changeSets.getCount(); i++) {
            BodyPart part = changeSets.getBodyPart(i);

            result.add(new InputStreamDataSource(part.getInputStream(), part
                    .getContentType()));
        }
        return result;
    }

    /**
     * Adds the operation.
     * 
     * @param entityBatchOperation
     *            the operation
     */
    public void addOperation(EntityBatchOperation entityBatchOperation) {
        this.entityBatchOperations.add(entityBatchOperation);
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
    private void addHttpMethod(ByteArrayOutputStream outputStream, String verb,
            URI uri) {
        try {
            String method = String.format("%s %s HTTP/1.1\r\n", verb, uri);
            outputStream.write(method.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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
    private void appendHeaders(OutputStream outputStream,
            InternetHeaders internetHeaders) {
        try {
            // Headers
            @SuppressWarnings("unchecked")
            Enumeration<Header> headers = internetHeaders.getAllHeaders();
            while (headers.hasMoreElements()) {
                Header header = headers.nextElement();
                String headerLine = String.format("%s: %s\r\n",
                        header.getName(), header.getValue());
                outputStream.write(headerLine.getBytes("UTF-8"));
            }

            // Empty line
            outputStream.write("\r\n".getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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
    private void appendEntity(OutputStream outputStream,
            ByteArrayInputStream byteArrayInputStream) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int bytesRead = byteArrayInputStream.read(buffer);
                if (bytesRead <= 0) {
                    break;
                }
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<EntityBatchOperation> getOperations() {
        return entityBatchOperations;
    }

    public String getBatchId() {
        return this.batchId;
    }

    public MediaType getContentType() {
        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put("boundary", this.batchId);
        MediaType contentType = new MediaType("multipart", "mixed", parameters);
        return contentType;
    }

}
