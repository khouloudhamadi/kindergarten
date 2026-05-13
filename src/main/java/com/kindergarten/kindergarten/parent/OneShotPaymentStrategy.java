package com.kindergarten.kindergarten.parent;


import java.util.List;

import org.springframework.stereotype.Component;



@Component("ONE_SHOT")
public class OneShotPaymentStrategy implements PaymentStrategy {

    private final PaymentRepo paymentRepo;

    public OneShotPaymentStrategy(PaymentRepo paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public void pay(Inscription inscription, PayReference payReference, List<Payment> payments, String today) {
        for (Payment p : payments) {
            if (p.getMontant_percu() == null || p.getMontant_percu() == 0) {
                p.setDate_payment(today);
                p.setMontant_percu(p.getMontant_du());
                p.setReference_payment(payReference.getReference());
                p.setType_payment("Bank Card");
                paymentRepo.save(p);
            }
        }
    }
}