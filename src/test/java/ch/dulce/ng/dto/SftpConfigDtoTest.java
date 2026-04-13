package ch.dulce.ng.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SftpConfigDtoTest {

  ObjectReader reader = new ObjectMapper().readerFor(SftpConfigDto.class);
  SftpConfigDto dto1;

  @BeforeEach
  void setup() throws IOException {
    dto1 = reader.readValue(Path.of("src/test/data", "example-sftpconfig-data.json").toFile());
  }

  @Test
  void unmarshallSuccessfullTest() {
    assertEquals("oravm05", dto1.getHost());
  }


}