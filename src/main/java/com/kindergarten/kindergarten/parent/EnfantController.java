package com.kindergarten.kindergarten.parent;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.kindergarten.kindergarten.compte.Compte;
import com.kindergarten.kindergarten.compte.CompteRepo;
import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import com.kindergarten.kindergarten.kindergarten.KinderGartenRepo;

@Controller
public class EnfantController {

    // =====================================================
    // PATRON GoF — FACADE : utilisation de FamilleFacade
    // Au lieu d'injecter EnfantRepo + ParentRepo séparément,
    // on utilise uniquement la Facade → interface unifiée.
    // =====================================================
    @Autowired
    private FamilleFacade familleFacade; // ← FACADE

    // Ces repos restent pour les besoins non couverts par la Facade
    @Autowired
    private CompteRepo cptrepo;

    @Autowired
    private KinderGartenRepo kgrepo;

    @Autowired
    private InscriptionRepo inscrepo;

    @GetMapping("/parent/children")
    public String home(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace parentrepo.findById() + repo.findByParent()
                FamilleFacade.ParentAvecEnfants pae = familleFacade.getParentAvecEnfants(email);

                model.addAttribute("parent", pae.getParent());
                model.addAttribute("children", pae.getEnfants());
                model.addAttribute("currentuser", currentuser);
                return "/parent/children/index";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/children/add")
    public String addEnfant(Principal principal, Model m) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace parentrepo.findById()
                Parent parent = familleFacade.getParent(email);

                Enfant enfant = new Enfant();
                m.addAttribute("enfant", enfant);
                m.addAttribute("parent", parent);
                m.addAttribute("currentuser", currentuser);
                return "/parent/children/showEnfantForm";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/children/edit/{id}")
    public String editEnfant(@PathVariable("id") Integer id, Principal principal, Model m) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace parentrepo.findById() + repo.findById()
                Parent parent = familleFacade.getParent(email);
                Enfant enfant = familleFacade.getEnfant(id);

                m.addAttribute("enfant", enfant);
                m.addAttribute("parent", parent);
                m.addAttribute("currentuser", currentuser);
                return "/parent/children/showEnfantForm";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/children/register/{kgid}")
    public String chooseChildToRegister(@PathVariable("kgid") Integer kgid, Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace parentrepo.findById() + repo.findByParent()
                List<Enfant> allchildren = familleFacade.getEnfantsduParent(email);
                Parent parent = familleFacade.getParent(email);
                KinderGarten kindergarten = kgrepo.findById(kgid).get();

                List<Enfant> children = new ArrayList<>();
                for (Enfant child : allchildren) {
                    if (!inscrepo.existsByEnfantAndValid(child, true)) {
                        children.add(child);
                    }
                }
                model.addAttribute("parent", parent);
                model.addAttribute("children", children);
                model.addAttribute("currentuser", currentuser);
                model.addAttribute("kindergarten", kindergarten);
                return "/parent/children/chooseToRegister";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/children/delete/{id}")
    public String deleteEnfant(@PathVariable("id") Integer id, Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace repo.deleteById()
                familleFacade.supprimerEnfant(id);
                return "redirect:/parent/children";
            }
        }
        return "/error/accessDenied";
    }

    @PostMapping("/parent/children/save")
    public String saveEnfant(Principal principal, Enfant enfant) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace parentrepo.findById() + enfant.setParent() + repo.save()
                familleFacade.ajouterEnfant(email, enfant);
                return "redirect:/parent/children";
            }
        }
        return "/error/accessDenied";
    }
}