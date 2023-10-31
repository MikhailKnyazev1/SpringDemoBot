package io.proj3ct.springdemobot.model;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long > {
}
