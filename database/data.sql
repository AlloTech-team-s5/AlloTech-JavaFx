-- =====================================================================
--  AlloTech : data.sql
--  Donnees initiales et donnees de test.
--  A executer APRES schema.sql :
--      mysql -u root -p < database/data.sql
--
--  COMPTES DE TEST
--  ---------------------------------------------------------------
--   Role         Email                          Mot de passe
--   ADMIN        admin@allotech.ma              Admin@123
--   CLIENT       sara.alaoui@test.ma            Test@123
--   CLIENT       youssef.benani@test.ma         Test@123
--   TECHNICIEN   (tous les techniciens ci-dessous) Test@123
--
--  Les mots de passe sont stockes sous forme de hash BCrypt ($2a$),
--  lisible par la librairie jBCrypt (BCrypt.checkpw).
-- =====================================================================

USE allotech;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
--  Categories (10)
-- ---------------------------------------------------------------------
INSERT INTO categorie (id, nom, actif) VALUES
    (1,  'Plombier',        TRUE),
    (2,  'Électricien',     TRUE),
    (3,  'Peintre',         TRUE),
    (4,  'Menuisier',       TRUE),
    (5,  'Maçon',           TRUE),
    (6,  'Carreleur',       TRUE),
    (7,  'Serrurier',       TRUE),
    (8,  'Climatisation',   TRUE),
    (9,  'Jardinier',       TRUE),
    (10, 'Mécanicien',      FALSE);   -- exemple de categorie desactivee

-- ---------------------------------------------------------------------
--  Plans d'abonnement (2 plans, prix en DH)
-- ---------------------------------------------------------------------
INSERT INTO plan_abonnement (id, nom, prix, duree_jours, actif) VALUES
    (1, 'Plan Mensuel', 99.00,  30,  TRUE),
    (2, 'Plan Annuel',  999.00, 365, TRUE);

-- ---------------------------------------------------------------------
--  Utilisateurs
--  id 1       : administrateur
--  id 2 - 3   : clients
--  id 4 - 10  : techniciens (un par statut possible)
-- ---------------------------------------------------------------------
INSERT INTO utilisateur (id, nom_complet, email, mot_de_passe, telephone, ville, photo, role, statut_compte, date_inscription) VALUES
    (1,  'Administrateur AlloTech', 'admin@allotech.ma',
         '$2a$10$wMPjs69cZwqMjatSwDcmke.E6kYDDfFvhAZIql7w1f8vagzDWdSWu',
         '0600000000', 'Agadir', NULL, 'ADMIN', 'APPROUVE', NOW()),

    (2,  'Sara Alaoui', 'sara.alaoui@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0611111111', 'Agadir', NULL, 'CLIENT', 'APPROUVE', NOW()),
    (3,  'Youssef Benani', 'youssef.benani@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0622222222', 'Casablanca', NULL, 'CLIENT', 'APPROUVE', NOW()),

    (4,  'Hassan Idrissi', 'hassan.idrissi@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0633333333', 'Agadir', NULL, 'TECHNICIEN', 'APPROUVE', NOW()),
    (5,  'Karim Tazi', 'karim.tazi@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0644444444', 'Agadir', NULL, 'TECHNICIEN', 'APPROUVE', NOW()),
    (6,  'Omar Berrada', 'omar.berrada@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0655555555', 'Casablanca', NULL, 'TECHNICIEN', 'APPROUVE', NOW()),
    (7,  'Nabil Chraibi', 'nabil.chraibi@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0666666666', 'Marrakech', NULL, 'TECHNICIEN', 'EN_ATTENTE', NOW()),
    (8,  'Rachid Lahlou', 'rachid.lahlou@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0677777777', 'Rabat', NULL, 'TECHNICIEN', 'REFUSE', NOW()),
    (9,  'Samir Ouazzani', 'samir.ouazzani@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0688888888', 'Agadir', NULL, 'TECHNICIEN', 'SUSPENDU', NOW()),
    (10, 'Driss Fassi', 'driss.fassi@test.ma',
         '$2a$10$nermFH/JfxUhhAzSHh0HE.aXXI6DZXikWBdf8hgIkHUZnq49PIGki',
         '0699999999', 'Inezgane', NULL, 'TECHNICIEN', 'REFUSE_DEFINITIF', NOW());

INSERT INTO client (id, adresse) VALUES
    (2, 'Hay Salam, Agadir'),
    (3, 'Maarif, Casablanca');

