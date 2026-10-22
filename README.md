# QuizApp — rapport du projet

## 1. Présentation

QuizApp est une application backend de quiz organisée en microservices Spring Boot. Les services se découvrent avec Eureka, les requêtes externes passent par une API Gateway et MySQL conserve les comptes, les questions, les quiz et les gratifications.

**Ce dépôt ne contient pas d’interface graphique frontend.** `http://localhost:8761` ouvre le tableau de bord Eureka. `http://localhost:8765` affiche les informations de la Gateway au format JSON ; ce n’est pas une interface graphique. Pour utiliser l’application, appelez les chemins d’API ci-dessous avec un client HTTP (PowerShell, curl ou Postman). Un navigateur seul ne peut pas envoyer le jeton requis ni les corps JSON des requêtes `POST`.

## 2. Architecture et responsabilités

| Service | Port hôte | Base de données | Rôle principal |
|---|---:|---|---|
| `register-service` | 8761 | — | Serveur Eureka et registre des services |
| `api-gateway` | 8765 | — | Point d’entrée HTTP, découverte/routage et vérification JWT |
| `auth-service` | 8093 | — | Authentification et émission des jetons JWT |
| `user-service` | 8091 | `userdb` | Inscription et gestion des comptes |
| `question-service` | 8096 | `questionsdb` | Banque de questions et calcul des réponses |
| `quiz-service` | 8090 | `quizdb` | Création, consultation et soumission des quiz |
| `gratification-service` | 8092 | `gratificationdb` | Calcul et consultation des points |
| `notification-service` | 8094 | — | Envoi des courriels de résultat par SMTP |
| `leaderboard-service` | 8095 | — | Calcul du classement à la demande |
| MySQL | 3307 dans Docker | — | Serveur de bases de données (port conteneur : 3306) |

### Parcours fonctionnel

1. L’utilisateur crée un compte auprès de `user-service`.
2. Il se connecte auprès de `auth-service`, qui vérifie le mot de passe et retourne un JWT.
3. La Gateway vérifie le JWT des requêtes protégées et transmet la requête au service ciblé.
4. `quiz-service` demande des questions à `question-service`, puis calcule le score de la soumission.
5. Le score est transmis à `gratification-service`, qui enregistre les points et demande l’envoi d’un courriel à `notification-service`.
6. `leaderboard-service` agrège les points pour calculer le classement.

## 3. Prérequis

### Lancement recommandé : Docker

- Docker Desktop démarré avec la commande `docker compose` disponible.
- Une copie de `.env.example` nommée `.env`.

### Lancement local sous Windows

- JDK 25 et Maven 3.9 ou ultérieur.
- MySQL démarré et accessible sur `localhost:3306`.
- Un compte MySQL `root` dont le mot de passe correspond à celui défini dans les fichiers `application.properties` (valeur du projet : `rootroot`).
- La variable `USER_SERVICE_INTERNAL_TOKEN` définie dans la session PowerShell qui lance les services.

Le port local configuré par défaut pour MySQL est **3306**. Le port **3307** est uniquement le port publié par Docker Compose sur la machine hôte ; les conteneurs communiquent entre eux sur le port 3306.

## 4. Démarrage avec Docker

