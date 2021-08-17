const args = require('yargs').argv;
const os = require('os');
const fs = require('fs');
const path = require('path');

function credcheck(dir) {
    console.log('Path: ' + dir);

    const redactDict = new Map();
    // storage account keys
    redactDict.set(/\\"keyName\\":\\"key1\\",\\"value\\":\\"(.*?)\\"/g, '\\"keyName\\":\\"key1\\",\\"value\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"keyName\\":\\"key2\\",\\"value\\":\\"(.*?)\\"/g, '\\"keyName\\":\\"key2\\",\\"value\\":\\"***REMOVED***\\"');
    redactDict.set(/;AccountKey=(.*?)(;|\\")/g, ';AccountKey=***REMOVED***$2');
    redactDict.set(/\\"primaryMasterKey\\":\\"(.*?)\\"/g, '\\"primaryMasterKey\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"primaryReadonlyMasterKey\\":\\"(.*?)\\"/g, '\\"primaryReadonlyMasterKey\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"secondaryMasterKey\\":\\"(.*?)\\"/g, '\\"secondaryMasterKey\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"secondaryReadonlyMasterKey\\":\\"(.*?)\\"/g, '\\"secondaryReadonlyMasterKey\\":\\"***REMOVED***\\"');
    redactDict.set(/;SharedAccessKey=(.*?)(;|\\")/g, ';SharedAccessKey=***REMOVED***$2');
    redactDict.set(/\\"primaryKey\\":\\"(.*?)\\"/g, '\\"primaryKey\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"secondaryKey\\":\\"(.*?)\\"/g, '\\"secondaryKey\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"secretText\\":\\"(.*?)\\"/g, '\\"secretText\\":\\"***REMOVED***\\"');
    redactDict.set(/&sig=(.*?)(&|\\")/g, '&sig=***REMOVED***$2');
    redactDict.set(/\\"DOCKER_REGISTRY_SERVER_PASSWORD\\":\\"(.*?)\\"/g, '\\"DOCKER_REGISTRY_SERVER_PASSWORD\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"ServiceApiKey\\":\\"(.*?)\\"/g, '\\"ServiceApiKey\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"Givex.Password\\":\\"(.*?)\\"/g, '\\"Givex.Password\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"Sendgrid.Password\\":\\"(.*?)\\"/g, '\\"Sendgrid.Password\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"primary\\":\\"(.*?)\\"/g, '\\"primary\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"secondary\\":\\"(.*?)\\"/g, '\\"secondary\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"accessSAS\\": \\"(.*?)\\"/g, '\\"accessSAS\\": \\"***REMOVED***\\"');
    redactDict.set(/\\"administratorLoginPassword\\":\\"(.*?)\\"/g, '\\"administratorLoginPassword\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"permissions\\":\\"Full\\",\\"value\\":\\"(.*?)\\"/g, '\\"keyName\\":\\"key1\\",\\"value\\":\\"***REMOVED***\\"');
    redactDict.set(/\\"adminPassword\\":{\\"type\\":\\"String\\",\\"value\\":\\"(.*?)\\"}/g, '\\"adminPassword\\":{\\"type\\":\\"String\\",\\"value\\":\\"***REMOVED***\\"}');
    redactDict.set(/\\"connectionString\\":\\"(.*?)\\"/g, '\\"connectionString\\":\\"***REMOVED***\\"');

    credcheckRecursive(dir, redactDict);
}

function credcheckRecursive(dir, redactDict) {
    if (fs.existsSync(dir)) {
        fs.readdirSync(dir).forEach(function(file, index) {
            const curPath = path.join(dir, file);
            if (fs.lstatSync(curPath).isDirectory()) {
                // recurse
                credcheckRecursive(curPath, redactDict);
            } else {
                if (curPath.endsWith('.json')) {
                    const content = fs.readFileSync(curPath).toString('utf8');
                    var redactedContent = content;
                    for (const [key, value] of redactDict.entries()) {
                        redactedContent = redactedContent.replace(key, value);
                    }
                    if (redactedContent !== content) {
                        console.log('File redacted: ' + curPath);

                        fs.writeFileSync(curPath, redactedContent);
                    }
                }
            }
        });
    }
}

const dir = args['path']

credcheck(dir);
