package com.example.educationalqualityproject.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationForm {

    private List<String> resourceIds = new ArrayList<>();
    private String teacherId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds == null ? new ArrayList<>() : resourceIds;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
