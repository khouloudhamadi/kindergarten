# SOLID SRP - Diagramme UML

## 📊 Avant SRP (Violations)

```
┌─────────────────────────────────────────────────────┐
│                    Compte (AVANT)                    │
├─────────────────────────────────────────────────────┤
│ - email : String [PK]                               │
│ - password : String  ← Responsabilité 1 : Identité  │
│ - type : String      ← Responsabilité 3 : Rôle      │
│ - confirm_password   ← Responsabilité UI            │
│ - enabled : boolean  ← Responsabilité 2 : Activation│
└─────────────────────────────────────────────────────┘
            ❌ 3 responsabilités mélangées

┌─────────────────────────────────────────────────────┐
│                 Authorities (AVANT)                  │
├─────────────────────────────────────────────────────┤
│ - username : String [PK] ← String libre = couplage  │
│ - authority : String     ← String libre = risque    │
│                                                     │
│ ❌ Problème 1 : username comme @Id = 1 rôle max    │
│ ❌ Problème 2 : authority String = typo possible   │
│ ❌ Problème 3 : Pas de lien JPA vers Compte        │
└─────────────────────────────────────────────────────┘
```

---

## 📊 Après SRP (Solution)

```
┌──────────────────────────────────────────────────────┐
│               Compte (APRÈS SRP)                     │
├──────────────────────────────────────────────────────┤
│ @Entity                                              │
│ @Table(name="users")                                 │
├──────────────────────────────────────────────────────┤
│ - email : String [PK]                                │
│ - password : String                                  │
│ - enabled : boolean                                  │
├──────────────────────────────────────────────────────┤
│ + getEmail() : String                                │
│ + getPassword() : String                             │
│ + isEnabled() : boolean                              │
│ + setEmail(String) : void                            │
│ + setPassword(String) : void                         │
│ + setEnabled(boolean) : void                         │
└──────────────────────────────────────────────────────┘
    ✓ 1 SEULE responsabilité : Identité + Activation


┌──────────────────────────────────────────────────────┐
│             RoleType (ENUM NEW!)                     │
├──────────────────────────────────────────────────────┤
│ + ROLE_ADMIN                                         │
│ + ROLE_PARENT                                        │
│ + ROLE_DIRECTOR                                      │
├──────────────────────────────────────────────────────┤
│ + fromLegacy(String) : RoleType                      │
│ + toString() : String                                │
└──────────────────────────────────────────────────────┘
    ✓ Valeurs contraintes (pas de typo)
    ✓ Compatible Spring Security
    ✓ Facile d'ajouter un rôle


┌──────────────────────────────────────────────────────┐
│            Authorities (APRÈS SRP)                   │
├──────────────────────────────────────────────────────┤
│ @Entity                                              │
│ @Table(name="authorities")                           │
├──────────────────────────────────────────────────────┤
│ - id : Long [PK AUTO]                    ← NOUVEAU! │
│ - compte : Compte [FK] @ManyToOne        ← NOUVEAU! │
│ - authority : RoleType @Enumerated       ← NOUVEAU! │
├──────────────────────────────────────────────────────┤
│ + getId() : Long                                     │
│ + getCompte() : Compte                               │
│ + getAuthority() : RoleType                          │
│ + getUsername() : String                 ← Utile!   │
└──────────────────────────────────────────────────────┘
    ✓ id auto = multi-rôles possibles
    ✓ @ManyToOne = lien JPA explicite
    ✓ RoleType enum = pas de typo


┌──────────────────────────────────────────────────────┐
│               CompteDTO (NEW!)                       │
├──────────────────────────────────────────────────────┤
│ - email : String                                     │
│ - password : String                                  │
│ - confirmPassword : String               ← Ici!     │
│ - type : String                                      │
├──────────────────────────────────────────────────────┤
│ + isPasswordValid() : boolean                        │
│ + isTypeValid() : boolean                            │
│ + getTypeAsRoleType() : RoleType                     │
└──────────────────────────────────────────────────────┘
    ✓ Jamais persisté
    ✓ Valide les données du formulaire
    ✓ Séparation UI / Métier


┌──────────────────────────────────────────────────────┐
│           AccountService (NEW!)                      │
├──────────────────────────────────────────────────────┤
│ @Service (Singleton)                                 │
├──────────────────────────────────────────────────────┤
│ - compteRepo : CompteRepo                            │
│ - passwordEncoder : BCryptPasswordEncoder            │
├──────────────────────────────────────────────────────┤
│ + creerCompte(email, password) : Compte              │
│ + activer(email) : void                              │
│ + desactiver(email) : void                           │
│ + changerMotDePasse(email, mdp) : void               │
│ + verifierMotDePasse(email, mdp) : boolean           │
│ + getCurrentUser(principal) : Compte                 │
│ + obtenirCompte(email) : Optional<Compte>            │
│ + exists(email) : boolean                            │
│ + supprimer(email) : void                            │
└──────────────────────────────────────────────────────┘
    ✓ 1 SEULE responsabilité : Cycle de vie du compte
    ✗ NE touche PAS aux rôles (→ RoleService)


┌──────────────────────────────────────────────────────┐
│            RoleService (NEW!)                        │
├──────────────────────────────────────────────────────┤
│ @Service (Singleton)                                 │
├──────────────────────────────────────────────────────┤
│ - authoritiesRepo : AuthoritiesRepo                  │
│ - compteRepo : CompteRepo                            │
├──────────────────────────────────────────────────────┤
│ + attribuerRole(email, role) : void                  │
│ + retirerRole(email, role) : void                    │
│ + retirerTousLesRoles(email) : void                  │
│ + obtenirRoles(email) : List<RoleType>               │
│ + aLe(email, role) : boolean                         │
│ + aLes(email, roles...) : boolean                    │
│ + aAuMoinsUn(email, roles...) : boolean              │
│ + changerRolePrincipal(email, newRole) : void        │
│ + obtenirRolePrincipal(email) : RoleType             │
└──────────────────────────────────────────────────────┘
    ✓ 1 SEULE responsabilité : Gestion des rôles
    ✗ NE touche PAS au mot de passe (→ AccountService)
```

