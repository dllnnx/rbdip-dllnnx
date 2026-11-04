package com.rbdip.bookstore.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
// JPA-сущность по определению является носителем состояния, отдаваемым
// геттерами; добавлять к ней бизнес-поведение ради PMD DataClass не нужно.
@SuppressWarnings("PMD.DataClass")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column
    private String address;

    @Column
    private String phone;

    protected Customer() {
        // for JPA
    }

    public Customer(String firstName, String lastName, String address, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
    }

    public static String firstNameOf(String fullName) {
        return fullName.split(" ", 2)[0];
    }

    public static String lastNameOf(String fullName) {
        String[] parts = fullName.split(" ", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    public String getFullName() {
        return lastName.isBlank() ? firstName : firstName + " " + lastName;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }
}
