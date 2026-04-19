// lx 五源 esbuild 打包主入口。
// 每个 source ({kw,kg,tx,wy,mg}) 独立打出一个 IIFE bundle，产出到：
//   shared/online/src/commonMain/composeResources/files/sdk/{src}.js
//
// 打包完成后，在 IIFE 尾部把 default 挂到 globalThis.__lyn_source_{src}，
// runtime 侧 evaluate 完 bundle 后从这个全局变量取入口对象。
//
// 合规背景: lx SDK 为未授权抓取六大音乐平台 RN app (GPL-3.0)，合规自担。

import * as esbuild from 'esbuild';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { promises as fs } from 'node:fs';

import { lynShimsPlugin } from './bundle-sdk-shims.mjs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(__dirname, '..');
const VENDOR = path.join(REPO_ROOT, 'vendor', 'lx-sdk');
const OUT_DIR = path.join(REPO_ROOT, 'shared', 'online', 'src', 'commonMain', 'composeResources', 'files', 'sdk');

const SOURCES = ['kw', 'kg', 'tx', 'wy', 'mg'];

// ---- 自建内部 shim：lx 内部 utils/api-source/... ----------------------------
// lx 源里写的 '../../index' 实际指向 src/utils/index.ts（合并了 common.ts），
// '../api-source' 指向 musicSdk/api-source.js（又依赖 @/store/... TS 源和 api-source-info.ts）。
// M0 目标只是打包通过 + evaluate 不抛，所以这两个统一替换成最小 stub。
const INTERNAL_SHIMS = {
  'lx-internal-index': `
    // lx utils/index.ts + common.ts 的最小 JS 兼容子集
    export const decodeName = (str) => {
      if (str == null) return '';
      return String(str)
        .replace(/&amp;/g, '&')
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&quot;/g, '"')
        .replace(/&#039;/g, "'")
        .replace(/&apos;/g, "'")
        .replace(/&nbsp;/g, ' ');
    };
    export const sizeFormate = (size) => {
      if (isNaN(size)) return '-';
      const companys = ['B', 'KB', 'MB', 'GB', 'TB'];
      let i = 0;
      while (size >= 1024 && i < companys.length - 1) { size /= 1024; i++; }
      return size.toFixed(2) + companys[i];
    };
    export const formatPlayTime = (time) => {
      const m = (time / 60) | 0;
      const s = (time % 60) | 0;
      return (m < 10 ? '0' + m : m) + ':' + (s < 10 ? '0' + s : s);
    };
    export const formatPlayTime2 = formatPlayTime;
    export const dateFormat = (date, fmt = 'Y-M-D h:m:s') => {
      const d = new Date(date);
      const map = {
        Y: d.getFullYear(),
        M: ('0' + (d.getMonth() + 1)).slice(-2),
        D: ('0' + d.getDate()).slice(-2),
        h: ('0' + d.getHours()).slice(-2),
        m: ('0' + d.getMinutes()).slice(-2),
        s: ('0' + d.getSeconds()).slice(-2),
      };
      return fmt.replace(/[YMDhms]/g, (k) => map[k]);
    };
    export const dateFormat2 = (time) => {
      const diff = Date.now() - time;
      const minute = 60 * 1000;
      const hour = 60 * minute;
      const day = 24 * hour;
      if (diff < minute) return '刚刚';
      if (diff < hour) return ((diff / minute) | 0) + '分钟前';
      if (diff < day) return ((diff / hour) | 0) + '小时前';
      if (diff < 30 * day) return ((diff / day) | 0) + '天前';
      return dateFormat(time, 'Y-M-D');
    };
    export const formatPlayCount = (num) => {
      if (num > 100000000) return (num / 10000 / 10000).toFixed(2) + '亿';
      if (num > 10000) return (num / 10000).toFixed(2) + '万';
      return '' + num;
    };
    export const b64DecodeUnicode = (str) => {
      const bytes = globalThis.lyn.base64Decode(str);
      return globalThis.lyn.bufferToString(bytes, 'utf8');
    };
    export const compareVer = () => 0;
    export const langS2T = (s) => s;
  `,

  'lx-internal-api-source': `
    // musicSdk/api-source.js 的最小 stub。M0 不走 getMusicUrl，
    // 留空方法以避免 import 报错；真正实现在 T5/M5 接入 host 网络后再补。
    export const apis = (_src) => ({
      getMusicUrl: (_songInfo, _type) => ({
        promise: Promise.reject(new Error('apis.getMusicUrl: not implemented in M0 shim')),
      }),
      getLyric: () => ({ promise: Promise.reject(new Error('apis.getLyric: not implemented')) }),
      getPic: () => ({ promise: Promise.reject(new Error('apis.getPic: not implemented')) }),
    });
    export const supportQuality = {};
    export default { apis, supportQuality };
  `,

  // kg/vendors/infSign.min.js (压缩签名，尝试直接 bundle；若 esbuild 打不过就在这里替换成 throw)
  // 默认 pass-through, 但留一个开关；若打包失败会切到 stub。
  'lx-internal-stub-empty': `
    export default {};
  `,
};

