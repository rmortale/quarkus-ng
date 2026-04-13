package ch.dulce.ng.listener;

import static ch.dulce.ng.routing.Router.ROUTING_EP;
import static ch.dulce.ng.templates.KafkaRouteTemplates.KAFKA_EVENT_LISTENER_TPL;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MinioListener extends RouteBuilder {

  @Override
  public void configure() throws Exception {

    // for every system separate topic and listener
    templatedRoute(KAFKA_EVENT_LISTENER_TPL)
        .parameter("topic", "minio-events-pri")
        .parameter("groupId", "avtGroup")
        .parameter("toEndpoint", ROUTING_EP);

    templatedRoute(KAFKA_EVENT_LISTENER_TPL)
        .parameter("topic", "minio-events-sec")
        .parameter("groupId", "vvzGroup")
        .parameter("toEndpoint", ROUTING_EP);

  }

}
