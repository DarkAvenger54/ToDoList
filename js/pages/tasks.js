import { apiFetch } from "../api.js";
import { state } from "../state.js";
import {
  qs,
  qsa,
  showToast,
  openDialog,
  closeDialog,
  confirmDialog,
  renderPagination,
  formatDateTime,
  toInputDateTime,
} from "../ui.js";

let editingTaskId = null;
let lastPage = 0;
const pageSize = 10;

export function initTasks() {
  const filter = qs("#tasks-filter");
  if (filter) {
    filter.addEventListener("change", () => loadTasks(0));
  }

  const createButton = qs("#task-create-button");
  if (createButton) {
    createButton.addEventListener("click", () => openTaskDialog());
  }

  const form = qs("#task-form");
  if (form) {
    form.addEventListener("submit", handleTaskSubmit);
  }

  const list = qs("#tasks-list");
  if (list) {
    list.addEventListener("click", async (event) => {
      const editButton = event.target.closest("[data-action='edit']");
      const deleteButton = event.target.closest("[data-action='delete']");
      if (editButton) {
        const id = editButton.dataset.id;
        await openTaskDialogById(id);
      }
      if (deleteButton) {
        const id = deleteButton.dataset.id;
        const confirmed = await confirmDialog({
          title: "Delete task",
          message: "This task will be removed permanently.",
          confirmText: "Delete",
        });
        if (confirmed) {
          await apiFetch(`/api/tasks/${id}`, { method: "DELETE" });
          showToast("Task deleted.", "success");
          loadTasks(lastPage);
        }
      }
    });
  }
}

export async function loadTasks(page = 0) {
  const filter = qs("#tasks-filter");
  const status = filter?.value || "";
  const params = new URLSearchParams({
    page: `${page}`,
    size: `${pageSize}`,
  });
  if (status) params.append("status", status);

  const data = await apiFetch(`/api/tasks?${params.toString()}`);
  lastPage = data.number ?? 0;
  renderTasks(data.content || []);
  renderPagination(qs("#tasks-pagination"), data, loadTasks);
}

function renderTasks(tasks) {
  const list = qs("#tasks-list");
  if (!list) return;
  list.innerHTML = "";
  if (!tasks.length) {
    list.innerHTML =
      "<div class='empty-state'>No tasks found. Create one to get started.</div>";
    return;
  }

  const sorted = [...tasks].sort((a, b) => {
    if (!a.dueAt && !b.dueAt) return 0;
    if (!a.dueAt) return 1;
    if (!b.dueAt) return -1;
    return a.dueAt.localeCompare(b.dueAt);
  });

  sorted.forEach((task) => {
    const card = document.createElement("div");
    card.className = "card task-card";
    const isOverdue =
      task.dueAt &&
      new Date(task.dueAt) < new Date() &&
      !["DONE", "CANCELLED"].includes(task.status);
    if (isOverdue) card.classList.add("overdue");

    const title = document.createElement("h3");
    title.textContent = task.title;

    const desc = document.createElement("p");
    desc.textContent = task.description || "No description.";

    const meta = document.createElement("div");
    meta.className = "task-meta";
    const statusChip = document.createElement("span");
    statusChip.className = "chip status";
    statusChip.textContent = task.status;
    const priorityChip = document.createElement("span");
    priorityChip.className = `chip priority ${task.priority?.toLowerCase() || ""}`;
    priorityChip.textContent = task.priority || "NONE";
    meta.append(statusChip, priorityChip);

    if (task.dueAt) {
      const due = document.createElement("span");
      due.textContent = `Due ${formatDateTime(task.dueAt)}`;
      meta.appendChild(due);
    }

    const actions = document.createElement("div");
    actions.className = "inline-actions";
    const edit = document.createElement("button");
    edit.className = "button small";
    edit.textContent = "Edit";
    edit.dataset.action = "edit";
    edit.dataset.id = task.id;
    const del = document.createElement("button");
    del.className = "button small ghost";
    del.textContent = "Delete";
    del.dataset.action = "delete";
    del.dataset.id = task.id;
    actions.append(edit, del);

    card.append(title, desc, meta, actions);
    list.appendChild(card);
  });
}

async function openTaskDialogById(id) {
  const task = await apiFetch(`/api/tasks/${id}`);
  openTaskDialog(task);
}

function openTaskDialog(task = null) {
  editingTaskId = task?.id || null;
  qs("#task-dialog-title").textContent = task ? "Edit task" : "New task";
  qs("#task-title").value = task?.title || "";
  qs("#task-description").value = task?.description || "";
  qs("#task-priority").value = task?.priority || "";
  qs("#task-status").value = task?.status || "TODO";
  qs("#task-dueAt").value = toInputDateTime(task?.dueAt);
  qs("#task-clear-due").checked = false;

  const statusGroup = qs("#task-status-group");
  const clearGroup = qs("#task-clear-group");
  statusGroup.classList.toggle("hidden", !task);
  clearGroup.classList.toggle("hidden", !task);

  openDialog("#task-dialog");
}

async function handleTaskSubmit(event) {
  event.preventDefault();
  const title = qs("#task-title").value.trim();
  const descriptionValue = qs("#task-description").value.trim();
  const description = descriptionValue ? descriptionValue : null;
  const priority = qs("#task-priority").value || null;
  const status = qs("#task-status").value || null;
  const dueAtValue = qs("#task-dueAt").value;
  const dueAt = dueAtValue ? dueAtValue : null;
  const clearDueAt = qs("#task-clear-due").checked;

  if (!title) {
    showToast("Title is required.", "error");
    return;
  }

  if (editingTaskId) {
    await apiFetch(`/api/tasks/${editingTaskId}`, {
      method: "PUT",
      body: {
        title,
        description,
        status,
        priority,
        dueAt,
        clearDueAt: clearDueAt || null,
      },
    });
    showToast("Task updated.", "success");
  } else {
    await apiFetch("/api/tasks", {
      method: "POST",
      body: {
        title,
        description,
        priority,
        dueAt,
      },
    });
    showToast("Task created.", "success");
  }
  closeDialog("#task-dialog");
  loadTasks(lastPage);
}
