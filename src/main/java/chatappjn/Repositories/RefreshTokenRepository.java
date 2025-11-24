package chatappjn.Repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import chatappjn.Models.RefreshToken;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(String userId);
}
