package com.youwo.api.people;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
    name = "pin_rules",
    indexes = {
      @Index(name = "uq_pin_rules_person_id", columnList = "person_id", unique = true),
      @Index(name = "idx_pin_rules_enabled_target", columnList = "enabled,target_position")
    })
public class PinRuleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id", nullable = false)
  private PersonEntity person;

  @Column(name = "target_position", nullable = false)
  private Integer targetPosition;

  @Column(nullable = false)
  private boolean enabled = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public UUID getId() {
    return id;
  }

  public PersonEntity getPerson() {
    return person;
  }

  public void setPerson(PersonEntity person) {
    this.person = person;
  }

  public Integer getTargetPosition() {
    return targetPosition;
  }

  public void setTargetPosition(Integer targetPosition) {
    this.targetPosition = targetPosition;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
