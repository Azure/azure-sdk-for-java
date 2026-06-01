// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;

/**
 * Removes generated public client surface so the hand-written ConfigurationClient,
 * ConfigurationAsyncClient, ConfigurationClientBuilder, and ConfigurationServiceVersion
 * in com.azure.data.appconfiguration are not overwritten by tsp-client update.
 * The generated implementation client under com.azure.data.appconfiguration.implementation
 * is preserved and used by the hand-written public classes.
 *
 * Also promotes the generated private *SinglePage / *SinglePageAsync / *NextSinglePage
 * methods on AzureAppConfigurationImpl to public so the hand-written paging code in
 * ConfigurationClient and ConfigurationAsyncClient can drive pagination directly
 * (needed for per-page conditional ETag matching).
 */
public class AppConfigurationCustomizations extends Customization {

    private static final String ROOT_FILE_PATH = "src/main/java/com/azure/data/appconfiguration/";

    private static final String IMPL_CLIENT_PATH
        = ROOT_FILE_PATH + "implementation/ConfigurationClientImpl.java";

    private static final String[] FILES_TO_REMOVE = new String[] {
        "AzureAppConfigurationServiceVersion.java",
        // The public client surface is hand-written. Drop any codegen-emitted copies so the hand-written
        // versions in the repo survive a regen.
        "ConfigurationClient.java",
        "ConfigurationAsyncClient.java",
        "ConfigurationClientBuilder.java",
        "ConfigurationServiceVersion.java"
    };

    // Matches: "    private Mono<PagedResponse<BinaryData>> fooSinglePageAsync("
    //          "    private PagedResponse<BinaryData> fooSinglePage("
    //          "    private Mono<PagedResponse<BinaryData>> fooNextSinglePageAsync("
    //          "    private PagedResponse<BinaryData> fooNextSinglePage("
    private static final Pattern SINGLE_PAGE_METHOD = Pattern.compile(
        "(^|\\n)(\\s*)private(\\s+(?:Mono<PagedResponse<BinaryData>>|PagedResponse<BinaryData>)\\s+\\w*SinglePage\\w*\\s*\\()");

    // Matches the createSnapshotWithResponse / createSnapshotWithResponseAsync helpers we need to drive the LRO
    // ourselves from CreateSnapshotUtilClient (typespec-java generates them as private).
    private static final Pattern CREATE_SNAPSHOT_HELPER = Pattern.compile(
        "(^|\\n)(\\s*)private(\\s+(?:Mono<Response<BinaryData>>|Response<BinaryData>)\\s+createSnapshotWithResponse\\w*\\s*\\()");

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        Editor editor = customization.getRawEditor();
        for (String fileName : FILES_TO_REMOVE) {
            String path = ROOT_FILE_PATH + fileName;
            if (editor.getContents().containsKey(path)) {
                editor.removeFile(path);
                logger.info("Removed generated file {}", path);
            } else {
                logger.info("Generated file {} not present; skipping removal.", path);
            }
        }
        promoteSinglePageMethodsToPublic(editor, logger);
        patchModuleInfo(editor, logger);
        addKeyValueSetKey(editor, logger);
        rewriteServiceVersionType(editor, logger);
    }

    // The TypeSpec @key decorator drops the setter for KeyValue.key. We still need to populate it on
    // the request body for set/put operations, so add the setter back via customization.
    private static void addKeyValueSetKey(Editor editor, Logger logger) {
        String path = ROOT_FILE_PATH + "implementation/models/KeyValue.java";
        String content = editor.getContents().get(path);
        if (content == null) {
            logger.warn("{} not present; skipping setKey injection.", path);
            return;
        }
        if (content.contains("public KeyValue setKey(")) {
            logger.info("KeyValue.setKey already present; no patch needed.");
            return;
        }
        String marker = "    public String getKey() {";
        int idx = content.indexOf(marker);
        if (idx < 0) {
            logger.warn("Could not find getKey() in KeyValue; skipping setKey injection.");
            return;
        }
        String setter = "    /**\n"
            + "     * Set the key property: The key of the key-value.\n"
            + "     *\n"
            + "     * @param key the key value to set.\n"
            + "     * @return the KeyValue object itself.\n"
            + "     */\n"
            + "    public KeyValue setKey(String key) {\n"
            + "        this.key = key;\n"
            + "        return this;\n"
            + "    }\n\n";
        String updated = content.substring(0, idx) + setter + content.substring(idx);
        editor.replaceFile(path, updated);
        logger.info("Injected KeyValue.setKey(String).");
    }

    private static void patchModuleInfo(Editor editor, Logger logger) {
        String path = "src/main/java/module-info.java";
        String content = editor.getContents().get(path);
        if (content == null) {
            logger.warn("{} not present; skipping module-info patch.", path);
            return;
        }
        String updated = content;
        if (!updated.contains("exports com.azure.data.appconfiguration.models;")) {
            updated = updated.replace(
                "exports com.azure.data.appconfiguration;",
                "exports com.azure.data.appconfiguration;\n    exports com.azure.data.appconfiguration.models;");
        }
        if (!updated.contains("opens com.azure.data.appconfiguration.models to com.azure.core;")) {
            updated = updated.replace(
                "opens com.azure.data.appconfiguration.implementation.models to com.azure.core;",
                "opens com.azure.data.appconfiguration.implementation.models to com.azure.core;\n"
                    + "    opens com.azure.data.appconfiguration.models to com.azure.core;");
        }
        if (!updated.equals(content)) {
            editor.replaceFile(path, updated);
            logger.info("Patched module-info.java to export and open com.azure.data.appconfiguration.models.");
        } else {
            logger.info("module-info.java already exports models package; no patch needed.");
        }
    }

    // The generated impl references typespec-generated AzureAppConfigurationServiceVersion which we delete.
    // Rewrite to use the hand-written ConfigurationServiceVersion enum.
    private static void rewriteServiceVersionType(Editor editor, Logger logger) {
        String content = editor.getContents().get(IMPL_CLIENT_PATH);
        if (content == null || !content.contains("AzureAppConfigurationServiceVersion")) {
            return;
        }
        editor.replaceFile(IMPL_CLIENT_PATH,
            content.replace("AzureAppConfigurationServiceVersion", "ConfigurationServiceVersion"));
        logger.info("Rewrote AzureAppConfigurationServiceVersion -> ConfigurationServiceVersion in impl.");
    }

    private static void promoteSinglePageMethodsToPublic(Editor editor, Logger logger) {
        String content = editor.getContents().get(IMPL_CLIENT_PATH);
        if (content == null) {
            logger.warn("{} not present; skipping single-page visibility promotion.", IMPL_CLIENT_PATH);
            return;
        }
        content = promote(content, SINGLE_PAGE_METHOD, "single-page", logger);
        content = promote(content, CREATE_SNAPSHOT_HELPER, "createSnapshotWithResponse", logger);
        editor.replaceFile(IMPL_CLIENT_PATH, content);
    }

    private static String promote(String content, Pattern pattern, String label, Logger logger) {
        Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();
        int count = 0;
        while (matcher.find()) {
            matcher.appendReplacement(sb,
                Matcher.quoteReplacement(matcher.group(1) + matcher.group(2) + "public" + matcher.group(3)));
            count++;
        }
        matcher.appendTail(sb);
        if (count > 0) {
            logger.info("Promoted {} {} method(s) on AzureAppConfigurationImpl from private to public.",
                count, label);
        } else {
            logger.info("No private {} methods found on AzureAppConfigurationImpl to promote.", label);
        }
        return sb.toString();
    }
}
