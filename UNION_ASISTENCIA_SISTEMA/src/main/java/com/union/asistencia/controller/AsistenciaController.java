package com.union.asistencia.controller;

import com.union.asistencia.dao.AsistenciaDAO;
import com.union.asistencia.dao.EstudianteDAO;
import com.union.asistencia.dao.AsignaturaDAO;
import com.union.asistencia.dao.DocenteDAO;
import com.union.asistencia.dao.EventoDAO;
import com.union.asistencia.model.*;
import com.union.asistencia.util.ExportUtils;
import com.union.asistencia.util.QRGenerator;
import com.union.asistencia.util.QRService;
import com.union.asistencia.util.QRReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.java.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log
public class AsistenciaController extends BaseController {

    // ==================== COMPONENTES DE LA TABLA (TableView) ====================
    /** Tabla principal que muestra los registros de asistencia */
    @FXML private TableView<Asistencia> tableView;

    /** Columna que muestra el ID único del registro */
    @FXML private TableColumn<Asistencia, Integer> colId;

    /** Columna que muestra la fecha y hora del registro */
    @FXML private TableColumn<Asistencia, LocalDateTime> colFechaHora;

    /** Columna que muestra el nombre del estudiante */
    @FXML private TableColumn<Asistencia, String> colEstudiante;

    /** Columna que muestra el nombre de la asignatura */
    @FXML private TableColumn<Asistencia, String> colAsignatura;

    /** Columna que muestra el estado de la asistencia */
    @FXML private TableColumn<Asistencia, String> colEstado;

    /** Columna que muestra el tipo de registro utilizado */
    @FXML private TableColumn<Asistencia, String> colTipoRegistro;

    /** Columna que muestra las observaciones del registro */
    @FXML private TableColumn<Asistencia, String> colObservaciones;

    // ==================== COMPONENTES DEL FORMULARIO ====================
    /** Selector de fecha para filtros */
    @FXML private DatePicker dpFecha;

    /** ComboBox para seleccionar estudiante */
    @FXML private ComboBox<Estudiante> cbxEstudiante;

    /** ComboBox para seleccionar asignatura */
    @FXML private ComboBox<Asignatura> cbxAsignatura;

    /** ComboBox para seleccionar docente */
    @FXML private ComboBox<Docente> cbxDocente;

    /** ComboBox para seleccionar estado de asistencia */
    @FXML private ComboBox<String> cbxEstado;

    /** ComboBox para seleccionar tipo de registro */
    @FXML private ComboBox<String> cbxTipoRegistro;

    /** Área de texto para observaciones adicionales */
    @FXML private TextArea txtObservaciones;

    /** ComboBox para filtrar por asignatura en la tabla */
    @FXML private ComboBox<Asignatura> cbxAsignaturaFiltro;

    // ==================== BOTONES DE ACCIÓN ====================
    /** Botón para guardar o actualizar registros */
    @FXML private Button btnGuardar;

    /** Botón para cancelar operaciones */
    @FXML private Button btnCancelar;

    /** Botón para generar códigos QR */
    @FXML private Button btnGenerarQR;

    /** Botón para exportar datos */
    @FXML private Button btnExportar;

    /** Botón para aplicar filtros */
    @FXML private Button btnFiltrar;

    /** Botón para eliminar registros */
    @FXML private Button btnEliminar;

    // ==================== COMPONENTES QR MEJORADOS ====================
    /** Botón para generar QR personal */
    @FXML private Button btnMiQR;

    /** Botón para escanear QR */
    @FXML private Button btnEscanearQR;

    /** ImageView para mostrar el código QR generado */
    @FXML private ImageView imgQRAsistencia;

    // ==================== LISTAS OBSERVABLES DE DATOS ====================
    /** Lista observable de registros de asistencia para la tabla */
    private ObservableList<Asistencia> asistenciasList;

    /** Lista observable de estudiantes para los ComboBox */
    private ObservableList<Estudiante> estudiantesList;

    /** Lista observable de asignaturas para los ComboBox */
    private ObservableList<Asignatura> asignaturasList;

    /** Lista observable de docentes para los ComboBox */
    private ObservableList<Docente> docentesList;

    // ==================== OBJETOS DE ACCESO A DATOS (DAOs) ====================
    /** DAO para operaciones de asistencia */
    private AsistenciaDAO asistenciaDAO;

    /** DAO para operaciones de estudiantes */
    private EstudianteDAO estudianteDAO;

    /** DAO para operaciones de asignaturas */
    private AsignaturaDAO asignaturaDAO;

    /** DAO para operaciones de docentes */
    private DocenteDAO docenteDAO;

    /** DAO para operaciones de eventos */
    private EventoDAO eventoDAO;

    // ==================== VARIABLES DE CONTROL DE ESTADO ====================
    /** Usuario actualmente logueado en el sistema */
    private Usuario usuarioLogueado;

    /** Asistencia actualmente seleccionada en la tabla */
    private Asistencia asistenciaSeleccionada;

    // ==================== MÉTODOS DE LA INTERFAZ BaseController ====================

