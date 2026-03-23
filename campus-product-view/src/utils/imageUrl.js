/**
 * 统一处理后端返回的图片地址：
 * 1. 兼容完整 URL、相对路径、仅文件访问路径
 * 2. 兼容当前项目与历史项目的 API 前缀
 * 3. 始终转成当前页面 origin 下的可访问地址，避免 localhost/旧域名写死
 */
const IMAGE_BASE = "/api/campus-product-sys/v1.0";
const LEGACY_API_PREFIXES = [IMAGE_BASE, "/api/book-manage-sys-api/v1.0"];

function normalizeApiPath(pathname) {
  if (!pathname || typeof pathname !== "string") return "";

  const normalized = pathname.startsWith("/") ? pathname : `/${pathname}`;
  const matchedPrefix = LEGACY_API_PREFIXES.find(prefix =>
    normalized.startsWith(prefix)
  );

  if (matchedPrefix) {
    return normalized.replace(matchedPrefix, IMAGE_BASE);
  }

  if (normalized.startsWith("/api/")) {
    return normalized;
  }

  return `${IMAGE_BASE}${normalized}`;
}

export function toFullImageUrl(url) {
  if (!url || typeof url !== "string") return "";

  const trimmed = url.trim();
  if (!trimmed) return "";

  const origin = window.location.origin;

  try {
    const isAbsolute = /^https?:\/\//i.test(trimmed);
    const resolved = new URL(trimmed, origin);
    const pathname = resolved.pathname || "";
    const search = resolved.search || "";
    const isApiAsset =
      pathname.startsWith("/api/") || pathname.startsWith("/file/");

    if (isAbsolute && !isApiAsset) {
      return trimmed;
    }

    const normalizedPath = normalizeApiPath(pathname);
    if (!normalizedPath) {
      return isAbsolute ? trimmed : "";
    }

    return origin + normalizedPath + search;
  } catch (_) {
    const normalizedPath = normalizeApiPath(trimmed);
    return normalizedPath ? origin + normalizedPath : trimmed;
  }
}

export function normalizeHtmlImageUrls(html) {
  if (!html || typeof html !== "string") return "";
  if (typeof window === "undefined") return html;

  const rewriteByRegex = source =>
    source.replace(
      /(<img\b[^>]*?\bsrc=["'])([^"']+)(["'][^>]*>)/gi,
      (match, prefix, src, suffix) => {
        const normalized = toFullImageUrl(src);
        return normalized ? `${prefix}${normalized}${suffix}` : match;
      }
    );

  if (typeof window.DOMParser === "undefined") {
    return rewriteByRegex(html);
  }

  try {
    const parser = new window.DOMParser();
    const doc = parser.parseFromString(html, "text/html");

    doc.querySelectorAll("img[src]").forEach(img => {
      const normalized = toFullImageUrl(img.getAttribute("src"));
      if (normalized) {
        img.setAttribute("src", normalized);
      }
    });

    return doc.body.innerHTML;
  } catch (_) {
    return rewriteByRegex(html);
  }
}
