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
import com.kindergarten.kindergarten.compte.RoleService;
import com.kindergarten.kindergarten.compte.RoleType;
import com.kindergarten.kindergarten.director.Director;
import com.kindergarten.kindergarten.director.DirectorRepo;
import com.kindergarten.kindergarten.imgfiles.FileDB;
import com.kindergarten.kindergarten.imgfiles.FileDBRepo;
import com.kindergarten.kindergarten.imgfiles.IFileStorageService;
import com.kindergarten.kindergarten.parent.InscriptionRepo;

@Controller
public class KinderGartenDController {

    @Autowired private KinderGartenRepo repo;
    @Autowired private DirectorRepo directeurrepo;
    @Autowired private CompteRepo compterepo;
    @Autowired private FileDBRepo filedbrepo;
    @Autowired private InscriptionRepo inscriptionrepo;
    @Autowired private RoleService roleService; // ✅ AJOUTÉ

    // ✅ DIP : dépendance sur l'interface uniquement
    @Autowired private IFileStorageService storage;

    @GetMapping("/director/kindergarten")
    public String listKinderGartens(Principal principal, Model m) {
        if (principal == null) return "/error/accessDenied";
        String email = principal.getName();
        Compte cpt = compterepo.findById(email).get();
        // ✅ RoleService au lieu de getType()
        if (!roleService.aLe(email, RoleType.ROLE_DIRECTOR)) return "/error/accessDenied";

        Director directeur = directeurrepo.findById(email).get();
        List<KinderGarten> listKG = repo.findByDirector(directeur);
        List<KGwithPhotos> listKGwithPhotos = new ArrayList<>();
        for (KinderGarten kg : listKG) {
            listKGwithPhotos.add(buildKGwithPhotos(kg, directeur));
        }

        m.addAttribute("currentuser", cpt);
        m.addAttribute("directeur", directeur);
        m.addAttribute("listKGwithPhotos", listKGwithPhotos);
        return "/director/kindergarten/index";
    }

    @GetMapping("/director/kindergarten/new")
    public String showKGForm(Principal principal, Model model) {
        if (principal == null) return "/error/accessDenied";
        String email = principal.getName();
        Compte cpt = compterepo.findById(email).get();
        if (!roleService.aLe(email, RoleType.ROLE_DIRECTOR)) return "/error/accessDenied";

        Director directeur = directeurrepo.findById(email).get();
        model.addAttribute("currentuser", cpt);
        model.addAttribute("directeur", directeur);
        return "/director/kindergarten/showKGForm";
    }

    @PostMapping("/director/kindergarten/save")
    public String saveKg(Principal principal,
            @RequestParam("nom") String nom,
            @RequestParam("adresse") String adresse,
            @RequestParam("email") String email,
            @RequestParam("tel") String tel,
            @RequestParam("photos") MultipartFile[] photos) {
        if (principal == null) return "/error/accessDenied";
        String em = principal.getName();
        if (!roleService.aLe(em, RoleType.ROLE_DIRECTOR)) return "/error/accessDenied";

        KinderGarten kg = new KinderGarten();
        kg.setNom(nom);
        kg.setAdresse(adresse);
        kg.setEmail(email);
        kg.setTel(tel);
        kg.setDirector(directeurrepo.findById(em).get());

        try {
            kg.setPhotos(storePhotos(photos));
            repo.save(kg);
            return "redirect:/director/kindergarten";
        } catch (Exception e) {
            return "/error/erreur";
        }
    }

