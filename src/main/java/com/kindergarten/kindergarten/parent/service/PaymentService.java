package com.kindergarten.kindergarten.parent.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kindergarten.kindergarten.parent.Inscription;
import com.kindergarten.kindergarten.parent.PayReference;
import com.kindergarten.kindergarten.parent.Payment;
import com.kindergarten.kindergarten.parent.PaymentRepo;

@Service
public class PaymentService implements OneShotPaymentProcessor, SelectedMonthsPaymentProcessor {

    private final PaymentRepo paymentRepo;

    public PaymentService(PaymentRepo paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    @Override
    public void processOneShotPayment(Inscription inscription, PayReference payReference, String today) {
        List<Payment> payments = paymentRepo.findByInscription(inscription);

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

    @Override
    public void processSelectedMonthsPayment(Inscription inscription, PayReference payReference, String today) {
        List<Payment> payments = paymentRepo.findByInscription(inscription);
        String[] idmonths = payReference.getMonths().split("@");

        Set<Integer> selectedMonths = new HashSet<>();
        for (String id : idmonths) {
            selectedMonths.add(Integer.parseInt(id.substring(2)));
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