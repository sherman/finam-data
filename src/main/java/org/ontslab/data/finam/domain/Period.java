package org.ontslab.data.finam.domain;

/**
 * @author Denis Gabaydulin
 * @since 11.06.17
 */
public enum Period {
    // TODO: add more periods
    FIVE_MINUTES(3),
    TEN_MINUTES(4);

    private final int id;

    Period(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
