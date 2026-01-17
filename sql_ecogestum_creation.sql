DROP DATABASE IF EXISTS ECOGESTUM;
CREATE DATABASE ECOGESTUM;
USE ECOGESTUM;

-- Creation des tables
CREATE TABLE ROLE (
    id_role  INT AUTO_INCREMENT PRIMARY KEY,
    nom_role VARCHAR(100) NOT NULL
);

CREATE TABLE DEPSER (
    id_depser      INT AUTO_INCREMENT PRIMARY KEY,
    nom_depser     VARCHAR(100) NOT NULL
);

CREATE TABLE UTILISATEUR (
    id_utilisateur     INT AUTO_INCREMENT PRIMARY KEY,
    nom_utilisateur    VARCHAR(50)         NOT NULL,
    prenom_utilisateur VARCHAR(50)         NOT NULL,
    email_utilisateur  VARCHAR(100) UNIQUE NOT NULL,
    mdp_utilisateur    VARCHAR(255),
    id_role            INT NULL,
    id_depser          INT NULL,
    CONSTRAINT fk_utilisateur_role FOREIGN KEY (id_role) REFERENCES ROLE (id_role) ON DELETE SET NULL,
    CONSTRAINT fk_utilisateur_depser FOREIGN KEY (id_depser) REFERENCES DEPSER (id_depser) ON DELETE SET NULL
);

CREATE TABLE CATEGORIE (
    id_categorie          INT AUTO_INCREMENT PRIMARY KEY,
    nom_categorie         VARCHAR(100) NOT NULL,
    image_categorie       VARCHAR(255) NOT NULL,
    description_categorie VARCHAR(250),
    statut_categorie      VARCHAR(20) DEFAULT 'Active'
);

CREATE TABLE ETAT (
    id_etat  INT AUTO_INCREMENT PRIMARY KEY,
    nom_etat VARCHAR(100) NOT NULL
);

CREATE TABLE STATUTDISPONIBLE (
    id_statut_disponibilite  INT AUTO_INCREMENT PRIMARY KEY,
    nom_statut_disponibilite VARCHAR(100) NOT NULL
);

CREATE TABLE STATUTRESERVATION (
    id_statut_reservation  INT AUTO_INCREMENT PRIMARY KEY,
    nom_statut_reservation VARCHAR(100) NOT NULL
);

CREATE TABLE RAPPORT (
    id_rapport      INT AUTO_INCREMENT PRIMARY KEY,
    titre_rapport   VARCHAR(100) NOT NULL,
    contenu_rapport VARCHAR(250) NOT NULL,
    date_rapport    DATE         NOT NULL,
    id_utilisateur  INT,
    CONSTRAINT fk_rapport_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE SET NULL
);

CREATE TABLE COMMUNIQUE (
    id_communique      INT AUTO_INCREMENT PRIMARY KEY,
    titre_communique   VARCHAR(100) NOT NULL,
    contenu_communique VARCHAR(250) NOT NULL,
    date_communique    DATETIME     NOT NULL,
    id_utilisateur     INT,
    CONSTRAINT fk_communique_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE SET NULL
);

CREATE TABLE TEMOIGNAGE (
    id_temoignage      INT AUTO_INCREMENT PRIMARY KEY,
    contenu_temoignage VARCHAR(500) NOT NULL,
    date_temoignage    DATETIME     NOT NULL,
    id_utilisateur     INT,
    CONSTRAINT fk_temoignage_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE SET NULL
);

CREATE TABLE CONSEIL (
    id_conseil      INT AUTO_INCREMENT PRIMARY KEY,
    titre_conseil   VARCHAR(250) NOT NULL,
    contenu_conseil VARCHAR(255) NOT NULL,
    date_conseil    DATETIME     NOT NULL,
    id_utilisateur  INT,
    CONSTRAINT fk_conseil_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE SET NULL
);

CREATE TABLE NOTIFICATION (
    id_notification      INT AUTO_INCREMENT PRIMARY KEY,
    contenu_notification VARCHAR(200) NOT NULL,
    date_notification    DATETIME     NOT NULL
);

CREATE TABLE STATISTIQUE (
    id_statistique     INT AUTO_INCREMENT PRIMARY KEY,
    date_statistique   DATE        NOT NULL,
    type_statistique   VARCHAR(50) NOT NULL,
    valeur_statistique VARCHAR(50) NOT NULL,
    id_utilisateur     INT,
    CONSTRAINT fk_statistique_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE SET NULL
);

CREATE TABLE TYPEEVENEMENT (
    id_type_evenement  INT AUTO_INCREMENT PRIMARY KEY,
    nom_type_evenement VARCHAR(50)
);

CREATE TABLE TYPEECHANGE (
    id_type_echange  INT AUTO_INCREMENT PRIMARY KEY,
    nom_type_echange VARCHAR(50)
);

