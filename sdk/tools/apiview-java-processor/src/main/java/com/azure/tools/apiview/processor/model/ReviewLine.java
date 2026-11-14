// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.tools.apiview.processor.model.traits.Parent;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_CROSS_LANGUAGE_ID;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_IS_CONTEXT_END_LINE;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_IS_HIDDEN;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_LINE_ID;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_CHILDREN;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_NAME_TOKENS;
import static com.azure.tools.apiview.processor.analysers.models.Constants.JSON_RELATED_TO_LINE;

/**
 * A tokenized API review line and its nested declarations.
 */
public class ReviewLine implements Parent, JsonSerializable<ReviewLine> {
    /*
     * lineId is only required if we need to support commenting on a line that contains this token.
     * Usually code line for documentation or just punctuation is not required to have lineId. lineId should be a unique
     * value within the review token file to use it assign to review comments as well as navigation Id within the review
     * page, for e.g Azure.Core.HttpHeader.Common, azure.template.template_main
     */
    private final String lineId;

    private String crossLanguageId;

    /*
     * list of tokens that constructs a line in API review.
     */
    private final List<ReviewToken> tokens;

    private final Parent parent;

    /*
     * Add any child lines as children. For e.g. all classes and namespace level methods are added as a children of
     * namespace(module) level code line. Similarly all method level code lines are added as children of it's class code
     * line.
     */
    private final List<ReviewLine> children;

    /*
     * Set current line as hidden code line by default. .NET has hidden APIs and architects don't want to see them by default.
     */
    private boolean isHidden = false;

    /*
     * Set current line as context end line. For e.g. line with token } or empty line after the class to mark end of context.
     */
    private boolean isContextEndLine;

    /*
     * Set ID of related line to ensure current line is not visible when a related line is hidden. One e.g. is a code
     * line for class attribute should set class line's Line ID as related line ID.
     */
    private String relatedToLine;

    /*
     * This is not used by APIView - this is for use by the parser only, so that it may write more diagnostics
     */
    private final Map<String, String> properties;

    /**
     * Creates a review line.
     *
     * @param parent the parent
     */
    public ReviewLine(Parent parent) {
        this(parent, null);
    }

    /**
     * Creates a review line.
     *
     * @param parent the parent
     * @param lineId the line id
     */
    public ReviewLine(final Parent parent, final String lineId) {
        this.parent = Objects.requireNonNull(parent);
        this.tokens = new ArrayList<>();
        this.children = new ArrayList<>();
        this.lineId = lineId;
        this.properties = new HashMap<>();
    }

    /**
     * Adds token.
     *
     * @param token the token
     * @return this instance
     */
    public ReviewLine addToken(ReviewToken token) {
        tokens.add(token);
        return this;
    }

    /**
     * Adds token.
     *
     * @param tokenKind the token kind
     * @param value the value
     * @return this instance
     */
    public ReviewLine addToken(TokenKind tokenKind, String value) {
        return addToken(tokenKind, value, Spacing.DEFAULT);
    }

    /**
     * Adds token.
     *
     * @param tokenKind the token kind
     * @param value the value
     * @param spacing the spacing
     * @return this instance
     */
    public ReviewLine addToken(TokenKind tokenKind, String value, Spacing spacing) {
        return addToken(tokenKind, value, null, spacing);
    }

    /**
     * Adds token.
     *
     * @param tokenKind the token kind
     * @param value the value
     * @param navigateToId the navigate to id
     * @return this instance
     */
    public ReviewLine addToken(TokenKind tokenKind, String value, String navigateToId) {
        return addToken(tokenKind, value, navigateToId, Spacing.DEFAULT);
    }

    /**
     * Adds token.
     *
     * @param tokenKind the token kind
     * @param value the value
     * @param navigateToId the navigate to id
     * @param spacing the spacing
     * @return this instance
     */
    public ReviewLine addToken(TokenKind tokenKind, String value, String navigateToId, Spacing spacing) {
        return addToken(new ReviewToken(tokenKind, value, navigateToId).setSpacing(spacing));
    }

