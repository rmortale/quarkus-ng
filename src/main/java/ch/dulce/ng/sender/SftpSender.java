package ch.dulce.ng.sender;

import ch.dulce.ng.dto.MinioEvent;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;

import static ch.dulce.ng.routing.Router.*;
import static ch.dulce.ng.routing.SuccessRoute.SUCCESS_EP;
import static ch.dulce.ng.templates.KafkaRouteTemplates.MINIO_OBJECT_VARNAME;


@ApplicationScoped
public class SftpSender extends RouteBuilder {

  @Override
  public void configure() throws Exception {

    // zero redeliveries configured as the sftp ep already has retries set
    errorHandler(deadLetterChannel(ERROR_HANDLER_EP)
        .maximumRedeliveries(0)
        .useOriginalBody());

    from("""
        kafka:ng-sftp-events?groupId=sftpConsumerGroup&consumersCount={{sftp.sender.consumersCount}}
        &maxPollIntervalMs={{sftp.sender.maxPollIntervalMs}}
        &maxPollRecords=1""")
        .streamCache(false) //OOME occure with large files if set to true
        .routeId("sftp-sender-route")
        .unmarshal().json(MinioEvent.class)
        .setVariable(MINIO_OBJECT_VARNAME, body())

        // download data from minio
        .setHeader("CamelAwsS3OverrideBucketName", simple("${variable.savedMinioObject.bucketname}"))
        .setHeader("CamelAwsS3Key", simple("${variable.savedMinioObject.filenameWithPath}"))
        .setHeader("CamelFileName", simple("${variable.savedMinioObject.filename}"))
        .to("aws2-s3://dummybucket?operation=getObject")

        // upload data to SFTP host
        .toD("mina-sftp://${variable.savedMinioObject.sftpConfigDto.host}:" +
          "${variable.savedMinioObject.sftpConfigDto.port}/${variable.savedMinioObject.sftpConfigDto.targetDir}" +
          "?binary=true&username=${variable.savedMinioObject.sftpConfigDto.username}" +
          "&password=${variable.savedMinioObject.sftpConfigDto.password}&autoCreate=true" +
          "&maximumReconnectAttempts={{sftp.sender.maximumReconnectAttempts}}&reconnectDelay={{sftp.sender.reconnectDelay}}")

        .setBody(variable(MINIO_OBJECT_VARNAME))
        .log("Uploaded file ${header.CamelFileName} to SFTP server successfully.")
        .to(SUCCESS_EP);

  }

}
