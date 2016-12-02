package entities;

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
import dto.SucursalDTO;
import dto.TrayectoDTO;
import hbt.PersistentObject;

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
	
	@OneToMany
	@JoinColumn(name = "idRuta")
	private List<Sucursal> sucursales;

	@Column(name = "precio", columnDefinition = "float", nullable = true)
	private float precio;

	@ManyToOne
	@JoinColumn(name = "idSucursalOrigen", referencedColumnName = "idSucursal")
	private Sucursal sucursalOrigen;

	@ManyToOne
	@JoinColumn(name = "idSucursalDestino", referencedColumnName = "idSucursal")
	private Sucursal sucursalDestino;

	public Ruta() {
		super();
	}
	

	public Ruta(int idRuta, List<Trayecto> trayectos, List<Sucursal> sucursales, float precio, Sucursal sucursalOrigen,
			Sucursal sucursalDestino) {
		super();
		this.idRuta = idRuta;
		this.trayectos = trayectos;
		this.sucursales = sucursales;
		this.precio = precio;
		this.sucursalOrigen = sucursalOrigen;
		this.sucursalDestino = sucursalDestino;
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


	public Ruta(int idRuta, List<Trayecto> trayectos, float precio, List<Sucursal> sucursales,
			Sucursal sucursalOrigen, Sucursal sucursalDestino) {
		super();
		this.idRuta = idRuta;
		this.trayectos = trayectos;
		this.precio = precio;
		this.sucursales = sucursales;
		this.sucursalDestino = sucursalDestino;
		this.sucursalOrigen = sucursalOrigen;
	}

	public Ruta(int idRuta, List<Trayecto> trayectos, float precio) {
		super();
		this.idRuta = idRuta;
		this.trayectos = trayectos;
		this.precio = precio;
	}
	
	public List<Sucursal> getSucursales() {
		return sucursales;
	}

	public void setSucursales(List<Sucursal> sucursales) {
		this.sucursales = sucursales;
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
		if(trayectos!=null){
			for (Trayecto trayecto : trayectos)
				trayectosDTO.add(trayecto.toDTO());
		}
		else{
			trayectosDTO=null;
		}
		List<SucursalDTO> sucursalsDTO = new ArrayList<SucursalDTO>();
		if(sucursales!=null){
			
			for (Sucursal sucursal : sucursales)
				sucursalsDTO.add(sucursal.toDTO());
		}else
			sucursalsDTO=null;
		SucursalDTO so;
		if(sucursalOrigen!=null){
			so=sucursalOrigen.toDTO();
		}else
			so=null;
		SucursalDTO sd;
		if(sucursalDestino!=null)
			sd=sucursalDestino.toDTO();
		else
			sd=null;

		return new RutaDTO(idRuta, trayectosDTO,sucursalsDTO, precio,
				so, sd);
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
	
	public Sucursal getNextSucursal(Sucursal sucursal) {
		for (Trayecto trayecto : trayectos) {
			if (trayecto.getSucursalOrigen().getIdSucursal() ==  sucursal.getIdSucursal()) {
				return trayecto.getSucursalDestino();
			}
		}
		return null;
	}
	
	public float getTiempoRuta() {
		float tiempoRuta = 0;
		for (Trayecto trayecto : this.getTrayectos()) {
			tiempoRuta = trayecto.getTiempo() + tiempoRuta;
		}
		return tiempoRuta;
	}
	
	public long getTiempoPrimerTrayecto() {
		return (long) this.getTrayectos().get(0).getTiempo() * 60000;
	}

}
