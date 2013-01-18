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
package com.microsoft.windowsazure.services.queue.client;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.windowsazure.services.core.storage.StorageErrorCodeStrings;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * Table Client Tests
 */
public class CloudQueueTests extends QueueTestBase {
    @Test
    public void queueGetSetPermissionTest() throws StorageException, URISyntaxException {
        String name = generateRandomQueueName();
        CloudQueue newQueue = qClient.getQueueReference(name);
        newQueue.create();

        QueuePermissions expectedPermissions;
        QueuePermissions testPermissions;

        try {
            // Test new permissions.
            expectedPermissions = new QueuePermissions();
            testPermissions = newQueue.downloadPermissions();
            assertQueuePermissionsEqual(expectedPermissions, testPermissions);

            // Test setting empty permissions.
            newQueue.uploadPermissions(expectedPermissions);
            testPermissions = newQueue.downloadPermissions();
            assertQueuePermissionsEqual(expectedPermissions, testPermissions);

            // Add a policy, check setting and getting.
            SharedAccessQueuePolicy policy1 = new SharedAccessQueuePolicy();
            Calendar now = GregorianCalendar.getInstance();
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 10);
            policy1.setSharedAccessExpiryTime(now.getTime());

            policy1.setPermissions(EnumSet.of(SharedAccessQueuePermissions.READ,
                    SharedAccessQueuePermissions.PROCESSMESSAGES, SharedAccessQueuePermissions.ADD,
                    SharedAccessQueuePermissions.UPDATE));
            expectedPermissions.getSharedAccessPolicies().put(UUID.randomUUID().toString(), policy1);

            newQueue.uploadPermissions(expectedPermissions);
            testPermissions = newQueue.downloadPermissions();
            assertQueuePermissionsEqual(expectedPermissions, testPermissions);
        }
        finally {
            // cleanup
            newQueue.deleteIfExists();
        }
    }

    @Test
    public void testQueueSAS() throws StorageException, URISyntaxException, InvalidKeyException {
        String name = generateRandomQueueName();
        CloudQueue newQueue = qClient.getQueueReference(name);
        newQueue.create();
        newQueue.addMessage(new CloudQueueMessage("sas queue test"));

        QueuePermissions expectedPermissions;

        try {
            expectedPermissions = new QueuePermissions();
            // Add a policy, check setting and getting.
            SharedAccessQueuePolicy policy1 = new SharedAccessQueuePolicy();
            Calendar now = GregorianCalendar.getInstance();
            now.add(Calendar.MINUTE, -15);
            policy1.setSharedAccessStartTime(now.getTime());
            now.add(Calendar.MINUTE, 30);
            policy1.setSharedAccessExpiryTime(now.getTime());
            String identifier = UUID.randomUUID().toString();

            policy1.setPermissions(EnumSet.of(SharedAccessQueuePermissions.READ,
                    SharedAccessQueuePermissions.PROCESSMESSAGES, SharedAccessQueuePermissions.ADD,
                    SharedAccessQueuePermissions.UPDATE));
            expectedPermissions.getSharedAccessPolicies().put(identifier, policy1);

            newQueue.uploadPermissions(expectedPermissions);

            CloudQueueClient queueClientFromIdentifierSAS = getQueueClientForSas(newQueue, null, identifier);
            CloudQueue identifierSasQueue = queueClientFromIdentifierSAS.getQueueReference(newQueue.getName());

            identifierSasQueue.downloadAttributes();
            identifierSasQueue.exists();

            identifierSasQueue.addMessage(new CloudQueueMessage("message"), 20, 0, null, null);
            CloudQueueMessage message1 = identifierSasQueue.retrieveMessage();
            identifierSasQueue.deleteMessage(message1);

            CloudQueueClient queueClientFromPolicySAS = getQueueClientForSas(newQueue, policy1, null);
            CloudQueue policySasQueue = queueClientFromPolicySAS.getQueueReference(newQueue.getName());
            policySasQueue.exists();
            policySasQueue.downloadAttributes();

            policySasQueue.addMessage(new CloudQueueMessage("message"), 20, 0, null, null);
            CloudQueueMessage message2 = policySasQueue.retrieveMessage();
            policySasQueue.deleteMessage(message2);
        }
        finally {
            // cleanup
            newQueue.deleteIfExists();
        }
    }

    private CloudQueueClient getQueueClientForSas(CloudQueue queue, SharedAccessQueuePolicy policy,
            String accessIdentifier) throws InvalidKeyException, StorageException {
        String sasString = queue.generateSharedAccessSignature(policy, accessIdentifier);
        return new CloudQueueClient(qClient.getEndpoint(), new StorageCredentialsSharedAccessSignature(sasString));
    }

    static void assertQueuePermissionsEqual(QueuePermissions expected, QueuePermissions actual) {
        HashMap<String, SharedAccessQueuePolicy> expectedPolicies = expected.getSharedAccessPolicies();
        HashMap<String, SharedAccessQueuePolicy> actualPolicies = actual.getSharedAccessPolicies();
        Assert.assertEquals("SharedAccessPolicies.Count", expectedPolicies.size(), actualPolicies.size());
        for (String name : expectedPolicies.keySet()) {
            Assert.assertTrue("Key" + name + " doesn't exist", actualPolicies.containsKey(name));
            SharedAccessQueuePolicy expectedPolicy = expectedPolicies.get(name);
            SharedAccessQueuePolicy actualPolicy = actualPolicies.get(name);
            Assert.assertEquals("Policy: " + name + "\tPermissions\n", expectedPolicy.getPermissions().toString(),
                    actualPolicy.getPermissions().toString());
            Assert.assertEquals("Policy: " + name + "\tStartDate\n", expectedPolicy.getSharedAccessStartTime()
                    .toString(), actualPolicy.getSharedAccessStartTime().toString());
            Assert.assertEquals("Policy: " + name + "\tExpireDate\n", expectedPolicy.getSharedAccessExpiryTime()
                    .toString(), actualPolicy.getSharedAccessExpiryTime().toString());

        }

    }

    @Test
    public void testQueueClientConstructor() throws URISyntaxException, StorageException {
        String queueName = "queue";
        String baseAddress = AppendQueueName(qClient.getEndpoint(), queueName);
        CloudQueue queue1 = new CloudQueue(baseAddress, qClient);
        Assert.assertEquals(queueName, queue1.getName());
        Assert.assertTrue(queue1.getUri().toString().endsWith(baseAddress));
        Assert.assertEquals(qClient, queue1.getServiceClient());

        CloudQueue queue2 = new CloudQueue(new URI(AppendQueueName(qClient.getEndpoint(), queueName)), qClient);

        Assert.assertEquals(queueName, queue2.getName());
        Assert.assertEquals(qClient, queue2.getServiceClient());

        CloudQueue queue3 = new CloudQueue(queueName, qClient);
        Assert.assertEquals(queueName, queue3.getName());
        Assert.assertEquals(qClient, queue3.getServiceClient());
    }

    @Test
    public void testGetMetadata() throws URISyntaxException, StorageException {
        HashMap<String, String> metadata = new HashMap<String, String>();
        metadata.put("ExistingMetadata", "ExistingMetadataValue");
        queue.setMetadata(metadata);
        queue.uploadMetadata();
        queue.downloadAttributes();
        Assert.assertEquals(queue.getMetadata().get("ExistingMetadata"), "ExistingMetadataValue");
        Assert.assertTrue(queue.getMetadata().containsKey("ExistingMetadata"));

        HashMap<String, String> empytMetadata = null;
        queue.setMetadata(empytMetadata);
        queue.uploadMetadata();
        queue.downloadAttributes();
        Assert.assertTrue(queue.getMetadata().size() == 0);
    }

    @Test
    public void testUploadMetadata() throws URISyntaxException, StorageException {
        CloudQueue queueForGet = new CloudQueue(queue.getUri(), queue.getServiceClient());

        HashMap<String, String> metadata1 = new HashMap<String, String>();
        metadata1.put("ExistingMetadata1", "ExistingMetadataValue1");
        queue.setMetadata(metadata1);

        queueForGet.downloadAttributes();
        Assert.assertFalse(queueForGet.getMetadata().containsKey("ExistingMetadata1"));

        queue.uploadMetadata();
        queueForGet.downloadAttributes();
        Assert.assertTrue(queueForGet.getMetadata().containsKey("ExistingMetadata1"));
    }

    @Test
    public void testUploadMetadataNullInput() throws URISyntaxException, StorageException {
        CloudQueue queueForGet = new CloudQueue(queue.getUri(), queue.getServiceClient());

        HashMap<String, String> metadata1 = new HashMap<String, String>();
        String key = "ExistingMetadata1" + UUID.randomUUID().toString().replace("-", "");
        metadata1.put(key, "ExistingMetadataValue1");
        queue.setMetadata(metadata1);

        queueForGet.downloadAttributes();
        Assert.assertFalse(queueForGet.getMetadata().containsKey(key));

        queue.uploadMetadata();
        queueForGet.downloadAttributes();
        Assert.assertTrue(queueForGet.getMetadata().containsKey(key));

        queue.setMetadata(null);
        queue.uploadMetadata();
        queueForGet.downloadAttributes();
        Assert.assertTrue(queueForGet.getMetadata().size() == 0);
    }

    @Test
    public void testUploadMetadataClearExisting() throws URISyntaxException, StorageException {
        CloudQueue queueForGet = new CloudQueue(queue.getUri(), queue.getServiceClient());

        HashMap<String, String> metadata1 = new HashMap<String, String>();
        String key = "ExistingMetadata1" + UUID.randomUUID().toString().replace("-", "");
        metadata1.put(key, "ExistingMetadataValue1");
        queue.setMetadata(metadata1);

        queueForGet.downloadAttributes();
        Assert.assertFalse(queueForGet.getMetadata().containsKey(key));

        HashMap<String, String> metadata2 = new HashMap<String, String>();
        queue.setMetadata(metadata2);
        queue.uploadMetadata();
        queueForGet.downloadAttributes();
        Assert.assertTrue(queueForGet.getMetadata().size() == 0);
    }

    @Test
    public void testUploadMetadataNotFound() throws URISyntaxException, StorageException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        try {
            newQueue.uploadMetadata();
            Assert.fail();
        }
        catch (StorageException e) {
            Assert.assertTrue(e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);

        }
    }

    @Test
    public void testQueueCreate() throws URISyntaxException, StorageException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        OperationContext createQueueContext = new OperationContext();
        queue.create(null, createQueueContext);
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        try {
            HashMap<String, String> metadata1 = new HashMap<String, String>();
            metadata1.put("ExistingMetadata1", "ExistingMetadataValue1");
            queue.setMetadata(metadata1);
            queue.create();
            Assert.fail();
        }
        catch (StorageException e) {
            Assert.assertTrue(e.getHttpStatusCode() == HttpURLConnection.HTTP_CONFLICT);

        }

        queue.downloadAttributes();
        OperationContext createQueueContext2 = new OperationContext();
        queue.create(null, createQueueContext2);
        Assert.assertEquals(createQueueContext2.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        queue.delete();
    }

    @Test
    public void testQueueCreateAlreadyExists() throws URISyntaxException, StorageException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        OperationContext createQueueContext1 = new OperationContext();
        queue.create(null, createQueueContext1);
        Assert.assertEquals(createQueueContext1.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        OperationContext createQueueContext2 = new OperationContext();
        queue.create(null, createQueueContext2);
        Assert.assertEquals(createQueueContext2.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void testQueueCreateAfterDelete() throws URISyntaxException, StorageException {

        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        OperationContext createQueueContext1 = new OperationContext();
        Assert.assertTrue(queue.createIfNotExist(null, createQueueContext1));
        Assert.assertEquals(createQueueContext1.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        Assert.assertTrue(queue.deleteIfExists());
        try {
            queue.create();
            Assert.fail("Queue CreateIfNotExists did not throw exception while trying to create a queue in BeingDeleted State");
        }
        catch (StorageException ex) {
            Assert.assertEquals("Expected 409 Exception, QueueBeingDeleted not thrown", ex.getHttpStatusCode(),
                    HttpURLConnection.HTTP_CONFLICT);
            Assert.assertEquals("Expected 409 Exception, QueueBeingDeleted not thrown", ex
                    .getExtendedErrorInformation().getErrorCode(), StorageErrorCodeStrings.QUEUE_BEING_DELETED);
        }
    }

    @Test
    public void testQueueCreateIfNotExists() throws URISyntaxException, StorageException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        OperationContext createQueueContext = new OperationContext();
        Assert.assertTrue(queue.createIfNotExist(null, createQueueContext));
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);
    }

    @Test
    public void testQueueCreateIfNotExistsAfterCreate() throws URISyntaxException, StorageException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        OperationContext createQueueContext1 = new OperationContext();
        Assert.assertTrue(queue.createIfNotExist(null, createQueueContext1));
        Assert.assertEquals(createQueueContext1.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        OperationContext createQueueContext2 = new OperationContext();
        Assert.assertFalse(queue.createIfNotExist(null, createQueueContext2));
        Assert.assertEquals(createQueueContext2.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    public void testQueueCreateIfNotExistsAfterDelete() throws URISyntaxException, StorageException {

        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        OperationContext createQueueContext1 = new OperationContext();
        Assert.assertTrue(queue.createIfNotExist(null, createQueueContext1));
        Assert.assertEquals(createQueueContext1.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        Assert.assertTrue(queue.deleteIfExists());
        try {
            queue.createIfNotExist();
            Assert.fail("Queue CreateIfNotExists did not throw exception while trying to create a queue in BeingDeleted State");
        }
        catch (StorageException ex) {
            Assert.assertEquals("Expected 409 Exception, QueueBeingDeleted not thrown", ex.getHttpStatusCode(),
                    HttpURLConnection.HTTP_CONFLICT);
            Assert.assertEquals("Expected 409 Exception, QueueBeingDeleted not thrown", ex
                    .getExtendedErrorInformation().getErrorCode(), StorageErrorCodeStrings.QUEUE_BEING_DELETED);
        }
    }

    @Test
    public void testQueueDelete() throws URISyntaxException, StorageException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        OperationContext createQueueContext = new OperationContext();
        queue.create(null, createQueueContext);
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        OperationContext deleteQueueContext = new OperationContext();
        queue.delete(null, deleteQueueContext);
        Assert.assertEquals(deleteQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        try {
            queue.downloadAttributes();
            Assert.fail();
        }
        catch (StorageException ex) {
            Assert.assertEquals("Expected 404 Exception", ex.getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
        }

        queue.delete();
    }

    @Test
    public void testDeleteQueueIfExists() throws URISyntaxException, StorageException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);

        Assert.assertFalse(queue.deleteIfExists());

        final OperationContext createQueueContext = new OperationContext();
        queue.create(null, createQueueContext);
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        Assert.assertTrue(queue.deleteIfExists());

        try {
            queue.create();
            Assert.fail("Queue CreateIfNotExists did not throw exception while trying to create a queue in BeingDeleted State");
        }
        catch (StorageException ex) {
            Assert.assertEquals("Expected 409 Exception, QueueBeingDeleted not thrown", ex.getHttpStatusCode(),
                    HttpURLConnection.HTTP_CONFLICT);
            Assert.assertEquals("Expected 409 Exception, QueueBeingDeleted not thrown", ex
                    .getExtendedErrorInformation().getErrorCode(), StorageErrorCodeStrings.QUEUE_BEING_DELETED);
        }
    }

    @Test
    public void testDeleteNonExistingQueue() throws URISyntaxException, StorageException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);

        final OperationContext existQueueContext1 = new OperationContext();
        Assert.assertTrue(!queue.exists(null, existQueueContext1));
        Assert.assertEquals(existQueueContext1.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);

        try {
            queue.delete();
            Assert.fail("Queue delete no exsiting queue. ");
        }
        catch (StorageException ex) {
            Assert.assertEquals("Expected 404 Exception", ex.getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Test
    public void testQueueExist() throws URISyntaxException, StorageException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);

        final OperationContext existQueueContext1 = new OperationContext();
        Assert.assertTrue(!queue.exists(null, existQueueContext1));
        Assert.assertEquals(existQueueContext1.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);

        final OperationContext createQueueContext = new OperationContext();
        queue.create(null, createQueueContext);
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        final OperationContext existQueueContext2 = new OperationContext();
        Assert.assertTrue(queue.exists(null, existQueueContext2));
        Assert.assertEquals(existQueueContext2.getLastResult().getStatusCode(), HttpURLConnection.HTTP_OK);
    }

    @Test
    public void testClearMessages() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        final CloudQueue queue = qClient.getQueueReference(UUID.randomUUID().toString().toLowerCase());
        queue.create();

        CloudQueueMessage message1 = new CloudQueueMessage("messagetest1");
        queue.addMessage(message1);

        CloudQueueMessage message2 = new CloudQueueMessage("messagetest2");
        queue.addMessage(message2);

        int count = 0;
        for (CloudQueueMessage m : queue.peekMessages(32)) {
            count++;
        }

        Assert.assertTrue(count == 2);

        OperationContext oc = new OperationContext();
        queue.clear(null, oc);
        Assert.assertEquals(oc.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);

        count = 0;
        for (CloudQueueMessage m : queue.peekMessages(32)) {
            count++;
        }

        Assert.assertTrue(count == 0);
    }

    public void testClearMessagesEmptyQueue() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        final CloudQueue queue = qClient.getQueueReference(UUID.randomUUID().toString().toLowerCase());
        queue.create();
        queue.clear();
        queue.delete();
    }

    public void testClearMessagesNotFound() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        final CloudQueue queue = qClient.getQueueReference(UUID.randomUUID().toString().toLowerCase());
        try {
            queue.clear();
            Assert.fail();
        }
        catch (StorageException ex) {
            Assert.assertEquals("Expected 404 Exception", ex.getHttpStatusCode(), HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    @Test
    public void testAddMessage() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.create();

        String msgContent = UUID.randomUUID().toString();
        final CloudQueueMessage message = new CloudQueueMessage(msgContent);
        queue.addMessage(message);
        CloudQueueMessage msgFromRetrieve1 = queue.retrieveMessage();
        Assert.assertEquals(message.getMessageContentAsString(), msgContent);
        Assert.assertEquals(msgFromRetrieve1.getMessageContentAsString(), msgContent);

        queue.delete();
    }

    @Test
    public void testAddMessageUnicode() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.create();

        ArrayList<String> messages = new ArrayList<String>();
        messages.add("Le débat sur l'identité nationale, l'idée du président Nicolas Sarkozy de déchoir des personnes d'origine étrangère de la nationalité française ... certains cas et les récentes mesures prises contre les Roms ont choqué les experts, qui rendront leurs conclusions le 27 août.");
        messages.add("Ваш логин Yahoo! дает доступ к таким мощным инструментам связи, как электронная почта, отправка мгновенных сообщений, функции безопасности, в частности, антивирусные средства и блокировщик всплывающей рекламы, и избранное, например, фото и музыка в сети — все бесплат");
        messages.add("据新华社8月12日电 8月11日晚，舟曲境内再次出现强降雨天气，使特大山洪泥石流灾情雪上加霜。白龙江水在梨坝子村的交汇地带形成一个新的堰塞湖，水位比平时高出3米。甘肃省国土资源厅副厅长张国华当日22时许在新闻发布会上介绍，截至12日21时50分，舟曲堰塞湖堰塞体已消除，溃坝险情已消除，目前针对堰塞湖的主要工作是疏通河道。");
        messages.add("ל כולם\", הדהים יעלון, ויישר קו עם העדות שמסר ראש הממשלה, בנימין נתניהו, לוועדת טירקל. לדבריו, אכן השרים דנו רק בהיבטים התקשורתיים של עצירת המשט: \"בשביעייה לא התקיים דיון על האלטרנטיבות. עסקנו בהיבטים ");
        messages.add("Prozent auf 0,5 Prozent. Im Vergleich zum Vorjahresquartal wuchs die deutsche Wirtschaft von Januar bis März um 2,1 Prozent. Auch das ist eine Korrektur nach oben, ursprünglich waren es hier 1,7 Prozent");
        messages.add("<?xml version=\"1.0\"?>\n<!DOCTYPE PARTS SYSTEM \"parts.dtd\">\n<?xml-stylesheet type=\"text/css\" href=\"xmlpartsstyle.css\"?>\n<PARTS>\n   <TITLE>Computer Parts</TITLE>\n   <PART>\n      <ITEM>Motherboard</ITEM>\n      <MANUFACTURER>ASUS</MANUFACTURER>\n      <MODEL>"
                + "P3B-F</MODEL>\n      <COST> 123.00</COST>\n   </PART>\n   <PART>\n      <ITEM>Video Card</ITEM>\n      <MANUFACTURER>ATI</MANUFACTURER>\n      <MODEL>All-in-Wonder Pro</MODEL>\n      <COST> 160.00</COST>\n   </PART>\n   <PART>\n      <ITEM>Sound Card</ITEM>\n      <MANUFACTURER>"
                + "Creative Labs</MANUFACTURER>\n      <MODEL>Sound Blaster Live</MODEL>\n      <COST> 80.00</COST>\n   </PART>\n   <PART>\n      <ITEM> inch Monitor</ITEM>\n      <MANUFACTURER>LG Electronics</MANUFACTURER>\n      <MODEL> 995E</MODEL>\n      <COST> 290.00</COST>\n   </PART>\n</PARTS>");

        for (int i = 0; i < messages.size(); i++) {
            String msg = messages.get(i);
            queue.addMessage(new CloudQueueMessage(msg));
            CloudQueueMessage readBack = queue.retrieveMessage();
            Assert.assertEquals(msg, readBack.getMessageContentAsString());
            queue.deleteMessage(readBack);
        }

        queue.shouldEncodeMessage = false;
        for (int i = 0; i < messages.size(); i++) {
            String msg = messages.get(i);
            queue.addMessage(new CloudQueueMessage(msg));
            CloudQueueMessage readBack = queue.retrieveMessage();
            Assert.assertEquals(msg, readBack.getMessageContentAsString());
            queue.deleteMessage(readBack);
        }

        queue.delete();
    }

    @Test
    public void testAddMessageLargeVisibilityDelay() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.create();

        String msgContent = UUID.randomUUID().toString();
        final CloudQueueMessage message = new CloudQueueMessage(msgContent);
        queue.addMessage(message, 100, 50, null, null);
        CloudQueueMessage msgFromRetrieve1 = queue.retrieveMessage();
        Assert.assertNull(msgFromRetrieve1);

        queue.delete();
    }

    @Test
    public void testDeleteMessageWithDifferentQueueInstance() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue1 = qClient.getQueueReference(queueName);
        queue1.create();

        String msgContent = UUID.randomUUID().toString();
        final CloudQueueMessage message = new CloudQueueMessage(msgContent);
        queue1.addMessage(message);
        CloudQueueMessage msgFromRetrieved = queue1.retrieveMessage();

        final CloudQueue queue2 = qClient.getQueueReference(queueName);
        queue2.deleteMessage(msgFromRetrieved);

        queue1.delete();
    }

    @Test
    public void testAddMessageToNonExistingQueue() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);

        String messageContent = "messagetest";
        CloudQueueMessage message1 = new CloudQueueMessage(messageContent);

        try {
            newQueue.addMessage(message1);
            Assert.fail();
        }
        catch (StorageException e) {
            Assert.assertTrue(e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);

        }
    }

    @Test
    public void testQueueUnicodeAndXmlMessageTest() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.create();

        String msgContent = "好<?xml version= 1.0  encoding= utf-8  ?>";
        final CloudQueueMessage message = new CloudQueueMessage(msgContent);
        queue.addMessage(message);
        CloudQueueMessage msgFromRetrieve1 = queue.retrieveMessage();
        Assert.assertEquals(message.getMessageContentAsString(), msgContent);
        Assert.assertEquals(msgFromRetrieve1.getMessageContentAsString(), msgContent);
        //Assert.assertEquals(message.getMessageContentAsByte(), msgFromRetrieve1.getMessageContentAsByte());

        queue.delete();
    }

    @Test
    public void testAddMessageLargeMessageInput() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        final OperationContext createQueueContext = new OperationContext();
        queue.create(null, createQueueContext);
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        final Random rand = new Random();

        byte[] content = new byte[64 * 1024];
        rand.nextBytes(content);
        CloudQueueMessage message1 = new CloudQueueMessage(new String(content));

        try {
            queue.addMessage(message1);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        queue.delete();
    }

    @Test
    public void testAddMessageWithVisibilityTimeout() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.create();
        queue.addMessage(new CloudQueueMessage("message"), 20, 0, null, null);
        CloudQueueMessage m1 = queue.retrieveMessage();
        Date d1 = m1.getExpirationTime();
        queue.deleteMessage(m1);

        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        queue.addMessage(new CloudQueueMessage("message"), 20, 0, null, null);
        CloudQueueMessage m2 = queue.retrieveMessage();
        Date d2 = m2.getExpirationTime();
        queue.deleteMessage(m2);
        Assert.assertTrue(d1.before(d2));
    }

    @Test
    public void testAddMessageNullMessage() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        try {
            queue.addMessage(null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testAddMessageSpecialVisibilityTimeout() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        final OperationContext createQueueContext = new OperationContext();
        queue.create(null, createQueueContext);
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        CloudQueueMessage message = new CloudQueueMessage("test");
        queue.addMessage(message, 1, 0, null, null);
        queue.addMessage(message, 7 * 24 * 60 * 60, 0, null, null);
        queue.addMessage(message, 7 * 24 * 60 * 60, 7 * 24 * 60 * 60 - 1, null, null);

        try {
            queue.addMessage(message, -1, 0, null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.addMessage(message, 0, -1, null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.addMessage(message, 7 * 24 * 60 * 60, 7 * 24 * 60 * 60, null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.addMessage(message, 7 * 24 * 60 * 60 + 1, 0, null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.addMessage(message, 0, 7 * 24 * 60 * 60 + 1, null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.updateMessage(message, 0, EnumSet.of(MessageUpdateFields.CONTENT), null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        queue.delete();
    }

    @Test
    public void testDeleteMessage() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();

        CloudQueueMessage message1 = new CloudQueueMessage("messagetest1");
        newQueue.addMessage(message1);

        CloudQueueMessage message2 = new CloudQueueMessage("messagetest2");
        newQueue.addMessage(message2);

        for (CloudQueueMessage message : newQueue.retrieveMessages(32)) {
            OperationContext deleteQueueContext = new OperationContext();
            newQueue.deleteMessage(message, null, deleteQueueContext);
            Assert.assertEquals(deleteQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }

        Assert.assertTrue(newQueue.retrieveMessage() == null);
    }

    @Test
    public void testQueueCreateAddingMetaData() throws URISyntaxException, StorageException {
        final CloudQueue queue = qClient.getQueueReference(UUID.randomUUID().toString().toLowerCase());

        final HashMap<String, String> metadata = new HashMap<String, String>(5);
        for (int i = 0; i < 5; i++) {
            metadata.put("key" + i, "value" + i);
        }

        queue.setMetadata(metadata);

        final OperationContext createQueueContext = new OperationContext();
        queue.create(null, createQueueContext);
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);
    }

    @Test
    public void testDeleteMessageNullMessage() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        try {
            queue.deleteMessage(null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testRetrieveMessage() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();
        newQueue.addMessage(new CloudQueueMessage("message"), 20, 0, null, null);
        OperationContext opContext = new OperationContext();
        CloudQueueMessage message1 = newQueue.retrieveMessage(10, null /*QueueRequestOptions*/, opContext);
        Date expirationTime1 = message1.getExpirationTime();
        Date insertionTime1 = message1.getInsertionTime();
        Date nextVisibleTime1 = message1.getNextVisibleTime();

        Assert.assertEquals(HttpURLConnection.HTTP_OK, opContext.getLastResult().getStatusCode());

        newQueue.deleteMessage(message1);

        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        newQueue.addMessage(new CloudQueueMessage("message"), 20, 0, null, null);
        CloudQueueMessage message2 = newQueue.retrieveMessage();
        Date expirationTime2 = message2.getExpirationTime();
        Date insertionTime2 = message2.getInsertionTime();
        Date nextVisibleTime2 = message2.getNextVisibleTime();
        newQueue.deleteMessage(message2);
        Assert.assertTrue(expirationTime1.before(expirationTime2));
        Assert.assertTrue(insertionTime1.before(insertionTime2));
        Assert.assertTrue(nextVisibleTime1.before(nextVisibleTime2));
    }

    @Test
    public void testRetrieveMessageNonExistingQueue() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        try {
            newQueue.retrieveMessage();
            Assert.fail();
        }
        catch (StorageException e) {
            Assert.assertTrue(e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);

        }
    }

    @Test
    public void testRetrieveMessageInvalidInput() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);

        try {
            queue.retrieveMessage(-1, null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.retrieveMessage(7 * 24 * 3600 + 1, null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testRetrieveMessagesFromEmptyQueue() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();

        for (CloudQueueMessage m : newQueue.retrieveMessages(32)) {
            Assert.assertTrue(m.getId() != null);
            Assert.assertTrue(m.getPopReceipt() == null);
        }
    }

    @Test
    public void testRetrieveMessagesNonFound() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        try {
            newQueue.retrieveMessages(1);
            Assert.fail();
        }
        catch (StorageException e) {
            Assert.assertTrue(e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);

        }
    }

    @Test
    public void testDequeueCountIncreases() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();
        newQueue.addMessage(new CloudQueueMessage("message"), 20, 0, null, null);
        CloudQueueMessage message1 = newQueue.retrieveMessage(1, null, null);
        Assert.assertTrue(message1.getDequeueCount() == 1);

        for (int i = 2; i < 5; i++) {
            try {
                Thread.sleep(2000);
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            CloudQueueMessage message2 = newQueue.retrieveMessage(1, null, null);
            Assert.assertTrue(message2.getDequeueCount() == i);
        }

    }

    @Test
    public void testRetrieveMessageSpecialVisibilityTimeout() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {

        try {
            queue.retrieveMessage(-1, null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testRetrieveMessages() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();

        CloudQueueMessage message1 = new CloudQueueMessage("messagetest1");
        newQueue.addMessage(message1);

        CloudQueueMessage message2 = new CloudQueueMessage("messagetest2");
        newQueue.addMessage(message2);

        for (CloudQueueMessage m : newQueue.retrieveMessages(32)) {
            Assert.assertTrue(m.getId() != null);
            Assert.assertTrue(m.getPopReceipt() != null);
        }
    }

    @Test
    public void testRetrieveMessagesInvalidInput() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.createIfNotExist();

        for (int i = 0; i < 33; i++) {
            queue.addMessage(new CloudQueueMessage("test" + i));
        }

        queue.retrieveMessages(1, 1, null, null);
        queue.retrieveMessages(32, 1, null, null);

        try {
            queue.retrieveMessages(-1);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.retrieveMessages(0);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.retrieveMessages(33);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        queue.delete();
    }

    @Test
    public void testPeekMessage() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();

        CloudQueueMessage message1 = new CloudQueueMessage("messagetest1");
        newQueue.addMessage(message1);

        CloudQueueMessage msg = newQueue.peekMessage();
        Assert.assertTrue(msg.getId() != null);
        Assert.assertTrue(msg.getPopReceipt() == null);

        newQueue.delete();
    }

    @Test
    public void testPeekMessages() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();

        CloudQueueMessage message1 = new CloudQueueMessage("messagetest1");
        newQueue.addMessage(message1);

        CloudQueueMessage message2 = new CloudQueueMessage("messagetest2");
        newQueue.addMessage(message2);

        for (CloudQueueMessage m : newQueue.peekMessages(32)) {
            Assert.assertTrue(m.getId() != null);
            Assert.assertTrue(m.getPopReceipt() == null);
        }

        newQueue.delete();
    }

    @Test
    public void testPeekMessagesInvalidInput() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.createIfNotExist();

        for (int i = 0; i < 33; i++) {
            queue.addMessage(new CloudQueueMessage("test" + i));
        }

        queue.peekMessages(1);
        queue.peekMessages(32);

        try {
            queue.peekMessages(-1);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.peekMessages(0);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        try {
            queue.peekMessages(33);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        queue.delete();
    }

    @Test
    public void testPeekMessageNonExistingQueue() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        try {
            newQueue.peekMessage();
            Assert.fail();
        }
        catch (StorageException e) {
            Assert.assertTrue(e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);

        }
    }

    @Test
    public void testPeekMessagesNonFound() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        try {
            newQueue.peekMessages(1);
            Assert.fail();
        }
        catch (StorageException e) {
            Assert.assertTrue(e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);

        }
    }

    @Test
    public void testPeekMessagesFromEmptyQueue() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();

        for (CloudQueueMessage m : newQueue.peekMessages(32)) {
            Assert.assertTrue(m.getId() != null);
            Assert.assertTrue(m.getPopReceipt() == null);
        }
    }

    @Test
    public void testUpdateMessage() throws URISyntaxException, StorageException, UnsupportedEncodingException {

        String messageContent = "messagetest";
        CloudQueueMessage message1 = new CloudQueueMessage(messageContent);
        queue.addMessage(message1);

        CloudQueueMessage message2 = new CloudQueueMessage(messageContent);
        queue.addMessage(message2);

        String newMesage = message1.getMessageContentAsString() + "updated";

        for (CloudQueueMessage message : queue.retrieveMessages(32)) {
            OperationContext oc = new OperationContext();
            message.setMessageContent(newMesage);
            queue.updateMessage(message, 0, EnumSet.of(MessageUpdateFields.VISIBILITY), null, oc);
            Assert.assertEquals(oc.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
            CloudQueueMessage messageFromGet = queue.retrieveMessage();
            Assert.assertEquals(messageFromGet.getMessageContentAsString(), messageContent);
        }
    }

    @Test
    public void testUpdateMessageFullPass() throws URISyntaxException, StorageException, UnsupportedEncodingException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        newQueue.create();
        CloudQueueMessage message = new CloudQueueMessage("message");
        newQueue.addMessage(message, 20, 0, null, null);
        CloudQueueMessage message1 = newQueue.retrieveMessage();
        String popreceipt1 = message1.getPopReceipt();
        Date NextVisibleTim1 = message1.getNextVisibleTime();
        newQueue.updateMessage(message1, 100, EnumSet.of(MessageUpdateFields.VISIBILITY), null, null);
        String popreceipt2 = message1.getPopReceipt();
        Date NextVisibleTim2 = message1.getNextVisibleTime();
        Assert.assertTrue(popreceipt2 != popreceipt1);
        Assert.assertTrue(NextVisibleTim1.before(NextVisibleTim2));

        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String newMesage = message.getMessageContentAsString() + "updated";
        message.setMessageContent(newMesage);
        OperationContext oc = new OperationContext();
        newQueue.updateMessage(message1, 100, EnumSet.of(MessageUpdateFields.CONTENT), null, oc);
        Assert.assertEquals(oc.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        String popreceipt3 = message1.getPopReceipt();
        Date NextVisibleTim3 = message1.getNextVisibleTime();
        Assert.assertTrue(popreceipt3 != popreceipt2);
        Assert.assertTrue(NextVisibleTim2.before(NextVisibleTim3));

        Assert.assertTrue(newQueue.retrieveMessage() == null);

        newQueue.updateMessage(message1, 0, EnumSet.of(MessageUpdateFields.VISIBILITY), null, null);

        CloudQueueMessage messageFromGet = newQueue.retrieveMessage();
        Assert.assertEquals(messageFromGet.getMessageContentAsString(), message1.getMessageContentAsString());
    }

    @Test
    public void testUpdateMessageWithContentChange() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {

        CloudQueueMessage message1 = new CloudQueueMessage("messagetest1");
        queue.addMessage(message1);

        CloudQueueMessage message2 = new CloudQueueMessage("messagetest2");
        queue.addMessage(message2);

        for (CloudQueueMessage message : queue.retrieveMessages(32)) {
            OperationContext oc = new OperationContext();
            message.setMessageContent(message.getMessageContentAsString() + "updated");
            queue.updateMessage(message, 100, EnumSet.of(MessageUpdateFields.CONTENT), null, oc);
            Assert.assertEquals(oc.getLastResult().getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }
    }

    @Test
    public void testUpdateMessageNullMessage() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        try {
            queue.updateMessage(null, 0);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void testUpdateMessageInvalidMessage() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.create(null, null);

        CloudQueueMessage message = new CloudQueueMessage("test");
        queue.addMessage(message, 1, 0, null, null);

        try {
            queue.updateMessage(message, 0, EnumSet.of(MessageUpdateFields.CONTENT), null, null);
            Assert.fail();
        }
        catch (final IllegalArgumentException e) {
        }

        queue.delete();
    }

    @Test
    public void testGetApproximateMessageCount() throws URISyntaxException, StorageException,
            UnsupportedEncodingException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.create();
        Assert.assertTrue(queue.getApproximateMessageCount() == 0);
        queue.addMessage(new CloudQueueMessage("message1"));
        queue.addMessage(new CloudQueueMessage("message2"));
        Assert.assertTrue(queue.getApproximateMessageCount() == 0);
        queue.downloadAttributes();
        Assert.assertTrue(queue.getApproximateMessageCount() == 2);
        queue.delete();
    }

    @Test
    public void testShouldEncodeMessage() throws URISyntaxException, StorageException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        queue.create();

        String msgContent = UUID.randomUUID().toString();
        final CloudQueueMessage message = new CloudQueueMessage(msgContent);
        queue.setShouldEncodeMessage(true);
        queue.addMessage(message);
        CloudQueueMessage msgFromRetrieve1 = queue.retrieveMessage();
        Assert.assertEquals(msgFromRetrieve1.getMessageContentAsString(), msgContent);
        queue.deleteMessage(msgFromRetrieve1);

        queue.setShouldEncodeMessage(false);
        queue.addMessage(message);
        CloudQueueMessage msgFromRetrieve2 = queue.retrieveMessage();
        Assert.assertEquals(msgFromRetrieve2.getMessageContentAsString(), msgContent);
        queue.deleteMessage(msgFromRetrieve2);

        queue.setShouldEncodeMessage(true);
    }

    @Test
    public void testQueueDownloadAttributes() throws URISyntaxException, StorageException,
            UnsupportedEncodingException, XMLStreamException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();

        final CloudQueue queue1 = qClient.getQueueReference(queueName);
        queue1.create();

        final CloudQueueMessage message1 = new CloudQueueMessage("messagetest1");
        queue1.addMessage(message1);

        final CloudQueueMessage message2 = new CloudQueueMessage("messagetest2");
        queue1.addMessage(message2);

        final HashMap<String, String> metadata = new HashMap<String, String>(5);
        int sum = 5;
        for (int i = 0; i < sum; i++) {
            metadata.put("key" + i, "value" + i);
        }

        queue1.setMetadata(metadata);
        queue1.uploadMetadata();

        final CloudQueue queue2 = qClient.getQueueReference(queueName);
        queue2.downloadAttributes();

        System.out.println(queue2.getApproximateMessageCount());

        int count = 0;
        for (final String s : queue2.getMetadata().keySet()) {
            count++;
            System.out.println(s + ":" + queue2.getMetadata().get(s));
        }

        Assert.assertEquals(count, sum);

        queue1.delete();
    }

    @Test
    public void testQueueDownloadAttributesNotFound() throws URISyntaxException, StorageException {
        String queueName = UUID.randomUUID().toString().toLowerCase();
        CloudQueue newQueue = qClient.getQueueReference(queueName);
        try {
            newQueue.downloadAttributes();
            Assert.fail();
        }
        catch (StorageException e) {
            Assert.assertTrue(e.getHttpStatusCode() == HttpURLConnection.HTTP_NOT_FOUND);

        }
    }

    @Test
    public void testQueueUpdateMetaData() throws URISyntaxException, StorageException {
        final String queueName = UUID.randomUUID().toString().toLowerCase();
        final CloudQueue queue = qClient.getQueueReference(queueName);
        Assert.assertEquals(queueName, queue.getName());

        final OperationContext createQueueContext = new OperationContext();
        queue.create(null, createQueueContext);
        Assert.assertEquals(createQueueContext.getLastResult().getStatusCode(), HttpURLConnection.HTTP_CREATED);

        final HashMap<String, String> metadata = new HashMap<String, String>(5);
        for (int i = 0; i < 5; i++) {
            metadata.put("key" + i, "value" + i);
        }

        queue.setMetadata(metadata);
        queue.uploadMetadata();
    }

    @Test
    public void testSASClientParse() throws StorageException, URISyntaxException, InvalidKeyException {

        // Add a policy, check setting and getting.
        SharedAccessQueuePolicy policy1 = new SharedAccessQueuePolicy();
        Calendar now = GregorianCalendar.getInstance();
        now.add(Calendar.MINUTE, -15);
        policy1.setSharedAccessStartTime(now.getTime());
        now.add(Calendar.MINUTE, 30);
        policy1.setSharedAccessExpiryTime(now.getTime());

        policy1.setPermissions(EnumSet.of(SharedAccessQueuePermissions.READ,
                SharedAccessQueuePermissions.PROCESSMESSAGES, SharedAccessQueuePermissions.ADD,
                SharedAccessQueuePermissions.UPDATE));

        String sasString = queue.generateSharedAccessSignature(policy1, null);

        URI queueUri = new URI("http://myaccount.queue.core.windows.net/myqueue");

        CloudQueueClient queueClient1 = new CloudQueueClient(new URI("http://myaccount.queue.core.windows.net/"),
                new StorageCredentialsSharedAccessSignature(sasString));

        CloudQueue queue1 = new CloudQueue(queueUri, queueClient1);
        queue1.getName();

        CloudQueueClient queueClient2 = new CloudQueueClient(new URI("http://myaccount.queue.core.windows.net/"),
                new StorageCredentialsSharedAccessSignature(sasString));
        CloudQueue queue2 = new CloudQueue(queueUri, queueClient2);
        queue2.getName();
    }
}
