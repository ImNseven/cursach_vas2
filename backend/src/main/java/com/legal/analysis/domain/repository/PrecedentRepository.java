package com.legal.analysis.domain.repository;

import com.legal.analysis.domain.model.Precedent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrecedentRepository extends JpaRepository<Precedent, Long>, JpaSpecificationExecutor<Precedent> {

    Page<Precedent> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Precedent p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Precedent> searchByText(@Param("query") String query);

    List<Precedent> findAllByOrderByCreatedAtDesc();

    long countByCategoryId(Long categoryId);
}
