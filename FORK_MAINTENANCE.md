# Ghosten Player Flutter Packages Fork

This repository contains the player and native Android changes used by the
maintained Ghosten Player TV fork.

## Stable branch

`fork-main` is the long-lived branch. Feature branches target it. The app
repository pins this repository by exact commit SHA; never release the app from
a moving branch reference.

## Required tests

Run from `video_player/`:

```bash
flutter pub get
flutter analyze
flutter test
```

Android-native changes must also pass:

```bash
./gradlew :video_player:testDebugUnitTest
```

The CI workflow creates a temporary Flutter host app to compile the plugin and
run its native unit tests.

## Compatibility contracts

- Track preferences are stored by descriptive metadata, with track id only as
  a last-resort fallback.
- Selecting “None” for subtitles is a persisted preference.
- Subtitle font scale is persisted as the fifth integer in the subtitle style
  payload. Four-value legacy settings migrate to `100%` without data loss.
- Subtitle bottom padding is persisted as the sixth integer. `-1` keeps the
  renderer default; values from `0` through `50` represent a percentage of the
  viewport height. Four- and five-value payloads migrate to `-1`.
- Media3 scales embedded cue sizes and its default cue size; MPV receives the
  same value through `sub-scale`.
- Media3 maps subtitle position to `SubtitleView` bottom padding. MPV maps the
  same value to `sub-pos` and receives the persisted text, background, and edge
  colors. Explicitly positioned ASS cues remain authored-layout content on
  Media3.
- Unsupported tracks must not be selected automatically.
- SSA headers with non-positive `PlayResX` or `PlayResY` are normalized to the
  conventional ASS fallback resolution `384 × 288`; valid headers must remain
  byte-for-byte unchanged.
- Do not log playback URLs, account tokens, or API keys.

## Device regression media

- `Thor Love and Thunder`: TrueHD/DTS selection and track persistence.
- `Thor The Dark World`: embedded bilingual ASS with zero play resolution.

After pushing a package change, update the exact SHA in the app repository,
regenerate `pubspec.lock`, run the app CI checks, and test on the S901 projector.
