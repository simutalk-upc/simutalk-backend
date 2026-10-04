package pe.upc.simutalk.assessment.application.internal.queryservices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upc.simutalk.assessment.application.internal.outboundservices.acl.ExternalContextsService;
import pe.upc.simutalk.assessment.application.internal.outboundservices.anonymization.TranscriptAnonymizer;
import pe.upc.simutalk.entities.Assessment;
import pe.upc.simutalk.entities.Evidence;
import pe.upc.simutalk.assessment.domain.model.queries.CountAssessmentsByJobPostingIdsQuery;
import pe.upc.simutalk.assessment.domain.model.queries.CountEvidencesByJobPostingIdsQuery;
import pe.upc.simutalk.assessment.domain.model.queries.GetAssessmentByIdQuery;
import pe.upc.simutalk.assessment.domain.model.queries.GetCriterionAveragesQuery;
import pe.upc.simutalk.assessment.domain.model.valueobjects.CriterionAverage;
import pe.upc.simutalk.assessment.domain.model.queries.GetAssessmentByInterviewSessionIdQuery;
import pe.upc.simutalk.assessment.domain.model.queries.GetEvidencesByCriterionScoreQuery;
import pe.upc.simutalk.assessment.domain.model.queries.GetRankingByJobPostingIdQuery;
import pe.upc.simutalk.assessment.domain.model.valueobjects.CandidateCode;
import pe.upc.simutalk.assessment.domain.model.valueobjects.Ranking;
import pe.upc.simutalk.assessment.domain.model.valueobjects.RankingEntry;
import pe.upc.simutalk.assessment.domain.services.AssessmentQueryService;
import pe.upc.simutalk.assessment.domain.services.RankingPolicy;
import pe.upc.simutalk.assessment.infrastructure.persistence.jpa.repositories.AssessmentRepository;
import pe.upc.simutalk.exceptions.ResourceNotFoundException;
import pe.upc.simutalk.shared.interfaces.acl.CandidatePersonalData;
import pe.upc.simutalk.shared.interfaces.acl.CriterionView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AssessmentQueryServiceImpl implements AssessmentQueryService {

    private final AssessmentRepository assessmentRepository;
    private final ExternalContextsService externalContextsService;
    private final TranscriptAnonymizer transcriptAnonymizer;
    private final String anonymizationSecret;

    public AssessmentQueryServiceImpl(AssessmentRepository assessmentRepository,
                                      ExternalContextsService externalContextsService,
                                      TranscriptAnonymizer transcriptAnonymizer,
                                      @Value("${app.anonymization.secret}") String anonymizationSecret) {
        this.assessmentRepository = assessmentRepository;
        this.externalContextsService = externalContextsService;
        this.transcriptAnonymizer = transcriptAnonymizer;
        this.anonymizationSecret = anonymizationSecret;
    }

    @Override
    public Optional<Assessment> handle(GetAssessmentByIdQuery query) {
        return assessmentRepository.findWithScoresById(query.assessmentId()).map(AssessmentQueryServiceImpl::loaded);
    }

    @Override
    public Optional<Assessment> handle(GetAssessmentByInterviewSessionIdQuery query) {
        return assessmentRepository.findWithScoresByInterviewSessionId(query.interviewSessionId())
                .map(AssessmentQueryServiceImpl::loaded);
    }

    @Override
    public List<Evidence> handle(GetEvidencesByCriterionScoreQuery query) {
        var assessment = assessmentRepository.findWithScoresById(query.assessmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", query.assessmentId()));
        var evidences = assessment.getCriterionScore(query.criterionScoreId()).getEvidences();
        if (!externalContextsService.isAnonymizedScreening(assessment.getJobPostingId())) {
            return List.copyOf(evidences);
        }
        var candidate = externalContextsService.fetchCandidatePersonalData(assessment.getCandidateId()).orElse(null);
        // Redacted copies (not persisted); offsets still point to the original transcript.
        return evidences.stream()
                .map(evidence -> new Evidence(evidence.getAnswerId(),
                        transcriptAnonymizer.anonymize(evidence.getExcerpt(), candidate).text(),
                        evidence.getStartOffset(), evidence.getEndOffset()))
                .toList();
    }

    @Override
    public Ranking handle(GetRankingByJobPostingIdQuery query) {
        if (!externalContextsService.existsJobPosting(query.jobPostingId())) {
            throw new ResourceNotFoundException("Job posting", query.jobPostingId());
        }
        var forced = externalContextsService.isAnonymizedScreening(query.jobPostingId());
        var anonymized = RankingPolicy.isAnonymized(query.anonymizedRequested(), forced);
        var mandatoryCertifications = externalContextsService.fetchCriteria(query.jobPostingId()).stream()
                .filter(criterion -> !criterion.isCompetency() && criterion.mandatory())
                .map(CriterionView::criterionId)
                .collect(Collectors.toSet());

        var assessments = new ArrayList<>(assessmentRepository.findAllWithScoresByJobPostingId(query.jobPostingId()));
        assessments.forEach(AssessmentQueryServiceImpl::loaded);
        assessments.sort(RankingPolicy.ORDER);

        var entries = new ArrayList<RankingEntry>();
        for (var index = 0; index < assessments.size(); index++) {
            var assessment = assessments.get(index);
            var rank = index + 1;
            var verified = externalContextsService.fetchVerifiedCertificationCount(assessment.getCandidateId());
            var code = CandidateCode.of(assessment.getCandidateId(), assessment.getJobPostingId(), anonymizationSecret).value();
            var person = anonymized ? Optional.<CandidatePersonalData>empty()
                    : externalContextsService.fetchCandidatePersonalData(assessment.getCandidateId());
            var breakdown = assessment.getCriterionScores().stream()
                    .map(score -> new RankingEntry.CriterionBreakdown(score.getId(), score.getCriterionId(),
                            score.getCriterionName(), score.getCriterionKind(), score.getWeightApplied(), score.getScore(),
                            score.getConfidence(), score.getEvidences().size()))
                    .toList();
            entries.add(new RankingEntry(rank, assessment.getId(), assessment.getApplicationId(),
                    anonymized ? null : assessment.getCandidateId(), code,
                    person.map(CandidatePersonalData::fullName).orElse(null),
                    person.map(CandidatePersonalData::documentNumber).orElse(null),
                    assessment.getWeightedScore(), breakdown,
                    RankingPolicy.badgesFor(assessment, rank, verified, mandatoryCertifications)));
        }
        return new Ranking(query.jobPostingId(), anonymized, forced, entries);
    }

    @Override
    public List<CriterionAverage> handle(GetCriterionAveragesQuery query) {
        return assessmentRepository.averageScoreByCriterion(query.jobPostingId()).stream()
                .map(row -> new CriterionAverage(row.getCriterionId(), row.getCriterionName(),
                        BigDecimal.valueOf(row.getAverageScore()).setScale(1, RoundingMode.HALF_UP), row.getAssessedCount()))
                .toList();
    }

    @Override
    public long handle(CountAssessmentsByJobPostingIdsQuery query) {
        return query.jobPostingIds().isEmpty() ? 0L : assessmentRepository.countByJobPostingIdIn(query.jobPostingIds());
    }

    @Override
    public long handle(CountEvidencesByJobPostingIdsQuery query) {
        return query.jobPostingIds().isEmpty() ? 0L : assessmentRepository.countEvidencesByJobPostingIds(query.jobPostingIds());
    }

    /** Initializes the batched collections inside the transaction (open-in-view is disabled). */
    private static Assessment loaded(Assessment assessment) {
        assessment.getIntegrityFlags().size();
        assessment.getCriterionScores().forEach(score -> score.getEvidences().size());
        return assessment;
    }
}
