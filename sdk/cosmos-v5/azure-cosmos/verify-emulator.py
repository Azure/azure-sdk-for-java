# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

"""Build a separately pinned test emulator and verify bounded start/health/stop.

This is SDK engineering infrastructure, not the production native driver.
"""

import argparse
from contextlib import contextmanager
import json
import os
from pathlib import Path
import shutil
import socket
import subprocess
import tempfile
import time
import urllib.parse
import urllib.request


MODULE = Path(__file__).resolve().parent
OUTPUT = MODULE / "target/emulator"
SOURCE = OUTPUT / "source"
CARGO_HOME = OUTPUT / "cargo"
CONFIG = MODULE / "src/test/resources/native-emulator.json"


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def run(*arguments, **kwargs):
    return subprocess.run([str(arg) for arg in arguments], check=True, **kwargs)


def prepare(pin):
    OUTPUT.mkdir(parents=True, exist_ok=True)
    if not SOURCE.exists():
        with tempfile.TemporaryDirectory(prefix="source-", dir=OUTPUT) as directory:
            candidate = Path(directory) / "repo"
            run("git", "init", "--quiet", candidate)
            run("git", "-C", candidate, "fetch", "--depth=1", pin["repository"], pin["sourceRevision"],
                timeout=300)
            run("git", "-C", candidate, "checkout", "--detach", "FETCH_HEAD")
            candidate.replace(SOURCE)
    revision = run("git", "-C", SOURCE, "rev-parse", "HEAD", capture_output=True, text=True).stdout.strip()
    require(revision == pin["sourceRevision"], "Emulator source revision does not match emulator.json")
    changes = run("git", "-C", SOURCE, "status", "--porcelain", capture_output=True, text=True).stdout
    require(not changes, "Emulator source cache has local changes; use an unmodified pinned checkout")


def build(pin):
    rustup = shutil.which("rustup")
    require(rustup, f"Install rustup and toolchain {pin['rustVersion']} to build the pinned test emulator")
    environment = os.environ.copy()
    environment["CARGO_HOME"] = str(CARGO_HOME)
    run(rustup, "run", pin["rustVersion"], "cargo", "build", "--locked", "--release",
        "-p", "azure_data_cosmos_emulator", "--target-dir", OUTPUT / "build",
        cwd=SOURCE, env=environment, timeout=1800)
    executable = OUTPUT / "build/release/azure_data_cosmos_emulator"
    require(executable.is_file() and os.access(executable, os.X_OK), "Built emulator executable is missing")
    return executable


def endpoint(value):
    url = urllib.parse.urlsplit(value)
    require(url.scheme == "http" and url.hostname == "127.0.0.1" and url.port
            and url.username is None and url.password is None and url.path in ("", "/")
            and not url.query and not url.fragment,
            f"Emulator endpoint must be a loopback HTTP root: {value}")
    return url


def wait_ready(process, path, timeout):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        require(process.poll() is None, f"Emulator exited before readiness: {process.returncode}")
        text = path.read_text()
        if "\n" in text:
            ready = json.loads(text.splitlines()[0])
            require(ready.get("event") == "ready", "Emulator did not emit a ready record")
            endpoint(ready["managementEndpoint"])
            endpoint(ready["accountEndpoint"])
            return ready
        time.sleep(0.05)
    raise RuntimeError("Timed out waiting for emulator readiness")


@contextmanager
def fixture(command, directory, timeout=60):
    directory.mkdir(parents=True, exist_ok=True)
    ready_path = directory / "ready.jsonl"
    with ready_path.open("w") as stdout, (directory / "stderr.log").open("w") as stderr:
        with subprocess.Popen([str(arg) for arg in command], stdout=stdout, stderr=stderr) as process:
            try:
                ready = wait_ready(process, ready_path, timeout)
                opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
                with opener.open(ready["managementEndpoint"].rstrip("/") + "/health", timeout=10) as response:
                    require(json.load(response).get("status") == "ok", "Emulator health check failed")
                gateway = endpoint(ready["accountEndpoint"])
                with socket.create_connection((gateway.hostname, gateway.port), timeout=10):
                    pass
                yield ready
            finally:
                if process.poll() is None:
                    process.terminate()
                    try:
                        process.wait(timeout=10)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait(timeout=10)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--prepare-only", action="store_true",
                        help="Fetch/verify source without building, for CI feed authentication.")
    parser.add_argument("--configure-feed", action="store_true",
                        help="Copy the pinned Cargo feed template before CargoAuthenticate@0.")
    args = parser.parse_args()
    pin = json.loads((MODULE / "emulator.json").read_text())
    prepare(pin)
    CARGO_HOME.mkdir(parents=True, exist_ok=True)
    if args.configure_feed:
        require(args.prepare_only, "--configure-feed requires --prepare-only")
        destination = CARGO_HOME / "config.toml"
        require(not destination.exists(), "Use a fresh emulator Cargo cache when configuring the CI feed")
        shutil.copyfile(SOURCE / "eng/templates/config.toml.template", destination)
    if args.prepare_only:
        print(f"Prepared emulator source {pin['sourceRevision']}; no driver build is required.")
        return
    executable = build(pin)
    with fixture([executable, "--config", CONFIG], OUTPUT / "run") as ready:
        print(f"Emulator ready and healthy: {ready['accountEndpoint']}")
    print("Emulator stopped; no process was left running.")


if __name__ == "__main__":
    main()
