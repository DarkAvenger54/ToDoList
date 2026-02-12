import { API_BASE } from "./config.js";
import { clearAuth, state } from "./state.js";
import { showToast } from "./ui.js";

export class ApiError extends Error {
  constructor(message, status, data) {
    super(message);
    this.status = status;
    this.data = data;
  }
}

function isEmailVerificationError(message) {
  const msg = (message || "").toLowerCase();
  return (
    msg.includes("disabled") ||
    msg.includes("not verified") ||
    (msg.includes("email") && msg.includes("confirm"))
  );
}

function isAuthInvalid(message) {
  const msg = (message || "").toLowerCase();
  return (
    msg.includes("jwt") ||
    msg.includes("token") ||
    msg.includes("expired") ||
    msg.includes("unauthorized")
  );
}

export async function apiFetch(
  path,
  { method = "GET", body = null, auth = true, headers = {} } = {}
) {
  const options = { method, headers: { ...headers } };
  if (body !== null && body !== undefined) {
    options.body = JSON.stringify(body);
    options.headers["Content-Type"] = "application/json";
  }
  if (auth && state.token) {
    options.headers.Authorization = `Bearer ${state.token}`;
  }

  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, options);
  } catch (error) {
    showToast("Network error. Please try again.", "error");
    throw new ApiError("Network error", 0, null);
  }
  const contentType = response.headers.get("content-type") || "";
  let data = null;
  if (contentType.includes("application/json")) {
    data = await response.json().catch(() => null);
  } else {
    data = await response.text().catch(() => "");
  }

  if (!response.ok) {
    const message =
      (data && (data.message || data.error || data.detail)) ||
      (typeof data === "string" && data) ||
      `Request failed (${response.status})`;

    if (response.status === 401 || (response.status === 403 && isAuthInvalid(message))) {
      clearAuth();
      window.dispatchEvent(new CustomEvent("auth:logout"));
      showToast("Session expired. Please sign in again.", "error");
    } else if (response.status === 403 && isEmailVerificationError(message)) {
      window.dispatchEvent(
        new CustomEvent("auth:verify-required", { detail: message })
      );
    } else {
      showToast(message, "error");
    }
    throw new ApiError(message, response.status, data);
  }

  return data;
}
