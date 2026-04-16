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
    @Autowired
    private EnfantRepo repo;

    @Autowired
    private CompteRepo cptrepo;

    @Autowired
    private ParentRepo parentrepo;

    @Autowired
    private KinderGartenRepo kgrepo;

    @Autowired
    private InscriptionRepo inscrepo;

    @GetMapping("/parent/children")
    public String home(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                Parent parent = parentrepo.findById(email).get();
                List<Enfant> children = repo.findByParent(parent);
                model.addAttribute("parent", parent);
                model.addAttribute("children", children);
                model.addAttribute("currentuser", currentuser);
                return "/parent/children/index";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/children/add")
    public String addEnfant(Principal principal, Model m) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                Parent parent = parentrepo.findById(email).get();
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
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                Parent parent = parentrepo.findById(email).get();
                Enfant enfant = repo.findById(id).get();
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
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                Parent parent = parentrepo.findById(email).get();
                KinderGarten kindergarten = kgrepo.findById(kgid).get();
                List<Enfant> allchildren = repo.findByParent(parent);
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
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                repo.deleteById(id);
                return "redirect:/parent/children";
            }
        }
        return "/error/accessDenied";
    }

    @PostMapping("/parent/children/save")
    public String saveEnfant(Principal principal, Enfant enfant) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Parent")) {
                Parent parent = parentrepo.findById(email).get();
                enfant.setParent(parent);
                repo.save(enfant);
                return "redirect:/parent/children";
            }
        }
        return "/error/accessDenied";
    }
}