    /**
     * Método obligatorio de la interfaz BaseController.
     * Se ejecuta automáticamente cuando se carga el controlador y recibe el usuario logueado.
     *
     * @param usuario Objeto Usuario con la información del usuario autenticado
     */
    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    // ==================== MÉTODOS DE INICIALIZACIÓN ====================

    /**
     * Método de inicialización automática de JavaFX.
     * Se ejecuta cuando se carga el archivo FXML y antes de que se muestre la interfaz.
     * Configura los componentes básicos del controlador.
     */
    @FXML
    private void initialize() {
        log.info("🔄 Inicializando AsistenciaController...");

        // ==================== INICIALIZACIÓN DE DAOs ====================
        asistenciaDAO = new AsistenciaDAO();
        estudianteDAO = new EstudianteDAO();
        asignaturaDAO = new AsignaturaDAO();
        docenteDAO = new DocenteDAO();
        eventoDAO = new EventoDAO(); // ✅ AGREGADO: Inicializar EventoDAO

        // ==================== INICIALIZACIÓN DE LISTAS OBSERVABLES ====================
        asistenciasList = FXCollections.observableArrayList();
        estudiantesList = FXCollections.observableArrayList();
        asignaturasList = FXCollections.observableArrayList();
        docentesList = FXCollections.observableArrayList();

        // ==================== INICIALIZACIÓN DE VARIABLES DE ESTADO ====================
        asistenciaSeleccionada = null;

        // ==================== CONFIGURACIÓN DE COMPONENTES ====================
        configurarTabla();
        configurarCombobox();
        configurarEventos();

        // Vincular la lista observable con la tabla
        tableView.setItems(asistenciasList);
        log.info("✅ AsistenciaController inicializado correctamente");
    }

    /**
     * Inicialización personalizada que se ejecuta después de recibir el usuario logueado.
     * Carga los datos iniciales y prepara la interfaz para el uso.
     */
    private void inicializar() {
        log.info("🔄 Cargando datos de asistencia...");
        cargarDatos();
        limpiarFormulario();

        // Temporal: diagnóstico
        diagnosticarUsuarios();

        log.info("✅ Datos de asistencia cargados correctamente");
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN DE COMPONENTES ====================

    /**
     * Configura las columnas de la tabla de asistencias.
     * Define cómo se mapean las propiedades del objeto Asistencia a las columnas visuales.
     */
    private void configurarTabla() {
        log.info("🔄 Configurando tabla de asistencias...");

        // ==================== CONFIGURACIÓN DE CELL VALUE FACTORIES ====================
        // Columna ID: usa PropertyValueFactory estándar
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Columna Fecha/Hora: usa PropertyValueFactory estándar
        colFechaHora.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));

        // ==================== CONFIGURACIÓN PERSONALIZADA PARA COLUMNAS DE RELACIONES ====================
        // Columna Estudiante: usa lambda para mostrar nombre completo del estudiante
        colEstudiante.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            if (asistencia.getEstudiante() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        asistencia.getEstudiante().getNombreCompleto()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // Columna Asignatura: usa lambda para mostrar nombre de la asignatura
        colAsignatura.setCellValueFactory(cellData -> {
            Asistencia asistencia = cellData.getValue();
            if (asistencia.getAsignatura() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        asistencia.getAsignatura().getNombreCompleto()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });

        // ==================== CONFIGURACIÓN ESTÁNDAR PARA OTRAS COLUMNAS ====================
        // Columna Estado: usa PropertyValueFactory estándar
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Columna Tipo Registro: usa PropertyValueFactory estándar
        colTipoRegistro.setCellValueFactory(new PropertyValueFactory<>("tipoRegistro"));

        // Columna Observaciones: usa PropertyValueFactory estándar
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        log.info("✅ Tabla de asistencias configurada");
    }

    /**
     * Configura los ComboBox con sus datos y renderizadores personalizados.
     * Carga las listas de estudiantes, asignaturas y docentes desde la base de datos.
     */
    private void configurarCombobox() {
        log.info("🔄 Configurando ComboBox...");

        // ==================== CONFIGURACIÓN DE OPCIONES FIJAS ====================
        // Estados predefinidos para la asistencia
        cbxEstado.getItems().addAll("PRESENTE", "AUSENTE", "TARDANZA", "JUSTIFICADO");

        // Tipos de registro predefinidos
        cbxTipoRegistro.getItems().addAll("MANUAL", "QR", "BIOMETRICO");

        // ==================== CONFIGURACIÓN DE COMBOBOX DE FILTRO ====================
        // Vincular lista de asignaturas al ComboBox de filtro
        cbxAsignaturaFiltro.setItems(asignaturasList);

        // Configurar cómo se muestran las asignaturas en el ComboBox de filtro
        cbxAsignaturaFiltro.setCellFactory(param -> new ListCell<Asignatura>() {
            @Override
            protected void updateItem(Asignatura item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto());
            }
        });

        // ==================== CARGA Y CONFIGURACIÓN DE ESTUDIANTES ====================
        // Cargar estudiantes desde la base de datos
        estudiantesList.setAll(estudianteDAO.obtenerTodos());

        // Vincular lista al ComboBox
        cbxEstudiante.setItems(estudiantesList);

        // Configurar visualización personalizada de estudiantes
        cbxEstudiante.setCellFactory(param -> new ListCell<Estudiante>() {
            @Override
            protected void updateItem(Estudiante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto() + " - " + item.getCodigoEstudiante());
            }
        });

