import { apiFetch } from "../api.js";
import { setToken, state } from "../state.js";
import { navigate } from "../router.js";
import { qs, showToast } from "../ui.js";

export function initAuth() {
  const loginForm = qs("#login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const login = qs("#login-login").value.trim();
      const password = qs("#login-password").value;
      state.lastLoginValue = login;
      try {
        const data = await apiFetch("/api/auth/login", {
          method: "POST",
          auth: false,
          body: { login, password },
        });
        setToken(data.jwtToken);
        showToast("Signed in successfully.", "success");
        window.dispatchEvent(new CustomEvent("auth:login"));
        navigate("/tasks");
      } catch {
        // Errors handled in apiFetch
      }
    });
  }

  const registerForm = qs("#register-form");
  if (registerForm) {
    registerForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const username = qs("#register-username").value.trim();
      const email = qs("#register-email").value.trim();
      const password = qs("#register-password").value;
      try {
        await apiFetch("/api/auth/register", {
          method: "POST",
          auth: false,
          body: { username, email, password },
        });
        showToast("Registration successful. Check your email.", "success");
        navigate("/login");
      } catch {
        // handled
      }
    });
  }

  const forgotForm = qs("#forgot-form");
  if (forgotForm) {
    forgotForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const email = qs("#forgot-email").value.trim();
      try {
        await apiFetch("/api/auth/forgot-password", {
          method: "POST",
          auth: false,
          body: { email },
        });
        showToast(
          "If the email exists, a reset link has been sent.",
          "success"
        );
        navigate("/login");
      } catch {
        // handled
      }
    });
  }

  const resetForm = qs("#reset-form");
  if (resetForm) {
    resetForm.addEventListener("submit", async (event) => {
      event.preventDefault();
      const token = qs("#reset-token").value.trim();
      const newPassword = qs("#reset-password").value;
      if (!token) {
        showToast("Reset token is missing.", "error");
        return;
      }
      try {
        await apiFetch("/api/auth/reset-password", {
          method: "POST",
          auth: false,
          body: { token, newPassword },
        });
        showToast("Password reset successful. Sign in again.", "success");
        navigate("/login");
      } catch {
        // handled
      }
    });
  }

  const resendButton = qs("#resend-confirmation");
  if (resendButton) {
    resendButton.addEventListener("click", async () => {
      const emailInput = qs("#verify-email");
      const email = emailInput.value.trim();
      if (!email) {
        showToast("Enter your email to resend confirmation.", "error");
        return;
      }
      try {
        await apiFetch("/api/auth/resend-confirmation", {
          method: "POST",
          auth: false,
          body: { email },
        });
        showToast("Confirmation email sent.", "success");
      } catch {
        // handled
      }
    });
  }

  window.addEventListener("auth:verify-required", (event) => {
    showVerifyPanel(event.detail);
  });
}

export function showVerifyPanel(message) {
  const panel = qs("#verify-panel");
  if (!panel) return;
  panel.classList.remove("hidden");
  const helper = qs("#verify-message");
  helper.textContent =
    message ||
    "Email verification is required. Please check your inbox for the confirmation link.";
  const emailInput = qs("#verify-email");
  if (emailInput && state.lastLoginValue) {
    emailInput.value = state.lastLoginValue;
  }
}

export async function handleConfirmEmail(token) {
  const status = qs("#confirm-status");
  if (!status) return;
  status.textContent = "Confirming your email...";
  try {
    const result = await apiFetch(`/api/auth/confirm-email?token=${token}`, {
      method: "GET",
      auth: false,
    });
    status.textContent =
      typeof result === "string" ? result : "Email confirmed.";
  } catch (error) {
    status.textContent =
      error?.message || "Email confirmation failed. Try again.";
  }
}

export function setResetToken(token) {
  const input = qs("#reset-token");
  if (input) input.value = token;
}
