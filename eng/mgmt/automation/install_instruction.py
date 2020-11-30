#!/usr/bin/env python3
import os
import re
import sys
import json
import logging
import urllib.parse


def log_and_exit(message: str):
    logging.error(message)
    sys.exit(1)


def main():
    if len(sys.argv) != 3:
        log_and_exit('Usage: {0} <input> <output>'.format(sys.argv[0]))

    with open(sys.argv[1]) as fin:
        config = json.load(fin)

    artifact_id: str = config.get('packageName', '')
    group_id = 'com.azure.resourcemanager'
    logging.debug('Got artifact_id: {0}'.format(artifact_id))

    for artifact in config.get('artifacts', []):
        logging.debug('Got artifact: {0}'.format(artifact))
        if re.match('{0}-\d+\.\d+\.\d+(-beta\.\d+)?\.jar'.format(artifact_id),
                    artifact):
            logging.debug('Match jar package: {0}'.format(artifact))
            download_url = config.get('downloadUrlPrefix', '') + artifact
            with open(sys.argv[2], 'w') as fout:
                command = config.get('downloadCommandTemplate', '').format(
                    URL = download_url,
                    FILENAME = artifact,
                ) + '\nmvn install:install-file -DgroupId={0} -DartifactId={1} -Dversion=1.0.0-beta.0 -Dfile={2} -Dpackaging=jar -DgeneratePom=true\n'.format(
                    group_id,
                    artifact_id,
                    artifact,
                )  # 1.0.0-beta.0 will not be conflict with any existing one
                output = {'full': '```sh\n{0}\n```'.format(command)}
                logging.debug('output: {0}'.format(json.dumps(output)))
                json.dump(output, fout)
            sys.exit(0)
    else:
        logging.error('Unmatch any jar in artifacts')
        with open(sys.argv[2], 'w') as fout:
            json.dump({'full': ''}, fout)


if __name__ == "__main__":
    logging.basicConfig(
        level = logging.DEBUG,
        format = '%(asctime)s %(levelname)s %(message)s',
        datefmt = '%Y-%m-%d %X',
    )
    main()