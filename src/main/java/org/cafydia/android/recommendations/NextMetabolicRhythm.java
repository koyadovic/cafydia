package org.cafydia.android.recommendations;

import org.cafydia.android.core.Instant;

/**
 * Created by user on 9/08/14.
 */
public class NextMetabolicRhythm {
    private Integer id;
    private String name;
    private Instant startDate;

    public NextMetabolicRhythm(Integer id, String name, Instant startDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }
}
