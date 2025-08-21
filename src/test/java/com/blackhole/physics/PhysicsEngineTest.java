package com.blackhole.physics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.joml.Vector3d;
import org.joml.Vector3f;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for black hole physics engine.
 * Validates scientific accuracy and maintains exact physics from C++ implementation.
 */
public class PhysicsEngineTest {
    
    private BlackHole sagittariusA;
    private PhysicsEngine engine;
    
    @BeforeEach
    void setUp() {
        sagittariusA = BlackHole.createSagittariusA();
        engine = new PhysicsEngine();
    }
    
    @Test
    void testSagittariusACreation() {
        assertEquals(8.54e36, sagittariusA.getMass(), 1e34, "Sagittarius A* mass");
        
        // Schwarzschild radius should be rs = 2GM/c²
        double expectedRs = 2.0 * Constants.GRAVITATIONAL_CONSTANT * Constants.SAG_A_MASS 
                          / (Constants.SPEED_OF_LIGHT * Constants.SPEED_OF_LIGHT);
        assertEquals(expectedRs, sagittariusA.getSchwarzchildRadius(), 1e8, "Schwarzschild radius");
        
        Vector3f position = sagittariusA.getPosition();
        assertEquals(0.0f, position.x, 1e-6f, "Black hole at origin X");
        assertEquals(0.0f, position.y, 1e-6f, "Black hole at origin Y");  
        assertEquals(0.0f, position.z, 1e-6f, "Black hole at origin Z");
    }
    
    @Test
    void testEventHorizonDetection() {
        // Point well outside event horizon should not be intercepted
        assertFalse(sagittariusA.intercepts(1e12, 0, 0), "Point outside horizon");
        
        // Point exactly on event horizon should be intercepted
        double rs = sagittariusA.getSchwarzchildRadius();
        assertTrue(sagittariusA.intercepts(rs, 0, 0), "Point on horizon");
        
        // Point inside event horizon should be intercepted
        assertTrue(sagittariusA.intercepts(rs * 0.5, 0, 0), "Point inside horizon");
    }
    
    @Test
    void testRayConservedQuantities() {
        // Create a ray starting well outside event horizon
        double rs = sagittariusA.getSchwarzchildRadius();
        Vector3d startPos = new Vector3d(rs * 10, 0, 0);
        Vector3d direction = new Vector3d(-1, 0.1, 0).normalize();
        
        Ray ray = new Ray(startPos, direction);
        
        // Verify initial conditions
        assertEquals(rs * 10, ray.getR(), 1e6, "Initial radius");
        
        // Store initial conserved quantities
        double initialEnergy = ray.getEnergy();
        double initialAngularMomentum = ray.getAngularMomentum();
        
        // Integrate for several steps and verify conservation
        for (int i = 0; i < 100; i++) {
            if (!ray.step(Constants.DEFAULT_STEP_SIZE, rs)) {
                break; // Ray hit event horizon
            }
            
            // Energy and angular momentum should be conserved (within numerical precision)
            assertEquals(initialEnergy, ray.getEnergy(), 1e-10, 
                "Energy conservation at step " + i);
            assertEquals(initialAngularMomentum, ray.getAngularMomentum(), 1e3, 
                "Angular momentum conservation at step " + i);
        }
        
        assertTrue(ray.getTrail().size() > 1, "Ray should have trail points");
    }
    
    @Test
    void testRayTracing() {
        PhysicsEngine.RayTraceResult result = engine.traceRay(
            new Vector3d(1e12, 0, 0),
            new Vector3d(-1, 0, 0),
            1000
        );
        
        assertNotNull(result, "Ray trace result should not be null");
        assertNotNull(result.ray, "Ray should not be null");
        assertNotNull(result.finalPosition, "Final position should not be null");
    }
    
    @Test
    void testGravitationalSimulation() {
        // Add test particle
        PhysicsEngine.SceneObject particle = new PhysicsEngine.SceneObject(
            new Vector3f(1e11f, 0, 0),
            new Vector3f(0, 0, 1000),
            new Vector3f(1.0f, 0, 0),
            1000.0f,
            1e9f
        );
        
        engine.addObject(particle);
        engine.setGravityEnabled(true);
        
        Vector3f initialPos = particle.getPosition();
        Vector3f initialVel = particle.getVelocity();
        
        // Run gravity simulation
        engine.updateGravity(1000.0);
        
        // Particle should have moved due to gravitational force
        Vector3f finalPos = engine.getObjects().get(0).getPosition();
        Vector3f finalVel = engine.getObjects().get(0).getVelocity();
        
        // Position should change
        assertNotEquals(initialPos.x, finalPos.x, 1e3, "Position should change due to gravity");
        
        // Velocity should change (acceleration toward black hole)
        assertNotEquals(initialVel.x, finalVel.x, 10.0, "Velocity should change due to gravity");
    }
    
    @Test
    void testPhysicsConstants() {
        assertEquals(299792458.0, Constants.SPEED_OF_LIGHT, 1.0, "Speed of light");
        assertEquals(6.67430e-11, Constants.GRAVITATIONAL_CONSTANT, 1e-16, "Gravitational constant");
        assertEquals(8.54e36, Constants.SAG_A_MASS, 1e34, "Sagittarius A* mass");
        
        // Verify Schwarzschild radius calculation
        double expectedRs = 2.0 * Constants.GRAVITATIONAL_CONSTANT * Constants.SAG_A_MASS 
                          / (Constants.SPEED_OF_LIGHT * Constants.SPEED_OF_LIGHT);
        assertEquals(expectedRs, Constants.SAG_A_SCHWARZSCHILD_RADIUS, 1e8, 
            "Pre-calculated Schwarzschild radius");
    }
}