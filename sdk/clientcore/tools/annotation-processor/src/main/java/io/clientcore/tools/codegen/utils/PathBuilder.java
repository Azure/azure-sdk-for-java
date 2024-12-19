// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.utils;

import io.clientcore.tools.codegen.exceptions.MissingSubstitutionException;
import io.clientcore.tools.codegen.models.HttpRequestContext;
import io.clientcore.tools.codegen.models.Substitution;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathBuilder {
    // this class takes a 'raw host' string that contains {} delimited parameters, and needs to convert it into a
    // Java string concatenation that can be used in the generated code. For example, the raw host string:
    // https://{endpoint}/keys/{path1}
    // would be converted into:
    // "https://" + endpointParam + "/keys/" + pathValue
    // Note that query parameters may also exist, and should be appended to the end of the URL string using
    // a Map containing key-value pairs.
    // Note that the 'endpoint' parameter is special - it is always the first parameter, and is always a host parameter.
    public static String buildPath(String rawHost, HttpRequestContext method) {
        if (method == null) {
            throw new NullPointerException("method cannot be null");
        }

        final boolean hasQueryParams = !method.getQueryParams().isEmpty();

        Pattern pattern = Pattern.compile("\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(rawHost);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Substitution substitution = method.getSubstitution(paramName);
            if (substitution != null && !substitution.getParameterVariableName().isEmpty()) {
                matcher.appendReplacement(buffer, "");

                if (buffer.length() != 0) {
                    buffer.append("\" + ");
                }
                buffer.append(substitution.getParameterVariableName()).append(" + \"");
            } else {
                throw new MissingSubstitutionException("Could not find substitution for '" + paramName + "' in method '" + method.getMethodName() + "'");
            }
        }

        // Remove the last " + \""
        if (matcher.hitEnd()) {
            matcher.appendTail(buffer);

            if (!hasQueryParams) {
                buffer.append("\"");
            }
        }

        if (hasQueryParams) {
            buffer.append("?");
            for (Map.Entry<String, String> entry : method.getQueryParams().entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key.isEmpty() || value.isEmpty()) {
                    throw new IllegalArgumentException("Query parameter key and value must not be empty");
                }
                buffer.append(entry.getKey()).append("=\" + ").append(entry.getValue()).append(" + \"&");
            }
            // Remove the last "&\""
            buffer.delete(buffer.length() - 5, buffer.length());
        }

        // Add opening and closing quotes if they are not present and the first/last part is not a substitution
        if (buffer.charAt(0) != '\"' && !rawHost.startsWith("{")) {
            buffer.insert(0, '\"');
        }
        if (!hasQueryParams && buffer.charAt(buffer.length() - 1) != '\"' && !rawHost.endsWith("}")) {
            buffer.append('\"');
        }

        // strip out unnecessary `+ ""` in the buffer
        String result = buffer.toString();
        result = result.replaceAll(" \\+ \"\"", "");

        return result;
    }
}
