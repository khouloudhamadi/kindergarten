# Guide d'Utilisation - Pattern GRASP Controller

## 🎯 Utilisation Rapide

### Pour les Développeurs

#### 1️⃣ Créer un nouveau compte

```java
// Dans AuthController ou votre contrôleur
@Autowired
private AuthService authService;

// Créer le compte
Compte compte = authService.creerCompte(
    email,
    password,
    "Parent"  // Type : Admin, Kindergarten Director, Parent
);
```

#### 2️⃣ Vérifier l'utilisateur connecté

```java
@GetMapping("/my-page")
public String myPage(Principal principal) {
    // Récupérer l'utilisateur courant
    Compte currentUser = authService.getCurrentUser(principal);

    if (currentUser == null) {
        return "redirect:/login";  // Pas connecté
    }

    return "my-page";
}
```

#### 3️⃣ Vérifier les droits d'accès

```java
@GetMapping("/admin/dashboard")
public String adminDashboard(Principal principal) {
    Compte currentUser = authService.getCurrentUser(principal);

    // Vérifier que c'est un Admin
    if (!authService.isAdmin(currentUser)) {
        return "/error/accessDenied";
    }

    return "admin/dashboard";
}
```

#### 4️⃣ Activer/Désactiver un compte

```java
@GetMapping("/activate/{email}")
public String activate(
        @PathVariable String email,
        Principal principal) {

    Compte currentUser = authService.getCurrentUser(principal);
    if (!authService.isAdmin(currentUser)) {
        return "/error/accessDenied";
    }

    authService.activerCompte(email);
    return "redirect:/comptes";
}
```

#### 5️⃣ Changer le mot de passe

```java
@PostMapping("/change-password")
public String changePassword(
        Principal principal,
        @RequestParam String newPassword) {

    Compte currentUser = authService.getCurrentUser(principal);
    if (currentUser == null) {
        return "/error/accessDenied";
    }

    authService.changerMotDePasse(
        currentUser.getEmail(),
        newPassword
    );

    return "redirect:/profile?success=Password changed";
}
```

---

## 📋 Endpoints Disponibles

### AuthController (Nouveaux)

| Endpoint                   | Méthode | Accès    | Description           |
| -------------------------- | ------- | -------- | --------------------- |
| `/auth/director/register`  | POST    | Public   | Inscription directeur |
| `/auth/parent/register`    | POST    | Public   | Inscription parent    |
| `/auth/activate/{email}`   | GET     | Admin    | Activer un compte     |
| `/auth/deactivate/{email}` | GET     | Admin    | Désactiver un compte  |
| `/auth/change-password`    | POST    | Connecté | Changer mot de passe  |

### CompteController (Refactorisé)

| Endpoint                     | Avant                  | Après              |
| ---------------------------- | ---------------------- | ------------------ |
| `/compte/activer/{email}`    | ❌ Pas de vérification | ✅ Admin seulement |
| `/compte/desactiver/{email}` | ❌ Pas de vérification | ✅ Admin seulement |

---

## 🔍 Vérification des Types de Compte

```java
// Vérifier Admin
if (authService.isAdmin(compte)) { /* ... */ }

// Vérifier Directeur
if (authService.isDirector(compte)) { /* ... */ }

// Vérifier Parent
if (authService.isParent(compte)) { /* ... */ }

// Vérifier un rôle personnalisé
if (authService.hasRole(compte, "Custom Role")) { /* ... */ }
```

---

## 🧪 Tests

### Exécuter les tests AuthService

```bash
# Tous les tests AuthService
mvn test -Dtest=AuthServiceTest

# Test spécifique
mvn test -Dtest=AuthServiceTest#testCreerCompte

# Avec couverture
mvn clean test jacoco:report
```

### Exemple de test

```java
@Test
void testCreerCompte() {
    // Arrange
    when(passwordEncoder.encode("password"))
        .thenReturn("hashed");
    when(compteRepo.save(any()))
        .thenReturn(compte);

    // Act
    Compte result = authService.creerCompte(
        "user@test.com",
        "password",
        "Parent"
    );

    // Assert
    assertEquals("Parent", result.getType());
    assertFalse(result.isEnabled());
}
```

---

## ⚡ Exemples Complets

### Exemple 1 : Endpoint d'inscription

