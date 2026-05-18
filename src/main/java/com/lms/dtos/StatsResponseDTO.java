package com.lms.dtos;

import java.util.List;
import java.util.Map;

/** Dashboard payload. Metric keys vary by caller role; see StatsService for the full list. */
public class StatsResponseDTO {
    private Map<String, Long> metrics;
    private List<DashboardActivityDTO> recentActivities;

    public StatsResponseDTO(Map<String, Long> metrics, List<DashboardActivityDTO> recentActivities) {
        this.metrics = metrics;
        this.recentActivities = recentActivities;
    }

    public Map<String, Long> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Long> metrics) { this.metrics = metrics; }
    public List<DashboardActivityDTO> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<DashboardActivityDTO> recentActivities) { this.recentActivities = recentActivities; }
}