Depuis la racine du projet, dans PowerShell :

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
```

Ouvrez `.env` et définissez des valeurs longues et aléatoires pour `JWT_SECRET` et `USER_SERVICE_INTERNAL_TOKEN` (les valeurs de l’exemple sont réservées au développement). Pour activer les courriels, renseignez `GMAIL_USERNAME` et `GMAIL_APP_PASSWORD` avec un mot de passe d’application Gmail. Ces valeurs sont facultatives pour le démarrage et le parcours du quiz.

```powershell
docker compose -p quizapp-microservices up --build -d
docker compose -p quizapp-microservices ps
```

La première compilation peut prendre quelques minutes. Au démarrage, la Gateway peut aussi avoir besoin de quelques minutes pour terminer son initialisation. Vérifiez qu’elle répond avant de commencer le parcours API :

```powershell
Invoke-RestMethod http://localhost:8765/
```

Attendez que les services apparaissent dans [Eureka](http://localhost:8761). MySQL doit être sain et les microservices doivent avoir démarré et s’être enregistrés avant que la Gateway puisse les router. Pour suivre les journaux, appuyez sur `Ctrl+C` pour quitter l’affichage des journaux ; cela n’arrête pas le conteneur :

```powershell
docker compose -p quizapp-microservices logs -f api-gateway
docker compose -p quizapp-microservices logs -f question-service
```

Pour arrêter l’application sans supprimer les données :

```powershell
docker compose -p quizapp-microservices down
```

Pour arrêter l’application **et effacer les données enregistrées** dans le volume MySQL :

```powershell
docker compose -p quizapp-microservices down -v
```

> `down -v` efface définitivement les données des quiz, comptes et questions stockées dans le volume MySQL. Ne l’utilisez pas pour un arrêt normal.

## 5. Démarrage local sous Windows

Vérifiez d’abord que MySQL écoute sur `localhost:3306`. Définissez ensuite un jeton interne aléatoire dans la session PowerShell qui lancera les services, puis exécutez le script depuis la racine du projet :

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:USER_SERVICE_INTERNAL_TOKEN = [Convert]::ToBase64String($bytes)
powershell -ExecutionPolicy Bypass -File .\scripts\start-all.ps1
```

Le script ouvre une fenêtre PowerShell par service, démarre les services dans l’ordre de leurs dépendances, attend l’ouverture des ports puis leur enregistrement dans Eureka. Les fenêtres restent ouvertes pour afficher les journaux. Si un démarrage échoue, consultez la fenêtre de ce service : une erreur MySQL indique généralement que MySQL n’est pas démarré, que le port est incorrect ou que le compte/mot de passe ne correspond pas.

Ordre des dépendances :

1. `register-service`
2. `user-service`, `question-service`, `notification-service`
3. `auth-service`, `quiz-service`, `gratification-service`
4. `leaderboard-service`, `api-gateway`

Il faut que Maven soit accessible dans `PATH` ou installé à l’un des emplacements recherchés par le script. Le lancement local utilise les paramètres de `application.properties` et MySQL sur le port 3306 ; il ne démarre pas MySQL lui-même.

## 6. Accès aux API

### Règles d’accès

- Préfixez chaque chemin par `http://localhost:8765`.
- La Gateway découvre les services enregistrés dans Eureka. Le préfixe du chemin est l’identifiant du service en minuscules, par exemple `/user-service/...`.
- Seules l’inscription et la connexion sont publiques.
- Pour toutes les autres routes de la Gateway, envoyez l’en-tête `Authorization: Bearer <JWT>`.
- Une route `POST` ou `PUT` doit être appelée avec sa méthode HTTP et son corps JSON ; coller son URL dans la barre d’adresse d’un navigateur ne l’exécute pas.

| Méthode | Chemin via la Gateway | Accès | Description |
|---|---|---|---|
| `POST` | `/user-service/user/inscrire` | Public | Inscrire un utilisateur |
| `POST` | `/auth-service/auth/login` | Public | Se connecter et obtenir le JWT |
| `GET` | `/user-service/user/all` | JWT | Lister les utilisateurs |
| `GET` | `/user-service/user/{id}` | JWT | Consulter un utilisateur |
| `GET` | `/user-service/user/email/{email}` | JWT | Rechercher un utilisateur par courriel |
| `PUT` | `/user-service/user/modifier/{id}` | JWT | Modifier un utilisateur |
| `DELETE` | `/user-service/user/supprimer/{id}` | JWT | Supprimer un utilisateur |
| `GET` | `/auth-service/auth/valider?token={JWT}` | JWT | Valider un JWT (le JWT doit aussi être fourni dans l’en-tête) |
| `GET` | `/question-service/question/allQuestions` | JWT | Lister les questions |
| `GET` | `/question-service/question/categorie/{categorie}` | JWT | Filtrer les questions par catégorie |
| `POST` | `/question-service/question/ajouter` | JWT | Ajouter une question |
| `GET` | `/question-service/question/generate?nomCategorie={categorie}&nombreDeQuestions={n}` | Interne | Sélectionner des questions pour un quiz |
| `POST` | `/question-service/question/getQuestions` | Interne | Récupérer les questions correspondant à des identifiants |
| `POST` | `/question-service/question/getScore` | Interne | Calculer le score d’une soumission |
| `POST` | `/quiz-service/quiz/creer` | JWT | Créer un quiz |
| `GET` | `/quiz-service/quiz/get/{id}` | JWT | Obtenir les questions d’un quiz |
| `POST` | `/quiz-service/quiz/submit/{id}?utilisateurId={userId}` | JWT | Soumettre les réponses |
| `POST` | `/gratification-service/gratification/ajouter` | Interne | Enregistrer une gratification |
| `GET` | `/gratification-service/gratification/user/{userId}` | JWT | Consulter les gratifications d’un utilisateur |
| `GET` | `/gratification-service/gratification/total/{userId}` | JWT | Consulter le total de points |
| `GET` | `/gratification-service/gratification/all` | JWT | Lister les gratifications |
| `GET` | `/gratification-service/gratification/{id}` | JWT | Consulter une gratification |
| `POST` | `/notification-service/notification/quiz-complete` | Interne | Envoyer le courriel de résultat |
| `GET` | `/leaderboard-service/leaderboard/classement` | JWT | Obtenir le classement complet |
| `GET` | `/leaderboard-service/leaderboard/top/{n}` | JWT | Obtenir les `n` premiers |
| `GET` | `/leaderboard-service/leaderboard/rang/{userId}` | JWT | Obtenir le rang d’un utilisateur |

