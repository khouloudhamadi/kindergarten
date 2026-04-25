package com.kindergarten.kindergarten.director;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.kindergarten.kindergarten.compte.AuthService;
import com.kindergarten.kindergarten.compte.Compte;
import com.kindergarten.kindergarten.compte.CompteService;
import com.kindergarten.kindergarten.compte.RoleService;
import com.kindergarten.kindergarten.compte.RoleType;
import com.kindergarten.kindergarten.parent.Enfant;
import com.kindergarten.kindergarten.parent.EnfantRepo;
import com.kindergarten.kindergarten.parent.Inscription;
import com.kindergarten.kindergarten.parent.InscriptionPayment;
import com.kindergarten.kindergarten.parent.InscriptionRepo;
import com.kindergarten.kindergarten.parent.Parent;
import com.kindergarten.kindergarten.parent.PayReference;
import com.kindergarten.kindergarten.parent.Payment;
import com.kindergarten.kindergarten.parent.PaymentRepo;

/**
 * DirectorController - GRASP Controller Pattern
 *
 * Responsabilités : 1. Créer un nouveau directeur (inscription) 2. Afficher le
 * profil du directeur 3. Sauvegarder les modifications du profil 4. Gérer les
 * enfants et les paiements
 *
 * Délégation : - Création du compte → AuthService - Récupération de
 * l'utilisateur courant → AuthService - Gestion des directeurs → DirectorRepo
 */
@Controller
public class DirectorController {

    @Autowired
    private DirectorRepo repo;

    @Autowired
    private CompteService compteService;

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private EnfantRepo enfrepo;

    @Autowired
    private InscriptionRepo inscrepo;

    @Autowired
    private PaymentRepo paymentrepo;

    /**
     * Inscription d'un nouveau directeur - Pattern GRASP Controller
     *
     * 1. Délègue la création du compte à AuthService 2. Crée l'entité Director
     * 3. Retourne la réponse
     */
    @PostMapping("/director/register")
    public String registerDirector(DirectorInfo dinfo) {
        // Déléguer la création du compte à AuthService
        Compte compte = authService.creerCompte(
                dinfo.getEmail(),
                dinfo.getPassword(),
                "Kindergarten Director"
        );

        // Créer l'entité Director avec le compte
        Director director = new Director();
        director.setCompte(compte);
        director.setEmail(dinfo.getEmail());
        director.setAdresse(dinfo.getAdresse());
        director.setPrenom(dinfo.getPrenom());
        director.setNom(dinfo.getNom());
        director.setTel(dinfo.getTel());
        repo.save(director);

        return "redirect:/";
    }

    @GetMapping("/director/profile")
    public String setProfile(Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        if (currentuser != null && roleService.aLe(currentuser.getEmail(), RoleType.ROLE_DIRECTOR)) {
            model.addAttribute("currentuser", currentuser);
            Director directeur = repo.findById(currentuser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Director introuvable"));
            DirectorInfo dinfo = new DirectorInfo();
            dinfo.setEmail(directeur.getEmail());
            dinfo.setAdresse(directeur.getAdresse());
            dinfo.setNom(directeur.getNom());
            dinfo.setPrenom(directeur.getPrenom());
            dinfo.setTel(directeur.getTel());

            model.addAttribute("dinfo", dinfo);
            model.addAttribute("currentuser", currentuser);

            return "/director/profile";
        }

        return "/error/accessDenied";

    }

    @PostMapping("/director/profile/save")
    public String saveProfile(DirectorInfo dinf) {
        Compte cpt = compteService.getCompteByEmail(dinf.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
        Director directeur = repo.findById(dinf.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Director introuvable"));

        // Mettre à jour les informations du directeur
        directeur.setAdresse(dinf.getAdresse());
        directeur.setNom(dinf.getNom());
        directeur.setPrenom(dinf.getPrenom());
        directeur.setTel(dinf.getTel());
        directeur.setCompte(cpt);
        repo.save(directeur);

        // Changer le mot de passe si fourni
        if (dinf.getPassword() != null && dinf.getPassword().length() > 0) {
            compteService.changerMotDePasse(dinf.getEmail(), dinf.getPassword());
        }

        return "redirect:/";
    }

    public String generateKey() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    @GetMapping("/director/payments")
    public String paymentDInscriptions(Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        if (currentuser != null && roleService.aLe(currentuser.getEmail(), RoleType.ROLE_DIRECTOR)) {
            model.addAttribute("currentuser", currentuser);
            Director director = repo.findById(currentuser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Director introuvable"));
            List<Enfant> listEnfants = (List<Enfant>) enfrepo.findAll();
            List<InscriptionPayment> listInscPayment = new ArrayList<>();
            for (Enfant e : listEnfants) {
                InscriptionPayment inscp = new InscriptionPayment();
                inscp.setId(e.getId());
                inscp.setNom(e.getNom());
                inscp.setPrenom(e.getPrenom());
                inscp.setParent(e.getParent().getNom() + " " + e.getParent().getPrenom());
                List<Inscription> linsc = inscrepo.findByEnfantAndValidOrderByDateDesc(e, true);
                if (linsc != null) {
                    if (linsc.size() > 0) {
                        Inscription ins = linsc.get(0);
                        inscp.setKindergarten_name(ins.getKindergarten().getNom());
                        inscp.setAnneescol(ins.getAnneescolaire());
                        inscp.setClassLevel(ins.getClass_level());
                        inscp.setDateInsc(ins.getDate());
                        inscp.setIdInsc(ins.getId());
                        listInscPayment.add(inscp);
                    }
                }

            }
            model.addAttribute("director", director);
            model.addAttribute("currentuser", currentuser);
            model.addAttribute("listInscPayment", listInscPayment);

            return "/director/payment/index";
        }

        return "/error/accessDenied";

    }

    @GetMapping("/director/payment/pay/{id}")
    public String showPaymentList(@PathVariable("id") Integer id, Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        if (currentuser != null && roleService.aLe(currentuser.getEmail(), RoleType.ROLE_DIRECTOR)) {
            model.addAttribute("currentuser", currentuser);
            PayReference payreference = new PayReference();
            Inscription insc = inscrepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable"));
            Parent parent = insc.getParent();
            List<Payment> payments = paymentrepo.findByInscriptionOrderById(insc);
            model.addAttribute("parent", parent);
            model.addAttribute("currentuser", currentuser);
            model.addAttribute("inscription", insc);
            model.addAttribute("payments", payments);
            model.addAttribute("payreference", payreference);
            return "/director/payment/payForm";
        }
        return "/error/accessDenied";
    }

}
