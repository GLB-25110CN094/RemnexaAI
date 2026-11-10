
document.getElementById("sendBtn")
    .addEventListener("click", sendMessage);

let conversationId = null;

// CREATE OR LOAD CONVERSATION
async function initializeConversation() {
    const token = localStorage.getItem("jwtToken");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    try {
        const response = await fetch(
            "http://localhost:8080/api/chat/conversation",
            {
                method: "POST",
                headers: {
                    "Authorization": "Bearer " + token
                }
            }
        );

        if (!response.ok) {
            throw new Error("Could not create conversation");
        }

        conversationId = await response.json();

        console.log("Conversation ID:", conversationId);

    } catch (error) {
        console.error("Conversation initialization error:", error);
        alert("Unable to start chat. Please refresh the page.");
    }
}

// Initialize when chat page loads
initializeConversation().then(() => {
    loadRecentChats();
});
// LOAD RECENT CHATS
async function loadRecentChats() {
    const token = localStorage.getItem("jwtToken");
    const conversationList = document.getElementById("conversationList");

    if (!token || !conversationList) return;

    try {
        const response = await fetch(
            "http://localhost:8080/api/chat/conversations",
            {
                method: "GET",
                headers: {
                    "Authorization": "Bearer " + token
                }
            }
        );

        if (!response.ok) {
            throw new Error("Could not load recent chats");
        }

        const conversations = await response.json();

        conversationList.innerHTML = "";

        conversations.forEach((conversation) => {
            const item = document.createElement("div");
            item.className = "conversation-item";

            // API response ke hisaab se title ya ID dikhayein
            item.textContent =
                conversation.title ||
                conversation.name ||
                "Chat " + conversation.id;

            item.addEventListener("click", () => {
                conversationId = conversation.id;

                document.querySelectorAll(".conversation-item")
                    .forEach(chat => chat.classList.remove("active"));

                item.classList.add("active");

                // Note: purane messages load karne ke liye
                // backend ki message-history API bhi chahiye.
            });

            conversationList.appendChild(item);
        });

    } catch (error) {
        console.error("Recent chats error:", error);
    }
}



// VOICE INPUT
function startVoiceInput() {
    const SpeechRecognition =
        window.SpeechRecognition ||
        window.webkitSpeechRecognition;

    if (!SpeechRecognition) {
        alert("Voice input is not supported in this browser. Try Chrome.");
        return;
    }

    const recognition = new SpeechRecognition();

    recognition.lang = "en-IN";
    recognition.continuous = false;
    recognition.interimResults = false;

    recognition.onstart = () => {
        console.log("Listening...");
    };

    recognition.onresult = (event) => {
        const spokenText = event.results[0][0].transcript;

        document.getElementById("userInput").value = spokenText;
    };

    recognition.onerror = (event) => {
        console.error("Voice error:", event.error);
        alert("Voice input error: " + event.error);
    };

    recognition.start();
}


// VOICE OUTPUT
function speakReply(text) {
    if (!("speechSynthesis" in window)) {
        return;
    }

    window.speechSynthesis.cancel();

    const speech = new SpeechSynthesisUtterance(text);
    speech.lang = "en-IN";
    speech.rate = 1;

    window.speechSynthesis.speak(speech);
}


// SEND MESSAGE
async function sendMessage() {
    const inputBox = document.getElementById("userInput");
    const chatbox = document.getElementById("chatbox");

    const input = inputBox.value.trim();

    if (input === "") return;

    const token = localStorage.getItem("jwtToken");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    // Wait until conversation ID is available
    if (conversationId === null) {
        alert("Chat is initializing. Please try again in a moment.");
        return;
    }

    const userMessage = document.createElement("div");
    userMessage.className = "user";
    userMessage.textContent = input;
    chatbox.appendChild(userMessage);

    inputBox.value = "";

    try {
        const response = await fetch(
            "http://localhost:8080/api/chat",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + token
                },
                body: JSON.stringify({
                    conversationId: conversationId,
                    question: input
                })
            }
        );

        const data = await response.text();

        if (!response.ok) {
            throw new Error(data || "Request failed: " + response.status);
        }

        const botMessage = document.createElement("div");
        botMessage.className = "bot";
        botMessage.textContent = data;
        chatbox.appendChild(botMessage);

        chatbox.scrollTop = chatbox.scrollHeight;

        // Uncomment to enable voice replies:
        // speakReply(data);

    } catch (error) {
        const errorMessage = document.createElement("div");
        errorMessage.className = "bot";
        errorMessage.textContent = "Error: " + error.message;
        chatbox.appendChild(errorMessage);

        console.error(error);
    }
}
