package com.azure.tools.bomgenerator.models;

import org.apache.maven.model.Dependency;

import java.util.Comparator;

public abstract class DependencyComparator implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 instanceof BomDependency && o2 instanceof BomDependency) {
            BomDependency bomSource = (BomDependency) o1;
            BomDependency bomTarget = (BomDependency) o2;

            return getDependencyName(bomSource).compareTo(getDependencyName(bomTarget));

        } else if (o1 instanceof Dependency && o2 instanceof Dependency) {
            Dependency depSource = (Dependency) o1;
            Dependency depTarget = (Dependency) o2;

            return getDependencyName(depSource).compareTo(getDependencyName(depTarget));
        } else if (o1 instanceof BomDependencyNoVersion && o2 instanceof BomDependencyNoVersion) {
            BomDependencyNoVersion bomSource = (BomDependencyNoVersion) o1;
            BomDependencyNoVersion bomTarget = (BomDependencyNoVersion) o2;

            return getDependencyName(bomSource).compareTo(getDependencyName(bomTarget));

        }

        return -1;
    }

    abstract String getDependencyName(BomDependency dependency);
    abstract String getDependencyName(BomDependencyNoVersion dependencyNoVersion);
    abstract String getDependencyName(Dependency dependency);
}
