package com.revolut;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.revolut.service.exception.DaoException;
import com.revolut.dto.TransferRequest;
import com.revolut.dto.Response;
import com.revolut.service.exception.AccountException;
import com.revolut.service.model.Account;
import com.revolut.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import static java.util.Objects.isNull;
import static spark.Spark.*;

class ServerStarter {
    private static final Logger log = LoggerFactory.getLogger(ServerStarter.class);
    private static final Gson gson = new Gson();

    private final int port;
    private final String basePath;
    private final AccountService accountService;

    ServerStarter(int port, String basePath, AccountService accountService) {
        this.port = port;
        this.basePath = basePath;
        this.accountService = accountService;
    }

    void start() {
        port(port);
        path(basePath, () -> {
            before("/*", (request, response) -> log.info("Received api call: " + request.body()));
            post("/transfer", "application/json", this::handleTransferRequest, gson::toJson);
            get("/account/:number", "application/json", this::handleAccountRequest, gson::toJson);
        });
        exception(DaoException.class, (exception, request, response)
                -> response.body(gson.toJson(Response.error("Dao exception: " + exception.getMessage()))));
        exception(AccountException.class, (exception, request, response)
                -> response.body(gson.toJson(Response.error(exception.getMessage()))));
        exception(JsonSyntaxException.class, (exception, request, response)
                -> response.body(gson.toJson(Response.error("Input data has invalid format: " + exception.getMessage()))));
        exception(JsonParseException.class, (exception, request, response)
                -> response.body(gson.toJson(Response.error("Input data has invalid format: " + exception.getMessage()))));

    }

    private Response handleTransferRequest(Request request, spark.Response response) {
        TransferRequest transferRequest = parseTransferRequest(request.body());
        accountService.transferMoney(
                transferRequest.getFromAccountNumber(),
                transferRequest.getToAccountNumber(),
                transferRequest.getAmount()
        );
        response.type("application/json");
        return Response.done(null);
    }

    private Response<Account> handleAccountRequest(Request request, spark.Response response) {
        long accountNumber = Long.parseLong(request.params(":number"));
        response.type("application/json");
        return Response.done(
                accountService.getAccount(accountNumber)
        );
    }

    private TransferRequest parseTransferRequest(String request) {
        TransferRequest requestObject = gson.fromJson(request, TransferRequest.class);
        validateTransferRequest(requestObject);
        return requestObject;
    }

    private void validateTransferRequest(TransferRequest request) throws JsonParseException {
        StringBuilder errorMessage = new StringBuilder();
        if (isNull(request)) {
            throw new JsonParseException("Request body is empty");
        }
        if (isNull(request.getFromAccountNumber())) {
            errorMessage.append("\"fromAccountNumber\" is required; ");
        }
        if (isNull(request.getToAccountNumber())) {
            errorMessage.append("\"toAccountNumber\" is required; ");
        }
        if (isNull(request.getAmount())) {
            errorMessage.append("\"amount\" is required; ");
        } else if(request.getAmount() <= 0){
            errorMessage.append("\"amount\" should be positive");
        }
        String message = errorMessage.toString();
        if (!message.isEmpty()) {
            throw new JsonParseException(message);
        }
    }
}
