package com.azure.identity.implementation;

import com.microsoft.aad.msal4jextensions.persistence.linux.ISecurityLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class LinuxKeyRingAccessor {
    private String keyringSchemaName;
    private String attributeKey1;
    private String attributeValue1;
    private String attributeKey2;
    private String attributeValue2;
    private Pointer libSecretSchema;

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
            (Pointer)null, error, this.attributeKey1, attributeValue1, this.attributeKey2
            , attributeValue2, (Pointer)null);
        if (error[0] != Pointer.NULL) {
            GError err = new GError(error[0]);
            throw new RuntimeException("An error while reading secret from keyring, domain:"
                + err.domain + " code:" + err.code + " message:" + err.message);
        } else {
            if (secret != null && !secret.isEmpty()) {
                data = secret.getBytes();
            }
            return data;
        }
    }

    public byte[] read() {
        return this.read(this.attributeValue1, this.attributeValue2);
    }

    private Pointer getLibSecretSchema() {
        if (this.libSecretSchema == Pointer.NULL) {
            this.libSecretSchema = ISecurityLibrary.library.secret_schema_new(this.keyringSchemaName,
                0, this.attributeKey1, 0, this.attributeKey2, 0, (Pointer)null);
            if (this.libSecretSchema == Pointer.NULL) {
                throw new RuntimeException("Failed to create libSecret schema " + this.keyringSchemaName);
            }
        }

        return this.libSecretSchema;
    }

    class GError extends Structure {
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

