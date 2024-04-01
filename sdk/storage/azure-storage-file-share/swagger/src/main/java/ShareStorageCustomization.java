// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Customization class for File Share Storage.
 */
public class ShareStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.storage.file.share.models");

        ClassCustomization shareTokenIntent = models.getClass("ShareTokenIntent");
        shareTokenIntent.getJavadoc().setDescription("The request intent specifies requests that are intended for " +
            "backup/admin type operations, meaning that all file/directory ACLs are bypassed and full permissions are " +
            "granted. User must also have required RBAC permission.");

        models.getClass("AccessRight").rename("ShareFileHandleAccessRights");
    }
}