---

## 🔗 Relations UML (Après SRP)

```
                        ┌─────────────┐
                        │ RoleType    │
                        │  (ENUM)     │
                        └──────┬──────┘
                               │
                               │ uses
                               │
                        ┌──────▼──────┐
       ┌───────────────►│ Authorities │◄───────────────┐
       │                │   (Entity)  │                │
       │ <<FK>>         └──────┬──────┘                │
       │ ManyToOne             │                       │
       │                       │ 1 : N                 │ <<FK>>
       │                       │                       │ (id auto)
    ┌──┴─────────────┐         │         ┌─────────────┴──┐
    │    Compte      │         │         │  CompteDTO     │
    │   (Entity)     │         │         │   (Simple)     │
    │                │         │         │                │
    │ - email [PK]   │         │         │ - email        │
    │ - password     │         │         │ - password     │
    │ - enabled      │         │         │ - confirmPwd   │
    └────────────────┘         │         │ - type         │
           ▲                    │         └────────────────┘
           │                    │
           │                    └─► Used to validate
           │                         form data
           │
      Has 0..N
       roles


┌─────────────────────────────────────────────────────────────────┐
│                         Services                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  AccountService ◄──────────┐                                    │
│  (Cycle de vie)            │                                    │
│  - creerCompte()           │ Uses                               │
│  - activer()               │                                    │
│  - desactiver()            │                                    │
│  - changerMotDePasse()     │                                    │
│                            │                                    │
│  RoleService ◄─────────────┤                                    │
│  (Gestion rôles)           │ Uses                               │
│  - attribuerRole()         │                                    │
│  - retirerRole()           │                                    │
│  - obtenirRoles()          │                                    │
│  - aLe()                   │                                    │
│                            │                                    │
│                    Repositories                                 │
│                    (Data Access)                                │
│                            │                                    │
│                            ├─► CompteRepo                       │
│                            │   - CRUD<Compte, String>          │
│                            │                                    │
│                            └─► AuthoritiesRepo                  │
│                                - CRUD<Authorities, Long>        │
│                                - findByCompteEmail()            │
│                                - existsByCompteEmailAndAuth()   │
│                                - deleteByCompteEmailAndAuth()   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Responsabilités - Une par classe

```
Class               │ Raison de changer              │ Responsabilité unique
────────────────────┼────────────────────────────────┼──────────────────────
Compte              │ Logique d'identité évolue      │ Identité + Activation
Authorities         │ Politique de rôles évolue      │ Lien Compte ↔ Rôle
RoleType            │ Nouveaux rôles à ajouter       │ Énumérer les rôles
CompteDTO           │ Formulaire d'inscription chg.  │ Transport données UI
AccountService      │ Gestion des comptes évolue     │ Cycle de vie du compte
RoleService         │ Gestion des rôles évolue       │ Gestion des rôles
CompteRepo          │ Changement technologie SGBD    │ Accès données Compte
AuthoritiesRepo     │ Changement technologie SGBD    │ Accès données Authorities
```

---

## 📈 Impact sur la maintenabilité

### ✓ Avant SRP (Couplage fort)

```
Modifier la gestion des rôles
    ↓
Risque de casser le mot de passe
    ↓
Risque de casser l'activation
    ↓
Tests à refaire complètement
```

### ✓ Après SRP (Isolation)

```
Modifier la gestion des rôles (RoleService)
    ↓
RoleService testé isolément
    ↓
AccountService inaffecté
    ↓
Compte inaffecté
    ↓
Tests seuls les changements affectés
```

---

## 🚀 Exemple : Ajouter un rôle ROLE_TEACHER

### ❌ AVANT SRP (Avant-goût du chaos)

```
1. Modifier RoleType enum
2. Modifier Compte (type String possible)
3. Modifier Authorities (authority String)
4. Modifier tous les contrôleurs
5. Modifier les templates
6. Modifier les permissions Spring Security
7. Tester partout...
8. Risque de régression
```

### ✅ APRÈS SRP (Trivial)

```java
// RoleType.java (SEUL changement)
public enum RoleType {
    ROLE_ADMIN("Admin"),
    ROLE_PARENT("Parent"),
    ROLE_DIRECTOR("Kindergarten Director"),
    ROLE_TEACHER("Teacher");  // ← C'est tout !
}
// Rien d'autre à modifier
// RoleService.attribuerRole(email, RoleType.ROLE_TEACHER);
// C'est fini !
```

---

## 📝 Checklist SRP

- ✅ **Compte** : Contient UNIQUEMENT email, password, enabled
- ✅ **Authorities** : Contient UNIQUEMENT id (Long), compte (FK), authority (RoleType)
- ✅ **RoleType** : Enum pour les valeurs contraintes
- ✅ **CompteDTO** : Porte confirmPassword et type (jamais persistés)
- ✅ **AccountService** : Gère UNIQUEMENT le cycle de vie du compte
- ✅ **RoleService** : Gère UNIQUEMENT les rôles
- ✅ **AuthoritiesRepo** : Méthodes de recherche spécialisées pour les autorités
- ✅ **CompteRepo** : Interface minimaliste pour CRUD basique

**Résultat : Code maintenable, testable, extensible ! 🎉**
