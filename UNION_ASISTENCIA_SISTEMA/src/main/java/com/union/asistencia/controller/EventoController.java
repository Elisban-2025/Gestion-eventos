package com.union.asistencia.controller;

import com.union.asistencia.dao.EventoDAO;
import com.union.asistencia.dao.EstudianteDAO;
import com.union.asistencia.dao.DocenteDAO;
import com.union.asistencia.dao.AsistenciaEventoDAO;
import com.union.asistencia.model.Evento;
import com.union.asistencia.model.Estudiante;
import com.union.asistencia.model.Docente;
import com.union.asistencia.model.ParticipanteEvento;
import com.union.asistencia.model.AsistenciaEvento;
import com.union.asistencia.model.Usuario;
import com.union.asistencia.util.ExportUtils;
import com.union.asistencia.util.QRGenerator;
import com.union.asistencia.util.QRService;
import com.union.asistencia.util.QRReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import lombok.extern.java.Log;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Log
public class EventoController extends BaseController {

    @FXML private TableView<Evento> tableView;
    @FXML private TableColumn<Evento, Integer> colId;
    @FXML private TableColumn<Evento, String> colNombre;
    @FXML private TableColumn<Evento, String> colTipo;
    @FXML private TableColumn<Evento, LocalDateTime> colFechaInicio;
    @FXML private TableColumn<Evento, String> colLugar;
    @FXML private TableColumn<Evento, String> colEstado;

    @FXML private TableView<ParticipanteEvento> tablaParticipantes;
    @FXML private TableColumn<ParticipanteEvento, String> colParticipante;
    @FXML private TableColumn<ParticipanteEvento, String> colTipoParticipante;
    @FXML private TableColumn<ParticipanteEvento, Boolean> colAsistio;

    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbxTipo;
    @FXML private TextArea txtDescripcion;
    @FXML private DatePicker dpFechaInicio;
    @FXML private ComboBox<String> cbxHoraInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<String> cbxHoraFin;
    @FXML private TextField txtLugar;
    @FXML private TextField txtResponsable;
    @FXML private Spinner<Integer> spnCapacidad;
    @FXML private CheckBox chkRequiereInscripcion;
    @FXML private ComboBox<Estudiante> cbxEstudiante;
    @FXML private ComboBox<Docente> cbxDocente;
    @FXML private ComboBox<String> cbxTipoParticipante;
    @FXML private TextField txtBuscar;
    @FXML private ImageView imgQREvento;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnInscribir;
    @FXML private Button btnGenerarQR;
    @FXML private Button btnExportar;
    @FXML private Button btnRegistroRapido;
    @FXML private Button btnEliminar;

    private ObservableList<Evento> eventosList;
    private ObservableList<ParticipanteEvento> participantesList;
    private ObservableList<Estudiante> estudiantesList;
    private ObservableList<Docente> docentesList;

    private EventoDAO eventoDAO;
    private EstudianteDAO estudianteDAO;
    private DocenteDAO docenteDAO;
    private AsistenciaEventoDAO asistenciaEventoDAO;

    private Usuario usuarioLogueado;
    private Evento eventoSeleccionado;

