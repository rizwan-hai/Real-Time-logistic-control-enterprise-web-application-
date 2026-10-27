package com.logistics.controltower.exceptions;

import com.logistics.controltower.audit.AuditService;
import com.logistics.controltower.notifications.NotificationService;
import com.logistics.controltower.security.Role;
import com.logistics.controltower.shipments.ShipmentEntity;
import com.logistics.controltower.shipments.ShipmentRepository;
import com.logistics.controltower.users.UserEntity;
import com.logistics.controltower.users.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExceptionService {
    private static final List<ExceptionStatus> OPEN_STATUSES = List.of(
            ExceptionStatus.OPEN, ExceptionStatus.ACKNOWLEDGED,
            ExceptionStatus.IN_PROGRESS, ExceptionStatus.ESCALATED);
    private final DeliveryExceptionRepository exceptions;
    private final ExceptionHistoryRepository history;
    private final ShipmentRepository shipments;
    private final UserRepository users;
    private final AuditService audit;
    private final NotificationService notifications;
    private final Duration responseDeadline;

    public ExceptionService(DeliveryExceptionRepository exceptions, ExceptionHistoryRepository history,
                            ShipmentRepository shipments, UserRepository users, AuditService audit,
                            NotificationService notifications,
                            @Value("${app.exceptions.response-deadline:PT30M}") Duration responseDeadline) {
        if (responseDeadline.isZero() || responseDeadline.isNegative()) {
            throw new IllegalStateException("Exception response deadline must be positive");
        }
        this.exceptions = exceptions;
        this.history = history;
        this.shipments = shipments;
        this.users = users;
        this.audit = audit;
        this.notifications = notifications;
        this.responseDeadline = responseDeadline;
    }

    @Transactional
    public ExceptionDtos.View create(ExceptionDtos.CreateRequest input, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER, Role.DRIVER);
        ShipmentEntity shipment = shipments.findById(input.shipmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
        if (hasRole(authentication, Role.DRIVER) && !isAssignedDriver(shipment, authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Drivers may report only on assigned shipments");
        }
        String assignee = validateAssignee(input.assignedToEmail());
        DeliveryException exception = exceptions.save(new DeliveryException(shipment, input.type(), input.severity(),
                input.description(), assignee, authentication.getName(), Instant.now().plus(responseDeadline)));
        history.save(new ExceptionHistory(exception, "CREATED", authentication.getName(), input.description()));
        audit.record("EXCEPTION_CREATED", "SUCCESS", "DELIVERY_EXCEPTION", exception.getId(),
                "type=" + input.type() + ";severity=" + input.severity());
        notifyAssignee(exception, "EXCEPTION_CREATED", "An exception requires your attention.");
        return ExceptionDtos.View.from(exception);
    }

    @Transactional(readOnly = true)
    public Page<ExceptionDtos.View> search(ExceptionType type, ExceptionStatus status, ExceptionSeverity severity,
                                           Pageable pageable, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER,
                Role.DRIVER, Role.SECURITY_AUDITOR, Role.VIEWER);
        boolean includeSecurity = hasRole(authentication, Role.ADMIN)
                || hasRole(authentication, Role.LOGISTICS_MANAGER)
                || hasRole(authentication, Role.SECURITY_AUDITOR);
        String actor = hasRole(authentication, Role.DRIVER) ? authentication.getName() : null;
        return exceptions.search(type, status, severity, includeSecurity, actor,
                        ExceptionType.SECURITY_INCIDENT, pageable)
                .map(ExceptionDtos.View::from);
    }

    @Transactional(readOnly = true)
    public ExceptionDtos.View get(long id, Authentication authentication) {
        DeliveryException exception = find(id);
        authorizeRead(exception, authentication);
        return ExceptionDtos.View.from(exception);
    }

    @Transactional
    public ExceptionDtos.View updateStatus(long id, ExceptionDtos.StatusRequest input,
                                           Authentication authentication) {
        DeliveryException exception = findForUpdate(id);
        authorizeRead(exception, authentication);
        ExceptionStatus from = exception.getStatus();
        ExceptionStatus to = input.status();
        if (!allowedTransitions(from).contains(to)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invalid exception status transition");
        }
        authorizeTransition(exception, to, authentication);
        if (to == ExceptionStatus.RESOLVED && (input.notes() == null || input.notes().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resolution notes are required");
        }
        exception.setStatus(to, input.notes());
        history.save(new ExceptionHistory(exception, "STATUS_CHANGED", authentication.getName(),
                from + " -> " + to + (input.notes() == null ? "" : ": " + input.notes())));
        if (to == ExceptionStatus.ESCALATED) {
            audit.record("EXCEPTION_ESCALATED", "SUCCESS", "DELIVERY_EXCEPTION", id, null);
            notifyAssignee(exception, "EXCEPTION_ESCALATED", "An assigned exception has been escalated.");
        } else if (to == ExceptionStatus.RESOLVED) {
            audit.record("EXCEPTION_RESOLVED", "SUCCESS", "DELIVERY_EXCEPTION", id, null);
        } else if (to == ExceptionStatus.CLOSED) {
            audit.record("EXCEPTION_CLOSED", "SUCCESS", "DELIVERY_EXCEPTION", id, null);
        } else {
            audit.record("EXCEPTION_STATUS_CHANGED", "SUCCESS", "DELIVERY_EXCEPTION", id,
                    "from=" + from + ";to=" + to);
        }
        return ExceptionDtos.View.from(exception);
    }

    @Transactional
    public ExceptionDtos.View updateSeverity(long id, ExceptionSeverity severity, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER);
        DeliveryException exception = findForUpdate(id);
        requireSecurityAccess(exception, authentication);
        exception.setSeverity(severity);
        history.save(new ExceptionHistory(exception, "SEVERITY_CHANGED", authentication.getName(),
                "severity=" + severity));
        audit.record("EXCEPTION_SEVERITY_CHANGED", "SUCCESS", "DELIVERY_EXCEPTION", id,
                "severity=" + severity);
        return ExceptionDtos.View.from(exception);
    }

    @Transactional
    public ExceptionDtos.View assign(long id, String assignedToEmail, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER);
        DeliveryException exception = findForUpdate(id);
        requireSecurityAccess(exception, authentication);
        String assignee = validateAssignee(assignedToEmail);
        exception.setAssignedToEmail(assignee);
        history.save(new ExceptionHistory(exception, "ASSIGNED", authentication.getName(),
                "Assigned to " + assignee));
        audit.record("EXCEPTION_ASSIGNED", "SUCCESS", "DELIVERY_EXCEPTION", id, null);
        return ExceptionDtos.View.from(exception);
    }

    @Transactional
    public void addNote(long id, String note, Authentication authentication) {
        DeliveryException exception = findForUpdate(id);
        authorizeRead(exception, authentication);
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER, Role.DRIVER);
        history.save(new ExceptionHistory(exception, "NOTE_ADDED", authentication.getName(), note));
        audit.record("EXCEPTION_NOTE_ADDED", "SUCCESS", "DELIVERY_EXCEPTION", id, null);
    }

    @Transactional(readOnly = true)
    public List<ExceptionDtos.HistoryView> history(long id, Authentication authentication) {
        DeliveryException exception = find(id);
        authorizeRead(exception, authentication);
        return history.findByDeliveryExceptionIdOrderByOccurredAtAsc(id).stream()
                .map(ExceptionDtos.HistoryView::from).toList();
    }

    @Transactional
    public int escalateOverdue() {
        var overdue = exceptions.findOverdueForUpdate(
                List.of(ExceptionStatus.OPEN, ExceptionStatus.ACKNOWLEDGED), Instant.now());
        int count = 0;
        for (DeliveryException exception : overdue) {
            if (exception.getStatus() != ExceptionStatus.OPEN
                    && exception.getStatus() != ExceptionStatus.ACKNOWLEDGED) continue;
            exception.setStatus(ExceptionStatus.ESCALATED, null);
            history.save(new ExceptionHistory(exception, "AUTO_ESCALATED", "SYSTEM",
                    "Response deadline passed"));
            audit.record("EXCEPTION_ESCALATED", "SUCCESS", "DELIVERY_EXCEPTION",
                    exception.getId(), "source=deadline_scheduler");
            notifyAssignee(exception, "EXCEPTION_ESCALATED", "An assigned exception has been escalated.");
            count++;
        }
        return count;
    }

    public long countOpen() { return exceptions.countByStatusIn(OPEN_STATUSES); }
    public long countOverdue() {
        return exceptions.countByStatusInAndResponseDeadlineBefore(OPEN_STATUSES, Instant.now());
    }
    public long countHighSeverity() {
        return exceptions.countBySeverityAndStatusIn(
                ExceptionSeverity.HIGH, List.of(ExceptionStatus.OPEN, ExceptionStatus.ACKNOWLEDGED,
                        ExceptionStatus.IN_PROGRESS, ExceptionStatus.ESCALATED))
                + exceptions.countBySeverityAndStatusIn(
                ExceptionSeverity.CRITICAL, List.of(ExceptionStatus.OPEN, ExceptionStatus.ACKNOWLEDGED,
                        ExceptionStatus.IN_PROGRESS, ExceptionStatus.ESCALATED));
    }

    private DeliveryException find(long id) {
        return exceptions.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exception not found"));
    }
    private DeliveryException findForUpdate(long id) {
        return exceptions.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exception not found"));
    }
    private void authorizeRead(DeliveryException exception, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER,
                Role.DRIVER, Role.SECURITY_AUDITOR, Role.VIEWER);
        requireSecurityAccess(exception, authentication);
        if (hasRole(authentication, Role.DRIVER)
                && !authentication.getName().equalsIgnoreCase(exception.getAssignedToEmail())
                && !authentication.getName().equalsIgnoreCase(exception.getCreatedByEmail())
                && !isAssignedDriver(exception.getShipment(), authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exception is not assigned to this driver");
        }
    }
    private void authorizeTransition(DeliveryException exception, ExceptionStatus target,
                                     Authentication authentication) {
        if (target == ExceptionStatus.CLOSED || target == ExceptionStatus.IN_PROGRESS
                && exception.getStatus() == ExceptionStatus.RESOLVED) {
            requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER);
            return;
        }
        if (hasRole(authentication, Role.DRIVER)) {
            if (!authentication.getName().equalsIgnoreCase(exception.getAssignedToEmail())
                    && !isAssignedDriver(exception.getShipment(), authentication.getName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exception is not assigned to this driver");
            }
            if (!EnumSet.of(ExceptionStatus.ACKNOWLEDGED, ExceptionStatus.IN_PROGRESS,
                    ExceptionStatus.RESOLVED).contains(target)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver cannot perform this exception action");
            }
        } else {
            requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER);
        }
    }
    private void requireSecurityAccess(DeliveryException exception, Authentication authentication) {
        if (exception.getType() == ExceptionType.SECURITY_INCIDENT
                && !hasRole(authentication, Role.ADMIN)
                && !hasRole(authentication, Role.LOGISTICS_MANAGER)
                && !hasRole(authentication, Role.SECURITY_AUDITOR)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Security incident access is restricted");
        }
    }
    private String validateAssignee(String email) {
        if (email == null || email.isBlank()) return null;
        return users.findByEmailIgnoreCase(email.trim())
                .filter(UserEntity::isActive)
                .map(UserEntity::getEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active assignee not found"));
    }
    private void notifyAssignee(DeliveryException exception, String type, String message) {
        String email = exception.getAssignedToEmail();
        if (email == null) return;
        if (exception.getType() == ExceptionType.SECURITY_INCIDENT) {
            boolean authorizedRecipient = users.findByEmailIgnoreCase(email)
                    .map(user -> user.getRoles().stream().anyMatch(role -> role == Role.ADMIN
                            || role == Role.LOGISTICS_MANAGER || role == Role.SECURITY_AUDITOR))
                    .orElse(false);
            if (!authorizedRecipient) return;
        }
        notifications.notifyUser(email, type, message, "DELIVERY_EXCEPTION", exception.getId());
    }
    private boolean isAssignedDriver(ShipmentEntity shipment, String email) {
        return shipment.getDriver() != null
                && shipment.getDriver().getUser().getEmail().equalsIgnoreCase(email);
    }
    private boolean hasRole(Authentication authentication, Role role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.name()));
    }
    private void requireAnyRole(Authentication authentication, Role... roles) {
        for (Role role : roles) if (hasRole(authentication, role)) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role for this operation");
    }
    private EnumSet<ExceptionStatus> allowedTransitions(ExceptionStatus from) {
        return switch (from) {
            case OPEN -> EnumSet.of(ExceptionStatus.ACKNOWLEDGED, ExceptionStatus.IN_PROGRESS,
                    ExceptionStatus.ESCALATED, ExceptionStatus.RESOLVED);
            case ACKNOWLEDGED -> EnumSet.of(ExceptionStatus.IN_PROGRESS, ExceptionStatus.ESCALATED,
                    ExceptionStatus.RESOLVED);
            case IN_PROGRESS -> EnumSet.of(ExceptionStatus.ESCALATED, ExceptionStatus.RESOLVED);
            case ESCALATED -> EnumSet.of(ExceptionStatus.IN_PROGRESS, ExceptionStatus.RESOLVED);
            case RESOLVED -> EnumSet.of(ExceptionStatus.CLOSED, ExceptionStatus.IN_PROGRESS);
            case CLOSED -> EnumSet.noneOf(ExceptionStatus.class);
        };
    }
}
