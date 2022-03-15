// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.microsoft.aad.msal4jextensions.persistence.linux.ISecurityLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * This class exposes functions from Key Ring on Linux platform
 * via JNA.
 */
public class LinuxKeyRingAccessor {
    private static final ClientLogger LOGGER = new ClientLogger(LinuxKeyRingAccessor.class);
    private String keyringSchemaName;
    private String attributeKey1;
    private String attributeValue1;
    private String attributeKey2;
    private String attributeValue2;
    private Pointer libSecretSchema;

    /**
     * Creates an instance of the {@link LinuxKeyRingAccessor} with specified attributes and schema.
     *
     * @param keyringSchemaName the key ring schema to access.
     * @param attributeKey1 the key value of the attribute to lookup
     * @param attributeValue1 the value of the attribute to lookup
     * @param attributeKey2 the key value of the attribute to lookup
     * @param attributeValue2 the value of the attribute to lookup
     */
    public LinuxKeyRingAccessor(String keyringSchemaName, String attributeKey1,
                                String attributeValue1, String attributeKey2, String attributeValue2) {
        this.keyringSchemaName = keyringSchemaName;
        this.attributeKey1 = attributeKey1;
        this.attributeValue1 = attributeValue1;
        this.attributeKey2 = attributeKey2;
        this.attributeValue2 = attributeValue2;
    }


    private byte[] read(String attributeValue1, String attributeValue2) {
        byte[] data = null;
        Pointer[] error = new Pointer[1];
        String secret = ISecurityLibrary.library.secret_password_lookup_sync(this.getLibSecretSchema(),
            (Pointer) null, error, this.attributeKey1, attributeValue1, this.attributeKey2,
            attributeValue2, (Pointer) null);
        if (error[0] != Pointer.NULL) {
            GError err = new GError(error[0]);
            throw LOGGER.logExceptionAsError(new RuntimeException("An error while reading secret from keyring, domain:"
                + err.domain + " code:" + err.code + " message:" + err.message));
        } else {
            if (secret != null && !secret.isEmpty()) {
                data = secret.getBytes(StandardCharsets.UTF_8);
            }
            return data;
        }
    }

    /**
     * Read the value of the configured secret attributes.
     * @return the byte array holding the secret.
     */
    public byte[] read() {
        return this.read(this.attributeValue1, this.attributeValue2);
    }

    private Pointer getLibSecretSchema() {
        if (this.libSecretSchema == Pointer.NULL) {
            this.libSecretSchema = ISecurityLibrary.library.secret_schema_new(this.keyringSchemaName,
                0, this.attributeKey1, 0, this.attributeKey2, 0, (Pointer) null);
            if (this.libSecretSchema == Pointer.NULL) {
                throw LOGGER.logExceptionAsError(
                    new RuntimeException("Failed to create libSecret schema " + this.keyringSchemaName));
            }
        }

        return this.libSecretSchema;
    }

    static class GError extends Structure {
        int domain;
        int code;
        String message;

        GError(Pointer p) {
            super(p);
            this.read();
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("domain", "code", "message");
        }
    }
}

