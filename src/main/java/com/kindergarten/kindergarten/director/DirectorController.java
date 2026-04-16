package com.kindergarten.kindergarten.director;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.kindergarten.kindergarten.compte.Compte;
import com.kindergarten.kindergarten.compte.CompteRepo;
import com.kindergarten.kindergarten.parent.Enfant;
import com.kindergarten.kindergarten.parent.EnfantRepo;
import com.kindergarten.kindergarten.parent.Inscription;
import com.kindergarten.kindergarten.parent.InscriptionPayment;
import com.kindergarten.kindergarten.parent.InscriptionRepo;
import com.kindergarten.kindergarten.parent.Parent;
import com.kindergarten.kindergarten.parent.PayReference;
import com.kindergarten.kindergarten.parent.Payment;
import com.kindergarten.kindergarten.parent.PaymentRepo;

@Controller
public class DirectorController {
    @Autowired
    private DirectorRepo repo;

    @Autowired
    private CompteRepo cptrepo;

    @Autowired
    private DirectorRepo directeurrepo;

    @Autowired
    private EnfantRepo enfrepo;

    @Autowired
    private InscriptionRepo inscrepo;

    @Autowired
    private PaymentRepo paymentrepo;

    @PostMapping("/director/register")
    public String registerDirector(DirectorInfo dinfo) {
        BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
        Compte compte = new Compte();
        Director d = new Director();
        compte.setType("Kindergarten Director");
        compte.setEmail(dinfo.getEmail());
        compte.setPassword(bcpe.encode(dinfo.getPassword()));
        compte.setEnabled(false);
        cptrepo.save(compte);
        d.setCompte(compte);
        d.setEmail(dinfo.getEmail());
        d.setAdresse(dinfo.getAdresse());
        d.setPrenom(dinfo.getPrenom());
        d.setNom(dinfo.getNom());
        d.setTel(dinfo.getTel());
        repo.save(d);
        return "redirect:/";
    }

    @GetMapping("/director/profile")
    public String setProfile(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);
            if (currentuser.getType().equals("Kindergarten Director")) {
                Director directeur = directeurrepo.findById(email).get();
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
        }

        return "/error/accessDenied";

    }

    @PostMapping("/director/profile/save")
    public String saveProfile(DirectorInfo dinf) {
        BCryptPasswordEncoder bpe = new BCryptPasswordEncoder();
        Compte cpt = cptrepo.findById(dinf.getEmail()).get();
        Director directeur = directeurrepo.findById(dinf.getEmail()).get();
        directeur.setAdresse(dinf.getAdresse());
        directeur.setNom(dinf.getNom());
        directeur.setPrenom(dinf.getPrenom());
        if (dinf.getPassword().length() > 0) {
            cpt.setPassword(bpe.encode(dinf.getPassword()));
            cpt.setConfirm_password(bpe.encode(dinf.getPassword()));
        }
        directeur.setCompte(cpt);
        directeur.setTel(dinf.getTel());
        cptrepo.save(cpt);
        directeurrepo.save(directeur);

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
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);
            if (currentuser.getType().equals("Kindergarten Director")) {
                Director director = directeurrepo.findById(email).get();
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
        }

        return "/error/accessDenied";

    }

    @GetMapping("/director/payment/pay/{id}")
    public String showPaymentList(@PathVariable("id") Integer id, Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);
            if (currentuser.getType().equals("Kindergarten Director")) {
                PayReference payreference = new PayReference();
                Inscription insc = inscrepo.findById(id).get();
                Parent parent = insc.getParent();
                List<Payment> payments = paymentrepo.findByInscriptionOrderById(insc);
                model.addAttribute("parent", parent);
                model.addAttribute("currentuser", currentuser);
                model.addAttribute("inscription", insc);
                model.addAttribute("payments", payments);
                model.addAttribute("payreference", payreference);
                return "/director/payment/payForm";
            }
        }
        return "/error/accessDenied";
    }

}
