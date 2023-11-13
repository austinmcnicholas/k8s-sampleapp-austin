package viewer3d.actions;

import com.mendix.viewer3d.jtreader.JtOutputWriter;

import viewer3d.actions.websocket.IWebSocketResponder;

public class WebSocketWriter implements JtOutputWriter {
	
	IWebSocketResponder responder;
	
	WebSocketWriter(IWebSocketResponder responder) {
		this.responder = responder;
	}

	@Override
	public void setLength(int arg0) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws Exception {
	}
	
	void flush() {
	}
}
