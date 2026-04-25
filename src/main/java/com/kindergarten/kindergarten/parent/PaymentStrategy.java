package com.kindergarten.kindergarten.parent;

import java.util.List;


public interface PaymentStrategy {
    void pay(Inscription inscription, PayReference payReference, List<Payment> payments, String today);
}