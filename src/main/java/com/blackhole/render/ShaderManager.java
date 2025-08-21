package com.blackhole.render;

import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manages OpenGL shaders for the black hole simulation.
 * Handles vertex, fragment, and compute shaders with uniform management.
 */
public class ShaderManager {
    
    private final Map<String, Integer> programs = new HashMap<>();
    private final Map<String, Integer> uniforms = new HashMap<>();
    
    /**
     * Load and compile a shader program from vertex and fragment shader files.
     * 
     * @param name Program name for later reference
     * @param vertexPath Path to vertex shader file
     * @param fragmentPath Path to fragment shader file
     * @return OpenGL program ID
     */
    public int createShaderProgram(String name, String vertexPath, String fragmentPath) {
        String vertexSource = loadShaderSource(vertexPath);
        String fragmentSource = loadShaderSource(fragmentPath);
        
        int vertexShader = compileShader(GL33.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GL33.GL_FRAGMENT_SHADER, fragmentSource);
        
        int program = GL33.glCreateProgram();
        GL33.glAttachShader(program, vertexShader);
        GL33.glAttachShader(program, fragmentShader);
        GL33.glLinkProgram(program);
        
        // Check linking
        if (GL33.glGetProgrami(program, GL33.GL_LINK_STATUS) == GL33.GL_FALSE) {
            String log = GL33.glGetProgramInfoLog(program);
            throw new RuntimeException("Shader program linking failed: " + log);
        }
        
        GL33.glDeleteShader(vertexShader);
        GL33.glDeleteShader(fragmentShader);
        
        programs.put(name, program);
        return program;
    }
    
    /**
     * Create a compute shader program.
     * 
     * @param name Program name for later reference
     * @param computePath Path to compute shader file
     * @return OpenGL program ID
     */
    public int createComputeProgram(String name, String computePath) {
        String computeSource = loadShaderSource(computePath);
        
        int computeShader = compileShader(GL33.GL_COMPUTE_SHADER, computeSource);
        
        int program = GL33.glCreateProgram();
        GL33.glAttachShader(program, computeShader);
        GL33.glLinkProgram(program);
        
        // Check linking
        if (GL33.glGetProgrami(program, GL33.GL_LINK_STATUS) == GL33.GL_FALSE) {
            String log = GL33.glGetProgramInfoLog(program);
            throw new RuntimeException("Compute program linking failed: " + log);
        }
        
        GL33.glDeleteShader(computeShader);
        
        programs.put(name, program);
        return program;
    }
    
    /**
     * Create default fullscreen quad shader.
     */
    public int createDefaultQuadShader(String name) {
        String vertexSource = 
            "#version 330 core\n" +
            "layout (location = 0) in vec2 aPos;\n" +
            "layout (location = 1) in vec2 aTexCoord;\n" +
            "out vec2 TexCoord;\n" +
            "void main() {\n" +
            "    gl_Position = vec4(aPos, 0.0, 1.0);\n" +
            "    TexCoord = aTexCoord;\n" +
            "}";
        
        String fragmentSource = 
            "#version 330 core\n" +
            "in vec2 TexCoord;\n" +
            "out vec4 FragColor;\n" +
            "uniform sampler2D screenTexture;\n" +
            "void main() {\n" +
            "    FragColor = texture(screenTexture, TexCoord);\n" +
            "}";
        
        int vertexShader = compileShader(GL33.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compileShader(GL33.GL_FRAGMENT_SHADER, fragmentSource);\n        \n        int program = GL33.glCreateProgram();\n        GL33.glAttachShader(program, vertexShader);\n        GL33.glAttachShader(program, fragmentShader);\n        GL33.glLinkProgram(program);\n        \n        if (GL33.glGetProgrami(program, GL33.GL_LINK_STATUS) == GL33.GL_FALSE) {\n            String log = GL33.glGetProgramInfoLog(program);\n            throw new RuntimeException(\"Default shader linking failed: \" + log);\n        }\n        \n        GL33.glDeleteShader(vertexShader);\n        GL33.glDeleteShader(fragmentShader);\n        \n        programs.put(name, program);\n        return program;\n    }\n    \n    /**\n     * Load shader source code from file.\n     */\n    private String loadShaderSource(String path) {\n        try {\n            return Files.readString(Paths.get(path));\n        } catch (IOException e) {\n            // Try loading from resources\n            try {\n                var resource = getClass().getClassLoader().getResourceAsStream(path);\n                if (resource != null) {\n                    return new String(resource.readAllBytes());\n                }\n            } catch (IOException ex) {\n                // Fall through to throw original exception\n            }\n            throw new RuntimeException(\"Failed to load shader: \" + path, e);\n        }\n    }\n    \n    /**\n     * Compile a shader.\n     */\n    private int compileShader(int type, String source) {\n        int shader = GL33.glCreateShader(type);\n        GL33.glShaderSource(shader, source);\n        GL33.glCompileShader(shader);\n        \n        if (GL33.glGetShaderi(shader, GL33.GL_COMPILE_STATUS) == GL33.GL_FALSE) {\n            String log = GL33.glGetShaderInfoLog(shader);\n            GL33.glDeleteShader(shader);\n            throw new RuntimeException(\"Shader compilation failed: \" + log);\n        }\n        \n        return shader;\n    }\n    \n    /**\n     * Get uniform location and cache it.\n     */\n    public int getUniformLocation(int program, String name) {\n        String key = program + \":\" + name;\n        return uniforms.computeIfAbsent(key, k -> GL33.glGetUniformLocation(program, name));\n    }\n    \n    /**\n     * Set uniform matrix4f.\n     */\n    public void setUniformMatrix4f(int program, String name, FloatBuffer matrix) {\n        int location = getUniformLocation(program, name);\n        if (location != -1) {\n            GL33.glProgramUniformMatrix4fv(program, location, false, matrix);\n        }\n    }\n    \n    /**\n     * Set uniform float.\n     */\n    public void setUniformFloat(int program, String name, float value) {\n        int location = getUniformLocation(program, name);\n        if (location != -1) {\n            GL33.glProgramUniform1f(program, location, value);\n        }\n    }\n    \n    /**\n     * Set uniform int.\n     */\n    public void setUniformInt(int program, String name, int value) {\n        int location = getUniformLocation(program, name);\n        if (location != -1) {\n            GL33.glProgramUniform1i(program, location, value);\n        }\n    }\n    \n    /**\n     * Set uniform vec3.\n     */\n    public void setUniformVec3(int program, String name, float x, float y, float z) {\n        int location = getUniformLocation(program, name);\n        if (location != -1) {\n            GL33.glProgramUniform3f(program, location, x, y, z);\n        }\n    }\n    \n    /**\n     * Use a shader program.\n     */\n    public void useProgram(String name) {\n        Integer program = programs.get(name);\n        if (program != null) {\n            GL33.glUseProgram(program);\n        } else {\n            throw new RuntimeException(\"Shader program not found: \" + name);\n        }\n    }\n    \n    /**\n     * Get program ID by name.\n     */\n    public int getProgram(String name) {\n        Integer program = programs.get(name);\n        if (program != null) {\n            return program;\n        }\n        throw new RuntimeException(\"Shader program not found: \" + name);\n    }\n    \n    /**\n     * Clean up all shader resources.\n     */\n    public void cleanup() {\n        for (int program : programs.values()) {\n            GL33.glDeleteProgram(program);\n        }\n        programs.clear();\n        uniforms.clear();\n    }\n}