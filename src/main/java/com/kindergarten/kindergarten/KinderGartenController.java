package com.kindergarten.kindergarten;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kindergarten.kindergarten.compte.Compte;
import com.kindergarten.kindergarten.compte.CompteRepo;
import com.kindergarten.kindergarten.director.DirectorInfo;
import com.kindergarten.kindergarten.imgfiles.FileDB;
import com.kindergarten.kindergarten.imgfiles.FileStorageService;
import com.kindergarten.kindergarten.kindergarten.KGwithPhotos;
import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import com.kindergarten.kindergarten.kindergarten.KinderGartenRepo;
import com.kindergarten.kindergarten.parent.ParentInfo;

@Controller
public class KinderGartenController {
    @Autowired
    CompteRepo cptrepo;
    @Autowired
    KinderGartenRepo kgrepo;
    @Autowired
    private FileStorageService storage;

    @GetMapping("/")
    public String home(Principal principal, Model model) {
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
            if (kg.getPhotos().length() > 0) {
                List<FileDB> lfdb = new ArrayList<>();
                String[] arnf = kg.getPhotos().split("@");

                for (int i = 0; i < arnf.length; i++) {
                    FileDB fdb = storage.getFile(arnf[i]);
                    fdb.setDataB64(Base64.getEncoder().encodeToString(fdb.getData()));
                    lfdb.add(fdb);
                }
                kgph.setPhotos(lfdb);
            }
            listKGwithPhotos.add(kgph);
        }
        model.addAttribute("currentuser", currentuser);
        model.addAttribute("listKGwithPhotos", listKGwithPhotos);
        return "/index";
    }

    @GetMapping("/jardins")
    public String jardin(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
        }
        model.addAttribute("currentuser", currentuser);
        return "/jardin/indexjardin";
    }

    @GetMapping("/kindergartens")
    public String showKinderGartensList(Principal principal, Model model) {
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
            if (kg.getPhotos().length() > 0) {
                List<FileDB> lfdb = new ArrayList<>();
                String[] arnf = kg.getPhotos().split("@");
                for (int i = 0; i < arnf.length; i++) {
                    FileDB fdb = storage.getFile(arnf[i]);
                    fdb.setDataB64(Base64.getEncoder().encodeToString(fdb.getData()));
                    lfdb.add(fdb);
                }
                kgph.setPhotos(lfdb);
            }
            listKGwithPhotos.add(kgph);
        }
        model.addAttribute("currentuser", currentuser);
        model.addAttribute("listKGwithPhotos", listKGwithPhotos);
        return "/kindergartens/index";
    }

    @GetMapping("/register")
    public String register(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
        }

        ParentInfo parentinfo = new ParentInfo();
        DirectorInfo dinfo = new DirectorInfo();
        model.addAttribute("parentinfo", parentinfo);
        model.addAttribute("dinfo", dinfo);
        model.addAttribute("currentuser", currentuser);

        return "/formulaire/registerparent";
    }

    @GetMapping("/login")
    public String log_in(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
        }
        model.addAttribute("currentuser", currentuser);
        return "/login";
    }

    @GetMapping("/accessdenied")
    public String accessDeniedPage(Principal principal, Model model) {
        Compte currentuser = null;
        if (principal != null) {
            String email = principal.getName();
            currentuser = cptrepo.findById(email).get();
        }
        model.addAttribute("currentuser", currentuser);
        return "/error/accessDenied";
    }

}
