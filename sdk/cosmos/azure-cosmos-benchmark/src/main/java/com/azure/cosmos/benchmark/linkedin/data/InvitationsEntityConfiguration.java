// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.entity.InvitationDataGenerator;
import com.azure.cosmos.benchmark.linkedin.data.entity.InvitationsCollectionAttributes;
import com.azure.cosmos.benchmark.linkedin.data.entity.InvitationsKeyGenerator;
import com.azure.cosmos.implementation.guava25.base.Preconditions;
import java.util.function.Supplier;


/**
 * Entity configuration for the Invitations use-case
 */
public class InvitationsEntityConfiguration implements EntityConfiguration {

    private final Supplier<KeyGenerator> _keyGenerator;
    private final DataGenerator _dataGenerator;
    private final CollectionAttributes _collectionAttributes;

    public InvitationsEntityConfiguration(final Configuration configuration) {
        Preconditions.checkNotNull(configuration, "The test configuration can not be null");
        _keyGenerator = () -> new InvitationsKeyGenerator(configuration.getNumberOfPreCreatedDocuments());
        _dataGenerator = new InvitationDataGenerator(_keyGenerator.get());
        _collectionAttributes = new InvitationsCollectionAttributes();
    }

    @Override
    public KeyGenerator keyGenerator() {
        return _keyGenerator.get();
    }

    @Override
    public DataGenerator dataGenerator() {
        return _dataGenerator;
    }

    @Override
    public CollectionAttributes collectionAttributes() {
        return _collectionAttributes;
    }
}
