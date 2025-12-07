package chatappjn.Services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import chatappjn.Repositories.UserRepository;
import chatappjn.Repositories.MessageRepository;
import chatappjn.Repositories.RefreshTokenRepository;
import chatappjn.Repositories.RoleRepository;
import chatappjn.Repositories.ChatRepository;
import chatappjn.Common.AuthUser;
import chatappjn.Config.JwtBuilder;
import chatappjn.Models.RefreshToken;
import chatappjn.Models.User;

import com.fasterxml.jackson.databind.ObjectMapper;


// Spring-managed singleton 
@Service
public class Authentication extends MiddlewareCommon {
    @Autowired
    private UserRepository userRepository;

     @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired  // Add for RBAC
    private RoleRepository roleRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ObjectMapper mapper;

    private RefreshToken checkReceivedToken(String refreshToken) {
      if (refreshToken == null || refreshToken.isEmpty()) 
          return null; // Missing token
        
      Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
      if (tokenOpt.isEmpty()) 
          return null; // Token not found in DB

      RefreshToken token = tokenOpt.get();
      if (token.getExpiresAt().isBefore(Instant.now())) 
          return null; // Token expired

      return token; // Valid Token
    }

    public AuthUser authenticate(String userId, String password){
      // Find user
      Optional<User> optionalUser = userRepository.findById(userId);
      if (optionalUser.isEmpty()) 
        return new AuthUser("UserID Not found");
      User user = optionalUser.get();

      // Password validation 
      // if no hashed pwd in DB => new user, first time password hashing
      if( user.getPwd().isEmpty() ){
        // Hash the password using BCrypt
        String hashedPwd = passwordEncoder.encode(password);
        user.setPwd(hashedPwd);
        userRepository.save(user);
      }       
      else {
        boolean passwordsMatch = passwordEncoder.matches(password, user.getPwd());
        if( !passwordsMatch )
          return new AuthUser( "Invalid password" );
      }

      return buildAuthUser(user);
    }

    private String renewAndStoreRefreshToken(RefreshToken tokenEntity){
      String refreshToken = java.util.UUID.randomUUID().toString();
      Instant expiresAt = Instant.now().plusSeconds(7 * 24 * 60 * 60); // 7 days
      tokenEntity.setToken(refreshToken);
      tokenEntity.setExpiresAt(expiresAt);
      refreshTokenRepository.save(tokenEntity);
      return refreshToken;
    }

    private AuthUser buildAuthUser(User user) {
      List<String> roles = user.getRoles(); // Add for RBAC
      List<String> claims = collectClaims(user); // Add for RBAC
      String accessToken = JwtBuilder.generateToken(
              user.getId(),
              user.getLogin(),
              roles, claims ); // Add for RBAC
      RefreshToken tokenEntity = new RefreshToken(user.getId());
      String refreshToken = renewAndStoreRefreshToken(tokenEntity);
      return new AuthUser(accessToken, refreshToken, user);
    }

    public AuthUser authenticate(String refreshToken){
      RefreshToken refTokenEntity = checkReceivedToken(refreshToken);
      if( refTokenEntity == null ) 
        return new AuthUser("Refresh token missing, invalid or expired");
      // valid token - get user by userId from refreshToken 
      var userOpt = userRepository.findById(refTokenEntity.getUserId());
      if (userOpt.isEmpty()) 
          return new AuthUser("User not found for the provided refresh token");
      
      // delete existing refreshToken
      refreshTokenRepository.delete(refTokenEntity);
      return buildAuthUser(userOpt.get());
    }
    
    //Add for RBAC
    private List<String> collectClaims(User user) {
      // No roles → no claims
      if (user.getRoles() == null || user.getRoles().isEmpty())
        return List.of();
      // Fetch role documents from DB
      var roleDocs = roleRepository.findByRoleIn(user.getRoles());
      // Merge claims from all role documents
      return roleDocs.stream()
        .flatMap(r -> r.getClaims().stream())
        .distinct()
        .toList();
    } 
}
