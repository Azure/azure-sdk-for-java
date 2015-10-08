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
package com.microsoft.windowsazure.core.utils;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamDataSource implements DataSource {
    private final InputStream stream;
    private final String contentType;

    public InputStreamDataSource(InputStream stream, String contentType) {
        this.stream = stream;
        this.contentType = contentType;

    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return stream;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
