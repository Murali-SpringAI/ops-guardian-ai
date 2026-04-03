const API_BASE = "/api/agent";
const TOKEN = "TOKEN";

let pendingAction = null;
let pendingTarget = null;
let sessionMemory = [];

// 🔹 Clear UI
function clearUI() {
    document.getElementById("chat").innerHTML = "";
    document.getElementById("flow").innerHTML = "";
    document.getElementById("confirmSection").style.display = "none";
}

// 🔹 Typing animation
function typeText(element, text, speed = 20) {
    element.innerHTML = "";
    let i = 0;
    function typing() {
        if (i < text.length) {
            element.innerHTML += text.charAt(i);
            i++;
            setTimeout(typing, speed);
        }
    }
    typing();
}

// 🔹 Add message
async function addMessage(sender, text, typing = false) {
    const chat = document.getElementById("chat");
    const msg = document.createElement("div");
    msg.style.margin = "8px 0";

    const timestamp = new Date().toLocaleTimeString();

    if (sender === "user") {
        msg.innerHTML = `<b>🧑 You:</b> ${text}
        <span style="font-size:10px;color:#888;">${timestamp}</span>`;
    } else {
        msg.innerHTML = `<b>🤖 OpsGuardian:</b> `;
        chat.appendChild(msg);

        // 🔴 Block / 🟠 Warning
        if (text.startsWith("🛑") || text.startsWith("🔴")) msg.style.color = "red";
        if (text.startsWith("⚠️") || text.startsWith("🟠")) msg.style.color = "orange";

        // 🌐 System-based coloring
        if (text.includes("AWS")) msg.style.color = "#ff9900";
        if (text.includes("Kubernetes")) msg.style.color = "#326ce5";
        if (text.includes("Datadog")) msg.style.color = "#632ca6";
        if (text.includes("Splunk")) msg.style.color = "#65a637";
        if (text.includes("GitHub")) msg.style.color = "#24292e";

        if (typing) {
            await typeText(msg, text);
            msg.innerHTML += ` <span style="font-size:10px;color:#888;">${timestamp}</span>`;
            chat.scrollTop = chat.scrollHeight;
            return;
        }

        msg.innerHTML += text;
    }

    chat.appendChild(msg);
    chat.scrollTop = chat.scrollHeight;
}

// ⚡ Simulate AI thinking
async function simulateSteps(action) {
    const steps = [
        "🧠 Understanding request...",
        "📊 Analyzing intent...",
        "🔒 AI running in restricted mode...",
        "🛡️ Routing via OpsGuardian Agent...",
        "🔐 Validating JWT with Auth0...",
        "🛡️ Checking RBAC permissions..."
    ];

    for (let step of steps) {
        await addMessage("bot", step, true);
        await new Promise(r => setTimeout(r, 120));
    }

    if (action === "RESTART_SERVICE" || action === "SCALE_SERVICE") {
        await addMessage("bot", "⚠️ This action may require confirmation", true);
    } else {
        await addMessage("bot", "🚀 Executing action...", true);
    }
}

// 🚀 Send Command
async function sendCommand() {
    let command = document.getElementById("command").value;
    if (!command) return;

    // 🧠 Session memory ("it")
    if (command.toLowerCase().includes("it") && sessionMemory.length > 0) {
        command = sessionMemory[sessionMemory.length - 1];
    }

    sessionMemory.push(command);

    clearUI();

    await addMessage("user", command);
    document.getElementById("command").value = "";

    await addMessage("bot", "🧠 Thinking...", true);

    const res = await fetch(`${API_BASE}/execute`, {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + TOKEN,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ command })
    });

    const data = await res.json();
    console.log("API RESPONSE:", data);

    // 🔴 BLOCKED
    if (data.status === "BLOCKED") {
        await addMessage("bot", `🛑 ${data.message}`, true);
        renderFlow(data, { action: "BLOCKED", requiresConfirmation: false });
        return;
    }

    // 🟠 WARNING
    if (data.status === "WARNING") {
        await addMessage("bot", `⚠️ ${data.message}`, true);
    }

    const action = data.data?.action || "UNKNOWN";

    await simulateSteps(action);

    await addMessage("bot", `⚙️ Action: ${action}`, true);
    await addMessage("bot", `💡 Reason: ${data.message || "N/A"}`, true);
    await addMessage("bot", "🛡️ OpsGuardian enforcing security...", true);
    await addMessage("bot", "🔐 Acting via Auth0 identity", true);

    if (data.status === "CONFIRMATION_REQUIRED") {
        pendingAction = data.data.action;
        pendingTarget = data.data.target;
        document.getElementById("confirmSection").style.display = "block";
    } else {
        document.getElementById("confirmSection").style.display = "none";

        await addMessage(
            "bot",
            `✅ Result: ${JSON.stringify(data.data?.result || data.data)}`,
            true
        );
    }

    renderFlow(data, {
        action: action,
        requiresConfirmation: data.status === "CONFIRMATION_REQUIRED"
    });
}

// ✅ Confirm Action
async function confirmAction() {
    if (!pendingAction || !pendingTarget) return;

    await addMessage("bot", "🚀 Executing confirmed action...", true);

    const res = await fetch(
        `${API_BASE}/confirm?action=${pendingAction}&target=${pendingTarget}`,
        {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + TOKEN,
                "Content-Type": "application/json"
            }
        }
    );

    const data = await res.json();

    await addMessage(
        "bot",
        `✅ Result: ${JSON.stringify(data.data?.result || data.data)}`,
        true
    );

    document.getElementById("confirmSection").style.display = "none";

    renderFlow(data, {
        action: pendingAction,
        requiresConfirmation: false
    });

    pendingAction = null;
    pendingTarget = null;
}

// 🎨 Visual Flow Renderer
function renderFlow(response, decision) {
    const flowEl = document.getElementById("flow");

    const steps = [
        { id: "ai", label: "🤖 AI" },
        { id: "llm", label: "🧠 LLM" },
        { id: "agent", label: "🛡️ Agent" },
        { id: "auth", label: "🔐 Auth0" },
        { id: "rbac", label: "🛡️ RBAC" },
        { id: "confirm", label: "⚠️ Confirm", optional: true },
        { id: "exec", label: "⚙️ Execute" },
        { id: "system", label: "🌐 System" },
        { id: "response", label: "📦 Response" }
    ];

    flowEl.innerHTML = "";

    steps.forEach((step, index) => {
        if (step.optional && !decision.requiresConfirmation) return;

        const div = document.createElement("div");
        div.className = "flow-step";
        div.id = "step-" + step.id;
        div.innerText = step.label;

        flowEl.appendChild(div);

        if (index < steps.length - 1) {
            const arrow = document.createElement("div");
            arrow.className = "flow-arrow";
            arrow.innerText = "➡️";
            flowEl.appendChild(arrow);
        }
    });

    animateFlow(response);
}

// 🎬 Animate Flow
async function animateFlow(response) {
    const order = ["ai", "llm", "agent", "auth", "rbac", "confirm", "exec", "system", "response"];

    for (let step of order) {
        const el = document.getElementById("step-" + step);
        if (!el) continue;

        el.classList.add("active");
        await new Promise(r => setTimeout(r, 250));

        // 🔴 Block
        if (response.status === "BLOCKED" && step === "agent") {
            el.classList.remove("active");
            el.classList.add("blocked");
            break;
        }

        // 🟠 Warning
        if (response.status === "WARNING" && step === "agent") {
            el.classList.add("warning");
        }
    }
}