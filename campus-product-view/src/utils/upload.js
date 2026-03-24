import { API_BASE_URL } from "@/utils/request";
import { getToken } from "@/utils/storage";

/**
 * Common upload endpoint for file/image upload.
 */
export const FILE_UPLOAD_ACTION = `${API_BASE_URL}/file/upload`;

/**
 * Build upload headers for non-axios upload components (e.g. el-upload action mode).
 */
export function getUploadHeaders() {
  const token = getToken();
  return token ? { token } : {};
}

/**
 * Normalize upload response into a usable file URL string.
 * Supports:
 * - direct url string
 * - { data: "url" }
 * - { url/path/fileUrl: "url" }
 * - { data: { url/path/fileUrl: "url" } }
 */
export function resolveUploadUrl(payload) {
  if (!payload) return "";
  if (typeof payload === "string") return payload.trim();
  if (typeof payload !== "object") return "";

  const data = payload.data;
  const candidate =
    (typeof data === "string" && data) ||
    payload.url ||
    payload.path ||
    payload.fileUrl ||
    (data &&
      typeof data === "object" &&
      (data.url || data.path || data.fileUrl)) ||
    "";

  return typeof candidate === "string" ? candidate.trim() : "";
}

/**
 * Resolve a readable error message for upload operations.
 */
export function getUploadErrorMessage(error, fallback = "Upload failed") {
  const responseData =
    error && error.response && error.response.data ? error.response.data : null;
  const message =
    (responseData && (responseData.msg || responseData.message)) ||
    (error && error.message) ||
    "";
  return message || fallback;
}

/**
 * Upload a file via axios instance and return normalized file URL.
 */
export async function uploadFileWithAxios({
  axiosInstance,
  file,
  endpoint = "/file/upload",
  fieldName = "file",
  extraData = null
}) {
  if (!axiosInstance) {
    throw new Error("axiosInstance is required");
  }
  if (!file) {
    throw new Error("file is required");
  }

  const formData = new FormData();
  formData.append(fieldName, file);
  if (extraData && typeof extraData === "object") {
    Object.keys(extraData).forEach(key => {
      const value = extraData[key];
      if (value !== undefined && value !== null) {
        formData.append(key, value);
      }
    });
  }

  const res = await axiosInstance.post(endpoint, formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });
  const body = res && res.data ? res.data : null;
  if (!body || body.code !== 200) {
    throw new Error(
      (body && (body.msg || body.message)) || "Upload failed, please retry"
    );
  }

  const url = resolveUploadUrl(body);
  if (!url) {
    throw new Error("Upload succeeded but returned file URL is empty");
  }
  return url;
}
