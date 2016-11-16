package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import dto.TrayectoDTO;

@Entity
@Table(name = "Trayectos")
public class Trayecto extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "idTrayecto", columnDefinition = "int", nullable = false)
	private int idTrayecto;

	@ManyToOne
	@JoinColumn(name = "idSucursalOrigen", referencedColumnName = "idSucursal")
	private Sucursal sucursalOrigen;

	@ManyToOne
	@JoinColumn(name = "idSucursalDestino", referencedColumnName = "idSucursal")
	Sucursal sucursalDestino;

	@Column(name = "tiempo", nullable = true)
	private float tiempo;

	@Column(name = "km", columnDefinition = "int", nullable = true)
	private int km;

	@Column(name = "precio", columnDefinition = "float", nullable = true)
	private float precio;

	public Trayecto() {

	}

	public Trayecto(int idTrayecto, Sucursal sucursalOrigen,
			Sucursal sucursalDestino, float tiempo, int km, float precio) {
		super();
		this.idTrayecto = idTrayecto;
		this.sucursalOrigen = sucursalOrigen;
		this.sucursalDestino = sucursalDestino;
		this.tiempo = tiempo;
		this.km = km;
		this.precio = precio;
	}

	public int getIdTrayecto() {
		return idTrayecto;
	}

	public void setIdTrayecto(int idTrayecto) {
		this.idTrayecto = idTrayecto;
	}

	public Sucursal getSucursalOrigen() {
		return sucursalOrigen;
	}

	public void setSucursalOrigen(Sucursal sucursalOrigen) {
		this.sucursalOrigen = sucursalOrigen;
	}

	public Sucursal getSucursalDestino() {
		return sucursalDestino;
	}

	public void setSucursalDestino(Sucursal sucursalDestino) {
		this.sucursalDestino = sucursalDestino;
	}

	public float getTiempo() {
		return tiempo;
	}

	public void setTiempo(float tiempo) {
		this.tiempo = tiempo;
	}

	public int getKm() {
		return km;
	}

	public void setKm(int km) {
		this.km = km;
	}

	public float getPrecio() {
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
	}

	public TrayectoDTO toDTO() {
		return new TrayectoDTO(idTrayecto, getSucursalOrigen().toDTO(),
				getSucursalDestino().toDTO(), tiempo, km, precio);
	}
}
