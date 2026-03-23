import axios from "axios";
import { Message } from "element-ui";
import { getToken, clearToken } from "@/utils/storage.js";
import router from "@/router";

// 使用相对路径，开发时通过 vue.config.js 代理转发，部署时与前端同源
export const API_BASE_URL = "/api/campus-product-sys/v1.0";

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 8000
});

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = getToken();
    if (token !== null && token !== "") {
      config.headers["token"] = token;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 响应拦截器：认证失败时清除 token 并跳转登录
request.interceptors.response.use(
  response => {
    const data = response.data;
    if (data && data.code !== 200) {
      const msg = data.msg || data.message || "";
      const isAuthError =
        msg.indexOf("登录") !== -1 || msg.indexOf("身份认证") !== -1;
      if (isAuthError) {
        clearToken();
        router.push({
          path: "/login",
          query: { redirect: router.currentRoute.fullPath }
        });
      } else if (msg) {
        // 统一业务错误提示
        Message.error(msg);
      }
    }
    return response;
  },
  error => {
    const res = error.response;
    if (res && res.status === 401) {
      clearToken();
      router.push({
        path: "/login",
        query: { redirect: router.currentRoute.fullPath }
      });
      return Promise.reject(error);
    }
    if (res && res.data) {
      const msg = res.data.msg || res.data.message || "";
      const isAuthError =
        (res.data.code !== 200 && msg.indexOf("登录") !== -1) ||
        msg.indexOf("身份认证") !== -1;
      if (isAuthError) {
        clearToken();
        router.push({
          path: "/login",
          query: { redirect: router.currentRoute.fullPath }
        });
      } else if (msg) {
        Message.error(msg);
      }
    } else {
      // 网络错误等情况统一提示
      Message.error("网络异常，请稍后重试");
    }
    return Promise.reject(error);
  }
);

export default request;
