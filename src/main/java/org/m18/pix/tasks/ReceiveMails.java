package org.m18.pix.tasks;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.digest.Md5Crypt;
import org.m18.pix.processors.MailProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
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

    @Autowired
    MailProcessor mailProcessor;

    @Override
    public void configure() throws Exception {

        from("{{routes.ReceiveMails.imapConnection}}")
            .process(mailProcessor)
            .filter(body().isNotNull())
            .to("log:upload?showHeaders=true")
            .to("aws-s3://{{aws.bucketName}}?amazonS3Client=#client&region={{aws.region}}");

    }

    @Bean()
    public AmazonS3Client client() {
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return new AmazonS3Client(awsCredentials);
    }
}
