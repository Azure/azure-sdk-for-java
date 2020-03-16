import os
import glob
import re
import json
import pprint
from xml.sax.saxutils import escape, unescape

# run this from the root of the repository
root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))

SNIPPET_BEGIN = r"\s*\/\/\s*BEGIN\:\s*(?P<id>[a-zA-Z0-9\.\#\-\_]*)\s*"
SNIPPET_END = r"\s*\/\/\s*END\:\s*(?P<id>[a-zA-Z0-9\.\#\-\_]*)\s*"
SNIPPET_CALL = r"(?P<leadingspace>.*)\{\@codesnippet(?P<snippetid>.*)\}"

EXCLUSION_ARRAY = [
  "JavadocCodeSnippetCheck.java"
]

html_escape_table = {
  "&": "&amp;",
  '"': "&quot;",
  "'": "&apos;",
  ">": "&gt;",
  "<": "&lt;",
  "@": "&#64;",
  "{": "&#123;",
  "}": "&#125;",
  "(": "&#40;",
  ")": "&#41;",
  "/": "&#47;",
  "\\" :"&#92;"
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
      raise "Duplicate snippet begin."

  def process_line(self, line):
    self.snippet_dict = {k: v + [line] for k, v in self.snippet_dict.items()}

  def finalize_snippet(self, key):
    return self.snippet_dict.pop(key)

def get_snippets_from_file(file):
  finished_snippets = {}
  running_dict = SnippetDict()

  with open(file, 'r', encoding="utf-8") as source:
    print(file)
    for line in source.readlines():
      
      begin = re.match(SNIPPET_BEGIN, line)
      end = re.match(SNIPPET_END, line)

      if begin:
        id_beginning = begin.groupdict()['id']
        running_dict.begin_snippet(id_beginning)
      elif end:
        id_ending = end.groupdict()['id']
        ending = running_dict.finalize_snippet(id_ending)
        finished_snippets[id_ending] = ending
      elif running_dict:
        running_dict.process_line(line)    
  
  return finished_snippets

def check_exclusion(file_name, exclusion_array):
  if not os.path.isdir(file_name):
    name = os.path.basename(file_name)

    return name in exclusion_array

if __name__ == "__main__":

  # walk the codebase, find all java files
  all_files = glob.glob('**/*.java', recursive=True)
  snippet_files = [source_file for source_file in all_files if ("snippet" in source_file.lower() or "sample" in source_file.lower()) and not check_exclusion(source_file, EXCLUSION_ARRAY)]
  snippets = {}

  for file in snippet_files:
    snippet_dict = get_snippets_from_file(file)
    snippets.update(snippet_dict)

  for file in all_files:
    needs_amend = False
    amended_file = []
    
    with open(file, 'r', encoding="utf-8") as source:
      for line in source.readlines():
        snippet_ref = re.match(SNIPPET_CALL, line)

        if snippet_ref:
          id_ending = snippet_ref.groupdict()['snippetid'].strip()
          lead_space = snippet_ref.groupdict()['leadingspace']
          if id_ending in snippets:
            result_array = [
              lead_space + "<pre>\n",
              escape(("".join(map(lambda x : lead_space + x, snippets[id_ending]))), html_escape_table),
              lead_space + "</pre>\n"
            ]
            line_replacement = "".join(result_array)
            amended_file.append(line_replacement)
            needs_amend = True
          else:
            print("Can't find snippet for ref: " + id_ending)
        else:
          amended_file.append(line)

    if needs_amend:
      print("Replacing " + file)
      with open(file, 'w') as out_file:
        for line in amended_file:
          out_file.write(line)

