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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class StreamUtils {
    private StreamUtils() {
    }

    public static String toString(final InputStream inputStream)
            throws IOException {
        final BufferedInputStream bufferedStream = new BufferedInputStream(
                inputStream);
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        int result = bufferedStream.read();
        while (result >= 0) {
            final byte data = (byte) result;
            byteStream.write(data);
            result = bufferedStream.read();
        }
        return byteStream.toString();
    }
}
