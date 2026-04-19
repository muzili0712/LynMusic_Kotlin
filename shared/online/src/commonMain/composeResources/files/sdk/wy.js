var __lynSource = (() => {
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
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

  // ../vendor/lx-sdk/wy/index.js
  var index_exports = {};
  __export(index_exports, {
    default: () => index_default
  });

  // lyn-shim:@craftzdog/react-native-buffer
  var LynBuffer = class _LynBuffer extends Uint8Array {
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
  var Buffer2 = LynBuffer;

  // lyn-shim:react-native-quick-base64
  var enc = (s, utf8 = true) => {
    if (typeof s === "string") s = globalThis.lyn.bufferFrom(s, utf8 ? "utf8" : "binary");
    return globalThis.lyn.base64Encode(s);
  };

  // lyn-shim:crypto-native
  var aesEncryptSync = (data, mode, key, iv2) => globalThis.lyn.aesEncrypt(data, key, iv2, mode);
  var rsaEncrypt = (data, publicKey2) => globalThis.lyn.rsaEncrypt(data, publicKey2);
  var rsaEncryptSync = rsaEncrypt;
  var AES_MODE = {
    CBC_128_NoPadding: "CBC/NoPadding",
    CBC_128_PKCS7Padding: "CBC/PKCS7Padding",
    ECB_128_NoPadding: "ECB/NoPadding",
    ECB_128_PKCS7Padding: "ECB/PKCS7Padding",
    CBC_128_PKCS5Padding: "CBC/PKCS5Padding",
    ECB_128_PKCS5Padding: "ECB/PKCS5Padding"
  };
  var RSA_PADDING = {
    PKCS1Padding: "PKCS1Padding",
    NoPadding: "NoPadding",
    OAEPPadding: "OAEPPadding"
  };

  // lyn-shim:react-native-quick-md5
  var hexHost = (input) => {
    if (typeof input === "string") input = globalThis.lyn.bufferFrom(input, "utf8");
    return globalThis.lyn.md5(input);
  };
  var stringMd5 = hexHost;

  // lyn-internal:lx-internal-index
  var decodeName = (str) => {
    if (str == null) return "";
    return String(str).replace(/&amp;/g, "&").replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&quot;/g, '"').replace(/&#039;/g, "'").replace(/&apos;/g, "'").replace(/&nbsp;/g, " ");
  };
  var sizeFormate = (size) => {
    if (isNaN(size)) return "-";
    const companys = ["B", "KB", "MB", "GB", "TB"];
    let i = 0;
    while (size >= 1024 && i < companys.length - 1) {
      size /= 1024;
      i++;
    }
    return size.toFixed(2) + companys[i];
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
  var formatPlayCount = (num) => {
    if (num > 1e8) return (num / 1e4 / 1e4).toFixed(2) + "\u4EBF";
    if (num > 1e4) return (num / 1e4).toFixed(2) + "\u4E07";
    return "" + num;
  };

  // ../vendor/lx-sdk/shared/utils.js
  var toMD5 = (str) => stringMd5(str);
  var formatSingerName = (singers, nameKey = "name", join = "\u3001") => {
    if (Array.isArray(singers)) {
      const singer = [];
      singers.forEach((item) => {
        let name = item[nameKey];
        if (!name) return;
        singer.push(name);
      });
      return decodeName(singer.join(join));
    }
    return decodeName(String(singers ?? ""));
  };

  // ../vendor/lx-sdk/wy/utils/crypto.js
  var iv = enc("0102030405060708");
  var presetKey = enc("0CoJUm6Qyw8W8jud");
  var linuxapiKey = enc("rFgB&h#%2?^eDg:Q");
  var publicKey = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDgtQn2JZ34ZC28NWYpAUd98iZ37BUrX/aKzmFbt7clFSs6sXqHauqKWqdtLkF2KexO40H1YTX8z2lSgBBOAxLsvaklV8k4cBFK9snQXE9/DDaFt6Rr7iVZMldczhC0JNgTz+SHXT6CBHuX3e9SdB1Ua44oncaTWz7OBGLbCiK45wIDAQAB\n-----END PUBLIC KEY-----";
  var eapiKey = enc("e82ckenh8dichen8");
  var aesEncrypt = (b64, mode, key, iv2) => {
    return aesEncryptSync(b64, key, iv2, mode);
  };
  var rsaEncrypt2 = (buffer, key) => {
    buffer = Buffer2.concat([Buffer2.alloc(128 - buffer.length), buffer]);
    return Buffer2.from(rsaEncryptSync(buffer.toString("base64"), key, RSA_PADDING.NoPadding), "base64");
  };
  var weapi = (object) => {
    const text = JSON.stringify(object);
    const secretKey = String(Math.random()).substring(2, 18);
    return {
      params: aesEncrypt(enc(aesEncrypt(Buffer2.from(text).toString("base64"), AES_MODE.CBC_128_PKCS7Padding, presetKey, iv)), AES_MODE.CBC_128_PKCS7Padding, enc(secretKey), iv),
      encSecKey: rsaEncrypt2(Buffer2.from(secretKey).reverse(), publicKey).toString("hex")
    };
  };
  var linuxapi = (object) => {
    const text = JSON.stringify(object);
    return {
      eparams: Buffer2.from(aesEncrypt(Buffer2.from(text).toString("base64"), AES_MODE.ECB_128_NoPadding, linuxapiKey, ""), "base64").toString("hex").toUpperCase()
    };
  };
  var eapi = (url, object) => {
    const text = typeof object === "object" ? JSON.stringify(object) : object;
    const message = `nobody${url}use${text}md5forencrypt`;
    const digest = toMD5(message);
    const data = `${url}-36cd479b6b5-${text}-36cd479b6b5-${digest}`;
    return {
      params: Buffer2.from(aesEncrypt(Buffer2.from(data).toString("base64"), AES_MODE.ECB_128_NoPadding, eapiKey, ""), "base64").toString("hex").toUpperCase()
    };
  };

  // lyn-shim:request
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

  // ../vendor/lx-sdk/wy/musicDetail.js
  var musicDetail_default = {
    getSinger(singers) {
      let arr = [];
      singers?.forEach((singer) => {
        arr.push(singer.name);
      });
      return arr.join("\u3001");
    },
    filterList({ songs, privileges }) {
      const list = [];
      songs.forEach((item, index) => {
        const types = [];
        const _types = {};
        let size;
        let privilege = privileges[index];
        if (privilege.id !== item.id) privilege = privileges.find((p) => p.id === item.id);
        if (!privilege) return;
        if (privilege.maxBrLevel == "hires") {
          size = item.hr ? sizeFormate(item.hr.size) : null;
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        switch (privilege.maxbr) {
          case 999e3:
            size = item.sq ? sizeFormate(item.sq.size) : null;
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
          case 32e4:
            size = item.h ? sizeFormate(item.h.size) : null;
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
          case 192e3:
          case 128e3:
            size = item.l ? sizeFormate(item.l.size) : null;
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
        }
        types.reverse();
        if (item.pc) {
          list.push({
            singer: item.pc.ar ?? "",
            name: item.pc.sn ?? "",
            albumName: item.pc.alb ?? "",
            albumId: item.al?.id,
            source: "wy",
            interval: formatPlayTime(item.dt / 1e3),
            songmid: item.id,
            img: item.al?.picUrl ?? "",
            lrc: null,
            otherSource: null,
            types,
            _types,
            typeUrl: {}
          });
        } else {
          list.push({
            singer: this.getSinger(item.ar),
            name: item.name ?? "",
            albumName: item.al?.name,
            albumId: item.al?.id,
            source: "wy",
            interval: formatPlayTime(item.dt / 1e3),
            songmid: item.id,
            img: item.al?.picUrl,
            lrc: null,
            otherSource: null,
            types,
            _types,
            typeUrl: {}
          });
        }
      });
      return list;
    },
    async getList(ids = [], retryNum = 0) {
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj = httpFetch("https://music.163.com/weapi/v3/song/detail", {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
          origin: "https://music.163.com"
        },
        form: weapi({
          c: "[" + ids.map((id) => '{"id":' + id + "}").join(",") + "]",
          ids: "[" + ids.join(",") + "]"
        })
      });
      const { body, statusCode } = await requestObj.promise;
      if (statusCode != 200 || body.code !== 200) throw new Error("\u83B7\u53D6\u6B4C\u66F2\u8BE6\u60C5\u5931\u8D25");
      return { source: "wy", list: this.filterList(body) };
    }
  };

  // ../vendor/lx-sdk/wy/leaderboard.js
  var topList = [
    { id: "wy__19723756", name: "\u98D9\u5347\u699C", bangid: "19723756" },
    { id: "wy__3779629", name: "\u65B0\u6B4C\u699C", bangid: "3779629" },
    { id: "wy__2884035", name: "\u539F\u521B\u699C", bangid: "2884035" },
    { id: "wy__3778678", name: "\u70ED\u6B4C\u699C", bangid: "3778678" },
    { id: "wy__991319590", name: "\u8BF4\u5531\u699C", bangid: "991319590" },
    { id: "wy__71384707", name: "\u53E4\u5178\u699C", bangid: "71384707" },
    { id: "wy__1978921795", name: "\u7535\u97F3\u699C", bangid: "1978921795" },
    { id: "wy__5453912201", name: "\u9ED1\u80F6VIP\u7231\u542C\u699C", bangid: "5453912201" },
    { id: "wy__71385702", name: "ACG\u699C", bangid: "71385702" },
    { id: "wy__745956260", name: "\u97E9\u8BED\u699C", bangid: "745956260" },
    { id: "wy__10520166", name: "\u56FD\u7535\u699C", bangid: "10520166" },
    { id: "wy__180106", name: "UK\u6392\u884C\u699C\u5468\u699C", bangid: "180106" },
    { id: "wy__60198", name: "\u7F8E\u56FDBillboard\u699C", bangid: "60198" },
    { id: "wy__3812895", name: "Beatport\u5168\u7403\u7535\u5B50\u821E\u66F2\u699C", bangid: "3812895" },
    { id: "wy__21845217", name: "KTV\u551B\u699C", bangid: "21845217" },
    { id: "wy__60131", name: "\u65E5\u672COricon\u699C", bangid: "60131" },
    { id: "wy__2809513713", name: "\u6B27\u7F8E\u70ED\u6B4C\u699C", bangid: "2809513713" },
    { id: "wy__2809577409", name: "\u6B27\u7F8E\u65B0\u6B4C\u699C", bangid: "2809577409" },
    { id: "wy__27135204", name: "\u6CD5\u56FD NRJ Vos Hits \u5468\u699C", bangid: "27135204" },
    { id: "wy__3001835560", name: "ACG\u52A8\u753B\u699C", bangid: "3001835560" },
    { id: "wy__3001795926", name: "ACG\u6E38\u620F\u699C", bangid: "3001795926" },
    { id: "wy__3001890046", name: "ACG VOCALOID\u699C", bangid: "3001890046" },
    { id: "wy__3112516681", name: "\u4E2D\u56FD\u65B0\u4E61\u6751\u97F3\u4E50\u6392\u884C\u699C", bangid: "3112516681" },
    { id: "wy__5059644681", name: "\u65E5\u8BED\u699C", bangid: "5059644681" },
    { id: "wy__5059633707", name: "\u6447\u6EDA\u699C", bangid: "5059633707" },
    { id: "wy__5059642708", name: "\u56FD\u98CE\u699C", bangid: "5059642708" },
    { id: "wy__5338990334", name: "\u6F5C\u529B\u7206\u6B3E\u699C", bangid: "5338990334" },
    { id: "wy__5059661515", name: "\u6C11\u8C23\u699C", bangid: "5059661515" },
    { id: "wy__6688069460", name: "\u542C\u6B4C\u8BC6\u66F2\u699C", bangid: "6688069460" },
    { id: "wy__6723173524", name: "\u7F51\u7EDC\u70ED\u6B4C\u699C", bangid: "6723173524" },
    { id: "wy__6732051320", name: "\u4FC4\u8BED\u699C", bangid: "6732051320" },
    { id: "wy__6732014811", name: "\u8D8A\u5357\u8BED\u699C", bangid: "6732014811" },
    { id: "wy__6886768100", name: "\u4E2D\u6587DJ\u699C", bangid: "6886768100" },
    { id: "wy__6939992364", name: "\u4FC4\u7F57\u65AFtop hit\u6D41\u884C\u97F3\u4E50\u699C", bangid: "6939992364" },
    { id: "wy__7095271308", name: "\u6CF0\u8BED\u699C", bangid: "7095271308" },
    { id: "wy__7356827205", name: "BEAT\u6392\u884C\u699C", bangid: "7356827205" },
    { id: "wy__7325478166", name: "\u7F16\u8F91\u63A8\u8350\u699CVOL.44 \u5929\u624D\u5973\u5B50\u6447\u6EDA\u4E50\u961Fboygenius\u5256\u767D\u5351\u5FAE\u5FC3\u8FF9", bangid: "7325478166" },
    { id: "wy__7603212484", name: "LOOK\u76F4\u64AD\u6B4C\u66F2\u699C", bangid: "7603212484" },
    { id: "wy__7775163417", name: "\u8D4F\u97F3\u699C", bangid: "7775163417" },
    { id: "wy__7785123708", name: "\u9ED1\u80F6VIP\u65B0\u6B4C\u699C", bangid: "7785123708" },
    { id: "wy__7785066739", name: "\u9ED1\u80F6VIP\u70ED\u6B4C\u699C", bangid: "7785066739" },
    { id: "wy__7785091694", name: "\u9ED1\u80F6VIP\u7231\u641C\u699C", bangid: "7785091694" }
  ];
  var leaderboard_default = {
    limit: 1e5,
    list: [
      {
        id: "wybsb",
        name: "\u98D9\u5347\u699C",
        bangid: "19723756"
      },
      {
        id: "wyrgb",
        name: "\u70ED\u6B4C\u699C",
        bangid: "3778678"
      },
      {
        id: "wyxgb",
        name: "\u65B0\u6B4C\u699C",
        bangid: "3779629"
      },
      {
        id: "wyycb",
        name: "\u539F\u521B\u699C",
        bangid: "2884035"
      },
      {
        id: "wygdb",
        name: "\u53E4\u5178\u699C",
        bangid: "71384707"
      },
      {
        id: "wydouyb",
        name: "\u6296\u97F3\u699C",
        bangid: "2250011882"
      },
      {
        id: "wyhyb",
        name: "\u97E9\u8BED\u699C",
        bangid: "745956260"
      },
      {
        id: "wydianyb",
        name: "\u7535\u97F3\u699C",
        bangid: "1978921795"
      },
      {
        id: "wydjb",
        name: "\u7535\u7ADE\u699C",
        bangid: "2006508653"
      },
      {
        id: "wyktvbb",
        name: "KTV\u551B\u699C",
        bangid: "21845217"
      }
    ],
    getUrl(id) {
      return `https://music.163.com/discover/toplist?id=${id}`;
    },
    regExps: {
      list: /<textarea id="song-list-pre-data" style="display:none;">(.+?)<\/textarea>/
    },
    _requestBoardsObj: null,
    getBoardsData() {
      if (this._requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this._requestBoardsObj = httpFetch("https://music.163.com/weapi/toplist", {
        method: "post",
        form: weapi({})
      });
      return this._requestBoardsObj.promise;
    },
    getData(id) {
      const requestBoardsDetailObj = httpFetch("https://music.163.com/weapi/v3/playlist/detail", {
        method: "post",
        form: weapi({
          id,
          n: 1e5,
          p: 1
        })
      });
      return requestBoardsDetailObj.promise;
    },
    filterBoardsData(rawList) {
      let list = [];
      for (const board of rawList) {
        list.push({
          id: "wy__" + board.id,
          name: board.name,
          bangid: String(board.id)
        });
      }
      return list;
    },
    async getBoards(retryNum = 0) {
      this.list = topList;
      return {
        list: topList,
        source: "wy"
      };
    },
    async getList(bangid, page, retryNum = 0) {
      if (++retryNum > 6) return Promise.reject(new Error("try max num"));
      let resp;
      try {
        resp = await this.getData(bangid);
      } catch (err) {
        if (err.message == "try max num") {
          throw err;
        } else {
          return this.getList(bangid, page, retryNum);
        }
      }
      if (resp.statusCode !== 200 || resp.body.code !== 200) return this.getList(bangid, page, retryNum);
      let musicDetail;
      try {
        musicDetail = await musicDetail_default.getList(resp.body.playlist.trackIds.map((trackId) => trackId.id));
      } catch (err) {
        console.log(err);
        if (err.message == "try max num") {
          throw err;
        } else {
          return this.getList(bangid, page, retryNum);
        }
      }
      return {
        total: musicDetail.list.length,
        list: musicDetail.list,
        limit: this.limit,
        page,
        source: "wy"
      };
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") id = id.replace("wy__", "");
      return `https://music.163.com/#/discover/toplist?id=${id}`;
    }
  };

  // lyn-internal:lx-internal-api-source
  var apis = (_src) => ({
    getMusicUrl: (_songInfo, _type) => ({
      promise: Promise.reject(new Error("apis.getMusicUrl: not implemented in M0 shim"))
    }),
    getLyric: () => ({ promise: Promise.reject(new Error("apis.getLyric: not implemented")) }),
    getPic: () => ({ promise: Promise.reject(new Error("apis.getPic: not implemented")) })
  });

  // ../vendor/lx-sdk/wy/lyric.js
  var eapiRequest = (url, data) => {
    return httpFetch("https://interface3.music.163.com/eapi/song/lyric/v1", {
      method: "post",
      headers: {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
        origin: "https://music.163.com"
        // cookie: 'os=pc; deviceId=A9C064BB4584D038B1565B58CB05F95290998EE8B025AA2D07AE; osver=Microsoft-Windows-10-Home-China-build-19043-64bit; appver=2.5.2.197409; channel=netease; MUSIC_A=37a11f2eb9de9930cad479b2ad495b0e4c982367fb6f909d9a3f18f876c6b49faddb3081250c4980dd7e19d4bd9bf384e004602712cf2b2b8efaafaab164268a00b47359f85f22705cc95cb6180f3aee40f5be1ebf3148d888aa2d90636647d0c3061cd18d77b7a0; __csrf=05b50d54082694f945d7de75c210ef94; mode=Z7M-KP5(7)GZ; NMTID=00OZLp2VVgq9QdwokUgq3XNfOddQyIAAAF_6i8eJg; ntes_kaola_ad=1',
      },
      form: eapi(url, data)
    });
  };
  var parseTools = {
    rxps: {
      info: /^{"/,
      lineTime: /^\[(\d+),\d+\]/,
      wordTime: /\(\d+,\d+,\d+\)/,
      wordTimeAll: /(\(\d+,\d+,\d+\))/g
    },
    msFormat(timeMs) {
      if (Number.isNaN(timeMs)) return "";
      let ms = timeMs % 1e3;
      timeMs /= 1e3;
      let m = parseInt(timeMs / 60).toString().padStart(2, "0");
      timeMs %= 60;
      let s = parseInt(timeMs).toString().padStart(2, "0");
      return `[${m}:${s}.${ms}]`;
    },
    parseLyric(lines) {
      const lxlrcLines = [];
      const lrcLines = [];
      for (let line of lines) {
        line = line.trim();
        let result = this.rxps.lineTime.exec(line);
        if (!result) {
          if (line.startsWith("[offset")) {
            lxlrcLines.push(line);
            lrcLines.push(line);
          }
          continue;
        }
        const startMsTime = parseInt(result[1]);
        const startTimeStr = this.msFormat(startMsTime);
        if (!startTimeStr) continue;
        let words = line.replace(this.rxps.lineTime, "");
        lrcLines.push(`${startTimeStr}${words.replace(this.rxps.wordTimeAll, "")}`);
        let times = words.match(this.rxps.wordTimeAll);
        if (!times) continue;
        times = times.map((time) => {
          const result2 = /\((\d+),(\d+),\d+\)/.exec(time);
          return `<${Math.max(parseInt(result2[1]) - startMsTime, 0)},${result2[2]}>`;
        });
        const wordArr = words.split(this.rxps.wordTime);
        wordArr.shift();
        const newWords = times.map((time, index) => `${time}${wordArr[index]}`).join("");
        lxlrcLines.push(`${startTimeStr}${newWords}`);
      }
      return {
        lyric: lrcLines.join("\n"),
        lxlyric: lxlrcLines.join("\n")
      };
    },
    parseHeaderInfo(str) {
      str = str.trim();
      str = str.replace(/\r/g, "");
      if (!str) return null;
      const lines = str.split("\n");
      return lines.map((line) => {
        if (!this.rxps.info.test(line)) return line;
        try {
          const info = JSON.parse(line);
          const timeTag = this.msFormat(info.t);
          return timeTag ? `${timeTag}${info.c.map((t) => t.tx).join("")}` : "";
        } catch {
          return "";
        }
      });
    },
    getIntv(interval) {
      if (!interval) return 0;
      if (!interval.includes(".")) interval += ".0";
      let arr = interval.split(/:|\./);
      while (arr.length < 3) arr.unshift("0");
      const [m, s, ms] = arr;
      return parseInt(m) * 36e5 + parseInt(s) * 1e3 + parseInt(ms);
    },
    fixTimeTag(lrc, targetlrc) {
      let lrcLines = lrc.split("\n");
      const targetlrcLines = targetlrc.split("\n");
      const timeRxp = /^\[([\d:.]+)\]/;
      let temp = [];
      let newLrc = [];
      targetlrcLines.forEach((line) => {
        const result = timeRxp.exec(line);
        if (!result) return;
        const words = line.replace(timeRxp, "");
        if (!words.trim()) return;
        const t1 = this.getIntv(result[1]);
        while (lrcLines.length) {
          const lrcLine = lrcLines.shift();
          const lrcLineResult = timeRxp.exec(lrcLine);
          if (!lrcLineResult) continue;
          const t2 = this.getIntv(lrcLineResult[1]);
          if (Math.abs(t1 - t2) < 100) {
            const lrc2 = line.replace(timeRxp, lrcLineResult[0]).trim();
            if (!lrc2) continue;
            newLrc.push(lrc2);
            break;
          }
          temp.push(lrcLine);
        }
        lrcLines = [...temp, ...lrcLines];
        temp = [];
      });
      return newLrc.join("\n");
    },
    parse(ylrc, ytlrc, yrlrc, lrc, tlrc, rlrc) {
      const info = {
        lyric: "",
        tlyric: "",
        rlyric: "",
        lxlyric: ""
      };
      if (ylrc) {
        let lines = this.parseHeaderInfo(ylrc);
        if (lines) {
          const result = this.parseLyric(lines);
          if (ytlrc) {
            const lines2 = this.parseHeaderInfo(ytlrc);
            if (lines2) {
              info.tlyric = this.fixTimeTag(result.lyric, lines2.join("\n"));
            }
          }
          if (yrlrc) {
            const lines2 = this.parseHeaderInfo(yrlrc);
            if (lines2) {
              info.rlyric = this.fixTimeTag(result.lyric, lines2.join("\n"));
            }
          }
          const timeRxp = /^\[[\d:.]+\]/;
          const headers = lines.filter((l) => timeRxp.test(l)).join("\n");
          info.lyric = `${headers}
${result.lyric}`;
          info.lxlyric = result.lxlyric;
          return info;
        }
      }
      if (lrc) {
        const lines = this.parseHeaderInfo(lrc);
        if (lines) info.lyric = lines.join("\n");
      }
      if (tlrc) {
        const lines = this.parseHeaderInfo(tlrc);
        if (lines) info.tlyric = lines.join("\n");
      }
      if (rlrc) {
        const lines = this.parseHeaderInfo(rlrc);
        if (lines) info.rlyric = lines.join("\n");
      }
      return info;
    }
  };
  var fixTimeLabel = (lrc, tlrc, romalrc) => {
    if (lrc) {
      let newLrc = lrc.replace(/\[(\d{2}:\d{2}):(\d{2})]/g, "[$1.$2]");
      let newTlrc = tlrc?.replace(/\[(\d{2}:\d{2}):(\d{2})]/g, "[$1.$2]") ?? tlrc;
      if (newLrc != lrc || newTlrc != tlrc) {
        lrc = newLrc;
        tlrc = newTlrc;
        if (romalrc) romalrc = romalrc.replace(/\[(\d{2}:\d{2}):(\d{2,3})]/g, "[$1.$2]").replace(/\[(\d{2}:\d{2}\.\d{2})0]/g, "[$1]");
      }
    }
    return { lrc, tlrc, romalrc };
  };
  var lyric_default = (songmid) => {
    const requestObj = eapiRequest("/api/song/lyric/v1", {
      id: songmid,
      cp: false,
      tv: 0,
      lv: 0,
      rv: 0,
      kv: 0,
      yv: 0,
      ytv: 0,
      yrv: 0
    });
    requestObj.promise = requestObj.promise.then(({ body }) => {
      if (body.code !== 200 || !body?.lrc?.lyric) return Promise.reject(new Error("Get lyric failed"));
      const fixTimeLabelLrc = fixTimeLabel(body.lrc.lyric, body.tlyric?.lyric, body.romalrc?.lyric);
      const info = parseTools.parse(body.yrc?.lyric, body.ytlrc?.lyric, body.yromalrc?.lyric, fixTimeLabelLrc.lrc, fixTimeLabelLrc.tlrc, fixTimeLabelLrc.romalrc);
      if (!info.lyric) return Promise.reject(new Error("Get lyric failed"));
      return info;
    });
    return requestObj;
  };

  // ../vendor/lx-sdk/wy/musicInfo.js
  var musicInfo_default = (songmid) => {
    const requestObj = httpFetch("https://music.163.com/weapi/v3/song/detail", {
      method: "post",
      headers: {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
        Referer: "https://music.163.com/song?id=" + songmid,
        origin: "https://music.163.com"
      },
      form: weapi({
        c: `[{"id":${songmid}}]`,
        ids: `[${songmid}]`
      })
    });
    requestObj.promise = requestObj.promise.then(({ body }) => {
      if (body.code !== 200 || !body.songs.length) return Promise.reject(new Error("\u83B7\u53D6\u6B4C\u66F2\u4FE1\u606F\u5931\u8D25"));
      return body.songs[0];
    });
    return requestObj;
  };

  // ../vendor/lx-sdk/wy/utils/index.js
  var eapiRequest2 = (url, data) => {
    return httpFetch("http://interface.music.163.com/eapi/batch", {
      method: "post",
      headers: {
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
        origin: "https://music.163.com"
        // cookie: 'os=pc; deviceId=A9C064BB4584D038B1565B58CB05F95290998EE8B025AA2D07AE; osver=Microsoft-Windows-10-Home-China-build-19043-64bit; appver=2.5.2.197409; channel=netease; MUSIC_A=37a11f2eb9de9930cad479b2ad495b0e4c982367fb6f909d9a3f18f876c6b49faddb3081250c4980dd7e19d4bd9bf384e004602712cf2b2b8efaafaab164268a00b47359f85f22705cc95cb6180f3aee40f5be1ebf3148d888aa2d90636647d0c3061cd18d77b7a0; __csrf=05b50d54082694f945d7de75c210ef94; mode=Z7M-KP5(7)GZ; NMTID=00OZLp2VVgq9QdwokUgq3XNfOddQyIAAAF_6i8eJg; ntes_kaola_ad=1',
      },
      form: eapi(url, data)
    });
  };

  // ../vendor/lx-sdk/wy/musicSearch.js
  var musicSearch_default = {
    limit: 30,
    total: 0,
    page: 0,
    allPage: 1,
    musicSearch(str, page, limit) {
      const searchRequest = eapiRequest2("/api/search/song/list/page", {
        keyword: str,
        needCorrect: "1",
        channel: "typing",
        offset: limit * (page - 1),
        scene: "normal",
        total: page == 1,
        limit
      });
      return searchRequest.promise.then(({ body }) => body);
    },
    getSinger(singers) {
      let arr = [];
      singers.forEach((singer) => {
        arr.push(singer.name);
      });
      return arr.join("\u3001");
    },
    handleResult(rawList) {
      if (!rawList) return [];
      return rawList.map((item) => {
        item = item.baseInfo.simpleSongData;
        const types = [];
        const _types = {};
        let size;
        if (item.privilege.maxBrLevel == "hires") {
          size = item.hr ? sizeFormate(item.hr.size) : null;
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        switch (item.privilege.maxbr) {
          case 999e3:
            size = item.sq ? sizeFormate(item.sq.size) : null;
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
          case 32e4:
            size = item.h ? sizeFormate(item.h.size) : null;
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
          case 192e3:
          case 128e3:
            size = item.l ? sizeFormate(item.l.size) : null;
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
        }
        types.reverse();
        return {
          singer: this.getSinger(item.ar),
          name: item.name,
          albumName: item.al.name,
          albumId: item.al.id,
          source: "wy",
          interval: formatPlayTime(item.dt / 1e3),
          songmid: item.id,
          img: item.al.picUrl,
          lrc: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    search(str, page = 1, limit, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then((result) => {
        if (!result || result.code !== 200) return this.search(str, page, limit, retryNum);
        let list = this.handleResult(result.data.resources || []);
        if (list == null) return this.search(str, page, limit, retryNum);
        this.total = result.data.totalCount || 0;
        this.page = page;
        this.allPage = Math.ceil(this.total / this.limit);
        return {
          list,
          allPage: this.allPage,
          limit: this.limit,
          total: this.total,
          source: "wy"
        };
      });
    }
  };

  // ../vendor/lx-sdk/wy/songList.js
  var songList_default = {
    _requestObj_tags: null,
    _requestObj_hotTags: null,
    _requestObj_list: null,
    limit_list: 30,
    limit_song: 1e5,
    successCode: 200,
    cookie: "MUSIC_U=",
    sortList: [
      {
        name: "\u6700\u70ED",
        tid: "hot",
        id: "hot"
      }
      // {
      //   name: '最新',
      //   tid: 'new',
      //   id: 'new',
      // },
    ],
    regExps: {
      listDetailLink: /^.+(?:\?|&)id=(\d+)(?:&.*$|#.*$|$)/,
      listDetailLink2: /^.+\/playlist\/(\d+)\/\d+\/.+$/
    },
    async handleParseId(link, retryNum = 0) {
      if (retryNum > 2) throw new Error("link try max num");
      const requestObj_listDetailLink = httpFetch(link);
      const { url, statusCode } = await requestObj_listDetailLink.promise;
      if (statusCode > 400) return this.handleParseId(link, ++retryNum);
      return this.regExps.listDetailLink.test(url) ? url.replace(this.regExps.listDetailLink, "$1") : url.replace(this.regExps.listDetailLink2, "$1");
    },
    async getListId(id) {
      let cookie;
      if (/###/.test(id)) {
        const [url, token] = id.split("###");
        id = url;
        cookie = `MUSIC_U=${token}`;
      }
      if (/[?&:/]/.test(id)) {
        if (this.regExps.listDetailLink.test(id)) {
          id = id.replace(this.regExps.listDetailLink, "$1");
        } else if (this.regExps.listDetailLink2.test(id)) {
          id = id.replace(this.regExps.listDetailLink2, "$1");
        } else {
          id = await this.handleParseId(id);
        }
      }
      return { id, cookie };
    },
    async getListDetail(rawId, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const { id, cookie } = await this.getListId(rawId);
      if (cookie) this.cookie = cookie;
      const requestObj_listDetail = httpFetch("https://music.163.com/api/linux/forward", {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
          Cookie: this.cookie
        },
        credentials: "omit",
        cache: "default",
        form: linuxapi({
          method: "POST",
          url: "https://music.163.com/api/v3/playlist/detail",
          params: {
            id,
            n: this.limit_song,
            s: 8
          }
        })
      });
      const { statusCode, body } = await requestObj_listDetail.promise;
      if (statusCode !== 200 || body.code !== this.successCode) return this.getListDetail(id, page, ++tryNum);
      let limit = 1e3;
      let rangeStart = (page - 1) * limit;
      let list;
      if (body.playlist.trackIds.length == body.privileges.length) {
        list = this.filterListDetail(body);
      } else {
        try {
          list = (await musicDetail_default.getList(body.playlist.trackIds.slice(rangeStart, limit * page).map((trackId) => trackId.id))).list;
        } catch (err) {
          console.log(err);
          if (err.message == "try max num") {
            throw err;
          } else {
            return this.getListDetail(id, page, ++tryNum);
          }
        }
      }
      return {
        list,
        page,
        limit,
        total: body.playlist.trackIds.length,
        source: "wy",
        info: {
          play_count: formatPlayCount(body.playlist.playCount),
          name: body.playlist.name,
          img: body.playlist.coverImgUrl,
          desc: body.playlist.description,
          author: body.playlist.creator.nickname
        }
      };
    },
    filterListDetail({ playlist: { tracks }, privileges }) {
      const list = [];
      tracks.forEach((item, index) => {
        const types = [];
        const _types = {};
        let size;
        let privilege = privileges[index];
        if (privilege.id !== item.id) privilege = privileges.find((p) => p.id === item.id);
        if (!privilege) return;
        if (privilege.maxBrLevel == "hires") {
          size = item.hr ? sizeFormate(item.hr.size) : null;
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        switch (privilege.maxbr) {
          case 999e3:
            size = null;
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
          case 32e4:
            size = item.h ? sizeFormate(item.h.size) : null;
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
          case 192e3:
          case 128e3:
            size = item.l ? sizeFormate(item.l.size) : null;
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
        }
        types.reverse();
        if (item.pc) {
          list.push({
            singer: item.pc.ar ?? "",
            name: item.pc.sn ?? "",
            albumName: item.pc.alb ?? "",
            albumId: item.al?.id,
            source: "wy",
            interval: formatPlayTime(item.dt / 1e3),
            songmid: item.id,
            img: item.al?.picUrl ?? "",
            lrc: null,
            otherSource: null,
            types,
            _types,
            typeUrl: {}
          });
        } else {
          list.push({
            singer: formatSingerName(item.ar, "name"),
            name: item.name ?? "",
            albumName: item.al?.name,
            albumId: item.al?.id,
            source: "wy",
            interval: formatPlayTime(item.dt / 1e3),
            songmid: item.id,
            img: item.al?.picUrl,
            lrc: null,
            otherSource: null,
            types,
            _types,
            typeUrl: {}
          });
        }
      });
      return list;
    },
    // 获取列表数据
    getList(sortId, tagId, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      this._requestObj_list = httpFetch("https://music.163.com/weapi/playlist/list", {
        method: "post",
        form: weapi({
          cat: tagId || "\u5168\u90E8",
          // 全部,华语,欧美,日语,韩语,粤语,小语种,流行,摇滚,民谣,电子,舞曲,说唱,轻音乐,爵士,乡村,R&B/Soul,古典,民族,英伦,金属,朋克,蓝调,雷鬼,世界音乐,拉丁,另类/独立,New Age,古风,后摇,Bossa Nova,清晨,夜晚,学习,工作,午休,下午茶,地铁,驾车,运动,旅行,散步,酒吧,怀旧,清新,浪漫,性感,伤感,治愈,放松,孤独,感动,兴奋,快乐,安静,思念,影视原声,ACG,儿童,校园,游戏,70后,80后,90后,网络歌曲,KTV,经典,翻唱,吉他,钢琴,器乐,榜单,00后
          order: sortId,
          // hot,new
          limit: this.limit_list,
          offset: this.limit_list * (page - 1),
          total: true
        })
      });
      return this._requestObj_list.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getList(sortId, tagId, page, ++tryNum);
        return {
          list: this.filterList(body.playlists),
          total: parseInt(body.total),
          page,
          limit: this.limit_list,
          source: "wy"
        };
      });
    },
    filterList(rawData) {
      return rawData.map((item) => ({
        play_count: formatPlayCount(item.playCount),
        id: String(item.id),
        author: item.creator.nickname,
        name: item.name,
        time: item.createTime ? dateFormat(item.createTime, "Y-M-D") : "",
        img: item.coverImgUrl,
        grade: item.grade,
        total: item.trackCount,
        desc: item.description,
        source: "wy"
      }));
    },
    // 获取标签
    getTag(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch("https://music.163.com/weapi/playlist/catalogue", {
        method: "post",
        form: weapi({})
      });
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterTagInfo(body);
      });
    },
    filterTagInfo({ sub, categories }) {
      const subList = {};
      for (const item of sub) {
        if (!subList[item.category]) subList[item.category] = [];
        subList[item.category].push({
          parent_id: categories[item.category],
          parent_name: categories[item.category],
          id: item.name,
          name: item.name,
          source: "wy"
        });
      }
      const list = [];
      for (const key of Object.keys(categories)) {
        list.push({
          name: categories[key],
          list: subList[key],
          source: "wy"
        });
      }
      return list;
    },
    // 获取热门标签
    getHotTag(tryNum = 0) {
      if (this._requestObj_hotTags) this._requestObj_hotTags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_hotTags = httpFetch("https://music.163.com/weapi/playlist/hottags", {
        method: "post",
        form: weapi({})
      });
      return this._requestObj_hotTags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterHotTagInfo(body.tags);
      });
    },
    filterHotTagInfo(rawList) {
      return rawList.map((item) => ({
        id: item.playlistTag.name,
        name: item.playlistTag.name,
        source: "wy"
      }));
    },
    getTags() {
      return Promise.all([this.getTag(), this.getHotTag()]).then(([tags, hotTag]) => ({ tags, hotTag, source: "wy" }));
    },
    async getDetailPageUrl(rawId) {
      const { id } = await this.getListId(rawId);
      return `https://music.163.com/#/playlist?id=${id}`;
    },
    search(text, page, limit = 20) {
      return eapiRequest2("/api/cloudsearch/pc", {
        s: text,
        type: 1e3,
        // 1: 单曲, 10: 专辑, 100: 歌手, 1000: 歌单, 1002: 用户, 1004: MV, 1006: 歌词, 1009: 电台, 1014: 视频
        limit,
        total: page == 1,
        offset: limit * (page - 1)
      }).promise.then(({ body }) => {
        if (body.code != this.successCode) throw new Error("filed");
        return {
          list: this.filterList(body.result.playlists),
          limit,
          total: body.result.playlistCount,
          source: "wy"
        };
      });
    }
  };

  // ../vendor/lx-sdk/wy/hotSearch.js
  var hotSearch_default = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = eapiRequest2("/api/search/chart/detail", {
        id: "HOT_SEARCH_SONG#@#"
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== 200) throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "wy", list: this.filterList(body.data.itemList) };
    },
    filterList(rawList) {
      return rawList.map((item) => item.searchWord);
    }
  };

  // ../vendor/lx-sdk/wy/comment.js
  var emojis = [
    ["\u5927\u7B11", "\u{1F603}"],
    ["\u53EF\u7231", "\u{1F60A}"],
    ["\u61A8\u7B11", "\u263A\uFE0F"],
    ["\u8272", "\u{1F60D}"],
    ["\u4EB2\u4EB2", "\u{1F619}"],
    ["\u60CA\u6050", "\u{1F631}"],
    ["\u6D41\u6CEA", "\u{1F62D}"],
    ["\u4EB2", "\u{1F61A}"],
    ["\u5446", "\u{1F633}"],
    ["\u54C0\u4F24", "\u{1F614}"],
    ["\u5472\u7259", "\u{1F601}"],
    ["\u5410\u820C", "\u{1F61D}"],
    ["\u6487\u5634", "\u{1F612}"],
    ["\u6012", "\u{1F621}"],
    ["\u5978\u7B11", "\u{1F60F}"],
    ["\u6C57", "\u{1F613}"],
    ["\u75DB\u82E6", "\u{1F616}"],
    ["\u60F6\u6050", "\u{1F630}"],
    ["\u751F\u75C5", "\u{1F628}"],
    ["\u53E3\u7F69", "\u{1F637}"],
    ["\u5927\u54ED", "\u{1F602}"],
    ["\u6655", "\u{1F635}"],
    ["\u53D1\u6012", "\u{1F47F}"],
    ["\u5F00\u5FC3", "\u{1F604}"],
    ["\u9B3C\u8138", "\u{1F61C}"],
    ["\u76B1\u7709", "\u{1F61E}"],
    ["\u6D41\u611F", "\u{1F622}"],
    ["\u7231\u5FC3", "\u2764\uFE0F"],
    ["\u5FC3\u788E", "\u{1F494}"],
    ["\u949F\u60C5", "\u{1F498}"],
    ["\u661F\u661F", "\u2B50\uFE0F"],
    ["\u751F\u6C14", "\u{1F4A2}"],
    ["\u4FBF\u4FBF", "\u{1F4A9}"],
    ["\u5F3A", "\u{1F44D}"],
    ["\u5F31", "\u{1F44E}"],
    ["\u62DC", "\u{1F64F}"],
    ["\u7275\u624B", "\u{1F46B}"],
    ["\u8DF3\u821E", "\u{1F46F}\u200D\u2640\uFE0F"],
    ["\u7981\u6B62", "\u{1F645}\u200D\u2640\uFE0F"],
    ["\u8FD9\u8FB9", "\u{1F481}\u200D\u2640\uFE0F"],
    ["\u7231\u610F", "\u{1F48F}"],
    ["\u793A\u7231", "\u{1F469}\u200D\u2764\uFE0F\u200D\u{1F468}"],
    ["\u5634\u5507", "\u{1F444}"],
    ["\u72D7", "\u{1F436}"],
    ["\u732B", "\u{1F431}"],
    ["\u732A", "\u{1F437}"],
    ["\u5154\u5B50", "\u{1F430}"],
    ["\u5C0F\u9E21", "\u{1F424}"],
    ["\u516C\u9E21", "\u{1F414}"],
    ["\u5E7D\u7075", "\u{1F47B}"],
    ["\u5723\u8BDE", "\u{1F385}"],
    ["\u5916\u661F", "\u{1F47D}"],
    ["\u94BB\u77F3", "\u{1F48E}"],
    ["\u793C\u7269", "\u{1F381}"],
    ["\u7537\u5B69", "\u{1F466}"],
    ["\u5973\u5B69", "\u{1F467}"],
    ["\u86CB\u7CD5", "\u{1F382}"],
    ["18", "\u{1F51E}"],
    ["\u5708", "\u2B55"],
    ["\u53C9", "\u274C"]
  ];
  var applyEmoji = (text) => {
    for (const e of emojis) text = text.replaceAll(`[${e[0]}]`, e[1]);
    return text;
  };
  var cursorTools = {
    cache: {},
    getCursor(id, page, limit) {
      let cacheData = this.cache[id];
      if (!cacheData) cacheData = this.cache[id] = {};
      let orderType;
      let cursor;
      let offset;
      if (page == 1) {
        cacheData.page = 1;
        cursor = cacheData.cursor = cacheData.prevCursor = Date.now();
        orderType = 1;
        offset = 0;
      } else if (cacheData.page) {
        cursor = cacheData.cursor;
        if (page > cacheData.page) {
          orderType = 1;
          offset = (page - cacheData.page - 1) * limit;
        } else if (page < cacheData.page) {
          orderType = 0;
          offset = (cacheData.page - page - 1) * limit;
        } else {
          cursor = cacheData.cursor = cacheData.prevCursor;
          offset = cacheData.offset;
          orderType = cacheData.orderType;
        }
      }
      return {
        orderType,
        cursor,
        offset
      };
    },
    setCursor(id, cursor, orderType, offset, page) {
      let cacheData = this.cache[id];
      if (!cacheData) cacheData = this.cache[id] = {};
      cacheData.prevCursor = cacheData.cursor;
      cacheData.cursor = cursor;
      cacheData.orderType = orderType;
      cacheData.offset = offset;
      cacheData.page = page;
    }
  };
  var comment_default = {
    _requestObj: null,
    _requestObj2: null,
    async getComment({ songmid }, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      const id = "R_SO_4_" + songmid;
      const cursorInfo = cursorTools.getCursor(songmid, page, limit);
      const _requestObj = httpFetch("https://music.163.com/weapi/comment/resource/comments/get", {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
          origin: "https://music.163.com",
          Refere: "http://music.163.com/"
        },
        form: weapi({
          cursor: cursorInfo.cursor,
          offset: cursorInfo.offset,
          orderType: cursorInfo.orderType,
          pageNo: page,
          pageSize: limit,
          rid: id,
          threadId: id
        })
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== 200) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      cursorTools.setCursor(songmid, body.data.cursor, cursorInfo.orderType, cursorInfo.offset, page);
      return { source: "wy", comments: this.filterComment(body.data.comments), total: body.data.totalCount, page, limit, maxPage: Math.ceil(body.data.totalCount / limit) || 1 };
    },
    async getHotComment({ songmid }, page = 1, limit = 100) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      const id = "R_SO_4_" + songmid;
      const _requestObj2 = httpFetch(`https://music.163.com/weapi/v1/resource/hotcomments/${id}`, {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36",
          origin: "https://music.163.com",
          Refere: "http://music.163.com/"
        },
        form: weapi({
          rid: id,
          limit,
          offset: limit * (page - 1),
          beforeTime: Date.now().toString()
        })
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code !== 200) throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const total = body.total ?? 0;
      return { source: "wy", comments: this.filterComment(body.hotComments), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    filterComment(rawList) {
      return rawList.map((item) => {
        let data = {
          id: item.commentId,
          text: item.content ? applyEmoji(item.content) : "",
          time: item.time ? item.time : "",
          timeStr: item.time ? dateFormat2(item.time) : "",
          location: item.ipLocation?.location,
          userName: item.user.nickname,
          avatar: item.user.avatarUrl,
          userId: item.user.userId,
          likedCount: item.likedCount,
          reply: []
        };
        let replyData = item.beReplied && item.beReplied[0];
        return replyData ? {
          id: item.commentId,
          rootId: replyData.beRepliedCommentId,
          text: replyData.content ? applyEmoji(replyData.content) : "",
          time: item.time,
          timeStr: null,
          location: replyData.ipLocation?.location,
          userName: replyData.user.nickname,
          avatar: replyData.user.avatarUrl,
          userId: replyData.user.userId,
          likedCount: null,
          reply: [data]
        } : data;
      });
    }
  };

  // ../vendor/lx-sdk/wy/index.js
  var wy = {
    // tipSearch,
    leaderboard: leaderboard_default,
    musicSearch: musicSearch_default,
    songList: songList_default,
    hotSearch: hotSearch_default,
    comment: comment_default,
    getMusicUrl(songInfo, type) {
      return apis("wy").getMusicUrl(songInfo, type);
    },
    getLyric(songInfo) {
      return lyric_default(songInfo.songmid);
    },
    getPic(songInfo) {
      const requestObj = musicInfo_default(songInfo.songmid);
      return requestObj.promise.then((info) => info.al.picUrl);
    },
    getMusicDetailPageUrl(songInfo) {
      return `https://music.163.com/#/song?id=${songInfo.songmid}`;
    }
  };
  var index_default = wy;
  return __toCommonJS(index_exports);
})();
globalThis.__lyn_source_wy = (typeof __lynSource !== 'undefined' && __lynSource.default) ? __lynSource.default : __lynSource;
