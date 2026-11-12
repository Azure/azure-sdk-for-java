package com.azure.tools.apiview.processor.model;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.tools.apiview.processor.model.traits.Parent;

import java.io.IOException;
import java.util.*;

import static com.azure.tools.apiview.processor.analysers.models.Constants.*;

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

    public ReviewLine(Parent parent) {
        this(parent, null);
    }

    public ReviewLine(final Parent parent, final String lineId) {
        this.parent = Objects.requireNonNull(parent);
        this.tokens = new ArrayList<>();
        this.children = new ArrayList<>();
        this.lineId = lineId;
        this.properties = new HashMap<>();
    }

    public ReviewLine addToken(ReviewToken token) {
        tokens.add(token);
        return this;
    }

    public ReviewLine addToken(TokenKind tokenKind, String value) {
        return addToken(tokenKind, value, Spacing.DEFAULT);
    }

    public ReviewLine addToken(TokenKind tokenKind, String value, Spacing spacing) {
        return addToken(tokenKind, value, null, spacing);
    }

    public ReviewLine addToken(TokenKind tokenKind, String value, String navigateToId) {
        return addToken(tokenKind, value, navigateToId, Spacing.DEFAULT);
    }

    public ReviewLine addToken(TokenKind tokenKind, String value, String navigateToId, Spacing spacing) {
        return addToken(new ReviewToken(tokenKind, value, navigateToId).setSpacing(spacing));
    }

    public ReviewLine addSpace() {
        return addToken(TokenKind.TEXT, " ", Spacing.NO_SPACE);
    }

    /**
     * Returns the child that was passed in as the argument
     * @param child
     * @return
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

    public ReviewLine addProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public List<ReviewToken> getTokens() {
        return tokens;
    }

    public ReviewLine addContextStartTokens() {
        addToken(TokenKind.PUNCTUATION, "{");
        return this;
    }

    public void addContextEndTokens() {
        parent.addChildLine(new ReviewLine(this).addToken(TokenKind.PUNCTUATION, "}").setContextEndLine());
    }

    public ReviewLine setContextEndLine() {
        isContextEndLine = true;
        return this;
    }

    public String getLineId() {
        return lineId;
    }

    public ReviewLine setRelatedToLine(ReviewLine relatedToLine) {
        this.relatedToLine = relatedToLine.lineId;
        return this;
    }

    public ReviewLine setRelatedToLine(String relatedToLineId) {
        this.relatedToLine = relatedToLineId;
        return this;
    }

    public ReviewLine setCrossLanguageId(String crossLanguageId) {
        this.crossLanguageId = crossLanguageId;
        return this;
    }

    /**
     * This line will not appear in the APIView navigation or content area.
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
