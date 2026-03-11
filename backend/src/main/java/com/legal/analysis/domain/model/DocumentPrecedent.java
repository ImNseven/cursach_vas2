package com.legal.analysis.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_precedents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPrecedent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "precedent_id", nullable = false)
    private Precedent precedent;

    @Column(name = "similarity_score", nullable = false)
    private Float similarityScore;

    @CreationTimestamp
    @Column(name = "matched_at", nullable = false, updatable = false)
    private LocalDateTime matchedAt;
}
