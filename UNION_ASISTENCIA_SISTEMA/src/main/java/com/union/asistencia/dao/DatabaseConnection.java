package com.union.asistencia.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:data/asistencia.db";
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error al cargar el driver de SQLite", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // ✅ VERIFICAR DATOS EXISTENTES CON CONEXIÓN FRESCA
            String checkDocentes = "SELECT COUNT(*) as count FROM docentes";
            var rs = stmt.executeQuery(checkDocentes);
            int count = rs.getInt("count");
            System.out.println("🔍 Docentes existentes en BD: " + count);

            // ✅ VERIFICAR ESTADO DE DOCENTES EXISTENTES
            String checkActivos = "SELECT codigo_docente, activo FROM docentes";
            var rsActivos = stmt.executeQuery(checkActivos);
            while (rsActivos.next()) {
                System.out.println("📋 Docente: " + rsActivos.getString("codigo_docente") +
                        " - Activo: " + rsActivos.getBoolean("activo"));
            }

            // Crear tabla de usuarios
            String createUsuariosTable = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    nombre TEXT NOT NULL,
                    apellido TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    rol TEXT DEFAULT 'DOCENTE',
                    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    activo BOOLEAN DEFAULT 1
                )
            """;

            // Crear tabla de estudiantes - ✅ EMAIL SIN UNIQUE
            String createEstudiantesTable = """
                CREATE TABLE IF NOT EXISTS estudiantes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo_estudiante TEXT UNIQUE NOT NULL,
                    dni TEXT UNIQUE NOT NULL,
                    nombre TEXT NOT NULL,
                    apellido TEXT NOT NULL,
                    email TEXT,
                    telefono TEXT,
                    carrera TEXT NOT NULL,
                    semestre INTEGER NOT NULL,
                    grupo TEXT,
                    fecha_nacimiento TEXT NOT NULL,
                    direccion TEXT,
                    activo BOOLEAN DEFAULT 1,
                    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;

            // Crear tabla de docentes
            String createDocentesTable = """
                CREATE TABLE IF NOT EXISTS docentes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo_docente TEXT UNIQUE NOT NULL,
                    dni TEXT UNIQUE NOT NULL,
                    nombre TEXT NOT NULL,
                    apellido TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    telefono TEXT,
                    facultad TEXT NOT NULL,
                    especialidad TEXT,
                    carga_horaria INTEGER DEFAULT 0,
                    fecha_contratacion TEXT NOT NULL,
                    activo BOOLEAN DEFAULT 1
                )
            """;

            // Crear tabla de asignaturas
            String createAsignaturasTable = """
                CREATE TABLE IF NOT EXISTS asignaturas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo_asignatura TEXT UNIQUE NOT NULL,
                    nombre TEXT NOT NULL,
                    creditos INTEGER NOT NULL,
                    horas_teoria INTEGER DEFAULT 0,
                    horas_practica INTEGER DEFAULT 0,
                    ciclo TEXT,
                    facultad TEXT NOT NULL,
                    plan_estudios TEXT,
                    docente_id INTEGER,
                    activo BOOLEAN DEFAULT 1,
                    FOREIGN KEY (docente_id) REFERENCES docentes(id)
                )
            """;

            // Crear tabla de aulas
            String createAulasTable = """
                CREATE TABLE IF NOT EXISTS aulas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo_aula TEXT UNIQUE NOT NULL,
                    nombre TEXT NOT NULL,
                    edificio TEXT NOT NULL,
                    capacidad INTEGER NOT NULL,
                    tipo TEXT DEFAULT 'AULA',
                    equipamiento TEXT,
                    disponible BOOLEAN DEFAULT 1,
                    observaciones TEXT
                )
            """;

            // Crear tabla de horarios
            String createHorariosTable = """
                CREATE TABLE IF NOT EXISTS horarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    dia_semana TEXT NOT NULL,
                    hora_inicio TEXT NOT NULL,
                    hora_fin TEXT NOT NULL,
                    tipo TEXT DEFAULT 'TEORIA',
                    aula TEXT,
                    asignatura_id INTEGER NOT NULL,
                    docente_id INTEGER NOT NULL,
                    FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id),
                    FOREIGN KEY (docente_id) REFERENCES docentes(id)
                )
            """;

            // Crear tabla de eventos
            String createEventosTable = """
                CREATE TABLE IF NOT EXISTS eventos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    tipo TEXT DEFAULT 'ACADEMICO',
                    descripcion TEXT,
                    fecha_hora_inicio TEXT NOT NULL,
                    fecha_hora_fin TEXT NOT NULL,
                    lugar TEXT NOT NULL,
                    responsable TEXT,
                    capacidad_maxima INTEGER,
                    requiere_inscripcion BOOLEAN DEFAULT 0,
                    activo BOOLEAN DEFAULT 1,
                    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;

            // Crear tabla de asistencias
            String createAsistenciasTable = """
                CREATE TABLE IF NOT EXISTS asistencias (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha_hora TEXT NOT NULL,
                    estado TEXT DEFAULT 'PRESENTE',
                    observaciones TEXT,
                    tipo_registro TEXT DEFAULT 'MANUAL',
                    estudiante_id INTEGER NOT NULL,
                    asignatura_id INTEGER NOT NULL,
                    docente_id INTEGER NOT NULL,
                    evento_id INTEGER,
                    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id),
                    FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id),
                    FOREIGN KEY (docente_id) REFERENCES docentes(id),
                    FOREIGN KEY (evento_id) REFERENCES eventos(id)
                )
            """;

            // Crear tabla de participantes_evento
            String createParticipantesEventoTable = """
                CREATE TABLE IF NOT EXISTS participantes_evento (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo_participante TEXT DEFAULT 'ESTUDIANTE',
                    asistio BOOLEAN DEFAULT 0,
                    fecha_inscripcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    codigo_qr TEXT,
                    evento_id INTEGER NOT NULL,
                    estudiante_id INTEGER,
                    docente_id INTEGER,
                    FOREIGN KEY (evento_id) REFERENCES eventos(id),
                    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id),
                    FOREIGN KEY (docente_id) REFERENCES docentes(id),
                    UNIQUE(evento_id, estudiante_id, docente_id)
                )
            """;

            // ✅ NUEVA TABLA: asistencias_eventos
            String createAsistenciasEventosTable = """
                CREATE TABLE IF NOT EXISTS asistencias_eventos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    evento_id INTEGER NOT NULL,
                    estudiante_id INTEGER,
                    docente_id INTEGER,
                    fecha_hora_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    tipo_registro TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    FOREIGN KEY (evento_id) REFERENCES eventos(id),
                    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id),
                    FOREIGN KEY (docente_id) REFERENCES docentes(id)
                )
            """;

            // Crear tabla de auditoria_login
            String createAuditoriaLoginTable = """
                CREATE TABLE IF NOT EXISTS auditoria_login (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    fecha_intento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    ip_address TEXT,
                    exito BOOLEAN DEFAULT 0,
                    intento_numero INTEGER,
                    user_agent TEXT
                )
            """;

            // Ejecutar todas las creaciones de tablas
            stmt.execute(createUsuariosTable);
            stmt.execute(createEstudiantesTable);
            stmt.execute(createDocentesTable);
            stmt.execute(createAsignaturasTable);
            stmt.execute(createAulasTable);
            stmt.execute(createHorariosTable);
            stmt.execute(createEventosTable);
            stmt.execute(createAsistenciasTable);
            stmt.execute(createParticipantesEventoTable);
            stmt.execute(createAsistenciasEventosTable);
            stmt.execute(createAuditoriaLoginTable);

            // Insertar datos iniciales
            insertInitialData(stmt);

            System.out.println("✅ Base de datos SQLite inicializada exitosamente!");

        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void insertInitialData(Statement stmt) throws SQLException {
        // ✅ ACTIVAR DOCENTES EXISTENTES PRIMERO
        String activarDocentes = "UPDATE docentes SET activo = 1 WHERE activo = 0 OR activo IS NULL";
        stmt.execute(activarDocentes);

        // Solo mantener el usuario admin mínimo para poder acceder al sistema
        String insertAdmin = """
            INSERT OR IGNORE INTO usuarios (username, password_hash, nombre, apellido, email, rol) 
            VALUES ('admin', 'admin', 'Administrador', 'Sistema', 'admin@upeu.edu.pe', 'ADMIN')
            """;
        stmt.execute(insertAdmin);

        // ✅ INSERTAR SOLO DOCENTES BÁSICOS (sin datos de prueba antiguos)
        String insertDocentes = """
            INSERT OR IGNORE INTO docentes (codigo_docente, dni, nombre, apellido, email, telefono, facultad, especialidad, carga_horaria, fecha_contratacion, activo) VALUES
            ('DOC001', '87654321', 'Roberto', 'Silva Mendoza', 'roberto.silva@upeu.edu.pe', '987654326', 'Ingeniería', 'Sistemas', 40, '2018-03-15', 1),
            ('DOC002', '76543210', 'Elena', 'Torres Rojas', 'elena.torres@upeu.edu.pe', '987654327', 'Ciencias de la Salud', 'Enfermería', 35, '2019-08-20', 1)
            """;
        stmt.execute(insertDocentes);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error al probar conexión: " + e.getMessage());
            return false;
        }
    }
}