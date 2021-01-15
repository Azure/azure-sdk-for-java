// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.query;

import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.core.mapping.CosmosPersistentProperty;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * TODO: String based query, based on how cosmosDB provides.
 *  StringCosmosQuery class,
 *  How to bind values to the query. if CosmosDb already has binding capability, if not we would have to do it here in
 *  some creative way.query creator are associated with part tree queries,
 */
public class CosmosQueryCreator extends AbstractQueryCreator<CosmosQuery, Criteria> {

    private final MappingContext<?, CosmosPersistentProperty> mappingContext;

    /**
     * Creates a new {@link CosmosQueryCreator}. {@link CosmosParameterAccessor} is used to hand actual
     * parameter values into the callback methods as well as to apply dynamic sorting via a {@link Sort} parameter.
     *
     * @param tree must not be {@literal null}.
     * @param accessor must not be {@literal null}.
     * @param mappingContext must not be {@literal null}.
     */
    public CosmosQueryCreator(PartTree tree, CosmosParameterAccessor accessor,
                              MappingContext<?, CosmosPersistentProperty> mappingContext) {
        super(tree, accessor);

        this.mappingContext = mappingContext;
    }

    private String getSubject(@NonNull Part part) {
        String subject = mappingContext.getPersistentPropertyPath(part.getProperty()).toDotPath();
        final Class<?> domainType = part.getProperty().getOwningType().getType();

        @SuppressWarnings("unchecked") final CosmosEntityInformation<?, ?> information =
                new CosmosEntityInformation<>(domainType);

        if (information.getIdField().getName().equals(subject)) {
            subject = Constants.ID_PROPERTY_NAME;
        }

        return subject;
    }

    @Override // Note (panli): side effect here, this method will change the iterator status of parameters.
    protected Criteria create(Part part, Iterator<Object> parameters) {
        final Part.Type type = part.getType();
        final String subject = getSubject(part);
        final List<Object> values = new ArrayList<>();

        if (CriteriaType.isPartTypeUnSupported(type)) {
            throw new UnsupportedOperationException("Unsupported keyword: "
                + type);
        }

        for (int i = 0; i < part.getNumberOfArguments(); i++) {
            Assert.isTrue(parameters.hasNext(), "should not reach the end of iterator");
            values.add(parameters.next());
        }

        return Criteria.getInstance(CriteriaType.toCriteriaType(type), subject, values, part.shouldIgnoreCase());
    }

    @Override
    protected Criteria and(@NonNull Part part, @NonNull Criteria base, @NonNull Iterator<Object> parameters) {
        final Criteria right = this.create(part, parameters);

        return Criteria.getInstance(CriteriaType.AND, base, right);
    }

    @Override
    protected Criteria or(@NonNull Criteria base, @NonNull Criteria criteria) {
        return Criteria.getInstance(CriteriaType.OR, base, criteria);
    }

    @Override
    protected CosmosQuery complete(@NonNull Criteria criteria, @NonNull Sort sort) {
        return new CosmosQuery(criteria).with(sort);
    }
}
