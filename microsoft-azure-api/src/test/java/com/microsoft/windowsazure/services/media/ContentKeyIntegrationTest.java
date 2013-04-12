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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyType;
import com.microsoft.windowsazure.services.media.models.ProtectionKey;
import com.microsoft.windowsazure.services.media.models.ProtectionKeyType;

public class ContentKeyIntegrationTest extends IntegrationTestBase {

    private final String validButNonexistContentKeyId = "nb:kid:UUID:80dfe751-e5a1-4b29-a992-4a75276473af";
    private final ContentKeyType testContentKeyType = ContentKeyType.CommonEncryption;
    private final String testEncryptedContentKey = "ThisIsEncryptedContentKey";

    private void assertByteArrayEquals(byte[] source, byte[] target) {
        assertEquals(source.length, target.length);
        for (int i = 0; i < source.length; i++) {
            assertEquals(source[i], target[i]);
        }
    }

    private ContentKeyInfo createTestContentKey(String contentKeyNameSuffix) throws ServiceException {
        String testContentKeyId = createRandomContentKeyId();
        String testContentKeyName = testContentKeyPrefix + contentKeyNameSuffix;

        ContentKeyInfo contentKeyInfo = service.create(ContentKey.create(testContentKeyId, testContentKeyType,
                testEncryptedContentKey).setName(testContentKeyName));
        return contentKeyInfo;
    }

    private ContentKeyInfo createValidTestContentKeyWithAesKey(String contentKeyNameSuffix, byte[] aesKey)
            throws Exception {
        String testContnetKeyName = testContentKeyPrefix + contentKeyNameSuffix;
        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(ContentKeyType.StorageEncryption));
        String protectionKey = service.action(ProtectionKey.getProtectionKey(protectionKeyId));

        String testContentKeyIdUuid = UUID.randomUUID().toString();
        String testContentKeyId = String.format("nb:kid:UUID:%s", testContentKeyIdUuid);

        byte[] encryptedContentKey = EncryptionHelper.encryptSymmetricKey(protectionKey, aesKey);
        String encryptedContentKeyString = Base64.encode(encryptedContentKey);
        String checksum = EncryptionHelper.calculateContentKeyChecksum(testContentKeyIdUuid, aesKey);

        ContentKeyInfo contentKeyInfo = service.create(ContentKey
                .create(testContentKeyId, ContentKeyType.StorageEncryption, encryptedContentKeyString)
                .setChecksum(checksum).setProtectionKeyId(protectionKeyId).setName(testContnetKeyName));

