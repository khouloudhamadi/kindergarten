# 🎨 SOLID SRP - Avant / Après VISUEL

## ❌ AVANT (Violations SRP)

```
┌─────────────────────────────────────────┐
│           Architecture AVANT             │
├─────────────────────────────────────────┤
│                                         │
│  CompteController                       │
│  ├─ creerCompte() ──────────┐           │
│  ├─ activerCompte() ────────├──┐        │
│  ├─ changerMotDePasse() ────┤  │        │
│  └─ affecterRole() ─────────┤  │        │
│                             │  │        │
│  DirectorController         │  │        │
│  ├─ creerDirector() ────────┤  │        │
│  ├─ ...                     │  │        │
│                             │  │        │
│  ParentController           │  │        │
│  ├─ creerParent() ──────────┤  │        │
│  ├─ ...                     │  │        │
│                             ▼  ▼        │
│             ┌─────────────────────────┐ │
│             │    Compte (CHAOTIQUE)   │ │
│             ├─────────────────────────┤ │
│             │ - email                 │ │
│             │ - password              │ │
│             │ - type (rôle) ❌        │ │
│             │ - confirm_password ❌   │ │
│             │ - enabled               │ │
│             │                         │ │
│             │ 3 responsabilités 💥   │ │
│             └─────────────────────────┘ │
│                     │                   │
│                     ▼                   │
│         ┌────────────────────────────┐  │
│         │ Authorities (CASSÉE)       │  │
│         ├────────────────────────────┤  │
│         │ - username: String [PK] ❌│  │
│         │   (couplage fort)         │  │
│         │ - authority: String ❌    │  │
│         │   (libre = typo possible) │  │
│         │                           │  │
│         │ ❌ 1 seul rôle par user  │  │
│         │ ❌ Pas de FK vers Compte │  │
│         │ ❌ 3 problèmes           │  │
│         └────────────────────────────┘  │
│                                         │
│         Logique métier dispersée ❌     │
│         Multi-rôles impossible ❌       │
│         Typos non détectées ❌         │
│         Difficile à tester ❌          │
│                                         │
└─────────────────────────────────────────┘
```

**Résultats :**

- 💥 Spaghetti code
- 💥 Responsabilités mélangées
- 💥 Bugs difficiles à diagnostiquer
- 💥 Tests complexes et fragiles

---

## ✅ APRÈS (SRP Appliqué)

