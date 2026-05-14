package com.skybooker.notification.service;

import com.skybooker.notification.dto.BookingConfirmationRequest;
import com.skybooker.notification.dto.BulkNotificationRequest;
import com.skybooker.notification.dto.EmailAttachmentRequest;
import com.skybooker.notification.dto.NotificationRequest;
import com.skybooker.notification.dto.NotificationResponse;
import com.skybooker.notification.entity.Notification;
import com.skybooker.notification.entity.NotificationChannel;
import com.skybooker.notification.entity.NotificationType;
import com.skybooker.notification.exception.ResourceNotFoundException;
import com.skybooker.notification.repository.NotificationRepository;
import com.skybooker.notification.service.NotifService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotifServiceImpl implements NotifService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender javaMailSender;
    private final String fromEmail;

    public NotifServiceImpl(NotificationRepository notificationRepository,
                            JavaMailSender javaMailSender,
                            @Value("${application.notification.email.from}") String fromEmail) {
        this.notificationRepository = notificationRepository;
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public NotificationResponse send(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setRecipientId(request.recipientId());
        notification.setType(request.type());
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setChannel(request.channel());
        notification.setRelatedBookingId(request.relatedBookingId());

        Notification saved = notificationRepository.save(notification);
        return map(saved);
    }

    @Override
    public List<NotificationResponse> sendBulk(BulkNotificationRequest request) {
        List<NotificationResponse> responses = new ArrayList<>();

        for (UUID recipientId : request.recipientIds()) {
            NotificationRequest singleRequest = new NotificationRequest(
                    recipientId,
                    request.type(),
                    request.title(),
                    request.message(),
                    request.channel(),
                    request.relatedBookingId()
            );
            responses.add(send(singleRequest));
        }

        return responses;
    }

    @Override
    public List<NotificationResponse> sendBookingConfirmation(BookingConfirmationRequest request) {
        List<NotificationResponse> responses = new ArrayList<>();

        String appMessage = "Your booking is confirmed. PNR: " + request.pnrCode();
        String emailMessage = buildBookingConfirmationEmail(request.pnrCode());
        String smsMessage = "SkyBooker booking confirmed. PNR: " + request.pnrCode();

        NotificationResponse appNotification = send(
                new NotificationRequest(
                        request.recipientId(),
                        NotificationType.BOOKING_CONFIRMED,
                        "Booking Confirmed",
                        appMessage,
                        NotificationChannel.APP,
                        request.relatedBookingId()
                )
        );
        responses.add(appNotification);

        sendEmail(
                request.recipientEmail(),
                "Your SkyBooker booking is confirmed - " + request.pnrCode(),
                emailMessage,
                request.attachments()
        );
        NotificationResponse emailNotification = send(
                new NotificationRequest(
                        request.recipientId(),
                        NotificationType.BOOKING_CONFIRMED,
                        "Booking Confirmation Email",
                        emailMessage,
                        NotificationChannel.EMAIL,
                        request.relatedBookingId()
                )
        );
        responses.add(emailNotification);

        sendSMS(request.recipientPhone(), smsMessage);
        NotificationResponse smsNotification = send(
                new NotificationRequest(
                        request.recipientId(),
                        NotificationType.BOOKING_CONFIRMED,
                        "Booking Confirmation SMS",
                        smsMessage,
                        NotificationChannel.SMS,
                        request.relatedBookingId()
                )
        );
        responses.add(smsNotification);

        return responses;
    }

    @Override
    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = findNotification(notificationId);
        notification.setIsRead(true);
        return map(notificationRepository.save(notification));
    }

    @Override
    public List<NotificationResponse> markAllRead(UUID recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsRead(recipientId, false);

        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }

        return notificationRepository.saveAll(notifications)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByRecipient(UUID recipientId) {
        return notificationRepository.findByRecipientId(recipientId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public void deleteNotification(UUID notificationId) {
        findNotification(notificationId);
        notificationRepository.deleteByNotificationId(notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByType(NotificationType type) {
        return notificationRepository.findByType(type)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public void sendEmail(String toEmail, String subject, String body, List<EmailAttachmentRequest> attachments) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            if (attachments != null) {
                for (EmailAttachmentRequest attachment : attachments) {
                    if (attachment == null || attachment.contentBase64() == null || attachment.contentBase64().isBlank()) {
                        continue;
                    }

                    byte[] fileBytes = Base64.getDecoder().decode(attachment.contentBase64());
                    helper.addAttachment(
                            attachment.fileName() == null || attachment.fileName().isBlank() ? "travel-document.pdf" : attachment.fileName(),
                            new ByteArrayResource(fileBytes),
                            attachment.contentType() == null || attachment.contentType().isBlank() ? "application/pdf" : attachment.contentType()
                    );
                }
            }

            javaMailSender.send(message);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }

    @Override
    public void sendSMS(String toPhone, String message) {
        System.out.println("SMS SENT TO: " + toPhone);
        System.out.println("MESSAGE: " + message);
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void sendCheckInReminders() {
        System.out.println("Hourly check-in reminder scheduler triggered");
    }

    private Notification findNotification(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    private NotificationResponse map(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getRecipientId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getChannel(),
                notification.getRelatedBookingId(),
                notification.getIsRead(),
                notification.getSentAt()
        );
    }

    private String buildBookingConfirmationEmail(String pnrCode) {
        return """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #162236; line-height: 1.6;">
                    <h2 style="margin-bottom: 8px;">Thank you for booking with SkyBooker</h2>
                    <p style="margin-top: 0;">Your trip is confirmed and ready to travel.</p>
                    <p><strong>PNR:</strong> %s</p>
                    <p>We have attached your travel documents to this email:</p>
                    <ul>
                      <li>E-ticket</li>
                      <li>Payment receipt</li>
                      <li>Boarding pass</li>
                    </ul>
                    <p>We wish you a smooth and comfortable journey.</p>
                    <p>SkyBooker Team</p>
                  </body>
                </html>
                """.formatted(pnrCode);
    }
}
