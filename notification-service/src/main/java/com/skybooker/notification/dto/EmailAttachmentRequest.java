package com.skybooker.notification.dto;

public record EmailAttachmentRequest(
        String fileName,
        String contentType,
        String contentBase64
) {
}
