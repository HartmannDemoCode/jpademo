package dk.cphbusiness.persistence.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Purpose of this class is to
 * Author: Thomas Hartmann
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name="Boat.deleteAll", query="DELETE FROM Boat")
})
public class Boat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Temporal(TemporalType.DATE)
    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @OneToMany(mappedBy = "boat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // eager because the generic DAO approach doesnt allow for loading specific collections
    private Set<Seat> seats = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( // owning side
            name = "boat_owner",
            joinColumns = @JoinColumn(name = "boat_id"),
            inverseJoinColumns = @JoinColumn(name = "owner_id")
    )
    private Set<Owner> owners = new HashSet<>();

    @ManyToOne
    private Harbour harbour;

    @Temporal(TemporalType.DATE)
    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Builder
    public Boat(String brand, String model, String name, LocalDate registrationDate) {
        this.brand = brand;
        this.model = model;
        this.name = name;
        this.registrationDate = registrationDate;
    }

    @PrePersist
    private void prePersist() {
        this.creationDate = LocalDate.now();
        if (!validatePhoneNumbers(this.seats)) {
            throw new IllegalArgumentException("One or more phone numbers are invalid");
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (!validatePhoneNumbers(this.seats)) {
            throw new IllegalArgumentException("One or more phone numbers are invalid");
        }
    }

    private boolean validatePhoneNumbers(Set<Seat> seats) {
        for (Seat seat : seats) {
            if (!validatePhoneNumber(seat.getNumber())) {
                return false;
            }
        }

        return true;
    }

    public void addPhone(Seat seat) {
        this.seats.add(seat);
        seat.setBoat(this);
    }
    public void removePhone(Seat seat) {
        this.seats.remove(seat);
        seat.setBoat(null);
    }
    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return true;
        }

        return phoneNumber.matches("^[0-9]{8,11}$");
    }

    public void addOwner(Owner owner) {
        this.owners.add(owner);
        owner.getBoats().add(this);
    }
    public void removeOwner(Owner owner) {
        this.owners.remove(owner);
        owner.getBoats().remove(this);
    }

    public void setHarbour(Harbour harbour) {
        this.harbour = harbour;
        harbour.getBoats().add(this);
    }
    public void removeHarbour(Harbour harbour) {
        this.harbour = null;
        harbour.getBoats().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Boat boat = (Boat) o;
        return Objects.equals(id, boat.id) && Objects.equals(brand, boat.brand) && Objects.equals(model, boat.model) && Objects.equals(name, boat.getName()) && Objects.equals(registrationDate, boat.registrationDate) && Objects.equals(creationDate, boat.creationDate);
    }



    @Override
    public int hashCode() {
        return Objects.hash(id, model, brand, creationDate);
    }

    @Override
    public String toString() {
        return "Boat{" +
                "id=" + id +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", tier='" + name + '\'' +
                ", harbour=" + harbour +
                ", creationDate=" + creationDate +
                '}';
    }
    public int getAge(){
        return Period.between(LocalDate.now(), creationDate).getYears();
    }
}