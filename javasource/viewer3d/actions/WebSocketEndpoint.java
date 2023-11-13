package viewer3d.actions;

import javax.websocket.*;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;

import java.io.IOException;
import java.util.*;

public class WebSocketEndpoint extends Endpoint {
    private Set<Session> sessions;

    private ILogNode logger;

    public WebSocketEndpoint() {
        this.sessions = new HashSet<>();
        this.logger = Core.getLogger(WebSocketEndpoint.class.getSimpleName());
    }

    public static void register(String webSocketEndpoint) throws DeploymentException {
        Core.addWebSocketEndpoint("/" + webSocketEndpoint, new WebSocketEndpoint());
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        sessions.add(session);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                try {
                    session.getBasicRemote().sendText("hello");
                    // session.close();
                } catch (IOException e) {
                    logger.error(e);
                    e.printStackTrace();
                }
            }
        });

        try {
            session.getBasicRemote().sendText("socket opened");
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        logger.info("Received onClose call with reason: " + closeReason);
        sessions.remove(session);
    }
}
