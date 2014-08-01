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
package com.microsoft.azure.storage.analytics;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringEscapeUtils;

import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * RESERVED FOR INTERNAL USE. Reads LogRecord information from an InputStream.
 */
class LogRecordStreamReader extends InputStreamReader {

    /**
     * Holds the field delimiter character.
     */
    public static final char FIELD_DELIMITER = ';';

    /**
     * Holds the record delimiter character.
     */
    public static final char RECORD_DELIMITER = '\n';

    /**
     * Holds the quote character.
     */
    public static final char QUOTE_CHAR = '"';

    /**
     * Flag that indicates whether this is the start of a record.
     */
    private boolean isFirstFieldInRecord;

    /**
     * Peeked character, if it has been pulled already.
     */
    private Integer peekedCharacter = null;

    /**
     * Constructs a new LogRecordStreamReader to read the stream given.
     * 
     * @param stream
     */
    public LogRecordStreamReader(InputStream stream) {
        super(stream);

        this.isFirstFieldInRecord = true;
    }

    /**
     * Checks if another field exists in the record.
     * 
     * @return
     *         true if another field exists, false otherwise.
     * @throws IOException
     */
    public boolean hasMoreFieldsInRecord() throws IOException {
        return this.tryPeekDelimiter(LogRecordStreamReader.FIELD_DELIMITER);
    }

    /**
     * Checks to see if the end of the stream has been reached.
     * 
     * @return
     *         true if at the end of the stream, false otherwise.
     * @throws IOException
     */
    public boolean isEndOfFile() throws IOException {
        return (this.peek() == -1);
    }

    /**
     * Read a String from the stream.
     * 
     * @return
     *         the String read.
     * @throws IOException
     */
    public String readString() throws IOException {
        String temp = this.readField(false /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return temp;
        }
    }

    /**
     * Read a quoted String from the stream.
     * 
     * @return
     *         the String read.
     * @throws IOException
     */
    public String readQuotedString() throws IOException {
        String temp = this.readField(true /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return temp;
        }
    }

    /**
     * Read a Boolean from the stream.
     * 
     * @return
     *         the Boolean read.
     * @throws IOException
     */
    public Boolean readBoolean() throws IOException {
        String temp = this.readField(false /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return Boolean.parseBoolean(temp);
        }
    }

    /**
     * Read a Date from the stream.
     * 
     * @param format
     *            the format in which the date is stored, for parsing purposes.
     * @return
     *         the Date read.
     * @throws IOException
     * @throws ParseException
     */
    public Date readDate(DateFormat format) throws IOException, ParseException {
        String temp = this.readField(false /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return format.parse(temp);
        }
    }

    /**
     * Read a Double from the stream.
     * 
     * @return
     *         the Double read.
     * @throws IOException
     */
    public Double readDouble() throws IOException {
        String temp = this.readField(false /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return Double.parseDouble(temp);
        }
    }

    /**
     * Read a UUID from the stream.
     * 
     * @return
     *         the UUID read.
     * @throws IOException
     */
    public UUID readUuid() throws IOException {
        String temp = this.readField(false /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return UUID.fromString(temp);
        }
    }

    /**
     * Read an Integer from the stream.
     * 
     * @return
     *         the Integer read.
     * @throws IOException
     */
    public Integer readInteger() throws IOException {
        String temp = this.readField(false /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return Integer.parseInt(temp);
        }
    }

    /**
     * Read a Long from the stream.
     * 
     * @return
     *         the Long read.
     * @throws IOException
     */
    public Long readLong() throws IOException {
        String temp = this.readField(false /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return Long.parseLong(temp);
        }
    }

    /**
     * Read a URI from the stream.
     * 
     * @return
     *         the URI read.
     * @throws URISyntaxException
     * @throws IOException
     */
    public URI readUri() throws URISyntaxException, IOException {
        String temp = this.readField(true /* isQuotedString */);

        if (Utility.isNullOrEmpty(temp)) {
            return null;
        }
        else {
            return new URI(StringEscapeUtils.unescapeHtml4(temp));
        }
    }

    /**
     * Ends the current record by reading the record delimiter and adjusting internal state.
     * 
     * @throws IOException
     */
    public void endCurrentRecord() throws IOException {
        this.readDelimiter(LogRecordStreamReader.RECORD_DELIMITER);
        this.isFirstFieldInRecord = true;
    }

    /**
     * Read a delimiter from the stream.
     * 
     * @param delimiter
     *            the delimiter to read.
     * @throws IOException
     */
    private void readDelimiter(char delimiter) throws IOException {
        if (this.isEndOfFile()) {
            throw new EOFException(SR.LOG_STREAM_END_ERROR);
        }
        else {
            int read = this.read();
            if (read == -1 || (char) read != delimiter) {
                throw new IllegalStateException(SR.LOG_STREAM_DELIMITER_ERROR);
            }
        }
    }

