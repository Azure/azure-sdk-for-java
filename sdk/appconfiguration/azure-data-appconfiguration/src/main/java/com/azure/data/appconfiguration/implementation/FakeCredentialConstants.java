// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

/**
 * Never explore this class publicly. It should be only use in internal to gather the fake credential or keyword that
 * failed the CredScan.
 */
public final class FakeCredentialConstants {
    /**
     * 'secret=' keyword placeholder.
     */
    public static final String SECRET_PLACEHOLDER = "secret=";
}
