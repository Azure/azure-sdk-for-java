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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.activation.DataSource;

import com.sun.mail.util.LineInputStream;

public class StatusLine {
    private static final int DELIMITER = -1;
    private int status;
    private String reason;

    public static StatusLine create(DataSource dataSource) {
        try {
            LineInputStream stream = new LineInputStream(
                    dataSource.getInputStream());
            try {
                String line = stream.readLine();
                StringReader lineReader = new StringReader(line);

                expect(lineReader, "HTTP/1.1");
                expect(lineReader, " ");
                String statusString = extractInput(lineReader, ' ');
                String reason = extractInput(lineReader, DELIMITER);

                return new StatusLine().setStatus(
                        Integer.parseInt(statusString)).setReason(reason);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void expect(Reader reader, String string) {
        try {
            byte[] byteArray = string.getBytes("UTF-8");
            int ch;
            for (int i = 0; i < string.length(); i++) {
                ch = reader.read();
                if (ch != byteArray[i]) {
                    throw new RuntimeException(String.format(
                            "Expected '%s', found '%s' instead", string,
                            string.substring(0, i) + (char) ch));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractInput(Reader reader, int delimiter) {
        try {
            StringBuilder sb = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == DELIMITER || ch == delimiter) {
                    break;
                }

                sb.append((char) ch);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
