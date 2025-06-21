// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.InputLocation;

import com.azure.sdk.build.tool.models.BuildErrorCode;
import com.azure.sdk.build.tool.models.OutdatedDependency;
import com.azure.sdk.build.tool.mojo.AzureSdkMojo;
import com.azure.sdk.build.tool.util.MavenUtils;
import static com.azure.sdk.build.tool.util.MojoUtils.failOrWarn;
import static com.azure.sdk.build.tool.util.MojoUtils.getAllDependencies;
import static com.azure.sdk.build.tool.util.MojoUtils.getDirectDependencies;
import static com.azure.sdk.build.tool.util.MojoUtils.getString;
import com.azure.sdk.build.tool.util.logging.Logger;

/**
 * Performs the following tasks:
 *
 * <ul>
 *   <li>Warnings about missing BOM.</li>
 *   <li>Warnings about not using the latest available version of BOM.</li>
 *   <li>Warnings about explicit dependency versions.</li>
 *   <li>Warnings about dependency clashes between Azure libraries and other dependencies.</li>
 *   <li>Warnings about using track one libraries.</li>
 *   <li>Warnings about out of date track two dependencies (BOM and individual libraries).</li>
 * </ul>
 */
public class DependencyCheckerTool implements Runnable {
    private static final Logger LOGGER = Logger.getInstance();

    private static final String AZURE_SDK_BOM_ARTIFACT_ID = "azure-sdk-bom";
    private static final String COM_MICROSOFT_AZURE_GROUP_ID = "com.microsoft.azure";

    /**
     * Runs the dependency checker tool.
     */
    public void run() {
        LOGGER.info("Running Dependency Checker Tool");

        checkForBom();
        checkForAzureSdkTrackOneDependencies();
    }

    private void checkForBom() {
        // we are looking for the azure-sdk-bom artifact ID listed as a dependency in the dependency management section
        DependencyManagement depMgmt = AzureSdkMojo.getMojo().getProject().getDependencyManagement();
        DependencyManagement originalDepMgmt =
                AzureSdkMojo.getMojo().getProject().getOriginalModel().getDependencyManagement();

        Optional<Dependency> bomDependency = Optional.empty();
        Optional<Dependency> originalBomDependency = Optional.empty();
        if (depMgmt != null) {
            bomDependency = depMgmt.getDependencies().stream()
                    .filter(d -> d.getArtifactId().equals(AZURE_SDK_BOM_ARTIFACT_ID))
                    .findAny();
        }

        if (originalDepMgmt != null) {
            originalBomDependency = originalDepMgmt.getDependencies().stream()
                    .filter(d -> d.getArtifactId().equals(AZURE_SDK_BOM_ARTIFACT_ID))
                    .findAny();
        }

        bomDependency = bomDependency.isPresent() ? bomDependency : originalBomDependency;

        if (bomDependency.isPresent()) {
            String latestAvailableBomVersion = MavenUtils.getLatestArtifactVersion("com.azure", "azure-sdk-bom");
            String usedBomVersion = bomDependency.get().getVersion();
            if (usedBomVersion.startsWith("${")) {
                String propertyName = usedBomVersion.substring(2, usedBomVersion.indexOf("}"));
                AzureSdkMojo.getMojo().getLog().info("BOM Version property name " + propertyName);
                usedBomVersion = AzureSdkMojo.getMojo().getProject().getProperties().getProperty(propertyName);
            }
            boolean isLatestBomVersion = usedBomVersion.equals(latestAvailableBomVersion);
            if (!isLatestBomVersion) {
                failOrWarn(AzureSdkMojo.getMojo()::isValidateLatestBomVersionUsed, BuildErrorCode.OUTDATED_DEPENDENCY, getString("outdatedBomDependency") + " using"
                    + " version" + usedBomVersion + " latest version: " + latestAvailableBomVersion, Arrays.asList(MavenUtils.toGAV(bomDependency.get())));
            }
            checkForAzureSdkDependencyVersions();
            AzureSdkMojo.getMojo().getReport().setBomVersion(usedBomVersion);
        } else {
            failOrWarn(AzureSdkMojo.getMojo()::isValidateAzureSdkBomUsed, BuildErrorCode.BOM_NOT_USED, getString("missingBomDependency"));
        }
    }

