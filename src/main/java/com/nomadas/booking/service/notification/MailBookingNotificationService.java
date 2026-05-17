package com.nomadas.booking.service.notification;

import com.nomadas.booking.model.Booking;
import com.nomadas.booking.model.Companion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class MailBookingNotificationService implements BookingNotificationService {

    private static final Logger log = LoggerFactory.getLogger(MailBookingNotificationService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public MailBookingNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@nomadas.local}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendBookingConfirmation(Booking booking) {
        String to = booking.getUser().getEmail();
        if (to == null || to.isBlank()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Confirmación de tu reserva con Nomadas");
        message.setText(buildBody(booking));

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Booking confirmation email could not be sent for booking {}: {}", booking.getId(), ex.getMessage());
        }
    }

    private String buildBody(Booking booking) {
        String companions = booking.getCompanions().stream()
                .map(this::formatCompanion)
                .collect(Collectors.joining("\n  - ", "  - ", ""));

        return """
                Hola %s,

                Tu reserva con Nomadas ha sido confirmada.

                Destino: %s
                Salida: %s
                Regreso: %s
                Régimen: %s
                Grupo: %s
                Total: %s €
                Descuento aplicado: %s €

                Acompañantes:
                %s

                Gracias por viajar con nosotros.
                """.formatted(
                booking.getUser().getFirstName(),
                booking.getTrip().getDestination(),
                booking.getTrip().getDepartureDate().format(DATE_FORMAT),
                booking.getTrip().getReturnDate().format(DATE_FORMAT),
                booking.getBoardType(),
                booking.getGroupType(),
                booking.getTotalPrice(),
                booking.getGroupDiscount(),
                companions
        );
    }

    private String formatCompanion(Companion companion) {
        return "%s %s (%s)".formatted(
                companion.getFirstName(),
                companion.getLastName(),
                companion.getBirthDate().format(DATE_FORMAT)
        );
    }
}
