## System dependencies

- `npm install -g elm-live` (used by the existing `Makefile` target)

## Install project dependencies

Run `npm install` from the `ui` directory to pull in Elm, Tailwind, PostCSS, and friends.

## Tailwind CSS

- `npm run build:css` compiles `src/styles.css` (Tailwind + any extra layers you add) into `static/styles.css`.
- Use `npm run watch:css` in another terminal to rebuild the stylesheet automatically while you edit Elm or CSS.
- Keep `custom.html` pointed at `static/styles.css` and make your Tailwind changes inside `src/styles.css`.

## Development server

`make run` now rebuilds the CSS and launches `elm-live` with the existing flags. Keep a separate watch process running if you want live Tailwind updates.
