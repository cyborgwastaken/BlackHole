package com.blackhole.physics;

import org.joml.Vector3f;

/**
 * Represents a black hole with its physical properties.
 * Implements the same physics as the C++ BlackHole struct.
 */
public class BlackHole {
    
    private final Vector3f position;
    private final double mass;
    private final double radius; // Visual radius for rendering
    private final double schwarzschildRadius;
    
    /**
     * Creates a new black hole.
     * 
     * @param position Position in 3D space
     * @param mass Mass in kilograms
     */
    public BlackHole(Vector3f position, double mass) {
        this.position = new Vector3f(position);
        this.mass = mass;
        this.radius = calculateVisualRadius(mass);
        this.schwarzschildRadius = 2.0 * Constants.GRAVITATIONAL_CONSTANT * mass / 
                                   (Constants.SPEED_OF_LIGHT * Constants.SPEED_OF_LIGHT);
    }
    
    /**
     * Creates Sagittarius A* black hole at origin.
     */
    public static BlackHole createSagittariusA() {
        return new BlackHole(new Vector3f(0.0f, 0.0f, 0.0f), Constants.SAG_A_MASS);
    }
    
    /**
     * Checks if a point intercepts the black hole (within event horizon).
     * 
     * @param x X coordinate
     * @param y Y coordinate  
     * @param z Z coordinate
     * @return true if point is within event horizon
     */
    public boolean intercepts(double x, double y, double z) {
        double dx = x - position.x;
        double dy = y - position.y;
        double dz = z - position.z;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared < schwarzschildRadius * schwarzschildRadius;
    }
    
    /**
     * Checks if a point intercepts the black hole.
     * 
     * @param point Position to check
     * @return true if point is within event horizon
     */
    public boolean intercepts(Vector3f point) {
        return intercepts(point.x, point.y, point.z);
    }
    
    /**
     * Calculate visual radius for rendering (arbitrary scaling for visibility).
     */
    private double calculateVisualRadius(double mass) {
        // Use a scaling factor to make the black hole visible
        return schwarzschildRadius * 2.0;
    }
    
    // Getters
    public Vector3f getPosition() {
        return new Vector3f(position);
    }
    
    public double getMass() {
        return mass;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public double getSchwarzchildRadius() {
        return schwarzschildRadius;
    }
    
    @Override
    public String toString() {
        return String.format("BlackHole{position=%s, mass=%.2e kg, rs=%.2e m}", 
                           position, mass, schwarzschildRadius);
    }
}