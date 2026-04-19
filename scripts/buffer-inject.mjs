// 通过 esbuild `inject` 把 Buffer 作为“全局”挂进每个 bundle。
// 引用方式：esbuild 会把 import { Buffer } from 'this-file' 自动加到用 Buffer 的文件头部。
import { Buffer } from '@craftzdog/react-native-buffer';
export { Buffer };
