import { apiFetch } from "../api.js";
import { state } from "../state.js";
import { qs, showToast } from "../ui.js";
import { navigate } from "../router.js";

export function initGroups() {
  const createForm = qs("#group-create-form");
  if (createForm) {
    createForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const name = qs("#group-name").value.trim();
      if (!name) {
        showToast("Group name is required.", "error");
        return;
      }
      await apiFetch("/api/groups", {
        method: "POST",
        body: { name },
      });
      qs("#group-name").value = "";
      showToast("Group created.", "success");
      loadGroups();
    });
  }
}

export async function loadGroups() {
  const list = qs("#groups-list");
  if (list) list.innerHTML = "";
  const groups = await apiFetch("/api/groups");
  state.groups = groups || [];
  renderGroups(state.groups);
}

function renderGroups(groups) {
  const list = qs("#groups-list");
  if (!list) return;
  list.innerHTML = "";
  if (!groups.length) {
    list.innerHTML =
      "<div class='empty-state'>No groups yet. Create one to collaborate.</div>";
    return;
  }

  groups.forEach((group) => {
    const item = document.createElement("div");
    item.className = "list-item";

    const info = document.createElement("div");
    const name = document.createElement("div");
    name.textContent = group.name;
    const meta = document.createElement("div");
    meta.className = "meta";
    meta.textContent = `Owner: ${group.ownerUsername}`;
    info.append(name, meta);

    const actions = document.createElement("div");
    const button = document.createElement("button");
    button.className = "button small";
    button.textContent = "Open";
    button.addEventListener("click", () => {
      navigate(`/groups/${group.id}`);
    });
    actions.appendChild(button);

    item.append(info, actions);
    list.appendChild(item);
  });
}
