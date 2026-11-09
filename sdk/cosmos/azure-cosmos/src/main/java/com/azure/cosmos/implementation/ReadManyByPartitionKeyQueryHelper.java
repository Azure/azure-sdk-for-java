// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper for constructing SqlQuerySpec instances for readManyByPartitionKeys operations.
 * This class is not intended to be used directly by end-users.
 */
public class ReadManyByPartitionKeyQueryHelper {

    private static final String DEFAULT_TABLE_ALIAS = "c";
    // Internal parameter prefix - uses double-underscore to avoid collisions with user-provided parameters
    private static final String PK_PARAM_PREFIX = "@__rmPk_";

    public static SqlQuerySpec createReadManyByPkQuerySpec(
        String baseQueryText,
        List<SqlParameter> baseParameters,
        List<PartitionKey> pkValues,
        List<String> partitionKeySelectors,
        PartitionKeyDefinition pkDefinition) {

        // Guard against collisions with our internal parameter names - callers cannot realistically
        // use the @__rmPk_ prefix for their own parameters, but if they do we surface a clear error
        // rather than letting the server reject a SqlQuerySpec with duplicate parameter names.
        for (SqlParameter baseParam : baseParameters) {
            String name = baseParam.getName();
            if (name != null && name.startsWith(PK_PARAM_PREFIX)) {
                throw new IllegalArgumentException(
                    "Custom query parameter name '" + name + "' collides with the reserved " +
                        "readManyByPartitionKeys internal prefix '" + PK_PARAM_PREFIX + "'. Rename the parameter.");
            }
        }

        // Extract the table alias from the FROM clause (e.g. "FROM x" -> "x", "FROM c" -> "c")
        String tableAlias = extractTableAlias(baseQueryText);

        StringBuilder pkFilter = new StringBuilder();
        List<SqlParameter> parameters = new ArrayList<>(baseParameters);
        int paramCount = 0;

        boolean isSinglePathPk = partitionKeySelectors.size() == 1;

        if (isSinglePathPk && pkDefinition.getKind() != PartitionKind.MULTI_HASH) {
            // Single PK path - use IN clause for normal values, OR NOT IS_DEFINED for NONE
            // First, separate NONE PKs from normal PKs
            boolean hasNone = false;
            List<PartitionKey> normalPkValues = new ArrayList<>();
            for (PartitionKey pk : pkValues) {
                PartitionKeyInternal pkInternal = BridgeInternal.getPartitionKeyInternal(pk);
                if (pkInternal.getComponents() == null) {
                    hasNone = true;
                } else {
                    normalPkValues.add(pk);
                }
            }

            pkFilter.append(" ");
            boolean hasNormalValues = !normalPkValues.isEmpty();
            if (hasNormalValues && hasNone) {
                pkFilter.append("(");
            }
            if (hasNormalValues) {
                pkFilter.append(tableAlias);
                pkFilter.append(partitionKeySelectors.get(0));
                pkFilter.append(" IN ( ");
                for (int i = 0; i < normalPkValues.size(); i++) {
                    PartitionKeyInternal pkInternal = BridgeInternal.getPartitionKeyInternal(normalPkValues.get(i));
                    Object[] pkComponents = pkInternal.toObjectArray();
                    String pkParamName = PK_PARAM_PREFIX + paramCount;
                    parameters.add(new SqlParameter(pkParamName, pkComponents[0]));
                    paramCount++;

                    pkFilter.append(pkParamName);
                    if (i < normalPkValues.size() - 1) {
                        pkFilter.append(", ");
                    }
                }
                pkFilter.append(" )");
            }
            if (hasNone) {
                if (hasNormalValues) {
                    pkFilter.append(" OR ");
                }
                pkFilter.append("NOT IS_DEFINED(");
                pkFilter.append(tableAlias);
                pkFilter.append(partitionKeySelectors.get(0));
                pkFilter.append(")");
            }
            if (hasNormalValues && hasNone) {
                pkFilter.append(")");
            }
        } else {
            // Multiple PK paths (HPK) or MULTI_HASH - use OR of AND clauses
            pkFilter.append(" ");
            for (int i = 0; i < pkValues.size(); i++) {
                PartitionKeyInternal pkInternal = BridgeInternal.getPartitionKeyInternal(pkValues.get(i));
                Object[] pkComponents = pkInternal.toObjectArray();

                if (pkComponents == null) {
                    throw new IllegalArgumentException(
                        "PartitionKey.NONE is not supported for multi-path partition keys in readManyByPartitionKeys.");
                }

                {
                    pkFilter.append("(");
                    for (int j = 0; j < pkComponents.length; j++) {
                        String pkParamName = PK_PARAM_PREFIX + paramCount;
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
                }

                if (i < pkValues.size() - 1) {
                    pkFilter.append(" OR ");
                }
            }
        }

        // Compose final query: handle existing WHERE clause in base query
        String finalQuery;
        int whereIndex = findTopLevelWhereIndex(baseQueryText);
        if (whereIndex >= 0) {
            // Base query has WHERE - AND our PK filter
            String beforeWhere = baseQueryText.substring(0, whereIndex);
            String afterWhere = baseQueryText.substring(whereIndex + 5); // skip "WHERE"
            finalQuery = beforeWhere + "WHERE (" + afterWhere.trim() + "\n) AND (" + pkFilter.toString().trim() + ")";
        } else {
            // No WHERE - add one. Use \n before WHERE so that a trailing single-line comment
            // (-- ...) in the base query does not swallow the WHERE clause.
            finalQuery = baseQueryText + "\n WHERE" + pkFilter.toString();
        }

        return new SqlQuerySpec(finalQuery, parameters);
    }

    /**
     * Extracts the table/collection alias from a SQL query's FROM clause.
     * Handles: "SELECT * FROM c", "SELECT x.id FROM x WHERE ...", "SELECT * FROM root r", etc.
     * Returns the alias used after FROM (last token before WHERE or end of FROM clause).
     */
    static String extractTableAlias(String queryText) {
        String upper = queryText.toUpperCase(Locale.ROOT);
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
            String remaining = upper.substring(afterFrom);
            // Reserved keywords that terminate the FROM clause - when the next token is one of these,
            // containerName itself IS the alias used throughout the rest of the query.
            if (isFollowedByReservedKeyword(remaining)) {
                return containerName;
            }
            // Handle optional AS: "FROM root AS r" -> alias is "r"
            if (remaining.startsWith("AS")
                && (remaining.length() == 2 || !isIdentifierChar(remaining.charAt(2)))) {
                afterFrom += 2; // skip AS
                while (afterFrom < queryText.length()
                    && Character.isWhitespace(queryText.charAt(afterFrom))) {
                    afterFrom++;
                }
            }
            // Otherwise the next token is the alias ("FROM root r" -> alias is "r")
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

    private static boolean isFollowedByReservedKeyword(String remainingUpper) {
        String[] keywords = { "WHERE", "ORDER", "GROUP", "JOIN", "OFFSET", "LIMIT", "HAVING" };
        for (String kw : keywords) {
            if (remainingUpper.startsWith(kw)
                && (remainingUpper.length() == kw.length()
                    || !isIdentifierChar(remainingUpper.charAt(kw.length())))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the index of a top-level SQL keyword in the query text (case-insensitive),
     * ignoring occurrences inside parentheses or string literals.
     */
    static int findTopLevelKeywordIndex(String queryText, String keyword) {
        String queryTextUpper = queryText.toUpperCase(Locale.ROOT);
        String keywordUpper = keyword.toUpperCase(Locale.ROOT);
        int depth = 0;
        int keyLen = keywordUpper.length();
        int len = queryTextUpper.length();
        for (int i = 0; i <= len - keyLen; i++) {
            char ch = queryText.charAt(i);
            // Skip single-line comments: -- ... end-of-line
            if (ch == '-' && i + 1 < len && queryText.charAt(i + 1) == '-') {
                i += 2;
                while (i < len && queryText.charAt(i) != '\n' && queryText.charAt(i) != '\r') {
                    i++;
                }
                continue;
            }
            // Skip block comments: /* ... */
            if (ch == '/' && i + 1 < len && queryText.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < len
                    && !(queryText.charAt(i) == '*' && queryText.charAt(i + 1) == '/')) {
                    i++;
                }
                i++; // position on the '/'; loop post-increment moves past it
                continue;
            }
            // Skip string literals enclosed in single quotes (handle '' escape)
            if (ch == '\'') {
                i++;
                while (i < len) {
                    if (queryText.charAt(i) == '\'') {
                        if (i + 1 < len && queryText.charAt(i + 1) == '\'') {
                            i += 2; // escaped quote - skip both
                            continue;
                        }
                        break; // end of string literal
                    }
                    i++;
                }
                continue;
            }
            // Skip double-quoted identifiers (e.g. c["WHERE"])
            if (ch == '"') {
                i++;
                while (i < len && queryText.charAt(i) != '"') {
                    i++;
                }
                continue;
            }
            char upperCh = queryTextUpper.charAt(i);
            if (upperCh == '(') {
                depth++;
            } else if (upperCh == ')') {
                depth--;
            } else if (depth == 0 && upperCh == keywordUpper.charAt(0)
                && queryTextUpper.startsWith(keywordUpper, i)
                && (i == 0 || !isIdentifierChar(queryTextUpper.charAt(i - 1)))
                && (i + keyLen >= queryTextUpper.length() || !isIdentifierChar(queryTextUpper.charAt(i + keyLen)))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns true if the character can appear in a SQL identifier or property access,
     * meaning it should NOT be treated as a word boundary for keyword matching.
     * Covers letters, digits, underscore, dot (property access), bracket (bracket notation),
     * and dollar sign (system properties).
     */
    private static boolean isIdentifierChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == '[' || ch == '$';
    }

    /**
     * Finds the index of the top-level WHERE keyword in the query text,
     * ignoring WHERE that appears inside parentheses (subqueries).
     */
    public static int findTopLevelWhereIndex(String queryTextUpper) {
        return findTopLevelKeywordIndex(queryTextUpper, "WHERE");
    }
}
