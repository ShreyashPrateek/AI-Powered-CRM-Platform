package com.crm.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailSender {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Renders a Thymeleaf template and sends it via SMTP.
     *
     * @param to          recipient address
     * @param subject     email subject
     * @param templateKey Thymeleaf template name (without .html)
     * @param variables   template variables
     * @throws MessagingException on SMTP failure
     */
    public void send(String to, String subject, String templateKey, Map<String, Object> variables)
        throws MessagingException {

        Context ctx = new Context();
        ctx.setVariables(variables);
        String html = templateEngine.process(templateKey, ctx);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
        log.debug("Email sent to={} subject={}", to, subject);
    }
}
