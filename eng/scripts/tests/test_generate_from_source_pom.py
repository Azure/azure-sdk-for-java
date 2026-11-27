# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import contextlib
import io
from pathlib import Path
import shutil
import sys
import unittest
from unittest.mock import patch
import uuid
import xml.etree.ElementTree as ET

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import generate_from_source_pom as generator
from pom_helper import Project, maven_xml_namespace


class KafkaFromSourceSelectionTests(unittest.TestCase):
    def setUp(self):
        self.workspace = Path.cwd() / (".kafka-ci-fromsource-" + uuid.uuid4().hex)
        self.workspace.mkdir()
        self.addCleanup(shutil.rmtree, self.workspace)
        self.pom_path = self.workspace / "ClientFromSourcePom.xml"
        parent = Project("com.azure:azure-client-sdk-parent", "/sdk/parents/azure-client-sdk-parent",
                         "/sdk/parents/azure-client-sdk-parent/pom.xml", None)
        cosmos = Project("com.azure:azure-cosmos", "/sdk/cosmos/azure-cosmos",
                         "/sdk/cosmos/azure-cosmos/pom.xml", parent.identifier)
        helper = Project("com.azure:azure-cosmos-test", "/sdk/cosmos/azure-cosmos-test",
                         "/sdk/cosmos/azure-cosmos-test/pom.xml", parent.identifier)
        kafka = Project("com.azure.cosmos.kafka:azure-cosmos-kafka-connect",
                        "/sdk/cosmos/azure-cosmos-kafka-connect",
                        "/sdk/cosmos/azure-cosmos-kafka-connect/pom.xml", parent.identifier)
        helper.add_dependency(cosmos.identifier)
        cosmos.add_dependent(helper.identifier)
        kafka.add_dependency(helper.identifier)
        helper.add_dependent(kafka.identifier)
        self.projects = {p.identifier: p for p in (parent, cosmos, helper, kafka)}
        self.kafka_module = kafka.module_path

    def generate(self, job_type, java_version):
        with patch.object(generator, "client_from_source_pom_path", str(self.pom_path)), \
                patch.object(generator, "load_client_artifact_identifiers", return_value={}), \
                patch.object(generator, "create_projects", return_value=self.projects), \
                patch.object(generator, "proj_path_has_yml", return_value=False), \
                contextlib.redirect_stdout(io.StringIO()):
            generator.create_from_source_pom("com.azure:azure-cosmos", None, None, False,
                                             job_type, java_version)
        return {module.text for module in ET.parse(self.pom_path).iter(maven_xml_namespace + "module")}

    def test_unsupported_test_jdks_do_not_reintroduce_transitive_kafka(self):
        for java_version in ("1.8", "1.11", "1.25"):
            with self.subTest(java_version=java_version):
                modules = self.generate("test", java_version)
                self.assertNotIn(self.kafka_module, modules)
                self.assertEqual({
                    "/sdk/cosmos/azure-cosmos/pom.xml",
                    "/sdk/cosmos/azure-cosmos-test/pom.xml",
                    "/sdk/parents/azure-client-sdk-parent/pom.xml",
                }, modules)

    def test_supported_test_jdks_keep_transitive_kafka(self):
        for java_version in ("1.17", "1.21", "17", "21"):
            with self.subTest(java_version=java_version):
                self.assertIn(self.kafka_module, self.generate("test", java_version))

    def test_build_analysis_and_unspecified_context_are_not_filtered(self):
        for job_type, java_version in (("Build", "1.8"), ("Analyze", "1.25"), ("", "1.8"), ("", "")):
            with self.subTest(job_type=job_type, java_version=java_version):
                self.assertIn(self.kafka_module, self.generate(job_type, java_version))


if __name__ == "__main__":
    unittest.main()
