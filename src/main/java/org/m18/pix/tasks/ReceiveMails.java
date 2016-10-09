package org.m18.pix.tasks;

import com.amazonaws.auth.AWSCredentials;
import org.m18.pix.processors.MailProcessor;
import org.apache.camel.builder.RouteBuilder;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.stereotype.Component;
import org.m18.pix.processors.PersistenceProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <woh@m18.io>
 */
@Component
public class ReceiveMails extends RouteBuilder {

    @Value("${aws.accessKey}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    MailProcessor mailProcessor;
    PersistenceProcessor persistenceProcessor;

    @Autowired
    public ReceiveMails(MailProcessor mailProcessor, PersistenceProcessor persistenceProcessor) {
        this.mailProcessor = mailProcessor;
        this.persistenceProcessor = persistenceProcessor;
    }

    @Override
    public void configure() throws Exception {
        from("{{routes.ReceiveMails.imapConnection}}")
            .process(mailProcessor)
            .filter(body().isNotNull())
            .to("log:upload?showHeaders=true")
            .to("aws-s3://{{aws.bucketName}}?amazonS3Client=#client&region={{aws.region}}")
            .setBody(header("OriginalMailBody")).convertBodyTo(String.class)
            .process(persistenceProcessor)
            .to("jpa:Image?persistenceUnit=image-persistence");
    }

    @Bean()
    public AmazonS3Client client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return new AmazonS3Client(awsCredentials);
    }
}
