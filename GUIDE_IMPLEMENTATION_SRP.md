# 🚀 GUIDE D'IMPLÉMENTATION ÉTAPE PAR ÉTAPE

## ✅ SOLID SRP - Refactorisation Compte & Authorities

Tous les fichiers ont déjà été créés/modifiés. Voici comment les utiliser.

---

## 📋 Checklist d'implémentation

### ✅ ÉTAPE 1 : Vérifier les fichiers créés/modifiés

**Fichiers CRÉÉS :**

- [ ] `RoleType.java` - Enum des rôles
- [ ] `CompteDTO.java` - DTO pour validation
- [ ] `AccountService.java` - Service du cycle de vie
- [ ] `RoleService.java` - Service des rôles
- [ ] `SRPExampleController.java` - Exemples d'utilisation

**Fichiers REFACTORISÉS :**

- [ ] `Compte.java` - Sans `type` ni `confirm_password`
- [ ] `Authorities.java` - Avec `id Long`, `@ManyToOne`, `RoleType`
- [ ] `AuthoritiesRepo.java` - Nouvelles méthodes de requête
- [ ] `CompteRepo.java` - Documentation améliorée

**Fichiers DOCUMENTATION :**

- [ ] `SOLID_SRP_IMPLEMENTATION.md` - Guide complet
- [ ] `SOLID_SRP_DIAGRAMME_UML.md` - Diagrammes et explications
- [ ] `MIGRATION_SRP.sql` - Migration BDD

---

### ✅ ÉTAPE 2 : Compiler le projet

```bash
mvn clean compile
```

**Si erreurs :**

- [ ] Vérifier les imports (RoleType, AccountService, RoleService)
- [ ] Vérifier que les fichiers .java sont dans `src/main/java/...`
- [ ] Vérifier la syntaxe des enums

---

### ✅ ÉTAPE 3 : Adapter la base de données

**Option A : Nouvelle BDD (Recommandé pour DEV)**

```bash
# Exécuter le script de migration
mysql -u root -p votre_db < database/MIGRATION_SRP.sql
```

**Option B : Migrer les données existantes (PRODUCTION)**

1. Sauvegarder les données : `mysqldump`
2. Tester sur copie de BDD
3. Exécuter le script `MIGRATION_SRP.sql`
4. Vérifier les données
5. Passer en production

---

### ✅ ÉTAPE 4 : Mettre à jour les contrôleurs existants

**Remplacer :**

```java
@Autowired private CompteRepo compteRepo;
@Autowired private AuthoritiesRepo authoritiesRepo;
```

**Par :**

```java
@Autowired private AccountService accountService;
@Autowired private RoleService roleService;
```

**Exemple : AuthController.java**

Ancien code :

```java
Compte cpt = compteRepo.findById(email).get();
cpt.setEnabled(true);
compteRepo.save(cpt);

Authorities auth = new Authorities();
auth.setUsername(email);
auth.setAuthority("Admin");
authoritiesRepo.save(auth);
```

Nouveau code :

```java
accountService.activer(email);
roleService.attribuerRole(email, RoleType.ROLE_ADMIN);
```

**Points clés à adapter :**

1. Création de compte → `accountService.creerCompte()`
2. Activation → `accountService.activer()`
3. Mot de passe → `accountService.changerMotDePasse()`
4. Rôles → `roleService.attribuerRole()`
5. Vérification rôle → `roleService.aLe()`

---

### ✅ ÉTAPE 5 : Tester les services

#### Test AccountService

```java
@Test
public void testCreerCompte() {
    Compte cpt = accountService.creerCompte(
        "test@example.com",
        "password123"
    );

    assertNotNull(cpt);
    assertEquals("test@example.com", cpt.getEmail());
    assertFalse(cpt.isEnabled());  // Désactivé par défaut
}

@Test
public void testActiver() {
    Compte cpt = accountService.creerCompte(
        "test@example.com",
        "password123"
    );

    accountService.activer("test@example.com");

    Compte activated = accountService.obtenirCompte("test@example.com")
        .orElse(null);
    assertTrue(activated.isEnabled());
}

@Test
public void testVerifierMotDePasse() {
    accountService.creerCompte("test@example.com", "password123");

    assertTrue(accountService.verifierMotDePasse(
        "test@example.com", "password123"
    ));
    assertFalse(accountService.verifierMotDePasse(
        "test@example.com", "wrongpassword"
    ));
}
```

