const fs = require('fs');
const path = require('path');
const request = require('request');

// mapping for services with different spec folder names
const specs = {
  'avs': 'vmware',
  'cosmos': 'cosmos-db',
  'costmanagement': 'cost-management',
  'customerinsights': 'customer-insights',
  'datalakeanalytics': 'datalake-analytics',
  'datalakestore': 'datalake-store',
  'delegatednetwork': 'dnc',
  'eventhubs': 'eventhub',
  'loganalytics': 'operationalinsights',
  'kusto': 'azure-kusto',
  'servicemap': 'service-map',
  'managedapplications': 'resources'
};
const groupUrl = 'https://repo1.maven.org/maven2/com/azure/resourcemanager/';
const artiRegEx = /<a href="(azure-resourcemanager-[-\w]+)\/"/g;
const verRegEx = /<version>(.+)<\/version>/g;
const pkgRegEx = /Package\s+tag\s+(.+)\.\s+For/;
const pkgRegEx2 = /Package\s+tag\s+(.+)\.</;
var startCnt = 0;
var endCnt = 0;
var data = {};

function autocent() {
  console.log('[INFO] Automation task to update the mapping of services and API version tags.');

  getArtifacts();
}

// method to get all matched artifacts from maven central
function getArtifacts() {
  sendRequest(groupUrl, function(response) {
    var artifacts = [];
    var match = artiRegEx.exec(response);
    while (match !== null) {
      artifacts.push(match[1]);
      match = artiRegEx.exec(response);
    }
    for (var i in artifacts) {
      readMetadata(artifacts[i]);
    }
    artiRegEx.lastIndex = 0;
  });
}

// method to read metadata for getting all published package versions
function readMetadata(artifact) {
  sendRequest(groupUrl + artifact + '/maven-metadata.xml', function(response) {
    var versions = [];
    var match = verRegEx.exec(response);
    while (match !== null) {
      ++startCnt;
      versions.push(match[1]);
      match = verRegEx.exec(response);
    }
    for (var i in versions) {
      readPom(artifact, versions[i]);
    }
    verRegEx.lastIndex = 0;
  });
}

// method to read pom for each package version and get API version tag from description
function readPom(artifact, version) {
  sendRequest(groupUrl + artifact + '/' + version + '/' + artifact + '-' + version + '.pom', function(response) {
    var match = pkgRegEx2.exec(response);
    if (!match) {
      match = pkgRegEx.exec(response);
    }
    ++endCnt;
    if (!match) {
      console.log('[WARN] no package tag found in ' + artifact + '_' + version);
    } else {
      var tag = match[1];
      var service = artifact.split('-').pop();
      if (!data[service]) {
        data[service] = {};
      }
      if (!data[service][tag]) {
        data[service][tag] = [];
      }
      data[service][tag].push(version);
      console.log('[INFO] find tag %s and version %s for service %s.', tag, version, service);
    }
    pkgRegEx.lastIndex = 0;
    if (startCnt == endCnt) {
      // update file for listing all latest releases of the packages
      var content = '# Single-Service Packages Latest Releases\n\n' +
        'The single-service packages provide easy-to-use APIs for each Azure service following the design principals of [Azure Management Libraries for Java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager). If you have specific requirement on certain service API version, you may find appropriate package below. If not, you could always choose the latest release.\n\n' +
        'According to [Azure REST API reference](https://docs.microsoft.com/rest/api/azure/), most request URIs of Azure services require `api-version` as the query-string parameter. All supported API versions for each Azure service can be found via [Azure REST API Specifications](https://github.com/Azure/azure-rest-api-specs/tree/master/specification). For your convenience, we provide more details of the published packages by format below.\n\n' +
        '```\n' +
        'service\n' +
        '* package-tag\n' +
        '    * maven.version\n' +
        '```\n\n' +
        '- `service` for Azure service.\n' +
        '- `package-tag` for included resources with API versions.\n' +
        '- `maven.version` for maven version of the artifact.\n\n';

      var sortedServices = Object.keys(data).sort();
      for (var i in sortedServices) {
        var service = sortedServices[i];
        content += '\n<br/>\n' +
          '<details>\n' +
          '<summary> ' + service + ' </summary>\n\n';
        var sortedTags = Object.keys(data[service]).sort().reverse();
        for (var j in sortedTags) {
          var tag = sortedTags[j];
          var spec = specs[service] ? specs[service] : service;
          content += '* [' + tag + '](https://github.com/Azure/azure-rest-api-specs/tree/master/specification/' + spec + '/resource-manager#tag-' + tag + ')\n';
          var sortedVersions = data[service][tag].sort(function(a, b) {
            // custom method to sort versions
            var aVerNums = a.split(".");
            var bVerNums = b.split(".");
            if (aVerNums[0] > bVerNums[0]) {
              return -1;
            } else if (aVerNums[0] < bVerNums[0]) {
              return 1;
            } else {
              if (aVerNums[1] > bVerNums[1]) {
                return -1;
              } else if (aVerNums[1] < bVerNums[1]) {
                return 1;
              } else {
                var aPatchNums = aVerNums[2].split("-beta.");
                var bPatchNums = bVerNums[2].split("-beta.");
                if (aPatchNums.length < bPatchNums.length) {
                  return -1;
                } else if (aPatchNums.length > bPatchNums.length) {
                  return 1;
                } else {
                  return b.localeCompare(a);
                }
              }
            }
          });
          for (var k in sortedVersions) {
            var sdk = sortedVersions[k];
            content += '    * [' + sdk + '](https://search.maven.org/artifact/com.azure.resourcemanager/azure-resourcemanager-' + service + '/' + sdk + '/jar)\n';
          }
        }
        content += '</details>\n';
      }
      fs.writeFileSync("docs/SINGLE_SERVICE_PACKAGES.md", content);
    }
  });
}

// method to send GET request
function sendRequest(url, callback) {
  request({
    url: url,
    method: 'GET',
    headers: {
      'user-agent': 'AutoCent'
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
