package pe.upc.simutalk.recruitment.application.internal.queryservices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pe.upc.simutalk.recruitment.domain.model.aggregates.JobPosting;
import pe.upc.simutalk.recruitment.domain.model.commands.CreateJobPostingCommand;
import pe.upc.simutalk.recruitment.domain.model.queries.GetJobPostingByIdQuery;
import pe.upc.simutalk.recruitment.domain.model.queries.SearchJobPostingsQuery;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.CriterionType;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.JobPostingViewer;
import pe.upc.simutalk.recruitment.domain.model.valueobjects.Weight;
import pe.upc.simutalk.recruitment.infrastructure.persistence.jpa.repositories.JobPostingRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobPostingQueryServiceImplTest {

    private final JobPostingRepository repository = mock(JobPostingRepository.class);
    private final JobPostingQueryServiceImpl service = new JobPostingQueryServiceImpl(repository);
    private JobPosting draftOfCompany1;
    private JobPosting publishedOfCompany2;

    private static JobPosting posting(long id, long companyId) {
        var posting = new JobPosting(new CreateJobPostingCommand("Title " + id, "Desc", companyId, null, false));
        ReflectionTestUtils.setField(posting, "id", id);
        return posting;
    }

    @BeforeEach
    void setUp() {
        draftOfCompany1 = posting(1, 1);
        publishedOfCompany2 = posting(2, 2);
        publishedOfCompany2.addCriterion("Comunicación", "x", new Weight(100), CriterionType.COMPETENCY, null, false);
        publishedOfCompany2.publish();
        when(repository.findAllByOrderByIdAsc()).thenReturn(List.of(draftOfCompany1, publishedOfCompany2));
        when(repository.findWithCriteriaById(1L)).thenReturn(Optional.of(draftOfCompany1));
    }

    @Test
    void outsidersOnlyListPublishedPostings() {
        var result = service.handle(new SearchJobPostingsQuery(null, null, JobPostingViewer.ofCompany(null)));

        assertThat(result).containsExactly(publishedOfCompany2);
    }

    @Test
    void ownerListsItsDraftsToo() {
        var result = service.handle(new SearchJobPostingsQuery(null, null, JobPostingViewer.ofCompany(1L)));

        assertThat(result).containsExactly(draftOfCompany1, publishedOfCompany2);
    }

    @Test
    void draftByIdIsNotFoundForOtherCompanies() {
        assertThat(service.handle(new GetJobPostingByIdQuery(1L, JobPostingViewer.ofCompany(2L)))).isEmpty();
        assertThat(service.handle(new GetJobPostingByIdQuery(1L, JobPostingViewer.ofCompany(1L)))).isPresent();
        assertThat(service.handle(new GetJobPostingByIdQuery(1L, JobPostingViewer.unrestrictedViewer()))).isPresent();
    }
}
