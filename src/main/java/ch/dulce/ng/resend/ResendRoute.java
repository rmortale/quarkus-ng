package ch.dulce.ng.resend;

import ch.dulce.ng.dto.MinioEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;

import static ch.dulce.ng.routing.Router.ROUTING_EP;
import static ch.dulce.ng.routing.Router.STARTED_MESSAGE;
import static ch.dulce.ng.templates.KafkaRouteTemplates.MINIO_JSON_VARNAME;
import static ch.dulce.ng.templates.KafkaRouteTemplates.MINIO_OBJECT_VARNAME;

@ApplicationScoped
public class ResendRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {

    /**
     * Rest route to select events from db by tracingid and resend them to the routing.
     */
    rest("/resend")
        .post("/{tracingid}")
        .param().name("tracingid").type(RestParamType.path).dataType("string").endParam()
        .to("direct:resend");

    from("direct:resend")
        .routeId("resend-route")
        .setBody().simple("select event from ng_events where tracingid = :?tracingid and status = '" + STARTED_MESSAGE + "'").to("jdbc:default?useHeadersAsParameters=true")
        .split(body())
          .setBody().simple("${body.get('event')}")
          .setVariable(MINIO_JSON_VARNAME, body())
          .unmarshal().json(MinioEvent.class)
          .setVariable(MINIO_OBJECT_VARNAME, body())
          .to(ROUTING_EP)
        .end()
        .setBody().simple("Sent ${header.CamelJdbcRowCount} resend request(s) for tracingid ${header.tracingid}!");
  }

}
