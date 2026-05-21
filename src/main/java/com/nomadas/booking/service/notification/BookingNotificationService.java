package com.nomadas.booking.service.notification;

import com.nomadas.booking.model.Booking;

public interface BookingNotificationService {
    void sendBookingConfirmation(Booking booking);
}
