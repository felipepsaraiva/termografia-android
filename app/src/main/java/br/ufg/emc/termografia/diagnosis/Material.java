package br.ufg.emc.termografia.diagnosis;

public class Material {
    public static final String COPPER = "copper";
    public static final String ALUMINIUM = "aluminium";

    private Material() {}

    public static double getConstant(String material) {
        switch (material) {
            case COPPER: return 234.5;
            case ALUMINIUM: return 225.0;

            default:
                throw new IllegalArgumentException("Material \"" + material + "\" is not valid!");
        }
    }
}
