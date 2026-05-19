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
import com.kindergarten.kindergarten.parent.service.BusinessException;
import com.kindergarten.kindergarten.parent.service.OneShotPaymentProcessor;
import com.kindergarten.kindergarten.parent.service.SelectedMonthsPaymentProcessor;

@Controller
public class ParentController {

    // =====================================================
    // PATRON GoF — FACADE : utilisation de FamilleFacade
    // Au lieu d'injecter ParentRepo + EnfantRepo séparément,
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
    private FileStorageService storage;

    @Autowired
    private InscriptionRepo inscrepo;

    @Autowired
    private PaymentRepo paymentrepo;

    @Autowired
    private PaymentStrategyFactory paymentStrategyFactory;

    @Autowired
    private com.kindergarten.kindergarten.parent.service.PaymentService paymentService;

    @Autowired
private OneShotPaymentProcessor oneShotPaymentProcessor;

@Autowired
private SelectedMonthsPaymentProcessor selectedMonthsPaymentProcessor;

    @PostMapping("/parent/register")
    public String registerParent(ParentInfo pinf) {
        BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();

        Compte cpt = new Compte();
        cpt.setType("Parent");
        cpt.setEmail(pinf.getEmail());
        cpt.setPassword(bcpe.encode(pinf.getPassword()));
        cpt.setConfirm_password(bcpe.encode(pinf.getPassword()));
        cpt.setEnabled(false);
        cptrepo.save(cpt);

        Parent parent = new Parent();
        parent.setCompte(cpt);
        parent.setEmail(pinf.getEmail());
        parent.setAdresse(pinf.getAdresse());
        parent.setNom(pinf.getNom());
        parent.setPrenom(pinf.getPrenom());
        parent.setSexe(pinf.getSexe());
        parent.setTel1(pinf.getTel1());
        parent.setTel2(pinf.getTel2());

        // ✅ FACADE : remplace repo.save(parent)
        familleFacade.sauvegarderParent(parent);

        return "redirect:/";
    }

