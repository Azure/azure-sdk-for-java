// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.core.generator;

import com.azure.data.cosmos.SqlParameterList;
import com.azure.data.cosmos.SqlQuerySpec;
import com.microsoft.azure.spring.data.cosmosdb.core.query.Criteria;
import com.microsoft.azure.spring.data.cosmosdb.core.query.CriteriaType;
import com.microsoft.azure.spring.data.cosmosdb.core.query.DocumentQuery;
import com.microsoft.azure.spring.data.cosmosdb.exception.IllegalQueryException;
import org.javatuples.Pair;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.azure.spring.data.cosmosdb.core.convert.MappingCosmosConverter.toCosmosDbValue;

public abstract class AbstractQueryGenerator {

    protected AbstractQueryGenerator() {
    }

    private String generateQueryParameter(@NonNull String subject) {
        return subject.replaceAll("\\.", "_"); // user.name is not valid sql parameter identifier.
    }

    private String generateUnaryQuery(@NonNull Criteria criteria) {
        Assert.isTrue(criteria.getSubjectValues().isEmpty(), "Unary criteria should have no one subject value");
        Assert.isTrue(CriteriaType.isUnary(criteria.getType()), "Criteria type should be unary operation");
        final String subject = criteria.getSubject();

        if (CriteriaType.isFunction(criteria.getType())) {
            return String.format("%s(r.%s)", criteria.getType().getSqlKeyword(), subject);
        } else {
            return String.format("r.%s %s", subject, criteria.getType().getSqlKeyword());
        }
    }

    private String generateBinaryQuery(@NonNull Criteria criteria, @NonNull List<Pair<String, Object>> parameters) {
        Assert.isTrue(criteria.getSubjectValues().size() == 1, "Binary criteria should have only one subject value");
        Assert.isTrue(CriteriaType.isBinary(criteria.getType()), "Criteria type should be binary operation");

        final String subject = criteria.getSubject();
        final Object subjectValue = toCosmosDbValue(criteria.getSubjectValues().get(0));
        final String parameter = generateQueryParameter(subject);

        parameters.add(Pair.with(parameter, subjectValue));

        if (CriteriaType.isFunction(criteria.getType())) {
            return String.format("%s(r.%s, @%s)", criteria.getType().getSqlKeyword(), subject, parameter);
        } else {
            return String.format("r.%s %s @%s", subject, criteria.getType().getSqlKeyword(), parameter);
        }
    }

    private String generateBetween(@NonNull Criteria criteria, @NonNull List<Pair<String, Object>> parameters) {
        final String subject = criteria.getSubject();
        final Object value1 = toCosmosDbValue(criteria.getSubjectValues().get(0));
        final Object value2 = toCosmosDbValue(criteria.getSubjectValues().get(1));
        final String subject1 = subject + "start";
        final String subject2 = subject + "end";
        final String parameter1 = generateQueryParameter(subject1);
        final String parameter2 = generateQueryParameter(subject2);
        final String keyword = criteria.getType().getSqlKeyword();

        parameters.add(Pair.with(parameter1, value1));
        parameters.add(Pair.with(parameter2, value2));

        return String.format("(r.%s %s @%s AND @%s)", subject, keyword, parameter1, parameter2);
    }

    private String generateClosedQuery(@NonNull String left, @NonNull String right, CriteriaType type) {
        Assert.isTrue(CriteriaType.isClosed(type) && CriteriaType.isBinary(type),
                "Criteria type should be binary and closure operation");

        return String.join(" ", left, type.getSqlKeyword(), right);
    }

    @SuppressWarnings("unchecked")
    private String generateInQuery(Criteria criteria) {
        Assert.isTrue(criteria.getSubjectValues().size() == 1, "Criteria should have only one subject value");
        if (!(criteria.getSubjectValues().get(0) instanceof Collection)) {
            throw new IllegalQueryException("IN keyword requires Collection type in parameters");
        }
        final List<String> inRangeValues = new ArrayList<>();
        final Collection values = (Collection) criteria.getSubjectValues().get(0);

        values.forEach(o -> {
            if (o instanceof Integer || o instanceof Long) {
                inRangeValues.add(String.format("%d", o));
            } else if (o instanceof String) {
                inRangeValues.add(String.format("'%s'", (String) o));
            } else if (o instanceof Boolean) {
                inRangeValues.add(String.format("%b", (Boolean) o));
            } else {
                throw new IllegalQueryException("IN keyword Range only support Number and String type.");
            }
        });

        final String inRange = String.join(",", inRangeValues);
        return String.format("r.%s %s (%s)", criteria.getSubject(), criteria.getType().getSqlKeyword(), inRange);
    }

