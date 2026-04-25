package com.kindergarten.kindergarten.observer;

import com.kindergarten.kindergarten.parent.Inscription;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KinderGartenSubject implements InscriptionSubject{

    private final List<InscriptionObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(InscriptionObserver observer){
        if(!observers.contains(observer)){
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(InscriptionObserver observer){
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Inscription inscription) {
        for (InscriptionObserver observer : observers){
            observer.OnNouvelleInscritpion(inscription);
        }
    }
}
