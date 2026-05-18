# Black Hole Simulation - Java Implementation

A comprehensive Java port of the C++ black hole simulation, featuring exact Schwarzschild metric calculations, geodesic ray tracing, and gravitational lensing effects.

## 🌌 Features

### Core Physics Engine
- **Exact Schwarzschild Metric**: Implements precise spacetime curvature calculations
- **Sagittarius A* Parameters**: Real black hole with mass 8.54×10³⁶ kg 
- **Geodesic Ray Tracing**: 4th-order Runge-Kutta integration of light paths
- **Conserved Quantities**: Energy (E) and angular momentum (L) conservation
- **Event Horizon Detection**: Accurate Schwarzschild radius rs = 1.269×10¹⁰ m
- **Multi-Object Physics**: Support for up to 16 gravitational bodies

### Scientific Accuracy
- **Physical Constants**: CODATA-compliant values (c = 299,792,458 m/s, G = 6.674×10⁻¹¹ m³/kg/s²)
- **Coordinate Systems**: Spherical coordinates (r, θ, φ) with proper transformations
- **Christoffel Symbols**: Identical geodesic equations to C++ reference implementation
- **Numerical Precision**: Double-precision floating point throughout

### Java Implementation
- **Modern Java 17**: Professional architecture with latest language features
- **LWJGL 3 Ready**: OpenGL bindings prepared for GPU compute shaders
- **JOML Mathematics**: High-performance vector and matrix operations
- **Maven Build**: Cross-platform dependency management
- **Comprehensive Tests**: Unit tests validating physics accuracy

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Building and Running
```bash
# Clone the repository
git clone https://github.com/cyborgwastaken/BlackHole.git
cd BlackHole

# Build the project
mvn clean compile

# Run physics demonstration
mvn exec:java

# Run tests
mvn test
```

### Expected Output
```
=== Black Hole Physics Engine - Java Implementation ===

Created Sagittarius A* black hole:
BlackHole{position=( 0.000E+0  0.000E+0  0.000E+0), mass=8.54e+36 kg, rs=1.27e+10 m}

=== Ray Tracing Demonstration ===
Ray 1 - Start: r=1.27e+11 m, θ=1.571 rad
        Conserved Energy: E=9.482135e-01
        Angular Momentum: L=1.262093e+10
  Step 0: r=1.27e+11 m, position=(1.27e+11, 9.95e+05, 7.77e-06)
  ...
Final trail length: 1001 points
```

## 📋 Project Structure

```
src/main/java/com/blackhole/
├── PhysicsDemo.java              # Main demonstration program
├── physics/
│   ├── Constants.java            # Physical constants and parameters
│   ├── BlackHole.java           # Black hole representation
│   ├── Ray.java                 # Geodesic ray with RK4 integration
│   └── PhysicsEngine.java       # Complete physics simulation
├── camera/
│   └── Camera.java              # Orbital camera system (ready)
└── render/ (coming soon)
    ├── RenderEngine.java        # OpenGL rendering with LWJGL 3
    └── ShaderManager.java       # GPU compute shader management

src/main/resources/shaders/
├── geodesic.comp                # GPU ray tracing compute shader
├── grid.vert                   # Spacetime grid vertex shader  
└── grid.frag                   # Spacetime grid fragment shader
```

## 🔬 Physics Implementation

### Schwarzschild Metric
The simulation uses the exact Schwarzschild line element:
```
ds² = -(1-rs/r)c²dt² + (1-rs/r)⁻¹dr² + r²(dθ² + sin²θdφ²)
```

### Geodesic Equations
Ray paths follow geodesics computed via Christoffel symbols:
```java
// Radial acceleration  
d²r/dλ² = -(rs/2r²)f(dt/dλ)² + (rs/2r²f)(dr/dλ)² + r(dθ/dλ)² + r sin²θ(dφ/dλ)²

// Angular accelerations
d²θ/dλ² = -2(dr/dλ)(dθ/dλ)/r + sinθ cosθ(dφ/dλ)²
d²φ/dλ² = -2(dr/dλ)(dφ/dλ)/r - 2cotθ(dθ/dλ)(dφ/dλ)
```

### Conserved Quantities
- **Energy**: E = f × dt/dλ (constant along geodesics)
- **Angular Momentum**: L = r² sin²θ × dφ/dλ (axisymmetry)

## 🧪 Testing

The physics engine includes comprehensive unit tests:

```bash
mvn test
```

Tests validate:
- Physical constant accuracy
- Schwarzschild radius calculation  
- Event horizon detection
- Conservation law maintenance
- Geodesic integration precision

## 🎯 Performance

- **Ray Integration**: 1000+ steps in milliseconds
- **Conservation Error**: < 10⁻¹⁰ for energy, < 10³ for angular momentum  
- **Memory Efficient**: Minimal allocations during integration
- **Scalable**: Supports multiple concurrent ray traces

## 🔄 C++ Compatibility

This Java implementation maintains **exact physics compatibility** with the original C++ version:

| Feature | C++ | Java | Status |
|---------|-----|------|---------|
| Schwarzschild Metric | ✅ | ✅ | Identical |
| RK4 Integration | ✅ | ✅ | Same precision |
| Sagittarius A* Mass | ✅ | ✅ | 8.54×10³⁶ kg |
| Event Horizon | ✅ | ✅ | rs = 1.269×10¹⁰ m |
| Conserved Quantities | ✅ | ✅ | Energy & L |
| Multi-body Physics | ✅ | ✅ | Up to 16 objects |

## 🚧 Coming Soon

- **GPU Compute Shaders**: LWJGL 3 OpenGL ray tracing acceleration
- **Real-time Rendering**: 60 FPS interactive visualization  
- **Orbital Camera**: Mouse/keyboard controls for exploration
- **Accretion Disk**: Equatorial plane particle visualization
- **Spacetime Grid**: Curved spacetime mesh rendering

## 📚 References

- **Schwarzschild, K.** (1916). "Über das Gravitationsfeld eines Massenpunktes"
- **Chandrasekhar, S.** (1983). "The Mathematical Theory of Black Holes"
- **Event Horizon Telescope** Collaboration (2019). Sagittarius A* observations
- **CODATA 2018**: Fundamental physical constants

## 📄 License

This project maintains the same licensing as the original C++ implementation.

---

*Developed with exact scientific accuracy and professional Java engineering practices.*