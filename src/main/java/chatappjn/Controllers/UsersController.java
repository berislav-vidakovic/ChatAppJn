package chatappjn.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chatappjn.Common.UserDTO;
import chatappjn.Models.RefreshToken;
import chatappjn.Models.Role;
import chatappjn.Models.User;
import chatappjn.Repositories.RoleRepository;
import chatappjn.Repositories.UserRepository;
import chatappjn.Services.Authentication;
import chatappjn.Services.WebSocketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

// GET /all
// POST /register
// POST /roles
@RestController
@RequestMapping("/api/users")
public class UsersController {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private WebSocketService webSocketService;

  @GetMapping("/all")
  public ResponseEntity<Map<String, Object>> getUsers() {
    List<User> users = userRepository.findAll();
    if (users.isEmpty()) 
      return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204

    UUID clientId = UUID.randomUUID();

    // build base URL from request
    String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(); 

    List<String> techstack = List.of(
        baseUrl + "/images/java.png",
        baseUrl + "/images/spring.png",
        baseUrl + "/images/mysql.png"
    );

    List<UserDTO> usersDTO = new ArrayList<>();
    for( User user : users ){
      List<String> roles = user.getRoles();
      HashSet<String> claims = new HashSet<>();
      for( String strRole : roles){
        Role role = roleRepository.findByRole(strRole);
        List<String> roleClaims = role.getClaims();
        for (String roleClaim : roleClaims ) {
          claims.add(roleClaim);
        }
      }
      usersDTO.add(new UserDTO(user, claims));
    }
    // All available roles with claims
    List<Role> roles = roleRepository.findAll();

    Map<String, Object> response = Map.of(
        "id", clientId.toString(),
        "users", usersDTO,
        "techstack", techstack,
        "roles", roles        
    );

    return new ResponseEntity<>(response, HttpStatus.OK); // 200
  }    

  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> body) {
    try {
      // Expecting: {"login": "penny", "fullname": "Penny", "password": "pwd123"} 
      String login = (String)body.get("login");
      String fullName = (String)body.get("fullname");
      String password = (String)body.get("password");
      if (login == null || login.isBlank() || fullName == null || fullName.isBlank()
          || password == null || password.isBlank()) {
        Map<String, Object> response = Map.of(
                  "acknowledged", false,
                  "error", "Missing login or fullname or password"
          );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
      }
      System.out.printf("Register request: login=%s, fullname=%s, password=%s%n", login, fullName, password);

      boolean exists = userRepository.existsByLoginOrFullName(login, fullName);
      if (exists) {
        Map<String, Object> response = Map.of(
                "acknowledged", false,
                "error", "User  already exists"
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409
      }
      // Hash the password using BCrypt
      String hashedPwd = passwordEncoder.encode(password);

      // Create and persist user
      User newUser = new User();
      newUser.setLogin(login);
      newUser.setFullName(fullName);
      newUser.setPwd(hashedPwd);
      newUser.setRoles(List.of("Basic"));
      userRepository.save(newUser);
      System.out.printf("New user inserted: %s%n", login);

      Map<String, Object> response = Map.of(
              "acknowledged", true,
              "user", newUser
      );
      
      webSocketService.broadcastMessage("userRegister", "WsStatus.OK", response);

      return new ResponseEntity<>(response, HttpStatus.CREATED); // 201
    } 
    catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> errorResponse = Map.of( 
          "acknowledged", false, "error", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR); // 500   
    }
  }

  @PostMapping("/roles")
  public ResponseEntity<?> updateUserRoles(@RequestBody Map<String, Object> body) {
    try {
      // Expecting: { userId, userRoles }
      String userId = (String)body.get("userId");
      List<String> userRoles = (List<String>) body.get("userRoles"); // cast to List

      if (userId == null || userId.isBlank() ) {
        Map<String, Object> response = Map.of(
                  "acknowledged", false,
                  "error", "Missing userId "
          );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400
      }
      System.out.printf("User Roles request: userId=%s, roles=%s", userId, userRoles);

   
      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        Map<String, Object> response = Map.of(
                "acknowledged", false,
                "error", "User  not found "
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404
      }
      User user = userOpt.get();
      user.setRoles(userRoles);
      userRepository.save(user);

      System.out.printf("User roles updates: %s%n", userId);

      Map<String, Object> response = Map.of(
              "acknowledged", true,
              "userId", userId,
              "userRoles", userRoles
      );

      webSocketService.broadcastMessage("userRoles", "WsStatus.OK", response);

      return new ResponseEntity<>(response, HttpStatus.OK); // 200
    } 
    catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> errorResponse = Map.of( 
          "acknowledged", false, "error", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR); // 500   
    }
  }

}
