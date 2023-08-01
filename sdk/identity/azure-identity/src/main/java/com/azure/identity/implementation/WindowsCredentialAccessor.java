// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.win32.W32APIOptions;

import java.nio.charset.StandardCharsets;

/**
 * This class allows access to Windows credentials via JNA.
 */
public class WindowsCredentialAccessor {
    private static final ClientLogger LOGGER = new ClientLogger(WindowsCredentialAccessor.class);
    private WindowsCredentialApi accessor;
    private String serviceName;
    private String accountName;

    /**
     * Creates an instance of the {@link WindowsCredentialAccessor}
     *
     * <p> The specified {@code serviceName} and {@code accountName} are used to form target credential name. </p>
     *
     * @param serviceName the service name to lookup.
     * @param accountName the account name to lookup.
     */
    public WindowsCredentialAccessor(String serviceName, String accountName) {
        accessor = Native.load("Advapi32", WindowsCredentialApi.class, W32APIOptions.UNICODE_OPTIONS);
        this.serviceName = serviceName;
        this.accountName = accountName;
    }

    /**
     * Reads the credential from windows credential store.
     * @return the credential.
     */
    public String read() {
        WindowsCredentialApi.PCREDENTIAL pcredential = new WindowsCredentialApi.PCREDENTIAL();
        try {
            boolean readOk = accessor.CredRead(String.format("%s/%s", serviceName, accountName),
                    WindowsCredentialApi.CRED_TYPE_GENERIC, 0, pcredential);

            if (!readOk) {
                int rc = Kernel32.INSTANCE.GetLastError();
                String errMsg = Kernel32Util.formatMessage(rc);
                throw LOGGER.logExceptionAsError(new RuntimeException(errMsg));
            }
            final WindowsCredentialApi.CREDENTIAL credential =
                    new WindowsCredentialApi.CREDENTIAL(pcredential.credential);

            byte[] secretBytes = credential.CredentialBlob.getByteArray(0, credential.CredentialBlobSize);
            return new String(secretBytes, StandardCharsets.UTF_8);
        } catch (LastErrorException e) {
            int errorCode = e.getErrorCode();
            String errMsg = Kernel32Util.formatMessage(errorCode);
            throw LOGGER.logExceptionAsError(new RuntimeException(errMsg));

        } finally {
            if (pcredential.credential != null) {
                accessor.CredFree(pcredential.credential);
            }
        }
    }

}
