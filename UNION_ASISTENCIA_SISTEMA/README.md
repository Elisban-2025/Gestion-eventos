# 🎓 SISTEMA DE GESTIÓN DE ASISTENCIA - UNIVERSIDAD PERUANA UNIÓN

Sistema completo de gestión de asistencia desarrollado para la Universidad Peruana Unión.

## 🚀 Características Principales

### 🔐 Seguridad Avanzada
- Autenticación con encriptación BCrypt
- Bloqueo automático después de 5 intentos fallidos
- Toggle de visibilidad de contraseña
- Auditoría de accesos

### 🧑‍🎓 Gestión Académica Completa
- **Gestión de Estudiantes**: Registro y control de información estudiantil
- **Gestión de Docentes**: Administración de datos del personal docente
- **Gestión de Asignaturas**: Control de materias y cursos
- **Control de Asistencia**: Registro con múltiples métodos (Manual, QR, Biométrico)
- **Gestión de Horarios**: Administración de horarios de clases
- **Gestión de Aulas**: Control de ambientes académicos
- **Gestión de Eventos**: Administración de eventos académicos y extracurriculares
- **Reportes y Estadísticas**: Generación de reportes detallados

### 🎨 Interfaz Moderna
- 3 temas personalizables (Claro, Oscuro, Institucional)
- Diseño responsive y profesional
- Navegación intuitiva
- Exportación a CSV

## 🛠️ Tecnologías Utilizadas

- **Java 17** - Lenguaje de programación
- **JavaFX** - Interfaz gráfica
- **MySQL** - Base de datos
- **Maven** - Gestión de dependencias
- **Lombok** - Reducción de código boilerplate
- **BCrypt** - Encriptación de contraseñas
- **ZXing** - Generación de códigos QR

## 📦 Instalación y Configuración

### Prerrequisitos
1. Java 17 o superior
2. MySQL Server 8.0+
3. Maven 3.6+

### Configuración de Base de Datos
1. Ejecutar el script `database_schema.sql`
2. La base de datos se creará automáticamente
3. Usuario por defecto: `admin` / Contraseña: `admin123`

### Ejecución del Proyecto
```bash
# Compilar con Maven
mvn clean compile

# Ejecutar la aplicación
mvn javafx:run

# O desde el IDE
Ejecutar Main.java