    /**
     * Checks to see if the next character is the delimiter expected.
     * 
     * @param delimiter
     *            the delimiter to try to peek.
     * @return
     * @throws IOException
     */
    private boolean tryPeekDelimiter(char delimiter) throws IOException {
        if (this.isEndOfFile()) {
            throw new EOFException(SR.LOG_STREAM_END_ERROR);
        }
        else {
            if ((char) this.peek() != delimiter) {
                return false;
            }
            else {
                return true;
            }
        }
    }

    /**
     * Read a field from the stream.
     * 
     * @param isQuotedString
     *            whether the field is encased in quotes and escaped.
     * @return
     *         the field read.
     * @throws IOException
     */
    private String readField(boolean isQuotedString) throws IOException {
        if (!this.isFirstFieldInRecord) {
            this.readDelimiter(LogRecordStreamReader.FIELD_DELIMITER);
        }
        else {
            this.isFirstFieldInRecord = false;
        }

        // Read a field, handling field/record delimiters in quotes and not counting them,
        // and also check that there are no record delimiters since they are only expected
        // outside of a field.
        // Note: We only need to handle strings that are quoted once from the beginning,
        // (e.g. "mystring"). We do not need to handle nested quotes or anything because
        // we control the string format. 
        StringBuilder fieldBuilder = new StringBuilder();
        boolean hasSeenQuoteForQuotedString = false;
        boolean isExpectingDelimiterForNextCharacterForQuotedString = false;
        while (true) {
            // If EOF when we haven't read the delimiter; unexpected.
            if (this.isEndOfFile()) {
                throw new EOFException(SR.LOG_STREAM_END_ERROR);
            }

            // Since we just checked isEndOfFile above, we know this is a char.
            char c = (char) this.peek();

            // If we hit a delimiter that is not quoted or we hit the delimiter for
            // a quoted string or we hit the empty value string and hit a delimiter,
            // then we have finished reading the field.
            // Note: The empty value string is the only string that we don't require
            // quotes for for a quoted string.
            if ((!isQuotedString || isExpectingDelimiterForNextCharacterForQuotedString || fieldBuilder.length() == 0)
                    && (c == LogRecordStreamReader.FIELD_DELIMITER || c == LogRecordStreamReader.RECORD_DELIMITER)) {
                // The delimiter character was peeked but not read -- they'll be consumed 
                // on either the next call to readField or to EndCurrentRecord.
                break;
            }

            if (isExpectingDelimiterForNextCharacterForQuotedString) {
                // We finished reading a quoted string but found no delimiter following it.
                throw new IllegalStateException(SR.LOG_STREAM_QUOTE_ERROR);
            }

            // The character was not a delimiter, so consume and add to builder.
            this.read();
            fieldBuilder.append(c);

            // We need to handle quotes specially since quoted delimiters
            // do not count since they are considered to be part of the
            // quoted string and not actually a delimiter.
            // Note: We use a specific quote character since we control the format
            // and we only allow non-encoded quote characters at the beginning/end
            // of the string.
            if (c == LogRecordStreamReader.QUOTE_CHAR) {
                if (!isQuotedString) {
                    // Non-encoded quote character only allowed for quoted strings.
                    throw new IllegalStateException(SR.LOG_STREAM_QUOTE_ERROR);
                }
                else if (fieldBuilder.length() == 1) {
                    // Opening quote for a quoted string.
                    hasSeenQuoteForQuotedString = true;
                }
                else if (hasSeenQuoteForQuotedString) {
                    // Closing quote for a quoted string.
                    isExpectingDelimiterForNextCharacterForQuotedString = true;
                }
                else {
                    // Unexpected non-encoded quote.
                    throw new IllegalStateException(SR.LOG_STREAM_QUOTE_ERROR);
                }
            }
        }

        String field;

        // Note: For quoted strings we remove the quotes.
        // We do not do this for the empty value string since it represents empty
        // and we don't write that out in quotes even for quoted strings.
        if (isQuotedString && fieldBuilder.length() != 0) {
            field = fieldBuilder.substring(1, fieldBuilder.length() - 1);
        }
        else {
            field = fieldBuilder.toString();
        }

        return field;
    }

    @Override
    public int read() throws IOException {
        // Before calling the super, check to see if a char has been peeked.
        // Short circuit, reset it, and return it if so.
        if (this.peekedCharacter != null) {
            int temp = this.peekedCharacter;
            this.peekedCharacter = null;
            return temp;
        }
        else {
            return super.read();
        }
    }

    /**
     * Peek a character from the stream. This character is not consumed until read() is called.
     * 
     * @return
     *         the character peeked, or -1 if none exist.
     * @throws IOException
     */
    protected int peek() throws IOException {
        if (this.peekedCharacter != null) {
            // If we already have peeked, just return that.
            return this.peekedCharacter;
        }
        else {
            this.peekedCharacter = super.read();
            return this.peekedCharacter;
        }
    }
}
