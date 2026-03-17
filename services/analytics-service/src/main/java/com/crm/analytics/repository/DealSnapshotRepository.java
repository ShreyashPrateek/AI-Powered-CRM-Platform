package com.crm.analytics.repository;

import com.crm.analytics.entity.DealSnapshot;
import com.crm.analytics.enums.DealStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface DealSnapshotRepository extends JpaRepository<DealSnapshot, Long> {

    // ── Revenue ──────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(d.value), 0) FROM DealSnapshot d WHERE d.stage = 'CLOSED_WON'")
    BigDecimal totalRevenue();

    @Query("""
        SELECT COALESCE(SUM(d.value), 0) FROM DealSnapshot d
        WHERE d.stage = 'CLOSED_WON'
          AND d.updatedAt >= :from AND d.updatedAt < :to
        """)
    BigDecimal revenueInPeriod(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
        SELECT FUNCTION('to_char', CAST(d.updatedAt AS date), 'YYYY-MM') AS month,
               COALESCE(SUM(d.value), 0) AS revenue
        FROM DealSnapshot d
        WHERE d.stage = 'CLOSED_WON'
          AND d.updatedAt >= :from
        GROUP BY FUNCTION('to_char', CAST(d.updatedAt AS date), 'YYYY-MM')
        ORDER BY month
        """)
    List<Object[]> monthlyRevenue(@Param("from") Instant from);

    @Query("""
        SELECT d.ownerId, COALESCE(SUM(d.value), 0) AS revenue
        FROM DealSnapshot d
        WHERE d.stage = 'CLOSED_WON'
          AND d.updatedAt >= :from AND d.updatedAt < :to
        GROUP BY d.ownerId
        ORDER BY revenue DESC
        """)
    List<Object[]> revenueByOwner(@Param("from") Instant from, @Param("to") Instant to);

    // ── Pipeline ─────────────────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(d.value), 0) FROM DealSnapshot d WHERE d.stage NOT IN ('CLOSED_WON','CLOSED_LOST')")
    BigDecimal pipelineValue();

    @Query("""
        SELECT d.stage, COUNT(d), COALESCE(SUM(d.value), 0)
        FROM DealSnapshot d
        GROUP BY d.stage
        ORDER BY d.stage
        """)
    List<Object[]> dealCountAndValueByStage();

    // ── Win / Loss ────────────────────────────────────────────────────────────

    @Query("SELECT COUNT(d) FROM DealSnapshot d WHERE d.stage = 'CLOSED_WON'")
    long countWon();

    @Query("SELECT COUNT(d) FROM DealSnapshot d WHERE d.stage = 'CLOSED_LOST'")
    long countLost();

    @Query("SELECT COUNT(d) FROM DealSnapshot d WHERE d.stage IN ('CLOSED_WON','CLOSED_LOST')")
    long countTerminal();

    // ── Sales performance per owner ───────────────────────────────────────────

    @Query("""
        SELECT d.ownerId,
               COUNT(d)                                                   AS totalDeals,
               SUM(CASE WHEN d.stage = 'CLOSED_WON'  THEN 1 ELSE 0 END) AS won,
               SUM(CASE WHEN d.stage = 'CLOSED_LOST' THEN 1 ELSE 0 END) AS lost,
               COALESCE(SUM(CASE WHEN d.stage = 'CLOSED_WON' THEN d.value ELSE 0 END), 0) AS revenue
        FROM DealSnapshot d
        WHERE d.updatedAt >= :from AND d.updatedAt < :to
        GROUP BY d.ownerId
        ORDER BY revenue DESC
        """)
    List<Object[]> salesPerformanceByOwner(@Param("from") Instant from, @Param("to") Instant to);

    // ── Deal probability / success prediction ────────────────────────────────

    @Query("""
        SELECT d.stage, AVG(d.probability)
        FROM DealSnapshot d
        WHERE d.stage NOT IN ('CLOSED_WON','CLOSED_LOST')
        GROUP BY d.stage
        """)
    List<Object[]> avgProbabilityByStage();

    @Query("""
        SELECT d FROM DealSnapshot d
        WHERE d.stage NOT IN ('CLOSED_WON','CLOSED_LOST')
          AND d.ownerId = :ownerId
        ORDER BY d.probability DESC
        """)
    List<DealSnapshot> openDealsByOwnerOrderedByProbability(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT d FROM DealSnapshot d
        WHERE d.stage NOT IN ('CLOSED_WON','CLOSED_LOST')
          AND d.expectedCloseDate <= :deadline
        ORDER BY d.probability DESC
        """)
    List<DealSnapshot> dealsClosingSoon(@Param("deadline") java.time.LocalDate deadline);

    // ── Average deal value ────────────────────────────────────────────────────

    @Query("SELECT COALESCE(AVG(d.value), 0) FROM DealSnapshot d WHERE d.stage = 'CLOSED_WON'")
    BigDecimal avgWonDealValue();
}
