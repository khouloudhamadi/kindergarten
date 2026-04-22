package com.kindergarten.kindergarten.observer;

import com.kindergarten.kindergarten.director.Director;
import com.kindergarten.kindergarten.parent.Inscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


public class DirectorNotifier implements InscriptionObserver{

    private static final Logger logger  = LoggerFactory.getLogger(DirectorNotifier.class);
    private  Director director;
    private  JavaMailSender mailSender;

    public DirectorNotifier() {
    }

    // Constructor for manual instantiation
    public DirectorNotifier(Director director, JavaMailSender mailSender) {
        this.director = director;
        this.mailSender = mailSender;
    }

    @Override
    public void OnNouvelleInscritpion(Inscription inscription){
        String directorName = (director!=null)
                ? director.getPrenom()+" "+director.getNom()
                : "Director";

        String directorEmail = (director != null)? director.getEmail() : null;

        String enfantName = inscription.getEnfant() !=null
                ? inscription.getEnfant().getPrenom()+" "+ inscription.getEnfant().getNom()
                : "Enfant inconnu";

        String kgName = inscription.getKindergarten()!= null
                ? inscription.getKindergarten().getNom()
                : "KinderGarten inconnu";

        if(directorEmail != null && mailSender != null){
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("noreply@kindergarten.com");
                message.setTo(directorEmail);
                message.setSubject("Nouvelle insctiption -- "+kgName);
                message.setText(
                        "Bonjour " + directorName + ",\n\n" +
                                "Une nouvelle inscription a été soumise dans votre établissement.\n\n" +
                                "  Établissement : " + kgName + "\n" +
                                "  Enfant        : " + enfantName + "\n" +
                                "  Année scolaire: " + inscription.getAnneescolaire() + "\n" +
                                "  Classe        : " + inscription.getClass_level() + "\n\n" +
                                "Veuillez vous connecter pour valider ou refuser cette inscription.\n\n" +
                                "Cordialement,\nLe système KinderGarten"
                );
                logger.debug("Email message prepared. Sending to: {}", directorEmail);

                // Send email
                mailSender.send(message);

                logger.info("✓ Email successfully sent to director: {}", directorEmail);
                logger.info("=== Email notification completed successfully ===");

            } catch (org.springframework.mail.MailAuthenticationException e) {
                logger.error("✗ [AUTHENTICATION ERROR] Email credentials invalid or expired");
                logger.error("  - Check spring.mail.username and spring.mail.password in application.properties");
                logger.error("  - For Gmail: Use App Password, not regular password");
                logger.error("  - Exception: {}", e.getMessage(), e);

            } catch (org.springframework.mail.MailSendException e) {
                logger.error("✗ [SEND ERROR] Failed to send email via SMTP");
                logger.error("  - Check spring.mail.host and spring.mail.port in application.properties");
                logger.error("  - Verify network connectivity to SMTP server");
                logger.error("  - Verify firewall allows outbound SMTP (usually port 587 or 465)");
                logger.error("  - Exception: {}", e.getMessage(), e);

            } catch (org.springframework.mail.MailParseException e) {
                logger.error("✗ [PARSE ERROR] Invalid email address or message format");
                logger.error("  - Check director email: {}", directorEmail);
                logger.error("  - Check 'from' address in code");
                logger.error("  - Exception: {}", e.getMessage(), e);

            } catch (Exception e) {
                logger.error("✗ [UNEXPECTED ERROR] Unexpected exception sending email");
                logger.error("  - Exception type: {}", e.getClass().getName());
                logger.error("  - Message: {}", e.getMessage());
                logger.error("  - Full stacktrace: ", e);
            }
        }
    }
    public Director getDirector() {
        return director;
    }




}