```
┌────────────────────────────────────────────────────────────────┐
│                  Architecture APRÈS SRP                        │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌─ CompteController  ─ DirectorController  ─ ParentController │
│  │                                                              │
│  │                                                              │
│  ├────────────────────┬──────────────────────┬───────────────┐ │
│  │                    │                      │               │ │
│  ▼                    ▼                      ▼               ▼ │
│ ┌────────────────────┐                  ┌──────────────────┐  │
│ │ AccountService     │                  │  RoleService     │  │
│ │ ✅ 1 responsabilité │                  │ ✅ 1 responsabilité│
│ ├────────────────────┤                  ├──────────────────┤  │
│ │ + crierCompte()    │                  │ + attribuerRole()  │  │
│ │ + activer()        │                  │ + retirerRole()    │  │
│ │ + desactiver()     │                  │ + obtenirRoles()   │  │
│ │ + changerMdp()     │                  │ + aLe()            │  │
│ │ + getCurrentUser() │                  │ + aAuMoinsUn()     │  │
│ └────┬───────────────┘                  └────┬──────────────┘  │
│      │                                       │                 │
│      ▼                                       ▼                 │
│ ┌──────────────────────────────────────────────────────────┐  │
│ │           CompteRepo                 AuthoritiesRepo      │  │
│ └──────────────────────────────────────────────────────────┘  │
│      │                                       │                 │
│      ▼                                       ▼                 │
│ ┌──────────────────────────────────────────────────────────┐  │
│ │ Compte (Pur & Simple)    Authorities (Bien structuré)   │  │
│ ├──────────────────────────────────────────────────────────┤  │
│ │ - email [PK]             - id: Long [PK] ✅              │  │
│ │ - password               - compte: Compte @ManyToOne ✅  │  │
│ │ - enabled ✅             - authority: RoleType ✅        │  │
│ │                                                           │  │
│ │ ✅ 1 responsabilité      ✅ 1 responsabilité             │  │
│ │    (Identité)               (Lien Compte↔Rôle)         │  │
│ └──────────────────────────────────────────────────────────┘  │
│      │                                       │                 │
│      │            ┌────────────────────────┐ │                 │
│      │            │   RoleType (ENUM)      │ │                 │
│      │            ├────────────────────────┤ │                 │
│      └─► CompteDTO│ + ROLE_ADMIN           │ │                 │
│                   │ + ROLE_PARENT          │ │                 │
│      - email      │ + ROLE_DIRECTOR        │ │                 │
│      - password   │ + fromLegacy()         │◄┘                 │
│      - confirmPwd │                        │                   │
│      - type       │ ✅ Valeurs contraintes│                   │
│                   └────────────────────────┘                   │
│      ✅ Validation UI                                         │
│      ✅ Jamais persisté                                       │
│                                                                │
│      Logique métier CENTRALISÉE ✅                            │
│      Multi-rôles SUPPORTÉ ✅                                  │
│      Type-safe (ENUM) ✅                                      │
│      Facile à tester ✅                                       │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

**Résultats :**

- ✅ Code propre et organisé
- ✅ Responsabilités séparées
- ✅ Bugs faciles à diagnostiquer
- ✅ Tests simples et rapides
- ✅ Multi-rôles nativement supporté

---

## 📊 Comparaison Side-by-Side

### INSCRIPTION

#### ❌ AVANT

```java
// Logique dispersée partout
@PostMapping("/inscription")
public String inscription(CompteDTO dto, Model m) {
    // Validation UI
    if (!dto.getPassword().equals(dto.getConfirm_password())) {
        m.addAttribute("error", "Mots de passe différents");
        return "inscription";
    }

    // Créer compte
    Compte cpt = new Compte();
    cpt.setEmail(dto.getEmail());
    cpt.setPassword(encoder.encode(dto.getPassword()));
    cpt.setType(dto.getType()); // ❌ Mélange rôle et compte
    cpt.setEnabled(false);
    compteRepo.save(cpt);

    // Créer autorité (code dupliqué !)
    Authorities auth = new Authorities();
    auth.setUsername(dto.getEmail());  // ❌ String libre
    auth.setAuthority(dto.getType()); // ❌ String libre = typo ?
    authRepo.save(auth);

    // Envoyer email (si temps...)
    // ...

    return "redirect:login";
}
```

**Problèmes :** 💥 Logique métier en contrôleur, 💥 Dupliquée partout, 💥 String libres

---

#### ✅ APRÈS

```java
@PostMapping("/inscription")
public ResponseEntity<?> inscription(@RequestBody CompteDTO dto) {
    // Validation centralisée
    if (!dto.isPasswordValid()) {
        return ResponseEntity.badRequest()
            .body("Mots de passe différents");
    }

    if (!dto.isTypeValid()) {
        return ResponseEntity.badRequest()
            .body("Type invalide");
    }

    // Créer compte (1 ligne !)
    Compte cpt = accountService.crierCompte(
        dto.getEmail(),
        dto.getPassword()
    );

    // Attribuer rôle (1 ligne !)
    roleService.attribuerRole(
        dto.getEmail(),
        dto.getTypeAsRoleType()  // Type-safe !
    );

    return ResponseEntity.ok("Inscription réussie");
}
```

**Avantages :** ✅ Métier dans services, ✅ Pas de duplication, ✅ Type-safe, ✅ Testable

---

### VÉRIFIER UN RÔLE

#### ❌ AVANT

```java
// Inconsistant et fragile
if (user.getType().equals("Admin")) {  // String libre !
    // Risque typo : "admin" vs "Admin"
    // À chercher dans 10 contrôleurs
}
```

#### ✅ APRÈS

```java
// Centralisé et type-safe
if (roleService.aLe(email, RoleType.ROLE_ADMIN)) {
    // Enum = pas de typo possible
    // Logique en un seul lieu
}
```

---

### CHANGER LE MOT DE PASSE

#### ❌ AVANT

```java
// Manuelle et répétée
Optional<Compte> opt = compteRepo.findById(email);
if (opt.isPresent()) {
    Compte cpt = opt.get();
    cpt.setPassword(encoder.encode(newPassword));
    compteRepo.save(cpt);
}
```

#### ✅ APRÈS

```java
// Centralisée et testée
accountService.changerMotDePasse(email, newPassword);
```

---

## 📈 MÉTRIQUES AVANT/APRÈS

| Métrique                     | Avant     | Après    | Amélioration  |
| ---------------------------- | --------- | -------- | ------------- |
| **Responsabilités / classe** | 3         | 1        | 🔴 -67%       |
| **Rôles max / utilisateur**  | 1         | ∞        | 🟢 +∞         |
| **Code dupliqué**            | 15 lignes | 2 lignes | 🔴 -87%       |
| **Possibilité typo**         | OUI       | NON      | 🟢 Éliminée   |
| **Services métier**          | 0         | 2        | 🟢 +2         |
| **DTO de validation**        | NON       | OUI      | 🟢 +1         |
| **Couplage**                 | Fort      | Faible   | 🟢 Réduit     |
| **Testabilité**              | Difficile | Facile   | 🟢 Simplifiée |
| **Extensibilité**            | Complexe  | Facile   | 🟢 Améliorée  |

---

## 🎯 RÉSULTAT FINAL

```
┌─────────────────────────────────────────────────────────────┐
│                   ✨ CODE SOLIDE ✨                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ✅ SOLID SRP                                              │
│     Chaque classe a UNE SEULE responsabilité               │
│                                                             │
│  ✅ Type-Safe                                              │
│     RoleType enum = pas de typo                           │
│                                                             │
│  ✅ Multi-rôles                                            │
│     N rôles par utilisateur (avant = 1 max)               │
│                                                             │
│  ✅ Testable                                               │
│     Services isolés = tests simples                        │
│                                                             │
│  ✅ Maintenable                                            │
│     Logique métier centralisée                             │
│                                                             │
│  ✅ Extensible                                             │
│     Ajouter ROLE_TEACHER = +1 ligne enum                 │
│                                                             │
│  ✅ Scalable                                               │
│     Architecture prête pour production                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘

   De legacy Code vers Enterprise Architecture

   Code CHAOTIQUE ──────────────► Code PROPRE
```

---

## 🚀 Démarrer maintenant

```bash
# 1. Compiler
mvn clean compile

# 2. Migrer la BDD
mysql < database/MIGRATION_SRP.sql

# 3. Adapter les contrôleurs
# Utiliser AccountService et RoleService

# 4. Tester
mvn test

# 5. Déployer
mvn clean package
```

**Vous êtes maintenant un maitre du SOLID SRP ! 🎉**
