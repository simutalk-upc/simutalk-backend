package pe.upc.simutalk.analytics.domain.services;

import pe.upc.simutalk.analytics.domain.model.queries.GetCarbonSavingsReportQuery;
import pe.upc.simutalk.analytics.domain.model.queries.GetCompanySummaryReportQuery;
import pe.upc.simutalk.analytics.domain.model.queries.GetCriterionAveragesReportQuery;
import pe.upc.simutalk.analytics.domain.model.queries.GetFunnelReportQuery;
import pe.upc.simutalk.analytics.domain.model.valueobjects.CarbonSavingsReport;
import pe.upc.simutalk.analytics.domain.model.valueobjects.CompanySummaryReport;
import pe.upc.simutalk.analytics.domain.model.valueobjects.CriterionAveragesReport;
import pe.upc.simutalk.analytics.domain.model.valueobjects.FunnelReport;

public interface ReportQueryService {

    FunnelReport handle(GetFunnelReportQuery query);

    CriterionAveragesReport handle(GetCriterionAveragesReportQuery query);

    CarbonSavingsReport handle(GetCarbonSavingsReportQuery query);

    CompanySummaryReport handle(GetCompanySummaryReportQuery query);
}
