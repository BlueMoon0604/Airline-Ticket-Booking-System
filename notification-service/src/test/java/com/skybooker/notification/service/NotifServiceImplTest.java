package com.skybooker.notification.service;

import com.skybooker.notification.dto.BookingConfirmationRequest;
import com.skybooker.notification.dto.EmailAttachmentRequest;
import com.skybooker.notification.entity.Notification;
import com.skybooker.notification.repository.NotificationRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotifServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private JavaMailSender javaMailSender;

    private NotifServiceImpl notifService;

    private UUID recipientId;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        notifService = new NotifServiceImpl(notificationRepository, javaMailSender, "support@skybooker.com");
        recipientId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
    }

    @Test
    void sendBookingConfirmationShouldCreateThreeNotificationsAndSendEmail() {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getNotificationId() == null) {
                notification.setNotificationId(UUID.randomUUID());
            }
            return notification;
        });

        String fakePdf = Base64.getEncoder().encodeToString("pdf-content".getBytes());
        BookingConfirmationRequest request = new BookingConfirmationRequest(
                recipientId,
                bookingId,
                "user@example.com",
                "9876543210",
                "ABC123",
                List.of(new EmailAttachmentRequest("e-ticket.pdf", "application/pdf", fakePdf))
        );

        var responses = notifService.sendBookingConfirmation(request);

        assertThat(responses).hasSize(3);
        verify(notificationRepository, times(3)).save(any(Notification.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void markAsReadShouldPersistUpdatedNotification() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setNotificationId(notificationId);
        notification.setIsRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = notifService.markAsRead(notificationId);

        assertThat(response.isRead()).isTrue();
    }
}
