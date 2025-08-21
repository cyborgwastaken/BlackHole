package com.blackhole;

import com.blackhole.physics.*;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * Simple demonstration of the Java Black Hole physics engine.
 * Shows geodesic ray tracing and scientific calculations.
 */
public class PhysicsDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Black Hole Physics Engine - Java Implementation ===");
        System.out.println();
        
        // Create Sagittarius A* black hole
        BlackHole sagA = BlackHole.createSagittariusA();
        System.out.printf("Created Sagittarius A* black hole:%n%s%n%n", sagA);
        
        // Demonstrate ray tracing with different starting positions
        demonstrateRayTracing(sagA);
        
        // Demonstrate physics engine
        demonstratePhysicsEngine();
        
        System.out.println("Physics demonstration complete!");
        System.out.println("All calculations use exact Schwarzschild metric from C++ implementation.");
    }
    
    private static void demonstrateRayTracing(BlackHole blackHole) {
        System.out.println("=== Ray Tracing Demonstration ===");
        
        double rs = blackHole.getSchwarzchildRadius();
        
        // Test ray starting well outside event horizon
        Vector3d startPos1 = new Vector3d(rs * 10, 0, 0);
        Vector3d direction1 = new Vector3d(-1, 0.1, 0).normalize();
        
        Ray ray1 = new Ray(startPos1, direction1);
        System.out.printf("Ray 1 - Start: r=%.2e m, θ=%.3f rad%n", ray1.getR(), ray1.getTheta());
        System.out.printf("        Conserved Energy: E=%.6e%n", ray1.getEnergy());
        System.out.printf("        Angular Momentum: L=%.6e%n", ray1.getAngularMomentum());
        
        // Integrate ray for several steps
        int steps = 1000;
        double stepSize = Constants.DEFAULT_STEP_SIZE;
        boolean continuing = true;
        
        for (int i = 0; i < steps && continuing; i++) {
            continuing = ray1.step(stepSize, rs);
            
            // Print progress every 200 steps
            if (i % 200 == 0) {
                System.out.printf("  Step %d: r=%.2e m, position=(%.2e, %.2e, %.2e)%n", 
                    i, ray1.getR(), ray1.getX(), ray1.getY(), ray1.getZ());
            }
        }
        
        if (ray1.getR() <= rs) {
            System.out.println("  Ray entered event horizon - absorbed by black hole!");
        } else if (ray1.getR() > Constants.ESCAPE_RADIUS) {
            System.out.println("  Ray escaped to infinity - gravitational lensing demonstrated!");
        }
        
        System.out.printf("Final trail length: %d points%n%n", ray1.getTrail().size());
    }
    
    private static void demonstratePhysicsEngine() {
        System.out.println("=== Physics Engine Demonstration ===");
        
        PhysicsEngine engine = new PhysicsEngine();
        
        // Add test particles
        float distance = Constants.DEFAULT_CAMERA_RADIUS * 0.5f;
        
        PhysicsEngine.SceneObject particle1 = new PhysicsEngine.SceneObject(
            new Vector3f(distance, 0, 0),
            new Vector3f(0, 0, 50000), // Orbital velocity
            new Vector3f(1.0f, 0.0f, 0.0f), // Red
            1000.0f,
            1e9f
        );
        
        PhysicsEngine.SceneObject particle2 = new PhysicsEngine.SceneObject(
            new Vector3f(-distance * 0.7f, 0, distance * 0.7f),
            new Vector3f(30000, 0, -30000),
            new Vector3f(0.0f, 0.0f, 1.0f), // Blue
            800.0f,
            8e8f
        );
        
        engine.addObject(particle1);
        engine.addObject(particle2);
        engine.setGravityEnabled(true);
        
        System.out.printf("Added %d test particles%n", engine.getObjects().size());
        System.out.printf("Central black hole mass: %.2e kg%n", engine.getCentralBlackHole().getMass());
        
        // Simulate for a few time steps
        double deltaTime = 1000.0; // 1000 seconds per step
        
        System.out.println("\\nGravitational simulation (first 5 steps):");
        for (int step = 0; step < 5; step++) {
            System.out.printf("Step %d:%n", step + 1);
            
            for (int i = 0; i < engine.getObjects().size(); i++) {
                PhysicsEngine.SceneObject obj = engine.getObjects().get(i);
                Vector3f pos = obj.getPosition();
                Vector3f vel = obj.getVelocity();
                
                System.out.printf("  Particle %d: pos=(%.2e, %.2e, %.2e) vel=(%.1f, %.1f, %.1f)%n",
                    i + 1, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
            }
            
            // Update physics
            engine.updateGravity(deltaTime);
        }
        
        System.out.println("\\nPhysics engine demonstration complete!");
    }
}