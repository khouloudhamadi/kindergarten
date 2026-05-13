# SOLID SRP - Séparation Compte & Authorities

## ✅ IMPLÉMENTATION COMPLÈTE

Toutes les classes ont été refactorisées selon le principe SRP (Single Responsibility Principle).

---

## 📋 Fichiers créés/modifiés

### ✓ CRÉÉS

1. **RoleType.java** - Enum pour les rôles (valeurs contraintes)
2. **CompteDTO.java** - DTO pour la validation UI
3. **AccountService.java** - Service pour le cycle de vie du compte
4. **RoleService.java** - Service pour la gestion des rôles

### ✓ REFACTORISÉS

1. **Compte.java** - Retrait de `type` et `confirm_password`
2. **Authorities.java** - Ajout de `id` Long, `@ManyToOne`, `RoleType` enum
3. **AuthoritiesRepo.java** - Nouvelles méthodes pour les requêtes
4. **CompteRepo.java** - Documentation améliorée

---

## 🎯 Responsabilités après SRP

### Compte

- **Seule responsabilité** : identité et état d'activation
- **Contient** : email, password, enabled
- **Ne contient PAS** : type (→ Authorities), confirm_password (→ CompteDTO)

### Authorities

- **Seule responsabilité** : lien Compte ↔ Rôle
- **Contient** : id Long, Compte (FK), RoleType (enum)
- **Avantages** :
  - Multi-rôles possible (id technique)
  - Enum pour prévenir les typos
  - Lien JPA explicite

### CompteDTO

- **Seule responsabilité** : transporter données du formulaire
- **Contient** : email, password, confirmPassword, type
- **Jamais persisté** en base

### AccountService

- **Seule responsabilité** : cycle de vie du compte
- **Gère** : créer, activer, désactiver, changer mot de passe
- **NE gère PAS** : les rôles (→ RoleService)

### RoleService

- **Seule responsabilité** : gestion des rôles
- **Gère** : attribuer, retirer, lister, vérifier les rôles
- **NE gère PAS** : mot de passe, activation (→ AccountService)

### RoleType

- **Seule responsabilité** : énumérer les rôles valides
- **Valeurs** : ROLE_ADMIN, ROLE_PARENT, ROLE_DIRECTOR
- **Méthode utile** : `fromLegacy()` pour la migration

---

## 📚 GUIDE D'UTILISATION

### 1️⃣ Créer un compte + attribuer un rôle

```java
@Autowired private AccountService accountService;
@Autowired private RoleService roleService;

// Créer le compte (enabled=false)
Compte compte = accountService.crierCompte(
    "email@example.com",
    "motDePasse123"
);

// Attribuer le rôle
roleService.attribuerRole(
    "email@example.com",
    RoleType.ROLE_PARENT
);

// Activer le compte
accountService.activer("email@example.com");
```

### 2️⃣ Utiliser le DTO pour l'inscription

```java
@PostMapping("/inscription")
public ResponseEntity<?> inscription(@RequestBody CompteDTO dto) {
    // Valider les mots de passe
    if (!dto.isPasswordValid()) {
        return ResponseEntity.badRequest()
            .body("Les mots de passe ne correspondent pas");
    }

    // Valider le type de compte
    if (!dto.isTypeValid()) {
        return ResponseEntity.badRequest()
            .body("Type de compte invalide");
    }

    // Créer le compte (sans type, sans confirmPassword)
    Compte compte = accountService.creerCompte(
        dto.getEmail(),
        dto.getPassword()
    );

    // Créer l'autorité avec le type converti
    roleService.attribuerRole(
        dto.getEmail(),
        dto.getTypeAsRoleType()
    );

    return ResponseEntity.ok("Inscription réussie");
}
```

### 3️⃣ Vérifier les rôles

```java
// Vérifier UN rôle
if (roleService.aLe("user@example.com", RoleType.ROLE_ADMIN)) {
    // L'utilisateur est admin
}

// Vérifier PLUSIEURS rôles (tous)
if (roleService.aLes("user@example.com",
    RoleType.ROLE_ADMIN, RoleType.ROLE_DIRECTOR)) {
    // L'utilisateur a TOUS ces rôles
}

// Vérifier AU MOINS UN rôle
if (roleService.aAuMoinsUn("user@example.com",
    RoleType.ROLE_ADMIN, RoleType.ROLE_DIRECTOR)) {
    // L'utilisateur a AU MOINS l'un de ces rôles
}

// Lister tous les rôles
List<RoleType> roles = roleService.obtenirRoles("user@example.com");
```

### 4️⃣ Gérer le mot de passe

