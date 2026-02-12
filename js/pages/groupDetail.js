import { apiFetch } from "../api.js";
import { state } from "../state.js";
import { navigate } from "../router.js";
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

const pageSize = 10;
let currentGroupId = null;
let currentTab = "mine";
let lastPage = 0;
let editingTaskId = null;

export function initGroupDetail() {
  qsa("[data-group-tab]").forEach((button) => {
    button.addEventListener("click", () => {
      currentTab = button.dataset.groupTab;
      setActiveTab();
      updateGroupPanels();
      if (currentTab !== "create") {
        loadGroupTasks(0);
      }
    });
  });

  const renameForm = qs("#group-rename-form");
  if (renameForm) {
    renameForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const name = qs("#group-rename").value.trim();
      if (!name) {
        showToast("Group name is required.", "error");
        return;
      }
      await apiFetch(`/api/groups/${currentGroupId}/name`, {
        method: "PUT",
        body: { name },
      });
      showToast("Group renamed.", "success");
      loadGroupDetail(currentGroupId);
    });
  }

  const inviteForm = qs("#group-invite-form");
  if (inviteForm) {
    inviteForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const username = qs("#group-invite-username").value.trim();
      if (!username) {
        showToast("Username is required.", "error");
        return;
      }
      await apiFetch(
        `/api/groups/${currentGroupId}/invites/${encodeURIComponent(username)}`,
        { method: "POST" }
      );
      qs("#group-invite-username").value = "";
      showToast("Invitation sent.", "success");
    });
  }

  const leaveButton = qs("#group-leave-button");
  if (leaveButton) {
    leaveButton.addEventListener("click", async () => {
      const confirmed = await confirmDialog({
        title: "Leave group",
        message: "You will lose access to this group.",
        confirmText: "Leave",
      });
      if (confirmed) {
        await apiFetch(`/api/groups/${currentGroupId}/leave`, { method: "POST" });
        showToast("Left the group.", "success");
        navigate("/groups");
      }
    });
  }

  const deleteButton = qs("#group-delete-button");
  if (deleteButton) {
    deleteButton.addEventListener("click", async () => {
      const confirmed = await confirmDialog({
        title: "Delete group",
        message: "This action will remove the group permanently.",
        confirmText: "Delete",
      });
      if (confirmed) {
        await apiFetch(`/api/groups/${currentGroupId}`, { method: "DELETE" });
        showToast("Group deleted.", "success");
        navigate("/groups");
      }
    });
  }

  const assignForm = qs("#group-assign-form");
  if (assignForm) {
    assignForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const assigneeUsername = qs("#group-assign-username").value.trim();
      const title = qs("#group-assign-title").value.trim();
      const descriptionValue = qs("#group-assign-description").value.trim();
      const description = descriptionValue ? descriptionValue : null;
      const priority = qs("#group-assign-priority").value || null;
      const dueAtValue = qs("#group-assign-dueAt").value;
      const dueAt = dueAtValue || null;
      const visibleInGroup = qs("#group-assign-visible").checked;
      if (!assigneeUsername || !title) {
        showToast("Assignee and title are required.", "error");
        return;
      }
      await apiFetch(`/api/groups/${currentGroupId}/tasks/assign`, {
        method: "POST",
        body: {
          assigneeUsername,
          title,
          description,
          priority,
          dueAt,
          visibleInGroup,
        },
      });
      assignForm.reset();
      showToast("Task assigned.", "success");
      loadGroupTasks(0);
    });
  }

  const forAllForm = qs("#group-forall-form");
  if (forAllForm) {
    forAllForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const title = qs("#group-forall-title").value.trim();
      const descriptionValue = qs("#group-forall-description").value.trim();
      const description = descriptionValue ? descriptionValue : null;
      const priority = qs("#group-forall-priority").value || null;
      const dueAtValue = qs("#group-forall-dueAt").value;
      const dueAt = dueAtValue || null;
      if (!title) {
        showToast("Title is required.", "error");
        return;
      }
      await apiFetch(`/api/groups/${currentGroupId}/tasks/for-all`, {
        method: "POST",
        body: { title, description, priority, dueAt },
      });
      forAllForm.reset();
      showToast("Group task created.", "success");
      loadGroupTasks(0);
    });
  }

  const list = qs("#group-tasks-list");
  if (list) {
    list.addEventListener("click", async (event) => {
      const editButton = event.target.closest("[data-action='edit']");
      const deleteButton = event.target.closest("[data-action='delete']");
      const statusButton = event.target.closest("[data-action='status']");

      if (editButton) {
        const taskId = editButton.dataset.id;
        await openGroupTaskDialog(taskId);
      }

      if (deleteButton) {
        const taskId = deleteButton.dataset.id;
        const confirmed = await confirmDialog({
          title: "Delete task",
          message: "This group task will be removed permanently.",
          confirmText: "Delete",
        });
        if (confirmed) {
          await apiFetch(
            `/api/groups/${currentGroupId}/tasks/${taskId}`,
            { method: "DELETE" }
          );
          showToast("Task deleted.", "success");
          loadGroupTasks(lastPage);
        }
      }

      if (statusButton) {
        const taskId = statusButton.dataset.id;
        const status = statusButton.dataset.status;
        await apiFetch(
          `/api/groups/${currentGroupId}/tasks/${taskId}/status`,
          { method: "POST", body: { status } }
        );
        showToast("Status updated.", "success");
        loadGroupTasks(lastPage);
      }
    });
  }

  const form = qs("#group-task-form");
  if (form) {
    form.addEventListener("submit", handleGroupTaskSubmit);
  }
}

