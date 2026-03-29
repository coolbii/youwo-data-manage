/// <reference types='vitest' />
import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import tailwindcss from '@tailwindcss/vite';
import { nxViteTsPaths } from '@nx/vite/plugins/nx-tsconfig-paths.plugin';
import { nxCopyAssetsPlugin } from '@nx/vite/plugins/nx-copy-assets.plugin';
import { resolve } from 'node:path';

const workspaceRoot = resolve(import.meta.dirname, '..');

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, workspaceRoot, '');
  const apiPort = env.SERVER_PORT?.trim() || '3000';
  const apiTarget = env.VITE_API_PROXY_TARGET?.trim() || `http://localhost:${apiPort}`;

  return {
    root: import.meta.dirname,
    envDir: workspaceRoot,
    cacheDir: '../node_modules/.vite/client',
    server: {
      port: 4200,
      host: 'localhost',
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
        },
      },
    },
    preview: {
      port: 4300,
      host: 'localhost',
    },
    plugins: [vue(), tailwindcss(), nxViteTsPaths(), nxCopyAssetsPlugin(['*.md'])],
    // Uncomment this if you are using workers.
    // worker: {
    //   plugins: () => [ nxViteTsPaths() ],
    // },
    build: {
      outDir: '../dist/client',
      emptyOutDir: true,
      reportCompressedSize: true,
      commonjsOptions: {
        transformMixedEsModules: true,
      },
    },
    test: {
      name: 'client',
      watch: false,
      globals: true,
      environment: 'jsdom',
      include: ['{src,tests}/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
      reporters: ['default'],
      coverage: {
        reportsDirectory: '../coverage/client',
        provider: 'v8' as const,
      },
    },
  };
});
