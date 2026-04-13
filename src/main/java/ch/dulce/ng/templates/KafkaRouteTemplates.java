package ch.dulce.ng.templates;

import org.apache.camel.builder.RouteBuilder;

import ch.dulce.ng.dto.MinioEvent;


public class KafkaRouteTemplates extends RouteBuilder {

  public static final String KAFKA_EVENT_LISTENER_TPL = "minioEventListener";
  public static final String MINIO_OBJECT_VARNAME = "savedMinioObject";
  public static final String MINIO_JSON_VARNAME = "savedMinioJson";

  @Override
  public void configure() throws Exception {

    /** listens for minio notification events on a topic and
     *  serializes the event into a MinioEvent and saves it into a variable
     *  and sending it to the given ep.
     */
    routeTemplate(KAFKA_EVENT_LISTENER_TPL)
        .templateParameter("topic")
        .templateParameter("groupId")
        .templateParameter("toEndpoint")
        .from("kafka:{{topic}}?groupId={{groupId}}")
        .setVariable(MINIO_JSON_VARNAME, body())
        .unmarshal().json(MinioEvent.class)
        .setVariable(MINIO_OBJECT_VARNAME, body())
        .to("{{toEndpoint}}");

  }

}
