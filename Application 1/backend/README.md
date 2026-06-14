# Backend GET Events (Ktor)

## Démarrage rapide (H2 — sans MySQL)

Par défaut, `DB_MODE=h2` utilise une base fichier dans `./data/getevents_db`.

```bash
cd e:\App1project
.\gradlew.bat :backend:run
```

API : http://localhost:8080  
Santé : http://localhost:8080/health  
Test DB : http://localhost:8080/test-db

### Comptes de démo (créés au premier lancement)

| Rôle  | Email         | Mot de passe |
|-------|---------------|--------------|
| Admin | admin@get.mg  | admin123     |
| User  | demo@get.mg   | demo123      |

## MySQL (optionnel)

1. Démarrer MySQL : `docker compose up -d` à la racine du projet
2. Lancer le backend :

```powershell
$env:DB_MODE="mysql"
$env:DB_URL="jdbc:mysql://localhost:3306/getevents_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:DB_USER="root"
$env:DB_PASSWORD="root"
.\gradlew.bat :backend:run
```

## Variables d'environnement

| Variable      | Défaut (H2) | Description        |
|---------------|-------------|--------------------|
| DB_MODE       | h2          | `h2` ou `mysql`    |
| DB_URL        | (fichier H2)| JDBC URL           |
| DB_USER       | sa          | Utilisateur        |
| DB_PASSWORD   | (vide)      | Mot de passe       |
| JWT_SECRET    | (dev)       | Secret JWT         |
| PORT          | 8080        | Port HTTP          |
