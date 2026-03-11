package com.legal.analysis.service;

import com.legal.analysis.infrastructure.analyzer.TfIdfAnalysisStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class TfIdfAnalysisStrategyTest {

    private TfIdfAnalysisStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TfIdfAnalysisStrategy();
    }

    @Test
    void tokenize_shouldReturnNonEmptyListForValidText() {
        List<String> tokens = strategy.tokenize("Иск о взыскании задолженности по договору займа");
        assertThat(tokens).isNotEmpty();
    }

    @Test
    void tokenize_shouldFilterStopWords() {
        List<String> tokens = strategy.tokenize("и в на что");
        assertThat(tokens).isEmpty();
    }

    @Test
    void tokenize_shouldFilterShortWords() {
        List<String> tokens = strategy.tokenize("я он");
        assertThat(tokens).isEmpty();
    }

    @Test
    void tokenize_shouldReturnEmptyListForNullText() {
        List<String> tokens = strategy.tokenize(null);
        assertThat(tokens).isEmpty();
    }

    @Test
    void tokenize_shouldReturnEmptyListForBlankText() {
        List<String> tokens = strategy.tokenize("   ");
        assertThat(tokens).isEmpty();
    }

    @Test
    void computeVector_shouldReturnNonEmptyMapForValidText() {
        Map<String, Double> vector = strategy.computeVector("взыскание задолженности займ договор");
        assertThat(vector).isNotEmpty();
    }

    @Test
    void computeVector_shouldReturnEmptyMapForEmptyText() {
        Map<String, Double> vector = strategy.computeVector("");
        assertThat(vector).isEmpty();
    }

    @Test
    void computeSimilarity_shouldReturnOneForIdenticalVectors() {
        Map<String, Double> vectorA = strategy.computeVector("взыскание задолженности займ договор суд иск");
        Map<String, Double> vectorB = strategy.computeVector("взыскание задолженности займ договор суд иск");
        double similarity = strategy.computeSimilarity(vectorA, vectorB);
        assertThat(similarity).isCloseTo(1.0, within(0.001));
    }

    @Test
    void computeSimilarity_shouldReturnZeroForEmptyVectors() {
        double similarity = strategy.computeSimilarity(Map.of(), Map.of());
        assertThat(similarity).isEqualTo(0.0);
    }

    @Test
    void computeSimilarity_shouldReturnHighValueForSimilarTexts() {
        Map<String, Double> vecA = strategy.computeVector(
                "иск взыскание задолженности договор займа ответчик истец суд"
        );
        Map<String, Double> vecB = strategy.computeVector(
                "иск взыскание долга договор займа суд решение"
        );
        double similarity = strategy.computeSimilarity(vecA, vecB);
        assertThat(similarity).isGreaterThan(0.3);
    }

    @Test
    void computeSimilarity_shouldReturnLowValueForDifferentTexts() {
        Map<String, Double> vecA = strategy.computeVector(
                "иск взыскание задолженности займ договор суд ответчик"
        );
        Map<String, Double> vecB = strategy.computeVector(
                "кот собака мяукает лает животные питомцы"
        );
        double similarity = strategy.computeSimilarity(vecA, vecB);
        assertThat(similarity).isLessThan(0.1);
    }

    @Test
    void computeVector_valuesShouldSumToOne() {
        Map<String, Double> vector = strategy.computeVector("слово один два три");
        double sum = vector.values().stream().mapToDouble(Double::doubleValue).sum();
        assertThat(sum).isCloseTo(1.0, within(0.01));
    }
}
