# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

- Install deps: `npm install`
- Start local dev server: `npm run dev`
- Build production bundle: `npm run build`

Notes:
- `package.json` only defines `dev` and `build`; there is no lint or test script in the repo right now.
- Dev server runs through uni-app/Vite. `vite.config.js` sets the dev server port to `3000` and proxies `/api` to `http://112.124.47.169:8081`.
- The project-level规范文档 says Node `18.x` or `20.x` should be used, and npm is the package manager.

## High-level architecture

This is a uni-app + Vue 3 mobile-style Instagram clone using Vite, Pinia, and axios.

### App shell and routing
- `src/main.js` creates the app with Pinia and `pinia-plugin-persistedstate` enabled globally.
- `src/pages.json` is the real route/tab definition source. Global navigation is set to `custom`, so pages usually render their own top bars instead of relying on native headers.
- Main tabs are:
  - `pages/index/index` — home feed
  - `pages/explore/explore` — explore/search
  - `pages/publish/publish` — post creation
  - `pages/messages/messages` — placeholder (just shows "消息" text)
  - `pages/profile/profile` — current user profile
- Sub-pages (non-tab, navigated via `uni.navigateTo`):
  - `pages/login/login` — authentication
  - `pages/profile/edit-profile` — edit profile fields
  - `pages/profile/edit-field` — single field editor
  - `pages/profile/follow-list` — followers/following list
  - `pages/user-detail/user-detail` — other user's profile
  - `pages/search-overview/search-overview` — filtered search results

### Page pattern: view in `pages/`, state/data logic in `composables/`
The codebase generally splits page rendering from business/data logic:
- page SFCs in `src/pages/**` mostly handle template + local styles + navigation wiring
- data fetching, normalization, and UI state are pushed into `src/composables/use*.js`

Examples:
- `src/pages/index/index.vue` uses `src/composables/useHome.js`
- `src/pages/profile/profile.vue` uses `src/composables/useProfile.js`
- `src/pages/profile/follow-list.vue` uses `src/composables/useFollowList.js`
- `src/pages/explore/explore.vue` uses `src/composables/useExplore.js`
- `src/pages/user-detail/user-detail.vue` uses `src/composables/useUserDetail.js`
- `src/pages/search-overview/search-overview.vue` uses `src/composables/useSearchOverview.js`
- `src/pages/publish/publish.vue` uses `src/composables/usePublish.js` and `src/composables/usePublishTagAutocomplete.js`

When implementing new page behavior, prefer extending the matching composable instead of putting async/network/state orchestration directly into the page SFC.

### API layer
All normal API calls go through `src/api/index.js`:
- axios instance with a request interceptor that injects `Authorization: Bearer <token>` from `userStore.token`
- response interceptor expects backend shape `{ code, message, data }`
- only `code === 200` resolves; the wrapper returns the unwrapped `data`
- `401` clears auth and navigates to `/pages/login/login`
- GET requests are passed as `params`; POST requests are passed as `data`

Implications:
- API modules in `src/api/*.js` should stay thin and only describe endpoint/method/params.
- Callers should expect already-unwrapped `data`, not the full axios response.
- If backend payloads vary (`[]` vs `{ list: [] }`), normalize in composables rather than in page templates.

Exceptions:
- `src/api/auth.js` uses raw `axios.post` for token refresh to avoid interceptor loops.
- `src/api/upload.js` also uses raw axios with `FormData` for image/avatar/video uploads.

### Global state
Pinia stores live in `src/pinia/modules/`.

Current important stores:
- `userStore.js`
  - persists `token`, `refreshToken`, `expiresIn`, `userInfo`
  - `clearAuth()` is the central logout/reset path
- `appStore.js`
  - persists `baseUrl`
  - currently defaults to `http://112.124.47.169:8081`

A recurring pattern in composables is to normalize backend media paths by prefixing relative URLs with `appStore.baseUrl`.
Before changing avatar/image rendering, check whether the field is already absolute (`startsWith('http')`) or needs base URL prefixing.

### Shared UI shell
- `src/components/common/Layout.vue` wraps pages and applies top/bottom safe-area padding from `uni.getSystemInfoSync()`.
- Pages that should respect mobile safe areas typically render inside `<Layout>`.
- Global styles are pulled from `src/assets/styles/index.css`, which imports theme/common CSS.

### Data-shaping conventions
Composables usually adapt backend payloads into UI-friendly objects before exposing them to the page.
Common patterns:
- map backend IDs like `postId`, `userId`, `storyId` to a simpler `id`
- prefix media URLs with `appStore.baseUrl`
- convert backend booleans/nullable fields into stable UI booleans/strings
- expose list/loading/hasMore state from the composable, not the page

Examples:
- `src/composables/useHome.js` shapes stories and feed posts for the home page
- `src/composables/useFollowList.js` normalizes follow/follower user rows and handles paginated tab state
- `src/composables/useProfile.js` merges persisted auth user info with server stats/discover lists
- `src/composables/useUserPostsDetail.js` normalizes post data and manages bidirectional scroll loading

When adding features, keep normalization close to the fetching logic in the composable.

### Current functional areas
- Home feed: stories + post feed + follow action (`useHome.js`, `api/story.js`, `api/post.js`)
- Explore: recommended posts in waterfall layout + search mode (`useExplore.js`, `api/explore.js`, `api/search.js`)
- Auth/login: login stores tokens, then fetches `/api/user/me` and persists user info (`useLogin.js`, `api/auth.js`)
- Profile: stats, discover users, posts grid, followers/following (`useProfile.js`, `useFollowList.js`, `api/user.js`)
- User detail: view other users' profiles with follow/unfollow (`useUserDetail.js`, `api/user.js`)
- Publish: image/video selection + upload + create post with hashtag autocomplete (`usePublish.js`, `usePublishTagAutocomplete.js`, `api/upload.js`, `api/post.js`)
- Post detail: full-screen post viewer with like/save/delete actions (`usePostDetail.js`, `useUserPostsDetail.js`, `api/post.js`)
- Search: multi-tab search results (Recommend/Account/Tag) (`useSearchOverview.js`, `api/search.js`)
- Action feedback: visual animations for like/save interactions (`useActionFeedback.js`)

## Repository-specific guidance

- Follow the existing `script setup` + Composition API style used across the repo.
- Keep styles local in the page/component `scoped` blocks unless the style is truly global.
- Reuse existing composable patterns for:
  - avatar/media URL normalization
  - loading and pagination state
  - mapping backend fields into UI models
- `pages.json` is the authoritative place for routes/tab pages. If you add a page, update it there.
- Because navigation is custom, UI changes to headers/back buttons are usually local to each page rather than centralized.

## Relevant repo docs

The repo contains `开发规范文档.md`. The parts that match the current codebase and are worth preserving here are:
- Use Node 18.x or 20.x.
- Use npm as the package manager.
- Use Vue 3 `<script setup>` and Composition API hooks (`useXxx`) for reusable logic.
- Keep API definitions under `src/api/` and state modules under `src/pinia/modules/`.

Do not assume the directory plan in that document is fully implemented; treat the current `src/` tree as source of truth.