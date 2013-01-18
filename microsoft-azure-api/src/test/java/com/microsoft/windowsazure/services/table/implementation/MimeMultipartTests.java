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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.MultipartDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.junit.Test;

import com.microsoft.windowsazure.services.table.IntegrationTestBase;

public class MimeMultipartTests extends IntegrationTestBase {
    @Test
    public void parseMimeWorks() throws Exception {
        //@formatter:off
        String s = "--batchresponse_dc0fea8c-ed83-4aa8-ac9b-bf56a2d46dfb \r\n"
                + "Content-Type: multipart/mixed; boundary=changesetresponse_8a28b620-b4bb-458c-a177-0959fb14c977\r\n"
                + "\r\n"
                + "--changesetresponse_8a28b620-b4bb-458c-a177-0959fb14c977\r\n"
                + "Content-Type: application/http\r\n"
                + "Content-Transfer-Encoding: binary\r\n"
                + "\r\n"
                + "HTTP/1.1 201 Created\r\n"
                + "Content-ID: 1\r\n"
                + "Content-Type: application/atom+xml;charset=utf-8\r\n"
                + "Cache-Control: no-cache\r\n"
                + "ETag: W/\"datetime'2009-04-30T20%3A44%3A09.5789464Z'\"\r\n"
                + "Location: http://myaccount.tables.core.windows.net/Blogs(PartitionKey='Channel_19',RowKey='1')\r\n"
                + "DataServiceVersion: 1.0;\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n"
                + "<entry xml:base=\"http:// myaccount.tables.core.windows.net/\" xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" m:etag=\"W/&quot;datetime'2009-04-30T20%3A44%3A09.5789464Z'&quot;\" xmlns=\"http://www.w3.org/2005/Atom\">\r\n"
                + "  <id> http://myaccount.tables.core.windows.net/Blogs(PartitionKey='Channel_19',RowKey='1')</id>\r\n"
                + "  <title type=\"text\"></title>\r\n"
                + "  <updated>2009-04-30T20:44:09Z</updated>\r\n"
                + "  <author>\r\n"
                + "    <name />\r\n"
                + "  </author>\r\n"
                + "  <link rel=\"edit\" title=\"Blogs\" href=\" Blogs(PartitionKey='Channel_19',RowKey='1')\" />\r\n"
                + "  <category term=\"myaccount.Blogs\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\" />\r\n"
                + "  <content type=\"application/xml\">\r\n"
                + "    <m:properties>\r\n"
                + "      <d:PartitionKey>Channel_19</d:PartitionKey>\r\n"
                + "       <d:RowKey>1</d:RowKey>\r\n"
                + "       <d:Timestamp m:type=\"Edm.DateTime\">2009-04-30T20:44:09.5789464Z</d:Timestamp>\r\n"
                + "       <d:Text>.Net...</d:RowKey>\r\n"
                + "      <d:Rating m:type=\"Edm.Int32\">9</d:Rating>\r\n"
                + "    </m:properties>\r\n"
                + "  </content>\r\n"
                + "</entry>\r\n"
                + "\r\n"
                + "--changesetresponse_8a28b620-b4bb-458c-a177-0959fb14c977\r\n"
                + "Content-Type: application/http\r\n"
                + "Content-Transfer-Encoding: binary\r\n"
                + "\r\n"
                + "HTTP/1.1 201 Created\r\n"
                + "Content-ID: 2\r\n"
                + "Content-Type: application/atom+xml;charset=utf-8\r\n"
                + "Cache-Control: no-cache\r\n"
                + "ETag: W/\"datetime'2009-04-30T20%3A44%3A09.5789464Z'\"\r\n"
                + "Location: http://myaccount.tables.core.windows.net/Blogs(PartitionKey='Channel_19',RowKey='2')\r\n"
                + "DataServiceVersion: 1.0;\r\n"
                + "\r\n"
                + "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n"
                + "<entry xml:base=\"http:// myaccount.tables.core.windows.net/\" xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" m:etag=\"W/&quot;datetime'2009-04-30T20%3A44%3A09.5789464Z'&quot;\" xmlns=\"http://www.w3.org/2005/Atom\">\r\n"
                + "  <id> http://myaccount.tables.core.windows.net/Blogs(PartitionKey='Channel_19',RowKey='2')</id>\r\n"
                + "  <title type=\"text\"></title>\r\n"
                + "  <updated>2009-04-30T20:44:09Z</updated>\r\n"
                + "  <author>\r\n"
                + "    <name />\r\n"
                + "  </author>\r\n"
                + "  <link rel=\"edit\" title=\"Blogs\" href=\" Blogs(PartitionKey='Channel_19',RowKey='2')\" />\r\n"
                + "  <category term=\"myaccount.Blogs\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\" />\r\n"
                + "  <content type=\"application/xml\">\r\n"
                + "    <m:properties>\r\n"
                + "      <d:PartitionKey>Channel_19</d:PartitionKey>\r\n"
                + "       <d:RowKey>2</d:RowKey>\r\n"
                + "       <d:Timestamp m:type=\"Edm.DateTime\">2009-04-30T20:44:09.5789464Z</d:Timestamp>\r\n"
                + "       <d:Text>Azure...</d:RowKey>\r\n"
                + "      <d:Rating m:type=\"Edm.Int32\">9</d:Rating>\r\n"
                + "    </m:properties>\r\n"
                + "  </content>\r\n"
                + "</entry>\r\n"
                + "\r\n"
                + "--changesetresponse_8a28b620-b4bb-458c-a177-0959fb14c977\r\n"
                + "Content-Type: application/http\r\n"
                + "Content-Transfer-Encoding: binary\r\n"
                + "\r\n"
                + "HTTP/1.1 204 No Content\r\n"
                + "Content-ID: 3\r\n"
                + "Cache-Control: no-cache\r\n"
                + "ETag: W/\"datetime'2009-04-30T20%3A44%3A10.0019041Z'\"\r\n"
                + "DataServiceVersion: 1.0;\r\n"
                + "\r\n"
                + "--changesetresponse_8a28b620-b4bb-458c-a177-0959fb14c977\r\n"
                + "Content-Type: application/http\r\n"
                + "Content-Transfer-Encoding: binary\r\n"
                + "\r\n"
                + "HTTP/1.1 204 No Content\r\n"
                + "Content-ID: 4\r\n"
                + "Cache-Control: no-cache\r\n"
                + "DataServiceVersion: 1.0;\r\n"
                + "\r\n"
                + "--changesetresponse_8a28b620-b4bb-458c-a177-0959fb14c977--\r\n"
                + "--batchresponse_4c637ba4-b2f8-40f8-8856-c2d10d163a83--\r\n";
        //@formatter:on

        DataSource ds = new ByteArrayDataSource(s,
                "multipart/mixed; boundary=batchresponse_dc0fea8c-ed83-4aa8-ac9b-bf56a2d46dfb");
        MimeMultipart m = new MimeMultipart(ds);

        assertEquals(1, m.getCount());
        assertTrue(m.getBodyPart(0) instanceof MimeBodyPart);

        MimeBodyPart part = (MimeBodyPart) m.getBodyPart(0);
        String contentType = part.getHeader("Content-Type", ":");
        assertEquals("multipart/mixed; boundary=changesetresponse_8a28b620-b4bb-458c-a177-0959fb14c977", contentType);

        DataSource ds2 = new ByteArrayDataSource(part.getInputStream(), contentType);
        MimeMultipart m2 = new MimeMultipart(ds2);

        assertEquals(4, m2.getCount());
    }

