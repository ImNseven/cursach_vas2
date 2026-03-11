package com.legal.analysis.domain.repository;

import com.legal.analysis.domain.model.DocumentPrecedent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentPrecedentRepository extends JpaRepository<DocumentPrecedent, Long> {

    @Query("SELECT dp FROM DocumentPrecedent dp WHERE dp.document.id = :documentId ORDER BY dp.similarityScore DESC")
    List<DocumentPrecedent> findByDocumentIdOrderBySimilarityScoreDesc(@Param("documentId") Long documentId);

    @Query("SELECT dp FROM DocumentPrecedent dp WHERE dp.document.id = :documentId ORDER BY dp.similarityScore DESC")
    Page<DocumentPrecedent> findByDocumentId(@Param("documentId") Long documentId, Pageable pageable);

    void deleteByDocumentId(Long documentId);
}
