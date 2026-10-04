package pe.upc.simutalk.interviews.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.entities.Question;
import pe.upc.simutalk.interviews.domain.model.queries.CountQuestionsByCriterionIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetQuestionByIdQuery;
import pe.upc.simutalk.interviews.domain.model.queries.GetQuestionsByJobPostingIdQuery;
import pe.upc.simutalk.interviews.domain.services.QuestionQueryService;
import pe.upc.simutalk.repositories.QuestionRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuestionQueryServiceImpl implements QuestionQueryService {

    private final QuestionRepository questionRepository;

    @Override
    public List<Question> handle(GetQuestionsByJobPostingIdQuery query) {
        return questionRepository.findAllByJobPostingIdOrderByPositionAscIdAsc(query.jobPostingId());
    }

    @Override
    public Optional<Question> handle(GetQuestionByIdQuery query) {
        return questionRepository.findById(query.questionId());
    }

    @Override
    public long handle(CountQuestionsByCriterionIdQuery query) {
        return query.criterionId() == null ? 0L : questionRepository.countByCriterionId(query.criterionId());
    }
}
