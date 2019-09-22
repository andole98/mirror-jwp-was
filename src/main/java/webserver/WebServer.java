package webserver;

import http.controller.FileResourceHandler;
import http.controller.Handler;
import http.controller.ServletRequestHandlers;
import http.controller.UserHandler;
import http.supoort.RequestMapping;
import http.view.FileResourceViewResolver;
import http.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebServer {
    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = getPort(args);

        ServletRequestHandlers requestHandlers = initRequestHandlers();
        ViewResolver viewResolver = initViewResolver();

        ExecutorService es = Executors.newFixedThreadPool(100);

        // 서버소켓을 생성한다. 웹서버는 기본적으로 8080번 포트를 사용한다.
        try (ServerSocket listenSocket = new ServerSocket(port)) {
            logger.info("Web Application Server started {} port.", port);

            // 클라이언트가 연결될때까지 대기한다.
            Socket connection;
            while ((connection = listenSocket.accept()) != null) {
                es.execute(new RequestHandler(connection, requestHandlers, viewResolver));
            }
        }
        es.shutdown();
        es.awaitTermination(100, TimeUnit.SECONDS);
    }

    private static int getPort(String[] args) {
        int port = 0;
        if (args == null || args.length == 0) {
            port = DEFAULT_PORT;
        } else {
            port = Integer.parseInt(args[0]);
        }
        return port;
    }

    private static ServletRequestHandlers initRequestHandlers() {
        ServletRequestHandlers servletRequestHandlers = new ServletRequestHandlers(new FileResourceHandler(RequestMapping.GET("/*")));
        Handler userRequestHandler = new UserHandler(RequestMapping.GET("/user/create"), RequestMapping.POST("/user/create"));

        servletRequestHandlers.addHandler(userRequestHandler);
        return servletRequestHandlers;
    }

    private static ViewResolver initViewResolver() {
        return new FileResourceViewResolver();
    }

}
