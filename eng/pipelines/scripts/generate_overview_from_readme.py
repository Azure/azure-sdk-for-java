# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Use case: Given a README.md file, generate a readme_overview.html file and place it next
# to the README.md. This will allow the javadocs jar step to append the contents of the
# readme onto the landing page.
#
# This script is necessary, instead of just invoking python markdown2 directly from the
# command line because the generated overview.html file needs to be enclosed inside of <body>
# tags. When the attach-javadocs runs with the overview option it will append it the contents
# to the overview-summary.html which is the landing page. If the <body> tags aren't in place
# the page won't be formatted correctly.
#
# Regardless of whether or not there's a readme.md file the doc version and return to index link
# will be written to overview file. If there is a readme, its contents will be added after that.

import argparse
from bs4 import BeautifulSoup
import pdb
import markdown2
import os.path
from io import open
import re

def generate_overview(readme_file, version, overview_file_path):

    readme_exists = False
    if os.path.exists(readme_file) and os.path.isfile(readme_file):
        readme_exists = True
    else:
        # Not every artifact has a README.md file. If the file doesn't exist then
        # just output a message which will end up in the build logs. This will
        # allow processing to continue without failing the build the way a raise would.
        print('{} does not exist'.format(readme_file))

    if overview_file_path:
        html_overview_file = overview_file_path + 'readme_overview.html'
    else:
        html_overview_file = str(readme_file).lower().replace('readme.md', 'readme_overview.html')

    if (readme_exists):
        with open(readme_file, 'r', encoding='utf-8') as f:
            readme_content = f.read()

        # Before passing the README contents to markdown2 clean out the codesnippet tags on the java code fences.
        # Clean ```java com.azure.core.aCodeSnippetTag to ```java, without doing this markdown2 won't properly process
        # the contents of the code fence.
        readme_content = re.sub(pattern="```\s*java\s+[a-zA-Z0-9.#\-_]*", repl="```java", string=readme_content, flags=re.MULTILINE)

        # markdown2.markdown will create html from the readme.md file. The fenced-code-blocks
        # extras being passed into the markdown call is necessary to deal with the embedded
        # code blocks within the readme so they'll displaye correctly in the html
        # The target-blank-links will open new tab for new page, but leave the anchor link in the same page.
        # The toc helps the anchor link to jump to the right place.
        html_readme_content = markdown2.markdown(re.sub(pattern='(?<!opencode)@', repl='{@literal @}', string=readme_content, flags=re.MULTILINE), extras=["fenced-code-blocks", "target-blank-links", "toc"])

        # Now use BeautifulSoup to cleanup the generated HTML so that it conforms to Javadoc compliance.
        soup = BeautifulSoup(html_readme_content, features="html.parser")

        # Find all anchor tags with the rel attribute and remove the attribute.
        anchors_with_rel = soup.find_all(name='a', attrs={'rel':'noopener'})
        for anchor in anchors_with_rel:
            del anchor['rel']

    # The html_readme_content needs to be encapsulated inside of <body> tags in order
    # for the content to correctly be added to the landing page
    with open(html_overview_file, 'w', encoding='utf-8') as f:
        # The literal strings have to be unicode otherwise the write will fail.
        # This will allow this code to work for python 2 and 3
        f.write('<body>')
        f.write('Current version is {}, click <a href="https://azure.github.io/azure-sdk-for-java" target="new">here</a> for the index'.format(version))
        f.write('<br>')
        if (readme_exists):
            f.write(str(soup.encode(formatter="html5").decode('utf-8')))
        f.write('</body>')


def main():
    parser = argparse.ArgumentParser(description='Generate a readme_overview.html from a README.md.')
    parser.add_argument('--readme-file', '--rf', help='path to the README.md file to readme_generate the overview.html file from.', required=True)
    parser.add_argument('--overview-file-path', '--ofp', help='path to the overview.html file.')
    parser.add_argument('--version', '--v', help='Version, used on the landing page to identify the version.', required=True)
    args = parser.parse_args()
    # verify the argument is a readme.md file
    if str(args.readme_file).lower().endswith('readme.md'):
        generate_overview(args.readme_file, args.version, args.overview_file_path)
    else:
        raise ValueError('{} is not a readmefile. The --readme-file argument must be a readme.md file.'.format(args.readme_file))

if __name__ == '__main__':
    main()
