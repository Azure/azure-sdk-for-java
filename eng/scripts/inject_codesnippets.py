import os
import glob
import re
import json

# run this from the root of the repository
root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))

SNIPPET_BEGIN = r"\s*\/\/\s*BEGIN\:\s*(?P<id>[a-zA-Z0-9\.\#\-\_]*)\s*"
SNIPPET_END = r"\s*\/\/\s*END\:\s*(?P<id>[a-zA-Z0-9\.\#\-\_]*)\s*"


class SnippetDict:
  def __init__(self):
    self.snippet_dict = {}

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




# def update_snippet_dict(snippet_dict, key, line):

#   if key in snippet_dict:
#     snippet_dict,

def get_snippets_from_file(file):
  finished_snippets = {}
  running_dict = SnippetDict()

  with open(file, 'r') as source:
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
# walk the codebase, searching each file for 
#    --> // BEGIN: <id>
#        // END: <id>
#   or 
#    --> {@codesnippet <id>}
#
# generate code snippets

# 

if __name__ == "__main__":

  # walk the codebase, find all java files
  files = [source_file for source_file in glob.glob('**/*.java', recursive=True) if "snippet" in source_file.lower()]

  for file in files:
    snippet_dict = get_snippets_from_file(file)
    for key in snippet_dict:
      print(key)
      print("\n".join([line.rstrip() for line in snippet_dict[key]]))