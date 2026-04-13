// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for constructing SqlQuerySpec instances for readManyByPartitionKey operations.
 * This class is not intended to be used directly by end-users.
 */
public class ReadManyByPartitionKeyQueryHelper {

    private static final String DEFAULT_TABLE_ALIAS = "c";

    public static SqlQuerySpec createReadManyByPkQuerySpec(
        String baseQueryText,
        List<SqlParameter> baseParameters,
        List<PartitionKey> pkValues,
        List<String> partitionKeySelectors,
        PartitionKeyDefinition pkDefinition) {

        // Extract the table alias from the FROM clause (e.g. "FROM x" → "x", "FROM c" → "c")
        String tableAlias = extractTableAlias(baseQueryText);

        StringBuilder pkFilter = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>(baseParameters);
        int paramCount = baseParameters.size();

        boolean isSinglePathPk = partitionKeySelectors.size() == 1;

        if (isSinglePathPk && pkDefinition.getKind() != PartitionKind.MULTI_HASH) {
            // Single PK path — use IN clause: alias["pkPath"] IN (@pk0, @pk1, ...)
            pkFilter.append(" ");
            pkFilter.append(tableAlias);
            pkFilter.append(partitionKeySelectors.get(0));
            pkFilter.append(" IN ( ");
            for (int i = 0; i < pkValues.size(); i++) {
                PartitionKeyInternal pkInternal = BridgeInternal.getPartitionKeyInternal(pkValues.get(i));
                Object[] pkComponents = pkInternal.toObjectArray();
                String pkParamName = "@pkParam" + paramCount;
                parameters.add(new SqlParameter(pkParamName, pkComponents[0]));
                paramCount++;

                pkFilter.append(pkParamName);
                if (i < pkValues.size() - 1) {
                    pkFilter.append(", ");
                }
            }
            pkFilter.append(" )");
        } else {
            // Multiple PK paths (HPK) or MULTI_HASH — use OR of AND clauses
            pkFilter.append(" ");
            for (int i = 0; i < pkValues.size(); i++) {
                PartitionKeyInternal pkInternal = BridgeInternal.getPartitionKeyInternal(pkValues.get(i));
                Object[] pkComponents = pkInternal.toObjectArray();

                pkFilter.append("(");
                for (int j = 0; j < pkComponents.length; j++) {
                    String pkParamName = "@pkParam" + paramCount;
                    parameters.add(new SqlParameter(pkParamName, pkComponents[j]));
                    paramCount++;

                    if (j > 0) {
                        pkFilter.append(" AND ");
                    }
                    pkFilter.append(tableAlias);
                    pkFilter.append(partitionKeySelectors.get(j));
                    pkFilter.append(" = ");
                    pkFilter.append(pkParamName);
                }
                pkFilter.append(")");

                if (i < pkValues.size() - 1) {
                    pkFilter.append(" OR ");
                }
            }
        }

        // Compose final query: handle existing WHERE clause in base query
        String finalQuery;
        int whereIndex = findTopLevelWhereIndex(baseQueryText);
        if (whereIndex >= 0) {
            // Base query has WHERE — AND our PK filter
            String beforeWhere = baseQueryText.substring(0, whereIndex);
            String afterWhere = baseQueryText.substring(whereIndex + 5); // skip "WHERE"
            finalQuery = beforeWhere + "WHERE (" + afterWhere.trim() + ") AND (" + pkFilter.toString().trim() + ")";
        } else {
            // No WHERE — add one
            finalQuery = baseQueryText + " WHERE" + pkFilter.toString();
        }

        return new SqlQuerySpec(finalQuery, parameters);
    }

    /**
     * Extracts the table/collection alias from a SQL query's FROM clause.
     * Handles: "SELECT * FROM c", "SELECT x.id FROM x WHERE ...", "SELECT * FROM root r", etc.
     * Returns the alias used after FROM (last token before WHERE or end of FROM clause).
     */
    static String extractTableAlias(String queryText) {
        String upper = queryText.toUpperCase();
        int fromIndex = findTopLevelKeywordIndex(upper, "FROM");
        if (fromIndex < 0) {
            return DEFAULT_TABLE_ALIAS;
        }

        // Start scanning after "FROM"
        int afterFrom = fromIndex + 4;
        // Skip whitespace
        while (afterFrom < queryText.length() && Character.isWhitespace(queryText.charAt(afterFrom))) {
            afterFrom++;
        }

        // Collect the container name token (could be "root", "c", etc.)
        int tokenStart = afterFrom;
        while (afterFrom < queryText.length()
            && !Character.isWhitespace(queryText.charAt(afterFrom))
            && queryText.charAt(afterFrom) != '('
            && queryText.charAt(afterFrom) != ')') {
            afterFrom++;
        }
        String containerName = queryText.substring(tokenStart, afterFrom);

        // Skip whitespace after container name
        while (afterFrom < queryText.length() && Character.isWhitespace(queryText.charAt(afterFrom))) {
            afterFrom++;
        }

        // Check if there's an alias after the container name (before WHERE or end)
        if (afterFrom < queryText.length()) {
            char nextChar = Character.toUpperCase(queryText.charAt(afterFrom));
            // If the next token is a keyword (WHERE, ORDER, GROUP, JOIN) or end, containerName IS the alias
            if (nextChar == 'W' || nextChar == 'O' || nextChar == 'G' || nextChar == 'J') {
                // Check if it's actually a keyword
                String remaining = upper.substring(afterFrom);
                if (remaining.startsWith("WHERE") || remaining.startsWith("ORDER")
                    || remaining.startsWith("GROUP") || remaining.startsWith("JOIN")) {
                    return containerName;
                }
            }
            // Otherwise the next token is the alias ("FROM root r" → alias is "r")
            int aliasStart = afterFrom;
            while (afterFrom < queryText.length()
                && !Character.isWhitespace(queryText.charAt(afterFrom))
                && queryText.charAt(afterFrom) != '('
                && queryText.charAt(afterFrom) != ')') {
                afterFrom++;
            }
            if (afterFrom > aliasStart) {
                return queryText.substring(aliasStart, afterFrom);
            }
        }

        return containerName;
    }

    /**
     * Finds the index of a top-level SQL keyword in the query text (case-insensitive),
     * ignoring occurrences inside parentheses.
     */
    static int findTopLevelKeywordIndex(String queryText, String keyword) {
        String queryTextUpper = queryText.toUpperCase();
        String keywordUpper = keyword.toUpperCase();
        int depth = 0;
        int keyLen = keywordUpper.length();
        for (int i = 0; i <= queryTextUpper.length() - keyLen; i++) {
            char ch = queryTextUpper.charAt(i);
            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
            } else if (depth == 0 && ch == keywordUpper.charAt(0)
                && queryTextUpper.startsWith(keywordUpper, i)
                && (i == 0 || !Character.isLetterOrDigit(queryTextUpper.charAt(i - 1)))
                && (i + keyLen >= queryTextUpper.length() || !Character.isLetterOrDigit(queryTextUpper.charAt(i + keyLen)))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the index of the top-level WHERE keyword in the query text,
     * ignoring WHERE that appears inside parentheses (subqueries).
     */
    public static int findTopLevelWhereIndex(String queryTextUpper) {
        return findTopLevelKeywordIndex(queryTextUpper, "WHERE");
    }
}
