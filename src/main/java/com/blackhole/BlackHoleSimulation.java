package com.blackhole;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;
import org.joml.Vector3f;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import com.blackhole.camera.Camera;
import com.blackhole.physics.PhysicsEngine;
import com.blackhole.physics.Constants;
import com.blackhole.render.RenderEngine;

import java.nio.IntBuffer;

/**
 * Main application class for the Black Hole Simulation in Java.
 * Provides a comprehensive implementation of gravitational lensing and spacetime visualization.
 */
public class BlackHoleSimulation {
    
    // Window properties
    private long window;
    private int windowWidth = Constants.DEFAULT_WIDTH;
    private int windowHeight = Constants.DEFAULT_HEIGHT;
    
    // Core systems
    private Camera camera;
    private PhysicsEngine physicsEngine;
    private RenderEngine renderEngine;
    
    // Timing
    private double lastTime;
    private int frameCount = 0;
    private double lastFPSTime = 0.0;
    
    // Control state
    private boolean gravityEnabled = false;
    
    public static void main(String[] args) {
        System.out.println("Black Hole Simulation - Java Implementation");
        System.out.println("LWJGL Version: " + Version.getVersion());
        System.out.println("Based on Sagittarius A* black hole physics");
        System.out.println();
        System.out.println("Controls:");
        System.out.println("  Mouse: Orbit camera around black hole");
        System.out.println("  Scroll: Zoom in/out");
        System.out.println("  G: Toggle gravity simulation");
        System.out.println("  ESC: Exit");
        System.out.println();
        
        new BlackHoleSimulation().run();
    }
    
    public void run() {
        try {
            init();
            loop();
        } finally {
            cleanup();
        }
    }
    
