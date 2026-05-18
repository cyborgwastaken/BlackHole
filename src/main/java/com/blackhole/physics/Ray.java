package com.blackhole.physics;

import org.joml.Vector3d;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a light ray in curved spacetime around a black hole.
 * Implements geodesic integration using spherical coordinates and conserved quantities.
 * Maintains exact physics from the original C++ Ray struct.
 */
public class Ray {
    
    // Cartesian coordinates
    private double x, y, z;
    
    // Spherical coordinates  
    private double r, theta, phi;
    
    // Velocities in spherical coordinates
    private double dr, dtheta, dphi;
    
    // Conserved quantities
    private double energy;    // E - conserved energy
    private double angularMomentum; // L - conserved angular momentum
    
    // Integration trail for visualization
    private final List<Vector3d> trail;
    
    /**
     * Initialize ray from starting position and direction.
     * 
     * @param position Starting position in Cartesian coordinates
     * @param direction Initial direction vector (normalized)
     */
    public Ray(Vector3d position, Vector3d direction) {
        this.trail = new ArrayList<>();
        
        // Store cartesian coordinates
        this.x = position.x;
        this.y = position.y; 
        this.z = position.z;
        
        // Convert to spherical coordinates
        this.r = Math.sqrt(x*x + y*y + z*z);
        this.theta = Math.acos(z / r);
        this.phi = Math.atan2(y, x);
        
        // Convert direction to spherical basis velocities
        double dx = direction.x, dy = direction.y, dz = direction.z;
        this.dr = Math.sin(theta)*Math.cos(phi)*dx + Math.sin(theta)*Math.sin(phi)*dy + Math.cos(theta)*dz;
        this.dtheta = (Math.cos(theta)*Math.cos(phi)*dx + Math.cos(theta)*Math.sin(phi)*dy - Math.sin(theta)*dz) / r;
        this.dphi = (-Math.sin(phi)*dx + Math.cos(phi)*dy) / (r * Math.sin(theta));
        
        // Calculate conserved quantities for Schwarzschild metric
        this.angularMomentum = r * r * Math.sin(theta) * dphi;
        double f = 1.0 - Constants.SAG_A_SCHWARZSCHILD_RADIUS / r;
        double dtdL = Math.sqrt((dr*dr)/f + r*r*(dtheta*dtheta + Math.sin(theta)*Math.sin(theta)*dphi*dphi));
        this.energy = f * dtdL;
        
        // Start trail
        trail.add(new Vector3d(x, y, z));
    }
    
    /**
     * Take a single integration step using 4th-order Runge-Kutta method.
     * Integrates geodesic equations in Schwarzschild spacetime.
     * 
     * @param stepSize Integration step size (λ parameter)
     * @param schwarzschildRadius Event horizon radius
     * @return true if integration should continue, false if ray hits horizon
     */
    public boolean step(double stepSize, double schwarzschildRadius) {
        // Stop if inside event horizon
        if (r <= schwarzschildRadius) {
            return false;
        }
        
        // Perform RK4 step
        rungeKutta4Step(stepSize, schwarzschildRadius);
        
        // Convert back to Cartesian coordinates
        x = r * Math.sin(theta) * Math.cos(phi);
        y = r * Math.sin(theta) * Math.sin(phi);
        z = r * Math.cos(theta);
        
        // Record trail point
        trail.add(new Vector3d(x, y, z));
        
        return true;
    }
    
    /**
     * 4th-order Runge-Kutta integration step for geodesic equations.
     */
    private void rungeKutta4Step(double dL, double rs) {
        // State vector: [r, theta, phi, dr, dtheta, dphi]
        double[] state = {r, theta, phi, dr, dtheta, dphi};
        double[] k1 = new double[6], k2 = new double[6], k3 = new double[6], k4 = new double[6];
        double[] temp = new double[6];
        
        // k1 = f(y)
        geodesicRHS(state, k1, rs);
        
        // k2 = f(y + k1*h/2)
        for (int i = 0; i < 6; i++) temp[i] = state[i] + k1[i] * dL/2.0;
        geodesicRHS(temp, k2, rs);
        
        // k3 = f(y + k2*h/2)
        for (int i = 0; i < 6; i++) temp[i] = state[i] + k2[i] * dL/2.0;
        geodesicRHS(temp, k3, rs);
        
        // k4 = f(y + k3*h)
        for (int i = 0; i < 6; i++) temp[i] = state[i] + k3[i] * dL;
        geodesicRHS(temp, k4, rs);
        
        // Update state: y += (h/6)*(k1 + 2*k2 + 2*k3 + k4)
        r      += (dL/6.0) * (k1[0] + 2*k2[0] + 2*k3[0] + k4[0]);
        theta  += (dL/6.0) * (k1[1] + 2*k2[1] + 2*k3[1] + k4[1]);
        phi    += (dL/6.0) * (k1[2] + 2*k2[2] + 2*k3[2] + k4[2]);
        dr     += (dL/6.0) * (k1[3] + 2*k2[3] + 2*k3[3] + k4[3]);
        dtheta += (dL/6.0) * (k1[4] + 2*k2[4] + 2*k3[4] + k4[4]);
        dphi   += (dL/6.0) * (k1[5] + 2*k2[5] + 2*k3[5] + k4[5]);
    }
    
    /**
     * Compute right-hand side of geodesic equations in Schwarzschild spacetime.
     * Uses Christoffel symbols for curved spacetime.
     */
    private void geodesicRHS(double[] state, double[] rhs, double rs) {
        double r = state[0], theta = state[1];
        double dr = state[3], dtheta = state[4], dphi = state[5];
        
        double f = 1.0 - rs / r;
        double dtdL = energy / f;
        
        // First derivatives (velocities)
        rhs[0] = dr;
        rhs[1] = dtheta;  
        rhs[2] = dphi;
        
        // Second derivatives (accelerations from Christoffel symbols)
        rhs[3] = -(rs / (2.0 * r*r)) * f * dtdL * dtdL
               + (rs / (2.0 * r*r * f)) * dr * dr
               + r * (dtheta*dtheta + Math.sin(theta)*Math.sin(theta)*dphi*dphi);
               
        rhs[4] = -2.0*dr*dtheta/r + Math.sin(theta)*Math.cos(theta)*dphi*dphi;
        
        rhs[5] = -2.0*dr*dphi/r - 2.0*Math.cos(theta)/(Math.sin(theta)) * dtheta * dphi;
    }
    
    /**
     * Check if ray crosses the equatorial plane (for accretion disk detection).
     */
    public boolean crossesEquatorialPlane(Vector3d previousPosition, double diskInnerRadius, double diskOuterRadius) {
        boolean crossed = (previousPosition.y * y < 0.0); // Sign change in y
        if (!crossed) return false;
        
        double cylindricalRadius = Math.sqrt(x*x + z*z);
        return cylindricalRadius >= diskInnerRadius && cylindricalRadius <= diskOuterRadius;
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getR() { return r; }
    public double getTheta() { return theta; }
    public double getPhi() { return phi; }
    public double getEnergy() { return energy; }
    public double getAngularMomentum() { return angularMomentum; }
    public List<Vector3d> getTrail() { return new ArrayList<>(trail); }
    
    public Vector3d getPosition() {
        return new Vector3d(x, y, z);
    }
}