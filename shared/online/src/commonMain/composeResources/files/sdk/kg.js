var __lynSource = (() => {
  var __create = Object.create;
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __getProtoOf = Object.getPrototypeOf;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __esm = (fn, res) => function __init() {
    return fn && (res = (0, fn[__getOwnPropNames(fn)[0]])(fn = 0)), res;
  };
  var __commonJS = (cb, mod) => function __require() {
    return mod || (0, cb[__getOwnPropNames(cb)[0]])((mod = { exports: {} }).exports, mod), mod.exports;
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
  var __toESM = (mod, isNodeMode, target) => (target = mod != null ? __create(__getProtoOf(mod)) : {}, __copyProps(
    // If the importer is in node compatibility mode or this is not an ESM
    // file that has been converted to a CommonJS file using a Babel-
    // compatible transform (i.e. "__esModule" has not been set), then set
    // "default" to the CommonJS "module.exports" for node compatibility.
    isNodeMode || !mod || !mod.__esModule ? __defProp(target, "default", { value: mod, enumerable: true }) : target,
    mod
  ));
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

  // ../vendor/lx-sdk/kg/vendors/infSign.min.js
  var require_infSign_min = __commonJS({
    "../vendor/lx-sdk/kg/vendors/infSign.min.js"(exports, module) {
      init_buffer_inject();
      !function(t, n) {
        "object" == typeof exports && "undefined" != typeof module ? module.exports = n() : "function" == typeof define && define.amd ? define(n) : (t = t || self, t.infSign = n());
      }(exports, function() {
        "use strict";
        function t(t2, n2, r2) {
          return n2 in t2 ? Object.defineProperty(t2, n2, { value: r2, enumerable: true, configurable: true, writable: true }) : t2[n2] = r2, t2;
        }
        function n(t2, n2) {
          var r2 = Object.keys(t2);
          if (Object.getOwnPropertySymbols) {
            var e2 = Object.getOwnPropertySymbols(t2);
            n2 && (e2 = e2.filter(function(n3) {
              return Object.getOwnPropertyDescriptor(t2, n3).enumerable;
            })), r2.push.apply(r2, e2);
          }
          return r2;
        }
        function r(r2) {
          for (var e2 = 1; e2 < arguments.length; e2++) {
            var o2 = null != arguments[e2] ? arguments[e2] : {};
            e2 % 2 ? n(o2, true).forEach(function(n2) {
              t(r2, n2, o2[n2]);
            }) : Object.getOwnPropertyDescriptors ? Object.defineProperties(r2, Object.getOwnPropertyDescriptors(o2)) : n(o2).forEach(function(t2) {
              Object.defineProperty(r2, t2, Object.getOwnPropertyDescriptor(o2, t2));
            });
          }
          return r2;
        }
        function e(t2, n2) {
          return n2 = { exports: {} }, t2(n2, n2.exports), n2.exports;
        }
        function o(t2) {
          return !!t2.constructor && "function" == typeof t2.constructor.isBuffer && t2.constructor.isBuffer(t2);
        }
        function i(t2) {
          return "function" == typeof t2.readFloatLE && "function" == typeof t2.slice && o(t2.slice(0, 0));
        }
        function c() {
          var t2, n2 = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : {}, e2 = arguments.length > 1 && void 0 !== arguments[1] ? arguments[1] : "", o2 = arguments.length > 2 && void 0 !== arguments[2] ? arguments[2] : {}, i2 = false, c2 = false, a2 = "json", l2 = r({}, n2), u2 = s.isInClient();
          "function" == typeof o2 ? t2 = o2 : (t2 = o2.callback, i2 = o2.useH5 || false, a2 = o2.postType || "json", c2 = o2.isCDN || false), e2 && ("[object Object]" != Object.prototype.toString.call(e2) ? u2 = false : "urlencoded" == a2 && (u2 = false));
          var f2 = function() {
            var n3 = (/* @__PURE__ */ new Date()).getTime(), i3 = [], s2 = [], u3 = "NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt", f3 = { srcappid: "2919", clientver: "20000", clienttime: n3, mid: n3, uuid: n3, dfid: "-" };
            c2 && (delete f3.clienttime, delete f3.mid, delete f3.uuid, delete f3.dfid), l2 = r({}, f3, {}, l2);
            for (var g2 in l2) i3.push(g2);
            if (i3.sort(), i3.forEach(function(t3) {
              s2.push(t3 + "=" + l2[t3]);
            }), e2) if ("[object Object]" == Object.prototype.toString.call(e2)) if ("json" == a2) s2.push(JSON.stringify(e2));
            else {
              var b = [];
              for (var g2 in e2) b.push(g2 + "=" + e2[g2]);
              s2.push(b.join("&"));
            }
            else s2.push(e2);
            s2.unshift(u3), s2.push(u3), l2.signature = d(s2.join("")), o2.log && (console.log("H5\u7B7E\u540D\u524D\u53C2\u6570", s2), console.log("H5\u7B7E\u540D\u540E\u8FD4\u56DE", l2)), e2 ? t2 && t2(l2, "[object Object]" == Object.prototype.toString.call(e2) && "json" == a2 ? JSON.stringify(e2) : e2) : t2 && t2(l2);
          };
          if (u2 && !i2) {
            var g = false;
            s.mobileCall(764, { get: l2, post: e2 }, function(n3) {
              return !g && (g = true, n3 && n3.status ? (delete n3.status, o2.log && (console.log("\u5BA2\u6237\u7AEF\u7B7E\u540D\u524D\u53C2\u6570", { get: l2, post: e2 }), console.log("\u5BA2\u6237\u7AEF\u7B7E\u540D\u540E\u8FD4\u56DE", r({}, l2, {}, n3))), l2 = r({}, l2, {}, n3), e2 ? t2 && t2(l2, "[object Object]" == Object.prototype.toString.call(e2) && "json" == a2 ? JSON.stringify(e2) : e2) : t2 && t2(l2), false) : (u2 = false, void f2()));
            });
          } else u2 = false, f2();
        }
        "undefined" != typeof globalThis ? globalThis : "undefined" != typeof window ? window : "undefined" != typeof global ? global : "undefined" != typeof self && self;
        var s = e(function(t2, n2) {
          !function(n3, r2) {
            t2.exports = function() {
              var t3 = { str2Json: function(t4) {
                var n4 = {};
                if ("[object String]" === Object.prototype.toString.call(t4)) try {
                  n4 = JSON.parse(t4);
                } catch (t5) {
                  n4 = {};
                }
                return n4;
              }, json2Str: function(t4) {
                var n4 = t4;
                if ("string" != typeof t4) try {
                  n4 = JSON.stringify(t4);
                } catch (t5) {
                  n4 = "";
                }
                return n4;
              }, _extend: function(t4, n4) {
                if (n4) for (var r3 in t4) n4.hasOwnProperty(r3) || (n4[r3] = t4[r3]);
                return n4;
              }, formatURL: { browser: "", url: "" }, formatSong: { filename: "", filesize: "", hash: "", bitrate: "", extname: "", duration: "", mvhash: "", m4afilesize: "", "320hash": "", "320filesize": "", sqhash: "", sqfilesize: 0, feetype: 0, isfirst: 0 }, formatMV: { filename: "", singername: "", hash: "", imgurl: "" }, formatShare: { shareName: "", topicName: "", hash: "", listID: "", type: "", suid: "", slid: "", imgUrl: "", filename: "", duration: "", shareData: { linkUrl: "", picUrl: "", content: "", title: "" } }, cbNum: 0, isIOS: false, isKugouAndroid: false, isAndroid: true, loadUrl: function(t4) {
                var n4 = document.createElement("iframe");
                n4.setAttribute("src", t4), n4.setAttribute("style", "display:none;"), n4.setAttribute("height", "0px"), n4.setAttribute("width", "0px"), n4.setAttribute("frameborder", "0"), document.body.appendChild(n4), n4.parentNode.removeChild(n4), n4 = null;
              }, callCmd: function(n4) {
                var r3 = t3;
                if (r3.isKugouAndroid) {
                  var e2 = {}, o2 = "";
                  if (n4.cmd && (e2.cmd = n4.cmd), n4.jsonStr && (e2.jsonStr = n4.jsonStr), n4.callback && (o2 = "kgandroidmobilecall" + ++r3.cbNum + Math.random().toString().substr(2, 9), e2.callback = o2, window[o2] = function(t4, e3) {
                    void 0 !== t4 && ("[object String]" === Object.prototype.toString.call(t4) ? (t4 = "#" === e3 ? decodeURIComponent(t4) : decodeURIComponent(decodeURIComponent(t4)), n4.callback(r3.str2Json(t4))) : n4.callback(t4));
                  }), n4.AndroidCallback) {
                    var i2 = r3.str2Json(n4.jsonStr);
                    i2.AndroidCallback = o2, n4.jsonStr = r3.json2Str(i2), n4.jsonStr && (e2.jsonStr = n4.jsonStr);
                  }
                  var c2 = encodeURIComponent(JSON.stringify(e2)), s2 = "kugoujsbridge://start.kugou_jsbridge/?".concat(c2);
                  r3.loadUrl(s2);
                } else if (r3.isAndroid) {
                  var a2 = "", l2 = "";
                  if (n4.jsonStr) {
                    if (n4.callback && "" !== n4.callback && true === n4.AndroidCallback) {
                      l2 = "kgmobilecall" + ++r3.cbNum + Math.random().toString().substr(2, 9), window[l2] = function(t4, e3) {
                        void 0 !== t4 && ("[object String]" === Object.prototype.toString.call(t4) ? (t4 = "#" === e3 ? decodeURIComponent(t4) : decodeURIComponent(decodeURIComponent(t4)), n4.callback(r3.str2Json(t4))) : n4.callback(t4));
                      };
                      var u2 = r3.str2Json(n4.jsonStr);
                      u2.AndroidCallback = l2, n4.jsonStr = r3.json2Str(u2);
                    }
                    try {
                      a2 = external.superCall(n4.cmd, n4.jsonStr);
                    } catch (t4) {
                    }
                  } else try {
                    a2 = external.superCall(n4.cmd);
                  } catch (t4) {
                  }
                  n4.callback && "" !== n4.callback && "AndroidCallback" != a2 && (a2 = r3.str2Json(a2), n4.callback(a2));
                } else {
                  var f2 = "", d2 = "";
                  n4.callback && (d2 = "kgmobilecall" + ++r3.cbNum + Math.random().toString().substr(2, 9), window[d2] = function(t4) {
                    void 0 !== t4 && n4.callback && ("[object String]" === Object.prototype.toString.call(t4) ? n4.callback(r3.str2Json(t4)) : n4.callback(t4));
                  }), d2 && "" != d2 && n4.jsonStr && (f2 = 'kugouurl://start.music/?{"cmd":' + n4.cmd + ', "jsonStr":' + n4.jsonStr + ', "callback":"' + d2 + '"}'), d2 && "" != d2 && !n4.jsonStr && (f2 = 'kugouurl://start.music/?{"cmd":' + n4.cmd + ', "callback":"' + d2 + '"}'), "" == d2 && n4.jsonStr && (f2 = 'kugouurl://start.music/?{"cmd":' + n4.cmd + ', "jsonStr":' + n4.jsonStr + "}"), "" != d2 || n4.jsonStr || (f2 = 'kugouurl://start.music/?{"cmd":' + n4.cmd + "}"), r3.loadUrl(f2);
                }
              }, formartData: function(n4, r3) {
                n4 && 123 == n4 && r3 && (r3 = t3._extend(t3.formatURL, r3)), n4 && 123 == n4 && r3 && (r3 = t3._extend(t3.formatURL, r3));
              } };
              return { isIOS: t3.isIOS, isKugouAndroid: t3.isKugouAndroid, isAndroid: t3.isAndroid, isInClient: function() {
                return !(!t3.isAndroid && !t3.isKugouAndroid && !t3.isIOS);
              }, mobileCall: function(n4, r3, e2) {
                var o2 = "";
                if (r3 && (o2 = t3.json2Str(r3)), !n4) return console.error("\u8BF7\u8F93\u5165\u547D\u4EE4\u53F7\uFF01"), false;
                var i2 = {};
                n4 && (i2.cmd = n4), "" != o2 && (i2.jsonStr = o2), e2 && (i2.callback = e2), n4 && 186 == n4 && e2 && (i2.AndroidCallback = true), t3.callCmd(i2);
              }, KgWebMobileCall: function(t4, n4) {
                if (t4) try {
                  var r3 = t4.split(".");
                  r3.reduce(function(e2, o2) {
                    if (e2[o2]) {
                      if (o2 === r3[r3.length - 1]) {
                        var i2 = e2[o2];
                        return "function" == typeof i2 ? (e2[o2] = function(t5) {
                          i2 && i2(t5), n4 && n4(t5);
                        }, e2[o2]) : (console.error("\u8BF7\u68C0\u67E5\uFF0C\u5F53\u524D\u73AF\u5883\u53D8\u91CF\u5DF2\u6CE8\u518C\u4E86\u5BF9\u8C61\uFF1A" + t4 + "\uFF0C\u4E14\u8BE5\u5BF9\u8C61\u4E0D\u662F\u65B9\u6CD5"), null);
                      }
                      return e2[o2];
                    }
                    return o2 === r3[r3.length - 1] ? e2[o2] = function(t5) {
                      n4 && n4(t5);
                    } : e2[o2] = new Object(), e2[o2];
                  }, window);
                } catch (t5) {
                }
              } };
            }();
          }();
        }), a = e(function(t2) {
          !function() {
            var n2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", r2 = { rotl: function(t3, n3) {
              return t3 << n3 | t3 >>> 32 - n3;
            }, rotr: function(t3, n3) {
              return t3 << 32 - n3 | t3 >>> n3;
            }, endian: function(t3) {
              if (t3.constructor == Number) return 16711935 & r2.rotl(t3, 8) | 4278255360 & r2.rotl(t3, 24);
              for (var n3 = 0; n3 < t3.length; n3++) t3[n3] = r2.endian(t3[n3]);
              return t3;
            }, randomBytes: function(t3) {
              for (var n3 = []; t3 > 0; t3--) n3.push(Math.floor(256 * Math.random()));
              return n3;
            }, bytesToWords: function(t3) {
              for (var n3 = [], r3 = 0, e2 = 0; r3 < t3.length; r3++, e2 += 8) n3[e2 >>> 5] |= t3[r3] << 24 - e2 % 32;
              return n3;
            }, wordsToBytes: function(t3) {
              for (var n3 = [], r3 = 0; r3 < 32 * t3.length; r3 += 8) n3.push(t3[r3 >>> 5] >>> 24 - r3 % 32 & 255);
              return n3;
            }, bytesToHex: function(t3) {
              for (var n3 = [], r3 = 0; r3 < t3.length; r3++) n3.push((t3[r3] >>> 4).toString(16)), n3.push((15 & t3[r3]).toString(16));
              return n3.join("");
            }, hexToBytes: function(t3) {
              for (var n3 = [], r3 = 0; r3 < t3.length; r3 += 2) n3.push(parseInt(t3.substr(r3, 2), 16));
              return n3;
            }, bytesToBase64: function(t3) {
              for (var r3 = [], e2 = 0; e2 < t3.length; e2 += 3) for (var o2 = t3[e2] << 16 | t3[e2 + 1] << 8 | t3[e2 + 2], i2 = 0; i2 < 4; i2++) 8 * e2 + 6 * i2 <= 8 * t3.length ? r3.push(n2.charAt(o2 >>> 6 * (3 - i2) & 63)) : r3.push("=");
              return r3.join("");
            }, base64ToBytes: function(t3) {
              t3 = t3.replace(/[^A-Z0-9+\/]/gi, "");
              for (var r3 = [], e2 = 0, o2 = 0; e2 < t3.length; o2 = ++e2 % 4) 0 != o2 && r3.push((n2.indexOf(t3.charAt(e2 - 1)) & Math.pow(2, -2 * o2 + 8) - 1) << 2 * o2 | n2.indexOf(t3.charAt(e2)) >>> 6 - 2 * o2);
              return r3;
            } };
            t2.exports = r2;
          }();
        }), l = { utf8: { stringToBytes: function(t2) {
          return l.bin.stringToBytes(unescape(encodeURIComponent(t2)));
        }, bytesToString: function(t2) {
          return decodeURIComponent(escape(l.bin.bytesToString(t2)));
        } }, bin: { stringToBytes: function(t2) {
          for (var n2 = [], r2 = 0; r2 < t2.length; r2++) n2.push(255 & t2.charCodeAt(r2));
          return n2;
        }, bytesToString: function(t2) {
          for (var n2 = [], r2 = 0; r2 < t2.length; r2++) n2.push(String.fromCharCode(t2[r2]));
          return n2.join("");
        } } }, u = l, f = function(t2) {
          return null != t2 && (o(t2) || i(t2) || !!t2._isBuffer);
        }, d = e(function(t2) {
          !function() {
            var n2 = a, r2 = u.utf8, e2 = f, o2 = u.bin, i2 = function(t3, c2) {
              t3.constructor == String ? t3 = c2 && "binary" === c2.encoding ? o2.stringToBytes(t3) : r2.stringToBytes(t3) : e2(t3) ? t3 = Array.prototype.slice.call(t3, 0) : Array.isArray(t3) || (t3 = t3.toString());
              for (var s2 = n2.bytesToWords(t3), a2 = 8 * t3.length, l2 = 1732584193, u2 = -271733879, f2 = -1732584194, d2 = 271733878, g = 0; g < s2.length; g++) s2[g] = 16711935 & (s2[g] << 8 | s2[g] >>> 24) | 4278255360 & (s2[g] << 24 | s2[g] >>> 8);
              s2[a2 >>> 5] |= 128 << a2 % 32, s2[14 + (a2 + 64 >>> 9 << 4)] = a2;
              for (var b = i2._ff, p = i2._gg, h = i2._hh, m = i2._ii, g = 0; g < s2.length; g += 16) {
                var y = l2, j = u2, S = f2, v = d2;
                u2 = m(u2 = m(u2 = m(u2 = m(u2 = h(u2 = h(u2 = h(u2 = h(u2 = p(u2 = p(u2 = p(u2 = p(u2 = b(u2 = b(u2 = b(u2 = b(u2, f2 = b(f2, d2 = b(d2, l2 = b(l2, u2, f2, d2, s2[g + 0], 7, -680876936), u2, f2, s2[g + 1], 12, -389564586), l2, u2, s2[g + 2], 17, 606105819), d2, l2, s2[g + 3], 22, -1044525330), f2 = b(f2, d2 = b(d2, l2 = b(l2, u2, f2, d2, s2[g + 4], 7, -176418897), u2, f2, s2[g + 5], 12, 1200080426), l2, u2, s2[g + 6], 17, -1473231341), d2, l2, s2[g + 7], 22, -45705983), f2 = b(f2, d2 = b(d2, l2 = b(l2, u2, f2, d2, s2[g + 8], 7, 1770035416), u2, f2, s2[g + 9], 12, -1958414417), l2, u2, s2[g + 10], 17, -42063), d2, l2, s2[g + 11], 22, -1990404162), f2 = b(f2, d2 = b(d2, l2 = b(l2, u2, f2, d2, s2[g + 12], 7, 1804603682), u2, f2, s2[g + 13], 12, -40341101), l2, u2, s2[g + 14], 17, -1502002290), d2, l2, s2[g + 15], 22, 1236535329), f2 = p(f2, d2 = p(d2, l2 = p(l2, u2, f2, d2, s2[g + 1], 5, -165796510), u2, f2, s2[g + 6], 9, -1069501632), l2, u2, s2[g + 11], 14, 643717713), d2, l2, s2[g + 0], 20, -373897302), f2 = p(f2, d2 = p(d2, l2 = p(l2, u2, f2, d2, s2[g + 5], 5, -701558691), u2, f2, s2[g + 10], 9, 38016083), l2, u2, s2[g + 15], 14, -660478335), d2, l2, s2[g + 4], 20, -405537848), f2 = p(f2, d2 = p(d2, l2 = p(l2, u2, f2, d2, s2[g + 9], 5, 568446438), u2, f2, s2[g + 14], 9, -1019803690), l2, u2, s2[g + 3], 14, -187363961), d2, l2, s2[g + 8], 20, 1163531501), f2 = p(f2, d2 = p(d2, l2 = p(l2, u2, f2, d2, s2[g + 13], 5, -1444681467), u2, f2, s2[g + 2], 9, -51403784), l2, u2, s2[g + 7], 14, 1735328473), d2, l2, s2[g + 12], 20, -1926607734), f2 = h(f2, d2 = h(d2, l2 = h(l2, u2, f2, d2, s2[g + 5], 4, -378558), u2, f2, s2[g + 8], 11, -2022574463), l2, u2, s2[g + 11], 16, 1839030562), d2, l2, s2[g + 14], 23, -35309556), f2 = h(f2, d2 = h(d2, l2 = h(l2, u2, f2, d2, s2[g + 1], 4, -1530992060), u2, f2, s2[g + 4], 11, 1272893353), l2, u2, s2[g + 7], 16, -155497632), d2, l2, s2[g + 10], 23, -1094730640), f2 = h(f2, d2 = h(d2, l2 = h(l2, u2, f2, d2, s2[g + 13], 4, 681279174), u2, f2, s2[g + 0], 11, -358537222), l2, u2, s2[g + 3], 16, -722521979), d2, l2, s2[g + 6], 23, 76029189), f2 = h(f2, d2 = h(d2, l2 = h(l2, u2, f2, d2, s2[g + 9], 4, -640364487), u2, f2, s2[g + 12], 11, -421815835), l2, u2, s2[g + 15], 16, 530742520), d2, l2, s2[g + 2], 23, -995338651), f2 = m(f2, d2 = m(d2, l2 = m(l2, u2, f2, d2, s2[g + 0], 6, -198630844), u2, f2, s2[g + 7], 10, 1126891415), l2, u2, s2[g + 14], 15, -1416354905), d2, l2, s2[g + 5], 21, -57434055), f2 = m(f2, d2 = m(d2, l2 = m(l2, u2, f2, d2, s2[g + 12], 6, 1700485571), u2, f2, s2[g + 3], 10, -1894986606), l2, u2, s2[g + 10], 15, -1051523), d2, l2, s2[g + 1], 21, -2054922799), f2 = m(f2, d2 = m(d2, l2 = m(l2, u2, f2, d2, s2[g + 8], 6, 1873313359), u2, f2, s2[g + 15], 10, -30611744), l2, u2, s2[g + 6], 15, -1560198380), d2, l2, s2[g + 13], 21, 1309151649), f2 = m(f2, d2 = m(d2, l2 = m(l2, u2, f2, d2, s2[g + 4], 6, -145523070), u2, f2, s2[g + 11], 10, -1120210379), l2, u2, s2[g + 2], 15, 718787259), d2, l2, s2[g + 9], 21, -343485551), l2 = l2 + y >>> 0, u2 = u2 + j >>> 0, f2 = f2 + S >>> 0, d2 = d2 + v >>> 0;
              }
              return n2.endian([l2, u2, f2, d2]);
            };
            i2._ff = function(t3, n3, r3, e3, o3, i3, c2) {
              var s2 = t3 + (n3 & r3 | ~n3 & e3) + (o3 >>> 0) + c2;
              return (s2 << i3 | s2 >>> 32 - i3) + n3;
            }, i2._gg = function(t3, n3, r3, e3, o3, i3, c2) {
              var s2 = t3 + (n3 & e3 | r3 & ~e3) + (o3 >>> 0) + c2;
              return (s2 << i3 | s2 >>> 32 - i3) + n3;
            }, i2._hh = function(t3, n3, r3, e3, o3, i3, c2) {
              var s2 = t3 + (n3 ^ r3 ^ e3) + (o3 >>> 0) + c2;
              return (s2 << i3 | s2 >>> 32 - i3) + n3;
            }, i2._ii = function(t3, n3, r3, e3, o3, i3, c2) {
              var s2 = t3 + (r3 ^ (n3 | ~e3)) + (o3 >>> 0) + c2;
              return (s2 << i3 | s2 >>> 32 - i3) + n3;
            }, i2._blocksize = 16, i2._digestsize = 16, t2.exports = function(t3, r3) {
              if (void 0 === t3 || null === t3) throw new Error("Illegal argument " + t3);
              var e3 = n2.wordsToBytes(i2(t3, r3));
              return r3 && r3.asBytes ? e3 : r3 && r3.asString ? o2.bytesToString(e3) : n2.bytesToHex(e3);
            };
          }();
        });
        return c;
      });
    }
  });

  // ../vendor/lx-sdk/kg/index.js
  var index_exports = {};
  __export(index_exports, {
    default: () => index_default
  });
  init_buffer_inject();

  // ../vendor/lx-sdk/kg/leaderboard.js
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

  // lyn-internal:lx-internal-index
  init_buffer_inject();
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

  // ../vendor/lx-sdk/kg/leaderboard.js
  var boardList = [{ id: "kg__8888", name: "TOP500", bangid: "8888" }, { id: "kg__6666", name: "\u98D9\u5347\u699C", bangid: "6666" }, { id: "kg__59703", name: "\u8702\u9E1F\u6D41\u884C\u97F3\u4E50\u699C", bangid: "59703" }, { id: "kg__52144", name: "\u6296\u97F3\u70ED\u6B4C\u699C", bangid: "52144" }, { id: "kg__52767", name: "\u5FEB\u624B\u70ED\u6B4C\u699C", bangid: "52767" }, { id: "kg__24971", name: "DJ\u70ED\u6B4C\u699C", bangid: "24971" }, { id: "kg__23784", name: "\u7F51\u7EDC\u7EA2\u6B4C\u699C", bangid: "23784" }, { id: "kg__44412", name: "\u8BF4\u5531\u5148\u950B\u699C", bangid: "44412" }, { id: "kg__31308", name: "\u5185\u5730\u699C", bangid: "31308" }, { id: "kg__33160", name: "\u7535\u97F3\u699C", bangid: "33160" }, { id: "kg__31313", name: "\u9999\u6E2F\u5730\u533A\u699C", bangid: "31313" }, { id: "kg__51341", name: "\u6C11\u8C23\u699C", bangid: "51341" }, { id: "kg__54848", name: "\u53F0\u6E7E\u5730\u533A\u699C", bangid: "54848" }, { id: "kg__31310", name: "\u6B27\u7F8E\u699C", bangid: "31310" }, { id: "kg__33162", name: "ACG\u65B0\u6B4C\u699C", bangid: "33162" }, { id: "kg__31311", name: "\u97E9\u56FD\u699C", bangid: "31311" }, { id: "kg__31312", name: "\u65E5\u672C\u699C", bangid: "31312" }, { id: "kg__49225", name: "80\u540E\u70ED\u6B4C\u699C", bangid: "49225" }, { id: "kg__49223", name: "90\u540E\u70ED\u6B4C\u699C", bangid: "49223" }, { id: "kg__49224", name: "00\u540E\u70ED\u6B4C\u699C", bangid: "49224" }, { id: "kg__33165", name: "\u7CA4\u8BED\u91D1\u66F2\u699C", bangid: "33165" }, { id: "kg__33166", name: "\u6B27\u7F8E\u91D1\u66F2\u699C", bangid: "33166" }, { id: "kg__33163", name: "\u5F71\u89C6\u91D1\u66F2\u699C", bangid: "33163" }, { id: "kg__51340", name: "\u4F24\u611F\u699C", bangid: "51340" }, { id: "kg__35811", name: "\u4F1A\u5458\u4E13\u4EAB\u699C", bangid: "35811" }, { id: "kg__37361", name: "\u96F7\u8FBE\u699C", bangid: "37361" }, { id: "kg__21101", name: "\u5206\u4EAB\u699C", bangid: "21101" }, { id: "kg__46910", name: "\u7EFC\u827A\u65B0\u6B4C\u699C", bangid: "46910" }, { id: "kg__30972", name: "\u9177\u72D7\u97F3\u4E50\u4EBA\u539F\u521B\u699C", bangid: "30972" }, { id: "kg__60170", name: "\u95FD\u5357\u8BED\u699C", bangid: "60170" }, { id: "kg__65234", name: "\u513F\u6B4C\u699C", bangid: "65234" }, { id: "kg__4681", name: "\u7F8E\u56FDBillBoard\u699C", bangid: "4681" }, { id: "kg__25028", name: "Beatport\u7535\u5B50\u821E\u66F2\u699C", bangid: "25028" }, { id: "kg__4680", name: "\u82F1\u56FD\u5355\u66F2\u699C", bangid: "4680" }, { id: "kg__38623", name: "\u97E9\u56FDMelon\u97F3\u4E50\u699C", bangid: "38623" }, { id: "kg__42807", name: "joox\u672C\u5730\u70ED\u6B4C\u699C", bangid: "42807" }, { id: "kg__36107", name: "\u5C0F\u8BED\u79CD\u70ED\u6B4C\u699C", bangid: "36107" }, { id: "kg__4673", name: "\u65E5\u672C\u516C\u4FE1\u699C", bangid: "4673" }, { id: "kg__46868", name: "\u65E5\u672CSPACE SHOWER\u699C", bangid: "46868" }, { id: "kg__42808", name: "KKBOX\u98CE\u4E91\u699C", bangid: "42808" }, { id: "kg__60171", name: "\u8D8A\u5357\u8BED\u699C", bangid: "60171" }, { id: "kg__60172", name: "\u6CF0\u8BED\u699C", bangid: "60172" }, { id: "kg__59895", name: "R&B\u699C", bangid: "59895" }, { id: "kg__59896", name: "\u6447\u6EDA\u699C", bangid: "59896" }, { id: "kg__59897", name: "\u7235\u58EB\u699C", bangid: "59897" }, { id: "kg__59898", name: "\u4E61\u6751\u97F3\u4E50\u699C", bangid: "59898" }, { id: "kg__59900", name: "\u7EAF\u97F3\u4E50\u699C", bangid: "59900" }, { id: "kg__59899", name: "\u53E4\u5178\u699C", bangid: "59899" }, { id: "kg__22603", name: "5sing\u97F3\u4E50\u699C", bangid: "22603" }, { id: "kg__21335", name: "\u7E41\u661F\u97F3\u4E50\u699C", bangid: "21335" }, { id: "kg__33161", name: "\u53E4\u98CE\u65B0\u6B4C\u699C", bangid: "33161" }];
  var leaderboard_default = {
    listDetailLimit: 100,
    list: [
      {
        id: "kgtop500",
        name: "TOP500",
        bangid: "8888"
      },
      {
        id: "kgwlhgb",
        name: "\u7F51\u7EDC\u699C",
        bangid: "23784"
      },
      {
        id: "kgbsb",
        name: "\u98D9\u5347\u699C",
        bangid: "6666"
      },
      {
        id: "kgfxb",
        name: "\u5206\u4EAB\u699C",
        bangid: "21101"
      },
      {
        id: "kgcyyb",
        name: "\u7EAF\u97F3\u4E50\u699C",
        bangid: "33164"
      },
      {
        id: "kggfjqb",
        name: "\u53E4\u98CE\u699C",
        bangid: "33161"
      },
      {
        id: "kgyyjqb",
        name: "\u7CA4\u8BED\u699C",
        bangid: "33165"
      },
      {
        id: "kgomjqb",
        name: "\u6B27\u7F8E\u699C",
        bangid: "33166"
      },
      {
        id: "kgdyrgb",
        name: "\u7535\u97F3\u699C",
        bangid: "33160"
      },
      {
        id: "kgjdrgb",
        name: "DJ\u70ED\u6B4C\u699C",
        bangid: "24971"
      },
      {
        id: "kghyxgb",
        name: "\u534E\u8BED\u65B0\u6B4C\u699C",
        bangid: "31308"
      }
    ],
    getUrl(p, id, limit) {
      return `http://mobilecdnbj.kugou.com/api/v3/rank/song?version=9108&ranktype=1&plat=0&pagesize=${limit}&area_code=1&page=${p}&rankid=${id}&with_res_tag=0&show_portrait_mv=1`;
    },
    regExps: {
      total: /total: '(\d+)',/,
      page: /page: '(\d+)',/,
      limit: /pagesize: '(\d+)',/,
      listData: /global\.features = (\[.+\]);/
    },
    _requestBoardsObj: null,
    getBoardsData() {
      if (this._requestBoardsObj) this._requestBoardsObj.cancelHttp();
      this._requestBoardsObj = httpFetch("http://mobilecdnbj.kugou.com/api/v5/rank/list?version=9108&plat=0&showtype=2&parentid=0&apiver=6&area_code=1&withsong=1");
      return this._requestBoardsObj.promise;
    },
    getData(url) {
      const requestDataObj = httpFetch(url);
      return requestDataObj.promise;
    },
    getSinger(singers) {
      let arr = [];
      singers.forEach((singer) => {
        arr.push(singer.author_name);
      });
      return arr.join("\u3001");
    },
    filterData(rawList) {
      return rawList.map((item) => {
        const types = [];
        const _types = {};
        if (item.filesize !== 0) {
          let size = sizeFormate(item.filesize);
          types.push({ type: "128k", size, hash: item.hash });
          _types["128k"] = {
            size,
            hash: item.hash
          };
        }
        if (item["320filesize"] !== 0) {
          let size = sizeFormate(item["320filesize"]);
          types.push({ type: "320k", size, hash: item["320hash"] });
          _types["320k"] = {
            size,
            hash: item["320hash"]
          };
        }
        if (item.sqfilesize !== 0) {
          let size = sizeFormate(item.sqfilesize);
          types.push({ type: "flac", size, hash: item.sqhash });
          _types.flac = {
            size,
            hash: item.sqhash
          };
        }
        if (item.filesize_high !== 0) {
          let size = sizeFormate(item.filesize_high);
          types.push({ type: "flac24bit", size, hash: item.hash_high });
          _types.flac24bit = {
            size,
            hash: item.hash_high
          };
        }
        return {
          singer: formatSingerName(item.authors, "author_name"),
          name: decodeName(item.songname),
          albumName: decodeName(item.remark),
          albumId: item.album_id,
          songmid: item.audio_id,
          source: "kg",
          interval: formatPlayTime(item.duration),
          img: null,
          lrc: null,
          hash: item.hash,
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
        if (board.isvol != 1) continue;
        list.push({
          id: "kg__" + board.rankid,
          name: board.rankname,
          bangid: String(board.rankid)
        });
      }
      return list;
    },
    async getBoards(retryNum = 0) {
      this.list = boardList;
      return {
        list: boardList,
        source: "kg"
      };
    },
    async getList(bangid, page, retryNum = 0) {
      if (++retryNum > 3) throw new Error("try max num");
      const { body } = await this.getData(this.getUrl(page, bangid, this.listDetailLimit));
      if (body.errcode != 0) return this.getList(bangid, page, retryNum);
      let total = body.data.total;
      let limit = 100;
      let listData = this.filterData(body.data.info);
      return {
        total,
        list: listData,
        limit,
        page,
        source: "kg"
      };
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") id = id.replace("kg__", "");
      return `https://www.kugou.com/yy/rank/home/1-${id}.html`;
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

  // ../vendor/lx-sdk/kg/songList.js
  init_buffer_inject();
  var import_infSign = __toESM(require_infSign_min());

  // ../vendor/lx-sdk/kg/util.js
  init_buffer_inject();

  // lyn-shim:pako
  init_buffer_inject();
  var inflate = (input) => globalThis.lyn.zlibInflate(input);
  var inflateRaw = (input) => globalThis.lyn.zlibInflate(input);
  var ungzip = (input) => globalThis.lyn.zlibInflate(input);
  var pako_default = { inflate, inflateRaw, ungzip };

  // ../vendor/lx-sdk/kg/util.js
  init_react_native_buffer();
  var enc_key = Buffer2.from([64, 71, 97, 119, 94, 50, 116, 71, 81, 54, 49, 45, 206, 210, 110, 105], "binary");
  var decodeLyric = (str) => new Promise((resolve, reject) => {
    if (!str.length) return;
    const buf_str = Buffer2.from(str, "base64").slice(4);
    for (let i = 0, len = buf_str.length; i < len; i++) {
      buf_str[i] = buf_str[i] ^ enc_key[i % 16];
    }
    const result = pako_default.inflate(buf_str, { to: "string" });
    resolve(result);
  });
  var signatureParams = (params, platform = "android", body = "") => {
    let keyparam = "OIlwieks28dk2k092lksi2UIkp";
    if (platform === "web") keyparam = "NVPh5oo715z5DIWAeQlhMDsWXXQV4hwt";
    let param_list = params.split("&");
    param_list.sort();
    let sign_params = `${keyparam}${param_list.join("")}${body}${keyparam}`;
    return toMD5(sign_params);
  };

  // ../vendor/lx-sdk/kg/songList.js
  var handleSignature = (id, page, limit) => new Promise((resolve, reject) => {
    (0, import_infSign.default)({ appid: 1058, type: 0, module: "playlist", page, pagesize: limit, specialid: id }, null, {
      useH5: true,
      isCDN: true,
      callback(i) {
        resolve(i.signature);
      }
    });
  });
  var songList_default = {
    _requestObj_tags: null,
    _requestObj_listInfo: null,
    _requestObj_list: null,
    _requestObj_listRecommend: null,
    listDetailLimit: 1e4,
    currentTagInfo: {
      id: void 0,
      info: void 0
    },
    sortList: [
      {
        name: "\u63A8\u8350",
        tid: "recommend",
        id: "5"
      },
      {
        name: "\u6700\u70ED",
        tid: "hot",
        id: "6"
      },
      {
        name: "\u6700\u65B0",
        tid: "new",
        id: "7"
      },
      {
        name: "\u70ED\u85CF",
        tid: "hot_collect",
        id: "3"
      },
      {
        name: "\u98D9\u5347",
        tid: "rise",
        id: "8"
      }
    ],
    cache: /* @__PURE__ */ new Map(),
    regExps: {
      listData: /global\.data = (\[.+\]);/,
      listInfo: /global = {[\s\S]+?name: "(.+)"[\s\S]+?pic: "(.+)"[\s\S]+?};/,
      // https://www.kugou.com/yy/special/single/1067062.html
      listDetailLink: /^.+\/(\d+)\.html(?:\?.*|&.*$|#.*$|$)/
    },
    // async getGlobalSpecialId(specialId) {
    //   return httpFetch(`http://mobilecdnbj.kugou.com/api/v5/special/info?specialid=${specialId}`, {
    //     headers: {
    //       'User-Agent': 'Mozilla/5.0 (Linux; Android 10; HLK-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.102 Mobile Safari/537.36 EdgA/104.0.1293.70',
    //     },
    //   }).promise.then(({ body }) => {
    //     // console.log(body)
    //     if (!body.data.global_specialid) Promise.reject(new Error('Failed to get global collection id.'))
    //     return body.data.global_specialid
    //   })
    // },
    // async getListInfoBySpecialId(special_id, retry = 0) {
    //   if (++retry > 2) throw new Error('failed')
    //   return httpFetch(`https://m.kugou.com/plist/list/${special_id}/?json=true`, {
    //     headers: {
    //       'User-Agent': 'Mozilla/5.0 (Linux; Android 10; HLK-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.102 Mobile Safari/537.36 EdgA/104.0.1293.70',
    //     },
    //     follow_max: 2,
    //   }).promise.then(({ body }) => {
    //     // console.log(body)
    //     if (!body.info.list) return this.getListInfoBySpecialId(special_id, retry)
    //     let listinfo = body.info.list
    //     return {
    //       listInfo: {
    //         name: listinfo.specialname,
    //         image: listinfo.imgurl.replace('{size}', '150'),
    //         intro: listinfo.intro,
    //         author: listinfo.nickname,
    //         playcount: listinfo.playcount,
    //         total: listinfo.songcount,
    //       },
    //       globalSpecialId: listinfo.global_specialid,
    //     }
    //   })
    // },
    // async getSongListDetailByGlobalSpecialId(id, page, limit = 100, retry = 0) {
    //   if (++retry > 2) throw new Error('failed')
    //   console.log(id)
    //   const params = `specialid=0&need_sort=1&module=CloudMusic&clientver=11409&pagesize=${limit}&global_collection_id=${id}&userid=0&page=${page}&type=1&area_code=1&appid=1005`
    //   return httpFetch(`http://pubsongscdn.tx.kugou.com/v2/get_other_list_file?${params}&signature=${signatureParams(params)}`).promise.then(({ body }) => {
    //     // console.log(body)
    //     if (body.data?.info == null) return this.getSongListDetailByGlobalSpecialId(id, page, limit, retry)
    //     return body.data.info
    //   })
    // },
    parseHtmlDesc(html) {
      const prefix = '<div class="pc_specail_text pc_singer_tab_content" id="specailIntroduceWrap">';
      let index = html.indexOf(prefix);
      if (index < 0) return null;
      const afterStr = html.substring(index + prefix.length);
      index = afterStr.indexOf("</div>");
      if (index < 0) return null;
      return decodeName(afterStr.substring(0, index));
    },
    async getListDetailBySpecialId(id, page, tryNum = 0) {
      if (tryNum > 2) throw new Error("try max num");
      const { body } = await httpFetch(this.getSongListDetailUrl(id)).promise;
      let listData = body.match(this.regExps.listData);
      let listInfo = body.match(this.regExps.listInfo);
      if (!listData) return this.getListDetailBySpecialId(id, page, ++tryNum);
      let list = await this.getMusicInfos(JSON.parse(listData[1]));
      let name;
      let pic;
      if (listInfo) {
        name = listInfo[1];
        pic = listInfo[2];
      }
      let desc = this.parseHtmlDesc(body);
      return {
        list,
        page: 1,
        limit: 1e4,
        total: list.length,
        source: "kg",
        info: {
          name,
          img: pic,
          desc
          // author: body.result.info.userinfo.username,
          // play_count: formatPlayCount(body.result.listen_num),
        }
      };
    },
    getInfoUrl(tagId) {
      return tagId ? `http://www2.kugou.kugou.com/yueku/v9/special/getSpecial?is_smarty=1&cdn=cdn&t=5&c=${tagId}` : "http://www2.kugou.kugou.com/yueku/v9/special/getSpecial?is_smarty=1&";
    },
    getSongListUrl(sortId, tagId, page) {
      if (tagId == null) tagId = "";
      return `http://www2.kugou.kugou.com/yueku/v9/special/getSpecial?is_ajax=1&cdn=cdn&t=${sortId}&c=${tagId}&p=${page}`;
    },
    getSongListDetailUrl(id) {
      return `http://www2.kugou.kugou.com/yueku/v9/special/single/${id}-5-9999.html`;
    },
    filterInfoHotTag(rawData) {
      const result = [];
      if (rawData.status !== 1) return result;
      for (const key of Object.keys(rawData.data)) {
        let tag = rawData.data[key];
        result.push({
          id: tag.special_id,
          name: tag.special_name,
          source: "kg"
        });
      }
      return result;
    },
    filterTagInfo(rawData) {
      const result = [];
      for (const name of Object.keys(rawData)) {
        result.push({
          name,
          list: rawData[name].data.map((tag) => ({
            parent_id: tag.parent_id,
            parent_name: tag.pname,
            id: tag.id,
            name: tag.name,
            source: "kg"
          }))
        });
      }
      return result;
    },
    getSongList(sortId, tagId, page, tryNum = 0) {
      if (this._requestObj_list) this._requestObj_list.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_list = httpFetch(
        this.getSongListUrl(sortId, tagId, page)
      );
      return this._requestObj_list.promise.then(({ body }) => {
        if (!body || body.status !== 1) return this.getSongList(sortId, tagId, page, ++tryNum);
        return this.filterList(body.special_db);
      });
    },
    getSongListRecommend(tryNum = 0) {
      if (this._requestObj_listRecommend) this._requestObj_listRecommend.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_listRecommend = httpFetch(
        "http://everydayrec.service.kugou.com/guess_special_recommend",
        {
          method: "post",
          headers: {
            "User-Agent": "KuGou2012-8275-web_browser_event_handler"
          },
          body: {
            appid: 1001,
            clienttime: 1566798337219,
            clientver: 8275,
            key: "f1f93580115bb106680d2375f8032d96",
            mid: "21511157a05844bd085308bc76ef3343",
            platform: "pc",
            userid: "262643156",
            return_min: 6,
            return_max: 15
          }
        }
      );
      return this._requestObj_listRecommend.promise.then(({ body }) => {
        if (body.status !== 1) return this.getSongListRecommend(++tryNum);
        return this.filterList(body.data.special_list);
      });
    },
    filterList(rawData) {
      return rawData.map((item) => ({
        play_count: item.total_play_count || formatPlayCount(item.play_count),
        id: "id_" + item.specialid,
        author: item.nickname,
        name: item.specialname,
        time: dateFormat(item.publish_time || item.publishtime, "Y-M-D"),
        img: item.img || item.imgurl,
        total: item.songcount,
        grade: item.grade,
        desc: item.intro,
        source: "kg"
      }));
    },
    async createHttp(url, options, retryNum = 0) {
      if (retryNum > 2) throw new Error("try max num");
      let result;
      options.cache = "default";
      try {
        result = await httpFetch(url, options).promise;
      } catch (err) {
        console.log(err);
        return this.createHttp(url, options, ++retryNum);
      }
      if (result.statusCode !== 200 || (result.body.error_code !== void 0 ? result.body.error_code : result.body.errcode !== void 0 ? result.body.errcode : result.body.err_code) !== 0) return this.createHttp(url, options, ++retryNum);
      if (result.body.data) return result.body.data;
      if (Array.isArray(result.body.info)) return result.body;
      return result.body.info;
    },
    createTask(hashs) {
      let data = {
        area_code: "1",
        show_privilege: 1,
        show_album_info: "1",
        is_publish: "",
        appid: 1005,
        clientver: 11451,
        mid: "1",
        dfid: "-",
        clienttime: Date.now(),
        key: "OIlwieks28dk2k092lksi2UIkp",
        fields: "album_info,author_name,audio_info,ori_audio_name,base,songname"
      };
      let list = hashs;
      let tasks = [];
      while (list.length) {
        tasks.push(Object.assign({ data: list.slice(0, 100) }, data));
        if (list.length < 100) break;
        list = list.slice(100);
      }
      let url = "http://gateway.kugou.com/v2/album_audio/audio";
      return tasks.map((task) => this.createHttp(url, {
        method: "POST",
        body: task,
        headers: {
          "KG-THash": "13a3164",
          "KG-RC": "1",
          "KG-Fake": "0",
          "KG-RF": "00869891",
          "User-Agent": "Android712-AndroidPhone-11451-376-0-FeeCacheUpdate-wifi",
          "x-router": "kmr.service.kugou.com"
        }
      }).then((data2) => data2.map((s) => s[0])));
    },
    async getMusicInfos(list) {
      return this.filterData2(
        await Promise.all(
          this.createTask(
            this.deDuplication(list).map((item) => ({ hash: item.hash }))
          )
        ).then(([...datas]) => datas.flat())
      );
    },
    async getUserListDetailByCode(id) {
      const songInfo = await this.createHttp("http://t.kugou.com/command/", {
        method: "POST",
        headers: {
          "KG-RC": 1,
          "KG-THash": "network_super_call.cpp:3676261689:379",
          "User-Agent": ""
        },
        body: { appid: 1001, clientver: 9020, mid: "21511157a05844bd085308bc76ef3343", clienttime: 640612895, key: "36164c4015e704673c588ee202b9ecb8", data: id }
      });
      let songList;
      let info = songInfo.info;
      switch (info.type) {
        case 2:
          if (!info.global_collection_id) return this.getListDetailBySpecialId(info.id);
          break;
        default:
          break;
      }
      if (info.global_collection_id) return this.getUserListDetail2(info.global_collection_id);
      if (info.userid != null) {
        songList = await this.createHttp("http://www2.kugou.kugou.com/apps/kucodeAndShare/app/", {
          method: "POST",
          headers: {
            "KG-RC": 1,
            "KG-THash": "network_super_call.cpp:3676261689:379",
            "User-Agent": ""
          },
          body: { appid: 1001, clientver: 9020, mid: "21511157a05844bd085308bc76ef3343", clienttime: 640612895, key: "36164c4015e704673c588ee202b9ecb8", data: { id: info.id, type: 3, userid: info.userid, collect_type: 0, page: 1, pagesize: info.count } }
        });
      }
      let list = await this.getMusicInfos(songList || songInfo.list);
      return {
        list,
        page: 1,
        limit: info.count,
        total: list.length,
        source: "kg",
        info: {
          name: info.name,
          img: info.img_size && info.img_size.replace("{size}", 240) || info.img,
          // desc: body.result.info.list_desc,
          author: info.username
          // play_count: formatPlayCount(info.count),
        }
      };
    },
    async getUserListDetail3(chain, page) {
      const songInfo = await this.createHttp(`http://m.kugou.com/schain/transfer?pagesize=${this.listDetailLimit}&chain=${chain}&su=1&page=${page}&n=0.7928855356604456`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1"
        }
      });
      if (!songInfo.list) {
        if (songInfo.global_collection_id) return this.getUserListDetail2(songInfo.global_collection_id);
        else return this.getUserListDetail4(songInfo, chain, page).catch(() => this.getUserListDetail5(chain));
      }
      let list = await this.getMusicInfos(songInfo.list);
      return {
        list,
        page: 1,
        limit: this.listDetailLimit,
        total: list.length,
        source: "kg",
        info: {
          name: songInfo.info.name,
          img: songInfo.info.img,
          // desc: body.result.info.list_desc,
          author: songInfo.info.username
          // play_count: formatPlayCount(info.count),
        }
      };
    },
    deDuplication(datas) {
      let ids = /* @__PURE__ */ new Set();
      return datas.filter(({ hash }) => {
        if (ids.has(hash)) return false;
        ids.add(hash);
        return true;
      });
    },
    async decodeGcid(gcid) {
      const params = "dfid=-&appid=1005&mid=0&clientver=20109&clienttime=640612895&uuid=-";
      const body = {
        ret_info: 1,
        data: [
          {
            id: gcid,
            id_type: 2
          }
        ]
      };
      const result = await this.createHttp(`https://t.kugou.com/v1/songlist/batch_decode?${params}&signature=${signatureParams(params, "android", JSON.stringify(body))}`, {
        method: "POST",
        headers: {
          "User-Agent": "Mozilla/5.0 (Linux; Android 10; HUAWEI HMA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Mobile Safari/537.36",
          Referer: "https://m.kugou.com/"
        },
        body
      });
      return result.list[0].global_collection_id;
    },
    async getUserListDetailByLink({ info }, link) {
      let listInfo = info["0"];
      let total = listInfo.count;
      let tasks = [];
      let page = 0;
      while (total) {
        const limit = total > 90 ? 90 : total;
        total -= limit;
        page += 1;
        tasks.push(this.createHttp(link.replace(/pagesize=\d+/, "pagesize=" + limit).replace(/page=\d+/, "page=" + page), {
          headers: {
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
            Referer: link
          }
        }).then((data) => data.list.info));
      }
      let result = await Promise.all(tasks).then(([...datas]) => datas.flat());
      result = await this.getMusicInfos(result);
      return {
        list: result,
        page,
        limit: this.listDetailLimit,
        total: result.length,
        source: "kg",
        info: {
          name: listInfo.name,
          img: listInfo.pic && listInfo.pic.replace("{size}", 240),
          // desc: body.result.info.list_desc,
          author: listInfo.list_create_username
          // play_count: formatPlayCount(listInfo.count),
        }
      };
    },
    createGetListDetail2Task(id, total) {
      let tasks = [];
      let page = 0;
      while (total) {
        const limit = total > 300 ? 300 : total;
        total -= limit;
        page += 1;
        const params = "appid=1058&global_specialid=" + id + "&specialid=0&plat=0&version=8000&page=" + page + "&pagesize=" + limit + "&srcappid=2919&clientver=20000&clienttime=1586163263991&mid=1586163263991&uuid=1586163263991&dfid=-";
        tasks.push(this.createHttp(`https://mobiles.kugou.com/api/v5/special/song_v2?${params}&signature=${signatureParams(params, "web")}`, {
          headers: {
            mid: "1586163263991",
            Referer: "https://m3ws.kugou.com/share/index.php",
            "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
            dfid: "-",
            clienttime: "1586163263991"
          }
        }).then((data) => data.info));
      }
      return Promise.all(tasks).then(([...datas]) => datas.flat());
    },
    async getUserListDetail2(global_collection_id) {
      let id = global_collection_id;
      if (id.length > 1e3) throw new Error("get list error");
      const params = "appid=1058&specialid=0&global_specialid=" + id + "&format=jsonp&srcappid=2919&clientver=20000&clienttime=1586163242519&mid=1586163242519&uuid=1586163242519&dfid=-";
      let info = await this.createHttp(`https://mobiles.kugou.com/api/v5/special/info_v2?${params}&signature=${signatureParams(params, "web")}`, {
        headers: {
          mid: "1586163242519",
          Referer: "https://m3ws.kugou.com/share/index.php",
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
          dfid: "-",
          clienttime: "1586163242519"
        }
      });
      const songInfo = await this.createGetListDetail2Task(id, info.songcount);
      let list = await this.getMusicInfos(songInfo);
      return {
        list,
        page: 1,
        limit: this.listDetailLimit,
        total: list.length,
        source: "kg",
        info: {
          name: info.specialname,
          img: info.imgurl && info.imgurl.replace("{size}", 240),
          desc: info.intro,
          author: info.nickname,
          play_count: formatPlayCount(info.playcount)
        }
      };
    },
    async getListInfoByChain(chain) {
      if (this.cache.has(chain)) return this.cache.get(chain);
      const { body } = await httpFetch(`https://m.kugou.com/share/?chain=${chain}&id=${chain}`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1"
        }
      }).promise;
      let result = body.match(/var\sphpParam\s=\s({.+?});/);
      if (result) result = JSON.parse(result[1]);
      this.cache.set(chain, result);
      return result;
    },
    async getUserListDetailByPcChain(chain) {
      let key = `${chain}_pc_list`;
      if (this.cache.has(key)) return this.cache.get(key);
      const { body } = await httpFetch(`http://www.kugou.com/share/${chain}.html`, {
        headers: {
          "User-Agent": "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36"
        }
      }).promise;
      let result = body.match(/var\sdataFromSmarty\s=\s(\[.+?\])/);
      if (result) result = JSON.parse(result[1]);
      this.cache.set(chain, result);
      result = await this.getMusicInfos(result);
      return result;
    },
    async getUserListDetail4(songInfo, chain, page) {
      const limit = 100;
      const [listInfo, list] = await Promise.all([
        this.getListInfoByChain(chain),
        this.getUserListDetailById(songInfo.id, page, limit)
      ]);
      return {
        list: list || [],
        page,
        limit,
        total: list.length ?? 0,
        source: "kg",
        info: {
          name: listInfo.specialname,
          img: listInfo.imgurl && listInfo.imgurl.replace("{size}", 240),
          // desc: body.result.info.list_desc,
          author: listInfo.nickname
          // play_count: formatPlayCount(info.count),
        }
      };
    },
    async getUserListDetail5(chain) {
      const [listInfo, list] = await Promise.all([
        this.getListInfoByChain(chain),
        this.getUserListDetailByPcChain(chain)
      ]);
      return {
        list: list || [],
        page: 1,
        limit: this.listDetailLimit,
        total: list.length ?? 0,
        source: "kg",
        info: {
          name: listInfo.specialname,
          img: listInfo.imgurl && listInfo.imgurl.replace("{size}", 240),
          // desc: body.result.info.list_desc,
          author: listInfo.nickname
          // play_count: formatPlayCount(info.count),
        }
      };
    },
    async getUserListDetailById(id, page, limit) {
      const signature = await handleSignature(id, page, limit);
      let info = await this.createHttp(`https://pubsongscdn.kugou.com/v2/get_other_list_file?srcappid=2919&clientver=20000&appid=1058&type=0&module=playlist&page=${page}&pagesize=${limit}&specialid=${id}&signature=${signature}`, {
        headers: {
          Referer: "https://m3ws.kugou.com/share/index.php",
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",
          dfid: "-"
        }
      });
      let result = await this.getMusicInfos(info.info);
      return result;
    },
    async getUserListDetail(link, page, retryNum = 0) {
      if (retryNum > 3) return Promise.reject(new Error("link try max num"));
      if (link.includes("#")) link = link.replace(/#.*$/, "");
      if (link.includes("global_collection_id")) return this.getUserListDetail2(link.replace(/^.*?global_collection_id=(\w+)(?:&.*$|#.*$|$)/, "$1"));
      if (link.includes("gcid_")) {
        let gcid = link.match(/gcid_\w+/)?.[0];
        if (gcid) {
          const global_collection_id = await this.decodeGcid(gcid);
          if (global_collection_id) return this.getUserListDetail2(global_collection_id);
        }
      }
      if (link.includes("chain=")) return this.getUserListDetail3(link.replace(/^.*?chain=(\w+)(?:&.*$|#.*$|$)/, "$1"), page);
      if (link.includes(".html")) {
        if (link.includes("zlist.html")) {
          link = link.replace(/^(.*)zlist\.html/, "https://m3ws.kugou.com/zlist/list");
          if (link.includes("pagesize")) {
            link = link.replace("pagesize=30", "pagesize=" + this.listDetailLimit).replace("page=1", "page=" + page);
          } else {
            link += `&pagesize=${this.listDetailLimit}&page=${page}`;
          }
        } else if (!link.includes("song.html")) return this.getUserListDetail3(link.replace(/.+\/(\w+).html(?:\?.*|&.*$|#.*$|$)/, "$1"), page);
      }
      const requestObj_listDetailLink = httpFetch(link, {
        headers: {
          "User-Agent": "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1",
          Referer: link
        }
      });
      const { url: location, statusCode, body } = await requestObj_listDetailLink.promise;
      if (statusCode > 400) return this.getUserListDetail(link, page, ++retryNum);
      if (location.split("?")[0] != link.split("?")[0]) {
        if (location.includes("global_collection_id")) return this.getUserListDetail2(location.replace(/^.*?global_collection_id=(\w+)(?:&.*$|#.*$|$)/, "$1"));
        if (location.includes("gcid_")) {
          let gcid = link.match(/gcid_\w+/)?.[0];
          if (gcid) {
            const global_collection_id = await this.decodeGcid(gcid);
            if (global_collection_id) return this.getUserListDetail2(global_collection_id);
          }
        }
        if (location.includes("chain=")) return this.getUserListDetail3(location.replace(/^.*?chain=(\w+)(?:&.*$|#.*$|$)/, "$1"), page);
        if (location.includes(".html")) {
          if (location.includes("zlist.html")) {
            let link2 = location.replace(/^(.*)zlist\.html/, "https://m3ws.kugou.com/zlist/list");
            if (link2.includes("pagesize")) {
              link2 = link2.replace("pagesize=30", "pagesize=" + this.listDetailLimit).replace("page=1", "page=" + page);
            } else {
              link2 += `&pagesize=${this.listDetailLimit}&page=${page}`;
            }
            return this.getUserListDetail(link2, page, ++retryNum);
          } else return this.getUserListDetail3(location.replace(/.+\/(\w+).html(?:\?.*|&.*$|#.*$|$)/, "$1"), page);
        }
      }
      if (typeof body == "string") return this.getUserListDetail2(body.replace(/^[\s\S]+?"global_collection_id":"(\w+)"[\s\S]+?$/, "$1"));
      if (body.errcode !== 0) return this.getUserListDetail(link, page, ++retryNum);
      return this.getUserListDetailByLink(body, link);
    },
    async getListDetail(id, page) {
      id = id.toString();
      if (id.includes("special/single/")) {
        id = id.replace(this.regExps.listDetailLink, "$1");
      } else if (/https?:/.test(id)) {
        return this.getUserListDetail(id.replace(/^.*?http/, "http"), page);
      } else if (/^\d+$/.test(id)) {
        return this.getUserListDetailByCode(id);
      } else if (id.startsWith("id_")) {
        id = id.replace("id_", "");
      }
      return this.getListDetailBySpecialId(id, page);
    },
    filterData(rawList) {
      return rawList.map((item) => {
        const types = [];
        const _types = {};
        if (item.filesize !== 0) {
          let size = sizeFormate(item.filesize);
          types.push({ type: "128k", size, hash: item.hash });
          _types["128k"] = {
            size,
            hash: item.hash
          };
        }
        if (item.filesize_320 !== 0) {
          let size = sizeFormate(item.filesize_320);
          types.push({ type: "320k", size, hash: item.hash_320 });
          _types["320k"] = {
            size,
            hash: item.hash_320
          };
        }
        if (item.filesize_ape !== 0) {
          let size = sizeFormate(item.filesize_ape);
          types.push({ type: "ape", size, hash: item.hash_ape });
          _types.ape = {
            size,
            hash: item.hash_ape
          };
        }
        if (item.filesize_flac !== 0) {
          let size = sizeFormate(item.filesize_flac);
          types.push({ type: "flac", size, hash: item.hash_flac });
          _types.flac = {
            size,
            hash: item.hash_flac
          };
        }
        return {
          singer: decodeName(item.singername),
          name: decodeName(item.songname),
          albumName: decodeName(item.album_name),
          albumId: item.album_id,
          songmid: item.audio_id,
          source: "kg",
          interval: formatPlayTime(item.duration / 1e3),
          img: null,
          lrc: null,
          hash: item.hash,
          types,
          _types,
          typeUrl: {}
        };
      });
    },
    // getSinger(singers) {
    //   let arr = []
    //   singers?.forEach(singer => {
    //     arr.push(singer.name)
    //   })
    //   return arr.join('、')
    // },
    // v9 API
    // filterDatav9(rawList) {
    //   console.log(rawList)
    //   return rawList.map(item => {
    //     const types = []
    //     const _types = {}
    //     item.relate_goods.forEach(qualityObj => {
    //       if (qualityObj.level === 2) {
    //         let size = sizeFormate(qualityObj.size)
    //         types.push({ type: '128k', size, hash: qualityObj.hash })
    //         _types['128k'] = {
    //           size,
    //           hash: qualityObj.hash,
    //         }
    //       } else if (qualityObj.level === 4) {
    //         let size = sizeFormate(qualityObj.size)
    //         types.push({ type: '320k', size, hash: qualityObj.hash })
    //         _types['320k'] = {
    //           size,
    //           hash: qualityObj.hash,
    //         }
    //       } else if (qualityObj.level === 5) {
    //         let size = sizeFormate(qualityObj.size)
    //         types.push({ type: 'flac', size, hash: qualityObj.hash })
    //         _types.flac = {
    //           size,
    //           hash: qualityObj.hash,
    //         }
    //       } else if (qualityObj.level === 6) {
    //         let size = sizeFormate(qualityObj.size)
    //         types.push({ type: 'flac24bit', size, hash: qualityObj.hash })
    //         _types.flac24bit = {
    //           size,
    //           hash: qualityObj.hash,
    //         }
    //       }
    //     })
    //     const nameInfo = item.name.split(' - ')
    //     return {
    //       singer: this.getSinger(item.singerinfo),
    //       name: decodeName((nameInfo[1] ?? nameInfo[0]).trim()),
    //       albumName: decodeName(item.albuminfo.name),
    //       albumId: item.albuminfo.id,
    //       songmid: item.audio_id,
    //       source: 'kg',
    //       interval: formatPlayTime(item.timelen / 1000),
    //       img: null,
    //       lrc: null,
    //       hash: item.hash,
    //       types,
    //       _types,
    //       typeUrl: {},
    //     }
    //   })
    // },
    // hash list filter
    filterData2(rawList) {
      let ids = /* @__PURE__ */ new Set();
      let list = [];
      rawList.forEach((item) => {
        if (!item) return;
        if (ids.has(item.audio_info.audio_id)) return;
        ids.add(item.audio_info.audio_id);
        const types = [];
        const _types = {};
        if (item.audio_info.filesize !== "0") {
          let size = sizeFormate(parseInt(item.audio_info.filesize));
          types.push({ type: "128k", size, hash: item.audio_info.hash });
          _types["128k"] = {
            size,
            hash: item.audio_info.hash
          };
        }
        if (item.audio_info.filesize_320 !== "0") {
          let size = sizeFormate(parseInt(item.audio_info.filesize_320));
          types.push({ type: "320k", size, hash: item.audio_info.hash_320 });
          _types["320k"] = {
            size,
            hash: item.audio_info.hash_320
          };
        }
        if (item.audio_info.filesize_flac !== "0") {
          let size = sizeFormate(parseInt(item.audio_info.filesize_flac));
          types.push({ type: "flac", size, hash: item.audio_info.hash_flac });
          _types.flac = {
            size,
            hash: item.audio_info.hash_flac
          };
        }
        if (item.audio_info.filesize_high !== "0") {
          let size = sizeFormate(parseInt(item.audio_info.filesize_high));
          types.push({ type: "flac24bit", size, hash: item.audio_info.hash_high });
          _types.flac24bit = {
            size,
            hash: item.audio_info.hash_high
          };
        }
        list.push({
          singer: decodeName(item.author_name),
          name: decodeName(item.songname),
          albumName: decodeName(item.album_info.album_name),
          albumId: item.album_info.album_id,
          songmid: item.audio_info.audio_id,
          source: "kg",
          interval: formatPlayTime(parseInt(item.audio_info.timelength) / 1e3),
          img: null,
          lrc: null,
          hash: item.audio_info.hash,
          otherSource: null,
          types,
          _types,
          typeUrl: {}
        });
      });
      return list;
    },
    // 获取列表信息
    getListInfo(tagId, tryNum = 0) {
      if (this._requestObj_listInfo) this._requestObj_listInfo.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_listInfo = httpFetch(this.getInfoUrl(tagId));
      return this._requestObj_listInfo.promise.then(({ body }) => {
        if (body.status !== 1) return this.getListInfo(tagId, ++tryNum);
        return {
          limit: body.data.params.pagesize,
          page: body.data.params.p,
          total: body.data.params.total,
          source: "kg"
        };
      });
    },
    // 获取列表数据
    getList(sortId, tagId, page) {
      let tasks = [this.getSongList(sortId, tagId, page)];
      tasks.push(
        this.currentTagInfo.id === tagId ? Promise.resolve(this.currentTagInfo.info) : this.getListInfo(tagId).then((info) => {
          this.currentTagInfo.id = tagId;
          this.currentTagInfo.info = Object.assign({}, info);
          return info;
        })
      );
      if (!tagId && page === 1 && sortId === this.sortList[0].id) tasks.push(this.getSongListRecommend());
      return Promise.all(tasks).then(([list, info, recommendList]) => {
        if (recommendList) list.unshift(...recommendList);
        return {
          list,
          ...info
        };
      });
    },
    // 获取标签
    getTags(tryNum = 0) {
      if (this._requestObj_tags) this._requestObj_tags.cancelHttp();
      if (tryNum > 2) return Promise.reject(new Error("try max num"));
      this._requestObj_tags = httpFetch(this.getInfoUrl());
      return this._requestObj_tags.promise.then(({ body }) => {
        if (body.status !== 1) return this.getTags(++tryNum);
        return {
          hotTag: this.filterInfoHotTag(body.data.hotTag),
          tags: this.filterTagInfo(body.data.tagids),
          source: "kg"
        };
      });
    },
    getDetailPageUrl(id) {
      if (typeof id == "string") {
        if (/^https?:\/\//.test(id)) return id;
        id = id.replace("id_", "");
      }
      return `https://www.kugou.com/yy/special/single/${id}.html`;
    },
    search(text, page, limit = 20) {
      return httpFetch(`http://msearchretry.kugou.com/api/v3/search/special?keyword=${encodeURIComponent(text)}&page=${page}&pagesize=${limit}&showtype=10&filter=0&version=7910&sver=2`).promise.then(({ body }) => {
        if (body.errcode != 0) throw new Error("filed");
        return {
          list: body.data.info.map((item) => {
            return {
              play_count: formatPlayCount(item.playcount),
              id: "id_" + item.specialid,
              author: item.nickname,
              name: item.specialname,
              time: dateFormat(item.publishtime, "Y-M-D"),
              img: item.imgurl,
              grade: item.grade,
              desc: item.intro,
              total: item.songcount,
              source: "kg"
            };
          }),
          limit,
          total: body.data.total,
          source: "kg"
        };
      });
    }
  };

  // ../vendor/lx-sdk/kg/musicSearch.js
  init_buffer_inject();
  var musicSearch_default = {
    limit: 30,
    total: 0,
    page: 0,
    allPage: 1,
    musicSearch(str, page, limit) {
      const searchRequest = httpFetch(`https://songsearch.kugou.com/song_search_v2?keyword=${encodeURIComponent(str)}&page=${page}&pagesize=${limit}&userid=0&clientver=&platform=WebFilter&filter=2&iscorrection=1&privilege_filter=0&area_code=1`);
      return searchRequest.promise.then(({ body }) => body);
    },
    filterData(rawData) {
      const types = [];
      const _types = {};
      if (rawData.FileSize !== 0) {
        let size = sizeFormate(rawData.FileSize);
        types.push({ type: "128k", size, hash: rawData.FileHash });
        _types["128k"] = {
          size,
          hash: rawData.FileHash
        };
      }
      if (rawData.HQFileSize !== 0) {
        let size = sizeFormate(rawData.HQFileSize);
        types.push({ type: "320k", size, hash: rawData.HQFileHash });
        _types["320k"] = {
          size,
          hash: rawData.HQFileHash
        };
      }
      if (rawData.SQFileSize !== 0) {
        let size = sizeFormate(rawData.SQFileSize);
        types.push({ type: "flac", size, hash: rawData.SQFileHash });
        _types.flac = {
          size,
          hash: rawData.SQFileHash
        };
      }
      if (rawData.ResFileSize !== 0) {
        let size = sizeFormate(rawData.ResFileSize);
        types.push({ type: "flac24bit", size, hash: rawData.ResFileHash });
        _types.flac24bit = {
          size,
          hash: rawData.ResFileHash
        };
      }
      return {
        singer: decodeName(formatSingerName(rawData.Singers, "name")),
        name: decodeName(rawData.SongName),
        albumName: decodeName(rawData.AlbumName),
        albumId: rawData.AlbumID,
        songmid: rawData.Audioid,
        source: "kg",
        interval: formatPlayTime(rawData.Duration),
        _interval: rawData.Duration,
        img: null,
        lrc: null,
        otherSource: null,
        hash: rawData.FileHash,
        types,
        _types,
        typeUrl: {}
      };
    },
    handleResult(rawData) {
      let ids = /* @__PURE__ */ new Set();
      const list = [];
      rawData.forEach((item) => {
        const key = item.Audioid + item.FileHash;
        if (ids.has(key)) return;
        ids.add(key);
        list.push(this.filterData(item));
        for (const childItem of item.Grp) {
          const key2 = item.Audioid + item.FileHash;
          if (ids.has(key2)) continue;
          ids.add(key2);
          list.push(this.filterData(childItem));
        }
      });
      return list;
    },
    search(str, page = 1, limit, retryNum = 0) {
      if (++retryNum > 3) return Promise.reject(new Error("try max num"));
      if (limit == null) limit = this.limit;
      return this.musicSearch(str, page, limit).then((result) => {
        if (!result || result.error_code !== 0) return this.search(str, page, limit, retryNum);
        let list = this.handleResult(result.data.lists);
        if (list == null) return this.search(str, page, limit, retryNum);
        this.total = result.data.total;
        this.page = page;
        this.allPage = Math.ceil(this.total / limit);
        return Promise.resolve({
          list,
          allPage: this.allPage,
          limit,
          total: this.total,
          source: "kg"
        });
      });
    }
  };

  // ../vendor/lx-sdk/kg/pic.js
  init_buffer_inject();
  var pic_default = {
    getPic(songInfo) {
      const requestObj = httpFetch(
        "http://media.store.kugou.com/v1/get_res_privilege",
        {
          method: "POST",
          headers: {
            "KG-RC": 1,
            "KG-THash": "expand_search_manager.cpp:852736169:451",
            "User-Agent": "KuGou2012-9020-ExpandSearchManager"
          },
          body: {
            appid: 1001,
            area_code: "1",
            behavior: "play",
            clientver: "9020",
            need_hash_offset: 1,
            relate: 1,
            resource: [
              {
                album_audio_id: songInfo.songmid.length == 32 ? songInfo.audioId.split("_")[0] : songInfo.songmid,
                album_id: songInfo.albumId,
                hash: songInfo.hash,
                id: 0,
                name: `${songInfo.singer} - ${songInfo.name}.mp3`,
                type: "audio"
              }
            ],
            token: "",
            userid: 2626431536,
            vip: 1
          }
        }
      );
      return requestObj.promise.then(({ body }) => {
        if (body.error_code !== 0) return Promise.reject(new Error("\u56FE\u7247\u83B7\u53D6\u5931\u8D25"));
        let info = body.data[0].info;
        const img = info.imgsize ? info.image.replace("{size}", info.imgsize[0]) : info.image;
        if (!img) return Promise.reject(new Error("Pic get failed"));
        return img;
      });
    }
  };

  // ../vendor/lx-sdk/kg/lyric.js
  init_buffer_inject();
  var headExp = /^.*\[id:\$\w+\]\n/;
  var parseLyric = (str) => {
    str = str.replace(/\r/g, "");
    if (headExp.test(str)) str = str.replace(headExp, "");
    let trans = str.match(/\[language:([\w=\\/+]+)\]/);
    let lyric;
    let rlyric;
    let tlyric;
    if (trans) {
      str = str.replace(/\[language:[\w=\\/+]+\]\n/, "");
      let json = JSON.parse(Buffer2.from(trans[1], "base64").toString());
      for (const item of json.content) {
        switch (item.type) {
          case 0:
            rlyric = item.lyricContent;
            break;
          case 1:
            tlyric = item.lyricContent;
            break;
        }
      }
    }
    let i = 0;
    let lxlyric = str.replace(/\[((\d+),\d+)\].*/g, (str2) => {
      let result = str2.match(/\[((\d+),\d+)\].*/);
      let time = parseInt(result[2]);
      let ms = time % 1e3;
      time /= 1e3;
      let m = parseInt(time / 60).toString().padStart(2, "0");
      time %= 60;
      let s = parseInt(time).toString().padStart(2, "0");
      time = `${m}:${s}.${ms}`;
      if (rlyric) rlyric[i] = `[${time}]${rlyric[i]?.join("") ?? ""}`;
      if (tlyric) tlyric[i] = `[${time}]${tlyric[i]?.join("") ?? ""}`;
      i++;
      return str2.replace(result[1], time);
    });
    rlyric = rlyric ? rlyric.join("\n") : "";
    tlyric = tlyric ? tlyric.join("\n") : "";
    lxlyric = lxlyric.replace(/<(\d+,\d+),\d+>/g, "<$1>");
    lxlyric = decodeName(lxlyric);
    lyric = lxlyric.replace(/<\d+,\d+>/g, "");
    rlyric = decodeName(rlyric);
    tlyric = decodeName(tlyric);
    return {
      lyric,
      tlyric,
      rlyric,
      lxlyric
    };
  };
  var lyric_default = {
    getIntv(interval) {
      if (!interval) return 0;
      let intvArr = interval.split(":");
      let intv = 0;
      let unit = 1;
      while (intvArr.length) {
        intv += intvArr.pop() * unit;
        unit *= 60;
      }
      return parseInt(intv);
    },
    // getLyric(songInfo, tryNum = 0) {
    //   let requestObj = httpFetch(`http://m.kugou.com/app/i/krc.php?cmd=100&keyword=${encodeURIComponent(songInfo.name)}&hash=${songInfo.hash}&timelength=${songInfo._interval || this.getIntv(songInfo.interval)}&d=0.38664927426725626`, {
    //     headers: {
    //       'KG-RC': 1,
    //       'KG-THash': 'expand_search_manager.cpp:852736169:451',
    //       'User-Agent': 'KuGou2012-9020-ExpandSearchManager',
    //     },
    //   })
    //   requestObj.promise = requestObj.promise.then(({ body, statusCode }) => {
    //     if (statusCode !== 200) {
    //       if (tryNum > 5) return Promise.reject(new Error('歌词获取失败'))
    //       let tryRequestObj = this.getLyric(songInfo, ++tryNum)
    //       requestObj.cancelHttp = tryRequestObj.cancelHttp.bind(tryRequestObj)
    //       return tryRequestObj.promise
    //     }
    //     return {
    //       lyric: body,
    //       tlyric: '',
    //     }
    //   })
    //   return requestObj
    // },
    searchLyric(name, hash, time, tryNum = 0) {
      let requestObj = httpFetch(`http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword=${encodeURIComponent(name)}&hash=${hash}&timelength=${time}&lrctxt=1`, {
        headers: {
          "KG-RC": 1,
          "KG-THash": "expand_search_manager.cpp:852736169:451",
          "User-Agent": "KuGou2012-9020-ExpandSearchManager"
        }
      });
      requestObj.promise = requestObj.promise.then(({ body, statusCode }) => {
        if (statusCode !== 200) {
          if (tryNum > 5) return Promise.reject(new Error("\u6B4C\u8BCD\u83B7\u53D6\u5931\u8D25"));
          let tryRequestObj = this.searchLyric(name, hash, time, ++tryNum);
          requestObj.cancelHttp = tryRequestObj.cancelHttp.bind(tryRequestObj);
          return tryRequestObj.promise;
        }
        if (body.candidates.length) {
          let info = body.candidates[0];
          return { id: info.id, accessKey: info.accesskey, fmt: info.krctype == 1 && info.contenttype != 1 ? "krc" : "lrc" };
        }
        return null;
      });
      return requestObj;
    },
    getLyricDownload(id, accessKey, fmt, tryNum = 0) {
      let requestObj = httpFetch(`http://lyrics.kugou.com/download?ver=1&client=pc&id=${id}&accesskey=${accessKey}&fmt=${fmt}&charset=utf8`, {
        headers: {
          "KG-RC": 1,
          "KG-THash": "expand_search_manager.cpp:852736169:451",
          "User-Agent": "KuGou2012-9020-ExpandSearchManager"
        }
      });
      requestObj.promise = requestObj.promise.then(({ body, statusCode }) => {
        if (statusCode !== 200) {
          if (tryNum > 5) return Promise.reject(new Error("\u6B4C\u8BCD\u83B7\u53D6\u5931\u8D25"));
          let tryRequestObj = this.getLyric(id, accessKey, fmt, ++tryNum);
          requestObj.cancelHttp = tryRequestObj.cancelHttp.bind(tryRequestObj);
          return tryRequestObj.promise;
        }
        switch (body.fmt) {
          case "krc":
            return decodeLyric(body.content).then((result) => parseLyric(result));
          case "lrc":
            return {
              lyric: Buffer2.from(body.content, "base64").toString("utf-8"),
              tlyric: "",
              rlyric: "",
              lxlyric: ""
            };
          default:
            return Promise.reject(new Error(`\u672A\u77E5\u6B4C\u8BCD\u683C\u5F0F: ${body.fmt}`));
        }
      });
      return requestObj;
    },
    getLyric(songInfo, tryNum = 0) {
      let requestObj = this.searchLyric(songInfo.name, songInfo.hash, songInfo._interval || this.getIntv(songInfo.interval));
      requestObj.promise = requestObj.promise.then((result) => {
        if (!result) return Promise.reject(new Error("Get lyric failed"));
        let requestObj2 = this.getLyricDownload(result.id, result.accessKey, result.fmt);
        requestObj.cancelHttp = requestObj2.cancelHttp.bind(requestObj2);
        return requestObj2.promise;
      });
      return requestObj;
    }
  };

  // ../vendor/lx-sdk/kg/hotSearch.js
  init_buffer_inject();
  var hotSearch_default = {
    _requestObj: null,
    async getList(retryNum = 0) {
      if (this._requestObj) this._requestObj.cancelHttp();
      if (retryNum > 2) return Promise.reject(new Error("try max num"));
      const _requestObj = httpFetch("http://gateway.kugou.com/api/v3/search/hot_tab?signature=ee44edb9d7155821412d220bcaf509dd&appid=1005&clientver=10026&plat=0", {
        method: "get",
        cache: null,
        headers: {
          dfid: "1ssiv93oVqMp27cirf2CvoF1",
          mid: "156798703528610303473757548878786007104",
          clienttime: 1584257267,
          "x-router": "msearch.kugou.com",
          "user-agent": "Android9-AndroidPhone-10020-130-0-searchrecommendprotocol-wifi",
          "kg-rc": 1
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.errcode !== 0) throw new Error("\u83B7\u53D6\u70ED\u641C\u8BCD\u5931\u8D25");
      return { source: "kg", list: this.filterList(body.data.list) };
    },
    filterList(rawList) {
      const list = [];
      rawList.forEach((item) => {
        item.keywords.map((k) => list.push(decodeName(k.keyword)));
      });
      return list;
    }
  };

  // ../vendor/lx-sdk/kg/comment.js
  init_buffer_inject();
  var comment_default = {
    _requestObj: null,
    _requestObj2: null,
    async getComment({ hash }, page = 1, limit = 20) {
      if (this._requestObj) this._requestObj.cancelHttp();
      let timestamp = Date.now();
      const params = `dfid=0&mid=16249512204336365674023395779019&clienttime=${timestamp}&uuid=0&extdata=${hash}&appid=1005&code=fc4be23b4e972707f36b8a828a93ba8a&schash=${hash}&clientver=11409&p=${page}&clienttoken=&pagesize=${limit}&ver=10&kugouid=0`;
      const _requestObj = httpFetch(`http://m.comment.service.kugou.com/r/v1/rank/newest?${params}&signature=${signatureParams(params)}`, {
        cache: "default",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.24"
        }
      });
      const { body, statusCode } = await _requestObj.promise;
      if (statusCode != 200 || body.err_code !== 0) throw new Error("\u83B7\u53D6\u8BC4\u8BBA\u5931\u8D25");
      const total = body.count ?? 0;
      return { source: "kg", comments: this.filterComment(body.list || []), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    async getHotComment({ hash }, page = 1, limit = 20) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      let timestamp = Date.now();
      const params = `dfid=0&mid=16249512204336365674023395779019&clienttime=${timestamp}&uuid=0&extdata=${hash}&appid=1005&code=fc4be23b4e972707f36b8a828a93ba8a&schash=${hash}&clientver=11409&p=${page}&clienttoken=&pagesize=${limit}&ver=10&kugouid=0`;
      const _requestObj2 = httpFetch(`http://m.comment.service.kugou.com/r/v1/rank/topliked?${params}&signature=${signatureParams(params)}`, {
        cache: "default",
        headers: {
          "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.24"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.err_code !== 0) throw new Error("\u83B7\u53D6\u70ED\u95E8\u8BC4\u8BBA\u5931\u8D25");
      const total = body.count ?? 0;
      return { source: "kg", comments: this.filterComment(body.list || []), total, page, limit, maxPage: Math.ceil(total / limit) || 1 };
    },
    async getReplyComment({ songmid, audioId }, replyId, page = 1, limit = 100) {
      if (this._requestObj2) this._requestObj2.cancelHttp();
      songmid = songmid.length == 32 ? audioId.split("_")[0] : songmid;
      const _requestObj2 = httpFetch(`http://comment.service.kugou.com/index.php?r=commentsv2/getReplyWithLike&code=fc4be23b4e972707f36b8a828a93ba8a&p=${page}&pagesize=${limit}&ver=1.01&clientver=8373&kugouid=687373022&need_show_image=1&appid=1001&childrenid=${songmid}&tid=${replyId}`, {
        headers: {
          "User-Agent": "Android712-AndroidPhone-8983-18-0-COMMENT-wifi"
        }
      });
      const { body, statusCode } = await _requestObj2.promise;
      if (statusCode != 200 || body.err_code !== 0) throw new Error("\u83B7\u53D6\u56DE\u590D\u8BC4\u8BBA\u5931\u8D25");
      return { source: "kg", comments: this.filterComment(body.list || []) };
    },
    replaceAt(raw, atList) {
      atList.forEach((atobj) => {
        raw = raw.replaceAll(`[at=${atobj.id}]`, `@${atobj.name} `);
      });
      return raw;
    },
    filterComment(rawList) {
      return rawList.map((item) => {
        let data = {
          id: item.id,
          text: decodeName((item.atlist ? this.replaceAt(item.content, item.atlist) : item.content) || ""),
          images: item.images ? item.images.map((i) => i.url) : [],
          location: item.location,
          time: item.addtime,
          timeStr: dateFormat2(new Date(item.addtime).getTime()),
          userName: item.user_name,
          avatar: item.user_pic,
          userId: item.user_id,
          likedCount: item.like.likenum,
          replyNum: item.reply_num,
          reply: []
        };
        return item.pcontent ? {
          id: item.id,
          text: decodeName(item.pcontent),
          time: null,
          userName: item.puser,
          avatar: null,
          userId: item.puser_id,
          likedCount: null,
          replyNum: null,
          reply: [data]
        } : data;
      });
    }
  };

  // ../vendor/lx-sdk/kg/index.js
  var kg = {
    // tipSearch,
    leaderboard: leaderboard_default,
    songList: songList_default,
    musicSearch: musicSearch_default,
    hotSearch: hotSearch_default,
    comment: comment_default,
    getMusicUrl(songInfo, type) {
      return apis("kg").getMusicUrl(songInfo, type);
    },
    getLyric(songInfo) {
      return lyric_default.getLyric(songInfo);
    },
    // getLyric(songInfo) {
    //   return apis('kg').getLyric(songInfo)
    // },
    getPic(songInfo) {
      return pic_default.getPic(songInfo);
    },
    getMusicDetailPageUrl(songInfo) {
      return `https://www.kugou.com/song/#hash=${songInfo.hash}&album_id=${songInfo.albumId}`;
    }
    // getPic(songInfo) {
    //   return apis('kg').getPic(songInfo)
    // },
  };
  var index_default = kg;
  return __toCommonJS(index_exports);
})();
globalThis.__lyn_source_kg = (typeof __lynSource !== 'undefined' && __lynSource.default) ? __lynSource.default : __lynSource;
