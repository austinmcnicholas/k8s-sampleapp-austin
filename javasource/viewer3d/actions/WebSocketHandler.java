package viewer3d.actions;

import javax.websocket.*;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.viewer3d.jtreader.JtReader;
import com.mendix.viewer3d.jtreader.JtUtils;
import com.mendix.viewer3d.jtreader.StreamType;

import java.nio.ByteBuffer;
import java.util.*;

import viewer3d.actions.websocket.IWebSocketHandler;
import viewer3d.actions.websocket.IWebSocketResponder;
import viewer3d.actions.websocket.WebSocketChannel;

public class WebSocketHandler extends Endpoint implements IWebSocketHandler {

    private ILogNode _logger;

    public WebSocketHandler() {
        this._logger = Core.getLogger(WebSocketHandler.class.getSimpleName());
    }

    public static void register(String webSocketEndpoint) throws DeploymentException {
        Core.addWebSocketEndpoint("/" + webSocketEndpoint, new WebSocketHandler());
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        var endpoint = session.getBasicRemote();
        String sessionId = (String) config.getUserProperties().get("mxSessionId");
        if (sessionId != null) {
          var mxSession = Core.getSessionById(UUID.fromString(sessionId));
          var ctx = mxSession.createContext();
          session.addMessageHandler(new WebSocketChannel(_logger, ctx, endpoint, this));
          _logger.info("WebSocketChannel is registered");
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        _logger.info("Received onClose call with reason: " + closeReason);
    }
    
    @Override
	public void handleRequest(IContext context, Object request, Map<String, String> headers, String method, int messageId, IWebSocketResponder responder) {
    	try {
    		var action = headers.get("action");
        	var modelId = headers.get("modelName");
        	switch(action) {
        		case "ps":
        			this.processJtRequest(context, null, modelId, StreamType.ProductStructure, responder);
        			break;
        		case "pmi":
        			this.processJtRequest(context, request, modelId, StreamType.Pmi, responder);
        			break;
        		case "shapes":
        			this.processJtRequest(context, request, modelId, StreamType.Shapes, responder);
        			break;
        		case "metadata":
        			this.processJtRequest(context, request, modelId, StreamType.Metadata, responder);
        			break;
        	}
    	} catch(CoreException exception) {
    		responder.respond(exception, -1, -1, headers, false);
    	}
    	
	}
    
    private void processJtRequest(IContext ctx, Object request, String modelId, StreamType st, IWebSocketResponder responder) throws CoreException  {
        var mendixObjects = Core.retrieveXPathQuery(ctx, "//Viewer3D.MxModelDocument[ModelId='" + modelId + "']");
        if (mendixObjects.size() == 0) {
            mendixObjects = Core.retrieveXPathQuery(ctx, "//Viewer3D.MxChildDocument[ModelId='" + modelId + "']");
            if (mendixObjects.size() == 0) {
                return;
            }
        }
        
        Set<UUID> segmentIds = null;
        if(request instanceof ByteBuffer) {
        	segmentIds = JtUtils.INSTANCE.parseSegmentIds(((ByteBuffer) request).array());
        }
        
        var mendixObject = mendixObjects.get(0);
        var size = (long) mendixObject.getMember(ctx, "Size").getValue(ctx);
        var dp = new MendixFileDocumentDataProvider(ctx, mendixObject, size);
        var jtReader = new JtReader(dp);
        var writer = new WebSocketWriter(responder);
        jtReader.pipe(st, writer, segmentIds);
        writer.flush();
    }
}
