package com.kindergarten.kindergarten.parent.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kindergarten.kindergarten.parent.PayReference;
import com.kindergarten.kindergarten.parent.Payment;

@Service
public class PaymentValidationService {

    public void validateBeforePayment(PayReference payReference, List<Payment> payments) {
        validatePayReference(payReference);
        validatePaymentsStructure(payments);

        if ("SELECTED_MONTHS".equals(payReference.getPaymentStrategy())) {
            validateSelectedMonths(payReference, payments);
        } else if ("ONE_SHOT".equals(payReference.getPaymentStrategy())) {
            validateOneShot(payments);
        }
    }

    private void validatePayReference(PayReference payReference) {
        if (payReference == null) {
            throw new BusinessException("La demande de paiement est obligatoire");
        }

        if (payReference.getIdinsc() == null) {
            throw new BusinessException("L'inscription est obligatoire");
        }

        if (payReference.getReference() == null || payReference.getReference().trim().isEmpty()) {
            throw new BusinessException("La référence de paiement est obligatoire");
        }

        if (payReference.getPaymentStrategy() == null || payReference.getPaymentStrategy().trim().isEmpty()) {
            throw new BusinessException("La stratégie de paiement est obligatoire");
        }

        if (!"ONE_SHOT".equals(payReference.getPaymentStrategy())
                && !"SELECTED_MONTHS".equals(payReference.getPaymentStrategy())) {
            throw new BusinessException("La stratégie de paiement est invalide");
        }
    }

    private void validatePaymentsStructure(List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            throw new BusinessException("Aucun échéancier de paiement trouvé");
        }

        for (Payment p : payments) {
            if (p.getMonthnumber() == null || p.getMonthnumber() < 1 || p.getMonthnumber() > 12) {
                throw new BusinessException("Le numéro du mois doit être compris entre 1 et 12");
            }

            if (p.getMontant_du() == null || p.getMontant_du() < 0) {
                throw new BusinessException("Le montant dû est invalide pour le mois " + p.getMonthnumber());
            }

            if (p.getMontant_percu() != null) {
                if (p.getMontant_percu() < 0) {
                    throw new BusinessException("Le montant perçu ne peut pas être négatif");
                }

                if (p.getMontant_percu() > p.getMontant_du()) {
                    throw new BusinessException("Le montant perçu ne peut pas dépasser le montant dû");
                }

                if (p.getMontant_percu() > 0) {
                    if (p.getDate_payment() == null || p.getDate_payment().trim().isEmpty()) {
                        throw new BusinessException("La date de paiement est obligatoire si un paiement existe");
                    }

                    if (p.getReference_payment() == null || p.getReference_payment().trim().isEmpty()) {
                        throw new BusinessException("La référence est obligatoire si un paiement existe");
                    }

                    if (p.getType_payment() == null || p.getType_payment().trim().isEmpty()) {
                        throw new BusinessException("Le type de paiement est obligatoire si un paiement existe");
                    }
                }
            }
        }
    }

    private void validateSelectedMonths(PayReference payReference, List<Payment> payments) {
        if (payReference.getMonths() == null || payReference.getMonths().trim().isEmpty()) {
            throw new BusinessException("Les mois sélectionnés sont obligatoires");
        }

        if (payReference.getAmountpermonth() == null || payReference.getAmountpermonth() <= 0) {
            throw new BusinessException("Le montant par mois doit être strictement positif");
        }

        String[] ids = payReference.getMonths().split("@");
        Set<Integer> selectedMonths = new HashSet<>();

        for (String id : ids) {
            if (id == null || id.trim().isEmpty() || !id.startsWith("id")) {
                throw new BusinessException("Format des mois sélectionnés invalide");
            }

            Integer monthNumber;
            try {
                monthNumber = Integer.parseInt(id.substring(2));
            } catch (Exception e) {
                throw new BusinessException("Format du mois sélectionné invalide : " + id);
            }

            if (monthNumber < 1 || monthNumber > 12) {
                throw new BusinessException("Le mois sélectionné est invalide : " + monthNumber);
            }

            selectedMonths.add(monthNumber);
        }

        if (selectedMonths.isEmpty()) {
            throw new BusinessException("Aucun mois sélectionné");
        }

        List<Integer> ordered = new ArrayList<>(selectedMonths);
        ordered.sort(Integer::compareTo);

        for (int i = 1; i < ordered.size(); i++) {
            if (ordered.get(i) != ordered.get(i - 1) + 1) {
                throw new BusinessException("Les mois sélectionnés doivent être consécutifs");
            }
        }

        boolean firstGapFound = false;
        for (Payment p : payments) {
            boolean unpaid = p.getMontant_percu() == null || p.getMontant_percu() == 0;

            if (unpaid && !selectedMonths.contains(p.getMonthnumber()) && !selectedMonths.isEmpty()) {
                firstGapFound = true;
            }

            if (selectedMonths.contains(p.getMonthnumber())) {
                if (p.getMontant_percu() != null && p.getMontant_percu() > 0) {
                    throw new BusinessException("Le mois " + p.getMonthnumber() + " est déjà payé");
                }

                if (payReference.getAmountpermonth() > p.getMontant_du()) {
                    throw new BusinessException(
                            "Le montant perçu ne peut pas dépasser le montant dû du mois " + p.getMonthnumber());
                }

                if (firstGapFound) {
                    throw new BusinessException("Vous devez payer les mois précédents d'abord");
                }
            }
        }
    }

    private void validateOneShot(List<Payment> payments) {
        boolean hasUnpaid = false;

        for (Payment p : payments) {
            boolean unpaid = p.getMontant_percu() == null || p.getMontant_percu() == 0;
            if (unpaid) {
                hasUnpaid = true;
            }
        }

        if (!hasUnpaid) {
            throw new BusinessException("Aucun mois impayé à régler");
        }
    }

    public void validateAfterPayment(List<Payment> payments) {
        for (Payment p : payments) {
            if (p.getMontant_percu() == null) {
                continue;
            }

            if (p.getMontant_percu() < 0 || p.getMontant_percu() > p.getMontant_du()) {
                throw new BusinessException("Montant perçu invalide après traitement");
            }

            if (p.getMontant_percu() > 0) {
                if (p.getDate_payment() == null || p.getDate_payment().trim().isEmpty()) {
                    throw new BusinessException("Date de paiement absente après traitement");
                }
                if (p.getReference_payment() == null || p.getReference_payment().trim().isEmpty()) {
                    throw new BusinessException("Référence absente après traitement");
                }
                if (p.getType_payment() == null || p.getType_payment().trim().isEmpty()) {
                    throw new BusinessException("Type de paiement absent après traitement");
                }
            }
        }
    }
}