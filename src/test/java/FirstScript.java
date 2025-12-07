import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class FirstScript {

    @Test
    public void test() throws Exception {
        TestPlanStats stats = testPlan(
                csvDataSet("id.csv"),
                rpsThreadGroup()
                        .maxThreads(500)
                        .rampToAndHold(10, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .rampToAndHold(20, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .children(
                                httpSampler("http://localhost:8080/api/books")
                                        .post("{\n" +
                                                "    \"title\": \"qwert\",\n" +
                                                "    \"author\": \"rtyui\",\n" +
                                                "    \"price\": 5.7\n" +
                                                "}", ContentType.APPLICATION_JSON)
                                        .children(
                                                jsonExtractor("id", "id")
                                        )
                                        .children(
                                                responseAssertion().containsSubstrings("id")
                                        )
                                        .children(
                                                jsr223PostProcessor(
                                                        "System.out.println(\"Hello world \" + vars.get(\"id\"));"
                                                )
                                        ),
                        httpSampler("http://localhost:8080/api/books")
                                .children(
                                        responseAssertion().containsSubstrings("[{\"id\":")
                                ),
                        httpSampler("http://localhost:8080/api/books/${id}")
                                .children(
                                        jsonAssertion("id").matches("${id}")
                                ),
//                        httpSampler("http://localhost:8080/api/books/${id}")
//                                .method("DELETE")
//                                .children(
//                                        jsr223PostProcessor(
//                                                s -> {
//                                                    String responseCode = s.prev.getResponseCode();
//                                                    if (!responseCode.contains("204")) {
//                                                        s.prev.setSuccessful(false);
//                                                        s.prev.setResponseMessage("Custom failure: missing 'Success'");
//                                                    }
//                                                }
//                                        )
//                                ),
                                httpSampler("http://localhost:8080/api/books/${id}")
                                        .method("PUT")
                                        .body("{\n" +
                                                "    \"title\": \"qwert\",\n" +
                                                "    \"author\": \"rtyui\",\n" +
                                                "    \"price\": 5.7\n" +
                                                "}")
                                        .header("Content-Type", "application/json")
                                        .children(
                                                responseAssertion().containsSubstrings("id")
                                        )
                                        .children(
                                                jsr223PostProcessor(
                                                        "System.out.println(\"put = \" + ${id})"
                                                )
                                        )
                        ),
//                resultsTreeVisualizer()*
                DashboardVisualizer.dashboardVisualizer()
        ).run();
    }
}
