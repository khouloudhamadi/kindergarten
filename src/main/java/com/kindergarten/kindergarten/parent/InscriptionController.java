package com.kindergarten.kindergarten.parent;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.kindergarten.kindergarten.compte.Compte;
import com.kindergarten.kindergarten.compte.CompteRepo;
import com.kindergarten.kindergarten.director.PaymentParams;
import com.kindergarten.kindergarten.director.PaymentParamsRepo;
import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import com.kindergarten.kindergarten.kindergarten.KinderGartenRepo;

@Controller
public class InscriptionController {
    @Autowired
    private InscriptionRepo inscrepo;
    @Autowired
    private KinderGartenRepo kgrepo;
    @Autowired
    private EnfantRepo enfantRepo;
    @Autowired
    private CompteRepo cptrepo;

    @Autowired
    private ParentRepo parentrepo;

    @Autowired
    private PaymentRepo paymentrepo;

    @Autowired
    private PaymentParamsRepo paymentparamsrepo;

    @GetMapping("/director/children")
    public String listValidateInsc(Principal principal, Model m) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Kindergarten Director")) {
                List<Inscription> listinscription = (List<Inscription>) inscrepo.findAll();
                m.addAttribute("currentuser", currentuser);
                m.addAttribute("listinscription", listinscription);
                return "/director/children/validenroll";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/children/unenroll")
    public String showlist(Principal principal, Model m) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                Parent parent = parentrepo.findById(email).get();
                List<Inscription> listinscription = (List<Inscription>) inscrepo.findByParent(parent);
                m.addAttribute("currentuser", currentuser);
                m.addAttribute("listinscription", listinscription);
                return "/parent/children/unenroll";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/children/registerChild/{kgid}/{childid}")
    public String showInscForm(@PathVariable("kgid") Integer kgid, @PathVariable("childid") Integer childid,
            Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                Parent parent = parentrepo.findById(email).get();
                KinderGarten kindergarten = kgrepo.findById(kgid).get();
                Enfant enfant = enfantRepo.findById(childid).get();
                InscriptionUI inscription = new InscriptionUI();
                inscription.setEnfid(enfant.getId());
                inscription.setKgid(kindergarten.getId());
                inscription.setId(0);
                model.addAttribute("parent", parent);
                model.addAttribute("enfant", enfant);
                model.addAttribute("currentuser", currentuser);
                model.addAttribute("kindergarten", kindergarten);
                model.addAttribute("inscription", inscription);
                return "/parent/children/FormInsc";
            }
        }
        return "/error/accessDenied";
    }

    @PostMapping("/parent/children/saveInsc")
    public String saveInscription(Principal principal, InscriptionUI inscriptionUI) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                Parent parent = parentrepo.findById(email).get();
                Enfant enfant = enfantRepo.findById(inscriptionUI.getEnfid()).get();
                KinderGarten kindergarten = kgrepo.findById(inscriptionUI.getKgid()).get();
                Inscription inscription = new Inscription();
                inscription.setAnneescolaire(inscriptionUI.getAnneescol());
                inscription.setDate(inscriptionUI.getDate());
                inscription.setClass_level(inscriptionUI.getClass_level());
                inscription.setEnfant(enfant);
                inscription.setKindergarten(kindergarten);
                inscription.setParent(parent);
                inscription.setValid(false);
                inscrepo.save(inscription);
                return "redirect:/";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/children/unenroll/delete/{id}")
    public String deleteInscription(@PathVariable("id") String id) {
        inscrepo.deleteById(Integer.parseInt(id));
        ;
        return "redirect:/parent/children/unenroll";
    }

    @GetMapping("/director/children/validateinsc/{id}/{op}")
    public String validUnvalidInscription(@PathVariable("id") Integer id, @PathVariable("op") Integer op) {
        Inscription inscription = inscrepo.findById(id).get();

        if (op == 1) {
            inscription.setValid(true);
            PaymentParams pp = paymentparamsrepo.findByKindergartenAndAnneescolAndClassLevel(
                    inscription.getKindergarten(),
                    inscription.getAnneescolaire(), inscription.getClass_level());

            Double monthPrice = pp.getPrice() / pp.getNbMonths();
            for (int i = 0; i < pp.getNbMonths(); i++) {
                Payment payment = new Payment();
                payment.setInscription(inscription);
                payment.setMontant_du(monthPrice);
                payment.setMontant_percu(0.0);
                payment.setType_payment("");
                payment.setReference_payment("");
                payment.setDate_payment("");
                int mn = (pp.getStartMonth() + i) % 12;
                if (mn == 0)
                    mn = 12;
                payment.setMonthnumber(mn);
                paymentrepo.save(payment);
            }
        } else {
            inscription.setValid(false);
        }
        inscrepo.save(inscription);
        return "redirect:/director/children";
    }
}
