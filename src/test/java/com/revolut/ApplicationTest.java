package com.revolut;

import org.junit.jupiter.api.*;
import spark.Spark;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

@DisplayName("Rest test")
public class ApplicationTest {

    @BeforeEach
    void createApplication() throws SQLException, IOException, URISyntaxException {
        new Application(4567, "/api", "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1").start();
    }

    @AfterEach
    void killServer() throws SQLException {
        Spark.stop();
        try(Connection connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            Statement statement = connection.createStatement()){
            statement.execute("DROP ALL OBJECTS");
        }
    }

    @Test
    @DisplayName("Response validation for invalid request")
    void testInvalidRequest1() throws URISyntaxException, IOException, InterruptedException {
        HttpResponse<String> response = doTransferRequest("{\"a\": \"b\"}");
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), is("{\"status\":\"ERROR\",\"errorMessage\":\"Input data has invalid format: \\\"fromAccountNumber\\\" is required; \\\"toAccountNumber\\\" is required; \\\"amount\\\" is required; \"}"));
    }

    @Test
    @DisplayName("Response validation for negative amount")
    void testInvalidRequest2() throws URISyntaxException, IOException, InterruptedException {
        HttpResponse<String> response = doTransferRequest(requestJson(1, 2, -0.01));
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), is("{\"status\":\"ERROR\",\"errorMessage\":\"Input data has invalid format: \\\"amount\\\" should be positive\"}"));
    }

    @Test
    @DisplayName("Response validation for valid request")
    void testResponse() throws URISyntaxException, IOException, InterruptedException {
        HttpResponse<String> response = doTransferRequest(requestJson(1L, 2L, 34.561));
        assertThat(response.statusCode(), is(200));
        assertThat(response.body(), is("{\"status\":\"DONE\"}"));
    }

    @Test
    @DisplayName("Invalid account")
    void testInvalidAccount() throws URISyntaxException, IOException, InterruptedException {
        HttpResponse<String> response = doTransferRequest(requestJson(7L, 2L, 101));
        assertThat(response.body(), is("{\"status\":\"ERROR\",\"errorMessage\":\"Account 7 does not exist\"}"));
    }

    @Test
    @DisplayName("Not enough money")
    void testTransferLotsOfMoney() throws URISyntaxException, IOException, InterruptedException {
        HttpResponse<String> response = doTransferRequest(requestJson(1L, 2L, 101));
        assertThat(response.body(), is("{\"status\":\"ERROR\",\"errorMessage\":\"Not enough money\"}"));
        assertThat(doAccountRequest("1").body(), is("{\"status\":\"DONE\",\"result\":{\"number\":1,\"balance\":100.0}}"));
    }

    @Test
    @DisplayName("Calculation validation")
    void testCalculation() throws URISyntaxException, IOException, InterruptedException {
        doTransferRequest(requestJson(1L, 2L, 34.56341));
        doTransferRequest(requestJson(3L, 1L, 50.73126));
        doTransferRequest(requestJson(2L, 3L, 130));
        doTransferRequest(requestJson(1L, 2L, 68));
        assertThat(doAccountRequest("1").body(), is("{\"status\":\"DONE\",\"result\":{\"number\":1,\"balance\":48.1679}}"));
        assertThat(doAccountRequest("2").body(), is("{\"status\":\"DONE\",\"result\":{\"number\":2,\"balance\":172.5634}}"));
        assertThat(doAccountRequest("3").body(), is("{\"status\":\"DONE\",\"result\":{\"number\":3,\"balance\":379.2687}}"));
   }


    private HttpResponse<String> doTransferRequest(String requestBody) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:4567/api/transfer"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> doAccountRequest(String accountNumber) throws IOException, InterruptedException, URISyntaxException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:4567/api/account/" + accountNumber))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String requestJson(long fromAccount, long toAccount, double amount) {
        return "{\"fromAccountNumber\":" + fromAccount + ",\"toAccountNumber\":" + toAccount + ", \"amount\": " + amount + "}";
    }
}
