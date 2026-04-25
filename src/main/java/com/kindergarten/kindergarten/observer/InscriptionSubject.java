package com.kindergarten.kindergarten.observer;

import com.kindergarten.kindergarten.parent.Inscription;

public interface InscriptionSubject {

    void addObserver(InscriptionObserver observer);
    void removeObserver(InscriptionObserver observer);
    void notifyObservers(Inscription inscription);
}