Les routes marquées **Interne** sont utilisées par les échanges entre microservices ; elles ne sont pas nécessaires pour le parcours utilisateur normal.

**Limite de sécurité importante :** la mention « JWT » ci-dessus signifie que la requête est contrôlée lorsqu’elle passe par la Gateway. Dans `docker-compose.yml`, les ports des services métier sont également publiés sur l’hôte ; un appel direct à ces ports contourne donc ce contrôle. En particulier, `user-service` autorise actuellement toutes les requêtes dans sa configuration Spring Security. Ne considérez pas les opérations sensibles comme protégées contre un accès direct.

## 7. Exemple de parcours HTTP sous PowerShell

### Parcours complet interactif sous Windows

Ce script crée un compte, se connecte, crée un quiz Java, affiche les questions, demande une réponse pour chacune et affiche le score. Exécutez tout le bloc dans une seule fenêtre PowerShell, avec Docker démarré. Saisissez votre adresse e-mail et choisissez un mot de passe ; ne réutilisez pas ce mot de passe sur un autre service.

```powershell
$base = "http://localhost:8765"
$email = Read-Host "Ton adresse e-mail"
$motDePasse = Read-Host "Choisis un mot de passe pour ce compte de démonstration"

# \u00e9 représente « é » en JSON et évite les problèmes d'encodage
# des accents avec Windows PowerShell 5.
$inscriptionJson = '{"nom":"Bald\u00e9","prenom":"Hassanatou","email":"' + $email + '","motDePasse":"' + $motDePasse + '"}'
$inscriptionBody = [System.Text.Encoding]::UTF8.GetBytes($inscriptionJson)

try {
    Invoke-RestMethod -Method Post -Uri "$base/user-service/user/inscrire" `
        -ContentType "application/json; charset=utf-8" -Body $inscriptionBody
} catch {
    if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 409) {
        Write-Host "Ce compte existe déjà ; la connexion va être tentée."
    } else {
        throw
    }
}

$identifiants = @{
    email = $email
    motDePasse = $motDePasse
} | ConvertTo-Json

$session = Invoke-RestMethod -Method Post -Uri "$base/auth-service/auth/login" `
    -ContentType "application/json" -Body $identifiants
$headers = @{ Authorization = "Bearer $($session.token)" }

$quiz = @{
    categorie = "Java"
    numQuestions = 5
    titre = "Quiz Java"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/quiz-service/quiz/creer" `
    -Headers $headers -ContentType "application/json" -Body $quiz