    @Test
    public void buildMimeWorks() throws Exception {
        //@formatter:off
        String changeset1 = "POST http://myaccount.tables.core.windows.net/Blogs HTTP/1.1\r\n" + 
        		"Content-ID: 1\r\n" + 
        		"Content-Type: application/atom+xml;type=entry\r\n" + 
        		"Content-Length: ###\r\n" + 
        		"\r\n" + 
        		"<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n" + 
        		"<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\">\r\n" + 
        		"  <title />\r\n" + 
        		"  <updated>2009-04-30T20:45:13.7155321Z</updated>\r\n" + 
        		"  <author>\r\n" + 
        		"    <name />\r\n" + 
        		"  </author>\r\n" + 
        		"  <id />\r\n" + 
        		"  <content type=\"application/xml\">\r\n" + 
        		"    <m:properties>\r\n" + 
        		"      <d:PartitionKey>Channel_19</d:PartitionKey>\r\n" + 
        		"      <d:RowKey>1</d:RowKey>\r\n" + 
        		"      <d:Timestamp m:type=\"Edm.DateTime\">0001-01-01T00:00:00</d:Timestamp>\r\n" + 
        		"      <d:Rating m:type=\"Edm.Int32\">9</d:Rating>\r\n" + 
        		"      <d:Text>.NET...</d:Title>\r\n" + 
        		"    </m:properties>\r\n" + 
        		"  </content>\r\n" + 
        		"</entry>";
        //@formatter:on

        //
        // Build inner list of change sets
        //

        MimeMultipart changeSets = new MimeMultipart(new SetBoundaryMultipartDataSource(
                "changeset_8a28b620-b4bb-458c-a177-0959fb14c977"));

        MimeBodyPart cs1 = new MimeBodyPart();
        cs1.setContent(changeset1, "application/http");
        changeSets.addBodyPart(cs1);

        MimeBodyPart cs2 = new MimeBodyPart();
        cs2.setContent(changeset1, "application/http");
        changeSets.addBodyPart(cs2);

        //
        // Build outer "batch" body part
        //
        MimeBodyPart batchbody = new MimeBodyPart();
        batchbody.setContent(changeSets);

        //
        // Build outer "batch" multipart
        //
        MimeMultipart batch = new MimeMultipart(new SetBoundaryMultipartDataSource(
                "batch_a1e9d677-b28b-435e-a89e-87e6a768a431"));
        batch.addBodyPart(batchbody);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        batch.writeTo(stream);

        String result = stream.toString("UTF-8");
        //@formatter:off
        String expectedResult =
                "--batch_a1e9d677-b28b-435e-a89e-87e6a768a431\r\n" + 
        		"\r\n" + 
        		"--changeset_8a28b620-b4bb-458c-a177-0959fb14c977\r\n" + 
        		"\r\n" + 
        		"POST http://myaccount.tables.core.windows.net/Blogs HTTP/1.1\r\n" + 
        		"Content-ID: 1\r\n" + 
        		"Content-Type: application/atom+xml;type=entry\r\n" + 
        		"Content-Length: ###\r\n" + 
        		"\r\n" + 
        		"<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n" + 
        		"<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\">\r\n" + 
        		"  <title />\r\n" + 
        		"  <updated>2009-04-30T20:45:13.7155321Z</updated>\r\n" + 
        		"  <author>\r\n" + 
        		"    <name />\r\n" + 
        		"  </author>\r\n" + 
        		"  <id />\r\n" + 
        		"  <content type=\"application/xml\">\r\n" + 
        		"    <m:properties>\r\n" + 
        		"      <d:PartitionKey>Channel_19</d:PartitionKey>\r\n" + 
        		"      <d:RowKey>1</d:RowKey>\r\n" + 
        		"      <d:Timestamp m:type=\"Edm.DateTime\">0001-01-01T00:00:00</d:Timestamp>\r\n" + 
        		"      <d:Rating m:type=\"Edm.Int32\">9</d:Rating>\r\n" + 
        		"      <d:Text>.NET...</d:Title>\r\n" + 
        		"    </m:properties>\r\n" + 
        		"  </content>\r\n" + 
        		"</entry>\r\n" + 
        		"--changeset_8a28b620-b4bb-458c-a177-0959fb14c977\r\n" + 
        		"\r\n" + 
        		"POST http://myaccount.tables.core.windows.net/Blogs HTTP/1.1\r\n" + 
        		"Content-ID: 1\r\n" + 
        		"Content-Type: application/atom+xml;type=entry\r\n" + 
        		"Content-Length: ###\r\n" + 
        		"\r\n" + 
        		"<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\r\n" + 
        		"<entry xmlns:d=\"http://schemas.microsoft.com/ado/2007/08/dataservices\" xmlns:m=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\" xmlns=\"http://www.w3.org/2005/Atom\">\r\n" + 
        		"  <title />\r\n" + 
        		"  <updated>2009-04-30T20:45:13.7155321Z</updated>\r\n" + 
        		"  <author>\r\n" + 
        		"    <name />\r\n" + 
        		"  </author>\r\n" + 
        		"  <id />\r\n" + 
        		"  <content type=\"application/xml\">\r\n" + 
        		"    <m:properties>\r\n" + 
        		"      <d:PartitionKey>Channel_19</d:PartitionKey>\r\n" + 
        		"      <d:RowKey>1</d:RowKey>\r\n" + 
        		"      <d:Timestamp m:type=\"Edm.DateTime\">0001-01-01T00:00:00</d:Timestamp>\r\n" + 
        		"      <d:Rating m:type=\"Edm.Int32\">9</d:Rating>\r\n" + 
        		"      <d:Text>.NET...</d:Title>\r\n" + 
        		"    </m:properties>\r\n" + 
        		"  </content>\r\n" + 
        		"</entry>\r\n" + 
        		"--changeset_8a28b620-b4bb-458c-a177-0959fb14c977--\r\n" + 
        		"\r\n" + 
        		"--batch_a1e9d677-b28b-435e-a89e-87e6a768a431--\r\n";
        //@formatter:on
        StringReader reader1 = new StringReader(result);
        StringReader reader2 = new StringReader(expectedResult);

        for (int i = 0;; i++) {
            int ch1 = reader1.read();
            int ch2 = reader2.read();
            if (ch1 == -1) {
                assertEquals(-1, ch2);
                break;
            }
            if (ch2 == -1) {
                assertEquals(-1, ch1);
                break;
            }

            if (ch1 != ch2) {
                int min1 = Math.max(0, i - 20);
                int max1 = Math.min(result.length(), i + 20);

                int min2 = Math.max(0, i - 20);
                int max2 = Math.min(expectedResult.length(), i + 20);

                String closeBy1 = result.substring(min1, max1);
                String closeBy2 = expectedResult.substring(min2, max2);

                assertEquals("Message content are no equal starting at position " + i, closeBy2, closeBy1);
            }
        }
    }

    private class SetBoundaryMultipartDataSource implements MultipartDataSource {

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
}
