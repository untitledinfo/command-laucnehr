Put real app icons here before running `npm run dist:win`:

- icon.ico  (Windows, ideally 256x256 multi-size .ico)
- icon.icns (macOS)
- icon.png  (Linux, 512x512 recommended)

electron-builder will fail (or fall back to its own default icon) without
these. Any square PNG can be converted to .ico/.icns with a free tool like
https://icoconvert.com or the `electron-icon-builder` npm package.
