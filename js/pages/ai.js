import { apiFetch } from "../api.js";
import { state } from "../state.js";
import {
  qs,
  showToast,
  formatDateTime,
  promptDialog,
} from "../ui.js";

let isThinking = false;

export function clearAiResults() {
  const container = qs("#ai-results");
  if (container) container.innerHTML = "";
  const command = qs("#ai-command");
  if (command) command.value = "";
  setAiThinking(false);
  isThinking = false;
}

export function initAI() {
  const scopeSelect = qs("#ai-scope");
  if (scopeSelect) {
    scopeSelect.addEventListener("change", updateScopeVisibility);
  }

  const form = qs("#ai-form");
  if (form) {
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      if (isThinking) return;
      const command = qs("#ai-command").value.trim();
      const scope = qs("#ai-scope").value || null;
      const groupIdValue = qs("#ai-group").value;
      const groupId = scope === "GROUP" ? Number(groupIdValue) || null : null;
      const maxTasksValue = qs("#ai-max").value;
      const maxTasks = maxTasksValue ? Number(maxTasksValue) : null;
      if (!command) {
        showToast("Describe what you need help with.", "error");
        return;
      }

      const results = qs("#ai-results");
      if (results) results.innerHTML = "";
      setAiThinking(true);
      isThinking = true;
      try {
        const data = await apiFetch("/api/ai/tasks/suggest", {
          method: "POST",
          body: {
            command,
            scope: scope || null,
            groupId,
            maxTasks,
          },
        });
        renderSuggestions(data, scope, groupId);
      } finally {
        isThinking = false;
        setAiThinking(false);
      }
    });
  }

  updateScopeVisibility();
}

export async function loadGroupsForAi() {
  if (!state.groups.length) {
    state.groups = await apiFetch("/api/groups");
  }
  const select = qs("#ai-group");
  if (!select) return;
  select.innerHTML = "<option value=''>Select group</option>";
  state.groups.forEach((group) => {
    const option = document.createElement("option");
    option.value = group.id;
    option.textContent = group.name;
    select.appendChild(option);
  });
}

function updateScopeVisibility() {
  const scope = qs("#ai-scope").value;
  const groupRow = qs("#ai-group-row");
  if (groupRow) {
    groupRow.classList.toggle("hidden", scope !== "GROUP");
  }
}

function setAiThinking(isLoading) {
  const status = qs("#ai-status");
  if (status) status.classList.toggle("hidden", !isLoading);
  const form = qs("#ai-form");
  if (form) {
    form.setAttribute("aria-busy", isLoading ? "true" : "false");
    const submitButton = form.querySelector("button[type='submit']");
    if (submitButton) {
      if (!submitButton.dataset.label) {
        submitButton.dataset.label = submitButton.textContent.trim();
      }
      submitButton.disabled = isLoading;
      submitButton.textContent = isLoading
        ? "Thinking..."
        : submitButton.dataset.label;
    }
  }
}

function renderSuggestions(data, scope, groupId) {
  const container = qs("#ai-results");
  if (!container) return;
  container.innerHTML = "";
  if (!data?.tasks?.length) {
    container.innerHTML =
      "<div class='empty-state'>No suggestions returned.</div>";
    return;
  }

  const summary = document.createElement("div");
  summary.className = "panel";
  summary.textContent = data.summary || "AI suggestions ready.";
  container.appendChild(summary);

  data.tasks.forEach((task) => {
    const card = document.createElement("div");
    card.className = "card";
    const title = document.createElement("h3");
    title.textContent = task.title;
    const desc = document.createElement("p");
    desc.textContent = task.description || "No description.";
    const meta = document.createElement("div");
    meta.className = "task-meta";
    if (task.priority) {
      const chip = document.createElement("span");
      chip.className = `chip priority ${task.priority.toLowerCase()}`;
      chip.textContent = task.priority;
      meta.appendChild(chip);
    }
    if (task.dueAtIso) {
      const due = document.createElement("span");
      due.textContent = `Due ${formatDateTime(task.dueAtIso)}`;
      meta.appendChild(due);
    }

    const actions = document.createElement("div");
    actions.className = "inline-actions";
    const addPersonal = document.createElement("button");
    addPersonal.className = "button small";
    addPersonal.textContent = "Add personal task";
    addPersonal.addEventListener("click", async () => {
      await apiFetch("/api/tasks", {
        method: "POST",
        body: {
          title: task.title,
          description: task.description || null,
          priority: task.priority || null,
          dueAt: task.dueAtIso || null,
        },
      });
      showToast("Personal task created.", "success");
    });
    actions.appendChild(addPersonal);

    if (scope === "GROUP" && groupId) {
      const createForAll = document.createElement("button");
      createForAll.className = "button small ghost";
      createForAll.textContent = "Create for all";
      createForAll.addEventListener("click", async () => {
        await apiFetch(`/api/groups/${groupId}/tasks/for-all`, {
          method: "POST",
          body: {
            title: task.title,
            description: task.description || null,
            priority: task.priority || null,
            dueAt: task.dueAtIso || null,
          },
        });
        showToast("Group task created.", "success");
      });
      actions.appendChild(createForAll);

      const assign = document.createElement("button");
      assign.className = "button small ghost";
      assign.textContent = "Assign to user";
      assign.addEventListener("click", async () => {
        const username = await promptDialog({
          title: "Assign to user",
          message: "Enter the assignee username.",
          placeholder: "username",
        });
        if (!username) return;
        await apiFetch(`/api/groups/${groupId}/tasks/assign`, {
          method: "POST",
          body: {
            assigneeUsername: username,
            title: task.title,
            description: task.description || null,
            priority: task.priority || null,
            dueAt: task.dueAtIso || null,
            visibleInGroup: true,
          },
        });
        showToast("Task assigned.", "success");
      });
      actions.appendChild(assign);
    }

    card.append(title, desc, meta, actions);
    container.appendChild(card);
  });
}
