package com.GeekHub.userservice.db.repository;

import com.GeekHub.userservice.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<User,Long> {
    Optional<User> findByUserId(String userId);
    List<User> findAllByLocalSchool(String localSchool);
    Optional<User> findUserByUserIdx(long userIdx);
}
