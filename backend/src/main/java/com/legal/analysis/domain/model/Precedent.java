package com.legal.analysis.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "precedents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Precedent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_number", unique = true, length = 100)
    private String caseNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "decision_date")
    private LocalDate decisionDate;

    @Column(name = "court_name", length = 300)
    private String courtName;

    @Column(length = 100)
    private String decision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "precedent_tags",
            joinColumns = @JoinColumn(name = "precedent_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "precedent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentPrecedent> documentMatches = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
