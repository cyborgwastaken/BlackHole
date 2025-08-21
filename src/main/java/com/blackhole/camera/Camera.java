package com.blackhole.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import com.blackhole.physics.Constants;

/**
 * Orbital camera system for black hole simulation.
 * Maintains exact behavior from the C++ Camera struct with orbital controls.
 */
public class Camera {
    
    // Camera target (always centered on black hole)
    private final Vector3f target = new Vector3f(0.0f, 0.0f, 0.0f);
    
    // Orbital parameters
    private float radius = Constants.DEFAULT_CAMERA_RADIUS;
    private float azimuth = 0.0f;
    private float elevation = (float)(Math.PI / 2.0);
    
    // Control parameters
    private final float orbitSpeed = 0.01f;
    private final float zoomSpeed = 25e9f;
    private final float minRadius = Constants.MIN_CAMERA_RADIUS;
    private final float maxRadius = Constants.MAX_CAMERA_RADIUS;
    
    // Input state
    private boolean dragging = false;
    private boolean panning = false; // Disabled to keep camera centered
    private boolean moving = false; // For compute shader optimization
    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    
    // Camera matrices
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewProjectionMatrix = new Matrix4f();
    
    /**
     * Calculate camera position in world space based on orbital parameters.
     */
    public Vector3f getPosition() {
        float clampedElevation = Math.max(0.01f, Math.min(elevation, (float)Math.PI - 0.01f));
        return new Vector3f(
            radius * (float)(Math.sin(clampedElevation) * Math.cos(azimuth)),
            radius * (float)Math.cos(clampedElevation),
            radius * (float)(Math.sin(clampedElevation) * Math.sin(azimuth))
        );
    }
    
    /**
     * Get camera forward vector.
     */
    public Vector3f getForward() {
        Vector3f position = getPosition();
        return new Vector3f(target).sub(position).normalize();
    }
    
    /**
     * Get camera right vector.
     */
    public Vector3f getRight() {
        Vector3f forward = getForward();
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        return new Vector3f(forward).cross(up).normalize();
    }
    
    /**
     * Get camera up vector.
     */
    public Vector3f getUp() {
        Vector3f forward = getForward();
        Vector3f right = getRight();
        return new Vector3f(right).cross(forward).normalize();
    }
    
    /**
     * Update camera matrices.
     */
    public void updateMatrices(int windowWidth, int windowHeight) {
        Vector3f position = getPosition();
        
        // Update view matrix - always look at target (black hole center)
        viewMatrix.setLookAt(position, target, new Vector3f(0.0f, 1.0f, 0.0f));
        
        // Update projection matrix
        float aspect = (float) windowWidth / windowHeight;
        projectionMatrix.setPerspective(
            (float)Math.toRadians(Constants.DEFAULT_FOV), 
            aspect, 
            1.0f, 
            Constants.MAX_CAMERA_RADIUS * 2.0f
        );
        
        // Combine matrices
        viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
    }
    
    /**
     * Process mouse movement for camera controls.
     */
    public void processMouseMovement(double xpos, double ypos) {
        double deltaX = xpos - lastMouseX;
        double deltaY = ypos - lastMouseY;
        
        if (dragging && !panning) {
            // Orbital movement - rotate around black hole
            azimuth += (float)(deltaX * orbitSpeed);
            elevation -= (float)(deltaY * orbitSpeed);
            
            // Clamp elevation to avoid gimbal lock
            elevation = Math.max(0.01f, Math.min(elevation, (float)Math.PI - 0.01f));
        }
        
        lastMouseX = xpos;
        lastMouseY = ypos;
        updateMovingState();
    }
    
    /**
     * Process mouse button events.
     */
    public void processMouseButton(int button, int action, int mods) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            if (action == GLFW.GLFW_PRESS) {
                dragging = true;
                // Always disable panning to keep camera centered on black hole
                panning = false;
            } else if (action == GLFW.GLFW_RELEASE) {
                dragging = false;
                panning = false;
            }
        }
    }
    
    /**
     * Process scroll for zoom control.
     */
    public void processScroll(double xoffset, double yoffset) {
        radius -= (float)(yoffset * zoomSpeed);
        radius = Math.max(minRadius, Math.min(radius, maxRadius));
        updateMovingState();
    }
    
    /**
     * Process keyboard input.
     */
    public void processKey(int key, int scancode, int action, int mods) {
        // Additional key controls can be added here if needed
    }
    
    /**
     * Update moving state for compute shader optimization.
     */
    private void updateMovingState() {
        moving = dragging || panning;
    }
    
    /**
     * Set mouse position (used for initialization).
     */
    public void setMousePosition(double x, double y) {
        this.lastMouseX = x;
        this.lastMouseY = y;
    }
    
    // Getters
    public Vector3f getTarget() {
        return new Vector3f(target);
    }
    
    public float getRadius() {
        return radius;
    }
    
    public float getAzimuth() {
        return azimuth;
    }
    
    public float getElevation() {
        return elevation;
    }
    
    public boolean isMoving() {
        return moving;
    }
    
    public Matrix4f getViewMatrix() {
        return new Matrix4f(viewMatrix);
    }
    
    public Matrix4f getProjectionMatrix() {
        return new Matrix4f(projectionMatrix);
    }
    
    public Matrix4f getViewProjectionMatrix() {
        return new Matrix4f(viewProjectionMatrix);
    }
    
    /**
     * Get tangent of half field of view for compute shaders.
     */
    public float getTanHalfFov() {
        return (float)Math.tan(Math.toRadians(Constants.DEFAULT_FOV) / 2.0);
    }
    
    /**
     * Get aspect ratio for compute shaders.
     */
    public float getAspect(int windowWidth, int windowHeight) {
        return (float) windowWidth / windowHeight;
    }
}