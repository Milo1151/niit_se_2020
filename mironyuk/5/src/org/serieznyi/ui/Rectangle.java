package org.serieznyi.ui;

import org.serieznyi.ui.util.Assert;

public abstract class Rectangle {
    private boolean enabled = true;

    private final int x;
    private final int y;
    private final int height;
    private final int width;
    private final String caption;

    protected Rectangle(int x, int y, int height, int width, String caption) {
        Assert.zeroOrPositiveNumber(x);
        this.x = x;

        Assert.zeroOrPositiveNumber(y);
        this.y = y;

        Assert.positiveNumber(height);
        this.height = height;

        Assert.positiveNumber(width);
        this.width = width;

        Assert.requireNotEmptyString(caption);
        this.caption = caption;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getCaption() {
        return caption;
    }

    public void disable()
    {
        enabled = false;
    }

    public void enable()
    {
        enabled = true;
    }

    @Override
    public String toString() {
        return String.format(
            "%s в координатах [%s, %s], размер [h=%s, w=%s], название: \"%s\"",
                getTypeName(),
                x,
                y,
                height,
                width,
                caption
        );
    }

    protected abstract String getTypeName();
}

















