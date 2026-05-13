-- =====================================================
-- BASE DE DONNÉES KINDERGARTEN (Version corrigée pgAdmin)
-- =====================================================

CREATE EXTENSION IF NOT EXISTS plpgsql;

-- =========================
-- TABLE users
-- =========================
CREATE TABLE public.users (
    username VARCHAR(255) PRIMARY KEY,
    enabled BOOLEAN NOT NULL,
    password VARCHAR(255) NOT NULL,
    type VARCHAR(255)
);

-- =========================
-- TABLE authorities
-- =========================
CREATE TABLE public.authorities (
    username VARCHAR(255),
    authority VARCHAR(255)
);

-- =========================
-- TABLE director
-- =========================
CREATE TABLE public.director (
    email VARCHAR(255) PRIMARY KEY,
    adresse VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    tel VARCHAR(255) NOT NULL,
    compte_username VARCHAR(255)
);

-- =========================
-- TABLE parent
-- =========================
CREATE TABLE public.parent (
    email VARCHAR(255) PRIMARY KEY,
    adresse VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    sexe VARCHAR(255) NOT NULL,
    tel1 VARCHAR(255) NOT NULL,
    tel2 VARCHAR(255) NOT NULL,
    compte_username VARCHAR(255)
);

-- =========================
-- TABLE enfant
-- =========================
CREATE TABLE public.enfant (
    id SERIAL PRIMARY KEY,
    datenais VARCHAR(255),
    etatsante VARCHAR(255),
    nom VARCHAR(255),
    prenom VARCHAR(255),
    sexe VARCHAR(255),
    parent_email VARCHAR(255)
);

-- =========================
-- TABLE kinder_garten
-- =========================
CREATE TABLE public.kinder_garten (
    id SERIAL PRIMARY KEY,
    adresse VARCHAR(255),
    email VARCHAR(255),
    nom VARCHAR(255) NOT NULL,
    photos VARCHAR(255),
    tel VARCHAR(255),
    director_id VARCHAR(255) NOT NULL
);

-- =========================
-- TABLE inscription
-- =========================
CREATE TABLE public.inscription (
    id SERIAL PRIMARY KEY,
    anneescolaire VARCHAR(255),
    class_level VARCHAR(255),
    date VARCHAR(255),
    valid BOOLEAN NOT NULL,
    enfant_id INTEGER,
    kindergarten_id INTEGER,
    parent_email VARCHAR(255)
);

-- =========================
-- TABLE payment
-- =========================
CREATE TABLE public.payment (
    id SERIAL PRIMARY KEY,
    date_payment VARCHAR(255),
    montant_du DOUBLE PRECISION,
    montant_percu DOUBLE PRECISION,
    monthnumber INTEGER,
    reference_payment VARCHAR(255),
    type_payment VARCHAR(255),
    inscription_id INTEGER
);

-- =========================
-- TABLE payment_params
-- =========================
CREATE TABLE public.payment_params (
    id SERIAL PRIMARY KEY,
    anneescol VARCHAR(255),
    class_level VARCHAR(255),
    nb_months INTEGER,
    price DOUBLE PRECISION NOT NULL,
    start_month INTEGER,
    director_email VARCHAR(255),
    kindergarten_id INTEGER
);

-- =========================
-- TABLE files
-- =========================
CREATE TABLE public.files (
    id VARCHAR(255) PRIMARY KEY,
    data OID,
    name VARCHAR(255),
    type VARCHAR(255)
);

-- =========================
-- TABLE director_kindergartens
-- =========================
CREATE TABLE public.director_kindergartens (
    director_email VARCHAR(255),
    kindergartens_id INTEGER UNIQUE
);

-- =====================================================
-- FOREIGN KEYS
-- =====================================================

ALTER TABLE public.parent
ADD CONSTRAINT fk_parent_user
FOREIGN KEY (compte_username) REFERENCES public.users(username);

ALTER TABLE public.director
ADD CONSTRAINT fk_director_user
FOREIGN KEY (compte_username) REFERENCES public.users(username);

ALTER TABLE public.enfant
ADD CONSTRAINT fk_enfant_parent
FOREIGN KEY (parent_email) REFERENCES public.parent(email) ON DELETE CASCADE;

ALTER TABLE public.kinder_garten
ADD CONSTRAINT fk_kindergarten_director
FOREIGN KEY (director_id) REFERENCES public.director(email) ON DELETE CASCADE;

ALTER TABLE public.inscription
ADD CONSTRAINT fk_inscription_parent
FOREIGN KEY (parent_email) REFERENCES public.parent(email) ON DELETE CASCADE;

ALTER TABLE public.inscription
ADD CONSTRAINT fk_inscription_enfant
FOREIGN KEY (enfant_id) REFERENCES public.enfant(id) ON DELETE CASCADE;

ALTER TABLE public.inscription
ADD CONSTRAINT fk_inscription_kindergarten
FOREIGN KEY (kindergarten_id) REFERENCES public.kinder_garten(id) ON DELETE CASCADE;

ALTER TABLE public.payment
ADD CONSTRAINT fk_payment_inscription
FOREIGN KEY (inscription_id) REFERENCES public.inscription(id) ON DELETE CASCADE;

ALTER TABLE public.payment_params
ADD CONSTRAINT fk_paymentparams_director
FOREIGN KEY (director_email) REFERENCES public.director(email) ON DELETE CASCADE;

ALTER TABLE public.payment_params
ADD CONSTRAINT fk_paymentparams_kindergarten
FOREIGN KEY (kindergarten_id) REFERENCES public.kinder_garten(id) ON DELETE CASCADE;

ALTER TABLE public.director_kindergartens
ADD CONSTRAINT fk_dk_director
FOREIGN KEY (director_email) REFERENCES public.director(email);

ALTER TABLE public.director_kindergartens
ADD CONSTRAINT fk_dk_kindergarten
FOREIGN KEY (kindergartens_id) REFERENCES public.kinder_garten(id);

-- =====================================================
-- DONNÉES ADMIN
-- =====================================================

INSERT INTO public.users(username, enabled, password, type)
VALUES (
'arbia@gmail.com',
true,
'$2a$10$6U7hYbLZg34srI2IO1MPRuzsLqAzOgVKrPnHssZfvk90NgANZZF2m',
'Admin'
);

INSERT INTO public.authorities(username, authority)
VALUES ('arbia@gmail.com','admin');