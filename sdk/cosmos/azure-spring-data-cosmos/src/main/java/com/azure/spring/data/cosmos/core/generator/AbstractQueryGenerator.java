// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.generator;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.query.CosmosQuery;
import com.azure.spring.data.cosmos.core.query.Criteria;
import com.azure.spring.data.cosmos.core.query.CriteriaType;
import com.azure.spring.data.cosmos.exception.IllegalQueryException;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter.toCosmosDbValue;

/**
 * Base class for generating sql query
 */
public abstract class AbstractQueryGenerator {

    protected AbstractQueryGenerator() {
    }

    private String generateQueryParameter(@NonNull String subject, int counter) {
        // user.name, user['name'] or user["first name"] are not valid sql parameter identifiers.
        return subject.replaceAll("[^a-zA-Z\\d]", "_") + counter;
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

    private String generateBinaryQuery(@NonNull Criteria criteria, @NonNull List<Pair<String, Object>> parameters, int counter) {
        Assert.isTrue(criteria.getSubjectValues().size() == 1,
            "Binary criteria should have only one subject value");
        Assert.isTrue(CriteriaType.isBinary(criteria.getType()), "Criteria type should be binary operation");

        final String subject = criteria.getSubject();
        final Object subjectValue = toCosmosDbValue(criteria.getSubjectValues().get(0));
        final String parameter = generateQueryParameter(subject, counter);
        final Part.IgnoreCaseType ignoreCase = criteria.getIgnoreCase();
        final String sqlKeyword = criteria.getType().getSqlKeyword();
        parameters.add(Pair.of(parameter, subjectValue));

        if (CriteriaType.isFunction(criteria.getType())) {
            return getFunctionCondition(ignoreCase, sqlKeyword, subject, parameter);
        } else {
            return getCondition(ignoreCase, sqlKeyword, subject, parameter);
        }
    }

    /**
     * Get condition string with function
     *
     * @param ignoreCase ignore case flag
     * @param sqlKeyword sql key word, operation name
     * @param subject sql column name
     * @param parameter sql filter value
     * @return condition string
     */
    private String getCondition(final Part.IgnoreCaseType ignoreCase, final String sqlKeyword,
                                final String subject, final String parameter) {
        if (Part.IgnoreCaseType.NEVER == ignoreCase) {
            return String.format("r.%s %s @%s", subject, sqlKeyword, parameter);
        } else {
            return String.format("UPPER(r.%s) %s UPPER(@%s)", subject, sqlKeyword, parameter);
        }
    }

    /**
     * Get condition string without function
     *
     * @param ignoreCase ignore case flag
     * @param sqlKeyword sql key word, operation name
     * @param subject sql column name
     * @param parameter sql filter value
     * @return condition string
     */
    private String getFunctionCondition(final Part.IgnoreCaseType ignoreCase, final String sqlKeyword,
                                        final String subject, final String parameter) {
        if (Part.IgnoreCaseType.NEVER == ignoreCase) {
            return String.format("%s(r.%s, @%s)", sqlKeyword, subject, parameter);
        } else {
            return String.format("%s(UPPER(r.%s), UPPER(@%s))", sqlKeyword, subject, parameter);
        }
    }

    private String generateBetween(@NonNull Criteria criteria, @NonNull List<Pair<String, Object>> parameters, int counter) {
        final String subject = criteria.getSubject();
        final Object value1 = toCosmosDbValue(criteria.getSubjectValues().get(0));
        final Object value2 = toCosmosDbValue(criteria.getSubjectValues().get(1));
        final String subject1 = subject + "start";
        final String subject2 = subject + "end";
        final String parameter1 = generateQueryParameter(subject1, counter);
        final String parameter2 = generateQueryParameter(subject2, counter);
        final String keyword = criteria.getType().getSqlKeyword();

        parameters.add(Pair.of(parameter1, value1));
        parameters.add(Pair.of(parameter2, value2));

        return String.format("(r.%s %s @%s AND @%s)", subject, keyword, parameter1, parameter2);
    }

    private String generateClosedQuery(@NonNull String left, @NonNull String right, CriteriaType type) {
        Assert.isTrue(CriteriaType.isClosed(type)
                && CriteriaType.isBinary(type),
            "Criteria type should be binary and closure operation");

        return String.join(" ", left, type.getSqlKeyword(), right);
    }

    @SuppressWarnings("unchecked")
    private String generateInQuery(@NonNull Criteria criteria, @NonNull List<Pair<String, Object>> parameters) {
        Assert.isTrue(criteria.getSubjectValues().size() == 1,
            "Criteria should have only one subject value");
        if (!(criteria.getSubjectValues().get(0) instanceof Collection)) {
            throw new IllegalQueryException("IN keyword requires Collection type in parameters");
        }

        final Collection<Object> values = (Collection<Object>) criteria.getSubjectValues().get(0);

        final List<String> paras = new ArrayList<>();
        for (Object o : values) {
            if (o instanceof String || o instanceof Integer || o instanceof Long || o instanceof Boolean) {
                String key = "p" + parameters.size();
                paras.add("@" + key);
                parameters.add(Pair.of(key, o));
            } else {
                throw new IllegalQueryException("IN keyword Range only support Number and String type.");
            }
        }

        return String.format("r.%s %s (%s)", criteria.getSubject(), criteria.getType().getSqlKeyword(),
            String.join(",", paras));
    }

    private String generateQueryBody(@NonNull Criteria criteria, @NonNull List<Pair<String, Object>> parameters, @NonNull final AtomicInteger counter) {
        final CriteriaType type = criteria.getType();

        switch (type) {
            case ALL:
                return "";
            case IN:
            case NOT_IN:
                return generateInQuery(criteria, parameters);
            case BETWEEN:
                return generateBetween(criteria, parameters, counter.getAndIncrement());
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
            case ARRAY_CONTAINS:
                return generateBinaryQuery(criteria, parameters, counter.getAndIncrement());
            case AND:
            case OR:
                Assert.isTrue(criteria.getSubCriteria().size() == 2,
                    "criteria should have two SubCriteria");

                final String left = generateQueryBody(criteria.getSubCriteria().get(0), parameters, counter);
                final String right = generateQueryBody(criteria.getSubCriteria().get(1), parameters, counter);

                return generateClosedQuery(left, right, type);
            default:
                throw new UnsupportedOperationException("unsupported Criteria type: "
                    + type);
        }
    }

    /**
     * Generate a query body for interface QuerySpecGenerator. The query body compose of Sql query String and its'
     * parameters. The parameters organized as a list of Pair, for each pair compose parameter name and value.
     *
     * @param query the representation for query method.
     * @return A pair tuple compose of Sql query.
     */
    @NonNull
    private Pair<String, List<Pair<String, Object>>> generateQueryBody(@NonNull CosmosQuery query, @NonNull final AtomicInteger counter) {
        final List<Pair<String, Object>> parameters = new ArrayList<>();
        String queryString = this.generateQueryBody(query.getCriteria(), parameters, counter);

        if (StringUtils.hasText(queryString)) {
            queryString = String.join(" ", "WHERE", queryString);
        }

        return Pair.of(queryString, parameters);
    }

    private static String getParameter(@NonNull Sort.Order order) {
        Assert.isTrue(!order.isIgnoreCase(), "Ignore case is not supported");

        final String direction = order.isDescending() ? "DESC" : "ASC";

        return String.format("r.%s %s", order.getProperty(), direction);
    }

    static String generateQuerySort(@NonNull Sort sort) {
        if (sort.isUnsorted()) {
            return "";
        }

        final String queryTail = "ORDER BY";
        final List<String> subjects = sort.stream().map(AbstractQueryGenerator::getParameter).collect(Collectors.toList());

        return queryTail
            + " "
            + String.join(",", subjects);
    }

    @NonNull
    private String generateQueryTail(@NonNull CosmosQuery query) {
        final List<String> queryTails = new ArrayList<>();

        queryTails.add(generateQuerySort(query.getSort()));

        return String.join(" ", queryTails.stream().filter(StringUtils::hasText).collect(Collectors.toList()));
    }


    protected SqlQuerySpec generateCosmosQuery(@NonNull CosmosQuery query,
                                               @NonNull String queryHead) {
        final AtomicInteger counter = new AtomicInteger();
        final Pair<String, List<Pair<String, Object>>> queryBody = generateQueryBody(query, counter);
        String queryString = String.join(" ", queryHead, queryBody.getFirst(), generateQueryTail(query));
        final List<Pair<String, Object>> parameters = queryBody.getSecond();

        List<SqlParameter> sqlParameters = parameters.stream()
                                                     .map(p -> new SqlParameter("@" + p.getFirst(),
                                                         toCosmosDbValue(p.getSecond())))
                                                     .collect(Collectors.toList());

        if (query.getLimit() > 0) {
            queryString = new StringBuilder(queryString)
                .append(" OFFSET 0 LIMIT ")
                .append(query.getLimit()).toString();
        }

        return new SqlQuerySpec(queryString, sqlParameters);
    }
}
