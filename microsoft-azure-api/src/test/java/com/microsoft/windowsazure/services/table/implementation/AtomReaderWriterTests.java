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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.services.blob.implementation.ISO8601DateConverter;
import com.microsoft.windowsazure.services.core.utils.DefaultDateFactory;
import com.microsoft.windowsazure.services.table.IntegrationTestBase;
import com.microsoft.windowsazure.services.table.models.TableEntry;

public class AtomReaderWriterTests extends IntegrationTestBase {
    @Test
    public void parseTableEntriesWorks() throws Exception {
        // Arrange
        AtomReaderWriter atom = new AtomReaderWriter(new DefaultXMLStreamFactory(), new DefaultDateFactory(),
                new ISO8601DateConverter(), new DefaultEdmValueConverter(new ISO8601DateConverter()));
        String feed = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n"
                + "<feed xml:base=\"http://rpaquaytest.table.core.windows.net/\" xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\">\r\n"
                + "  <title type=\"text\">Tables</title>\r\n"
                + "  <id>http://rpaquaytest.table.core.windows.net/Tables</id>\r\n"
                + "  <updated>2012-01-10T21:23:30Z</updated>\r\n"
                + "  <link rel=\"self\" title=\"Tables\" href=\"Tables\" />\r\n"
                + "  <entry>\r\n"
                + "    <id>http://rpaquaytest.table.core.windows.net/Tables('sdktest1')</id>\r\n"
                + "    <title type=\"text\"></title>\r\n"
                + "    <updated>2012-01-10T21:23:30Z</updated>\r\n"
                + "    <author>\r\n"
                + "      <name />\r\n"
                + "    </author>\r\n"
                + "    <link rel=\"edit\" title=\"Tables\" href=\"Tables('sdktest1')\" />\r\n"
                + "    <category term=\"rpaquaytest.Tables\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\" />\r\n"
                + "    <content type=\"application/xml\">\r\n"
                + "      <m:properties>\r\n"
                + "        <d:TableName>sdktest1</d:TableName>\r\n"
                + "      </m:properties>\r\n"
                + "    </content>\r\n"
                + "  </entry>\r\n"
                + "  <entry>\r\n"
                + "    <id>http://rpaquaytest.table.core.windows.net/Tables('sdktest10')</id>\r\n"
                + "    <title type=\"text\"></title>\r\n"
                + "    <updated>2012-01-10T21:23:30Z</updated>\r\n"
                + "    <author>\r\n"
                + "      <name />\r\n"
                + "    </author>\r\n"
                + "    <link rel=\"edit\" title=\"Tables\" href=\"Tables('sdktest10')\" />\r\n"
                + "    <category term=\"rpaquaytest.Tables\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\" />\r\n"
                + "    <content type=\"application/xml\">\r\n" + "      <m:properties>\r\n"
                + "        <d:TableName>sdktest10</d:TableName>\r\n" + "      </m:properties>\r\n"
                + "    </content>\r\n" + "  </entry>\r\n" + "</feed>\r\n";
        InputStream stream = new ByteArrayInputStream(feed.getBytes("UTF-8"));

        // Act
        List<TableEntry> entries = atom.parseTableEntries(stream);

        // Assert
        assertNotNull(entries);
        assertEquals(2, entries.size());
        assertEquals("sdktest1", entries.get(0).getName());
        assertEquals("sdktest10", entries.get(1).getName());
    }
}