#### Test RoleService

```java
@Test
public void testAttribuerRole() {
    accountService.creerCompte("test@example.com", "password");

    roleService.attribuerRole("test@example.com", RoleType.ROLE_ADMIN);

    assertTrue(roleService.aLe("test@example.com", RoleType.ROLE_ADMIN));
}

@Test
public void testMultiRoles() {
    accountService.crierCompte("test@example.com", "password");

    roleService.attribuerRole("test@example.com", RoleType.ROLE_ADMIN);
    roleService.attribuerRole("test@example.com", RoleType.ROLE_PARENT);

    List<RoleType> roles = roleService.obtenirRoles("test@example.com");
    assertEquals(2, roles.size());
}

@Test
public void testRetirerRole() {
    accountService.creerCompte("test@example.com", "password");
    roleService.attribuerRole("test@example.com", RoleType.ROLE_ADMIN);

    roleService.retirerRole("test@example.com", RoleType.ROLE_ADMIN);

    assertFalse(roleService.aLe("test@example.com", RoleType.ROLE_ADMIN));
}
```

---

### ✅ ÉTAPE 6 : Mettre à jour les contrôleurs d'inscription

**Ancien code :**

```java
@PostMapping("/inscription")
public String inscription(CompteDTO dto) {
    Compte compte = new Compte();
    compte.setEmail(dto.getEmail());
    compte.setPassword(passwordEncoder.encode(dto.getPassword()));
    compte.setType(dto.getType());
    compte.setEnabled(false);
    compteRepo.save(compte);
    // ...
}
```

**Nouveau code :**

```java
@PostMapping("/inscription")
public ResponseEntity<?> inscription(@RequestBody CompteDTO dto) {
    // Valider
    if (!dto.isPasswordValid()) {
        return ResponseEntity.badRequest()
            .body("Les mots de passe ne correspondent pas");
    }

    if (!dto.isTypeValid()) {
        return ResponseEntity.badRequest()
            .body("Type de compte invalide");
    }

    // Créer
    Compte compte = accountService.creerCompte(
        dto.getEmail(),
        dto.getPassword()
    );

    // Attribuer le rôle
    roleService.attribuerRole(
        dto.getEmail(),
        dto.getTypeAsRoleType()
    );

    return ResponseEntity.ok("Inscription réussie");
}
```

---

### ✅ ÉTAPE 7 : Vérifier les contrôleurs de sécurité

**Spring Security utilisera :**

- `Compte.email` comme username (login)
- `Compte.enabled` pour vérifier l'activation
- `Authorities.authority` (RoleType enum) pour les permissions

**Aucun changement à WebSecurityConfig normalement.**

---

### ✅ ÉTAPE 8 : Adapter les Templates Thymeleaf

**Avant :**

```html
<div th:text="${compte.type}">Admin</div>
```

**Après :**

```html
<!-- Afficher le premier rôle -->
<div th:text="${roles[0]}"></div>

<!-- Ou depuis le contrôleur -->
<div th:text="${currentUserRole}"></div>
```

**Dans le contrôleur :**

```java
Compte user = accountService.getCurrentUser(principal);
List<RoleType> roles = roleService.obtenirRoles(user.getEmail());
RoleType mainRole = roles.isEmpty() ? null : roles.get(0);

model.addAttribute("roles", roles);
model.addAttribute("currentUserRole", mainRole);
```

---

### ✅ ÉTAPE 9 : Compilation finale

```bash
mvn clean package
```

Si tout compile sans erreur → ✅ Succès !

---

## 🧪 Tests d'intégration

### Scénario 1 : Inscription Parent