```java
@PostMapping("/register/parent")
public String registerParent(ParentInfo pinfo) {
    // 1. Créer le compte via AuthService
    Compte compte = authService.creerCompte(
        pinfo.getEmail(),
        pinfo.getPassword(),
        "Parent"
    );

    // 2. Créer l'entité Parent
    Parent parent = new Parent();
    parent.setCompte(compte);
    parent.setEmail(pinfo.getEmail());
    parent.setNom(pinfo.getNom());
    parent.setPrenom(pinfo.getPrenom());
    parentRepo.save(parent);

    // 3. Retourner au formulaire
    return "redirect:/?msg=Inscription successful";
}
```

### Exemple 2 : Dashboard admin

```java
@GetMapping("/admin/dashboard")
public String adminDashboard(Principal principal, Model model) {
    // 1. Récupérer l'utilisateur courant
    Compte currentUser = authService.getCurrentUser(principal);

    // 2. Vérifier les droits
    if (!authService.isAdmin(currentUser)) {
        return "redirect:/error/accessDenied";
    }

    // 3. Charger les données
    List<Compte> allComptes = compteService.getAllComptes();
    model.addAttribute("comptes", allComptes);
    model.addAttribute("currentUser", currentUser);

    // 4. Retourner la vue
    return "admin/dashboard";
}
```

### Exemple 3 : Gestion de profil

```java
@GetMapping("/profile")
public String getProfile(Principal principal, Model model) {
    Compte currentUser = authService.getCurrentUser(principal);
    if (currentUser == null) {
        return "redirect:/login";
    }

    model.addAttribute("user", currentUser);

    // Charger les données supplémentaires selon le type
    if (authService.isAdmin(currentUser)) {
        // Données admin
    } else if (authService.isDirector(currentUser)) {
        Director director = directorRepo.findById(currentUser.getEmail()).orElse(null);
        model.addAttribute("director", director);
    } else if (authService.isParent(currentUser)) {
        Parent parent = parentRepo.findById(currentUser.getEmail()).orElse(null);
        model.addAttribute("parent", parent);
    }

    return "profile";
}
```

---

## 🛠️ Dépannage

### Problème 1 : "Compte introuvable" lors de l'activation

```
❌ IllegalArgumentException: Compte introuvable : user@test.com
```

**Solution** :

- Vérifier que l'email existe dans la base de données
- Vérifier l'orthographe de l'email
- Vérifier que le compte a été créé avec le même email

```java
// Vérifier avant d'activer
Optional<Compte> compte = authService.getCompteByEmail("user@test.com");
if (!compte.isPresent()) {
    return "redirect:/?error=Compte non trouvé";
}
```

### Problème 2 : Accès refusé à un endpoint

```
❌ Redirection vers /error/accessDenied
```

**Solution** :

- Vérifier que l'utilisateur est connecté : `authService.getCurrentUser(principal)` ne doit pas être null
- Vérifier le type de compte requis (Admin, Director, Parent)
- Vérifier que le compte est activé : `compte.isEnabled()` doit être true

```java
// Debug : Ajouter les logs
Compte currentUser = authService.getCurrentUser(principal);
System.out.println("User: " + (currentUser != null ? currentUser.getEmail() : "null"));
System.out.println("Type: " + (currentUser != null ? currentUser.getType() : "null"));
System.out.println("Enabled: " + (currentUser != null ? currentUser.isEnabled() : "n/a"));
```

### Problème 3 : Mot de passe incorrecte après création

**Cause** : Le mot de passe n'a pas été chiffré

**Solution** : Utiliser AuthService.creerCompte() qui encode automatiquement

```java
// ✅ BON : Mot de passe encodé automatiquement
authService.creerCompte(email, "password123", "Parent");

// ❌ MAUVAIS : Mot de passe non encodé
new Compte().setPassword("password123");
```

---

## 📚 Ressources

### Documentation

- [GRASP_CONTROLLER_DOCUMENTATION.md](./src/main/java/com/kindergarten/kindergarten/compte/GRASP_CONTROLLER_DOCUMENTATION.md) - Architecture détaillée
- [GRASP_IMPLEMENTATION_SUMMARY.md](./GRASP_IMPLEMENTATION_SUMMARY.md) - Résumé des changements

### Code Source

- [AuthService.java](./src/main/java/com/kindergarten/kindergarten/compte/AuthService.java) - Service métier
- [AuthController.java](./src/main/java/com/kindergarten/kindergarten/compte/AuthController.java) - GRASP Controller
- [AuthServiceTest.java](./src/test/java/com/kindergarten/kindergarten/compte/AuthServiceTest.java) - Tests

