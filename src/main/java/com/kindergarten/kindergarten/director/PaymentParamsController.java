package com.kindergarten.kindergarten.director;

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
import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import com.kindergarten.kindergarten.kindergarten.KinderGartenRepo;

@Controller
public class PaymentParamsController {
    @Autowired
    private CompteRepo cptrepo;

    @Autowired
    private DirectorRepo drepo;

    @Autowired
    private KinderGartenRepo kgrepo;

    @Autowired
    private PaymentParamsRepo repo;

    @GetMapping("/director/paymentParams")
    public String listPaymentParams(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Kindergarten Director")) {
                Director directeur = drepo.findById(email).get();
                List<PaymentParams> listpaymentparams = repo.findByDirectorOrderByKindergarten(directeur);
                model.addAttribute("listpaymentparams", listpaymentparams);
                model.addAttribute("directeur", directeur);
                model.addAttribute("currentuser", currentuser);

                return "/director/paymentParams";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/director/paymentParams/new")
    public String addPaymentParam(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Kindergarten Director")) {
                Director directeur = drepo.findById(email).get();
                PaymentParams pp = new PaymentParams();
                List<KinderGarten> listkindergarten = kgrepo.findByDirector(directeur);
                pp.setDirector(directeur);
                model.addAttribute("paymentparams", pp);
                model.addAttribute("directeur", directeur);
                model.addAttribute("listkindergarten", listkindergarten);
                model.addAttribute("currentuser", currentuser);

                return "/director/paymentParamsForm";
            }
        }
        return "/error/accessDenied";
    }

    @PostMapping("/director/paymentParams/save")
    public String teacherSave(Principal principal, PaymentParams paymentparams) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Kindergarten Director")) {
                Director directeur = drepo.findById(email).get();
                paymentparams.setDirector(directeur);
                repo.save(paymentparams);
                return "redirect:/director/paymentParams";
            }
        }
        return "/error/erreur";

    }

    @GetMapping("/director/paymentParams/edit/{id}")
    public String paymentParamEdit(@PathVariable("id") Integer id, Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
            if (currentuser.getType().equals("Kindergarten Director")) {
                Director directeur = drepo.findById(email).get();
                PaymentParams paymentparams = repo.findById(id).get();
                List<KinderGarten> listkindergarten = kgrepo.findByDirector(directeur);
                model.addAttribute("paymentparams", paymentparams);
                model.addAttribute("directeur", directeur);
                model.addAttribute("listkindergarten", listkindergarten);
                model.addAttribute("currentuser", currentuser);

                return "/director/paymentParamsForm";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/director/paymentParams/delete/{id}")
    public String paymentParamsDelete(@PathVariable("id") Integer id) {
        repo.deleteById(id);
        return "redirect:/director/paymentParams";
    }

}
