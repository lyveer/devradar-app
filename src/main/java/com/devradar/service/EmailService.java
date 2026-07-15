package com.devradar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) {
        String subject = "DevRadarAI E-posta Doğrulama Kodu";
        String text = "Merhaba,\n\nDevRadarAI platformuna kaydolduğunuz için teşekkür ederiz. E-posta adresinizi doğrulamak için aşağıdaki 6 haneli kodu kullanın:\n\n"
                + "Doğrulama Kodu: " + code + "\n\nBu kod 10 dakika geçerlidir.\n\nİyi çalışmalar,\nLyver Software Ekibi";

        sendEmail(toEmail, subject, text);
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        String subject = "DevRadarAI Şifre Sıfırlama Kodu";
        String text = "Merhaba,\n\nŞifrenizi sıfırlamak için bir talepte bulundunuz. Şifrenizi sıfırlamak için aşağıdaki 6 haneli doğrulama kodunu kullanın:\n\n"
                + "Şifre Sıfırlama Kodu: " + code + "\n\nBu kod 10 dakika geçerlidir. Eğer bu talebi siz yapmadıysanız lütfen bu e-postayı dikkate almayın.\n\nİyi çalışmalar,\nLyver Software Ekibi";

        sendEmail(toEmail, subject, text);
    }

    private void sendEmail(String toEmail, String subject, String text) {
        // Log to console first so that developers/testers can always see the code instantly
        System.out.println("\n=================================================================");
        System.out.println("E-POSTA GÖNDERİLİYOR (SIMULATION)");
        System.out.println("Alıcı: " + toEmail);
        System.out.println("Konu: " + subject);
        System.out.println("İçerik:\n" + text);
        System.out.println("=================================================================\n");

        if (mailSender != null && fromEmail != null && !fromEmail.isBlank()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
                log.info("E-posta başarıyla gönderildi: {}", toEmail);
            } catch (Exception e) {
                log.warn("E-posta gönderimi başarısız oldu (Simülasyon moduna devam ediliyor): {}", e.getMessage());
            }
        }
    }
}
