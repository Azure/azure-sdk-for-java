/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.ProtectionKeyType;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class ContentKeyIntegrationTest extends IntegrationTestBase {

    private final String validButNonexistContentKeyId = "nb:kid:80dfe751-e5a1-4b29-a992-4a75276473af";
    private final String testName = testContentKeyPrefix + "CanCreate";
    private final String testContentKeyId = "nb:kid:80dfe751-e5a1-4b29-a992-4a75276473ae";
    private final ContentKeyType testContentKeyType = ContentKeyType.ConfigurationEncryption;
    private final String testEncryptedContentKey = "ThisIsEncryptedContentKey";

    private void verifyInfosEqual(String message, ContentKeyInfo expected, ContentKeyInfo actual) {
        verifyContentKeyProperties(message, expected.getId(), expected.getContentKeyType(),
                expected.getEncryptedContentKey(), expected.getName(), expected.getProtectionKeyId(),
                expected.getProtectionKeyType(), expected.getChecksum(), actual);
    }

    private void verifyContentKeyProperties(String message, String id, ContentKeyType contentKeyType,
            String encryptedContentKey, String name, String protectionKeyId, ProtectionKeyType protectionKeyType,
            String checksum, ContentKeyInfo actual) {
        assertNotNull(message, actual);
        assertEquals(message + " Id", id, actual.getId());
        assertEquals(message + " ContentKeyType", contentKeyType, actual.getContentKeyType());
        assertEquals(message + " EncryptedContentKey", encryptedContentKey, actual.getEncryptedContentKey());
        assertEquals(message + " Name", name, actual.getName());
        assertEquals(message + " ProtectionKeyId", actual.getProtectionKeyId());
        assertEquals(message + " ProtectionKeyType", actual.getProtectionKeyType());
        assertEquals(message + " Checksum", actual.getChecksum());
    }

    @Test
    public void canCreateContentKey() throws Exception {
        // Arrange

        // Act
        ContentKeyInfo contentKeyInfo = service.create(ContentKey.create(testContentKeyId, testContentKeyType,
                testEncryptedContentKey));

        // Assert
        verifyContentKeyProperties("ContentKey", testContentKeyId, testContentKeyType, testEncryptedContentKey,
                testName, null, null, "", contentKeyInfo);
    }

    @Test
    public void canGetSingleContentKeyById() throws Exception {
        // Arrange
        String expectedName = testContentKeyPrefix + "GetOne";
        ContentKeyInfo ContentKeyToGet = service.create(ContentKey.create(testContentKeyId, testContentKeyType,
                testEncryptedContentKey));

        // Act
        ContentKeyInfo retrievedContentKeyInfo = service.get(ContentKey.get(ContentKeyToGet.getId()));

        // Assert
        assertEquals(ContentKeyToGet.getId(), retrievedContentKeyInfo.getId());
        verifyContentKeyProperties("ContentKey", testContentKeyId, testContentKeyType, testEncryptedContentKey,
                testName, null, null, "", retrievedContentKeyInfo);
    }

    @Test
    public void cannotGetSingleContentKeyByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(ContentKey.get(invalidId));
    }

    @Test
    public void canRetrieveListOfContentKeys() throws Exception {
        String[] ContentKeyNames = new String[] { testContentKeyPrefix + "ListOne", testContentKeyPrefix + "ListTwo" };
        double duration = 3;

        List<ContentKeyInfo> expectedAccessPolicies = new ArrayList<ContentKeyInfo>();
        for (int i = 0; i < ContentKeyNames.length; i++) {
            ContentKeyInfo contentKey = service.create(ContentKey.create(testContentKeyId, testContentKeyType,
                    testEncryptedContentKey));
            expectedAccessPolicies.add(contentKey);
        }

        List<ContentKeyInfo> actualAccessPolicies = service.list(ContentKey.list());

        verifyListResultContains("listAccessPolicies", expectedAccessPolicies, actualAccessPolicies,
                new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected, Object actual) {
                        verifyInfosEqual(message, (ContentKeyInfo) expected, (ContentKeyInfo) actual);
                    }
                });
    }

    @Test
    public void canUseQueryParametersWhenListingContentKeys() throws Exception {
        String[] ContentKeyNames = new String[] { testContentKeyPrefix + "ListThree",
                testContentKeyPrefix + "ListFour", testContentKeyPrefix + "ListFive", testContentKeyPrefix + "ListSix",
                testContentKeyPrefix + "ListSeven" };

        List<ContentKeyInfo> expectedAccessPolicies = new ArrayList<ContentKeyInfo>();
        for (int i = 0; i < ContentKeyNames.length; i++) {
            ContentKeyInfo contentKeyInfo = service.create(ContentKey.create(testContentKeyId, testContentKeyType,
                    testEncryptedContentKey));
            expectedAccessPolicies.add(contentKeyInfo);
        }

        MultivaluedMap<String, String> options = new MultivaluedMapImpl();
        options.add("$top", "2");

        List<ContentKeyInfo> actualAccessPolicies = service.list(ContentKey.list(options));

        assertEquals(2, actualAccessPolicies.size());
    }

    @Test
    public void canDeleteContentKeyById() throws Exception {
        String ContentKeyName = testContentKeyPrefix + "ToDelete";
        double duration = 1;
        ContentKeyInfo ContentKeyToDelete = service.create(ContentKey.create(testContentKeyId, testContentKeyType,
                testEncryptedContentKey));
        List<ContentKeyInfo> listPoliciesResult = service.list(ContentKey.list());
        int ContentKeyCountBaseline = listPoliciesResult.size();

        service.delete(ContentKey.delete(ContentKeyToDelete.getId()));

        listPoliciesResult = service.list(ContentKey.list());
        assertEquals("listPoliciesResult.size", ContentKeyCountBaseline - 1, listPoliciesResult.size());

        for (ContentKeyInfo contentKey : service.list(ContentKey.list())) {
            assertFalse(ContentKeyToDelete.getId().equals(contentKey.getId()));
        }

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(ContentKey.get(ContentKeyToDelete.getId()));
    }

    @Test
    public void cannotDeleteContentKeyByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(ContentKey.delete(invalidId));
    }

    @Test
    public void canDeleteContentKeyByNonexistId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(ContentKey.delete(validButNonexistContentKeyId));
    }

}
