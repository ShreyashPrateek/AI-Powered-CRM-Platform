package com.crm.analytics.repository;

import com.crm.analytics.entity.LeadSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LeadSnapshotRepository extends JpaRepository<LeadSnapshot, Long> {

    @Query("SELECT COUNT(l) FROM LeadSnapshot l")
    long totalLeads();

    @Query("SELECT COUNT(l) FROM LeadSnapshot l WHERE l.status = 'QUALIFIED'")
    long qualifiedLeads();

    @Query("SELECT COUNT(l) FROM LeadSnapshot l WHERE l.status = 'LOST'")
    long lostLeads();

    @Query("""
        SELECT l.status, COUNT(l)
        FROM LeadSnapshot l
        GROUP BY l.status
        """)
    List<Object[]> countByStatus();

    @Query("""
        SELECT l.assignedUserId,
               COUNT(l)                                                          AS total,
               SUM(CASE WHEN l.status = 'QUALIFIED' THEN 1 ELSE 0 END)          AS qualified,
               SUM(CASE WHEN l.status = 'LOST'      THEN 1 ELSE 0 END)          AS lost
        FROM LeadSnapshot l
        WHERE l.createdAt >= :from AND l.createdAt < :to
        GROUP BY l.assignedUserId
        ORDER BY qualified DESC
        """)
    List<Object[]> conversionByOwner(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT FUNCTION('to_char', CAST(l.createdAt AS date), 'YYYY-MM') AS month,
               COUNT(l)                                                   AS total,
               SUM(CASE WHEN l.status = 'QUALIFIED' THEN 1 ELSE 0 END)   AS qualified
        FROM LeadSnapshot l
        WHERE l.createdAt >= :from
        GROUP BY FUNCTION('to_char', CAST(l.createdAt AS date), 'YYYY-MM')
        ORDER BY month
        """)
    List<Object[]> monthlyLeadConversion(@Param("from") Instant from);
}
