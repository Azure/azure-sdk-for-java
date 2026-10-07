# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import contextlib
import io
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest
import uuid
import xml.etree.ElementTree as ET

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'versioning'))
from project_overrides import namespace, prepare_project_overrides


def pom(contents):
    return ('<!-- fixture copyright -->\n'
            '<project xmlns="http://maven.apache.org/POM/4.0.0">\n'
            '<modelVersion>4.0.0</modelVersion>\n' + contents + '\n</project>\n')


def write(path, contents):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(contents, encoding='utf-8')


class ProjectOverridesTests(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = Path(directory.name)
        self.aggregate = self.root / 'ClientPom.xml'
        self.overrides = self.root / 'overrides.json'
        write(self.overrides, json.dumps({'compat': [{'fixture:api': '1.0'}]}))
        self.parent = self.root / 'parent' / 'pom.xml'
        self.support = self.root / 'support' / 'pom.xml'
        self.consumer = self.root / 'consumer' / 'pom.xml'
        write(self.aggregate, pom(
            '<groupId>fixture</groupId><artifactId>aggregate</artifactId><version>1.0</version><packaging>pom</packaging>'
            '<modules><module>/parent/pom.xml</module><module>support</module><module>consumer/pom.xml</module></modules>'))
        write(self.parent, pom(
            '<groupId>fixture</groupId><artifactId>parent</artifactId><version>1.0</version><packaging>pom</packaging>'))
        self.parent_reference = (
            '<parent><groupId>fixture</groupId><artifactId>parent</artifactId><version>1.0</version>'
            '<relativePath>../parent/pom.xml</relativePath></parent>')
        write(self.support, pom(self.parent_reference + '<artifactId>support</artifactId>'
                               '<dependencies><dependency><groupId>fixture</groupId><artifactId>api</artifactId>'
                               '<version>2.0</version></dependency></dependencies>'))
        self.consumer_contents = (
            self.parent_reference + '<artifactId>consumer</artifactId><dependencies>'
            '<dependency><groupId>fixture</groupId><artifactId>support</artifactId><version>1.0</version></dependency>'
            '</dependencies>')
        write(self.consumer, pom(self.consumer_contents))

    def prepare(self, projects='fixture:consumer', override='compat'):
        with contextlib.redirect_stdout(io.StringIO()):
            prepare_project_overrides(self.aggregate, projects, self.overrides, override)
        return ET.parse(self.consumer).getroot()

    def managed(self, root):
        return root.findall(namespace + 'dependencyManagement/' + namespace + 'dependencies/' + namespace + 'dependency')

    def test_transitive_override_does_not_add_a_direct_dependency(self):
        root = self.prepare()
        self.assertEqual('1.0', self.managed(root)[0].findtext(namespace + 'version'))
        self.assertEqual(['support'], [
            d.findtext(namespace + 'artifactId')
            for d in root.findall(namespace + 'dependencies/' + namespace + 'dependency')])

    def test_unselected_modules_and_parent_remain_byte_identical(self):
        originals = {path: path.read_bytes() for path in [self.aggregate, self.parent, self.support]}
        self.prepare()
        for path, contents in originals.items():
            self.assertEqual(contents, path.read_bytes())

    def test_direct_and_profile_dependencies_are_overridden(self):
        write(self.consumer, pom(self.consumer_contents +
            '<profiles><profile><id>alternate</id><dependencies><dependency>'
            '<groupId>fixture</groupId><artifactId>api</artifactId><version>2.0</version>'
            '<scope>test</scope><classifier>tests</classifier><type>test-jar</type>'
            '</dependency></dependencies></profile></profiles>'))
        root = self.prepare()
        dependency = root.find(namespace + 'profiles/' + namespace + 'profile/' +
                               namespace + 'dependencies/' + namespace + 'dependency')
        self.assertEqual('1.0', dependency.findtext(namespace + 'version'))
        self.assertEqual('test-jar', dependency.findtext(namespace + 'type'))
        self.assertEqual('tests', dependency.findtext(namespace + 'classifier'))
        self.assertEqual('test', dependency.findtext(namespace + 'scope'))

    def test_existing_management_is_updated_without_duplicates(self):
        write(self.consumer, pom(self.consumer_contents +
            '<dependencyManagement><dependencies><dependency><groupId>fixture</groupId>'
            '<artifactId>api</artifactId><version>2.0</version></dependency></dependencies></dependencyManagement>'))
        root = self.prepare()
        self.assertEqual(1, len(self.managed(root)))
        self.assertEqual('1.0', self.managed(root)[0].findtext(namespace + 'version'))

    def test_inherited_direct_dependency_is_overridden_locally(self):
        write(self.parent, pom(
            '<groupId>fixture</groupId><artifactId>parent</artifactId><version>1.0</version><packaging>pom</packaging>'
            '<dependencies><dependency><groupId>fixture</groupId><artifactId>api</artifactId>'
            '<version>2.0</version><scope>provided</scope><optional>true</optional>'
            '</dependency></dependencies>'))
        root = self.prepare()
        dependency = root.findall(namespace + 'dependencies/' + namespace + 'dependency')[-1]
        self.assertEqual('1.0', dependency.findtext(namespace + 'version'))
        self.assertEqual('provided', dependency.findtext(namespace + 'scope'))
        self.assertEqual('true', dependency.findtext(namespace + 'optional'))

    def test_inherited_version_property_is_shadowed_only_in_selected_project(self):
        write(self.parent, pom(
            '<groupId>fixture</groupId><artifactId>parent</artifactId><version>1.0</version><packaging>pom</packaging>'
            '<properties><api.version>2.0</api.version><!-- {x-version-update;fixture:api;external_dependency} -->'
            '</properties>'))
        root = self.prepare()
        self.assertEqual('1.0', root.findtext(namespace + 'properties/' + namespace + 'api.version'))
        self.assertIn('<api.version>2.0</api.version>', self.parent.read_text())

    def test_marked_properties_and_enforcer_allowlists_use_override(self):
        write(self.consumer, pom(self.consumer_contents +
            '<properties><api.version>2.0</api.version><!-- {x-version-update;fixture:api;external_dependency} -->'
            '</properties><build><plugins><plugin><groupId>org.apache.maven.plugins</groupId>'
            '<artifactId>maven-enforcer-plugin</artifactId><configuration><rules><bannedDependencies><includes>'
            '<include>fixture:api:[2.0]</include><!-- {x-include-update;fixture:api;external_dependency} -->'
            '<include>fixture:unrelated:[7.0]</include></includes></bannedDependencies></rules></configuration>'
            '</plugin></plugins></build>'))
        root = self.prepare()
        self.assertEqual('1.0', root.findtext(namespace + 'properties/' + namespace + 'api.version'))
        includes = list(root.iter(namespace + 'includes'))[0]
        self.assertEqual('append', includes.get('combine.children'))
        self.assertEqual(['fixture:api:[1.0]', 'fixture:unrelated:[7.0]'], [i.text for i in includes])

    def test_multiple_selected_projects_and_duplicate_selectors(self):
        self.prepare('fixture:consumer,fixture:support,fixture:consumer')
        self.assertEqual('1.0', ET.parse(self.support).findtext(
            namespace + 'dependencies/' + namespace + 'dependency/' + namespace + 'version'))

    def test_preparation_is_idempotent_and_preserves_copyright(self):
        self.prepare()
        first = self.consumer.read_bytes()
        self.prepare()
        self.assertEqual(first, self.consumer.read_bytes())
        self.assertTrue(self.consumer.read_text().startswith('<!-- fixture copyright -->\n'))

    def test_invalid_selection_does_not_change_any_pom(self):
        original = self.consumer.read_bytes()
        for projects in ['', 'fixture:missing', 'fixture:consumer,fixture:missing', 'fixture:parent']:
            with self.subTest(projects=projects), self.assertRaises(ValueError):
                self.prepare(projects)
            self.assertEqual(original, self.consumer.read_bytes())

    def test_unknown_or_empty_override_fails_before_writing(self):
        original = self.consumer.read_bytes()
        for name in ['missing', 'empty']:
            write(self.overrides, json.dumps({'empty': []}))
            with self.subTest(name=name), self.assertRaises(ValueError):
                self.prepare(override=name)
            self.assertEqual(original, self.consumer.read_bytes())

    def test_empty_version_or_exception_key_fails_before_writing(self):
        original = self.consumer.read_bytes()
        for entry in [{'fixture:api': ''}, {'pinned_fixture:api': '1.0'}]:
            write(self.overrides, json.dumps({'compat': [entry]}))
            with self.subTest(entry=entry), self.assertRaises(ValueError):
                self.prepare()
            self.assertEqual(original, self.consumer.read_bytes())

    def test_cyclic_parent_hierarchy_fails_without_writing(self):
        write(self.parent, pom(
            '<parent><groupId>fixture</groupId><artifactId>consumer</artifactId><version>1.0</version></parent>'
            '<groupId>fixture</groupId><artifactId>parent</artifactId><version>1.0</version><packaging>pom</packaging>'))
        original = self.support.read_bytes()
        with self.assertRaisesRegex(ValueError, 'Cyclic'):
            self.prepare('fixture:support')
        self.assertEqual(original, self.support.read_bytes())

    def test_legacy_individual_file_override_remains_supported(self):
        script = Path(__file__).resolve().parents[1] / 'versioning' / 'update_versions.py'
        write(self.consumer, pom(self.parent_reference + '<artifactId>consumer</artifactId><dependencies><dependency>'
            '<groupId>io.projectreactor</groupId><artifactId>reactor-core</artifactId>\n'
            '<version>3.7.19</version><!-- {x-version-update;io.projectreactor:reactor-core;external_dependency} -->\n'
            '</dependency></dependencies>'))
        result = subprocess.run([sys.executable, str(script), '--skip-readme', '--target-file', str(self.consumer),
                                 '--version-override', 'reactor_2020'], capture_output=True, text=True)
        self.assertEqual(0, result.returncode, result.stdout + result.stderr)
        root = ET.parse(self.consumer).getroot()
        self.assertEqual('3.4.15', root.findtext(
            namespace + 'dependencies/' + namespace + 'dependency/' + namespace + 'version'))
        self.assertIsNone(root.find(namespace + 'dependencyManagement'))

    def test_cli_project_selection_requires_an_aggregate_and_named_override(self):
        script = Path(__file__).resolve().parents[1] / 'versioning' / 'update_versions.py'
        result = subprocess.run([sys.executable, str(script), '--project-list', 'fixture:consumer'],
                                capture_output=True, text=True)
        self.assertEqual(2, result.returncode)
        self.assertIn('--project-list requires --target-file and a named --version-override', result.stderr)

    def test_parent_profile_override_fails_explicitly_without_writing(self):
        write(self.parent, pom(
            '<groupId>fixture</groupId><artifactId>parent</artifactId><version>1.0</version><packaging>pom</packaging>'
            '<profiles><profile><id>conditional</id><dependencies><dependency>'
            '<groupId>fixture</groupId><artifactId>api</artifactId><version>2.0</version>'
            '</dependency></dependencies></profile></profiles>'))
        original = self.consumer.read_bytes()
        with self.assertRaisesRegex(ValueError, 'parent profile'):
            self.prepare()
        self.assertEqual(original, self.consumer.read_bytes())


@unittest.skipUnless(os.environ.get('AZSDK_RUN_MAVEN_OVERRIDE_TESTS') == 'true',
                     'Set AZSDK_RUN_MAVEN_OVERRIDE_TESTS=true to run Maven integration fixtures.')
class ProjectOverridesMavenTests(unittest.TestCase):
    def test_single_reactor_build_with_transitive_and_inherited_overrides(self):
        with tempfile.TemporaryDirectory(prefix='project-overrides-') as temporary:
            root = Path(temporary)
            group = 'fixture.override.' + uuid.uuid4().hex
            cached_artifacts = Path.home().joinpath('.m2', 'repository', *group.split('.'))
            self.addCleanup(lambda: shutil.rmtree(cached_artifacts) if cached_artifacts.is_dir() else None)
            repository = root / 'artifacts'
            for version in ['1.0', '2.0']:
                source = root / ('api-' + version) / 'src'
                classes = root / ('api-' + version) / 'classes'
                write(source / 'fixture' / 'Api.java',
                      'package fixture; public class Api { public static String version() { return "' +
                      version + '"; }' +
                      ('public static int newMethod() { return 2; }' if version == '2.0' else '') + '}')
                if version == '1.0':
                    write(source / 'fixture' / 'OnlyOld.java',
                          'package fixture; public class OnlyOld { public static String value() { return "old"; } }')
                classes.mkdir(parents=True)
                subprocess.run(['javac', '--release', '8', '-d', str(classes)] +
                               [str(p) for p in source.rglob('*.java')], check=True, capture_output=True, text=True)
                artifact = repository.joinpath(*group.split('.'), 'api', version)
                artifact.mkdir(parents=True)
                subprocess.run(['jar', 'cf', str(artifact / ('api-' + version + '.jar')),
                                '-C', str(classes), '.'], check=True, capture_output=True, text=True)
                write(artifact / ('api-' + version + '.pom'), pom(
                    '<groupId>' + group + '</groupId><artifactId>api</artifactId><version>' + version + '</version>'))

            for inherited in [False, True]:
                with self.subTest(inherited=inherited):
                    reactor = root / ('inherited' if inherited else 'transitive')
                    dependency = ('<dependency><groupId>' + group + '</groupId><artifactId>api</artifactId>'
                                  '<version>2.0</version></dependency>')
                    parent = (
                        '<groupId>' + group + '</groupId><artifactId>parent</artifactId><version>1.0</version>'
                        '<packaging>pom</packaging><properties><maven.compiler.release>8</maven.compiler.release>'
                        '<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding></properties>'
                        '<repositories><repository><id>fixture</id><url>' + repository.as_uri() +
                        '</url></repository></repositories><build><pluginManagement><plugins>'
                        '<plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-compiler-plugin</artifactId>'
                        '<version>3.15.0</version></plugin><plugin><groupId>org.apache.maven.plugins</groupId>'
                        '<artifactId>maven-surefire-plugin</artifactId><version>3.5.6</version></plugin>'
                        '<plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-enforcer-plugin</artifactId>'
                        '<version>3.6.3</version></plugin></plugins></pluginManagement></build>')
                    if inherited:
                        parent = parent.replace('</properties>', '<api.version>2.0</api.version>'
                                                '<!-- {x-version-update;' + group + ':api;external_dependency} -->'
                                                '</properties>', 1)
                        parent += ('<dependencies>' +
                                   dependency.replace('<version>2.0</version>', '<version>${api.version}</version>') +
                                   '</dependencies>')
                    write(reactor / 'parent' / 'pom.xml', pom(parent))
                    parent_reference = ('<parent><groupId>' + group + '</groupId><artifactId>parent</artifactId>'
                                        '<version>1.0</version><relativePath>../parent/pom.xml</relativePath></parent>')
                    write(reactor / 'support' / 'pom.xml', pom(
                        parent_reference + '<artifactId>support</artifactId><dependencies>' + dependency +
                        '</dependencies>'))
                    write(reactor / 'support' / 'src' / 'main' / 'java' / 'fixture' / 'Support.java',
                          'package fixture; public class Support { public static int newMethod() { '
                          'return Api.newMethod(); } public static String marker() { return "support"; } }')
                    write(reactor / 'consumer' / 'pom.xml', pom(
                        parent_reference + '<artifactId>consumer</artifactId><dependencies><dependency>'
                        '<groupId>' + group + '</groupId><artifactId>support</artifactId><version>1.0</version>'
                        '</dependency><dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId>'
                        '<version>5.13.4</version><scope>test</scope></dependency></dependencies>'))
                    write(reactor / 'consumer' / 'src' / 'main' / 'java' / 'fixture' / 'Consumer.java',
                          'package fixture; public class Consumer { public static String value() { '
                          'return OnlyOld.value() + ":" + Api.version() + ":" + Support.marker(); } }')
                    write(reactor / 'consumer' / 'src' / 'test' / 'java' / 'fixture' / 'ConsumerTest.java',
                          'package fixture; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.Assertions; '
                          'public class ConsumerTest { @Test public void usesOldVersion() { '
                          'Assertions.assertEquals("old:1.0:support", Consumer.value()); } }')
                    aggregate = reactor / 'ClientPom.xml'
                    write(aggregate, pom(
                        '<groupId>' + group + '</groupId><artifactId>aggregate</artifactId><version>1.0</version>'
                        '<packaging>pom</packaging><modules><module>parent</module><module>support</module>'
                        '<module>consumer</module></modules>'))
                    overrides = reactor / 'overrides.json'
                    write(overrides, json.dumps({'compat': [{group + ':api': '1.0'}]}))
                    parent_before = (reactor / 'parent' / 'pom.xml').read_bytes()
                    support_before = (reactor / 'support' / 'pom.xml').read_bytes()
                    prepare_project_overrides(aggregate, group + ':consumer', overrides, 'compat')
                    result = subprocess.run(
                        [shutil.which('mvn'), '--batch-mode', '--no-transfer-progress', '-f', str(aggregate), 'verify'],
                        capture_output=True, text=True, timeout=300)
                    self.assertEqual(0, result.returncode, result.stdout + result.stderr)
                    self.assertEqual(parent_before, (reactor / 'parent' / 'pom.xml').read_bytes())
                    self.assertEqual(support_before, (reactor / 'support' / 'pom.xml').read_bytes())
                    report = ET.parse(reactor / 'consumer' / 'target' / 'surefire-reports' / 'TEST-fixture.ConsumerTest.xml')
                    self.assertEqual('0', report.getroot().get('failures'))
                    self.assertEqual('0', report.getroot().get('errors'))


if __name__ == '__main__':
    unittest.main()
