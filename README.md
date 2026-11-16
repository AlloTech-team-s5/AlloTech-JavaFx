# AlloTech

Application desktop de mise en relation **Clients / Techniciens** (JavaFX + MySQL).
Ce dépôt contient l'infrastructure, la base de données et les modules communs (rôle P1).

## 1. Prérequis

| Outil | Version | Remarque |
|---|---|---|
| JDK | **23 ou plus** (équipe : 25, Temurin) | `java -version` : tout le monde doit avoir le même JDK, JavaFX 25 ne fonctionne pas avec un JDK plus ancien |
| Maven | 3.9 ou plus | `mvn -version` |
| MySQL Server | 8.0 ou plus | le service doit être démarré |
| Git | récent | |

## 2. Installation (à faire une seule fois)

### 2.1 Créer la base de données

Ouvrir un terminal **à la racine du projet** puis lancer le client MySQL :

```
mysql -u root -p --default-character-set=utf8mb4
```

Dans le client MySQL, exécuter les deux scripts (la commande `source` marche sous Windows, Mac et Linux) :

```
source database/schema.sql;
source database/data.sql;
```

> `schema.sql` **supprime puis recrée** toutes les tables : toutes les données sont effacées.
> `data.sql` recharge ensuite les données de test. Pour repartir de zéro, relancer les deux scripts.

### 2.2 Créer son fichier de configuration

**Copier** `config.example.properties` en `config.properties` (même dossier), puis remplacer
`VOTRE_MOT_DE_PASSE` par votre mot de passe MySQL. Si votre utilisateur n'est pas `root`,
changer aussi `db.user`. Ne pas supprimer ni renommer le modèle `config.example.properties`.

`config.properties` est dans `.gitignore` : **il ne doit jamais être envoyé sur GitHub**.

### 2.3 Vérifier que tout fonctionne

```
mvn test
```

Les tests d'intégration utilisent MySQL. Si MySQL est arrêté ou si `config.properties` est incorrect,
ils sont **ignorés** (« skipped ») et non en échec : vérifier que le résumé indique `Skipped: 0`.

### 2.4 Lancer l'application

```
mvn javafx:run
```

L'écran **Gestion des catégories** s'ouvre (ajouter, modifier, activer ou désactiver, supprimer, rechercher).
C'est l'écran provisoire de P1 : P2 le remplacera par l'écran de connexion.

## 3. Comptes de test

Le script `database/data.sql` crée automatiquement des utilisateurs de test : 1 administrateur, 2 clients et 7 techniciens,
avec des statuts différents (approuvé, en attente, refusé, suspendu, refusé définitivement).
Les identifiants de connexion :
| Rôle | E-mail | Mot de passe | Statut |
|---|---|---|---|
| Administrateur | admin@allotech.ma | `Admin@123` | APPROUVE |
| Client | sara.alaoui@test.ma | `Test@123` | APPROUVE |
| Client | youssef.benani@test.ma | `Test@123` | APPROUVE |
| Technicien | hassan.idrissi@test.ma | `Test@123` | APPROUVE (Agadir, plombier) |
| Technicien | karim.tazi@test.ma | `Test@123` | APPROUVE (Agadir, électricien) |
| Technicien | omar.berrada@test.ma | `Test@123` | APPROUVE (Casablanca, peintre) |
| Technicien | nabil.chraibi@test.ma | `Test@123` | EN_ATTENTE |
| Technicien | rachid.lahlou@test.ma | `Test@123` | REFUSE (avec motif) |
| Technicien | samir.ouazzani@test.ma | `Test@123` | SUSPENDU |
| Technicien | driss.fassi@test.ma | `Test@123` | REFUSE_DEFINITIF |

## 4. Structure du projet

```
allotech/
├── pom.xml                         dépendances et lancement
├── config.example.properties       modèle de configuration (config.properties = local, non versionné)
├── .gitignore
├── README.md
├── database/
│   ├── schema.sql                  les 16 tables
│   └── data.sql                    catégories, plans, admin, comptes de test
├── uml/                            diagrammes
├── maquettes/                      maquettes
└── src/
    ├── main/
    │   ├── java/com/allotech/
    │   │   ├── MainApp.java        point d'entrée (provisoire)
    │   │   ├── model/              classes métier et énumérations
    │   │   ├── dao/                accès MySQL (SQL uniquement)
    │   │   ├── service/             règles de gestion
    │   │   ├── controller/          contrôleurs JavaFX
    │   │   └── util/                configuration, connexion, validation, dialogues
    │   └── resources/
    │       ├── fxml/                vues : admin/client/technicien/common
    │       └── css/                 styles
    └── test/
        └── java/com/allotech/
            ├── dao/                 tests des DAO
            ├── service/             tests des services
            └── util/                tests des utilitaires
```

## 5. Règles d'équipe

- Chaque membre travaille sur sa branche et ne modifie pas les fichiers des autres.
- Fichiers appartenant à P1 : `pom.xml`, `config.example.properties`, `database/`, `util/`, `BaseDAO`, `CrudDAO`,
  les énumérations. **Pour ajouter une table, une colonne, un paramètre ou une dépendance : demander à P1.**
