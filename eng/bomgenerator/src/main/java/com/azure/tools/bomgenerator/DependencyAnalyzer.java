// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import com.azure.tools.bomgenerator.models.BOMReport;
import com.azure.tools.bomgenerator.models.BomDependency;
import com.azure.tools.bomgenerator.models.BomDependencyErrorInfo;
import com.azure.tools.bomgenerator.models.BomDependencyNoVersion;
import com.azure.tools.bomgenerator.models.ConflictingDependency;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.azure.tools.bomgenerator.Utils.IGNORE_CONFLICT_LIST;
import static com.azure.tools.bomgenerator.Utils.RESOLVED_EXCLUSION_LIST;
import static com.azure.tools.bomgenerator.Utils.getResolvedArtifact;
import static com.azure.tools.bomgenerator.Utils.toBomDependencyNoVersion;

public class DependencyAnalyzer {
    private Map<BomDependencyNoVersion, BomDependency> inputDependencies = new HashMap<>();
    private Set<BomDependency> externalDependencies = new HashSet<>();
    private Set<BomDependency> bomEligibleDependencies = new HashSet<>();
    private Set<BomDependency> bomIneligibleDependencies = new HashSet<>();
    private Map<BomDependencyNoVersion, BomDependency> coreDependencyNameToDependency = new HashMap<>();
    private Map<BomDependency, BomDependencyErrorInfo> errorInfo = new HashMap();
    private Map<BomDependency, List<ConflictingDependency>> dependencyConflicts = new HashMap<>();
    private final String reportFileName;

    private Map<BomDependencyNoVersion, HashMap<String, Collection<BomDependency>>> nameToVersionToChildrenDependencyTree = new TreeMap<>(new Comparator<BomDependencyNoVersion>() {
        @Override
        public int compare(BomDependencyNoVersion o1, BomDependencyNoVersion o2) {
            return (o1.getGroupId() + o1.getArtifactId()).compareTo(o1.getGroupId() + o2.getArtifactId());
        }
    });
    private static Logger logger = LoggerFactory.getLogger(BomGenerator.class);

    DependencyAnalyzer(Collection<BomDependency> inputDependencies, Collection<BomDependency> externalDependencies, String reportFileName) {
        if (inputDependencies != null) {
            this.inputDependencies = inputDependencies.stream().collect(Collectors.toMap(Utils::toBomDependencyNoVersion, dependency -> dependency));
        }
        if (externalDependencies != null) {
            this.externalDependencies.addAll(externalDependencies);
        }
        this.reportFileName = reportFileName;
    }

    public Collection<BomDependency> getBomEligibleDependencies() {
        return this.bomEligibleDependencies;
    }

    public void reduce() {
        analyze();
        generateReport();
        this.bomEligibleDependencies.retainAll(this.inputDependencies.values());
    }

    public boolean validate() {
        analyze();
        generateReport();
        return nameToVersionToChildrenDependencyTree.values().stream().anyMatch(value -> value.size() > 1);
    }

    private void analyze() {
        pickCoreDependencyRoots();
        resolveTree();
        resolveConflicts();
        filterConflicts();
    }

    private void generateReport() {
        // From the input assemblies find the ones that have been dropped, along with why?
        Set<BomDependency> droppedDependencies = inputDependencies.values().stream().filter(dependency -> bomIneligibleDependencies.contains(dependency)).collect(Collectors.toSet());
        if (droppedDependencies.size() == 0) {
            return;
        }

        if (errorInfo.size() > 0) {
            errorInfo.keySet().stream().forEach(key -> {
                if (droppedDependencies.contains(key)) {
                    var conflictingDependencies = errorInfo.get(key).getConflictingDependencies();
                    var dependencyWithConflict = errorInfo.get(key).getDependencyWithConflict();
                    if (dependencyWithConflict != null) {
                        logger.info("Dropped dependency {}.", key.toString());
                    }

                    conflictingDependencies.stream().forEach(conflictingDependency -> {
                        if (!dependencyConflicts.containsKey(key)) {
                            dependencyConflicts.put(key, new ArrayList<>());
                        }
                        dependencyConflicts.get(key).add(conflictingDependency);
                        logger.info("\t\tIncludes dependency {}. Expected dependency {}", conflictingDependency.getActualDependency(), conflictingDependency.getExpectedDependency());
                    });
                }
            });
        }

        var bomReport = new BOMReport(reportFileName, dependencyConflicts);
        bomReport.generateReport();
    }

