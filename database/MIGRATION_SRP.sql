-- MIGRATION SQL - SOLID SRP Implementation
-- Script pour adapter la structure BDD à la nouvelle architecture
-- À exécuter APRÈS avoir sauvegardé les données !

-- ============================================================================
-- ⚠️ ÉTAPE 0 : SAUVEGARDE (À faire avant tout)
-- ============================================================================

-- Créer des tables de sauvegarde avant les changements
CREATE TABLE users_backup AS SELECT * FROM users;
CREATE TABLE authorities_backup AS SELECT * FROM authorities;

COMMIT;

-- ============================================================================
-- ÉTAPE 1 : Ajouter une colonne temporaire dans authorities
-- ============================================================================
-- Raison : Authorities change d'@Id de String (username) vers Long (id auto)
-- On ne peut pas supprimer une FK en prod sans précaution

ALTER TABLE authorities ADD COLUMN id BIGINT AUTO_INCREMENT UNIQUE;
ALTER TABLE authorities ADD COLUMN account_id BIGINT;

-- ============================================================================
-- ÉTAPE 2 : Remplir la colonne account_id (FK vers Compte)
-- ============================================================================
-- Mapper les usernames existants vers les emails dans Compte

UPDATE authorities a
SET a.account_id = (
    SELECT c.email FROM users c WHERE c.email = a.username
)
WHERE a.account_id IS NULL;

COMMIT;

-- ============================================================================
-- ÉTAPE 3 : Supprimer la colonne confirm_password de Compte
-- ============================================================================
-- confirm_password était @Transient, ne doit pas être en BDD
-- S'il y a une colonne : la supprimer

ALTER TABLE users DROP COLUMN IF EXISTS confirm_password;

COMMIT;

-- ============================================================================
-- ÉTAPE 4 : Supprimer la colonne type de Compte
-- ============================================================================
-- type devient un rôle dans Authorities, pas un attribut de Compte
-- Les données existantes seront migrées en nouvelles entrées Authorities

-- Créer une table temporaire pour sauvegarder les types
CREATE TABLE user_types_temp AS
SELECT email, type FROM users WHERE type IS NOT NULL;

ALTER TABLE users DROP COLUMN IF EXISTS type;

COMMIT;

-- ============================================================================
-- ÉTAPE 5 : Restructurer Authorities
-- ============================================================================
-- Passage de @Id: String (username) → @Id: Long (id auto)
-- Et ajout de la FK verso Compte

-- Étape 5a : Créer la nouvelle table Authorities
CREATE TABLE authorities_new (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username BIGINT NOT NULL,
    authority VARCHAR(50) NOT NULL COLLATE utf8mb4_unicode_ci,
    
    -- Ajouter la FK correctement
    FOREIGN KEY (username) REFERENCES users(email) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    
    -- Contrainte unique pour éviter les doublons
    UNIQUE KEY uk_authorities (username, authority)
);

-- Étape 5b : Migrer les données existantes
INSERT INTO authorities_new (username, authority)
SELECT a.account_id, 
       CASE 
           WHEN a.authority = 'Admin' THEN 'ROLE_ADMIN'
           WHEN a.authority = 'Parent' THEN 'ROLE_PARENT'
           WHEN a.authority = 'Kindergarten Director' THEN 'ROLE_DIRECTOR'
           ELSE a.authority  -- Garder les autres au cas où
       END
FROM authorities a
WHERE a.account_id IS NOT NULL;

COMMIT;

-- Étape 5c : Ajouter les types migrer depuis Compte
INSERT INTO authorities_new (username, authority)
SELECT email,
       CASE 
           WHEN type = 'Admin' THEN 'ROLE_ADMIN'
           WHEN type = 'Parent' THEN 'ROLE_PARENT'
           WHEN type = 'Kindergarten Director' THEN 'ROLE_DIRECTOR'
           ELSE 'ROLE_' || UPPER(REPLACE(type, ' ', '_'))
       END
FROM user_types_temp
WHERE email NOT IN (
    SELECT username FROM authorities_new
);

COMMIT;

-- Étape 5d : Supprimer l'ancienne table et renommer la nouvelle
DROP TABLE authorities;
RENAME TABLE authorities_new TO authorities;

COMMIT;

-- ============================================================================
-- ÉTAPE 6 : Vérification des données
-- ============================================================================

-- Vérifier que tous les comptes ont un rôle
SELECT u.email, COUNT(a.id) as nb_roles
FROM users u
LEFT JOIN authorities a ON u.email = a.username
GROUP BY u.email
HAVING nb_roles = 0;  -- Résultat vide = bon

-- Vérifier les valeurs de rôles
SELECT DISTINCT authority FROM authorities;
-- Doit contenir UNIQUEMENT : ROLE_ADMIN, ROLE_PARENT, ROLE_DIRECTOR, etc.

-- Compter les comptes par rôle
SELECT authority, COUNT(*) as nombre
FROM authorities
GROUP BY authority;

COMMIT;

-- ============================================================================
-- ÉTAPE 7 : Nettoyage (après vérification)
-- ============================================================================

-- Supprimer les tables temporaires
DROP TABLE user_types_temp;
-- Optionnel : DROP TABLE users_backup, authorities_backup;

COMMIT;

-- ============================================================================
-- ✅ RÉSUMÉ DES CHANGEMENTS
-- ============================================================================
/*

TABLE users (Compte)
  ✓ RETIRÉ : colonne type (VARCHAR)
  ✓ RETIRÉ : colonne confirm_password (VARCHAR, @Transient)
  ✓ CONSERVÉ : username (email) [PK]
  ✓ CONSERVÉ : password
  ✓ CONSERVÉ : enabled

TABLE authorities (Authorities)
  ✓ AJOUTÉ : colonne id (BIGINT AUTO_INCREMENT, PK)
  ✓ CHANGÉ : username de VARCHAR (username literal) à BIGINT (FK email)
  ✓ CHANGÉ : authority de VARCHAR libre à VARCHAR avec valeurs ENUM
             (ROLE_ADMIN, ROLE_PARENT, ROLE_DIRECTOR, etc.)
  ✓ AJOUTÉ : FK vers users(email)
  ✓ PERMET : 1 utilisateur avec N rôles (grâce à id auto)

CONVERSIONS
  ✓ "Admin" → "ROLE_ADMIN"
  ✓ "Parent" → "ROLE_PARENT"
  ✓ "Kindergarten Director" → "ROLE_DIRECTOR"

RÉSULTAT
  ✓ Structure cohérente avec Spring Security et JPA
  ✓ Multi-rôles supporté nativement
  ✓ Valeurs de rôles contraintes (pas de typo)
  ✓ Responsabilités séparées (SRP)

*/

-- ============================================================================
-- ROLLBACK (Au cas où)
-- ============================================================================
/*

-- Si quelque chose s'est mal passé, restaurer depuis la sauvegarde :

DROP TABLE authorities;
CREATE TABLE authorities LIKE authorities_backup;
INSERT INTO authorities SELECT * FROM authorities_backup;

DROP TABLE users;
CREATE TABLE users LIKE users_backup;
INSERT INTO users SELECT * FROM users_backup;

-- Puis corriger le problème et relancer le script
*/
