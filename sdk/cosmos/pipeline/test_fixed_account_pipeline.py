# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
# cspell:ignore DACCOUNT

"""Offline configuration checks for fixed-account thin-client and GSI stages."""

import json
from pathlib import Path
import unittest

import yaml


COSMOS = Path(__file__).resolve().parents[1]
REPO = COSMOS.parents[1]
TEMPLATE = "/eng/pipelines/templates/stages/archetype-sdk-tests-isolated.yml"
# Stage suffix -> endpoint variable, key variable, matrix filename, selected profiles.
STAGES = {
    "Http2NetworkFault": (
        "thinclient-test-endpoint", "thinclient-test-key",
        "live-http2-network-fault-platform-matrix.json",
        ["manual-http-network-fault", "manual-http-network-fault-compute"],
    ),
    "ThinClient": (
        "thinclient-test-endpoint", "thinclient-test-key",
        "live-thinclient-platform-matrix.json",
        ["thinclient", "query", "consistency-overrides"],
    ),
    "ThinClient_EndpointProbe": (
        "thinclient-test-endpoint", "thinclient-test-key",
        "live-thinclient-endpoint-probe-platform-matrix.json",
        ["thinclient-endpoint-probe"],
    ),
    "ThinClient_MultiRegion": (
        "thin-client-canary-multi-region-session-endpoint", "thin-client-canary-multi-region-session-key",
        "live-thinclient-multi-region-platform-matrix.json",
        ["fi-thinclient-multi-region"],
    ),
    "ThinClient_MultiMaster": (
        "thin-client-canary-multi-writer-session-endpoint", "thin-client-canary-multi-writer-session-key",
        "live-thinclient-multi-master-platform-matrix.json",
        ["fi-thinclient-multi-master"],
    ),
    "GSI": (
        "gsi-pipeline-uri", "gsi-pipeline-key",
        "live-gsi-platform-matrix.json", ["gsi"],
    ),
}


class FixedAccountPipelineTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        pipeline = yaml.safe_load((COSMOS / "tests.yml").read_text(encoding="utf-8"))
        cls.stages = {}
        for suffix in STAGES:
            name = f"Cosmos_Live_Test_{suffix}"
            stages = [
                stage for stage in pipeline["extends"]["parameters"]["stages"]
                if stage["parameters"].get("TestName") == name
            ]
            if len(stages) != 1:
                raise AssertionError(f"Expected exactly one {name} stage, found {len(stages)}")
            cls.stages[suffix] = stages[0]

    def test_fixed_account_disables_arm_provisioning(self):
        for name, stage in self.stages.items():
            with self.subTest(stage=name):
                self.assertEqual(stage["template"], TEMPLATE)
                self.assertIs(stage["parameters"].get("DisableAzureResourceCreation"), True)

    def test_test_runner_keeps_fixed_credentials_and_profiles(self):
        for name, (host, key, matrix_file, profiles) in STAGES.items():
            with self.subTest(stage=name):
                parameters = self.stages[name]["parameters"]
                args = self.additional_args(name)
                self.assertIn(f"-DACCOUNT_HOST=$({host})", args)
                self.assertIn(f"-DACCOUNT_KEY=$({key})", args)
                self.assertEqual(parameters["TestGoals"], "verify")
                self.assertIn("$(ProfileFlag)", parameters["TestOptions"])
                self.assertIn("$(AdditionalArgs)", parameters["TestOptions"])

                self.assertEqual(parameters["MatrixConfigs"][0]["Path"], f"sdk/cosmos/{matrix_file}")
                matrix = json.loads((COSMOS / matrix_file).read_text(encoding="utf-8"))
                self.assertTrue(matrix["include"])
                actual_profiles = []
                for leg in matrix["include"]:
                    actual_profiles.extend(leg["ProfileFlag"])
                    self.assertEqual(leg["ACCOUNT_CONSISTENCY"], "Session")
                self.assertCountEqual(actual_profiles, [f"-P{profile}" for profile in profiles])

    def test_cleanup_keeps_fixed_credentials(self):
        for name, (host, key, _, _) in STAGES.items():
            with self.subTest(stage=name):
                cleanup = [
                    step for step in self.stages[name]["parameters"]["PostSteps"]
                    if step.get("template") == "/sdk/cosmos/cleanup-test-resources.yml"
                ]
                self.assertEqual(len(cleanup), 1)
                self.assertEqual(cleanup[0]["parameters"]["AccountHost"], f"$({host})")
                self.assertEqual(cleanup[0]["parameters"]["AccountKey"], f"$({key})")

    def test_service_connection_and_federated_auth_are_preserved(self):
        template = yaml.safe_load((REPO / TEMPLATE.lstrip("/")).read_text(encoding="utf-8"))
        defaults = {parameter["name"]: parameter["default"] for parameter in template["parameters"]}
        for name, stage in self.stages.items():
            with self.subTest(stage=name):
                parameters = stage["parameters"]
                self.assertEqual(
                    parameters["CloudConfig"]["Public"]["ServiceConnection"],
                    "azure-sdk-tests-cosmos",
                )
                self.assertIs(parameters.get("UseFederatedAuth", defaults["UseFederatedAuth"]), True)

    def test_transport_selection_is_preserved(self):
        for name in STAGES:
            with self.subTest(stage=name):
                args = self.additional_args(name)
                thinclient_flags = [arg for arg in args if arg.startswith("-DCOSMOS.THINCLIENT_ENABLED=")]
                http2_flags = [arg for arg in args if arg.startswith("-DCOSMOS.HTTP2_ENABLED=")]
                if name in ("Http2NetworkFault", "GSI"):
                    self.assertEqual(thinclient_flags, [])
                    self.assertEqual(http2_flags, [])
                elif name == "ThinClient_EndpointProbe":
                    self.assertEqual(thinclient_flags, [])
                    self.assertEqual(http2_flags, ["-DCOSMOS.HTTP2_ENABLED=true"])
                else:
                    self.assertEqual(thinclient_flags, ["-DCOSMOS.THINCLIENT_ENABLED=true"])
                    self.assertEqual(http2_flags, ["-DCOSMOS.HTTP2_ENABLED=true"])

    def test_network_fault_setup_is_preserved(self):
        steps = self.stages["Http2NetworkFault"]["parameters"]["PreSteps"]
        self.assertTrue(any("apt-get install -y -qq iptables" in step.get("script", "") for step in steps))

    def additional_args(self, name):
        variables = {
            variable["name"]: variable["value"]
            for variable in self.stages[name]["parameters"]["AdditionalVariables"]
        }
        return variables["AdditionalArgs"].split()


if __name__ == "__main__":
    unittest.main()
