package pe.upc.simutalk.recruitment.domain.model.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pe.upc.simutalk.recruitment.domain.model.commands.CreateJobPostingCommand;
import pe.upc.simutalk.recruitment.domain.model.entities.EvaluationCriterion;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionType;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingStatus;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.Weight;
import pe.upc.simutalk.shared.domain.exceptions.BusinessRuleViolationException;
import pe.upc.simutalk.shared.domain.exceptions.ResourceNotFoundException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobPostingTest {

    private JobPosting jobPosting;

    @BeforeEach
    void setUp() {
        jobPosting = new JobPosting(new CreateJobPostingCommand(
                "Backend Developer", "APIs REST con Spring Boot", 1L, LocalDate.now().plusDays(30), true));
    }

    @Test
    void startsAsDraftWithoutCriteria() {
        assertThat(jobPosting.getStatus()).isEqualTo(JobPostingStatus.DRAFT);
        assertThat(jobPosting.getCriteria()).isEmpty();
        assertThat(jobPosting.getTotalWeight()).isZero();
    }

    @Test
    void publishFailsWithoutCriteria() {
        assertThatThrownBy(jobPosting::publish)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("at least one evaluation criterion");
        assertThat(jobPosting.getStatus()).isEqualTo(JobPostingStatus.DRAFT);
    }

    @Test
    void publishFailsWhenWeightsAddUpToLessThanOneHundred() {
        addCompetency("Comunicación", 60);
        addCompetency("Trabajo en equipo", 30);

        assertThatThrownBy(jobPosting::publish)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("current total: 90");
    }

    @Test
    void publishFailsWhenWeightsAddUpToMoreThanOneHundred() {
        addCompetency("Comunicación", 60);
        addCompetency("Trabajo en equipo", 50);

        assertThatThrownBy(jobPosting::publish)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("current total: 110");
    }

    @Test
    void publishSucceedsWhenWeightsAddUpToExactlyOneHundred() {
        addCompetency("Comunicación", 60);
        jobPosting.addCriterion("Java SE", "Certificación vigente", new Weight(40),
                CriterionType.CERTIFICATION, "Oracle Certified Professional Java SE 21", true);

        jobPosting.publish();

        assertThat(jobPosting.getStatus()).isEqualTo(JobPostingStatus.PUBLISHED);
        assertThat(jobPosting.getTotalWeight()).isEqualTo(100);
    }

    @Test
    void criteriaCannotChangeOnceTheJobPostingIsPublished() {
        var criterion = addCompetency("Comunicación", 100);
        jobPosting.publish();

        assertThatThrownBy(() -> addCompetency("Liderazgo", 10))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> jobPosting.removeCriterion(criterion.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void criterionNamesAreUniqueIgnoringCase() {
        addCompetency("Comunicación", 50);

        assertThatThrownBy(() -> addCompetency("  comunicación ", 50))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void certificationCriterionRequiresCertificationName() {
        assertThatThrownBy(() -> jobPosting.addCriterion("AWS", "Certificación cloud", new Weight(20),
                CriterionType.CERTIFICATION, " ", true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void competencyCriterionDiscardsCertificationData() {
        var criterion = jobPosting.addCriterion("Comunicación", "Claridad", new Weight(30),
                CriterionType.COMPETENCY, "Algo", true);

        assertThat(criterion.getCertificationName()).isNull();
        assertThat(criterion.isMandatory()).isFalse();
    }

    @Test
    void updateAndRemoveCriterionById() {
        var criterion = addCompetency("Comunicación", 30);

        jobPosting.updateCriterion(criterion.getId(), "Comunicación oral", "Claridad al hablar", new Weight(45),
                CriterionType.COMPETENCY, null, false);
        assertThat(jobPosting.getTotalWeight()).isEqualTo(45);
        assertThat(criterion.getName()).isEqualTo("Comunicación oral");

        jobPosting.removeCriterion(criterion.getId());
        assertThat(jobPosting.getCriteria()).isEmpty();
    }

    @Test
    void updatingUnknownCriterionFails() {
        assertThatThrownBy(() -> jobPosting.removeCriterion(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cannotGoBackToDraft() {
        assertThatThrownBy(() -> jobPosting.changeStatus(JobPostingStatus.DRAFT))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void closedJobPostingIsReadOnly() {
        jobPosting.changeStatus(JobPostingStatus.CLOSED);

        assertThatThrownBy(() -> jobPosting.updateDetails("Otro", "Otra", null, true))
                .isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(jobPosting::close).isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(jobPosting::publish).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void anonymizedScreeningIsFixedAfterPublishing() {
        addCompetency("Comunicación", 100);
        jobPosting.publish();

        assertThatThrownBy(() -> jobPosting.updateDetails("Backend Developer", "APIs", null, false))
                .isInstanceOf(BusinessRuleViolationException.class);

        jobPosting.updateDetails("Backend Developer Sr", "APIs", null, true);
        assertThat(jobPosting.getTitle()).isEqualTo("Backend Developer Sr");
    }

    @Test
    void publishedJobPostingCannotBeDeleted() {
        addCompetency("Comunicación", 100);
        jobPosting.publish();

        assertThatThrownBy(jobPosting::ensureCanBeDeleted).isInstanceOf(BusinessRuleViolationException.class);

        jobPosting.close();
        jobPosting.ensureCanBeDeleted();
    }

    private long nextId = 1;

    private EvaluationCriterion addCompetency(String name, int weight) {
        var criterion = jobPosting.addCriterion(name, "Descripción de " + name, new Weight(weight),
                CriterionType.COMPETENCY, null, false);
        ReflectionTestUtils.setField(criterion, "id", nextId++);
        return criterion;
    }
}