// ---- 路径重写 plugin -------------------------------------------------------
function internalRemapPlugin() {
  return {
    name: 'lyn-internal-remap',
    setup(build) {
      // 1) '../../request' / '../../../request' 来自 vendor/lx-sdk/{src}/**
      //    本应是 src/utils/request.js；直接重写到 shim 'request'
      build.onResolve({ filter: /^(\.\.\/)+request$/ }, () => ({
        path: 'request',
        namespace: 'lyn-shim',
      }));

      // 2) '../../index' 来自五源子文件：指向 src/utils/index.ts；重写到 lx-internal-index shim
      build.onResolve({ filter: /^(\.\.\/)+index$/ }, () => ({
        path: 'lx-internal-index',
        namespace: 'lyn-internal',
      }));

      // 3) '../api-source' 来自五源 index.js：重写到 lx-internal-api-source shim
      build.onResolve({ filter: /^\.\.\/api-source$/ }, () => ({
        path: 'lx-internal-api-source',
        namespace: 'lyn-internal',
      }));

      // 4) '../utils' 来自五源子文件 → vendor/lx-sdk/shared/utils.js
      build.onResolve({ filter: /^\.\.\/utils$/ }, (args) => ({
        path: path.join(VENDOR, 'shared', 'utils.js'),
      }));

      // 5) '../../utils' 来自 temp/ 二级目录 → vendor/lx-sdk/shared/utils.js
      build.onResolve({ filter: /^\.\.\/\.\.\/utils$/ }, () => ({
        path: path.join(VENDOR, 'shared', 'utils.js'),
      }));

      // 6) 'buffer' (Node builtin) → @craftzdog/react-native-buffer shim
      build.onResolve({ filter: /^buffer$/ }, () => ({
        path: '@craftzdog/react-native-buffer',
        namespace: 'lyn-shim',
      }));

      // 内部 shim loader
      build.onLoad({ filter: /.*/, namespace: 'lyn-internal' }, (args) => {
        const contents = INTERNAL_SHIMS[args.path];
        if (!contents) {
          return { errors: [{ text: `[lyn-internal] 未知 shim: ${args.path}` }] };
        }
        return { contents, loader: 'js' };
      });
    },
  };
}

// ---- 主流程 ----------------------------------------------------------------
async function bundleOne(src) {
  const entry = path.join(VENDOR, src, 'index.js');
  const out = path.join(OUT_DIR, `${src}.js`);

  await esbuild.build({
    entryPoints: [entry],
    bundle: true,
    outfile: out,
    format: 'iife',
    globalName: '__lynSource',
    target: 'es2020',
    platform: 'neutral',
    logLevel: 'info',
    mainFields: ['module', 'main'],
    conditions: ['import', 'default'],
    loader: { '.js': 'js', '.ts': 'ts' },
    // 把 Buffer 注入到用到它但未 import 的文件（例如 kw/decodeLyric.js）
    inject: [path.join(__dirname, 'buffer-inject.mjs')],
    footer: {
      js: `globalThis.__lyn_source_${src} = (typeof __lynSource !== 'undefined' && __lynSource.default) ? __lynSource.default : __lynSource;`,
    },
    plugins: [internalRemapPlugin(), lynShimsPlugin()],
  });

  const stat = await fs.stat(out);
  console.log(`[lyn-bundle] ${src} -> ${path.relative(REPO_ROOT, out)} (${stat.size} bytes)`);
}

async function main() {
  await fs.mkdir(OUT_DIR, { recursive: true });
  for (const src of SOURCES) {
    await bundleOne(src);
  }
  console.log('[lyn-bundle] done. 5 sources built.');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
