package org.m18.pix.processors;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
import java.io.IOException;
import java.security.MessageDigest;
import javax.activation.DataHandler;
import org.apache.camel.component.aws.s3.S3Constants;

/**
 * Extract biggest image from attachments and set the stream as the body for S3
 * @author Wolfram Huesken <woh@m18.io>
 */
@Slf4j
@org.springframework.stereotype.Component
public class MailProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Map<String, DataHandler> attachments = exchange.getIn().getAttachments();
        String subject = exchange.getIn().getHeader("Subject", String.class);
        String sender = exchange.getIn().getHeader("From", String.class);

        if (sender == null) {
            throw new RuntimeException("No sender found... Weird!");
        }

        log.info("New Mail from " + sender);

        if (subject == null || subject.equals("")) {
            subject = "[ Bildermaschine ]";
        }
        exchange.getIn().setHeader("Subject", subject);

        if (attachments.size() == 0) {
            exchange.getIn().setBody(null);
            return;
        }

        DataHandler image = getBiggestImage(attachments, exchange);
        if (image == null) {
            exchange.getIn().setBody(null);
            return;
        }

        byte[] data = exchange.getContext().getTypeConverter().convertTo(
            byte[].class,
            image.getInputStream()
        );

        exchange.getIn().setHeader(S3Constants.KEY, getHexString(MessageDigest.getInstance("MD5").digest(data)));
        exchange.getIn().setHeader(S3Constants.CONTENT_LENGTH, data.length);

        // Store original Body
        exchange.getIn().setHeader("OriginalMailBody", exchange.getIn().getBody());
        exchange.getIn().setBody(exchange.getContext().getTypeConverter().convertTo(
            byte[].class, image.getInputStream()
        ));
    }

    private boolean isImage(String contentType) {
        if (contentType == null || contentType.length() < 5 || !contentType.substring(0, 5).equals("image")) {
            return false;
        }

        return true;
    }

    private String getHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private DataHandler getBiggestImage(Map<String, DataHandler> attachments, Exchange exchange) {
        String name = "";
        int size = 0;
        byte[] data;

        for (Map.Entry<String, DataHandler> entry : attachments.entrySet()) {
            if (isImage(entry.getValue().getContentType())) {
                try {
                    data = exchange.getContext().getTypeConverter().convertTo(
                        byte[].class,
                        entry.getValue().getInputStream()
                    );

                    if (data.length > size) {
                        size = data.length;
                        name = entry.getKey();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (attachments.containsKey(name)) {
            exchange.getIn().setHeader("OriginalName", name);
            return attachments.get(name);
        }

        return null;
    }
}