    private BomDependency getAzureCoreDependencyFromInput() {
        return inputDependencies.values().stream().filter(dependency -> dependency.getArtifactId().equals("azure-core")).findFirst().get();
    }

    private void pickCoreDependencyRoots() {
        BomDependency coreDependency = getAzureCoreDependencyFromInput();

        // Get all the dependencies of the azure-core dependency and put them in the coreDependencyMap.
        var coreDependencies = getDependencies(coreDependency);
        coreDependencyNameToDependency.put(toBomDependencyNoVersion(coreDependency), coreDependency);
        coreDependencies.forEach(dependency -> coreDependencyNameToDependency.put(toBomDependencyNoVersion(dependency), dependency));

        // Put all the core dependencies which are not external dependencies in the bomEligible list.
        for(var dependency : coreDependencyNameToDependency.values()) {
            if(!externalDependencies.contains(dependency)) {
                // If this is not an external dependency, it needs to be in the BOM.
                bomEligibleDependencies.add(dependency);
            }
        }
    }


    /* Create a tree map of all the input binaries into the following map.
     * {groupId_artifactId}: {v1} : {all ancestors that include this binary.}
     *                     : {v2} : {all ancestors that include this binary.}
     *                     : {v3} : {all ancestors that include this binary.}
     */
    private void resolveTree() {
        for (MavenDependency gaLibrary : inputDependencies.values()) {
            try {

                BomDependency parentDependency = new BomDependency(gaLibrary.getGroupId(), gaLibrary.getArtifactId(), gaLibrary.getVersion());

                addDependencyToDependencyTree(parentDependency, null, nameToVersionToChildrenDependencyTree);

                List<BomDependency> dependencies = getDependencies(gaLibrary);
                for (BomDependency dependency : dependencies) {
                    if (dependency.getScope() == ScopeType.TEST) {
                        continue;
                    }
                    if (RESOLVED_EXCLUSION_LIST.contains(dependency.getArtifactId())) {
                        continue;
                    }

                    BomDependency childDependency = new BomDependency(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
                    addDependencyToDependencyTree(childDependency, parentDependency, nameToVersionToChildrenDependencyTree);
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private static List<BomDependency> getDependencies(MavenDependency dependency) {
        try {
            MavenResolvedArtifact mavenResolvedArtifact = getResolvedArtifact(dependency);
            return Arrays.stream(mavenResolvedArtifact.getDependencies()).map(mavenDependency ->
                new BomDependency(mavenDependency.getCoordinate().getGroupId(),
                    mavenDependency.getCoordinate().getArtifactId(),
                    mavenDependency.getCoordinate().getVersion(),
                    mavenDependency.getScope())).collect(Collectors.toList());
        } catch (Exception ex) {
            logger.error(ex.toString());
        }

        return new ArrayList<>();
    }

    private static void addDependencyToDependencyTree(BomDependency dependency, BomDependency parentDependency, Map<BomDependencyNoVersion, HashMap<String, Collection<BomDependency>>> dependencyTree) {
        if (IGNORE_CONFLICT_LIST.contains(dependency.getArtifactId())) {
            return;
        }

        dependencyTree.computeIfAbsent(new BomDependencyNoVersion(dependency.getGroupId(), dependency.getArtifactId()), key -> new HashMap<>());

        var value = dependencyTree.get(dependency).computeIfAbsent(dependency.getVersion(), key -> new ArrayList<>());
        if(parentDependency != null) {
            value.add(parentDependency);
        }
    }

    private void updateErrorInfo(BomDependency droppedDependency, String expectedVersion) {
        if (!errorInfo.containsKey(droppedDependency)) {
            errorInfo.put(droppedDependency, new BomDependencyErrorInfo(new BomDependency(droppedDependency.getGroupId(), droppedDependency.getArtifactId(), expectedVersion)));
        }
    }

    private void updateErrorInfo(BomDependency droppedDependency, BomDependency actualDependency, String expectedVersion) {
        updateErrorInfo(droppedDependency, expectedVersion);
        errorInfo.get(droppedDependency).addConflictingDependency(actualDependency, new BomDependency(actualDependency.getGroupId(), actualDependency.getArtifactId(), expectedVersion));
    }


    private void makeDependencyInEligible(BomDependency dependency, BomDependency dependencyReason, String expectedVersion) {
        if (nameToVersionToChildrenDependencyTree.containsKey(dependency)) {
            HashMap<String, Collection<BomDependency>> versionToDependency = nameToVersionToChildrenDependencyTree.get(dependency);
            bomIneligibleDependencies.add(dependency);
            if (dependencyReason == null) {
                dependencyReason = dependency;
                updateErrorInfo(dependency, expectedVersion);
            } else {
                updateErrorInfo(dependency, dependencyReason, expectedVersion);
            }

            // Make all the dependencies that include these also ineligible.
            BomDependency finalDependencyReason = dependencyReason;
            versionToDependency.get(dependency.getVersion()).forEach(parent -> makeDependencyInEligible(parent, finalDependencyReason, expectedVersion));
        }
    }

    private void resolveConflict(BomDependencyNoVersion dependencyNoVersion) {
        Map<String, Collection<BomDependency>> versionToDependency = nameToVersionToChildrenDependencyTree.get(dependencyNoVersion);
        if (versionToDependency.size() > 1) {
            List<String> versionList = versionToDependency.keySet().stream().sorted(new DependencyVersionComparator()).collect(Collectors.toList());
            String eligibleVersion;

            logger.trace("Multiple version of the dependency {} included", dependencyNoVersion);

            // 1. We give preference to the dependency version that is included by azure-core.
            if (coreDependencyNameToDependency.containsKey(dependencyNoVersion)) {
                eligibleVersion = coreDependencyNameToDependency.get(dependencyNoVersion).getVersion();
                logger.trace(String.format("\tPicking the version used by azure-core - %s:%s", dependencyNoVersion, eligibleVersion));
            } else {
                // We check if any of the assemblies were directly added as inputs, if so, we give that preference.
                if (inputDependencies.containsKey(dependencyNoVersion)) {
                    eligibleVersion = inputDependencies.get(dependencyNoVersion).getVersion();
                } else {
                    eligibleVersion = versionList.get(versionList.size() - 1);
                }

                logger.trace(String.format("\tPicking the version %s:%s", dependencyNoVersion, eligibleVersion));
            }

            BomDependency dependency = new BomDependency(dependencyNoVersion.getGroupId(), dependencyNoVersion.getArtifactId(), eligibleVersion);
            if (!externalDependencies.contains(dependency)) {
                bomEligibleDependencies.add(dependency);
            }

            // All the other versions of this library are made ineligible.
            for (String version : versionList) {
                if (!version.equals(eligibleVersion)) {
                    makeDependencyInEligible(new BomDependency(dependency.getGroupId(), dependency.getArtifactId(), version), null, eligibleVersion);
                }
            }
        }
    }

    private void resolveConflicts() {
        nameToVersionToChildrenDependencyTree.keySet().stream().forEach(this::resolveConflict);
        bomEligibleDependencies.removeAll(bomIneligibleDependencies);
    }

    private void filterConflicts() {
        nameToVersionToChildrenDependencyTree.keySet().stream().forEach(
            key -> {
                HashMap<String, Collection<BomDependency>> versionToDependency = nameToVersionToChildrenDependencyTree.get(key);

                if (versionToDependency.size() == 1) {
                    BomDependency dependency = new BomDependency(key.getGroupId(), key.getArtifactId(), versionToDependency.keySet().stream().findFirst().get());
                    if (!bomIneligibleDependencies.contains(dependency)
                        && !externalDependencies.contains(dependency)) {
                        // No conflict, the library can be added to the list.
                        bomEligibleDependencies.add(dependency);
                    }
                }
            });
    }
}
