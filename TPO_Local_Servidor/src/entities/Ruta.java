package entities;

import hbt.PersistentObject;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import dto.RutaDTO;
import dto.TrayectoDTO;

@Entity
@Table(name = "Rutas")
public class Ruta extends PersistentObject {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue
	@Column(name = "idRuta", columnDefinition = "int", nullable = false)
	private int idRuta;
	@OneToMany
	@JoinColumn(name = "idRuta")
	private List<Trayecto> trayectos;

	@Column(name = "precio", columnDefinition = "float", nullable = true)
	private float precio;

	@ManyToOne
	@JoinColumn(name = "idSucursalOrigen", referencedColumnName = "idSucursal")
	private Sucursal sucursalOrigen;

	@ManyToOne
	@JoinColumn(name = "idSucursalDestino", referencedColumnName = "idSucursal")
	Sucursal sucursalDestino;

	public Ruta() {
		super();
	}

	public Ruta(int idRuta, List<Trayecto> trayectos, float precio,
			Sucursal sucursalOrigen, Sucursal sucursalDestino) {
		super();
		this.idRuta = idRuta;
		this.trayectos = trayectos;
		this.precio = precio;
		this.sucursalDestino = sucursalDestino;
		this.sucursalOrigen = sucursalOrigen;

	}

	public Ruta(int idRuta, List<Trayecto> trayectos, float precio) {
		super();
		this.idRuta = idRuta;
		this.trayectos = trayectos;
		this.precio = precio;

	}

	public float getPrecio() {
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
	}

	public int getIdRuta() {
		return idRuta;
	}

	public void setIdRuta(int idRuta) {
		this.idRuta = idRuta;
	}

	public List<Trayecto> getTrayectos() {
		return trayectos;
	}

	public void setTrayectos(List<Trayecto> trayectos) {
		this.trayectos = trayectos;
	}

	public RutaDTO toDTO() {
		List<TrayectoDTO> trayectosDTO = new ArrayList<TrayectoDTO>();
		for (Trayecto trayecto : trayectos)
			trayectosDTO.add(trayecto.toDTO());
		return new RutaDTO(idRuta, trayectosDTO, precio,
				sucursalOrigen.toDTO(), sucursalDestino.toDTO());
	}

	public int calcularKm() {
		int km = 0;
		for (Trayecto t : trayectos) {
			km = t.getKm() + km;
		}
		return km;
	}

	public Sucursal getOrigen() {
		return sucursalOrigen;
	}

	public Sucursal getDestino() {
		return sucursalDestino;
	}
}
