// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;


import static com.azure.spring.autoconfigure.b2c.AADB2CProperties.PREFIX;
import static com.azure.spring.autoconfigure.b2c.AADB2CProperties.USER_FLOW_SIGN_IN;
import static com.azure.spring.autoconfigure.b2c.AADB2CProperties.USER_FLOW_SIGN_UP;
import static com.azure.spring.autoconfigure.b2c.AADB2CProperties.USER_FLOW_SIGN_UP_OR_SIGN_IN;

public class AADB2CConstants {
    public static final String AUTHENTICATE_ADDITIONAL_PARAMETERS_LOGIN_HINT = ".authenticate-additional-parameters"
        + ".login-hint";

    public static final String AUTHENTICATE_ADDITIONAL_PARAMETERS_PROMPT = ".authenticate-additional-parameters.prompt";

    public static final String PROMPT = "prompt";

    public static final String LOGIN_HINT = "login-hint";

    public static final Object TEST_PROMPT = "fake-prompt";

    public static final String TEST_LOGIN_HINT = "fake-login-hint";

    public static final String TEST_BASE_URI = "https://faketenant.b2clogin.com/faketenant.onmicrosoft.com/";

    public static final String TEST_CLIENT_ID = "fake-client-id";

    public static final String TEST_CLIENT_SECRET = "fake-client-secret";

    public static final String TEST_REPLY_URL = "http://localhost:8080/index";

    public static final String TEST_SIGN_UP_OR_IN_NAME = "fake-sign-in-or-up";

    public static final Object TEST_SIGN_IN_NAME = "fake-sign-in";

    public static final Object TEST_SIGN_UP_NAME = "fake-sign-up";

    public static final String TEST_LOGOUT_SUCCESS_URL = "https://fake-logout-success-url";

    public static final String BASE_URI = String.format("%s.%s", PREFIX, "base-uri");

    public static final String TEST_ATTRIBUTE_NAME = String.format("%s.%s", PREFIX, "name");

    public static final String USER_NAME_ATTRIBUTE_NAME = String.format("%s.%s", PREFIX, "user-name-attribute-name");

    public static final String CLIENT_ID = String.format("%s.%s", PREFIX, "client-id");

    public static final String CLIENT_SECRET = String.format("%s.%s", PREFIX, "client-secret");

    public static final String LOGOUT_SUCCESS_URL = String.format("%s.%s", PREFIX, "logout-success-url");

    public static final String SIGN_UP_OR_SIGN_IN = String.format("%s.%s", PREFIX, USER_FLOW_SIGN_UP_OR_SIGN_IN);

    public static final String SIGN_UP = String.format("%s.%s", PREFIX, USER_FLOW_SIGN_UP);

    public static final String SIGN_IN = String.format("%s.%s", PREFIX, USER_FLOW_SIGN_IN);

    public static final Object CONFIG_PROMPT = String.format("%s.%s", PREFIX,
        AUTHENTICATE_ADDITIONAL_PARAMETERS_PROMPT);

    public static final String CONFIG_LOGIN_HINT = String.format("%s.%s", PREFIX,
        AUTHENTICATE_ADDITIONAL_PARAMETERS_LOGIN_HINT);
}
