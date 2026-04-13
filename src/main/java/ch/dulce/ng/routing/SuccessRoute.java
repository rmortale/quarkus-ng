package ch.dulce.ng.routing;

import ch.dulce.ng.dto.MinioEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

import static ch.dulce.ng.routing.Router.SUCCESS_MESSAGE;
import static ch.dulce.ng.templates.KafkaRouteTemplates.*;

@ApplicationScoped
public class SuccessRoute extends RouteBuilder {

  private static final String SUCCESS_GROUP = "successGroup";
  public static final String SUCCESS_EP = "direct:success";
  public static final String SUCCESS_HANDLER_KAFKA_EP = "kafka:ng-success-events";
  private static final String INTERNAL_EP = "direct:internal_success";
  private static final String SUCCESS_TOPIC = "ng-success-events";

  @Override
  public void configure() throws Exception {

    from(SUCCESS_EP)
        .setBody(variable(MINIO_OBJECT_VARNAME))
        .process(ex -> {
          MinioEvent ev = ex.getMessage().getBody(MinioEvent.class);
          ev.setStatus(SUCCESS_MESSAGE);
          ex.getMessage().setBody(ev);
        })
        .marshal().json()
        .to(SUCCESS_HANDLER_KAFKA_EP);

    templatedRoute(KAFKA_EVENT_LISTENER_TPL)
        .parameter("topic", SUCCESS_TOPIC)
        .parameter("groupId", SUCCESS_GROUP)
        .parameter("toEndpoint", INTERNAL_EP);

    from(INTERNAL_EP)
        .setVariable("status", constant(SUCCESS_MESSAGE))
        .marshal().json()
        .setBody()
        .simple("INSERT INTO ng_events (tracingid, eventtime, status, event) VALUES('${variable.savedMinioObject.tracingId}', '${date-with-timezone:now:UTC:yyyy-MM-dd'T'HH:mm:ss.SSSZ}', '${variable.status}', '${body}')")
        .to("jdbc:default");

  }

}
