package com.kindergarten.kindergarten.parent;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

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
import com.kindergarten.kindergarten.imgfiles.FileDB;
import com.kindergarten.kindergarten.imgfiles.FileStorageService;
import com.kindergarten.kindergarten.kindergarten.KGwithPhotos;
import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import com.kindergarten.kindergarten.kindergarten.KinderGartenRepo;
import com.kindergarten.kindergarten.parent.service.BusinessException;
import com.kindergarten.kindergarten.parent.service.OneShotPaymentProcessor;
import com.kindergarten.kindergarten.parent.service.SelectedMonthsPaymentProcessor;

/**
 * ParentController - GRASP Controller Pattern
 *
 * Responsabilités :
 * 1. Créer un nouveau parent (inscription)
 * 2. Afficher le profil du parent
 * 3. Sauvegarder les modifications du profil
 * 4. Gérer les enfants et les inscriptions
 * 5. Gérer les paiements (GoF Strategy via OneShotPaymentProcessor /
 * SelectedMonthsPaymentProcessor)
 *
 * Délégation :
 * - Création du compte → AuthService
 * - Récupération de l'utilisateur courant → CompteService
 * - Vérification des rôles → RoleService
 */
@Controller
public class ParentController {

    @Autowired
    private ParentRepo repo;

    @Autowired
    private CompteService compteService;

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private KinderGartenRepo kgrepo;

    @Autowired
    private FileStorageService storage;

    @Autowired
    private EnfantRepo enfrepo;

    @Autowired
    private InscriptionRepo inscrepo;

    @Autowired
    private PaymentRepo paymentrepo;

    // GoF Strategy — paiement en un seul versement
    @Autowired
    private OneShotPaymentProcessor oneShotPaymentProcessor;

    // GoF Strategy — paiement par mois sélectionnés
    @Autowired
    private SelectedMonthsPaymentProcessor selectedMonthsPaymentProcessor;

    // -------------------------------------------------------
    // PARENT : inscription
    // -------------------------------------------------------
    @PostMapping("/parent/register")
    public String registerParent(ParentInfo pinf) {
        Compte cpt = authService.creerCompte(
                pinf.getEmail(),
                pinf.getPassword(),
                "Parent");

        Parent parent = new Parent();
        parent.setCompte(cpt);
        parent.setEmail(pinf.getEmail());
        parent.setAdresse(pinf.getAdresse());
        parent.setNom(pinf.getNom());
        parent.setPrenom(pinf.getPrenom());
        parent.setSexe(pinf.getSexe());
        parent.setTel1(pinf.getTel1());
        parent.setTel2(pinf.getTel2());
        repo.save(parent);

        return "redirect:/";
    }

