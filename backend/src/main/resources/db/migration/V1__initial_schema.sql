CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(254) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(40) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE token_revocations (
    token_id VARCHAR(36) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (token_id),
    INDEX idx_token_revocations_expiry (expires_at)
) ENGINE=InnoDB;

CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    email VARCHAR(254) NULL,
    phone VARCHAR(40) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_customers_name (name)
) ENGINE=InnoDB;

CREATE TABLE drivers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    license_number VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_drivers_user UNIQUE (user_id),
    CONSTRAINT uk_drivers_license UNIQUE (license_number),
    CONSTRAINT fk_drivers_user FOREIGN KEY (user_id) REFERENCES app_users (id)
) ENGINE=InnoDB;

CREATE TABLE vehicles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    registration VARCHAR(30) NOT NULL,
    type VARCHAR(60) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_vehicles_registration UNIQUE (registration)
) ENGINE=InnoDB;

CREATE TABLE shipments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tracking_number VARCHAR(40) NOT NULL,
    customer_id BIGINT NOT NULL,
    origin VARCHAR(250) NOT NULL,
    destination VARCHAR(250) NOT NULL,
    delivery_address VARCHAR(500) NOT NULL,
    delivery_instructions VARCHAR(1000) NULL,
    description VARCHAR(1000) NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    driver_id BIGINT NULL,
    vehicle_id BIGINT NULL,
    estimated_delivery_at TIMESTAMP(6) NULL,
    actual_delivery_at TIMESTAMP(6) NULL,
    outcome_notes VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_shipments_tracking UNIQUE (tracking_number),
    CONSTRAINT fk_shipments_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_shipments_driver FOREIGN KEY (driver_id) REFERENCES drivers (id),
    CONSTRAINT fk_shipments_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles (id),
    INDEX idx_shipments_status_created (status, created_at),
    INDEX idx_shipments_customer (customer_id),
    INDEX idx_shipments_driver_status (driver_id, status),
    INDEX idx_shipments_vehicle_status (vehicle_id, status),
    INDEX idx_shipments_estimated_delivery (estimated_delivery_at)
) ENGINE=InnoDB;

CREATE TABLE shipment_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    actor_email VARCHAR(254) NOT NULL,
    notes VARCHAR(1000) NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_shipment_history_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
    INDEX idx_shipment_history_shipment_time (shipment_id, occurred_at)
) ENGINE=InnoDB;

CREATE TABLE delivery_exceptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shipment_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    assigned_to_email VARCHAR(254) NULL,
    created_by_email VARCHAR(254) NOT NULL,
    resolution_notes VARCHAR(2000) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    response_deadline TIMESTAMP(6) NOT NULL,
    resolved_at TIMESTAMP(6) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_exceptions_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id),
    INDEX idx_exceptions_status_deadline (status, response_deadline),
    INDEX idx_exceptions_type_severity (type, severity),
    INDEX idx_exceptions_assignee (assigned_to_email)
) ENGINE=InnoDB;

CREATE TABLE exception_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exception_id BIGINT NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    actor_email VARCHAR(254) NOT NULL,
    notes VARCHAR(2000) NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_exception_history_exception FOREIGN KEY (exception_id) REFERENCES delivery_exceptions (id),
    INDEX idx_exception_history_exception_time (exception_id, occurred_at)
) ENGINE=InnoDB;

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_email VARCHAR(254) NOT NULL,
    type VARCHAR(60) NOT NULL,
    message VARCHAR(500) NOT NULL,
    entity_type VARCHAR(60) NULL,
    entity_id BIGINT NULL,
    `read` BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_notifications_recipient_time (recipient_email, created_at)
) ENGINE=InnoDB;

CREATE TABLE audit_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(80) NOT NULL,
    actor_email VARCHAR(254) NULL,
    outcome VARCHAR(80) NOT NULL,
    target_type VARCHAR(80) NULL,
    target_id BIGINT NULL,
    correlation_id VARCHAR(80) NULL,
    metadata VARCHAR(1000) NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_events_time (occurred_at),
    INDEX idx_audit_events_actor_time (actor_email, occurred_at),
    INDEX idx_audit_events_target (target_type, target_id)
) ENGINE=InnoDB;
