package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import dto.PlanDeMantenimientoDTO;

@Entity
@Table(name = "PlanesDeMantenimiento")
public class PlanDeMantenimiento extends PersistentObject {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue
	@Column(name = "idPlanDeMantenimiento", columnDefinition = "int", nullable = false)
	private int idPlanDeMantenimiento;
	@Column(name = "diasProxControl", columnDefinition = "int", nullable = true)
	private int diasProxControl;
	@Column(name = "diasDemora", columnDefinition = "int", nullable = true)
	private int diasDemora;
	@Column(name = "kmProxControl", columnDefinition = "int", nullable = true)
	private int kmProxControl;
	@Column(name = "puntoControlar", columnDefinition = "varchar")
	private String puntoAControlar;
	@Column(name = "tarea", columnDefinition = "varchar")
	private String tareas;

	public PlanDeMantenimiento() {
		super();
	}

	public PlanDeMantenimiento(int idPlanDeMantenimiento, int diasProxControl,
			int diasDemora, int kmProxControl) {
		super();
		this.idPlanDeMantenimiento = idPlanDeMantenimiento;
		this.diasProxControl = diasProxControl;
		this.diasDemora = diasDemora;
		this.kmProxControl = kmProxControl;
	}

	public int getIdPlanDeMantenimiento() {
		return idPlanDeMantenimiento;
	}

	public void setIdPlanDeMantenimiento(int idPlanDeMantenimiento) {
		this.idPlanDeMantenimiento = idPlanDeMantenimiento;
	}

	public int getDiasProxControl() {
		return diasProxControl;
	}

	public void setDiasProxControl(int diasProxControl) {
		this.diasProxControl = diasProxControl;
	}

	public int getDiasDemora() {
		return diasDemora;
	}

	public void setDiasDemora(int diasDemora) {
		this.diasDemora = diasDemora;
	}

	public int getKmProxControl() {
		return kmProxControl;
	}

	public void setKmProxControl(int kmProxControl) {
		this.kmProxControl = kmProxControl;
	}

	public String getPuntoAControlar() {
		return puntoAControlar;
	}

	public void setPuntoAControlar(String puntoAControlar) {
		this.puntoAControlar = puntoAControlar;
	}

	public String getTareas() {
		return tareas;
	}

	public void setTareas(String tareas) {
		this.tareas = tareas;
	}

	public PlanDeMantenimientoDTO toDTO() {
		return new PlanDeMantenimientoDTO(idPlanDeMantenimiento,
				diasProxControl, diasDemora, kmProxControl);
	}
}
