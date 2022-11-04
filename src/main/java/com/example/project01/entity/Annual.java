package com.example.project01.entity;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Table(name = "annual")
@Data
public class Annual {
    @Id
    private int mno;

    @ColumnDefault("0")
    private double tnumber;

    @ColumnDefault("0")
    private double unumber;

    @ColumnDefault("0")
    private double lnumber;

    private String start_date;
    private String end_date;

    @Builder
    public Annual(int mno,String start_date,String end_date) {
        this.mno = mno;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public Annual() {}
}