```java
// Changer le mot de passe
accountService.changerMotDePasse(
    "user@example.com",
    "nouveauMotDePasse123"
);

// Vérifier un mot de passe
if (accountService.verifierMotDePasse(
    "user@example.com",
    "motDePasse123")) {
    // Correct !
}
```

### 5️⃣ Activer / Désactiver

```java
// Activer un compte
accountService.activer("user@example.com");

// Désactiver un compte
accountService.desactiver("user@example.com");
```

### 6️⃣ Changer le rôle principal

```java
// Retire tous les rôles et en attribue un nouveau
roleService.changerRolePrincipal(
    "user@example.com",
    RoleType.ROLE_ADMIN
);
```

---

## 🔄 Migration des données existantes

Si vous avez des données existantes avec la structure ancienne :

```sql
-- Les anciens types String
SELECT * FROM users WHERE type = 'Admin';

-- Utiliser RoleType.fromLegacy() pour convertir
RoleType role = RoleType.fromLegacy("Admin"); // → ROLE_ADMIN
```

---

## 📊 Avant vs Après SRP

### ❌ AVANT (Violations SRP)

```java
// Compte mélange 3 responsabilités
Compte cpt = repo.findById("user@email.com").get();
cpt.setEmail("newemail@gmail.com");      // Responsabilité 1 : identité
cpt.setPassword(encoder.encode("mdp"));   // Responsabilité 1 : password
cpt.setEnabled(true);                     // Responsabilité 2 : activation
cpt.setType("Admin");                     // Responsabilité 3 : rôle
repo.save(cpt);

// Authorities a 3 problèmes
Authorities auth = new Authorities();
auth.setUsername(email);      // String libre = couplage fort
auth.setAuthority("Admin");   // String libre = risque de typo
// Pas de lien JPA vers Compte !
authRepo.save(auth);
```

### ✅ APRÈS (SRP appliqué)

```java
// Compte : SEULE identité + activation
accountService.changerMotDePasse("user@email.com", "mdp");
accountService.activer("user@email.com");

// Authorities : SEULE gestion des rôles
roleService.attribuerRole("user@email.com", RoleType.ROLE_ADMIN);

// Clair, séparé, maintenable !
```

---

## 🎁 Bénéfices du SRP

| Aspect                            | Avant              | Après                |
| --------------------------------- | ------------------ | -------------------- |
| **Responsabilités par classe**    | 3                  | 1                    |
| **Rôles max par utilisateur**     | 1                  | N                    |
| **Typo "admin" vs "Admin"**       | ❌ Possible        | ✅ Impossible (enum) |
| **Lien JPA**                      | ❌ String          | ✅ @ManyToOne        |
| **Ajouter un rôle**               | Refactoring majeur | Ajouter valeur enum  |
| **Changer la politique password** | Chercher partout   | 1 classe modifiée    |

---

## 🚀 Ajouter un nouveau rôle

Grâce au SRP, ajouter un nouveau rôle est maintenant trivial :

```java
// RoleType.java
public enum RoleType {
    ROLE_ADMIN("Admin"),
    ROLE_PARENT("Parent"),
    ROLE_DIRECTOR("Kindergarten Director"),
    ROLE_TEACHER("Teacher");  // ← NOUVEAU
    // C'est tout ! Rien d'autre ne change
}
```

---

## ⚠️ Points importants

1. **Authorities.id est maintenant Long** (avant String username)
   - Migration SQL nécessaire pour les données existantes
   - Multi-rôles automatiquement supporté

2. **Compte.type n'existe plus**
   - Migration SQL nécessaire pour les données existantes
   - RoleType.fromLegacy() aide à la conversion

3. **CompteDTO n'est jamais persisté**
   - Utilisez-le UNIQUEMENT pour les formulaires
   - Convertissez en Compte après validation

4. **AccountService et RoleService sont @Service** (Singleton)
   - Une seule instance pour toute l'appli
   - Autowiredable partout

---

## 📝 Notes pour les contrôleurs

### Ancien code (à remplacer)

```java
@Autowired private CompteRepo cptRepo;
@Autowired private AuthoritiesRepo authRepo;

// Logique métier dispersée dans les contrôleurs
```

### Nouveau code (utiliser)

```java
@Autowired private AccountService accountService;
@Autowired private RoleService roleService;

// Logique métier concentrée dans les services
```

---

## 🎓 Résumé SOLID SRP

**"Chaque classe doit avoir une SEULE raison de changer"**

- **Compte** change si : logique d'identité évolue
- **Authorities** change si : politique de rôles évolue
- **AccountService** change si : gestion des comptes évolue
- **RoleService** change si : gestion des rôles évolue

Ceci garantit : **maintenance facile, tests isolés, évolution rapide** 🚀