    private void init() {
        // Setup error callback
        GLFWErrorCallback.createPrint(System.err).set();
        
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        
        // Create window
        window = glfwCreateWindow(windowWidth, windowHeight, "Black Hole Simulation - Java", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        
        // Setup key callback
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                handleKeyInput(key, scancode, action, mods);
            }
        });
        
        // Setup mouse button callback
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                camera.processMouseButton(button, action, mods);
            }
        });
        
        // Setup cursor position callback
        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                camera.processMouseMovement(xpos, ypos);
            }
        });
        
        // Setup scroll callback
        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                camera.processScroll(xoffset, yoffset);
            }
        });\n        \n        // Setup window resize callback\n        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {\n            windowWidth = width;\n            windowHeight = height;\n            if (renderEngine != null) {\n                renderEngine.resize(width, height);\n            }\n        });\n        \n        // Center window\n        try (MemoryStack stack = MemoryStack.stackPush()) {\n            IntBuffer pWidth = stack.mallocInt(1);\n            IntBuffer pHeight = stack.mallocInt(1);\n            \n            glfwGetWindowSize(window, pWidth, pHeight);\n            \n            var vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());\n            if (vidmode != null) {\n                glfwSetWindowPos(\n                    window,\n                    (vidmode.width() - pWidth.get(0)) / 2,\n                    (vidmode.height() - pHeight.get(0)) / 2\n                );\n            }\n        }\n        \n        // Make the OpenGL context current\n        glfwMakeContextCurrent(window);\n        \n        // Enable v-sync\n        glfwSwapInterval(1);\n        \n        // Make the window visible\n        glfwShowWindow(window);\n        \n        // Initialize OpenGL capabilities\n        GL.createCapabilities();\n        \n        // Print OpenGL information\n        System.out.println("OpenGL Version: " + GL33.glGetString(GL33.GL_VERSION));\n        System.out.println("GPU: " + GL33.glGetString(GL33.GL_RENDERER));\n        System.out.println();\n        \n        // Initialize systems\n        camera = new Camera();\n        physicsEngine = new PhysicsEngine();\n        renderEngine = new RenderEngine(windowWidth, windowHeight);\n        \n        // Initialize mouse position\n        try (MemoryStack stack = MemoryStack.stackPush()) {\n            var xPos = stack.mallocDouble(1);\n            var yPos = stack.mallocDouble(1);\n            glfwGetCursorPos(window, xPos, yPos);\n            camera.setMousePosition(xPos.get(0), yPos.get(0));\n        }\n        \n        // Add some test objects\n        addTestObjects();\n        \n        // Initialize timing\n        lastTime = glfwGetTime();\n        lastFPSTime = lastTime;\n        \n        System.out.println("Initialization complete. Starting simulation...");\n    }\n    \n    private void loop() {\n        while (!glfwWindowShouldClose(window)) {\n            double currentTime = glfwGetTime();\n            double deltaTime = currentTime - lastTime;\n            lastTime = currentTime;\n            \n            // Update camera matrices\n            camera.updateMatrices(windowWidth, windowHeight);\n            \n            // Update physics if gravity is enabled\n            if (gravityEnabled) {\n                physicsEngine.updateGravity(deltaTime);\n            }\n            \n            // Render scene\n            renderEngine.render(camera, physicsEngine.getObjects());\n            \n            // Swap buffers and poll events\n            glfwSwapBuffers(window);\n            glfwPollEvents();\n            \n            // Update FPS counter\n            updateFPS(currentTime);\n        }\n    }\n    \n    private void handleKeyInput(int key, int scancode, int action, int mods) {\n        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {\n            glfwSetWindowShouldClose(window, true);\n        }\n        \n        if (key == GLFW_KEY_G && action == GLFW_PRESS) {\n            gravityEnabled = !gravityEnabled;\n            physicsEngine.setGravityEnabled(gravityEnabled);\n            System.out.println("Gravity " + (gravityEnabled ? "enabled" : "disabled"));\n        }\n        \n        // Pass to camera\n        camera.processKey(key, scancode, action, mods);\n    }\n    \n    private void addTestObjects() {\n        // Add some test objects around the black hole\n        float blackHoleDistance = Constants.DEFAULT_CAMERA_RADIUS * 0.3f;\n        \n        // Test particle 1 - Red sphere\n        physicsEngine.addObject(new PhysicsEngine.SceneObject(\n            new Vector3f(blackHoleDistance, 0, 0),\n            new Vector3f(0, 0, 100000), // Some orbital velocity\n            new Vector3f(1.0f, 0.2f, 0.2f), // Red color\n            1000.0f,\n            1e9f\n        ));\n        \n        // Test particle 2 - Blue sphere\n        physicsEngine.addObject(new PhysicsEngine.SceneObject(\n            new Vector3f(-blackHoleDistance * 0.7f, 0, blackHoleDistance * 0.7f),\n            new Vector3f(50000, 0, -50000),\n            new Vector3f(0.2f, 0.2f, 1.0f), // Blue color\n            800.0f,\n            8e8f\n        ));\n        \n        // Test particle 3 - Green sphere\n        physicsEngine.addObject(new PhysicsEngine.SceneObject(\n            new Vector3f(0, blackHoleDistance * 0.5f, -blackHoleDistance * 0.5f),\n            new Vector3f(-75000, 0, 0),\n            new Vector3f(0.2f, 1.0f, 0.2f), // Green color\n            1200.0f,\n            1.2e9f\n        ));\n    }\n    \n    private void updateFPS(double currentTime) {\n        frameCount++;\n        \n        if (currentTime - lastFPSTime >= 1.0) {\n            double fps = frameCount / (currentTime - lastFPSTime);\n            String title = String.format("Black Hole Simulation - Java | FPS: %.1f | Objects: %d | Gravity: %s", \n                fps, physicsEngine.getObjects().size(), gravityEnabled ? "ON" : "OFF");\n            glfwSetWindowTitle(window, title);\n            \n            frameCount = 0;\n            lastFPSTime = currentTime;\n        }\n    }\n    \n    private void cleanup() {\n        // Cleanup render engine\n        if (renderEngine != null) {\n            renderEngine.cleanup();\n        }\n        \n        // Free window callbacks and destroy window\n        glfwFreeCallbacks(window);\n        glfwDestroyWindow(window);\n        \n        // Terminate GLFW and free error callback\n        glfwTerminate();\n        var errorCallback = glfwSetErrorCallback(null);\n        if (errorCallback != null) {\n            errorCallback.free();\n        }\n        \n        System.out.println("Cleanup complete. Thank you for using Black Hole Simulation!");\n    }\n}