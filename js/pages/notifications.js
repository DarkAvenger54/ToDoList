import { apiFetch } from "../api.js";
import { state } from "../state.js";
import {
  qs,
  showToast,
  renderPagination,
  formatDateTime,
  confirmDialog,
} from "../ui.js";
import { navigate } from "../router.js";

let lastPage = 0;
const pageSize = 20;
const acceptedRequests = new Set();
const rejectedRequests = new Set();

function setAcceptedState(button, notificationId) {
  if (!button) return;
  button.textContent = "Accepted";
  button.disabled = true;
  button.classList.add("is-accepted");
  if (Number.isFinite(notificationId)) {
    acceptedRequests.add(notificationId);
  }
}

function setRejectedState(button, notificationId) {
  if (!button) return;
  button.textContent = "Rejected";
  button.disabled = true;
  button.classList.add("is-rejected");
  if (Number.isFinite(notificationId)) {
    rejectedRequests.add(notificationId);
    acceptedRequests.delete(notificationId);
  }
}

export function initNotifications() {
  const toggle = qs("#notifications-unread-toggle");
  if (toggle) {
    toggle.addEventListener("change", () => loadNotifications(0));
  }

  const markAll = qs("#notifications-mark-all");
  if (markAll) {
    markAll.addEventListener("click", async () => {
      await apiFetch("/api/notifications/mark-all-read", { method: "POST" });
      showToast("All notifications marked as read.", "success");
      loadNotifications(lastPage);
      refreshUnreadCount();
    });
  }

  const deleteRead = qs("#notifications-delete-read");
  if (deleteRead) {
    deleteRead.addEventListener("click", async () => {
      const confirmed = await confirmDialog({
        title: "Delete read notifications",
        message: "This will permanently remove all read notifications.",
        confirmText: "Delete",
      });
      if (!confirmed) return;
      await apiFetch("/api/notifications/read", { method: "DELETE" });
      showToast("Read notifications deleted.", "success");
      loadNotifications(0);
      refreshUnreadCount();
    });
  }

  const list = qs("#notifications-list");
  if (list) {
    list.addEventListener("click", async (event) => {
      const markButton = event.target.closest("[data-action='mark-read']");
      const acceptFriend = event.target.closest("[data-action='accept-friend']");
      const acceptInvite = event.target.closest("[data-action='accept-invite']");
      const rejectInvite = event.target.closest("[data-action='reject-invite']");
      const openTasks = event.target.closest("[data-action='open-tasks']");

      if (markButton) {
        const id = markButton.dataset.id;
        await apiFetch("/api/notifications/mark-read", {
          method: "POST",
          body: { notificationId: Number(id) },
        });
        showToast("Notification marked as read.", "success");
        loadNotifications(lastPage);
        refreshUnreadCount();
      }

      if (acceptFriend) {
        const id = acceptFriend.dataset.refId;
        const notificationId = Number(acceptFriend.dataset.id);
        const originalText = acceptFriend.textContent;
        acceptFriend.disabled = true;
        try {
          await apiFetch(`/api/friends/accept/${id}`, { method: "POST" });
          if (Number.isFinite(notificationId)) {
            await apiFetch("/api/notifications/mark-read", {
              method: "POST",
              body: { notificationId },
            });
          }
          setAcceptedState(acceptFriend, notificationId);
          showToast("Friend request accepted.", "success");
          loadNotifications(lastPage);
          refreshUnreadCount();
        } catch {
          acceptFriend.disabled = false;
          acceptFriend.textContent = originalText;
          acceptFriend.classList.remove("is-accepted");
        }
      }

      if (acceptInvite) {
        const id = acceptInvite.dataset.refId;
        const notificationId = Number(acceptInvite.dataset.id);
        const originalText = acceptInvite.textContent;
        acceptInvite.disabled = true;
        try {
          await apiFetch(`/api/group-invites/${id}/accept`, { method: "POST" });
          if (Number.isFinite(notificationId)) {
            await apiFetch("/api/notifications/mark-read", {
              method: "POST",
              body: { notificationId },
            });
          }
          setAcceptedState(acceptInvite, notificationId);
          showToast("Group invite accepted.", "success");
          loadNotifications(lastPage);
          refreshUnreadCount();
        } catch {
          acceptInvite.disabled = false;
          acceptInvite.textContent = originalText;
          acceptInvite.classList.remove("is-accepted");
        }
      }

      if (rejectInvite) {
        const id = rejectInvite.dataset.refId;
        const notificationId = Number(rejectInvite.dataset.id);
        const originalText = rejectInvite.textContent;
        const acceptButton = rejectInvite
          .closest(".notification-item")
          ?.querySelector("[data-action='accept-invite']");
        const confirmed = await confirmDialog({
          title: "Reject invite",
          message: "Do you want to reject this group invite?",
          confirmText: "Reject",
        });
        if (!confirmed) return;
        rejectInvite.disabled = true;
        if (acceptButton) acceptButton.disabled = true;
        try {
          await apiFetch(`/api/group-invites/${id}/reject`, { method: "POST" });
          if (Number.isFinite(notificationId)) {
            await apiFetch("/api/notifications/mark-read", {
              method: "POST",
              body: { notificationId },
            });
          }
          setRejectedState(rejectInvite, notificationId);
          if (acceptButton) acceptButton.remove();
          showToast("Invite rejected.", "success");
          loadNotifications(lastPage);
          refreshUnreadCount();
        } catch {
          rejectInvite.disabled = false;
          rejectInvite.textContent = originalText;
          rejectInvite.classList.remove("is-rejected");
          if (acceptButton) acceptButton.disabled = false;
        }
      }

      if (openTasks) {
        navigate("/tasks");
      }
    });
  }
}

