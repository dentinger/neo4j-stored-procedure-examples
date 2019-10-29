package org.dentinger.neo4j;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;
import static org.neo4j.driver.v1.Values.parameters;

public class SimpleNodeAndSubNodeCreationTest {

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            // This is the Procedure we want to test
            .withProcedure(NodeCreation.class);


    @Test
    public void create_simple_data_singleNode() {

        try (
                Driver driver =
                        GraphDatabase.driver(
                                neo4j.boltURI(),
                                Config.build().withoutEncryption().toConfig());
                Session session = driver.session()
        ) {


            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> datas = objectMapper
                        .readValue(new File("src/test/resources/simpleData_Single_With_SubData.json"),
                                new TypeReference<Map<String,Object>>(){});

                List dataList = (ArrayList)datas.get("dataList");

                StatementResult result =
                        session.run("CALL sample.create.subdata({params})",
                                parameters("params", dataList));

                //validate the result
                List<Record> recordList = result.list();
                assertEquals("SUCCESS", recordList.get(0).get("message").asString());

                result = session.run("Match (d:Data)  return count(d)");
                assertEquals("Only should have been one Data Node", 1, result.single().get(0).asInt());

                result = session.run("Match (s:SubData) return count(s)");
                assertEquals("Only should have been one SubData Node",1, result.single().get(0).asInt());

                result = session.run("Match (d:Data) -[:DATA_CONTAINS]-> (s:SubData) return count(s)");
                assertEquals("There should have been one relationship", 1, result.single().get(0).asInt());
                session.close(); //forces the exception to get thrown

            } catch (Exception e) {
                e.printStackTrace();
                fail("create simple  should not have thrown an Exception: "+e.getMessage());
            }
        }
    }
}
