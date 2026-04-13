package ch.dulce.ng.sender;

import ch.dulce.ng.dto.MinioEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

import static ch.dulce.ng.routing.Router.S3_KAFKA_EP;


@ApplicationScoped
public class S3Listener extends RouteBuilder {

  @Override
  public void configure() throws Exception {

//    from(S3_KAFKA_EP + "?groupId=s3ConsumerGroup")
//        .unmarshal().json(MinioEvent.class)
//        .log("${body}")
//        .log("${headers}");
  }

}
