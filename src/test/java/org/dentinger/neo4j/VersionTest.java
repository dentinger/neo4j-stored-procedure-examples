package org.dentinger.neo4j;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.harness.junit.Neo4jRule;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class VersionTest {
  @Rule
  public Neo4jRule neo4j = new Neo4jRule()
      .withProcedure(Version.class);

  @Test
  public void canGetStoredProcedureVersion() {
    try (
        Driver driver =
            GraphDatabase.driver(
                neo4j.boltURI(),
                Config.build().withoutEncryption().toConfig());
        Session session = driver.session()
    ) {

      try {
        StatementResult result = session.run("CALL curation.version()");

        Record single = result.single();
        assertNotNull(single);
        assertThat(single.get("version").asString(), is("\"N/A\"\n"));


      } catch (ClientException ce) {
        ce.printStackTrace();
        fail();
      }
    }
  }
}
