package chatappjn.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import chatappjn.Models.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findById(String id);
    boolean existsByLoginOrFullName(String login, String fullName);
}
