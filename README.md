# Pingloo - Visual Database Editor

## TODO

### Système de base
- [X] Pouvoir nommer et modifier le nom de la bdd
- [ ] Créer une table
- [ ] Créer les attributs d'une table

#### Chargement
- [ ] charger un fichier
    - [X] MYSQL
    - [ ] MSSQL
    - [ ] PostgreSQL
    - [ ] ...
- [ ] se connecter à une bdd
    - [X] MYSQL
    - [ ] MSSQL
    - [ ] PostgreSQL
    - [ ] ...
- [ ] charger un dictionnaire des données et ses dépendances fonctionnelles

#### Exportation
- [ ] exporter un fichier
    - [X] MYSQL
    - [ ] MSSQL
    - [ ] PostgreSQL
    - [ ] ...
- [ ] créer/modifier une bdd
    - [ ] MYSQL
    - [ ] MSSQL
    - [ ] PostgreSQL
    - [ ] ...
- [ ] exporter un dictionnaire des données et ses dépendances fonctionnelles

### Personnalisation
- [X] Choisir un thème (couleur)
- [X] Pouvoir modifier le thème perso

### Vues
- [X] MLD
- [ ] MCD
- [ ] dictionnaire des données + dépendances fonctionnelles
- [ ] schéma des dépendances fonctionnelles
- [ ] visualisation des données des tables + modification

### Autres
- [ ] style des pages comme blender (pour les vues)
- [ ] les types de données
- [ ] contraintes de tables
- [ ] foreign keys
- [ ] cardinalités -> à partir de MCD
- [ ] liens visuelles entre les tables (fk) (flèches)

## Revue de code

### controller
- [ ] CanvasController
- [ ] TableController
- [ ] view/View
- [ ] view/McdController
- [ ] view/MldController
- [ ] view/DfController
- [X] view/helpers/LassoSelector
- [X] view/helpers/MultiDragManager
- [X] view/helpers/SelectionModel
- [X] view/helpers/ZoomPanHandler
- [ ] parameter/ThemeController
- [ ] parameter/MysqlController

### model
- [X] DatabaseSchema
- [X] Table
- [X] Column
- [X] ForeignKey

### sql
#### db
- [ ] SqlDb
- [ ] MySqlDb
- [ ] MsSqlDb
- [ ] PostgreSqlDb
#### file
- [ ] parser/SqlParser
- [ ] parser/MySqlParser
- [ ] exporter/SqlExporter
- [ ] exporter/MySqlExporter

### theme
- [ ] Theme
- [ ] LightTheme
- [ ] DarkTheme
- [ ] PersoTheme

### util
- [ ] DbManager
- [ ] FileManager
- [ ] JsonManager
- [ ] ThemeManager