---

## ✨ Bonnes Pratiques

### ✅ À FAIRE

```java
// 1. Toujours vérifier les droits
Compte user = authService.getCurrentUser(principal);
if (!authService.isAdmin(user)) return "/error/accessDenied";

// 2. Déléguer à AuthService
authService.activerCompte(email);

// 3. Gérer les exceptions
try {
    authService.changerMotDePasse(email, newPassword);
} catch (IllegalArgumentException ex) {
    return "redirect:/?error=" + ex.getMessage();
}

// 4. Utiliser les méthodes de vérification
if (authService.isAdmin(user)) { /* ... */ }
```

### ❌ À NE PAS FAIRE

```java
// 1. Accéder directement aux repos
compteRepo.findById(email).get().setEnabled(true);

// 2. Créer des encodeurs dans le contrôleur
new BCryptPasswordEncoder().encode(password);

// 3. Oublier les vérifications de droits
authService.activerCompte(email);  // Sans vérification Admin

// 4. Duplicer la logique métier
// Voir AuthService et l'utiliser à la place
```

---

## 🎓 Cas d'Utilisation Courants

### Cas 1 : Afficher tous les utilisateurs (Admin)

```java
@GetMapping("/users")
public String listUsers(Principal principal, Model model) {
    Compte currentUser = authService.getCurrentUser(principal);

    // Vérifier Admin
    if (!authService.isAdmin(currentUser)) {
        return "/error/accessDenied";
    }

    // Afficher tous les utilisateurs
    List<Compte> users = compteService.getAllComptes();
    model.addAttribute("users", users);

    return "users/list";
}
```

### Cas 2 : Afficher le profil personnel

```java
@GetMapping("/my-profile")
public String myProfile(Principal principal, Model model) {
    Compte currentUser = authService.getCurrentUser(principal);

    if (currentUser == null) {
        return "redirect:/login";
    }

    model.addAttribute("user", currentUser);
    return "profile/view";
}
```

### Cas 3 : Donner les permissions à un directeur (Admin)

```java
@PostMapping("/director/{email}/permissions")
public String setPermissions(
        @PathVariable String email,
        @RequestParam String permissions,
        Principal principal) {

    Compte currentUser = authService.getCurrentUser(principal);
    if (!authService.isAdmin(currentUser)) {
        return "/error/accessDenied";
    }

    // Donner les permissions
    Director director = directorRepo.findById(email).orElse(null);
    if (director != null) {
        director.setPermissions(permissions);
        directorRepo.save(director);
    }

    return "redirect:/directors";
}
```

---

## 📊 Flux d'Authentification Résumé

```
┌─────────────────────────┐
│ 1. Principal HTTP       │
│ (Session Spring)        │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ 2. getCurrentUser()     │
│ (AuthService)           │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│ 3. Vérifier droits      │
│ (isAdmin, isParent...)  │
└────────────┬────────────┘
             │
             ├─ Oui ──▶ 4. Exécuter l'action
             │
             └─ Non ──▶ Retourner /error/accessDenied
```

---

## 🚀 Déploiement

### Avant de mettre en production

1. ✅ Exécuter les tests

   ```bash
   mvn clean test
   ```

2. ✅ Vérifier la couverture

   ```bash
   mvn jacoco:report
   ```

3. ✅ Tester les endpoints
   - POST `/auth/parent/register` - Inscription
   - GET `/auth/activate/test@test.com` - Activation (admin)
   - POST `/auth/change-password` - Changement mot de passe

4. ✅ Vérifier les accès refusés
   - GET `/auth/deactivate/test@test.com` en tant que Parent → /error/accessDenied

---

## 📞 Aide Rapide

**Q** : Comment créer un nouveau compte ?  
**R** : `authService.creerCompte(email, password, type)`

**Q** : Comment vérifier l'utilisateur connecté ?  
**R** : `authService.getCurrentUser(principal)`

**Q** : Comment vérifier si Admin ?  
**R** : `authService.isAdmin(compte)`

**Q** : Comment activer un compte ?  
**R** : `authService.activerCompte(email)` (après vérification Admin)

**Q** : Comment changer le mot de passe ?  
**R** : `authService.changerMotDePasse(email, newPassword)`

---

**Dernière mise à jour** : [Aujourd'hui]
**Version** : 1.0
**Status** : ✅ Production Ready
