package com.azure.tools.bomgenerator;

import com.azure.tools.bomgenerator.models.BomDependency;
import com.azure.tools.bomgenerator.models.BomDependencyComparator;
import com.azure.tools.bomgenerator.models.BomDependencyNoVersion;
import com.azure.tools.bomgenerator.models.BomDependencyNonVersionComparator;
import org.apache.maven.model.Dependency;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystemBase;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomlessResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.azure.tools.bomgenerator.Utils.RESOLVED_EXCLUSION_LIST;

public class DependencyAnalyzer {
    private TreeSet<BomDependency> inputDependencies = new TreeSet<>(new BomDependencyComparator());
    private TreeSet<BomDependency> externalDependencies = new TreeSet<>(new BomDependencyComparator());
    private TreeSet<BomDependency> bomEligibleDependencies = new TreeSet<>(new BomDependencyComparator());
    private TreeMap<BomDependency, String> bomIneligibleDependencies = new TreeMap<>(new BomDependencyComparator());
    private TreeMap<BomDependencyNoVersion, BomDependency> coreDependencies = new TreeMap<>(new BomDependencyNonVersionComparator());

    private TreeMap<BomDependencyNoVersion, HashMap<String, Collection<BomDependency>>> nameToVersionToChildrenDependencyTree = new TreeMap<>(new BomDependencyNonVersionComparator());
    private static Logger logger = LoggerFactory.getLogger(BomGenerator.class);

    DependencyAnalyzer(Collection<BomDependency> inputDependencies, Collection<BomDependency> externalDependencies){
        if(inputDependencies != null) {
            this.inputDependencies.addAll(inputDependencies);
        }
        if(externalDependencies != null) {
            this.externalDependencies.addAll(externalDependencies);
        }
    }

    public Collection<BomDependency> getBomEligibleDependencies() {
        return this.bomEligibleDependencies;
    }

    public void reduce() {
        analyze();
        generateReport();
        this.bomEligibleDependencies.retainAll(this.inputDependencies);
    }

    public boolean validate() {
        analyze();
        return nameToVersionToChildrenDependencyTree.keySet().stream().anyMatch(key -> nameToVersionToChildrenDependencyTree.get(key).size() > 1);
    }

    private void analyze() {
        pickCoreDependencyRoots();
        resolveTree();
        resolveConflicts();
        filterConflicts();
    }

    private static List<BomDependency> getDependencies(MavenDependency dependency) {
        try {

            MavenResolvedArtifact mavenResolvedArtifact = null;

            mavenResolvedArtifact = getMavenResolver()
                .addDependency(dependency)
                .resolve()
                .withoutTransitivity()
                .asSingleResolvedArtifact();

            return Arrays.stream(mavenResolvedArtifact.getDependencies()).map(mavenDependency ->
                new BomDependency(mavenDependency.getCoordinate().getGroupId(),
                    mavenDependency.getCoordinate().getArtifactId(),
                    mavenDependency.getCoordinate().getVersion(),
                    mavenDependency.getScope())).collect(Collectors.toList());
        }
        catch (Exception ex) {
            logger.error(ex.toString());
        }

        return new ArrayList<>();
    }

