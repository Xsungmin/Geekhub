package com.GeekHub.userservice.db.entity;

import com.GeekHub.userservice.common.enums.UserStatus;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Getter @Setter
@Builder
@DynamicInsert
@DynamicUpdate
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Column(name="userIdx")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userIdx;

    @Column(name="username")
    private String userName;

    @Column(name = "userId", unique = true)
    private String userId;

    @Column(name="password")
    private String password;

    @Column(name="phone")
    private String phone;

    @Column(name="local_city")
    private String local_city;

    @Column(name="local_school")
    private String local_school;

    @Column(name="userStatus")
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Column
    @Embedded
    private Location location;
}