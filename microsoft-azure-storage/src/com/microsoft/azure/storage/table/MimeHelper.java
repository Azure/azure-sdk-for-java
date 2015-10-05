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

package com.microsoft.azure.storage.table;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageErrorCodeStrings;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.core.PathUtility;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.UriQueryBuilder;
import com.microsoft.azure.storage.core.Utility;

/**
 * Reserved for internal use. A class used to read and write MIME requests and responses.
 */
final class MimeHelper {
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
     * @param format
     *            The {@link TablePayloadFormat} that will be used for parsing
     * @return
     *         An <code>ArrayList</code> of {@link MimePart} objects parsed from the input stream.
     * @throws StorageException
     *             if a storage service error occurs.
     * @throws IOException
     *             if an error occurs while accessing the stream with Json.
     */
    static ArrayList<MimePart> readBatchResponseStream(final InputStream inStream, final String expectedBundaryName,
            final OperationContext opContext, TablePayloadFormat format) throws IOException, StorageException {
        final ArrayList<MimePart> result = new ArrayList<MimePart>();
        final InputStreamReader streamReader = new InputStreamReader(inStream, Constants.UTF8_CHARSET);
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
                throw generateMimeParseException();
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
     * Reserved for internal use. Writes the batch operation to the output stream using batch request syntax.
     * Batch request syntax is described in the MSDN topic <a
     * href="http://msdn.microsoft.com/en-us/library/azure/dd894038.aspx">Performing Entity Group
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
     */
    static void writeBatchToStream(final OutputStream outStream, final TableRequestOptions options,
            final String tableName, final URI baseUri, final TableBatchOperation batch, final String batchID,
            final String changeSet, final OperationContext opContext) throws IOException, URISyntaxException,
            StorageException {
        final OutputStreamWriter outWriter = new OutputStreamWriter(outStream, Constants.UTF8_CHARSET);

        MimePart mimePart;
        int contentID = 0;
        boolean isQuery = batch.size() == 1 && batch.get(0).getOperationType() == TableOperationType.RETRIEVE;

        // when batch is made, a check is done to make sure only one retrieve is added
        if (isQuery) {
            final QueryTableOperation qOp = (QueryTableOperation) batch.get(0);
            // Write MIME batch Header
            MimeHelper.writeMIMEBoundary(outWriter, batchID);

            mimePart = new MimePart();
            mimePart.op = qOp.getOperationType();

            UriQueryBuilder builder = new UriQueryBuilder();
            mimePart.requestIdentity = builder.addToURI(PathUtility.appendPathToSingleUri(baseUri,
                    qOp.generateRequestIdentityWithTable(tableName)));

            mimePart.headers.put(Constants.HeaderConstants.ACCEPT,
                    generateAcceptHeaderValue(options.getTablePayloadFormat()));
            mimePart.headers.put(TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION,
                    TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION_VALUE);

            outWriter.write(mimePart.toRequestString());
        }
        else {
            // Write MIME batch Header
            MimeHelper.writeMIMEBoundary(outWriter, batchID);
            MimeHelper.writeMIMEContentType(outWriter, changeSet);
            outWriter.write("\r\n");

            // Write each operation
            for (final TableOperation op : batch) {
                // New mime part for changeset
                MimeHelper.writeMIMEBoundary(outWriter, changeSet);

                mimePart = new MimePart();
                mimePart.op = op.getOperationType();

                UriQueryBuilder builder = new UriQueryBuilder();
                mimePart.requestIdentity = builder.addToURI(PathUtility.appendPathToSingleUri(baseUri,
                        op.generateRequestIdentityWithTable(tableName)));

                mimePart.headers.put(TableConstants.HeaderConstants.CONTENT_ID, Integer.toString(contentID));
                mimePart.headers.put(Constants.HeaderConstants.ACCEPT,
                        generateAcceptHeaderValue(options.getTablePayloadFormat()));
                mimePart.headers.put(TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION,
                        TableConstants.HeaderConstants.MAX_DATA_SERVICE_VERSION_VALUE);

                if (op.getOperationType() == TableOperationType.INSERT_OR_MERGE
                        || op.getOperationType() == TableOperationType.MERGE) {
                    // post tunnelling
                    mimePart.headers.put(TableConstants.HeaderConstants.X_HTTP_METHOD,
                            TableOperationType.MERGE.toString());
                }

                // etag
                if (op.getOperationType() == TableOperationType.DELETE
                        || op.getOperationType() == TableOperationType.REPLACE
                        || op.getOperationType() == TableOperationType.MERGE) {
                    if (op.getEntity() != null && op.getEntity().getEtag() != null) {
                        mimePart.headers.put(Constants.HeaderConstants.IF_MATCH, op.getEntity().getEtag());
                    }
                }

                // prefer header
                if (op.getOperationType() == TableOperationType.INSERT) {
                    mimePart.headers.put(TableConstants.HeaderConstants.PREFER,
                            op.getEchoContent() ? TableConstants.HeaderConstants.RETURN_CONTENT
                                    : TableConstants.HeaderConstants.RETURN_NO_CONTENT);
                }

                if (op.getOperationType() != TableOperationType.DELETE) {
                    mimePart.headers.put(Constants.HeaderConstants.CONTENT_TYPE,
                            TableConstants.HeaderConstants.JSON_CONTENT_TYPE);
                    mimePart.payload = writeStringForOperation(op, options.getTablePayloadFormat(), opContext);
                    mimePart.headers.put(Constants.HeaderConstants.CONTENT_LENGTH,
                            Integer.toString(mimePart.payload.getBytes(Constants.UTF8_CHARSET).length));
                }

                // write the request (no body)
                outWriter.write(mimePart.toRequestString());

                contentID = contentID + 1;
            }
        }

        if (!isQuery) {
            // end changeset
            MimeHelper.writeMIMEBoundaryClosure(outWriter, changeSet);
        }
        // end batch
        MimeHelper.writeMIMEBoundaryClosure(outWriter, batchID);

        outWriter.flush();
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
    private static MimeHeader readMimeHeader(final BufferedReader reader, final OperationContext opContext)
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
    private static MimePart readMimePart(final BufferedReader reader, final String boundary,
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
     * Reserved for internal use. Writes a MIME part boundary to the output stream.
     * 
     * @param outWriter
     *            The {@link OutputStreamWriter} to write the MIME part boundary to.
     * @param boundaryID
     *            The <code>String</code> containing the MIME part boundary string.
     * @throws IOException
     *             if an error occurs writing to the output stream.
     */
    private static void writeMIMEBoundary(final OutputStreamWriter outWriter, final String boundaryID)
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
    private static void writeMIMEBoundaryClosure(final OutputStreamWriter outWriter, final String boundaryID)
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
    private static void writeMIMEContentType(final OutputStreamWriter outWriter, final String boundaryName)
            throws IOException {
        outWriter.write(String.format("Content-Type: multipart/mixed; boundary=%s\r\n", boundaryName));
    }

    /**
     * Reserved for internal use. Generates a <code>String</code> containing the entity associated with an operation in
     * Json format.
     * 
     * @param operation
     *            A {@link TableOperation} containing the entity to write to the returned <code>String</code>.
     * @param opContext
     *            An {@link OperationContext} object for tracking the current operation. Specify <code>null</code> to
     *            safely ignore operation context.
     * @return
     *         A <code>String</code> containing the entity associated with the operation in Json format
     * @throws StorageException
     *             if a Storage error occurs.
     * @throws IOException
     */
    private static String writeStringForOperation(final TableOperation operation, TablePayloadFormat format,
            final OperationContext opContext) throws StorageException, IOException {
        Utility.assertNotNull("entity", operation.getEntity());
        final StringWriter outWriter = new StringWriter();

        TableEntitySerializer.writeSingleEntityToString(outWriter, format, operation.getEntity(), false, opContext);
        outWriter.write("\r\n");

        return outWriter.toString();
    }

    private static String generateAcceptHeaderValue(TablePayloadFormat payloadFormat) {
        if (payloadFormat == TablePayloadFormat.JsonFullMetadata) {
            return TableConstants.HeaderConstants.JSON_FULL_METADATA_ACCEPT_TYPE;
        }
        else if (payloadFormat == TablePayloadFormat.Json) {
            return TableConstants.HeaderConstants.JSON_ACCEPT_TYPE;
        }
        else {
            return TableConstants.HeaderConstants.JSON_NO_METADATA_ACCEPT_TYPE;
        }
    }

    /**
     * Reserved for internal use. A static factory method that generates a {@link StorageException} for invalid MIME
     * responses.
     * 
     * @return
     *         The {@link StorageException} for the invalid MIME response.
     */
    private static StorageException generateMimeParseException() {
        return new StorageException(StorageErrorCodeStrings.OUT_OF_RANGE_INPUT, SR.INVALID_MIME_RESPONSE,
                Constants.HeaderConstants.HTTP_UNUSED_306, null, null);
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
    private static String getNextLineSkippingBlankLines(final BufferedReader reader) throws IOException {
        String tString = null;
        do {
            tString = reader.readLine();
        } while (tString != null && tString.length() == 0);

        return tString;
    }
}
