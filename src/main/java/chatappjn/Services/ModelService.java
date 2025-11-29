package chatappjn.Services;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import chatappjn.Models.User;
import chatappjn.Repositories.UserRepository;
import chatappjn.Repositories.MessageRepository;
import chatappjn.Repositories.ChatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;


// Spring-managed singleton 
@Service
public class ModelService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ObjectMapper mapper;

    }
