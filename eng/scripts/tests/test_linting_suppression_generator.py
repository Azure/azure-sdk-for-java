# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

import importlib.util
from pathlib import Path
import tempfile
import unittest
import xml.etree.ElementTree as ET


SCRIPT = Path(__file__).resolve().parents[1] / 'linting_suppression_generator.py'
SPEC = importlib.util.spec_from_file_location('linting_suppression_generator', SCRIPT)
generator = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(generator)


class CheckstyleSuppressionTests(unittest.TestCase):
    def test_merged_rule_diagnostics_generate_rule_name_suppressions(self):
        prefix = 'io.clientcore.linting.extensions.checkstyle.checks.'
        rules = [prefix + 'PublicApiTypeCheck', prefix + 'ExceptionHandlingCheck']
        entries = [('/repo/src/main/java/com/azure/Api.java', rule) for rule in rules for _ in range(2)]
        entries.append(('/repo/src/main/java/com/azure/Api.java',
                        'com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMethodCheck'))
        text, suppressions = self.generate(entries)
        self.assertEqual(set(rules + ['JavadocMethodCheck']), {item['checks'] for item in suppressions})
        self.assertEqual(3, len(suppressions))
        self.assertTrue(all(item['files'] == 'com.azure.Api.java' for item in suppressions))
        self.assertTrue(all('id' not in item for item in suppressions))
        self.assertNotIn(' id=', text)

    def test_output_is_deterministic(self):
        entries = [('/repo/src/main/java/com/azure/B.java', 'ExampleCheck'),
                   ('/repo/src/main/java/com/azure/A.java', 'ExampleCheck')]
        first, _ = self.generate(entries)
        second, _ = self.generate(list(reversed(entries)))
        self.assertEqual(first, second)

    def test_windows_source_paths_are_normalized(self):
        _, suppressions = self.generate([(r'C:\repo\src\main\java\com\azure\Api.java', 'ExampleCheck')])
        self.assertEqual([{'files': 'com.azure.Api.java', 'checks': 'ExampleCheck'}], suppressions)

    def test_xml_attributes_are_escaped(self):
        text, suppressions = self.generate([('/repo/src/main/java/com/azure/Api&Other.java', 'ExampleCheck')])
        self.assertIn('Api&amp;Other.java', text)
        self.assertEqual('com.azure.Api&Other.java', suppressions[0]['files'])

    @staticmethod
    def generate(entries):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / 'target').mkdir()
            report = ET.Element('checkstyle')
            for name, source in entries:
                file = ET.SubElement(report, 'file', name=name)
                ET.SubElement(file, 'error', source=source)
            ET.ElementTree(report).write(root / 'target/checkstyle-result.xml', encoding='utf-8')
            generator.generate_checkstyle_suppression_file(directory)
            output = root / 'checkstyle-suppressions.xml'
            return (output.read_text(encoding='utf-8'),
                    [element.attrib for element in ET.parse(output).getroot().findall('suppress')])


if __name__ == '__main__':
    unittest.main()

