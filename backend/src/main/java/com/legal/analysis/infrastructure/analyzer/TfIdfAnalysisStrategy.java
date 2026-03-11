package com.legal.analysis.infrastructure.analyzer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TfIdfAnalysisStrategy implements TextAnalysisStrategy {

    private static final Set<String> STOP_WORDS = Set.of(
            "и", "в", "во", "не", "что", "он", "на", "я", "с", "со", "как", "а", "то",
            "все", "она", "так", "его", "но", "да", "ты", "к", "у", "же", "вы", "за",
            "бы", "по", "только", "ее", "мне", "было", "вот", "от", "меня", "еще",
            "нет", "о", "из", "ему", "теперь", "когда", "даже", "ну", "вдруг", "ли",
            "если", "уже", "или", "ни", "быть", "был", "него", "до", "вас", "нибудь",
            "опять", "уж", "вам", "ведь", "там", "потом", "себя", "ничего", "ей",
            "может", "они", "тут", "где", "есть", "надо", "ней", "для", "мы", "тебя",
            "их", "чем", "была", "сам", "чтоб", "без", "будто", "чего", "раз",
            "тоже", "себе", "под", "будет", "ж", "тогда", "кто", "этот", "того",
            "потому", "этого", "какой", "совсем", "ним", "здесь", "этом", "один",
            "почти", "мой", "тем", "чтобы", "нее", "сейчас", "были", "куда",
            "зачем", "всех", "никогда", "можно", "при", "наконец", "два", "об",
            "другой", "хоть", "после", "над", "больше", "тот", "через", "эти",
            "нас", "про", "всего", "них", "какая", "много", "разве", "три", "эту",
            "моя", "впрочем", "хорошо", "свою", "этой", "перед", "иногда", "лучше",
            "чуть", "том", "нельзя", "такой", "им", "более", "всегда", "конечно",
            "всю", "между", "the", "a", "an", "and", "or", "but", "in", "on", "at",
            "to", "for", "of", "with", "by", "from", "is", "was", "are", "were",
            "be", "been", "being", "have", "has", "had", "do", "does", "did",
            "will", "would", "could", "should", "may", "might", "shall", "can",
            "that", "this", "these", "those", "it", "its", "he", "she", "they",
            "we", "you", "i", "me", "him", "her", "us", "them"
    );

    @Override
    public Map<String, Double> computeVector(String text) {
        List<String> tokens = tokenize(text);
        if (tokens.isEmpty()) return new HashMap<>();

        Map<String, Long> termFreq = tokens.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        long totalTerms = tokens.size();
        Map<String, Double> tfVector = new HashMap<>();
        termFreq.forEach((term, freq) ->
                tfVector.put(term, (double) freq / totalTerms));

        return tfVector;
    }

    @Override
    public double computeSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        if (vectorA.isEmpty() || vectorB.isEmpty()) return 0.0;

        double dotProduct = 0.0;
        for (Map.Entry<String, Double> entry : vectorA.entrySet()) {
            if (vectorB.containsKey(entry.getKey())) {
                dotProduct += entry.getValue() * vectorB.get(entry.getKey());
            }
        }

        double magnitudeA = Math.sqrt(vectorA.values().stream()
                .mapToDouble(v -> v * v).sum());
        double magnitudeB = Math.sqrt(vectorB.values().stream()
                .mapToDouble(v -> v * v).sum());

        if (magnitudeA == 0 || magnitudeB == 0) return 0.0;

        return dotProduct / (magnitudeA * magnitudeB);
    }

    @Override
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();

        return Arrays.stream(text.toLowerCase()
                        .replaceAll("[^а-яёa-z0-9\\s]", " ")
                        .split("\\s+"))
                .filter(token -> token.length() > 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toList());
    }
}
