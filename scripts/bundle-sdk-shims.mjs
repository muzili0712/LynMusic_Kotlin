// esbuild plugin: 把 lx RN-only / Node-only 依赖重写为 globalThis.lyn.* 桥接调用。
//
// 契约：宿主（Kotlin 侧 JsBridge）会在每个 runtime 里注入一个
// globalThis.lyn 对象，字段完全对齐 JsBridge 的 19 个方法。
// 这些 shim 只做 JS 侧 adapter：把 lx 源代码里 `require('react-native-quick-md5')`
// 之类的调用翻译成 `globalThis.lyn.md5(...)`。
//
// M0 目标：五源（kw/kg/tx/wy/mg）全部能被 esbuild 打出 bundle。
// 具体 host 实现（M5 的 real request / real crypto）与本文件解耦。

export const shimSources = {
  // ---- md5 -----------------------------------------------------------------
  'react-native-quick-md5': `
    const hexHost = (input) => {
      if (typeof input === 'string') input = globalThis.lyn.bufferFrom(input, 'utf8');
      return globalThis.lyn.md5(input);
    };
    export const hex_md5 = hexHost;
    // lx 里还用 stringMd5
    export const stringMd5 = hexHost;
    export const arrayMd5 = hexHost;
    export default { hex_md5: hexHost, stringMd5: hexHost, arrayMd5: hexHost };
  `,

  // ---- base64 --------------------------------------------------------------
  'react-native-quick-base64': `
    const enc = (s, utf8 = true) => {
      if (typeof s === 'string') s = globalThis.lyn.bufferFrom(s, utf8 ? 'utf8' : 'binary');
      return globalThis.lyn.base64Encode(s);
    };
    const dec = (s, utf8 = true) => {
      const bytes = globalThis.lyn.base64Decode(s);
      return utf8 ? globalThis.lyn.bufferToString(bytes, 'utf8') : bytes;
    };
    export { enc as btoa, dec as atob };
    export default { btoa: enc, atob: dec };
  `,

  // ---- Buffer --------------------------------------------------------------
  '@craftzdog/react-native-buffer': `
    class LynBuffer extends Uint8Array {
      static from(input, encoding) {
        if (input instanceof Uint8Array) return new LynBuffer(input);
        if (typeof input === 'string') {
          return new LynBuffer(globalThis.lyn.bufferFrom(input, encoding || 'utf8'));
        }
        if (Array.isArray(input)) return new LynBuffer(Uint8Array.from(input));
        return new LynBuffer(input);
      }
      static alloc(size) { return new LynBuffer(size); }
      static concat(list) {
        let total = 0;
        for (const b of list) total += b.length;
        const out = new LynBuffer(total);
        let off = 0;
        for (const b of list) { out.set(b, off); off += b.length; }
        return out;
      }
      static isBuffer(x) { return x instanceof LynBuffer || x instanceof Uint8Array; }
      toString(encoding) {
        return globalThis.lyn.bufferToString(this, encoding || 'utf8');
      }
    }
    export const Buffer = LynBuffer;
    export default { Buffer: LynBuffer };
  `,

  // ---- pako (zlib inflate) -------------------------------------------------
  pako: `
    export const inflate = (input) => globalThis.lyn.zlibInflate(input);
    export const inflateRaw = (input) => globalThis.lyn.zlibInflate(input);
    export const ungzip = (input) => globalThis.lyn.zlibInflate(input);
    export default { inflate, inflateRaw, ungzip };
  `,

  // ---- iconv-lite ----------------------------------------------------------
  'iconv-lite': `
    export const decode = (input, encoding) => globalThis.lyn.iconvDecode(input, encoding);
    export const encode = (input, encoding) => globalThis.lyn.iconvEncode(input, encoding);
    export default { decode, encode };
  `,

  // ---- background-timer ----------------------------------------------------
  'react-native-background-timer': `
    const setTimeoutShim = (cb, delay) => globalThis.lyn.setTimeout(delay || 0, cb);
    const clearTimeoutShim = (id) => globalThis.lyn.clearTimeout(id);
    const setIntervalShim = (cb, delay) => {
      // M0: 只做 fire-and-forget one-shot，lx 真用到 setInterval 的地方再补
      return globalThis.lyn.setTimeout(delay || 0, cb);
    };
    const clearIntervalShim = (id) => globalThis.lyn.clearTimeout(id);
    export default {
      setTimeout: setTimeoutShim,
      clearTimeout: clearTimeoutShim,
      setInterval: setIntervalShim,
      clearInterval: clearIntervalShim,
      runBackgroundTimer: setIntervalShim,
      stopBackgroundTimer: () => {},
    };
  `,

  // ---- crypto-native (路径 @/utils/nativeModules/crypto) ------------------
  'crypto-native': `
    // lx 里的签名：(data, mode, key, iv) -> ByteArray
    const aesEncrypt = (data, mode, key, iv) => globalThis.lyn.aesEncrypt(data, key, iv, mode);
    const aesDecrypt = (data, mode, key, iv) => globalThis.lyn.aesEncrypt(data, key, iv, mode);
    // lx 在几处用 'Sync' 后缀（同步 API），我们统一桥接到同一组 host 函数
    const aesEncryptSync = (data, mode, key, iv) => globalThis.lyn.aesEncrypt(data, key, iv, mode);
    const aesDecryptSync = (data, mode, key, iv) => globalThis.lyn.aesEncrypt(data, key, iv, mode);
    const desEncrypt = (data, mode, key, iv) => globalThis.lyn.desEncrypt(data, key, iv, mode);
    const desDecrypt = (data, mode, key, iv) => globalThis.lyn.desEncrypt(data, key, iv, mode);
    const desEncryptSync = desEncrypt;
    const desDecryptSync = desEncrypt;
    const rsaEncrypt = (data, publicKey) => globalThis.lyn.rsaEncrypt(data, publicKey);
    const rsaEncryptSync = rsaEncrypt;
    const md5 = (input) => globalThis.lyn.md5(input);
    const sha1 = (input) => globalThis.lyn.sha1(input);
    const sha256 = (input) => globalThis.lyn.sha256(input);
    const hashSHA1 = (input) => globalThis.lyn.sha1(input);
    // lx 用的枚举常量
    const AES_MODE = {
      CBC_128_NoPadding: 'CBC/NoPadding',
      CBC_128_PKCS7Padding: 'CBC/PKCS7Padding',
      ECB_128_NoPadding: 'ECB/NoPadding',
      ECB_128_PKCS7Padding: 'ECB/PKCS7Padding',
      CBC_128_PKCS5Padding: 'CBC/PKCS5Padding',
      ECB_128_PKCS5Padding: 'ECB/PKCS5Padding',
    };
    const RSA_PADDING = {
      PKCS1Padding: 'PKCS1Padding',
      NoPadding: 'NoPadding',
      OAEPPadding: 'OAEPPadding',
    };
    export {
      aesEncrypt, aesDecrypt, aesEncryptSync, aesDecryptSync,
      desEncrypt, desDecrypt, desEncryptSync, desDecryptSync,
      rsaEncrypt, rsaEncryptSync,
      md5, sha1, sha256, hashSHA1,
      AES_MODE, RSA_PADDING,
    };
    export default {
      aesEncrypt, aesDecrypt, aesEncryptSync, aesDecryptSync,
      desEncrypt, desDecrypt, desEncryptSync, desDecryptSync,
      rsaEncrypt, rsaEncryptSync,
      md5, sha1, sha256, hashSHA1,
      AES_MODE, RSA_PADDING,
    };
  `,

  // ---- request (路径 @/utils/request) -------------------------------------
  request: `
    const doRequest = (url, options, callback) => {
      const opt = options || {};
      const promise = globalThis.lyn.request(url, opt).then((resp) => {
        if (typeof callback === 'function') {
          try { callback(null, resp, resp && resp.body); } catch (e) {}
        }
        return resp;
      }).catch((err) => {
        if (typeof callback === 'function') {
          try { callback(err); } catch (_) {}
        }
        throw err;
      });
      // lx 的 request 期望返回 { promise, cancelHttp }
      return {
        promise,
        cancelHttp: () => {},
      };
    };
    // lx 里的别名，多种 import 形式
    export const httpFetch = doRequest;
    export const request = doRequest;
    export const cancelHttp = () => {};
    export default doRequest;
  `,
};

