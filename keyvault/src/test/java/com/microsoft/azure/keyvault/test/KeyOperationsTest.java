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

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyIdentifier;
import com.microsoft.windowsazure.exception.ServiceException;

public class KeyOperationsTest extends KeyVaultClientIntegrationTestBase {

    private static final String KEY_NAME = "javaKey";

    private static ObjectWriter jsonWriter;
    private static ObjectReader jsonReader;

    @BeforeClass
    public static void setup() throws Exception {
        addRegexRule("testjava[a-z]{10}");
        createKeyVaultClient();
        jsonWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        jsonReader = new ObjectMapper().reader();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }

    @Test
    public void transparentAuthentication() throws Exception {

        if (!handling401) {
            // TODO: Is there a way to report "not tested" without generating a failure?  
            return;
        }

        // Create a key on a vault.
        {
            Future<KeyBundle> result = keyVaultClient.createKeyAsync(getVaultUri(), KEY_NAME, "RSA", null, null, null, null);
            KeyBundle bundle = result.get();
            validateRsaKeyBundle(bundle, getVaultUri(), KEY_NAME, "RSA", null);
        }

        // Create a key on a different vault. Key Vault Data Plane returns 401, which must be transparently handled by KeyVaultCredentials.  
        {
            Future<KeyBundle> result = keyVaultClient.createKeyAsync(getSecondaryVaultUri(), KEY_NAME, "RSA", null, null, null, null);
            KeyBundle bundle = result.get();
            validateRsaKeyBundle(bundle, getSecondaryVaultUri(), KEY_NAME, "RSA", null);
        }

    }

    @SuppressWarnings("serial")
    @Test
    public void basicCrudTest() throws Exception {

        KeyBundle createdBundle;
        {
            // Create key
            Future<KeyBundle> result = keyVaultClient.createKeyAsync(getVaultUri(), KEY_NAME, "RSA", null, null, null, null);
            createdBundle = result.get();
            validateRsaKeyBundle(createdBundle, getVaultUri(), KEY_NAME, "RSA", null);
        }

        // Key identifier.
        KeyIdentifier keyId = new KeyIdentifier(createdBundle.getKey().getKid());

        {
            // Get key using kid WO version
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(keyId.getBaseIdentifier());
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get key using full kid as defined in the bundle
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(createdBundle.getKey().getKid());
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get key using vault and key name.
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(getVaultUri(), KEY_NAME, null);
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Get key using vault, key name and version.
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(getVaultUri(), KEY_NAME, keyId.getVersion());
            KeyBundle readBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(readBundle));
        }

        {
            // Update key using the kid as defined in the bundle

            // First we create a bundle with the modified attributes.
            KeyBundle updatingBundle = cloneWithJson(createdBundle); // Start with a copy of original bundle.
            updatingBundle.getAttributes().setExpires(newDate(2050, 1, 2));
            String[] key_ops;
            updatingBundle.getKey().setKeyOps(Arrays.asList(key_ops = new String[] {"encrypt", "decrypt" }));
            updatingBundle.setTags(new HashMap<String, String>() {
                {
                    put("foo", "baz");
                }
            });

            // Perform the operation.
            Future<KeyBundle> result = keyVaultClient.updateKeyAsync(createdBundle.getKey().getKid(), key_ops, updatingBundle.getAttributes(), updatingBundle.getTags());
            KeyBundle updatedBundle = result.get();
            validateRsaKeyBundle(createdBundle, getVaultUri(), KEY_NAME, "RSA", null); // Basic
            // check.

            // Now the detailed check. Let's compare the JSON value.
            updatingBundle.getAttributes().setUpdatedUnixTime(updatedBundle.getAttributes().getUpdatedUnixTime()); // Nonsense comparing this.
            Assert.assertEquals(jsonWriter.writeValueAsString(updatingBundle), jsonWriter.writeValueAsString(updatedBundle));

            // Subsequent operations must use the updated bundle for comparison.
            createdBundle = updatedBundle;
        }

        {
            // Update key using vault and key name.

            // First we create a bundle with the modified attributes.
            KeyBundle updatingBundle = cloneWithJson(createdBundle); // Start with a copy of original bundle.
            updatingBundle.getAttributes().setNotBefore(newDate(2000, 1, 2));
            String[] key_ops;
            updatingBundle.getKey().setKeyOps(Arrays.asList(key_ops = new String[] {"sign", "verify" }));
            updatingBundle.setTags(new HashMap<String, String>() {
                {
                    put("rex", "woof");
                }
            });

            // Perform the operation.
            Future<KeyBundle> result = keyVaultClient.updateKeyAsync(getVaultUri(), KEY_NAME, key_ops, updatingBundle.getAttributes(), updatingBundle.getTags());
            KeyBundle updatedBundle = result.get();
            validateRsaKeyBundle(createdBundle, getVaultUri(), KEY_NAME, "RSA", null); // Basic check.

            // Now the detailed check. Let's compare the JSON value.
            updatingBundle.getAttributes().setUpdatedUnixTime(updatedBundle.getAttributes().getUpdatedUnixTime()); // Nonsense comparing this.
            Assert.assertEquals(jsonWriter.writeValueAsString(updatingBundle), jsonWriter.writeValueAsString(updatedBundle));

            // Subsequent operations must use the updated bundle for comparison.
            createdBundle = updatedBundle;
        }

        {
            // Delete key
            Future<KeyBundle> result = keyVaultClient.deleteKeyAsync(getVaultUri(), KEY_NAME);
            KeyBundle deleteBundle = result.get();
            Assert.assertEquals(jsonWriter.writeValueAsString(createdBundle), jsonWriter.writeValueAsString(deleteBundle));
        }

        {
            // Expects a key not found
            Future<KeyBundle> result = keyVaultClient.getKeyAsync(keyId.getBaseIdentifier());
            try {
                result.get();
            } catch (ExecutionException e) {
                ServiceException cause = (ServiceException) e.getCause();
                Assert.assertNotNull(cause.getError());
                Assert.assertEquals("KeyNotFound", cause.getError().getCode());
            }
        }

    }

    private static KeyBundle cloneWithJson(KeyBundle template) throws Exception {
        String json = jsonWriter.writeValueAsString(template);
        return jsonReader.withType(KeyBundle.class).readValue(json);
    }

    private static Date newDate(int y, int m, int d) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        calendar.clear();
        calendar.set(y, m, d);
        return calendar.getTime();
    }

    private static void validateRsaKeyBundle(KeyBundle bundle, String vault, String keyName, String kty, String[] key_ops) throws Exception, Exception, Exception {
        String prefix = vault + "/keys/" + keyName + "/";
        String kid = bundle.getKey().getKid();
        Assert.assertTrue( //
                String.format("\"kid\" should start with \"%s\", but instead the value is \"%s\".", prefix, kid),//
                kid.startsWith(prefix));
        Assert.assertEquals(kty, bundle.getKey().getKty());
        Assert.assertNotNull("\"n\" should not be null.", bundle.getKey().getN());
        Assert.assertNotNull("\"e\" should not be null.", bundle.getKey().getE());
        if (key_ops != null) {
            String expected = jsonWriter.writeValueAsString(Arrays.asList(key_ops));
            String actual = jsonWriter.writeValueAsString(bundle.getKey().getKeyOps());
            Assert.assertEquals(expected, actual);
        }
        Assert.assertNotNull("\"created\" should not be null.", bundle.getAttributes().getCreated());
        Assert.assertNotNull("\"updated\" should not be null.", bundle.getAttributes().getUpdated());
    }

}
