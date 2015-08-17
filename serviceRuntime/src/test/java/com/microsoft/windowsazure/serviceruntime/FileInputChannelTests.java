/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class FileInputChannelTests {
    @Test
    public void getInputStreamOpensFile() {
        InputChannel inputChannel = new FileInputChannel();

        try {
            File tempFile = File
                    .createTempFile("getInputStreamOpensFile", null);
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            String expectedData = "test content";

            writer.write(expectedData);
            writer.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputChannel.getInputStream(tempFile.getAbsolutePath())));

            assertThat(reader.readLine(), equalTo(expectedData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
