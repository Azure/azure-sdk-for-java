# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

"""Cold, nonpublishing build validation for the Cosmos v5 SDK."""

import argparse
from pathlib import Path
import shutil
import struct
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET
import zipfile


MODULE = Path(__file__).resolve().parent
ROOT = MODULE.parents[2]
BUILD = MODULE / "sdk-build.xml"
REACTOR = MODULE.parent / "reactor.xml"
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def verify_version_configuration():
    # Reuse the repository updater without modifying sources or the shared v4 registration.
    sys.path.insert(0, str(ROOT / "eng" / "versioning"))
    from update_versions import exception_list, update_versions
    from utils import load_version_map_from_file

    versions, external, local = {}, {}, {}
    load_version_map_from_file(str(ROOT / "eng/versioning/version_client.txt"), versions)
    load_version_map_from_file(str(ROOT / "eng/versioning/external_dependencies.txt"), external)
    load_version_map_from_file(str(MODULE.parent / "versions.txt"), local)
    require(set(local) == {"com.azure:azure-cosmos"}, "Only Cosmos may override the shared versions")
    require(local["com.azure:azure-cosmos"].current.startswith("5."), "Expected an isolated v5 version")
    versions.update(local)
    with tempfile.TemporaryDirectory() as directory:
        candidate = Path(directory) / BUILD.name
        shutil.copyfile(BUILD, candidate)
        exception_list.clear()
        update_versions(versions, external, str(candidate), True, False, [])
        require(not exception_list, f"Version validation failed: {exception_list}")
        require(candidate.read_bytes() == BUILD.read_bytes(),
                "sdk-build.xml differs from versions.txt or the shared dependency/plugin versions")


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
    project = ET.parse(BUILD).getroot()
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
                f"Failed or skipped build metadata tests: {report}")
        executed += int(suite.get("tests", "0"))
    require(executed >= 4, "Expected all four build metadata tests to execute")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--maven-repository", type=Path, required=True,
                        help="Empty isolated Maven repository; existing dependencies are never reused.")
    args = parser.parse_args()
    verify_version_configuration()
    subprocess.run([sys.executable, "-B", "-m", "unittest", "discover", "-s", str(MODULE),
                    "-p", "test_build_configuration.py"], cwd=ROOT, check=True)
    repository = args.maven_repository.resolve()
    require(not repository.exists() or not any(repository.iterdir()),
            f"Cold validation requires an empty Maven repository: {repository}")
    maven = shutil.which("mvn")
    require(maven, "Maven is required on PATH")
    command = [maven, "-B", "-ntp", f"-Dmaven.repo.local={repository}", "-Dgpg.skip"]

    def run(*arguments):
        subprocess.run(command + list(arguments), cwd=ROOT, check=True)

    # Bootstrap unchanged upstream dependencies, not Cosmos, using the documented quick-build options.
    run("-f", str(REACTOR), "-pl", "com.azure.v2:azure-core", "-am", "clean", "install",
        "-Dmaven.test.skip=true", "-Dcheckstyle.skip", "-Dmaven.javadoc.skip", "-Drevapi.skip",
        "-Dspotbugs.skip", "-Djacoco.skip", "-Dspotless.skip")
    run("-f", str(REACTOR), "-pl", "com.azure:azure-cosmos", "clean", "verify", "-Dspotless.apply.skip=true")
    effective = MODULE / "target" / "cosmos-v5-effective-pom.xml"
    run("-f", str(BUILD), "help:effective-pom", f"-Doutput={effective}")
    verify_effective_pom(effective)
    verify_artifacts()
    deployment_guard = MODULE / "target" / "deployment-guard"
    run("-f", str(BUILD), "deploy:deploy", "-Dmaven.deploy.skip=false",
        f"-DaltDeploymentRepository=cosmos-v5-deploy-guard::{deployment_guard.as_uri()}")
    require(not deployment_guard.exists(), "Foundation deployment must remain disabled")
    print("Cosmos v5 build verified; no package was deployed.")


if __name__ == "__main__":
    main()
