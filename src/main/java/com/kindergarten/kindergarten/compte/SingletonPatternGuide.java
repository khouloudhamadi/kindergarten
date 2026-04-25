package com.kindergarten.kindergarten.compte;

/**
 * GUIDE COMPLET : Pattern Singleton GoF - Gestion des Comptes
 *
 * ============================================================================
 * ✅ STATUT: CompteController REFACTORISÉ avec CompteService Singleton
 * ============================================================================
 *
 * Ce document explique comment utiliser le Singleton CompteService partout dans
 * l'application pour centraliser la logique métier.
 *
 * ============================================================================
 * 1️⃣ COMMENT FONCTIONNE LE SINGLETON?
 * ============================================================================
 *
 * @Service public class CompteService { ... }
 *
 * ┌─────────────────────────────────────┐ │ Démarrage de Spring │
 * └─────────────────────────────────────┘ ↓
 * ┌─────────────────────────────────────┐ │ Spring crée UNE SEULE INSTANCE │ │
 * CompteService service = new ...() │ └─────────────────────────────────────┘ ↓
 * ┌─────────────────────────────────────────────────────────────┐ │ Injection
 * dans TOUS les contrôleurs │ │ @Autowired private CompteService compteService;
 * │ │ │ │ CompteController ──┐ │ │ DirectorController ──┼──→ MÊME INSTANCE │ │
 * ParentController ──┤ │ │ InscriptionController┘ │
 * └─────────────────────────────────────────────────────────────┘
 *
 * ✅ AVANTAGES: - UNE SEULE INSTANCE pour toute l'application - Thread-safe
 * (géré par Spring) - Pas de duplication de logique métier - Pas de création
 * répétée de BCryptPasswordEncoder
 *
 * ============================================================================
 * 2️⃣ AVANT/APRÈS: CompteController (DÉJÀ REFACTORISÉ)
 * ============================================================================
 *
 * ❌ AVANT (code dupliqué partout): ──────────────────────────────────────
 * @Controller public class CompteController {
 * @Autowired private CompteRepo repo;
 * @Autowired private AuthoritiesRepo authrepo;
 * @Autowired private ParentRepo parentrepo;
 * @Autowired private DirectorRepo directorrepo;
 *
 * @GetMapping("/compte") public String listComptes(Principal principal, Model
 * m) { Compte currentuser = null; if (principal != null) { String email =
 * principal.getName(); currentuser = repo.findById(email).get(); // ← Dupliqué
 * dans 3 contrôleurs } List<Compte> listcomptes = (List<Compte>)
 * repo.findAll(); m.addAttribute("currentuser", currentuser);
 * m.addAttribute("listcomptes", listcomptes); return "/compte/index"; }
 *
 * @PostMapping("/compte/save") public String saveCompte(Compte cpt) {
 * BCryptPasswordEncoder bpe = new BCryptPasswordEncoder(); // ← Créé à chaque
 * requête! if (cpt.getPassword() == null) { Compte old =
 * repo.findById(cpt.getEmail()).get(); cpt.setPassword(old.getPassword()); }
 * else { cpt.setPassword(bpe.encode(cpt.getPassword())); } repo.save(cpt);
 * return "redirect:/compte"; } }
 *
 * ✅ APRÈS (avec CompteService Singleton):
 * ──────────────────────────────────────
 * @Controller public class CompteController {
 * @Autowired private CompteService compteService; // ← Une SEULE INSTANCE
 * injectée
 *
 * @GetMapping("/compte") public String listComptes(Principal principal, Model
 * m) { Compte currentuser = compteService.getCurrentUser(principal);
 * List<Compte> listcomptes = compteService.getAllComptes();
 * m.addAttribute("currentuser", currentuser); m.addAttribute("listcomptes",
 * listcomptes); return "/compte/index"; }
 *
 * @PostMapping("/compte/save") public String saveCompte(Compte cpt) {
 * compteService.saveCompteWithOldPassword(cpt); return "redirect:/compte"; } }
 *
 * BILAN: Moins de code, plus lisible, pas de duplication ✅
 *
 * ============================================================================
 * 3️⃣ MÉTHODES DISPONIBLES DANS CompteService
 * ============================================================================
 *
 * CRÉER UN COMPTE: ─────────────── compteService.creerCompte(email, password,
 * "Parent");
 *
 * RÉCUPÉRER L'UTILISATEUR COURANT: ──────────────────────────────── Compte user
 * = compteService.getCurrentUser(principal);
 *
 * SAUVEGARDER UN COMPTE: ──────────────────────
 * compteService.saveCompte(compte); // Chiffre le mot de passe
 * compteService.saveCompteWithOldPassword(compte); // Préserve l'ancien mdp
 *
 * ACTIVER / DÉSACTIVER: ────────────────────
 * compteService.activerCompte(email); // Crée aussi l'autorité
 * compteService.desactiverCompte(email);
 *
 * SUPPRIMER: ────────── compteService.supprimerCompte(email); // Supprime tout
 * (compte + autorités + données)
 *
 * CHANGER LE MOT DE PASSE: ───────────────────────
 * compteService.changerMotDePasse(email, "newPassword");
 * compteService.changerMotDePasseSecurise(email, "oldPassword", "newPassword");
 *
 * VÉRIFIER L'AUTHENTIFICATION: ─────────────────────────── boolean isValid =
 * compteService.validateCredentials(email, password);
 *
 * CONSTRUIRE UN OBJET AFFICHAGE: ───────────────────────────── CompteOwner
 * owner = compteService.buildCompteOwner(compte);
 *
 * ============================================================================
 * 4️⃣ EXEMPLE: Refactoriser DirectorController
 * ============================================================================
 *
 * Voici comment refactoriser DirectorController avec CompteService:
 *
 * ❌ AVANT: ────────
 * @PostMapping("/director/register") public String
 * registerDirector(DirectorInfo info) { // Créer le compte
 * BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder(); Compte compte = new
 * Compte(); compte.setEmail(info.getEmail());
 * compte.setPassword(bcpe.encode(info.getPassword()));
 * compte.setType("Kindergarten Director"); compte.setEnabled(false);
 * compteRepo.save(compte);
 *
 *     // Créer le Director Director director = new Director();
 * director.setEmail(info.getEmail()); ... autres champs ...
 * directorRepo.save(director);
 *
 * return "redirect:/login"; }
 *
 * ✅ APRÈS: ────────
 * @PostMapping("/director/register") public String
 * registerDirector(DirectorInfo info) { // Créer le compte via le Singleton
 * compteService.creerCompte( info.getEmail(), info.getPassword(), "Kindergarten
 * Director" );
 *
 *     // Créer le Director Director director = new Director();
 * director.setEmail(info.getEmail()); ... autres champs ...
 * directorRepo.save(director);
 *
 * return "redirect:/login"; }
 *
 * ============================================================================
 * 5️⃣ EXEMPLE: Refactoriser ParentController
 * ============================================================================
 *
 * De la même façon, tu peux refactoriser ParentController:
 *
 * @PostMapping("/parent/register") public String registerParent(ParentInfo
 * info) { // Créer le compte via le Singleton compteService.creerCompte(
 * info.getEmail(), info.getPassword(), "Parent" );
 *
 *     // Créer le Parent Parent parent = new Parent();
 * parent.setEmail(info.getEmail()); ... autres champs ...
 * parentRepo.save(parent);
 *
 * return "redirect:/login"; }
 *
 * ============================================================================
 * 6️⃣ CAS D'USAGE: Gestion du l'utilisateur courant
 * ============================================================================
 *
 * Pattern récurrent: Il y a une ligne répétée dans CHAQUE contrôleur:
 *
 * ❌ AVANT: ────────
 * @GetMapping("/parent/home") public String parentHome(Principal principal,
 * Model m) { Compte currentuser = null; if (principal != null) { String email =
 * principal.getName(); currentuser = cptrepo.findById(email).get(); // ← Ligne
 * dupliquée! } ... reste du code ... }
 *
 * ✅ APRÈS: ────────
 * @GetMapping("/parent/home") public String parentHome(Principal principal,
 * Model m) { Compte currentuser = compteService.getCurrentUser(principal); //
 * Une ligne! ... reste du code ... }
 *
 * ============================================================================
 * 7️⃣ POURQUOI C'EST UN VRAI SINGLETON GoF?
 * ============================================================================
 *
 * Le pattern GoF Singleton impose 3 principes:
 *
 * ✅ 1. UNE SEULE INSTANCE Spring gère @Service avec un scope "singleton" par
 * défaut → Une seule instance créée au démarrage
 *
 * ✅ 2. POINT D'ACCÈS GLOBAL
 * @Autowired private CompteService compteService; → Accessible depuis n'importe
 * quel bean
 *
 * ✅ 3. CONTRÔLE DE LA CONSTRUCTION Spring IoC crée et gère le cycle de vie →
 * Pas d'appel direct new CompteService() → Équivalent du getInstance()
 * classique
 *
 * Comparaison avec le Singleton classique (pur GoF):
 *
 * Classique: private static CompteService instance; private CompteService() { }
 * public static CompteService getInstance() { if (instance == null) {
 * synchronized(CompteService.class) { if (instance == null) { instance = new
 * CompteService(); } } } return instance; }
 *
 * Spring:
 * @Service public class CompteService { }
 *
 * Spring = Singleton classique mais avec gestion de conteneur ✅
 *
 * ============================================================================
 * 8️⃣ CHECKLIST: QUE MODIFIER POUR LES AUTRES CONTRÔLEURS?
 * ============================================================================
 *
 * Pour refactoriser DirectorController et ParentController:
 *
 * ☐ 1. Remplacer les @Autowired des repos par une seule injection:
 * @Autowired private CompteService compteService;
 *
 * ☐ 2. Remplacer "new BCryptPasswordEncoder()" par l'injection du service
 *
 * ☐ 3. Remplacer les blocs getCurrentUser() par:
 * compteService.getCurrentUser(principal)
 *
 * ☐ 4. Remplacer la création de compte par: compteService.creerCompte(email,
 * password, type)
 *
 * ☐ 5. Supprimer les imports inutiles (Parent, Director, ParentRepo, etc.)
 *
 * ☐ 6. Tester que tout fonctionne!
 *
 * ============================================================================
 * 9️⃣ EXEMPLE COMPLET: InscriptionController
 * ============================================================================
 *
 * Un autre exemple avec InscriptionController (si tu en as un):
 *
 * @Controller public class InscriptionController {
 *
 * @Autowired private CompteService compteService; // ← Singleton
 *
 * @Autowired private InscriptionRepo inscriptionRepo;
 *
 * @GetMapping("/inscription") public String showInscription(Principal
 * principal, Model m) { Compte user = compteService.getCurrentUser(principal);
 * m.addAttribute("user", user); return "/inscription/form"; }
 *
 * @PostMapping("/inscription/save") public String
 * saveInscription(InscriptionData data, Principal principal) { Compte user =
 * compteService.getCurrentUser(principal);
 *
 * Inscription insc = new Inscription(); insc.setEmail(user.getEmail());
 * insc.setData(data); inscriptionRepo.save(insc);
 *
 * return "redirect:/success"; } }
 *
 * ============================================================================
 * 🔟 PROCHAINES ÉTAPES
 * ============================================================================
 *
 * ✅ CompteController: REFACTORISÉ ✓ ⏳ DirectorController: À refactoriser ⏳
 * ParentController: À refactoriser ⏳ Autres contrôleurs: À vérifier
 *
 * Une fois tous les contrôleurs refactorisés: - La gestion des comptes est
 * centralisée - Le code est plus maintenable - Les modifications se font dans
 * un seul endroit - Plus de duplication
 *
 */
public class SingletonPatternGuide {
    // Ce fichier est un guide, pas du code exécutable
}
