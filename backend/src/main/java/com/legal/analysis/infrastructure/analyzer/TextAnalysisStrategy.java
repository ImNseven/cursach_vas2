package com.legal.analysis.infrastructure.analyzer;

import java.util.List;
import java.util.Map;

public interface TextAnalysisStrategy {

    Map<String, Double> computeVector(String text);

    double computeSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB);

    List<String> tokenize(String text);
}
