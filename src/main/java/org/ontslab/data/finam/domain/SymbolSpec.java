package org.ontslab.data.finam.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.joda.time.LocalDate;

/**
 * @author Denis Gabaydulin
 * @since 11.06.17
 */
public class SymbolSpec {
    private String name;
    private Period period;
    private int marketId;
    private int em;
    private int days;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public int getMarketId() {
        return marketId;
    }

    public void setMarketId(int marketId) {
        this.marketId = marketId;
    }

    public int getEm() {
        return em;
    }

    public void setEm(int em) {
        this.em = em;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("period", period)
                .add("marketId", marketId)
                .add("em", em)
                .add("days", days)
                .toString();
    }
}
