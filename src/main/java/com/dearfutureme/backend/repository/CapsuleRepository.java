package com.dearfutureme.backend.repository;

import com.dearfutureme.backend.entity.Capsule;
import com.dearfutureme.backend.entity.CapsuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CapsuleRepository extends JpaRepository<Capsule, Long> {

    List<Capsule> findByEmailOrderByCreatedAtDesc(String email);

    List<Capsule> findByIsPublicTrueOrderByCreatedAtDesc();

    @Query("SELECT c FROM Capsule c WHERE c.status = :status AND c.deliveryDate <= :now")
    List<Capsule> findDueCapsules(
            @Param("status") CapsuleStatus status,
            @Param("now") LocalDateTime now
    );
}
