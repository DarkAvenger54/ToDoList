import { apiFetch } from "../api.js";
import { qs, showToast } from "../ui.js";

export function initSettings() {
  const form = qs("#change-password-form");
  if (form) {
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const oldPassword = qs("#old-password").value;
      const newPassword = qs("#new-password").value;
      if (!oldPassword || !newPassword) {
        showToast("Fill out both password fields.", "error");
        return;
      }
      await apiFetch("/api/users/me/password", {
        method: "PUT",
        body: { oldPassword, newPassword },
      });
      form.reset();
      showToast("Password updated.", "success");
    });
  }
}
