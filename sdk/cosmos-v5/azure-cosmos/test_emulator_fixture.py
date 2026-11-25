# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

"""Bounded fixture lifecycle tests without Rust or a real emulator."""

import importlib.util
import io
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch


MODULE = Path(__file__).resolve().parent
SPEC = importlib.util.spec_from_file_location("verify_emulator", MODULE / "verify-emulator.py")
verify_emulator = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(verify_emulator)


class EmulatorFixtureTests(unittest.TestCase):
    def run_fixture(self, script, action, timeout=5):
        processes = []
        popen = subprocess.Popen

        def start(*args, **kwargs):
            process = popen(*args, **kwargs)
            processes.append(process)
            return process

        with tempfile.TemporaryDirectory() as directory:
            with patch.object(verify_emulator.subprocess, "Popen", side_effect=start):
                action(verify_emulator.fixture([sys.executable, "-u", "-c", script],
                                               Path(directory), timeout))
        self.assertEqual(len(processes), 1)
        self.assertIsNotNone(processes[0].poll(), "Fixture leaked its child process")

    def test_timeout_terminates_the_process(self):
        def action(fixture):
            with self.assertRaisesRegex(RuntimeError, "Timed out"):
                with fixture:
                    self.fail("An emulator without readiness must not run tests")

        self.run_fixture("import time; time.sleep(60)", action, timeout=0.1)

    def test_early_exit_is_not_a_skipped_test(self):
        def action(fixture):
            with self.assertRaisesRegex(RuntimeError, "exited before readiness"):
                with fixture:
                    self.fail("An exited emulator must not run tests")

        self.run_fixture("raise SystemExit(7)", action)

    def test_test_failure_still_terminates_the_process(self):
        ready = {
            "event": "ready",
            "managementEndpoint": "http://127.0.0.1:12345/",
            "accountEndpoint": "http://127.0.0.1:12346/",
        }
        script = f"import time; print({json.dumps(json.dumps(ready))}); time.sleep(60)"

        def action(fixture):
            with self.assertRaisesRegex(ValueError, "test failure"):
                with fixture as actual:
                    self.assertEqual(actual, ready)
                    raise ValueError("test failure")

        with patch.object(verify_emulator.urllib.request, "build_opener") as opener:
            opener.return_value.open.return_value = io.BytesIO(b'{"status":"ok"}')
            with patch.object(verify_emulator.socket, "create_connection"):
                self.run_fixture(script, action)

    def test_readiness_cannot_redirect_health_checks_off_host(self):
        for value in ("https://127.0.0.1:12345/", "http://example.com:12345/",
                      "http://user:password@127.0.0.1:12345/", "http://127.0.0.1:12345/other"):
            with self.subTest(value=value):
                with self.assertRaisesRegex(RuntimeError, "loopback"):
                    verify_emulator.endpoint(value)

    def test_invalid_ready_record_terminates_the_process(self):
        def action(fixture):
            with self.assertRaisesRegex(RuntimeError, "ready record"):
                with fixture:
                    self.fail("Invalid readiness must not run tests")

        self.run_fixture('import time; print(\'{"event":"not-ready"}\'); time.sleep(60)', action)


if __name__ == "__main__":
    unittest.main()
