# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

"""Cold, nonpublishing validation of the feature-branch Cosmos v5 foundation."""

import argparse
from pathlib import Path
import shutil
import struct
import subprocess
import xml.etree.ElementTree as ET
import zipfile


MODULE = Path(__file__).resolve().parent
ROOT = MODULE.parents[2]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def verify_effective_pom(path):
    project = ET.parse(path).getroot()
    plugins = {
        plugin.findtext("m:artifactId", namespaces=NS): plugin
        for plugin in project.findall("m:build/m:plugins/m:plugin", NS)
    }
    executions = {
        execution.findtext("m:id", namespaces=NS): execution
        for execution in plugins["maven-compiler-plugin"].findall("m:executions/m:execution", NS)
    }
    for name in ("default-compile", "base-modules-compile", "base-compile",
                 "default-testCompile", "base-testCompile"):
        execution = executions[name]
        require(execution.findtext("m:configuration/m:release", namespaces=NS) == "17",
                f"{name} must compile with --release 17")
        if "testCompile" in name:
            require(execution.findtext("m:configuration/m:testRelease", namespaces=NS) == "17",
                    f"{name} must compile tests with --release 17")
    require(plugins["maven-deploy-plugin"].findtext("m:configuration/m:skip", namespaces=NS) == "true",
            "The foundation must not enable Maven deployment")
    arg_line = plugins["maven-surefire-plugin"].findtext("m:configuration/m:argLine", namespaces=NS)
    require("@{jacocoUnitTestAgent}" in arg_line, "Inherited JaCoCo test hook was lost")


def verify_artifacts():
    project = ET.parse(MODULE / "pom.xml").getroot()
    version = project.findtext("m:version", namespaces=NS)
    base = MODULE / "target" / f"azure-cosmos-{version}"
    for suffix in (".jar", "-sources.jar", "-javadoc.jar", ".pom"):
        require(Path(str(base) + suffix).is_file(), f"Missing required artifact: {base}{suffix}")
    with zipfile.ZipFile(str(base) + ".jar") as jar:
        classes = [name for name in jar.namelist() if name.endswith(".class")]
        require("module-info.class" in classes, "Missing JPMS descriptor")
        require(all(name.endswith("package-info.class") or name == "module-info.class" for name in classes),
                "The foundation must not contain legacy implementation or placeholder public clients")
        require(not any(name.endswith((".dll", ".so", ".dylib")) for name in jar.namelist()),
                "Native binaries belong to a later migration slice")
        for name in classes:
            require(struct.unpack(">H", jar.read(name)[6:8])[0] == 61,
                    f"{name} does not have the Java 17 class-file baseline")
    with zipfile.ZipFile(str(base) + "-sources.jar") as jar:
        require("module-info.java" in jar.namelist(), "Missing module source")
        require("com/azure/cosmos/package-info.java" in jar.namelist(), "Missing package documentation")
    with zipfile.ZipFile(str(base) + "-javadoc.jar") as jar:
        require(any(name.endswith("module-summary.html") for name in jar.namelist()),
                "Missing module Javadoc")
    reports = list((MODULE / "target" / "surefire-reports").glob("TEST-*.xml"))
    require(reports, "JUnit results are missing")
    executed = 0
    for report in reports:
        suite = ET.parse(report).getroot()
        require(all(int(suite.get(key, "0")) == 0 for key in ("errors", "failures", "skipped")),
                f"Failed or skipped foundation tests: {report}")
        executed += int(suite.get("tests", "0"))
    require(executed >= 4, "Expected all four foundation tests to execute")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--maven-repository", type=Path, required=True,
                        help="Empty isolated Maven repository; existing dependencies are never reused.")
    args = parser.parse_args()
    repository = args.maven_repository.resolve()
    require(not repository.exists() or not any(repository.iterdir()),
            f"Cold validation requires an empty Maven repository: {repository}")
    maven = shutil.which("mvn")
    require(maven, "Maven is required on PATH")
    command = [maven, "-B", "-ntp", f"-Dmaven.repo.local={repository}", "-Dgpg.skip"]

    def run(*arguments):
        subprocess.run(command + list(arguments), cwd=ROOT, check=True)

    # Bootstrap unchanged upstream dependencies, not Cosmos, using the documented quick-build options.
    run("-pl", "com.azure.v2:azure-core", "-am", "clean", "install",
        "-Dmaven.test.skip=true", "-Dcheckstyle.skip", "-Dmaven.javadoc.skip", "-Drevapi.skip",
        "-Dspotbugs.skip", "-Djacoco.skip", "-Dspotless.skip")
    run("-f", str(MODULE / "pom.xml"), "clean", "verify", "-Dspotless.apply.skip=true")
    effective = MODULE / "target" / "foundation-effective-pom.xml"
    run("-f", str(MODULE / "pom.xml"), "help:effective-pom", f"-Doutput={effective}")
    verify_effective_pom(effective)
    verify_artifacts()
    deployment_guard = MODULE / "target" / "deployment-guard"
    run("-f", str(MODULE / "pom.xml"), "deploy:deploy", "-Dmaven.deploy.skip=false",
        f"-DaltDeploymentRepository=foundation-guard::{deployment_guard.as_uri()}")
    require(not deployment_guard.exists(), "Foundation deployment must remain disabled")
    print("Cosmos v5 foundation verified; no package was deployed.")


if __name__ == "__main__":
    main()
