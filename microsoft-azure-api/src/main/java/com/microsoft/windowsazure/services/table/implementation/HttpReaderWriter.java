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
package com.microsoft.windowsazure.services.table.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Enumeration;

import javax.activation.DataSource;
import javax.inject.Inject;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

import com.sun.mail.util.LineInputStream;

public class HttpReaderWriter {

    @Inject
    public HttpReaderWriter() {
    }

    public StatusLine parseStatusLine(DataSource ds) {
        try {
            LineInputStream stream = new LineInputStream(ds.getInputStream());
            String line = stream.readLine();
            StringReader lineReader = new StringReader(line);

            expect(lineReader, "HTTP/1.1");
            expect(lineReader, " ");
            String statusString = extractInput(lineReader, ' ');
            String reason = extractInput(lineReader, -1);

            return new StatusLine().setStatus(Integer.parseInt(statusString)).setReason(reason);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InternetHeaders parseHeaders(DataSource ds) {
        try {
            return new InternetHeaders(ds.getInputStream());
        }
        catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream parseEntity(DataSource ds) {
        try {
            return ds.getInputStream();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendMethod(OutputStream stream, String verb, URI uri) {
        try {
            String method = String.format("%s %s %s\r\n", verb, uri, "HTTP/1.1");
            stream.write(method.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendHeaders(OutputStream stream, InternetHeaders headers) {
        try {
            // Headers
            @SuppressWarnings("unchecked")
            Enumeration<Header> e = headers.getAllHeaders();
            while (e.hasMoreElements()) {
                Header header = e.nextElement();

                String headerLine = String.format("%s: %s\r\n", header.getName(), header.getValue());
                stream.write(headerLine.getBytes("UTF-8"));
            }

            // Empty line
            stream.write("\r\n".getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendEntity(OutputStream stream, InputStream entity) {
        try {
            byte[] buffer = new byte[1024];
            while (true) {
                int n = entity.read(buffer);
                if (n == -1)
                    break;
                stream.write(buffer, 0, n);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void expect(Reader reader, String string) {
        try {
            for (int i = 0; i < string.length(); i++) {
                int ch = reader.read();
                if (ch < 0)
                    throw new RuntimeException(String.format("Expected '%s', found '%s' instead", string,
                            string.substring(0, i) + ch));
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractInput(Reader reader, int delimiter) {
        try {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == -1 || ch == delimiter)
                    break;

                sb.append((char) ch);
            }
            return sb.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class StatusLine {
        private int status;
        private String reason;

        public int getStatus() {
            return status;
        }

        public StatusLine setStatus(int status) {
            this.status = status;
            return this;
        }

        public String getReason() {
            return reason;
        }

        public StatusLine setReason(String reason) {
            this.reason = reason;
            return this;
        }
    }
}
