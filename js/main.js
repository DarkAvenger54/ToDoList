import { initRouter, navigate } from "./router.js";
import { state, clearAuth } from "./state.js";
import { setTheme, qs, qsa, setActiveNav, resolveConfirm, resolvePrompt } from "./ui.js";
import { initAuth, handleConfirmEmail, setResetToken } from "./pages/auth.js";
import { initTasks, loadTasks } from "./pages/tasks.js";
import { initGroups, loadGroups } from "./pages/groups.js";
import { initGroupDetail, loadGroupDetail } from "./pages/groupDetail.js";
import { initFriends, loadFriends } from "./pages/friends.js";
import {
  initNotifications,
  loadNotifications,
  refreshUnreadCount,
} from "./pages/notifications.js";
import { initSettings } from "./pages/settings.js";
import { initAI, loadGroupsForAi, clearAiResults } from "./pages/ai.js";

const PRIVATE_VIEWS = new Set([
  "tasks",
  "groups",
  "group-detail",
  "friends",
  "notifications",
  "settings",
  "ai",
]);

let confirmToken = null;

const ROUTE_TITLES = {
  login: "Sign In",
  register: "Create Account",
  "confirm-email": "Confirm Email",
  "forgot-password": "Recover Password",
  "reset-password": "Reset Password",
  tasks: "Personal Tasks",
  groups: "Groups",
  "group-detail": "Group Detail",
  friends: "Friends",
  notifications: "Notifications",
  settings: "Settings",
  ai: "AI Task Suggestions",
};

function setView(name) {
  qsa(".view").forEach((view) => {
    view.classList.toggle("active", view.dataset.view === name);
  });
  const title = qs("#route-title");
  if (title) title.textContent = ROUTE_TITLES[name] || "ToDoList";
}

function updateAuthUI() {
  const isAuthed = Boolean(state.token);
  qsa("[data-auth='private']").forEach((el) => {
    el.classList.toggle("hidden", !isAuthed);
  });
  qsa("[data-auth='public']").forEach((el) => {
    el.classList.toggle("hidden", isAuthed);
  });
  const userLabel = qs("#current-user");
  if (userLabel) {
    userLabel.textContent = state.username
      ? `Logged as ${state.username}`
      : "Logged as —";
  }
}

function handleRoute(route) {
  const isAuthed = Boolean(state.token);

  if (route.name === "home" || route.name === "not-found") {
    navigate(isAuthed ? "/tasks" : "/login");
    return;
  }

  if (PRIVATE_VIEWS.has(route.name) && !isAuthed) {
    navigate("/login");
    return;
  }

  if (
    !PRIVATE_VIEWS.has(route.name) &&
    isAuthed &&
    route.name !== "reset-password" &&
    route.name !== "confirm-email"
  ) {
    navigate("/tasks");
    return;
  }

  setView(route.name);
  setActiveNav(`#${route.path}`);

  if (route.name === "tasks") {
    loadTasks(0);
  }
  if (route.name === "groups") {
    loadGroups();
  }
  if (route.name === "group-detail") {
    loadGroupDetail(route.params.id);
  }
  if (route.name === "friends") {
    loadFriends();
  }
  if (route.name === "notifications") {
    loadNotifications(0);
  }
  if (route.name === "ai") {
    loadGroupsForAi();
  }
  if (route.name === "reset-password") {
    const token = route.query.get("token");
    if (token) setResetToken(token);
  }
  if (route.name === "confirm-email") {
    const token =
      confirmToken || new URLSearchParams(window.location.search).get("token");
    if (token) {
      handleConfirmEmail(token);
      confirmToken = null;
    }
  }

  if (isAuthed) {
    refreshUnreadCount();
  }
}

function handleConfirmEmailFromQuery() {
  const params = new URLSearchParams(window.location.search);
  const token = params.get("token");
  if (token) {
    confirmToken = token;
    setView("confirm-email");
    history.replaceState({}, document.title, window.location.pathname);
    window.location.hash = "#/confirm-email";
  }
}

function bindGlobalEvents() {
  document.addEventListener("click", (event) => {
    const link = event.target.closest("[data-link]");
    if (link) {
      const target = link.getAttribute("data-link");
      if (target) {
        window.location.hash = target;
        event.preventDefault();
      }
    }
  });

  const themeToggle = qs("#theme-toggle");
  if (themeToggle) {
    themeToggle.addEventListener("click", () => {
      setTheme(state.theme === "dark" ? "light" : "dark");
    });
  }

  const logoutButton = qs("#logout-button");
  if (logoutButton) {
    logoutButton.addEventListener("click", () => {
      clearAuth();
      updateAuthUI();
      navigate("/login");
    });
  }

  const confirmYes = qs("#confirm-yes");
  const confirmNo = qs("#confirm-no");
  if (confirmYes) confirmYes.addEventListener("click", () => resolveConfirm(true));
  if (confirmNo) confirmNo.addEventListener("click", () => resolveConfirm(false));

  const promptOk = qs("#prompt-ok");
  const promptCancel = qs("#prompt-cancel");
  if (promptOk) {
    promptOk.addEventListener("click", () => {
      resolvePrompt(qs("#prompt-input").value.trim());
    });
  }
  if (promptCancel) promptCancel.addEventListener("click", () => resolvePrompt(""));

  window.addEventListener("auth:logout", () => {
    updateAuthUI();
    clearAiResults();
    navigate("/login");
  });

  window.addEventListener("auth:verify-required", () => {
    clearAuth();
    updateAuthUI();
    navigate("/login");
  });

  window.addEventListener("auth:login", () => {
    updateAuthUI();
    clearAiResults();
    refreshUnreadCount();
    loadTasks(0);
  });
}

function init() {
  setTheme(state.theme);
  updateAuthUI();
  initAuth();
  initTasks();
  initGroups();
  initGroupDetail();
  initFriends();
  initNotifications();
  initSettings();
  initAI();
  bindGlobalEvents();
  handleConfirmEmailFromQuery();
  initRouter(handleRoute);
  setInterval(() => {
    if (state.token) refreshUnreadCount();
  }, 10000);
}

init();
