package webserver;

import http.controller.HttpRequestControllers;
import http.model.request.ServletRequest;
import http.model.response.ServletResponse;
import http.session.HttpSessionManager;
import http.supoort.converter.request.HttpRequestFactory;
import http.supoort.converter.response.ResponseMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final HttpRequestControllers httpRequestControllers;

    public RequestHandler(Socket connection, HttpRequestControllers httpRequestControllers) {
        this.connection = connection;
        this.httpRequestControllers = httpRequestControllers;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            handleRequest(in, out);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new FailToHandleRequestException(e.getMessage());
        }
    }

    private void handleRequest(InputStream in, OutputStream out) {
        try {
            ServletRequest request = new HttpRequestFactory(new HttpSessionManager(() -> UUID.randomUUID().toString())).getRequest(in);
            ServletResponse response = new ServletResponse();

            httpRequestControllers.doService(request, response);

            render(response, out);

        } catch (Exception e) {
            logger.error(e.getMessage());
            sendError(e.getMessage(), out);
        }
    }

    private void sendError(String message, OutputStream out) {

    }

    private void render(ServletResponse response, OutputStream out) {
        DataOutputStream dos = new DataOutputStream(out);
        ResponseMessageConverter.convert(response, dos);
    }
}
