package com.blackhole.physics;

import org.joml.Vector3d;
import org.joml.Vector3f;
import java.util.List;
import java.util.ArrayList;

/**
 * Physics engine for black hole simulation.
 * Handles ray tracing, gravitational interactions, and geodesic calculations.
 */
public class PhysicsEngine {
    
    private final BlackHole centralBlackHole;
    private final List<SceneObject> objects;
    private boolean gravityEnabled = false;
    
    public PhysicsEngine() {
        this.centralBlackHole = BlackHole.createSagittariusA();
        this.objects = new ArrayList<>();
    }
    
    /**
     * Represents a gravitational object in the scene.
     */
    public static class SceneObject {
        private Vector3f position;
        private Vector3f velocity;
        private Vector3f color;
        private float mass;
        private float radius;
        
        public SceneObject(Vector3f position, Vector3f velocity, Vector3f color, float mass, float radius) {
            this.position = new Vector3f(position);
            this.velocity = new Vector3f(velocity);
            this.color = new Vector3f(color);
            this.mass = mass;
            this.radius = radius;
        }
        
        // Getters and setters
        public Vector3f getPosition() { return new Vector3f(position); }
        public Vector3f getVelocity() { return new Vector3f(velocity); }
        public Vector3f getColor() { return new Vector3f(color); }
        public float getMass() { return mass; }
        public float getRadius() { return radius; }
        
        public void setPosition(Vector3f position) { this.position.set(position); }
        public void setVelocity(Vector3f velocity) { this.velocity.set(velocity); }
        
        /**
         * Check if a point is inside this object.
         */
        public boolean contains(Vector3f point) {
            return position.distance(point) <= radius;
        }
    }
    
    /**
     * Trace a ray through curved spacetime.
     * 
     * @param startPosition Ray starting position
     * @param direction Ray direction (normalized)
     * @param maxSteps Maximum integration steps
     * @return Ray tracing result
     */
    public RayTraceResult traceRay(Vector3d startPosition, Vector3d direction, int maxSteps) {
        Ray ray = new Ray(startPosition, direction);
        Vector3d previousPosition = new Vector3d(startPosition);
        
        RayTraceResult result = new RayTraceResult();
        result.ray = ray;
        
        for (int step = 0; step < maxSteps; step++) {
            // Check for black hole intersection
            if (centralBlackHole.intercepts(ray.getX(), ray.getY(), ray.getZ())) {
                result.hitBlackHole = true;
                result.finalPosition = ray.getPosition();
                break;
            }
            
            // Check for accretion disk intersection
            if (ray.crossesEquatorialPlane(previousPosition, 
                                         Constants.DISK_INNER_RADIUS, 
                                         Constants.DISK_OUTER_RADIUS)) {
                result.hitAccretionDisk = true;
                result.finalPosition = ray.getPosition();
                
                // Calculate disk color based on radius
                double diskRadius = Math.sqrt(ray.getX()*ray.getX() + ray.getZ()*ray.getZ());
                double normalizedRadius = diskRadius / Constants.DISK_OUTER_RADIUS;
                result.diskColor = calculateDiskColor(normalizedRadius);
                break;
            }
            
            // Check for object intersections
            Vector3f rayPos = new Vector3f((float)ray.getX(), (float)ray.getY(), (float)ray.getZ());
            for (SceneObject obj : objects) {
                if (obj.contains(rayPos)) {
                    result.hitObject = true;
                    result.hitObjectColor = obj.getColor();
                    result.hitObjectCenter = obj.getPosition();
                    result.hitObjectRadius = obj.getRadius();
                    result.finalPosition = ray.getPosition();
                    break;
                }
            }
            
            if (result.hitObject) break;
            
            // Take integration step
            previousPosition.set(ray.getPosition());
            if (!ray.step(Constants.DEFAULT_STEP_SIZE, centralBlackHole.getSchwarzchildRadius())) {
                result.hitBlackHole = true;
                result.finalPosition = ray.getPosition();
                break;
            }
            
            // Check if ray escaped
            if (ray.getR() > Constants.ESCAPE_RADIUS) {
                result.escaped = true;
                result.finalPosition = ray.getPosition();
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Calculate accretion disk color based on radius.
     */
    private Vector3f calculateDiskColor(double normalizedRadius) {
        // Orange/red gradient from inner to outer disk
        float red = 1.0f;
        float green = (float) normalizedRadius;
        float blue = 0.2f;
        return new Vector3f(red, green, blue);
    }
    
    /**
     * Update gravitational physics for objects.
     * 
     * @param deltaTime Time step in seconds
     */
    public void updateGravity(double deltaTime) {
        if (!gravityEnabled) return;
        
        // Update object positions and velocities due to gravity
        for (int i = 0; i < objects.size(); i++) {
            SceneObject obj1 = objects.get(i);
            Vector3f acceleration = new Vector3f();
            
            // Gravitational force from black hole
            Vector3f toBlackHole = new Vector3f(centralBlackHole.getPosition()).sub(obj1.getPosition());
            float distanceToBlackHole = toBlackHole.length();
            if (distanceToBlackHole > 0) {
                toBlackHole.normalize();
                float force = (float)(Constants.GRAVITATIONAL_CONSTANT * centralBlackHole.getMass() * obj1.getMass()) 
                            / (distanceToBlackHole * distanceToBlackHole);
                acceleration.add(toBlackHole.mul(force / obj1.getMass()));
            }
            
            // Gravitational forces from other objects
            for (int j = 0; j < objects.size(); j++) {
                if (i == j) continue;
                
                SceneObject obj2 = objects.get(j);
                Vector3f toOther = new Vector3f(obj2.getPosition()).sub(obj1.getPosition());
                float distance = toOther.length();
                if (distance > 0) {
                    toOther.normalize();
                    float force = (float)(Constants.GRAVITATIONAL_CONSTANT * obj1.getMass() * obj2.getMass()) 
                                / (distance * distance);
                    acceleration.add(toOther.mul(force / obj1.getMass()));
                }
            }
            
            // Update velocity and position
            obj1.getVelocity().add(acceleration.mul((float)deltaTime));
            obj1.getPosition().add(new Vector3f(obj1.getVelocity()).mul((float)deltaTime));
        }
    }
    
    /**
     * Add a gravitational object to the scene.
     */
    public void addObject(SceneObject object) {
        if (objects.size() < Constants.MAX_OBJECTS) {
            objects.add(object);
        }
    }
    
    /**
     * Remove all objects from the scene.
     */
    public void clearObjects() {
        objects.clear();
    }
    
    // Getters and setters
    public BlackHole getCentralBlackHole() {
        return centralBlackHole;
    }
    
    public List<SceneObject> getObjects() {
        return new ArrayList<>(objects);
    }
    
    public boolean isGravityEnabled() {
        return gravityEnabled;
    }
    
    public void setGravityEnabled(boolean enabled) {
        this.gravityEnabled = enabled;
    }
    
    /**
     * Result of ray tracing operation.
     */
    public static class RayTraceResult {
        public Ray ray;
        public boolean hitBlackHole = false;
        public boolean hitAccretionDisk = false;
        public boolean hitObject = false;
        public boolean escaped = false;
        
        public Vector3d finalPosition;
        public Vector3f diskColor;
        public Vector3f hitObjectColor;
        public Vector3f hitObjectCenter;
        public float hitObjectRadius;
    }
}