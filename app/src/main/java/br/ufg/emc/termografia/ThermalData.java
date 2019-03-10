package br.ufg.emc.termografia;

public class ThermalData {
    private int[] temperatures;
    private int width;
    private int height;

    public ThermalData(int[] temperatures, int width, int height) {
        this.temperatures = temperatures;
        this.width = width;
        this.height = height;
    }

    public int[] getTemperatures() {
        return temperatures;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
