package com.logistics.controltower.exceptions;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exceptions")
public class ExceptionController {
    private final ExceptionService exceptions;
    public ExceptionController(ExceptionService exceptions) { this.exceptions = exceptions; }

    @GetMapping
    public Page<ExceptionDtos.View> search(
            @RequestParam(required = false) ExceptionType type,
            @RequestParam(required = false) ExceptionStatus status,
            @RequestParam(required = false) ExceptionSeverity severity,
            Pageable pageable, Authentication authentication) {
        return exceptions.search(type, status, severity, pageable, authentication);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','DISPATCHER','DRIVER')")
    public ExceptionDtos.View create(@Valid @RequestBody ExceptionDtos.CreateRequest request,
                                     Authentication authentication) {
        return exceptions.create(request, authentication);
    }

    @GetMapping("/{id}")
    public ExceptionDtos.View get(@PathVariable long id, Authentication authentication) {
        return exceptions.get(id, authentication);
    }

    @PutMapping("/{id}/status")
    public ExceptionDtos.View updateStatus(@PathVariable long id,
                                           @Valid @RequestBody ExceptionDtos.StatusRequest request,
                                           Authentication authentication) {
        return exceptions.updateStatus(id, request, authentication);
    }

    @PutMapping("/{id}/severity")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER')")
    public ExceptionDtos.View updateSeverity(@PathVariable long id,
                                             @Valid @RequestBody ExceptionDtos.SeverityRequest request,
                                             Authentication authentication) {
        return exceptions.updateSeverity(id, request.severity(), authentication);
    }

    @PutMapping("/{id}/assignment")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','DISPATCHER')")
    public ExceptionDtos.View assign(@PathVariable long id,
                                     @Valid @RequestBody ExceptionDtos.AssignmentRequest request,
                                     Authentication authentication) {
        return exceptions.assign(id, request.assignedToEmail(), authentication);
    }

    @PostMapping("/{id}/notes")
    public void addNote(@PathVariable long id, @Valid @RequestBody ExceptionDtos.NoteRequest request,
                        Authentication authentication) {
        exceptions.addNote(id, request.notes(), authentication);
    }

    @GetMapping("/{id}/history")
    public List<ExceptionDtos.HistoryView> history(@PathVariable long id, Authentication authentication) {
        return exceptions.history(id, authentication);
    }
}
