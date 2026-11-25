# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

"""Verify a pinned driver distribution can be statically linked into a JNI library.

This validation does not build Rust or put native binaries in the SDK artifact.
"""

import hashlib
import json
import os
from pathlib import Path
import platform
import shutil
import subprocess
import tempfile
import urllib.request


MODULE = Path(__file__).resolve().parent
PIN = MODULE / "native-driver.json"
SMOKE = MODULE / "src/test/native"
OUTPUT = MODULE / "target/native-validation"


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def target_for(system, machine, libc):
    if system == "Darwin" and machine == "arm64":
        return "darwin-arm64"
    if system == "Linux" and machine in ("x86_64", "amd64") and libc == "glibc":
        return "linux-amd64-glibc"
    raise RuntimeError(f"Unsupported native validation platform: {system}/{machine}/{libc}")


def verify_digest(path, expected):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    require(digest.hexdigest() == expected, f"SHA-256 mismatch: {path.name}")


def acquire(url, destination, expected):
    if destination.exists():
        verify_digest(destination, expected)
        return
    # Only promote a complete, verified download; corrupt cache entries fail closed.
    with tempfile.TemporaryDirectory(dir=destination.parent) as directory:
        candidate = Path(directory) / destination.name
        with urllib.request.urlopen(url, timeout=60) as response, candidate.open("wb") as stream:
            shutil.copyfileobj(response, stream)
        verify_digest(candidate, expected)
        candidate.replace(destination)


def verify_provenance(pin, target, provenance):
    require(provenance["source_commit"] == pin["rustSourceCommit"], "Rust source pin mismatch")
    require(provenance["native_interface_version"] == pin["nativeVersion"], "Native version pin mismatch")
    matches = [entry for entry in provenance["targets"] if entry["id"] == target]
    require(len(matches) == 1, f"Missing or duplicate provenance target: {target}")
    entry = matches[0]
    selected = pin["targets"][target]
    require(entry["static_library_path"] == selected["modulePath"] + "/libazurecosmosdriver.a",
            "Archive path mismatch")
    require(entry["static_library_sha256"] == selected["archiveSha256"], "Archive provenance mismatch")
    require(entry["header_sha256"] == pin["headerSha256"], "Header provenance mismatch")


def run(*args, **kwargs):
    subprocess.run([str(arg) for arg in args], check=True, timeout=180, **kwargs)


def main():
    target = target_for(platform.system(), platform.machine(), platform.libc_ver()[0])
    java_home = os.environ.get("JAVA_HOME")
    require(java_home, "Set JAVA_HOME to a JDK 17+ matching the host architecture")
    java_home = Path(java_home).resolve()
    compiler = shutil.which("cc")
    require(compiler, "A C compiler and platform development libraries are required")
    for executable in ("java", "javac"):
        require((java_home / "bin" / executable).is_file(), f"Missing JDK tool: {executable}")
    require((java_home / "include/jni.h").is_file(), "Missing JNI headers in JAVA_HOME")
    pin = json.loads(PIN.read_text())
    selected = pin["targets"][target]
    base_url = (f"https://raw.githubusercontent.com/{pin['repository']}/"
                f"{pin['distributionCommit']}")
    distribution = OUTPUT / pin["distributionCommit"] / target
    distribution.mkdir(parents=True, exist_ok=True)
    acquire(base_url + "/provenance.json", distribution / "provenance.json", pin["provenanceSha256"])
    verify_provenance(pin, target, json.loads((distribution / "provenance.json").read_text()))
    for filename, digest in (("azurecosmosdriver.h", pin["headerSha256"]),
                             ("libazurecosmosdriver.a", selected["archiveSha256"])):
        acquire(f"{base_url}/{selected['modulePath']}/{filename}", distribution / filename, digest)

    darwin = target == "darwin-arm64"
    suffix = "dylib" if darwin else "so"
    library_name = f"libcosmos_jni_smoke.{suffix}"
    with tempfile.TemporaryDirectory(prefix="cosmos-native-build-") as directory:
        build = Path(directory)
        classes = build / "classes"
        headers = build / "headers"
        classes.mkdir()
        headers.mkdir()
        run(java_home / "bin/javac", "--release", "17", "-h", headers, "-d", classes,
            MODULE / "src/main/java/com/azure/v2/cosmos/implementation/NativeLibraryLoader.java",
            SMOKE / "NativeDriverSmoke.java")
        flags = (["-dynamiclib", "-framework", "Security", "-framework", "CoreFoundation",
                  "-liconv", "-lc", "-lm",
                  "-Wl,-exported_symbol,_Java_com_azure_v2_cosmos_implementation_NativeDriverSmoke_verifyAndCreate"]
                 if darwin else
                 ["-shared", "-Wl,-z,defs", "-Wl,--exclude-libs,ALL",
                  "-lgcc_s", "-lutil", "-lrt", "-lpthread", "-lm", "-ldl", "-lc"])
        run(compiler, "-std=c11", "-Wall", "-Wextra", "-Werror", "-fPIC", "-fvisibility=hidden",
            "-I", java_home / "include", "-I", java_home / "include" / ("darwin" if darwin else "linux"),
            "-I", headers, "-I", distribution, SMOKE / "native_driver_smoke.c",
            distribution / "libazurecosmosdriver.a", *flags, "-o", build / library_name)
        # No absolute rpath or separate driver library: copy only the JNI library
        # and Java classes into a separate consumer directory; remove the original library.
        with tempfile.TemporaryDirectory(prefix="cosmos-native-consumer-") as consumer_directory:
            consumer = Path(consumer_directory)
            shutil.copytree(classes, consumer / "classes")
            shutil.copyfile(build / library_name, consumer / library_name)
            (build / library_name).unlink()
            environment = os.environ.copy()
            for key in ("LD_LIBRARY_PATH", "DYLD_LIBRARY_PATH", "DYLD_FALLBACK_LIBRARY_PATH"):
                environment.pop(key, None)
            run(java_home / "bin/java", "--enable-native-access=ALL-UNNAMED", "-Xcheck:jni",
                "-cp", consumer / "classes",
                "com.azure.v2.cosmos.implementation.NativeDriverSmoke",
                consumer / library_name, pin["nativeVersion"], cwd=consumer, env=environment)
    print(f"Verified distribution {pin['distributionCommit']} on {target}; no SDK package was modified.")


if __name__ == "__main__":
    main()
