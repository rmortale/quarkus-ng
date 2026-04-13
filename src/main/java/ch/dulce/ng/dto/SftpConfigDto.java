package ch.dulce.ng.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SftpConfigDto {

  private String host;
  private int port;
  private String username;
  private String password;
  private String targetDir;

}
