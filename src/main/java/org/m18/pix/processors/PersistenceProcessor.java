package org.m18.pix.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jsoup.Jsoup;
import org.m18.pix.models.entities.Image;
import org.apache.camel.component.aws.s3.S3Constants;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

/**
 * Fill the image object to persist the metadata
 *
 * @author Wolfram Huesken <woh@m18.io>
 */
@org.springframework.stereotype.Component
public class PersistenceProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Image image = new Image();
        String sender = exchange.getIn().getHeader("From", String.class);
        InternetAddress address = new InternetAddress(sender);

        image.setOriginalName(exchange.getIn().getHeader("OriginalName", String.class));
        image.setHash(exchange.getIn().getHeader(S3Constants.KEY, String.class));

        image.setSender(sender);
        image.setSenderEmail(address.getAddress());
        image.setSenderName(address.getPersonal());

        image.setDescription(removeHtmlTags(exchange.getIn().getBody(String.class).trim()));
        image.setTitle(removeHtmlTags(MimeUtility.decodeText(exchange.getIn().getHeader("Subject", String.class))));

        exchange.getIn().setBody(image);
    }

    private String removeHtmlTags(String title) {
        return Jsoup.parse(title).text();
    }

}
