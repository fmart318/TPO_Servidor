package entities;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import dto.VehiculoDTO;
import hbt.PersistentObject;

@Entity
@Table(name = "Vehiculos")
public class Vehiculo extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "idVehiculo", columnDefinition = "int", nullable = false)
	private int idVehiculo;

	@Column(name = "tipo", columnDefinition = "varchar(50)", nullable = true)
	private String tipo;

	@Column(name = "volumen", columnDefinition = "float", nullable = true)
	private float volumen;

	@Column(name = "peso", columnDefinition = "float", nullable = true)
	private float peso;

	@Column(name = "ancho", columnDefinition = "float", nullable = true)
	private float ancho;

	@Column(name = "alto", columnDefinition = "float", nullable = true)
	private float alto;

	@Column(name = "profundidad", columnDefinition = "float", nullable = true)
	private float profundidad;

	@Column(name = "tara", columnDefinition = "float", nullable = true)
	private float tara;

	@Column(name = "kilometraje", columnDefinition = "int", nullable = true)
	private int kilometraje;

	@Column(name = "estado", columnDefinition = "varchar(50)", nullable = true)
	private String estado;

	@Column(name = "trabajoEspecifico", columnDefinition = "bit", nullable = true)
	private boolean trabajoEspecifico;

	@Column(name = "especificacion", columnDefinition = "bit", nullable = true)
	private boolean enGarantia;

	@Column(name = "fechaUltimaControl", columnDefinition = "datetime", nullable = true)
	private Date fechaUltimoControl;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idPlanDeMantenimiento")
	private PlanDeMantenimiento planDeMantenimiento;

	@Column(name = "sucursalIdActual", columnDefinition = "int", nullable = true)
	private int sucursalIdActual;

	public Vehiculo() {
		super();
	}
	

	public Vehiculo(int idVehiculo, String tipo, float volumen, float peso, float ancho, float alto, float profundidad,
			float tara, int kilometraje, String estado, boolean trabajoEspecifico, boolean enGarantia,
			Date fechaUltimoControl, PlanDeMantenimiento planDeMantenimiento, int sucursalIdActual) {
		super();
		this.idVehiculo = idVehiculo;
		this.tipo = tipo;
		this.volumen = volumen;
		this.peso = peso;
		this.ancho = ancho;
		this.alto = alto;
		this.profundidad = profundidad;
		this.tara = tara;
		this.kilometraje = kilometraje;
		this.estado = estado;
		this.trabajoEspecifico = trabajoEspecifico;
		this.enGarantia = enGarantia;
		this.fechaUltimoControl = fechaUltimoControl;
		this.planDeMantenimiento = planDeMantenimiento;
		this.sucursalIdActual = sucursalIdActual;
	}


	public int getIdVehiculo() {
		return idVehiculo;
	}

	public void setIdVehiculo(int idVehiculo) {
		this.idVehiculo = idVehiculo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public float getVolumen() {
		return volumen;
	}

	public void setVolumen(float volumen) {
		this.volumen = volumen;
	}

	public float getPeso() {
		return peso;
	}

	public void setPeso(float peso) {
		this.peso = peso;
	}

	public float getAncho() {
		return ancho;
	}

	public void setAncho(float ancho) {
		this.ancho = ancho;
	}

	public float getAlto() {
		return alto;
	}

	public void setAlto(float alto) {
		this.alto = alto;
	}

	public float getProfundidad() {
		return profundidad;
	}

	public void setProfundidad(float profundidad) {
		this.profundidad = profundidad;
	}

	public float getTara() {
		return tara;
	}

	public void setTara(float tara) {
		this.tara = tara;
	}

	public int getKilometraje() {
		return kilometraje;
	}

	public void setKilometraje(int kilometraje) {
		this.kilometraje = kilometraje;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public boolean isTrabajoEspecifico() {
		return trabajoEspecifico;
	}

	public void setTrabajoEspecifico(boolean trabajoEspecifico) {
		this.trabajoEspecifico = trabajoEspecifico;
	}

	public boolean isEnGarantia() {
		return enGarantia;
	}

	public void setEnGarantia(boolean enGarantia) {
		this.enGarantia = enGarantia;
	}

	public Date getFechaUltimoControl() {
		return fechaUltimoControl;
	}

	public void setFechaUltimoControl(Date fechaUltimoControl) {
		this.fechaUltimoControl = fechaUltimoControl;
	}

	public PlanDeMantenimiento getPlanDeMantenimiento() {
		return planDeMantenimiento;
	}

	public void setPlanDeMantenimiento(PlanDeMantenimiento planDeMantenimiento) {
		this.planDeMantenimiento = planDeMantenimiento;
	}

	public int getSucursalIdActual() {
		return sucursalIdActual;
	}

	public void setSucursalIdActual(int sucursalIdActual) {
		this.sucursalIdActual = sucursalIdActual;
	}

	public VehiculoDTO toDTO() {
		if (planDeMantenimiento != null)
			return new VehiculoDTO(idVehiculo, tipo, volumen, peso, ancho, alto, profundidad, tara, kilometraje, estado,
					enGarantia, trabajoEspecifico, fechaUltimoControl, sucursalIdActual, planDeMantenimiento.toDTO());
		else
			return new VehiculoDTO(idVehiculo, tipo, volumen, peso, ancho, alto, profundidad, tara, kilometraje, estado,
					enGarantia, trabajoEspecifico, fechaUltimoControl, sucursalIdActual, null);
	}
}