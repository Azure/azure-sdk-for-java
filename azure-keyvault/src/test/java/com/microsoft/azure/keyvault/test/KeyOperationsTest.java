/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.test;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.models.KeyItem;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.KeyVaultErrorException;
import com.microsoft.azure.keyvault.models.KeyVerifyResult;
import com.microsoft.azure.keyvault.requests.CreateKeyRequest;
import com.microsoft.azure.keyvault.requests.ImportKeyRequest;
import com.microsoft.azure.keyvault.requests.UpdateKeyRequest;
import com.microsoft.azure.keyvault.models.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyEncryptionAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;
import com.microsoft.azure.keyvault.webkey.JsonWebKeySignatureAlgorithm;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;

public class KeyOperationsTest extends KeyVaultClientIntegrationTestBase {

    private static final String KEY_NAME = "javaKey";
    private static final int MAX_KEYS = 4;
    private static final int PAGELIST_MAX_KEYS = 3;

    @Test
    public void transparentAuthentication() throws Exception {

        // Create a key on a vault.
        {
            KeyBundle bundle = keyVaultClient.createKey(new CreateKeyRequest.Builder(getVaultUri(), KEY_NAME, "RSA").build()).getBody();
            validateRsaKeyBundle(bundle, getVaultUri(), KEY_NAME, "RSA", null);
        }

        // Create a key on a different vault. Key Vault Data Plane returns 401,
        // which must be transparently handled by KeyVaultCredentials.
        {
            KeyBundle bundle = keyVaultClient.createKey(new CreateKeyRequest.Builder(getSecondaryVaultUri(), KEY_NAME, "RSA").build()).getBody();
            validateRsaKeyBundle(bundle, getSecondaryVaultUri(), KEY_NAME, "RSA", null);
        }

    }

    @Test
    public void importKeyOperation() throws Exception {

        KeyBundle keyBundle = new KeyBundle();
        JsonWebKey key = JsonWebKey.fromRSA(getTestKeyMaterial());

        key.withKeyOps(Arrays.asList(JsonWebKeyOperation.ENCRYPT, JsonWebKeyOperation.DECRYPT));

        keyBundle.withKey(key);

        checkImportOperation(keyBundle, false);
        checkImportOperation(keyBundle, true);
    }

    private void checkImportOperation(KeyBundle keyBundle, boolean importToHardware) throws Exception {
        JsonWebKey importedJwk = keyBundle.key();
        KeyBundle importResultBundle = keyVaultClient.importKey(
        		new ImportKeyRequest
        			.Builder(getVaultUri(), KEY_NAME, keyBundle.key())
	        			.withHsm(importToHardware)
	        			.build()).getBody();
        validateRsaKeyBundle(importResultBundle, getVaultUri(), KEY_NAME, importToHardware ? "RSA-HSM" : "RSA", importedJwk.keyOps());
        checkEncryptDecryptSequence(importedJwk, importResultBundle);
    }

    private void checkEncryptDecryptSequence(JsonWebKey importedKey, KeyBundle importedKeyBundle) throws Exception {

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        byte[] cipherText;

        // Encrypt in the service.
        {
            KeyOperationResult result = keyVaultClient.encrypt(importedKeyBundle.key().kid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText).getBody();
            cipherText = result.result();
        }

        // Decrypt in the client, notice OAEP algorithm instance to use.
        {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, importedKey.toRSA(true).getPrivate());

            byte[] beforeEncrypt = plainText;
            byte[] afterDecrypt = cipher.doFinal(cipherText);
            Assert.assertArrayEquals(beforeEncrypt, afterDecrypt);
        }

        // Encrypt in the client, using the service provided material. Also use
        // standard padding.
        {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, importedKeyBundle.key().toRSA().getPublic());

