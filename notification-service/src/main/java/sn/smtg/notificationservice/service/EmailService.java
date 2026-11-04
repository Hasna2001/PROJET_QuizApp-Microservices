package sn.smtg.notificationservice.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sn.smtg.notificationservice.dto.UserWrapper;

@Service
public class EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    TemplateEngine templateEngine;

    @Value("${mail.expediteur}")
    private String expediteur;

    @Value("${spring.mail.username:}")
    private String adresseExpediteur;

    @Value("${spring.mail.password:}")
    private String motDePasseApplication;

    public void envoyerResultatQuiz(UserWrapper user, Integer quizId, Integer score, Integer points) throws Exception {
        if (adresseExpediteur.isBlank() || motDePasseApplication.isBlank()) {
            throw new IllegalStateException(
                    "Configurez GMAIL_USERNAME et GMAIL_APP_PASSWORD dans .env. "
                            + "Pour Gmail, utilisez un mot de passe d'application, pas celui du compte.");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("L'utilisateur ne possède pas d'adresse e-mail.");
        }

        Context context = new Context();
        context.setVariable("nom", user.getNom());
        context.setVariable("prenom", user.getPrenom());
        context.setVariable("quizId", quizId);
        context.setVariable("score", score);
        context.setVariable("points", points);

        String contenuHtml = templateEngine.process("email-quiz-resultat", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(expediteur);
        helper.setTo(user.getEmail());
        helper.setSubject("Résultats de votre Quiz #" + quizId);
        helper.setText(contenuHtml, true);

        mailSender.send(message);
    }

    public void envoyerLienReinitialisation(String email, String resetUrl) throws Exception {
        if (adresseExpediteur.isBlank() || motDePasseApplication.isBlank()) {
            throw new IllegalStateException(
                    "Configurez GMAIL_USERNAME et GMAIL_APP_PASSWORD dans .env. "
                            + "Pour Gmail, utilisez un mot de passe d'application, pas celui du compte.");
        }
        if (email == null || email.isBlank() || resetUrl == null || resetUrl.isBlank()) {
            throw new IllegalArgumentException("Adresse e-mail ou lien de réinitialisation manquant.");
        }

        Context context = new Context();
        context.setVariable("resetUrl", resetUrl);
        String contenuHtml = templateEngine.process("email-reinitialisation-mot-de-passe", context);
        String contenuTexte = "Pour choisir un nouveau mot de passe QuizApp, ouvrez ce lien (valable 30 minutes) : "
                + resetUrl + "\nSi vous n'êtes pas à l'origine de cette demande, ignorez ce message.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(expediteur);
        helper.setTo(email);
        helper.setSubject("Réinitialisation du mot de passe QuizApp");
        helper.setText(contenuTexte, contenuHtml);

        mailSender.send(message);
    }
}
