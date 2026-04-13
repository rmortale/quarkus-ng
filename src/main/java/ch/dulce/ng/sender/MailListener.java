package ch.dulce.ng.sender;

import ch.dulce.ng.dto.MinioEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

import static ch.dulce.ng.routing.Router.MAIL_KAFKA_EP;


@ApplicationScoped
public class MailListener extends RouteBuilder {

  @Override
  public void configure() throws Exception {

//    from(MAIL_KAFKA_EP + "?groupId=mailConsumerGroup")
//        .unmarshal().json(MinioEvent.class)
//        .log("${body}")
//        .log("${headers}");
  }

}