    private void checkForAzureSdkDependencyVersions() {
        List<Dependency> dependencies = AzureSdkMojo.getMojo().getProject().getDependencies();
        List<Dependency> dependenciesWithOverriddenVersions = dependencies.stream()
                .filter(dependency -> dependency.getGroupId().equals("com.azure"))
                .filter(dependency -> {
                    InputLocation location = dependency.getLocation("version");
                    // if the version is not coming from Azure SDK BOM, filter those dependencies
                    return !location.getSource().getModelId().startsWith("com.azure:azure-sdk-bom");
                }).collect(Collectors.toList());


        dependenciesWithOverriddenVersions.forEach(dependency -> failOrWarn(AzureSdkMojo.getMojo()::isValidateBomVersionsAreUsed,
                BuildErrorCode.BOM_VERSION_OVERRIDDEN, dependency.getArtifactId() + " " + getString("overrideBomVersion"),
            Arrays.asList(MavenUtils.toGAV(dependency))));

        List<Dependency> betaDependencies = dependencies.stream()
                .filter(dependency -> dependency.getGroupId().equals("com.azure"))
                .filter(dependency -> dependency.getVersion().contains("-beta"))
                .collect(Collectors.toList());

        betaDependencies.forEach(dependency -> failOrWarn(AzureSdkMojo.getMojo()::isValidateNoBetaLibraryUsed, BuildErrorCode.BETA_DEPENDENCY_USED,
                dependency.getArtifactId() + " " + getString("betaDependencyUsed"),
            Arrays.asList(MavenUtils.toGAV(dependency))));
    }

    private void checkForAzureSdkTrackOneDependencies() {
        // Check direct dependencies first for any 'com.microsoft.azure' group IDs. These are under the users direct
        // control, so they could try to upgrade to a newer 'com.azure' version instead.
        List<OutdatedDependency> outdatedDirectDependencies = getDirectDependencies().stream()
                .filter(a -> COM_MICROSOFT_AZURE_GROUP_ID.equals(a.getGroupId()))
                .map(AzureDependencyMapping::lookupReplacement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // check indirect dependencies too, but filter out any dependencies we've already discovered above
        List<OutdatedDependency> outdatedTransitiveDependencies = getAllDependencies().stream()
                .filter(d -> COM_MICROSOFT_AZURE_GROUP_ID.equals(d.getGroupId()))
                .map(AzureDependencyMapping::lookupReplacement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(d -> !outdatedDirectDependencies.contains(d))
                .collect(Collectors.toList());

        // The report is only concerned with GAV, so we simplify it here
        AzureSdkMojo.getMojo().getReport().setOutdatedDirectDependencies(outdatedDirectDependencies);
        AzureSdkMojo.getMojo().getReport().setOutdatedTransitiveDependencies(outdatedTransitiveDependencies);

        if (!outdatedDirectDependencies.isEmpty()) {
            // convert each track one dependency into actionable guidance
            StringBuilder message = new StringBuilder(getString("deprecatedDirectDependency"));
            for (OutdatedDependency outdatedDependency : outdatedDirectDependencies) {
                message.append("\n    - ")
                    .append(outdatedDependency.getOutdatedDependency())
                    .append(" --> ")
                    .append(outdatedDependency.getSuggestedReplacements());
            }

            List<String> outdatedDependencyGavs = outdatedDirectDependencies
                .stream()
                .map(OutdatedDependency::getOutdatedDependency)
                .collect(Collectors.toList());
            failOrWarn(AzureSdkMojo.getMojo()::isValidateNoDeprecatedMicrosoftLibraryUsed, BuildErrorCode.DEPRECATED_DEPENDENCY_USED, message.toString(), outdatedDependencyGavs);
        }
        if (!outdatedTransitiveDependencies.isEmpty()) {
            // convert each track one dependency into actionable guidance
            StringBuilder message = new StringBuilder(getString("deprecatedIndirectDependency"));
            for (OutdatedDependency outdatedDependency : outdatedDirectDependencies) {
                message.append("\n    - ")
                    .append(outdatedDependency.getOutdatedDependency());
            }
            List<String> outdatedTransitiveDependencyGavs = outdatedTransitiveDependencies
                .stream()
                .map(OutdatedDependency::getOutdatedDependency)
                .collect(Collectors.toList());
            failOrWarn(AzureSdkMojo.getMojo()::isValidateNoDeprecatedMicrosoftLibraryUsed, BuildErrorCode.DEPRECATED_TRANSITIVE_DEPENDENCY, message.toString(), outdatedTransitiveDependencyGavs);
        }
    }
}
