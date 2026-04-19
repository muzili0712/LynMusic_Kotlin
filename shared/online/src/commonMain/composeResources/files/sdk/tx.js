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

  // ../vendor/lx-sdk/tx/index.js
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
  var b64DecodeUnicode = (str) => {
    const bytes = globalThis.lyn.base64Decode(str);
    return globalThis.lyn.bufferToString(bytes, "utf8");
  };

  // ../vendor/lx-sdk/shared/utils.js
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

  // ../vendor/lx-sdk/tx/leaderboard.js
  var boardList = [{ id: "tx__4", name: "\u6D41\u884C\u6307\u6570\u699C", bangid: "4" }, { id: "tx__26", name: "\u70ED\u6B4C\u699C", bangid: "26" }, { id: "tx__27", name: "\u65B0\u6B4C\u699C", bangid: "27" }, { id: "tx__62", name: "\u98D9\u5347\u699C", bangid: "62" }, { id: "tx__58", name: "\u8BF4\u5531\u699C", bangid: "58" }, { id: "tx__57", name: "\u559C\u529B\u7535\u97F3\u699C", bangid: "57" }, { id: "tx__28", name: "\u7F51\u7EDC\u6B4C\u66F2\u699C", bangid: "28" }, { id: "tx__5", name: "\u5185\u5730\u699C", bangid: "5" }, { id: "tx__3", name: "\u6B27\u7F8E\u699C", bangid: "3" }, { id: "tx__59", name: "\u9999\u6E2F\u5730\u533A\u699C", bangid: "59" }, { id: "tx__16", name: "\u97E9\u56FD\u699C", bangid: "16" }, { id: "tx__60", name: "\u6296\u5FEB\u699C", bangid: "60" }, { id: "tx__29", name: "\u5F71\u89C6\u91D1\u66F2\u699C", bangid: "29" }, { id: "tx__17", name: "\u65E5\u672C\u699C", bangid: "17" }, { id: "tx__52", name: "\u817E\u8BAF\u97F3\u4E50\u4EBA\u539F\u521B\u699C", bangid: "52" }, { id: "tx__36", name: "K\u6B4C\u91D1\u66F2\u699C", bangid: "36" }, { id: "tx__61", name: "\u53F0\u6E7E\u5730\u533A\u699C", bangid: "61" }, { id: "tx__63", name: "DJ\u821E\u66F2\u699C", bangid: "63" }, { id: "tx__64", name: "\u7EFC\u827A\u65B0\u6B4C\u699C", bangid: "64" }, { id: "tx__65", name: "\u56FD\u98CE\u70ED\u6B4C\u699C", bangid: "65" }, { id: "tx__67", name: "\u542C\u6B4C\u8BC6\u66F2\u699C", bangid: "67" }, { id: "tx__72", name: "\u52A8\u6F2B\u97F3\u4E50\u699C", bangid: "72" }, { id: "tx__73", name: "\u6E38\u620F\u97F3\u4E50\u699C", bangid: "73" }, { id: "tx__75", name: "\u6709\u58F0\u699C", bangid: "75" }, { id: "tx__131", name: "\u6821\u56ED\u97F3\u4E50\u4EBA\u6392\u884C\u699C", bangid: "131" }];
  var leaderboard_default = {
    limit: 300,
    list: [
      {
        id: "txlxzsb",
        name: "\u6D41\u884C\u699C",
        bangid: 4
      },
      {
        id: "txrgb",
        name: "\u70ED\u6B4C\u699C",
        bangid: 26
      },
      {
        id: "txwlhgb",
        name: "\u7F51\u7EDC\u699C",
        bangid: 28
      },
      {
        id: "txdyb",
        name: "\u6296\u97F3\u699C",
        bangid: 60
      },
      {
        id: "txndb",
        name: "\u5185\u5730\u699C",
        bangid: 5
      },
      {
        id: "txxgb",
        name: "\u9999\u6E2F\u699C",
        bangid: 59
      },
      {
        id: "txtwb",
        name: "\u53F0\u6E7E\u699C",
        bangid: 61
      },
      {
        id: "txoumb",
        name: "\u6B27\u7F8E\u699C",
        bangid: 3
      },
      {
        id: "txhgb",
        name: "\u97E9\u56FD\u699C",
        bangid: 16
      },
      {
        id: "txrbb",
        name: "\u65E5\u672C\u699C",
        bangid: 17
      },
      {
        id: "txtybb",
        name: "YouTube\u699C",
        bangid: 128
      }
    ],
    listDetailRequest(id, period, limit) {
      return httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
        method: "post",
        headers: {
          "User-Agent": "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)"
        },
        body: {
          toplist: {
            module: "musicToplist.ToplistInfoServer",
            method: "GetDetail",
            param: {
              topid: id,
              num: limit,
              period
            }
          },
          comm: {
            uin: 0,
            format: "json",
            ct: 20,
            cv: 1859
          }
        }
      }).promise;
    },
    regExps: {
      periodList: /<i class="play_cover__btn c_tx_link js_icon_play" data-listkey=".+?" data-listname=".+?" data-tid=".+?" data-date=".+?" .+?<\/i>/g,
      period: /data-listname="(.+?)" data-tid=".*?\/(.+?)" data-date="(.+?)" .+?<\/i>/
    },
    periods: {},
    periodUrl: "https://c.y.qq.com/node/pc/wk_v15/top.html",
    _requestBoardsObj: null,
    getBoardsData() {
      if (this._requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this._requestBoardsObj = httpFetch("https://c.y.qq.com/v8/fcg-bin/fcg_myqq_toplist.fcg?g_tk=1928093487&inCharset=utf-8&outCharset=utf-8&notice=0&format=json&uin=0&needNewCode=1&platform=h5");
      return this._requestBoardsObj.promise;
    },
    getData(url) {
      const requestDataObj = httpFetch(url);
      return requestDataObj.promise;
    },
    filterData(rawList) {
      return rawList.map((item) => {
        let types = [];
        let _types = {};
        if (item.file.size_128mp3 !== 0) {
          let size = sizeFormate(item.file.size_128mp3);
          types.push({ type: "128k", size });
          _types["128k"] = {
            size
          };
        }
        if (item.file.size_320mp3 !== 0) {
          let size = sizeFormate(item.file.size_320mp3);
          types.push({ type: "320k", size });
          _types["320k"] = {
            size
          };
        }
        if (item.file.size_flac !== 0) {
          let size = sizeFormate(item.file.size_flac);
          types.push({ type: "flac", size });
          _types.flac = {
            size
          };
        }
        if (item.file.size_hires !== 0) {
          let size = sizeFormate(item.file.size_hires);
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        return {
          singer: formatSingerName(item.singer, "name"),
          name: item.title,
          albumName: item.album.name,
          albumId: item.album.mid,
          source: "tx",
          interval: formatPlayTime(item.interval),
          songId: item.id,
          albumMid: item.album.mid,
          strMediaMid: item.file.media_mid,
          songmid: item.mid,
          img: item.album.name === "" || item.album.name === "\u7A7A" ? item.singer?.length ? `https://y.gtimg.cn/music/photo_new/T001R500x500M000${item.singer[0].mid}.jpg` : "" : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${item.album.mid}.jpg`,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    getPeriods(bangid) {
      return this.getData(this.periodUrl).then(({ body: html }) => {
        let result = html.match(this.regExps.periodList);
        if (!result) return Promise.reject(new Error("get data failed"));
        result.forEach((item) => {
          let result2 = item.match(this.regExps.period);
          if (!result2) return;
          this.periods[result2[2]] = {
            name: result2[1],
            bangid: result2[2],
            period: result2[3]
          };
        });
        const info = this.periods[bangid];
        return info && info.period;
      });
    },
    filterBoardsData(rawList) {
      let list = [];
      for (const board of rawList) {
        if (board.id == 201) continue;
        if (board.topTitle.startsWith("\u5DC5\u5CF0\u699C\xB7")) {
          board.topTitle = board.topTitle.substring(4, board.topTitle.length);
        }
        if (!board.topTitle.endsWith("\u699C")) board.topTitle += "\u699C";
        list.push({
          id: "tx__" + board.id,
          name: board.topTitle,
          bangid: String(board.id)
        });
      }
      return list;
    },
    async getBoards(retryNum = 0) {
      this.list = boardList;
      return {
        list: boardList,
        source: "tx"
      };
    },
    getList(bangid, page, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      bangid = parseInt(bangid);
      let info = this.periods[bangid];
      let p = info ? Promise.resolve(info.period) : this.getPeriods(bangid);
      return p.then((period) => {
        return this.listDetailRequest(bangid, period, this.limit).then((resp) => {
          if (resp.body.code !== 0) return this.getList(bangid, page, retryNum);
          return {
            total: resp.body.toplist.data.songInfoList.length,
            list: this.filterData(resp.body.toplist.data.songInfoList),
            limit: this.limit,
            page: 1,
            source: "tx"
          };
        });
      });
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") id = id.replace("tx__", "");
      return `https://y.qq.com/n/ryqq/toplist/${id}`;
    }
  };

  // ../vendor/lx-sdk/tx/lyric.js
  var lyric_default = {
    regexps: {
      matchLrc: /.+"lyric":"([\w=+/]*)".+/
    },
    getLyric(songmid) {
      const requestObj = httpFetch(`https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?songmid=${songmid}&g_tk=5381&loginUin=0&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&platform=yqq`, {
        headers: {
          Referer: "https://y.qq.com/portal/player.html"
        }
      });
      requestObj.promise = requestObj.promise.then(({ body }) => {
        if (body.code != 0 || !body.lyric) return Promise.reject(new Error("Get lyric failed"));
        return {
          lyric: decodeName(b64DecodeUnicode(body.lyric)),
          tlyric: decodeName(b64DecodeUnicode(body.trans))
        };
      });
      return requestObj;
    }
  };

  // ../vendor/lx-sdk/tx/songList.js
  var songList_default = {
    _requestObj_tags: null,
    _requestObj_hotTags: null,
    _requestObj_list: null,
    limit_list: 36,
    limit_song: 1e5,
    successCode: 0,
    sortList: [
      {
        name: "\u6700\u70ED",
        tid: "hot",
        id: 5
      },
      {
        name: "\u6700\u65B0",
        tid: "new",
        id: 2
      }
    ],
    regExps: {
      hotTagHtml: /class="c_bg_link js_tag_item" data-id="\w+">.+?<\/a>/g,
      hotTag: /data-id="(\w+)">(.+?)<\/a>/,
      // https://y.qq.com/n/yqq/playlist/7217720898.html
      // https://i.y.qq.com/n2/m/share/details/taoge.html?platform=11&appshare=android_qq&appversion=9050006&id=7217720898&ADTAG=qfshare
      listDetailLink: /\/playlist\/(\d+)/,
      listDetailLink2: /id=(\d+)/
    },
    tagsUrl: "https://u.y.qq.com/cgi-bin/musicu.fcg?loginUin=0&hostUin=0&format=json&inCharset=utf-8&outCharset=utf-8&notice=0&platform=wk_v15.json&needNewCode=0&data=%7B%22tags%22%3A%7B%22method%22%3A%22get_all_categories%22%2C%22param%22%3A%7B%22qq%22%3A%22%22%7D%2C%22module%22%3A%22playlist.PlaylistAllCategoriesServer%22%7D%7D",
    hotTagUrl: "https://c.y.qq.com/node/pc/wk_v15/category_playlist.html",
    getListUrl(sortId, id, page) {
      if (id) {
        id = parseInt(id);
        return `https://u.y.qq.com/cgi-bin/musicu.fcg?loginUin=0&hostUin=0&format=json&inCharset=utf-8&outCharset=utf-8&notice=0&platform=wk_v15.json&needNewCode=0&data=${encodeURIComponent(JSON.stringify({
          comm: { cv: 1602, ct: 20 },
          playlist: {
            method: "get_category_content",
            param: {
              titleid: id,
              caller: "0",
              category_id: id,
              size: this.limit_list,
              page: page - 1,
              use_page: 1
            },
            module: "playlist.PlayListCategoryServer"
          }
        }))}`;
      }
      return `https://u.y.qq.com/cgi-bin/musicu.fcg?loginUin=0&hostUin=0&format=json&inCharset=utf-8&outCharset=utf-8&notice=0&platform=wk_v15.json&needNewCode=0&data=${encodeURIComponent(JSON.stringify({
        comm: { cv: 1602, ct: 20 },
        playlist: {
          method: "get_playlist_by_tag",
          param: { id: 1e7, sin: this.limit_list * (page - 1), size: this.limit_list, order: sortId, cur_page: page },
          module: "playlist.PlayListPlazaServer"
        }
      }))}`;
    },
    getListDetailUrl(id) {
      return `https://c.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg?type=1&json=1&utf8=1&onlysong=0&new_format=1&disstid=${id}&loginUin=0&hostUin=0&format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq.json&needNewCode=0`;
    },
    // http://nplserver.kuwo.cn/pl.svc?op=getlistinfo&pid=2849349915&pn=0&rn=100&encode=utf8&keyset=pl2012&identity=kuwo&pcmp4=1&vipver=MUSIC_9.0.5.0_W1&newver=1
    // 获取标签
    getTag(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch(this.tagsUrl);
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getTag(++tryNum);
        return this.filterTagInfo(body.tags.data.v_group);
      });
    },
    // 获取标签
    getHotTag(tryNum = 0) {
      if (this._requestObj_hotTags) this._requestObj_hotTags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_hotTags = httpFetch(this.hotTagUrl);
      return this._requestObj_hotTags.promise.then(({ statusCode, body }) => {
        if (statusCode !== 200) return this.getHotTag(++tryNum);
        return this.filterInfoHotTag(body);
      });
    },
    filterInfoHotTag(html) {
      let hotTag = html.match(this.regExps.hotTagHtml);
      const hotTags = [];
      if (!hotTag) return hotTags;
      hotTag.forEach((tagHtml) => {
        let result = tagHtml.match(this.regExps.hotTag);
        if (!result) return;
        hotTags.push({
          id: parseInt(result[1]),
          name: result[2],
          source: "tx"
        });
      });
      return hotTags;
    },
    filterTagInfo(rawList) {
      return rawList.map((type) => ({
        name: type.group_name,
        list: type.v_item.map((item) => ({
          parent_id: type.group_id,
          parent_name: type.group_name,
          id: item.id,
          name: item.name,
          source: "tx"
        }))
      }));
    },
    // 获取列表数据
    getList(sortId, tagId, page, tryNum = 0) {
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_list = httpFetch(
        this.getListUrl(sortId, tagId, page)
      );
      return this._requestObj_list.promise.then(({ body }) => {
        if (body.code !== this.successCode) return this.getList(sortId, tagId, page, ++tryNum);
        return tagId ? this.filterList2(body.playlist.data, page) : this.filterList(body.playlist.data, page);
      });
    },
    filterList(data, page) {
      return {
        list: data.v_playlist.map((item) => ({
          play_count: formatPlayCount(item.access_num),
          id: String(item.tid),
          author: item.creator_info.nick,
          name: item.title,
          time: item.modify_time ? dateFormat(item.modify_time * 1e3, "Y-M-D") : "",
          img: item.cover_url_medium,
          // grade: item.favorcnt / 10,
          total: item.song_ids?.length,
          desc: decodeName(item.desc).replace(/<br>/g, "\n"),
          source: "tx"
        })),
        total: data.total,
        page,
        limit: this.limit_list,
        source: "tx"
      };
    },
    filterList2({ content }, page) {
      return {
        list: content.v_item.map(({ basic }) => ({
          play_count: formatPlayCount(basic.play_cnt),
          id: String(basic.tid),
          author: basic.creator.nick,
          name: basic.title,
          // time: basic.publish_time,
          img: basic.cover.medium_url || basic.cover.default_url,
          // grade: basic.favorcnt / 10,
          desc: decodeName(basic.desc).replace(/<br>/g, "\n"),
          source: "tx"
        })),
        total: content.total_cnt,
        page,
        limit: this.limit_list,
        source: "tx"
      };
    },
    async handleParseId(link, retryNum = 0) {
      if (retryNum > 2) return Promise.reject(new Error("link try max num"));
      const requestObj_listDetailLink = httpFetch(link);
      const { url, statusCode } = await requestObj_listDetailLink.promise;
      if (statusCode > 400) return this.handleParseId(link, ++retryNum);
      return url;
    },
    async getListId(id) {
      if (/[?&:/]/.test(id)) {
        if (!this.regExps.listDetailLink.test(id)) {
          id = await this.handleParseId(id);
        }
        let result = this.regExps.listDetailLink.exec(id);
        if (!result) {
          result = this.regExps.listDetailLink2.exec(id);
          if (!result) throw new Error("failed");
        }
        id = result[1];
      }
      return id;
    },
    // 获取歌曲列表内的音乐
    async getListDetail(id, tryNum = 0) {
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      id = await this.getListId(id);
      const requestObj_listDetail = httpFetch(this.getListDetailUrl(id), {
        headers: {
          Origin: "https://y.qq.com",
          Referer: `https://y.qq.com/n/yqq/playsquare/${id}.html`
        }
      });
      const { body } = await requestObj_listDetail.promise;
      if (body.code !== this.successCode) return this.getListDetail(id, ++tryNum);
      const cdlist = body.cdlist[0];
      return {
        list: this.filterListDetail(cdlist.songlist),
        page: 1,
        limit: cdlist.songlist.length + 1,
        total: cdlist.songlist.length,
        source: "tx",
        info: {
          name: cdlist.dissname,
          img: cdlist.logo,
          desc: decodeName(cdlist.desc).replace(/<br>/g, "\n"),
          author: cdlist.nickname,
          play_count: formatPlayCount(cdlist.visitnum)
        }
      };
    },
    filterListDetail(rawList) {
      return rawList.map((item) => {
        let types = [];
        let _types = {};
        if (item.file.size_128mp3 !== 0) {
          let size = sizeFormate(item.file.size_128mp3);
          types.push({ type: "128k", size });
          _types["128k"] = {
            size
          };
        }
        if (item.file.size_320mp3 !== 0) {
          let size = sizeFormate(item.file.size_320mp3);
          types.push({ type: "320k", size });
          _types["320k"] = {
            size
          };
        }
        if (item.file.size_flac !== 0) {
          let size = sizeFormate(item.file.size_flac);
          types.push({ type: "flac", size });
          _types.flac = {
            size
          };
        }
        if (item.file.size_hires !== 0) {
          let size = sizeFormate(item.file.size_hires);
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        return {
          singer: formatSingerName(item.singer, "name"),
          name: item.title,
          albumName: item.album.name,
          albumId: item.album.mid,
          source: "tx",
          interval: formatPlayTime(item.interval),
          songId: item.id,
          albumMid: item.album.mid,
          strMediaMid: item.file.media_mid,
          songmid: item.mid,
          img: item.album.name === "" || item.album.name === "\u7A7A" ? item.singer?.length ? `https://y.gtimg.cn/music/photo_new/T001R500x500M000${item.singer[0].mid}.jpg` : "" : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${item.album.mid}.jpg`,
          lrc: null,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    getTags() {
      return Promise.all([this.getTag(), this.getHotTag()]).then(([tags, hotTag]) => ({ tags, hotTag, source: "tx" }));
    },
    async getDetailPageUrl(id) {
      id = await this.getListId(id);
      return `https://y.qq.com/n/ryqq/playlist/${id}`;
    },
    search(text, page, limit = 20, retryNum = 0) {
      if (retryNum > 5) throw new Error("max retry");
      return httpFetch(`http://c.y.qq.com/soso/fcgi-bin/client_music_search_songlist?page_no=${page - 1}&num_per_page=${limit}&format=json&query=${encodeURIComponent(text)}&remoteplace=txt.yqq.playlist&inCharset=utf8&outCharset=utf-8`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
          Referer: "http://y.qq.com/portal/search.html"
        }
      }).promise.then(({ body }) => {
        if (body.code != 0) return this.search(text, page, limit, ++retryNum);
        return {
          list: body.data.list.map((item) => {
            return {
              play_count: formatPlayCount(item.listennum),
              id: String(item.dissid),
              author: decodeName(item.creator.name),
              name: decodeName(item.dissname),
              time: dateFormat(item.createtime, "Y-M-D"),
              img: item.imgurl,
              // grade: item.favorcnt / 10,
              total: item.song_count,
              desc: decodeName(decodeName(item.introduction)).replace(/<br>/g, "\n"),
              source: "tx"
            };
          }),
          limit,
          total: body.data.sum,
          source: "tx"
        };
      });
    }
  };

  // lyn-shim:crypto-native
  var hashSHA1 = (input) => globalThis.lyn.sha1(input);

  // ../vendor/lx-sdk/tx/utils/crypto.js
  var PART_1_INDEXES = [23, 14, 6, 36, 16, 40, 7, 19];
  var PART_2_INDEXES = [16, 1, 32, 12, 19, 27, 8, 5];
  var SCRAMBLE_VALUES = [89, 39, 179, 150, 218, 82, 58, 252, 177, 52, 186, 123, 120, 64, 242, 133, 143, 161, 121, 179];
  function pickHashByIdx(hash, indexes) {
    return indexes.map((idx) => hash[idx]).join("");
  }
  function base64Encode(data) {
    return Buffer2.from(data).toString("base64").replace(/[\\/+=]/g, "");
  }
  async function zzcSign(text) {
    const hash = await hashSHA1(text);
    const part1 = pickHashByIdx(hash, PART_1_INDEXES);
    const part2 = pickHashByIdx(hash, PART_2_INDEXES);
    const part3 = SCRAMBLE_VALUES.map((value, i) => value ^ parseInt(hash.slice(i * 2, i * 2 + 2), 16));
    const b64Part = base64Encode(part3).replace(/[\\/+=]/g, "");
    return `zzc${part1}${b64Part}${part2}`.toLowerCase();
  }

  // ../vendor/lx-sdk/tx/utils/index.js
  var signRequest = async (data) => {
    const sign = await zzcSign(JSON.stringify(data));
    return httpFetch(`https://u.y.qq.com/cgi-bin/musics.fcg?sign=${sign}`, {
      method: "post",
      headers: {
        "User-Agent": "QQMusic 14090508(android 12)"
      },
      body: data
    }).promise;
  };

  // ../vendor/lx-sdk/tx/musicSearch.js
  var musicSearch_default = {
    limit: 50,
    total: 0,
    page: 0,
    allPage: 1,
    successCode: 0,
    musicSearch(str, page, limit, retryNum = 0) {
      if (retryNum > 5) return Promise.reject(new Error("\u641C\u7D22\u5931\u8D25"));
      const searchRequest = signRequest({
        comm: {
          ct: "11",
          cv: "14090508",
          v: "14090508",
          tmeAppID: "qqmusic",
          phonetype: "EBG-AN10",
          deviceScore: "553.47",
          devicelevel: "50",
          newdevicelevel: "20",
          rom: "HuaWei/EMOTION/EmotionUI_14.2.0",
          os_ver: "12",
          OpenUDID: "0",
          OpenUDID2: "0",
          QIMEI36: "0",
          udid: "0",
          chid: "0",
          aid: "0",
          oaid: "0",
          taid: "0",
          tid: "0",
          wid: "0",
          uid: "0",
          sid: "0",
          modeSwitch: "6",
          teenMode: "0",
          ui_mode: "2",
          nettype: "1020",
          v4ip: ""
        },
        req: {
          module: "music.search.SearchCgiService",
          method: "DoSearchForQQMusicMobile",
          param: {
            search_type: 0,
            searchid: Math.random().toString().slice(2),
            query: str,
            page_num: page,
            num_per_page: limit,
            highlight: 0,
            nqc_flag: 0,
            multi_zhida: 0,
            cat: 2,
            grp: 1,
            sin: 0,
            sem: 0
          }
        }
      });
      return searchRequest.then(({ body }) => {
        if (!body || !body.req || body.code != this.successCode || body.req.code != this.successCode) {
          return this.musicSearch(str, page, limit, ++retryNum);
        }
        return body.req.data;
      });
    },
    // randomInt(min, max) {
    //   return Math.floor(Math.random() * (max - min + 1)) + min
    // },
    // getSearchId() {
    //   const e = BigInt(this.randomInt(1, 20))
    //   const t = e * 18014398509481984n
    //   const n = BigInt(this.randomInt(0, 4194304)) * 4294967296n
    //   const a = BigInt(Date.now())
    //   const r = (a * 1000n) % (24n * 60n * 60n * 1000n)
    //   return String(t + n + r)
    // },
    handleResult(rawList) {
      if (!rawList || !Array.isArray(rawList)) return [];
      const list = [];
      rawList.forEach((item) => {
        if (!item.file?.media_mid) return;
        let types = [];
        let _types = {};
        const file = item.file;
        if (file.size_128mp3 != 0) {
          let size = sizeFormate(file.size_128mp3);
          types.push({ type: "128k", size });
          _types["128k"] = {
            size
          };
        }
        if (file.size_320mp3 !== 0) {
          let size = sizeFormate(file.size_320mp3);
          types.push({ type: "320k", size });
          _types["320k"] = {
            size
          };
        }
        if (file.size_flac !== 0) {
          let size = sizeFormate(file.size_flac);
          types.push({ type: "flac", size });
          _types.flac = {
            size
          };
        }
        if (file.size_hires !== 0) {
          let size = sizeFormate(file.size_hires);
          types.push({ type: "flac24bit", size });
          _types.flac24bit = {
            size
          };
        }
        let albumId = "";
        let albumName = "";
        if (item.album) {
          albumName = item.album.name;
          albumId = item.album.mid;
        }
        list.push({
          singer: formatSingerName(item.singer, "name"),
          name: item.name + (item.title_extra ?? ""),
          albumName,
          albumId,
          source: "tx",
          interval: formatPlayTime(item.interval),
          songId: item.id,
          albumMid: item.album?.mid ?? "",
          strMediaMid: item.file.media_mid,
          songmid: item.mid,
          img: albumId === "" || albumId === "\u7A7A" ? item.singer?.length ? `https://y.gtimg.cn/music/photo_new/T001R500x500M000${item.singer[0].mid}.jpg` : "" : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${albumId}.jpg`,
          types,
          _types,
          typeUrl: {}
        });
      });
      return list;
    },
    search(str, page = 1, limit) {
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then(({ body, meta }) => {
        let list = this.handleResult(body.item_song);
        this.total = meta.estimate_sum;
        this.page = page;
        this.allPage = Math.ceil(this.total / limit);
        return Promise.resolve({
          list,
          allPage: this.allPage,
          limit,
          total: this.total,
          source: "tx"
        });
      });
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

  // ../vendor/lx-sdk/tx/hotSearch.js
  var hotSearch_default = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
        method: "post",
        body: {
          comm: {
            ct: "19",
            cv: "1803",
            guid: "0",
            patch: "118",
            psrf_access_token_expiresAt: 0,
            psrf_qqaccess_token: "",
            psrf_qqopenid: "",
            psrf_qqunionid: "",
            tmeAppID: "qqmusic",
            tmeLoginType: 0,
            uin: "0",
            wid: "0"
          },
          hotkey: {
            method: "GetHotkeyForQQMusicPC",
            module: "tencent_musicsoso_hotkey.HotkeyService",
            param: {
              search_id: "",
              uin: 0
            }
          }
        },
        headers: {
          Referer: "https://y.qq.com/portal/player.html"
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== 0) throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "tx", list: this.filterList(body.hotkey.data.vec_hotkey) };
    },
    filterList(rawList) {
      return rawList.map((item) => item.query);
    }
  };

  // ../vendor/lx-sdk/tx/musicInfo.js
  var getSinger = (singers) => {
    let arr = [];
    singers.forEach((singer) => {
      arr.push(singer.name);
    });
    return arr.join("\u3001");
  };
  var musicInfo_default = (songmid) => {
    const requestObj = httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
      method: "post",
      headers: {
        "User-Agent": "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)"
      },
      body: {
        comm: {
          ct: "19",
          cv: "1859",
          uin: "0"
        },
        req: {
          module: "music.pf_song_detail_svr",
          method: "get_song_detail_yqq",
          param: {
            song_type: 0,
            song_mid: songmid
          }
        }
      }
    });
    return requestObj.promise.then(({ body }) => {
      if (body.code != 0 || body.req.code != 0) return Promise.reject(new Error("\u83B7\u53D6\u6B4C\u66F2\u4FE1\u606F\u5931\u8D25"));
      const item = body.req.data.track_info;
      if (!item.file?.media_mid) return null;
      let types = [];
      let _types = {};
      const file = item.file;
      if (file.size_128mp3 != 0) {
        let size = sizeFormate(file.size_128mp3);
        types.push({ type: "128k", size });
        _types["128k"] = {
          size
        };
      }
      if (file.size_320mp3 !== 0) {
        let size = sizeFormate(file.size_320mp3);
        types.push({ type: "320k", size });
        _types["320k"] = {
          size
        };
      }
      if (file.size_flac !== 0) {
        let size = sizeFormate(file.size_flac);
        types.push({ type: "flac", size });
        _types.flac = {
          size
        };
      }
      if (file.size_hires !== 0) {
        let size = sizeFormate(file.size_hires);
        types.push({ type: "flac24bit", size });
        _types.flac24bit = {
          size
        };
      }
      let albumId = "";
      let albumName = "";
      if (item.album) {
        albumName = item.album.name;
        albumId = item.album.mid;
      }
      return {
        singer: getSinger(item.singer),
        name: item.title,
        albumName,
        albumId,
        source: "tx",
        interval: formatPlayTime(item.interval),
        songId: item.id,
        albumMid: item.album?.mid ?? "",
        strMediaMid: item.file.media_mid,
        songmid: item.mid,
        img: albumId === "" || albumId === "\u7A7A" ? item.singer?.length ? `https://y.gtimg.cn/music/photo_new/T001R500x500M000${item.singer[0].mid}.jpg` : "" : `https://y.gtimg.cn/music/photo_new/T002R500x500M000${albumId}.jpg`,
        types,
        _types,
        typeUrl: {}
      };
    });
  };

  // ../vendor/lx-sdk/tx/comment.js
  var emojis = {
    e400846: "\u{1F618}",
    e400874: "\u{1F634}",
    e400825: "\u{1F603}",
    e400847: "\u{1F619}",
    e400835: "\u{1F60D}",
    e400873: "\u{1F633}",
    e400836: "\u{1F60E}",
    e400867: "\u{1F62D}",
    e400832: "\u{1F60A}",
    e400837: "\u{1F60F}",
    e400875: "\u{1F62B}",
    e400831: "\u{1F609}",
    e400855: "\u{1F621}",
    e400823: "\u{1F604}",
    e400862: "\u{1F628}",
    e400844: "\u{1F616}",
    e400841: "\u{1F613}",
    e400830: "\u{1F608}",
    e400828: "\u{1F606}",
    e400833: "\u{1F60B}",
    e400822: "\u{1F600}",
    e400843: "\u{1F615}",
    e400829: "\u{1F607}",
    e400824: "\u{1F602}",
    e400834: "\u{1F60C}",
    e400877: "\u{1F637}",
    e400132: "\u{1F349}",
    e400181: "\u{1F37A}",
    e401067: "\u2615\uFE0F",
    e400186: "\u{1F967}",
    e400343: "\u{1F437}",
    e400116: "\u{1F339}",
    e400126: "\u{1F343}",
    e400613: "\u{1F48B}",
    e401236: "\u2764\uFE0F",
    e400622: "\u{1F494}",
    e400637: "\u{1F4A3}",
    e400643: "\u{1F4A9}",
    e400773: "\u{1F52A}",
    e400102: "\u{1F31B}",
    e401328: "\u{1F31E}",
    e400420: "\u{1F44F}",
    e400914: "\u{1F64C}",
    e400408: "\u{1F44D}",
    e400414: "\u{1F44E}",
    e401121: "\u270B",
    e400396: "\u{1F44B}",
    e400384: "\u{1F449}",
    e401115: "\u270A",
    e400402: "\u{1F44C}",
    e400905: "\u{1F648}",
    e400906: "\u{1F649}",
    e400907: "\u{1F64A}",
    e400562: "\u{1F47B}",
    e400932: "\u{1F64F}",
    e400644: "\u{1F4AA}",
    e400611: "\u{1F489}",
    e400185: "\u{1F381}",
    e400655: "\u{1F4B0}",
    e400325: "\u{1F425}",
    e400612: "\u{1F48A}",
    e400198: "\u{1F389}",
    e401685: "\u26A1\uFE0F",
    e400631: "\u{1F49D}",
    e400768: "\u{1F525}",
    e400432: "\u{1F451}"
  };
  var songIdMap = /* @__PURE__ */ new Map();
  var promises = /* @__PURE__ */ new Map();
  var comment_default = {
    _requestObj: null,
    _requestObj2: null,
    async getSongId({ songId, songmid }) {
      if (songId) return songId;
      if (songIdMap.has(songmid)) return songIdMap.get(songmid);
      if (promises.has(songmid)) return (await promises.get(songmid)).songId;
      const promise = musicInfo_default(songmid);
      promises.set(promise);
      const info = await promise;
      songIdMap.set(songmid, info.songId);
      promises.delete(songmid);
      return info.songId;
    },
    async getComment(mInfo, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      const songId = await this.getSongId(mInfo);
      const _requestObj = httpFetch("http://c.y.qq.com/base/fcgi-bin/fcg_global_comment_h5.fcg", {
        method: "POST",
        headers: {
          "User-Agent": "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)"
        },
        form: {
          uin: "0",
          format: "json",
          cid: "205360772",
          reqtype: "2",
          biztype: "1",
          topid: songId,
          cmd: "8",
          needmusiccrit: "1",
          pagenum: page - 1,
          pagesize: limit
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.code !== 0) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const comment = body.comment;
      return {
        source: "tx",
        comments: this.filterNewComment(comment.commentlist),
        total: comment.commenttotal,
        page,
        limit,
        maxPage: Math.ceil(comment.commenttotal / limit) || 1
      };
    },
    async getHotComment(mInfo, page = 1, limit = 20) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      const songId = await this.getSongId(mInfo);
      const _requestObj2 = httpFetch("https://u.y.qq.com/cgi-bin/musicu.fcg", {
        method: "POST",
        body: {
          comm: {
            cv: 4747474,
            ct: 24,
            format: "json",
            inCharset: "utf-8",
            outCharset: "utf-8",
            notice: 0,
            platform: "yqq.json",
            needNewCode: 1,
            uin: 0
          },
          req: {
            module: "music.globalComment.CommentRead",
            method: "GetHotCommentList",
            param: {
              BizType: 1,
              BizId: String(songId),
              LastCommentSeqNo: "",
              PageSize: limit,
              PageNum: page - 1,
              HotType: 1,
              WithAirborne: 0,
              PicEnable: 1
            }
          }
        },
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36 Edg/113.0.0.0",
          referer: "https://y.qq.com/",
          origin: "https://y.qq.com"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.code !== 0 || body.req.code !== 0) throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const comment = body.req.data.CommentList;
      return {
        source: "tx",
        comments: this.filterHotComment(comment.Comments),
        total: comment.Total,
        page,
        limit,
        maxPage: Math.ceil(comment.Total / limit) || 1
      };
    },
    filterNewComment(rawList) {
      return rawList.map((item) => {
        let time = this.formatTime(item.time);
        let timeStr = time ? dateFormat2(time) : null;
        if (item.middlecommentcontent) {
          let firstItem = item.middlecommentcontent[0];
          firstItem.avatarurl = item.avatarurl;
          firstItem.praisenum = item.praisenum;
          item.avatarurl = null;
          item.praisenum = null;
          item.middlecommentcontent.reverse();
        }
        return {
          id: `${item.rootcommentid}_${item.commentid}`,
          rootId: item.rootcommentid,
          text: item.rootcommentcontent ? this.replaceEmoji(item.rootcommentcontent).replace(/\\n/g, "\n") : "",
          time: item.rootcommentid == item.commentid ? time : null,
          timeStr: item.rootcommentid == item.commentid ? timeStr : null,
          userName: item.rootcommentnick ? item.rootcommentnick.substring(1) : "",
          avatar: item.avatarurl,
          userId: item.encrypt_rootcommentuin,
          likedCount: item.praisenum,
          reply: item.middlecommentcontent ? item.middlecommentcontent.map((c) => {
            return {
              id: `sub_${item.rootcommentid}_${c.subcommentid}`,
              text: this.replaceEmoji(c.subcommentcontent).replace(/\\n/g, "\n"),
              time: c.subcommentid == item.commentid ? time : null,
              timeStr: c.subcommentid == item.commentid ? timeStr : null,
              userName: c.replynick.substring(1),
              avatar: c.avatarurl,
              userId: c.encrypt_replyuin,
              likedCount: c.praisenum
            };
          }) : []
        };
      });
    },
    filterHotComment(rawList) {
      return rawList.map((item) => {
        return {
          id: `${item.SeqNo}_${item.CmId}`,
          rootId: item.SeqNo,
          text: item.Content ? this.replaceEmoji(item.Content).replace(/\\n/g, "\n") : "",
          time: item.PubTime ? this.formatTime(item.PubTime) : null,
          timeStr: item.PubTime ? dateFormat2(this.formatTime(item.PubTime)) : null,
          userName: item.Nick ?? "",
          images: item.Pic ? [item.Pic] : [],
          avatar: item.Avatar,
          location: item.Location ? item.Location : "",
          userId: item.EncryptUin,
          likedCount: item.PraiseNum,
          reply: item.SubComments ? item.SubComments.map((c) => {
            return {
              id: `sub_${c.SeqNo}_${c.CmId}`,
              text: this.replaceEmoji(c.Content).replace(/\\n/g, "\n"),
              time: c.PubTime ? this.formatTime(c.PubTime) : null,
              timeStr: c.PubTime ? dateFormat2(this.formatTime(c.PubTime)) : null,
              userName: c.Nick ?? "",
              avatar: c.Avatar,
              images: c.Pic ? [c.Pic] : [],
              userId: c.EncryptUin,
              likedCount: c.PraiseNum
            };
          }) : []
        };
      });
    },
    replaceEmoji(msg) {
      let rxp = /^\[em\](e\d+)\[\/em\]$/;
      let result = msg.match(/\[em\]e\d+\[\/em\]/g);
      if (!result) return msg;
      result = Array.from(new Set(result));
      for (let item of result) {
        let code = item.replace(rxp, "$1");
        msg = msg.replace(new RegExp(item.replace("[em]", "\\[em\\]").replace("[/em]", "\\[\\/em\\]"), "g"), emojis[code] || "");
      }
      return msg;
    },
    formatTime(time) {
      return String(time).length < 10 ? null : parseInt(time + "000");
    }
  };

  // ../vendor/lx-sdk/tx/index.js
  var tx = {
    // tipSearch,
    leaderboard: leaderboard_default,
    songList: songList_default,
    musicSearch: musicSearch_default,
    hotSearch: hotSearch_default,
    comment: comment_default,
    getMusicUrl(songInfo, type) {
      return apis("tx").getMusicUrl(songInfo, type);
    },
    getLyric(songInfo) {
      return lyric_default.getLyric(songInfo.songmid);
    },
    async getPic(songInfo) {
      return `https://y.gtimg.cn/music/photo_new/T002R500x500M000${songInfo.albumId}.jpg`;
    },
    getMusicDetailPageUrl(songInfo) {
      return `https://y.qq.com/n/yqq/song/${songInfo.songmid}.html`;
    }
  };
  var index_default = tx;
  return __toCommonJS(index_exports);
})();
globalThis.__lyn_source_tx = (typeof __lynSource !== 'undefined' && __lynSource.default) ? __lynSource.default : __lynSource;
