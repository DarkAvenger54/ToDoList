import { apiFetch } from "../api.js";
import { qs, showToast, confirmDialog } from "../ui.js";

export function initFriends() {
  const form = qs("#friend-request-form");
  if (form) {
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const username = qs("#friend-username").value.trim();
      if (!username) {
        showToast("Username is required.", "error");
        return;
      }
      await apiFetch("/api/friends/request", {
        method: "POST",
        body: { username },
      });
      qs("#friend-username").value = "";
      showToast("Friend request sent.", "success");
    });
  }

  const list = qs("#friends-list");
  if (list) {
    list.addEventListener("click", async (event) => {
      const deleteButton = event.target.closest("[data-action='remove']");
      if (deleteButton) {
        const id = deleteButton.dataset.id;
        const confirmed = await confirmDialog({
          title: "Remove friend",
          message: "This friend will be removed from your list.",
          confirmText: "Remove",
        });
        if (confirmed) {
          await apiFetch(`/api/friends/${id}`, { method: "DELETE" });
          showToast("Friend removed.", "success");
          loadFriends();
        }
      }
    });
  }
}

export async function loadFriends() {
  const friends = await apiFetch("/api/friends");
  renderFriends(friends || []);
}

function renderFriends(friends) {
  const list = qs("#friends-list");
  if (!list) return;
  list.innerHTML = "";
  if (!friends.length) {
    list.innerHTML =
      "<div class='empty-state'>No friends yet. Send a request to connect.</div>";
    return;
  }

  friends.forEach((friend) => {
    const item = document.createElement("div");
    item.className = "list-item";
    const info = document.createElement("div");
    const name = document.createElement("strong");
    name.textContent = friend.username;
    const meta = document.createElement("div");
    meta.className = "meta";
    meta.textContent = friend.email;
    info.append(name, meta);
    const actions = document.createElement("div");
    const remove = document.createElement("button");
    remove.className = "button small ghost";
    remove.textContent = "Remove";
    remove.dataset.action = "remove";
    remove.dataset.id = friend.id;
    actions.appendChild(remove);
    item.append(info, actions);
    list.appendChild(item);
  });
}
