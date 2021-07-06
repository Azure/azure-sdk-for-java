// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for {@link KeyVaultDataAction}.
 */
public final class KeyVaultDataAction extends ExpandableStringEnum<KeyVaultDataAction> {
    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/read/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction READ_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/read/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/write/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction WRITE_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/write/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/deletedKeys/read/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction READ_DELETED_HSM_KEY =
        fromString("Microsoft.KeyVault/managedHsm/keys/deletedKeys/read/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/deletedKeys/recover/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction RECOVER_DELETED_HSM_KEY =
        fromString("Microsoft.KeyVault/managedHsm/keys/deletedKeys/recover/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/backup/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction BACKUP_HSM_KEYS = fromString("Microsoft.KeyVault/managedHsm/keys/backup/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/restore/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction RESTORE_HSM_KEYS = fromString("Microsoft.KeyVault/managedHsm/keys/restore/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/roleAssignments/delete/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction DELETE_ROLE_ASSIGNMENT =
        fromString("Microsoft.KeyVault/managedHsm/roleAssignments/delete/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/roleAssignments/read/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction GET_ROLE_ASSIGNMENT =
        fromString("Microsoft.KeyVault/managedHsm/roleAssignments/read/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/roleAssignments/write/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction WRITE_ROLE_ASSIGNMENT =
        fromString("Microsoft.KeyVault/managedHsm/roleAssignments/write/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/roleDefinitions/read/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction READ_ROLE_DEFINITION =
        fromString("Microsoft.KeyVault/managedHsm/roleDefinitions/read/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/encrypt/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction ENCRYPT_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/encrypt/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/decrypt/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction DECRYPT_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/decrypt/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/wrap/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction WRAP_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/wrap/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/unwrap/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction UNWRAP_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/unwrap/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/sign/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction SIGN_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/sign/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/verify/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction VERIFY_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/verify/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/create for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction CREATE_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/create");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/delete for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction DELETE_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/delete");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/export/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction EXPORT_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/export/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/import/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction IMPORT_HSM_KEY = fromString("Microsoft.KeyVault/managedHsm/keys/import/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/keys/deletedKeys/delete for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction PURGE_DELETED_HSM_KEY =
        fromString("Microsoft.KeyVault/managedHsm/keys/deletedKeys/delete");

    /**
     * Static value Microsoft.KeyVault/managedHsm/securitydomain/download/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction DOWNLOAD_HSM_SECURITY_DOMAIN =
        fromString("Microsoft.KeyVault/managedHsm/securitydomain/download/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/securitydomain/upload/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction UPLOAD_HSM_SECURITY_DOMAIN =
        fromString("Microsoft.KeyVault/managedHsm/securitydomain/upload/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/securitydomain/upload/read for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction READ_HSM_SECURITY_DOMAIN_STATUS =
        fromString("Microsoft.KeyVault/managedHsm/securitydomain/upload/read");

    /**
     * Static value Microsoft.KeyVault/managedHsm/securitydomain/transferkey/read for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction READ_HSM_SECURITY_DOMAIN_TRANSFER_KEY =
        fromString("Microsoft.KeyVault/managedHsm/securitydomain/transferkey/read");

    /**
     * Static value Microsoft.KeyVault/managedHsm/backup/start/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction START_HSM_BACKUP = fromString("Microsoft.KeyVault/managedHsm/backup/start/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/restore/start/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction START_HSM_RESTORE = fromString("Microsoft.KeyVault/managedHsm/restore/start/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/backup/status/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction READ_HSM_BACKUP_STATUS =
        fromString("Microsoft.KeyVault/managedHsm/backup/status/action");

    /**
     * Static value Microsoft.KeyVault/managedHsm/restore/status/action for {@link KeyVaultDataAction}.
     */
    public static final KeyVaultDataAction READ_HSM_RESTORE_STATUS =
        fromString("Microsoft.KeyVault/managedHsm/restore/status/action");

    /**
     * Creates or finds a {@link KeyVaultDataAction} from its string representation.
     *
     * @param name A name to look for.
     *
     * @return The corresponding {@link KeyVaultDataAction}.
     */
    @JsonCreator
    public static KeyVaultDataAction fromString(String name) {
        return fromString(name, KeyVaultDataAction.class);
    }
}
