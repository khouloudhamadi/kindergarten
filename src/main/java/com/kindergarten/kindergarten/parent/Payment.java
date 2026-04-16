package com.kindergarten.kindergarten.parent;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Inscription inscription;

    private Integer monthnumber;

    private Double montant_du;

    private String date_payment;
    private Double montant_percu;
    private String type_payment;
    private String reference_payment;

    public Payment() {
    }

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
     * @return Inscription return the inscription
     */
    public Inscription getInscription() {
        return inscription;
    }

    /**
     * @param inscription the inscription to set
     */
    public void setInscription(Inscription inscription) {
        this.inscription = inscription;
    }

    /**
     * @return Integer return the monthnumber
     */
    public Integer getMonthnumber() {
        return monthnumber;
    }

    /**
     * @param monthnumber the monthnumber to set
     */
    public void setMonthnumber(Integer monthnumber) {
        this.monthnumber = monthnumber;
    }

    /**
     * @return Double return the montant_du
     */
    public Double getMontant_du() {
        return montant_du;
    }

    /**
     * @param montant_du the montant_du to set
     */
    public void setMontant_du(Double montant_du) {
        this.montant_du = montant_du;
    }

    /**
     * @return String return the date_payment
     */
    public String getDate_payment() {
        return date_payment;
    }

    /**
     * @param date_payment the date_payment to set
     */
    public void setDate_payment(String date_payment) {
        this.date_payment = date_payment;
    }

    /**
     * @return Double return the montant_percu
     */
    public Double getMontant_percu() {
        return montant_percu;
    }

    /**
     * @param montant_percu the montant_percu to set
     */
    public void setMontant_percu(Double montant_percu) {
        this.montant_percu = montant_percu;
    }

    /**
     * @return String return the reference_payment
     */
    public String getReference_payment() {
        return reference_payment;
    }

    /**
     * @param reference_payment the reference_payment to set
     */
    public void setReference_payment(String reference_payment) {
        this.reference_payment = reference_payment;
    }

    /**
     * @return String return the type_payment
     */
    public String getType_payment() {
        return type_payment;
    }

    /**
     * @param type_payment the type_payment to set
     */
    public void setType_payment(String type_payment) {
        this.type_payment = type_payment;
    }

}
