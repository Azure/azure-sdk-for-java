// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;


import static com.azure.spring.cloud.autoconfigure.aadb2c.properties.AadB2cProperties.PREFIX;

class AadB2cConstants {
    static final String AUTHENTICATE_ADDITIONAL_PARAMETERS_LOGIN_HINT = ".authenticate-additional-parameters"
        + ".login-hint";

    static final String AUTHENTICATE_ADDITIONAL_PARAMETERS_PROMPT = ".authenticate-additional-parameters.prompt";

    static final String PROMPT = "prompt";

    static final String LOGIN_HINT = "login-hint";

    static final Object TEST_PROMPT = "fake-prompt";

    static final String TEST_LOGIN_HINT = "fake-login-hint";

    static final String TEST_BASE_URI = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/";

    static final String TEST_CLIENT_ID = "fake-client-id";

    static final String TEST_TENANT_ID = "fake-tenant-id";

    static final String TEST_CLIENT_SECRET = "fake-client-secret";
    static final String TEST_APP_ID_URI = "https://fake-tenant.onmicrosoft.com/custom";

    static final String TEST_KEY_SIGN_UP_OR_IN = "sign-up-or-sign-in";
    static final String TEST_SIGN_UP_OR_IN_NAME = "fake-sign-in-or-up";

    static final Object TEST_KEY_SIGN_IN = "sign-in";
    static final Object TEST_SIGN_IN_NAME = "fake-sign-in";

    static final Object TEST_KEY_SIGN_UP = "sign-up";
    static final Object TEST_SIGN_UP_NAME = "fake-sign-up";

    static final String TEST_KEY_PROFILE_EDIT = "profile-edit";
    static final String TEST_PROFILE_EDIT_NAME = "profile_edit";

    static final String TEST_LOGOUT_SUCCESS_URL = "https://fake-logout-success-url";

    static final String TEST_CLIENT_CREDENTIAL_SCOPES = "https://fake-tenant.onmicrosoft.com/other/.default";
    static final String TEST_CLIENT_CREDENTIAL_GRANT_TYPE = "client_credentials";

    static final String CLIENT_CREDENTIAL_NAME = "webApiA";

    static final String BASE_URI = String.format("%s.%s", PREFIX, "base-uri");

    static final String TEST_ATTRIBUTE_NAME = String.format("%s.%s", PREFIX, "name");

    static final String USER_NAME_ATTRIBUTE_NAME = String.format("%s.%s", PREFIX, "user-name-attribute-name");

    static final String CLIENT_ID = String.format("%s.%s", PREFIX, "credential.client-id");

    static final String TENANT_ID = String.format("%s.%s", PREFIX, "profile.tenant-id");

    static final String CLIENT_SECRET = String.format("%s.%s", PREFIX, "credential.client-secret");
    static final String APP_ID_URI = String.format("%s.%s", PREFIX, "app-id-uri");

    static final String LOGOUT_SUCCESS_URL = String.format("%s.%s", PREFIX, "logout-success-url");

    static final String LOGIN_FLOW = String.format("%s.%s", PREFIX, "login-flow");

    static final String USER_FLOWS = String.format("%s.%s", PREFIX, "user-flows");

    static final Object CONFIG_PROMPT = String.format("%s.%s", PREFIX,
        AUTHENTICATE_ADDITIONAL_PARAMETERS_PROMPT);

    static final String CONFIG_LOGIN_HINT = String.format("%s.%s", PREFIX,
        AUTHENTICATE_ADDITIONAL_PARAMETERS_LOGIN_HINT);

    static final String AUTHORIZATION_CLIENTS = String.format("%s.%s", PREFIX, "authorization-clients");
}