        return contentKeyInfo;
    }

    private ContentKeyInfo createValidTestContentKey(String contentKeyNameSuffix) throws Exception {
        byte[] aesKey = createTestAesKey();
        return createValidTestContentKeyWithAesKey(contentKeyNameSuffix, aesKey);
    }

    private byte[] createTestAesKey() {
        byte[] aesKey = new byte[32];
        int i;
        for (i = 0; i < 32; i++) {
            aesKey[i] = 1;
        }

        return aesKey;
    }

    private String createRandomContentKeyId() {
        UUID uuid = UUID.randomUUID();
        String randomContentKey = String.format("nb:kid:UUID:%s", uuid);
        return randomContentKey;
    }

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
        assertEquals(message + " ProtectionKeyId", protectionKeyId, actual.getProtectionKeyId());
        assertEquals(message + " ProtectionKeyType", protectionKeyType, actual.getProtectionKeyType());
        assertEquals(message + " Checksum", checksum, actual.getChecksum());
    }

    @BeforeClass
    public static void Setup() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Test
    public void canCreateContentKey() throws Exception {
        // Arrange
        String testCanCreateContentKeyId = createRandomContentKeyId();
        String testCanCreateContentKeyName = testContentKeyPrefix + "testCanCreateContentKey";
        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(testContentKeyType));

        // Act
        ContentKeyInfo contentKeyInfo = service.create(ContentKey
                .create(testCanCreateContentKeyId, testContentKeyType, testEncryptedContentKey)
                .setName(testCanCreateContentKeyName).setProtectionKeyId(protectionKeyId));

        // Assert
        verifyContentKeyProperties("ContentKey", testCanCreateContentKeyId, testContentKeyType,
                testEncryptedContentKey, testCanCreateContentKeyName, protectionKeyId, ProtectionKeyType.fromCode(0),
                "", contentKeyInfo);
    }

    @Test
    public void canGetSingleContentKeyById() throws Exception {
        // Arrange
        String expectedName = testContentKeyPrefix + "GetOne";
        String testGetSingleContentKeyByIdId = createRandomContentKeyId();
        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(testContentKeyType));
        ContentKeyInfo ContentKeyToGet = service.create(ContentKey
                .create(testGetSingleContentKeyByIdId, testContentKeyType, testEncryptedContentKey)
                .setName(expectedName).setProtectionKeyId(protectionKeyId));

        // Act
        ContentKeyInfo retrievedContentKeyInfo = service.get(ContentKey.get(ContentKeyToGet.getId()));

        // Assert
        assertEquals(ContentKeyToGet.getId(), retrievedContentKeyInfo.getId());
        verifyContentKeyProperties("ContentKey", testGetSingleContentKeyByIdId, testContentKeyType,
                testEncryptedContentKey, expectedName, protectionKeyId, ProtectionKeyType.fromCode(0), "",
                retrievedContentKeyInfo);
    }

    @Test
    public void cannotGetSingleContentKeyByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(ContentKey.get(invalidId));
    }

    @Test
    public void canRetrieveListOfContentKeys() throws Exception {
        // Arrange
        String[] ContentKeyNames = new String[] { testContentKeyPrefix + "ListOne", testContentKeyPrefix + "ListTwo" };
        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(testContentKeyType));

        List<ContentKeyInfo> expectedContentKeys = new ArrayList<ContentKeyInfo>();
        for (int i = 0; i < ContentKeyNames.length; i++) {
            String testCanRetrieveListOfContentKeysId = createRandomContentKeyId();
            ContentKeyInfo contentKey = service.create(ContentKey.create(testCanRetrieveListOfContentKeysId,
                    testContentKeyType, testEncryptedContentKey).setProtectionKeyId(protectionKeyId));
            expectedContentKeys.add(contentKey);
        }

        // Act
        List<ContentKeyInfo> actualContentKeys = service.list(ContentKey.list());

        // Assert
        verifyListResultContains("listContentKeyss", expectedContentKeys, actualContentKeys, new ComponentDelegate() {
            @Override
            public void verifyEquals(String message, Object expected, Object actual) {
                verifyInfosEqual(message, (ContentKeyInfo) expected, (ContentKeyInfo) actual);
            }
        });
    }

    @Test
    public void canUseQueryParametersWhenListingContentKeys() throws Exception {
        // Arrange
        String[] ContentKeyNames = new String[] { testContentKeyPrefix + "ListThree",
                testContentKeyPrefix + "ListFour", testContentKeyPrefix + "ListFive", testContentKeyPrefix + "ListSix",
                testContentKeyPrefix + "ListSeven" };
        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(testContentKeyType));

        List<ContentKeyInfo> expectedContentKeys = new ArrayList<ContentKeyInfo>();
        for (int i = 0; i < ContentKeyNames.length; i++) {
            ContentKeyInfo contentKeyInfo = service.create(ContentKey.create(createRandomContentKeyId(),
                    testContentKeyType, testEncryptedContentKey).setProtectionKeyId(protectionKeyId));
            expectedContentKeys.add(contentKeyInfo);
        }

        // Act
        List<ContentKeyInfo> actualContentKeys = service.list(ContentKey.list().setTop(2));

        // Assert
        assertEquals(2, actualContentKeys.size());
    }

    @Test
    public void canDeleteContentKeyById() throws Exception {
        // Arrange
        String protectionKeyId = service.action(ProtectionKey.getProtectionKeyId(testContentKeyType));
        String contentKeyName = testContentKeyPrefix + "ToDelete";
        ContentKeyInfo contentKeyToDelete = service.create(ContentKey
                .create(createRandomContentKeyId(), testContentKeyType, testEncryptedContentKey)
                .setName(contentKeyName).setProtectionKeyId(protectionKeyId));
        List<ContentKeyInfo> listContentKeysResult = service.list(ContentKey.list());
        int ContentKeyCountBaseline = listContentKeysResult.size();

        // Act
        service.delete(ContentKey.delete(contentKeyToDelete.getId()));

        // Assert
        listContentKeysResult = service.list(ContentKey.list());
        assertEquals("listPoliciesResult.size", ContentKeyCountBaseline - 1, listContentKeysResult.size());

        for (ContentKeyInfo contentKey : service.list(ContentKey.list())) {
            assertFalse(contentKeyToDelete.getId().equals(contentKey.getId()));
        }

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(ContentKey.get(contentKeyToDelete.getId()));
    }

    @Test
    public void cannotDeleteContentKeyByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(ContentKey.delete(invalidId));
    }

    @Test
    public void cannotDeleteContentKeyByNonexistId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.delete(ContentKey.delete(validButNonexistContentKeyId));
    }

    @Test
    public void rebindContentKeyNoX509CertificateSuccess() throws Exception {

        ContentKeyInfo contentKeyInfo = createValidTestContentKey("rebindContentKeyNoX509Success");

        String contentKey = service.action(ContentKey.rebind(contentKeyInfo.getId()));
        assertNotNull(contentKey);

    }

    @Test
    public void rebindInvalidContentKeyNoX509CertificateFail() throws ServiceException {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        ContentKeyInfo contentKeyInfo = createTestContentKey("rebindInvalidContentKeyNoX509Fail");

        service.action(ContentKey.rebind(contentKeyInfo.getId()));

    }

    @Test
    public void rebindContentKeyWithX509CertficateSuccess() throws Exception {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        byte[] aesKey = createTestAesKey();
        ContentKeyInfo contentKeyInfo = createValidTestContentKeyWithAesKey("rebindContentKeyWithX509Success", aesKey);
        URL serverCertificateUri = getClass().getResource("/certificate/server.crt");
        X509Certificate x509Certificate = EncryptionHelper.loadX509Certificate(URLDecoder.decode(
                serverCertificateUri.getFile(), "UTF-8"));
        URL serverPrivateKey = getClass().getResource("/certificate/server.der");
        PrivateKey privateKey = EncryptionHelper.getPrivateKey(URLDecoder.decode(serverPrivateKey.getFile(), "UTF-8"));

        String rebindedContentKey = service.action(ContentKey.rebind(contentKeyInfo.getId(),
                URLEncoder.encode(Base64.encode(x509Certificate.getEncoded()), "UTF-8")));
        byte[] decryptedAesKey = EncryptionHelper.decryptSymmetricKey(rebindedContentKey, privateKey);
        assertByteArrayEquals(aesKey, decryptedAesKey);
    }

    @Test
    public void rebindContentKeyWithIncorrectContentKeyIdFailed() throws ServiceException {
        expectedException.expect(ServiceException.class);
        service.action(ContentKey.rebind("invalidContentKeyId"));
    }

    @Test
    public void rebindContentKeyWithIncorrectX509CertificateFailed() throws ServiceException {
        expectedException.expect(ServiceException.class);
        ContentKeyInfo contentKeyInfo = createTestContentKey("rebindContentKeyWithIncorrectX509CertficateFailed");

        service.action(ContentKey.rebind(contentKeyInfo.getId(), "InvalidX509Certificate"));
    }
}
