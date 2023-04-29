const form = document.getElementById("register-form");
const username = document.getElementById("username");
const email = document.getElementById("email");
const password = document.getElementById("password");
const confirmPassword = document.getElementById("confirm-password");
const errorMessage = document.querySelector('.error-message');
const errorMessageContainer = document.querySelector('.error-message-container');

function showError(message) {
    errorMessageContainer.style.display = 'inline';
    errorMessage.textContent = message;
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!username.value || !email.value || !password.value || !confirmPassword.value) {
        showError("Please fill in all fields.");
        return;
    }

    if (password.value !== confirmPassword.value) {
        showError("Passwords do not match.");
        return;
    }

    const response = await fetch("/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `username=${encodeURIComponent(username.value)}&email=${encodeURIComponent(email.value)}&password=${encodeURIComponent(password.value)}`
    });

    if (response.ok) {
        window.location.href = "/login";
    } else {
        const errorText = await response.text();
        showError(errorText);
    }
});