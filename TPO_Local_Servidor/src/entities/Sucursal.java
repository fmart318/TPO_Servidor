package entities;

import hbt.PersistentObject;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import dto.SucursalDTO;
import dto.ViajeDTO;

@Entity
@Table(name = "Sucursales")
public class Sucursal extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(nullable = false)
	private int idSucursal;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String nombre;

	@OneToOne
	@JoinColumn(name = "idDireccion")
	private Direccion ubicacion;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "sucursalDestino")
	private List<Viaje> viajes;

	public Sucursal() {

	}

	public Sucursal(int idSucursal, String nombre, Direccion ubicacion,
			List<Viaje> viajes) {
		super();
		this.idSucursal = idSucursal;
		this.nombre = nombre;
		this.ubicacion = ubicacion;
		this.viajes = viajes;
	}

	public int getIdSucursal() {
		return idSucursal;
	}

	public void setIdSucursal(int idSucursal) {
		this.idSucursal = idSucursal;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Direccion getUbicacion() {
		return ubicacion;
	}

	public void setUbicacion(Direccion ubicacion) {
		this.ubicacion = ubicacion;
	}

	public List<Viaje> getViajes() {
		return viajes;
	}

	public void setViajes(List<Viaje> viajes) {
		this.viajes = viajes;
	}

	public SucursalDTO toDTO() {
		List<ViajeDTO> viajesDTO = new ArrayList<ViajeDTO>();
		for (Viaje viaje : viajes)
			viajesDTO.add(viaje.toDTO());
		SucursalDTO sucursalDTO = new SucursalDTO(idSucursal, nombre,
				getUbicacion().toDTO(), viajesDTO);
		return sucursalDTO;
	}

	public SucursalDTO toDTONoRecursivo() {
		SucursalDTO sucursalDTO = new SucursalDTO(idSucursal, nombre,
				getUbicacion().toDTO(), null);
		return sucursalDTO;
	}
}