- Aucun SQL dans un contrôleur ; aucune règle de gestion dans un DAO.
- Ne jamais envoyer `config.properties` ni le dossier `uploads/` sur GitHub.

## 6. Répartition du travail : itération 2

Objectif : projet Maven, architecture MVC, connexion MySQL et premiers CRUD avec écrans JavaFX et tests JUnit.
Chaque membre est responsable d'un module et possède ses fichiers (modèle, DAO, service, contrôleur, FXML, tests).
La répartition des tâches entre les 6 membres a été établie par P1.

| Membre | Module | Ce qu'il livre : CRUD, écrans, tests |
|---|---|---|
| **P1 : Kaoutar Aassib** | A. Infrastructure et base de données | **Création de 37 fichiers au total** :<br>• **Données** : `schema.sql` (16 tables) et `data.sql` (catégories, plans, administrateur, utilisateurs de test) ;<br>• **Configuration du projet** : `pom.xml`, `config.example.properties`, `.gitignore`, `README.md` ;<br>• **Connexion et accès aux données** : `DBConnection`, `AppConfig`, `DataAccessException`, `BaseDAO`, `CrudDAO`, `RowMapper`, `TransactionWork` ;<br>• **Outils communs à l'équipe** : `Validator`, `AlertUtil` et les 8 énumérations (`Role`, `StatutCompte`, `StatutDemande`, `StatutOffre`, `StatutReclamation`, `StatutAbonnement`, `JourSemaine`, `TypeNotification`) ;<br>• **Gestion des catégories (CRUD complet)** : `Categorie`, `CategorieDAO`, `CategorieService`, `CategorieController` et l'écran `categories_panel.fxml` (formulaire et tableau) ;<br>• **Démarrage** : `MainApp` (point d'entrée provisoire) ;<br>• **Tests (8 fichiers, 37 tests, 0 échec)** : `DBConnectionTest`, `CategorieDAOTest`, `CategorieServiceTest`, `AppConfigTest`, `ValidatorTest`, `DataAccessExceptionTest`, et les outils `DatabaseTestSupport` et `TestData`.<br>**Répartition des tâches** de l'équipe. |
| **P2 : [Nom]** | B. Comptes, connexion et interface commune | **Code** : modèles `Utilisateur`, `Client`, `Technicien`, `Administrateur` ; `UtilisateurDAO` ; `AuthService` (inscriptions, connexion, BCrypt, 3 soumissions au plus) ; `Session`, `SceneNavigator`.<br>**Écrans** : connexion, inscription, formulaires client et technicien, écrans « Dossier en cours de vérification » et « Dossier refusé ».<br>**Test** : `AuthServiceTest`.<br>**🟠(À compléter par chaque membre : détailler les tâches réalisées, les fonctionnalités développées, etc.)** |
| **P3 : [Nom]** | C. Recherche et demandes (client) | **Code** : modèles `Probleme` et `Demande` et leurs DAO ; `DemandeService` (création, limite par problème et par jour, annulation).<br>**Écrans** : recherche par catégorie et ville, envoi d'une demande, panneau Demandes.<br>**Test** : `DemandeServiceTest`.<br>**🟠(À compléter par chaque membre : détailler les tâches réalisées, les fonctionnalités développées, etc.)** |
| **P4 : [Nom]** | D. Espace technicien | **Code** : `TechnicienDAO` ; `Disponibilite` et son DAO (CRUD des créneaux) ; invitations (accepter, refuser avec motif).<br>**Écrans** : mon profil, horaires, invitations.<br>**Tests** : `DisponibiliteDAOTest`, `TechnicienDAOTest`, validation du lien Google Maps. <br>**🟠(À compléter par chaque membre : détailler les tâches réalisées, les fonctionnalités développées, etc.)**|
| **P5 : [Nom]** | E. Messagerie et avis | **Code** : modèles `Avis`, `Conversation`, `Message` et DAO ; CRUD Avis ; `AvisService` (score moyen) ; mise à jour du diagramme de classes.<br>**Écrans** : formulaire et liste des avis, messagerie simple.<br>**Tests** : `AvisDAOTest`, calcul du score, `MessageDAOTest`.<br>**🟠(À compléter par chaque membre : détailler les tâches réalisées, les fonctionnalités développées, etc.)** |
| **P6 : [Nom]** | F. Administration et abonnement | **Code** : modèles `Abonnement`, `PlanAbonnement`, `Reclamation` et DAO ; CRUD des plans ; validation des techniciens (valider, refuser avec motif).<br>**Écrans** : plans, techniciens à valider, signalements.<br>**Tests** : `ValidationTechnicienTest`, `PlanAbonnementDAOTest`. <br>**🟠(À compléter par chaque membre : détailler les tâches réalisées, les fonctionnalités développées, etc.)**|


## - Auteurs

-  **Kaoutar Aassib**
-  **Aymane AGHENDA**
-  **Salma SOUFYAN**
-  **Mouad JARRY**
-  **Ibrahim ZAKRY**
-  **Hassan CHOUITE**

> Projet réalisé dans le cadre du module **Programmation Orientée Objet Avancée** (Encadré par **Pr. Youssef ES-SAADY**) — GI S5 Filière Génie Informatique | La Faculté des Sciences Appliquées Taroudant | 2026–2027