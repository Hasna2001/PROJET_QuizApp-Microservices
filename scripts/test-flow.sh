#!/usr/bin/env bash
# Scenario complet via la Gateway (port 8765) : inscription -> login -> quiz -> soumission -> classement
# Prerequis : docker compose up --build -d  (attendre ~1-2 min que tout s'enregistre dans Eureka)
set -e
GW=${GW:-http://localhost:8765}
EMAIL="hassanatou$RANDOM@example.com"

echo "== 1. Inscription"
curl -s -X POST $GW/user-service/user/inscrire -H "Content-Type: application/json" \
  -d "{\"nom\":\"Baldé\",\"prenom\":\"Hassanatou\",\"email\":\"$EMAIL\",\"motDePasse\":\"secret123\"}"; echo

echo "== 2. Login (recupere le JWT)"
LOGIN=$(curl -s -X POST $GW/auth-service/auth/login -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"motDePasse\":\"secret123\"}")
echo $LOGIN
TOKEN=$(echo $LOGIN | sed -E 's/.*"token":"([^"]+)".*/\1/')
UID_=$(echo $LOGIN | sed -E 's/.*"utilisateurId":([0-9]+).*/\1/')
AUTH="Authorization: Bearer $TOKEN"

echo "== 3. Acces sans token -> 401 attendu"
curl -s -o /dev/null -w "HTTP %{http_code}\n" $GW/question-service/question/allQuestions

echo "== 4. Creation d'un quiz Java de 10 questions"
curl -s -X POST $GW/quiz-service/quiz/creer -H "$AUTH" -H "Content-Type: application/json" \
  -d '{"categorie":"Java","numQuestions":10,"titre":"Quiz Java"}'; echo

echo "== 5. Questions du quiz 1"
QS=$(curl -s $GW/quiz-service/quiz/get/1 -H "$AUTH")
echo $QS | head -c 400; echo " ..."

echo "== 6. Soumission (toutes les reponses = option2 : score variable)"
BODY=$(echo $QS | python3 -c "import sys,json; print(json.dumps([{'id':q['id'],'response':q['option2']} for q in json.load(sys.stdin)]))")
curl -s -X POST "$GW/quiz-service/quiz/submit/1?utilisateurId=$UID_" -H "$AUTH" -H "Content-Type: application/json" -d "$BODY"; echo

echo "== 7. Points totaux"
curl -s $GW/gratification-service/gratification/total/$UID_ -H "$AUTH"; echo

echo "== 8. Top 10"
curl -s $GW/leaderboard-service/leaderboard/top/10 -H "$AUTH"; echo
