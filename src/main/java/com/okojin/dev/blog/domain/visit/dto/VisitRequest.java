package com.okojin.dev.blog.domain.visit.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VisitRequest(@NotNull UUID sessionId) {}
