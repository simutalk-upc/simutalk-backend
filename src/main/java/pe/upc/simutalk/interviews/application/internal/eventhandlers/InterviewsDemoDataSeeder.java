package pe.upc.simutalk.interviews.application.internal.eventhandlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pe.upc.simutalk.interviews.domain.model.aggregates.Question;
import pe.upc.simutalk.interviews.domain.model.commands.*;
import pe.upc.simutalk.interviews.domain.model.queries.GetQuestionsByJobPostingIdQuery;
import pe.upc.simutalk.interviews.domain.model.valueobjects.QuestionOrigin;
import pe.upc.simutalk.interviews.domain.services.InterviewSessionCommandService;
import pe.upc.simutalk.interviews.domain.services.QuestionCommandService;
import pe.upc.simutalk.interviews.domain.services.QuestionQueryService;
import pe.upc.simutalk.shared.interfaces.events.DemoApplicationsSubmittedEvent;
import pe.upc.simutalk.shared.interfaces.events.DemoInterviewsCompletedEvent;
import pe.upc.simutalk.shared.interfaces.events.DemoJobPostingDraftedEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Interviews part of the demo data. Reacts synchronously, inside recruitment's demo seeding
 * transaction, to the shared demo events:
 * <ul>
 *   <li>{@link DemoJobPostingDraftedEvent}: writes a 6-question script over the posting's
 *       COMPETENCY criteria, one of them allowing a follow-up.</li>
 *   <li>{@link DemoApplicationsSubmittedEvent}: INTERVIEWING applications get an IN_PROGRESS
 *       session with 3 and 5 answers; ASSESSED ones a COMPLETED session with the 6 answers,
 *       one of them including a follow-up. RECEIVED applications are left untouched.</li>
 * </ul>
 * Everything goes through the command services, so the same rules as the API apply.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewsDemoDataSeeder {

    private static final int DEMO_SESSION_VALIDITY_DAYS = 14;

    private final QuestionCommandService questionCommandService;
    private final QuestionQueryService questionQueryService;
    private final InterviewSessionCommandService interviewSessionCommandService;
    private final ApplicationEventPublisher eventPublisher;

    private record DemoQuestion(int criterionIndex, String statement, int maxDurationSeconds, boolean allowsFollowUp) {
    }

    /** criterionIndex: 0 = analytical thinking, 1 = communication (order of the drafted event). */
    private static final List<DemoQuestion> SCRIPT = List.of(
            new DemoQuestion(0, "Un cliente reporta que sus ventas mensuales cayeron 15 % respecto al mes anterior. "
                    + "¿Cómo usarías los datos disponibles para encontrar la causa?", 240, true),
            new DemoQuestion(0, "Recibes una tabla de pedidos con clientes duplicados, montos vacíos y fechas en formatos "
                    + "distintos. ¿Qué pasos seguirías para limpiarla antes de analizarla?", 180, false),
            new DemoQuestion(0, "Explica la diferencia entre un LEFT JOIN y un INNER JOIN y cuándo usarías cada uno "
                    + "al cruzar la tabla de clientes con la de pedidos.", 150, false),
            new DemoQuestion(0, "¿Cómo detectarías y qué harías con valores atípicos en los montos de facturación?",
                    150, false),
            new DemoQuestion(1, "¿Cómo le presentarías un hallazgo negativo a un gerente comercial que no tiene "
                    + "formación técnica?", 180, false),
            new DemoQuestion(1, "Un cliente cuestiona las cifras de tu reporte en una reunión. ¿Cómo le explicas de dónde "
                    + "salen los números?", 180, false));

    private record DemoAnswer(String transcript, int durationSeconds) {
    }

    /** Transcripts per demo interview, one per script question, in script order. */
    private static final List<List<DemoAnswer>> ASSESSED_ANSWERS = List.of(
            List.of(
                    new DemoAnswer("Primero confirmaría que la caída es real y no un problema de carga: comparo el total del "
                            + "reporte con la fuente y reviso si faltan días. Luego la descompongo por región, canal y "
                            + "categoría para ver dónde se concentra, y separo volumen de ticket promedio: no es lo mismo "
                            + "vender menos unidades que vender más barato. Con eso armo dos o tres hipótesis y las valido "
                            + "con el área comercial.", 205),
                    new DemoAnswer("Empezaría perfilando la tabla: cuántas filas, cuántos nulos por columna y cuántos "
                            + "clientes aparecen más de una vez con el mismo documento. Para los duplicados me quedaría con "
                            + "el registro más reciente usando ROW_NUMBER particionado por documento. Las fechas las "
                            + "normalizaría a formato ISO y los montos vacíos no los llenaría con cero: los marcaría y "
                            + "preguntaría si son pedidos anulados.", 170),
                    new DemoAnswer("El INNER JOIN solo devuelve clientes que tienen pedidos; el LEFT JOIN devuelve todos los "
                            + "clientes y deja nulos donde no hay pedidos. Si quiero saber cuántos clientes no compraron "
                            + "este mes uso LEFT JOIN y filtro donde el id del pedido es nulo. Para sumar ventas por "
                            + "cliente, el INNER JOIN es suficiente.", 120),
                    new DemoAnswer("Calcularía el rango intercuartílico y marcaría lo que queda fuera de 1,5 veces ese rango, "
                            + "pero no lo borraría de inmediato. Revisaría algunos casos con el equipo de facturación: a "
                            + "veces es una venta corporativa legítima y a veces un error de digitación con ceros de más. "
                            + "En el reporte mostraría la mediana además del promedio.", 140),
                    new DemoAnswer("Empezaría por la conclusión en una frase, sin tecnicismos: por ejemplo, que la zona norte "
                            + "explica casi toda la caída. Luego mostraría un solo gráfico que lo respalde y cerraría con "
                            + "lo que se puede hacer. Si pregunta por el método, tengo el detalle en un anexo, pero no "
                            + "abro con eso.", 150),
                    new DemoAnswer("Le mostraría la trazabilidad: de qué sistema sale cada cifra, qué filtros apliqué y en qué "
                            + "fecha se extrajo. Si la diferencia es con su propio número, busco juntos el origen, que "
                            + "suele ser un filtro distinto, como incluir o no las devoluciones. Me comprometo a enviarle "
                            + "la conciliación por escrito.", 160)),
            List.of(
                    new DemoAnswer("Lo primero es comparar contra el mismo mes del año anterior, porque una caída mensual "
                            + "puede ser estacional. Si se confirma, filtraría por producto y por tienda para ver si hay "
                            + "un grupo que arrastra el resultado, y cruzaría con cambios de precio o quiebres de stock "
                            + "de ese mes.", 190),
                    new DemoAnswer("Unificaría las fechas con una función de conversión y dejaría en una tabla aparte las que "
                            + "no se puedan convertir, para revisarlas. Los duplicados los identificaría por documento y "
                            + "correo, porque el nombre suele venir escrito de formas distintas. Antes de borrar nada "
                            + "guardaría una copia de la tabla original.", 160),
                    new DemoAnswer("Con INNER JOIN pierdo a los clientes sin pedidos, así que para un reporte de cobertura "
                            + "de cartera usaría LEFT JOIN desde clientes. Hay que tener cuidado al contar después: se "
                            + "cuenta el id del pedido, no las filas, porque el LEFT JOIN genera una fila aunque no haya "
                            + "pedido.", 115),
                    new DemoAnswer("Graficaría la distribución de montos por mes para ver si los atípicos se concentran en una "
                            + "fecha o en un vendedor. Si son errores, los corrijo en la fuente con quien corresponda; si "
                            + "son reales, los analizo por separado para que no distorsionen el promedio del resto.", 125),
                    new DemoAnswer("Traduciría el hallazgo a dinero y a una decisión: cuánto se dejó de vender y qué se podría "
                            + "hacer la próxima semana. Evito porcentajes sobre porcentajes y uso comparaciones que el "
                            + "gerente conozca, como el mismo mes del año pasado.", 135),
                    new DemoAnswer("Primero escucharía cuál es la cifra que no le cuadra. Después le mostraría el cálculo paso "
                            + "a paso, con el mismo filtro que él usa, y si encuentro un error lo reconozco en la reunión "
                            + "y envío el reporte corregido ese mismo día.", 140)));

    /** Follow-up to the first question, recorded in the second ASSESSED interview. */
    private static final DemoAnswer FOLLOW_UP = new DemoAnswer("Si la caída se concentra en una sola tienda, revisaría "
            + "primero eventos operativos de ese mes: si cerró algunos días, si cambió el horario o si hubo quiebre de stock "
            + "en los productos que más vende. Lo validaría con el jefe de tienda antes de presentarlo como causa.", 95);

    private static final List<List<DemoAnswer>> INTERVIEWING_ANSWERS = List.of(
            List.of(
                    new DemoAnswer("Revisaría si la caída es igual en todos los productos o solo en algunos. Haría una tabla "
                            + "dinámica por categoría y mes para comparar, y hablaría con ventas para saber si hubo "
                            + "cambios de precio o promociones que terminaron.", 150),
                    new DemoAnswer("Usaría Power Query para quitar duplicados y dar formato de fecha a la columna. Los montos "
                            + "vacíos los revisaría uno por uno si son pocos; si son muchos, preguntaría al área de "
                            + "facturación antes de decidir qué hacer con ellos.", 130),
                    new DemoAnswer("El LEFT JOIN trae todos los registros de la tabla de la izquierda aunque no tengan pareja "
                            + "en la otra, y el INNER JOIN solo los que coinciden en ambas. Para listar clientes con y sin "
                            + "pedidos usaría el LEFT JOIN.", 95)),
            List.of(
                    new DemoAnswer("Separaría la caída en cantidad de pedidos y valor promedio por pedido. Si bajaron los "
                            + "pedidos, miro canales y campañas; si bajó el valor, miro descuentos y mezcla de productos. "
                            + "Así sé a qué área llevarle la pregunta.", 160),
                    new DemoAnswer("Con SQL haría un SELECT DISTINCT sobre las columnas clave para ver cuántos duplicados hay "
                            + "y luego los eliminaría conservando el de menor id. Para las fechas usaría TO_DATE con cada "
                            + "formato detectado y dejaría registro de las filas que modifiqué.", 145),
                    new DemoAnswer("Si hago INNER JOIN entre clientes y pedidos, un cliente que nunca compró desaparece del "
                            + "resultado. Con LEFT JOIN aparece con valores nulos, que es lo que necesito si quiero medir "
                            + "clientes inactivos.", 90),
                    new DemoAnswer("Primero confirmaría si el valor es posible: una factura de diez veces el promedio puede ser "
                            + "un cliente corporativo. Si es un error, lo excluyo del análisis y lo reporto; si es real, "
                            + "lo dejo pero lo señalo en el gráfico.", 110),
                    new DemoAnswer("Le diría directamente qué pasó y por qué, con un ejemplo concreto de una tienda o un "
                            + "producto. Prefiero un gráfico de barras simple antes que una tabla llena de números, y "
                            + "terminaría con una recomendación.", 120)));

    @EventListener
    public void on(DemoJobPostingDraftedEvent event) {
        var criterionIds = event.competencyCriterionIds();
        for (var demo : SCRIPT) {
            questionCommandService.handle(new CreateQuestionCommand(event.jobPostingId(), criterionIds.get(demo.criterionIndex()),
                    demo.statement(), demo.maxDurationSeconds(), QuestionOrigin.MANUAL, demo.allowsFollowUp()));
        }
        log.info("Interviews demo data: {} questions written for job posting {}", SCRIPT.size(), event.jobPostingId());
    }

    @EventListener
    public void on(DemoApplicationsSubmittedEvent event) {
        var script = questionQueryService.handle(new GetQuestionsByJobPostingIdQuery(event.jobPostingId()));
        var assessed = event.applications().stream().filter(app -> "ASSESSED".equals(app.targetStage())).toList();
        var interviewing = event.applications().stream().filter(app -> "INTERVIEWING".equals(app.targetStage())).toList();

        var completedSessions = new ArrayList<Long>();
        for (var index = 0; index < assessed.size() && index < ASSESSED_ANSWERS.size(); index++) {
            var sessionId = startSession(assessed.get(index).applicationId());
            answer(sessionId, script, ASSESSED_ANSWERS.get(index), index == 1);
            interviewSessionCommandService.handle(new CompleteInterviewSessionCommand(sessionId));
            completedSessions.add(sessionId);
        }
        for (var index = 0; index < interviewing.size() && index < INTERVIEWING_ANSWERS.size(); index++) {
            var sessionId = startSession(interviewing.get(index).applicationId());
            answer(sessionId, script, INTERVIEWING_ANSWERS.get(index), false);
        }
        log.info("Interviews demo data: {} completed and {} in-progress sessions", assessed.size(), interviewing.size());
        eventPublisher.publishEvent(new DemoInterviewsCompletedEvent(event.jobPostingId(), completedSessions));
    }

    private Long startSession(Long applicationId) {
        var session = interviewSessionCommandService.handle(
                new CreateInterviewSessionCommand(applicationId, LocalDate.now().plusDays(DEMO_SESSION_VALIDITY_DAYS)));
        interviewSessionCommandService.handle(new StartInterviewSessionCommand(session.getId()));
        return session.getId();
    }

    private void answer(Long sessionId, List<Question> script, List<DemoAnswer> answers, boolean withFollowUp) {
        for (var index = 0; index < answers.size(); index++) {
            var question = script.get(index);
            var demo = answers.get(index);
            var recorded = interviewSessionCommandService.handle(new RecordAnswerCommand(sessionId, question.getId(),
                    demo.transcript(), null, demo.durationSeconds(), false, null));
            if (withFollowUp && question.isAllowsFollowUp()) {
                interviewSessionCommandService.handle(new RecordAnswerCommand(sessionId, question.getId(),
                        FOLLOW_UP.transcript(), null, FOLLOW_UP.durationSeconds(), true, recorded.getId()));
            }
        }
    }
}
