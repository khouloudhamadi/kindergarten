package com.kindergarten.kindergarten.observer;

import com.kindergarten.kindergarten.parent.Inscription;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KinderGartenSubject implements InscriptionSubject {

    private final List<InscriptionObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(InscriptionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(InscriptionObserver observer) {
        observers.remove(observer);
    }

    @Override
public void notifyObservers(Inscription inscription) {
    for (InscriptionObserver observer : observers) {
        if (observer instanceof DirectorNotifier notifier) {
            if (notifier.getDirector() != null
                    && inscription.getKindergarten() != null
                    && inscription.getKindergarten().getDirector() != null
                    && notifier.getDirector().getEmail().equals(
                            inscription.getKindergarten().getDirector().getEmail())) {
                notifier.OnNouvelleInscritpion(inscription);
            }
        }
    }
}
}
