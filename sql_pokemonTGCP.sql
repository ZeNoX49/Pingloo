-- Désactive temporairement les contraintes de clés étrangères
SET FOREIGN_KEY_CHECKS = 0;

-- Supprime les tables si elles existent (dans le bon ordre)
DROP TABLE IF EXISTS AutreCarte_Type;
DROP TABLE IF EXISTS AutreCarte_Pokemon;
DROP TABLE IF EXISTS eng_atk_2;
DROP TABLE IF EXISTS eng_atk_1;
DROP TABLE IF EXISTS PokemonCarteVariation;
DROP TABLE IF EXISTS AutreCarteVariation;
DROP TABLE IF EXISTS AutreCarte;
DROP TABLE IF EXISTS PokemonCarte;
DROP TABLE IF EXISTS Rarete;
DROP TABLE IF EXISTS Paquet;
DROP TABLE IF EXISTS Extension;
DROP TABLE IF EXISTS Energie;
DROP TABLE IF EXISTS Pokemon;
DROP TABLE IF EXISTS TypeEvolution;

-- Réactive les checks après le DROP
SET FOREIGN_KEY_CHECKS = 1;

-- Création des tables

CREATE TABLE TypeEvolution(
   id_evolution INT PRIMARY KEY AUTO_INCREMENT,
   nom_evolution VARCHAR(50) NOT NULL
);

CREATE TABLE Pokemon(
   num_pokemon INT PRIMARY KEY,
   generation_pokemon INT NOT NULL,
   nom_pokemon VARCHAR(150) NOT NULL,
   id_type_evolution INT NOT NULL,
   num_pokemon_evolution INT,
   image_pokemon TEXT NOT NULL,
   FOREIGN KEY(id_type_evolution) REFERENCES TypeEvolution(id_evolution),
   FOREIGN KEY(num_pokemon_evolution) REFERENCES Pokemon(num_pokemon)
);

CREATE TABLE Energie(
   id_eng SMALLINT PRIMARY KEY AUTO_INCREMENT,
   nom_eng VARCHAR(50) NOT NULL
);

CREATE TABLE Extension(
   id_extension VARCHAR(3) PRIMARY KEY,
   nom_extension VARCHAR(150) UNIQUE NOT NULL
);

CREATE TABLE Paquet(
   id_paquet VARCHAR(50) PRIMARY KEY,
   nomBonus_paquet VARCHAR(50),
   id_extension VARCHAR(3) NOT NULL,
   FOREIGN KEY(id_extension) REFERENCES Extension(id_extension)
);

CREATE TABLE Rarete(
   id_rarete INT AUTO_INCREMENT PRIMARY KEY,
   nom_rarete VARCHAR(100) NOT NULL
);

CREATE TABLE PokemonCarte(
   id_carte_base VARCHAR(7) PRIMARY KEY,
   num_pokemon INT NOT NULL,
   num_variation INT NOT NULL,
   nom_pokemon VARCHAR(50) NOT NULL,
   pv_pokemon INT NOT NULL,
   type_pokemon SMALLINT NOT NULL,
   competence_speciale TEXT,
   nom_atk_1 VARCHAR(50) NOT NULL,
   deg_atk_1 VARCHAR(4) NOT NULL,
   txt_atk_1 TEXT,
   nom_atk_2 VARCHAR(50),
   deg_atk_2 VARCHAR(4),
   txt_atk_2 TEXT,
   cout_retraite SMALLINT NOT NULL,
   faiblesse SMALLINT,
   FOREIGN KEY(num_pokemon) REFERENCES Pokemon(num_pokemon),
   FOREIGN KEY(type_pokemon) REFERENCES Energie(id_eng),
   FOREIGN KEY(faiblesse) REFERENCES Energie(id_eng)
);

CREATE TABLE AutreCarte(
   id_carte_base VARCHAR(7) PRIMARY KEY,
   nom_carte VARCHAR(50) UNIQUE NOT NULL,
   description_carte TEXT NOT NULL
);

CREATE TABLE PokemonCarteVariation(
   id_carte VARCHAR(7) PRIMARY KEY,
   image_carte TEXT NOT NULL,
   id_carte_base VARCHAR(7) NOT NULL,
   id_paquet VARCHAR(50) NOT NULL,
   id_rarete INT NOT NULL,
   nb_possede INT NOT NULL,
   FOREIGN KEY(id_carte_base) REFERENCES PokemonCarte(id_carte_base),
   FOREIGN KEY(id_paquet) REFERENCES Paquet(id_paquet),
   FOREIGN KEY(id_rarete) REFERENCES Rarete(id_rarete)
);

CREATE TABLE AutreCarteVariation(
   id_carte VARCHAR(7) PRIMARY KEY,
   image_carte TEXT NOT NULL,
   id_carte_base VARCHAR(7) NOT NULL,
   id_paquet VARCHAR(50) NOT NULL,
   id_rarete INT NOT NULL,
   nb_possede INT NOT NULL,
   FOREIGN KEY(id_carte_base) REFERENCES AutreCarte(id_carte_base),
   FOREIGN KEY(id_paquet) REFERENCES Paquet(id_paquet),
   FOREIGN KEY(id_rarete) REFERENCES Rarete(id_rarete)
);

CREATE TABLE eng_atk_1(
   id_eng SMALLINT,
   id_carte_base VARCHAR(7),
   quantite SMALLINT NOT NULL,
   PRIMARY KEY(id_eng, id_carte_base),
   FOREIGN KEY(id_eng) REFERENCES Energie(id_eng),
   FOREIGN KEY(id_carte_base) REFERENCES PokemonCarte(id_carte_base)
);

CREATE TABLE eng_atk_2(
   id_eng SMALLINT,
   id_carte_base VARCHAR(7),
   quantite SMALLINT NOT NULL,
   PRIMARY KEY(id_eng, id_carte_base),
   FOREIGN KEY(id_eng) REFERENCES Energie(id_eng),
   FOREIGN KEY(id_carte_base) REFERENCES PokemonCarte(id_carte_base)
);

CREATE TABLE AutreCarte_Pokemon(
   id_autre_carte VARCHAR(7),
   num_pokemon INT,
   PRIMARY KEY(id_autre_carte, num_pokemon),
   FOREIGN KEY(num_pokemon) REFERENCES Pokemon(num_pokemon),
   FOREIGN KEY(id_autre_carte) REFERENCES AutreCarte(id_carte_base)
);

CREATE TABLE AutreCarte_Type(
   id_autre_carte VARCHAR(7),
   id_eng SMALLINT,
   PRIMARY KEY(id_autre_carte, id_eng),
   FOREIGN KEY(id_eng) REFERENCES Energie(id_eng),
   FOREIGN KEY(id_autre_carte) REFERENCES AutreCarte(id_carte_base)
);