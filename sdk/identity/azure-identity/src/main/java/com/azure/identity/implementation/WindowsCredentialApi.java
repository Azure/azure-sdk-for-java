// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.sun.jna.Pointer;
import com.sun.jna.Memory;
import com.sun.jna.LastErrorException;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class exposes functions from credential manager on Windows platform
 * via JNA.
 */
public interface WindowsCredentialApi extends StdCallLibrary {

    /**
     * Type of Credential
     */
    int CRED_TYPE_GENERIC = 1;

    /**
     * Credential attributes
     *
     * typedef struct _CREDENTIAL_ATTRIBUTE {
     *   LPTSTR Keyword;
     *   DWORD  Flags;
     *   DWORD  ValueSize;
     *   LPBYTE Value;
     * } CREDENTIAL_ATTRIBUTE, *PCREDENTIAL_ATTRIBUTE;
     *
     */
    class CREDENTIAL_ATTRIBUTE extends Structure {

        public static class ByReference extends CREDENTIAL_ATTRIBUTE implements Structure.ByReference {
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("Keyword", "Flags", "ValueSize", "Value");
        }

        /**
         *    Name of the application-specific attribute. Names should be of the form "CompanyName_Name".
         *    This member cannot be longer than CRED_MAX_STRING_LENGTH (256) characters.
         */
        public String Keyword;

        /**
         *   Identifies characteristics of the credential attribute. This member is reserved and should be originally
         *   initialized as zero and not otherwise altered to permit future enhancement.
         */
        public int Flags;

        /**
         *   Length of Value in bytes. This member cannot be larger than CRED_MAX_VALUE_SIZE (256).
         */
        public int ValueSize;

        /**
         *   Data associated with the attribute. By convention, if Value is a text string, then Value should not
         *   include the trailing zero character and should be in UNICODE.
         *
         *   Credentials are expected to be portable. The application should take care to ensure that the data in
         *   value is portable. It is the responsibility of the application to define the byte-endian and alignment
         *   of the data in Value.
         */
        public Pointer Value;
    }

    /**
     * The CREDENTIAL structure contains an individual credential
     *
     * typedef struct _CREDENTIAL {
     *   DWORD                 Flags;
     *   DWORD                 Type;
     *   LPTSTR                TargetName;
     *   LPTSTR                Comment;
     *   FILETIME              LastWritten;
     *   DWORD                 CredentialBlobSize;
     *   LPBYTE                CredentialBlob;
     *   DWORD                 Persist;
     *   DWORD                 AttributeCount;
     *   PCREDENTIAL_ATTRIBUTE Attributes;
     *   LPTSTR                TargetAlias;
     *   LPTSTR                UserName;
     * } CREDENTIAL, *PCREDENTIAL;
     */
    class CREDENTIAL extends Structure {

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("Flags", "Type", "TargetName", "Comment", "LastWritten", "CredentialBlobSize",
                "CredentialBlob", "Persist", "AttributeCount", "Attributes", "TargetAlias", "UserName");
        }

        public CREDENTIAL() {
            super();
        }

        public CREDENTIAL(final int size) {
            super(new Memory(size));
        }

        public CREDENTIAL(Pointer memory) {
            super(memory);
            read();
        }

        /**
         *   A bit member that identifies characteristics of the credential. Undefined bits should be initialized
         *   as zero and not otherwise altered to permit future enhancement.
         *
         *   See MSDN doc for all possible flags
         */
        public int Flags;

        /**
         *   The type of the credential. This member cannot be changed after the credential is created.
         *
         *   See MSDN doc for all possible types
         */
        public int Type;

        /**
         *   The name of the credential. The TargetName and Type members uniquely identify the credential.
         *   This member cannot be changed after the credential is created. Instead, the credential with the old
         *   name should be deleted and the credential with the new name created.
         *
         *   See MSDN doc for additional requirement
         */
        public String TargetName;

        /**
         *   A string comment from the user that describes this credential. This member cannot be longer than
         *   CRED_MAX_STRING_LENGTH (256) characters.
         */
        public String Comment;

        /**
         *   The time, in Coordinated Universal Time (Greenwich Mean Time), of the last modification of the credential.
         *   For write operations, the value of this member is ignored.
         */
        public WinBase.FILETIME LastWritten;

        /**
         *   The size, in bytes, of the CredentialBlob member. This member cannot be larger than
         *   CRED_MAX_CREDENTIAL_BLOB_SIZE (512) bytes.
         */
        public int CredentialBlobSize;