    @Override
    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        inicializar();
    }

    @FXML
    private void initialize() {
        System.out.println("✅ Inicializando EventoController");
        try {
            eventoDAO = new EventoDAO();
            estudianteDAO = new EstudianteDAO();
            docenteDAO = new DocenteDAO();
            asistenciaEventoDAO = new AsistenciaEventoDAO();

            eventosList = FXCollections.observableArrayList();
            participantesList = FXCollections.observableArrayList();
            estudiantesList = FXCollections.observableArrayList();
            docentesList = FXCollections.observableArrayList();

            eventoSeleccionado = null;

            configurarTablas();
            configurarCombobox();
            configurarSpinner();
            configurarEventos();

            tableView.setItems(eventosList);
            tablaParticipantes.setItems(participantesList);

            System.out.println("✅ EventoController inicializado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error en initialize de EventoController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void inicializar() {
        System.out.println("✅ Inicializando datos de eventos");
        try {
            cargarDatos();
            limpiarFormulario();
            System.out.println("✅ Datos de eventos cargados correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error cargando datos de eventos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurarTablas() {
        // Configurar tabla de eventos
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaHoraInicio"));
        colLugar.setCellValueFactory(new PropertyValueFactory<>("lugar"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Configurar tabla de participantes
        colParticipante.setCellValueFactory(new PropertyValueFactory<>("nombreParticipante"));
        colTipoParticipante.setCellValueFactory(new PropertyValueFactory<>("tipoParticipante"));
        colAsistio.setCellValueFactory(new PropertyValueFactory<>("asistio"));

        // Formatear columna de asistencia
        colAsistio.setCellFactory(tc -> new TableCell<ParticipanteEvento, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✅ Asistió" : "❌ No Asistió");
                    if (item) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void configurarCombobox() {
        // Tipos de evento
        cbxTipo.getItems().addAll("ACADEMICO", "CULTURAL", "DEPORTIVO", "INSTITUCIONAL");

        // Horas
        ObservableList<String> horas = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 60; j += 30) {
                horas.add(String.format("%02d:%02d", i, j));
            }
        }
        cbxHoraInicio.setItems(horas);
        cbxHoraFin.setItems(horas);

        // Tipos de participante
        cbxTipoParticipante.getItems().addAll("ESTUDIANTE", "DOCENTE", "EXTERNO");

        // Cargar estudiantes y docentes
        estudiantesList.setAll(estudianteDAO.obtenerTodos());
        cbxEstudiante.setItems(estudiantesList);
        cbxEstudiante.setCellFactory(param -> new ListCell<Estudiante>() {
            @Override
            protected void updateItem(Estudiante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto() + " - " + item.getCodigoEstudiante());
            }
        });

        docentesList.setAll(docenteDAO.obtenerTodos());
        cbxDocente.setItems(docentesList);
        cbxDocente.setCellFactory(param -> new ListCell<Docente>() {
            @Override
            protected void updateItem(Docente item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNombreCompleto() + " - " + item.getCodigoDocente());
            }
        });
    }

    private void configurarSpinner() {
        SpinnerValueFactory<Integer> capacidadFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 50);
        spnCapacidad.setValueFactory(capacidadFactory);
    }

    private void configurarEventos() {
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        eventoSeleccionado = newSelection;
                        cargarDatosFormulario(newSelection);
                        cargarParticipantesEvento(newSelection.getId());
                    }
                });

        // Cambio en tipo de participante
        cbxTipoParticipante.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if ("ESTUDIANTE".equals(newVal)) {
                        cbxEstudiante.setDisable(false);
                        cbxDocente.setDisable(true);
                    } else if ("DOCENTE".equals(newVal)) {
                        cbxEstudiante.setDisable(true);
                        cbxDocente.setDisable(false);
                    } else {
                        cbxEstudiante.setDisable(true);
                        cbxDocente.setDisable(true);
                    }
                });
    }

    private void cargarDatos() {
        try {
            List<Evento> eventos = eventoDAO.obtenerTodos();
            System.out.println("📊 Eventos cargados desde BD: " + eventos.size());
            eventosList.setAll(eventos);

            if (eventos.isEmpty()) {
                System.out.println("ℹ️ No hay eventos en la base de datos");
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando eventos desde BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cargarParticipantesEvento(Integer eventoId) {
        participantesList.setAll(eventoDAO.obtenerParticipantesPorEvento(eventoId));
    }

    /**
     * NUEVO MÉTODO: Registro rápido de asistencia por DNI o código de estudiante
     */
    @FXML
    private void handleRegistroRapidoAsistencia() {
        if (eventoSeleccionado == null) {
            mostrarAlerta("Advertencia", "Por favor seleccione un evento primero", Alert.AlertType.WARNING);
            return;
        }

        // Crear diálogo para ingresar DNI/código
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Registro Rápido de Asistencia");
        dialog.setHeaderText("Ingrese DNI o Código del Estudiante");
        dialog.setContentText("DNI/Código:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(dniCodigo -> {
            if (!dniCodigo.trim().isEmpty()) {
                registrarAsistenciaPorDniCodigo(dniCodigo.trim());
            }
        });
    }

    /**
     * NUEVO MÉTODO: Registra asistencia automáticamente por DNI o código
     */
    private void registrarAsistenciaPorDniCodigo(String dniCodigo) {
        try {
            // Buscar estudiante
            Estudiante estudiante = estudianteDAO.buscarPorDniOCodigo(dniCodigo);

            if (estudiante == null) {
                mostrarAlerta("No Encontrado",
                        "❌ No se encontró estudiante con DNI/Código: " + dniCodigo +
                                "\n\nVerifique que el estudiante esté registrado y activo en el sistema.",
                        Alert.AlertType.ERROR);
                return;
            }

            // Verificar si ya está registrado en participantes_evento (TABLA QUE SE MUESTRA)
            if (asistenciaEventoDAO.existeParticipante(estudiante.getId(), eventoSeleccionado.getId())) {
                mostrarAlerta("Ya Registrado",
                        "⚠️ El estudiante ya está registrado para este evento:\n" +
                                "• Estudiante: " + estudiante.getNombreCompleto() + "\n" +
                                "• DNI: " + estudiante.getDni() + "\n" +
                                "• Código: " + estudiante.getCodigoEstudiante() + "\n" +
                                "• Evento: " + eventoSeleccionado.getNombre(),
                        Alert.AlertType.WARNING);
                return;
            }

            // Registrar en participantes_evento (ESTA ES LA TABLA QUE SE MUESTRA)
            boolean exito = asistenciaEventoDAO.registrarAsistenciaParticipante(
                    estudiante.getId(),
                    eventoSeleccionado.getId()
            );

            if (exito) {
                mostrarAlerta("Asistencia Registrada",
                        "✅ Asistencia registrada exitosamente\n\n" +
                                "• Estudiante: " + estudiante.getNombreCompleto() + "\n" +
                                "• DNI: " + estudiante.getDni() + "\n" +
                                "• Código: " + estudiante.getCodigoEstudiante() + "\n" +
                                "• Evento: " + eventoSeleccionado.getNombre() + "\n" +
                                "• Fecha: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        Alert.AlertType.INFORMATION);

                // ✅ ACTUALIZAR TABLA DE PARTICIPANTES - AHORA SÍ APARECERÁ
                cargarParticipantesEvento(eventoSeleccionado.getId());

                // 🔍 DIAGNÓSTICO: Verificar que se cargó correctamente
                diagnosticarParticipantes();

            } else {
                mostrarAlerta("Error",
                        "❌ No se pudo registrar la asistencia\n\n" +
                                "Por favor intente nuevamente.",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            mostrarAlerta("Error",
                    "❌ Error al registrar asistencia: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            log.severe("Error en registro rápido de asistencia: " + e.getMessage());
        }
    }

    @FXML
    private void guardarEvento() {
        if (validarFormularioEvento()) {
            Evento evento;
            boolean esNuevo = false;

            if (eventoSeleccionado == null) {
                evento = new Evento();
                esNuevo = true;
            } else {
                evento = eventoSeleccionado;
            }

            evento.setNombre(txtNombre.getText().trim());
            evento.setTipo(cbxTipo.getValue());
            evento.setDescripcion(txtDescripcion.getText().trim());
            evento.setFechaHoraInicio(LocalDateTime.of(dpFechaInicio.getValue(),
                    LocalTime.parse(cbxHoraInicio.getValue())));
            evento.setFechaHoraFin(LocalDateTime.of(dpFechaFin.getValue(),
                    LocalTime.parse(cbxHoraFin.getValue())));
            evento.setLugar(txtLugar.getText().trim());
            evento.setResponsable(txtResponsable.getText().trim());
            evento.setCapacidadMaxima(spnCapacidad.getValue());
            evento.setRequiereInscripcion(chkRequiereInscripcion.isSelected());
            evento.setActivo(true);

            boolean exito;
            if (esNuevo) {
                exito = eventoDAO.guardar(evento);
            } else {
                exito = eventoDAO.actualizar(evento);
            }

            if (exito) {
                mostrarAlerta("Éxito",
                        esNuevo ? "Evento registrado correctamente" : "Evento actualizado correctamente",
                        Alert.AlertType.INFORMATION);
                limpiarFormulario();
                cargarDatos();
            } else {
                mostrarAlerta("Error",
                        esNuevo ? "No se pudo registrar el evento" : "No se pudo actualizar el evento",
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void inscribirParticipante() {
        if (eventoSeleccionado == null) {
            mostrarAlerta("Advertencia", "Seleccione un evento para inscribir participantes", Alert.AlertType.WARNING);
            return;
        }

        if (validarFormularioInscripcion()) {
            ParticipanteEvento participante = new ParticipanteEvento();
            participante.setEventoId(eventoSeleccionado.getId());
            participante.setTipoParticipante(cbxTipoParticipante.getValue());

            if ("ESTUDIANTE".equals(cbxTipoParticipante.getValue())) {
                participante.setEstudianteId(cbxEstudiante.getValue().getId());
            } else if ("DOCENTE".equals(cbxTipoParticipante.getValue())) {
                participante.setDocenteId(cbxDocente.getValue().getId());
            }

            // Generar código QR
            String codigoQR = QRGenerator.generarCodigoEvento(
                    eventoSeleccionado.getId().toString(),
                    participante.getEstudianteId() != null ? participante.getEstudianteId().toString() :
                            participante.getDocenteId() != null ? participante.getDocenteId().toString() : "EXTERNO"
            );
            participante.setCodigoQR(codigoQR);

            if (eventoDAO.inscribirParticipante(participante)) {
                mostrarAlerta("Éxito", "Participante inscrito correctamente", Alert.AlertType.INFORMATION);
                limpiarFormularioInscripcion();
                cargarParticipantesEvento(eventoSeleccionado.getId());
            } else {
                mostrarAlerta("Error", "No se pudo inscribir al participante", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void generarQREvento() {
        if (eventoSeleccionado != null) {
            String qrData = "Evento: " + eventoSeleccionado.getNombre() +
                    "\nFecha: " + eventoSeleccionado.getFechaHoraInicio() +
                    "\nLugar: " + eventoSeleccionado.getLugar();
            var qrImage = QRGenerator.generarQRCode(qrData, 200, 200);
            if (qrImage != null) {
                imgQREvento.setImage(qrImage);
            } else {
                mostrarAlerta("Error", "No se pudo generar el código QR", Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione un evento para generar QR", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void eliminarEvento() {
        if (eventoSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("¿Está seguro de eliminar el evento?");
            alert.setContentText("Esta acción no se puede deshacer.");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (eventoDAO.eliminar(eventoSeleccionado.getId())) {
                    mostrarAlerta("Éxito", "Evento eliminado correctamente", Alert.AlertType.INFORMATION);
                    limpiarFormulario();
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el evento", Alert.AlertType.ERROR);
                }
            }
        } else {
            mostrarAlerta("Advertencia", "Seleccione un evento para eliminar", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void cancelar() {
        limpiarFormulario();
    }

    @FXML
    private void buscarEventos() {
        String criterio = txtBuscar.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatos();
        } else {
            // Crear una nueva lista filtrada de manera segura
            ObservableList<Evento> listaFiltrada = FXCollections.observableArrayList();

            for (Evento e : eventosList) {
                if (e.getNombre().toLowerCase().contains(criterio.toLowerCase()) ||
                        e.getTipo().toLowerCase().contains(criterio.toLowerCase()) ||
                        e.getLugar().toLowerCase().contains(criterio.toLowerCase())) {
                    listaFiltrada.add(e);
                }
            }

            // Actualizar la tabla de manera segura
            tableView.getItems().setAll(listaFiltrada);
        }
    }

    @FXML
    private void exportarDatos() {
        if (eventosList.isEmpty()) {
            mostrarAlerta("Advertencia", "No hay datos para exportar", Alert.AlertType.WARNING);
            return;
        }
        ExportUtils.exportarTablaACSV(tableView, "Reporte_Eventos_UPeU");
    }

    private boolean validarFormularioEvento() {
        StringBuilder errores = new StringBuilder();

        if (txtNombre.getText().trim().isEmpty()) {
            errores.append("• El nombre del evento es obligatorio\n");
        }

        if (cbxTipo.getValue() == null) {
            errores.append("• El tipo de evento es obligatorio\n");
        }

        if (dpFechaInicio.getValue() == null) {
            errores.append("• La fecha de inicio es obligatoria\n");
        }

        if (cbxHoraInicio.getValue() == null) {
            errores.append("• La hora de inicio es obligatoria\n");
        }

        if (dpFechaFin.getValue() == null) {
            errores.append("• La fecha de fin es obligatoria\n");
        }

        if (cbxHoraFin.getValue() == null) {
            errores.append("• La hora de fin es obligatoria\n");
        }

        if (txtLugar.getText().trim().isEmpty()) {
            errores.append("• El lugar es obligatorio\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación", "Por favor corrija los siguientes errores:\n\n" + errores.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean validarFormularioInscripcion() {
        StringBuilder errores = new StringBuilder();

        if (cbxTipoParticipante.getValue() == null) {
            errores.append("• El tipo de participante es obligatorio\n");
        }

        if ("ESTUDIANTE".equals(cbxTipoParticipante.getValue()) && cbxEstudiante.getValue() == null) {
            errores.append("• Debe seleccionar un estudiante\n");
        }

        if ("DOCENTE".equals(cbxTipoParticipante.getValue()) && cbxDocente.getValue() == null) {
            errores.append("• Debe seleccionar un docente\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Error de Validación", "Por favor corrija los siguientes errores:\n\n" + errores.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void cargarDatosFormulario(Evento evento) {
        txtNombre.setText(evento.getNombre());
        cbxTipo.setValue(evento.getTipo());
        txtDescripcion.setText(evento.getDescripcion());
        dpFechaInicio.setValue(evento.getFechaHoraInicio().toLocalDate());
        cbxHoraInicio.setValue(evento.getFechaHoraInicio().toLocalTime().toString());
        dpFechaFin.setValue(evento.getFechaHoraFin().toLocalDate());
        cbxHoraFin.setValue(evento.getFechaHoraFin().toLocalTime().toString());
        txtLugar.setText(evento.getLugar());
        txtResponsable.setText(evento.getResponsable());
        if (evento.getCapacidadMaxima() != null) {
            spnCapacidad.getValueFactory().setValue(evento.getCapacidadMaxima());
        }
        chkRequiereInscripcion.setSelected(evento.getRequiereInscripcion());

        btnGuardar.setText("💾 Actualizar Evento");
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        cbxTipo.getSelectionModel().clearSelection();
        txtDescripcion.clear();
        dpFechaInicio.setValue(null);
        cbxHoraInicio.getSelectionModel().clearSelection();
        dpFechaFin.setValue(null);
        cbxHoraFin.getSelectionModel().clearSelection();
        txtLugar.clear();
        txtResponsable.clear();
        if (spnCapacidad.getValueFactory() != null) {
            spnCapacidad.getValueFactory().setValue(50);
        }
        chkRequiereInscripcion.setSelected(false);
        txtBuscar.clear();
        imgQREvento.setImage(null);

        limpiarFormularioInscripcion();
        limpiarSeleccion();
    }

    private void limpiarFormularioInscripcion() {
        cbxTipoParticipante.getSelectionModel().clearSelection();
        cbxEstudiante.getSelectionModel().clearSelection();
        cbxDocente.getSelectionModel().clearSelection();
        cbxEstudiante.setDisable(true);
        cbxDocente.setDisable(true);
    }

    private void limpiarSeleccion() {
        tableView.getSelectionModel().clearSelection();
        eventoSeleccionado = null;
        participantesList.clear();
        btnGuardar.setText("💾 Guardar Evento");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


    private void diagnosticarParticipantes() {
        if (eventoSeleccionado != null) {
            System.out.println("🔍 DIAGNÓSTICO - Evento ID: " + eventoSeleccionado.getId());

            // Verificar participantes directamente desde la BD
            try {
                List<ParticipanteEvento> participantes = eventoDAO.obtenerParticipantesPorEvento(eventoSeleccionado.getId());
                System.out.println("👥 Participantes en eventoDAO: " + participantes.size());

                for (ParticipanteEvento p : participantes) {
                    System.out.println("   - " + p.getNombreParticipante() + " (" + p.getTipoParticipante() + ") - Asistió: " + p.getAsistio());
                }

                // Verificar asistencias en asistencias_eventos
                List<AsistenciaEvento> asistencias = asistenciaEventoDAO.obtenerAsistenciasPorEvento(eventoSeleccionado.getId());
                System.out.println("📊 Asistencias en asistencias_eventos: " + asistencias.size());

            } catch (Exception e) {
                System.err.println("❌ Error en diagnóstico: " + e.getMessage());
            }
        } else {
            System.out.println("❌ No hay evento seleccionado para diagnóstico");
        }
    }
}