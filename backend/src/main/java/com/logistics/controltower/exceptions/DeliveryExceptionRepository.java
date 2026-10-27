package com.logistics.controltower.exceptions;

import java.time.Instant;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface DeliveryExceptionRepository extends JpaRepository<DeliveryException, Long> {
    @Query("""
            select e from DeliveryException e
            where (:type is null or e.type = :type)
              and (:status is null or e.status = :status)
              and (:severity is null or e.severity = :severity)
              and (:includeSecurity = true or e.type <> :securityIncident)
              and (:actorEmail is null or e.assignedToEmail = :actorEmail
                   or e.createdByEmail = :actorEmail or e.shipment.driver.user.email = :actorEmail)
            """)
    Page<DeliveryException> search(@Param("type") ExceptionType type,
                                   @Param("status") ExceptionStatus status,
                                   @Param("severity") ExceptionSeverity severity,
                                   @Param("includeSecurity") boolean includeSecurity,
                                   @Param("actorEmail") String actorEmail,
                                   @Param("securityIncident") ExceptionType securityIncident,
                                   Pageable pageable);

    long countByStatus(ExceptionStatus status);
    long countByStatusIn(Collection<ExceptionStatus> statuses);
    long countBySeverityAndStatusIn(ExceptionSeverity severity, Collection<ExceptionStatus> statuses);
    long countByStatusInAndResponseDeadlineBefore(Collection<ExceptionStatus> statuses, Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DeliveryException e where e.id = :id")
    java.util.Optional<DeliveryException> findByIdForUpdate(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from DeliveryException e where e.status in :statuses and e.responseDeadline < :now")
    Collection<DeliveryException> findOverdueForUpdate(@Param("statuses") Collection<ExceptionStatus> statuses,
                                                        @Param("now") Instant now);
}
