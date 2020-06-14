// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.query;

import com.microsoft.azure.spring.data.cosmosdb.core.CosmosOperations;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosPersistentProperty;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * Cosmos query class with {@link PartTree} to parse a {@link String} into a tree or {@link PartTree.OrPart}s consisting
 * of simple {@link Part} instances in turn.
 */
public class PartTreeCosmosQuery extends AbstractCosmosQuery {

    private final PartTree tree;
    private final MappingContext<?, CosmosPersistentProperty> mappingContext;
    private final ResultProcessor processor;

    /**
     * Initialization
     * @param method CosmosQueryMethod
     * @param operations CosmosOperations
     */
    public PartTreeCosmosQuery(CosmosQueryMethod method, CosmosOperations operations) {
        super(method, operations);

        this.processor = method.getResultProcessor();
        this.tree = new PartTree(method.getName(), processor.getReturnedType().getDomainType());
        this.mappingContext = operations.getConverter().getMappingContext();
    }

    @Override
    protected DocumentQuery createQuery(CosmosParameterAccessor accessor) {
        final CosmosQueryCreator creator = new CosmosQueryCreator(tree, accessor, mappingContext);

        final DocumentQuery query = creator.createQuery();

        if (tree.isLimiting()) {
            throw new NotImplementedException("Limiting is not supported.");
        }

        return query;
    }

    @Override
    protected boolean isDeleteQuery() {
        return tree.isDelete();
    }

    @Override
    protected boolean isExistsQuery() {
        return tree.isExistsProjection();
    }
}
