package com.kindergarten.kindergarten.parent;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.kindergarten.kindergarten.compte.Compte;
import com.kindergarten.kindergarten.compte.CompteRepo;
import com.kindergarten.kindergarten.imgfiles.FileDB;
import com.kindergarten.kindergarten.imgfiles.FileStorageService;
import com.kindergarten.kindergarten.kindergarten.KGwithPhotos;
import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import com.kindergarten.kindergarten.kindergarten.KinderGartenRepo;

@Controller
public class ParentController {
    @Autowired
    private ParentRepo repo;

    @Autowired
    private CompteRepo cptrepo;

    @Autowired
    KinderGartenRepo kgrepo;

    @Autowired
    private FileStorageService storage;

    @Autowired
    private EnfantRepo enfrepo;

    @Autowired
    private InscriptionRepo inscrepo;

    @Autowired
    private PaymentRepo paymentrepo;

    @PostMapping("/parent/register")
    public String registerParent(ParentInfo pinf) {
        BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
        Parent parent = new Parent();
        Compte cpt = new Compte();
        cpt.setType("Parent");
        cpt.setEmail(pinf.getEmail());
        cpt.setPassword(bcpe.encode(pinf.getPassword()));
        cpt.setConfirm_password(bcpe.encode(pinf.getPassword()));
        cpt.setEnabled(false);
        cptrepo.save(cpt);
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

    @GetMapping("/parent/profile/myProfile")
    public String setProfile(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);
            if (currentuser.getType().equals("Parent")) {
                Parent parent = repo.findById(email).get();
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
        }

        return "/error/accessDenied";

    }

    @PostMapping("/parent/profile/save")
    public String saveProfile(ParentInfo pinf) {
        BCryptPasswordEncoder bpe = new BCryptPasswordEncoder();
        Compte cpt = cptrepo.findById(pinf.getEmail()).get();
        Parent parent = repo.findById(pinf.getEmail()).get();
        parent.setAdresse(pinf.getAdresse());
        parent.setNom(pinf.getNom());
        parent.setPrenom(pinf.getPrenom());
        if (pinf.getPassword().length() > 0) {
            cpt.setPassword(bpe.encode(pinf.getPassword()));
            cpt.setConfirm_password(bpe.encode(pinf.getPassword()));
        }
        parent.setCompte(cpt);
        parent.setTel1(pinf.getTel1());
        parent.setTel2(pinf.getTel2());
        parent.setSexe(pinf.getSexe());
        cptrepo.save(cpt);
        repo.save(parent);

        return "redirect:/";
    }

    @GetMapping("/parent/home")
    public String registerChild(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
        }
        List<KinderGarten> listKG = null;
        List<KGwithPhotos> listKGwithPhotos = new ArrayList<>();

        listKG = (List<KinderGarten>) kgrepo.findAll();
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
            for (int i = 0; i < arnf.length; i++) {
                FileDB fdb = storage.getFile(arnf[i]);
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

    @GetMapping("/parent/payment")
    public String paymentInscriptions(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);
            if (currentuser.getType().equals("Parent")) {
                Parent parent = repo.findById(email).get();
                List<Enfant> listEnfants = enfrepo.findByParent(parent);
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
                model.addAttribute("parent", parent);
                model.addAttribute("currentuser", currentuser);
                model.addAttribute("listInscPayment", listInscPayment);

                return "/parent/payment/index";
            }
        }

        return "/error/accessDenied";

    }

    @GetMapping("/parent/payment/pay/{id}")
    public String showPaymentForm(@PathVariable("id") Integer id, Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);
            if (currentuser.getType().equals("Parent")) {
                PayReference payreference = new PayReference();
                Parent parent = repo.findById(email).get();
                Inscription insc = inscrepo.findById(id).get();
                List<Payment> payments = paymentrepo.findByInscriptionOrderById(insc);
                model.addAttribute("parent", parent);
                model.addAttribute("currentuser", currentuser);
                model.addAttribute("inscription", insc);
                model.addAttribute("payments", payments);
                model.addAttribute("payreference", payreference);
                return "/parent/payment/payForm";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/registeredChildren")
    public String showRegistredChildren(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);
            if (currentuser.getType().equals("Parent")) {
                Parent parent = repo.findById(email).get();
                List<Enfant> listEnfants = enfrepo.findByParent(parent);
                List<InscriptionPayment> listInscPayment = new ArrayList<>();
                for (Enfant e : listEnfants) {
                    InscriptionPayment inscp = new InscriptionPayment();
                    inscp.setId(e.getId());
                    inscp.setNom(e.getNom());
                    inscp.setPrenom(e.getPrenom());
                    List<Inscription> linsc = inscrepo.findByEnfantAndValidOrderByDateDesc(e, true);
                    if (linsc != null) {
                        if (linsc.size() > 0) {
                            Inscription ins = linsc.get(0);
                            inscp.setKindergarten_name(ins.getKindergarten().getNom());
                            inscp.setAnneescol(ins.getAnneescolaire());
                            inscp.setClassLevel(ins.getClass_level());
                            inscp.setDateInsc(ins.getDate());
                            listInscPayment.add(inscp);
                        }
                    }

                }
                model.addAttribute("parent", parent);
                model.addAttribute("currentuser", currentuser);
                model.addAttribute("listInscPayment", listInscPayment);

                return "/parent/registeredChildren";
            }
        }

        return "/error/accessDenied";

    }

    @PostMapping("/parent/payment/pay/save")
    public String savePayment(Principal principal, PayReference payreference, Model model) {
        Compte currentuser = null;
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(date);
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);
            if (currentuser.getType().equals("Parent")) {
                Inscription insc = inscrepo.findById(payreference.getIdinsc()).get();
                System.out.println("ooooooooooooooooooooooooooooooooooooooooooooooooooo" + payreference.getMonths());
                String[] idmonths = payreference.getMonths().split("@");
                List<Payment> payments = paymentrepo.findByInscription(insc);
                for (Payment p : payments) {
                    for (int i = 0; i < idmonths.length; i++) {
                        Integer idm = Integer.parseInt(idmonths[i].substring(2));
                        if (p.getMonthnumber() == idm) {
                            p.setDate_payment(today);
                            p.setMontant_percu(payreference.getAmountpermonth());
                            p.setReference_payment(payreference.getReference());
                            p.setType_payment("Bank Card");
                            paymentrepo.save(p);
                        }
                    }

                }
                return "redirect:/parent/payment";
            }

        }
        return "/error/accessDenied";
    }
}