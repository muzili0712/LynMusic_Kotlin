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

  // ../vendor/lx-sdk/mg/index.js
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

  // lyn-internal:lx-internal-api-source
  var apis = (_src) => ({
    getMusicUrl: (_songInfo, _type) => ({
      promise: Promise.reject(new Error("apis.getMusicUrl: not implemented in M0 shim"))
    }),
    getLyric: () => ({ promise: Promise.reject(new Error("apis.getLyric: not implemented")) }),
    getPic: () => ({ promise: Promise.reject(new Error("apis.getPic: not implemented")) })
  });

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

  // ../vendor/lx-sdk/mg/utils/index.js
  var createHttpFetch = async (url, options, retryNum = 0) => {
    if (retryNum > 2) throw new Error("try max num");
    let result;
    try {
      result = await httpFetch(url, options).promise;
    } catch (err) {
      console.log(err);
      return createHttpFetch(url, options, ++retryNum);
    }
    if (result.statusCode !== 200 || (result.body.code !== void 0 ? result.body.code : result.body.returnCode !== void 0 ? result.body.returnCode : result.body.code) !== "000000") return createHttpFetch(url, options, ++retryNum);
    if (result.body.data) return result.body.data;
    return result.body;
  };

  // lyn-shim:react-native-quick-md5
  var hexHost = (input) => {
    if (typeof input === "string") input = globalThis.lyn.bufferFrom(input, "utf8");
    return globalThis.lyn.md5(input);
  };
  var stringMd5 = hexHost;

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

  // ../vendor/lx-sdk/mg/musicInfo.js
  var createGetMusicInfosTask = (ids) => {
    let list = ids;
    let tasks = [];
    while (list.length) {
      tasks.push(list.slice(0, 100));
      if (list.length < 100) break;
      list = list.slice(100);
    }
    let url = "https://c.musicapp.migu.cn/MIGUM2.0/v1.0/content/resourceinfo.do?resourceType=2";
    return Promise.all(tasks.map((task) => createHttpFetch(url, {
      method: "POST",
      form: {
        resourceId: task.join("|")
      }
    }).then((data) => data.resource)));
  };
  var filterMusicInfoList = (rawList) => {
    let ids = /* @__PURE__ */ new Set();
    const list = [];
    rawList.forEach((item) => {
      if (!item.songId || ids.has(item.songId)) return;
      ids.add(item.songId);
      const types = [];
      const _types = {};
      item.newRateFormats?.forEach((type) => {
        let size;
        switch (type.formatType) {
          case "PQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
            break;
          case "HQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
            break;
          case "SQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
            break;
          case "ZQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "flac24bit", size });
            _types.flac24bit = {
              size
            };
            break;
        }
      });
      const intervalTest = /(\d\d:\d\d)$/.test(item.length);
      list.push({
        singer: formatSingerName(item.artists, "name"),
        name: item.songName,
        albumName: item.album,
        albumId: item.albumId,
        songmid: item.songId,
        copyrightId: item.copyrightId,
        source: "mg",
        interval: intervalTest ? RegExp.$1 : null,
        img: item.albumImgs?.length ? item.albumImgs[0].img : null,
        lrc: null,
        lrcUrl: item.lrcUrl,
        mrcUrl: item.mrcUrl,
        trcUrl: item.trcUrl,
        otherSource: null,
        types,
        _types,
        typeUrl: {}
      });
    });
    return list;
  };
  var filterMusicInfoListV5 = (rawList) => {
    let ids = /* @__PURE__ */ new Set();
    const list = [];
    rawList.forEach((item) => {
      if (!item.songId || ids.has(item.songId)) return;
      ids.add(item.songId);
      const types = [];
      const _types = {};
      item.audioFormats?.forEach((type) => {
        let size;
        switch (type.formatType) {
          case "PQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "128k", size });
            _types["128k"] = {
              size
            };
            break;
          case "HQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "320k", size });
            _types["320k"] = {
              size
            };
            break;
          case "SQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "flac", size });
            _types.flac = {
              size
            };
            break;
          case "ZQ":
            size = sizeFormate(type.size ?? type.androidSize);
            types.push({ type: "flac24bit", size });
            _types.flac24bit = {
              size
            };
            break;
        }
      });
      list.push({
        singer: formatSingerName(item.singerList, "name"),
        name: item.songName,
        albumName: item.album,
        albumId: item.albumId,
        songmid: item.songId,
        copyrightId: item.copyrightId,
        source: "mg",
        interval: formatPlayTime(item.duration),
        img: item.img3 || item.img2 || item.img1 || null,
        lrc: null,
        lrcUrl: item.lrcUrl,
        mrcUrl: item.mrcUrl,
        trcUrl: item.trcUrl,
        otherSource: null,
        types,
        _types,
        typeUrl: {}
      });
    });
    return list;
  };
  var getMusicInfo = async (copyrightId) => {
    return getMusicInfos([copyrightId]).then((data) => data[0]);
  };
  var getMusicInfos = async (copyrightIds) => {
    return filterMusicInfoList(await Promise.all(createGetMusicInfosTask(copyrightIds)).then((data) => data.flat()));
  };

  // ../vendor/lx-sdk/mg/leaderboard.js
  var boardList = [
    {
      id: "mg__27553319",
      name: "\u65B0\u6B4C\u699C",
      bangid: "27553319",
      source: "mg"
    },
    {
      id: "mg__27186466",
      name: "\u70ED\u6B4C\u699C",
      bangid: "27186466",
      source: "mg"
    },
    {
      id: "mg__27553408",
      name: "\u539F\u521B\u699C",
      bangid: "27553408",
      source: "mg"
    },
    {
      id: "mg__75959118",
      name: "\u97F3\u4E50\u98CE\u5411\u699C",
      bangid: "75959118",
      source: "mg"
    },
    {
      id: "mg__76557036",
      name: "\u5F69\u94C3\u5206\u8D1D\u699C",
      bangid: "76557036",
      source: "mg"
    },
    {
      id: "mg__76557745",
      name: "\u4F1A\u5458\u81FB\u7231\u699C",
      bangid: "76557745",
      source: "mg"
    },
    {
      id: "mg__23189800",
      name: "\u6E2F\u53F0\u699C",
      bangid: "23189800",
      source: "mg"
    },
    {
      id: "mg__23189399",
      name: "\u5185\u5730\u699C",
      bangid: "23189399",
      source: "mg"
    },
    {
      id: "mg__19190036",
      name: "\u6B27\u7F8E\u699C",
      bangid: "19190036",
      source: "mg"
    },
    {
      id: "mg__83176390",
      name: "\u56FD\u98CE\u91D1\u66F2\u699C",
      bangid: "83176390",
      source: "mg"
    }
  ];
  var leaderboard_default = {
    limit: 200,
    list: [
      {
        id: "mgyyb",
        name: "\u97F3\u4E50\u699C",
        bangid: "27553319"
      },
      {
        id: "mgysb",
        name: "\u5F71\u89C6\u699C",
        bangid: "23603721"
      },
      {
        id: "mghybnd",
        name: "\u534E\u8BED\u5185\u5730\u699C",
        bangid: "23603926"
      },
      {
        id: "mghyjqbgt",
        name: "\u534E\u8BED\u6E2F\u53F0\u699C",
        bangid: "23603954"
      },
      {
        id: "mgomb",
        name: "\u6B27\u7F8E\u699C",
        bangid: "23603974"
      },
      {
        id: "mgrhb",
        name: "\u65E5\u97E9\u699C",
        bangid: "23603982"
      },
      {
        id: "mgwlb",
        name: "\u7F51\u7EDC\u699C",
        bangid: "23604058"
      },
      {
        id: "mgclb",
        name: "\u5F69\u94C3\u699C",
        bangid: "23604023"
      },
      {
        id: "mgktvb",
        name: "KTV\u699C",
        bangid: "23604040"
      },
      {
        id: "mgrcb",
        name: "\u539F\u521B\u699C",
        bangid: "23604032"
      }
    ],
    getUrl(id, page) {
      return `https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/querycontentbyId.do?columnId=${id}&needAll=0`;
    },
    successCode: "000000",
    requestBoardsObj: null,
    getBoardsData() {
      if (this.requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this.requestBoardsObj = httpFetch("https://app.c.nf.migu.cn/pc/bmw/rank/rank-index/v1.0", {
        // this.requestBoardsObj = httpFetch('https://app.c.nf.migu.cn/MIGUM3.0/v1.0/template/rank-list/release', {
        // this.requestBoardsObj = httpFetch('https://app.c.nf.migu.cn/MIGUM2.0/v2.0/content/indexrank.do?templateVersion=8', {
        headers: {
          Referer: "https://app.c.nf.migu.cn/",
          "User-Agent": "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36",
          channel: "0146921"
        }
      });
      return this.requestBoardsObj.promise;
    },
    getData(url) {
      const requestObj = httpFetch(url);
      return requestObj.promise;
    },
    // filterBoardsData(listData, list = [], ids = new Set()) {
    //   for (const item of listData) {
    //     if (item.rankId && !ids.has(item.rankId)) {
    //       ids.add(item.rankId)
    //       list.push({
    //         id: 'mg__' + item.rankId,
    //         name: item.rankName,
    //         bangid: String(item.rankId),
    //         source: 'mg',
    //       })
    //     } else if (item.contents) this.filterBoardsData(item.contents, list, ids)
    //   }
    //   return list
    // },
    // filterBoardsData(rawList) {
    //   // console.log(rawList)
    //   let list = []
    //   for (const board of rawList) {
    //     if (board.template != 'group1') continue
    //     for (const item of board.itemList) {
    //       if ((item.template != 'row1' && item.template != 'grid1' && !item.actionUrl) || !item.actionUrl.includes('rank-info')) continue
    //       let data = item.displayLogId.param
    //       list.push({
    //         id: 'mg__' + data.rankId,
    //         name: data.rankName,
    //         bangid: String(data.rankId),
    //       })
    //     }
    //   }
    //   return list
    // },
    async getBoards(retryNum = 0) {
      this.list = boardList;
      return {
        list: boardList,
        source: "mg"
      };
    },
    getList(bangid, page, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      return this.getData(this.getUrl(bangid, page)).then(({ statusCode, body }) => {
        if (statusCode !== 200 || body.code !== this.successCode) return this.getList(bangid, page, retryNum);
        const list = filterMusicInfoList(body.columnInfo.contents.map((m) => m.objectInfo));
        return {
          total: list.length,
          list,
          limit: this.limit,
          page,
          source: "mg"
        };
      });
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") id = id.replace("mg__", "");
      for (const item of boardList) {
        if (item.bangid == id) {
          return `https://music.migu.cn/v3/music/top/${item.webId}`;
        }
      }
      return null;
    }
  };

  // ../vendor/lx-sdk/mg/musicSearch.js
  var createSignature = (time, str) => {
    const deviceId = "963B7AA0D21511ED807EE5846EC87D20";
    const signatureMd5 = "6cdc72a439cef99a3418d2a78aa28c73";
    const sign = toMD5(`${str}${signatureMd5}yyapp2d16148780a1dcc7408e06336b98cfd50${deviceId}${time}`);
    return { sign, deviceId };
  };
  var musicSearch_default = {
    limit: 20,
    total: 0,
    page: 0,
    allPage: 1,
    // 旧版API
    // musicSearch(str, page, limit) {
    //   const searchRequest = httpFetch(`http://pd.musicapp.migu.cn/MIGUM2.0/v1.0/content/search_all.do?ua=Android_migu&version=5.0.1&text=${encodeURIComponent(str)}&pageNo=${page}&pageSize=${limit}&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A0%2C%22mvSong%22%3A0%2C%22songlist%22%3A0%2C%22bestShow%22%3A1%7D`, {
    // searchRequest = httpFetch(`http://pd.musicapp.migu.cn/MIGUM2.0/v1.0/content/search_all.do?ua=Android_migu&version=5.0.1&text=${encodeURIComponent(str)}&pageNo=${page}&pageSize=${limit}&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A0%2C%22mvSong%22%3A0%2C%22songlist%22%3A0%2C%22bestShow%22%3A1%7D`, {
    // searchRequest = httpFetch(`http://jadeite.migu.cn:7090/music_search/v2/search/searchAll?sid=4f87090d01c84984a11976b828e2b02c18946be88a6b4c47bcdc92fbd40762db&isCorrect=1&isCopyright=1&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A1%2C%22mvSong%22%3A0%2C%22bestShow%22%3A1%2C%22songlist%22%3A0%2C%22lyricSong%22%3A0%7D&pageSize=${limit}&text=${encodeURIComponent(str)}&pageNo=${page}&sort=0`, {
    // searchRequest = httpFetch(`https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/search_all.do?isCopyright=1&isCorrect=1&pageNo=${page}&pageSize=${limit}&searchSwitch={%22song%22:1,%22album%22:0,%22singer%22:0,%22tagSong%22:0,%22mvSong%22:0,%22songlist%22:0,%22bestShow%22:0}&sort=0&text=${encodeURIComponent(str)}`)
    //   // searchRequest = httpFetch(`http://jadeite.migu.cn:7090/music_search/v2/search/searchAll?sid=4f87090d01c84984a11976b828e2b02c18946be88a6b4c47bcdc92fbd40762db&isCorrect=1&isCopyright=1&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A1%2C%22mvSong%22%3A0%2C%22bestShow%22%3A1%2C%22songlist%22%3A0%2C%22lyricSong%22%3A0%7D&pageSize=${limit}&text=${encodeURIComponent(str)}&pageNo=${page}&sort=0`, {
    //     headers: {
    //       // sign: 'c3b7ae985e2206e97f1b2de8f88691e2',
    //       // timestamp: 1578225871982,
    //       // appId: 'yyapp2',
    //       // mode: 'android',
    //       // ua: 'Android_migu',
    //       // version: '6.9.4',
    //       osVersion: 'android 7.0',
    //       'User-Agent': 'okhttp/3.9.1',
    //     },
    //   })
    //   // searchRequest = httpFetch(`https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/search_all.do?isCopyright=1&isCorrect=1&pageNo=${page}&pageSize=${limit}&searchSwitch={%22song%22:1,%22album%22:0,%22singer%22:0,%22tagSong%22:0,%22mvSong%22:0,%22songlist%22:0,%22bestShow%22:0}&sort=0&text=${encodeURIComponent(str)}`)
    //   return searchRequest.promise.then(({ body }) => body)
    // },
    // handleResult(rawData) {
    //   // console.log(rawData)
    //   let ids = new Set()
    //   const list = []
    //   rawData.forEach(item => {
    //     if (ids.has(item.id)) return
    //     ids.add(item.id)
    //     const types = []
    //     const _types = {}
    //     item.newRateFormats && item.newRateFormats.forEach(type => {
    //       let size
    //       switch (type.formatType) {
    //         case 'PQ':
    //           size = sizeFormate(type.size ?? type.androidSize)
    //           types.push({ type: '128k', size })
    //           _types['128k'] = {
    //             size,
    //           }
    //           break
    //         case 'HQ':
    //           size = sizeFormate(type.size ?? type.androidSize)
    //           types.push({ type: '320k', size })
    //           _types['320k'] = {
    //             size,
    //           }
    //           break
    //         case 'SQ':
    //           size = sizeFormate(type.size ?? type.androidSize)
    //           types.push({ type: 'flac', size })
    //           _types.flac = {
    //             size,
    //           }
    //           break
    //         case 'ZQ':
    //           size = sizeFormate(type.size ?? type.androidSize)
    //           types.push({ type: 'flac24bit', size })
    //           _types.flac24bit = {
    //             size,
    //           }
    //           break
    //       }
    //     })
    //     const albumNInfo = item.albums && item.albums.length
    //       ? {
    //           id: item.albums[0].id,
    //           name: item.albums[0].name,
    //         }
    //       : {}
    //     list.push({
    //       singer: this.getSinger(item.singers),
    //       name: item.name,
    //       albumName: albumNInfo.name,
    //       albumId: albumNInfo.id,
    //       songmid: item.songId,
    //       copyrightId: item.copyrightId,
    //       source: 'mg',
    //       interval: null,
    //       img: item.imgItems && item.imgItems.length ? item.imgItems[0].img : null,
    //       lrc: null,
    //       lrcUrl: item.lyricUrl,
    //       mrcUrl: item.mrcurl,
    //       trcUrl: item.trcUrl,
    //       otherSource: null,
    //       types,
    //       _types,
    //       typeUrl: {},
    //     })
    //   })
    //   return list
    // },
    musicSearch(str, page, limit) {
      const time = Date.now().toString();
      const signData = createSignature(time, str);
      const searchRequest = httpFetch(`https://jadeite.migu.cn/music_search/v3/search/searchAll?isCorrect=0&isCopyright=1&searchSwitch=%7B%22song%22%3A1%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A1%2C%22mvSong%22%3A0%2C%22bestShow%22%3A1%2C%22songlist%22%3A0%2C%22lyricSong%22%3A0%7D&pageSize=${limit}&text=${encodeURIComponent(str)}&pageNo=${page}&sort=0&sid=USS`, {
        headers: {
          uiVersion: "A_music_3.6.1",
          deviceId: signData.deviceId,
          timestamp: time,
          sign: signData.sign,
          channel: "0146921",
          "User-Agent": "Mozilla/5.0 (Linux; U; Android 11.0.0; zh-cn; MI 11 Build/OPR1.170623.032) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
        }
      });
      return searchRequest.promise.then(({ body }) => body);
    },
    filterData(rawData) {
      const list = [];
      const ids = /* @__PURE__ */ new Set();
      rawData.forEach((item) => {
        item.forEach((data) => {
          if (!data.songId || !data.copyrightId || ids.has(data.copyrightId)) return;
          ids.add(data.copyrightId);
          const types = [];
          const _types = {};
          data.audioFormats && data.audioFormats.forEach((type) => {
            let size;
            switch (type.formatType) {
              case "PQ":
                size = sizeFormate(type.asize ?? type.isize);
                types.push({ type: "128k", size });
                _types["128k"] = {
                  size
                };
                break;
              case "HQ":
                size = sizeFormate(type.asize ?? type.isize);
                types.push({ type: "320k", size });
                _types["320k"] = {
                  size
                };
                break;
              case "SQ":
                size = sizeFormate(type.asize ?? type.isize);
                types.push({ type: "flac", size });
                _types.flac = {
                  size
                };
                break;
              case "ZQ24":
                size = sizeFormate(type.asize ?? type.isize);
                types.push({ type: "flac24bit", size });
                _types.flac24bit = {
                  size
                };
                break;
            }
          });
          let img = data.img3 || data.img2 || data.img1 || null;
          if (img && !/https?:/.test(data.img3)) img = "http://d.musicapp.migu.cn" + img;
          list.push({
            singer: formatSingerName(data.singerList),
            name: data.name,
            albumName: data.album,
            albumId: data.albumId,
            songmid: data.songId,
            copyrightId: data.copyrightId,
            source: "mg",
            interval: formatPlayTime(data.duration),
            img,
            lrc: null,
            lrcUrl: data.lrcUrl,
            mrcUrl: data.mrcurl,
            trcUrl: data.trcUrl,
            types,
            _types,
            typeUrl: {}
          });
        });
      });
      return list;
    },
    search(str, page = 1, limit, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then((result) => {
        if (!result || result.code !== "000000") return Promise.reject(new Error(result ? result.info : "\u641C\u7D22\u5931\u8D25"));
        const songResultData = result.songResultData || { resultList: [], totalCount: 0 };
        let list = this.filterData(songResultData.resultList);
        if (list == null) return this.search(str, page, limit, retryNum);
        this.total = parseInt(songResultData.totalCount);
        this.page = page;
        this.allPage = Math.ceil(this.total / limit);
        return {
          list,
          allPage: this.allPage,
          limit,
          total: this.total,
          source: "mg"
        };
      });
    }
  };

  // ../vendor/lx-sdk/mg/songList.js
  var songList_default = {
    _requestObj_tags: null,
    _requestObj_list: null,
    limit_list: 30,
    limit_song: 30,
    successCode: "000000",
    cachedDetailInfo: {},
    cachedUrl: {},
    sortList: [
      {
        name: "\u63A8\u8350",
        id: "15127315",
        tid: "recommend"
        // id: '1',
      }
      // {
      //   name: '最新',
      //   id: '15127272',
      //   tid: 'new',
      //   // id: '2',
      // },
    ],
    regExps: {
      list: /<li><div class="thumb">.+?<\/li>/g,
      listInfo: /.+data-original="(.+?)".*data-id="(\d+)".*<div class="song-list-name"><a\s.*?>(.+?)<\/a>.+<i class="iconfont cf-bofangliang"><\/i>(.+?)<\/div>/,
      // https://music.migu.cn/v3/music/playlist/161044573?page=1
      listDetailLink: /^.+\/playlist\/(\d+)(?:\?.*|&.*$|#.*$|$)/
    },
    tagsUrl: "https://app.c.nf.migu.cn/pc/v1.0/template/musiclistplaza-taglist/release",
    // tagsUrl: 'https://app.c.nf.migu.cn/MIGUM3.0/v1.0/template/musiclistplaza-taglist/release',
    // tagsUrl: 'https://app.c.nf.migu.cn/MIGUM2.0/v1.0/content/indexTagPage.do?needAll=0',
    getSongListUrl(sortId, tagId, page) {
      if (!tagId) {
        return `https://app.c.nf.migu.cn/pc/bmw/page-data/playlist-square-recommend/v1.0?templateVersion=2&pageNo=${page}`;
      }
      return `https://app.c.nf.migu.cn/pc/v1.0/template/musiclistplaza-listbytag/release?pageNumber=${page}&templateVersion=2&tagId=${tagId}`;
    },
    getSongListDetailUrl(id, page) {
      return `https://app.c.nf.migu.cn/MIGUM3.0/resource/playlist/song/v2.0?pageNo=${page}&pageSize=${this.limit_song}&playlistId=${id}`;
    },
    defaultHeaders: {
      "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1",
      Referer: "https://m.music.migu.cn/"
      // language: 'Chinese',
      // ua: 'Android_migu',
      // mode: 'android',
      // version: '6.8.5',
    },
    getListDetailList(id, page, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      const requestObj_listDetail = httpFetch(this.getSongListDetailUrl(id, page), { headers: this.defaultHeaders });
      return requestObj_listDetail.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getListDetailList(id, page, ++tryNum);
        return {
          list: filterMusicInfoListV5(body.data.songList),
          page,
          limit: this.limit_song,
          total: body.data.totalCount,
          source: "mg"
        };
      });
    },
    getListDetailInfo(id, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      if (this.cachedDetailInfo[id]) return Promise.resolve(this.cachedDetailInfo[id]);
      const requestObj_listDetailInfo = httpFetch(`https://c.musicapp.migu.cn/MIGUM3.0/resource/playlist/v2.0?playlistId=${id}`, {
        headers: this.defaultHeaders
      });
      return requestObj_listDetailInfo.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getListDetail(id, ++tryNum);
        const cachedDetailInfo = this.cachedDetailInfo[id] = {
          name: body.data.title,
          img: body.data.imgItem.img,
          desc: body.data.summary,
          author: body.data.ownerName,
          play_count: formatPlayCount(body.data.opNumItem.playNum)
        };
        return cachedDetailInfo;
      });
    },
    async getDetailUrl(link, page, retryNum = 0) {
      if (retryNum > 3) return Promise.reject(new Error("link try max num"));
      const requestObj_listDetailLink = httpFetch(link, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
          Referer: link
        }
      });
      const { url: location, statusCode } = await requestObj_listDetailLink.promise;
      if (statusCode > 400) return this.getDetailUrl(link, page, ++retryNum);
      if (location.split("?")[0] != link.split("?")[0]) {
        this.cachedUrl[link] = location;
        return this.getListDetail(location, page);
      }
      return Promise.reject(new Error("link get failed"));
    },
    getListDetail(id, page, retryNum = 0) {
      if (/\/playlist[/?]/.test(id)) {
        id = /(?:playlistId|id)=(\d+)/.exec(id)?.[1];
        if (!id) throw new Error("list detail id parse failed");
      } else if (this.regExps.listDetailLink.test(id)) {
        id = id.replace(this.regExps.listDetailLink, "$1");
      } else if (/[?&:/]/.test(id)) {
        const url = this.cachedUrl[id];
        return url ? this.getListDetail(url, page) : this.getDetailUrl(id, page);
      }
      return Promise.all([
        this.getListDetailList(id, page, retryNum),
        this.getListDetailInfo(id, retryNum)
      ]).then(([listData, info]) => {
        listData.info = info;
        return listData;
      });
    },
    // 获取列表数据
    getList(sortId, tagId, page, tryNum = 0) {
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_list = httpFetch(this.getSongListUrl(sortId, tagId, page), {
        headers: this.defaultHeaders
        // headers: {
        //   sign: 'c3b7ae985e2206e97f1b2de8f88691e2',
        //   timestamp: 1578225871982,
        //   appId: 'yyapp2',
        //   mode: 'android',
        //   ua: 'Android_migu',
        //   version: '6.9.4',
        //   osVersion: 'android 7.0',
        //   'User-Agent': 'okhttp/3.9.1',
        // },
      });
      return this._requestObj_list.promise.then(({ body }) => {
        if (body.code !== "000000") return this.getList(sortId, tagId, page, ++tryNum);
        const list = body.data.contents ? this.filterList2(body.data.contents) : this.filterList(body.data.contentItemList[1].itemList);
        return {
          list,
          total: 99999,
          page,
          limit: this.limit_list,
          source: "mg"
        };
      });
    },
    filterList2(listData, list = [], ids = /* @__PURE__ */ new Set()) {
      for (const item of listData) {
        if (item.contents) this.filterList2(item.contents, list, ids);
        else if (item.resType == "2021" && !ids.has(item.resId)) {
          ids.add(item.resId);
          list.push({
            id: String(item.resId),
            author: "",
            name: item.txt,
            // time: dateFormat(item.createTime, 'Y-M-D'),
            img: item.img,
            // grade: item.grade,
            // total: item.contentCount,
            desc: item.txt2,
            source: "mg"
          });
        }
      }
      return list;
    },
    filterList(rawData) {
      return rawData.map((item) => ({
        play_count: item.barList[0]?.title,
        id: String(item.logEvent.contentId),
        author: "",
        name: item.title,
        // time: dateFormat(item.createTime, 'Y-M-D'),
        img: item.imageUrl,
        // grade: item.grade,
        // total: item.contentCount,
        desc: "",
        source: "mg"
      }));
    },
    // 获取标签
    getTag(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch(this.tagsUrl, { headers: this.defaultHeaders });
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterTagInfo(body.data);
      });
    },
    filterTagInfo(rawList) {
      return {
        hotTag: rawList[0].content.map(({ texts: [name, id] }) => ({
          id,
          name,
          source: "mg"
        })),
        tags: rawList.slice(1).map(({ header, content }) => ({
          name: header.title,
          list: content.map(({ texts: [name, id] }) => ({
            // parent_id: objectInfo.columnId,
            // parent_name: objectInfo.columnTitle,
            id,
            name,
            source: "mg"
          }))
        })),
        source: "mg"
      };
    },
    getTags() {
      return this.getTag();
    },
    getDetailPageUrl(id) {
      if (/playlist\/index\.html\?/.test(id)) {
        id = id.replace(/.*(?:\?|&)id=(\d+)(?:&.*|$)/, "$1");
      } else if (this.regExps.listDetailLink.test(id)) {
        id = id.replace(this.regExps.listDetailLink, "$1");
      }
      return `https://music.migu.cn/v3/music/playlist/${id}`;
    },
    filterSongListResult(raw) {
      const list = [];
      raw.forEach((item) => {
        if (!item.id) return;
        const playCount = parseInt(item.playNum);
        list.push({
          play_count: isNaN(playCount) ? 0 : formatPlayCount(playCount),
          id: item.id,
          author: item.userName,
          name: item.name,
          img: item.musicListPicUrl,
          total: item.musicNum,
          source: "mg"
        });
      });
      return list;
    },
    search(text, page, limit = 20) {
      const timeStr = Date.now().toString();
      const signResult = createSignature(timeStr, text);
      return createHttpFetch(`https://jadeite.migu.cn/music_search/v3/search/searchAll?isCorrect=1&isCopyright=1&searchSwitch=%7B%22song%22%3A0%2C%22album%22%3A0%2C%22singer%22%3A0%2C%22tagSong%22%3A0%2C%22mvSong%22%3A0%2C%22bestShow%22%3A0%2C%22songlist%22%3A1%2C%22lyricSong%22%3A0%7D&pageSize=${limit}&text=${encodeURIComponent(text)}&pageNo=${page}&sort=0&sid=USS`, {
        headers: {
          uiVersion: "A_music_3.6.1",
          deviceId: signResult.deviceId,
          timestamp: timeStr,
          sign: signResult.sign,
          channel: "0146921",
          "User-Agent": "Mozilla/5.0 (Linux; U; Android 11.0.0; zh-cn; MI 11 Build/OPR1.170623.032) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
        }
      }).then((body) => {
        if (!body.songListResultData) throw new Error("get song list faild.");
        const list = this.filterSongListResult(body.songListResultData.result);
        return {
          list,
          limit,
          total: parseInt(body.songListResultData.totalCount),
          source: "mg"
        };
      });
    }
  };

  // ../vendor/lx-sdk/mg/songId.js
  var getSongId = async (mInfo) => {
    if (mInfo.songmid != mInfo.copyrightId) return mInfo.songmid;
    const musicInfo = await getMusicInfo(mInfo.copyrightId);
    return musicInfo.songmid;
  };
  var songId_default = getSongId;

  // ../vendor/lx-sdk/mg/pic.js
  var pic_default = {
    async getPicUrl(songId, tryNum = 0) {
      let requestObj = httpFetch(`http://music.migu.cn/v3/api/music/audioPlayer/getSongPic?songId=${songId}`, {
        headers: {
          Referer: "http://music.migu.cn/v3/music/player/audio?from=migu"
        }
      });
      requestObj.promise.then(({ body }) => {
        if (body.returnCode !== "000000") {
          if (tryNum > 5) return Promise.reject(new Error("\u56FE\u7247\u83B7\u53D6\u5931\u8D25"));
          let tryRequestObj = this.getPic(songId, ++tryNum);
          requestObj.cancelHttp = tryRequestObj.cancelHttp.bind(tryRequestObj);
          return tryRequestObj.promise;
        }
        let url = body.largePic || body.mediumPic || body.smallPic;
        if (!/https?:/.test(url)) url = "http:" + url;
        return url;
      });
      return requestObj;
    },
    async getPic(songInfo) {
      const songId = await songId_default(songInfo);
      return this.getPicUrl(songId);
    }
  };

  // ../vendor/lx-sdk/mg/utils/mrc.js
  var DELTA = 2654435769n;
  var MIN_LENGTH = 32;
  var keyArr = [
    27303562373562475n,
    18014862372307051n,
    22799692160172081n,
    34058940340699235n,
    30962724186095721n,
    27303523720101991n,
    27303523720101998n,
    31244139033526382n,
    28992395054481524n
  ];
  var teaDecrypt = (data, key) => {
    const length = data.length;
    const lengthBitint = BigInt(length);
    if (length >= 1) {
      let j2 = data[0];
      let j3 = toLong((6n + 52n / lengthBitint) * DELTA);
      while (true) {
        let j4 = j3;
        if (j4 == 0n) break;
        let j5 = toLong(3n & toLong(j4 >> 2n));
        let j6 = lengthBitint;
        while (true) {
          j6--;
          if (j6 > 0n) {
            let j7 = data[j6 - 1n];
            let i = j6;
            j2 = toLong(data[i] - (toLong(toLong(j2 ^ j4) + toLong(j7 ^ key[toLong(toLong(3n & j6) ^ j5)])) ^ toLong(toLong(toLong(j7 >> 5n) ^ toLong(j2 << 2n)) + toLong(toLong(j2 >> 3n) ^ toLong(j7 << 4n)))));
            data[i] = j2;
          } else break;
        }
        let j8 = data[lengthBitint - 1n];
        j2 = toLong(data[0n] - toLong(toLong(toLong(key[toLong(toLong(j6 & 3n) ^ j5)] ^ j8) + toLong(j2 ^ j4)) ^ toLong(toLong(toLong(j8 >> 5n) ^ toLong(j2 << 2n)) + toLong(toLong(j2 >> 3n) ^ toLong(j8 << 4n)))));
        data[0] = j2;
        j3 = toLong(j4 - DELTA);
      }
    }
    return data;
  };
  var longArrToString = (data) => {
    const arrayList = [];
    for (const j of data) arrayList.push(longToBytes(j).toString("utf16le"));
    return arrayList.join("");
  };
  var longToBytes = (l) => {
    const result = Buffer2.alloc(8);
    for (let i = 0; i < 8; i++) {
      result[i] = parseInt(l & 0xFFn);
      l >>= 8n;
    }
    return result;
  };
  var toBigintArray = (data) => {
    const length = Math.floor(data.length / 16);
    const jArr = Array(length);
    for (let i = 0; i < length; i++) {
      jArr[i] = toLong(data.substring(i * 16, i * 16 + 16));
    }
    return jArr;
  };
  var MAX = 9223372036854775807n;
  var MIN = -9223372036854775808n;
  var toLong = (str) => {
    const num = typeof str == "string" ? BigInt("0x" + str) : str;
    if (num > MAX) return toLong(num - (1n << 64n));
    else if (num < MIN) return toLong(num + (1n << 64n));
    return num;
  };
  var decrypt = (data) => {
    return data == null || data.length < MIN_LENGTH ? data : longArrToString(teaDecrypt(toBigintArray(data), keyArr));
  };

  // ../vendor/lx-sdk/mg/lyric.js
  var mrcTools = {
    rxps: {
      lineTime: /^\s*\[(\d+),\d+\]/,
      wordTime: /\(\d+,\d+\)/,
      wordTimeAll: /(\(\d+,\d+\))/g
    },
    parseLyric(str) {
      str = str.replace(/\r/g, "");
      const lines = str.split("\n");
      const lxlrcLines = [];
      const lrcLines = [];
      for (const line of lines) {
        if (line.length < 6) continue;
        let result = this.rxps.lineTime.exec(line);
        if (!result) continue;
        const startTime = parseInt(result[1]);
        let time = startTime;
        let ms = time % 1e3;
        time /= 1e3;
        let m = parseInt(time / 60).toString().padStart(2, "0");
        time %= 60;
        let s = parseInt(time).toString().padStart(2, "0");
        time = `${m}:${s}.${ms}`;
        let words = line.replace(this.rxps.lineTime, "");
        lrcLines.push(`[${time}]${words.replace(this.rxps.wordTimeAll, "")}`);
        let times = words.match(this.rxps.wordTimeAll);
        if (!times) continue;
        times = times.map((time2) => {
          const result2 = /\((\d+),(\d+)\)/.exec(time2);
          return `<${parseInt(result2[1]) - startTime},${result2[2]}>`;
        });
        const wordArr = words.split(this.rxps.wordTime);
        const newWords = times.map((time2, index) => `${time2}${wordArr[index]}`).join("");
        lxlrcLines.push(`[${time}]${newWords}`);
      }
      return {
        lyric: lrcLines.join("\n"),
        lxlyric: lxlrcLines.join("\n")
      };
    },
    getText(url, tryNum = 0) {
      const requestObj = httpFetch(url, {
        headers: {
          Referer: "https://app.c.nf.migu.cn/",
          "User-Agent": "Mozilla/5.0 (Linux; Android 5.1.1; Nexus 6 Build/LYZ28E) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Mobile Safari/537.36",
          channel: "0146921"
        }
      });
      return requestObj.promise.then(({ statusCode, body }) => {
        if (statusCode == 200) return body;
        if (tryNum > 5 || statusCode == 404) return Promise.reject(new Error("\u6B4C\u8BCD\u83B7\u53D6\u5931\u8D25"));
        return this.getText(url, ++tryNum);
      });
    },
    getMrc(url) {
      return this.getText(url).then((text) => {
        return this.parseLyric(decrypt(text));
      });
    },
    getLrc(url) {
      return this.getText(url).then((text) => ({ lxlyric: "", lyric: text }));
    },
    getTrc(url) {
      if (!url) return Promise.resolve("");
      return this.getText(url);
    },
    async getMusicInfo(songInfo) {
      return songInfo.mrcUrl == null ? getMusicInfo(songInfo.copyrightId) : songInfo;
    },
    getLyric(songInfo) {
      return {
        promise: this.getMusicInfo(songInfo).then((info) => {
          let p;
          if (info.mrcUrl) p = this.getMrc(info.mrcUrl);
          else if (info.lrcUrl) p = this.getLrc(info.lrcUrl);
          if (p == null) return Promise.reject(new Error("\u83B7\u53D6\u6B4C\u8BCD\u5931\u8D25"));
          return Promise.all([p, this.getTrc(info.trcUrl)]).then(([lrcInfo, tlyric]) => {
            lrcInfo.tlyric = tlyric;
            return lrcInfo;
          });
        }),
        cancelHttp() {
        }
      };
    }
  };
  var lyric_default = {
    getLyric(songInfo) {
      let requestObj = mrcTools.getLyric(songInfo);
      return requestObj;
    }
  };

  // ../vendor/lx-sdk/mg/hotSearch.js
  var hotSearch_default = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = httpFetch("http://jadeite.migu.cn:7090/music_search/v3/search/hotword");
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== "000000") throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "mg", list: this.filterList(body.data.hotwords[0].hotwordList) };
    },
    filterList(rawList) {
      return rawList.filter((item) => item.resourceType == "song").map((item) => item.word);
    }
  };

  // ../vendor/lx-sdk/mg/comment.js
  var comment_default = {
    _requestObj: null,
    _requestObj2: null,
    _requestObj3: null,
    lastCommentIds: /* @__PURE__ */ new Map(),
    async getComment(musicInfo, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (!musicInfo.songId) {
        let id = await songId_default(musicInfo);
        if (!id) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
        musicInfo.songId = id;
      }
      if (page === 1) this.lastCommentIds.clear();
      const lastCommentId = this.lastCommentIds.get(String(page)) || "";
      if (!lastCommentId && page > 1) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const _requestObj = httpFetch(`https://app.c.nf.migu.cn/MIGUM3.0/user/comment/stack/v1.0?pageSize=${limit}&queryType=1&resourceId=${musicInfo.songId}&resourceType=2&commentId=${lastCommentId}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
          // Referer: 'https://music.migu.cn',
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== "000000") throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const total = parseInt(body.data.commentNums);
      const list = this.filterComment(body.data.comments);
      this.lastCommentIds.set(String(page + 1), list.length ? list[list.length - 1].id : "");
      return { source: "mg", comments: list, total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    async getHotComment(musicInfo, page = 1, limit = 20) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      if (!musicInfo.songId) {
        let id = await songId_default(musicInfo);
        if (!id) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
        musicInfo.songId = id;
      }
      const _requestObj2 = httpFetch(`https://app.c.nf.migu.cn/MIGUM3.0/user/comment/stack/v1.0?pageSize=${limit}&queryType=2&resourceId=${musicInfo.songId}&resourceType=2&hotCommentStart=${(page - 1) * limit}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
          // Referer: 'https://music.migu.cn',
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code !== "000000") throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const total = parseInt(body.data.cfgHotCount);
      return { source: "mg", comments: this.filterComment(body.data.hotComments), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    async getReplyComment(musicInfo, replyId, page = 1, limit = 10) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      const _requestObj2 = httpFetch(`https://app.c.nf.migu.cn/MIGUM3.0/user/comment/stack/${replyId}/v1.0?pageSize=${limit}&queryType=2&resourceId=${musicInfo.songId}&resourceType=2&start=${(page - 1) * limit}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code !== "000000") throw new Error("\u83B7\u53D6\u56DE\u590D\u8BC4\u8BBA\u5931\u8D25");
      const total = parseInt(body.data.replyTotalCount);
      return { source: "mg", comments: this.filterComment(body.data.mainCommentItem.replyComments), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    filterComment(rawList) {
      return rawList.map((item) => ({
        id: item.commentId,
        text: item.commentInfo,
        time: item.commentTime,
        timeStr: dateFormat2(new Date(item.commentTime).getTime()),
        userName: item.user.nickName,
        avatar: item.user.middleIcon || item.user.bigIcon || item.user.smallIcon,
        userId: item.user.userId,
        likedCount: item.opNumItem.thumbNum,
        replyNum: item.replyTotalCount,
        reply: item.replyComments.map((c) => ({
          id: c.replyId,
          text: c.replyInfo,
          time: c.replyTime,
          timeStr: dateFormat2(new Date(c.replyTime).getTime()),
          userName: c.user.nickName,
          avatar: c.user.middleIcon || c.user.bigIcon || c.user.smallIcon,
          userId: c.user.userId,
          likedCount: null,
          replyNum: null
        }))
      }));
    }
  };

  // ../vendor/lx-sdk/mg/index.js
  var mg = {
    // tipSearch,
    songList: songList_default,
    musicSearch: musicSearch_default,
    leaderboard: leaderboard_default,
    hotSearch: hotSearch_default,
    comment: comment_default,
    getMusicUrl(songInfo, type) {
      return apis("mg").getMusicUrl(songInfo, type);
    },
    getLyric(songInfo) {
      return lyric_default.getLyric(songInfo);
    },
    getPic(songInfo) {
      return pic_default.getPic(songInfo);
    },
    getMusicDetailPageUrl(songInfo) {
      return `http://music.migu.cn/v3/music/song/${songInfo.copyrightId}`;
    }
  };
  var index_default = mg;
  return __toCommonJS(index_exports);
})();
globalThis.__lyn_source_mg = (typeof __lynSource !== 'undefined' && __lynSource.default) ? __lynSource.default : __lynSource;
