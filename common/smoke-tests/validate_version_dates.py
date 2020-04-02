import re
from util import get_xml_root

def main():
    root = get_xml_root('./pom.xml')
    dependencies = root.findall("dependencies/dependency[groupId='com.azure']")

    reference_date = ''
    for dependency in dependencies:
        version = dependency.find('version').text
        artifact_id = dependency.find('artifactId').text

        print(f'Inspecting {artifact_id}@{version}')

        # Match the dev.yyyymmdd expression
        match = re.search("dev\.(\d{8})", version)

        # There is no matching date portion, skip this entry in the loop (used
        # for packages that are not released to the dev feed)
        if not match or not match.group(1):
            continue

        if not reference_date:
            reference_date = match.group(1)
            print(f'Found reference_date: {reference_date}')
            continue

        if match.group(1) != reference_date:
            raise ValueError(f'Dependency version dev date mismatch for {artifact_id} (expected: {reference_date}, actual: {match.group(1)} )')

main()