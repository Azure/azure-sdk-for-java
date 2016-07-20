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

package com.microsoft.azure.keyvault.implementation;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;

public final class IssuerIdentifier extends ObjectIdentifier {
	
    public static boolean isIssuerIdentifier(String identifier) {
        identifier = verifyNonEmpty(identifier, "identifier");

        URI baseUri;
        try {
            baseUri = new URI(identifier);
        } catch (URISyntaxException e) {
            return false;
        }

        String[] segments = baseUri.getPath().split("/");
        if (segments.length != 4 || segments[1] != "certificates" || segments[2] != "issuers") {
            return false;
        }

        return true;
    }

    public IssuerIdentifier(String vault, String name) {
        vault = verifyNonEmpty(vault, "vault");

        name = verifyNonEmpty(name, "name");

        URI baseUri;
		try {
			baseUri = new URI(vault);
        } catch (URISyntaxException e) {
            throw new InvalidParameterException(String.format("Invalid ObjectIdentifier: %s. Not a valid URI", vault));
        }

        this.name = name;
        this.version = null;
        this.vault = String.format("%s://%s", baseUri.getScheme(), getFullAuthority(baseUri));
        
        baseIdentifier = String.format("%s/%s/%s", this.vault, "certificates/issuers", this.name);
        identifier = baseIdentifier;
    }

    public IssuerIdentifier(String identifier) {

        identifier = verifyNonEmpty(identifier, "identifier");

        URI baseUri;
        try {
            baseUri = new URI(identifier);
        } catch (URISyntaxException e) {
            throw new InvalidParameterException(String.format("Invalid ObjectIdentifier: %s. Not a valid URI", identifier));
        }

        // Path is of the form "/collection/name[/version]"
        String[] segments = baseUri.getPath().split("/");
        if (segments.length != 4) {
            throw new InvalidParameterException(String.format("Invalid ObjectIdentifier: %s. Bad number of segments: %d", identifier, segments.length));
        }
        
        if (!segments[1].equals("certificates")) {
            throw new InvalidParameterException(
            		String.format("Invalid ObjectIdentifier: %s. Segment [1] should be '%s', found '%s'", identifier, "certificates", segments[1]));
        }
        if (!segments[2].equals("issuers")) {
            throw new InvalidParameterException(
            		String.format("Invalid ObjectIdentifier: %s. Segment [2] should be '%s', found '%s'", identifier, "issuers", segments[2]));
        }

        name = segments[3];
        version = "";
        vault = String.format("%s://%s", baseUri.getScheme(), getFullAuthority(baseUri));
        baseIdentifier = String.format("%s/%s/%s", vault, "certificates/issuers", name);
        this.identifier = baseIdentifier;
    }
}
