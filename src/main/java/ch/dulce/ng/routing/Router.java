package ch.dulce.ng.routing;

import ch.dulce.ng.dto.MinioEvent;
import ch.dulce.ng.dto.SftpConfigDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.caffeine.CaffeineConstants;

import static ch.dulce.ng.templates.KafkaRouteTemplates.MINIO_OBJECT_VARNAME;

@ApplicationScoped
public class Router extends RouteBuilder {

  public static final String SUCCESS_MESSAGE = "TRANSFER_SUCCESS";
  public static final String FAILURE_MESSAGE = "TRANSFER_FAILED";
  public static final String STARTED_MESSAGE = "TRANSFER_STARTED";

  public static final String ERROR_HANDLER_EP = "direct:ng-error-events";
  public static final String ERROR_HANDLER_KAFKA_EP = "kafka:ng-error-events";
  public static final String SFTP_KAFKA_EP = "kafka:ng-sftp-events";
  public static final String SFTP_DIRECT_EP = "direct:sftp-ep";
  public static final String S3_KAFKA_EP = "kafka:ng-s3-events";
  public static final String MAIL_KAFKA_EP = "kafka:ng-mail-events";
  public static final String ROUTING_EP = "direct:routing";
  public static final String NO_ROUTING_FOUND_EP = "direct:no-routing-found-ep";
  public static final String ROUTING_CACHE_EP = "caffeine-cache:routing-cache";


  @Override
  public void configure() throws Exception {

    // query routing db for routing info
    // and caches routing info in caffeine cache
    from(ROUTING_EP)
        .routeId("routing-main-route")
        // query cache
        .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_GET))
        .setHeader(CaffeineConstants.KEY, simple("${variable.savedMinioObject.serviceId}"))
        .to(ROUTING_CACHE_EP)
        // save in chache if not found
        .choice().when(header(CaffeineConstants.ACTION_HAS_RESULT).isEqualTo(Boolean.FALSE))
          .setBody().simple("select id, serviceid, transfertype, transferconfig from ng_routing where serviceid = '${variable.savedMinioObject.serviceId}'").to("jdbc:default")
          .setHeader(CaffeineConstants.ACTION, constant(CaffeineConstants.ACTION_PUT))
          .setHeader(CaffeineConstants.KEY, simple("${variable.savedMinioObject.serviceId}"))
          .to(ROUTING_CACHE_EP)
        .otherwise()
          .log(LoggingLevel.DEBUG, "Routing for ${variable.savedMinioObject.serviceId} loaded from cache.")
        .end()

        // check if routing record exists for serviceId in db
        .choice()
          .when(simple("${body.isEmpty()}"))
            .to(NO_ROUTING_FOUND_EP)
        .otherwise()
          .split(body())
            .recipientList(simple("direct:${body[transfertype]}-ep")).ignoreInvalidEndpoints();

    from(NO_ROUTING_FOUND_EP)
        .routeId("routing-not-found-route")
        .log("No routing found for serviceId: ${variable.savedMinioObject.serviceId}!")
        .setProperty(ExchangePropertyKey.EXCEPTION_CAUGHT.getName(), simple("No routing found for serviceId: ${variable.savedMinioObject.serviceId}!"))
        .setProperty(ExchangePropertyKey.FAILURE_ENDPOINT.getName(), constant(ROUTING_EP))
        .setProperty(ExchangePropertyKey.FAILURE_ROUTE_ID.getName(), constant("routing-main-route"))
        .setBody(variable(MINIO_OBJECT_VARNAME))
        .marshal().json()
        .to(ERROR_HANDLER_EP);

    // combine MinioEvent with SFTP config and send to kafka sftp topic
    from(SFTP_DIRECT_EP)
        .routeId("routing-sftp-route")
        .setBody().simple("${body[transferconfig]}")
        .unmarshal().json(SftpConfigDto.class)
        .process(ex -> {
          SftpConfigDto cfg = ex.getMessage().getBody(SftpConfigDto.class);
          MinioEvent ev = ex.getVariable(MINIO_OBJECT_VARNAME, MinioEvent.class);
          ev.setSftpConfigDto(cfg);
          ex.getMessage().setBody(ev);
        })
        .marshal().json()
        .to(SFTP_KAFKA_EP);

//    from(S3_ROUTING_EP)
//        .log("S3 routing endpoint")
//        .marshal().json()
//        .to(S3_KAFKA_EP);
//
//    from(MAIL_ROUTING_EP)
//        .log("Mail routing endpoint")
//        .marshal().json()
//        .to(MAIL_KAFKA_EP);



  }

}
