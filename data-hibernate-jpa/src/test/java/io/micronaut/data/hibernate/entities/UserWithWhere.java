package io.micronaut.data.hibernate.entities;

import io.micronaut.data.annotation.Embeddable;
import io.micronaut.data.annotation.Where;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
@Where(value = "@.deleted = false")
public class UserWithWhere {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    private String email;
    private Boolean deleted;
    @Embedded
    private Audit audit = new Audit();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }
}
