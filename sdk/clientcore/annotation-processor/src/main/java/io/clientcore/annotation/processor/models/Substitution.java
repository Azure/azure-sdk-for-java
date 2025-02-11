// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.models;

/**
 * A Substitution is a value that can be used to replace placeholder values in a URL. Placeholders look like:
 * "http://{host}.com/{fileName}.html", where "{host}" and "{fileName}" are the placeholders.
 */
public class Substitution {
    private final String parameterName;
    private final String parameterVariableName;
    private final boolean shouldEncode;

    /**
     * Create a new Substitution.
     *
     * @param parameterName The name that is used between curly quotes as a placeholder in the target URL.
     * @param parameterVariableName The name of the variable that will be used to replace the placeholder in the target
     */
    public Substitution(String parameterName, String parameterVariableName) {
        this(parameterName, parameterVariableName, false);
    }

    /**
     * Create a new Substitution.
     *
     * @param parameterName The name that is used between curly quotes as a placeholder in the target URL.
     * @param parameterVariableName The name of the variable that will be used to replace the placeholder in the target
     * @param shouldEncode Whether the value from the method's argument should be encoded when the substitution is
     * taking place.
     */
    public Substitution(String parameterName, String parameterVariableName, boolean shouldEncode) {
        this.parameterName = parameterName;
        this.parameterVariableName = parameterVariableName;
        this.shouldEncode = shouldEncode;
    }

    /**
     * Get the placeholder's name.
     *
     * @return The name of the placeholder.
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Get the variable name that will be used to replace the placeholder in the target URL.
     *
     * @return The name of the variable that will be used to replace the placeholder in the target URL.
     */
    public String getParameterVariableName() {
        return parameterVariableName;
    }

    /**
     * Whether the replacement value from the method argument needs to be encoded when the substitution is taking
     * place.
     *
     * @return Whether the replacement value from the method argument needs to be encoded when the substitution is
     * taking place.
     */
    public boolean shouldEncode() {
        return shouldEncode;
    }
}
