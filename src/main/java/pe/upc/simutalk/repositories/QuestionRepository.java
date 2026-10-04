package pe.upc.simutalk.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.upc.simutalk.entities.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findAllByJobPostingIdOrderByPositionAscIdAsc(Long jobPostingId);

    long countByJobPostingId(Long jobPostingId);

    long countByCriterionId(Long criterionId);
}
