package ch.dulce.ng.listener;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

import static ch.dulce.ng.routing.Router.STARTED_MESSAGE;
import static ch.dulce.ng.templates.KafkaRouteTemplates.KAFKA_EVENT_LISTENER_TPL;


@ApplicationScoped
public class AuditListener extends RouteBuilder {

  private static final String AUDIT_EP = "direct:audit";
  private static final String AUDIT_GROUP = "auditGroup";

  @Override
  public void configure() throws Exception {

    templatedRoute(KAFKA_EVENT_LISTENER_TPL)
        .parameter("topic", "minio-events-pri,minio-events-sec")
        .parameter("groupId", AUDIT_GROUP)
        .parameter("toEndpoint", AUDIT_EP);

    from(AUDIT_EP)
        .setVariable("status", constant(STARTED_MESSAGE))
        .marshal().json()
        .setBody()
        .simple("INSERT INTO ng_events (tracingid, eventtime, status, event) VALUES('${variable.savedMinioObject.tracingId}', '${variable.savedMinioObject.eventTime}', '${variable.status}', '${body}')")
        .to("jdbc:default");
  }

}
