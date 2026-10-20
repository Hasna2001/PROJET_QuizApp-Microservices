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

    public void envoyerResultatQuiz(UserWrapper user, Integer quizId, Integer score, Integer points) throws Exception {
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
}
