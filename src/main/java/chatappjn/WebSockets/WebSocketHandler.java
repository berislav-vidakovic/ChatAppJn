package chatappjn.WebSockets;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import chatappjn.Models.Message;
import chatappjn.Repositories.ChatRepository;
import chatappjn.Repositories.MessageRepository;
import chatappjn.Services.Client;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private SessionMonitor sessionMonitor;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ObjectMapper mapper;

    public WebSocketSession getSessionByClientId(UUID clientId) {
      for (Map.Entry<WebSocketSession, Client> entry : sessionMonitor.getActivityMap().entrySet()) {
        WebSocketSession session = entry.getKey();
        Client client = entry.getValue();
        if (client.getClientId().equals(clientId)) 
          return session;
      }
      return null; // not found
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
      UUID clientId = null;
      try {
        String idParam = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("id");
        clientId = UUID.fromString(idParam);
      } 
      catch (Exception e) {
        System.err.println("Invalid or missing clientId in WebSocket URL: " + session.getUri());
        session.close(CloseStatus.BAD_DATA); // immediately close
        return; // abort further processing
      }
      // valid UUID, proceed
      sessionMonitor.addSocket(session, clientId);  
      //System.out.println(" *** WS Connected for clientId=" + clientId );
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
      /*UUID clientId = null;
      try {
        String idParam = UriComponentsBuilder.fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("id");
        clientId = UUID.fromString(idParam);
      } 
      catch (Exception e) {
        System.err.println("Invalid or missing clientId in WebSocket URL: " + session.getUri());
        session.close(CloseStatus.BAD_DATA); // immediately close
        return; // abort further processing
      }      */
      
      
      sessionMonitor.updateSessionActivity(session);
      //System.out.println("...DONE updateSessionActivity ");

      String payload = message.getPayload();

      // Parse JSON received: {"type":"newMessage",
      //                       "status":"WsStatus.Request",
      //                       "data":{
      //                          "id":"6f2f0d96-c8f9-4fb5-8507-1e7fd0d547b4",
      //                          "userId":"692326918a68875daa63b113",
      //                          "chatId":"692aeac4f646804bfa63b122",
      //                          "msg":"a"
                    //          }
          //                  }
      try {
        System.out.println("Received WS message " + payload);
        JsonNode rootNode = mapper.readTree(payload);  // parse JSON

        // Access fields directly
        String type = rootNode.path("type").asText();
        String status = rootNode.path("status").asText();
        JsonNode dataNode = rootNode.path("data");

        String id = dataNode.path("id").asText();
        String userId = dataNode.path("userId").asText();
        String chatId = dataNode.path("chatId").asText();
        String msg = dataNode.path("msg").asText();

        System.out.println("Type: " + type);
        System.out.println("Status: " + status);
        System.out.println("ID: " + id);
        System.out.println("User ID: " + userId);
        System.out.println("Chat ID: " + chatId);
        System.out.println("Message: " + msg);

        // create new Message object and update with values received
        ObjectId chatIdObj = new ObjectId(chatId);
        ObjectId userIdObj = new ObjectId(userId);
        Date now = new Date();
        Message messageObj = new Message(chatIdObj, userIdObj, now, msg);
        System.out.println("Mapped Message object: " + messageObj);
        // save it using MongoRepository
        messageRepository.save(messageObj);     

        // Broadcast new message        
        // Root JSON object
        ObjectNode rootNodeWs = mapper.createObjectNode();
        rootNodeWs.put("type", "newMessage");
        rootNodeWs.put("status", "WsStatus.OK");

        // Data node
        ObjectNode dataNodeWs = mapper.createObjectNode();
        dataNodeWs.put("userId", messageObj.getUserId().toHexString());
        dataNodeWs.put("messageId", messageObj.getId());
        dataNodeWs.put("chatId", messageObj.getChatId().toHexString());
        dataNodeWs.put("datetime", messageObj.getDatetime().getTime());
        dataNodeWs.put("text", messageObj.getText());

        rootNodeWs.set("data", dataNodeWs);

        // Convert to JSON string
        String broadcastPayload = mapper.writeValueAsString(rootNodeWs); 
        System.out.println("WS SEND: " + broadcastPayload);

        broadcast(broadcastPayload);
        
      } 
      catch (Exception e) {
          System.err.println("Invalid WS message JSON: " + payload);
          e.printStackTrace();
      }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
      sessionMonitor.removeSocket(session);
      System.out.println("WebSocket disconnected: ");
    }

    // Helper method to broadcast messages from anywhere
    public void broadcast(String message) {
      for (Map.Entry<WebSocketSession, Client> entry : sessionMonitor.getActivityMap().entrySet()) {
        WebSocketSession session = entry.getKey();
        synchronized (session) { //synchronize per session
          if (session.isOpen()) {
            try {
              session.sendMessage(new TextMessage(message));
            } 
            catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }      
    }

    public void sendWsMessage(WebSocketSession session, TextMessage msg) {
      try {
        synchronized (session) {  // required for thread safety
        if (session != null && session.isOpen()) 
          session.sendMessage(msg);
        else 
          System.err.println("Cannot send WS message: session is closed or null");
        }
      } 
      catch (Exception e) {
        System.err.println("Error in sendSafe:");
        e.printStackTrace();
      } 
    }   
}