        /**
         *   Secret data for the credential. The CredentialBlob member can be both read and written.
         *   If the Type member is CRED_TYPE_DOMAIN_PASSWORD, this member contains the plaintext Unicode password
         *   for UserName. The CredentialBlob and CredentialBlobSize members do not include a trailing zero character.
         *   Also, for CRED_TYPE_DOMAIN_PASSWORD, this member can only be read by the authentication packages.
         *
         *   If the Type member is CRED_TYPE_DOMAIN_CERTIFICATE, this member contains the clear test
         *   Unicode PIN for UserName. The CredentialBlob and CredentialBlobSize members do not include a trailing
         *   zero character. Also, this member can only be read by the authentication packages.
         *
         *   If the Type member is CRED_TYPE_GENERIC, this member is defined by the application.
         *   Credentials are expected to be portable. Applications should ensure that the data in CredentialBlob is
         *   portable. The application defines the byte-endian and alignment of the data in CredentialBlob.
         */
        public Pointer CredentialBlob;

        /**
         *   Defines the persistence of this credential. This member can be read and written.
         *
         *   See MSDN doc for all possible values
         */
        public int Persist;

        /**
         *   The number of application-defined attributes to be associated with the credential. This member can be
         *   read and written. Its value cannot be greater than CRED_MAX_ATTRIBUTES (64).
         */
        public int AttributeCount;

        /**
         *   Application-defined attributes that are associated with the credential. This member can be read
         *   and written.
         */
        public CREDENTIAL_ATTRIBUTE.ByReference Attributes;

        /**
         *   Alias for the TargetName member. This member can be read and written. It cannot be longer than
         *   CRED_MAX_STRING_LENGTH (256) characters.
         *
         *   If the credential Type is CRED_TYPE_GENERIC, this member can be non-NULL, but the credential manager
         *   ignores the member.
         */
        public String TargetAlias;

        /**
         *   The user name of the account used to connect to TargetName.
         *   If the credential Type is CRED_TYPE_DOMAIN_PASSWORD, this member can be either a DomainName\UserName
         *   or a UPN.
         *
         *   If the credential Type is CRED_TYPE_DOMAIN_CERTIFICATE, this member must be a marshaled certificate
         *   reference created by calling CredMarshalCredential with a CertCredential.
         *
         *   If the credential Type is CRED_TYPE_GENERIC, this member can be non-NULL, but the credential manager
         *   ignores the member.
         *
         *   This member cannot be longer than CRED_MAX_USERNAME_LENGTH (513) characters.
         */
        public String UserName;
    }

    /**
     *  Pointer to {@see CREDENTIAL} struct
     */
    class PCREDENTIAL extends Structure {

        @Override
        protected List<String> getFieldOrder() {
            return Collections.singletonList("credential");
        }

        public PCREDENTIAL() {
            super();
        }

        public PCREDENTIAL(byte[] data) {
            super(new Memory(data.length));
            getPointer().write(0, data, 0, data.length);
            read();
        }

        public PCREDENTIAL(Pointer memory) {
            super(memory);
            read();
        }

        public Pointer credential;
    }

    /**
     * The CredRead function reads a credential from the user's credential set.
     *
     * The credential set used is the one associated with the logon session of the current token.
     * The token must not have the user's SID disabled.
     *
     * @param targetName
     *      String that contains the name of the credential to read.
     * @param type
     *      Type of the credential to read. Type must be one of the CRED_TYPE_* defined types.
     * @param flags
     *      Currently reserved and must be zero.
     * @param pcredential
     *      Out - Pointer to a single allocated block buffer to return the credential.
     *      Any pointers contained within the buffer are pointers to locations within this single allocated block.
     *      The single returned buffer must be freed by calling <code>CredFree</code>.
     *
     * @return
     *      True if CredRead succeeded, false otherwise
     *
     * @throws LastErrorException
     *      GetLastError
     */
    boolean CredRead(String targetName, int type, int flags, PCREDENTIAL pcredential) throws LastErrorException;

    /**
     * The CredFree function frees a buffer returned by any of the credentials management functions.
     *
     * @param credential
     *      Pointer to CREDENTIAL to be freed
     *
     * @throws LastErrorException
     *      GetLastError
     */
    void CredFree(Pointer credential) throws LastErrorException;
}