    /**
     * Adds space.
     *
     * @return this instance
     */
    public ReviewLine addSpace() {
        return addToken(TokenKind.TEXT, " ", Spacing.NO_SPACE);
    }

    /**
     * Returns the child that was passed in as the argument
     *
     * @param child the child
     * @return the added child line
     */
    @Override
    public ReviewLine addChildLine(ReviewLine child) {
        children.add(child);
        return child;
    }

    /**
     * Add a review line with the given line ID
     * @return the new review line
     */
    @Override
    public ReviewLine addChildLine(final String lineId) {
        return addChildLine(new ReviewLine(this, lineId));
    }

    @Override
    public ReviewLine addChildLine() {
        return addChildLine(new ReviewLine(this, null));
    }

    @Override
    public List<ReviewLine> getChildren() {
        return children;
    }

    /**
     * Adds property.
     *
     * @param key the key
     * @param value the value
     * @return this instance
     */
    public ReviewLine addProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    /**
     * Checks whether this line contains the named parser property.
     *
     * @param key the key
     * @return true if the property is present
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Returns the property.
     *
     * @param key the key
     * @return the property
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Returns the tokens.
     *
     * @return the tokens
     */
    public List<ReviewToken> getTokens() {
        return tokens;
    }

    /**
     * Adds the opening brace for a declaration context.
     *
     * @return this instance
     */
    public ReviewLine addContextStartTokens() {
        addToken(TokenKind.PUNCTUATION, "{");
        return this;
    }

    /**
     * Adds a closing-brace line to the parent.
     */
    public void addContextEndTokens() {
        parent.addChildLine(new ReviewLine(this).addToken(TokenKind.PUNCTUATION, "}").setContextEndLine());
    }

    /**
     * Marks this line as the end of a declaration context.
     *
     * @return this instance
     */
    public ReviewLine setContextEndLine() {
        isContextEndLine = true;
        return this;
    }

    /**
     * Returns the line id.
     *
     * @return the line id
     */
    public String getLineId() {
        return lineId;
    }

    /**
     * Sets the line whose visibility controls this line.
     *
     * @param relatedToLine the related to line
     * @return this instance
     */
    public ReviewLine setRelatedToLine(ReviewLine relatedToLine) {
        this.relatedToLine = relatedToLine.lineId;
        return this;
    }

    /**
     * Sets the line whose visibility controls this line.
     *
     * @param relatedToLineId the related to line id
     * @return this instance
     */
    public ReviewLine setRelatedToLine(String relatedToLineId) {
        this.relatedToLine = relatedToLineId;
        return this;
    }

    /**
     * Sets the cross language id.
     *
     * @param crossLanguageId the cross language id
     * @return this instance
     */
    public ReviewLine setCrossLanguageId(String crossLanguageId) {
        this.crossLanguageId = crossLanguageId;
        return this;
    }

    /**
     * This line will not appear in the APIView navigation or content area.
     *
     * @return this instance
     */
    public ReviewLine hideLine() {
        isHidden = true;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField(JSON_LINE_ID, lineId)
            .writeStringField(JSON_CROSS_LANGUAGE_ID, crossLanguageId);

        if (!tokens.isEmpty()) {
            jsonWriter.writeArrayField(JSON_NAME_TOKENS, tokens, JsonWriter::writeJson);
        }

        if (!children.isEmpty()) {
            jsonWriter.writeArrayField(JSON_NAME_CHILDREN, children, JsonWriter::writeJson);
        }

        if (isHidden) {
            jsonWriter.writeBooleanField(JSON_IS_HIDDEN, isHidden);
        }

        if (isContextEndLine) {
            jsonWriter.writeBooleanField(JSON_IS_CONTEXT_END_LINE, isContextEndLine);
        }

        if (relatedToLine != null) {
            jsonWriter.writeStringField(JSON_RELATED_TO_LINE, relatedToLine);
        }

        return jsonWriter.writeEndObject();
    }
}
