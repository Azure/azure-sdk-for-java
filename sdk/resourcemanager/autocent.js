const fs = require("fs");
const path = require("path");
const request = require("request-promise");
const yaml = require("yaml");

// mapping for services with different spec folder names
const specs = getSpecsMapping();
const groupUrl = "https://repo1.maven.org/maven2/com/azure/resourcemanager/";
const artiRegEx = /<a href="(azure-resourcemanager-[-\w]+)\/"/g;
const verRegEx = /<version>(.+)<\/version>/g;
const pkgRegEx = /Package\s+tag\s+(.+)\.\s+For/;
const pkgRegEx2 = /Package\s+tag\s+(.+)\.</;
const data = {};
const servicesInvalidUrl = ["securitydevops"];
const deprecatedArtifacts = [
    "azure-resourcemanager-loadtestservice",
    "azure-resourcemanager-networkanalytics",
    "azure-resourcemanager-logz"
];
// exclude premium packages
const excludeArtifacts = [
    "azure-resourcemanager",
    "azure-resourcemanager-appplatform",
    "azure-resourcemanager-appservice",
    "azure-resourcemanager-authorization",
    "azure-resourcemanager-cdn",
    "azure-resourcemanager-compute",
    "azure-resourcemanager-containerinstance",
    "azure-resourcemanager-containerregistry",
    "azure-resourcemanager-cosmos",
    "azure-resourcemanager-dns",
    "azure-resourcemanager-eventhubs",
    "azure-resourcemanager-keyvault",
    "azure-resourcemanager-monitor",
    "azure-resourcemanager-msi",
    "azure-resourcemanager-network",
    "azure-resourcemanager-privatedns",
    "azure-resourcemanager-redis",
    "azure-resourcemanager-search",
    "azure-resourcemanager-servicebus",
    "azure-resourcemanager-sql",
    "azure-resourcemanager-storage"
]

async function autocent() {
    console.log("[INFO] Automation task to update the mapping of services and API version tags.");

    await getArtifacts();

    writeMarkdown();
}

// method to get all matched artifacts from maven central
async function getArtifacts() {
    var response = await sendRequest(groupUrl);
    var artifacts = [];
    var match = artiRegEx.exec(response);
    while (match !== null) {
        artifacts.push(match[1]);
        match = artiRegEx.exec(response);
    }
    var promises = [];
    for (var i in artifacts) {
        if (!deprecatedArtifacts.includes(artifacts[i]) && !excludeArtifacts.includes(artifacts[i])) {
            promises.push(readMetadata(artifacts[i]));
        }
    }
    artiRegEx.lastIndex = 0;

    await Promise.all(promises);
}

// method to read metadata for getting all published package versions
async function readMetadata(artifact) {
    var response = await sendRequest(groupUrl + artifact + "/maven-metadata.xml");
    var versions = [];
    var match = verRegEx.exec(response);
    while (match !== null) {
        versions.push(match[1]);
        match = verRegEx.exec(response);
    }
    var promises = [];
    for (var i in versions) {
        promises.push(readPom(artifact, versions[i]));
    }
    verRegEx.lastIndex = 0;

    await Promise.all(promises);
}

// method to read pom for each package version and get API version tag from description
async function readPom(artifact, version) {
    const response = await sendRequest(groupUrl + artifact + "/" + version + "/" + artifact + "-" + version + ".pom");
    let match = pkgRegEx2.exec(response);
    if (!match) {
        match = pkgRegEx.exec(response);
    }
    if (!match) {
        console.log("[WARN] no package tag found in version %s for service %s.", version, artifact);
    } else {
        const tag = match[1];
        const service = artifact.split("azure-resourcemanager-").pop();
        if (!data[service]) {
            data[service] = {};
        }
        if (!data[service][tag]) {
            data[service][tag] = [];
        }
        data[service][tag].push(version);
        console.log("[INFO] find tag %s and version %s for service %s.", tag, version, service);

        const spec = specs[service] ? specs[service] : service;
        const serviceUrl = getServiceUrl(spec);
        const serviceUrlValid = await existUrl(serviceUrl);
        if (!serviceUrlValid) {
            console.log("[ERROR] URL not exists %s", serviceUrl);
            servicesInvalidUrl.push(service);
        }
    }
    pkgRegEx.lastIndex = 0;
}

function getServiceUrl(spec) {
    if (spec.includes("/")) {
        const indexOfSlash = spec.indexOf("/");
        const service = spec.slice(0, indexOfSlash);
        const nested = spec.slice(indexOfSlash + 1);
        var serviceUrl =
            "https://github.com/Azure/azure-rest-api-specs/tree/main/specification/" +
            service +
            "/resource-manager/" +
            nested;
    } else {
        var serviceUrl =
            "https://github.com/Azure/azure-rest-api-specs/tree/main/specification/" + spec + "/resource-manager";
    }
    return serviceUrl;
}

