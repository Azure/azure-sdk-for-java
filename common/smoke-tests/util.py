import xml.etree.ElementTree as ET
from io import StringIO

# namespaces in xml really mess with xmlTree: https://bugs.python.org/issue18304
# this function provides a workaround for both parsing an xml file as well as REMOVING said namespaces
def get_xml_root(file_path):
    with open(file_path) as f:
        xml = f.read()

    it = ET.iterparse(StringIO(xml))
    for _, el in it:
        if '}' in el.tag:
            # strip all namespaces
            el.tag = el.tag.split('}', 1)[1]
    return it.root
