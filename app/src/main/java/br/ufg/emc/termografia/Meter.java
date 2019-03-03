package br.ufg.emc.termografia;

import br.ufg.emc.termografia.util.Converter;
import br.ufg.emc.termografia.util.RelativePoint;

public class Meter extends RelativePoint {
    private boolean ambient = false, selected = false;
    private double temperature = 0, avgDiscrepancy = 0, difference = 0;

    public Meter() {
        super(0, 0);
    }

    public Meter(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public Meter(double relativeX, double relativeY) {
        super(relativeX, relativeY);
    }

    public void setAmbient(boolean ambient) {
        this.ambient = ambient;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void updateTemperature(int[] values, int width, int height) {
        double average = 0;
        int x = getXForWidth(width), y = getYForHeight(height);
        int temperature, smallest = Integer.MAX_VALUE, biggest = Integer.MIN_VALUE;

        if (x == 0) x++;
        else if (x == width - 1 || x == width) x = width - 2;

        if (y == 0) y++;
        else if (y == height - 1 || y == height) y = height - 2;

        int centralIndex = (y * width) + x;
        int[] indexes = new int[] {
                centralIndex - 1, centralIndex, centralIndex + 1,
                centralIndex - width - 1, centralIndex - width, centralIndex - width + 1,
                centralIndex + width - 1, centralIndex + width, centralIndex + width + 1
        };

        for (int index : indexes) {
            temperature = values[index];
            smallest = Math.min(smallest, temperature);
            biggest = Math.max(biggest, temperature);

            average += temperature;
        }

        average = average / indexes.length;
        this.temperature = Converter.milliKelvinToCelsius(average);
        this.avgDiscrepancy = Converter.fromMilli(biggest - smallest);
    }

    public double updateDifference(double ambientTemperature) {
        difference = temperature - ambientTemperature;
        return difference;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean isSelected() {
        return selected;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getAverageDiscrepancy() {
        return avgDiscrepancy;
    }

    public double getPercentageAverageDiscrepancy() {
        return avgDiscrepancy / temperature * 100;
    }

    public double getDifference() {
        return difference;
    }
}
