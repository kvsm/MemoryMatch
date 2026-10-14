# Memory Match

A pass-the-phone memory card game for 2–5 players, with hand-drawn toy and animal cards.

## How to play

1. Pick the number of players (2–5) and how many cards: Easy (12), Medium (20) or Hard (30).
2. On your turn, tap a card to turn it over, then tap a second card.
3. **Match:** the pair is cleared, you score a point and go again.
4. **No match:** both cards flip back and it's the next player's turn.
5. When every pair is found, the player with the most points wins (ties are shared).

## Getting the app

Every push to `main` builds a signed APK with GitHub Actions and publishes it under
**Releases** as `MemoryMatch.apk`. Open the latest release on your phone and install it
(allow installs from your browser if Android asks).

The app's signing key is in `app/signing/` so every build installs over the last one.
To move it out of the repo, add `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and
`KEY_PASSWORD` as repository secrets (same key) and delete the folder — the workflow
prefers secrets when they're set.

## Code tour

- `game/GameEngine.kt` – the rules, pure Kotlin, unit tested in `src/test`.
- `game/GameViewModel.kt` – screen state and the pause before a pair is resolved.
- `ui/` – Jetpack Compose screens: setup, game board with flip animations, results with confetti.
- `res/drawable/card_*.xml` – the 16 card illustrations (8 animals, 8 toys) as vector drawables.

Built with Kotlin, Jetpack Compose and Material 3. Min Android 8.0.
