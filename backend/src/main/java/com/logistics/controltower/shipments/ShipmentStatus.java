package com.logistics.controltower.shipments;

public enum ShipmentStatus {
    CREATED,
    READY_FOR_DISPATCH,
    ASSIGNED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    FAILED_DELIVERY,
    CANCELLED,
    RETURNED
}
