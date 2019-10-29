package org.dentinger.neo4j;

import org.apache.commons.collections4.CollectionUtils;
import org.dentinger.neo4j.domain.Data;
import org.dentinger.neo4j.domain.DataBuilder;
import org.dentinger.neo4j.domain.SampleProcedureResponse;
import org.dentinger.neo4j.domain.SubData;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
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
import java.util.stream.StreamSupport;

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
    public Stream<SampleProcedureResponse> createData(@Name("data") List<Map> rawData) {
        List<SampleProcedureResponse> results = new ArrayList<>();

        if (validateParams(rawData, results)) return results.stream();

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

    @Procedure(value = "sample.create.subdata", mode = Mode.WRITE)
    public Stream<SampleProcedureResponse> createComplexData(@Name("data") List<Map> rawData) {
        List<SampleProcedureResponse> results = new ArrayList<>();

        if (validateParams(rawData, results)) return results.stream();

        List<Data> dataList = rawData
                .stream()
                .map(this::dataBuilderWrapper)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(dataList)) {
            for (Data algorithm : dataList) {
                results.add(subDataInsertion(algorithm));
            }
        } else {
            SampleProcedureResponse result = new SampleProcedureResponse("ERROR", "empty data list", -1);
            results.add(result);
        }
        return results.stream();
    }

    private boolean validateParams(@Name("data") List<Map> rawData, List<SampleProcedureResponse> results) {
        if (rawData == null) {
            SampleProcedureResponse result = new SampleProcedureResponse("ERROR", "No Data supplied", -1);
            results.add(result);
            return true;
        }
        return false;
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
        String dataName = data.name();

        SampleProcedureResponse x = validateDataBeforeInsertion(dataName);
        if (x != null) return x;

        String uuid = null;
        try {

            Node dataNode = db.findNode(DATA_LABEL, NAME_KEY, dataName);
            if (dataNode != null) {
                deleteSimple(dataName);
            }
            dataNode = db.createNode(DATA_LABEL);
            dataNode.setProperty(NAME_KEY, dataName);


            //create and add uuid
            uuid = UUID.randomUUID().toString();
            dataNode.setProperty(UUID_KEY, uuid);


        } catch (Exception e) {

            log.error("Unknown exception encountered in data insertion: " + e.getMessage(), e);
        }

        return new SampleProcedureResponse(uuid, "SUCCESS", 1);
    }

    private SampleProcedureResponse validateDataBeforeInsertion(String dataName) {
        if ("".equals(dataName)) {
            return new SampleProcedureResponse(null, "Invalid name value", -10);
        }

        if ("UNKNOWN".equals(dataName)) {
            return new SampleProcedureResponse(null, "Data name UNKNOWN", -10);
        }
        return null;
    }

    private SampleProcedureResponse subDataInsertion(Data data) {
        String dataName = data.name();

        if ("".equals(dataName)) {
            return new SampleProcedureResponse(null, "Invalid name value", -10);
        }

        if ("UNKNOWN".equals(dataName)) {
            return new SampleProcedureResponse(null, "Data name UNKNOWN", -10);
        }

        String uuid = null;
        try {

            Node dataNode = db.findNode(DATA_LABEL, NAME_KEY, dataName);
            if (dataNode != null) {
                deleteSub(dataName);
            }
            dataNode = db.createNode(DATA_LABEL);
            dataNode.setProperty(NAME_KEY, dataName);

            if(data.subData() != null) {
                insertSubNode(db, dataNode, data.subData());
            }

            //create and add uuid
            uuid = UUID.randomUUID().toString();
            dataNode.setProperty(UUID_KEY, uuid);


        } catch (Exception e) {

            log.error("Unknown exception encountered in sub data data insertion: " + e.getMessage(), e);
        }

        return new SampleProcedureResponse(uuid, "SUCCESS", 1);
    }

    private void insertSubNode(GraphDatabaseService db, Node dataNode, SubData subData) {
        Node subNode = db.createNode(SUBDATA_LABEL);
        subNode.setProperty(NAME_KEY, subData.name());
        subNode.setProperty("subData", subData.subData());

        dataNode.createRelationshipTo(subNode, RelationshipType.withName("DATA_CONTAINS"));
    }

    private void deleteSimple(String dataName) {
        Node dataNode = db.findNode(DATA_LABEL, NAME_KEY, dataName);
        if(dataNode != null) {
            dataNode.delete();
        }
    }

    private void deleteSub(String dataName) {
        Node dataNode = db.findNode(DATA_LABEL, NAME_KEY, dataName);
        List<Node> nodesToDelete = new ArrayList<>();
        List<Relationship> releationsToDelete = new ArrayList<>();
        if(dataNode != null) {
            //find subData nodes and delete the relation first then the nodes
            nodesToDelete.add(dataNode);
            Iterable<Relationship> relationships = dataNode.getRelationships();
            StreamSupport.stream(relationships.spliterator(), false).forEach( relationship -> {
                releationsToDelete.add(relationship);
                nodesToDelete.add(relationship.getEndNode());
            });

        }
        releationsToDelete.forEach(Relationship::delete);
        nodesToDelete.forEach(Node::delete);
    }
}


