const API_BASE = window.location.origin;
const SESSION_KEYS = {
  token: "quizapp.token",
  userId: "quizapp.userId",
  email: "quizapp.email",
  firstName: "quizapp.firstName"
};

const elements = {
  authPanel: document.querySelector("#auth-panel"),
  appPanel: document.querySelector("#app-panel"),
  loginForm: document.querySelector("#login-form"),
  registerForm: document.querySelector("#register-form"),
  authTitle: document.querySelector("#auth-title"),
  notice: document.querySelector("#notice"),
  logout: document.querySelector("#logout-button"),
  quizForm: document.querySelector("#quiz-form"),
  createQuiz: document.querySelector("#create-quiz-button"),
  questionsPanel: document.querySelector("#questions-panel"),
  answersForm: document.querySelector("#answers-form"),
  resultPanel: document.querySelector("#result-panel"),
  leaderboard: document.querySelector("#leaderboard")
};

let questionsActives = [];
let quizActifId = null;
let requeteEnCours = false;

function session() {
  return {
    token: sessionStorage.getItem(SESSION_KEYS.token),
    userId: sessionStorage.getItem(SESSION_KEYS.userId),
    email: sessionStorage.getItem(SESSION_KEYS.email),
    firstName: sessionStorage.getItem(SESSION_KEYS.firstName)
  };
}

function notify(message, isError = false) {
  elements.notice.textContent = message;
  elements.notice.classList.toggle("is-error", isError);
  elements.notice.classList.remove("hidden");
}

function clearNotice() {
  elements.notice.textContent = "";
  elements.notice.classList.add("hidden");
  elements.notice.classList.remove("is-error");
}

function setBusy(button, busy, label) {
  button.disabled = busy;
  button.setAttribute("aria-busy", String(busy));
  if (busy) {
    button.dataset.originalLabel = button.textContent;
    button.textContent = label;
  } else if (button.dataset.originalLabel) {
    button.textContent = button.dataset.originalLabel;
    delete button.dataset.originalLabel;
  }
}

async function apiRequest(path, options = {}, authenticated = true) {
  const headers = new Headers(options.headers || {});
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  const currentSession = session();
  if (authenticated && currentSession.token) {
    headers.set("Authorization", `Bearer ${currentSession.token}`);
  }

  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const content = await response.text();
  let data = content;
  if (content) {
    try {
      data = JSON.parse(content);
    } catch {
      data = content;
    }
  }

  if (!response.ok) {
    const detail = typeof data === "string" ? data : JSON.stringify(data);
    if (response.status === 401 && authenticated) {
      clearSession();
      showAuthentication();
    }
    throw new Error(detail || `La requête a échoué (HTTP ${response.status}).`);
  }
  return data;
}

function showAuthentication() {
  elements.authPanel.classList.remove("hidden");
  elements.appPanel.classList.add("hidden");
  elements.logout.classList.add("hidden");
}

function showApplication() {
  const currentSession = session();
  if (!currentSession.token || !currentSession.userId) {
    showAuthentication();
    return;
  }
  const firstName = currentSession.firstName || currentSession.email || "étudiant";
  document.querySelector("#student-name").textContent = firstName;
  document.querySelector("#result-name").textContent = firstName;
  elements.authPanel.classList.add("hidden");
  elements.appPanel.classList.remove("hidden");
  elements.logout.classList.remove("hidden");
  loadLeaderboard();
}

function clearSession() {
  Object.values(SESSION_KEYS).forEach((key) => sessionStorage.removeItem(key));
}

function saveSession(data, firstName = "") {
  if (!data || !data.token || !Number.isInteger(Number(data.utilisateurId))) {
    throw new Error("La réponse de connexion ne contient pas l’identifiant ou le jeton attendu.");
  }
  sessionStorage.setItem(SESSION_KEYS.token, data.token);
  sessionStorage.setItem(SESSION_KEYS.userId, String(data.utilisateurId));
  sessionStorage.setItem(SESSION_KEYS.email, data.email || "");
  sessionStorage.setItem(SESSION_KEYS.firstName, firstName);
}

function showLoginForm() {
  elements.authTitle.textContent = "Content de te revoir";
  elements.loginForm.classList.remove("hidden");
  elements.registerForm.classList.add("hidden");
  document.querySelector("#show-login").classList.add("is-active");
  document.querySelector("#show-register").classList.remove("is-active");
  clearNotice();
}

function showRegisterForm() {
  elements.authTitle.textContent = "Crée ton compte";
  elements.registerForm.classList.remove("hidden");
  elements.loginForm.classList.add("hidden");
  document.querySelector("#show-register").classList.add("is-active");
  document.querySelector("#show-login").classList.remove("is-active");
  clearNotice();
}

