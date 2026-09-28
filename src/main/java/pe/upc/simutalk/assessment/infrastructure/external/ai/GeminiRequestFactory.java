package pe.upc.simutalk.assessment.infrastructure.external.ai;

import pe.upc.simutalk.shared.infrastructure.external.ai.GenerativeAiClient;

import java.util.Map;

/**
 * Builds assessment's scoring prompt and, for inspection in tests, the full {@code generateContent}
 * body the shared {@link GenerativeAiClient} sends with it.
 * <p>
 * Privacy (Ley 29733): the payload is made ONLY of the anonymized transcript and the criterion.
 * This class has no access to candidate data at all; keep it that way.
 */
public final class GeminiRequestFactory {

    private GeminiRequestFactory() {
    }

    public static Map<String, Object> build(String anonymizedTranscript, String criterionName, String criterionDescription) {
        return GenerativeAiClient.requestBody(prompt(anonymizedTranscript, criterionName, criterionDescription));
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
