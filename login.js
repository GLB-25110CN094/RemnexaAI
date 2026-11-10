
document.getElementById("loginForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;
    const message = document.getElementById("message");

    try {
        const response = await fetch("http://localhost:8080/api/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        const token = await response.text();

        if (!response.ok) {
            throw new Error("Login failed");
        }

        localStorage.setItem("jwtToken", token);

        message.textContent = "Login successful! Opening chatbot...";

        window.location.href = "index.html";

    } catch (error) {
        message.textContent = "Login failed. Check your email, password, and backend.";
        console.error(error);
    }
});