export async function loadGroupDetail(groupId) {
  currentGroupId = groupId;
  state.selectedGroupId = groupId;
  currentTab = "mine";

  state.groups = await apiFetch("/api/groups");
  const group = state.groups.find((g) => `${g.id}` === `${groupId}`);
  qs("#group-title").textContent = group ? group.name : "Group";
  qs("#group-owner").textContent = group
    ? `Owner: ${group.ownerUsername}`
    : "Owner: -";

  qs("#group-rename").value = group?.name || "";

  const members = await apiFetch(`/api/groups/${groupId}/members`);
  renderMembers(members, group);

  setActiveTab();
  updateGroupPanels();
  loadGroupTasks(0);
}

function renderMembers(members, group) {
  const list = qs("#group-members-list");
  list.innerHTML = "";
  state.groupRole = null;

  members.forEach((member) => {
    if (state.username && member.username === state.username) {
      state.groupRole = member.role;
    }

    const item = document.createElement("div");
    item.className = "list-item";
    const info = document.createElement("div");
    const name = document.createElement("strong");
    name.textContent = member.username;
    const meta = document.createElement("div");
    meta.className = "meta";
    meta.textContent = member.email;
    info.append(name, meta);
    const roleChip = document.createElement("span");
    roleChip.className = "chip";
    roleChip.textContent = member.role;
    info.appendChild(roleChip);

    const actions = document.createElement("div");
    if (isOwner(group)) {
      if (member.role !== "OWNER") {
        const promote = document.createElement("button");
        promote.className = "button small";
        promote.textContent = member.role === "ADMIN" ? "Set Member" : "Set Admin";
        promote.addEventListener("click", async () => {
          const role = member.role === "ADMIN" ? "MEMBER" : "ADMIN";
          await apiFetch(`/api/groups/${currentGroupId}/role`, {
            method: "PUT",
            body: { username: member.username, role },
          });
          showToast("Role updated.", "success");
          loadGroupDetail(currentGroupId);
        });
        actions.appendChild(promote);
      }

      if (member.role !== "OWNER") {
        const remove = document.createElement("button");
        remove.className = "button small ghost";
        remove.textContent = "Remove";
        remove.addEventListener("click", async () => {
          const confirmed = await confirmDialog({
            title: "Remove member",
            message: `Remove ${member.username} from the group?`,
            confirmText: "Remove",
          });
          if (confirmed) {
            await apiFetch(
              `/api/groups/${currentGroupId}/members/${member.userId}`,
              { method: "DELETE" }
            );
            showToast("Member removed.", "success");
            loadGroupDetail(currentGroupId);
          }
        });
        actions.appendChild(remove);
      }
    }

    item.append(info, actions);
    list.appendChild(item);
  });

  const adminActions = qs("#group-admin-actions");
  if (adminActions) {
    adminActions.classList.add("hidden");
  }

  const ownerActions = qs("#group-owner-actions");
  if (ownerActions) {
    ownerActions.classList.toggle("hidden", !isOwner(group));
  }

  const renameInput = qs("#group-rename");
  const renameButton = qs("#group-rename-form button[type='submit']");
  if (renameInput && renameButton) {
    const allowRename = isOwner(group);
    renameInput.disabled = !allowRename;
    renameButton.disabled = !allowRename;
  }

  const leaveButton = qs("#group-leave-button");
  if (leaveButton && isOwner(group)) {
    leaveButton.disabled = true;
    leaveButton.title = "Owners cannot leave their own group.";
  } else if (leaveButton) {
    leaveButton.disabled = false;
    leaveButton.title = "";
  }

  updateGroupTabs(group);
}

function isOwner(group) {
  if (!group) return false;
  if (state.username && group.ownerUsername === state.username) return true;
  return state.groupRole === "OWNER";
}

function canManage(group) {
  if (isOwner(group)) return true;
  return state.groupRole === "ADMIN";
}

function getCurrentGroup() {
  if (!state.groups || !currentGroupId) return null;
  return state.groups.find((g) => `${g.id}` === `${currentGroupId}`) || null;
}

function setActiveTab() {
  qsa("[data-group-tab]").forEach((button) => {
    button.classList.toggle("active", button.dataset.groupTab === currentTab);
  });
}

