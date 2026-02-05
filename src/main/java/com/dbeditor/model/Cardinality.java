package com.dbeditor.model;

/**
 * Représente une cardinalité dans un MCD (Merise)
 * Format : min,max
 * Exemples : 0,1 / 1,1 / 0,n / 1,n
 */
public class Cardinality {
    private String min; // "0" ou "1"
    private String max; // "1" ou "n" (n = plusieurs, illimité)
    
    /**
     * Constructeur avec min et max séparés
     */
    public Cardinality(String min, String max) {
        this.min = min;
        this.max = max;
    }
    
    /**
     * Constructeur à partir d'une chaîne "min,max"
     */
    public Cardinality(String cardinalityString) {
        String[] parts = cardinalityString.split(",");
        if (parts.length == 2) {
            this.min = parts[0].trim();
            this.max = parts[1].trim();
        } else {
            // Valeur par défaut
            this.min = "0";
            this.max = "n";
        }
    }
    
    public String getMin() { return min; }
    public void setMin(String min) { this.min = min; }
    
    public String getMax() { return max; }
    public void setMax(String max) { this.max = max; }
    
    /**
     * Retourne true si la cardinalité est multiple (max = n)
     */
    public boolean isMultiple() {
        return "n".equals(max) || "N".equals(max);
    }
    
    /**
     * Retourne true si la cardinalité est obligatoire (min = 1)
     */
    public boolean isMandatory() {
        return "1".equals(min);
    }
    
    /**
     * Retourne true si la cardinalité est optionnelle (min = 0)
     */
    public boolean isOptional() {
        return "0".equals(min);
    }
    
    /**
     * Retourne true si c'est une cardinalité "un à un" (max = 1)
     */
    public boolean isOne() {
        return "1".equals(max);
    }
    
    @Override
    public String toString() {
        return min + "," + max;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Cardinality)) return false;
        Cardinality other = (Cardinality) obj;
        return this.min.equals(other.min) && this.max.equals(other.max);
    }
    
    @Override
    public int hashCode() {
        return (min + "," + max).hashCode();
    }
    
    // Cardinalités prédéfinies communes
    public static final Cardinality ZERO_ONE = new Cardinality("0", "1");
    public static final Cardinality ONE_ONE = new Cardinality("1", "1");
    public static final Cardinality ZERO_N = new Cardinality("0", "n");
    public static final Cardinality ONE_N = new Cardinality("1", "n");
}