package pe.upc.simutalk.recruitment.domain.services;

import pe.upc.simutalk.recruitment.domain.model.valueobjects.CandidateNotification;

/**
 * Port to the e-mail provider. Adapters live in infrastructure/external/mail. Sending is best effort: a
 * failure is logged and never interrupts the operation that triggered the notification.
 */
public interface NotificationService {

    void send(CandidateNotification notification);
}
