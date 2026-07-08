/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue';
  const component: DefineComponent<{}, {}, any>;
  export default component;
}

import type { LauncherApi } from '../electron/preload';

declare global {
  interface Window {
    launcherApi: LauncherApi;
  }
}

export {};
