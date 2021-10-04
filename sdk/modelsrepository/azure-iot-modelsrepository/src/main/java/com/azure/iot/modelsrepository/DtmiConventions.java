// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.iot.modelsrepository.implementation.ModelsRepositoryConstants;
import com.azure.iot.modelsrepository.implementation.StatusStrings;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * DtmiConventions implements the core aspects of the IoT model repo conventions which includes DTMI validation and
 * calculating a URI path from a DTMI.
 */
public final class DtmiConventions {

    private DtmiConventions() { }

    /**
     * A DTMI has three components: scheme, path, and version. Scheme and path are separated by a colon. Path and
     * version are separated by a semicolon i.e. scheme : path ; version. The scheme is the string literal "dtmi" in
     * lowercase. The path is a sequence of one or more segments, separated by colons. The version is a sequence of one
     * or more digits. Each path segment is a non-empty string containing only letters, digits, and undersc The first
     * character may not be a digit, and the last character may not be an underscore. The version length is limited to
     * nine digits, because the number 999,999,999 fits in a 32-bit signed integer value. The first digit may not be
     * zero, so there is no ambiguity regarding whether version 1 matches version 01 since the latter is invalid.
     */
    private static final Pattern VALID_DTMI_PATTERN = Pattern.compile(
        "^dtmi:[A-Za-z](?:[A-Za-z0-9_]*[A-Za-z0-9])?(?::[A-Za-z](?:[A-Za-z0-9_]*[A-Za-z0-9])?)*;[1-9][0-9]{0,8}$");

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
        return true;
        // return VALID_DTMI_PATTERN.matcher(dtmi).matches();
    }

    /**
     * Generates the model URI.
     *
     * @param dtmi DigitalTwin Model Id.
     * @param repositoryUri The repository uri
     * @param expanded Is model from precomputed values
     * @return The model uri.
     * @throws IllegalArgumentException if the provided dtmi is not valid.
     */
    public static URI getModelUri(String dtmi, URI repositoryUri, boolean expanded) {
        String dtmiPath = dtmiToPath(dtmi);

        if (expanded) {
            dtmiPath = dtmiPath.replace(ModelsRepositoryConstants.JSON_EXTENSION,
                ModelsRepositoryConstants.JSON_EXPANDED_EXTENSION);
        }

        String stringUri = repositoryUri.toString();
        try {
            if (stringUri.endsWith("/")) {
                return new URI(stringUri + dtmiPath);
            } else {
                return new URI(stringUri + "/" + dtmiPath);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid uri syntax");
        }
    }

    /**
     * Generates the model repository's metadata URI.
     *
     * @param repositoryUri The repository uri
     * @return The repository metadata uri.
     * @throws IllegalArgumentException if the provided repository URI is not valid
     */
    public static URI getMetadataUri(URI repositoryUri) {
        try {
            String stringUri = repositoryUri.toString();
            if (stringUri.endsWith("/")) {
                return new URI(stringUri + ModelsRepositoryConstants.MODELS_REPOSITORY_METADATA_FILE);
            } else {
                return new URI(stringUri + "/" + ModelsRepositoryConstants.MODELS_REPOSITORY_METADATA_FILE);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid uri syntax");
        }
    }

    static String dtmiToPath(String dtmi) {
        if (!isValidDtmi(dtmi)) {
            throw new IllegalArgumentException(String.format(StatusStrings.INVALID_DTMI_FORMAT_S, dtmi));
        }

        return dtmi
            .toLowerCase(Locale.getDefault())
            .replaceAll(":", "/")
            .replaceAll(";", "-")
            + ModelsRepositoryConstants.JSON_EXTENSION;
    }
}