    @GetMapping("/parent/profile/myProfile")
    public String setProfile(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            model.addAttribute("currentuser", currentuser);

            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace repo.findById(email).get()
                Parent parent = familleFacade.getParent(email);

                ParentInfo pinfo = new ParentInfo();
                pinfo.setEmail(parent.getEmail());
                pinfo.setAdresse(parent.getAdresse());
                pinfo.setNom(parent.getNom());
                pinfo.setPrenom(parent.getPrenom());
                pinfo.setTel1(parent.getTel1());
                pinfo.setTel2(parent.getTel2());
                pinfo.setSexe(parent.getSexe());

                model.addAttribute("pinfo", pinfo);
                return "/parent/profile";
            }
        }
        return "/error/accessDenied";
    }

    @PostMapping("/parent/profile/save")
    public String saveProfile(ParentInfo pinf) {
        BCryptPasswordEncoder bpe = new BCryptPasswordEncoder();

        Compte cpt = cptrepo.findById(pinf.getEmail()).get();

        // ✅ FACADE : remplace repo.findById(pinf.getEmail()).get()
        Parent parent = familleFacade.getParent(pinf.getEmail());

        parent.setAdresse(pinf.getAdresse());
        parent.setNom(pinf.getNom());
        parent.setPrenom(pinf.getPrenom());
        parent.setTel1(pinf.getTel1());
        parent.setTel2(pinf.getTel2());
        parent.setSexe(pinf.getSexe());

        if (pinf.getPassword().length() > 0) {
            cpt.setPassword(bpe.encode(pinf.getPassword()));
            cpt.setConfirm_password(bpe.encode(pinf.getPassword()));
        }
        parent.setCompte(cpt);
        cptrepo.save(cpt);

        // ✅ FACADE : remplace repo.save(parent)
        familleFacade.sauvegarderParent(parent);

        return "redirect:/";
    }

    @GetMapping("/parent/home")
    public String registerChild(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
        }

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
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            model.addAttribute("currentuser", currentuser);

            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace repo.findById() + enfrepo.findByParent()
                Parent parent = familleFacade.getParent(email);
                List<Enfant> listEnfants = familleFacade.getEnfantsduParent(email);

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
                model.addAttribute("listInscPayment", listInscPayment);
                return "/parent/payment/index";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/parent/payment/pay/{id}")
    public String showPaymentForm(@PathVariable("id") Integer id, Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            model.addAttribute("currentuser", currentuser);

            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace repo.findById(email).get()
                Parent parent = familleFacade.getParent(email);

                PayReference payreference = new PayReference();
                Inscription insc = inscrepo.findById(id).get();
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

    @GetMapping("/parent/registeredChildren")
    public String showRegistredChildren(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            model.addAttribute("currentuser", currentuser);

            if (currentuser.getType().equals("Parent")) {

                // ✅ FACADE : remplace repo.findById() + enfrepo.findByParent()
                Parent parent = familleFacade.getParent(email);
                List<Enfant> listEnfants = familleFacade.getEnfantsduParent(email);

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
                model.addAttribute("listInscPayment", listInscPayment);
                return "/parent/registeredChildren";
            }
        }
        return "/error/accessDenied";
    }


    /* 
    @PostMapping("/parent/payment/pay/save")
    public String savePayment(Principal principal, PayReference payreference, Model model) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String today = formatter.format(date);

        if (principal != null) {
            String email = principal.getName();
            Compte currentuser = cptrepo.findById(email).get();
            model.addAttribute("currentuser", currentuser);

            if (currentuser.getType().equals("Parent")) {
                Inscription insc = inscrepo.findById(payreference.getIdinsc()).get();
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
            List<Payment> payments = paymentrepo.findByInscription(insc);

            PaymentStrategy strategy =
                paymentStrategyFactory.getStrategy(payreference.getPaymentStrategy());

            strategy.pay(insc, payreference, payments, today);

            return "redirect:/parent/payment";
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

            if ("ONE_SHOT".equals(payreference.getPaymentStrategy())) {
                paymentService.processOneShotPayment(insc, payreference, today);
            } else {
                paymentService.processSelectedMonthsPayment(insc, payreference, today);
            }

            return "redirect:/parent/payment";
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

            if ("ONE_SHOT".equals(payreference.getPaymentStrategy())) {
                oneShotPaymentProcessor.processOneShotPayment(insc, payreference, today);
            } else {
                selectedMonthsPaymentProcessor.processSelectedMonthsPayment(insc, payreference, today);
            }

            return "redirect:/parent/payment";
        }
    }

    return "/error/accessDenied";
}
    */

@PostMapping("/parent/payment/pay/save")
public String savePayment(Principal principal, PayReference payreference, Model model) {
    Compte currentuser = null;
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    String today = formatter.format(date);

    try {
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();

            model.addAttribute("currentuser", currentuser);

            if (currentuser.getType().equals("Parent")) {
                Inscription insc = inscrepo.findById(payreference.getIdinsc()).get();

                if ("ONE_SHOT".equals(payreference.getPaymentStrategy())) {
                    oneShotPaymentProcessor.processOneShotPayment(insc, payreference, today);
                } else {
                    selectedMonthsPaymentProcessor.processSelectedMonthsPayment(insc, payreference, today);
                }

                return "redirect:/parent/payment";
            }
        }

        return "/error/accessDenied";
    } catch (BusinessException e) {
        model.addAttribute("errorMessage", e.getMessage());

        Parent parent = repo.findById(principal.getName()).get();
        Inscription insc = inscrepo.findById(payreference.getIdinsc()).get();
        List<Payment> payments = paymentrepo.findByInscriptionOrderById(insc);

        model.addAttribute("parent", parent);
        model.addAttribute("inscription", insc);
        model.addAttribute("payments", payments);
        model.addAttribute("payreference", payreference);

        return "/parent/payment/payForm";
    }
}
}