// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.core.util.UrlBuilder;
import com.azure.iot.modelsrepository.implementation.ErrorMessageConstants;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class DtmiConventions {

    /**
     * A DTMI has three components: scheme, path, and version.
     * Scheme and path are separated by a colon. Path and version are separated by a semicolon i.e. <scheme> : <path> ; <version>.
     * The scheme is the string literal "dtmi" in lowercase. The path is a sequence of one or more segments, separated by colons.
     * The version is a sequence of one or more digits. Each path segment is a non-empty string containing only letters, digits, and undersc
     * The first character may not be a digit, and the last character may not be an underscore.
     * The version length is limited to nine digits, because the number 999,999,999 fits in a 32-bit signed integer value.
     * The first digit may not be zero, so there is no ambiguity regarding whether version 1 matches version 01 since the latter is invalid.
     */
    private static final Pattern validDtmiPattern = Pattern.compile("^dtmi:[A-Za-z](?:[A-Za-z0-9_]*[A-Za-z0-9])?(?::[A-Za-z](?:[A-Za-z0-9_]*[A-Za-z0-9])?)*;[1-9][0-9]{0,8}$");

    /**
     * Indicates whether a given string DTMI value is well-formed.
     *
     * @param dtmi DigitalTwin Model Id
     * @return True if dtmi has a valid format. False otherwise.
     */
    public static boolean isValidDtmi(String dtmi) {
        if (dtmi == null || dtmi.isEmpty()) {
            return false;
        }

        return validDtmiPattern.matcher(dtmi).find();
    }

    public static URI getModelUri(String dtmi, URI repositoryUri, boolean fromExpanded) throws URISyntaxException {
        String dtmiPath = dtmiToPath(dtmi);

        if (dtmiPath == null) {
            throw new IllegalArgumentException(String.format(ErrorMessageConstants.InvalidDtmiFormat, dtmi));
        }

        if (fromExpanded) {
            dtmiPath = dtmiPath.replace(".json", ".expanded.json");
        }

        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.setHost(repositoryUri.getHost());
        urlBuilder.setScheme(repositoryUri.getScheme());
        urlBuilder.setPath(repositoryUri.getPath());
        urlBuilder.setQuery(repositoryUri.getQuery());

        if (repositoryUri.getPort() > 0) {
            urlBuilder.setPort(repositoryUri.getPort());
        }

        String path = urlBuilder.getPath();

        if (path != null && !path.endsWith("/")) {
            urlBuilder.setPath(path += "/");
        }

        if (urlBuilder.getPath() == null) {
            urlBuilder.setPath(dtmiPath);
        } else {
            urlBuilder.setPath(urlBuilder.getPath() + dtmiPath);
        }

        return new URI(urlBuilder.toString());
    }

    public static URI convertToUri(String uri) {

        try {
            return new URI(uri);
        } catch (URISyntaxException ex) {
            Path path = Path.of(uri).normalize();
            return new File(path.toAbsolutePath().toString()).toURI();
        }
    }

    static String dtmiToPath(String dtmi) {
        if (!isValidDtmi(dtmi)) {
            throw new IllegalArgumentException(String.format(ErrorMessageConstants.InvalidDtmiFormat, dtmi));
        }

        return dtmi
            .toLowerCase()
            .replaceAll(":", "/")
            .replaceAll(";", "-")
            + ".json";
    }
}