-- ---------------------------------------------------------------------
--  Techniciens (details du dossier)
--  4, 5, 6 = APPROUVE  (4 et 5 a Agadir, 6 a Casablanca)
--  7 = EN_ATTENTE, 8 = REFUSE (motif), 9 = SUSPENDU, 10 = REFUSE_DEFINITIF
-- ---------------------------------------------------------------------
INSERT INTO technicien (id, categorie_id, cin, photo_cin, cv, experience, lien_localisation,
                        score_moyenne, motif_refus, nombre_soumissions, date_approbation) VALUES
    (4,  1, 'JB100001', 'uploads/cin/jb100001.jpg', 'uploads/cv/hassan.pdf',
         '8 ans d''experience en plomberie sanitaire et chauffe-eau.',
         'https://www.google.com/maps/place/Agadir', 0.00, NULL, 1, CURDATE()),
    (5,  2, 'JB100002', 'uploads/cin/jb100002.jpg', 'uploads/cv/karim.pdf',
         '5 ans d''experience en installation electrique residentielle.',
         'https://www.google.com/maps/place/Agadir', 0.00, NULL, 1, CURDATE()),
    (6,  3, 'BE100003', 'uploads/cin/be100003.jpg', 'uploads/cv/omar.pdf',
         '10 ans d''experience en peinture interieure et exterieure.',
         'https://www.google.com/maps/place/Casablanca', 0.00, NULL, 1, CURDATE()),
    (7,  4, 'EE100004', 'uploads/cin/ee100004.jpg', 'uploads/cv/nabil.pdf',
         '3 ans d''experience en menuiserie bois.',
         NULL, 0.00, NULL, 1, NULL),
    (8,  5, 'A100005',  'uploads/cin/a100005.jpg',  'uploads/cv/rachid.pdf',
         '4 ans d''experience en maconnerie.',
         NULL, 0.00, 'Photo de la CIN illisible : merci de renvoyer une photo nette.', 1, NULL),
    (9,  1, 'JB100006', 'uploads/cin/jb100006.jpg', 'uploads/cv/samir.pdf',
         '6 ans d''experience en plomberie.',
         'https://www.google.com/maps/place/Agadir', 0.00, NULL, 1, CURDATE()),
    (10, 7, 'JB100007', 'uploads/cin/jb100007.jpg', 'uploads/cv/driss.pdf',
         '2 ans d''experience en serrurerie.',
         NULL, 0.00, 'Documents falsifies.', 3, NULL);

-- ---------------------------------------------------------------------
--  Abonnements : essai gratuit d'un mois pour les techniciens approuves
-- ---------------------------------------------------------------------
INSERT INTO abonnement (technicien_id, plan_id, date_debut, date_fin, periode_gratuite, statut) VALUES
    (4, NULL, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), TRUE, 'ESSAI'),
    (5, NULL, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), TRUE, 'ESSAI'),
    (6, NULL, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), TRUE, 'ESSAI'),
    (9, NULL, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), TRUE, 'ESSAI');

-- ---------------------------------------------------------------------
--  Disponibilites (exemples)
-- ---------------------------------------------------------------------
INSERT INTO disponibilite (technicien_id, jour_semaine, heure_debut, heure_fin) VALUES
    (4, 'LUNDI',    '08:00:00', '17:00:00'),
    (4, 'MARDI',    '08:00:00', '17:00:00'),
    (4, 'MERCREDI', '08:00:00', '12:00:00'),
    (5, 'LUNDI',    '09:00:00', '18:00:00'),
    (5, 'JEUDI',    '09:00:00', '18:00:00'),
    (6, 'SAMEDI',   '08:00:00', '14:00:00');

-- ---------------------------------------------------------------------
--  Problemes et demandes (pour tester les listes et les rapports)
-- ---------------------------------------------------------------------
INSERT INTO probleme (id, client_id, titre, description, date_creation) VALUES
    (1, 2, 'Fuite sous l''evier', 'L''eau coule sous l''evier de la cuisine depuis ce matin.', NOW()),
    (2, 2, 'Prise electrique en panne', 'Les prises du salon ne fonctionnent plus.', NOW()),
    (3, 3, 'Peinture du salon', 'Je souhaite repeindre un salon de 25 m2.', NOW());

INSERT INTO demande (id, probleme_id, technicien_id, date_creation, date_expiration, date_reponse, statut, motif_refus, prix_convenu) VALUES
    (1, 1, 4, NOW(), DATE_ADD(NOW(), INTERVAL 48 HOUR), NOW(), 'ACCEPTEE', NULL, NULL),
    (2, 2, 5, NOW(), DATE_ADD(NOW(), INTERVAL 48 HOUR), NULL,  'EN_ATTENTE', NULL, NULL),
    (3, 3, 6, NOW(), DATE_ADD(NOW(), INTERVAL 48 HOUR), NOW(), 'REFUSEE', 'Je ne suis pas disponible cette semaine.', NULL);

-- Conversation ouverte pour la demande acceptee (RG10)
INSERT INTO conversation (id, demande_id, date_creation) VALUES
    (1, 1, NOW());

INSERT INTO message (conversation_id, expediteur_id, contenu, date_envoi, lu) VALUES
    (1, 2, 'Bonjour, pouvez-vous passer demain matin ?', NOW(), TRUE),
    (1, 4, 'Bonjour, oui je peux etre chez vous vers 9h.', NOW(), FALSE);