function renderQuestions(questions, category, title) {
  elements.answersForm.replaceChildren();
  questionsActives = questions;

  questions.forEach((question, index) => {
    const fieldset = document.createElement("fieldset");
    fieldset.className = "question-card";
    fieldset.dataset.questionId = String(question.id);

    const legend = document.createElement("legend");
    const number = document.createElement("span");
    number.className = "question-number";
    number.textContent = `QUESTION ${String(index + 1).padStart(2, "0")}`;
    legend.append(number, document.createTextNode(question.questionTitle || "Question sans titre"));
    fieldset.append(legend);

    ["option1", "option2", "option3", "option4"].forEach((optionKey) => {
      const optionText = question[optionKey];
      if (typeof optionText !== "string" || !optionText.trim()) return;

      const label = document.createElement("label");
      label.className = "answer-option";
      const input = document.createElement("input");
      input.type = "radio";
      input.name = `question-${question.id}`;
      input.value = optionText;
      input.required = true;
      const text = document.createElement("span");
      text.textContent = optionText;
      label.append(input, text);
      fieldset.append(label);
    });

    elements.answersForm.append(fieldset);
  });

  const submitButton = document.createElement("button");
  submitButton.className = "button button-primary";
  submitButton.type = "submit";
  submitButton.textContent = "Valider mes réponses →";
  elements.answersForm.append(submitButton);

  document.querySelector("#question-category").textContent = category.toUpperCase();
  document.querySelector("#question-title").textContent = title;
  document.querySelector("#question-progress").textContent = `${questions.length} questions`;
  elements.resultPanel.classList.add("hidden");
  elements.questionsPanel.classList.remove("hidden");
  elements.questionsPanel.scrollIntoView({ behavior: "smooth", block: "start" });
}

async function loadLeaderboard() {
  elements.leaderboard.replaceChildren();
  try {
    const entries = await apiRequest("/leaderboard-service/leaderboard/top/10");
    if (!Array.isArray(entries) || entries.length === 0) {
      const empty = document.createElement("p");
      empty.className = "leaderboard-empty";
      empty.textContent = "Le classement apparaîtra après les premiers quiz.";
      elements.leaderboard.append(empty);
      return;
    }

    entries.forEach((entry) => {
      const row = document.createElement("div");
      row.className = "leaderboard-entry";
      if (String(entry.utilisateurId) === session().userId) row.classList.add("is-current");

      const rank = document.createElement("span");
      rank.className = "leaderboard-rank";
      rank.textContent = `#${entry.rang}`;
      const name = document.createElement("span");
      name.className = "leaderboard-name";
      name.textContent = [entry.prenom, entry.nom].filter(Boolean).join(" ") || entry.email || "Étudiant";
      const points = document.createElement("span");
      points.className = "leaderboard-points";
      points.textContent = `${entry.totalPoints ?? 0} pts`;
      row.append(rank, name, points);
      elements.leaderboard.append(row);
    });
  } catch (error) {
    const message = document.createElement("p");
    message.className = "leaderboard-empty";
    message.textContent = `Classement indisponible : ${error.message}`;
    elements.leaderboard.append(message);
  }
}

async function showResults(score) {
  const maximum = questionsActives.length;
  document.querySelector("#score-value").textContent = String(score);
  document.querySelector("#score-total").textContent = `/ ${maximum}`;
  document.querySelector("#result-message").textContent =
    score === maximum ? "Score parfait ! Continue comme ça." : "Chaque tentative est une occasion de progresser.";

  const pointsElement = document.querySelector("#points-value");
  const rankElement = document.querySelector("#rank-value");
  pointsElement.textContent = "—";
  rankElement.textContent = "—";
  elements.questionsPanel.classList.add("hidden");
  elements.resultPanel.classList.remove("hidden");

  const currentSession = session();
  const userId = encodeURIComponent(currentSession.userId);
  const pointsRequest = apiRequest(`/gratification-service/gratification/total/${userId}`);
  const rankRequest = apiRequest(`/leaderboard-service/leaderboard/rang/${userId}`);
  const [pointsResult, rankResult] = await Promise.allSettled([pointsRequest, rankRequest]);

  if (pointsResult.status === "fulfilled") pointsElement.textContent = String(pointsResult.value ?? 0);
  if (rankResult.status === "fulfilled" && rankResult.value) {
    rankElement.textContent = `#${rankResult.value.rang}`;
  }
  await loadLeaderboard();

  const missingStats = [pointsResult, rankResult].some((result) => result.status === "rejected");
  if (missingStats) notify("Le score est enregistré, mais les statistiques ne sont pas toutes disponibles.", true);
}

document.querySelector("#show-login").addEventListener("click", showLoginForm);
document.querySelector("#show-register").addEventListener("click", showRegisterForm);

