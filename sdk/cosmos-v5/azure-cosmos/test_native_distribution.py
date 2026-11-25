# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

"""Failure-path tests for native distribution acquisition; no downloads or compiler required."""

import copy
import hashlib
import importlib.util
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
from urllib.error import URLError


MODULE = Path(__file__).resolve().parent
SPEC = importlib.util.spec_from_file_location("verify_native", MODULE / "verify-native.py")
verify_native = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(verify_native)


class NativeDistributionTests(unittest.TestCase):
    def test_verified_download_is_reused_without_network(self):
        payload = b"verified archive"
        digest = hashlib.sha256(payload).hexdigest()
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "driver.a"
            with patch.object(verify_native.urllib.request, "urlopen", return_value=io.BytesIO(payload)) as request:
                verify_native.acquire("https://example.invalid/driver.a", path, digest)
                verify_native.acquire("https://example.invalid/driver.a", path, digest)
                request.assert_called_once()
            self.assertEqual(path.read_bytes(), payload)

    def test_corrupt_download_is_not_promoted(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "driver.a"
            with patch.object(verify_native.urllib.request, "urlopen", return_value=io.BytesIO(b"corrupt")):
                with self.assertRaisesRegex(RuntimeError, "SHA-256 mismatch"):
                    verify_native.acquire("https://example.invalid/driver.a", path,
                                          hashlib.sha256(b"expected").hexdigest())
            self.assertFalse(path.exists())
            self.assertEqual(list(Path(directory).iterdir()), [])

    def test_corrupt_cached_archive_fails_without_redownload(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "driver.a"
            path.write_bytes(b"tampered")
            with patch.object(verify_native.urllib.request, "urlopen") as request:
                with self.assertRaisesRegex(RuntimeError, "SHA-256 mismatch"):
                    verify_native.acquire("https://example.invalid/driver.a", path,
                                          hashlib.sha256(b"expected").hexdigest())
                request.assert_not_called()

    def test_download_error_does_not_create_cache_entry(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "driver.a"
            with patch.object(verify_native.urllib.request, "urlopen", side_effect=URLError("unavailable")):
                with self.assertRaises(URLError):
                    verify_native.acquire("https://example.invalid/driver.a", path, "unused")
            self.assertEqual(list(Path(directory).iterdir()), [])

    def test_provenance_must_match_source_version_archive_and_header(self):
        pin = {
            "rustSourceCommit": "source",
            "nativeVersion": "version",
            "headerSha256": "header",
            "targets": {"target": {"modulePath": "linux/amd64", "archiveSha256": "archive"}},
        }
        provenance = {
            "source_commit": "source",
            "native_interface_version": "version",
            "targets": [{
                "id": "target",
                "static_library_path": "linux/amd64/libazurecosmosdriver.a",
                "static_library_sha256": "archive",
                "header_sha256": "header",
            }],
        }
        verify_native.verify_provenance(pin, "target", provenance)
        for field in ("source_commit", "native_interface_version", "static_library_path",
                      "static_library_sha256", "header_sha256"):
            with self.subTest(field=field):
                changed = copy.deepcopy(provenance)
                owner = changed if field in changed else changed["targets"][0]
                owner[field] = "mismatch"
                with self.assertRaises(RuntimeError):
                    verify_native.verify_provenance(pin, "target", changed)
        for targets in ([], provenance["targets"] * 2):
            with self.subTest(targets=targets):
                with self.assertRaisesRegex(RuntimeError, "Missing or duplicate"):
                    verify_native.verify_provenance(pin, "target", dict(provenance, targets=targets))

    def test_unsupported_platforms_fail_instead_of_skipping(self):
        for system, machine, libc in (("Windows", "AMD64", ""), ("Linux", "x86_64", "musl"),
                                      ("Darwin", "x86_64", ""), ("Linux", "aarch64", "glibc")):
            with self.subTest(system=system, machine=machine, libc=libc):
                with self.assertRaisesRegex(RuntimeError, "Unsupported native validation platform"):
                    verify_native.target_for(system, machine, libc)


if __name__ == "__main__":
    unittest.main()
