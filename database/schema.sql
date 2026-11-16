-- =====================================================================
--  AlloTech : schema.sql
--  Base de donnees MySQL 8 : 16 tables
--  Auteur : P1 (Infrastructure et base de donnees)
--
--  ATTENTION : ce script SUPPRIME puis recree toutes les tables.
--  Toutes les donnees existantes sont perdues. Apres l'execution,
--  lancer data.sql pour recharger les donnees de test.
--
--  Execution (depuis la racine du projet) :
--      mysql -u root -p < database/schema.sql
--      mysql -u root -p < database/data.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS allotech
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE allotech;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
--  Suppression des tables (ordre inverse des dependances)
-- ---------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS abonnement;
DROP TABLE IF EXISTS plan_abonnement;
DROP TABLE IF EXISTS disponibilite;
DROP TABLE IF EXISTS photo_travail;
DROP TABLE IF EXISTS notification;
DROP TABLE IF EXISTS reclamation;
DROP TABLE IF EXISTS avis;
DROP TABLE IF EXISTS offre;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS conversation;
DROP TABLE IF EXISTS demande;
DROP TABLE IF EXISTS probleme;
DROP TABLE IF EXISTS technicien;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS categorie;
DROP TABLE IF EXISTS utilisateur;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
--  1. utilisateur
--  Table commune aux 3 roles (CLIENT, TECHNICIEN, ADMIN).
--  L'administrateur n'a pas de table propre : c'est un utilisateur
--  avec role = 'ADMIN' (RG16).
--  statut_compte n'a PAS de valeur par defaut : le code doit le fournir
--      client      -> 'APPROUVE'
--      technicien  -> 'EN_ATTENTE'   (RG2)
-- =====================================================================
CREATE TABLE utilisateur (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nom_complet      VARCHAR(100)  NOT NULL,
    email            VARCHAR(150)  NOT NULL,
    mot_de_passe     VARCHAR(255)  NOT NULL,              -- hash BCrypt (RG1)
    telephone        VARCHAR(20)   NULL,
    ville            VARCHAR(80)   NULL,
    photo            VARCHAR(255)  NULL,                  -- chemin du fichier
    role             ENUM('CLIENT','TECHNICIEN','ADMIN') NOT NULL,
    statut_compte    ENUM('EN_ATTENTE','APPROUVE','REFUSE','REFUSE_DEFINITIF','SUSPENDU') NOT NULL,
    date_inscription DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_utilisateur_email UNIQUE (email),       -- RG1
    INDEX idx_utilisateur_role_statut (role, statut_compte),
    INDEX idx_utilisateur_ville (ville)
) ENGINE=InnoDB;

-- =====================================================================
--  2. categorie   (RG14 : jamais supprimee si utilisee, seulement desactivee)
-- =====================================================================
CREATE TABLE categorie (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    nom   VARCHAR(80) NOT NULL,
    actif BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_categorie_nom UNIQUE (nom)
) ENGINE=InnoDB;

