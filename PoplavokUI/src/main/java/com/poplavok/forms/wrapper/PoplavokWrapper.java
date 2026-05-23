package com.poplavok.forms.wrapper;

import com.poplavok.data.model.Poplavok;

public record PoplavokWrapper(Poplavok poplavok) {
    @Override
    public String toString() {
        return "#" + poplavok.getId() + " " + poplavok.getTickerSymbol() + " " + poplavok.getName();
    }
}
