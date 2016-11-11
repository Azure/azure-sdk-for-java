/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.TransparentDataEncryption;
import com.microsoft.azure.management.sql.TransparentDataEncryptionActivity;
import com.microsoft.azure.management.sql.TransparentDataEncryptionStates;

/**
 * Implementation of SqlDatabase.TransparentDataEncryptions, which enables managing the Data Encryption settings.
 */
class TransparentDataEncryptionsImpl implements SqlDatabase.TransparentDataEncryptions {
    private final String resourceGroupName;
    private final String sqlServerName;
    private final String databaseName;
    private final DatabasesInner databasesInner;

    TransparentDataEncryptionsImpl(String resourceGroupName, String sqlServerName, String databaseName, DatabasesInner databasesInner) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.databaseName = databaseName;
        this.databasesInner = databasesInner;
    }

    @Override
    public TransparentDataEncryption get() {
        return new TransparentDataEncryptionImpl(
                this.databasesInner.getTransparentDataEncryptionConfiguration(
                        this.resourceGroupName,
                        this.sqlServerName,
                        this.databaseName));
    }

    @Override
    public TransparentDataEncryption update(TransparentDataEncryptionStates transparentDataEncryptionState) {
        TransparentDataEncryptionInner transparentDataEncryptionInner = new TransparentDataEncryptionInner();
        transparentDataEncryptionInner.withStatus(transparentDataEncryptionState);

        return new TransparentDataEncryptionImpl(
                this.databasesInner.createOrUpdateTransparentDataEncryptionConfiguration(
                        this.resourceGroupName,
                        this.sqlServerName,
                        this.databaseName,
                        transparentDataEncryptionInner));
    }

    @Override
    public PagedList<TransparentDataEncryptionActivity> listActivity() {
        PagedListConverter<TransparentDataEncryptionActivityInner, TransparentDataEncryptionActivity> converter
                = new PagedListConverter<TransparentDataEncryptionActivityInner, TransparentDataEncryptionActivity>() {
            @Override
            public TransparentDataEncryptionActivity typeConvert(TransparentDataEncryptionActivityInner transparentDataEncryptionActivityInner) {

                return new TransparentDataEncryptionActivityImpl(transparentDataEncryptionActivityInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.databasesInner.listTransparentDataEncryptionActivity(
                        this.resourceGroupName,
                        this.sqlServerName,
                        this.databaseName)));
    }

}