// ---------------------------------------------------------------------------
// esbuild plugin: intercept imports and feed shim source through onLoad.
// ---------------------------------------------------------------------------
export function lynShimsPlugin() {
  return {
    name: 'lyn-shims',
    setup(build) {
      // 1) bare module 名字直接命中
      const bareKeys = Object.keys(shimSources).filter((k) => k !== 'crypto-native' && k !== 'request');
      const bareFilter = new RegExp('^(' + bareKeys.map(escapeRe).join('|') + ')$');
      build.onResolve({ filter: bareFilter }, (args) => ({
        path: args.path,
        namespace: 'lyn-shim',
      }));

      // 2) @/utils/nativeModules/crypto -> crypto-native
      build.onResolve({ filter: /^@\/utils\/nativeModules\/crypto$/ }, () => ({
        path: 'crypto-native',
        namespace: 'lyn-shim',
      }));

      // 3) @/utils/request -> request
      build.onResolve({ filter: /^@\/utils\/request$/ }, () => ({
        path: 'request',
        namespace: 'lyn-shim',
      }));

      // 4) 其他 @/... 路径：走 vendor/lx-sdk/shared/... 兜底（能命中 lx 源里的 utils 共享）
      // vendor/lx-sdk/shared/request.js 同名资源：我们前面已经把 @/utils/request 劫持到 shim 了，
      // 所以真实 shared/request.js 只是留档，不参与打包。

      build.onLoad({ filter: /.*/, namespace: 'lyn-shim' }, (args) => {
        const contents = shimSources[args.path];
        if (!contents) {
          return { errors: [{ text: `[lyn-shim] 未知 shim: ${args.path}` }] };
        }
        return { contents, loader: 'js' };
      });
    },
  };
}

function escapeRe(s) {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
