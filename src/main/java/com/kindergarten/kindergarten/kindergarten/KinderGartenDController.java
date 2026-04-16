package com.kindergarten.kindergarten.kindergarten;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.kindergarten.kindergarten.compte.Compte;
import com.kindergarten.kindergarten.compte.CompteRepo;

import com.kindergarten.kindergarten.director.Director;
import com.kindergarten.kindergarten.director.DirectorRepo;
import com.kindergarten.kindergarten.imgfiles.FileDB;
import com.kindergarten.kindergarten.imgfiles.FileDBRepo;
import com.kindergarten.kindergarten.imgfiles.FileStorageService;
import com.kindergarten.kindergarten.parent.InscriptionRepo;

@Controller
public class KinderGartenDController {
    @Autowired
    private KinderGartenRepo repo;
    @Autowired
    private DirectorRepo directeurrepo;
    @Autowired
    private CompteRepo compterepo;
    @Autowired
    private FileStorageService storage;
    @Autowired
    private FileDBRepo filedbrepo;

    @Autowired
    private InscriptionRepo inscriptionrepo;

    @GetMapping("/director/kindergarten")
    public String listKinderGartens(Principal principal, Model m) {
        Director directeur = null;
        List<KinderGarten> listKG = null;
        List<KGwithPhotos> listKGwithPhotos = new ArrayList<>();
        if (principal != null) {
            String email = principal.getName();
            Compte cpt = compterepo.findById(email).get();
            if (cpt.getType().equals("Kindergarten Director")) {
                directeur = directeurrepo.findById(email).get();
                listKG = repo.findByDirector(directeur);
                for (KinderGarten kg : listKG) {
                    KGwithPhotos kgph = new KGwithPhotos();
                    kgph.setId(kg.getId());
                    kgph.setAdresse(kg.getAdresse());
                    kgph.setNom(kg.getNom());
                    kgph.setEmail(kg.getEmail());
                    kgph.setTel(kg.getTel());
                    kgph.setDirectorfirstname(directeur.getPrenom());
                    kgph.setDirectorlastname(directeur.getNom());
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
                Compte currentuser = null;

                currentuser = compterepo.findById(email).get();

                m.addAttribute("currentuser", currentuser);
                m.addAttribute("directeur", directeur);
                m.addAttribute("listKGwithPhotos", listKGwithPhotos);
                return "/director/kindergarten/index";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/director/kindergarten/new")
    public String showKGForm(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Compte cpt = compterepo.findById(email).get();
            Compte currentuser = null;
            currentuser = compterepo.findById(email).get();
            model.addAttribute("currentuser", currentuser);
            if (cpt.getType().equals("Kindergarten Director")) {
                Director directeur = directeurrepo.findById(email).get();
                model.addAttribute("directeur", directeur);
                return "/director/kindergarten/showKGForm";

            }
        }
        return "/error/accessDenied";

    }

    public void deletePhotos(Integer id) {
        KinderGarten kd = repo.findById(id).get();
        String[] photos = kd.getPhotos().split("@");
        if (photos.length > 0) {
            for (int i = 0; i < photos.length; i++) {
                filedbrepo.deleteById(photos[i]);
            }
        }
    }

    @PostMapping("/director/kindergarten/save")
    public String saveKg(Principal principal, @RequestParam("nom") String nom, @RequestParam("adresse") String adresse,
            @RequestParam("email") String email, @RequestParam("tel") String tel,
            @RequestParam("photos") MultipartFile[] photos) {
        if (principal != null) {
            KinderGarten kg = new KinderGarten();
            kg.setNom(nom);
            kg.setAdresse(adresse);
            kg.setEmail(email);
            kg.setTel(tel);
            String em = principal.getName();
            Compte cpt = compterepo.findById(em).get();
            if (cpt.getType().equals("Kindergarten Director")) {
                kg.setDirector(directeurrepo.findById(em).get());
                try {

                    List<String> fileNames = new ArrayList<>();
                    List<FileDB> lfdb = new ArrayList<>();
                    Arrays.asList(photos).stream().forEach(file -> {
                        try {
                            lfdb.add(storage.store(file));
                        } catch (IOException ioe) {

                        }
                        fileNames.add(file.getOriginalFilename());
                    });
                    String images = "";
                    if (lfdb.size() > 0) {
                        images = lfdb.get(0).getId();
                    }
                    for (int i = 1; i < lfdb.size(); i++) {
                        images = images + "@" + lfdb.get(i).getId();
                    }
                    kg.setPhotos(images);
                    repo.save(kg);
                    return "redirect:/director/kindergarten";
                } catch (Exception e) {
                    return "/error/erreur";
                }

            } else {
                return "/error/accessDenied";
            }

        } else {
            return "/error/accessDenied";
        }
    }

    @PostMapping("/director/kindergarten/edit/save")
    public String saveEditKg(Principal principal, @RequestParam("id") String id, @RequestParam("nom") String nom,
            @RequestParam("adresse") String adresse,
            @RequestParam("email") String email, @RequestParam("tel") String tel,
            @RequestParam("photos") MultipartFile[] photos) {
        if (principal != null) {
            String em = principal.getName();
            Compte cpt = compterepo.findById(em).get();
            if (cpt.getType().equals("Kindergarten Director")) {
                KinderGarten kg = repo.findById(Integer.parseInt(id)).get();
                deletePhotos(Integer.parseInt(id));
                kg.setNom(nom);
                kg.setAdresse(adresse);
                kg.setEmail(email);
                kg.setTel(tel);
                kg.setDirector(directeurrepo.findById(em).get());
                try {

                    List<String> fileNames = new ArrayList<>();
                    List<FileDB> lfdb = new ArrayList<>();
                    Arrays.asList(photos).stream().forEach(file -> {
                        try {
                            lfdb.add(storage.store(file));
                        } catch (IOException ioe) {

                        }
                        fileNames.add(file.getOriginalFilename());
                    });
                    String images = "";
                    if (lfdb.size() > 0) {
                        images = lfdb.get(0).getId();
                    }
                    for (int i = 1; i < lfdb.size(); i++) {
                        images = images + "@" + lfdb.get(i).getId();
                    }
                    kg.setPhotos(images);
                    repo.save(kg);
                    return "redirect:/director/kindergarten";
                } catch (Exception e) {
                    return "/error/erreur";
                }

            } else {
                return "/error/accessDenied";
            }

        } else {
            return "/error/accessDenied";
        }
    }

    @GetMapping("/director/kindergarten/delete/{id}")
    public String deleteKG(Principal principal, @PathVariable("id") Integer id, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Compte cpt = compterepo.findById(email).get();
            if (cpt.getType().equals("Kindergarten Director")) {
                Compte currentuser = compterepo.findById(email).get();
                KinderGarten kd = repo.findById(id).get();
                if (inscriptionrepo.existsByKindergartenAndValid(kd, true)) {
                    String message = "There is a validated Inscription related to this kinderGarten, So it could not be deleted";
                    model.addAttribute("message", message);
                    model.addAttribute("currentuser", currentuser);
                    return "/error/message";
                }
                inscriptionrepo.deleteByKindergarten(kd);

                if (kd.getPhotos().length() > 0) {
                    String[] photos = kd.getPhotos().split("@");
                    if (photos.length > 0) {
                        for (int i = 0; i < photos.length; i++) {
                            filedbrepo.deleteById(photos[i]);
                        }
                    }
                }
                repo.deleteById(id);
                return "redirect:/director/kindergarten";
            }
        }
        return "/error/accessDenied";
    }

    @GetMapping("/director/kindergarten/edit/{id}")
    public String editKG(Principal principal, @PathVariable("id") Integer id, Model model) {
        if (principal != null) {
            String email = principal.getName();
            Compte cpt = compterepo.findById(email).get();
            Compte currentuser = null;
            if (principal != null) {
                currentuser = compterepo.findById(email).get();
            }
            model.addAttribute("currentuser", currentuser);
            if (cpt.getType().equals("Kindergarten Director")) {
                Director directeur = directeurrepo.findById(email).get();
                KinderGarten kg = repo.findById(id).get();
                KGwithPhotos kgph = new KGwithPhotos();
                kgph.setId(kg.getId());
                kgph.setAdresse(kg.getAdresse());
                kgph.setNom(kg.getNom());
                kgph.setEmail(kg.getEmail());
                kgph.setTel(kg.getTel());
                kgph.setDirectorfirstname(directeur.getPrenom());
                kgph.setDirectorlastname(directeur.getNom());
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

                model.addAttribute("directeur", directeur);
                model.addAttribute("kgph", kgph);

                return "/director/kindergarten/showKGEditForm";

            } else {
                return "/error/accessDenied";
            }
        } else {
            return "/error/accessDenied";
        }
    }
}