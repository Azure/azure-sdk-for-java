#!/usr/bin/env python3
import os
import sys
import json
import requests
import urllib.parse

def create_pull_request(owner, repo, title, head, auth = os.environ.get("GITHUB_TOKEN")):
    url = "https://api.github.com/repos/{0}/{1}/pulls".format(owner, repo)
    resp = requests.post(
        url = url,
        auth = (auth, ""),
        json = {
            "title": title,
            "base": "main",
            "head": head,
        }
    )
    if resp.status_code == 422:
        print("duplicate pull request")
        print("Error: {0} for url: {1}".format(resp.reason, resp.url))
    else:
        resp.raise_for_status()
        print(json.dumps(resp.json(), indent = 2))

def main(argv):
    if len(argv) < 4:
        print("Usage: {} owner repo title head [auth]".format(argv[0]))
        sys.exit(1)
    create_pull_request(*argv[1:6])

if __name__ == "__main__":
    main(sys.argv)