    // -------------------------------------------------------
    // PARENT : profil
    // -------------------------------------------------------
    @GetMapping("/parent/profile/myProfile")
    public String setProfile(Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        if (currentuser != null && roleService.aLe(currentuser.getEmail(), RoleType.ROLE_PARENT)) {
            Parent parent = repo.findById(currentuser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Parent introuvable"));

            ParentInfo pinfo = new ParentInfo();
            pinfo.setEmail(parent.getEmail());
            pinfo.setAdresse(parent.getAdresse());
            pinfo.setNom(parent.getNom());
            pinfo.setPrenom(parent.getPrenom());
            pinfo.setTel1(parent.getTel1());
            pinfo.setTel2(parent.getTel2());
            pinfo.setSexe(parent.getSexe());

            model.addAttribute("pinfo", pinfo);
            model.addAttribute("currentuser", currentuser);
            return "/parent/profile";
        }

        return "/error/accessDenied";
    }

    @PostMapping("/parent/profile/save")
    public String saveProfile(ParentInfo pinf) {
        Compte cpt = compteService.getCompteByEmail(pinf.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
        Parent parent = repo.findById(pinf.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Parent introuvable"));

        parent.setAdresse(pinf.getAdresse());
        parent.setNom(pinf.getNom());
        parent.setPrenom(pinf.getPrenom());
        parent.setTel1(pinf.getTel1());
        parent.setTel2(pinf.getTel2());
        parent.setSexe(pinf.getSexe());
        parent.setCompte(cpt);
        repo.save(parent);

        if (pinf.getPassword() != null && pinf.getPassword().length() > 0) {
            compteService.changerMotDePasse(pinf.getEmail(), pinf.getPassword());
        }

        return "redirect:/";
    }

    // -------------------------------------------------------
    // PARENT : home — liste des kindergartens
    // -------------------------------------------------------
    @GetMapping("/parent/home")
    public String registerChild(Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);
        List<KinderGarten> listKG = (List<KinderGarten>) kgrepo.findAll();
        List<KGwithPhotos> listKGwithPhotos = new ArrayList<>();

        for (KinderGarten kg : listKG) {
            KGwithPhotos kgph = new KGwithPhotos();
            kgph.setId(kg.getId());
            kgph.setAdresse(kg.getAdresse());
            kgph.setNom(kg.getNom());
            kgph.setEmail(kg.getEmail());
            kgph.setTel(kg.getTel());
            kgph.setDirectorfirstname(kg.getDirector().getPrenom());
            kgph.setDirectorlastname(kg.getDirector().getNom());

            List<FileDB> lfdb = new ArrayList<>();
            String[] arnf = kg.getPhotos().split("@");
            for (String filename : arnf) {
                FileDB fdb = storage.getFile(filename);
                fdb.setDataB64(Base64.getEncoder().encodeToString(fdb.getData()));
                lfdb.add(fdb);
            }
            kgph.setPhotos(lfdb);
            listKGwithPhotos.add(kgph);
        }

        model.addAttribute("currentuser", currentuser);
        model.addAttribute("listKGwithPhotos", listKGwithPhotos);
        return "/parent/home";
    }

    // -------------------------------------------------------
    // PARENT : paiements — liste
    // -------------------------------------------------------
    @GetMapping("/parent/payment")
    public String paymentInscriptions(Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        if (currentuser != null && roleService.aLe(currentuser.getEmail(), RoleType.ROLE_PARENT)) {
            Parent parent = repo.findById(currentuser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Parent introuvable"));

            List<Enfant> listEnfants = enfrepo.findByParent(parent);
            List<InscriptionPayment> listInscPayment = new ArrayList<>();

            for (Enfant e : listEnfants) {
                List<Inscription> linsc = inscrepo.findByEnfantAndValidOrderByDateDesc(e, true);
                if (linsc != null && linsc.size() > 0) {
                    Inscription ins = linsc.get(0);
                    InscriptionPayment inscp = new InscriptionPayment();
                    inscp.setId(e.getId());
                    inscp.setNom(e.getNom());
                    inscp.setPrenom(e.getPrenom());
                    inscp.setParent(e.getParent().getNom() + " " + e.getParent().getPrenom());
                    inscp.setKindergarten_name(ins.getKindergarten().getNom());
                    inscp.setAnneescol(ins.getAnneescolaire());
                    inscp.setClassLevel(ins.getClass_level());
                    inscp.setDateInsc(ins.getDate());
                    inscp.setIdInsc(ins.getId());
                    listInscPayment.add(inscp);
                }
            }

            model.addAttribute("parent", parent);
            model.addAttribute("currentuser", currentuser);
            model.addAttribute("listInscPayment", listInscPayment);
            return "/parent/payment/index";
        }

        return "/error/accessDenied";
    }

    // -------------------------------------------------------
    // PARENT : paiements — formulaire
    // -------------------------------------------------------
    @GetMapping("/parent/payment/pay/{id}")
    public String showPaymentForm(@PathVariable("id") Integer id,
            Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        if (currentuser != null && roleService.aLe(currentuser.getEmail(), RoleType.ROLE_PARENT)) {
            Parent parent = repo.findById(currentuser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Parent introuvable"));
            Inscription insc = inscrepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable"));
            List<Payment> payments = paymentrepo.findByInscriptionOrderById(insc);

            model.addAttribute("parent", parent);
            model.addAttribute("currentuser", currentuser);
            model.addAttribute("inscription", insc);
            model.addAttribute("payments", payments);
            model.addAttribute("payreference", new PayReference());
            return "/parent/payment/payForm";
        }

        return "/error/accessDenied";
    }

    // -------------------------------------------------------
    // PARENT : paiements — sauvegarde
    // GoF Strategy : ONE_SHOT ou mois sélectionnés
    // -------------------------------------------------------
    @PostMapping("/parent/payment/pay/save")
    public String savePayment(Principal principal, PayReference payreference, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(date);

        if (currentuser != null && roleService.aLe(currentuser.getEmail(), RoleType.ROLE_PARENT)) {
            model.addAttribute("currentuser", currentuser);

            try {
                Inscription insc = inscrepo.findById(payreference.getIdinsc())
                        .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable"));

                // GoF Strategy — délégation selon le type de paiement
                if ("ONE_SHOT".equals(payreference.getPaymentStrategy())) {
                    oneShotPaymentProcessor.processOneShotPayment(insc, payreference, today);
                } else {
                    selectedMonthsPaymentProcessor.processSelectedMonthsPayment(insc, payreference, today);
                }

                return "redirect:/parent/payment";

            } catch (BusinessException e) {
                // Règle métier violée → réafficher le formulaire avec le message
                model.addAttribute("errorMessage", e.getMessage());

                Parent parent = repo.findById(currentuser.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Parent introuvable"));
                Inscription insc = inscrepo.findById(payreference.getIdinsc())
                        .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable"));
                List<Payment> payments = paymentrepo.findByInscriptionOrderById(insc);

                model.addAttribute("parent", parent);
                model.addAttribute("inscription", insc);
                model.addAttribute("payments", payments);
                model.addAttribute("payreference", payreference);

                return "/parent/payment/payForm";
            }
        }

        return "/error/accessDenied";
    }

    // -------------------------------------------------------
    // PARENT : enfants inscrits
    // -------------------------------------------------------
    @GetMapping("/parent/registeredChildren")
    public String showRegistredChildren(Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        if (currentuser != null && roleService.aLe(currentuser.getEmail(), RoleType.ROLE_PARENT)) {
            Parent parent = repo.findById(currentuser.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Parent introuvable"));

            List<Enfant> listEnfants = enfrepo.findByParent(parent);
            List<InscriptionPayment> listInscPayment = new ArrayList<>();

            for (Enfant e : listEnfants) {
                List<Inscription> linsc = inscrepo.findByEnfantAndValidOrderByDateDesc(e, true);
                if (linsc != null && linsc.size() > 0) {
                    Inscription ins = linsc.get(0);
                    InscriptionPayment inscp = new InscriptionPayment();
                    inscp.setId(e.getId());
                    inscp.setNom(e.getNom());
                    inscp.setPrenom(e.getPrenom());
                    inscp.setKindergarten_name(ins.getKindergarten().getNom());
                    inscp.setAnneescol(ins.getAnneescolaire());
                    inscp.setClassLevel(ins.getClass_level());
                    inscp.setDateInsc(ins.getDate());
                    listInscPayment.add(inscp);
                }
            }

            model.addAttribute("parent", parent);
            model.addAttribute("currentuser", currentuser);
            model.addAttribute("listInscPayment", listInscPayment);
            return "/parent/registeredChildren";
        }

        return "/error/accessDenied";
    }
}