            cipherText = cipher.doFinal(plainText);
        }

        // Decrypt in the service.
        {
            KeyOperationResult result = keyVaultClient.decrypt(importedKeyBundle.key().kid(), JsonWebKeyEncryptionAlgorithm.RSA15, cipherText).getBody();

            byte[] beforeEncrypt = plainText;
            byte[] afterDecrypt = result.result();
            Assert.assertArrayEquals(beforeEncrypt, afterDecrypt);
        }
    }

    @Test
    public void crudOperations() throws Exception {

        KeyBundle createdBundle;
        {
            // Create key
            createdBundle = keyVaultClient.createKey(new CreateKeyRequest.Builder(getVaultUri(), KEY_NAME, "RSA").build()).getBody();
            validateRsaKeyBundle(createdBundle, getVaultUri(), KEY_NAME, "RSA", null);
        }

        // Key identifier.
        KeyIdentifier keyId = new KeyIdentifier(createdBundle.key().kid());

        {
            // Get key using kid WO version
            KeyBundle readBundle = keyVaultClient.getKey(keyId.baseIdentifier()).getBody();
            compareKeyBundles(createdBundle, readBundle);
        }

        {
            // Get key using full kid as defined in the bundle
            KeyBundle readBundle = keyVaultClient.getKey(createdBundle.key().kid()).getBody();
            compareKeyBundles(createdBundle, readBundle);
        }

        {
            // Get key using vault and key name.
            KeyBundle readBundle = keyVaultClient.getKey(getVaultUri(), KEY_NAME).getBody();
            compareKeyBundles(createdBundle, readBundle);
        }

        {
            // Get key using vault, key name and version.
            KeyBundle readBundle = keyVaultClient.getKey(getVaultUri(), KEY_NAME, keyId.version()).getBody();
            compareKeyBundles(createdBundle, readBundle);
        }

        {
            // Get key using vault, key name and a null version.
            KeyBundle readBundle = keyVaultClient.getKey(getVaultUri(), KEY_NAME).getBody();
            compareKeyBundles(createdBundle, readBundle);
        }

        {
            // Update key using the kid as defined in the bundle

            // First we create a bundle with the modified attributes.
            createdBundle.attributes().withExpires(new DateTime()
														.withMonthOfYear(2)
														.withDayOfMonth(1)
														.withYear(2050));
            List<String> key_ops = Arrays.asList("encrypt", "decrypt");
            Map<String, String> tags = new HashMap<String, String>();
            tags.put("foo", "baz");
            createdBundle.key().withKeyOps(key_ops);
            createdBundle.withTags(tags);

            // Perform the operation.
            KeyBundle updatedBundle = keyVaultClient.updateKey(
            			new UpdateKeyRequest
            				.Builder(createdBundle.key().kid())
            				.withKeyOperations(key_ops)
            				.withAttributes(createdBundle.attributes())
            				.withTags(createdBundle.tags())
            				.build()).getBody();

            compareKeyBundles(createdBundle, updatedBundle);

            // Subsequent operations must use the updated bundle for comparison.
            createdBundle = updatedBundle;
        }

        {
            // Update key using vault and key name.

            // First we create a bundle with the modified attributes.
        	createdBundle.attributes().withNotBefore(new DateTime()
															.withMonthOfYear(2)
															.withDayOfMonth(1)
															.withYear(2000));
            List<String> key_ops = Arrays.asList("sign", "verify");
            createdBundle.key().withKeyOps(key_ops);
            Map<String, String> tags = new HashMap<String, String>();
            tags.put("foo", "baz");
            createdBundle.withTags(tags);

			// Perform the operation.
            KeyBundle updatedBundle = keyVaultClient.updateKey(
	        			new UpdateKeyRequest
		    				.Builder(getVaultUri(), KEY_NAME)
		    				.withKeyOperations(key_ops)
		    				.withAttributes(createdBundle.attributes())
		    				.withTags(createdBundle.tags())
		    				.build()).getBody();
            
            compareKeyBundles(createdBundle, updatedBundle);
        }

        {
            // Delete key
            KeyBundle deleteBundle = keyVaultClient.deleteKey(getVaultUri(), KEY_NAME).getBody();
            compareKeyBundles(createdBundle, deleteBundle);
        }

        {
            // Expects a key not found
            try {
            	keyVaultClient.getKey(keyId.baseIdentifier());
            } catch (KeyVaultErrorException e) {
                Assert.assertNotNull(e.getBody().error());
                Assert.assertEquals("KeyNotFound", e.getBody().error().code());
            }
        }

    }

    @Test
    public void backupRestore() throws Exception {

        KeyBundle createdBundle;

        // Creates a key
        {
            createdBundle = keyVaultClient.createKey(
            		new CreateKeyRequest.Builder(getVaultUri(), KEY_NAME, "RSA")
            							.build()).getBody();
            validateRsaKeyBundle(createdBundle, getVaultUri(), KEY_NAME, "RSA", null);
        }

        // Creates a backup of key.
        byte[] keyBackup;
        {
            keyBackup = keyVaultClient.backupKey(getVaultUri(), KEY_NAME).getBody().value();
        }

        // Deletes the key.
        {
            keyVaultClient.deleteKey(getVaultUri(), KEY_NAME);
        }

        // Restores the key.
        {
            KeyBundle restoredBundle = keyVaultClient.restoreKey(getVaultUri(), keyBackup).getBody();
            compareKeyBundles(createdBundle, restoredBundle);
        }

    }

    @Test
    public void listKeys() throws Exception {

        HashSet<String> keys = new HashSet<String>();
        for (int i = 0; i < MAX_KEYS; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    KeyBundle createdBundle = keyVaultClient.createKey(new CreateKeyRequest.Builder(getVaultUri(), KEY_NAME + i, "RSA").build()).getBody();
                    KeyIdentifier kid = new KeyIdentifier(createdBundle.key().kid());
                    keys.add(kid.baseIdentifier());
                    break;
                } catch (KeyVaultErrorException e) {
                    ++failureCount;
                    if (e.getBody().error().code().equals("Throttled")) {
                        System.out.println("Waiting to avoid throttling");
                        Thread.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
            }
        }

        PagedList<KeyItem> listResult = keyVaultClient.getKeys(getVaultUri(), PAGELIST_MAX_KEYS).getBody();
        Assert.assertTrue(PAGELIST_MAX_KEYS >= listResult.currentPage().getItems().size());

        HashSet<String> toDelete = new HashSet<String>();

        for (KeyItem item : listResult) {
            KeyIdentifier id = new KeyIdentifier(item.kid());
            toDelete.add(id.name());
            keys.remove(item.kid());
        }

        Assert.assertEquals(0, keys.size());

        for (String name : toDelete) {
        	try{
        		keyVaultClient.deleteKey(getVaultUri(), name);
        	}
        	catch(KeyVaultErrorException e){
        		// Ignore forbidden exception for certificate keys that cannot be deleted
        		if(!e.getBody().error().code().equals("Forbidden"))
        			throw e;
        	}
        }
    }

    @Test
    public void listKeyVersions() throws Exception {

        HashSet<String> keys = new HashSet<String>();
        for (int i = 0; i < MAX_KEYS; ++i) {
            int failureCount = 0;
            for (;;) {
                try {
                    KeyBundle createdBundle = keyVaultClient.createKey(new CreateKeyRequest.Builder(getVaultUri(), KEY_NAME, "RSA").build()).getBody();
                    keys.add(createdBundle.key().kid());
                    break;
                } catch (KeyVaultErrorException e) {
                    ++failureCount;
                    if (e.getBody().error().code().equals("Throttled")) {
                        System.out.println("Waiting to avoid throttling");
                        Thread.sleep(failureCount * 1500);
                        continue;
                    }
                    throw e;
                }
            }
        }

        PagedList<KeyItem> listResult = keyVaultClient.getKeyVersions(getVaultUri(), KEY_NAME, MAX_KEYS).getBody();
        //TODO bug: Assert.assertTrue(PAGELIST_MAX_KEYS >= listResult.currentPage().getItems().size());

        listResult = keyVaultClient.getKeyVersions(getVaultUri(), KEY_NAME).getBody();
        
        for (;;) {
        	for (KeyItem item : listResult) {
                keys.remove(item.kid());
            }
            String nextLink = listResult.nextPageLink();
            if (nextLink == null) {
                break;
            }
            keyVaultClient.getKeyVersionsNext(nextLink).getBody();
        }

        Assert.assertEquals(0, keys.size());

        keyVaultClient.deleteKey(getVaultUri(), KEY_NAME);
    }

    @Test
    public void encryptDecryptOperations() throws Exception {

        JsonWebKey testKey = importTestKey();
        KeyIdentifier keyId = new KeyIdentifier(testKey.kid());

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        byte[] cipherText;

        KeyOperationResult result;

        // encrypt and decrypt using kid WO version
        {
        	result = keyVaultClient.encrypt(keyId.baseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText).getBody();
            cipherText = result.result();

            result = keyVaultClient.decrypt(keyId.baseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText).getBody();
            Assert.assertArrayEquals(plainText, result.result());
        }

        // encrypt and decrypt using full kid
        {
        	result = keyVaultClient.encrypt(testKey.kid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText).getBody();
            cipherText = result.result();

            result = keyVaultClient.decrypt(testKey.kid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText).getBody();
            Assert.assertArrayEquals(plainText, result.result());
        }
    }

    @Test
    public void wrapUnwrapOperations() throws Exception {

        JsonWebKey testKey = importTestKey();
        KeyIdentifier keyId = new KeyIdentifier(testKey.kid());

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        byte[] cipherText;

        KeyOperationResult result;

        // wrap and unwrap using kid WO version
        {
        	result = keyVaultClient.wrapKey(keyId.baseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText).getBody();
            cipherText = result.result();

            result = keyVaultClient.unwrapKey(keyId.baseIdentifier(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText).getBody();
            Assert.assertArrayEquals(plainText, result.result());
        }

        // wrap and unwrap using full kid
        {
        	result = keyVaultClient.wrapKey(testKey.kid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, plainText).getBody();
            cipherText = result.result();

            result = keyVaultClient.unwrapKey(testKey.kid(), JsonWebKeyEncryptionAlgorithm.RSAOAEP, cipherText).getBody();
            Assert.assertArrayEquals(plainText, result.result());
        }
    }

    @Test
    public void signVerifyOperations() throws Exception {

        JsonWebKey testKey = importTestKey();
        KeyIdentifier keyId = new KeyIdentifier(testKey.kid());

        // Test variables
        byte[] plainText = new byte[100];
        new Random(0x1234567L).nextBytes(plainText);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(plainText);
        byte[] digest = md.digest();
        byte[] signature;

        KeyOperationResult result;
        KeyVerifyResult verifyResult;

        // Using kid WO version
        {
        	result = keyVaultClient.sign(keyId.baseIdentifier(), JsonWebKeySignatureAlgorithm.RS256, digest).getBody();
            signature = result.result();

            verifyResult = keyVaultClient.verify(keyId.baseIdentifier(), JsonWebKeySignatureAlgorithm.RS256, digest, signature).getBody();
            Assert.assertEquals(new Boolean(true), verifyResult.value());
        }

        // Using full kid
        {
            result = keyVaultClient.sign(testKey.kid(), JsonWebKeySignatureAlgorithm.RS256, digest).getBody();
            signature = result.result();

            verifyResult = keyVaultClient.verify(testKey.kid(), JsonWebKeySignatureAlgorithm.RS256, digest, signature).getBody();
            Assert.assertEquals(new Boolean(true), verifyResult.value());

        }
    }

    private static JsonWebKey importTestKey() throws Exception {

        KeyBundle keyBundle = new KeyBundle();
        JsonWebKey key = JsonWebKey.fromRSA(getTestKeyMaterial());

        key.withKty(JsonWebKeyType.RSA);
        key.withKeyOps(Arrays.asList(JsonWebKeyOperation.ENCRYPT, JsonWebKeyOperation.DECRYPT, JsonWebKeyOperation.SIGN, JsonWebKeyOperation.VERIFY, JsonWebKeyOperation.WRAP, JsonWebKeyOperation.UNWRAP));

        keyBundle = keyVaultClient.importKey(
        		new ImportKeyRequest
        			.Builder(getVaultUri(), KEY_NAME, key)
        				.withHsm(false)
        				.build()).getBody();
        
        validateRsaKeyBundle(keyBundle, getVaultUri(), KEY_NAME, "RSA", null);

        return keyBundle.key();
    }

    private static KeyPair getTestKeyMaterial() throws Exception {
    	return getWellKnownKey();
    }

    private static KeyPair getWellKnownKey() throws Exception {
        BigInteger modulus = new BigInteger("27266783713040163753473734334021230592631652450892850648620119914958066181400432364213298181846462385257448168605902438305568194683691563208578540343969522651422088760509452879461613852042845039552547834002168737350264189810815735922734447830725099163869215360401162450008673869707774119785881115044406101346450911054819448375712432746968301739007624952483347278954755460152795801894283389540036131881712321193750961817346255102052653789197325341350920441746054233522546543768770643593655942246891652634114922277138937273034902434321431672058220631825053788262810480543541597284376261438324665363067125951152574540779");
        BigInteger publicExponent = new BigInteger("65537");
        BigInteger privateExponent = new BigInteger("10466613941269075477152428927796086150095892102279802916937552172064636326433780566497000814207416485739683286961848843255766652023400959086290344987308562817062506476465756840999981989957456897020361717197805192876094362315496459535960304928171129585813477132331538577519084006595335055487028872410579127692209642938724850603554885478763205394868103298473476811627231543504190652483290944218004086457805431824328448422034887148115990501701345535825110962804471270499590234116100216841170344686381902328362376624405803648588830575558058257742073963036264273582756620469659464278207233345784355220317478103481872995809");
        BigInteger primeP = new BigInteger("175002941104568842715096339107566771592009112128184231961529953978142750732317724951747797764638217287618769007295505214923187971350518217670604044004381362495186864051394404165602744235299100790551775147322153206730562450301874236875459336154569893255570576967036237661594595803204808064127845257496057219227");
        BigInteger primeQ = new BigInteger("155807574095269324897144428622185380283967159190626345335083690114147315509962698765044950001909553861571493035240542031420213144237033208612132704562174772894369053916729901982420535940939821673277140180113593951522522222348910536202664252481405241042414183668723338300649954708432681241621374644926879028977");
        BigInteger primeExponentP = new BigInteger("79745606804504995938838168837578376593737280079895233277372027184693457251170125851946171360348440134236338520742068873132216695552312068793428432338173016914968041076503997528137698610601222912385953171485249299873377130717231063522112968474603281996190849604705284061306758152904594168593526874435238915345");
        BigInteger primeExponentQ = new BigInteger("80619964983821018303966686284189517841976445905569830731617605558094658227540855971763115484608005874540349730961777634427740786642996065386667564038755340092176159839025706183161615488856833433976243963682074011475658804676349317075370362785860401437192843468423594688700132964854367053490737073471709030801");
        BigInteger crtCoefficient = new BigInteger("2157818511040667226980891229484210846757728661751992467240662009652654684725325675037512595031058612950802328971801913498711880111052682274056041470625863586779333188842602381844572406517251106159327934511268610438516820278066686225397795046020275055545005189953702783748235257613991379770525910232674719428");

        KeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeySpec privateKeySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return new KeyPair(keyFactory.generatePublic(publicKeySpec), keyFactory.generatePrivate(privateKeySpec));
    }

    private static void validateRsaKeyBundle(KeyBundle bundle, String vault, String keyName, String kty, List<String> key_ops) throws Exception {
        String prefix = vault + "/keys/" + keyName + "/";
        String kid = bundle.key().kid();
        Assert.assertTrue( 
                String.format("\"kid\" should start with \"%s\", but instead the value is \"%s\".", prefix, kid), 
                kid.startsWith(prefix));
        Assert.assertEquals(kty, bundle.key().kty());
        Assert.assertNotNull("\"n\" should not be null.", bundle.key().n());
        Assert.assertNotNull("\"e\" should not be null.", bundle.key().e());
        if (key_ops != null) {
            Assert.assertTrue(key_ops.equals(bundle.key().keyOps()));
        }
        Assert.assertNotNull("\"created\" should not be null.", bundle.attributes().created());
        Assert.assertNotNull("\"updated\" should not be null.", bundle.attributes().updated());
    }



    private void compareKeyBundles(KeyBundle expected, KeyBundle actual) {
    	Assert.assertTrue(expected.key().toString().equals(actual.key().toString()));
    	Assert.assertEquals(expected.attributes().enabled(), actual.attributes().enabled());
    	Assert.assertEquals(expected.attributes().expires(), actual.attributes().expires());
    	Assert.assertEquals(expected.attributes().notBefore(), actual.attributes().notBefore());
    	if(expected.tags() != null || actual.tags() != null)
    		Assert.assertTrue(expected.tags().equals(actual.tags()));
	}
}
