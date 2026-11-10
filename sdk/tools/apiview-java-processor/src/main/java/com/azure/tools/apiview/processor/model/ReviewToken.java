package com.azure.tools.apiview.processor.model;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.*;

import static com.azure.tools.apiview.processor.analysers.models.Constants.*;

public class ReviewToken implements JsonSerializable<ReviewToken> {

    // The token value which will be displayed.
    private final String value;

    // NavigationDisplayName is used to create a tree node in the navigation panel. Navigation nodes will be created
    // only if token contains navigation display name.
    private String navigationDisplayName;

    // navigateToId should be set if the underlying token is required to be displayed as HREF to another type within
    // the review. For e.g. a param type which is class name in the same package
    private String navigateToId;

    // set skipDiff to true if underlying token needs to be ignored from diff calculation. For e.g. package metadata or
    // dependency versions are usually excluded when comparing two revisions to avoid reporting them as API changes
    private boolean skipDiff;

    // This is set if API is marked as deprecated
    private boolean isDeprecated;

    // Set this to true if there is a prefix space required before current token. For e.g, space before token for =
    private boolean hasPrefixSpace = false;

    // Set this to false if there is no suffix space required before next token. For e.g, punctuation right after method name
    private boolean hasSuffixSpace = true;

    // Set isDocumentation to true if current token is part of documentation
    private boolean isDocumentation;

    private final TokenKind tokenKind;

//    // for things like deprecated, hidden, etc
//    private final Set<String> tags;

//    // Capture any other interesting data here. e.g Use GroupId : documentation to group consecutive tokens.
//    private final Map<String, String> properties;

    // Add css classes for how the tokens will be rendered. To avoid collision between languages use a language prefix
    // for you classes. e.g csKeyword , jsModule, pyModule
    private final Set<RenderClass> renderClasses;

    public ReviewToken(TokenKind tokenKind, String value) {
        this(tokenKind, value, null);
    }

    public ReviewToken(TokenKind tokenKind, String value, String navigateToId) {
        this.value = value;
        this.navigateToId = navigateToId;

        if (tokenKind == null) {
            throw new NullPointerException("tokenKind cannot be null");
        }

        this.tokenKind = tokenKind;
        this.renderClasses = new LinkedHashSet<>(tokenKind.getRenderClasses());
    }

    public ReviewToken addRenderClass(RenderClass renderClass) {
        renderClasses.add(Objects.requireNonNull(renderClass));
        return this;
    }

    public ReviewToken setNavigationDisplayName(String navigationDisplayName) {
        this.navigationDisplayName = navigationDisplayName;
        return this;
    }

    public ReviewToken setSkipDiff() {
        this.skipDiff = true;
        return this;
    }

    public ReviewToken setSpacing(Spacing spacing) {
        switch (spacing) {
            case DEFAULT:
                break;
            case NO_SPACE:
                this.hasPrefixSpace = false;
                this.hasSuffixSpace = false;
                break;
            case SPACE_BEFORE:
                this.hasPrefixSpace = true;
                this.hasSuffixSpace = false;
                break;
            case SPACE_AFTER:
                this.hasPrefixSpace = false;
                this.hasSuffixSpace = true;
                break;
            case SPACE_BEFORE_AND_AFTER:
                this.hasPrefixSpace = true;
                this.hasSuffixSpace = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown spacing type: " + spacing);
        }

        return this;
    }

    public ReviewToken setNavigateToId(String navigateToId) {
        this.navigateToId = navigateToId;
        return this;
    }

    public ReviewToken setDocumentation() {
        this.isDocumentation = true;
        return this;
    }

    public ReviewToken setDeprecated() {
        this.isDeprecated = true;
        addRenderClass(RenderClass.DEPRECATED);
        return this;
    }

    public TokenKind getTokenKind() {
        return tokenKind;
    }

    public String getValue() {
        return value;
    }

    public boolean hasPrefixSpace() {
        return hasPrefixSpace;
    }

    public boolean hasSuffixSpace() {
        return hasSuffixSpace;
    }

    public boolean isDocumentation() {
        return isDocumentation;
    }

    //    public ReviewToken addProperty(String key, String value) {
//        properties.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
//        return this;
//    }
//
//    public ReviewToken addTag(String tag) {
//        tags.add(Objects.requireNonNull(tag));
//        return this;
//    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeIntField(JSON_NAME_KIND, tokenKind.getTokenKindId())
            .writeStringField(JSON_NAME_VALUE, value)
            .writeStringField("NavigationDisplayName", navigationDisplayName)
            .writeStringField("NavigateToId", navigateToId);

        if (skipDiff) {
            jsonWriter.writeBooleanField("SkipDiff", skipDiff);
        }

        if (isDeprecated) {
            jsonWriter.writeBooleanField("IsDeprecated", isDeprecated);
        }

        // hasSuffixSpace is true by default, so only write it if it's false.
        if (!hasSuffixSpace) {
            jsonWriter.writeBooleanField("HasSuffixSpace", hasSuffixSpace);
        }

        // hasPrefixSpace is false by default, so only write it if it's true.
        if (hasPrefixSpace) {
            jsonWriter.writeBooleanField("HasPrefixSpace", hasPrefixSpace);
        }

        if (isDocumentation) {
            jsonWriter.writeBooleanField("IsDocumentation", isDocumentation);
        }

        // FIXME tidy up
        if (renderClasses != null && !renderClasses.isEmpty()) {
            jsonWriter.writeArrayField(JSON_NAME_RENDER_CLASSES, renderClasses, (jw, rc) -> {
                for (String str : rc.getValues()) {
                    jw.writeString(str);
                }
            });
        }

        return jsonWriter.writeEndObject();
    }

    @Override
    public String toString() {
        return "ReviewToken{ value=" +
            (hasPrefixSpace ? "' " : "'") +
            value +
            (hasSuffixSpace ? " '" : "'") +
            " }";
    }
}
