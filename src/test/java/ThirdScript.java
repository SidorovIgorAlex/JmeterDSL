import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.controllers.DslController;
import us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer;

import java.time.Duration;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class ThirdScript {

    @Test
    public void test() throws Exception {
        TestPlanStats stats = testPlan(
                csvDataSet("id_1.csv"),
                rpsThreadGroup()
                        .maxThreads(500)
                        .rampToAndHold(10, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .rampToAndHold(20, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .children(
                                addNewBook(),
                                getAllBooks(),
                                getParticularBook(),
                                changeParticularBook()
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
                        ),
//                resultsTreeVisualizer()*
                DashboardVisualizer.dashboardVisualizer()
        ).run();
    }

    private static DslController addNewBook() {
        return transaction(
                "addNewBook",
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
                        )
        );
    }

    private static DslController getAllBooks() {
        return transaction(
                "getAllBooks",
                httpSampler("http://localhost:8080/api/books")
                        .children(
                                responseAssertion().containsSubstrings("[{\"id\":")
                        )
        );
    }

    private static DslController getParticularBook() {
        return transaction(
                "getParticularBook",
                httpSampler("http://localhost:8080/api/books/${id}")
                        .children(
                                jsonAssertion("id").matches("${id}")
                        )
        );
    }

            private static DslController changeParticularBook() {
        return transaction(
                "changeParticularBook",
                httpSampler("http://localhost:8080/api/books/${id}")
                        .method("PUT")
                        .body("{\n" +
                                "    \"title\": \"qwert\",\n" +
                                "    \"author\": \"rtyui\",\n" +
                                "    \"price\": 5.7\n" +
                                "}")
                        .contentType(ContentType.APPLICATION_JSON)
                        .children(
                                responseAssertion().containsSubstrings("id")
                        )
                        .children(
                                jsr223PostProcessor(
                                        "System.out.println(\"put = \" + ${id})"
                                )
                        )
        );
    }
}