    @PostMapping("/director/kindergarten/edit/save")
    public String saveEditKg(Principal principal,
            @RequestParam("id") String id,
            @RequestParam("nom") String nom,
            @RequestParam("adresse") String adresse,
            @RequestParam("email") String email,
            @RequestParam("tel") String tel,
            @RequestParam("photos") MultipartFile[] photos) {
        if (principal == null) return "/error/accessDenied";
        String em = principal.getName();
        if (!roleService.aLe(em, RoleType.ROLE_DIRECTOR)) return "/error/accessDenied";

        KinderGarten kg = repo.findById(Integer.parseInt(id)).get();
        deletePhotos(Integer.parseInt(id));
        kg.setNom(nom);
        kg.setAdresse(adresse);
        kg.setEmail(email);
        kg.setTel(tel);
        kg.setDirector(directeurrepo.findById(em).get());

        try {
            kg.setPhotos(storePhotos(photos));
            repo.save(kg);
            return "redirect:/director/kindergarten";
        } catch (Exception e) {
            return "/error/erreur";
        }
    }

    @GetMapping("/director/kindergarten/delete/{id}")
    public String deleteKG(Principal principal, @PathVariable Integer id, Model model) {
        if (principal == null) return "/error/accessDenied";
        String email = principal.getName();
        Compte cpt = compterepo.findById(email).get();
        if (!roleService.aLe(email, RoleType.ROLE_DIRECTOR)) return "/error/accessDenied";

        KinderGarten kd = repo.findById(id).get();
        if (inscriptionrepo.existsByKindergartenAndValid(kd, true)) {
            model.addAttribute("message",
                "There is a validated Inscription related to this KinderGarten, it could not be deleted");
            model.addAttribute("currentuser", cpt);
            return "/error/message";
        }
        inscriptionrepo.deleteByKindergarten(kd);
        deletePhotos(id);
        repo.deleteById(id);
        return "redirect:/director/kindergarten";
    }

    @GetMapping("/director/kindergarten/edit/{id}")
    public String editKG(Principal principal, @PathVariable Integer id, Model model) {
        if (principal == null) return "/error/accessDenied";
        String email = principal.getName();
        Compte cpt = compterepo.findById(email).get();
        if (!roleService.aLe(email, RoleType.ROLE_DIRECTOR)) return "/error/accessDenied";

        Director directeur = directeurrepo.findById(email).get();
        KinderGarten kg = repo.findById(id).get();
        KGwithPhotos kgph = buildKGwithPhotos(kg, directeur);

        model.addAttribute("currentuser", cpt);
        model.addAttribute("directeur", directeur);
        model.addAttribute("kgph", kgph);
        return "/director/kindergarten/showKGEditForm";
    }

    private String storePhotos(MultipartFile[] photos) throws IOException {
        List<FileDB> lfdb = new ArrayList<>();
        Arrays.asList(photos).forEach(file -> {
            try {
                lfdb.add(storage.store(file));
            } catch (IOException e) { }
        });
        if (lfdb.isEmpty()) return "";
        StringBuilder images = new StringBuilder(lfdb.get(0).getId());
        for (int i = 1; i < lfdb.size(); i++) {
            images.append("@").append(lfdb.get(i).getId());
        }
        return images.toString();
    }

    private void deletePhotos(Integer id) {
        KinderGarten kd = repo.findById(id).get();
        if (kd.getPhotos() != null && kd.getPhotos().length() > 0) {
            for (String photoId : kd.getPhotos().split("@")) {
                filedbrepo.deleteById(photoId);
            }
        }
    }

    private KGwithPhotos buildKGwithPhotos(KinderGarten kg, Director directeur) {
        KGwithPhotos kgph = new KGwithPhotos();
        kgph.setId(kg.getId());
        kgph.setAdresse(kg.getAdresse());
        kgph.setNom(kg.getNom());
        kgph.setEmail(kg.getEmail());
        kgph.setTel(kg.getTel());
        kgph.setDirectorfirstname(directeur.getPrenom());
        kgph.setDirectorlastname(directeur.getNom());
        if (kg.getPhotos() != null && kg.getPhotos().length() > 0) {
            List<FileDB> lfdb = new ArrayList<>();
            for (String photoId : kg.getPhotos().split("@")) {
                FileDB fdb = storage.getFile(photoId);
                fdb.setDataB64(Base64.getEncoder().encodeToString(fdb.getData()));
                lfdb.add(fdb);
            }
            kgph.setPhotos(lfdb);
        }
        return kgph;
    }
}
