const form = document.getElementById("login-form");
const username = document.getElementById("username");
const password = document.getElementById("password");
const errorMessage = document.querySelector('.error-message');
const errorMessageContainer = document.querySelector('.error-message-container');

function showError(message) {
    errorMessageContainer.style.display = 'inline';
    errorMessage.textContent = message;
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (!username.value || !password.value) {
        showError("Please fill in all fields.");
        return;
    }

    const response = await fetch("/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `username=${encodeURIComponent(username.value)}&password=${encodeURIComponent(password.value)}`
    });

    if (response.ok) {
        window.location.href = "/home";
    } else {
        const errorText = await response.text();
        showError(errorText);
    }
});