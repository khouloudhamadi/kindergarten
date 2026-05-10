package com.kindergarten.kindergarten.observer;

import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import com.kindergarten.kindergarten.kindergarten.KinderGartenRepo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ObserverConfig {

    private static final Logger logger = LoggerFactory.getLogger(ObserverConfig.class);

    @Autowired
    private KinderGartenSubject kinderGartenSubject;

    @Autowired
    private KinderGartenRepo kinderGartenRepo;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Enregistre un DirectorNotifier pour chaque KinderGarten
     * ayant un Director associé — exécuté au démarrage Spring.
     */
    @PostConstruct
    public void initObservers() {
        List<KinderGarten> allKG = (List<KinderGarten>) kinderGartenRepo.findAll();
        int count = 0;
        for (KinderGarten kg : allKG) {
            if (kg.getDirector() != null) {
                DirectorNotifier notifier = new DirectorNotifier(kg.getDirector(), mailSender);
                kinderGartenSubject.addObserver(notifier);
                count++;
                logger.info("[OBSERVER] Director '{}' abonné aux inscriptions de '{}'",
                        kg.getDirector().getEmail(), kg.getNom());
            }
        }
        logger.info("[OBSERVER] {} observer(s) enregistré(s) au démarrage", count);
    }
}
