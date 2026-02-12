export function parseHash() {
  const raw = window.location.hash.replace(/^#/, "");
  const [pathPart, queryString] = raw.split("?");
  const path = pathPart || "/";
  const query = new URLSearchParams(queryString || "");
  return { path, query };
}

export function matchRoute(path) {
  const segments = path.split("/").filter(Boolean);
  if (segments.length === 0) return { name: "home", params: {} };

  if (segments[0] === "groups" && segments[1]) {
    return { name: "group-detail", params: { id: segments[1] } };
  }

  const nameMap = {
    login: "login",
    register: "register",
    "confirm-email": "confirm-email",
    "forgot-password": "forgot-password",
    "reset-password": "reset-password",
    tasks: "tasks",
    groups: "groups",
    friends: "friends",
    notifications: "notifications",
    settings: "settings",
    ai: "ai",
  };

  return { name: nameMap[segments[0]] || "not-found", params: {} };
}

export function navigate(path) {
  window.location.hash = `#${path}`;
}

export function initRouter(onRoute) {
  const handle = () => {
    const { path, query } = parseHash();
    const match = matchRoute(path);
    onRoute({ ...match, path, query });
  };
  window.addEventListener("hashchange", handle);
  window.addEventListener("load", handle);
  handle();
}
