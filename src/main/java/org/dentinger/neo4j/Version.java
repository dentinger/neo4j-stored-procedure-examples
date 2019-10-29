package org.dentinger.neo4j;

import org.dentinger.neo4j.domain.VersionResult;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;
import org.neo4j.stream.Streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class Version {
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `logs/neo4j.log`
    @Context
    public Log log;

    @Procedure(value = "curation.version", mode = Mode.READ)
    public Stream<VersionResult> getVersion() {

        log.info("Getting Version Information");

        InputStream inputStream = Version.class.getResourceAsStream("/version.txt");
        if (inputStream != null) {
            try {
                String data = readFromInputStream(inputStream);
                log.debug("Found Version: " + data);
                return Streams.ofNullable(new VersionResult(data));
            } catch (IOException e) {
                log.error("Unable to read version.txt for  procedures", e);
                return Streams.ofNullable(new VersionResult("N/A"));
            }
        }
        log.error("Unable to read version.txt for procedures");
        return Streams.ofNullable(new VersionResult("N/A"));
    }

    private String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {

                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

}
