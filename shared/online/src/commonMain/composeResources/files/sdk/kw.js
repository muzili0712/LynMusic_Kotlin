var __lynSource = (() => {
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __esm = (fn, res) => function __init() {
    return fn && (res = (0, fn[__getOwnPropNames(fn)[0]])(fn = 0)), res;
  };
  var __export = (target, all) => {
    for (var name in all)
      __defProp(target, name, { get: all[name], enumerable: true });
  };
  var __copyProps = (to, from, except, desc) => {
    if (from && typeof from === "object" || typeof from === "function") {
      for (let key of __getOwnPropNames(from))
        if (!__hasOwnProp.call(to, key) && key !== except)
          __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
    }
    return to;
  };
  var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);

  // lyn-shim:@craftzdog/react-native-buffer
  var LynBuffer, Buffer2;
  var init_react_native_buffer = __esm({
    "lyn-shim:@craftzdog/react-native-buffer"() {
      init_buffer_inject();
      LynBuffer = class _LynBuffer extends Uint8Array {
        static from(input, encoding) {
          if (input instanceof Uint8Array) return new _LynBuffer(input);
          if (typeof input === "string") {
            return new _LynBuffer(globalThis.lyn.bufferFrom(input, encoding || "utf8"));
          }
          if (Array.isArray(input)) return new _LynBuffer(Uint8Array.from(input));
          return new _LynBuffer(input);
        }
        static alloc(size) {
          return new _LynBuffer(size);
        }
        static concat(list) {
          let total = 0;
          for (const b of list) total += b.length;
          const out = new _LynBuffer(total);
          let off = 0;
          for (const b of list) {
            out.set(b, off);
            off += b.length;
          }
          return out;
        }
        static isBuffer(x) {
          return x instanceof _LynBuffer || x instanceof Uint8Array;
        }
        toString(encoding) {
          return globalThis.lyn.bufferToString(this, encoding || "utf8");
        }
      };
      Buffer2 = LynBuffer;
    }
  });

  // buffer-inject.mjs
  var init_buffer_inject = __esm({
    "buffer-inject.mjs"() {
      init_react_native_buffer();
    }
  });

  // lyn-shim:pako
  var pako_exports = {};
  __export(pako_exports, {
    default: () => pako_default,
    inflate: () => inflate,
    inflateRaw: () => inflateRaw,
    ungzip: () => ungzip
  });
  var inflate, inflateRaw, ungzip, pako_default;
  var init_pako = __esm({
    "lyn-shim:pako"() {
      init_buffer_inject();
      inflate = (input) => globalThis.lyn.zlibInflate(input);
      inflateRaw = (input) => globalThis.lyn.zlibInflate(input);
      ungzip = (input) => globalThis.lyn.zlibInflate(input);
      pako_default = { inflate, inflateRaw, ungzip };
    }
  });

  // lyn-shim:iconv-lite
  var iconv_lite_exports = {};
  __export(iconv_lite_exports, {
    decode: () => decode,
    default: () => iconv_lite_default,
    encode: () => encode
  });
  var decode, encode, iconv_lite_default;
  var init_iconv_lite = __esm({
    "lyn-shim:iconv-lite"() {
      init_buffer_inject();
      decode = (input, encoding) => globalThis.lyn.iconvDecode(input, encoding);
      encode = (input, encoding) => globalThis.lyn.iconvEncode(input, encoding);
      iconv_lite_default = { decode, encode };
    }
  });

  // ../vendor/lx-sdk/kw/index.js
  var index_exports = {};
  __export(index_exports, {
    default: () => index_default
  });
  init_buffer_inject();

  // lyn-shim:request
  init_buffer_inject();
  var doRequest = (url, options, callback) => {
    const opt = options || {};
    const promise = globalThis.lyn.request(url, opt).then((resp) => {
      if (typeof callback === "function") {
        try {
          callback(null, resp, resp && resp.body);
        } catch (e) {
        }
      }
      return resp;
    }).catch((err) => {
      if (typeof callback === "function") {
        try {
          callback(err);
        } catch (_) {
        }
      }
      throw err;
    });
    return {
      promise,
      cancelHttp: () => {
      }
    };
  };
  var httpFetch = doRequest;

  // ../vendor/lx-sdk/kw/tipSearch.js
  init_buffer_inject();
  var tipSearch_default = {
    regExps: {
      relWord: /RELWORD=(.+)/
    },
    requestObj: null,
    async tipSearchBySong(str) {
      this.cancelTipSearch();
      this.requestObj = httpFetch(`https://tips.kuwo.cn/t.s?corp=kuwo&newver=3&p2p=1&notrace=0&c=mbox&w=${encodeURIComponent(str)}&encoding=utf8&rformat=json`, {
        Referer: "http://www.kuwo.cn/"
      });
      return this.requestObj.promise.then(({ body, statusCode }) => {
        if (statusCode != 200 || !body.WORDITEMS) return Promise.reject(new Error("\u8BF7\u6C42\u5931\u8D25"));
        return body.WORDITEMS;
      });
    },
    handleResult(rawData) {
      return rawData.map((item) => item.RELWORD);
    },
    cancelTipSearch() {
      if (this.requestObj && this.requestObj.cancelHttp) this.requestObj.cancelHttp();
    },
    async search(str) {
      return this.tipSearchBySong(str).then((result) => this.handleResult(result));
    }
  };

  // ../vendor/lx-sdk/kw/musicSearch.js
  init_buffer_inject();

  // lyn-internal:lx-internal-index
  init_buffer_inject();
  var decodeName = (str) => {
    if (str == null) return "";
    return String(str).replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&quot;/g, '"').replace(/&#039;/g, "'").replace(/&apos;/g, "'").replace(/&nbsp;/g, " ");
  };
  var formatPlayTime = (time) => {
    const m = time / 60 | 0;
    const s = time % 60 | 0;
    return (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
  };
  var dateFormat = (date, fmt = "Y-M-D h:m:s") => {
    const d = new Date(date);
    const map = {
      Y: d.getFullYear(),
      M: ("0" + (d.getMonth() + 1)).slice(-2),
      D: ("0" + d.getDate()).slice(-2),
      h: ("0" + d.getHours()).slice(-2),
      m: ("0" + d.getMinutes()).slice(-2),
      s: ("0" + d.getSeconds()).slice(-2)
    };
    return fmt.replace(/[YMDhms]/g, (k) => map[k]);
  };
  var dateFormat2 = (time) => {
    const diff = Date.now() - time;
    const minute = 60 * 1e3;
    const hour = 60 * minute;
    const day = 24 * hour;
    if (diff < minute) return "\u521A\u521A";
    if (diff < hour) return (diff / minute | 0) + "\u5206\u949F\u524D";
    if (diff < day) return (diff / hour | 0) + "\u5C0F\u65F6\u524D";
    if (diff < 30 * day) return (diff / day | 0) + "\u5929\u524D";
    return dateFormat(time, "Y-M-D");
  };

  // ../vendor/lx-sdk/kw/util.js
  init_buffer_inject();

  // ../vendor/lx-sdk/shared/utils.js
  init_buffer_inject();

  // lyn-shim:react-native-quick-md5
  init_buffer_inject();
  var hexHost = (input) => {
    if (typeof input === "string") input = globalThis.lyn.bufferFrom(input, "utf8");
    return globalThis.lyn.md5(input);
  };
  var stringMd5 = hexHost;

  // ../vendor/lx-sdk/shared/utils.js
  var toMD5 = (str) => stringMd5(str);

  // lyn-shim:crypto-native
  init_buffer_inject();
  var aesEncryptSync = (data, mode, key, iv) => globalThis.lyn.aesEncrypt(data, key, iv, mode);
  var aesDecryptSync = (data, mode, key, iv) => globalThis.lyn.aesEncrypt(data, key, iv, mode);
  var AES_MODE = {
    CBC_128_NoPadding: "CBC/NoPadding",
    CBC_128_PKCS7Padding: "CBC/PKCS7Padding",
    ECB_128_NoPadding: "ECB/NoPadding",
    ECB_128_PKCS7Padding: "ECB/PKCS7Padding",
    CBC_128_PKCS5Padding: "CBC/PKCS5Padding",
    ECB_128_PKCS5Padding: "ECB/PKCS5Padding"
  };

  // ../vendor/lx-sdk/kw/decodeLyric.js
  init_buffer_inject();
  var { inflate: inflate2 } = (init_pako(), __toCommonJS(pako_exports));
  var iconv = (init_iconv_lite(), __toCommonJS(iconv_lite_exports));
  var handleInflate = (data) => new Promise((resolve, reject) => {
    resolve(Buffer2.from(inflate2(data)));
  });
  var buf_key = Buffer2.from("yeelion");
  var buf_key_len = buf_key.length;
  var decodeLyric = async (buf, isGetLyricx) => {
    if (buf.toString("utf8", 0, 10) != "tp=content") return "";
    const lrcData = await handleInflate(buf.slice(buf.indexOf("\r\n\r\n") + 4));
    if (!isGetLyricx) return iconv.decode(lrcData, "gb18030");
    const buf_str = Buffer2.from(lrcData.toString(), "base64");
    const buf_str_len = buf_str.length;
    const output = new Uint16Array(buf_str_len);
    let i = 0;
    while (i < buf_str_len) {
      let j = 0;
      while (j < buf_key_len && i < buf_str_len) {
        output[i] = buf_str[i] ^ buf_key[j];
        i++;
        j++;
      }
    }
    return iconv.decode(Buffer2.from(output), "gb18030");
  };
  var decodeLyric_default = async ({ lrcBuffer, isGetLyricx }) => {
    const lrc = await decodeLyric(lrcBuffer, isGetLyricx);
    return Buffer2.from(lrc).toString("base64");
  };

  // ../vendor/lx-sdk/kw/util.js
  var objStr2JSON = (str) => {
    return JSON.parse(str.replace(/('(?=(,\s*')))|('(?=:))|((?<=([:,]\s*))')|((?<={)')|('(?=}))/g, '"'));
  };
  var formatSinger = (rawData) => rawData.replace(/&/g, "\u3001");
  var lrcTools = {
    rxps: {
      wordLine: /^(\[\d{1,2}:.*\d{1,4}\])\s*(\S+(?:\s+\S+)*)?\s*/,
      tagLine: /\[(ver|ti|ar|al|offset|by|kuwo):\s*(\S+(?:\s+\S+)*)\s*\]/,
      wordTimeAll: /<(-?\d+),(-?\d+)(?:,-?\d+)?>/g,
      wordTime: /<(-?\d+),(-?\d+)(?:,-?\d+)?>/
    },
    offset: 1,
    offset2: 1,
    isOK: false,
    lines: [],
    tags: [],
    getWordInfo(str, str2, prevWord) {
      const offset = parseInt(str);
      const offset2 = parseInt(str2);
      let startTime = Math.abs((offset + offset2) / (this.offset * 2));
      let endTime = Math.abs((offset - offset2) / (this.offset2 * 2)) + startTime;
      if (prevWord) {
        if (startTime < prevWord.endTime) {
          prevWord.endTime = startTime;
          if (prevWord.startTime > prevWord.endTime) {
            prevWord.startTime = prevWord.endTime;
          }
          prevWord.newTimeStr = `<${prevWord.startTime},${prevWord.endTime - prevWord.startTime}>`;
        }
      }
      return {
        startTime,
        endTime,
        timeStr: `<${startTime},${endTime - startTime}>`
      };
    },
    parseLine(line) {
      if (line.length < 6) return;
      let result = this.rxps.wordLine.exec(line);
      if (result) {
        const time = result[1];
        let words = result[2];
        if (words == null) {
          words = "";
        }
        const wordTimes = words.match(this.rxps.wordTimeAll);
        if (!wordTimes) return;
        let preTimeInfo;
        for (const timeStr of wordTimes) {
          const result2 = this.rxps.wordTime.exec(timeStr);
          const wordInfo = this.getWordInfo(result2[1], result2[2], preTimeInfo);
          words = words.replace(timeStr, wordInfo.timeStr);
          if (preTimeInfo?.newTimeStr) words = words.replace(preTimeInfo.timeStr, preTimeInfo.newTimeStr);
          preTimeInfo = wordInfo;
        }
        this.lines.push(time + words);
        return;
      }
      result = this.rxps.tagLine.exec(line);
      if (!result) return;
      if (result[1] == "kuwo") {
        let content = result[2];
        if (content != null && content.includes("][")) {
          content = content.substring(0, content.indexOf("]["));
        }
        const valueOf = parseInt(content, 8);
        this.offset = Math.trunc(valueOf / 10);
        this.offset2 = Math.trunc(valueOf % 10);
        if (this.offset == 0 || Number.isNaN(this.offset) || this.offset2 == 0 || Number.isNaN(this.offset2)) {
          this.isOK = false;
        }
      } else {
        this.tags.push(line);
      }
    },
    parse(lrc) {
      const lines = lrc.split(/\r\n|\r|\n/);
      const tools = Object.create(this);
      tools.isOK = true;
      tools.offset = 1;
      tools.offset2 = 1;
      tools.lines = [];
      tools.tags = [];
      for (const line of lines) {
        if (!tools.isOK) throw new Error("failed");
        tools.parseLine(line);
      }
      if (!tools.lines.length) return "";
      let lrcs = tools.lines.join("\n");
      if (tools.tags.length) lrcs = `${tools.tags.join("\n")}
${lrcs}`;
      return lrcs;
    }
  };
  var wbdCrypto = {
    aesMode: "aes-128-ecb",
    // aesKey: Buffer.from([112, 87, 39, 61, 199, 250, 41, 191, 57, 68, 45, 114, 221, 94, 140, 228], 'binary'),
    aesKey: "cFcnPcf6Kb85RC1y3V6M5A==",
    aesIv: "",
    appId: "y67sprxhhpws",
    decodeData(base64Result) {
      const data = decodeURIComponent(base64Result);
      return JSON.parse(aesDecryptSync(data, this.aesKey, this.aesIv, AES_MODE.ECB_128_NoPadding));
    },
    createSign(data, time) {
      const str = `${this.appId}${data}${time}`;
      return toMD5(str).toUpperCase();
    },
    buildParam(jsonData) {
      const data = Buffer2.from(JSON.stringify(jsonData)).toString("base64");
      const time = Date.now();
      const encodeData = aesEncryptSync(data, this.aesKey, this.aesIv, AES_MODE.ECB_128_NoPadding);
      const sign = this.createSign(encodeData, time);
      return `data=${encodeURIComponent(encodeData)}&time=${time}&appId=${this.appId}&sign=${sign}`;
    }
  };

  // ../vendor/lx-sdk/kw/musicSearch.js
  var musicSearch_default = {
    regExps: {
      mInfo: /level:(\w+),bitrate:(\d+),format:(\w+),size:([\w.]+)/
    },
    limit: 30,
    total: 0,
    page: 0,
    allPage: 1,
    // cancelFn: null,
    musicSearch(str, page, limit) {
      const musicSearchRequestObj = httpFetch(`http://search.kuwo.cn/r.s?client=kt&all=${encodeURIComponent(str)}&pn=${page - 1}&rn=${limit}&uid=794762570&ver=kwplayer_ar_9.2.2.1&vipver=1&show_copyright_off=1&newver=1&ft=music&cluster=0&strategy=2012&encoding=utf8&rformat=json&vermerge=1&mobi=1&issubtitle=1`);
      return musicSearchRequestObj.promise;
    },
    // getImg(songId) {
    //   return httpGet(`http://player.kuwo.cn/webmusic/sj/dtflagdate?flag=6&rid=MUSIC_${songId}`)
    // },
    // getLrc(songId) {
    //   return httpGet(`http://mobile.kuwo.cn/mpage/html5/songinfoandlrc?mid=${songId}&flag=0`)
    // },
    handleResult(rawData) {
      const result = [];
      if (!rawData) return result;
      for (let i = 0; i < rawData.length; i++) {
        const info = rawData[i];
        let songId = info.MUSICRID.replace("MUSIC_", "");
        if (!info.N_MINFO) {
          console.log("N_MINFO is undefined");
          return null;
        }
        const types = [];
        const _types = {};
        let infoArr = info.N_MINFO.split(";");
        for (let info2 of infoArr) {
          info2 = info2.match(this.regExps.mInfo);
          if (info2) {
            switch (info2[2]) {
              case "4000":
                types.push({ type: "flac24bit", size: info2[4] });
                _types.flac24bit = {
                  size: info2[4].toLocaleUpperCase()
                };
                break;
              case "2000":
                types.push({ type: "flac", size: info2[4] });
                _types.flac = {
                  size: info2[4].toLocaleUpperCase()
                };
                break;
              case "320":
                types.push({ type: "320k", size: info2[4] });
                _types["320k"] = {
                  size: info2[4].toLocaleUpperCase()
                };
                break;
              case "128":
                types.push({ type: "128k", size: info2[4] });
                _types["128k"] = {
                  size: info2[4].toLocaleUpperCase()
                };
                break;
            }
          }
        }
        types.reverse();
        let interval = parseInt(info.DURATION);
        result.push({
          name: decodeName(info.SONGNAME),
          singer: formatSinger(decodeName(info.ARTIST)),
          source: "kw",
          // img = (info.album.name === '' || info.album.name === '空')
          //   ? `http://player.kuwo.cn/webmusic/sj/dtflagdate?flag=6&rid=MUSIC_160911.jpg`
          //   : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${info.album.mid}.jpg`
          songmid: songId,
          albumId: decodeName(info.ALBUMID || ""),
          interval: Number.isNaN(interval) ? 0 : formatPlayTime(interval),
          albumName: info.ALBUM ? decodeName(info.ALBUM) : "",
          lrc: null,
          img: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        });
      }
      return result;
    },
    search(str, page = 1, limit, retryNum = 0) {
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then(({ body: result }) => {
        if (!result || result.TOTAL !== "0" && result.SHOW === "0") return this.search(str, page, limit, ++retryNum);
        let list = this.handleResult(result.abslist);
        if (list == null) return this.search(str, page, limit, ++retryNum);
        this.total = parseInt(result.TOTAL);
        this.page = page;
        this.allPage = Math.ceil(this.total / limit);
        return Promise.resolve({
          list,
          allPage: this.allPage,
          total: this.total,
          limit,
          source: "kw"
        });
      });
    }
  };

  // ../vendor/lx-sdk/kw/leaderboard.js
  init_buffer_inject();
  var boardList = [{ id: "kw__93", name: "\u98D9\u5347\u699C", bangid: "93" }, { id: "kw__17", name: "\u65B0\u6B4C\u699C", bangid: "17" }, { id: "kw__16", name: "\u70ED\u6B4C\u699C", bangid: "16" }, { id: "kw__158", name: "\u6296\u97F3\u70ED\u6B4C\u699C", bangid: "158" }, { id: "kw__292", name: "\u94C3\u58F0\u699C", bangid: "292" }, { id: "kw__284", name: "\u70ED\u8BC4\u699C", bangid: "284" }, { id: "kw__290", name: "ACG\u65B0\u6B4C\u699C", bangid: "290" }, { id: "kw__286", name: "\u53F0\u6E7EKKBOX\u699C", bangid: "286" }, { id: "kw__279", name: "\u51AC\u65E5\u6696\u5FC3\u699C", bangid: "279" }, { id: "kw__281", name: "\u5DF4\u58EB\u968F\u8EAB\u542C\u699C", bangid: "281" }, { id: "kw__255", name: "KTV\u70B9\u5531\u699C", bangid: "255" }, { id: "kw__280", name: "\u5BB6\u52A1\u8FDB\u884C\u66F2\u699C", bangid: "280" }, { id: "kw__282", name: "\u71AC\u591C\u4FEE\u4ED9\u699C", bangid: "282" }, { id: "kw__283", name: "\u6795\u8FB9\u8F7B\u97F3\u4E50\u699C", bangid: "283" }, { id: "kw__278", name: "\u53E4\u98CE\u97F3\u4E50\u699C", bangid: "278" }, { id: "kw__264", name: "Vlog\u97F3\u4E50\u699C", bangid: "264" }, { id: "kw__242", name: "\u7535\u97F3\u699C", bangid: "242" }, { id: "kw__187", name: "\u6D41\u884C\u8D8B\u52BF\u699C", bangid: "187" }, { id: "kw__204", name: "\u73B0\u573A\u97F3\u4E50\u699C", bangid: "204" }, { id: "kw__186", name: "ACG\u795E\u66F2\u699C", bangid: "186" }, { id: "kw__185", name: "\u6700\u5F3A\u7FFB\u5531\u699C", bangid: "185" }, { id: "kw__26", name: "\u7ECF\u5178\u6000\u65E7\u699C", bangid: "26" }, { id: "kw__104", name: "\u534E\u8BED\u699C", bangid: "104" }, { id: "kw__182", name: "\u7CA4\u8BED\u699C", bangid: "182" }, { id: "kw__22", name: "\u6B27\u7F8E\u699C", bangid: "22" }, { id: "kw__184", name: "\u97E9\u8BED\u699C", bangid: "184" }, { id: "kw__183", name: "\u65E5\u8BED\u699C", bangid: "183" }, { id: "kw__145", name: "\u4F1A\u5458\u7545\u542C\u699C", bangid: "145" }, { id: "kw__153", name: "\u7F51\u7EA2\u65B0\u6B4C\u699C", bangid: "153" }, { id: "kw__64", name: "\u5F71\u89C6\u91D1\u66F2\u699C", bangid: "64" }, { id: "kw__176", name: "DJ\u55E8\u6B4C\u699C", bangid: "176" }, { id: "kw__106", name: "\u771F\u58F0\u97F3", bangid: "106" }, { id: "kw__12", name: "Billboard\u699C", bangid: "12" }, { id: "kw__49", name: "iTunes\u97F3\u4E50\u699C", bangid: "49" }, { id: "kw__180", name: "beatport\u7535\u97F3\u699C", bangid: "180" }, { id: "kw__13", name: "\u82F1\u56FDUK\u699C", bangid: "13" }, { id: "kw__164", name: "\u767E\u5927DJ\u699C", bangid: "164" }, { id: "kw__246", name: "YouTube\u97F3\u4E50\u6392\u884C\u699C", bangid: "246" }, { id: "kw__265", name: "\u97E9\u56FDGenie\u699C", bangid: "265" }, { id: "kw__14", name: "\u97E9\u56FDM-net\u699C", bangid: "14" }, { id: "kw__8", name: "\u9999\u6E2F\u7535\u53F0\u699C", bangid: "8" }, { id: "kw__15", name: "\u65E5\u672C\u516C\u4FE1\u699C", bangid: "15" }, { id: "kw__151", name: "\u817E\u8BAF\u97F3\u4E50\u4EBA\u539F\u521B\u699C", bangid: "151" }];
  var sortQualityArray = (array) => {
    const qualityMap = {
      flac24bit: 4,
      flac: 3,
      "320k": 2,
      "128k": 1
    };
    const rawQualityArray = [];
    const newQualityArray = [];
    array.forEach((item, index) => {
      const type = qualityMap[item.type];
      if (!type) return;
      rawQualityArray.push({ type, index });
    });
    rawQualityArray.sort((a, b) => a.type - b.type);
    rawQualityArray.forEach((item) => {
      newQualityArray.push(array[item.index]);
    });
    return newQualityArray;
  };
  var leaderboard_default = {
    list: [
      {
        id: "kwbiaosb",
        name: "\u98D9\u5347\u699C",
        bangid: 93
      },
      {
        id: "kwregb",
        name: "\u70ED\u6B4C\u699C",
        bangid: 16
      },
      {
        id: "kwhuiyb",
        name: "\u4F1A\u5458\u699C",
        bangid: 145
      },
      {
        id: "kwdouyb",
        name: "\u6296\u97F3\u699C",
        bangid: 158
      },
      {
        id: "kwqsb",
        name: "\u8D8B\u52BF\u699C",
        bangid: 187
      },
      {
        id: "kwhuaijb",
        name: "\u6000\u65E7\u699C",
        bangid: 26
      },
      {
        id: "kwhuayb",
        name: "\u534E\u8BED\u699C",
        bangid: 104
      },
      {
        id: "kwyueyb",
        name: "\u7CA4\u8BED\u699C",
        bangid: 182
      },
      {
        id: "kwoumb",
        name: "\u6B27\u7F8E\u699C",
        bangid: 22
      },
      {
        id: "kwhanyb",
        name: "\u97E9\u8BED\u699C",
        bangid: 184
      },
      {
        id: "kwriyb",
        name: "\u65E5\u8BED\u699C",
        bangid: 183
      }
    ],
    // getUrl: (p, l, id) => `http://kbangserver.kuwo.cn/ksong.s?from=pc&fmt=json&pn=${p - 1}&rn=${l}&type=bang&data=content&id=${id}&show_copyright_off=0&pcmp4=1&isbang=1`,
    regExps: {
      mInfo: /level:(\w+),bitrate:(\d+),format:(\w+),size:([\w.]+)/
    },
    limit: 100,
    _requestBoardsObj: null,
    getBoardsData() {
      if (this._requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this._requestBoardsObj = httpFetch("http://qukudata.kuwo.cn/q.k?op=query&cont=tree&node=2&pn=0&rn=1000&fmt=json&level=2");
      return this._requestBoardsObj.promise;
    },
    getData(url) {
      const requestDataObj = httpFetch(url);
      return requestDataObj.promise;
    },
    filterData(rawList) {
      return rawList.map((item) => {
        let types = [];
        const _types = {};
        const qualitys = /* @__PURE__ */ new Set();
        item.n_minfo.split(";").forEach((i) => {
          const info = i.match(this.regExps.mInfo);
          if (!info) return;
          const quality = info[2];
          const size = info[4].toLocaleUpperCase();
          if (qualitys.has(quality)) return;
          qualitys.add(quality);
          switch (quality) {
            case "4000":
              types.push({ type: "flac24bit", size });
              _types.flac24bit = { size };
              break;
            case "2000":
              types.push({ type: "flac", size });
              _types.flac = { size };
              break;
            case "320":
              types.push({ type: "320k", size });
              _types["320k"] = { size };
              break;
            case "128":
              types.push({ type: "128k", size });
              _types["128k"] = { size };
              break;
          }
        });
        types = sortQualityArray(types);
        return {
          singer: formatSinger(decodeName(item.artist)),
          name: decodeName(item.name),
          albumName: decodeName(item.album),
          albumId: item.albumId,
          songmid: item.id,
          source: "kw",
          interval: formatPlayTime(parseInt(item.duration)),
          img: item.pic,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    filterBoardsData(rawList) {
      let list = [];
      for (const board of rawList) {
        if (board.source != "1") continue;
        list.push({
          id: "kw__" + board.sourceid,
          name: board.name,
          bangid: String(board.sourceid)
        });
      }
      return list;
    },
    async getBoards(retryNum = 0) {
      this.list = boardList;
      return {
        list: boardList,
        source: "kw"
      };
    },
    getList(id, page, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      const requestBody = { uid: "", devId: "", sFrom: "kuwo_sdk", user_type: "AP", carSource: "kwplayercar_ar_6.0.1.0_apk_keluze.apk", id, pn: page - 1, rn: this.limit };
      const requestUrl = `https://wbd.kuwo.cn/api/bd/bang/bang_info?${wbdCrypto.buildParam(requestBody)}`;
      const request = httpFetch(requestUrl, { cache: "default" }).promise;
      return request.then(({ statusCode, body }) => {
        const rawData = wbdCrypto.decodeData(body);
        const data = rawData.data;
        if (statusCode !== 200 || rawData.code != 200 || !data.musiclist) return this.getList(id, page, retryNum);
        const total = parseInt(data.total);
        const list = this.filterData(data.musiclist);
        return {
          total,
          list,
          limit: this.limit,
          page,
          source: "kw"
        };
      });
    }
    // getDetailPageUrl(id) {
    //   return `http://www.kuwo.cn/rankList/${id}`
    // },
  };

  // ../vendor/lx-sdk/kw/lyric.js
  init_buffer_inject();
  var buf_key2 = Buffer2.from("yeelion");
  var buf_key_len2 = buf_key2.length;
  var buildParams = (id, isGetLyricx) => {
    let params = `user=12345,web,web,web&requester=localhost&req=1&rid=MUSIC_${id}`;
    if (isGetLyricx) params += "&lrcx=1";
    const buf_str = Buffer2.from(params);
    const buf_str_len = buf_str.length;
    const output = new Uint16Array(buf_str_len);
    let i = 0;
    while (i < buf_str_len) {
      let j = 0;
      while (j < buf_key_len2 && i < buf_str_len) {
        output[i] = buf_key2[j] ^ buf_str[i];
        i++;
        j++;
      }
    }
    return Buffer2.from(output).toString("base64");
  };
  var timeExp = /^\[([\d:.]*)\]{1}/g;
  var existTimeExp = /\[\d{1,2}:.*\d{1,4}\]/;
  var lyricxTag = /^<-?\d+,-?\d+>/;
  var lyric_default = {
    /* sortLrcArr(arr) {
        const lrcSet = new Set()
        let lrc = []
        let lrcT = []
        let markIndex = []
        for (const item of arr) {
          if (lrcSet.has(item.time)) {
            if (lrc.length < 2) continue
            const index = lrc.findIndex(l => l.time == item.time)
            markIndex.push(index)
            if (index == lrc.length - 1) {
              lrcT.push({ ...lrc[index], time: item.time })
              lrc.push(item)
            } else {
              lrcT.push({ ...lrc[index], time: lrc[index + 1].time })
              if (item.text) {
                //   const lastIndex = lrc.length - 1
                //   markIndex.push(lastIndex)
                //   lrcT.push({ ...lrc[lastIndex], time: lrc[lastIndex - 1].time })
                lrc.push(item)
              }
            }
          } else {
            lrc.push(item)
            lrcSet.add(item.time)
          }
        }
    
        // console.log(markIndex)
        markIndex = Array.from(new Set(markIndex))
        for (let index = markIndex.length - 1; index >= 0; index--) {
          lrc.splice(markIndex[index], 1)
        }
    
        // if (lrcT.length) {
        //   if (lrc.length * 0.4 < lrcT.length) { // 翻译数量需大于歌词数量的0.4倍，否则认为没有翻译
        //     const tItem = lrc.pop()
        //     tItem.time = lrc[lrc.length - 1].time
        //     lrcT.push(tItem)
        //   } else {
        //     lrc = arr
        //     lrcT = []
        //   }
        // }
    
        console.log(lrc, lrcT)
    
        return {
          lrc,
          lrcT,
        }
      }, */
    sortLrcArr(arr) {
      const lrcSet = /* @__PURE__ */ new Set();
      let lrc = [];
      let lrcT = [];
      let isLyricx = false;
      for (const item of arr) {
        if (lrcSet.has(item.time)) {
          if (lrc.length < 2) continue;
          const tItem = lrc.pop();
          tItem.time = lrc[lrc.length - 1].time;
          lrcT.push(tItem);
          lrc.push(item);
        } else {
          lrc.push(item);
          lrcSet.add(item.time);
        }
        if (!isLyricx && lyricxTag.test(item.text)) isLyricx = true;
      }
      if (!isLyricx && lrcT.length > lrc.length * 0.3 && lrc.length - lrcT.length > 6) {
        throw new Error("failed");
      }
      return {
        lrc,
        lrcT
      };
    },
    transformLrc(tags, lrclist) {
      return `${tags.join("\n")}
${lrclist ? lrclist.map((l) => `[${l.time}]${l.text}
`).join("") : "\u6682\u65E0\u6B4C\u8BCD"}`;
    },
    parseLrc(lrc) {
      const lines = lrc.split(/\r\n|\r|\n/);
      let tags = [];
      let lrcArr = [];
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i].trim();
        let result = timeExp.exec(line);
        if (result) {
          const text = line.replace(timeExp, "").trim();
          let time = RegExp.$1;
          if (/\.\d\d$/.test(time)) time += "0";
          lrcArr.push({
            time,
            text
          });
        } else if (lrcTools.rxps.tagLine.test(line)) {
          tags.push(line);
        }
      }
      const lrcInfo = this.sortLrcArr(lrcArr);
      return {
        lyric: decodeName(this.transformLrc(tags, lrcInfo.lrc)),
        tlyric: lrcInfo.lrcT.length ? decodeName(this.transformLrc(tags, lrcInfo.lrcT)) : ""
      };
    },
    // getLyric2(musicInfo, isGetLyricx = true) {
    //   const requestObj = httpFetch(`http://newlyric.kuwo.cn/newlyric.lrc?${buildParams(musicInfo.songmid, isGetLyricx)}`)
    //   requestObj.promise = requestObj.promise.then(({ statusCode, body, raw }) => {
    //     if (statusCode != 200) return Promise.reject(new Error(JSON.stringify(body)))
    //     return decodeLyric({ lrcBase64: raw.toString('base64'), isGetLyricx }).then(base64Data => {
    //       let lrcInfo
    //       console.log(Buffer.from(base64Data, 'base64').toString())
    //       try {
    //         lrcInfo = this.parseLrc(Buffer.from(base64Data, 'base64').toString())
    //       } catch {
    //         return Promise.reject(new Error('Get lyric failed'))
    //       }
    //       if (lrcInfo.tlyric) lrcInfo.tlyric = lrcInfo.tlyric.replace(lrcTools.rxps.wordTimeAll, '')
    //       lrcInfo.lxlyric = lrcTools.parse(lrcInfo.lyric)
    //       // console.log(lrcInfo.lyric)
    //       // console.log(lrcInfo.tlyric)
    //       // console.log(lrcInfo.lxlyric)
    //       // console.log(JSON.stringify(lrcInfo))
    //     })
    //   })
    //   return requestObj
    // },
    getLyric(musicInfo, isGetLyricx = true) {
      const requestObj = httpFetch(`http://newlyric.kuwo.cn/newlyric.lrc?${buildParams(musicInfo.songmid, isGetLyricx)}`, {
        cache: false,
        binary: true
      });
      requestObj.promise = requestObj.promise.then(({ statusCode, body }) => {
        if (statusCode != 200) return Promise.reject(new Error(JSON.stringify(body)));
        return decodeLyric_default({ lrcBuffer: body, isGetLyricx }).then((base64Data) => {
          let lrcInfo;
          try {
            lrcInfo = this.parseLrc(Buffer2.from(base64Data, "base64").toString());
          } catch (err) {
            return Promise.reject(new Error("Get lyric failed"));
          }
          if (lrcInfo.tlyric) lrcInfo.tlyric = lrcInfo.tlyric.replace(lrcTools.rxps.wordTimeAll, "");
          try {
            lrcInfo.lxlyric = lrcTools.parse(lrcInfo.lyric);
          } catch {
            lrcInfo.lxlyric = "";
          }
          lrcInfo.lyric = lrcInfo.lyric.replace(lrcTools.rxps.wordTimeAll, "");
          if (!existTimeExp.test(lrcInfo.lyric)) return Promise.reject(new Error("Get lyric failed"));
          return lrcInfo;
        });
      });
      return requestObj;
    }
  };

  // ../vendor/lx-sdk/kw/pic.js
  init_buffer_inject();
  var pic_default = {
    getPic({ songmid }) {
      const requestObj = httpFetch(`http://artistpicserver.kuwo.cn/pic.web?corp=kuwo&type=rid_pic&pictype=500&size=500&rid=${songmid}`);
      requestObj.promise = requestObj.promise.then(({ body }) => /^http/.test(body) ? body : null);
      return requestObj.promise;
    }
  };

  // lyn-internal:lx-internal-api-source
  init_buffer_inject();
  var apis = (_src) => ({
    getMusicUrl: (_songInfo, _type) => ({
      promise: Promise.reject(new Error("apis.getMusicUrl: not implemented in M0 shim"))
    }),
    getLyric: () => ({ promise: Promise.reject(new Error("apis.getLyric: not implemented")) }),
    getPic: () => ({ promise: Promise.reject(new Error("apis.getPic: not implemented")) })
  });

  // ../vendor/lx-sdk/kw/songList.js
  init_buffer_inject();

  // ../vendor/lx-sdk/kw/album.js
  init_buffer_inject();
  var album_default = {
    limit_list: 36,
    limit_song: 1e3,
    filterListDetail(rawList, albumName, albumId) {
      return rawList.map((item, inedx) => {
        let formats = item.formats.split("|");
        let types = [];
        let _types = {};
        if (formats.includes("MP3128")) {
          types.push({ type: "128k", size: null });
          _types["128k"] = {
            size: null
          };
        }
        if (formats.includes("MP3H")) {
          types.push({ type: "320k", size: null });
          _types["320k"] = {
            size: null
          };
        }
        if (formats.includes("ALFLAC")) {
          types.push({ type: "flac", size: null });
          _types.flac = {
            size: null
          };
        }
        if (formats.includes("HIRFLAC")) {
          types.push({ type: "flac24bit", size: null });
          _types.flac24bit = {
            size: null
          };
        }
        return {
          singer: formatSinger(decodeName(item.artist)),
          name: decodeName(item.name),
          albumName,
          albumId,
          songmid: item.id,
          source: "kw",
          interval: null,
          img: item.pic,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    /**
     * 格式化播放数量
     * @param {*} num
     */
    formatPlayCount(num) {
      if (num > 1e8) return parseInt(num / 1e7) / 10 + "\u4EBF";
      if (num > 1e4) return parseInt(num / 1e3) / 10 + "\u4E07";
      return num;
    },
    getAlbumListDetail(id, page, retryNum = 0) {
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj_listDetail = httpFetch(`http://search.kuwo.cn/r.s?pn=${page - 1}&rn=${this.limit_song}&stype=albuminfo&albumid=${id}&show_copyright_off=0&encoding=utf&vipver=MUSIC_9.1.0`);
      return requestObj_listDetail.promise.then(({ statusCode, body }) => {
        if (statusCode !== 200) return this.getAlbumListDetail(id, page, ++retryNum);
        body = objStr2JSON(body);
        if (!body.musiclist) return this.getAlbumListDetail(id, page, ++retryNum);
        body.name = decodeName(body.name);
        return {
          list: this.filterListDetail(body.musiclist, body.name, body.albumid),
          page,
          limit: this.limit_song,
          total: parseInt(body.songnum),
          source: "kw",
          info: {
            name: body.name,
            img: body.img || body.hts_img,
            desc: decodeName(body.info),
            author: decodeName(body.artist)
            // play_count: this.formatPlayCount(body.playnum),
          }
        };
      });
    }
    // getAlbumListDetail(id, page, retryNum = 0) {
    //   if (retryNum > 2) return Promise.reject(new Error('try max num'))
    //   return tokenRequest(`http://www.kuwo.cn/api/www/album/albumInfo?albumId=${id}&pn=${page}&rn=${this.limit_song}&httpsStatus=1`).then((resp) => {
    //     return resp.promise.then(({ statusCode, body }) => {
    //       console.log(body)
    //       return Promise.reject(new Error('failed'))
    //       // if (statusCode !== 200) return this.getAlbumListDetail(id, page, ++retryNum)
    //       // const data = body.data
    //       // console.log(data)
    //       // if (!data.musicList) return this.getAlbumListDetail(id, page, ++retryNum)
    //       // return {
    //       //   list: this.filterListDetail(data.musiclist),
    //       //   page,
    //       //   limit: this.limit_song,
    //       //   total: data.total,
    //       //   source: 'kw',
    //       //   info: {
    //       //     name: data.album,
    //       //     img: data.pic,
    //       //     desc: data.albuminfo,
    //       //     author: data.artist,
    //       //     play_count: this.formatPlayCount(data.playCnt),
    //       //   },
    //       // }
    //     })
    //   })
    // },
  };

  // ../vendor/lx-sdk/kw/songList.js
  var songList_default = {
    _requestObj_tags: null,
    _requestObj_hotTags: null,
    _requestObj_list: null,
    limit_list: 36,
    limit_song: 1e3,
    successCode: 200,
    sortList: [
      {
        name: "\u6700\u65B0",
        tid: "new",
        id: "new"
      },
      {
        name: "\u6700\u70ED",
        tid: "hot",
        id: "hot"
      }
    ],
    regExps: {
      mInfo: /level:(\w+),bitrate:(\d+),format:(\w+),size:([\w.]+)/,
      // http://www.kuwo.cn/playlist_detail/2886046289
      // https://m.kuwo.cn/h5app/playlist/2736267853?t=qqfriend
      listDetailLink: /^.+\/playlist(?:_detail)?\/(\d+)(?:\?.*|&.*$|#.*$|$)/
    },
    tagsUrl: "http://wapi.kuwo.cn/api/pc/classify/playlist/getTagList?cmd=rcm_keyword_playlist&user=0&prod=kwplayer_pc_9.0.5.0&vipver=9.0.5.0&source=kwplayer_pc_9.0.5.0&loginUid=0&loginSid=0&appUid=76039576",
    hotTagUrl: "http://wapi.kuwo.cn/api/pc/classify/playlist/getRcmTagList?loginUid=0&loginSid=0&appUid=76039576",
    getListUrl({ sortId, id, type, page }) {
      if (!id) return `http://wapi.kuwo.cn/api/pc/classify/playlist/getRcmPlayList?loginUid=0&loginSid=0&appUid=76039576&&pn=${page}&rn=${this.limit_list}&order=${sortId}`;
      switch (type) {
        case "10000":
          return `http://wapi.kuwo.cn/api/pc/classify/playlist/getTagPlayList?loginUid=0&loginSid=0&appUid=76039576&pn=${page}&id=${id}&rn=${this.limit_list}`;
        case "43":
          return `http://mobileinterfaces.kuwo.cn/er.s?type=get_pc_qz_data&f=web&id=${id}&prod=pc`;
      }
    },
    getListDetailUrl(id, page) {
      return `http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=${id}&pn=${page - 1}&rn=${this.limit_song}&encode=utf8&keyset=pl2012&identity=kuwo&pcmp4=1&vipver=MUSIC_9.0.5.0_W1&newver=1`;
    },
    // http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=2849349915&pn=0&rn=100&encode=utf8&keyset=pl2012&identity=kuwo&pcmp4=1&vipver=MUSIC_9.0.5.0_W1&newver=1
    // 获取标签
    getTag(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch(this.tagsUrl);
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterTagInfo(body.data);
      });
    },
    // 获取标签
    getHotTag(tryNum = 0) {
      if (this._requestObj_hotTags) this._requestObj_hotTags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_hotTags = httpFetch(this.hotTagUrl);
      return this._requestObj_hotTags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getHotTag(++tryNum);
        return this.filterInfoHotTag(body.data[0].data);
      });
    },
    filterInfoHotTag(rawList) {
      return rawList.map((item) => ({
        id: `${item.id}-${item.digest}`,
        name: item.name,
        source: "kw"
      }));
    },
    filterTagInfo(rawList) {
      return rawList.map((type) => ({
        name: type.name,
        list: type.data.map((item) => ({
          parent_id: type.id,
          parent_name: type.name,
          id: `${item.id}-${item.digest}`,
          name: item.name,
          source: "kw"
        }))
      }));
    },
    // 获取列表数据
    getList(sortId, tagId, page, tryNum = 0) {
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      let id;
      let type;
      if (tagId) {
        let arr = tagId.split("-");
        id = arr[0];
        type = arr[1];
      } else {
        id = null;
      }
      this._requestObj_list = httpFetch(this.getListUrl({ sortId, id, type, page }));
      return this._requestObj_list.promise.then(({ body }) => {
        if (!id || type == "10000") {
          if (body.code !== this.successCode) return this.getList(sortId, tagId, page, ++tryNum);
          return {
            list: this.filterList(body.data.data),
            total: body.data.total,
            page: body.data.pn,
            limit: body.data.rn,
            source: "kw"
          };
        } else if (!body.length) {
          return this.getList(sortId, tagId, page, ++tryNum);
        }
        return {
          list: this.filterList2(body),
          total: 1e3,
          page,
          limit: 1e3,
          source: "kw"
        };
      });
    },
    /**
     * 格式化播放数量
     * @param {*} num
     */
    formatPlayCount(num) {
      if (num > 1e8) return parseInt(num / 1e7) / 10 + "\u4EBF";
      if (num > 1e4) return parseInt(num / 1e3) / 10 + "\u4E07";
      return num;
    },
    filterList(rawData) {
      return rawData.map((item) => ({
        play_count: this.formatPlayCount(item.listencnt),
        id: `digest-${item.digest}__${item.id}`,
        author: item.uname,
        name: item.name,
        // time: item.publish_time,
        total: item.total,
        img: item.img,
        grade: item.favorcnt / 10,
        desc: item.desc,
        source: "kw"
      }));
    },
    filterList2(rawData) {
      const list = [];
      rawData.forEach((item) => {
        if (!item.label) return;
        list.push(...item.list.map((item2) => ({
          play_count: item2.play_count && this.formatPlayCount(item2.listencnt),
          id: `digest-${item2.digest}__${item2.id}`,
          author: item2.uname,
          name: item2.name,
          total: item2.total,
          // time: item.publish_time,
          img: item2.img,
          grade: item2.favorcnt && item2.favorcnt / 10,
          desc: item2.desc,
          source: "kw"
        })));
      });
      return list;
    },
    getListDetailDigest8(id, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj = httpFetch(this.getListDetailUrl(id, page));
      return requestObj.promise.then(({ body }) => {
        if (body.result !== "ok") return this.getListDetail(id, page, ++tryNum);
        return {
          list: this.filterListDetail(body.musiclist),
          page,
          limit: body.rn,
          total: body.total,
          source: "kw",
          info: {
            name: body.title,
            img: body.pic,
            desc: body.info,
            author: body.uname,
            play_count: this.formatPlayCount(body.playnum)
          }
        };
      });
    },
    getListDetailDigest5Info(id, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj = httpFetch(`http://qukudata.kuwo.cn/q.k?op=query&cont=ninfo&node=${id}&pn=0&rn=1&fmt=json&src=mbox&level=2`);
      return requestObj.promise.then(({ statusCode, body }) => {
        if (statusCode != 200 || !body.child) return this.getListDetail(id, ++tryNum);
        return body.child.length ? body.child[0].sourceid : null;
      });
    },
    getListDetailDigest5Music(id, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj = httpFetch(`http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=${id}&pn=${page - 1}}&rn=${this.limit_song}&encode=utf-8&keyset=pl2012&identity=kuwo&pcmp4=1`);
      return requestObj.promise.then(({ body }) => {
        if (body.result !== "ok") return this.getListDetail(id, page, ++tryNum);
        return {
          list: this.filterListDetail(body.musiclist),
          page,
          limit: body.rn,
          total: body.total,
          source: "kw",
          info: {
            name: body.title,
            img: body.pic,
            desc: body.info,
            author: body.uname,
            play_count: this.formatPlayCount(body.playnum)
          }
        };
      });
    },
    async getListDetailDigest5(id, page, retryNum) {
      const detailId = await this.getListDetailDigest5Info(id, retryNum);
      return this.getListDetailDigest5Music(detailId, page, retryNum);
    },
    filterBDListDetail(rawList) {
      return rawList.map((item) => {
        let types = [];
        let _types = {};
        for (let info of item.audios) {
          info.size = info.size?.toLocaleUpperCase();
          switch (info.bitrate) {
            case "4000":
              types.push({ type: "flac24bit", size: info.size });
              _types.flac24bit = {
                size: info.size
              };
              break;
            case "2000":
              types.push({ type: "flac", size: info.size });
              _types.flac = {
                size: info.size
              };
              break;
            case "320":
              types.push({ type: "320k", size: info.size });
              _types["320k"] = {
                size: info.size
              };
              break;
            case "128":
              types.push({ type: "128k", size: info.size });
              _types["128k"] = {
                size: info.size
              };
              break;
          }
        }
        types.reverse();
        return {
          singer: item.artists.map((s) => s.name).join("\u3001"),
          name: item.name,
          albumName: item.album,
          albumId: item.albumId,
          songmid: item.id,
          source: "kw",
          interval: formatPlayTime(item.duration),
          img: item.albumPic,
          releaseDate: item.releaseDate,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    getReqId() {
      function t() {
        return (65536 * (1 + Math.random()) | 0).toString(16).substring(1);
      }
      return t() + t() + t() + t() + t() + t() + t() + t();
    },
    async getListDetailMusicListByBDListInfo(id, source) {
      const { body: infoData } = await httpFetch(`https://bd-api.kuwo.cn/api/service/playlist/info/${id}?reqId=${this.getReqId()}&source=${source}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36",
          plat: "h5"
        }
      }).promise.catch(() => ({ code: 0 }));
      if (infoData.code != 200) return null;
      return {
        name: infoData.data.name,
        img: infoData.data.pic,
        desc: infoData.data.description,
        author: infoData.data.creatorName,
        play_count: infoData.data.playNum
      };
    },
    async getListDetailMusicListByBDUserPub(id) {
      const { body: infoData } = await httpFetch(`https://bd-api.kuwo.cn/api/ucenter/users/pub/${id}?reqId=${this.getReqId()}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36",
          plat: "h5"
        }
      }).promise.catch(() => ({ code: 0 }));
      if (infoData.code != 200) return null;
      return {
        name: infoData.data.userInfo.nickname + "\u559C\u6B22\u7684\u97F3\u4E50",
        img: infoData.data.userInfo.headImg,
        desc: "",
        author: infoData.data.userInfo.nickname,
        play_count: ""
      };
    },
    async getListDetailMusicListByBDList(id, source, page, tryNum = 0) {
      const { body: listData } = await httpFetch(`https://bd-api.kuwo.cn/api/service/playlist/${id}/musicList?reqId=${this.getReqId()}&source=${source}&pn=${page}&rn=${this.limit_song}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36",
          plat: "h5"
        }
      }).promise.catch(() => {
        if (tryNum > 2) return Promise.reject(new Error("try max num"));
        return this.getListDetailMusicListByBDList(id, source, page, ++tryNum);
      });
      if (listData.code !== 200) return Promise.reject(new Error("failed"));
      return {
        list: this.filterBDListDetail(listData.data.list),
        page,
        limit: listData.data.pageSize,
        total: listData.data.total,
        source: "kw"
      };
    },
    async getListDetailMusicListByBD(id, page) {
      const uid = /uid=(\d+)/.exec(id)?.[1];
      const listId = /playlistId=(\d+)/.exec(id)?.[1];
      const source = /source=(\d+)/.exec(id)?.[1];
      if (!listId) return Promise.reject(new Error("failed"));
      const task = [this.getListDetailMusicListByBDList(listId, source, page)];
      switch (source) {
        case "4":
          task.push(this.getListDetailMusicListByBDListInfo(listId, source));
          break;
        case "5":
          task.push(this.getListDetailMusicListByBDUserPub(uid ?? listId));
          break;
      }
      const [listData, info] = await Promise.all(task);
      listData.info = info ?? {
        name: "",
        img: "",
        desc: "",
        author: "",
        play_count: ""
      };
      return listData;
    },
    // 获取歌曲列表内的音乐
    getListDetail(id, page, retryNum = 0) {
      if (/\/bodian\//.test(id)) return this.getListDetailMusicListByBD(id, page);
      if (/[?&:/]/.test(id)) id = id.replace(this.regExps.listDetailLink, "$1");
      else if (/^digest-/.test(id)) {
        let [digest, _id] = id.split("__");
        digest = digest.replace("digest-", "");
        id = _id;
        switch (digest) {
          case "8":
            break;
          case "13":
            return album_default.getAlbumListDetail(id, page, retryNum);
          case "5":
          default:
            return this.getListDetailDigest5(id, page, retryNum);
        }
      }
      return this.getListDetailDigest8(id, page, retryNum);
    },
    filterListDetail(rawData) {
      return rawData.map((item) => {
        let infoArr = item.N_MINFO.split(";");
        let types = [];
        let _types = {};
        for (let info of infoArr) {
          info = info.match(this.regExps.mInfo);
          if (info) {
            switch (info[2]) {
              case "4000":
                types.push({ type: "flac24bit", size: info[4] });
                _types.flac24bit = {
                  size: info[4].toLocaleUpperCase()
                };
                break;
              case "2000":
                types.push({ type: "flac", size: info[4] });
                _types.flac = {
                  size: info[4].toLocaleUpperCase()
                };
                break;
              case "320":
                types.push({ type: "320k", size: info[4] });
                _types["320k"] = {
                  size: info[4].toLocaleUpperCase()
                };
                break;
              case "128":
                types.push({ type: "128k", size: info[4] });
                _types["128k"] = {
                  size: info[4].toLocaleUpperCase()
                };
                break;
            }
          }
        }
        types.reverse();
        return {
          singer: formatSinger(decodeName(item.artist)),
          name: decodeName(item.name),
          albumName: decodeName(item.album),
          albumId: item.albumid,
          songmid: item.id,
          source: "kw",
          interval: formatPlayTime(parseInt(item.duration)),
          img: null,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    getTags() {
      return Promise.all([this.getTag(), this.getHotTag()]).then(([tags, hotTag]) => ({ tags, hotTag, source: "kw" }));
    },
    getDetailPageUrl(id) {
      if (/[?&:/]/.test(id)) id = id.replace(this.regExps.listDetailLink, "$1");
      else if (/^digest-/.test(id)) {
        let result = id.split("__");
        id = result[1];
      }
      return `http://www.kuwo.cn/playlist_detail/${id}`;
    },
    search(text, page, limit = 20) {
      return httpFetch(`http://search.kuwo.cn/r.s?all=${encodeURIComponent(text)}&pn=${page - 1}&rn=${limit}&rformat=json&encoding=utf8&ver=mbox&vipver=MUSIC_8.7.7.0_BCS37&plat=pc&devid=28156413&ft=playlist&pay=0&needliveshow=0`).promise.then(({ body }) => {
        body = objStr2JSON(body);
        return {
          list: body.abslist.map((item) => {
            return {
              play_count: this.formatPlayCount(item.playcnt),
              id: item.playlistid,
              author: decodeName(item.nickname),
              name: decodeName(item.name),
              total: item.songnum,
              // time: item.publish_time,
              img: item.pic,
              desc: decodeName(item.intro),
              source: "kw"
            };
          }),
          limit,
          total: parseInt(body.TOTAL),
          source: "kw"
        };
      });
    }
  };

  // ../vendor/lx-sdk/kw/hotSearch.js
  init_buffer_inject();
  var hotSearch_default = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = httpFetch("http://hotword.kuwo.cn/hotword.s?prod=kwplayer_ar_9.3.0.1&corp=kuwo&newver=2&vipver=9.3.0.1&source=kwplayer_ar_9.3.0.1_40.apk&p2p=1&notrace=0&uid=0&plat=kwplayer_ar&rformat=json&encoding=utf8&tabid=1", {
        headers: {
          "User-Agent": "Dalvik/2.1.0 (Linux; U; Android 9;)"
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.status !== "ok") throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "kw", list: this.filterList(body.tagvalue) };
    },
    filterList(rawList) {
      return rawList.map((item) => item.key);
    }
  };

  // ../vendor/lx-sdk/kw/comment.js
  init_buffer_inject();
  var comment_default = {
    _requestObj: null,
    _requestObj2: null,
    async getComment({ songmid }, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      const _requestObj = httpFetch(`http://ncomment.kuwo.cn/com.s?f=web&type=get_comment&aapiver=1&prod=kwplayer_ar_10.5.2.0&digest=15&sid=${songmid}&start=${limit * (page - 1)}&msgflag=1&count=${limit}&newver=3&uid=0`, {
        headers: {
          "User-Agent": "Dalvik/2.1.0 (Linux; U; Android 9;)"
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code != "200") throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const total = body.comments_counts;
      return {
        source: "kw",
        comments: this.filterComment(body.comments),
        total,
        page,
        limit,
        maxPage: Math.ceil(total / limit) || 1
      };
    },
    async getHotComment({ songmid }, page = 1, limit = 100) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      const _requestObj2 = httpFetch(`http://ncomment.kuwo.cn/com.s?f=web&type=get_rec_comment&aapiver=1&prod=kwplayer_ar_10.5.2.0&digest=15&sid=${songmid}&start=${limit * (page - 1)}&msgflag=1&count=${limit}&newver=3&uid=0`, {
        headers: {
          "User-Agent": "Dalvik/2.1.0 (Linux; U; Android 9;)"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code != "200") throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const total = body.hot_comments_counts;
      return {
        source: "kw",
        comments: this.filterComment(body.hot_comments),
        total,
        page,
        limit,
        maxPage: Math.ceil(total / limit) || 1
      };
    },
    filterComment(rawList) {
      if (!rawList) return [];
      return rawList.map((item) => {
        return {
          id: item.id,
          text: item.msg,
          time: item.time,
          timeStr: dateFormat2(Number(item.time) * 1e3),
          userName: item.u_name,
          avatar: item.u_pic,
          userId: item.u_id,
          likedCount: item.like_num,
          images: item.mpic ? [decodeURIComponent(item.mpic)] : [],
          reply: item.child_comments ? item.child_comments.map((i) => {
            return {
              id: i.id,
              text: i.msg,
              time: i.time,
              timeStr: dateFormat2(Number(i.time) * 1e3),
              userName: i.u_name,
              avatar: i.u_pic,
              userId: i.u_id,
              likedCount: i.like_num,
              images: i.mpic ? [i.mpic] : []
            };
          }) : []
        };
      });
    }
  };

  // ../vendor/lx-sdk/kw/index.js
  var kw = {
    _musicInfoRequestObj: null,
    _musicInfoPromiseCancelFn: null,
    _musicPicRequestObj: null,
    _musicPicPromiseCancelFn: null,
    // context: null,
    // init(context) {
    //   if (this.isInited) return
    //   this.isInited = true
    //   this.context = context
    //   // this.musicSearch.search('我又想你了').then(res => {
    //   //   console.log(res)
    //   // })
    //   // this.getMusicUrl('62355680', '320k').then(url => {
    //   //   console.log(url)
    //   // })
    // },
    tipSearch: tipSearch_default,
    musicSearch: musicSearch_default,
    leaderboard: leaderboard_default,
    songList: songList_default,
    hotSearch: hotSearch_default,
    comment: comment_default,
    getLyric(songInfo, isGetLyricx) {
      return lyric_default.getLyric(songInfo, isGetLyricx);
    },
    handleMusicInfo(songInfo) {
      return this.getMusicInfo(songInfo).then((info) => {
        songInfo.name = info.name;
        songInfo.singer = formatSinger(info.artist);
        songInfo.img = info.pic;
        songInfo.albumName = info.album;
        return songInfo;
      });
    },
    getMusicUrl(songInfo, type) {
      return apis("kw").getMusicUrl(songInfo, type);
    },
    getMusicInfo(songInfo) {
      if (this._musicInfoRequestObj) this._musicInfoRequestObj.cancelHttp();
      this._musicInfoRequestObj = httpFetch(`http://www.kuwo.cn/api/www/music/musicInfo?mid=${songInfo.songmid}`);
      return this._musicInfoRequestObj.promise.then(({ body }) => {
        return body.code === 200 ? body.data : Promise.reject(new Error(body.msg));
      });
    },
    getMusicUrls(musicInfo, cb) {
      let tasks = [];
      let songId = musicInfo.songmid;
      musicInfo.types.forEach((type) => {
        tasks.push(kw.getMusicUrl(songId, type.type).promise);
      });
      Promise.all(tasks).then((urlInfo) => {
        let typeUrl = {};
        urlInfo.forEach((info) => {
          typeUrl[info.type] = info.url;
        });
        cb(typeUrl);
      });
    },
    getPic(songInfo) {
      return pic_default.getPic(songInfo);
    },
    getMusicDetailPageUrl(songInfo) {
      return `http://www.kuwo.cn/play_detail/${songInfo.songmid}`;
    }
    // init() {
    //   return getToken()
    // },
  };
  var index_default = kw;
  return __toCommonJS(index_exports);
})();
globalThis.__lyn_source_kw = (typeof __lynSource !== 'undefined' && __lynSource.default) ? __lynSource.default : __lynSource;
