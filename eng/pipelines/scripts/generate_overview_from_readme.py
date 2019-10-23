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

import argparse
import markdown2
import os.path
from io import open

def generate_overview(readme_file):

    html_overview_file = str(readme_file).lower().replace('readme.md', 'readme_overview.html')

    with open(readme_file, 'r', encoding='utf-8') as f:
        readme_content = f.read()

    # markdown2.markdown will create html from the readme.md file. The fenced-code-blocks
    # extras being passed into the markdown call is necessary to deal with the embedded
    # code blocks within the readme so they'll displaye correctly in the html
    html_readme_content = markdown2.markdown(readme_content, extras=["fenced-code-blocks"])

    # The html_readme_content needs to be encapsulated inside of <body> tags in order
    # for the content to correctly be added to the landing page
    with open(html_overview_file, 'w', encoding='utf-8') as f:
        f.write("<body>")
        f.write(html_readme_content)
        f.write("</body>")


def main():
    parser = argparse.ArgumentParser(description='Generate a readme_overview.html from a README.md.')
    parser.add_argument('--readme-file', '--rf', help='path to the README.md file to readme_generate the overview.html file from.', required=True)
    args = parser.parse_args()
    # verify the argument is a readme.md file
    if str(args.readme_file).lower().endswith('readme.md'):
        if os.path.exists(args.readme_file) and os.path.isfile(args.readme_file):
            generate_overview(args.readme_file)
        else:
            # Not every artifact has a README.md file. If the file doesn't exist then
            # just output a message which will end up in the build logs. This will
            # allow processing to continue without failing the build the way a raise would.
            print('{} does not exist'.format(args.readme_file))

    else:
        raise ValueError('{} is not a readmefile. The --readme-file argument must be a readme.md file.'.format(args.readme_file))

if __name__ == '__main__':
    main()
