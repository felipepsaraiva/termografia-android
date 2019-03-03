package br.ufg.emc.termografia.util;

// TODO: Avaliar se o caso de x == width e y == height deve ser permitido

public class RelativePoint {
    private double relativeX, relativeY;

    public RelativePoint(int x, int y, int width, int height) {
        setCoordinates(x, y, width, height);
    }

    public RelativePoint(double relativeX, double relativeY) {
        setRelativeCoordinates(relativeX, relativeY);
    }

    public void setCoordinates(int x, int y, int width, int height) {
        if (x < 0 || x >= width) throw new IllegalArgumentException("Absolute X coordinate must be positive and smaller than the width");
        if (y < 0 || y >= height) throw new IllegalArgumentException("Absolute Y coordinate must be positive and smaller than the height");

        this.relativeX = (double)x / (double)width;
        this.relativeY = (double)y / (double)height;
    }

    public void setRelativeCoordinates(double relativeX, double relativeY) {
        if (relativeX < 0 || relativeX >= 1) throw new IllegalArgumentException("Relative X coordinate must be between 0 and 1");
        if (relativeY < 0 || relativeY >= 1) throw new IllegalArgumentException("Relative Y coordinate must be between 0 and 1");

        this.relativeX = relativeX;
        this.relativeY = relativeY;
    }

    public double getRelativeX() {
        return relativeX;
    }

    public double getRelativeY() {
        return relativeY;
    }

    public int getXForWidth(int width) {
        return (int)(relativeX * width);
    }

    public int getYForHeight(int height) {
        return (int)(relativeY * height);
    }
}
