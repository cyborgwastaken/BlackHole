package com.blackhole.render;

import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.blackhole.camera.Camera;
import com.blackhole.physics.PhysicsEngine;
import com.blackhole.physics.Constants;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * Main rendering engine for the black hole simulation.
 * Handles OpenGL rendering, compute shaders, and visual effects.
 */
public class RenderEngine {
    
    private final ShaderManager shaderManager;
    
    // OpenGL objects
    private int quadVAO;
    private int quadVBO;
    private int texture;
    private int gridVAO;
    private int gridVBO;
    
    // Uniform Buffer Objects
    private int cameraUBO;
    private int diskUBO;
    private int objectsUBO;
    
    // Window dimensions
    private int windowWidth;
    private int windowHeight;
    
    public RenderEngine(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        this.shaderManager = new ShaderManager();
        
        initializeOpenGL();
        createShaders();
        createGeometry();
        createUniformBuffers();
    }
    
    /**
     * Initialize OpenGL state.
     */
    private void initializeOpenGL() {
        GL33.glViewport(0, 0, windowWidth, windowHeight);
        GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // Enable blending for transparent effects
        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);
        
        // Enable depth testing
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glDepthFunc(GL33.GL_LESS);
    }
    
    /**
     * Create shader programs.
     */
    private void createShaders() {
        // Default quad shader for displaying compute results
        shaderManager.createDefaultQuadShader("quad");
        
        // Grid shader for spacetime visualization
        shaderManager.createShaderProgram("grid", 
            "src/main/resources/shaders/grid.vert",
            "src/main/resources/shaders/grid.frag");
        
        // Compute shader for ray tracing
        shaderManager.createComputeProgram("geodesic", 
            "src/main/resources/shaders/geodesic.comp");
    }
    
    /**
     * Create geometry for rendering.
     */
    private void createGeometry() {
        createQuad();
        createGrid();
    }\n    \n    /**\n     * Create fullscreen quad for displaying compute shader results.\n     */\n    private void createQuad() {\n        float[] quadVertices = {\n            // positions   // texCoords\n            -1.0f,  1.0f,  0.0f, 1.0f,  // top left\n            -1.0f, -1.0f,  0.0f, 0.0f,  // bottom left\n             1.0f, -1.0f,  1.0f, 0.0f,  // bottom right\n            \n            -1.0f,  1.0f,  0.0f, 1.0f,  // top left\n             1.0f, -1.0f,  1.0f, 0.0f,  // bottom right\n             1.0f,  1.0f,  1.0f, 1.0f   // top right\n        };\n        \n        quadVAO = GL33.glGenVertexArrays();\n        quadVBO = GL33.glGenBuffers();\n        \n        GL33.glBindVertexArray(quadVAO);\n        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, quadVBO);\n        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, quadVertices, GL33.GL_STATIC_DRAW);\n        \n        // Position attribute\n        GL33.glVertexAttribPointer(0, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 0);\n        GL33.glEnableVertexAttribArray(0);\n        \n        // Texture coordinate attribute\n        GL33.glVertexAttribPointer(1, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);\n        GL33.glEnableVertexAttribArray(1);\n        \n        // Create texture for compute shader output\n        texture = GL33.glGenTextures();\n        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);\n        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);\n        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);\n        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);\n        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);\n    }\n    \n    /**\n     * Create spacetime grid for visualization.\n     */\n    private void createGrid() {\n        // Create a simple grid in the XZ plane around the black hole\n        int gridSize = 20;\n        float gridSpacing = Constants.SAG_A_SCHWARZSCHILD_RADIUS / 1e9f; // Scale for visibility\n        \n        // Calculate number of vertices (lines)\n        int vertexCount = (gridSize + 1) * 4; // Horizontal and vertical lines\n        float[] vertices = new float[vertexCount * 3];\n        \n        int index = 0;\n        float halfSize = gridSize * gridSpacing * 0.5f;\n        \n        // Horizontal lines\n        for (int i = 0; i <= gridSize; i++) {\n            float z = -halfSize + i * gridSpacing;\n            vertices[index++] = -halfSize; vertices[index++] = 0.0f; vertices[index++] = z;\n            vertices[index++] =  halfSize; vertices[index++] = 0.0f; vertices[index++] = z;\n        }\n        \n        // Vertical lines\n        for (int i = 0; i <= gridSize; i++) {\n            float x = -halfSize + i * gridSpacing;\n            vertices[index++] = x; vertices[index++] = 0.0f; vertices[index++] = -halfSize;\n            vertices[index++] = x; vertices[index++] = 0.0f; vertices[index++] =  halfSize;\n        }\n        \n        gridVAO = GL33.glGenVertexArrays();\n        gridVBO = GL33.glGenBuffers();\n        \n        GL33.glBindVertexArray(gridVAO);\n        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, gridVBO);\n        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);\n        \n        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);\n        GL33.glEnableVertexAttribArray(0);\n    }\n    \n    /**\n     * Create uniform buffer objects for compute shader.\n     */\n    private void createUniformBuffers() {\n        // Camera UBO\n        cameraUBO = GL33.glGenBuffers();\n        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, cameraUBO);\n        GL33.glBufferData(GL33.GL_UNIFORM_BUFFER, 128, GL33.GL_DYNAMIC_DRAW); // 128 bytes for camera data\n        GL33.glBindBufferBase(GL33.GL_UNIFORM_BUFFER, 1, cameraUBO);\n        \n        // Disk UBO\n        diskUBO = GL33.glGenBuffers();\n        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, diskUBO);\n        GL33.glBufferData(GL33.GL_UNIFORM_BUFFER, 4 * Float.BYTES, GL33.GL_DYNAMIC_DRAW);\n        GL33.glBindBufferBase(GL33.GL_UNIFORM_BUFFER, 2, diskUBO);\n        \n        // Objects UBO\n        objectsUBO = GL33.glGenBuffers();\n        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, objectsUBO);\n        int objUBOSize = 4 + 3 * 4 + Constants.MAX_OBJECTS * (4 * 4 + 4 * 4 + 4); // int + padding + objects data\n        GL33.glBufferData(GL33.GL_UNIFORM_BUFFER, objUBOSize, GL33.GL_DYNAMIC_DRAW);\n        GL33.glBindBufferBase(GL33.GL_UNIFORM_BUFFER, 3, objectsUBO);\n    }\n    \n    /**\n     * Update camera uniform buffer.\n     */\n    private void updateCameraUBO(Camera camera) {\n        try (MemoryStack stack = MemoryStack.stackPush()) {\n            Vector3f position = camera.getPosition();\n            Vector3f forward = camera.getForward();\n            Vector3f right = camera.getRight();\n            Vector3f up = camera.getUp();\n            \n            FloatBuffer buffer = stack.mallocFloat(32); // 128 bytes / 4 bytes per float\n            \n            // Camera position\n            buffer.put(position.x).put(position.y).put(position.z).put(0.0f);\n            \n            // Camera right vector\n            buffer.put(right.x).put(right.y).put(right.z).put(0.0f);\n            \n            // Camera up vector\n            buffer.put(up.x).put(up.y).put(up.z).put(0.0f);\n            \n            // Camera forward vector\n            buffer.put(forward.x).put(forward.y).put(forward.z).put(0.0f);\n            \n            // Additional camera parameters\n            buffer.put(camera.getTanHalfFov());\n            buffer.put(camera.getAspect(windowWidth, windowHeight));\n            buffer.put(camera.isMoving() ? 1.0f : 0.0f);\n            buffer.put(0.0f); // padding\n            \n            buffer.flip();\n            \n            GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, cameraUBO);\n            GL33.glBufferSubData(GL33.GL_UNIFORM_BUFFER, 0, buffer);\n        }\n    }\n    \n    /**\n     * Update disk uniform buffer.\n     */\n    private void updateDiskUBO() {\n        try (MemoryStack stack = MemoryStack.stackPush()) {\n            FloatBuffer buffer = stack.mallocFloat(4);\n            \n            buffer.put((float)Constants.DISK_INNER_RADIUS);\n            buffer.put((float)Constants.DISK_OUTER_RADIUS);\n            buffer.put(1.0f); // disk_num (unused)\n            buffer.put((float)Constants.DISK_THICKNESS);\n            \n            buffer.flip();\n            \n            GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, diskUBO);\n            GL33.glBufferSubData(GL33.GL_UNIFORM_BUFFER, 0, buffer);\n        }\n    }\n    \n    /**\n     * Update objects uniform buffer.\n     */\n    private void updateObjectsUBO(List<PhysicsEngine.SceneObject> objects) {\n        try (MemoryStack stack = MemoryStack.stackPush()) {\n            // Calculate buffer size\n            int bufferSize = 4 + 12 + Constants.MAX_OBJECTS * 36; // int + padding + objects\n            FloatBuffer buffer = stack.mallocFloat(bufferSize / 4);\n            \n            // Number of objects\n            buffer.put(Float.intBitsToFloat(objects.size()));\n            buffer.put(0.0f).put(0.0f).put(0.0f); // padding\n            \n            // Object data\n            for (int i = 0; i < Constants.MAX_OBJECTS; i++) {\n                if (i < objects.size()) {\n                    PhysicsEngine.SceneObject obj = objects.get(i);\n                    Vector3f pos = obj.getPosition();\n                    Vector3f color = obj.getColor();\n                    \n                    // Position and radius\n                    buffer.put(pos.x).put(pos.y).put(pos.z).put(obj.getRadius());\n                    \n                    // Color\n                    buffer.put(color.x).put(color.y).put(color.z).put(1.0f);\n                    \n                    // Mass\n                    buffer.put(obj.getMass());\n                } else {\n                    // Fill with zeros\n                    for (int j = 0; j < 9; j++) {\n                        buffer.put(0.0f);\n                    }\n                }\n            }\n            \n            buffer.flip();\n            \n            GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, objectsUBO);\n            GL33.glBufferSubData(GL33.GL_UNIFORM_BUFFER, 0, buffer);\n        }\n    }\n    \n    /**\n     * Dispatch compute shader for ray tracing.\n     */\n    public void dispatchCompute(Camera camera, List<PhysicsEngine.SceneObject> objects) {\n        // Determine compute resolution based on camera movement\n        int computeWidth = camera.isMoving() ? Constants.COMPUTE_WIDTH_HIGH : Constants.COMPUTE_WIDTH_HIGH;\n        int computeHeight = camera.isMoving() ? Constants.COMPUTE_HEIGHT_HIGH : Constants.COMPUTE_HEIGHT_HIGH;\n        \n        // Update texture size\n        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);\n        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA8, computeWidth, computeHeight, \n                         0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, 0);\n        \n        // Bind compute program\n        GL33.glUseProgram(shaderManager.getProgram("geodesic"));\n        \n        // Update uniform buffers\n        updateCameraUBO(camera);\n        updateDiskUBO();\n        updateObjectsUBO(objects);\n        \n        // Bind texture as image\n        GL33.glBindImageTexture(0, texture, 0, false, 0, GL33.GL_WRITE_ONLY, GL33.GL_RGBA8);\n        \n        // Dispatch compute shader\n        int workGroupsX = (computeWidth + 15) / 16;\n        int workGroupsY = (computeHeight + 15) / 16;\n        GL43.glDispatchCompute(workGroupsX, workGroupsY, 1);\n        \n        // Wait for completion\n        GL33.glMemoryBarrier(GL33.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);\n    }\n    \n    /**\n     * Render the scene.\n     */\n    public void render(Camera camera, List<PhysicsEngine.SceneObject> objects) {\n        // Clear the framebuffer\n        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);\n        \n        // Dispatch compute shader for ray tracing\n        dispatchCompute(camera, objects);\n        \n        // Render fullscreen quad with ray tracing result\n        renderQuad();\n        \n        // Render spacetime grid (optional)\n        renderGrid(camera);\n    }\n    \n    /**\n     * Render fullscreen quad with texture.\n     */\n    private void renderQuad() {\n        GL33.glUseProgram(shaderManager.getProgram("quad"));\n        \n        // Bind texture\n        GL33.glActiveTexture(GL33.GL_TEXTURE0);\n        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);\n        shaderManager.setUniformInt(shaderManager.getProgram("quad"), "screenTexture", 0);\n        \n        // Draw quad\n        GL33.glBindVertexArray(quadVAO);\n        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 6);\n    }\n    \n    /**\n     * Render spacetime grid.\n     */\n    private void renderGrid(Camera camera) {\n        GL33.glUseProgram(shaderManager.getProgram("grid"));\n        \n        // Update view-projection matrix\n        try (MemoryStack stack = MemoryStack.stackPush()) {\n            FloatBuffer matrixBuffer = stack.mallocFloat(16);\n            camera.getViewProjectionMatrix().get(matrixBuffer);\n            shaderManager.setUniformMatrix4f(shaderManager.getProgram("grid"), "viewProj", matrixBuffer);\n        }\n        \n        // Enable line width for better visibility\n        GL33.glLineWidth(1.0f);\n        \n        // Draw grid\n        GL33.glBindVertexArray(gridVAO);\n        GL33.glDrawArrays(GL33.GL_LINES, 0, 84); // 21*4 vertices for grid\n    }\n    \n    /**\n     * Handle window resize.\n     */\n    public void resize(int width, int height) {\n        this.windowWidth = width;\n        this.windowHeight = height;\n        GL33.glViewport(0, 0, width, height);\n    }\n    \n    /**\n     * Cleanup resources.\n     */\n    public void cleanup() {\n        // Delete OpenGL objects\n        GL33.glDeleteVertexArrays(quadVAO);\n        GL33.glDeleteVertexArrays(gridVAO);\n        GL33.glDeleteBuffers(quadVBO);\n        GL33.glDeleteBuffers(gridVBO);\n        GL33.glDeleteBuffers(cameraUBO);\n        GL33.glDeleteBuffers(diskUBO);\n        GL33.glDeleteBuffers(objectsUBO);\n        GL33.glDeleteTextures(texture);\n        \n        // Cleanup shaders\n        shaderManager.cleanup();\n    }\n}