package com.example.educationalqualityproject.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "material_checkouts")
@CompoundIndex(name = "reservation_resource_unique", def = "{'reservationId': 1, 'resourceId': 1}", unique = true)
public class MaterialCheckout {

    @Id
    private String id;
    private String reservationId;
    private String resourceId;
    private String requester;
    private MaterialCheckoutStatus status = MaterialCheckoutStatus.WITHDRAWN;
    private String withdrawnBy;
    private Instant withdrawnAt;
    private String returnedBy;
    private Instant returnedAt;

    public MaterialCheckout() {
    }

    public MaterialCheckout(String reservationId, String resourceId, String requester, String withdrawnBy) {
        this.reservationId = reservationId;
        this.resourceId = resourceId;
        this.requester = requester;
        this.withdrawnBy = withdrawnBy;
        this.withdrawnAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public MaterialCheckoutStatus getStatus() {
        return status;
    }

    public void setStatus(MaterialCheckoutStatus status) {
        this.status = status;
    }

    public String getWithdrawnBy() {
        return withdrawnBy;
    }

    public void setWithdrawnBy(String withdrawnBy) {
        this.withdrawnBy = withdrawnBy;
    }

    public Instant getWithdrawnAt() {
        return withdrawnAt;
    }

    public void setWithdrawnAt(Instant withdrawnAt) {
        this.withdrawnAt = withdrawnAt;
    }

    public String getReturnedBy() {
        return returnedBy;
    }

    public void setReturnedBy(String returnedBy) {
        this.returnedBy = returnedBy;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(Instant returnedAt) {
        this.returnedAt = returnedAt;
    }
}