-- =====================================================================
--  3. client   (herite de utilisateur : meme id)
-- =====================================================================
CREATE TABLE client (
    id      INT PRIMARY KEY,
    adresse VARCHAR(255) NULL,
    CONSTRAINT fk_client_utilisateur FOREIGN KEY (id)
        REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
--  4. technicien   (herite de utilisateur : meme id)
--  nombre_soumissions : commence a 1 a l'inscription (RG3, RG18)
--  cin unique : un refus definitif bloque la reinscription (RG18)
-- =====================================================================
CREATE TABLE technicien (
    id                 INT PRIMARY KEY,
    categorie_id       INT           NOT NULL,
    cin                VARCHAR(20)   NOT NULL,
    photo_cin          VARCHAR(255)  NOT NULL,            -- visible par l'admin seulement (RG2)
    cv                 VARCHAR(255)  NULL,
    experience         TEXT          NULL,
    lien_localisation  VARCHAR(500)  NULL,                -- lien Google Maps (RG15)
    score_moyenne      DECIMAL(3,2)  NOT NULL DEFAULT 0.00,
    motif_refus        VARCHAR(500)  NULL,
    nombre_soumissions INT           NOT NULL DEFAULT 1,
    date_approbation   DATE          NULL,
    CONSTRAINT uq_technicien_cin UNIQUE (cin),
    CONSTRAINT ck_technicien_score CHECK (score_moyenne >= 0 AND score_moyenne <= 5),
    CONSTRAINT ck_technicien_soumissions CHECK (nombre_soumissions >= 1),
    CONSTRAINT fk_technicien_utilisateur FOREIGN KEY (id)
        REFERENCES utilisateur(id) ON DELETE CASCADE,
    CONSTRAINT fk_technicien_categorie FOREIGN KEY (categorie_id)
        REFERENCES categorie(id) ON DELETE RESTRICT       -- RG14
) ENGINE=InnoDB;

-- =====================================================================
--  5. probleme   (un client decrit un probleme, puis envoie des demandes)
-- =====================================================================
CREATE TABLE probleme (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    client_id     INT          NOT NULL,
    titre         VARCHAR(150) NOT NULL,
    description   TEXT         NOT NULL,
    date_creation DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_probleme_client FOREIGN KEY (client_id)
        REFERENCES client(id) ON DELETE CASCADE,
    INDEX idx_probleme_client_date (client_id, date_creation)   -- RG6 : limite par jour
) ENGINE=InnoDB;

-- =====================================================================
--  6. demande   (un probleme envoye a un technicien)
--  UNIQUE (probleme_id, technicien_id) : jamais deux fois le meme
--  probleme au meme technicien (RG6).
-- =====================================================================
CREATE TABLE demande (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    probleme_id     INT           NOT NULL,
    technicien_id   INT           NOT NULL,
    date_creation   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_expiration DATETIME      NOT NULL,               -- date_creation + D heures (RG7)
    date_reponse    DATETIME      NULL,
    statut          ENUM('EN_ATTENTE','ACCEPTEE','REFUSEE','ANNULEE','EXPIREE') NOT NULL DEFAULT 'EN_ATTENTE',
    motif_refus     VARCHAR(500)  NULL,                   -- obligatoire si REFUSEE (RG9)
    prix_convenu    DECIMAL(10,2) NULL,                   -- en DH (RG17)
    CONSTRAINT uq_demande_probleme_technicien UNIQUE (probleme_id, technicien_id),
    CONSTRAINT ck_demande_prix CHECK (prix_convenu IS NULL OR prix_convenu > 0),
    CONSTRAINT fk_demande_probleme FOREIGN KEY (probleme_id)
        REFERENCES probleme(id) ON DELETE CASCADE,
    CONSTRAINT fk_demande_technicien FOREIGN KEY (technicien_id)
        REFERENCES technicien(id) ON DELETE CASCADE,
    INDEX idx_demande_technicien_statut (technicien_id, statut),
    INDEX idx_demande_statut_expiration (statut, date_expiration)  -- expiration automatique (RG7)
) ENGINE=InnoDB;

-- =====================================================================
--  7. conversation   (creee a l'acceptation d'une demande, RG10)
-- =====================================================================
CREATE TABLE conversation (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    demande_id    INT      NOT NULL,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_conversation_demande UNIQUE (demande_id),
    CONSTRAINT fk_conversation_demande FOREIGN KEY (demande_id)
        REFERENCES demande(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
--  8. message
-- =====================================================================
CREATE TABLE message (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id INT      NOT NULL,
    expediteur_id   INT      NOT NULL,
    contenu         TEXT     NOT NULL,
    date_envoi      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lu              BOOLEAN  NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversation(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_expediteur FOREIGN KEY (expediteur_id)
        REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_message_conversation_date (conversation_id, date_envoi)
) ENGINE=InnoDB;

-- =====================================================================
--  9. offre   (offre de prix dans une conversation, RG17)
-- =====================================================================
CREATE TABLE offre (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id  INT           NOT NULL,
    montant          DECIMAL(10,2) NOT NULL,              -- strictement positif, en DH
    statut           ENUM('PROPOSEE','ACCEPTEE','REFUSEE','ANNULEE') NOT NULL DEFAULT 'PROPOSEE',
    date_proposition DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_reponse     DATETIME      NULL,
    CONSTRAINT ck_offre_montant CHECK (montant > 0),
    CONSTRAINT fk_offre_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversation(id) ON DELETE CASCADE,
    INDEX idx_offre_conversation_statut (conversation_id, statut)
) ENGINE=InnoDB;

-- =====================================================================
--  10. avis   (un seul avis par demande acceptee, note de 1 a 5, RG11)
-- =====================================================================
CREATE TABLE avis (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    demande_id    INT      NOT NULL,
    client_id     INT      NOT NULL,
    technicien_id INT      NOT NULL,
    note          TINYINT  NOT NULL,
    commentaire   TEXT     NULL,
    date_avis     DATE     NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT uq_avis_demande UNIQUE (demande_id),
    CONSTRAINT ck_avis_note CHECK (note BETWEEN 1 AND 5),
    CONSTRAINT fk_avis_demande FOREIGN KEY (demande_id)
        REFERENCES demande(id) ON DELETE CASCADE,
    CONSTRAINT fk_avis_client FOREIGN KEY (client_id)
        REFERENCES client(id) ON DELETE CASCADE,
    CONSTRAINT fk_avis_technicien FOREIGN KEY (technicien_id)
        REFERENCES technicien(id) ON DELETE CASCADE,
    INDEX idx_avis_technicien (technicien_id)             -- calcul du score moyen
) ENGINE=InnoDB;

-- =====================================================================
--  11. reclamation   (signalement d'un technicien par un client, RG12)
-- =====================================================================
CREATE TABLE reclamation (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    client_id        INT          NOT NULL,
    technicien_id    INT          NOT NULL,
    admin_id         INT          NULL,                   -- admin qui a traite
    motif            VARCHAR(255) NOT NULL,
    description      TEXT         NULL,
    statut           ENUM('EN_ATTENTE','TRAITEE','REJETEE') NOT NULL DEFAULT 'EN_ATTENTE',
    decision         VARCHAR(255) NULL,
    date_reclamation DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_traitement  DATETIME     NULL,
    CONSTRAINT fk_reclamation_client FOREIGN KEY (client_id)
        REFERENCES client(id) ON DELETE CASCADE,
    CONSTRAINT fk_reclamation_technicien FOREIGN KEY (technicien_id)
        REFERENCES technicien(id) ON DELETE CASCADE,
    CONSTRAINT fk_reclamation_admin FOREIGN KEY (admin_id)
        REFERENCES utilisateur(id) ON DELETE SET NULL,
    INDEX idx_reclamation_statut (statut),
    INDEX idx_reclamation_technicien (technicien_id)
) ENGINE=InnoDB;

-- =====================================================================
--  12. notification   (RF39)
--  id_reference : id de l'objet concerne (demande, offre, avis...)
-- =====================================================================
CREATE TABLE notification (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT          NOT NULL,
    type           ENUM('NOUVELLE_DEMANDE','DEMANDE_ACCEPTEE','DEMANDE_REFUSEE','DEMANDE_EXPIREE',
                        'NOUVELLE_OFFRE','OFFRE_ACCEPTEE','OFFRE_REFUSEE','NOUVEL_AVIS','SIGNALEMENT') NOT NULL,
    message        VARCHAR(500) NOT NULL,
    lu             BOOLEAN      NOT NULL DEFAULT FALSE,
    date_envoi     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_reference   INT          NULL,
    CONSTRAINT fk_notification_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_notification_utilisateur_lu (utilisateur_id, lu)    -- verification toutes les 5 s
) ENGINE=InnoDB;

-- =====================================================================
--  13. photo_travail
-- =====================================================================
CREATE TABLE photo_travail (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    technicien_id INT          NOT NULL,
    chemin        VARCHAR(255) NOT NULL,
    description   VARCHAR(255) NULL,
    date_ajout    DATE         NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT fk_photo_technicien FOREIGN KEY (technicien_id)
        REFERENCES technicien(id) ON DELETE CASCADE,
    INDEX idx_photo_technicien (technicien_id)
) ENGINE=InnoDB;

-- =====================================================================
--  14. disponibilite   (creneaux horaires du technicien)
-- =====================================================================
CREATE TABLE disponibilite (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    technicien_id INT  NOT NULL,
    jour_semaine  ENUM('LUNDI','MARDI','MERCREDI','JEUDI','VENDREDI','SAMEDI','DIMANCHE') NOT NULL,
    heure_debut   TIME NOT NULL,
    heure_fin     TIME NOT NULL,
    CONSTRAINT ck_disponibilite_heures CHECK (heure_fin > heure_debut),
    CONSTRAINT fk_disponibilite_technicien FOREIGN KEY (technicien_id)
        REFERENCES technicien(id) ON DELETE CASCADE,
    INDEX idx_disponibilite_technicien_jour (technicien_id, jour_semaine)
) ENGINE=InnoDB;

-- =====================================================================
--  15. plan_abonnement   (RG14 : desactive, jamais supprime si utilise)
-- =====================================================================
CREATE TABLE plan_abonnement (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    nom        VARCHAR(80)   NOT NULL,
    prix       DECIMAL(10,2) NOT NULL,                    -- en DH
    duree_jours INT          NOT NULL,
    actif      BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_plan_nom UNIQUE (nom),
    CONSTRAINT ck_plan_prix CHECK (prix >= 0),
    CONSTRAINT ck_plan_duree CHECK (duree_jours > 0)
) ENGINE=InnoDB;

-- =====================================================================
--  16. abonnement
--  plan_id est NULL pendant le mois d'essai (periode_gratuite = TRUE, RG4)
-- =====================================================================
CREATE TABLE abonnement (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    technicien_id    INT     NOT NULL,
    plan_id          INT     NULL,
    date_debut       DATE    NOT NULL,
    date_fin         DATE    NOT NULL,
    periode_gratuite BOOLEAN NOT NULL DEFAULT FALSE,
    statut           ENUM('ESSAI','ACTIF','EXPIRE') NOT NULL,
    CONSTRAINT ck_abonnement_dates CHECK (date_fin >= date_debut),
    CONSTRAINT fk_abonnement_technicien FOREIGN KEY (technicien_id)
        REFERENCES technicien(id) ON DELETE CASCADE,
    CONSTRAINT fk_abonnement_plan FOREIGN KEY (plan_id)
        REFERENCES plan_abonnement(id) ON DELETE RESTRICT,
    INDEX idx_abonnement_technicien_statut (technicien_id, statut)  -- RG5 : recherche
) ENGINE=InnoDB;
