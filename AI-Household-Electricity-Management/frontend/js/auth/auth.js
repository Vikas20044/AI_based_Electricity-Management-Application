document.addEventListener("DOMContentLoaded", () => {

  if (typeof ApiClient !== "undefined" && ApiClient.isAuthenticated()) {
    window.location.replace("dashboard.html");
    return;
  }

  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const emailInput = document.getElementById("loginEmail");
      const passwordInput = document.getElementById("loginPassword");
      const errorMsg = document.getElementById("authErrorMsg");
      const submitBtn = loginForm.querySelector("button[type='submit']");

      const email = emailInput ? emailInput.value.trim() : "";
      const password = passwordInput ? passwordInput.value : "";

      if (errorMsg) {
        errorMsg.classList.add("d-none");
        errorMsg.textContent = "";
      }

      if (!email || !password) {
        if (errorMsg) {
          errorMsg.textContent = "Please provide both email address and password.";
          errorMsg.classList.remove("d-none");
        }
        return;
      }

      const originalBtnText = submitBtn ? submitBtn.innerHTML : "";
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span>Signing In...`;
      }

      try {
        await ApiClient.login(email, password);

        window.location.replace("dashboard.html");
      } catch (err) {
        if (errorMsg) {
          errorMsg.textContent = err.message || "Failed to sign in. Please verify your credentials.";
          errorMsg.classList.remove("d-none");
        }
      } finally {
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.innerHTML = originalBtnText;
        }
      }
    });
  }

  const registerForm = document.getElementById("registerForm");
  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const fullName = document.getElementById("regFullName").value.trim();
      const email = document.getElementById("regEmail").value.trim();
      const phone = document.getElementById("regPhone").value.trim();
      const password = document.getElementById("regPassword").value;
      const confirmPassword = document.getElementById("regConfirmPassword").value;
      const errorMsg = document.getElementById("authErrorMsg");
      const submitBtn = registerForm.querySelector("button[type='submit']");

      if (errorMsg) {
        errorMsg.classList.add("d-none");
        errorMsg.textContent = "";
      }

      if (!fullName || !email || !password || !confirmPassword) {
        if (errorMsg) {
          errorMsg.textContent = "Please fill in all required fields.";
          errorMsg.classList.remove("d-none");
        }
        return;
      }

      if (password !== confirmPassword) {
        if (errorMsg) {
          errorMsg.textContent = "Passwords do not match. Please re-enter.";
          errorMsg.classList.remove("d-none");
        }
        return;
      }

      if (password.length < 6) {
        if (errorMsg) {
          errorMsg.textContent = "Password must be at least 6 characters long.";
          errorMsg.classList.remove("d-none");
        }
        return;
      }

      const originalBtnText = submitBtn ? submitBtn.innerHTML : "";
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-2" role="status"></span>Creating Account...`;
      }

      try {
        await ApiClient.register(fullName, email, password, phone);
        window.location.replace("household-profile.html");
      } catch (err) {
        if (errorMsg) {
          errorMsg.textContent = err.message || "Registration failed. Please try again.";
          errorMsg.classList.remove("d-none");
        }
      } finally {
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.innerHTML = originalBtnText;
        }
      }
    });
  }
});