function writeMarkdown() {
    // update file for listing all latest releases of the packages
    let content =
        "# Single-Service Packages Latest Releases\n\n" +
        "The single-service packages provide easy-to-use APIs for each Azure service following the design principals of [Azure Management Libraries for Java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager). If you have specific requirement on certain service API version, you may find appropriate package below. If not, you could always choose the latest release.\n\n" +
        "According to [Azure REST API reference](https://docs.microsoft.com/rest/api/azure/), most request URIs of Azure services require `api-version` as the query-string parameter. All supported API versions for each Azure service can be found via [Azure REST API Specifications](https://github.com/Azure/azure-rest-api-specs/tree/main/specification). For your convenience, we provide more details of the published packages by format below.\n\n" +
        "```\n" +
        "service\n" +
        "* package-tag\n" +
        "    * maven.version\n" +
        "```\n\n" +
        "- `service` for Azure service.\n" +
        "- `package-tag` for included resources with API versions.\n" +
        "- `maven.version` for maven version of the artifact.\n\n";

    const sortedServices = Object.keys(data).sort();
    for (const i in sortedServices) {
        const service = sortedServices[i];
        content += "\n<br/>\n" + "<details>\n" + "<summary> " + service + " </summary>\n\n";
        // 1. sort versions in tags
        const tags = Object.keys(data[service]);
        for (const j in tags) {
            const tag = tags[j];
            data[service][tag].sort(versionCompare);
        }
        // 2. sort tags by their largest version number
        const sortedTags = Object.keys(data[service]).sort(function(tag1, tag2) {
            return versionCompare(data[service][tag1][0], data[service][tag2][0]);
        });
        // 3. write by sorted tags
        for (const j in sortedTags) {
            const tag = sortedTags[j];
            const spec = specs[service] ? specs[service] : service;
            if (servicesInvalidUrl.includes(service)) {
                content += "* " + tag + "\n";
            } else {
                // For certain GA libs with some preview swaggers, we add a new tag in readme.java.md to remove them.
                // e.g. https://github.com/Azure/azure-rest-api-specs/pull/26391/files
                let readmeUrl;
                if(tag.endsWith("-java")) {
                    readmeUrl = getServiceUrl(spec) + "/readme.java.md#tag-" + tag;
                } else {
                    readmeUrl = getServiceUrl(spec) + "#tag-" + tag;
                }
                content += "* [" + tag + "](" + readmeUrl + ")\n";
            }
            for (const k in data[service][tag]) {
                const sdk = data[service][tag][k];
                content += "    * [" + sdk + "](" + groupUrl + "azure-resourcemanager-" + service + "/" + sdk + ")\n";
            }
        }
        content += "</details>\n";
    }
    fs.writeFileSync("docs/SINGLE_SERVICE_PACKAGES.md", content);
}

// compares two versions
// if a > b, return -1; a < b, return 1; else return 0;
function versionCompare(a, b) {
    // custom method to sort versions
    const aVerNums = a.split(".");
    const bVerNums = b.split(".");
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
            const aPatchNums = a.split("-beta.");
            const bPatchNums = b.split("-beta.");
            // sort GA version before beta version
            if (aPatchNums.length < bPatchNums.length) {
                return -1;
            } else if (aPatchNums.length > bPatchNums.length) {
                return 1;
            } else if (aPatchNums.length > 1) {
                // sort according to beta minor version
                return parseInt(bPatchNums[1]) - parseInt(aPatchNums[1]);
            } else {
                return b.localeCompare(a);
            }
        }
    }
}

async function existUrl(url, callback) {
    var result = await request({
        url: url,
        method: "HEAD",
        headers: {
            "user-agent": "AutoCent",
        },
        simple: false,
        resolveWithFullResponse: true,
    });
    return result.statusCode == 200 || result.statusCode == 429;
}

// method to send GET request
async function sendRequest(url) {
    return await request({
        url: url,
        method: "GET",
        headers: {
            "user-agent": "AutoCent",
        },
    });
}

function getSpecsMapping() {
    const api_specs_file = path.join(__dirname, "../../eng/automation/api-specs.yaml");
    const data = fs.readFileSync(api_specs_file, "utf-8");
    let specs = { managedapplications: "resources" };
    Object.entries(yaml.parse(data)).forEach(([rp, service]) => {
        // e.g.
        // web: (rp)
        //   service: appservice (service["service"])
        //   suffix: generated (service["suffix"])
        let serviceName = rp;
        if (service.hasOwnProperty("service")) {
            serviceName = service["service"];
        }
        if (service.hasOwnProperty("suffix")) {
            serviceName = serviceName + "-" + service["suffix"];
        }
        console.log(serviceName);
        specs[serviceName] = rp;
    });
    return specs;
}

autocent();
