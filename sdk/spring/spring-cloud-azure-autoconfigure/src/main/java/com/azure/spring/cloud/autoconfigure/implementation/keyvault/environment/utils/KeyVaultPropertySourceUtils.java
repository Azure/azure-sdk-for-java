// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment.utils;

import org.springframework.lang.NonNull;

import java.util.Locale;

/**
 * Util class for processing Key Vault property sources.
 */
public class KeyVaultPropertySourceUtils {

    /**
     * For convention, we need to support all relaxed binding format from spring, these may include:
     * <table>
     * <tr><td>Spring relaxed binding names</td></tr>
     * <tr><td>acme.my-project.person.first-name</td></tr>
     * <tr><td>acme.myProject.person.firstName</td></tr>
     * <tr><td>acme.my_project.person.first_name</td></tr>
     * <tr><td>ACME_MYPROJECT_PERSON_FIRSTNAME</td></tr>
     * </table>
     * But azure key vault only allows ^[0-9a-zA-Z-]+$ and case-insensitive, so
     * there must be some conversion between spring names and azure key vault
     * names. For example, the 4 properties stated above should be converted to
     * acme-myproject-person-firstname in key vault.
     * @param caseSensitive the case-sensitive flag.
     * @param property of secret instance.
     * @return the value of secret with given name or null.
     */
    public static String toKeyVaultSecretName(boolean caseSensitive, @NonNull String property) {
        if (!caseSensitive) {
            if (property.matches("[a-z0-9A-Z-]+")) {
                return property.toLowerCase(Locale.US);
            } else if (property.matches("[A-Z0-9_]+")) {
                return property.toLowerCase(Locale.US).replace("_", "-");
            } else {
                return property.toLowerCase(Locale.US)
                               .replace("-", "") // my-project -> myproject
                               .replace("_", "") // my_project -> myproject
                               .replace(".", "-"); // acme.myproject -> acme-myproject
            }
        } else {
            return property;
        }
    }
}
