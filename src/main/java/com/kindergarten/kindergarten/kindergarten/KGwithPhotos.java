package com.kindergarten.kindergarten.kindergarten;

import java.util.ArrayList;
import java.util.List;

import com.kindergarten.kindergarten.imgfiles.FileDB;

public class KGwithPhotos {
    private Integer id;
    private String nom;
    private String adresse;
    private String email;
    private String tel;
    private String directorfirstname;
    private String directorlastname;
    private List<FileDB> photos = new ArrayList<>();

    /**
     * @return Integer return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return String return the nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * @param nom the nom to set
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * @return String return the adresse
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * @param adresse the adresse to set
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /**
     * @return String return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return String return the tel
     */
    public String getTel() {
        return tel;
    }

    /**
     * @param tel the tel to set
     */
    public void setTel(String tel) {
        this.tel = tel;
    }

    /**
     * @return List<FileDB> return the photos
     */
    public List<FileDB> getPhotos() {
        return photos;
    }

    /**
     * @param photos the photos to set
     */
    public void setPhotos(List<FileDB> photos) {
        this.photos = photos;
    }

    /**
     * @return String return the directorfirstname
     */
    public String getDirectorfirstname() {
        return directorfirstname;
    }

    /**
     * @param directorfirstname the directorfirstname to set
     */
    public void setDirectorfirstname(String directorfirstname) {
        this.directorfirstname = directorfirstname;
    }

    /**
     * @return String return the directorlastname
     */
    public String getDirectorlastname() {
        return directorlastname;
    }

    /**
     * @param directorlastname the directorlastname to set
     */
    public void setDirectorlastname(String directorlastname) {
        this.directorlastname = directorlastname;
    }

}
