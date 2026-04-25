package com.kindergarten.kindergarten.parent.service;

import com.kindergarten.kindergarten.parent.Inscription;
import com.kindergarten.kindergarten.parent.PayReference;

public interface OneShotPaymentProcessor {
    void processOneShotPayment(Inscription inscription, PayReference payReference, String today);
}