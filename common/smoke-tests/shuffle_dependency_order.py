from random import shuffle
import xml.etree.ElementTree as ET
import xml.dom.minidom as md

from util import get_xml_root

def scrub_indented_lines(input):
    return "\n".join([line for line in input.split("\n") if line.strip() != ''])

def pretty_xml(xml_string):
    parsed = md.parseString(xml_string)
    return scrub_indented_lines(parsed.toprettyxml(indent='  ', newl="\n"))

def main():
    root = get_xml_root('./pom.xml')
    dependencies = root.findall("dependencies/dependency")

    print("==== Original Dependency Order ====")
    for dependency in dependencies:
        print(f"\t{dependency.find('artifactId').text}")

    # Shuffle dependencies in place
    shuffle(dependencies)

    print("==== Shuffled Dependency Order ====")
    for dependency in dependencies:
        print(f"\t{dependency.find('artifactId').text}")

    dependencies_element = root.find('dependencies')
    dependencies_element.clear()

    # Reinsert the dependencies in the shuffled order
    index = 0
    for dependency in dependencies:
        dependencies_element.insert(index, dependency)
        index += 1

    pom_output = pretty_xml(ET.tostring(root, encoding='utf8', method='xml').decode())
    print("==== OUTPUT POM FILE ====")
    print(pom_output)

    with open('./pom.xml', 'w') as f:
        f.write(pom_output)

main()