// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.generic.core.http.HttpClientProvider;
import com.generic.core.http.policy.AfterRetryPolicyProvider;
import com.generic.core.http.policy.BeforeRetryPolicyProvider;
import com.generic.core.util.serializer.JsonSerializerProvider;
import com.generic.core.util.serializer.MemberNameConverterProvider;

module com.generic.core {
    requires transitive org.slf4j;

    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;

    requires transitive com.fasterxml.jackson.datatype.jsr310;

    // public API surface area
    exports com.generic.core.annotation;
    exports com.generic.core.credential;
    exports com.generic.core.exception;
    exports com.generic.core.http;
    exports com.generic.core.http.policy;
    exports com.generic.core.http.rest;
    exports com.generic.core.util;
    exports com.generic.core.util.logging;
    exports com.generic.core.util.serializer;

    // exporting some packages specifically for Jackson
    opens com.generic.core.credential to com.fasterxml.jackson.databind;
    opens com.generic.core.http to com.fasterxml.jackson.databind;
    opens com.generic.core.util to com.fasterxml.jackson.databind;
    opens com.generic.core.util.logging to com.fasterxml.jackson.databind;
    opens com.generic.core.util.serializer to com.fasterxml.jackson.databind;
    opens com.generic.core.implementation to com.fasterxml.jackson.databind;
    opens com.generic.core.implementation.logging to com.fasterxml.jackson.databind;
    opens com.generic.core.implementation.serializer to com.fasterxml.jackson.databind;
    opens com.generic.core.implementation.jackson to com.fasterxml.jackson.databind;
    opens com.generic.core.implementation.util to com.fasterxml.jackson.databind;
    opens com.generic.core.implementation.http.rest to com.fasterxml.jackson.databind;
    opens com.generic.core.http.rest to com.fasterxml.jackson.databind;
    opens com.generic.core.util.url to com.fasterxml.jackson.databind;
    opens com.generic.core.util.configuration to com.fasterxml.jackson.databind;
    exports com.generic.core.util.configuration;
    opens com.generic.core.implementation.http.rest.sync to com.fasterxml.jackson.databind;

    // Service Provider Interfaces
    uses HttpClientProvider;
    uses BeforeRetryPolicyProvider;
    uses AfterRetryPolicyProvider;
    uses JsonSerializerProvider;
    uses MemberNameConverterProvider;
}
