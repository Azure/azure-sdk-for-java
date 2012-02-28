/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.table.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * Reserved for internal use. A class used to read and write MIME requests and responses.
 */
class MimeHelper {
    /**
     * Reserved for internal use. A static factory method that generates a {@link StorageException} for invalid MIME
     * responses.
     * 
     * @return
     *         The {@link StorageException} for the invalid MIME response.
     */
    protected static StorageException generateMimeParseException() {
        return new StorageException(StorageErrorCodeStrings.OUT_OF_RANGE_INPUT, "Invalid MIME response received.",
                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
    }

    /**
     * Reserved for internal use. Returns the HTTP verb for a table operation.
     * 
     * @param operation
     *            The {@link TableOperation} instance to get the HTTP verb for.
     * @return
     *         A <code>String</code> containing the HTTP verb to use with the operation.
     */
    protected static String getHttpVerbForOperation(final TableOperation operation) {
        if (operation.getOperationType() == TableOperationType.INSERT) {
            return "POST";
        }
        else if (operation.getOperationType() == TableOperationType.DELETE) {
            return "DELETE";
        }
        else if (operation.getOperationType() == TableOperationType.MERGE
                || operation.getOperationType() == TableOperationType.INSERT_OR_MERGE) {
            return "MERGE";
        }
        else if (operation.getOperationType() == TableOperationType.REPLACE
                || operation.getOperationType() == TableOperationType.INSERT_OR_REPLACE) {
            return "PUT";
        }
        else if (operation.getOperationType() == TableOperationType.RETRIEVE) {
            return "GET";
        }
        else {
            throw new IllegalArgumentException("Unknown table operation");
        }
    }

    /**
     * Reserved for internal use. Returns the next non-blank line from the {@link BufferedReader}.
     * 
     * @param reader
     *            The {@link BufferedReader} to read lines from.
     * @return
     *         A <code>String</code> containing the next non-blank line from the {@link BufferedReader}, or
     *         <code>null</code>.
     * @throws IOException
     *             if an error occurs reading from the {@link BufferedReader}.
     */
    protected static String getNextLineSkippingBlankLines(final BufferedReader reader) throws IOException {
        String tString = null;
        do {
            tString = reader.readLine();
        } while (tString != null && tString.length() == 0);

        return tString;
    }

    /**
     * Reserved for internal use. Reads the response stream from a batch operation into an <code>ArrayList</code> of
     * {@link MimePart} objects.
     * 
     * @param inStream
     *            An {@link InputStream} containing the operation response stream.
     * @param expectedBundaryName
     *            A <code>String</code> containing the MIME part boundary string.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * @return
     *         An <code>ArrayList</code> of {@link MimePart} objects parsed from the input stream.
     * @throws IOException
     *             if an error occurs accessing the input stream.
     * @throws StorageException
     *             if an error occurs parsing the input stream.
     */
    protected static ArrayList<MimePart> readBatchResponseStream(final InputStream inStream,
            final String expectedBundaryName, final OperationContext opContext) throws IOException, StorageException {
        final ArrayList<MimePart> result = new ArrayList<MimePart>();
        final InputStreamReader streamReader = new InputStreamReader(inStream, "UTF-8");
        final BufferedReader reader = new BufferedReader(streamReader);
        final String mungedExpectedBoundaryName = "--".concat(expectedBundaryName);

        final MimeHeader docHeader = readMimeHeader(reader, opContext);
        if (docHeader.boundary == null || !docHeader.boundary.equals(mungedExpectedBoundaryName)) {
            throw generateMimeParseException();
        }

        MimeHeader currHeader = null;

        // No explicit changeset present
        if (docHeader.subBoundary == null) {
            do {
                result.add(readMimePart(reader, docHeader.boundary, opContext));
                currHeader = readMimeHeader(reader, opContext);
            } while (currHeader != null);
        }
        else {
            // explicit changeset present.
            currHeader = readMimeHeader(reader, opContext);
            if (currHeader == null) {
                throw new TableServiceException(
                        -1,
                        "An Error Occurred while processing the request, check the extended error information for more details.",
                        null, reader);
            }
            else {
                do {
                    result.add(readMimePart(reader, docHeader.subBoundary, opContext));
                    currHeader = readMimeHeader(reader, opContext);
                } while (currHeader != null);
            }
        }

        return result;
    }

    /**
     * Reserved for internal use. A static factory method that constructs a {@link MimeHeader} by parsing the MIME
     * header
     * data from a {@link BufferedReader}.
     * 
     * @param reader
     *            The {@link BufferedReader} containing the response stream to parse.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * @return
     *         A {@link MimeHeader} constructed by parsing the MIME header data from the {@link BufferedReader}.
     * @throws IOException
     *             if an error occurs accessing the input stream.
     * @throws StorageException
     *             if an error occurs parsing the input stream.
     */
    protected static MimeHeader readMimeHeader(final BufferedReader reader, final OperationContext opContext)
            throws IOException, StorageException {
        final MimeHeader retHeader = new MimeHeader();
        reader.mark(1024 * 1024);

        // First thing is separator
        retHeader.boundary = getNextLineSkippingBlankLines(reader);
        if (retHeader.boundary.endsWith("--")) {
            return null;
        }
        if (!retHeader.boundary.startsWith("--")) {
            reader.reset();
            return null;
        }

        for (int m = 0; m < 2; m++) {
            final String tempString = reader.readLine();
            if (tempString == null || tempString.length() == 0) {
                break;
            }

            if (tempString.startsWith("Content-Type:")) {
                final String[] headerVals = tempString.split("Content-Type: ");
                if (headerVals == null || headerVals.length != 2) {
                    throw generateMimeParseException();
                }
                retHeader.contentType = headerVals[1];
            }
            else if (tempString.startsWith("Content-Transfer-Encoding:")) {
                final String[] headerVals = tempString.split("Content-Transfer-Encoding: ");
                if (headerVals == null || headerVals.length != 2) {
                    throw generateMimeParseException();
                }
                retHeader.contentTransferEncoding = headerVals[1];
            }
            else {
                throw generateMimeParseException();
            }
        }

        // Validate headers
        if (Utility.isNullOrEmpty(retHeader.boundary) || retHeader.contentType == null) {
            throw generateMimeParseException();
        }

        if (retHeader.contentType.startsWith("multipart/mixed; boundary=")) {
            final String[] headerVals = retHeader.contentType.split("multipart/mixed; boundary=");
            if (headerVals == null || headerVals.length != 2) {
                throw generateMimeParseException();
            }
            retHeader.subBoundary = "--".concat(headerVals[1]);
        }
        else if (!retHeader.contentType.equals("application/http")) {
            throw generateMimeParseException();
        }

        if (retHeader.contentTransferEncoding != null && !retHeader.contentTransferEncoding.equals("binary")) {
            throw generateMimeParseException();
        }

        return retHeader;
    }

    // Returns at start of next mime boundary header
    /**
     * Reserved for internal use. A static factory method that generates a {@link MimePart} containing the next MIME
     * part read from the {@link BufferedReader}.
     * The {@link BufferedReader} is left positioned at the start of the next MIME boundary header.
     * 
     * @param reader
     *            The {@link BufferedReader} containing the response stream to parse.
     * @param boundary
     *            A <code>String</code> containing the MIME part boundary string.
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * @return
     *         A {@link MimePart} constructed by parsing the next MIME part data from the {@link BufferedReader}.
     * @throws IOException
     *             if an error occured accessing the input stream.
     * @throws StorageException
     *             if an error occured parsing the input stream.
     */
    protected static MimePart readMimePart(final BufferedReader reader, final String boundary,
            final OperationContext opContext) throws IOException, StorageException {
        final MimePart retPart = new MimePart();
        // Read HttpStatus code
        String tempStr = getNextLineSkippingBlankLines(reader);
        if (!tempStr.startsWith("HTTP/1.1 ")) {
            throw generateMimeParseException();
        }

        final String[] headerVals = tempStr.split(" ");

        if (headerVals.length < 3) {
            throw generateMimeParseException();
        }

        retPart.httpStatusCode = Integer.parseInt(headerVals[1]);
        // "HTTP/1.1 XXX ".length() => 13
        retPart.httpStatusMessage = tempStr.substring(13);

        // Read headers
        tempStr = reader.readLine();
        while (tempStr != null && tempStr.length() > 0) {
            final String[] headerParts = tempStr.split(": ");
            if (headerParts.length < 2) {
                throw generateMimeParseException();
            }

            retPart.headers.put(headerParts[0], headerParts[1]);
            tempStr = reader.readLine();
        }

        // Store xml payload
        reader.mark(1024 * 1024);
        tempStr = getNextLineSkippingBlankLines(reader);

        if (tempStr == null) {
            throw generateMimeParseException();
        }

        // empty body
        if (tempStr.startsWith(boundary)) {
            reader.reset();
            retPart.payload = Constants.EMPTY_STRING;
            return retPart;
        }
        else if (!tempStr.startsWith("<?xml version=")) {
            throw generateMimeParseException();
        }
        final StringBuilder payloadBuilder = new StringBuilder();
        // read until mime closure or end of file
        while (!tempStr.startsWith(boundary)) {
            payloadBuilder.append(tempStr);
            reader.mark(1024 * 1024);
            tempStr = getNextLineSkippingBlankLines(reader);
            if (tempStr == null) {
                throw generateMimeParseException();
            }
        }

        // positions stream at start of next MIME Header
        reader.reset();

        retPart.payload = payloadBuilder.toString();

        return retPart;
    }

    /**
     * Reserved for internal use. Writes the batch operation to the output stream using batch request syntax.
     * Batch request syntax is described in the MSDN topic <a
     * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd894038.aspx">Performing Entity Group
     * Transactions</a>.
     * 
     * @param outStream
     *            The {@link OutputStream} to write the batch request to.
     * @param tableName
     *            A <code>String</code> containing the name of the table to apply each operation to.
     * @param batch
     *            A {@link TableBatchOperation} containing the operations to write to the output stream
     * @param batchID
     *            A <code>String</code> containing the identifier to use as the MIME boundary for the batch request.
     * @param changeSet
     *            A <code>String</code> containing the identifier to use as the MIME boundary for operations within the
     *            batch.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * @throws IOException
     *             if an IO error occurs.
     * @throws URISyntaxException
     *             if an invalid URI is used.
     * @throws StorageException
     *             if an error occurs accessing the Storage service.
     * @throws XMLStreamException
     *             if an error occurs accessing the stream.
     */
    protected static void writeBatchToStream(final OutputStream outStream, final String tableName,
            final TableBatchOperation batch, final String batchID, final String changeSet,
            final OperationContext opContext) throws IOException, URISyntaxException, StorageException,
            XMLStreamException {
        final OutputStreamWriter outWriter = new OutputStreamWriter(outStream, "UTF8");

        int contentID = 0;
        boolean inChangeSet = false;
        for (final TableOperation op : batch) {
            if (op.getOperationType() == TableOperationType.RETRIEVE) {
                final QueryTableOperation qOp = (QueryTableOperation) op;

                if (inChangeSet) {
                    inChangeSet = false;
                    // Write Boundary end.
                    MimeHelper.writeMIMEBoundaryClosure(outWriter, changeSet);
                    outWriter.write("\r\n");
                }

                // Write MIME Header
                MimeHelper.writeMIMEBoundary(outWriter, batchID);
                outWriter.write("Content-Type: application/http\r\n");
                outWriter.write("Content-Transfer-Encoding: binary\r\n\r\n");

                outWriter.write(String.format("%s %s HTTP/1.1\r\n", getHttpVerbForOperation(op),
                        qOp.generateRequestIdentityWithTable(tableName)));

                outWriter.write("Host: host\r\n\r\n");
            }
            else {
                if (!inChangeSet) {
                    inChangeSet = true;
                    // New batch mime part
                    MimeHelper.writeMIMEBoundary(outWriter, batchID);
                    MimeHelper.writeMIMEContentType(outWriter, changeSet);
                    outWriter.write("\r\n");
                }

                // New mime part for changeset
                MimeHelper.writeMIMEBoundary(outWriter, changeSet);

                // Write Headers
                outWriter.write("Content-Type: application/http\r\n");
                outWriter.write("Content-Transfer-Encoding: binary\r\n\r\n");

                outWriter.write(String.format("%s %s HTTP/1.1\r\n", getHttpVerbForOperation(op),
                        op.generateRequestIdentityWithTable(tableName)));

                outWriter.write(String.format("Content-ID: %s\r\n", Integer.toString(contentID)));

                if (op.getOperationType() != TableOperationType.INSERT
                        && op.getOperationType() != TableOperationType.INSERT_OR_MERGE
                        && op.getOperationType() != TableOperationType.INSERT_OR_REPLACE) {
                    outWriter.write(String.format("If-Match: %s\r\n", op.getEntity().getEtag()));
                }

                if (op.getOperationType() == TableOperationType.DELETE) {
                    // empty body
                    outWriter.write("\r\n");
                }
                else {
                    outWriter.write("Content-Type: application/atom+xml;type=entry\r\n");
                    final String opString = writeStringForOperation(op, opContext);
                    outWriter.write(String.format("Content-Length: %s\r\n\r\n",
                            Integer.toString(opString.getBytes("UTF-8").length)));
                    outWriter.write(opString);
                }
                contentID = contentID + 1;
            }
        }

        if (inChangeSet) {
            MimeHelper.writeMIMEBoundaryClosure(outWriter, changeSet);
        }
        MimeHelper.writeMIMEBoundaryClosure(outWriter, batchID);

        outWriter.flush();
    }

    /**
     * Reserved for internal use. Writes a MIME part boundary to the output stream.
     * 
     * @param outWriter
     *            The {@link OutputStreamWriter} to write the MIME part boundary to.
     * @param boundaryID
     *            The <code>String</code> containing the MIME part boundary string.
     * @throws IOException
     *             if an error occurs writing to the output stream.
     */
    protected static void writeMIMEBoundary(final OutputStreamWriter outWriter, final String boundaryID)
            throws IOException {
        outWriter.write(String.format("--%s\r\n", boundaryID));
    }

    /**
     * Reserved for internal use. Writes a MIME part boundary closure to the output stream.
     * 
     * @param outWriter
     *            The {@link OutputStreamWriter} to write the MIME part boundary closure to.
     * @param boundaryID
     *            The <code>String</code> containing the MIME part boundary string.
     * @throws IOException
     *             if an error occurs writing to the output stream.
     */
    protected static void writeMIMEBoundaryClosure(final OutputStreamWriter outWriter, final String boundaryID)
            throws IOException {
        outWriter.write(String.format("--%s--\r\n", boundaryID));
    }

    /**
     * Reserved for internal use. Writes a MIME content type string to the output stream.
     * 
     * @param outWriter
     *            The {@link OutputStreamWriter} to write the MIME content type string to.
     * @param boundaryID
     *            The <code>String</code> containing the MIME part boundary string.
     * @throws IOException
     *             if an error occurs writing to the output stream.
     */
    protected static void writeMIMEContentType(final OutputStreamWriter outWriter, final String boundaryName)
            throws IOException {
        outWriter.write(String.format("Content-Type: multipart/mixed; boundary=%s\r\n", boundaryName));
    }

    /**
     * Reserved for internal use. Generates a <code>String</code> containing the entity associated with an operation in
     * AtomPub format.
     * 
     * @param operation
     *            A {@link TableOperation} containing the entity to write to the returned <code>String</code>.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * @return
     *         A <code>String</code> containing the entity associated with the operation in AtomPub format
     * @throws StorageException
     *             if a Storage error occurs.
     * @throws XMLStreamException
     *             if an error occurs creating or writing to the output string.
     */
    protected static String writeStringForOperation(final TableOperation operation, final OperationContext opContext)
            throws StorageException, XMLStreamException {
        final StringWriter outWriter = new StringWriter();
        final XMLOutputFactory xmlOutFactoryInst = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlw = xmlOutFactoryInst.createXMLStreamWriter(outWriter);

        AtomPubParser.writeSingleEntityToStream(operation.getEntity(), false, xmlw, opContext);
        outWriter.write("\r\n");

        return outWriter.toString();
    }
}
