package org.m18.pix.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.m18.pix.models.entities.Image;
import org.apache.camel.component.aws.s3.S3Constants;

/**
 * Fill the image object to persist the metadata
 * @author Wolfram Huesken <woh@m18.io>
 */
@org.springframework.stereotype.Component
public class PersistenceProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Image image = new Image();
        image.setDescription(exchange.getIn().getBody(String.class).trim());
        image.setOriginalName(exchange.getIn().getHeader("OriginalName", String.class));
        image.setHash(exchange.getIn().getHeader(S3Constants.KEY, String.class));
        image.setSender(exchange.getIn().getHeader("From", String.class));
        image.setTitle(exchange.getIn().getHeader("Subject", String.class));

        exchange.getIn().setBody(image);
    }
}