# La création renvoie "Success" et non l'identifiant du quiz.
$quizId = [int](docker exec mysql mysql -N -uroot -prootroot quizdb -e "SELECT MAX(id) FROM quiz")
$questions = Invoke-RestMethod -Method Get -Uri "$base/quiz-service/quiz/get/$quizId" `
    -Headers $headers

$reponses = @()
foreach ($question in $questions) {
    Write-Host ""
    Write-Host $question.questionTitle
    Write-Host "1) $($question.option1)"
    Write-Host "2) $($question.option2)"
    Write-Host "3) $($question.option3)"
    Write-Host "4) $($question.option4)"

    do {
        $choix = Read-Host "Ta réponse (1-4)"
    } while ($choix -notmatch '^[1-4]$')

    $nomOption = "option$choix"
    $reponses += @{
        id = $question.id
        response = [string]$question.$nomOption
    }
}

$corpsReponses = ConvertTo-Json -InputObject @($reponses) -Depth 5
$score = Invoke-RestMethod -Method Post `
    -Uri "$base/quiz-service/quiz/submit/$quizId`?utilisateurId=$($session.utilisateurId)" `
    -Headers $headers -ContentType "application/json" -Body $corpsReponses

Write-Host ""
Write-Host "Résultat : $score / $($questions.Count)"
```

Si le compte existe déjà et que tu as oublié son mot de passe, le script ne peut pas le réinitialiser : connecte-toi avec le bon mot de passe ou utilise une autre adresse. Si aucune question Java n’est disponible, vérifie les journaux de `question-service`.

### Inscription et connexion

```powershell
$base = "http://localhost:8765"
$email = "utilisateur@example.com"

$inscription = @{
    nom = "Baldé"
    prenom = "Hassanatou"
    email = $email
    motDePasse = "secret123"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/user-service/user/inscrire" `
    -ContentType "application/json" -Body $inscription

$identifiants = @{
    email = $email
    motDePasse = "secret123"
} | ConvertTo-Json

$session = Invoke-RestMethod -Method Post -Uri "$base/auth-service/auth/login" `
    -ContentType "application/json" -Body $identifiants

$token = $session.token
$utilisateurId = $session.utilisateurId
$headers = @{ Authorization = "Bearer $token" }
```

### Création et consultation d’un quiz

```powershell
$quiz = @{
    categorie = "Java"
    numQuestions = 10
    titre = "Quiz Java"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "$base/quiz-service/quiz/creer" `
    -Headers $headers -ContentType "application/json" -Body $quiz

# Remplacez 1 par l’identifiant du quiz à consulter.
$questions = Invoke-RestMethod -Method Get -Uri "$base/quiz-service/quiz/get/1" `
    -Headers $headers
$questions
```

### Soumission et classement

Chaque réponse contient l’identifiant de la **question** (`id`) et le texte de la réponse (`response`). Reprenez les identifiants et les choix exacts renvoyés par l’API ; l’identifiant du quiz dans le chemin et l’identifiant de l’utilisateur dans la query string doivent également être ceux de votre session.

La création renvoie actuellement le texte `Success`, pas l’identifiant du quiz. Dans une base neuve, le premier quiz porte généralement l’identifiant `1`. Si la base contient déjà des quiz, repérez l’identifiant dans la table `quiz` de `quizdb` avant les appels de consultation et de soumission.

```powershell
$reponses = @(
    @{ id = 1; response = "final" }
) | ConvertTo-Json

Invoke-RestMethod -Method Post `
    -Uri "$base/quiz-service/quiz/submit/1?utilisateurId=$utilisateurId" `
    -Headers $headers -ContentType "application/json" -Body $reponses

Invoke-RestMethod -Method Get `
    -Uri "$base/gratification-service/gratification/total/$utilisateurId" `
    -Headers $headers

Invoke-RestMethod -Method Get `
    -Uri "$base/leaderboard-service/leaderboard/top/10" -Headers $headers
