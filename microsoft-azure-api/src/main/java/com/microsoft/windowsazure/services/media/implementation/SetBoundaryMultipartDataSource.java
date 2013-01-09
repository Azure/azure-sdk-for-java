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
