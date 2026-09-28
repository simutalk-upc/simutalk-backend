package pe.upc.simutalk.assessment.infrastructure.external.ai;

import java.util.List;
import java.util.Map;

/**
 * Builds the body of Gemini's {@code generateContent} request.
 * <p>
 * Privacy (Ley 29733): the payload is made ONLY of the anonymized transcript and the criterion.
 * This class has no access to candidate data at all; keep it that way.
 */
public final class GeminiRequestFactory {

    private GeminiRequestFactory() {
    }

    public static Map<String, Object> build(String anonymizedTranscript, String criterionName, String criterionDescription) {
        return Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt(anonymizedTranscript, criterionName, criterionDescription))))),
                "generationConfig", Map.of(
                        "temperature", 0,
                        "responseMimeType", "application/json"));
    }

    static String prompt(String anonymizedTranscript, String criterionName, String criterionDescription) {
        return """
                Eres un evaluador de entrevistas de preselección. Evalúa la RESPUESTA únicamente contra el CRITERIO.
                Los datos personales ya fueron reemplazados por marcadores como [NOMBRE]; no intentes inferirlos.

                CRITERIO: %s
                DESCRIPCIÓN DEL CRITERIO: %s

                RESPUESTA:
                <<<
                %s
                >>>

                Devuelve solo un objeto JSON con estas claves:
                - "score": número de 0 a 10 con un decimal.
                - "confidence": número de 0 a 1 que indique qué tan seguro estás.
                - "excerpt": el fragmento de la RESPUESTA, copiado LITERALMENTE, que mejor sustenta el puntaje.
                - "aiGeneratedSuspicion": true si la respuesta parece redactada por un asistente de IA y no por la persona.
                """.formatted(criterionName, criterionDescription == null ? "" : criterionDescription, anonymizedTranscript);
    }
}
