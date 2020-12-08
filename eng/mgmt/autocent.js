const fs = require('fs');
const path = require('path');
const request = require('request');

function autocent() {
  console.log('[INFO] Automation task to update the mapping of services and API version tags.');
  
  console.log('[Aritifact] Starting...');
  getArtifacts();
}

const groupUrl = 'https://repo1.maven.org/maven2/com/microsoft/azure/';
const artiRegEx = />(azure-mgmt-.+)\/</g;
const verRegEx = /<version>(.+)<\/version>/g;
const pkgRegEx = /Package\s+tag\s+(.+)\.\s+For/g;

function getArtifacts() {
  sendRequest(groupUrl, function(response) {
    var artifacts = [];
    var match = artiRegEx.exec(response);
    while (match !== null) {
      artifacts.push(match[1]);
	  match = artiRegEx.exec(response);
	}
    console.log(artifacts);
    console.log('[Aritifact] Completed.');
	for (var i in artifacts) {
      console.log('[Metadata] ' + artifacts[i]);
      readMetadata(artifacts[i]);
	}
  });
}

function readMetadata(artifact) {
  sendRequest(groupUrl + artifact + '/maven-metadata.xml', function(response) {
    var versions = [];
    var match = verRegEx.exec(response);
	while (match !== null) {
      versions.push(match[1]);
      match = verRegEx.exec(response);
	}
    console.log(versions);
    console.log('[Metadata] ' + artifact + ' completed.');
	for (var i in versions) {
      readPom(artifact, versions[i]);
	}
  });
}

function readPom(artifact, version) {
  sendRequest(groupUrl + artifact + '/' + version + '/' + artifact + '-' + version + '.pom', function(response) {
    var match = pkgRegEx.exec(response);
	if (match === null) {
      console.log('[WARN] no package tag found in ' + artifact + '_' + version);
	} else {
      console.log('tag found');
	}
  });
}

function sendRequest(url, callback) {
  request({
    url: url,
    method: 'GET',
    headers: {
      'user-agent':'AutoCent'
	}
  }, function(error, response) {
    if (error) {
		console.log('[ERROR] Request URL: ' + url);
		process.exit(1);
	}
	callback(response.body);
  });
}

autocent();