package chatappjn.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtValidator extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
          throws ServletException, IOException {  

    String headerSectionAuth = request.getHeader("Authorization");

    // White list - Allow endpoints without token
    String path = request.getRequestURI();
    if( path.startsWith("/api/ping") ||
        path.startsWith("/api/pingdb") ||
        path.startsWith("/api/users/all") ||
        //path.startsWith("/api/users/register") ||
        path.startsWith("/websocket") ||
        path.startsWith("/api/auth/refresh") ||
        path.startsWith("/api/auth/login") ||
        path.startsWith("/api/chat/new") 
      ){
          filterChain.doFilter(request, response);
          return;
    }
    if (headerSectionAuth != null && headerSectionAuth.startsWith("Bearer ")) {
      String accessJWT = headerSectionAuth.substring("Bearer ".length()); //remove prefix
      try { // Validate the JWT
        Claims claims = Jwts.parserBuilder()
          .setSigningKey(JwtBuilder.getSecretKey())  
          .build()
          .parseClaimsJws(accessJWT)
          .getBody();

        request.setAttribute("userId", claims.getSubject());
      } 
      catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        Map<String, Object> error = Map.of(
            "acknowledged", false,
            "error", "Invalid or expired token"
        );
        response.getWriter().write(new ObjectMapper().writeValueAsString(error));            
        return;
      }
    } 
    else { // missing Bearer in Authorization header section
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/json");
      Map<String, Object> error = Map.of(
          "acknowledged", false,
          "error", "Missing Authorization header"
      );
      response.getWriter().write(new ObjectMapper().writeValueAsString(error));
      return;
    }
    filterChain.doFilter(request, response);
  }
}
