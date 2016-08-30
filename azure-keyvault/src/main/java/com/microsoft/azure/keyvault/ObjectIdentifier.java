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

package com.microsoft.azure.keyvault;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;

/**
 * The key vault object identifier.
 */
public class ObjectIdentifier {

    /**
     * Verifies whether the identifier belongs to a key vault object. 
     * @param collection the object collection e.g. 'keys', 'secrets' and 'certificates'.
     * @param identifier the key vault object identifier.
     * @return true if the identifier belongs to a key vault object. False otherwise.
     */
    protected static boolean isObjectIdentifier(String collection, String identifier) {

        collection = verifyNonEmpty(collection, "collection");
        identifier = verifyNonEmpty(identifier, "identifier");

        URI baseUri;
        try {
            baseUri = new URI(identifier);
        } catch (URISyntaxException e) {
            return false;
        }

        // Path is of the form "/collection/name[/version]"
        String[] segments = baseUri.getPath().split("/");
        if (segments.length != 3 && segments.length != 4) {
            return false;
        }

        if (!collection.equals(segments[1])) {
            return false;
        }

        return true;
    }

    /**
     * Verifies a value is null or empty. Returns the value if non-empty and throws exception if empty.  
     * @param value the value to verify.
     * @param argName the name of the value.
     * @return Returns the value if non-empty.
     */
    protected static String verifyNonEmpty(String value, String argName) {
        if (value != null) {
            value = value.trim();
            if (value.isEmpty()) {
                value = null;
            }
        }
        if (value == null) {
            throw new IllegalArgumentException(argName);
        }
        return value;
    }

    protected String vault;
    protected String name;
    protected String version;
    protected String baseIdentifier;
    protected String identifier;

    /**
     * Constructor.
     */
    protected ObjectIdentifier() {
    }

    /**
     * Constructor.
     * @param vault the vault url.
     * @param collection the object collection name. e.g. 'keys', 'secrets' and 'certificates'.
     * @param name the object name.
     */
    protected ObjectIdentifier(String vault, String collection, String name) {
        this(vault, collection, name, null);
    }

    /**
     * Constructor.
     * @param vault the vault url.
     * @param collection the object collection name. e.g. 'keys', 'secrets' and 'certificates'.
     * @param name the object name.
     * @param version the object version.
     */
    protected ObjectIdentifier(String vault, String collection, String name, String version) {

        vault = verifyNonEmpty(vault, "vault");
        collection = verifyNonEmpty(collection, "collection");
        name = verifyNonEmpty(name, "name");

        if (version != null) {
            version = version.trim();
        } else {
            version = "";
        }

        URI baseUri;
        try {
            baseUri = new URI(vault);
        } catch (URISyntaxException e) {
            throw new InvalidParameterException(String.format("Invalid ObjectIdentifier: %s. Not a valid URI", vault));
        }

        this.name = name;
        this.version = version;
        this.vault = String.format("%s://%s", baseUri.getScheme(), getFullAuthority(baseUri));
        baseIdentifier = String.format("%s/%s/%s", this.vault, collection, this.name);
        identifier = (version == null || version.isEmpty()) ? baseIdentifier
                : String.format("%s/%s", baseIdentifier, version);
    }

    /**
     * Constructor.
     * @param collection the object collection name. e.g. 'keys', 'secrets' and 'certificates'.
     * @param identifier the object identifier.
     */
    protected ObjectIdentifier(String collection, String identifier) {

        if (collection == null || collection.length() == 0) {
            throw new IllegalArgumentException("collection");
        }

        if (identifier == null || identifier.length() == 0) {
            throw new IllegalArgumentException("identifier");
        }

        URI baseUri;
        try {
            baseUri = new URI(identifier);
        } catch (URISyntaxException e) {
            throw new InvalidParameterException(
                    String.format("Invalid ObjectIdentifier: %s. Not a valid URI", identifier));
        }

        // Path is of the form "/collection/name[/version]"
        String[] segments = baseUri.getPath().split("/");
        if (segments.length != 3 && segments.length != 4) {
            throw new InvalidParameterException(String
                    .format("Invalid ObjectIdentifier: %s. Bad number of segments: %d", identifier, segments.length));
        }

        if (!collection.equals(segments[1])) {
            throw new InvalidParameterException(
                    String.format("Invalid ObjectIdentifier: %s. segment [1] should be '%s', found '%s'", identifier,
                            collection, segments[1]));
        }

        name = segments[2];
        version = segments.length == 4 ? segments[3] : null;
        vault = String.format("%s://%s", baseUri.getScheme(), getFullAuthority(baseUri));
        baseIdentifier = String.format("%s/%s/%s", vault, collection, name);
        this.identifier = (version == null || version.equals("")) ? baseIdentifier
                : String.format("%s/%s", baseIdentifier, version);
    }

    /**
     * Gets full authority for a URL by appending port to the url authority.
     * @param uri the URL to get the full authority for.
     * @return the full authority.
     */
    protected String getFullAuthority(URI uri) {
        String authority = uri.getAuthority();
        if (!authority.contains(":") && uri.getPort() > 0) {
            // Append port for complete authority
            authority = String.format("%s:%d", uri.getAuthority(), uri.getPort());
        }
        return authority;
    }

    /**
     * @return The base identifier for an object, does not include the object
     *         version.
     */
    public String baseIdentifier() {
        return baseIdentifier;
    }

    /**
     * @return The identifier for an object, includes the objects version.
     */
    public String identifier() {
        return identifier;
    }

    /**
     * @return The name of the object.
     */
    public String name() {
        return name;
    }

    /**
     * @return The vault containing the object.
     */
    public String vault() {
        return vault;
    }

    /**
     * @return The version of the object.
     */
    public String version() {
        return version;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
