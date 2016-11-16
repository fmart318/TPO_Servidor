package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import dto.PrecioVehiculoDTO;

@Entity
@Table(name = "PreciosVehiculos")
public class PrecioVehiculo extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "idPrecioVehiculo", columnDefinition = "int", nullable = false)
	private int idPrecioVehiculo;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String tipoVehiculo;

	@Column(name = "precio", nullable = true)
	private float precio;

	public PrecioVehiculo() {
		super();
	}

	public PrecioVehiculo(int idPrecioVehiculo, String tipoVehiculo,
			float precio) {
		super();
		this.idPrecioVehiculo = idPrecioVehiculo;
		this.tipoVehiculo = tipoVehiculo;
		this.precio = precio;
	}

	public int getIdPrecioVehiculo() {
		return idPrecioVehiculo;
	}

	public void setIdPrecioVehiculo(int idPrecioVehiculo) {
		this.idPrecioVehiculo = idPrecioVehiculo;
	}

	public String getTipoVehiculo() {
		return tipoVehiculo;
	}

	public void setTipoVehiculo(String tipoVehiculo) {
		this.tipoVehiculo = tipoVehiculo;
	}

	public float getPrecio() {
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
	}

	public PrecioVehiculoDTO toDTO() {
		return new PrecioVehiculoDTO(idPrecioVehiculo, tipoVehiculo, precio);
	}
}
