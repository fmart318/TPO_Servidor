package entities;

import java.util.Calendar;
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
	
	public boolean isInSucursal(int sucursalId) {
		return this.getSucursalIdActual() == sucursalId;
	}
	
	public void setEnUso() {
		this.setEstado("En Uso");
		this.setSucursalIdActual(-1);
	}
	
	public void setEstadoLibre() {
		this.setEstado("Libre");
	}
	
	public boolean isEnUso() {
		return this.getEstado().equals("En Uso");
	}
	
	public boolean isLibre() {
		return this.getEstado().equals("Libre");
	}
	
	public boolean isEnDeposito() {
		return this.getEstado().equals("En Deposito");
	}
	
	public float getMinimoVolumenAceptado() {
		return (this.getVolumen() * 70) / 100;
	}
	
	public boolean necesitaMantenimiento() {
		return this.getKilometraje() >= this.getPlanDeMantenimiento().getKmProxControl();
	}
	
	public boolean hayQueMantener() {
		PlanDeMantenimiento plan = this.getPlanDeMantenimiento();
		long time = Calendar.getInstance().getTimeInMillis();
		long planTime;
		if (this.getFechaUltimoControl() != null) {
			planTime = plan.getDiasProxControl() + this.getFechaUltimoControl().getTime();
		} else {
			planTime = time + 1;
		}
		if (this.getKilometraje() % 10000 == 0 || this.getKilometraje() >= plan.getKmProxControl()
				|| time >= planTime) {
			return true;
		}
		return false;
	}
	
	public String getTipoTrabajo() {
		String tipo;
		if (this.isEnGarantia()) {
			tipo = "En Garantia: Llevar a la agencia oficial";
		} else if (this.isTrabajoEspecifico()) {
			tipo = "Trabajo Especifico: Llevar a taller";
		} else {
			tipo = "Trabajo General: Llevar a lubricentro";
		}
		return tipo;
	}
	
	public boolean alcanzaLugar(float volumen) {
		return this.getVolumen() > volumen;
	}
	
	public void setDiasProximoControl(int diasProxControl) {
		this.getPlanDeMantenimiento().setDiasProxControl(diasProxControl);
	}
	
	public void setKmProximoControl(int kmProximoControl) {
		this.getPlanDeMantenimiento().setKmProxControl(kmProximoControl);
	}
	
	public void setEstadoMantenimientoEspecifico() {
		this.setEstado("En mantenimiento por trabajo especifico.");
	}
	
	public void setEstadoMantenimientoGarantia() {
		this.setEstado("En mantenimiento por garantia.");
	}
	
	public void setEstadoMantenimientoGeneral() {
		this.setEstado("En mantenimiento general.");
	}
	
	public void setLlegoADestino(int sucursalDestino) {
		this.setSucursalIdActual(sucursalDestino);
		this.setEstadoLibre();
	}
}