package com.azure.tools.apiview.processor.analysers.util;

import com.azure.tools.apiview.processor.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class APIListingValidator {
    private APIListingValidator() { }

    public static void validate(APIListing apiListing) {
        // FIXME reintroduce on other side of port
        // Create a set to store the IDs
        Set<String> ids = new HashSet<>();

        // Recursively ensure that all tokens in the entire API listing have a kind, and a unique ID (except for those with no ID).
        apiListing.getChildren().forEach(reviewLine -> validateReviewLine(reviewLine, ids));
    }

    private static void validateReviewLine(ReviewLine reviewLine, Set<String> ids) {
//        validateTokenList(reviewLine.getTokens(), ids);
//        reviewLine.getChildren().forEach(childLine -> validateReviewLine(childLine, ids));
//
//        // Validate TreeNode name
//        String name = node.getName();
//        if (name == null || name.isEmpty()) {
//            throw new IllegalStateException("TreeNode name cannot be null or empty for node: " + node);
//        }
//
//        // Validate TreeNode kind
//        TreeNodeKind kind = node.getKind();
//        if (kind == null || kind.getName().isEmpty()) {
//            throw new IllegalStateException("TreeNode kind cannot be null or empty for node: " + node);
//        }
//
//        // Validate TreeNode ID
//        String id = node.getId();
//        if (id == null) {
//            throw new IllegalStateException("TreeNode ID cannot be null for node: " + node);
//        }
//        validateId(id, ids);
    }

    private static void validateTokenList(List<ReviewToken> tokens, Set<String> ids) {
        for (ReviewToken token : tokens) {
            validateToken(token, ids);
        }
    }

    private static void validateToken(ReviewToken token, Set<String> ids) {
//        // FIXME reintroduce on other side of port
//        if (token.getKind() == null) {
//            throw new IllegalStateException("Token kind cannot be null");
//        }
//
//        String id = token.getId();
//        if (id != null) {
//            // Check if the ID is unique
//            validateId(id, ids);
//        }
    }

    private static void validateId(String id, Set<String> ids) {
        if (ids.contains(id)) {
            throw new IllegalStateException("ID is not unique: \"" + id + "\"");
        } else {
            ids.add(id);
        }
    }
}