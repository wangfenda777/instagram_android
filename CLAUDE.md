# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository structure

This repo contains two independent sub-projects:

| Directory | Stack | Purpose |
|---|---|---|
| `ins_vue_uniapp_project/` | uni-app + Vue 3 + Vite | Mobile-style Instagram clone (H5/mini-program) |
| `app/` | Kotlin + Jetpack Compose | Android native app |

Each sub-project has its own dependency management and build toolchain. They are not coupled at the source level.

## Android app (`app/`)

### Commands

- Build debug APK: `./gradlew assembleDebug`
- Build release APK: `./gradlew assembleRelease`
- Run unit tests: `./gradlew test`
- Run instrumented tests: `./gradlew connectedAndroidTest`
- Clean: `./gradlew clean`

### Architecture

- `namespace`: `com.qiwang.ins_android`
- `minSdk`: 24, `targetSdk`/`compileSdk`: 36
- UI is built entirely with **Jetpack Compose** + Material3
- Kotlin `jvmTarget = 11`
- Maven mirrors via Aliyun are configured in `settings.gradle.kts` for faster dependency resolution in China

## Vue/uni-app frontend (`ins_vue_uniapp_project/`)

See `ins_vue_uniapp_project/CLAUDE.md` for full details. Quick reference:

- Install: `npm install`
- Dev server (port 3000, proxies `/api` → `http://112.124.47.169:8081`): `npm run dev`
- Build: `npm run build`
- Node 18.x or 20.x required; use npm
