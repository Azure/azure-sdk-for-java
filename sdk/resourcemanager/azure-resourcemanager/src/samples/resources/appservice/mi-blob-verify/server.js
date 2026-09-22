const http = require('http');
const https = require('https');

// The web app reaches Blob storage with its managed identity (no account key or connection string).
// This verifier has no npm dependencies so it deploys without a build step: it fetches a token from the
// App Service managed-identity endpoint and calls the Blob REST API directly.
const endpoint = (process.env.STORAGE_BLOB_ENDPOINT || '').replace(/\/$/, '');
const container = process.env.STORAGE_CONTAINER_NAME || 'verify';
const apiVersion = '2021-08-06';

function request(method, url, headers, body) {
  return new Promise((resolve, reject) => {
    const req = https.request(url, { method, headers }, res => {
      const chunks = [];
      res.on('data', d => chunks.push(d));
      res.on('end', () => resolve({ status: res.statusCode, body: Buffer.concat(chunks).toString() }));
    });
    req.on('error', reject);
    if (body) {
      req.write(body);
    }
    req.end();
  });
}

function getToken() {
  // App Service injects IDENTITY_ENDPOINT / IDENTITY_HEADER for the managed identity.
  const url = process.env.IDENTITY_ENDPOINT
    + '?resource=https://storage.azure.com/&api-version=2019-08-01';
  return new Promise((resolve, reject) => {
    http.get(url, { headers: { 'X-IDENTITY-HEADER': process.env.IDENTITY_HEADER } }, res => {
      const chunks = [];
      res.on('data', d => chunks.push(d));
      res.on('end', () => {
        try {
          resolve(JSON.parse(Buffer.concat(chunks).toString()).access_token);
        } catch (e) {
          reject(e);
        }
      });
    }).on('error', reject);
  });
}

async function verify() {
  const token = await getToken();
  const auth = { 'Authorization': 'Bearer ' + token, 'x-ms-version': apiVersion, 'x-ms-date': new Date().toUTCString() };
  const content = 'hello-from-managed-identity';

  // Create the container (ignore 409 Conflict if it already exists).
  const create = await request('PUT', endpoint + '/' + container + '?restype=container', auth);
  if (create.status !== 201 && create.status !== 409) {
    throw new Error('create container failed: ' + create.status + ' ' + create.body);
  }

  // Upload and read back a blob using only the managed identity.
  const blobUrl = endpoint + '/' + container + '/hello.txt';
  const put = await request('PUT', blobUrl,
    Object.assign({ 'x-ms-blob-type': 'BlockBlob', 'Content-Length': Buffer.byteLength(content) }, auth), content);
  if (put.status !== 201) {
    throw new Error('upload failed: ' + put.status + ' ' + put.body);
  }
  const get = await request('GET', blobUrl, auth);
  return { ok: get.body === content, endpoint, container, read: get.body };
}

const server = http.createServer(async (req, res) => {
  res.setHeader('content-type', 'application/json');
  try {
    res.end(JSON.stringify(await verify()));
  } catch (e) {
    res.statusCode = 500;
    res.end(JSON.stringify({ ok: false, error: e.message }));
  }
});
server.listen(process.env.PORT || 8080, () => console.log('listening'));
