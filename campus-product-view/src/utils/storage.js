const TOKEN_KEY = "token";
const USER_INFO = "userInfo";
const SEARCH_KEY = "searchKey";
const PRODUCT_INFO = "productInfo";

function parseSessionJSON(key) {
  const rawValue = sessionStorage.getItem(key);
  if (!rawValue) {
    return null;
  }

  try {
    return JSON.parse(rawValue);
  } catch (error) {
    console.warn(`failed to parse session storage for key: ${key}`, error);
    return null;
  }
}

export function getSearchKey() {
  return sessionStorage.getItem(SEARCH_KEY);
}

export function setSearchKey(key) {
  sessionStorage.setItem(SEARCH_KEY, key || "");
}

export function clearSearchKey() {
  sessionStorage.removeItem(SEARCH_KEY);
}

export function getUserInfo() {
  return parseSessionJSON(USER_INFO);
}

export function setUserInfo(userInfo) {
  sessionStorage.setItem(USER_INFO, JSON.stringify(userInfo || null));
}

export function getProductInfo() {
  return parseSessionJSON(PRODUCT_INFO);
}

export function setProductInfo(productInfo) {
  sessionStorage.setItem(PRODUCT_INFO, JSON.stringify(productInfo || null));
}

export function clearProductInfo() {
  sessionStorage.removeItem(PRODUCT_INFO);
}

export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token);
}

export function clearToken() {
  sessionStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(USER_INFO);
  sessionStorage.removeItem("role");
  clearSearchKey();
  clearProductInfo();
}
