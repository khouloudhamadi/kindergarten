package com.kindergarten.kindergarten.parent;



import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;



@Component("SELECTED_MONTHS")
public class SelectedMonthsPaymentStrategy implements PaymentStrategy {

    private final PaymentRepo paymentRepo;

    public SelectedMonthsPaymentStrategy(PaymentRepo paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public void pay(Inscription inscription, PayReference payReference, List<Payment> payments, String today) {
        if (payReference.getMonths() == null || payReference.getMonths().isEmpty()) {
            throw new IllegalArgumentException("Aucun mois sélectionné");
        }

        String[] idmonths = payReference.getMonths().split("@");
        Set<Integer> selectedMonths = new HashSet<>();

        for (String value : idmonths) {
            selectedMonths.add(Integer.parseInt(value.substring(2)));
        }

        for (Payment p : payments) {
            if (selectedMonths.contains(p.getMonthnumber())) {
                p.setDate_payment(today);
                p.setMontant_percu(payReference.getAmountpermonth());
                p.setReference_payment(payReference.getReference());
                p.setType_payment("Bank Card");
                paymentRepo.save(p);
            }
        }
    }
}