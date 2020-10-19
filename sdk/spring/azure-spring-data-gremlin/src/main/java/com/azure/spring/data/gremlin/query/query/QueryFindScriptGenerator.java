// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query.query;

import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import com.azure.spring.data.gremlin.query.criteria.Criteria;
import com.azure.spring.data.gremlin.query.criteria.CriteriaType;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.azure.spring.data.gremlin.common.Constants.GREMLIN_PRIMITIVE_WHERE;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateHas;
import static com.azure.spring.data.gremlin.conversion.script.GremlinScriptLiteralHelper.generateHasId;

public class QueryFindScriptGenerator<T> implements QueryScriptGenerator {

    private final GremlinSource<T> source;

    public QueryFindScriptGenerator(@NonNull GremlinSource<T> source) {
        this.source = source;
    }

    private String getCriteriaSubject(@NonNull Criteria criteria) {
        String subject = criteria.getSubject();

        if (subject.equals(this.source.getIdField().getName())) {
            subject = Constants.PROPERTY_ID; // If subject is @Id/id field, use id property in database.
        }

        return subject;
    }

    private String generateIsEqual(@NonNull Criteria criteria) {
        final String subject = getCriteriaSubject(criteria);

        if (subject.equals(Constants.PROPERTY_ID)) {
            return String.format(GREMLIN_PRIMITIVE_WHERE, generateHasId(criteria.getSubValues().get(0)));
        } else {
            return String.format(GREMLIN_PRIMITIVE_WHERE, generateHas(subject, criteria.getSubValues().get(0)));
        }
    }

    /**
     * Generate script with only one subject and no subValue, like findByActiveExists().
     *
     * @param criteria given query represent a query subject
     * @return simple script with keyword from criteria type
     */
    private String generateEmptyScript(@NonNull Criteria criteria) {
        final String subject = this.getCriteriaSubject(criteria);
        final String has = generateHas(subject, true);

        return String.format(GREMLIN_PRIMITIVE_WHERE, has);
    }

    /**
     * Generate script with only one subject and only one subValue, like findByCreateAtBefore(Date start).
     *
     * @param criteria given query represent a query subject
     * @return simple script with keyword from criteria type
     */
    private String generateSingleScript(@NonNull Criteria criteria) {
        final CriteriaType type = criteria.getType();
        final String subject = this.getCriteriaSubject(criteria);
        final long milliSeconds = GremlinUtils.timeToMilliSeconds(criteria.getSubValues().get(0));

        final String values = String.format(Constants.GREMLIN_PRIMITIVE_VALUES, subject);
        final String query = String.format(CriteriaType.criteriaTypeToGremlin(type), milliSeconds);
        final String content = String.join(Constants.GREMLIN_PRIMITIVE_INVOKE, values, query);

        return String.format(GREMLIN_PRIMITIVE_WHERE, content);
    }

    /**
     * Generate script with only one subject and two subValue, like findByCreateAtBetween(Date start, Date end).
     *
     * @param criteria given query represent a query subject
     * @return simple script with keyword from criteria type
     */
    private String generateDoubleScript(Criteria criteria) {
        final CriteriaType type = criteria.getType();
        final String subject = this.getCriteriaSubject(criteria);
        final long start = GremlinUtils.toPrimitiveLong(criteria.getSubValues().get(0));
        final long end = GremlinUtils.toPrimitiveLong(criteria.getSubValues().get(1));

        final String values = String.format(Constants.GREMLIN_PRIMITIVE_VALUES, subject);
        final String query = String.format(CriteriaType.criteriaTypeToGremlin(type), start, end);
        final String content = String.join(Constants.GREMLIN_PRIMITIVE_INVOKE, values, query);

        return String.format(GREMLIN_PRIMITIVE_WHERE, content);
    }

    /**
     * Generate script combined by AND/OR keyword.
     *
     * @param left Sub script on left
     * @param right Sub script on right
     * @param type Should be AND/OR
     * @return Combined script with AND/OR
     */
    private String generateCombinedScript(@NonNull String left, @NonNull String right, CriteriaType type) {
        final String operation = CriteriaType.criteriaTypeToGremlin(type);
        final String content = String.join(Constants.GREMLIN_PRIMITIVE_INVOKE, left, operation, right);

        return String.format(GREMLIN_PRIMITIVE_WHERE, content);
    }


    private String generateScriptTraversal(@NonNull Criteria criteria) {
        final CriteriaType type = criteria.getType();

        switch (type) {
            case IS_EQUAL:
                return this.generateIsEqual(criteria);
            case AND:
            case OR:
                final String left = this.generateScriptTraversal(criteria.getSubCriteria().get(0));
                final String right = this.generateScriptTraversal(criteria.getSubCriteria().get(1));

                return this.generateCombinedScript(left, right, type);
            case AFTER:
            case BEFORE:
                return this.generateSingleScript(criteria);
            case BETWEEN:
                return this.generateDoubleScript(criteria);
            case EXISTS:
                return this.generateEmptyScript(criteria);
            default:
                throw new UnsupportedOperationException("unsupported Criteria type");
        }
    }

    private List<String> generateScript(@NonNull GremlinQuery query) {
        final Criteria criteria = query.getCriteria();
        final List<String> scriptList = new ArrayList<>();

        scriptList.add(Constants.GREMLIN_PRIMITIVE_GRAPH);

        if (this.source instanceof GremlinSourceVertex) {
            scriptList.add(Constants.GREMLIN_PRIMITIVE_VERTEX_ALL);
        } else if (this.source instanceof GremlinSourceEdge) {
            scriptList.add(Constants.GREMLIN_PRIMITIVE_EDGE_ALL);
        } else {
            throw new UnsupportedOperationException("Cannot generate script from graph entity");
        }

        scriptList.add(GremlinScriptLiteralHelper.generateHasLabel(this.source.getLabel()));
        scriptList.add(this.generateScriptTraversal(criteria));

        return scriptList;
    }

    @Override
    public List<String> generate(@NonNull GremlinQuery query) {
        final List<String> scriptList = new ArrayList<>(this.generateScript(query));
        return Collections.singletonList(String.join(Constants.GREMLIN_PRIMITIVE_INVOKE, scriptList));
    }
}

