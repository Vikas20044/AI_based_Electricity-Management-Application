document.addEventListener("DOMContentLoaded", () => {

  const path = window.location.pathname.toLowerCase();
  const isAuthPage = path.endsWith("login.html") || path.endsWith("register.html");

  if (!isAuthPage) {
    if (typeof ApiClient !== "undefined" && !ApiClient.isAuthenticated()) {
      window.location.replace("login.html");
      return;
    }
  } else {
    if (typeof ApiClient !== "undefined" && ApiClient.isAuthenticated()) {
      window.location.replace("dashboard.html");
      return;
    }
  }

  const navLinks = document.querySelectorAll(".nav-link-custom");
  navLinks.forEach(link => {
    const href = link.getAttribute("href");
    if (href && path.endsWith(href.toLowerCase())) {
      link.classList.add("active");
    } else if (!href || href === "#") {
      link.classList.remove("active");
    }
  });

  const toggleBtn = document.getElementById("sidebarToggleBtn");
  const sidebar = document.querySelector(".app-sidebar");

  if (toggleBtn && sidebar) {
    toggleBtn.addEventListener("click", () => {
      sidebar.classList.toggle("show");
    });

    document.addEventListener("click", (e) => {
      if (window.innerWidth < 992) {
        if (!sidebar.contains(e.target) && !toggleBtn.contains(e.target)) {
          sidebar.classList.remove("show");
        }
      }
    });
  }

  if (typeof ApiClient !== "undefined") {
    const user = ApiClient.getUser();
    const userNameEls = document.querySelectorAll(".current-user-name");
    const userEmailEls = document.querySelectorAll(".current-user-email");

    if (user) {
      const displayName = user.fullName || (user.email ? user.email.split('@')[0] : "Consumer");
      userNameEls.forEach(el => el.textContent = displayName);
      userEmailEls.forEach(el => el.textContent = user.email || "");
    }
  }

  const logoutBtns = document.querySelectorAll(".action-logout");
  logoutBtns.forEach(btn => {
    btn.addEventListener("click", (e) => {
      e.preventDefault();
      if (confirm("Are you sure you want to sign out?")) {
        if (typeof ApiClient !== "undefined") {
          ApiClient.logout();
        } else {
          localStorage.removeItem("ai_electricity_token");
          localStorage.removeItem("ai_electricity_user");
          localStorage.removeItem("user_authenticated");
          window.location.replace("login.html");
        }
      }
    });
  });
});
