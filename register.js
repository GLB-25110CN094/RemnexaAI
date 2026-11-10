
document.getElementById("registerForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const message = document.getElementById("message");

    message.textContent = "Creating account...";

    try {
        const response = await fetch(
            "http://localhost:8080/api/auth/register",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            }
        );

        const result = await response.text();

        if (!response.ok ||
            result !== "User registered successfully") {
            throw new Error(result || "Registration failed");
        }

        message.textContent =
            "Account created successfully! Please login.";

        setTimeout(() => {
            window.location.href = "login.html";
        }, 1200);

    } catch (error) {
        message.textContent =
            "Registration failed: " + error.message;
        console.error(error);
    }
});