        // ==================== CARGA Y CONFIGURACIÓN DE ASIGNATURAS ====================
        // Cargar asignaturas desde la base de datos
        asignaturasList.setAll(asignaturaDAO.obtenerTodos());

        // Vincular lista al ComboBox
        cbxAsignatura.setItems(asignaturasList);

        // Configurar visualización personalizada de asignaturas
        cbxAsignatura.setCellFactory(param -> new ListCell<Asignatura>() {
            @Override
            protected void updateItem(Asignatura item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto());
            }
        });

        // ==================== CARGA Y CONFIGURACIÓN DE DOCENTES ====================
        // Cargar docentes desde la base de datos
        docentesList.setAll(docenteDAO.obtenerTodos());

        // Vincular lista al ComboBox
        cbxDocente.setItems(docentesList);

        // Configurar visualización personalizada de docentes
        cbxDocente.setCellFactory(param -> new ListCell<Docente>() {
            @Override
            protected void updateItem(Docente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto() + " - " + item.getCodigoDocente());
            }
        });

        log.info("✅ ComboBox configurados correctamente");
    }

    private void configurarEventos() {
        log.info("🔄 Configurando eventos...");

        // ==================== EVENTO DE SELECCIÓN EN LA TABLA ====================
        // Listener que se activa cuando se selecciona una fila en la tabla
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        asistenciaSeleccionada = newSelection;
                        cargarDatosFormulario(newSelection);
                        log.info("📋 Asistencia seleccionada: " + newSelection.getId());
                    }
                });

        // ==================== EVENTO DE CLIC EN ÁREA VACÍA DE LA TABLA ====================
        // Permite limpiar la selección al hacer clic en áreas vacías de la tabla
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1 && tableView.getSelectionModel().getSelectedItem() == null) {
                limpiarSeleccion();
                log.info("🧹 Selección limpiada");
            }
        });

        log.info("✅ Eventos configurados correctamente");
    }

    // ==================== MÉTODOS DE CARGA DE DATOS ====================


    private void cargarDatos() {
        try {
            log.info("📥 Cargando asistencias desde BD...");

            // Obtener todas las asistencias desde la base de datos
            asistenciasList.setAll(asistenciaDAO.obtenerTodas());

            log.info("✅ " + asistenciasList.size() + " asistencias cargadas correctamente");
        } catch (Exception e) {
            log.severe("❌ Error al cargar asistencias: " + e.getMessage());

            // Mostrar alerta al usuario en caso de error
            mostrarAlerta("Error", "No se pudieron cargar las asistencias: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarDatosFormulario(Asistencia asistencia) {
        log.info("📝 Cargando datos en formulario para asistencia ID: " + asistencia.getId());

        // ==================== BUSCAR Y ESTABLECER ESTUDIANTE CORRESPONDIENTE ====================
        // Buscar en la lista de estudiantes el que coincide con el ID de la asistencia
        Estudiante estudiante = estudiantesList.stream()
                .filter(e -> e.getId().equals(asistencia.getEstudianteId()))
                .findFirst()
                .orElse(null);
        cbxEstudiante.setValue(estudiante);

        // ==================== BUSCAR Y ESTABLECER ASIGNATURA CORRESPONDIENTE ====================
        // Buscar en la lista de asignaturas la que coincide con el ID de la asistencia
        Asignatura asignatura = asignaturasList.stream()
                .filter(a -> a.getId().equals(asistencia.getAsignaturaId()))
                .findFirst()
                .orElse(null);
        cbxAsignatura.setValue(asignatura);

        // ==================== BUSCAR Y ESTABLECER DOCENTE CORRESPONDIENTE ====================
        // Buscar en la lista de docentes el que coincide con el ID de la asistencia
        Docente docente = docentesList.stream()
                .filter(d -> d.getId().equals(asistencia.getDocenteId()))
                .findFirst()
                .orElse(null);
        cbxDocente.setValue(docente);

        // ==================== ESTABLECER VALORES DIRECTOS EN FORMULARIO ====================
        cbxEstado.setValue(asistencia.getEstado());
        cbxTipoRegistro.setValue(asistencia.getTipoRegistro());
        txtObservaciones.setText(asistencia.getObservaciones());

        // ==================== CAMBIAR TEXTO DEL BOTÓN PARA INDICAR MODO EDICIÓN ====================
        btnGuardar.setText("💾 Actualizar Asistencia");

        log.info("✅ Datos cargados en formulario correctamente");
    }

    // ==================== MÉTODOS PRINCIPALES (ACCIONES DE USUARIO) ====================

    @FXML
    private void guardarAsistencia() {
        log.info("💾 Intentando guardar asistencia...");

        // Validar el formulario antes de proceder
        if (validarFormulario()) {
            Asistencia asistencia;
            boolean esNuevo = false;

            // ==================== DETERMINAR SI ES NUEVO REGISTRO O ACTUALIZACIÓN ====================
            if (asistenciaSeleccionada == null) {
                asistencia = new Asistencia();
                esNuevo = true;
                log.info("➕ Nuevo registro de asistencia");
            } else {
                asistencia = asistenciaSeleccionada;
                log.info("✏️ Actualizando asistencia ID: " + asistencia.getId());
            }

            // ==================== ESTABLECER VALORES DESDE EL FORMULARIO ====================
            asistencia.setFechaHora(LocalDateTime.now());
            asistencia.setEstado(cbxEstado.getValue());
            asistencia.setObservaciones(txtObservaciones.getText().trim());
            asistencia.setTipoRegistro(cbxTipoRegistro.getValue());
            asistencia.setEstudianteId(cbxEstudiante.getValue().getId());
            asistencia.setAsignaturaId(cbxAsignatura.getValue().getId());
            asistencia.setDocenteId(cbxDocente.getValue().getId());

            // ==================== EJECUTAR OPERACIÓN EN BASE DE DATOS ====================
            boolean exito;
            if (esNuevo) {
                exito = asistenciaDAO.guardar(asistencia);
            } else {
                exito = asistenciaDAO.actualizar(asistencia);
            }

            // ==================== MANEJAR RESULTADO DE LA OPERACIÓN ====================
            if (exito) {
                String mensaje = esNuevo ? "Asistencia registrada correctamente" : "Asistencia actualizada correctamente";
                mostrarAlerta("Éxito", mensaje, Alert.AlertType.INFORMATION);
                log.info("✅ " + mensaje);

                // Limpiar formulario y recargar datos después de operación exitosa
                limpiarFormulario();
                cargarDatos();
            } else {
                String mensaje = esNuevo ? "No se pudo registrar la asistencia" : "No se pudo actualizar la asistencia";
                mostrarAlerta("Error", mensaje, Alert.AlertType.ERROR);
                log.severe("❌ " + mensaje);
            }
        } else {
            log.warning("⚠️ Validación de formulario fallida");
        }
    }

    @FXML
    private void eliminarAsistencia() {
        // Verificar que hay una asistencia seleccionada
        if (asistenciaSeleccionada != null) {
            log.info("🗑️ Solicitando eliminación de asistencia ID: " + asistenciaSeleccionada.getId());

            // ==================== ALERTA DE CONFIRMACIÓN ====================
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar la asistencia?");
            alert.setContentText("Esta acción no se puede deshacer.");

            // Esperar confirmación del usuario
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Proceder con eliminación si usuario confirma
                if (asistenciaDAO.eliminar(asistenciaSeleccionada.getId())) {
                    mostrarAlerta("Éxito", "Asistencia eliminada correctamente", Alert.AlertType.INFORMATION);
                    log.info("✅ Asistencia eliminada correctamente");

                    // Limpiar formulario y recargar datos después de eliminación
                    limpiarFormulario();
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la asistencia", Alert.AlertType.ERROR);
                    log.severe("❌ Error al eliminar asistencia");
                }
            } else {
                log.info("❌ Eliminación cancelada por el usuario");
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione una asistencia para eliminar", Alert.AlertType.WARNING);
            log.warning("⚠️ Intento de eliminar sin asistencia seleccionada");
        }
    }

    @FXML
    private void filtrarAsistencias() {
        log.info("🔍 Aplicando filtros...");

        // Obtener valores actuales de los filtros
        LocalDate fecha = dpFecha.getValue();
        Asignatura asignatura = cbxAsignaturaFiltro.getValue();

        // ==================== APLICAR FILTROS SI HAY VALORES SELECCIONADOS ====================
        if (fecha != null || asignatura != null) {
            // Crear una nueva lista filtrada de manera segura
            ObservableList<Asistencia> listaFiltrada = FXCollections.observableArrayList();

            // Filtrar la lista original basándose en los criterios seleccionados
            for (Asistencia a : asistenciasList) {
                boolean coincideFecha = fecha == null ||
                        (a.getFechaHora() != null && a.getFechaHora().toLocalDate().equals(fecha));
                boolean coincideAsignatura = asignatura == null ||
                        (a.getAsignaturaId() != null && a.getAsignaturaId().equals(asignatura.getId()));

                if (coincideFecha && coincideAsignatura) {
                    listaFiltrada.add(a);
                }
            }

            // Actualizar la tabla con la lista filtrada de manera segura
            tableView.getItems().setAll(listaFiltrada);
            log.info("✅ Filtros aplicados - " + listaFiltrada.size() + " registros encontrados");
        } else {
            // Si no hay filtros, recargar todos los datos
            cargarDatos();
            log.info("🔄 Sin filtros - Recargando todos los datos");
        }
    }

    @FXML
    private void exportarDatos() {
        // Verificar que hay datos para exportar
        if (asistenciasList.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos para exportar", Alert.AlertType.WARNING);
            log.warning("⚠️ Intento de exportar sin datos");
            return;
        }

        log.info("📊 Exportando datos a CSV...");

        // Utilizar utilidad de exportación para generar archivo CSV
        ExportUtils.exportarTablaACSV(tableView, "Reporte_Asistencias_UPeU");
        log.info("✅ Datos exportados correctamente");
    }


    @FXML
    private void generarQR() {
        // Verificar que hay estudiante y asignatura seleccionados
        if (cbxEstudiante.getValue() != null && cbxAsignatura.getValue() != null) {
            log.info("📱 Generando código QR...");

            // Generar código único para la asistencia
            String codigoQR = QRGenerator.generarCodigoAsistencia(
                    cbxEstudiante.getValue().getCodigoEstudiante(),
                    cbxAsignatura.getValue().getCodigoAsignatura()
            );

            // Generar imagen QR a partir del código
            var qrImage = QRGenerator.generarQRCode(codigoQR, 200, 200);
            if (qrImage != null) {
                // Mostrar imagen QR en la interfaz
                imgQRAsistencia.setImage(qrImage);
                log.info("✅ Código QR generado correctamente");
            } else {
                mostrarAlerta("Error", "No se pudo generar el código QR", Alert.AlertType.ERROR);
                log.severe("❌ Error al generar código QR");
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione un estudiante y una asignatura para generar QR", Alert.AlertType.WARNING);
            log.warning("⚠️ Intento de generar QR sin estudiante o asignatura seleccionada");
        }
    }

    @FXML
    private void cancelar() {
        log.info("❌ Cancelando operación...");
        limpiarFormulario();
        log.info("✅ Operación cancelada - Formulario limpiado");
    }

    // ==================== MÉTODOS QR MEJORADOS (NUEVOS) ====================


    @FXML
    private void generarMiQR() {
        log.info("📱 Generando código QR personalizado...");

        try {
            // Verificar que hay usuario logueado
            if (usuarioLogueado == null) {
                mostrarAlerta("Error", "No hay usuario logueado", Alert.AlertType.ERROR);
                return;
            }

            String qrData;
            String tipoUsuario;

            // Generar datos QR según el tipo de usuario
            if ("ESTUDIANTE".equals(usuarioLogueado.getRol())) {
                // Buscar estudiante por username o email
                Estudiante estudiante = estudiantesList.stream()
                        .filter(e -> {
                            String username = usuarioLogueado.getUsername();
                            return (e.getEmail() != null && e.getEmail().equals(usuarioLogueado.getEmail())) ||
                                    (e.getCodigoEstudiante() != null && e.getCodigoEstudiante().equals(username));
                        })
                        .findFirst()
                        .orElse(null);

                if (estudiante != null) {
                    qrData = QRService.generarDataEstudiante(
                            String.valueOf(estudiante.getId()),
                            estudiante.getCodigoEstudiante(),
                            estudiante.getNombreCompleto(),
                            "POO-2025"
                    );
                    tipoUsuario = "Estudiante";

                    // Auto-seleccionar en el combobox
                    cbxEstudiante.setValue(estudiante);
                    log.info("🎓 QR generado para estudiante: " + estudiante.getNombreCompleto());
                } else {
                    mostrarAlerta("Error",
                            "No se encontró información del estudiante\n\n" +
                                    "Usuario: " + usuarioLogueado.getUsername() + "\n" +
                                    "Email: " + usuarioLogueado.getEmail() + "\n" +
                                    "Rol: " + usuarioLogueado.getRol(),
                            Alert.AlertType.ERROR);
                    return;
                }

            } else if ("DOCENTE".equals(usuarioLogueado.getRol())) {
                // Buscar docente por username o email
                Docente docente = docentesList.stream()
                        .filter(d -> {
                            String username = usuarioLogueado.getUsername();
                            return (d.getEmail() != null && d.getEmail().equals(usuarioLogueado.getEmail())) ||
                                    (d.getCodigoDocente() != null && d.getCodigoDocente().equals(username));
                        })
                        .findFirst()
                        .orElse(null);

                if (docente != null) {
                    qrData = QRService.generarDataDocente(
                            String.valueOf(docente.getId()),
                            docente.getCodigoDocente(),
                            docente.getNombreCompleto(),
                            "Programación"
                    );
                    tipoUsuario = "Docente";

                    // Auto-seleccionar en el combobox
                    cbxDocente.setValue(docente);
                    log.info("👨‍🏫 QR generado para docente: " + docente.getNombreCompleto());
                } else {
                    mostrarAlerta("Error",
                            "No se encontró información del docente\n\n" +
                                    "Usuario: " + usuarioLogueado.getUsername() + "\n" +
                                    "Email: " + usuarioLogueado.getEmail() + "\n" +
                                    "Rol: " + usuarioLogueado.getRol(),
                            Alert.AlertType.ERROR);
                    return;
                }

            } else if ("ADMINISTRADOR".equals(usuarioLogueado.getRol())) {
                // Para administradores, generar un QR genérico
                qrData = QRService.generarDataDocente(
                        String.valueOf(usuarioLogueado.getId()),
                        usuarioLogueado.getUsername(),
                        usuarioLogueado.getNombreCompleto(),
                        "Administración"
                );
                tipoUsuario = "Administrador";
                log.info("👨‍💼 QR generado para administrador: " + usuarioLogueado.getNombreCompleto());

            } else {
                mostrarAlerta("Error",
                        "Rol no compatible con sistema QR\n\n" +
                                "Tu rol: " + usuarioLogueado.getRol() + "\n" +
                                "Roles válidos: ESTUDIANTE, DOCENTE, ADMINISTRADOR",
                        Alert.AlertType.ERROR);
                return;
            }

            // Generar y mostrar imagen QR
            ImageView qrImageView = QRService.generarQR(qrData, 200, 200);
            if (qrImageView != null && qrImageView.getImage() != null) {
                imgQRAsistencia.setImage(qrImageView.getImage());

                // Mostrar confirmación
                mostrarAlerta("QR Generado",
                        "✅ Código QR generado para " + tipoUsuario + "\n\n" +
                                "📱 Puede escanearlo con cualquier app lectora QR\n" +
                                "⏰ Válido por 5 minutos",
                        Alert.AlertType.INFORMATION);

                log.info("✅ QR generado exitosamente para " + tipoUsuario);
            } else {
                mostrarAlerta("Error", "No se pudo generar la imagen QR", Alert.AlertType.ERROR);
                log.severe("❌ Error al generar imagen QR");
            }

        } catch (Exception e) {
            log.severe("❌ Error generando QR: " + e.getMessage());
            mostrarAlerta("Error", "No se pudo generar el código QR: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Escanea código QR y registra asistencia automáticamente
     * Se ejecuta cuando el usuario hace clic en "Escanear QR"
     */
    @FXML
    private void escanearQR() {
        log.info("📱 Iniciando escaneo de QR...");

        try {
            // Seleccionar archivo QR
            String qrData = QRReader.seleccionarYLeerQR();

            if (qrData != null && !qrData.isEmpty()) {
                log.info("📱 QR escaneado: " + qrData);

                // Procesar QR de evento (formato: UPeU-ASIST-202434565-IG234-1763933574081)
                if (qrData.startsWith("UPeU-ASIST-")) {
                    procesarQREvento(qrData);
                }
                // Procesar QR personalizado (tu código existente)
                else {
                    QRService.QRData datosQR = QRService.procesarQRData(qrData);
                    if (datosQR != null) {
                        // Tu código existente para QR personalizado...
                        if (datosQR.esEstudiante()) {
                            procesarQREstudiante(datosQR);
                        } else if (datosQR.esDocente()) {
                            procesarQRDocente(datosQR);
                        }
                    }
                }
            } else {
                log.info("❌ Escaneo cancelado o QR inválido");
            }

        } catch (Exception e) {
            log.severe("❌ Error escaneando QR: " + e.getMessage());
            mostrarAlerta("Error", "Error al escanear código QR: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Procesa QR de estudiante y registra asistencia
     */
    private void procesarQREstudiante(QRService.QRData datosQR) {
        log.info("🎓 Procesando QR de estudiante: " + datosQR.getNombre());

        try {
            // Buscar estudiante en la lista
            Estudiante estudiante = estudiantesList.stream()
                    .filter(e -> e.getId().equals(datosQR.getId()))
                    .findFirst()
                    .orElse(null);

            if (estudiante != null) {
                // Auto-completar formulario
                cbxEstudiante.setValue(estudiante);
                cbxEstado.setValue("PRESENTE");
                cbxTipoRegistro.setValue("QR");

                // Si hay asignatura seleccionada, guardar automáticamente
                if (cbxAsignatura.getValue() != null && cbxDocente.getValue() != null) {
                    // Mostrar confirmación
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Registrar Asistencia");
                    alert.setHeaderText("¿Registrar asistencia automáticamente?");
                    alert.setContentText("Estudiante: " + estudiante.getNombreCompleto() +
                            "\nAsignatura: " + cbxAsignatura.getValue().getNombreCompleto() +
                            "\nDocente: " + cbxDocente.getValue().getNombreCompleto());

                    if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        guardarAsistencia();
                        log.info("✅ Asistencia registrada automáticamente vía QR");
                    }
                } else {
                    mostrarAlerta("Datos Completados",
                            "✅ Datos de estudiante cargados\n\n" +
                                    "👤 Estudiante: " + estudiante.getNombreCompleto() +
                                    "\n📚 Complete asignatura y docente para guardar",
                            Alert.AlertType.INFORMATION);
                }

            } else {
                mostrarAlerta("Estudiante No Encontrado",
                        "No se encontró al estudiante en el sistema",
                        Alert.AlertType.WARNING);
            }

        } catch (Exception e) {
            log.severe("❌ Error procesando QR estudiante: " + e.getMessage());
            mostrarAlerta("Error", "Error al procesar QR de estudiante", Alert.AlertType.ERROR);
        }
    }

    /**
     * Procesa QR de docente y auto-completa formulario
     */
    private void procesarQRDocente(QRService.QRData datosQR) {
        log.info("👨‍🏫 Procesando QR de docente: " + datosQR.getNombre());

        try {
            // Buscar docente en la lista
            Docente docente = docentesList.stream()
                    .filter(d -> d.getId().equals(datosQR.getId()))
                    .findFirst()
                    .orElse(null);

            if (docente != null) {
                // Auto-completar formulario
                cbxDocente.setValue(docente);

                mostrarAlerta("Docente Cargado",
                        "✅ Datos de docente cargados\n\n" +
                                "👨‍🏫 Docente: " + docente.getNombreCompleto() +
                                "\n📚 Complete los demás datos para registrar asistencia",
                        Alert.AlertType.INFORMATION);

                log.info("✅ Docente cargado desde QR: " + docente.getNombreCompleto());

            } else {
                mostrarAlerta("Docente No Encontrado",
                        "No se encontró al docente en el sistema",
                        Alert.AlertType.WARNING);
            }

        } catch (Exception e) {
            log.severe("❌ Error procesando QR docente: " + e.getMessage());
            mostrarAlerta("Error", "Error al procesar QR de docente", Alert.AlertType.ERROR);
        }
    }

    /**
     * Procesa QR de evento y registra asistencia automáticamente
     * Formato: UPeU-ASIST-{CODIGO_ESTUDIANTE}-{CODIGO_EVENTO}-{TIMESTAMP}
     */
    private void procesarQREvento(String qrData) {
        try {
            log.info("🎯 Procesando QR de evento: " + qrData);

            // Parsear datos del QR (formato: UPeU-ASIST-202434565-IG234-1763933574081)
            String[] partes = qrData.split("-");
            if (partes.length < 5) {
                mostrarAlerta("QR Inválido", "Formato de QR de evento incorrecto", Alert.AlertType.ERROR);
                return;
            }

            String codigoEstudiante = partes[2]; // Ej: 202434565
            String codigoEvento = partes[3];     // Ej: IG234
            String timestampStr = partes[4];     // Ej: 1763933574081

            // Validar timestamp (QR válido por 10 minutos)
            long timestampQR = Long.parseLong(timestampStr);
            long tiempoActual = System.currentTimeMillis();
            long diferencia = tiempoActual - timestampQR;

            if (diferencia > 10 * 60 * 1000) { // 10 minutos
                mostrarAlerta("QR Expirado",
                        "❌ El código QR ha expirado\n\n" +
                                "⏰ Generado hace: " + (diferencia/1000/60) + " minutos\n" +
                                "📱 Genere un nuevo código QR",
                        Alert.AlertType.WARNING);
                return;
            }

            // Obtener ID del estudiante
            Integer estudianteId = asistenciaDAO.obtenerEstudianteIdPorCodigo(codigoEstudiante);
            if (estudianteId == null) {
                mostrarAlerta("Estudiante No Encontrado",
                        "❌ No se encontró al estudiante con código: " + codigoEstudiante,
                        Alert.AlertType.WARNING);
                return;
            }

            // Obtener estudiante para mostrar info
            Estudiante estudiante = estudiantesList.stream()
                    .filter(e -> e.getCodigoEstudiante().equals(codigoEstudiante))
                    .findFirst()
                    .orElse(null);

            // Buscar evento por código (asumiendo que el código del evento está en la tabla eventos)
            // Si no tienes campo código_evento, necesitaríamos mapear codigoEvento a eventoId
            // Por ahora, usar un mapeo temporal
            Integer eventoId = mapearCodigoEventoAId(codigoEvento);

            if (eventoId == null) {
                mostrarAlerta("Evento No Encontrado",
                        "❌ No se encontró el evento con código: " + codigoEvento,
                        Alert.AlertType.WARNING);
                return;
            }

            // Verificar si el evento existe
            if (!eventoDAO.eventoExiste(eventoId)) {
                mostrarAlerta("Evento No Válido",
                        "❌ El evento no existe o no está activo",
                        Alert.AlertType.WARNING);
                return;
            }

            // Verificar si ya tiene asistencia registrada
            if (asistenciaDAO.existeAsistenciaEvento(estudianteId, eventoId)) {
                mostrarAlerta("Asistencia Ya Registrada",
                        "✅ Ya tienes asistencia registrada para este evento\n\n" +
                                "👤 Estudiante: " + (estudiante != null ? estudiante.getNombreCompleto() : codigoEstudiante) +
                                "\n📅 Código Evento: " + codigoEvento,
                        Alert.AlertType.INFORMATION);
                return;
            }

            // Registrar asistencia automáticamente
            boolean exito = asistenciaDAO.registrarAsistenciaDesdeQR(estudianteId, eventoId, "QR");

            if (exito) {
                // Obtener info del evento para mostrar
                Optional<Evento> eventoOpt = eventoDAO.obtenerEventoPorId(eventoId);
                String nombreEvento = eventoOpt.map(Evento::getNombre).orElse("Evento " + codigoEvento);

                mostrarAlerta("Asistencia Registrada",
                        "✅ Asistencia registrada automáticamente\n\n" +
                                "👤 Estudiante: " + (estudiante != null ? estudiante.getNombreCompleto() : codigoEstudiante) +
                                "\n🎯 Evento: " + nombreEvento +
                                "\n📅 Código: " + codigoEvento +
                                "\n⏰ Hora: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) +
                                "\n📱 Método: QR Automático",
                        Alert.AlertType.INFORMATION);

                log.info("✅ Asistencia automática registrada - Estudiante: " + codigoEstudiante + ", Evento: " + codigoEvento);

                // Recargar datos para mostrar en tabla
                cargarDatos();

            } else {
                mostrarAlerta("Error",
                        "❌ No se pudo registrar la asistencia automáticamente",
                        Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            log.severe("❌ Error parseando QR de evento: " + e.getMessage());
            mostrarAlerta("QR Inválido", "El código QR tiene formato incorrecto", Alert.AlertType.ERROR);
        } catch (Exception e) {
            log.severe("❌ Error procesando QR de evento: " + e.getMessage());
            mostrarAlerta("Error", "Error al procesar el código QR de evento", Alert.AlertType.ERROR);
        }
    }

    /**
     * Mapea código de evento a ID de evento (necesitas implementar según tu estructura)
     * Por ahora es un mapeo temporal - debes adaptarlo a tu base de datos
     */
    private Integer mapearCodigoEventoAId(String codigoEvento) {
        // EJEMPLO: Mapeo temporal - DEBES ADAPTAR ESTO A TU BD
        Map<String, Integer> mapeoEventos = new HashMap<>();
        mapeoEventos.put("IG234", 1);
        mapeoEventos.put("MAT101", 2);
        mapeoEventos.put("EVT001", 3);
        // Agrega más mapeos según tus eventos...

        return mapeoEventos.get(codigoEvento);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private boolean validarFormulario() {
        log.info("🔍 Validando formulario...");

        StringBuilder errores = new StringBuilder();

        // ==================== VALIDACIÓN DE CAMPOS OBLIGATORIOS ====================
        if (cbxEstudiante.getValue() == null) {
            errores.append("• El estudiante es obligatorio\n");
        }

        if (cbxAsignatura.getValue() == null) {
            errores.append("• La asignatura es obligatoria\n");
        }

        if (cbxDocente.getValue() == null) {
            errores.append("• El docente es obligatorio\n");
        }

        if (cbxEstado.getValue() == null) {
            errores.append("• El estado es obligatorio\n");
        }

        if (cbxTipoRegistro.getValue() == null) {
            errores.append("• El tipo de registro es obligatorio\n");
        }

        // ==================== MANEJO DE RESULTADO DE VALIDACIÓN ====================
        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación", "Por favor corrija los siguientes errores:\n\n" + errores.toString(), Alert.AlertType.ERROR);
            log.warning("❌ Validación fallida: " + errores.toString().replace("\n", " "));
            return false;
        }

        log.info("✅ Validación exitosa");
        return true;
    }

    private void limpiarFormulario() {
        log.info("🧹 Limpiando formulario...");

        // ==================== LIMPIAR SELECCIONES DE COMBOBOX ====================
        cbxEstudiante.getSelectionModel().clearSelection();
        cbxAsignatura.getSelectionModel().clearSelection();
        cbxDocente.getSelectionModel().clearSelection();
        cbxEstado.getSelectionModel().clearSelection();
        cbxTipoRegistro.getSelectionModel().clearSelection();

        // ==================== LIMPIAR CAMPOS DE TEXTO ====================
        txtObservaciones.clear();
        imgQRAsistencia.setImage(null);

        // ==================== LIMPIAR FILTROS SI EXISTEN ====================
        if (dpFecha != null) {
            dpFecha.setValue(null);
        }

        // ==================== LIMPIAR SELECCIÓN Y RESTAURAR BOTÓN ====================
        limpiarSeleccion();
        log.info("✅ Formulario limpiado correctamente");
    }

    /**
     * Limpia la selección actual de la tabla y restablece el estado del controlador.
     * Cambia el botón de "Actualizar" de vuelta a "Guardar".
     */
    private void limpiarSeleccion() {
        tableView.getSelectionModel().clearSelection();
        asistenciaSeleccionada = null;
        btnGuardar.setText("💾 Guardar Asistencia");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Maneja el cambio de asignatura en el ComboBox.
     * Puede usarse para lógica adicional como auto-seleccionar docentes relacionados.
     * Se ejecuta automáticamente cuando cambia la selección de asignatura.
     */
    @FXML
    private void onAsignaturaChanged() {
        // Lógica opcional: Auto-seleccionar docente relacionado con la asignatura seleccionada
        if (cbxAsignatura.getValue() != null && cbxDocente.getValue() == null) {
            Asignatura asignatura = cbxAsignatura.getValue();

            // Buscar y seleccionar automáticamente el docente relacionado con la asignatura
            if (asignatura.getDocenteId() != null) {
                for (Docente docente : docentesList) {
                    if (docente.getId().equals(asignatura.getDocenteId())) {
                        cbxDocente.setValue(docente);
                        log.info("👨‍🏫 Docente auto-seleccionado: " + docente.getNombreCompleto());
                        break;
                    }
                }
            }
        }
    }

    private void diagnosticarUsuarios() {
        log.info("🔍 DIAGNÓSTICO DE USUARIOS");
        log.info("Usuario logueado: " + (usuarioLogueado != null ? usuarioLogueado.getUsername() : "null"));
        log.info("Rol: " + (usuarioLogueado != null ? usuarioLogueado.getRol() : "null"));
        log.info("Email: " + (usuarioLogueado != null ? usuarioLogueado.getEmail() : "null"));

        log.info("Estudiantes en sistema: " + estudiantesList.size());
        estudiantesList.forEach(e ->
                log.info("Est: " + e.getCodigoEstudiante() + " - " + e.getEmail() + " - " + e.getNombreCompleto())
        );

        log.info("Docentes en sistema: " + docentesList.size());
        docentesList.forEach(d ->
                log.info("Doc: " + d.getCodigoDocente() + " - " + d.getEmail() + " - " + d.getNombreCompleto())
        );
    }

}