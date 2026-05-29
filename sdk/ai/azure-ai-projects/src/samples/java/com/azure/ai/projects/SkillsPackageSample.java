// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.CreateSkillVersionFromFilesBody;
import com.azure.ai.projects.models.SkillFileDetails;
import com.azure.ai.projects.models.Skill;
import com.azure.ai.projects.models.SkillVersion;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Sample demonstrating uploading and downloading a skill package using the synchronous SkillsClient.
 *
 * <p>Skills are a preview feature. Before running, set the following environment variable:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 * </ul>
 */
public class SkillsPackageSample {
    private static final String SKILL_NAME = "java-sample-skill-package";

    private static final SkillsClient SKILLS_CLIENT = new AIProjectClientBuilder()
        .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildSkillsClient();

    public static void main(String[] args) throws IOException {
        try {
            SKILLS_CLIENT.deleteSkill(SKILL_NAME);
            System.out.println("Deleted existing skill: " + SKILL_NAME);
        } catch (ResourceNotFoundException ignored) {
            // The sample skill does not already exist.
        }

        SkillVersion imported = null;
        try {
            // BEGIN:com.azure.ai.projects.SkillsPackageSample.createSkillVersionFromFiles

            imported = SKILLS_CLIENT.createSkillVersionFromFiles(SKILL_NAME, createSkillPackageBody());
            System.out.println("Imported skill from package: " + imported.getName()
                + " (version=" + imported.getVersion() + ")");

            // END:com.azure.ai.projects.SkillsPackageSample.createSkillVersionFromFiles

            // BEGIN:com.azure.ai.projects.SkillsPackageSample.getSkillContent

            Skill fetched = SKILLS_CLIENT.getSkill(imported.getName());
            System.out.println("Fetched imported skill: " + fetched.getName());

            BinaryData downloaded = SKILLS_CLIENT.getSkillContent(fetched.getName());
            Path downloadPath = Files.createTempFile(fetched.getName() + "-", ".zip");
            Files.write(downloadPath, downloaded.toBytes());
            System.out.println("Downloaded skill package path: " + downloadPath);

            // END:com.azure.ai.projects.SkillsPackageSample.getSkillContent
        } finally {
            String skillName = imported == null ? SKILL_NAME : imported.getName();
            try {
                SKILLS_CLIENT.deleteSkill(skillName);
                System.out.println("Deleted imported skill: " + skillName);
            } catch (ResourceNotFoundException ignored) {
                // The skill may not have been created.
            }
        }
    }

    private static CreateSkillVersionFromFilesBody createSkillPackageBody() throws IOException {
        String skillMarkdown = "---\n"
            + "name: " + SKILL_NAME + "\n"
            + "description: Answers product support questions using company policy and product guidance.\n"
            + "---\n\n"
            + "You help answer product support questions using company policy and product guidance.\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            zipOutputStream.putNextEntry(new ZipEntry(SKILL_NAME + "/SKILL.md"));
            zipOutputStream.write(skillMarkdown.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.closeEntry();
        }

        SkillFileDetails fileDetails = new SkillFileDetails(BinaryData.fromBytes(outputStream.toByteArray()))
            .setFilename(SKILL_NAME + ".zip");
        return new CreateSkillVersionFromFilesBody(Arrays.asList(fileDetails));
    }
}
