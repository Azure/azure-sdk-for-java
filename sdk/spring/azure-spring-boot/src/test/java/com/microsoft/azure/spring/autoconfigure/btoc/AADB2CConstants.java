// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.btoc;


import static com.microsoft.azure.spring.autoconfigure.btoc.AADB2CProperties.PREFIX;
import static com.microsoft.azure.spring.autoconfigure.btoc.AADB2CProperties.USER_FLOW_SIGN_UP_OR_SIGN_IN;

public class AADB2CConstants {

    public static final String TEST_TENANT = "fake-tenant";

    public static final String TEST_CLIENT_ID = "fake-client-id";

    public static final String TEST_CLIENT_SECRET = "fake-client-secret";

    public static final String TEST_REPLY_URL = "http://localhost:8080/index";

    public static final String TEST_SIGN_UP_OR_IN_NAME = "fake-sign-in-or-up";

    public static final String TEST_LOGOUT_SUCCESS_URL = "https://fake-logout-success-url";

    public static final String TENANT = String.format("%s.%s", PREFIX, "tenant");

    public static final String CLIENT_ID = String.format("%s.%s", PREFIX, "client-id");

    public static final String CLIENT_SECRET = String.format("%s.%s", PREFIX, "client-secret");

    public static final String REPLY_URL = String.format("%s.%s", PREFIX, "reply-url");

    public static final String LOGOUT_SUCCESS_URL = String.format("%s.%s", PREFIX, "logout-success-url");

    public static final String SIGN_UP_OR_SIGN_IN = String.format("%s.%s", PREFIX, USER_FLOW_SIGN_UP_OR_SIGN_IN);
}
