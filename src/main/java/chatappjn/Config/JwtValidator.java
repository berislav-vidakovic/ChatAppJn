package chatappjn.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtValidator extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
          throws ServletException, IOException {  

    String headerSectionAuth = request.getHeader("Authorization");

    // Allow endpoints without token
    String path = request.getRequestURI();
    if( path.startsWith("/api/ping") ||
        path.startsWith("/api/pingdb") ||
        path.startsWith("/api/users/all") ||
        //path.startsWith("/api/users/register") ||
        path.startsWith("/websocket") ||
        path.startsWith("/api/auth/refresh") 
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
        response.getWriter().write("Invalid or expired token");
        return;
      }
    } 
    else { // missing Bearer in Authorization header section
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Missing Authorization header");
      return;
    }
    filterChain.doFilter(request, response);
  }
}
