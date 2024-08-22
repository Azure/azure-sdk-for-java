// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Challenge {
    private String scheme;
    private final List<Parameter> parameters = new ArrayList<>();

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void addParameter(String name, String value) {
        this.parameters.add(new Parameter(name, value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Challenge challenge = (Challenge) o;
        return Objects.equals(scheme, challenge.scheme) && Objects.equals(parameters, challenge.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, parameters);
    }

    public static class Parameter {
        private final String name;
        private final String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Parameter parameter = (Parameter) o;
            return Objects.equals(name, parameter.name) && Objects.equals(value, parameter.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }
    }
}