function updateGroupTabs(group) {
  const createTab = qs("[data-group-tab='create']");
  const canCreate = canManage(group);
  if (createTab) {
    createTab.classList.toggle("hidden", !canCreate);
    createTab.disabled = !canCreate;
  }

  if (!canCreate && currentTab === "create") {
    currentTab = "mine";
    setActiveTab();
  }

  updateGroupPanels();
}

function updateGroupPanels() {
  const group = getCurrentGroup();
  const canCreate = canManage(group);
  const adminActions = qs("#group-admin-actions");
  const tasksSection = qs("#group-tasks-section");
  const locked = qs("#group-create-locked");
  if (!adminActions || !tasksSection) return;

  if (currentTab === "create") {
    tasksSection.classList.add("hidden");
    if (canCreate) {
      adminActions.classList.remove("hidden");
      if (locked) locked.classList.add("hidden");
    } else {
      adminActions.classList.add("hidden");
      if (locked) locked.classList.remove("hidden");
    }
  } else {
    tasksSection.classList.remove("hidden");
    adminActions.classList.add("hidden");
    if (locked) locked.classList.add("hidden");
  }
}

async function loadGroupTasks(page = 0) {
  if (!currentGroupId) return;
  const endpointMap = {
    mine: "mine",
    all: "for-all/list",
    visible: "visible",
  };
  const endpoint = endpointMap[currentTab] || "mine";
  const params = new URLSearchParams({
    page: `${page}`,
    size: `${pageSize}`,
  });
  const data = await apiFetch(
    `/api/groups/${currentGroupId}/tasks/${endpoint}?${params.toString()}`
  );
  lastPage = data.number ?? 0;
  renderGroupTasks(data.content || []);
  renderPagination(qs("#group-tasks-pagination"), data, loadGroupTasks);
}

function renderGroupTasks(tasks) {
  const list = qs("#group-tasks-list");
  if (!list) return;
  list.innerHTML = "";
  if (!tasks.length) {
    list.innerHTML =
      "<div class='empty-state'>No tasks in this feed yet.</div>";
    return;
  }

  tasks.forEach((task) => {
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

    if (task.assigneeUsername) {
      const assignee = document.createElement("span");
      assignee.textContent = `Assignee: ${task.assigneeUsername}`;
      meta.appendChild(assignee);
    }

    if (task.dueAt) {
      const due = document.createElement("span");
      due.textContent = `Due ${formatDateTime(task.dueAt)}`;
      meta.appendChild(due);
    }

    const actions = document.createElement("div");
    actions.className = "inline-actions";

    if (task.groupTask) {
      const statusGroup = document.createElement("div");
      statusGroup.className = "segmented";
      ["TODO", "IN_PROGRESS", "DONE", "CANCELLED"].forEach((status) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.textContent = status.replace("_", " ");
        btn.dataset.action = "status";
        btn.dataset.status = status;
        btn.dataset.id = task.id;
        if (task.status === status) btn.classList.add("active");
        statusGroup.appendChild(btn);
      });
      actions.appendChild(statusGroup);
    }

    const manageGroup = canManage(getCurrentGroup());
    if (manageGroup) {
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
    }

    card.append(title, desc, meta, actions);
    list.appendChild(card);
  });
}

async function openGroupTaskDialog(taskId) {
  const task = await apiFetch(`/api/tasks/${taskId}`);
  editingTaskId = taskId;
  qs("#group-task-dialog-title").textContent = "Edit group task";
  qs("#group-task-title").value = task?.title || "";
  qs("#group-task-description").value = task?.description || "";
  qs("#group-task-status").value = task?.status || "TODO";
  qs("#group-task-priority").value = task?.priority || "";
  qs("#group-task-dueAt").value = toInputDateTime(task?.dueAt);
  qs("#group-task-visible").checked = !!task?.visibleInGroup;
  qs("#group-task-clear-due").checked = false;
  openDialog("#group-task-dialog");
}

async function handleGroupTaskSubmit(event) {
  event.preventDefault();
  const title = qs("#group-task-title").value.trim();
  const descriptionValue = qs("#group-task-description").value.trim();
  const description = descriptionValue ? descriptionValue : null;
  const status = qs("#group-task-status").value || null;
  const priority = qs("#group-task-priority").value || null;
  const dueAtValue = qs("#group-task-dueAt").value;
  const dueAt = dueAtValue || null;
  const clearDueAt = qs("#group-task-clear-due").checked;
  const visibleInGroup = qs("#group-task-visible").checked;

  if (!title) {
    showToast("Title is required.", "error");
    return;
  }

  await apiFetch(`/api/groups/${currentGroupId}/tasks/${editingTaskId}`, {
    method: "PUT",
    body: {
      title,
      description,
      status,
      priority,
      dueAt,
      clearDueAt: clearDueAt || null,
      visibleInGroup,
    },
  });
  showToast("Group task updated.", "success");
  closeDialog("#group-task-dialog");
  loadGroupTasks(lastPage);
}
