package chatappjn.Repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import chatappjn.Models.Role;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {

  // Get a single role by its name
  Role findByRole(String role);

  // Get many roles by list of role names
  List<Role> findByRoleIn(List<String> roles);
}
