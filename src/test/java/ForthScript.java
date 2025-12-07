import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.controllers.DslController;
import us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer;

import java.time.Duration;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class ForthScript {

    @Test
    public void test() throws Exception {
        TestPlanStats stats = testPlan(
                csvDataSet("id_1.csv"),
                csvDataSet("id_2.csv"),
//                rpsThreadGroup()
//                        .maxThreads(500)
//                        .rampToAndHold(10, Duration.ofSeconds(30), Duration.ofMinutes(5))
//                        .rampToAndHold(20, Duration.ofSeconds(30), Duration.ofMinutes(5))
//                        .children(
//                                addNewBook()
//                        ),
                rpsThreadGroup()
                        .maxThreads(500)
                        .rampToAndHold(100, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .rampToAndHold(200, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .children(
                                getAllBooks()
                        ),
                rpsThreadGroup()
                        .maxThreads(500)
                        .rampToAndHold(100, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .rampToAndHold(200, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .children(
                                getParticularBook()
                        ),
                rpsThreadGroup()
                        .maxThreads(500)
                        .rampToAndHold(100, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .rampToAndHold(200, Duration.ofSeconds(30), Duration.ofMinutes(5))
                        .children(
                                changeParticularBook()
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
//                        .children(
//                                jsonExtractor("id", "id")
//                        )
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
                httpSampler("http://localhost:8080/api/books/${id_1}")
                        .children(
                                jsonAssertion("id").matches("${id_1}")
                        )
        );
    }

            private static DslController changeParticularBook() {
        return transaction(
                "changeParticularBook",
                httpSampler("http://localhost:8080/api/books/${id_2}")
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
        );
    }
}
