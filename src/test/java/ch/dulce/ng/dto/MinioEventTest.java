package ch.dulce.ng.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MinioEventTest {

  ObjectReader reader = new ObjectMapper().readerFor(MinioEvent.class);
  MinioEvent eventWithFlowid;
  MinioEvent eventWithoutFlowid;
  MinioEvent eventFromDb;

  @BeforeEach
  void setup() throws IOException {
    eventWithFlowid = reader.readValue(Path.of("src/test/data", "example-event.json").toFile());
    eventWithoutFlowid = reader.readValue(Path.of("src/test/data", "example-event-noflow.json").toFile());
    eventFromDb = reader.readValue(Path.of("src/test/data", "extracted-sql-event.json").toFile());
  }

  @Test
  void eventFromDbTest() {
    assertEquals("vvz-source-bucket/service1/68b4bab8-6e8a-486a-8113-447b9f9c9fd8/pom.xml",
        eventFromDb.getKey());
  }

  @Test
  void eventTimeTest() {
    assertEquals("2026-03-25T17:01:12.564Z", eventWithFlowid.getRecords().get(0).getEventTime());
  }

  @Test
  void deserializeTest() {
    assertEquals("b9e19aef-cb54-4781-a5c6-be35f32cc546", eventWithFlowid.tracingId());
    assertEquals("service1", eventWithFlowid.serviceId());
  }

  @Test
  void deserializeSuccessfull() {
    assertEquals("ng-file-store/flowid-25/bin/mvn.cmd", eventWithFlowid.getKey());
    assertEquals("ng-file-store/bin/mvn.cmd", eventWithoutFlowid.getKey());
    assertEquals(1, eventWithFlowid.getRecords().size());
    assertEquals(1, eventWithoutFlowid.getRecords().size());
  }
}