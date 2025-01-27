// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import io.clientcore.annotation.processor.exceptions.MissingSubstitutionException;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.Substitution;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for building the path of the request URL by replacing the placeholders with the actual
 */
public class PathBuilder {
    // this class takes a 'raw host' string that contains {} delimited parameters, and needs to convert it into a
    // Java string concatenation that can be used in the generated code. For example, the raw host string:
    // https://{endpoint}/keys/{path1}
    // would be converted into:
    // "https://" + endpointParam + "/keys/" + pathValue
    // Note that query parameters may also exist, and should be appended to the end of the URL string using
    // a Map containing key-value pairs.
    // Note that the 'endpoint' parameter is special - it is always the first parameter, and is always a host parameter.
    /**
     * Builds the path of the request URL by replacing the placeholders with the actual values.
     * @param rawHost The raw host string that contains {} delimited parameters.
     * @param method The HttpRequestContext object that contains the method's configuration, parameters, headers, and other details.
     * @return The path of the request URL with the placeholders replaced with the actual values.
     * @throws NullPointerException If the method is null.
     * @throws MissingSubstitutionException If a substitution is missing for a placeholder in the raw host string.
     * @throws IllegalArgumentException If the query parameter key or value is empty.
     */
    public static String buildPath(String rawHost, HttpRequestContext method) {
        if (method == null) {
            throw new NullPointerException("method cannot be null");
        }

        boolean hasQueryParams = !method.getQueryParams().isEmpty();

        // Pattern for substitution placeholders
        Pattern pattern = Pattern.compile("\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(rawHost);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Substitution substitution = method.getSubstitution(paramName);

            if (substitution != null) {
                String substitutionValue = substitution.getParameterVariableName();
                String replacementValue = substitutionValue != null ? Objects.toString(substitutionValue, "null") : "";

                matcher.appendReplacement(buffer, "");
                if (buffer.length() != 0) {
                    buffer.append("\" + ");
                }
                buffer.append(replacementValue).append(" + \"");
            } else {
                throw new MissingSubstitutionException(
                    "Could not find substitution for '" + paramName + "' in method '" + method.getMethodName() + "'");
            }
        }

        matcher.appendTail(buffer);

        if (hasQueryParams) {
            buffer.append("?");

            method.getQueryParams().forEach((key, value) -> {
                if (key.isEmpty() || value.isEmpty()) {
                    throw new IllegalArgumentException("Query parameter key and value must not be empty");
                }
                buffer.append(key).append("=\" + ").append(Objects.toString(value, "null")).append(" + \"&");
            });

            // Remove the trailing '&'
            buffer.setLength(buffer.length() - 1);
        }

        // Ensure the output is properly quoted
        if (buffer.charAt(0) != '"' && !rawHost.startsWith("{")) {
            buffer.insert(0, '"');
        }
        if (!hasQueryParams && buffer.charAt(buffer.length() - 1) != '"' && !rawHost.endsWith("}")) {
            buffer.append('"');
        }

        // Clean unnecessary `+ ""` in the buffer
        String result = buffer.toString().replaceAll(" \\+ \"\"", "");

        // Remove trailing ' + ' if it exists
        if (result.endsWith(" + ")) {
            result = result.substring(0, result.length() - 3);
        }

        // Remove trailing ' + "' if it exists
        if (result.endsWith(" + \"")) {
            result = result.substring(0, result.length() - 4);
        }

        // Check for missing or incorrect braces
        long openingBracesCount = rawHost.chars().filter(ch -> ch == '{').count();
        long closingBracesCount = rawHost.chars().filter(ch -> ch == '}').count();

        if (openingBracesCount != closingBracesCount) {
            throw new MissingSubstitutionException("Mismatched braces in raw host: " + rawHost);
        }

        return result;
    }
}
