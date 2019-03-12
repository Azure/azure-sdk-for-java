// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

// package-private implementation class
// Users will be passing in ConnectionDetails rather than this class.
class ApplicationConfigCredentials {
    private URL baseUri;
    private String credential;
    private byte[] secret;

    URL baseUri() {
        return baseUri;
    }

    String credential() {
        return credential;
    }

    byte[] secret() {
        return secret;
    }

    static ApplicationConfigCredentials parseConnectionString(String connectionString) {
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalArgumentException(connectionString);
        }

        // Parse connection string
        String[] args = connectionString.split(";");
        if (args.length < 3) {
            throw new IllegalArgumentException("invalid connection string segment count");
        }

        String endpointString = "endpoint=";
        String idString = "id=";
        String secretString = "secret=";

        ApplicationConfigCredentials credentials = new ApplicationConfigCredentials();

        for (String arg : args) {
            String segment = arg.trim();
            try {
                if (segment.toLowerCase().startsWith(endpointString)) {
                    credentials.baseUri = new URL(segment.substring(segment.indexOf('=') + 1));
                } else if (segment.toLowerCase().startsWith(idString)) {
                    credentials.credential = segment.substring(segment.indexOf('=') + 1);
                } else if (segment.toLowerCase().startsWith(secretString)) {
                    String secretBase64 = segment.substring(segment.indexOf('=') + 1);
                    credentials.secret = Base64.getDecoder().decode(secretBase64);
                }
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException(ex);
            }
        }
        return credentials;
    }
}
