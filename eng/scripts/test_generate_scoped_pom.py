import contextlib
import io
import json
import os
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET

import generate_scoped_pom as generator
from pom_helper import Project, maven_xml_namespace


class GenerateBuildPomTests(unittest.TestCase):
    def setUp(self):
        temporary_directory = tempfile.TemporaryDirectory()
        self.addCleanup(temporary_directory.cleanup)
        self.pom_path = os.path.join(temporary_directory.name, 'ClientPom.xml')
        self.output = io.StringIO()
        self.projects = {
            'com.azure:azure-client-sdk-parent': Project(
                'com.azure:azure-client-sdk-parent', '/sdk/parents/azure-client-sdk-parent',
                '/sdk/parents/azure-client-sdk-parent/pom.xml', None),
            'com.azure:requested': Project(
                'com.azure:requested', '/sdk/requested/requested',
                '/sdk/requested/requested/pom.xml', 'com.azure:azure-client-sdk-parent'),
            'com.azure:dependency': Project(
                'com.azure:dependency', '/sdk/dependency/dependency',
                '/sdk/dependency/dependency/pom.xml', 'com.azure:azure-client-sdk-parent'),
            'com.azure:dependent': Project(
                'com.azure:dependent', '/sdk/dependent/dependent',
                '/sdk/dependent/dependent/pom.xml', 'com.azure:azure-client-sdk-parent'),
            'com.azure:additional': Project(
                'com.azure:additional', '/sdk/additional/additional',
                '/sdk/additional/additional/pom.xml', None),
            'com.azure:unrelated': Project(
                'com.azure:unrelated', '/sdk/unrelated/unrelated',
                '/sdk/unrelated/unrelated/pom.xml', None),
        }
        self.projects['com.azure:requested'].add_dependency('com.azure:dependency')
        self.projects['com.azure:dependency'].add_dependent('com.azure:requested')
        self.projects['com.azure:requested'].add_dependent('com.azure:dependent')
        self.projects['com.azure:dependent'].add_dependency('com.azure:requested')
        self.projects['com.azure:additional'].add_dependency('com.azure:dependency')
        self.projects['com.azure:dependency'].add_dependent('com.azure:additional')

        for mock_patch in [
            patch.object(generator, 'client_pom_path', self.pom_path),
            patch.object(generator, 'load_client_artifact_identifiers', return_value={}),
            patch.object(generator, 'create_projects', return_value=self.projects),
            patch.object(generator, 'proj_path_has_yml', return_value=False),
        ]:
            mock_patch.start()
            self.addCleanup(mock_patch.stop)

    def modules(self):
        return [module.text for module in ET.parse(self.pom_path).getroot().findall(
            maven_xml_namespace + 'modules/' + maven_xml_namespace + 'module')]

    def variable(self, name):
        prefix = '##vso[task.setvariable variable={};]'.format(name)
        return next(line[len(prefix):] for line in self.output.getvalue().splitlines() if line.startswith(prefix))

    def generate(self, additional_modules=None, from_source=True, skip_linting=None):
        with contextlib.redirect_stdout(self.output):
            generator.create_pom('com.azure:requested', additional_modules, skip_linting, False, from_source)

    def test_non_source_includes_dependencies_and_parents_but_not_dependents(self):
        self.generate(from_source=False)
        self.assertEqual([
            '/sdk/dependency/dependency/pom.xml',
            '/sdk/parents/azure-client-sdk-parent/pom.xml',
            '/sdk/requested/requested/pom.xml',
        ], self.modules())

    def test_source_includes_dependencies_dependents_and_parents(self):
        self.generate()
        self.assertEqual([
            '/sdk/dependency/dependency/pom.xml',
            '/sdk/dependent/dependent/pom.xml',
            '/sdk/parents/azure-client-sdk-parent/pom.xml',
            '/sdk/requested/requested/pom.xml',
        ], self.modules())

    def test_non_source_includes_additional_modules_and_their_dependencies(self):
        self.generate('com.azure:additional', from_source=False)
        self.assertEqual([
            '/sdk/additional/additional/pom.xml',
            '/sdk/dependency/dependency/pom.xml',
            '/sdk/parents/azure-client-sdk-parent/pom.xml',
            '/sdk/requested/requested/pom.xml',
        ], self.modules())

    def test_source_includes_additional_modules_and_their_dependencies(self):
        self.generate('com.azure:additional')
        self.assertEqual([
            '/sdk/additional/additional/pom.xml',
            '/sdk/dependency/dependency/pom.xml',
            '/sdk/dependent/dependent/pom.xml',
            '/sdk/parents/azure-client-sdk-parent/pom.xml',
            '/sdk/requested/requested/pom.xml',
        ], self.modules())

    def test_duplicate_additional_modules_are_not_repeated(self):
        self.generate('com.azure:requested,com.azure:additional,com.azure:additional', from_source=False)
        self.assertEqual(4, len(self.modules()))
        self.assertEqual(len(self.modules()), len(set(self.modules())))

    def test_empty_additional_modules_are_ignored(self):
        self.generate('', from_source=False)
        self.assertEqual(3, len(self.modules()))

    def test_non_source_checkout_variables_include_dependencies_but_not_dependents(self):
        self.generate(from_source=False)
        self.assertEqual([
            '/sdk/dependency', '/sdk/parents', '/sdk/requested',
        ], json.loads(self.variable('SparseCheckoutDirectories')))
        self.assertEqual('requested', self.variable('ServiceDirectories'))

    def test_source_checkout_variables_include_dependencies_but_not_dependent_services(self):
        self.generate()
        self.assertEqual([
            '/sdk/dependency', '/sdk/dependent', '/sdk/parents', '/sdk/requested',
        ], json.loads(self.variable('SparseCheckoutDirectories')))
        self.assertEqual('requested', self.variable('ServiceDirectories'))

    def test_skip_linting_uses_the_selected_modules(self):
        self.generate('com.azure:additional', from_source=False, skip_linting='SkipLinting')
        self.assertEqual('!com.azure:additional', self.variable('SkipLinting'))

    def test_cli_accepts_pipeline_boolean_values(self):
        for from_source in ['true', 'false', 'True', 'False']:
            with self.subTest(from_source=from_source):
                with patch('sys.argv', ['generate_scoped_pom.py', '--al', 'com.azure:requested',
                                        '--from-source', from_source]):
                    with contextlib.redirect_stdout(self.output):
                        generator.main()
                expected_count = 4 if from_source.lower() == 'true' else 3
                self.assertEqual(expected_count, len(self.modules()))


if __name__ == '__main__':
    unittest.main()