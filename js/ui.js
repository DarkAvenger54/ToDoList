import { state } from "./state.js";

export const qs = (selector, scope = document) => scope.querySelector(selector);
export const qsa = (selector, scope = document) =>
  Array.from(scope.querySelectorAll(selector));

export function escapeHtml(value) {
  const div = document.createElement("div");
  div.textContent = value ?? "";
  return div.innerHTML;
}

export function showToast(message, type = "info") {
  if (!message) return;
  const container = qs("#snackbar-container");
  if (!container) return;
  const snack = document.createElement("div");
  snack.className = `snackbar ${type}`;
  snack.textContent = message;
  container.appendChild(snack);
  setTimeout(() => {
    snack.style.opacity = "0";
    snack.style.transform = "translateY(6px)";
    setTimeout(() => snack.remove(), 300);
  }, 3800);
}

export function setTheme(theme) {
  const nextTheme = theme === "dark" ? "dark" : "light";
  document.documentElement.setAttribute("data-theme", nextTheme);
  state.theme = nextTheme;
  localStorage.setItem("theme", nextTheme);
  const toggle = qs("#theme-toggle");
  if (toggle) toggle.textContent = nextTheme === "dark" ? "Light" : "Dark";
}

export function setActiveNav(hashPath) {
  const normalized = hashPath.startsWith("#/groups/") ? "#/groups" : hashPath;
  const links = qsa("[data-link]");
  links.forEach((link) => {
    const target = link.getAttribute("data-link");
    link.classList.toggle("active", target === normalized);
  });
}

export function renderPagination(container, page, onPageChange) {
  if (!container || !page) return;
  container.innerHTML = "";
  const totalPages = page.totalPages || 0;
  if (totalPages <= 1) return;

  const current = page.number ?? 0;
  const createButton = (label, pageIndex, isActive = false) => {
    const btn = document.createElement("button");
    btn.textContent = label;
    if (isActive) btn.classList.add("active");
    btn.disabled = pageIndex === current;
    btn.addEventListener("click", () => onPageChange(pageIndex));
    return btn;
  };

  if (current > 0) {
    container.appendChild(createButton("Prev", current - 1));
  }

  const start = Math.max(0, current - 2);
  const end = Math.min(totalPages - 1, current + 2);
  for (let i = start; i <= end; i += 1) {
    container.appendChild(createButton(`${i + 1}`, i, i === current));
  }

  if (current < totalPages - 1) {
    container.appendChild(createButton("Next", current + 1));
  }
}

export function formatDateTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString();
}

export function formatDate(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString();
}

export function toInputDateTime(value) {
  if (!value) return "";
  return value.length >= 16 ? value.slice(0, 16) : value;
}

export function openDialog(dialogId) {
  const dialog = qs(dialogId);
  if (dialog && !dialog.open) {
    dialog.showModal();
  }
}

export function closeDialog(dialogId) {
  const dialog = qs(dialogId);
  if (dialog && dialog.open) {
    dialog.close();
  }
}

let confirmResolver = null;

export function confirmDialog({ title, message, confirmText = "Confirm" }) {
  const dialog = qs("#confirm-dialog");
  if (!dialog) return Promise.resolve(false);
  qs("#confirm-title").textContent = title || "Confirm";
  qs("#confirm-message").textContent = message || "";
  const confirmButton = qs("#confirm-yes");
  confirmButton.textContent = confirmText;
  dialog.addEventListener(
    "close",
    () => {
      if (confirmResolver) resolveConfirm(false);
    },
    { once: true }
  );
  openDialog("#confirm-dialog");
  return new Promise((resolve) => {
    confirmResolver = resolve;
  });
}

export function resolveConfirm(value) {
  if (confirmResolver) {
    confirmResolver(value);
    confirmResolver = null;
  }
  closeDialog("#confirm-dialog");
}

let promptResolver = null;

export function promptDialog({ title, message, placeholder = "" }) {
  const dialog = qs("#prompt-dialog");
  if (!dialog) return Promise.resolve("");
  qs("#prompt-title").textContent = title || "Provide value";
  qs("#prompt-message").textContent = message || "";
  const input = qs("#prompt-input");
  input.value = "";
  input.placeholder = placeholder;
  dialog.addEventListener(
    "close",
    () => {
      if (promptResolver) resolvePrompt("");
    },
    { once: true }
  );
  openDialog("#prompt-dialog");
  return new Promise((resolve) => {
    promptResolver = resolve;
  });
}

export function resolvePrompt(value) {
  if (promptResolver) {
    promptResolver(value);
    promptResolver = null;
  }
  closeDialog("#prompt-dialog");
}