export async function loadNotifications(page = 0) {
  const unreadOnly = qs("#notifications-unread-toggle")?.checked || false;
  const params = new URLSearchParams({
    page: `${page}`,
    size: `${pageSize}`,
    unreadOnly: unreadOnly ? "true" : "false",
  });
  const data = await apiFetch(`/api/notifications?${params.toString()}`);
  lastPage = data.number ?? 0;
  renderNotifications(data.content || []);
  renderPagination(qs("#notifications-pagination"), data, loadNotifications);
}

export async function refreshUnreadCount() {
  if (!state.token) return;
  const previous = state.unreadCount;
  const count = await apiFetch("/api/notifications/unread-count");
  state.unreadCount = Number(count) || 0;
  const badge = qs("#notification-badge");
  const button = qs("#notifications-button");
  if (badge) {
    badge.textContent = `${state.unreadCount}`;
    badge.classList.toggle("hidden", state.unreadCount === 0);
  }
  if (button) {
    button.classList.toggle("has-unread", state.unreadCount > 0);
  }

  const view = qs("#view-notifications");
  if (view && view.classList.contains("active") && state.unreadCount !== previous) {
    await loadNotifications(lastPage);
  }
}

function renderNotifications(listData) {
  const list = qs("#notifications-list");
  if (!list) return;
  list.innerHTML = "";
  if (!listData.length) {
    list.innerHTML =
      "<div class='empty-state'>No notifications to show.</div>";
    return;
  }

  listData.forEach((note) => {
    const item = document.createElement("div");
    item.className = "list-item notification-item";
    item.classList.toggle("is-read", note.read);
    item.classList.toggle("is-unread", !note.read);
    const info = document.createElement("div");
    info.className = "notification-content";
    const titleRow = document.createElement("div");
    titleRow.className = "notification-title-row";
    const title = document.createElement("strong");
    title.className = "notification-title";
    title.textContent = note.title || note.type;
    if (!note.read) {
      const dot = document.createElement("span");
      dot.className = "notification-dot";
      dot.setAttribute("aria-label", "Unread");
      titleRow.append(dot, title);
    } else {
      titleRow.append(title);
    }
    const message = document.createElement("div");
    message.className = "meta";
    message.textContent = note.message || "";
    const time = document.createElement("div");
    time.className = "meta";
    time.textContent = formatDateTime(note.createdAt);
    info.append(titleRow, message, time);

    const actions = document.createElement("div");
    if (!note.read) {
      const mark = document.createElement("button");
      mark.className = "button small ghost";
      mark.textContent = "Mark read";
      mark.dataset.action = "mark-read";
      mark.dataset.id = note.id;
      actions.appendChild(mark);
    }

    if (note.type === "FRIEND_REQUEST_RECEIVED" && note.refId) {
      const accept = document.createElement("button");
      accept.className = "button small";
      accept.dataset.refId = note.refId;
      accept.dataset.id = note.id;
      if (acceptedRequests.has(note.id)) {
        setAcceptedState(accept, note.id);
      } else {
        accept.textContent = "Accept";
        accept.dataset.action = "accept-friend";
      }
      actions.appendChild(accept);
    }

    if (note.type === "GROUP_INVITE_RECEIVED" && note.refId) {
      const accept = document.createElement("button");
      accept.className = "button small";
      accept.dataset.refId = note.refId;
      accept.dataset.id = note.id;
      if (acceptedRequests.has(note.id)) {
        setAcceptedState(accept, note.id);
        actions.append(accept);
      } else if (rejectedRequests.has(note.id)) {
        setRejectedState(accept, note.id);
        actions.append(accept);
      } else {
        accept.textContent = "Accept";
        accept.dataset.action = "accept-invite";
        const reject = document.createElement("button");
        reject.className = "button small ghost";
        reject.textContent = "Reject";
        reject.dataset.action = "reject-invite";
        reject.dataset.refId = note.refId;
        reject.dataset.id = note.id;
        actions.append(accept, reject);
      }
    }

    const isTaskDueSoon =
      note.type === "TASK_DUE_SOON" || note.type === "TASK_DUE_SOON_5";
    if ((note.type === "TASK_ASSIGNED_TO_YOU" || isTaskDueSoon) && note.refId) {
      const openTasks = document.createElement("button");
      openTasks.className = "button small";
      openTasks.textContent = "Open tasks";
      openTasks.dataset.action = "open-tasks";
      actions.appendChild(openTasks);
    }

    item.append(info, actions);
    list.appendChild(item);
  });
}