elements.loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  clearNotice();
  const button = elements.loginForm.querySelector('button[type="submit"]');
  setBusy(button, true, "Connexion…");
  const formData = new FormData(elements.loginForm);
  try {
    const data = await apiRequest("/auth-service/auth/login", {
      method: "POST",
      body: JSON.stringify({
        email: formData.get("email"),
        motDePasse: formData.get("motDePasse")
      })
    }, false);
    saveSession(data);
    showApplication();
    notify(`Bienvenue${data.email ? `, ${data.email}` : ""} !`);
  } catch (error) {
    notify(error.message || "Connexion impossible. Vérifie tes identifiants.", true);
  } finally {
    setBusy(button, false);
  }
});

elements.registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  clearNotice();
  const button = elements.registerForm.querySelector('button[type="submit"]');
  setBusy(button, true, "Création du compte…");
  const formData = new FormData(elements.registerForm);
  const firstName = String(formData.get("prenom")).trim();
  const credentials = {
    nom: String(formData.get("nom")).trim(),
    prenom: firstName,
    email: String(formData.get("email")).trim(),
    motDePasse: String(formData.get("motDePasse"))
  };

  try {
    await apiRequest("/user-service/user/inscrire", {
      method: "POST",
      body: JSON.stringify(credentials)
    }, false);
    const data = await apiRequest("/auth-service/auth/login", {
      method: "POST",
      body: JSON.stringify({ email: credentials.email, motDePasse: credentials.motDePasse })
    }, false);
    saveSession(data, firstName);
    showApplication();
    notify("Ton compte est créé. Bienvenue sur QuizApp !");
  } catch (error) {
    notify(error.message || "Inscription impossible. Vérifie les informations saisies.", true);
  } finally {
    setBusy(button, false);
  }
});

elements.quizForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  clearNotice();
  setBusy(elements.createQuiz, true, "Préparation du quiz…");
  const formData = new FormData(elements.quizForm);
  const category = String(formData.get("categorie")).trim();
  const title = String(formData.get("titre")).trim();
  try {
    const createdId = await apiRequest("/quiz-service/quiz/creer", {
      method: "POST",
      body: JSON.stringify({
        categorie: category,
        numQuestions: Number(formData.get("numQuestions")),
        titre: title
      })
    });
    const quizId = Number(createdId);
    if (!Number.isInteger(quizId) || quizId < 1) {
      throw new Error("La Gateway n’a pas renvoyé un identifiant de quiz valide.");
    }

    const questions = await apiRequest(`/quiz-service/quiz/get/${quizId}`);
    if (!Array.isArray(questions) || questions.length === 0) {
      throw new Error("Ce quiz ne contient aucune question.");
    }
    quizActifId = quizId;
    renderQuestions(questions, category, title);
  } catch (error) {
    notify(error.message || "Impossible de préparer le quiz.", true);
  } finally {
    setBusy(elements.createQuiz, false);
  }
});

elements.answersForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (requeteEnCours || quizActifId === null) return;
  clearNotice();
  const button = elements.answersForm.querySelector('button[type="submit"]');
  requeteEnCours = true;
  setBusy(button, true, "Correction…");

  const responses = [...elements.answersForm.querySelectorAll("fieldset")].map((fieldset) => {
    const selected = fieldset.querySelector('input[type="radio"]:checked');
    return {
      id: Number(fieldset.dataset.questionId),
      response: selected ? selected.value : ""
    };
  });

  try {
    const score = Number(await apiRequest(
      `/quiz-service/quiz/submit/${quizActifId}?utilisateurId=${encodeURIComponent(session().userId)}`,
      { method: "POST", body: JSON.stringify(responses) }
    ));
    if (!Number.isFinite(score) || score < 0) throw new Error("Le service a renvoyé un score invalide.");
    await showResults(score);
    quizActifId = null;
  } catch (error) {
    notify(error.message || "La soumission du quiz a échoué.", true);
  } finally {
    requeteEnCours = false;
    if (button.isConnected) setBusy(button, false);
  }
});

document.querySelector("#new-quiz-button").addEventListener("click", () => {
  elements.resultPanel.classList.add("hidden");
  elements.questionsPanel.classList.add("hidden");
  elements.quizForm.scrollIntoView({ behavior: "smooth", block: "center" });
  clearNotice();
});

elements.logout.addEventListener("click", () => {
  clearSession();
  elements.questionsPanel.classList.add("hidden");
  elements.resultPanel.classList.add("hidden");
  showAuthentication();
  showLoginForm();
  notify("Tu es déconnecté.");
});

document.querySelector("#quiz-category").addEventListener("input", (event) => {
  const title = document.querySelector("#quiz-title");
  if (title.value === "Quiz Java" || title.value === "Quiz Python") {
    const category = event.target.value.trim();
    title.value = category ? `Quiz ${category}` : "";
  }
});

if (session().token && session().userId) {
  showApplication();
} else {
  showAuthentication();
}
