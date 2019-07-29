// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;

/**
 * Contains the configurations for tests.
 * 
 * For running tests, you can pass a customized endpoint configuration in one of the following
 * ways:
 * <ul>
 * <li>-DACCOUNT_KEY="[your-key]" -ACCOUNT_HOST="[your-endpoint]" as JVM
 * command-line option.</li>
 * <li>You can set ACCOUNT_KEY and ACCOUNT_HOST as environment variables.</li>
 * </ul>
 * 
 * If none of the above is set, emulator endpoint will be used.
 */
public final class TestConfigurations {
    // REPLACE MASTER_KEY and HOST with values from your Azure Cosmos DB account.
    // The default values are credentials of the local emulator, which are not used in any production environment.
    // <!--[SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine")]-->
    public static String MASTER_KEY =
            System.getProperty("ACCOUNT_KEY", 
                    StringUtils.defaultString(Strings.emptyToNull(
                            System.getenv().get("ACCOUNT_KEY")),
                            "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw=="));

    public static String HOST =
            System.getProperty("ACCOUNT_HOST",
                    StringUtils.defaultString(Strings.emptyToNull(
                            System.getenv().get("ACCOUNT_HOST")),
                            "https://localhost:443/"));

    public static String CONSISTENCY =
            System.getProperty("ACCOUNT_CONSISTENCY",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("ACCOUNT_CONSISTENCY")), "Strong"));

    public static String PREFERRED_LOCATIONS =
            System.getProperty("PREFERRED_LOCATIONS",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("PREFERRED_LOCATIONS")), null));

    public static String MAX_RETRY_LIMIT =
            System.getProperty("MAX_RETRY_LIMIT",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("MAX_RETRY_LIMIT")),
                                                         "2"));

    public static String DESIRED_CONSISTENCIES =
            System.getProperty("DESIRED_CONSISTENCIES",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("DESIRED_CONSISTENCIES")),
                                                         null));

    public static String PROTOCOLS =
            System.getProperty("PROTOCOLS",
                               StringUtils.defaultString(Strings.emptyToNull(
                                       System.getenv().get("PROTOCOLS")),
                                                         null));
}
