package http.session;

import java.util.HashMap;
import java.util.Map;

public class HttpSessionManager {
    private final IdGenerateStrategy idGenerateStrategy;
    private Map<String, HttpSession> sessions = new HashMap<>();


    public HttpSessionManager(IdGenerateStrategy idGenerateStrategy) {
        this.idGenerateStrategy = idGenerateStrategy;
    }

    public HttpSession getSession(String id) {
        return sessions.get(id);
    }

    public HttpSession getSession() {
        return generate();
    }

    private HttpSession generate() {
        String id = generateId();
        HttpSession session = new HttpSession(id);
        sessions.put(id, session);
        return session;
    }

    private String generateId() {
        String id;
        do {
            id = idGenerateStrategy.generate();
        } while (sessions.keySet().contains(id));
        return id;
    }
}