    private String generateQueryBody(@NonNull Criteria criteria, @NonNull List<Pair<String, Object>> parameters) {
        final CriteriaType type = criteria.getType();

        switch (type) {
            case ALL:
                return "";
            case IN:
            case NOT_IN:
                return generateInQuery(criteria);
            case BETWEEN:
                return generateBetween(criteria, parameters);
            case IS_NULL:
            case IS_NOT_NULL:
            case FALSE:
            case TRUE:
                return generateUnaryQuery(criteria);
            case IS_EQUAL:
            case NOT:
            case BEFORE:
            case AFTER:
            case LESS_THAN:
            case LESS_THAN_EQUAL:
            case GREATER_THAN:
            case GREATER_THAN_EQUAL:
            case CONTAINING:
            case ENDS_WITH:
            case STARTS_WITH:
                return generateBinaryQuery(criteria, parameters);
            case AND:
            case OR:
                Assert.isTrue(criteria.getSubCriteria().size() == 2, "criteria should have two SubCriteria");

                final String left = generateQueryBody(criteria.getSubCriteria().get(0), parameters);
                final String right = generateQueryBody(criteria.getSubCriteria().get(1), parameters);

                return generateClosedQuery(left, right, type);
            default:
                throw new UnsupportedOperationException("unsupported Criteria type: " + type);
        }
    }

    /**
     * Generate a query body for interface QuerySpecGenerator.
     * The query body compose of Sql query String and its' parameters.
     * The parameters organized as a list of Pair, for each pair compose parameter name and value.
     *
     * @param query the representation for query method.
     * @return A pair tuple compose of Sql query.
     */
    @NonNull
    private Pair<String, List<Pair<String, Object>>> generateQueryBody(@NonNull DocumentQuery query) {
        final List<Pair<String, Object>> parameters = new ArrayList<>();
        String queryString = this.generateQueryBody(query.getCriteria(), parameters);

        if (StringUtils.hasText(queryString)) {
            queryString = String.join(" ", "WHERE", queryString);
        }

        return Pair.with(queryString, parameters);
    }

    private String getParameter(@NonNull Sort.Order order) {
        Assert.isTrue(!order.isIgnoreCase(), "Ignore case is not supported");

        final String direction = order.isDescending() ? "DESC" : "ASC";

        return String.format("r.%s %s", order.getProperty(), direction);
    }

    private String generateQuerySort(@NonNull Sort sort) {
        if (sort.isUnsorted()) {
            return "";
        }

        final String queryTail = "ORDER BY";
        final List<String> subjects = sort.stream().map(this::getParameter).collect(Collectors.toList());

        return queryTail + " " + String.join(",", subjects);
    }

    @NonNull
    private String generateQueryTail(@NonNull DocumentQuery query) {
        final List<String> queryTails = new ArrayList<>();

        queryTails.add(generateQuerySort(query.getSort()));

        return String.join(" ", queryTails.stream().filter(StringUtils::hasText).collect(Collectors.toList()));
    }


    protected SqlQuerySpec generateCosmosQuery(@NonNull DocumentQuery query,
                                                                            @NonNull String queryHead) {
        final Pair<String, List<Pair<String, Object>>> queryBody = generateQueryBody(query);
        final String queryString = String.join(" ", queryHead, queryBody.getValue0(), generateQueryTail(query));
        final List<Pair<String, Object>> parameters = queryBody.getValue1();
        final SqlParameterList sqlParameters =
                new SqlParameterList();

        sqlParameters.addAll(
                parameters.stream()
                        .map(p -> new com.azure.data.cosmos.SqlParameter("@" + p.getValue0(),
                                toCosmosDbValue(p.getValue1())))
                        .collect(Collectors.toList())
        );

        return new SqlQuerySpec(queryString, sqlParameters);
    }
}
