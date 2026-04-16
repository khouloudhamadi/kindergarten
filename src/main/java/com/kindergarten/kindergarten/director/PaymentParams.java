package com.kindergarten.kindergarten.director;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.kindergarten.kindergarten.kindergarten.KinderGarten;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class PaymentParams {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String anneescol;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private KinderGarten kindergarten;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Director director;

    private String classLevel;
    private double price = 0;
    private Integer nbMonths = 10;
    private Integer startMonth = 10;

    public PaymentParams() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return String return the anneeescol
     */
    public String getAnneescol() {
        return anneescol;
    }

    /**
     * @param anneescol the anneescol to set
     */
    public void setAnneescol(String anneescol) {
        this.anneescol = anneescol;
    }

    /**
     * @return KinderGarten return the kindergarten
     */
    public KinderGarten getKindergarten() {
        return kindergarten;
    }

    /**
     * @param kindergarten the kindergarten to set
     */
    public void setKindergarten(KinderGarten kindergarten) {
        this.kindergarten = kindergarten;
    }

    /**
     * @return Director return the director
     */
    public Director getDirector() {
        return director;
    }

    /**
     * @param director the director to set
     */
    public void setDirector(Director director) {
        this.director = director;
    }

    /**
     * @return String return the classLevel
     */
    public String getClassLevel() {
        return classLevel;
    }

    /**
     * @param classLevel the classLevel to set
     */
    public void setClassLevel(String classLevel) {
        this.classLevel = classLevel;
    }

    /**
     * @return double return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * @return Integer return the nbMonths
     */
    public Integer getNbMonths() {
        return nbMonths;
    }

    /**
     * @param nbMonths the nbMonths to set
     */
    public void setNbMonths(Integer nbMonths) {
        this.nbMonths = nbMonths;
    }

    /**
     * @return Integer return the startMonth
     */
    public Integer getStartMonth() {
        return startMonth;
    }

    /**
     * @param startMonth the startMonth to set
     */
    public void setStartMonth(Integer startMonth) {
        this.startMonth = startMonth;
    }

}