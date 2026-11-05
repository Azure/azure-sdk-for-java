// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import javax.xml.parsers.DocumentBuilderFactory

def projectDir = context.get('projectDir')
def properties = new Properties()
new File(projectDir.parentFile.parentFile, 'archetype.properties').withInputStream {
    properties.load(it)
}
def junitVersion = properties.getProperty('junitVersion', '5')
def parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
def pom = parser.parse(new File(projectDir, 'pom.xml'))
assert pom.getElementsByTagName("junit${junitVersion}.version").item(0).textContent.startsWith(junitVersion + '.')

def report = parser.parse(new File(projectDir, 'target/surefire-reports/TEST-com.azure.archetype.AppTest.xml'))
def suite = report.documentElement
assert suite.getAttribute('tests') == '1'
assert suite.getAttribute('failures') == '0'
assert suite.getAttribute('errors') == '0'
assert suite.getAttribute('skipped') == '0'

return true
