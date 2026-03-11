package com.legal.analysis.domain.repository;

import com.legal.analysis.domain.model.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Page<Document> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.user.id = :userId AND d.id = :documentId")
    Optional<Document> findByIdAndUserId(@Param("documentId") Long documentId, @Param("userId") Long userId);

    long countByUserId(Long userId);
}
