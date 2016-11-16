package entities;

import hbt.PersistentObject;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import dto.EnvioDTO;

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

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "idPedido")
	private Pedido pedido;

	@Column(nullable = true)
	private int prioridad;

	@Column(columnDefinition = "varchar(40)", nullable = true)
	private String sucursalOrigen;

	public String getSucursalOrigen() {
		return sucursalOrigen;
	}

	public void setSucursalOrigen(String sucursalOrigen) {
		this.sucursalOrigen = sucursalOrigen;
	}

	public Envio(int idEnvio, Date fechaSalida, Date fechaLlegada,
			boolean cumpleCondicionesCarga, String estado, Pedido pedido,
			int prioridad) {
		super();
		this.idEnvio = idEnvio;
		this.fechaSalida = fechaSalida;
		this.fechaLlegada = fechaLlegada;
		this.cumpleCondicionesCarga = cumpleCondicionesCarga;
		this.estado = estado;
		this.pedido = pedido;
		this.prioridad = prioridad;

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

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public int getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(int prioridad) {
		this.prioridad = prioridad;
	}

	public EnvioDTO toDTO() {

		EnvioDTO envioDTO = new EnvioDTO(idEnvio, fechaSalida, fechaLlegada,
				cumpleCondicionesCarga, estado, pedido.toDTO(), prioridad);
		envioDTO.setSucursalOrigen(sucursalOrigen);
		return envioDTO;
	}
}