```

Dans cet exemple, remplacez `1` par l’identifiant réel du quiz et par l’identifiant d’une question présente dans ce quiz. La première réponse de démonstration Java est `final` ; consultez la liste retournée pour les autres questions.

### Barème des points

La gratification est calculée à partir du score obtenu au quiz :

| Score | Points attribués |
|---:|---:|
| 0 à 2 | 10 |
| 3 à 4 | 30 |
| 5 à 6 | 50 |
| 7 à 9 | 70 |
| 10 ou plus | 100 |

## 8. Bases de données et données de démonstration

`init-db.sql` crée les bases `userdb`, `questionsdb`, `quizdb` et `gratificationdb` lors de l’initialisation du volume MySQL Docker. Les tables sont ensuite gérées par Hibernate (`ddl-auto=update`). En exécution locale, les propriétés de connexion demandent la création des bases si nécessaire.

Au premier démarrage de `question-service`, 20 questions de démonstration sont insérées si la table est vide : 12 questions Java et 8 questions Python. Au démarrage suivant, les anciennes questions Java de démonstration reconnues par leur texte sont mises à jour sans changer leurs identifiants ; les autres questions et les données des autres services sont conservées.

## 9. Diagnostic des erreurs courantes

| Symptôme | Cause probable | Vérification / action |
|---|---|---|
| Eureka répond, mais les routes Gateway renvoient `404` | Le microservice ciblé n’a pas démarré ou n’est pas enregistré dans Eureka ; le chemin ou son préfixe est incorrect | Vérifier le service dans [Eureka](http://localhost:8761), consulter ses journaux et utiliser le chemin complet `/identifiant-service/chemin-du-contrôleur` |
| `404` en ouvrant `http://localhost:8765/` | La Gateway n’a pas été reconstruite ou redémarrée | Avec Docker : exécuter `docker compose -p quizapp-microservices up --build -d api-gateway`, puis réessayer |
| `401 Unauthorized` | Jeton absent, invalide ou envoyé sans le préfixe `Bearer ` | Se reconnecter, puis envoyer `Authorization: Bearer <JWT>` |
| `405 Method Not Allowed` | La route existe, mais la méthode HTTP n’est pas celle attendue | Respecter la méthode indiquée dans le tableau des API (`GET`, `POST`, etc.) |
| Service de données en échec au démarrage local | MySQL absent, inaccessible ou identifiants différents | Vérifier `localhost:3306`, le compte et le mot de passe MySQL dans les propriétés du service |
| `Access denied` ou `Unknown database` | Identifiants MySQL incorrects ou initialisation de base échouée | Consulter les journaux MySQL et vérifier le volume/droits de l’utilisateur |
| Création de quiz sans questions disponibles | Catégorie inexistante ou base de questions non initialisée | Vérifier que `question-service` est enregistré et que ses questions existent |
| Courriel non envoyé | Paramètres SMTP absents ou incorrects | Renseigner un mot de passe d’application Gmail dans `.env` et consulter les journaux de notification ; le score et les points restent indépendants de l’envoi du courriel |

Les noms `localhost` et `mysql` ne sont pas interchangeables : les fichiers locaux utilisent `localhost:3306`, tandis que les conteneurs accèdent à MySQL par `mysql:3306`. Docker publie le port conteneur 3306 sur `localhost:3307`.

## 10. Technologies

- Java 25 et Spring Boot 4.1.1
- Spring Cloud 2025.1.2 : Eureka, OpenFeign et Gateway WebFlux
- MySQL 8.4
- JWT avec JJWT 0.12.6
- Maven
- Thymeleaf pour le modèle de courriel

## 11. Portée et limites connues

- Les appels interservices sont synchrones ; Resilience4j et les mécanismes de reprise ne sont pas configurés.
- La Gateway vérifie le JWT, mais les services métier ne le valident pas eux-mêmes. Les ports de ces services sont publiés par Docker Compose ; un appel direct les contourne. `user-service` utilise actuellement `permitAll()` : ses opérations de modification et de suppression ne disposent pas d’un contrôle d’autorisation propre au service.
- Le jeton `USER_SERVICE_INTERNAL_TOKEN` protège uniquement la route interne de récupération du hash utilisée par l’authentification ; il ne remplace pas l’authentification ni l’autorisation des autres endpoints interservices.
- Le JWT utilise un secret symétrique partagé par `auth-service` et la Gateway. Ils doivent recevoir la même valeur via `JWT_SECRET` ; la valeur de développement fournie dans les fichiers de configuration ne doit pas être utilisée en production. Les secrets ne doivent pas être commités.
- La sécurité fine par rôles et les contrôles de propriété (par exemple, empêcher un utilisateur de modifier ou supprimer le compte d’un autre) ne sont pas implémentés.
- Les DTO publics de `user-service` excluent le hash du mot de passe ; la route interne d’authentification ne le retourne qu’avec le jeton de service attendu.
- L’envoi d’un courriel dépend de la configuration SMTP ; il n’est pas nécessaire au calcul du score.
