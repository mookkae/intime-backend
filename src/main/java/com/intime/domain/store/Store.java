package com.intime.domain.store;

import com.intime.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "store")
public class Store extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private int estimatedWaitMinutes;

    private Store(String name, String address, int estimatedWaitMinutes) {
        this.name = name;
        this.address = address;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
    }

    public static Store create(String name, String address, int estimatedWaitMinutes) {
        return new Store(name, address, estimatedWaitMinutes);
    }
}
