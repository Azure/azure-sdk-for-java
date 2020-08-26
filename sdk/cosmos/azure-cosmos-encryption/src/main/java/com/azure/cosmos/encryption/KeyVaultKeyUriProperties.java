// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;


//  TODO: this also doesn't need to be public, it is public because of test FIXME
public class KeyVaultKeyUriProperties {
    private final static Logger logger = LoggerFactory.getLogger(KeyVaultKeyUriProperties.class);
    private final URI keyUri;
    private String keyName;
    private URI keyVaultUri;
    private String keyVersion;

    /**
     * Initializes a new instance of the {@link KeyVaultKeyUriProperties} Helper Class to fetch frequently used Uri
     * parsed information for KeyVault.
     *
     * @param keyUri
     */
    private KeyVaultKeyUriProperties(URI keyUri) {
        this.keyUri = keyUri;
    }

    public URI getKeyUri() {
        return keyUri;
    }

    public String getKeyName() {
        return keyName;
    }

    public String getKeyVersion() {
        return keyVersion;
    }

    public URI getKeyVaultUri() {
        return keyVaultUri;
    }

    private static String[] getSegments(URI uri) {
        return StringUtils.split(uri.getPath(), "/");
    }

    public static boolean tryParse(URI keyUri,
                                   AtomicReference<KeyVaultKeyUriProperties> keyVaultUriPropertiesReference) {
        KeyVaultKeyUriProperties keyVaultUriProperties = null;

        String[] segments = getSegments(keyUri);
        // https://testdemo1.vault.azure.net/keys/testkey1/47d306aeaae74baab294672354603ca3

        // https://testdemo1.vault.azure.net/keys/testkey1/47d306aeaae74baab294672354603ca3
        if (!((segments.length == 3) && StringUtils.equalsIgnoreCase(segments[0], KeyVaultConstants.KeysSegment))) {
            return false;
        }

        try {
            // TODO: shifted the index wrt dot net and keyName is "testKey1 instead of testkey1/
            keyVaultUriProperties = new KeyVaultKeyUriProperties(keyUri);
            keyVaultUriProperties.keyName = segments[1]; // "testKey1/"
            keyVaultUriProperties.keyVersion = segments[2]; // "47d306aeaae74baab294672354603ca3"
            //keyVaultUriProperties.keyVaultUri = new URI(keyVaultUriProperties.KeyUri.GetLeftPart(UriPartial.Scheme
            // | UriPartial.Authority)); // https://testdemo1.vault.azure.net/
            keyVaultUriProperties.keyVaultUri = new URI(keyUri.getScheme(), keyUri.getAuthority(), null, null, null); // https://testdemo1.vault.azure.net/

            keyVaultUriPropertiesReference.set(keyVaultUriProperties);
        } catch (URISyntaxException e) {
            logger.error("failed to parse uri {}", keyUri, e);
            return false;
        }
        return true;
    }
}
