package entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import dto.EnvioDTO;
import dto.PedidoDTO;
import hbt.PersistentObject;

@Entity
@Table(name = "Envios")
public class Envio extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(nullable = false)
	private int idEnvio;

	@Column(columnDefinition = "datetime", nullable = true)
	private Date fechaSalida;

	@Column(columnDefinition = "datetime", nullable = true)
	private Date fechaLlegada;

	@Column(columnDefinition = "bit", nullable = true)
	private boolean cumpleCondicionesCarga;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String estado;

	@OneToMany
	@JoinColumn(name = "idEnvio", nullable = true, updatable = true, insertable = true)
	private List<Pedido> pedidos;

	@Column(nullable = true)
	private int prioridad;
	
	@Column(columnDefinition = "int", nullable = true)
	private int idVehiculo;

	@ManyToOne
	@JoinColumn(name = "idSucursalOrigen", referencedColumnName = "idSucursal")
	private Sucursal sucursalOrigen;

	@ManyToOne
	@JoinColumn(name = "idSucursalDestino", referencedColumnName = "idSucursal")
	private Sucursal sucursalDestino;

	public Envio(int idEnvio, Date fechaSalida, Date fechaLlegada,
			boolean cumpleCondicionesCarga, String estado, List<Pedido> pedidos,
			int prioridad, Sucursal sucursalOrigen, Sucursal sucursalDestino, int idVehiculo) {
		super();
		this.idEnvio = idEnvio;
		this.fechaSalida = fechaSalida;
		this.fechaLlegada = fechaLlegada;
		this.cumpleCondicionesCarga = cumpleCondicionesCarga;
		this.estado = estado;
		this.pedidos = pedidos;
		this.prioridad = prioridad;
		this.sucursalOrigen = sucursalOrigen;
		this.sucursalDestino = sucursalDestino;
		this.idVehiculo = idVehiculo;
	}

	public Envio() {
		// TODO Auto-generated constructor stub
	}

	public int getIdEnvio() {
		return idEnvio;
	}

	public void setIdEnvio(int idEnvio) {
		this.idEnvio = idEnvio;
	}

	public Date getFechaSalida() {
		return fechaSalida;
	}

	public void setFechaSalida(Date fechaSalida) {
		this.fechaSalida = fechaSalida;
	}

	public Date getFechaLlegada() {
		return fechaLlegada;
	}

	public void setFechaLlegada(Date fechaLlegada) {
		this.fechaLlegada = fechaLlegada;
	}

	public boolean isCumpleCondicionesCarga() {
		return cumpleCondicionesCarga;
	}

	public void setCumpleCondicionesCarga(boolean cumpleCondicionesCarga) {
		this.cumpleCondicionesCarga = cumpleCondicionesCarga;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public int getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(int prioridad) {
		this.prioridad = prioridad;
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
	
	public List<Pedido> getPedidos() {
		return pedidos;
	}

	public void setPedidos(List<Pedido> pedidos) {
		this.pedidos = pedidos;
	}

	public int getIdVehiculo() {
		return idVehiculo;
	}

	public void setIdVehiculo(int idVehiculo) {
		this.idVehiculo = idVehiculo;
	}
	
	public EnvioDTO toDTO() {
		
		List<PedidoDTO> pedidoDtos = new ArrayList<PedidoDTO>();
		for (Pedido pedido : pedidos) {
			pedidoDtos.add(pedido.toDTO());
		}
		
		EnvioDTO envioDTO = new EnvioDTO(idEnvio, fechaSalida, fechaLlegada,
				cumpleCondicionesCarga, estado, pedidoDtos, prioridad, sucursalOrigen.toDTO(), sucursalDestino.toDTO(), idVehiculo);
		return envioDTO;
	}
}