    private void resolveTree() {
        for (MavenDependency gaLibrary : inputDependencies) {
            try {

                BomDependency parentDependency = new BomDependency(gaLibrary.getGroupId(), gaLibrary.getArtifactId(), gaLibrary.getVersion());
                addDependencyToDependencyTree(parentDependency, null, nameToVersionToChildrenDependencyTree);

                List<BomDependency> dependencies = getDependencies(gaLibrary);
                for (BomDependency dependency : dependencies) {
                    if (dependency.getScope() == ScopeType.TEST) {
                        continue;
                    }
                    if(RESOLVED_EXCLUSION_LIST.contains(dependency.getArtifactId())) {
                        continue;
                    }

                    BomDependency childDependency = new BomDependency(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
                    addDependencyToDependencyTree(childDependency, parentDependency, nameToVersionToChildrenDependencyTree);
                }
            }
            catch(Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private static MavenResolverSystemBase<PomEquippedResolveStage, PomlessResolveStage, MavenStrategyStage, MavenFormatStage> getMavenResolver() {
        return Maven.configureResolver().withMavenCentralRepo(true);
    }

    private static void addDependencyToDependencyTree(BomDependency dependency, BomDependency parentDependency, TreeMap<BomDependencyNoVersion, HashMap<String, Collection<BomDependency>>> dependencyTree) {
        if (!dependencyTree.containsKey(dependency)) {
            dependencyTree.put(new BomDependencyNoVersion(dependency.getGroupId(), dependency.getArtifactId()), new HashMap<>());
        }

        HashMap<String, Collection<BomDependency>> versionToParents = dependencyTree.get(dependency);
        if(!versionToParents.containsKey(dependency.getVersion())) {
            versionToParents.put(dependency.getVersion(), new ArrayList<>());
        }

        if(parentDependency != null) {
            versionToParents.get(dependency.getVersion()).add(parentDependency);
        }
    }


    private void makeDependencyInEligible(BomDependency dependency, String dropDependencyReason) {
        if(nameToVersionToChildrenDependencyTree.containsKey(dependency)) {
            HashMap<String, Collection<BomDependency>> versionToDependency = nameToVersionToChildrenDependencyTree.get(dependency);
            bomIneligibleDependencies.put(dependency, dropDependencyReason);
            if(dropDependencyReason == null) {
                dropDependencyReason = dependency.toString();
                logger.trace("\t\tDropping dependency {}", dependency.toString());
            }
            else {
                logger.trace("\t\tDropping dependency {} due to dependency {}", dependency.toString(), dropDependencyReason);
            }

            // Make all the dependencies that include these also ineligible.
            String finalDropDependencyReason = dropDependencyReason;
            versionToDependency.get(dependency.getVersion()).forEach(parent -> makeDependencyInEligible(parent, finalDropDependencyReason));
        }
    }

    private void pickCoreDependencyRoots() {
        BomDependency coreDependency = inputDependencies.stream().filter(dependency -> dependency.getArtifactId().equals("azure-core")).findFirst().get();
        coreDependencies.put(coreDependency, coreDependency);
        coreDependencies.putAll(getDependencies(coreDependency).stream().collect(Collectors.toMap(dependency -> dependency, dependency -> dependency)));
        this.bomEligibleDependencies.addAll(coreDependencies.values().stream().filter(dependency -> !externalDependencies.contains(dependency)).collect(Collectors.toList()));
    }

    private void resolveConflict(BomDependencyNoVersion dependencyNoVersion) {
        Map<String, Collection<BomDependency>> versionToDependency = nameToVersionToChildrenDependencyTree.get(dependencyNoVersion);
        if (versionToDependency.size() > 1) {
            List<String> versionList = versionToDependency.keySet().stream().sorted(new DependencyVersionComparator()).collect(Collectors.toList());
            String eligibleVersion;

            logger.trace("Multiple version of the dependency {} included", dependencyNoVersion);

            // 1. We give preference to the dependency version that is included by azure-core.
            if (coreDependencies.containsKey(dependencyNoVersion)) {
                eligibleVersion = coreDependencies.get(dependencyNoVersion).getVersion();
                logger.trace(String.format("\tPicking the version used by azure-core - %s:%s", dependencyNoVersion, eligibleVersion));
            } else {
                eligibleVersion = versionList.get(versionList.size() - 1);
                logger.trace(String.format("\tPicking the latest version %s:%s", dependencyNoVersion, eligibleVersion));
            }

            BomDependency dependency = new BomDependency(dependencyNoVersion.getGroupId(), dependencyNoVersion.getArtifactId(), eligibleVersion);
            if (!externalDependencies.contains(dependency)) {
                bomEligibleDependencies.add(dependency);
            }

            // All the other versions of this library are made ineligible.
            for (String version : versionList) {
                if (!version.equals(eligibleVersion)) {
                    makeDependencyInEligible(new BomDependency(dependency.getGroupId(), dependency.getArtifactId(), version), null);
                }
            }
        }
    }

    private void resolveConflicts() {
        nameToVersionToChildrenDependencyTree.keySet().stream().forEach(this::resolveConflict);
        bomEligibleDependencies.removeAll(bomIneligibleDependencies.keySet());
    }

    private void filterConflicts() {
        nameToVersionToChildrenDependencyTree.keySet().stream().forEach(
            key -> {
                HashMap<String, Collection<BomDependency>> versionToDependency = nameToVersionToChildrenDependencyTree.get(key);

                if (versionToDependency.size() == 1) {
                    BomDependency dependency = new BomDependency(key.getGroupId(), key.getArtifactId(), versionToDependency.keySet().stream().findFirst().get());
                    if (!bomIneligibleDependencies.containsKey(dependency)
                        && !externalDependencies.contains(dependency)) {
                        // No conflict, the library can be added to the list.
                        bomEligibleDependencies.add(dependency);
                    }
                }
            });
    }

    public void generateReport() {
        // From the input assemblies find the ones that have been dropped, along with why?
        List<BomDependency> droppedDependencies = inputDependencies.stream().filter(dependency -> bomIneligibleDependencies.containsKey(dependency)).collect(Collectors.toList());
        if (droppedDependencies.size() == 0) {
            return;
        }

        // From all these dependencies, see why they were dropped.
        logger.info("We dropped the following dependencies from the input list.");
        for (BomDependency dependency : droppedDependencies) {
            logger.info("Dependency {}, Reason {}", dependency.toString(), bomIneligibleDependencies.get(dependency));
        }

    }
}
