const args = require('yargs').argv;
const os = require('os');
const fs = require('fs');
const path = require('path');
const parser = require("pom-parser");
const colors = require('colors');


function servcheck() {
    console.log('[INFO] starting to check api versions of services...\n');
    readPom();
}


function readPom(callback) {
    var opts = {
        filePath: __dirname + "/pom.xml",
    };
    parser.parse(opts, function(err, response) {
        if (err) {
            console.log('[ERROR] ' + err);
            process.exit(1);
        }
        console.log('[INFO] reading modules from pom...');
        readProjSpecs(response.pomObject.project.profiles.profile[0].modules.module);
    });
}

const mappings = require('./api-specs.json');

function readProjSpecs(modules) {
    console.log('[INFO] reading specs in project...');
    var map = {};
    Object.keys(mappings).forEach(key => {
        // skip graphrbac as it moves to MSGraph now
        if (key == 'graphrbac') {
            return;
		}
        if (modules.includes(mappings[key].dir)) {
            var val = getCurrentApiVersion(mappings[key].args);
            if (val !== undefined) {
                map[key] = {};
                map[key].name = key;
                map[key].tag = val;
                map[key].source = mappings[key].source;
            }
        }
    });
    readLatestSpecs(map);
}

function getCurrentApiVersion(value) {
    var res = undefined;
    value.split(/\s+/).forEach(item => {
        if (item.includes('--tag=')) {
            res = item.replace('--tag=', '');
        }
    });
    return res;
}

function readLatestSpecs(map) {
    console.log('[INFO] reading specs from swagger root...');
    Object.keys(map).forEach(item => {
        readLatestApiVersion(map[item]);
    });
}

const dir = args['spec-root'];

function readLatestApiVersion(spec) {
    console.log('\n[Service] ' + spec.name);
    console.log('    current: ' + spec.tag);
    const filePath = path.join(dir, spec.source);
    const content = fs.readFileSync(filePath).toString('utf8');

    var latest = {};
    var allTags = [];
    var lines = content.split('\n');
    lines.forEach(line => {
        if (line.startsWith('tag:')) {
            if (latest.tag === undefined || latest.tag != undefined && line.includes(spec.name)) {
                latest.tag = getApiVersion(line, 'tag:');
            }
        }
        if (line.includes('Tag:') && !line.includes(' and ')) {
            allTags.push(getApiVersion(line, '### Tag:'));
        }
    });

    console.log('    latest: ' + latest.tag);

    if (spec.tag !== latest.tag) {
        allTags.sort().reverse();
        console.log('\n    potential versions to upgrade:');
        allTags.forEach(tag => {
            if (tag == spec.tag) {
                console.log('        ' + tag.bold.underline.yellow);
            } else {
                console.log('        ' + tag);
            }
        });
    }

    return latest;
}

function getApiVersion(value, target) {
    return value.replace(target, '').replace('\r', '').trim();
}

servcheck();