CREATE TABLE POINTCOLLECTE (
    id_point_collecte      INT AUTO_INCREMENT PRIMARY KEY,
    adresse_point_collecte VARCHAR(100),
    nom_point_collecte     VARCHAR(100)   NOT NULL,
    latitude               DECIMAL(10, 8) NOT NULL,
    longitude              DECIMAL(11, 8) NOT NULL
);

CREATE TABLE OBJET (
    id_objet                INT AUTO_INCREMENT PRIMARY KEY,
    image_objet             VARCHAR(255),
    nom_objet               VARCHAR(100) NOT NULL,
    description_objet       VARCHAR(250),
    date_ajout_objet        DATETIME     NOT NULL,
    id_point_collecte       INT,
    id_type_echange         INT,
    id_utilisateur          INT          NOT NULL,
    id_statut_disponibilite INT          NOT NULL,
    id_etat                 INT,
    id_categorie            INT,
    quantite                INT DEFAULT 1,
    CONSTRAINT fk_objet_point_collecte FOREIGN KEY (id_point_collecte) REFERENCES POINTCOLLECTE (id_point_collecte) ON DELETE SET NULL,
    CONSTRAINT fk_objet_type_echange FOREIGN KEY (id_type_echange) REFERENCES TYPEECHANGE (id_type_echange) ON DELETE SET NULL,
    CONSTRAINT fk_objet_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_objet_statut_dispo FOREIGN KEY (id_statut_disponibilite) REFERENCES STATUTDISPONIBLE (id_statut_disponibilite) ON DELETE CASCADE,
    CONSTRAINT fk_objet_etat FOREIGN KEY (id_etat) REFERENCES ETAT (id_etat) ON DELETE SET NULL,
    CONSTRAINT fk_objet_categorie FOREIGN KEY (id_categorie) REFERENCES CATEGORIE (id_categorie) ON DELETE SET NULL
);
CREATE TABLE EVENEMENT (
    id_evenement          INT AUTO_INCREMENT PRIMARY KEY,
    titre_evenement       VARCHAR(100) NOT NULL,
    description_evenement VARCHAR(250),
    date_debut_evenement  DATETIME     NOT NULL,
    date_fin_evenement    DATETIME     NOT NULL,
    id_type_evenement     INT,
    id_utilisateur        INT          NOT NULL,
    CONSTRAINT fk_evenement_type FOREIGN KEY (id_type_evenement) REFERENCES TYPEEVENEMENT (id_type_evenement) ON DELETE SET NULL,
    CONSTRAINT fk_evenement_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE CASCADE
);

CREATE TABLE RESERVER (
    id_objet              INT AUTO_INCREMENT PRIMARY KEY,
    date_reservation      DATE NOT NULL,
    id_utilisateur        INT  NOT NULL,
    id_statut_reservation INT  NOT NULL,
    CONSTRAINT fk_reserver_objet FOREIGN KEY (id_objet) REFERENCES OBJET (id_objet) ON DELETE CASCADE,
    CONSTRAINT fk_reserver_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur),
    CONSTRAINT fk_reserver_statut FOREIGN KEY (id_statut_reservation) REFERENCES STATUTRESERVATION (id_statut_reservation)
);

CREATE TABLE REGROUPER (
    id_utilisateur INT,
    id_depser      INT,
    PRIMARY KEY (id_utilisateur, id_depser),
    CONSTRAINT fk_regrouper_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_regrouper_depser FOREIGN KEY (id_depser) REFERENCES DEPSER (id_depser) ON DELETE CASCADE
);

CREATE TABLE SIGNALER (
    id_objet                INT,
    id_utilisateur          INT,
    description_signalement VARCHAR(250),
    date_signalement        DATETIME NOT NULL,
    PRIMARY KEY (id_objet, id_utilisateur),
    CONSTRAINT fk_signaler_objet FOREIGN KEY (id_objet) REFERENCES OBJET (id_objet) ON DELETE CASCADE,
    CONSTRAINT fk_signaler_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur)
);

CREATE TABLE INSCRIRE (
    id_utilisateur INT,
    id_evenement   INT,
    PRIMARY KEY (id_utilisateur, id_evenement),
    CONSTRAINT fk_inscrire_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_inscrire_evenement FOREIGN KEY (id_evenement) REFERENCES EVENEMENT (id_evenement)
);

CREATE TABLE RECEVOIR (
    id_utilisateur  INT,
    id_notification INT,
    PRIMARY KEY (id_utilisateur, id_notification),
    CONSTRAINT fk_recevoir_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT fk_recevoir_notification FOREIGN KEY (id_notification) REFERENCES NOTIFICATION (id_notification)
);

CREATE TABLE CHOIXNOTIFICATION (
    id_choixnotification INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT,
    categorie_choix VARCHAR(100),
    type_choix VARCHAR(100),
    id_choix INT,
    CONSTRAINT fk_choixnotification_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR (id_utilisateur) ON DELETE CASCADE
);