export const state = {
  token: localStorage.getItem("jwtToken") || "",
  user: null,
  username: null,
  theme: localStorage.getItem("theme") || "light",
  unreadCount: 0,
  groups: [],
  selectedGroupId: null,
  lastLoginValue: "",
  groupRole: null,
};

export function parseJwt(token) {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    let base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const pad = base64.length % 4;
    if (pad) {
      base64 += "=".repeat(4 - pad);
    }
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((char) => `%${("00" + char.charCodeAt(0).toString(16)).slice(-2)}`)
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function setToken(token) {
  state.token = token || "";
  if (state.token) {
    localStorage.setItem("jwtToken", state.token);
  } else {
    localStorage.removeItem("jwtToken");
  }
  hydrateUserFromToken();
}

export function hydrateUserFromToken() {
  if (!state.token) {
    state.user = null;
    state.username = null;
    return;
  }
  state.user = parseJwt(state.token) || null;
  state.username =
    state.user?.username ||
    state.user?.sub ||
    state.user?.preferred_username ||
    state.user?.email ||
    null;
}

export function clearAuth() {
  setToken("");
  state.unreadCount = 0;
  state.groups = [];
  state.selectedGroupId = null;
  state.groupRole = null;
}

hydrateUserFromToken();
