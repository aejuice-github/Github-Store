/**
 * Generate bundled-manifest.json from Products.json
 *
 * Products.json is the source of truth. Each product must have:
 *   name, type, platforms, description, category, tags
 *
 * Usage: node generate-manifest.js
 */
const fs = require('fs');

const PRODUCTS_PATH = 'D:/Repositories/ae-framework/Pack Manager/Products.json';
const MANIFEST_PATH = 'D:/Repositories/component-manager/core/data/src/jvmMain/resources/bundled-manifest.json';
const SAMPLE_PATH = 'D:/Repositories/component-manager/sample-manifest.json';

const products = JSON.parse(fs.readFileSync(PRODUCTS_PATH, 'utf8'));

function toId(name) {
  return name.toLowerCase()
    .replace(/&/g, 'and')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '');
}

const S3_BASE = 'https://snas.aejuice.xyz/aejuice-plugins';

function getPlatforms(product) {
  const p = product.platforms;
  const result = {};
  const type = product.type;
  const encodedName = encodeURIComponent(`AEJuice ${product.name}`);

  if (type === 'software') {
    if (p.win) {
      const file = p.win.file || `${product.name}.exe`;
      result.windows = {
        url: `${S3_BASE}/Software/${encodeURIComponent(file)}`,
        sha256: '', size: 85000000,
        installPath: `C:/Program Files/AEJuice/${product.name}/`,
        requiresAdmin: true,
        fileName: file
      };
    }
    if (p.mac) {
      const file = p.mac.file || `${product.name}.dmg`;
      result.macos = {
        url: `${S3_BASE}/Software/${encodeURIComponent(file)}`,
        sha256: '', size: 82000000,
        installPath: `/Applications/${product.name}.app/`,
        requiresAdmin: false,
        fileName: file
      };
    }
  } else if (type === 'extension') {
    const file = product.file || `${product.name}.zxp`;
    result.windows = {
      url: `${S3_BASE}/Extensions/${encodeURIComponent(file)}`,
      sha256: '', size: 1500000,
      installPath: 'C:/Program Files/Common Files/Adobe/CEP/extensions/',
      requiresAdmin: true,
      fileName: file
    };
    result.macos = {
      url: `${S3_BASE}/Extensions/${encodeURIComponent(file)}`,
      sha256: '', size: 1400000,
      installPath: '/Library/Application Support/Adobe/CEP/extensions/',
      requiresAdmin: true,
      fileName: file
    };
  } else if (type === 'plugin') {
    const winFileName = `${encodedName}.aex`;
    const macFileName = `${encodedName}.plugin`;
    result.windows = {
      url: `${S3_BASE}/Plugins/Win/Adobe/${winFileName}`,
      sha256: '', size: 3500000,
      installPath: 'C:/Program Files/Adobe/Common/Plug-ins/7.0/MediaCore/',
      requiresAdmin: true,
      fileName: `AEJuice ${product.name}.aex`
    };
    result.macos = {
      url: `${S3_BASE}/Plugins/Mac/Adobe/${macFileName}`,
      sha256: '', size: 3200000,
      installPath: '/Library/Application Support/Adobe/Common/Plug-ins/7.0/MediaCore/',
      requiresAdmin: true,
      fileName: `AEJuice ${product.name}.plugin`
    };
  } else {
    const scriptFileName = `AEJuice ${product.name}.jsxbin`;
    result.windows = {
      url: `${S3_BASE}/Scripts/${encodedName}.jsxbin`,
      sha256: '', size: 256000,
      installPath: 'scriptui',
      requiresAdmin: true,
      fileName: scriptFileName
    };
    result.macos = {
      url: `${S3_BASE}/Scripts/${encodedName}.jsxbin`,
      sha256: '', size: 250000,
      installPath: 'scriptui',
      requiresAdmin: true,
      fileName: scriptFileName
    };
  }
  return result;
}

function getCompatibleApps(product) {
  const p = product.platforms;
  const type = product.type;
  const apps = [];

  if (type === 'software') {
    return ['Standalone'];
  }

  if (type === 'script') {
    return ['After Effects'];
  }

  if (type === 'extension') {
    return ['After Effects', 'Premiere Pro'];
  }

  if (p.adobe) {
    apps.push('After Effects');
    apps.push('Premiere Pro');
  }
  if (p.openfx) {
    apps.push('DaVinci Resolve');
    apps.push('Vegas');
    apps.push('Other OpenFX hosts');
  }
  if (p.fxplug) {
    apps.push('Final Cut Pro');
  }

  if (apps.length === 0) {
    apps.push('After Effects');
  }

  return apps;
}

// Validate products have required fields
const missing = products.filter(p => !p.description || !p.category || !p.tags);
if (missing.length > 0) {
  console.error('ERROR: Products missing metadata:');
  missing.forEach(p => console.error(`  - ${p.name}: missing ${!p.description ? 'description ' : ''}${!p.category ? 'category ' : ''}${!p.tags ? 'tags' : ''}`));
  process.exit(1);
}

const seen = {};
const components = [];

for (const p of products) {
  const id = toId(p.name);
  const uniqueId = seen[id] ? id + '-' + p.type : id;
  seen[id] = true;

  const version = p.version || '1.0.0';
  const author = ['Duik Angela', 'Gyroflow'].includes(p.name) ? 'Community' : 'AEJuice';
  const runnable = p.type === 'software';

  const compatibleApps = getCompatibleApps(p);

  const component = {
    id: uniqueId,
    name: p.name,
    type: p.type,
    description: p.description,
    tooltip: p.description.split('.')[0],
    version: version,
    author: author,
    category: p.category,
    tags: p.tags,
    icon: '',
    screenshots: [],
    platforms: getPlatforms(p),
    dependencies: [],
    runnable: runnable,
    changelog: `### ${version}\n- Latest release`,
    compatibleApps: compatibleApps,
    price: p.price || 0
  };

  if (runnable) {
    component.runCommand = uniqueId;
  }

  components.push(component);
}

const categories = [...new Set(components.map(c => c.category))].sort();

const manifest = {
  version: '1.0',
  appVersion: '1.0.0',
  appUpdateUrl: 'https://snas.aejuice.xyz/aejuice-plugins/ComponentManager.exe',
  categories: categories,
  components: components
};

const output = JSON.stringify(manifest, null, 2);
fs.writeFileSync(MANIFEST_PATH, output, 'utf8');
fs.writeFileSync(SAMPLE_PATH, output, 'utf8');

console.log(`Generated manifest: ${components.length} components, ${categories.length} categories`);
console.log(`Categories: ${categories.join(', ')}`);
const counts = {};
for (const c of components) {
  counts[c.category] = (counts[c.category] || 0) + 1;
}
for (const [cat, count] of Object.entries(counts).sort()) {
  console.log(`  ${cat}: ${count}`);
}
console.log(`\nWritten to:\n  ${MANIFEST_PATH}\n  ${SAMPLE_PATH}`);
