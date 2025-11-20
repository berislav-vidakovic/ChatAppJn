package chatappjn.Repositories;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import chatappjn.Models.HealthCheck;

/* DB-Collection-Document{key,value}
chatappdb> db.healthcheck.find()
[
  {
    _id: ObjectId('691f4c6d8a68875daa63b112'),
    pingdb: 'Hello world from MongoDB'
  }
] */
public interface HealthCheckRepository extends MongoRepository<HealthCheck, String> {
  Optional<HealthCheck> findTopByOrderByIdAsc();
}
