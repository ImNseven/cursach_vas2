package com.legal.analysis.domain.repository;

import com.legal.analysis.domain.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    Optional<Favorite> findByUserIdAndPrecedentId(Long userId, Long precedentId);

    boolean existsByUserIdAndPrecedentId(Long userId, Long precedentId);

    void deleteByUserIdAndPrecedentId(Long userId, Long precedentId);
}