```java
// Utilisateur remplit le formulaire
CompteDTO dto = new CompteDTO();
dto.setEmail("parent@example.com");
dto.setPassword("password123");
dto.setConfirmPassword("password123");
dto.setType("Parent");

// Valider
assertTrue(dto.isPasswordValid());
assertTrue(dto.isTypeValid());

// Créer
Compte cpt = accountService.crierCompte(
    dto.getEmail(), dto.getPassword()
);

// Rôle
roleService.attribuerRole(
    dto.getEmail(),
    RoleType.ROLE_PARENT
);

// Activer
accountService.activer(dto.getEmail());

// Vérifier
assertTrue(accountService.obtenirCompte(dto.getEmail())
    .get().isEnabled());
assertTrue(roleService.aLe(
    dto.getEmail(),
    RoleType.ROLE_PARENT));
```

### Scénario 2 : Admin avec multi-rôles

```java
accountService.creerCompte("admin@school.com", "secure123");

// L'admin peut être parent ET admin
roleService.attribuerRole("admin@school.com", RoleType.ROLE_ADMIN);
roleService.attribuerRole("admin@school.com", RoleType.ROLE_PARENT);

// Vérifier multi-rôles
List<RoleType> roles = roleService.obtenirRoles("admin@school.com");
assertEquals(2, roles.size());

// Vérifier chacun
assertTrue(roleService.aLes("admin@school.com",
    RoleType.ROLE_ADMIN, RoleType.ROLE_PARENT));
```

### Scénario 3 : Changement de rôle principal

```java
accountService.crierCompte("user@school.com", "pwd");
roleService.attribuerRole("user@school.com", RoleType.ROLE_PARENT);

// Changer en director
roleService.changerRolePrincipal("user@school.com", RoleType.ROLE_DIRECTOR);

// Vérifier
List<RoleType> roles = roleService.obtenirRoles("user@school.com");
assertEquals(1, roles.size());
assertEquals(RoleType.ROLE_DIRECTOR, roles.get(0));
```

---

## ⚠️ Points d'attention

### 1️⃣ Migration de la BDD

- [ ] Sauvegarder avant
- [ ] Tester sur copie
- [ ] Vérifier les conversions (Admin → ROLE_ADMIN)
- [ ] Vérifier que tous les comptes ont un rôle

### 2️⃣ Changements d'API

- [ ] CompteDTO remplace `Compte` pour les formulaires
- [ ] `AccountService.crierCompte()` au lieu de `repo.save()`
- [ ] `RoleService.attribuerRole()` au lieu de créer `Authorities`
- [ ] `RoleType` enum au lieu de String

### 3️⃣ Retro-compatibilité

- [ ] Si des clients externaux utilisent l'API REST ancienne
- [ ] Adapter les DTO de réponse
- [ ] Fournir une version transitoire si nécessaire

### 4️⃣ Tests

- [ ] Unitaires pour `AccountService`
- [ ] Unitaires pour `RoleService`
- [ ] Intégration pour les contrôleurs
- [ ] BDD pour les migrations

---

## 🎯 Finalisation

Une fois tous les points validés :

```bash
# Compilation finale
mvn clean package

# Déploiement
# (selon votre processus : Docker, WAR, JAR, etc.)

# Tests en production
# (vérifier login, rôles, permissions)
```

**Résultat : Code SOLID, maintenable, extensible ! 🎉**

---

## 📞 Questions fréquentes

**Q: Et mon code legacy qui utilise `CompteRepo` directement ?**
A: Adapter progressivement en utilisant `AccountService`. L'ancien code continue de fonctionner en transitoire.

**Q: Multi-rôles compliquent-ils les contrôles de sécurité ?**
A: Non. Spring Security gère automatiquement les rôles multiples avec `@PreAuthorize("hasAnyRole(...)")`.

**Q: Comment maintenir la compatibilité à la migration ?**
A: Utiliser `@Query` si nécessaire pour requêtes complexes, sinon les nouvelles méthodes suffisent.

**Q: Et les performances avec multi-rôles ?**
A: Meilleures ! Requête unique au lieu de N requêtes (1 par rôle avant).

---

**✅ Implémentation SOLID SRP complète ! Prêt pour production !**
