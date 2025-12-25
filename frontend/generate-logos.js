/**
 * Script to generate PNG and ICO files from SVG logo
 * Run with: node generate-logos.js
 * 
 * Note: This requires 'sharp' package. Install it with: npm install sharp --save-dev
 */

const fs = require('fs');
const path = require('path');

// Check if sharp is available
let sharp;
try {
  sharp = require('sharp');
} catch (e) {
  console.error('Error: sharp package is required. Install it with: npm install sharp --save-dev');
  console.log('\nAlternatively, you can:');
  console.log('1. Use an online SVG to PNG converter (e.g., https://convertio.co/svg-png/)');
  console.log('2. Use the logo.svg file and convert it to:');
  console.log('   - logo192.png (192x192 pixels)');
  console.log('   - logo512.png (512x512 pixels)');
  console.log('   - favicon.ico (16x16, 32x32, 48x48 sizes)');
  process.exit(1);
}

const publicDir = path.join(__dirname, 'public');
const logoSvg = path.join(publicDir, 'logo.svg');
const faviconSvg = path.join(publicDir, 'favicon.svg');

async function generateLogos() {
  try {
    // Generate logo192.png
    await sharp(logoSvg)
      .resize(192, 192)
      .png()
      .toFile(path.join(publicDir, 'logo192.png'));
    console.log('✓ Generated logo192.png');

    // Generate logo512.png
    await sharp(logoSvg)
      .resize(512, 512)
      .png()
      .toFile(path.join(publicDir, 'logo512.png'));
    console.log('✓ Generated logo512.png');

    // Generate favicon.ico (multi-size ICO)
    // Note: sharp doesn't directly support ICO, so we'll create a PNG and suggest conversion
    await sharp(faviconSvg)
      .resize(32, 32)
      .png()
      .toFile(path.join(publicDir, 'favicon-temp.png'));
    console.log('✓ Generated favicon-temp.png (32x32)');
    console.log('  Note: For favicon.ico, you may need to use an online converter');
    console.log('  or use the favicon.svg directly (modern browsers support SVG favicons)');

    console.log('\n✓ Logo generation complete!');
  } catch (error) {
    console.error('Error generating logos:', error.message);
    process.exit(1);
  }
}

generateLogos();

