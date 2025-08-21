package com.blackhole.physics;

/**
 * Physical constants and parameters used in black hole simulation.
 * Maintains exact values from the original C++ implementation.
 */
public final class Constants {
    
    // Physical constants
    public static final double SPEED_OF_LIGHT = 299792458.0; // m/s
    public static final double GRAVITATIONAL_CONSTANT = 6.67430e-11; // m³/(kg⋅s²)
    
    // Sagittarius A* black hole parameters (exact values from C++)
    public static final double SAG_A_MASS = 8.54e36; // kg
    public static final double SAG_A_SCHWARZSCHILD_RADIUS = 
        2.0 * GRAVITATIONAL_CONSTANT * SAG_A_MASS / (SPEED_OF_LIGHT * SPEED_OF_LIGHT);
    
    // Numerical integration parameters
    public static final double DEFAULT_STEP_SIZE = 1e7; // λ step size for RK4
    public static final double ESCAPE_RADIUS = 1e30; // Distance considered "escaped"
    public static final int MAX_INTEGRATION_STEPS = 60000;
    
    // Rendering constants
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final int COMPUTE_WIDTH_HIGH = 200;
    public static final int COMPUTE_HEIGHT_HIGH = 150; 
    public static final int MAX_OBJECTS = 16; // Maximum gravitational bodies
    
    // Camera parameters
    public static final float DEFAULT_FOV = 60.0f; // degrees
    public static final float MIN_CAMERA_RADIUS = 1e10f;
    public static final float MAX_CAMERA_RADIUS = 1e12f;
    public static final float DEFAULT_CAMERA_RADIUS = 6.34194e10f;
    
    // Accretion disk parameters
    public static final double DISK_INNER_RADIUS = 3.0 * SAG_A_SCHWARZSCHILD_RADIUS; // 3rs
    public static final double DISK_OUTER_RADIUS = 10.0 * SAG_A_SCHWARZSCHILD_RADIUS; // 10rs
    public static final double DISK_THICKNESS = 0.1 * SAG_A_SCHWARZSCHILD_RADIUS;
    
    private Constants() {
        // Utility class - prevent instantiation
    }
}