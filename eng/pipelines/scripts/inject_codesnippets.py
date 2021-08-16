import os
import glob
import re
import json
from xml.sax.saxutils import escape, unescape
import argparse
import pdb
import fnmatch

# run this from the root of the repository
root = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))

SNIPPET_BEGIN = r"\s*\/\/\s*BEGIN\:\s+(?P<id>[a-zA-Z0-9\.\#\-\_]*)\s*"
SNIPPET_END = r"\s*\/\/\s*END\:\s+(?P<id>[a-zA-Z0-9\.\#\-\_]*)\s*"
SNIPPET_CALL = r"(?P<leadingspace>.*\*).*\{\@codesnippet(?P<snippetid>.*)\}"
WHITESPACE_EXTRACTION = r"(?P<leadingspace>\s*).*"
SAMPLE_PATH_GLOB = "**/src/samples/java/**"

EXCLUSION_ARRAY = ["JavadocCodeSnippetCheck.java"]

HTML_ESCAPE_TABLE = {
    '"': "&quot;",
    ">": "&#62;",
    "<": "&#60;",
    "@": "{@literal @}",
    "{": "&#123;",
    "}": "&#125;",
    "(": "&#40;",
    ")": "&#41;",
    "/": "&#47;",
    "\\": "&#92;",
}


class SnippetDict:
    def __init__(self, start_dict={}):
        self.snippet_dict = start_dict

    def __nonzero__(self):
        return self.snippet_dict

    def __str__(self):
        return json.dumps(self.snippet_dict)

    def begin_snippet(self, key):
        if key not in self.snippet_dict:
            self.snippet_dict[key] = []
        else:
            raise "Duplicate snippet begin detected. Offending key is {}.".format(key)

    def process_line(self, line):
        self.snippet_dict = {k: v + [line] for k, v in self.snippet_dict.items()}

    def finalize_snippet(self, key):
        try:
            return self.snippet_dict.pop(key)
        except:
            print("Unable to finalize snippet w/ key {}".format(key))
            return [""]


def re_space_snippet(snippet_list):
    # find indentation (or whitespace characters) on the left side
    white_space = [
        re.match(WHITESPACE_EXTRACTION, line).groupdict()["leadingspace"]
        for line in snippet_list
        if line.strip()
    ]

    # now figure out the shortest one
    white_space_for_replacement = min(white_space, key=len)

    # return the list, replacing leading whitespace with the specified amount
    return [line.replace(white_space_for_replacement, "", 1) for line in snippet_list]


def get_snippets_from_file(file, verbose):
    finished_snippets = {}
    running_dict = SnippetDict()

    with open(file, "r", encoding="utf-8") as source:
        if verbose:
            print(file)
        for line in source.readlines():

            begin = re.match(SNIPPET_BEGIN, line)
            end = re.match(SNIPPET_END, line)

            if begin:
                id_beginning = begin.groupdict()["id"]
                if verbose:
                    print("beginning {}".format(id_beginning))
                running_dict.begin_snippet(id_beginning)
            elif end:
                id_ending = end.groupdict()["id"]
                ending = running_dict.finalize_snippet(id_ending)
                if verbose:
                    print("ending {}".format(id_ending))
                finished_snippets[id_ending] = ending
            elif running_dict:
                running_dict.process_line(line)

    return finished_snippets


def check_exclusion(file_name, exclusion_array):
    if not os.path.isdir(file_name):
        name = os.path.basename(file_name)

        return name in exclusion_array


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Generate a readme_overview.html from a README.md."
    )
    parser.add_argument(
        "--project-dir",
        "-p",
        dest="target",
        help="The path to the directory containing our package or service. Essentially used as a scoping mechanism for the replacement of the snippets.",
        required=True,
    )
    parser.add_argument(
        "--verbose",
        "-v",
        dest="verbose",
        help="Flag indicating if verbose, debug level, output should be printed.",
        required=False,
        default=False
    )
    args = parser.parse_args()

    # walk the codebase, find all java files
    all_files = glob.glob(os.path.join(args.target, "**/*.java"), recursive=True)
    snippet_files = [
        source_file
        for source_file in all_files
        if fnmatch.fnmatch(source_file, SAMPLE_PATH_GLOB)
        and not check_exclusion(source_file, EXCLUSION_ARRAY)
    ]

    snippets = {}

    for file in snippet_files:
        snippet_dict = get_snippets_from_file(file, args.verbose)
        snippets.update(snippet_dict)

    for file in all_files:
        needs_amend = False
        amended_file = []

        with open(file, "r", encoding="utf-8") as source:
            for line in source.readlines():
                snippet_ref = re.match(SNIPPET_CALL, line)

                if snippet_ref:
                    id_ending = snippet_ref.groupdict()["snippetid"].strip()
                    lead_space = snippet_ref.groupdict()["leadingspace"] + " "
                    if id_ending in snippets:
                        result_array = [
                            lead_space + "<pre>\n",
                            escape(
                                (
                                    "".join(
                                        map(
                                            lambda x: lead_space + x,
                                            re_space_snippet(snippets[id_ending]),
                                        )
                                    )
                                ),
                                HTML_ESCAPE_TABLE,
                            ),
                            lead_space + "</pre>\n",
                        ]
                        amended_file.append("".join(result_array))
                        needs_amend = True
                    else:
                        print("Can't find snippet for ref: " + id_ending)
                else:
                    amended_file.append(line)

        if needs_amend:
            print("Replacing " + file)
            with open(file, "w", encoding="utf-8") as out_file:
                for line in amended_file:
                    out_file.write(line)
