package com.azure.tools.apiview.processor.analysers.models;

public class Constants {

    public static final String APIVIEW_JSON_SCHEMA = "https://raw.githubusercontent.com/Azure/azure-sdk-tools/00aacf7f4e224e008702c3e77ecde46d25434e44/tools/apiview/parsers/apiview-treestyle-parser-schema/CodeFile.json";
    public static final String APIVIEW_JSON_SCHEMA_RESOURCE = "/CodeFile.json";

    /************************************************************************************
     * Mode Flags
     ************************************************************************************/

    /** If true, we will output two files - a standard .json file, as well as a gzipped .json.tgz file. */
    public static final boolean GZIP_OUTPUT = false;

    /** Converts links within JavaDoc to hyperlinks, but can result in unintended links. */
    public static final boolean JAVADOC_EXTRACT_LINKS = false;

    /** Validate against the published schema, that is referenced from the JSON output itself **/
    public static final boolean VALIDATE_JSON_SCHEMA = true;

    /** More readable JSON, but also larger file size. Good for debugging, but turn off for production. */
    public static final boolean PRETTY_PRINT_JSON = false;


    /************************************************************************************
     * Properties
     ************************************************************************************/

//    /** The filename of the icon to display for a TreeNode. */
//    public static final String PROPERTY_ICON_NAME = "IconName";
//
//    /**  */
//    public static final String PROPERTY_SUBKIND = "SubKind";
//
//    /**  */
//    public static final String PROPERTY_URL_LINK_TEXT = "LinkText";
//
//    /** Used to link to API elsewhere in the same review. */
//    public static final String PROPERTY_NAVIGATE_TO_ID = "NavigateToId";
//
//    /** The cross language definitionId for the node. */
//    public static final String PROPERTY_CROSS_LANGUAGE_ID = "CrossLangDefId";

    public static final String PROPERTY_MODULE_NAME = "module-name";
    public static final String PROPERTY_MODULE_EXPORTS = "module-exports";
    public static final String PROPERTY_MODULE_REQUIRES = "module-requires";
    public static final String PROPERTY_MODULE_OPENS = "module-opens";

    public static final String PROPERTY_MAVEN_NAME = "maven-name";
    public static final String PROPERTY_MAVEN_DESCRIPTION = "maven-description";



    /************************************************************************************
     * Tags
     ************************************************************************************/

    /**  */
    public static final String TAG_SKIP_DIFF = "SkipDiff";

    /**  */
    public static final String TAG_HIDE_FROM_NAVIGATION = "HideFromNav";


    /************************************************************************************
     * Render classes
     ************************************************************************************/

    // Render Classes are in the RenderClass enum


    /************************************************************************************
     * Annotation names
     ************************************************************************************/

    /** For @Deprecated annotation. */
    public static final String ANNOTATION_DEPRECATED = "Deprecated";

    public static final String ANNOTATION_SERVICE_METHOD = "ServiceMethod";
    public static final String ANNOTATION_SUPPRESS_WARNINGS = "SuppressWarnings";
    public static final String ANNOTATION_RETENTION = "Retention";
    public static final String ANNOTATION_TARGET = "Target";
    public static final String ANNOTATION_METADATA = "Metadata";


    /************************************************************************************
     * JSON output names
     ************************************************************************************/

    public static final String JSON_NAME_PARSER_VERSION = "ParserVersion";
    public static final String JSON_NAME_LANGUAGE = "Language";
    public static final String JSON_NAME_LANGUAGE_VARIANT = "LanguageVariant";
    public static final String JSON_NAME_PACKAGE_NAME = "PackageName";
    public static final String JSON_NAME_PACKAGE_VERSION = "PackageVersion";
//    public static final String JSON_NAME_API_FOREST = "APIForest";
    public static final String JSON_NAME_REVIEW_LINES = "ReviewLines";
    public static final String JSON_NAME_DIAGNOSTICS = "Diagnostics";
    public static final String JSON_LINE_ID = "LineId";
    public static final String JSON_CROSS_LANGUAGE_ID = "CrossLanguageId";
    public static final String JSON_NAME_TOKENS = "Tokens";
    public static final String JSON_IS_HIDDEN = "IsHidden";
    public static final String JSON_IS_CONTEXT_END_LINE = "IsContextEndLine";
    public static final String JSON_RELATED_TO_LINE = "RelatedToLine";
    public static final String JSON_NAME_ID = "Id";
    public static final String JSON_NAME_KIND = "Kind";
    public static final String JSON_NAME_TAGS = "Tags";
    public static final String JSON_NAME_PROPERTIES = "Properties";
    @Deprecated public static final String JSON_NAME_TOP_TOKENS = "TopTokens";
    @Deprecated public static final String JSON_NAME_BOTTOM_TOKENS = "BottomTokens";
    public static final String JSON_NAME_CHILDREN = "Children";
    public static final String JSON_NAME_VALUE = "Value";
    public static final String JSON_NAME_RENDER_CLASSES = "RenderClasses";
}
