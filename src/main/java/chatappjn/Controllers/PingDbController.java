package chatappjn.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chatappjn.Repositories.HealthCheckRepository;
import chatappjn.Models.HealthCheck;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PingDbController {

    private final HealthCheckRepository healthCheckRepository;

    public PingDbController(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    @GetMapping("/pingdb")
    public ResponseEntity<Map<String, Object>> pingDb() {
        try {
            Optional<HealthCheck> row = healthCheckRepository.findTopByOrderByIdAsc();
            if (!row.isPresent())
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);

            Map<String, Object> response = Map.of("response", row.get().getPingdb());
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception ex) {
            ex.printStackTrace();
            Map<String, Object> errorResponse = Map.of("error", "Database connection failed");
            return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
