const fs = require('fs');

const bundledRaw = fs.readFileSync(__dirname + '/bundled-original.json', 'utf8');
let bundled;
const parsed = JSON.parse(bundledRaw);
bundled = Array.isArray(parsed) ? parsed : parsed.components || [];

const server = JSON.parse(fs.readFileSync('D:/Repositories/component-manager/qt/resources/server-manifest.json', 'utf8'));

const bundledByName = {};
bundled.forEach(b => { bundledByName[b.name] = b; });

const merged = server.map(s => {
  const b = bundledByName[s.name];
  const result = { ...s };
  if (b) {
    if (b.description) result.description = b.description;
    if (b.category) result.category = b.category;
    if (b.tags && b.tags.length) result.tags = b.tags;
    if (b.author) result.author = b.author;
    if (b.price) result.price = b.price;
    if (b.pageUrl) result.pageUrl = b.pageUrl;
    if (b.compatibleApps && b.compatibleApps.length) result.compatibleApps = b.compatibleApps;
  }
  return result;
});

const missing = server.filter(s => !bundledByName[s.name]).map(s => s.name);
console.log('Total server:', server.length);
console.log('Matched with bundled:', server.length - missing.length);
if (missing.length) console.log('No bundled data for:', missing.join(', '));

fs.writeFileSync('D:/Repositories/component-manager/qt/resources/Products.json', JSON.stringify(merged, null, 2));
console.log('Wrote merged Products.json');
