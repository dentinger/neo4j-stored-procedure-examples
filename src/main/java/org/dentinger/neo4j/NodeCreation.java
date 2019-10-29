package org.dentinger.neo4j;

import org.apache.commons.collections4.CollectionUtils;
import org.dentinger.neo4j.domain.Data;
import org.dentinger.neo4j.domain.DataBuilder;
import org.dentinger.neo4j.domain.SampleProcedureResponse;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.dentinger.neo4j.domain.Constants.*;

public class NodeCreation {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `logs/neo4j.log`
    @Context
    public Log log;

    @Procedure(value = "sample.create.simple", mode = Mode.WRITE)
    public Stream<SampleProcedureResponse> createAlgorithm(@Name("data") List<Map> rawData) {
        List<SampleProcedureResponse> results = new ArrayList<>();

        if (rawData == null) {
            SampleProcedureResponse result = new SampleProcedureResponse("ERROR", "No Data supplied", -1);
            results.add(result);
            return results.stream();
        }

        List<Data> dataList = rawData
                .stream()
                .map(this::dataBuilderWrapper)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(dataList)) {
            for (Data algorithm : dataList) {
                results.add(dataInsertion(algorithm));
            }
        } else {
            SampleProcedureResponse result = new SampleProcedureResponse("ERROR", "empty data list", -1);
            results.add(result);
        }
        return results.stream();
    }

    private Data dataBuilderWrapper(Map raw) {
        Data a = null;
        try {
            a = DataBuilder.build(raw);
        } catch (Exception e) {

            log.error("Can not convert Data Map to object", e);
        }
        return a;

    }

    private SampleProcedureResponse dataInsertion(Data data) {
        String algoName = data.name();

        if ("".equals(algoName)) {
            return new SampleProcedureResponse(null, "Invalid name value", -10);
        }

        if ("UNKNOWN".equals(algoName)) {
            return new SampleProcedureResponse(null, "Data name UNKNOWN", -10);
        }

        String uuid = null;
        try {

            Node dataNode = db.findNode(DATA_LABEL, NAME_KEY, algoName);
            if (dataNode != null) {
                deleteSimple(algoName);
            }
            dataNode = db.createNode(DATA_LABEL);
            dataNode.setProperty(NAME_KEY, algoName);


            //create and add uuid
            uuid = UUID.randomUUID().toString();
            dataNode.setProperty(UUID_KEY, uuid);


        } catch (Exception e) {

            log.error("Unknown exception encountered in data insertion: " + e.getMessage(), e);
        }

        return new SampleProcedureResponse(uuid, "SUCCESS", 1);
    }

    private void deleteSimple(String algoName) {
        Node dataNode = db.findNode(DATA_LABEL, NAME_KEY, algoName);
        if(dataNode != null) {
            dataNode.delete();
        }
    }
}


