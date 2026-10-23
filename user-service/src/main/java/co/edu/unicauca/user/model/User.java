package co.edu.unicauca.user.model;

import jakarta.persistence.*;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    private String email;

    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name")
    private Set<Role> roles = new HashSet<>();

    protected User() {
    }

    public User(String username, String passwordHash, String fullName, String email, boolean active, Role... roles) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.active = active;
        this.roles = roles.length == 0 ? new HashSet<>() : EnumSet.of(roles[0], roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public Set<Role> getRoles() { return roles; }

    public void setActive(boolean active) { this.active = active; }
}
