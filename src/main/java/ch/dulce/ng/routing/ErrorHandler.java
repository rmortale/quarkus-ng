package ch.dulce.ng.routing;


import ch.dulce.ng.dto.MinioEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import static ch.dulce.ng.routing.Router.*;
import static ch.dulce.ng.templates.KafkaRouteTemplates.KAFKA_EVENT_LISTENER_TPL;

@ApplicationScoped
public class ErrorHandler extends RouteBuilder {

  private static final String ERRORS_EP = "direct:errors";
  private static final String ERRORS_GROUP = "errorGroup";
  private static final String ERRORS_TOPIC = "ng-error-events";

  @Override
  public void configure() throws Exception {

    // errors are processed first by this route and sent to kafka topic
    from(ERROR_HANDLER_EP)
        .routeId("error-handler-route")
        .log(LoggingLevel.ERROR,"Transfer failed for file ${variable.savedMinioObject.filename}. ${exception.message}")
        //.to("log:error?showAll=true&multiline=true")
        .unmarshal().json(MinioEvent.class)
        .process(ex -> {
          MinioEvent ev = ex.getMessage().getBody(MinioEvent.class);
          ev.setStatus(FAILURE_MESSAGE);
          ev.setExceptionCaught(ex.getProperty(ExchangePropertyKey.EXCEPTION_CAUGHT, String.class));
          ev.setExceptionEndpoint(ex.getProperty(ExchangePropertyKey.FAILURE_ENDPOINT, String.class));
          ev.setFailureRouteId(ex.getProperty(ExchangePropertyKey.FAILURE_ROUTE_ID, String.class));
          ex.getMessage().setBody(ev);
        })
        .marshal().json()
        .to(ERROR_HANDLER_KAFKA_EP);

    // kafka consumer routes for error processing
    templatedRoute(KAFKA_EVENT_LISTENER_TPL)
        .parameter("topic", ERRORS_TOPIC)
        .parameter("groupId", ERRORS_GROUP)
        .parameter("toEndpoint", ERRORS_EP);

    // save error to events table
    from(ERRORS_EP)
        .setVariable("status", constant(FAILURE_MESSAGE))
        .marshal().json()
        .setBody().simple("INSERT INTO ng_events (tracingid, eventtime, status, event) VALUES('${variable.savedMinioObject.tracingId}', '${date-with-timezone:now:UTC:yyyy-MM-dd'T'HH:mm:ss.SSSZ}', '${variable.status}', '${body}')").to("jdbc:default");
  }

}
