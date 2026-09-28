package pe.upc.simutalk.shared.interfaces.acl;

/**
 * Contract other bounded contexts (recruitment) use to ask interviews about interview
 * scripts. Implemented by interviews.
 */
public interface InterviewsContextFacade {

    /** Number of interview questions that evaluate the given criterion. */
    long countQuestionsByCriterionId(Long criterionId);
}
