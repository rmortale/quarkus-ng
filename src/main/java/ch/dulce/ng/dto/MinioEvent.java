package ch.dulce.ng.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.utils.http.SdkHttpUtils;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class MinioEvent {

  private SftpConfigDto sftpConfigDto;
  private String status = "TRANSFER_STARTED";
  private String exceptionCaught;
  private String exceptionEndpoint;
  private String failureRouteId;
  @JsonProperty("Key")
  private String key;
  @JsonProperty("Records")
  private List<S3Record> records;

  public String filename() {
    var filenames = SdkHttpUtils.urlDecode(records.getFirst().s3.object.key).split("/");
    return filenames[filenames.length - 1];
  }

  public String eventTime() {
    return records.getFirst().eventTime;
  }

  public String filenameWithPath() {
    return SdkHttpUtils.urlDecode(records.getFirst().s3.object.key);
  }

  public String bucketname() {
    return records.getFirst().s3.bucket.name;
  }

  public String tracingId() {
    return records.getFirst().s3.object.userMetadata.get("X-Amz-Meta-Tracingid");
  }

  public String serviceId() {
    return records.getFirst().s3.object.userMetadata.get("X-Amz-Meta-Serviceid");
  }

  @Override
  public String toString() {
    return "MinioEvent [key=" + key + ", " + "tracingid=" +  tracingId() + "]";
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  public static class S3Record {

    private String eventTime;
    private S3Entity s3;

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  public static class S3Entity {

    private String s3SchemaVersion;
    private S3Bucket bucket;
    private S3Object object;

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  public static class S3Object {

    private String key;
    private long size;
    private String contentType;
    private Map<String, String> userMetadata;

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  public static class S3Bucket {

    private String name;

  }
}
